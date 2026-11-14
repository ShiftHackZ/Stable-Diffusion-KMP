package com.shifthackz.aisdv1.feature.openaioauth.auth

import com.shifthackz.aisdv1.feature.openaioauth.api.ImageApi
import com.shifthackz.aisdv1.feature.openaioauth.api.OAuthApi
import com.shifthackz.aisdv1.feature.openaioauth.model.DeviceLoginPrompt
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthFailure
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthIdentity
import com.shifthackz.aisdv1.feature.openaioauth.model.StoredSession
import com.shifthackz.aisdv1.feature.openaioauth.model.TokenResponse
import com.shifthackz.aisdv1.feature.openaioauth.storage.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.coroutines.coroutineContext
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Owns device login, protected credentials and refresh rotation independently from application models.
 * Login/logout change the identity revision; token refresh does not. Credential-bearing exceptions
 * and HTTP bodies are discarded before crossing the public boundary.
 * @param auth Fixed-origin device and token API.
 * @param images Fixed-origin image API.
 * @param store Atomic protected platform storage.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalUuidApi::class)
class OAuthSessionManager internal constructor(
    private val auth: OAuthApi,
    private val images: ImageApi,
    private val store: SessionStore,
) {
    private val stateLock = Mutex()
    private val refreshLock = Mutex()
    private val mutableIdentity = MutableStateFlow<OAuthIdentity?>(null)
    private var session: StoredSession? = null
    private var loaded = false
    private var mutation = 0L
    private val json = Json { ignoreUnknownKeys = true }

    /** Safe observable account identity; call currentIdentity once to initialize protected storage.
     * @author Dmitriy Moroz */
    val identity: StateFlow<OAuthIdentity?> = mutableIdentity.asStateFlow()

    /** Loads the protected session without exposing credentials. @author Dmitriy Moroz */
    suspend fun currentIdentity(): OAuthIdentity? = safeCall { stateLock.withLock { load(); session?.identity() } }

    /**
     * Requests a transient code, polls for at most fifteen minutes and commits a complete session.
     * Cancellation or failure leaves the prior session intact. Concurrent logout/new login invalidates
     * an earlier attempt before it can publish credentials.
     * @param onPrompt Receives only the user code and fixed trusted verification URL.
     * @author Dmitriy Moroz
     */
    suspend fun login(onPrompt: suspend (DeviceLoginPrompt) -> Unit): OAuthIdentity = safeCall {
        val ticket = stateLock.withLock {
            try { load() } catch (error: OAuthFailure) {
                // Explicit sign-in may replace an unreadable envelope only after a complete grant.
                if (error.reason != OAuthFailure.Reason.STORAGE) throw error
            }
            ++mutation
        }
        withTimeoutOrNull(15 * 60_000L) {
            val code = auth.requestCode()
            val userCode = (code.userCode ?: code.usercode).orEmpty()
            if (code.deviceAuthId.isBlank() || userCode.isBlank()) throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
            onPrompt(DeviceLoginPrompt(userCode, "${OAuthApi.AUTH}/codex/device"))
            val interval = (code.interval as? JsonPrimitive)?.content?.trim()?.toLongOrNull()?.coerceIn(1, 900) ?: 5
            var result = auth.poll(code)
            while (result == null) {
                delay(interval * 1000)
                result = auth.poll(code)
            }
            val tokens = auth.exchange(result)
            coroutineContext.ensureActive()
            val candidate = tokens.toSession(revision = Uuid.random().toString())
            stateLock.withLock {
                coroutineContext.ensureActive()
                if (ticket != mutation) throw OAuthFailure(OAuthFailure.Reason.IDENTITY_CHANGED)
                replace(candidate)
                candidate.identity()
            }
        } ?: throw OAuthFailure(OAuthFailure.Reason.LOGIN_EXPIRED)
    }

    /** Invalidates in-flight login/refresh and atomically removes protected credentials.
     * @author Dmitriy Moroz */
    suspend fun logout() = safeCall {
        stateLock.withLock {
            mutation++
            replace(null)
        }
    }

    /**
     * Generates under an immutable login identity; retries only one confirmed HTTP 401 after refresh.
     * Network/timeout failures never replay an ambiguous image POST.
     * @param identity Identity captured when work was submitted.
     * @param prompt User's image description.
     * @param width Requested positive pixel width.
     * @param height Requested positive pixel height.
     * @author Dmitriy Moroz
     */
    suspend fun generate(identity: OAuthIdentity, prompt: String, width: Int, height: Int): String = safeCall {
        val credentials = freshSession(identity)
        val image = try {
            images.generate(credentials, prompt, width, height)
        } catch (error: OAuthFailure) {
            if (error.statusCode != 401) throw error
            val refreshed = freshSession(identity, rejectedAccessToken = credentials.accessToken)
            images.generate(refreshed, prompt, width, height)
        }
        stateLock.withLock { requireIdentity(identity) }
        image
    }

    /**
     * Serializes a final persistence action against logout/account replacement.
     * @param identity Identity of the generated image.
     * @param block Application persistence action; must not call session operations recursively.
     * @author Dmitriy Moroz
     */
    suspend fun <T> withIdentity(identity: OAuthIdentity, block: suspend () -> T): T = stateLock.withLock {
        requireIdentity(identity)
        coroutineContext.ensureActive()
        block()
    }

    /**
     * Serializes token rotation while allowing logout to invalidate a network operation immediately.
     * @param identity Submitted login identity.
     * @param rejectedAccessToken Exact token rejected with 401; a concurrent successful refresh is reused.
     * @author Dmitriy Moroz
     */
    private suspend fun freshSession(identity: OAuthIdentity, rejectedAccessToken: String? = null): StoredSession = refreshLock.withLock {
        val previous = stateLock.withLock { load(); requireIdentity(identity) }
        val now = Clock.System.now().toEpochMilliseconds()
        if ((rejectedAccessToken == null && previous.expiresAtMillis > now + 60_000) ||
            (rejectedAccessToken != null && previous.accessToken != rejectedAccessToken)) return@withLock previous
        val tokens = try {
            auth.refresh(previous.refreshToken)
        } catch (error: OAuthFailure) {
            if (error.reason == OAuthFailure.Reason.LOGIN_REQUIRED) stateLock.withLock {
                if (session?.revision == identity.revision) replace(null)
            }
            throw error
        }
        val next = tokens.toSession(previous.revision, previous)
        withContext(NonCancellable) {
            stateLock.withLock {
                requireIdentity(identity)
                if (next.accountId != identity.accountId) throw OAuthFailure(OAuthFailure.Reason.IDENTITY_CHANGED)
                replace(next)
            }
        }
        next
    }

    /**
     * Initializes the session and its observable identity from one complete protected envelope.
     * Read/decode failures leave initialization retryable without publishing a partial session.
     * @throws OAuthFailure When the protected envelope cannot be read or decoded.
     * @author Dmitriy Moroz
     */
    private fun load() {
        if (loaded) return
        session = try { store.read()?.let {
            json.decodeFromString<StoredSession>(it).also { value ->
                check(value.accessToken.isNotBlank() && value.refreshToken.isNotBlank() &&
                    value.accountId.isNotBlank() && value.revision.isNotBlank() && value.expiresAtMillis > 0L)
            }
        } } catch (_: Exception) {
            throw OAuthFailure(OAuthFailure.Reason.STORAGE)
        }
        loaded = true
        mutableIdentity.value = session?.identity()
    }

    /**
     * Publishes the in-memory identity only after atomic protected storage succeeds.
     * @param value Complete replacement envelope, or null for logout.
     * @author Dmitriy Moroz
     */
    private fun replace(value: StoredSession?) {
        try { store.replace(value?.let { json.encodeToString(it) }) } catch (_: Exception) {
            throw OAuthFailure(OAuthFailure.Reason.STORAGE)
        }
        session = value
        loaded = true
        mutableIdentity.value = value?.identity()
    }

    /**
     * Rejects work submitted before logout or account replacement.
     * @param identity Original token-free account/revision pair.
     * @author Dmitriy Moroz
     */
    private fun requireIdentity(identity: OAuthIdentity): StoredSession = session
        ?.takeIf { it.accountId == identity.accountId && it.revision == identity.revision }
        ?: throw OAuthFailure(if (session == null) OAuthFailure.Reason.LOGIN_REQUIRED else OAuthFailure.Reason.IDENTITY_CHANGED)

    /**
     * Normalizes a completed grant; JWT claims supply identity/expiry, not signature validation.
     * Tokens are trusted only as responses from the fixed TLS authentication endpoint.
     * @param revision Login identity retained across ordinary refresh.
     * @param previous Existing envelope used only for optional refresh response fields.
     * @author Dmitriy Moroz
     */
    private fun TokenResponse.toSession(revision: String, previous: StoredSession? = null): StoredSession {
        val tokenClaims = listOfNotNull(idToken, accessToken).mapNotNull(::claims)
        val accountId = tokenClaims.firstNotNullOfOrNull {
            it["https://api.openai.com/auth"]?.jsonObject?.get("chatgpt_account_id")?.jsonPrimitive?.contentOrNull
        } ?: previous?.accountId ?: throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
        val expiry = claims(accessToken)?.get("exp")?.jsonPrimitive?.longOrNull?.times(1000)
            ?: expiresIn?.takeIf { it > 0 }?.let { Clock.System.now().toEpochMilliseconds() + it * 1000 }
            ?: throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
        val refresh = refreshToken ?: previous?.refreshToken ?: throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
        if (accountId.isBlank() || refresh.isBlank() || accessToken.isBlank()) throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
        return StoredSession(accessToken, refresh, accountId, revision, expiry)
    }

    /**
     * Reads only account and expiry claims without retaining or logging an identity token.
     * @param token JWT received from the fixed authentication origin.
     * @author Dmitriy Moroz
     */
    private fun claims(token: String): JsonObject? = try {
        val encoded = token.split('.').getOrNull(1) ?: return null
        json.parseToJsonElement(Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(encoded).decodeToString()).jsonObject
    } catch (_: Exception) { null }

    /**
     * Strips response-bearing exception causes at the feature boundary and preserves cancellation.
     * @param block Credential-sensitive operation.
     * @author Dmitriy Moroz
     */
    private suspend fun <T> safeCall(block: suspend () -> T): T = try { block() }
    catch (error: CancellationException) { throw error }
    catch (error: OAuthFailure) { throw error }
    catch (_: Exception) { throw OAuthFailure(OAuthFailure.Reason.NETWORK) }
}

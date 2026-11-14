package com.shifthackz.aisdv1.domain.feature.openaioauth

import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import kotlinx.coroutines.flow.Flow

/**
 * App-facing device-code login and protected session lifecycle.
 * Credentials stay entirely inside the bundled feature.
 * @author Dmitriy Moroz
 */
interface OpenAiOAuthGateway {
    /**
     * Observes token-free login/logout changes without reading protected storage.
     * Emits null before initialization; call [currentIdentity] to load a persisted session.
     * Ordinary access-token refresh retains the same identity revision.
     * @return Session identity updates for the lifetime of the collector.
     * @author Dmitriy Moroz
     */
    fun observeIdentity(): Flow<OpenAiOAuthIdentity?>

    /**
     * Loads the protected session once and returns its account and login revision.
     * @return Current identity, or null when no session is stored.
     * @throws OpenAiOAuthFailure When protected storage cannot be read or decoded.
     * @author Dmitriy Moroz
     */
    suspend fun currentIdentity(): OpenAiOAuthIdentity?

    /**
     * Completes a pending device authorization without changing the selected provider.
     * Cancellation or failure preserves the previous session; a completed grant replaces it atomically.
     * @param onCode Receives the transient user code and fixed trusted HTTPS link.
     * @return Newly committed account identity with a fresh login revision.
     * @throws OpenAiOAuthFailure When login expires, is rejected or cannot be securely persisted.
     * @author Dmitriy Moroz
     */
    suspend fun login(onCode: suspend (code: String, verificationUrl: String) -> Unit): OpenAiOAuthIdentity

    /**
     * Deletes protected credentials without decoding them and invalidates pending login tickets.
     * Publishes a signed-out identity only after protected deletion succeeds.
     * @throws OpenAiOAuthFailure When protected credentials cannot be deleted.
     * @author Dmitriy Moroz
     */
    suspend fun logout()

    /**
     * Runs a short UI publication while preventing concurrent account replacement/logout.
     * @param identity Original token-free login identity of the completed operation.
     * @param block Immediate publication action; must not recursively call gateway operations.
     * @return Value produced by [block] while the captured identity remains current.
     * @throws OpenAiOAuthFailure When logout or account replacement invalidated [identity].
     * @author Dmitriy Moroz
     */
    suspend fun <T> withIdentity(identity: OpenAiOAuthIdentity, block: suspend () -> T): T
}

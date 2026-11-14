package com.shifthackz.aisdv1.data.feature.openaioauth

import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.feature.openaioauth.auth.OAuthSessionManager
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthFailure
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthIdentity
import kotlinx.coroutines.flow.map

/**
 * Adapts the independent feature session API to token-free application contracts.
 * @param sessions Protected feature session owner.
 * @author Dmitriy Moroz
 */
internal class OpenAiOAuthGatewayImpl(private val sessions: OAuthSessionManager) :
    OpenAiOAuthGateway {
    /**
     * Maps session identity updates without exposing or reading protected credentials.
     * @return Token-free account and login revision updates.
     * @author Dmitriy Moroz
     */
    override fun observeIdentity() = sessions.identity.map { it?.toDomain() }

    /**
     * Initializes protected session storage and maps the current account to its domain identity.
     * @return Loaded identity, or null when signed out.
     * @author Dmitriy Moroz
     */
    override suspend fun currentIdentity() = oauthCall { sessions.currentIdentity()?.toDomain() }

    /**
     * Completes device authorization and maps only the committed, credential-free identity.
     * @param onCode Receives the transient code and trusted verification URL.
     * @return Newly committed domain identity.
     * @author Dmitriy Moroz
     */
    override suspend fun login(onCode: suspend (String, String) -> Unit) = oauthCall {
        sessions.login { onCode(it.code, it.verificationUrl) }.toDomain()
    }

    /**
     * Deletes the protected envelope and maps storage failures to safe domain categories.
     * @author Dmitriy Moroz
     */
    override suspend fun logout() = oauthCall { sessions.logout() }

    /**
     * Serializes publication with session replacement and maps stale-identity failures.
     * @param identity Domain identity captured by the completed operation.
     * @param block Short publication action that must not call session operations recursively.
     * @return Value produced by [block] while its session is current.
     * @author Dmitriy Moroz
     */
    override suspend fun <T> withIdentity(
        identity: OpenAiOAuthIdentity,
        block: suspend () -> T
    ): T = oauthCall {
        sessions.withIdentity(OAuthIdentity(identity.accountId, identity.revision), block)
    }
}

/**
 * Converts safe feature categories without retaining cause chains across layer boundaries.
 * @param block Feature operation.
 * @author Dmitriy Moroz
 */
internal suspend fun <T> oauthCall(block: suspend () -> T): T = try {
    block()
} catch (error: OAuthFailure) {
    throw OpenAiOAuthFailure(error.reason.name, error.statusCode)
}

/**
 * Preserves the account and stable login revision across the feature/domain boundary.
 * @return Domain identity containing no credential data.
 * @author Dmitriy Moroz
 */
private fun OAuthIdentity.toDomain() = OpenAiOAuthIdentity(accountId, revision)

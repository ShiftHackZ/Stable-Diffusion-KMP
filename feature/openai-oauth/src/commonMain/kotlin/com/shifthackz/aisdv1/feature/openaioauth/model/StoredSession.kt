package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.Serializable

/**
 * Complete protected-store envelope; never expose it through UI or queued work.
 * @param accessToken Short-lived bearer credential.
 * @param refreshToken Rotation credential.
 * @param accountId ChatGPT account identifier.
 * @param revision Stable login revision.
 * @param expiresAtMillis Access-token expiry in Unix milliseconds.
 * @author Dmitriy Moroz
 */
@Serializable
internal class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val accountId: String,
    val revision: String,
    val expiresAtMillis: Long,
) {
    /**
     * Extracts the token-free identity used to guard submitted work and result publication.
     * @return Account and stable login revision without credential fields.
     * @author Dmitriy Moroz
     */
    fun identity() = OAuthIdentity(accountId, revision)
}

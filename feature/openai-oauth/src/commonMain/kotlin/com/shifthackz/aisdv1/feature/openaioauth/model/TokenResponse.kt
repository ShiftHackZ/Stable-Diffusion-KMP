package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Credentials returned only by the fixed TLS token endpoint.
 * @param accessToken Short-lived bearer credential.
 * @param refreshToken New refresh token, or null when rotation retains the prior value.
 * @param idToken Optional identity JWT used transiently for account claims.
 * @param expiresIn Access lifetime in seconds when JWT expiry is unavailable.
 * @author Dmitriy Moroz
 */
@Serializable
internal class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
)

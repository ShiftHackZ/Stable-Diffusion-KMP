package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Completed device grant passed directly into authorization-code exchange.
 * @param authorizationCode Single-use code returned after browser authorization.
 * @param codeVerifier PKCE verifier supplied by the service.
 * @param codeChallenge PKCE challenge associated with this authorization.
 * @author Dmitriy Moroz
 */
@Serializable
internal class DevicePollResponse(
    @SerialName("authorization_code") val authorizationCode: String,
    @SerialName("code_verifier") val codeVerifier: String,
    @SerialName("code_challenge") val codeChallenge: String,
)

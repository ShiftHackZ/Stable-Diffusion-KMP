package com.shifthackz.aisdv1.feature.openaioauth.model

/**
 * Transient device login prompt containing no access or refresh token.
 * @param code One-time code entered by the user on the trusted verification page.
 * @param verificationUrl Fixed HTTPS authentication page.
 * @author Dmitriy Moroz
 */
data class DeviceLoginPrompt(
    val code: String,
    val verificationUrl: String,
)

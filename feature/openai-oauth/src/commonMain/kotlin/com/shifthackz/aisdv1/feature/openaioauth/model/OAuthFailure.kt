package com.shifthackz.aisdv1.feature.openaioauth.model

/**
 * Safe failure category; responses and credential-bearing causes are never retained.
 * @param reason Stable category suitable for localized UI guidance.
 * @param statusCode Optional HTTP status without response text.
 * @author Dmitriy Moroz
 */
class OAuthFailure(
    val reason: Reason,
    val statusCode: Int? = null,
) : Exception(reason.name) {
    /**
     * Credential-free failure categories shared by authentication, storage and image transport.
     * @author Dmitriy Moroz
     */
    enum class Reason {
        /** No valid protected session remains. */
        LOGIN_REQUIRED,
        /** Device authorization is unavailable for the account or service. */
        LOGIN_UNAVAILABLE,
        /** The pending device authorization exceeded its local deadline. */
        LOGIN_EXPIRED,
        /** Work belongs to an earlier login or superseded authorization attempt. */
        IDENTITY_CHANGED,
        /** The credential-safe client failed before receiving a usable HTTP response. */
        NETWORK,
        /** The service returned a non-success HTTP response. */
        HTTP,
        /** A successful response omitted required account or image data. */
        INVALID_RESPONSE,
        /** Protected storage could not read, replace or delete the session. */
        STORAGE,
    }
}

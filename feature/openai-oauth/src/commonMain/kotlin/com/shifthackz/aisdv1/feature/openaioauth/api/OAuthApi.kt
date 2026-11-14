package com.shifthackz.aisdv1.feature.openaioauth.api

import com.shifthackz.aisdv1.feature.openaioauth.model.DeviceCodeRequest
import com.shifthackz.aisdv1.feature.openaioauth.model.DeviceCodeResponse
import com.shifthackz.aisdv1.feature.openaioauth.model.DevicePollRequest
import com.shifthackz.aisdv1.feature.openaioauth.model.DevicePollResponse
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthFailure
import com.shifthackz.aisdv1.feature.openaioauth.model.TokenResponse
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.setTrackedTextBody
import com.shifthackz.aisdv1.network.client.trackedBodyAsText
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.plugins.expectSuccess
import io.ktor.http.Parameters
import io.ktor.http.ContentType
import io.ktor.http.formUrlEncode

/**
 * Fixed-origin device authorization and token exchange. The client must disable logging and redirects.
 * @param client Dedicated credential-safe client with live connection limits.
 * @author Dmitriy Moroz
 */
internal class OAuthApi(private val client: HttpClient) {
    /** Requests a transient authorization challenge; non-success preserves existing sessions.
     * @author Dmitriy Moroz */
    suspend fun requestCode(): DeviceCodeResponse {
        val response = client.post("$AUTH/api/accounts/deviceauth/usercode") {
            expectSuccess = false
            setTrackedJsonBody(NetworkUsageCategory.CONFIGS, DeviceCodeRequest(CLIENT_ID))
        }
        if (response.status.value !in 200..299) {
            response.trackedBodyAsText(NetworkUsageCategory.CONFIGS)
            throw OAuthFailure(OAuthFailure.Reason.LOGIN_UNAVAILABLE, response.status.value)
        }
        return response.trackedJsonBody(NetworkUsageCategory.CONFIGS)
    }

    /**
     * Polls an existing challenge once; HTTP 403/404 means authorization is pending.
     * @param code Original in-memory device challenge.
     * @author Dmitriy Moroz
     */
    suspend fun poll(code: DeviceCodeResponse): DevicePollResponse? {
        val response = client.post("$AUTH/api/accounts/deviceauth/token") {
            expectSuccess = false
            setTrackedJsonBody(NetworkUsageCategory.CONFIGS, DevicePollRequest(code.deviceAuthId, code.userCode ?: code.usercode.orEmpty()))
        }
        return when (response.status.value) {
            in 200..299 -> response.trackedJsonBody(NetworkUsageCategory.CONFIGS)
            403, 404 -> { response.trackedBodyAsText(NetworkUsageCategory.CONFIGS); null }
            else -> {
                response.trackedBodyAsText(NetworkUsageCategory.CONFIGS)
                throw OAuthFailure(OAuthFailure.Reason.HTTP, response.status.value)
            }
        }
    }

    /**
     * Exchanges a single-use device grant using its PKCE verifier and registered callback.
     * @param code Successful poll response, retained only for this exchange.
     * @author Dmitriy Moroz
     */
    suspend fun exchange(code: DevicePollResponse): TokenResponse = token(Parameters.build {
        append("grant_type", "authorization_code")
        append("client_id", CLIENT_ID)
        append("redirect_uri", "$AUTH/deviceauth/callback")
        append("code", code.authorizationCode)
        append("code_verifier", code.codeVerifier)
    })

    /**
     * Rotates a credential without exposing server response bodies in failures.
     * @param value Refresh token obtained from protected storage.
     * @author Dmitriy Moroz
     */
    suspend fun refresh(value: String): TokenResponse = token(Parameters.build {
        append("grant_type", "refresh_token")
        append("client_id", CLIENT_ID)
        append("refresh_token", value)
    })

    /**
     * Counts the encoded request and response bytes without logging their contents.
     * @param parameters Typed authorization-code or refresh grant fields.
     * @author Dmitriy Moroz
     */
    private suspend fun token(parameters: Parameters): TokenResponse {
        val response = client.post("$AUTH/oauth/token") {
            expectSuccess = false
            setTrackedTextBody(NetworkUsageCategory.CONFIGS, parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
        }
        if (response.status.value !in 200..299) {
            response.trackedBodyAsText(NetworkUsageCategory.CONFIGS)
            throw OAuthFailure(if (response.status.value in listOf(400, 401, 403)) OAuthFailure.Reason.LOGIN_REQUIRED else OAuthFailure.Reason.HTTP, response.status.value)
        }
        return response.trackedJsonBody(NetworkUsageCategory.CONFIGS)
    }

    /** Fixed authentication origin and public application identifier; contains no client secret.
     * @author Dmitriy Moroz */
    companion object {
        /** TLS origin used by every device and token operation. @author Dmitriy Moroz */
        const val AUTH = "https://auth.openai.com"
        /** Public Codex OAuth application identifier, not a secret credential. @author Dmitriy Moroz */
        const val CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann"
    }
}

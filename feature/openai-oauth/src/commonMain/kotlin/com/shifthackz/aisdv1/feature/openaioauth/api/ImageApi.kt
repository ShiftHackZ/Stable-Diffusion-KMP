package com.shifthackz.aisdv1.feature.openaioauth.api

import com.shifthackz.aisdv1.feature.openaioauth.model.ImageRequest
import com.shifthackz.aisdv1.feature.openaioauth.model.ImageResponse
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthFailure
import com.shifthackz.aisdv1.feature.openaioauth.model.StoredSession
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackedBodyAsText
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders

/**
 * Direct JSON image transport adapted from EvanZhouDev/openai-oauth (Apache-2.0).
 * @param client Dedicated client with redirects and logging disabled.
 * @param timeoutProvider Live application generation timeout.
 * @author Dmitriy Moroz
 */
internal class ImageApi(private val client: HttpClient, private val timeoutProvider: GenerationTimeoutProvider) {
    /**
     * Sends exactly one direct image request. Redirects never receive account credentials.
     * @param session Protected credentials for the identity pinned to this request.
     * @param prompt User image description.
     * @param width Positive requested pixel width.
     * @param height Positive requested pixel height.
     * @author Dmitriy Moroz
     */
    suspend fun generate(session: StoredSession, prompt: String, width: Int, height: Int): String {
        require(width > 0 && height > 0)
        val response = client.post("https://chatgpt.com/backend-api/codex/images/generations") {
            expectSuccess = false
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            header("chatgpt-account-id", session.accountId)
            applyGenerationTimeout(timeoutProvider)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, ImageRequest(prompt, "${width}x${height}"))
        }
        if (response.status.value !in 200..299) {
            response.trackedBodyAsText(NetworkUsageCategory.INFERENCE)
            throw OAuthFailure(OAuthFailure.Reason.HTTP, response.status.value)
        }
        return response.trackedJsonBody<ImageResponse>(NetworkUsageCategory.INFERENCE).data.firstOrNull()?.base64
            ?.takeIf(String::isNotBlank) ?: throw OAuthFailure(OAuthFailure.Reason.INVALID_RESPONSE)
    }
}

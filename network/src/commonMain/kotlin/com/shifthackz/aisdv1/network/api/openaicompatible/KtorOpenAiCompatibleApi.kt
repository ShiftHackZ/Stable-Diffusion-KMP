package com.shifthackz.aisdv1.network.api.openaicompatible

import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackedBodyAsText
import com.shifthackz.aisdv1.network.client.trackedByteArrayBody
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.request.OpenAiCompatibleRequest
import com.shifthackz.aisdv1.network.response.OpenAiCompatibleResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * Appends paths to the full prefix and accounts for configuration and image traffic.
 *
 * @param httpClient Provider client with live connection limits, no default authentication.
 * @param imageClient Independent client with no authentication state or default headers.
 * @param generationTimeoutProvider Live request/socket generation limits.
 * @author Dmitriy Moroz
 */
class KtorOpenAiCompatibleApi(
    private val httpClient: HttpClient,
    private val imageClient: HttpClient,
    private val generationTimeoutProvider: GenerationTimeoutProvider,
) : OpenAiCompatibleApi {
    /**
     * Checks discovery without requiring a particular successful response body.
     *
     * @param baseUrl Full API prefix; nested paths are retained.
     * @param apiKey Optional bearer token.
     * @author Dmitriy Moroz
     */
    override suspend fun probe(baseUrl: String, apiKey: String): Boolean {
        val response = httpClient.get {
            expectSuccess = false
            url.takeFrom(baseUrl.trimEnd('/'))
            url.appendPathSegments("models")
            optionalBearer(apiKey)
        }
        response.trackedBodyAsText(NetworkUsageCategory.CONFIGS)
        return when (response.status.value) {
            in 200..299 -> true
            404, 405 -> false
            else -> throw OpenAiCompatibleHttpException(response.status.value)
        }
    }

    /**
     * Requests exactly one image using the current generation timeouts.
     *
     * @param baseUrl Full API prefix.
     * @param apiKey Optional bearer token.
     * @param request Minimal interoperable image request.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        baseUrl: String,
        apiKey: String,
        request: OpenAiCompatibleRequest,
    ): OpenAiCompatibleResponse {
        val response = httpClient.post {
            expectSuccess = false
            url.takeFrom(baseUrl.trimEnd('/'))
            url.appendPathSegments("images", "generations")
            applyGenerationTimeout(generationTimeoutProvider)
            optionalBearer(apiKey)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        if (response.status.value !in 200..299) {
            response.trackedBodyAsText(NetworkUsageCategory.INFERENCE)
            throw OpenAiCompatibleHttpException(response.status.value)
        }
        return response.trackedJsonBody(NetworkUsageCategory.INFERENCE)
    }

    /**
     * Fetches image bytes using an independent client with logging disabled.
     *
     * @param url Absolute HTTP(S) URL, including any server-provided signed query.
     * @author Dmitriy Moroz
     */
    override suspend fun downloadImage(url: String): ByteArray {
        val parsed = Url(url)
        require(parsed.protocol == URLProtocol.HTTP || parsed.protocol == URLProtocol.HTTPS) { "Invalid image URL" }
        require(parsed.host.isNotBlank() && parsed.user == null && parsed.password == null) { "Invalid image URL" }
        val response = imageClient.get(parsed) {
            expectSuccess = false
            applyGenerationTimeout(generationTimeoutProvider)
        }
        val bytes = response.trackedByteArrayBody(NetworkUsageCategory.INFERENCE)
        if (response.status.value !in 200..299) throw OpenAiCompatibleHttpException(response.status.value)
        return bytes
    }

    /**
     * Adds authorization only to requests explicitly made against the configured API.
     *
     * @param apiKey Optional bearer token entered by the user.
     * @author Dmitriy Moroz
     */
    private fun HttpRequestBuilder.optionalBearer(apiKey: String) {
        apiKey.trim().takeIf(String::isNotEmpty)?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }
}

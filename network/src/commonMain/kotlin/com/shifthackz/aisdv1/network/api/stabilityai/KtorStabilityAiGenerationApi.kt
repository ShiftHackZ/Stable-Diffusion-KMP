package com.shifthackz.aisdv1.network.api.stabilityai

import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.client.recordUsageBytes
import com.shifthackz.aisdv1.network.client.setTrackedTextBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedBodyAsText
import com.shifthackz.aisdv1.network.request.StabilityTextToImageRequest
import com.shifthackz.aisdv1.network.response.StabilityAiErrorResponse
import com.shifthackz.aisdv1.network.response.StabilityCreditsResponse
import com.shifthackz.aisdv1.network.response.StabilityGenerationResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json

/**
 * Ktor implementation of Stability AI generation calls with counted inference traffic.
 *
 * @param httpClient Configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each generation request.
 * @param baseUrl Stability AI API base URL.
 * @param json JSON codec used for counted request/response payloads.
 *
 * @author Dmitriy Moroz
 */
class KtorStabilityAiGenerationApi(
    /**
     * Exposes the `httpClient` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val httpClient: HttpClient,
    /**
     * Exposes the `baseUrl` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val baseUrl: String,
    /**
     * Exposes the `json` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val json: Json = defaultNetworkJson,
    private val generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
) : StabilityAiGenerationApi {

    /**
     * Creates the generation client with the standard timeout for account and balance calls.
     *
     * @param baseUrl Stability AI API base URL.
     * @param generationTimeoutProvider Source of the current generation waiting limit.
     * @author Dmitriy Moroz
     */
    constructor(
        baseUrl: String,
        generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
    ) : this(
        httpClient = createConfiguredHttpClient(
            installContentNegotiation = false,
        ),
        baseUrl = baseUrl,
        generationTimeoutProvider = generationTimeoutProvider,
    )

    override suspend fun validateBearerToken(apiKey: String) {
        mapStabilityError {
            httpClient.get {
                url.takeFrom(baseUrl)
                url.appendPathSegments(PATH_API_VERSION, PATH_USER, PATH_ACCOUNT)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
    }

    override suspend fun fetchCredits(apiKey: String): StabilityCreditsResponse = mapStabilityError {
        httpClient
            .get {
                url.takeFrom(baseUrl)
                url.appendPathSegments(PATH_API_VERSION, PATH_USER, PATH_BALANCE)
                trackUsage(NetworkUsageCategory.CONFIGS)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
            .trackedBodyAsText(NetworkUsageCategory.CONFIGS)
            .let { json.decodeFromString<StabilityCreditsResponse>(it) }
    }

    override suspend fun textToImage(
        apiKey: String,
        engineId: String,
        request: StabilityTextToImageRequest,
    ): StabilityGenerationResponse = mapStabilityError {
        httpClient
            .post {
                url.takeFrom(baseUrl)
                url.appendPathSegments(PATH_API_VERSION, PATH_GENERATION, engineId, PATH_TEXT_TO_IMAGE)
                applyGenerationTimeout(generationTimeoutProvider)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setTrackedTextBody(
                    category = NetworkUsageCategory.INFERENCE,
                    body = json.encodeToString(request),
                )
            }
            .trackedBodyAsText(NetworkUsageCategory.INFERENCE)
            .let { json.decodeFromString<StabilityGenerationResponse>(it) }
    }

    override suspend fun imageToImage(
        apiKey: String,
        engineId: String,
        imageBytes: ByteArray,
        parameters: Map<String, String>,
    ): StabilityGenerationResponse = mapStabilityError {
        httpClient
            .post {
                url.takeFrom(baseUrl)
                url.appendPathSegments(PATH_API_VERSION, PATH_GENERATION, engineId, PATH_IMAGE_TO_IMAGE)
                applyGenerationTimeout(generationTimeoutProvider)
                trackUsage(NetworkUsageCategory.INFERENCE)
                recordUsageBytes(
                    category = NetworkUsageCategory.INFERENCE,
                    bytes = imageBytes.size.toLong() + parameters.multipartTextBytes(),
                )
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "init_image",
                                imageBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"image.png\"")
                                    append(HttpHeaders.ContentType, ContentType.Image.PNG.toString())
                                },
                            )
                            parameters.forEach { (key, value) ->
                                append(key, value)
                            }
                        },
                    ),
                )
            }
            .trackedBodyAsText(NetworkUsageCategory.INFERENCE)
            .let { json.decodeFromString<StabilityGenerationResponse>(it) }
    }

    private suspend fun <T> mapStabilityError(block: suspend () -> T): T = try {
        block()
    } catch (t: ResponseException) {
        val message = runCatching {
            json
                .decodeFromString<StabilityAiErrorResponse>(t.response.bodyAsText())
                .message
        }.getOrNull()
        throw message?.let(::Throwable) ?: t
    }

    private companion object {
        const val PATH_API_VERSION = "v1"
        const val PATH_USER = "user"
        const val PATH_ACCOUNT = "account"
        const val PATH_BALANCE = "balance"
        const val PATH_GENERATION = "generation"
        const val PATH_TEXT_TO_IMAGE = "text-to-image"
        const val PATH_IMAGE_TO_IMAGE = "image-to-image"
    }
}

private fun Map<String, String>.multipartTextBytes(): Long = entries.sumOf { (key, value) ->
    key.encodeToByteArray().size.toLong() + value.encodeToByteArray().size.toLong()
}

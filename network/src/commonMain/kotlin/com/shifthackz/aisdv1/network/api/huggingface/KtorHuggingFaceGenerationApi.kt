package com.shifthackz.aisdv1.network.api.huggingface

import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.client.setTrackedTextBody
import com.shifthackz.aisdv1.network.client.trackedByteArrayBody
import com.shifthackz.aisdv1.network.request.HuggingFaceGenerationRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Ktor implementation of Hugging Face generation requests with counted inference traffic.
 *
 * @param httpClient Configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each generation request.
 * @param apiBaseUrl Hugging Face API base URL used for model validation.
 * @param inferenceBaseUrl Hugging Face inference base URL used for generation requests.
 * @param json JSON codec used for counted request/response payloads.
 *
 * @author Dmitriy Moroz
 */
class KtorHuggingFaceGenerationApi(
    /**
     * Exposes the `httpClient` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val httpClient: HttpClient,
    /**
     * Exposes the `apiBaseUrl` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val apiBaseUrl: String,
    /**
     * Exposes the `inferenceBaseUrl` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val inferenceBaseUrl: String,
    /**
     * Exposes the `json` value used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private val json: Json = defaultNetworkJson,
    private val generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
) : HuggingFaceGenerationApi {

    /**
     * Creates the inference client while keeping validation on the standard HTTP timeout.
     *
     * @param apiBaseUrl API URL used to validate credentials.
     * @param inferenceBaseUrl Inference URL used to generate images.
     * @param generationTimeoutProvider Source of the current generation waiting limit.
     * @author Dmitriy Moroz
     */
    constructor(
        apiBaseUrl: String,
        inferenceBaseUrl: String,
        generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
    ) : this(
        httpClient = createConfiguredHttpClient(
            installContentNegotiation = false,
        ),
        apiBaseUrl = apiBaseUrl,
        inferenceBaseUrl = inferenceBaseUrl,
        generationTimeoutProvider = generationTimeoutProvider,
    )

    override suspend fun validateBearerToken(apiKey: String) {
        httpClient.get {
            url.takeFrom(apiBaseUrl)
            url.appendPathSegments(PATH_API, PATH_WHOAMI)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }
    }

    override suspend fun generate(
        apiKey: String,
        model: String,
        request: HuggingFaceGenerationRequest,
    ): ByteArray = retryServiceUnavailable {
        httpClient
            .post {
                url.takeFrom(inferenceBaseUrl)
                url.appendPathSegments(PATH_MODELS, model)
                applyGenerationTimeout(generationTimeoutProvider)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, ContentType.Image.PNG.toString())
                setTrackedTextBody(
                    category = NetworkUsageCategory.INFERENCE,
                    body = json.encodeToString(request),
                )
            }
            .trackedByteArrayBody(NetworkUsageCategory.INFERENCE)
    }

    private suspend fun <T> retryServiceUnavailable(block: suspend () -> T): T {
        while (true) {
            try {
                return block()
            } catch (t: ServerResponseException) {
                if (t.response.status != HttpStatusCode.ServiceUnavailable) {
                    throw t
                }
                delay(RETRY_DELAY_MILLIS)
            }
        }
    }

    private companion object {
        const val PATH_API = "api"
        const val PATH_WHOAMI = "whoami-v2"
        const val PATH_MODELS = "models"
        const val RETRY_DELAY_MILLIS = 20_000L
    }
}

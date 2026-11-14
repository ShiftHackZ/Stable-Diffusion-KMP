package com.shifthackz.aisdv1.network.api.openai

import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.request.OpenAiRequest
import com.shifthackz.aisdv1.network.response.OpenAiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * Ktor implementation of OpenAI validation and image generation with traffic accounting.
 *
 * @param httpClient Configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each generation request.
 * @param baseUrl OpenAI API base URL.
 *
 * @author Dmitriy Moroz
 */
class KtorOpenAiGenerationApi(
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
    private val generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
) : OpenAiGenerationApi {

    /**
     * Creates a new SDAI component instance.
     *
     * @param baseUrl base url value consumed by the API.
     * @param generationTimeoutProvider Source of the current generation waiting limit.
     * @author Dmitriy Moroz
     */
    constructor(
        baseUrl: String,
        generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
    ) : this(
        httpClient = createConfiguredHttpClient(),
        baseUrl = baseUrl,
        generationTimeoutProvider = generationTimeoutProvider,
    )

    /**
     * Executes the `validateBearerToken` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @author Dmitriy Moroz
     */
    override suspend fun validateBearerToken(apiKey: String) {
        httpClient.get {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API_VERSION, PATH_MODELS)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }
    }

    /**
     * Executes the `generateImage` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @param request request value consumed by the API.
     * @return Result produced by `generateImage`.
     * @author Dmitriy Moroz
     */
    override suspend fun generateImage(
        apiKey: String,
        request: OpenAiRequest,
    ): OpenAiResponse = httpClient
        .post {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API_VERSION, PATH_IMAGES, PATH_GENERATIONS)
            applyGenerationTimeout(generationTimeoutProvider)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    /**
     * Provides the `companion object` singleton used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        /**
         * Exposes the `PATH_API_VERSION` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_API_VERSION = "v1"
        /**
         * Exposes the `PATH_MODELS` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_MODELS = "models"
        /**
         * Exposes the `PATH_IMAGES` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_IMAGES = "images"
        /**
         * Exposes the `PATH_GENERATIONS` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_GENERATIONS = "generations"
    }
}

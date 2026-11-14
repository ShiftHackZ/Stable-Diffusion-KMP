package com.shifthackz.aisdv1.network.api.horde

import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedByteArrayBody
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.request.HordeGenerationAsyncRequest
import com.shifthackz.aisdv1.network.response.HordeGenerationAsyncResponse
import com.shifthackz.aisdv1.network.response.HordeGenerationCheckFullResponse
import com.shifthackz.aisdv1.network.response.HordeGenerationCheckResponse
import com.shifthackz.aisdv1.network.response.HordeUserResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * Ktor implementation of Horde generation, status polling, and image download calls.
 *
 * @param httpClient Configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each generation request.
 * @param baseUrl Horde API base URL.
 *
 * @author Dmitriy Moroz
 */
class KtorHordeGenerationApi(
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
) : HordeGenerationApi {

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
     * Executes the `generateAsync` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @param request request value consumed by the API.
     * @return Result produced by `generateAsync`.
     * @author Dmitriy Moroz
     */
    override suspend fun generateAsync(
        apiKey: String,
        request: HordeGenerationAsyncRequest,
    ): HordeGenerationAsyncResponse = httpClient
        .post {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API, PATH_VERSION, PATH_GENERATE, PATH_ASYNC)
            applyGenerationTimeout(generationTimeoutProvider)
            hordeApiKey(apiKey)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    /**
     * Executes the `checkGeneration` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @param id identifier of the target entity.
     * @return Result produced by `checkGeneration`.
     * @author Dmitriy Moroz
     */
    override suspend fun checkGeneration(
        apiKey: String,
        id: String,
    ): HordeGenerationCheckResponse = httpClient
        .get {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API, PATH_VERSION, PATH_GENERATE, PATH_CHECK, id)
            applyGenerationTimeout(generationTimeoutProvider)
            trackUsage(NetworkUsageCategory.INFERENCE)
            hordeApiKey(apiKey)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    /**
     * Executes the `checkStatus` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @param id identifier of the target entity.
     * @return Result produced by `checkStatus`.
     * @author Dmitriy Moroz
     */
    override suspend fun checkStatus(
        apiKey: String,
        id: String,
    ): HordeGenerationCheckFullResponse = httpClient
        .get {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API, PATH_VERSION, PATH_GENERATE, PATH_STATUS, id)
            applyGenerationTimeout(generationTimeoutProvider)
            trackUsage(NetworkUsageCategory.INFERENCE)
            hordeApiKey(apiKey)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    /**
     * Executes the `checkHordeApiKey` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @return Result produced by `checkHordeApiKey`.
     * @author Dmitriy Moroz
     */
    override suspend fun checkHordeApiKey(apiKey: String): HordeUserResponse = httpClient
        .get {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API, PATH_VERSION, PATH_FIND_USER)
            trackUsage(NetworkUsageCategory.CONFIGS)
            hordeApiKey(apiKey)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Executes the `cancelRequest` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @param requestId request id value consumed by the API.
     * @author Dmitriy Moroz
     */
    override suspend fun cancelRequest(
        apiKey: String,
        requestId: String,
    ) {
        httpClient.delete {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_API, PATH_VERSION, PATH_GENERATE, PATH_STATUS, requestId)
            hordeApiKey(apiKey)
        }
    }

    /**
     * Executes the `downloadImage` step in the SDAI network layer.
     *
     * @param url remote URL used by the operation.
     * @return Result produced by `downloadImage`.
     * @author Dmitriy Moroz
     */
    override suspend fun downloadImage(url: String): ByteArray = httpClient
        .get {
            this.url.takeFrom(url)
            applyGenerationTimeout(generationTimeoutProvider)
            trackUsage(NetworkUsageCategory.INFERENCE)
        }
        .trackedByteArrayBody(NetworkUsageCategory.INFERENCE)

    /**
     * Executes the `function` step in the SDAI network layer.
     *
     * @param apiKey api key value consumed by the API.
     * @author Dmitriy Moroz
     */
    private fun io.ktor.client.request.HttpRequestBuilder.hordeApiKey(apiKey: String) {
        header(HttpHeaders.UserAgent, "SDAI")
        header(HEADER_API_KEY, apiKey)
    }

    /**
     * Provides the `companion object` singleton used by the SDAI network layer.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        /**
         * Exposes the `HEADER_API_KEY` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val HEADER_API_KEY = "apikey"
        /**
         * Exposes the `PATH_API` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_API = "api"
        /**
         * Exposes the `PATH_VERSION` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_VERSION = "v2"
        /**
         * Exposes the `PATH_GENERATE` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_GENERATE = "generate"
        /**
         * Exposes the `PATH_ASYNC` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_ASYNC = "async"
        /**
         * Exposes the `PATH_CHECK` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_CHECK = "check"
        /**
         * Exposes the `PATH_STATUS` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_STATUS = "status"
        /**
         * Exposes the `PATH_FIND_USER` value used by the SDAI network layer.
         *
         * @author Dmitriy Moroz
         */
        const val PATH_FIND_USER = "find_user"
    }
}

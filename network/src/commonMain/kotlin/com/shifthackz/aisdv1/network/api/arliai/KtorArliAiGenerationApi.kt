package com.shifthackz.aisdv1.network.api.arliai

import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.model.KtorStableDiffusionModelRaw
import com.shifthackz.aisdv1.network.request.ArliAiImageToImageRequest
import com.shifthackz.aisdv1.network.request.ArliAiTextToImageRequest
import com.shifthackz.aisdv1.network.response.SdGenerationResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * Ktor implementation of ArliAI validation, model discovery, and image generation.
 *
 * Model-list traffic is counted as [NetworkUsageCategory.CONFIGS]. Text-to-image and
 * image-to-image request and response bodies are counted as [NetworkUsageCategory.INFERENCE].
 *
 * @param httpClient configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each generation request.
 * @param baseUrl ArliAI SDNext-compatible API base URL.
 *
 * @author Dmitriy Moroz
 */
class KtorArliAiGenerationApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
) : ArliAiGenerationApi {

    /**
     * Creates an ArliAI API client with the shared application HTTP configuration.
     *
     * @param baseUrl ArliAI SDNext-compatible API base URL.
     * @param generationTimeoutProvider Source of the current generation waiting limit.
     *
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
     * Validates the key by loading the ArliAI model list.
     *
     * @param apiKey ArliAI API key sent as bearer authorization.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateApiKey(apiKey: String) {
        fetchModels(apiKey)
    }

    /**
     * Loads available ArliAI checkpoints and records the response as configuration traffic.
     *
     * @param apiKey ArliAI API key sent as bearer authorization.
     * @return raw checkpoint metadata returned by the provider.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchModels(apiKey: String): List<KtorStableDiffusionModelRaw> = httpClient.get {
        url.takeFrom(baseUrl)
        url.appendPathSegments(PATH_SD_API, PATH_V1, PATH_SD_MODELS)
        header(HttpHeaders.Authorization, apiKey.headerValue)
        trackUsage(NetworkUsageCategory.CONFIGS)
    }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Sends text-to-image generation and records request plus response bytes as inference traffic.
     *
     * @param apiKey ArliAI API key sent as bearer authorization.
     * @param request SDNext-compatible text-to-image payload.
     * @return generated image payload returned by ArliAI.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun textToImage(
        apiKey: String,
        request: ArliAiTextToImageRequest,
    ): SdGenerationResponse = httpClient
        .post {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_SD_API, PATH_V1, PATH_TXT_TO_IMG)
            applyGenerationTimeout(generationTimeoutProvider)
            header(HttpHeaders.Authorization, apiKey.headerValue)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    /**
     * Sends image-to-image generation and records request plus response bytes as inference traffic.
     *
     * @param apiKey ArliAI API key sent as bearer authorization.
     * @param request SDNext-compatible image-to-image payload.
     * @return generated image payload returned by ArliAI.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun imageToImage(
        apiKey: String,
        request: ArliAiImageToImageRequest,
    ): SdGenerationResponse = httpClient
        .post {
            url.takeFrom(baseUrl)
            url.appendPathSegments(PATH_SD_API, PATH_V1, PATH_IMG_TO_IMG)
            applyGenerationTimeout(generationTimeoutProvider)
            header(HttpHeaders.Authorization, apiKey.headerValue)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        .trackedJsonBody(NetworkUsageCategory.INFERENCE)

    private val String.headerValue: String
        get() = "Bearer $this"

    private companion object {
        const val PATH_SD_API = "sdapi"
        const val PATH_V1 = "v1"
        const val PATH_SD_MODELS = "sd-models"
        const val PATH_TXT_TO_IMG = "txt2img"
        const val PATH_IMG_TO_IMG = "img2img"
    }
}

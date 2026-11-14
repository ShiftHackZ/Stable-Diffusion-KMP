package com.shifthackz.aisdv1.network.di

import com.shifthackz.aisdv1.network.api.openaicompatible.OpenAiCompatibleApi
import com.shifthackz.aisdv1.network.api.openaicompatible.KtorOpenAiCompatibleApi
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111GenerationApi
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111MetadataApi
import com.shifthackz.aisdv1.network.api.automatic1111.KtorAutomatic1111GenerationApi
import com.shifthackz.aisdv1.network.api.automatic1111.KtorAutomatic1111MetadataApi
import com.shifthackz.aisdv1.network.api.arliai.ArliAiGenerationApi
import com.shifthackz.aisdv1.network.api.arliai.KtorArliAiGenerationApi
import com.shifthackz.aisdv1.network.api.falai.FalAiGenerationApi
import com.shifthackz.aisdv1.network.api.falai.KtorFalAiGenerationApi
import com.shifthackz.aisdv1.network.api.horde.HordeGenerationApi
import com.shifthackz.aisdv1.network.api.horde.KtorHordeGenerationApi
import com.shifthackz.aisdv1.network.api.huggingface.HuggingFaceGenerationApi
import com.shifthackz.aisdv1.network.api.huggingface.HuggingFaceModelsApi
import com.shifthackz.aisdv1.network.api.huggingface.KtorHuggingFaceGenerationApi
import com.shifthackz.aisdv1.network.api.huggingface.KtorHuggingFaceModelsApi
import com.shifthackz.aisdv1.network.api.imagecdn.ImageCdnApi
import com.shifthackz.aisdv1.network.api.imagecdn.KtorImageCdnApi
import com.shifthackz.aisdv1.network.api.openai.KtorOpenAiGenerationApi
import com.shifthackz.aisdv1.network.api.openai.OpenAiGenerationApi
import com.shifthackz.aisdv1.network.api.sdai.KtorSdaiAppApi
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import com.shifthackz.aisdv1.network.api.stabilityai.KtorStabilityAiEnginesApi
import com.shifthackz.aisdv1.network.api.stabilityai.KtorStabilityAiGenerationApi
import com.shifthackz.aisdv1.network.api.stabilityai.StabilityAiEnginesApi
import com.shifthackz.aisdv1.network.api.stabilityai.StabilityAiGenerationApi
import com.shifthackz.aisdv1.network.api.swarmui.KtorSwarmUiGenerationApi
import com.shifthackz.aisdv1.network.api.swarmui.KtorSwarmUiModelsApi
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiGenerationApi
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiModelsApi
import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import org.koin.core.scope.Scope
import org.koin.dsl.module

/**
 * Exposes the `coreNetworkModule` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
val coreNetworkModule = module {
    single<SdaiAppApi> {
        KtorSdaiAppApi(
            appBaseUrl = stableDiffusionAppApiUrlOverride() ?: STABLE_DIFFUSION_APP_API_URL,
            reportBaseUrl = STABLE_DIFFUSION_REPORT_API_URL,
        )
    }
    single<ImageCdnApi> {
        KtorImageCdnApi(IMAGE_CDN_API_URL)
    }
    single<StabilityAiEnginesApi> {
        KtorStabilityAiEnginesApi(httpClient = providerHttpClient(false), baseUrl = STABILITY_AI_API_URL)
    }
    single<StabilityAiGenerationApi> {
        KtorStabilityAiGenerationApi(
            httpClient = providerHttpClient(false),
            baseUrl = STABILITY_AI_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<OpenAiCompatibleApi> {
        KtorOpenAiCompatibleApi(
            httpClient = providerHttpClient(),
            imageClient = createConfiguredHttpClient(
                installContentNegotiation = false,
                connectionTimeoutProvider = get(),
            ) {
                install(Logging) { level = LogLevel.NONE }
            },
            generationTimeoutProvider = get(),
        )
    }
    single<OpenAiGenerationApi> {
        KtorOpenAiGenerationApi(
            httpClient = providerHttpClient(),
            baseUrl = OPEN_AI_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<HordeGenerationApi> {
        KtorHordeGenerationApi(
            httpClient = providerHttpClient(),
            baseUrl = HORDE_AI_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<HuggingFaceGenerationApi> {
        KtorHuggingFaceGenerationApi(
            httpClient = providerHttpClient(false),
            apiBaseUrl = HUGGING_FACE_API_URL,
            inferenceBaseUrl = HUGGING_FACE_INFERENCE_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<HuggingFaceModelsApi> {
        KtorHuggingFaceModelsApi(httpClient = providerHttpClient(), apiBaseUrl = HUGGING_FACE_API_URL)
    }
    single<FalAiGenerationApi> {
        KtorFalAiGenerationApi(
            httpClient = providerHttpClient(),
            apiBaseUrl = FAL_AI_API_URL,
            queueBaseUrl = FAL_AI_QUEUE_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<ArliAiGenerationApi> {
        KtorArliAiGenerationApi(
            httpClient = providerHttpClient(),
            baseUrl = ARLI_AI_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<SwarmUiModelsApi> {
        KtorSwarmUiModelsApi(httpClient = providerHttpClient())
    }
    single<SwarmUiGenerationApi> {
        KtorSwarmUiGenerationApi(httpClient = providerHttpClient(), generationTimeoutProvider = get())
    }
    single<Automatic1111MetadataApi> {
        KtorAutomatic1111MetadataApi(httpClient = providerHttpClient())
    }
    single<Automatic1111GenerationApi> {
        KtorAutomatic1111GenerationApi(httpClient = providerHttpClient(), generationTimeoutProvider = get())
    }
}

/**
 * Builds a provider client with live connection preferences for all of its HTTP calls.
 *
 * @param installContentNegotiation Whether the provider uses Ktor JSON conversion rather than raw bodies.
 * @author Dmitriy Moroz
 */
private fun Scope.providerHttpClient(installContentNegotiation: Boolean = true): HttpClient =
    createConfiguredHttpClient(
        installContentNegotiation = installContentNegotiation,
        connectionTimeoutProvider = get(),
    )

/**
 * Exposes the `STABLE_DIFFUSION_APP_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val STABLE_DIFFUSION_APP_API_URL = "https://sdai.moroz.cc"
/**
 * Exposes the `STABLE_DIFFUSION_REPORT_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val STABLE_DIFFUSION_REPORT_API_URL = "https://sdai-report.moroz.cc"
/**
 * Exposes the `IMAGE_CDN_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val IMAGE_CDN_API_URL = "https://random.imagecdn.app"
/**
 * Exposes the `STABILITY_AI_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val STABILITY_AI_API_URL = "https://api.stability.ai"
/**
 * Exposes the `OPEN_AI_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val OPEN_AI_API_URL = "https://api.openai.com"
/**
 * Exposes the `HORDE_AI_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val HORDE_AI_API_URL = "https://stablehorde.net"
/**
 * Exposes the `HUGGING_FACE_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val HUGGING_FACE_API_URL = "https://huggingface.co"
/**
 * Exposes the `HUGGING_FACE_INFERENCE_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val HUGGING_FACE_INFERENCE_API_URL = "https://router.huggingface.co/hf-inference"
/**
 * Exposes the `FAL_AI_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val FAL_AI_API_URL = "https://api.fal.ai"
/**
 * Exposes the `FAL_AI_QUEUE_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val FAL_AI_QUEUE_API_URL = "https://queue.fal.run"
/**
 * Exposes the `ARLI_AI_API_URL` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val ARLI_AI_API_URL = "https://api.arliai.com"

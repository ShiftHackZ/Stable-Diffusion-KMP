package com.shifthackz.aisdv1.network.di

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
        KtorStabilityAiEnginesApi(STABILITY_AI_API_URL)
    }
    single<StabilityAiGenerationApi> {
        KtorStabilityAiGenerationApi(STABILITY_AI_API_URL, generationTimeoutProvider = get())
    }
    single<OpenAiGenerationApi> {
        KtorOpenAiGenerationApi(OPEN_AI_API_URL, generationTimeoutProvider = get())
    }
    single<HordeGenerationApi> {
        KtorHordeGenerationApi(HORDE_AI_API_URL, generationTimeoutProvider = get())
    }
    single<HuggingFaceGenerationApi> {
        KtorHuggingFaceGenerationApi(
            HUGGING_FACE_API_URL,
            HUGGING_FACE_INFERENCE_API_URL,
            generationTimeoutProvider = get(),
        )
    }
    single<HuggingFaceModelsApi> {
        KtorHuggingFaceModelsApi(HUGGING_FACE_API_URL)
    }
    single<FalAiGenerationApi> {
        KtorFalAiGenerationApi(FAL_AI_API_URL, FAL_AI_QUEUE_API_URL, generationTimeoutProvider = get())
    }
    single<ArliAiGenerationApi> {
        KtorArliAiGenerationApi(ARLI_AI_API_URL, generationTimeoutProvider = get())
    }
    single<SwarmUiModelsApi> {
        KtorSwarmUiModelsApi()
    }
    single<SwarmUiGenerationApi> {
        KtorSwarmUiGenerationApi(generationTimeoutProvider = get())
    }
    single<Automatic1111MetadataApi> {
        KtorAutomatic1111MetadataApi()
    }
    single<Automatic1111GenerationApi> {
        KtorAutomatic1111GenerationApi(generationTimeoutProvider = get())
    }
}

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

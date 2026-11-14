package com.shifthackz.aisdv1.presentation.screen.txt2img

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormState

/**
 * Complete render state for txt2img.
 *
 * @param isLoadingSamplers Whether an AUTOMATIC1111 sampler request is running.
 * @param hasSamplerLoadFailed Whether the sampler field should offer an inline retry.
 *
 * The state combines screen lifecycle flags, generated results, validation
 * feedback, current platform metadata, and the shared generation form contract
 * used by provider-specific controls such as Android Bonsai backend selection.
 * @param openAiOAuthAccountId Token-free account identity observed from protected session state.
 * @param openAiOAuthRevision Login revision captured when generation is submitted.
 * @param providerSelectionRevision Durable source-selection revision captured with submitted work.
 * @author Dmitriy Moroz
 */
@Immutable
data class TextToImageState(
    val loadingConfiguration: Boolean = true,
    val generating: Boolean = false,
    val savingImage: Boolean = false,
    val sharingImage: Boolean = false,
    val promptValidationError: UiText? = null,
    val error: UiText? = null,
    val message: UiText? = null,
    val screenModal: GenerationModal = GenerationModal.None,
    val results: List<AiGenerationResult> = emptyList(),
    val editTag: PromptTagEditRequest? = null,
    override val onBoardingDemo: Boolean = false,
    val platform: Platform = Platform.ANDROID,
    override val mode: ServerSource = ServerSource.AUTOMATIC1111,
    override val advancedToggleButtonVisible: Boolean = true,
    override val advancedOptionsVisible: Boolean = false,
    override val formPromptTaggedInput: Boolean = false,
    override val prompt: String = "",
    override val negativePrompt: String = "",
    override val width: String = DEFAULT_SIZE.toString(),
    override val height: String = DEFAULT_SIZE.toString(),
    override val samplingSteps: Int = 20,
    override val cfgScale: Float = 7f,
    override val restoreFaces: Boolean = false,
    override val seed: String = "",
    override val subSeed: String = "",
    override val subSeedStrength: Float = 0f,
    override val selectedSampler: String = "",
    override val selectedScheduler: Scheduler = Scheduler.AUTOMATIC,
    override val availableForgeModules: List<ForgeModule> = emptyList(),
    override val selectedForgeModules: List<ForgeModule> = emptyList(),
    override val availableSamplers: List<String> = emptyList(),
    override val isLoadingSamplers: Boolean = false,
    override val hasSamplerLoadFailed: Boolean = false,
    override val selectedStylePreset: StabilityAiStylePreset = StabilityAiStylePreset.NONE,
    override val selectedClipGuidancePreset: StabilityAiClipGuidance = StabilityAiClipGuidance.NONE,
    override val openAiCompatibleModel: String = "gpt-image-2",
    val openAiOAuthAccountId: String? = null,
    val openAiOAuthRevision: String? = null,
    val providerSelectionRevision: Long = 0L,
    override val openAiModel: OpenAiModel = OpenAiModel.default,
    override val openAiSize: OpenAiSize = OpenAiSize.W1024_H1024,
    override val openAiQuality: OpenAiQuality = OpenAiQuality.AUTO,
    override val falAiModel: FalAiModel = FalAiModel.defaultTextToImage,
    override val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    override val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    override val falAiSyncMode: Boolean = false,
    override val sdxlBackend: SdxlBackend = SdxlBackend.AUTO,
    override val bonsaiBackend: BonsaiBackend = BonsaiBackend.AUTO,
    override val bonsaiBackendSelectionVisible: Boolean = false,
    override val arliAiModels: List<String> = emptyList(),
    override val arliAiModel: String = "",
    override val widthValidationError: UiText? = null,
    override val heightValidationError: UiText? = null,
    override val nsfw: Boolean = false,
    override val batchCount: Int = 1,
    override val hires: HiresConfig = HiresConfig.DISABLED,
    override val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    override val aDetailerAvailable: Boolean = false,
    override val aDetailerRefreshing: Boolean = false,
) : MviState, GenerationInputFormState {

    val localSourceSelected: Boolean
        get() = mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
            mode == ServerSource.LOCAL_GOOGLE_MEDIA_PIPE ||
            mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP ||
            mode == ServerSource.LOCAL_APPLE_CORE_ML ||
            mode == ServerSource.LOCAL_APPLE_BONSAI

    val hasValidationErrors: Boolean
        get() = promptValidationError != null ||
            widthValidationError != null ||
            heightValidationError != null ||
            error != null
}

/**
 * Converts the current txt2img state into the domain generation request.
 */
internal fun TextToImageState.mapToPayload(): TextToImagePayload = TextToImagePayload(
    prompt = prompt.trim(),
    negativePrompt = negativePrompt.trim(),
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    width = when (mode) {
        ServerSource.OPEN_AI -> openAiSize.width
        ServerSource.FAL_AI -> falAiImageSize.width
        ServerSource.LOCAL_APPLE_BONSAI -> width.toIntOrNull() ?: BONSAI_DEFAULT_SIZE
        else -> width.toIntOrNull() ?: DEFAULT_SIZE
    },
    height = when (mode) {
        ServerSource.OPEN_AI -> openAiSize.height
        ServerSource.FAL_AI -> falAiImageSize.height
        ServerSource.LOCAL_APPLE_BONSAI -> height.toIntOrNull() ?: BONSAI_DEFAULT_SIZE
        else -> height.toIntOrNull() ?: DEFAULT_SIZE
    },
    restoreFaces = restoreFaces,
    seed = seed.trim(),
    subSeed = subSeed.trim(),
    subSeedStrength = subSeedStrength,
    sampler = selectedSampler,
    scheduler = selectedScheduler.takeIf {
        mode == ServerSource.AUTOMATIC1111
    } ?: Scheduler.AUTOMATIC,
    nsfw = if (
        mode == ServerSource.HORDE ||
        mode == ServerSource.LOCAL_APPLE_CORE_ML ||
        mode == ServerSource.LOCAL_APPLE_BONSAI ||
        mode == ServerSource.FAL_AI
    ) nsfw else false,
    batchCount = if (
        mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
        mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP ||
        mode == ServerSource.LOCAL_APPLE_BONSAI
    ) {
        1
    } else {
        batchCount
    },
    style = null,
    quality = openAiQuality.key.takeIf {
        mode == ServerSource.OPEN_AI && openAiQuality != OpenAiQuality.AUTO
    },
    openAiModel = openAiModel.takeIf { mode == ServerSource.OPEN_AI },
    stabilityAiClipGuidance = selectedClipGuidancePreset.takeIf { mode == ServerSource.STABILITY_AI },
    stabilityAiStylePreset = selectedStylePreset.takeIf { mode == ServerSource.STABILITY_AI },
    hires = hires.takeIf {
        mode == ServerSource.AUTOMATIC1111 && hires.enabled
    } ?: HiresConfig.DISABLED,
    aDetailer = aDetailer.takeIf {
        mode == ServerSource.AUTOMATIC1111 && aDetailer.enabled && aDetailerAvailable
    } ?: ADetailerConfig.DISABLED,
    forgeModules = selectedForgeModules.takeIf {
        mode == ServerSource.AUTOMATIC1111
    }.orEmpty(),
    falAiModel = falAiModel.takeIf {
        mode == ServerSource.FAL_AI
    } ?: FalAiModel.defaultTextToImage,
    falAiImageSize = falAiImageSize,
    falAiAcceleration = falAiAcceleration,
    sdxlBackend = sdxlBackend.takeIf {
        mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP
    } ?: SdxlBackend.AUTO,
    bonsaiBackend = bonsaiBackend.takeIf {
        mode == ServerSource.LOCAL_APPLE_BONSAI
    } ?: BonsaiBackend.AUTO,
    falAiSyncMode = falAiSyncMode,
    arliAiModel = arliAiModel.takeIf { mode == ServerSource.ARLI_AI }.orEmpty(),
    openAiCompatibleModel = openAiCompatibleModel.trim(),
    sourceSnapshot = mode,
    providerSelectionRevision = providerSelectionRevision,
    openAiOAuthAccountId = openAiOAuthAccountId.takeIf { mode == ServerSource.OPEN_AI_OAUTH },
    openAiOAuthRevision = openAiOAuthRevision.takeIf { mode == ServerSource.OPEN_AI_OAUTH },
)

internal const val DEFAULT_SIZE = 512

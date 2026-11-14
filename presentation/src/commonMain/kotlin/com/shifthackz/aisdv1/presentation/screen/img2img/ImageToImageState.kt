package com.shifthackz.aisdv1.presentation.screen.img2img

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
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormState

/**
 * Complete render state for img2img.
 *
 * @param isLoadingSamplers Whether an AUTOMATIC1111 sampler request is running.
 * @param hasSamplerLoadFailed Whether the sampler field should offer an inline retry.
 *
 * It extends the shared generation form state with source-image, inpaint, and
 * result handling fields while keeping platform-aware local provider controls
 * consistent with the txt2img screen.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class ImageToImageState(
    val loadingConfiguration: Boolean = true,
    val imageBase64: String = "",
    val denoisingStrength: Float = DEFAULT_DENOISING_STRENGTH,
    val pickingImage: Boolean = false,
    val generating: Boolean = false,
    val savingImage: Boolean = false,
    val sharingImage: Boolean = false,
    val inPaint: ImageInPaintState = ImageInPaintState(),
    val promptValidationError: String? = null,
    val error: String? = null,
    val message: String? = null,
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
    override val openAiModel: OpenAiModel = OpenAiModel.default,
    override val openAiSize: OpenAiSize = OpenAiSize.W1024_H1024,
    override val openAiQuality: OpenAiQuality = OpenAiQuality.AUTO,
    override val falAiModel: FalAiModel = FalAiModel.defaultImageToImage,
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

    val hasInputImage: Boolean
        get() = imageBase64.isNotBlank()

    val sourceSupportsImageToImage: Boolean
        get() = mode == ServerSource.AUTOMATIC1111 ||
            mode == ServerSource.SWARM_UI ||
            mode == ServerSource.HORDE ||
            mode == ServerSource.HUGGING_FACE ||
            mode == ServerSource.STABILITY_AI ||
            mode == ServerSource.LOCAL_APPLE_CORE_ML ||
            mode == ServerSource.FAL_AI ||
            mode == ServerSource.ARLI_AI

    val sourceSupportsInPaint: Boolean
        get() = mode != ServerSource.LOCAL_APPLE_CORE_ML

    val hasValidationErrors: Boolean
        get() = promptValidationError != null ||
            widthValidationError != null ||
            heightValidationError != null ||
            error != null

    val canGenerate: Boolean
        get() = hasInputImage &&
            sourceSupportsImageToImage &&
            !hasValidationErrors &&
            !pickingImage &&
            !generating
}

/**
 * Converts the current img2img state into the domain generation request.
 */
internal fun ImageToImageState.mapToPayload(
    maskBase64: String? = null,
): ImageToImagePayload = ImageToImagePayload(
    base64Image = imageBase64,
    base64MaskImage = maskBase64.orEmpty(),
    denoisingStrength = denoisingStrength,
    prompt = prompt.trim(),
    negativePrompt = negativePrompt.trim(),
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    width = when (mode) {
        ServerSource.FAL_AI -> falAiImageSize.width
        else -> width.toIntOrNull() ?: DEFAULT_SIZE
    },
    height = when (mode) {
        ServerSource.FAL_AI -> falAiImageSize.height
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
        mode == ServerSource.FAL_AI
    ) nsfw else false,
    batchCount = batchCount,
    inPaintingMaskInvert = inPaint.maskMode.inverse,
    inPaintFullResPadding = inPaint.onlyMaskedPaddingPx,
    inPaintingFill = inPaint.maskContent.fill,
    inPaintFullRes = inPaint.area.fullRes,
    maskBlur = inPaint.maskBlur,
    stabilityAiClipGuidance = selectedClipGuidancePreset.takeIf { mode == ServerSource.STABILITY_AI },
    stabilityAiStylePreset = selectedStylePreset.takeIf { mode == ServerSource.STABILITY_AI },
    aDetailer = aDetailer.takeIf {
        mode == ServerSource.AUTOMATIC1111 && aDetailer.enabled && aDetailerAvailable
    } ?: ADetailerConfig.DISABLED,
    falAiModel = falAiModel.takeIf {
        mode == ServerSource.FAL_AI
    } ?: FalAiModel.defaultImageToImage,
    falAiImageSize = falAiImageSize,
    falAiAcceleration = falAiAcceleration,
    falAiSyncMode = falAiSyncMode,
    arliAiModel = arliAiModel.takeIf { mode == ServerSource.ARLI_AI }.orEmpty(),
)

internal const val DEFAULT_SIZE = 512
internal const val DEFAULT_DENOISING_STRENGTH = 0.75f

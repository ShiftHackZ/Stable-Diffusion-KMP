package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.presentation.widget.input.GenerationAspectRatio

/**
 * Defines the `ImageToImageIntent` contract for the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
sealed interface ImageToImageIntent : MviIntent {
    /**
     * Retries an empty AUTOMATIC1111 sampler list after foregrounding or a user retry.
     *
     * @author Dmitriy Moroz
     */
    data object RetrySamplers : ImageToImageIntent

    /**
     * Provides the `OpenDrawer` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object OpenDrawer : ImageToImageIntent
    /**
     * Provides the `NavigateBack` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : ImageToImageIntent
    /**
     * Provides the `ConfigureProvider` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object ConfigureProvider : ImageToImageIntent
    /**
     * Provides the `NavigateToInPaint` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateToInPaint : ImageToImageIntent
    /**
     * Provides the `PickCamera` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object PickCamera : ImageToImageIntent
    /**
     * Provides the `PickGallery` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object PickGallery : ImageToImageIntent
    /**
     * Provides the `PickRandom` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object PickRandom : ImageToImageIntent
    /**
     * Provides the `ClearImageInput` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object ClearImageInput : ImageToImageIntent
    /**
     * Provides the `Generate` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object Generate : ImageToImageIntent
    /**
     * Opens the benchmark screen from the first local generation prompt.
     *
     * @author Dmitriy Moroz
     */
    data object RunBenchmarkFromPrompt : ImageToImageIntent
    /**
     * Skips the first local generation benchmark prompt and continues generation.
     *
     * @author Dmitriy Moroz
     */
    data object SkipBenchmarkPrompt : ImageToImageIntent
    /**
     * Continues generation after the benchmark recommendation warning.
     *
     * @author Dmitriy Moroz
     */
    data object ContinueAfterBenchmarkWarning : ImageToImageIntent
    /**
     * Suppresses future benchmark recommendation warnings and continues generation.
     *
     * @author Dmitriy Moroz
     */
    data object SuppressBenchmarkWarningAndContinue : ImageToImageIntent
    /**
     * Provides the `DismissModal` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DismissModal : ImageToImageIntent
    /**
     * Provides the `CancelGeneration` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object CancelGeneration : ImageToImageIntent
    /**
     * Provides the `UndoInPaintStroke` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object UndoInPaintStroke : ImageToImageIntent
    /**
     * Provides the `ClearInPaintMask` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object ClearInPaintMask : ImageToImageIntent
    /**
     * Provides the `DismissError` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DismissError : ImageToImageIntent
    /**
     * Provides the `DismissMessage` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DismissMessage : ImageToImageIntent
    /**
     * Provides the `DismissEditTag` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DismissEditTag : ImageToImageIntent
    /**
     * Carries `DrawInPaintStroke` data through the SDAI presentation layer.
     *
     * @param stroke stroke value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class DrawInPaintStroke(val stroke: InPaintStroke) : ImageToImageIntent
    /**
     * Carries `SaveResult` data through the SDAI presentation layer.
     *
     * @param base64 Base64 image payload used by the operation.
     * @author Dmitriy Moroz
     */
    data class SaveResult(val base64: String) : ImageToImageIntent
    /**
     * Carries `ShareResult` data through the SDAI presentation layer.
     *
     * @param base64 Base64 image payload used by the operation.
     * @author Dmitriy Moroz
     */
    data class ShareResult(val base64: String) : ImageToImageIntent
    /**
     * Carries `SaveGenerationResults` data through the SDAI presentation layer.
     *
     * @param results results value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class SaveGenerationResults(val results: List<AiGenerationResult>) : ImageToImageIntent
    /**
     * Carries `ViewGenerationResult` data through the SDAI presentation layer.
     *
     * @param result result value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class ViewGenerationResult(val result: AiGenerationResult) : ImageToImageIntent
    /**
     * Carries `ReportGenerationResult` data through the SDAI presentation layer.
     *
     * @param result result value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class ReportGenerationResult(val result: AiGenerationResult) : ImageToImageIntent
    /**
     * Carries `ShowEditTag` data through the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data class ShowEditTag(
        /**
         * Exposes the `prompt` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val prompt: String,
        /**
         * Exposes the `negativePrompt` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val negativePrompt: String,
        /**
         * Exposes the `tag` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val tag: String,
        /**
         * Exposes the `isNegative` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val isNegative: Boolean,
    ) : ImageToImageIntent
    /**
     * Carries `ApplyPrompts` data through the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data class ApplyPrompts(
        /**
         * Exposes the `prompt` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val prompt: String,
        /**
         * Exposes the `negativePrompt` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val negativePrompt: String,
    ) : ImageToImageIntent
    /**
     * Carries `ApplyGenerationResult` data through the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data class ApplyGenerationResult(
        /**
         * Exposes the `ai` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val ai: AiGenerationResult,
        /**
         * Exposes the `inputImage` value used by the SDAI presentation layer.
         *
         * @author Dmitriy Moroz
         */
        val inputImage: Boolean = false,
    ) : ImageToImageIntent
    /**
     * Carries `UpdateAdvancedOptionsVisibility` data through the SDAI presentation layer.
     *
     * @param visible visible value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateAdvancedOptionsVisibility(val visible: Boolean) : ImageToImageIntent
    /**
     * Carries `UpdatePrompt` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdatePrompt(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateNegativePrompt` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateNegativePrompt(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateWidth` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateWidth(val value: String) : ImageToImageIntent
    /**
     * Provides the `SwapDimensions` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object SwapDimensions : ImageToImageIntent
    /**
     * Carries `ApplyAspectRatio` data through the SDAI presentation layer.
     *
     * @param ratio ratio value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class ApplyAspectRatio(val ratio: GenerationAspectRatio) : ImageToImageIntent
    /**
     * Carries `UpdateHeight` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateHeight(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateSamplingSteps` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateSamplingSteps(val value: Int) : ImageToImageIntent
    /**
     * Carries `UpdateCfgScale` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateCfgScale(val value: Float) : ImageToImageIntent
    /**
     * Carries `UpdateRestoreFaces` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateRestoreFaces(val value: Boolean) : ImageToImageIntent
    /**
     * Carries `UpdateSeed` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateSeed(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateSubSeed` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateSubSeed(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateSubSeedStrength` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateSubSeedStrength(val value: Float) : ImageToImageIntent
    /**
     * Carries `UpdateSampler` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateSampler(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateScheduler` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateScheduler(val value: Scheduler) : ImageToImageIntent
    /**
     * Carries `UpdateNsfw` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateNsfw(val value: Boolean) : ImageToImageIntent
    /**
     * Carries `UpdateBatchCount` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateBatchCount(val value: Int) : ImageToImageIntent
    /**
     * Carries `UpdateOpenAiModel` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiModel(val value: OpenAiModel) : ImageToImageIntent
    /**
     * Carries `UpdateOpenAiSize` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiSize(val value: OpenAiSize) : ImageToImageIntent
    /**
     * Carries `UpdateOpenAiQuality` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiQuality(val value: OpenAiQuality) : ImageToImageIntent
    /**
     * Carries `UpdateFalAiModel` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateFalAiModel(val value: FalAiModel) : ImageToImageIntent
    /**
     * Carries `UpdateFalAiImageSize` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateFalAiImageSize(val value: FalAiImageSize) : ImageToImageIntent
    /**
     * Carries `UpdateFalAiAcceleration` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateFalAiAcceleration(val value: FalAiAcceleration) : ImageToImageIntent
    /**
     * Carries `UpdateFalAiSyncMode` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateFalAiSyncMode(val value: Boolean) : ImageToImageIntent
    /**
     * Carries `UpdateArliAiModel` data through the SDAI presentation layer.
     *
     * @param value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateArliAiModel(val value: String) : ImageToImageIntent
    /**
     * Carries `UpdateStabilityAiStyle` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateStabilityAiStyle(val value: StabilityAiStylePreset) : ImageToImageIntent
    /**
     * Carries `UpdateStabilityAiClipGuidance` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateStabilityAiClipGuidance(val value: StabilityAiClipGuidance) : ImageToImageIntent
    /**
     * Carries `UpdateADetailerConfig` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateADetailerConfig(val value: ADetailerConfig) : ImageToImageIntent
    /**
     * Provides the `RefreshADetailerAvailability` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object RefreshADetailerAvailability : ImageToImageIntent
    /**
     * Provides the `OpenADetailerInstallInstructions` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object OpenADetailerInstallInstructions : ImageToImageIntent
    /**
     * Carries `UpdateDenoisingStrength` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateDenoisingStrength(val value: Float) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintBrushSize` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintBrushSize(val value: Int) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintMaskBlur` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintMaskBlur(val value: Int) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintOnlyMaskedPadding` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintOnlyMaskedPadding(val value: Int) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintMaskMode` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintMaskMode(val value: ImageInPaintState.MaskMode) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintMaskContent` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintMaskContent(val value: ImageInPaintState.MaskContent) : ImageToImageIntent
    /**
     * Carries `UpdateInPaintArea` data through the SDAI presentation layer.
     *
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class UpdateInPaintArea(val value: ImageInPaintState.Area) : ImageToImageIntent
}

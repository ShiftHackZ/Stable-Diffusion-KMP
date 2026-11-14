package com.shifthackz.aisdv1.presentation.screen.txt2img

import com.shifthackz.aisdv1.core.mvi.MviIntent
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
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.presentation.widget.input.GenerationAspectRatio

/**
 * User actions and form updates emitted by the txt2img screen.
 *
 * Benchmark warning actions are modeled here with regular generation intents so
 * the view-model can gate local providers without coupling the shared input
 * form to benchmark-specific UI.
 */
sealed interface TextToImageIntent : MviIntent {
    /**
     * Retries an empty AUTOMATIC1111 sampler list after foregrounding or a user retry.
     *
     * @author Dmitriy Moroz
     */
    data object RetrySamplers : TextToImageIntent

    data object OpenDrawer : TextToImageIntent
    data object NavigateBack : TextToImageIntent
    data object ConfigureProvider : TextToImageIntent
    data object Generate : TextToImageIntent
    data object RunBenchmarkFromPrompt : TextToImageIntent
    data object SkipBenchmarkPrompt : TextToImageIntent
    data object ContinueAfterBenchmarkWarning : TextToImageIntent
    data object SuppressBenchmarkWarningAndContinue : TextToImageIntent
    data object DismissModal : TextToImageIntent
    data object CancelGeneration : TextToImageIntent
    data object DismissError : TextToImageIntent
    data object DismissMessage : TextToImageIntent
    data object DismissEditTag : TextToImageIntent
    data class SaveResult(val base64: String) : TextToImageIntent
    data class ShareResult(val base64: String) : TextToImageIntent
    data class SaveGenerationResults(val results: List<AiGenerationResult>) : TextToImageIntent
    data class ViewGenerationResult(val result: AiGenerationResult) : TextToImageIntent
    data class ReportGenerationResult(val result: AiGenerationResult) : TextToImageIntent
    data class ShowEditTag(
        val prompt: String,
        val negativePrompt: String,
        val tag: String,
        val isNegative: Boolean,
    ) : TextToImageIntent
    data class ApplyPrompts(
        val prompt: String,
        val negativePrompt: String,
    ) : TextToImageIntent
    data class ApplyGenerationResult(
        val ai: AiGenerationResult,
    ) : TextToImageIntent
    data class UpdateAdvancedOptionsVisibility(val visible: Boolean) : TextToImageIntent
    data class UpdatePrompt(val value: String) : TextToImageIntent
    data class UpdateNegativePrompt(val value: String) : TextToImageIntent
    data class UpdateWidth(val value: String) : TextToImageIntent
    data object SwapDimensions : TextToImageIntent
    data class ApplyAspectRatio(val ratio: GenerationAspectRatio) : TextToImageIntent
    data class UpdateHeight(val value: String) : TextToImageIntent
    data class UpdateSamplingSteps(val value: Int) : TextToImageIntent
    data class UpdateCfgScale(val value: Float) : TextToImageIntent
    data class UpdateRestoreFaces(val value: Boolean) : TextToImageIntent
    data class UpdateSeed(val value: String) : TextToImageIntent
    data class UpdateSubSeed(val value: String) : TextToImageIntent
    data class UpdateSubSeedStrength(val value: Float) : TextToImageIntent
    data class UpdateSampler(val value: String) : TextToImageIntent
    data class UpdateScheduler(val value: Scheduler) : TextToImageIntent
    data class UpdateForgeModules(val value: List<ForgeModule>) : TextToImageIntent
    data class UpdateNsfw(val value: Boolean) : TextToImageIntent
    data class UpdateBatchCount(val value: Int) : TextToImageIntent
    data class UpdateOpenAiModel(val value: OpenAiModel) : TextToImageIntent
    data class UpdateOpenAiSize(val value: OpenAiSize) : TextToImageIntent
    data class UpdateOpenAiQuality(val value: OpenAiQuality) : TextToImageIntent
    data class UpdateFalAiModel(val value: FalAiModel) : TextToImageIntent
    data class UpdateFalAiImageSize(val value: FalAiImageSize) : TextToImageIntent
    data class UpdateFalAiAcceleration(val value: FalAiAcceleration) : TextToImageIntent
    data class UpdateSdxlBackend(val value: SdxlBackend) : TextToImageIntent
    data class UpdateBonsaiBackend(val value: BonsaiBackend) : TextToImageIntent
    data class UpdateFalAiSyncMode(val value: Boolean) : TextToImageIntent
    data class UpdateOpenAiCompatibleModel(val value: String) : TextToImageIntent
    data class UpdateArliAiModel(val value: String) : TextToImageIntent
    data class UpdateStabilityAiStyle(val value: StabilityAiStylePreset) : TextToImageIntent
    data class UpdateStabilityAiClipGuidance(val value: StabilityAiClipGuidance) : TextToImageIntent
    data class UpdateHiresConfig(val value: HiresConfig) : TextToImageIntent
    data class UpdateADetailerConfig(val value: ADetailerConfig) : TextToImageIntent
    data object RefreshADetailerAvailability : TextToImageIntent
    data object OpenADetailerInstallInstructions : TextToImageIntent
}

package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
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

/**
 * Events emitted by the reusable generation input form.
 *
 * Screen-specific processors translate these into txt2img/img2img intents so
 * the form can stay shared while each screen decides how to persist settings,
 * validate dimensions, and handle provider-specific side effects.
 */
sealed interface GenerationInputFormEvent {
    /**
     * Requests another attempt to load an empty AUTOMATIC1111 sampler list.
     *
     * @author Dmitriy Moroz
     */
    data object RetrySamplers : GenerationInputFormEvent

    data class UpdateAdvancedOptionsVisibility(val visible: Boolean) : GenerationInputFormEvent
    data class UpdatePrompt(val value: String) : GenerationInputFormEvent
    data class UpdateNegativePrompt(val value: String) : GenerationInputFormEvent
    data class EditTag(
        val prompt: String,
        val negativePrompt: String,
        val tag: String,
        val isNegative: Boolean,
    ) : GenerationInputFormEvent

    data class UpdateWidth(val value: String) : GenerationInputFormEvent
    data object SwapDimensions : GenerationInputFormEvent
    data class ApplyAspectRatio(val ratio: GenerationAspectRatio) : GenerationInputFormEvent
    data class UpdateHeight(val value: String) : GenerationInputFormEvent
    data class UpdateSamplingSteps(val value: Int) : GenerationInputFormEvent
    data class UpdateCfgScale(val value: Float) : GenerationInputFormEvent
    data class UpdateRestoreFaces(val value: Boolean) : GenerationInputFormEvent
    data class UpdateSeed(val value: String) : GenerationInputFormEvent
    data class UpdateSubSeed(val value: String) : GenerationInputFormEvent
    data class UpdateSubSeedStrength(val value: Float) : GenerationInputFormEvent
    data class UpdateSampler(val value: String) : GenerationInputFormEvent
    data class UpdateScheduler(val value: Scheduler) : GenerationInputFormEvent
    data class UpdateForgeModules(val value: List<ForgeModule>) : GenerationInputFormEvent
    data class UpdateNsfw(val value: Boolean) : GenerationInputFormEvent
    data class UpdateBatch(val value: Int) : GenerationInputFormEvent
    data class UpdateOpenAiModel(val value: OpenAiModel) : GenerationInputFormEvent
    data class UpdateOpenAiSize(val value: OpenAiSize) : GenerationInputFormEvent
    data class UpdateOpenAiQuality(val value: OpenAiQuality) : GenerationInputFormEvent
    data class UpdateFalAiModel(val value: FalAiModel) : GenerationInputFormEvent
    data class UpdateFalAiImageSize(val value: FalAiImageSize) : GenerationInputFormEvent
    data class UpdateFalAiAcceleration(val value: FalAiAcceleration) : GenerationInputFormEvent
    data class UpdateSdxlBackend(val value: SdxlBackend) : GenerationInputFormEvent
    data class UpdateBonsaiBackend(val value: BonsaiBackend) : GenerationInputFormEvent
    data class UpdateFalAiSyncMode(val value: Boolean) : GenerationInputFormEvent
    data class UpdateOpenAiCompatibleModel(val value: String) : GenerationInputFormEvent
    data class UpdateArliAiModel(val value: String) : GenerationInputFormEvent
    data class UpdateStabilityAiStyle(val value: StabilityAiStylePreset) : GenerationInputFormEvent
    data class UpdateStabilityAiClipGuidance(val value: StabilityAiClipGuidance) : GenerationInputFormEvent
    data class UpdateHiresConfig(val value: HiresConfig) : GenerationInputFormEvent
    data class UpdateADetailerConfig(val value: ADetailerConfig) : GenerationInputFormEvent
    data object RefreshADetailerAvailability : GenerationInputFormEvent
    data object OpenADetailerInstallInstructions : GenerationInputFormEvent
}

/**
 * Preset aspect ratios applied by resizing the active width/height fields.
 */
enum class GenerationAspectRatio(
    val displayName: String,
    val width: Int,
    val height: Int,
) {
    SQUARE("1:1", 1, 1),
    PORTRAIT("2:3", 2, 3),
    PHOTO_PORTRAIT("3:4", 3, 4),
    WIDE("16:9", 16, 9),
    LANDSCAPE("3:2", 3, 2),
    PHOTO_LANDSCAPE("4:3", 4, 3),
}

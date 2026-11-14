package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.core.model.UiText
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
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset

/**
 * Shared state contract for generation controls.
 *
 * Txt2img and img2img screens implement this interface so the common form can
 * render provider-specific controls, including local runtime backend selectors,
 * without owning screen-level loading, result, or navigation state.
 */
interface GenerationInputFormState {
    val onBoardingDemo: Boolean
    val mode: ServerSource
    val advancedToggleButtonVisible: Boolean
    val advancedOptionsVisible: Boolean
    val formPromptTaggedInput: Boolean
    val prompt: String
    val negativePrompt: String
    val width: String
    val height: String
    val samplingSteps: Int
    val cfgScale: Float
    val restoreFaces: Boolean
    val seed: String
    val subSeed: String
    val subSeedStrength: Float
    val selectedSampler: String
    val selectedScheduler: Scheduler
    val availableForgeModules: List<ForgeModule>
    val selectedForgeModules: List<ForgeModule>
    val availableSamplers: List<String>
    val selectedStylePreset: StabilityAiStylePreset
    val selectedClipGuidancePreset: StabilityAiClipGuidance
    val openAiCompatibleModel: String
        get() = "gpt-image-2"
    val openAiModel: OpenAiModel
    val openAiSize: OpenAiSize
    val openAiQuality: OpenAiQuality
    val falAiModel: FalAiModel
    val falAiImageSize: FalAiImageSize
    val falAiAcceleration: FalAiAcceleration
    val falAiSyncMode: Boolean
    val sdxlBackend: SdxlBackend
    val bonsaiBackend: BonsaiBackend
    val bonsaiBackendSelectionVisible: Boolean
        get() = false
    val arliAiModels: List<String>
    val arliAiModel: String
    val widthValidationError: UiText?
    val heightValidationError: UiText?
    val nsfw: Boolean
    val batchCount: Int
    val hires: HiresConfig
    val aDetailer: ADetailerConfig
    val aDetailerAvailable: Boolean
    val aDetailerRefreshing: Boolean

    val promptKeywords: List<String>
        get() = prompt.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    val negativePromptKeywords: List<String>
        get() = negativePrompt.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}

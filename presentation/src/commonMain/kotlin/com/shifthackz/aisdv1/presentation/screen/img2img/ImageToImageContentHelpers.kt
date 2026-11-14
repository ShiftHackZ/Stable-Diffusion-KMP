@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.img2img

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.modal.history.InputHistoryBottomSheet
import com.shifthackz.aisdv1.presentation.model.ExtraType
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormEvent
import kotlin.math.roundToInt

/**
 * Maps shared generation form events into img2img intents.
 *
 * @return The corresponding screen action, or null for controls unsupported by img2img.
 * @author Dmitriy Moroz
 */
internal fun GenerationInputFormEvent.toImageToImageIntent(): ImageToImageIntent? = when (this) {
    GenerationInputFormEvent.RetrySamplers -> ImageToImageIntent.RetrySamplers
    is GenerationInputFormEvent.EditTag -> ImageToImageIntent.ShowEditTag(
        prompt = prompt,
        negativePrompt = negativePrompt,
        tag = tag,
        isNegative = isNegative,
    )
    is GenerationInputFormEvent.UpdateAdvancedOptionsVisibility ->
        ImageToImageIntent.UpdateAdvancedOptionsVisibility(visible)
    is GenerationInputFormEvent.UpdatePrompt -> ImageToImageIntent.UpdatePrompt(value)
    is GenerationInputFormEvent.UpdateNegativePrompt -> ImageToImageIntent.UpdateNegativePrompt(value)
    is GenerationInputFormEvent.UpdateWidth -> ImageToImageIntent.UpdateWidth(value)
    GenerationInputFormEvent.SwapDimensions -> ImageToImageIntent.SwapDimensions
    is GenerationInputFormEvent.ApplyAspectRatio -> ImageToImageIntent.ApplyAspectRatio(ratio)
    is GenerationInputFormEvent.UpdateHeight -> ImageToImageIntent.UpdateHeight(value)
    is GenerationInputFormEvent.UpdateSamplingSteps -> ImageToImageIntent.UpdateSamplingSteps(value)
    is GenerationInputFormEvent.UpdateCfgScale -> ImageToImageIntent.UpdateCfgScale(value)
    is GenerationInputFormEvent.UpdateRestoreFaces -> ImageToImageIntent.UpdateRestoreFaces(value)
    is GenerationInputFormEvent.UpdateSeed -> ImageToImageIntent.UpdateSeed(value)
    is GenerationInputFormEvent.UpdateSubSeed -> ImageToImageIntent.UpdateSubSeed(value)
    is GenerationInputFormEvent.UpdateSubSeedStrength -> ImageToImageIntent.UpdateSubSeedStrength(value)
    is GenerationInputFormEvent.UpdateSampler -> ImageToImageIntent.UpdateSampler(value)
    is GenerationInputFormEvent.UpdateScheduler -> ImageToImageIntent.UpdateScheduler(value)
    is GenerationInputFormEvent.UpdateForgeModules -> null
    is GenerationInputFormEvent.UpdateNsfw -> ImageToImageIntent.UpdateNsfw(value)
    is GenerationInputFormEvent.UpdateBatch -> ImageToImageIntent.UpdateBatchCount(value)
    is GenerationInputFormEvent.UpdateOpenAiModel -> ImageToImageIntent.UpdateOpenAiModel(value)
    is GenerationInputFormEvent.UpdateOpenAiSize -> ImageToImageIntent.UpdateOpenAiSize(value)
    is GenerationInputFormEvent.UpdateOpenAiQuality -> ImageToImageIntent.UpdateOpenAiQuality(value)
    is GenerationInputFormEvent.UpdateFalAiModel -> ImageToImageIntent.UpdateFalAiModel(value)
    is GenerationInputFormEvent.UpdateFalAiImageSize -> ImageToImageIntent.UpdateFalAiImageSize(value)
    is GenerationInputFormEvent.UpdateFalAiAcceleration -> ImageToImageIntent.UpdateFalAiAcceleration(value)
    is GenerationInputFormEvent.UpdateSdxlBackend -> null
    is GenerationInputFormEvent.UpdateBonsaiBackend -> null
    is GenerationInputFormEvent.UpdateFalAiSyncMode -> ImageToImageIntent.UpdateFalAiSyncMode(value)
    is GenerationInputFormEvent.UpdateOpenAiCompatibleModel -> null
    is GenerationInputFormEvent.UpdateArliAiModel -> ImageToImageIntent.UpdateArliAiModel(value)
    is GenerationInputFormEvent.UpdateStabilityAiStyle -> ImageToImageIntent.UpdateStabilityAiStyle(value)
    is GenerationInputFormEvent.UpdateStabilityAiClipGuidance ->
        ImageToImageIntent.UpdateStabilityAiClipGuidance(value)
    is GenerationInputFormEvent.UpdateHiresConfig -> null
    is GenerationInputFormEvent.UpdateADetailerConfig -> ImageToImageIntent.UpdateADetailerConfig(value)
    GenerationInputFormEvent.RefreshADetailerAvailability -> ImageToImageIntent.RefreshADetailerAvailability
    GenerationInputFormEvent.OpenADetailerInstallInstructions -> ImageToImageIntent.OpenADetailerInstallInstructions
}

internal fun String.appendPromptTag(tag: String): String =
    listOf(this, tag.trim())
        .filter(String::isNotBlank)
        .joinToString(", ")

internal fun flushPendingTaggedText(
    state: ImageToImageState,
    promptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    processIntent: (ImageToImageIntent) -> Unit,
) {
    promptChipTextFieldState.value.text
        .takeIf(String::isNotBlank)
        ?.let { state.prompt.appendPromptTag(it) }
        ?.let(ImageToImageIntent::UpdatePrompt)
        ?.let(processIntent)
        ?.also { promptChipTextFieldState.value = TextFieldValue("") }

    negativePromptChipTextFieldState.value.text
        .takeIf(String::isNotBlank)
        ?.let { state.negativePrompt.appendPromptTag(it) }
        ?.let(ImageToImageIntent::UpdateNegativePrompt)
        ?.let(processIntent)
        ?.also { negativePromptChipTextFieldState.value = TextFieldValue("") }
}

@Composable
internal fun GenerationHistoryDialog(
    onClose: () -> Unit,
    onGenerationSelected: (AiGenerationResult) -> Unit,
) {
    InputHistoryBottomSheet(
        onClose = onClose,
        onGenerationSelected = onGenerationSelected,
    )
}

internal sealed interface ImageToImagePanel {
    data object History : ImageToImagePanel
    data class Embeddings(
        val prompt: String,
        val negativePrompt: String,
    ) : ImageToImagePanel

    data class Extras(
        val prompt: String,
        val negativePrompt: String,
        val type: ExtraType,
    ) : ImageToImagePanel
}

internal val AiGenerationResult.aspectRatio: Float
    get() = if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 1f

internal fun Float.roundToString(): String {
    val rounded = (this * 100f).roundToInt() / 100f
    return rounded.toString()
}

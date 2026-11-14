@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.txt2img

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.modal.GenerationModalRenderer
import com.shifthackz.aisdv1.presentation.modal.embedding.EmbeddingScreen
import com.shifthackz.aisdv1.presentation.modal.extras.ExtrasScreen
import com.shifthackz.aisdv1.presentation.modal.history.InputHistoryBottomSheet
import com.shifthackz.aisdv1.presentation.modal.tag.EditTagDialog
import com.shifthackz.aisdv1.presentation.model.ExtraType
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.widget.toolbar.GenerationBottomToolbar
import com.shifthackz.aisdv1.presentation.widget.work.BackgroundWorkWidget

data class TextToImageStrings(
    val title: String = Localization.string("title_text_to_image"),
    val prompt: String = Localization.string("hint_prompt"),
    val negativePrompt: String = Localization.string("hint_prompt_negative"),
    val width: String = Localization.string("width"),
    val height: String = Localization.string("height"),
    val steps: String = Localization.string("gallery_info_field_sampling_steps"),
    val cfgScale: String = Localization.string("gallery_info_field_cfg"),
    val batch: String = Localization.string("hint_batch_tag"),
    val generate: String = Localization.string("action_generate"),
    val generating: String = Localization.string("notification_running_title"),
    val save: String = Localization.string("action_save"),
    val savingImage: String = Localization.string("message_image_saving"),
    val share: String = Localization.string("action_share_prompt"),
    val sharingImage: String = Localization.string("message_image_sharing"),
    val configureProvider: String = Localization.string("settings_item_config"),
    val sourceUnavailable: String = Localization.string("error_source_android_only"),
    val results: String = Localization.string("title_generation_results"),
    val imageUnavailable: String = Localization.string("message_image_data_received"),
    val resultMeta: (AiGenerationResult) -> String = { result ->
        Localization.string(
            "generation_result_meta",
            result.width,
            result.height,
            result.samplingSteps,
        )
    },
)

/**
 * Screen content for text-to-image generation.
 *
 * The composable owns transient modal selection and pending prompt-chip text,
 * while all durable state changes are sent back through [processIntent].
 */
@Composable
fun TextToImageContent(
    state: TextToImageState,
    processIntent: (TextToImageIntent) -> Unit,
    modifier: Modifier = Modifier,
    strings: TextToImageStrings = TextToImageStrings(),
    useDrawerNavigation: Boolean = false,
) {
    val promptChipTextFieldState = remember { mutableStateOf(TextFieldValue()) }
    val negativePromptChipTextFieldState = remember { mutableStateOf(TextFieldValue()) }
    var activeModal by remember { mutableStateOf<TextToImagePanel?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                processIntent(
                                    if (useDrawerNavigation) {
                                        TextToImageIntent.OpenDrawer
                                    } else {
                                        TextToImageIntent.NavigateBack
                                    },
                                )
                            },
                        ) {
                            Icon(
                                imageVector = if (useDrawerNavigation) {
                                    Icons.Default.Menu
                                } else {
                                    Icons.AutoMirrored.Filled.ArrowBack
                                },
                                contentDescription = null,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = strings.title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { activeModal = TextToImagePanel.History },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        }
                    },
                )
                BackgroundWorkWidget(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 4.dp),
                )
            }
        },
        bottomBar = {
            GenerationBottomToolbar(
                strokeAccentState = !state.hasValidationErrors,
                mode = state.mode,
                prompt = state.prompt,
                negativePrompt = state.negativePrompt,
                onExtraSelected = { prompt, negativePrompt, type ->
                    activeModal = TextToImagePanel.Extras(prompt, negativePrompt, type)
                },
                onEmbeddingsSelected = { prompt, negativePrompt ->
                    activeModal = TextToImagePanel.Embeddings(prompt, negativePrompt)
                },
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    enabled = !state.generating && !state.hasValidationErrors,
                    onClick = {
                        flushPendingTaggedText(
                            state = state,
                            promptChipTextFieldState = promptChipTextFieldState,
                            negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                            processIntent = processIntent,
                        )
                        processIntent(TextToImageIntent.Generate)
                    },
                ) {
                    if (state.generating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoFixNormal,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (state.generating) strings.generating else strings.generate,
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (state.loadingConfiguration) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                )
            } else {
                TextToImageBody(
                    state = state,
                    strings = strings,
                    promptChipTextFieldState = promptChipTextFieldState,
                    negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                    processIntent = processIntent,
                )
            }
        }
    }
    state.editTag?.let { editTag ->
        EditTagDialog(
            prompt = editTag.prompt,
            negativePrompt = editTag.negativePrompt,
            tag = editTag.tag,
            isNegative = editTag.isNegative,
            onDismissRequest = { processIntent(TextToImageIntent.DismissEditTag) },
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(TextToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
        )
    }
    when (val modal = activeModal) {
        null -> Unit
        is TextToImagePanel.Embeddings -> EmbeddingScreen(
            prompt = modal.prompt,
            negativePrompt = modal.negativePrompt,
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(TextToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
            onClose = { activeModal = null },
        )
        is TextToImagePanel.Extras -> ExtrasScreen(
            prompt = modal.prompt,
            negativePrompt = modal.negativePrompt,
            type = modal.type,
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(TextToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
            onClose = { activeModal = null },
        )
        TextToImagePanel.History -> GenerationHistoryDialog(
            onClose = { activeModal = null },
            onGenerationSelected = { ai ->
                processIntent(TextToImageIntent.ApplyGenerationResult(ai))
                activeModal = null
            },
        )
    }
    GenerationModalRenderer(
        screenModal = state.screenModal,
        onDismissRequest = { processIntent(TextToImageIntent.DismissModal) },
        onCancelGeneration = { processIntent(TextToImageIntent.CancelGeneration) },
        onSaveRequest = { processIntent(TextToImageIntent.SaveGenerationResults(it)) },
        onReportRequest = { processIntent(TextToImageIntent.ReportGenerationResult(it)) },
        onViewDetailRequest = { processIntent(TextToImageIntent.ViewGenerationResult(it)) },
        onBenchmarkRequest = { processIntent(TextToImageIntent.RunBenchmarkFromPrompt) },
        onSkipBenchmarkRequest = { processIntent(TextToImageIntent.SkipBenchmarkPrompt) },
        onBenchmarkContinueRequest = { processIntent(TextToImageIntent.ContinueAfterBenchmarkWarning) },
        onBenchmarkDoNotAskRequest = { processIntent(TextToImageIntent.SuppressBenchmarkWarningAndContinue) },
    )
}

/**
 * Maps shared generation form events into txt2img intents.
 *
 * @return The corresponding screen action, or null for controls unsupported by txt2img.
 * @author Dmitriy Moroz
 */
internal fun GenerationInputFormEvent.toTextToImageIntent(): TextToImageIntent? = when (this) {
    GenerationInputFormEvent.RetrySamplers -> TextToImageIntent.RetrySamplers
    is GenerationInputFormEvent.EditTag -> TextToImageIntent.ShowEditTag(
        prompt = prompt,
        negativePrompt = negativePrompt,
        tag = tag,
        isNegative = isNegative,
    )
    is GenerationInputFormEvent.UpdateAdvancedOptionsVisibility ->
        TextToImageIntent.UpdateAdvancedOptionsVisibility(visible)
    is GenerationInputFormEvent.UpdateBatch -> TextToImageIntent.UpdateBatchCount(value)
    is GenerationInputFormEvent.UpdateCfgScale -> TextToImageIntent.UpdateCfgScale(value)
    is GenerationInputFormEvent.ApplyAspectRatio -> TextToImageIntent.ApplyAspectRatio(ratio)
    GenerationInputFormEvent.SwapDimensions -> TextToImageIntent.SwapDimensions
    is GenerationInputFormEvent.UpdateHeight -> TextToImageIntent.UpdateHeight(value)
    is GenerationInputFormEvent.UpdateNegativePrompt -> TextToImageIntent.UpdateNegativePrompt(value)
    is GenerationInputFormEvent.UpdateNsfw -> TextToImageIntent.UpdateNsfw(value)
    is GenerationInputFormEvent.UpdateOpenAiModel -> TextToImageIntent.UpdateOpenAiModel(value)
    is GenerationInputFormEvent.UpdateOpenAiQuality -> TextToImageIntent.UpdateOpenAiQuality(value)
    is GenerationInputFormEvent.UpdateOpenAiSize -> TextToImageIntent.UpdateOpenAiSize(value)
    is GenerationInputFormEvent.UpdateFalAiModel -> TextToImageIntent.UpdateFalAiModel(value)
    is GenerationInputFormEvent.UpdateFalAiImageSize -> TextToImageIntent.UpdateFalAiImageSize(value)
    is GenerationInputFormEvent.UpdateFalAiAcceleration -> TextToImageIntent.UpdateFalAiAcceleration(value)
    is GenerationInputFormEvent.UpdateSdxlBackend -> TextToImageIntent.UpdateSdxlBackend(value)
    is GenerationInputFormEvent.UpdateBonsaiBackend -> TextToImageIntent.UpdateBonsaiBackend(value)
    is GenerationInputFormEvent.UpdateFalAiSyncMode -> TextToImageIntent.UpdateFalAiSyncMode(value)
    is GenerationInputFormEvent.UpdateOpenAiCompatibleModel -> TextToImageIntent.UpdateOpenAiCompatibleModel(value)
    is GenerationInputFormEvent.UpdateArliAiModel -> TextToImageIntent.UpdateArliAiModel(value)
    is GenerationInputFormEvent.UpdatePrompt -> TextToImageIntent.UpdatePrompt(value)
    is GenerationInputFormEvent.UpdateRestoreFaces -> TextToImageIntent.UpdateRestoreFaces(value)
    is GenerationInputFormEvent.UpdateSampler -> TextToImageIntent.UpdateSampler(value)
    is GenerationInputFormEvent.UpdateScheduler -> TextToImageIntent.UpdateScheduler(value)
    is GenerationInputFormEvent.UpdateForgeModules -> TextToImageIntent.UpdateForgeModules(value)
    is GenerationInputFormEvent.UpdateSamplingSteps -> TextToImageIntent.UpdateSamplingSteps(value)
    is GenerationInputFormEvent.UpdateSeed -> TextToImageIntent.UpdateSeed(value)
    is GenerationInputFormEvent.UpdateStabilityAiClipGuidance ->
        TextToImageIntent.UpdateStabilityAiClipGuidance(value)
    is GenerationInputFormEvent.UpdateStabilityAiStyle -> TextToImageIntent.UpdateStabilityAiStyle(value)
    is GenerationInputFormEvent.UpdateSubSeed -> TextToImageIntent.UpdateSubSeed(value)
    is GenerationInputFormEvent.UpdateSubSeedStrength -> TextToImageIntent.UpdateSubSeedStrength(value)
    is GenerationInputFormEvent.UpdateWidth -> TextToImageIntent.UpdateWidth(value)
    is GenerationInputFormEvent.UpdateHiresConfig -> TextToImageIntent.UpdateHiresConfig(value)
    is GenerationInputFormEvent.UpdateADetailerConfig -> TextToImageIntent.UpdateADetailerConfig(value)
    GenerationInputFormEvent.RefreshADetailerAvailability -> TextToImageIntent.RefreshADetailerAvailability
    GenerationInputFormEvent.OpenADetailerInstallInstructions -> TextToImageIntent.OpenADetailerInstallInstructions
}

@Composable
private fun GenerationHistoryDialog(
    onClose: () -> Unit,
    onGenerationSelected: (AiGenerationResult) -> Unit,
) {
    InputHistoryBottomSheet(
        onClose = onClose,
        onGenerationSelected = onGenerationSelected,
    )
}

private fun String.appendPromptTag(tag: String): String =
    listOf(this, tag.trim())
        .filter(String::isNotBlank)
        .joinToString(", ")

private fun flushPendingTaggedText(
    state: TextToImageState,
    promptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    processIntent: (TextToImageIntent) -> Unit,
) {
    promptChipTextFieldState.value.text
        .takeIf(String::isNotBlank)
        ?.let { state.prompt.appendPromptTag(it) }
        ?.let(TextToImageIntent::UpdatePrompt)
        ?.let(processIntent)
        ?.also { promptChipTextFieldState.value = TextFieldValue() }

    negativePromptChipTextFieldState.value.text
        .takeIf(String::isNotBlank)
        ?.let { state.negativePrompt.appendPromptTag(it) }
        ?.let(TextToImageIntent::UpdateNegativePrompt)
        ?.let(processIntent)
        ?.also { negativePromptChipTextFieldState.value = TextFieldValue() }
}

private sealed interface TextToImagePanel {
    data object History : TextToImagePanel
    data class Embeddings(
        val prompt: String,
        val negativePrompt: String,
    ) : TextToImagePanel
    data class Extras(
        val prompt: String,
        val negativePrompt: String,
        val type: ExtraType,
    ) : TextToImagePanel
}

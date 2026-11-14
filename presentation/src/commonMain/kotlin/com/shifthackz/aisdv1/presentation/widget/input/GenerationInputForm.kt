package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.widget.engine.EngineSelectionComponent
import com.shifthackz.aisdv1.presentation.widget.input.chip.ChipTextFieldWithItem

/**
 * Renders the `GenerationInputForm` UI for the SDAI presentation layer.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param state state rendered or processed by the component.
 * @param isImg2Img is img2 img value consumed by the API.
 * @param textFieldContainerColor text field container color value consumed by the API.
 * @param promptChipTextFieldState prompt chip text field state value consumed by the API.
 * @param negativePromptChipTextFieldState negative prompt chip text field state value consumed by the API.
 * @param onEvent callback invoked by the component.
 * @param afterSlidersSection after sliders section value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationInputForm(
    modifier: Modifier = Modifier,
    state: GenerationInputFormState,
    isImg2Img: Boolean = false,
    textFieldContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit = {},
    afterSlidersSection: @Composable () -> Unit = {},
) {
    Column(modifier = modifier) {
        if (!state.onBoardingDemo) {
            when (state.mode) {
                ServerSource.AUTOMATIC1111,
                ServerSource.SWARM_UI,
                ServerSource.STABILITY_AI,
                ServerSource.HUGGING_FACE,
                ServerSource.LOCAL_MICROSOFT_ONNX,
                ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                ServerSource.LOCAL_APPLE_CORE_ML,
                ServerSource.LOCAL_APPLE_BONSAI -> EngineSelectionComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )

                ServerSource.OPEN_AI -> DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = Localization.string("hint_model_open_ai").asUiText(),
                    value = state.openAiModel,
                    items = OpenAiModel.entries,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiModel(it)) },
                    displayDelegate = { it.alias.asUiText() },
                )

                ServerSource.FAL_AI -> DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = Localization.string("hint_fal_ai_endpoint").asUiText(),
                    value = state.falAiModel,
                    items = if (isImg2Img) {
                        FalAiModel.imageToImage
                    } else {
                        FalAiModel.textToImage
                    },
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateFalAiModel(it)) },
                    displayDelegate = { it.displayName.asUiText() },
                )

                ServerSource.ARLI_AI -> DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = Localization.string("hint_arli_ai_model").asUiText(),
                    value = state.arliAiModel.takeIf(String::isNotBlank),
                    items = state.arliAiModels.ifEmpty {
                        listOfNotNull(state.arliAiModel.takeIf(String::isNotBlank))
                    },
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateArliAiModel(it)) },
                )

                else -> Unit
            }

            if (state.mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP) {
                DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = Localization.string("hint_sdxl_backend").asUiText(),
                    value = state.sdxlBackend,
                    items = SdxlBackend.entries,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateSdxlBackend(it)) },
                    displayDelegate = { it.displayName.asUiText() },
                )
            }
            if (
                state.mode == ServerSource.LOCAL_APPLE_BONSAI &&
                state.bonsaiBackendSelectionVisible
            ) {
                DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = Localization.string("hint_bonsai_backend").asUiText(),
                    value = state.bonsaiBackend,
                    items = BonsaiBackend.entries,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateBonsaiBackend(it)) },
                    displayDelegate = { it.displayName.asUiText() },
                )
            }
        }
        if (state.mode == ServerSource.OPEN_AI_COMPATIBLE || state.mode == ServerSource.OPEN_AI_OAUTH) OpenAiCompatibleInputs(state, onEvent)
        if (state.formPromptTaggedInput) {
            ChipTextFieldWithItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textFieldValueState = promptChipTextFieldState,
                label = Localization.string("hint_prompt").asUiText(),
                list = state.promptKeywords,
                onItemClick = { _, tag ->
                    onEvent(
                        GenerationInputFormEvent.EditTag(
                            prompt = state.prompt,
                            negativePrompt = state.negativePrompt,
                            tag = tag,
                            isNegative = false,
                        )
                    )
                },
            ) { event ->
                val prompt = processTaggedPrompt(state.promptKeywords, event)
                onEvent(GenerationInputFormEvent.UpdatePrompt(prompt))
            }
        } else {
            PlatformOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = state.prompt,
                onValueChange = { onEvent(GenerationInputFormEvent.UpdatePrompt(it)) },
                label = Localization.string("hint_prompt"),
                containerColor = textFieldContainerColor,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Horde does not support "negative prompt"
        when (state.mode) {
            ServerSource.AUTOMATIC1111,
            ServerSource.SWARM_UI,
            ServerSource.HUGGING_FACE,
            ServerSource.STABILITY_AI,
            ServerSource.ARLI_AI,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI -> {
                if (state.formPromptTaggedInput) {
                    ChipTextFieldWithItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textFieldValueState = negativePromptChipTextFieldState,
                        label = Localization.string("hint_prompt_negative").asUiText(),
                        list = state.negativePromptKeywords,
                        onItemClick = { _, tag ->
                            onEvent(
                                GenerationInputFormEvent.EditTag(
                                    prompt = state.prompt,
                                    negativePrompt = state.negativePrompt,
                                    tag = tag,
                                    isNegative = true,
                                )
                            )
                        },
                    ) { event ->
                        val prompt = processTaggedPrompt(state.negativePromptKeywords, event)
                        onEvent(GenerationInputFormEvent.UpdateNegativePrompt(prompt))
                    }
                } else {
                    PlatformOutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.negativePrompt,
                        onValueChange = {
                            onEvent(GenerationInputFormEvent.UpdateNegativePrompt(it))
                        },
                        label = Localization.string("hint_prompt_negative"),
                        containerColor = textFieldContainerColor,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> Unit
        }

        val sizeInputVisible = state.mode != ServerSource.OPEN_AI_COMPATIBLE && state.mode != ServerSource.OPEN_AI_OAUTH &&
            (state.mode != ServerSource.FAL_AI || state.falAiModel.supportsImageSize)
        // Size input fields
        if (sizeInputVisible) Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val localModifier = Modifier.weight(1f)

            when (state.mode) {
                ServerSource.HORDE,
                ServerSource.LOCAL_MICROSOFT_ONNX,
                ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                ServerSource.LOCAL_APPLE_CORE_ML,
                ServerSource.LOCAL_APPLE_BONSAI -> {
                    DropdownTextField(
                        modifier = localModifier.padding(end = 4.dp),
                        label = Localization.string("width").asUiText(),
                        value = state.width,
                        items = GenerationInputFormConstants.sizes,
                        onItemSelected = { onEvent(GenerationInputFormEvent.UpdateWidth(it)) },
                    )
                    DropdownTextField(
                        modifier = localModifier.padding(start = 4.dp),
                        label = Localization.string("height").asUiText(),
                        value = state.height,
                        items = GenerationInputFormConstants.sizes,
                        onItemSelected = { onEvent(GenerationInputFormEvent.UpdateHeight(it)) },
                    )
                }

                ServerSource.AUTOMATIC1111,
                ServerSource.SWARM_UI,
                ServerSource.HUGGING_FACE,
                ServerSource.ARLI_AI,
                -> {
                    GenerationSizeTextFieldsComponent(modifier = localModifier, state = state, onEvent = onEvent)
                }

                ServerSource.FAL_AI -> {
                    DropdownTextField(
                        label = Localization.string("hint_image_size").asUiText(),
                        value = state.falAiImageSize,
                        items = FalAiImageSize.entries,
                        onItemSelected = { onEvent(GenerationInputFormEvent.UpdateFalAiImageSize(it)) },
                        displayDelegate = { it.displayName.asUiText() },
                    )
                }

                ServerSource.STABILITY_AI -> {
                    if (isImg2Img) Unit
                    else GenerationSizeTextFieldsComponent(modifier = localModifier, state = state, onEvent = onEvent)
                }

                ServerSource.OPEN_AI -> {
                    DropdownTextField(
                        label = Localization.string("hint_image_size").asUiText(),
                        value = state.openAiSize,
                        items = OpenAiSize.entries.filter {
                            it.supportedModels.contains(state.openAiModel)
                        },
                        onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiSize(it)) },
                        displayDelegate = { it.key.asUiText() },
                    )
                }
                else -> Unit
            }
        }

        if (state.mode == ServerSource.OPEN_AI) {
            DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_quality").asUiText(),
                value = state.openAiQuality,
                items = OpenAiQuality.entries,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiQuality(it)) },
                displayDelegate = { it.key.asUiText() },
            )
            GenerationBatchComponent(state = state, onEvent = onEvent)
        }

        if (state.advancedToggleButtonVisible && state.mode != ServerSource.OPEN_AI && state.mode != ServerSource.OPEN_AI_COMPATIBLE && state.mode != ServerSource.OPEN_AI_OAUTH) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    onEvent(
                        GenerationInputFormEvent.UpdateAdvancedOptionsVisibility(
                            !state.advancedOptionsVisible,
                        )
                    )
                },
            ) {
                Icon(
                    imageVector = if (state.advancedOptionsVisible) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
                Text(
                    text = Localization.string(
                        if (state.advancedOptionsVisible) "action_options_hide"
                        else "action_options_show",
                    )
                )
            }
        }

        GenerationInputAdvancedOptions(
            state = state,
            isImg2Img = isImg2Img,
            onEvent = onEvent,
            afterSlidersSection = afterSlidersSection,
        )
    }
}

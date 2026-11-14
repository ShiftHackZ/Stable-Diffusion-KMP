package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.presentation.theme.sliderColors
import com.shifthackz.aisdv1.presentation.theme.textFieldColors
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.CFG_SCALE_RANGE_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.CFG_SCALE_RANGE_MIN
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_ARLI_AI_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_MIN
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SUB_SEED_STRENGTH_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.SUB_SEED_STRENGTH_MIN
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Renders the `GenerationInputAdvancedOptions` UI for the SDAI presentation layer.
 *
 * @param state state rendered or processed by the component.
 * @param isImg2Img is img2 img value consumed by the API.
 * @param onEvent callback invoked by the component.
 * @param afterSlidersSection after sliders section value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationInputAdvancedOptions(
    state: GenerationInputFormState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
        AnimatedVisibility(
            visible = state.advancedOptionsVisible && state.mode != ServerSource.OPEN_AI && state.mode != ServerSource.OPEN_AI_COMPATIBLE && state.mode != ServerSource.OPEN_AI_OAUTH,
        ) {
            Column {
                // Sampler selection only supported for A1111, STABILITY AI
                when (state.mode) {
                    ServerSource.STABILITY_AI,
                    ServerSource.ARLI_AI,
                    ServerSource.AUTOMATIC1111 -> GenerationSamplerField(state, onEvent)

                    else -> Unit
                }
                // Style-preset only for Stablity AI
                if (state.mode == ServerSource.STABILITY_AI) {
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = Localization.string("hint_style_preset").asUiText(),
                        value = state.selectedStylePreset,
                        items = StabilityAiStylePreset.entries,
                        onItemSelected = {
                            onEvent(GenerationInputFormEvent.UpdateStabilityAiStyle(it))
                        },
                        displayDelegate = { value ->
                            if (value == StabilityAiStylePreset.NONE) {
                                Localization.string("hint_autodetect").asUiText()
                            } else {
                                value.key.asUiText()
                            }
                        },
                    )
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = Localization.string("hint_clip_guidance_preset").asUiText(),
                        value = state.selectedClipGuidancePreset,
                        items = StabilityAiClipGuidance.entries,
                        onItemSelected = {
                            onEvent(GenerationInputFormEvent.UpdateStabilityAiClipGuidance(it))
                        },
                    )
                }

                // Seed is not available for Hugging Face
                if (state.mode != ServerSource.OPEN_AI) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.seed,
                        onValueChange = { value ->
                            onEvent(GenerationInputFormEvent.UpdateSeed(value.filter { it.isDigit() }))
                        },
                        label = { Text(Localization.string("hint_seed")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Row {
                                if (state.seed.isNotEmpty()) {
                                    IconButton(onClick = {
                                        onEvent(GenerationInputFormEvent.UpdateSeed(""))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = Localization.string("action_clear"),
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    onEvent(
                                        GenerationInputFormEvent.UpdateSeed(
                                            "${Random.nextLong().absoluteValue}",
                                        ),
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = Localization.string("action_image_picker_random"),
                                    )
                                }
                            }
                        },
                        colors = textFieldColors,
                    )
                }
                if (state.mode == ServerSource.FAL_AI) {
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = Localization.string("hint_fal_ai_acceleration").asUiText(),
                        value = state.falAiAcceleration,
                        items = state.falAiModel.supportedAccelerations.toList(),
                        onItemSelected = {
                            onEvent(GenerationInputFormEvent.UpdateFalAiAcceleration(it))
                        },
                        displayDelegate = { it.displayName.asUiText() },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = state.falAiSyncMode,
                            onCheckedChange = {
                                onEvent(GenerationInputFormEvent.UpdateFalAiSyncMode(it))
                            },
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = Localization.string("hint_fal_ai_sync_mode"),
                        )
                    }
                }
                // NSFW flag maps to provider-specific safety settings.
                if (
                    state.mode == ServerSource.HORDE ||
                    state.mode == ServerSource.FAL_AI ||
                    state.mode == ServerSource.LOCAL_APPLE_CORE_ML
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = state.nsfw,
                            onCheckedChange = { onEvent(GenerationInputFormEvent.UpdateNsfw(it)) },
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = Localization.string("hint_nsfw"),
                        )
                    }
                }
                // Variation seed supported for A1111, SwarmUI
                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI -> OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.subSeed,
                        onValueChange = { value ->
                            onEvent(
                                GenerationInputFormEvent.UpdateSubSeed(
                                    value.filter { it.isDigit() },
                                ),
                            )
                        },
                        label = { Text(Localization.string("hint_sub_seed")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Row {
                                if (state.subSeed.isNotEmpty()) {
                                    IconButton(onClick = {
                                        onEvent(GenerationInputFormEvent.UpdateSubSeed(""))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = Localization.string("action_clear"),
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    onEvent(
                                        GenerationInputFormEvent.UpdateSubSeed(
                                            "${Random.nextLong().absoluteValue}",
                                        ),
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = Localization.string("action_image_picker_random"),
                                    )
                                }
                            }
                        },
                        colors = textFieldColors,
                    )

                    else -> Unit
                }
                // Sub-seed strength is not available for Local Diffusion
                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI,
                    ServerSource.HORDE -> {
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = Localization.string(
                                "hint_sub_seed_strength",
                                "${state.subSeedStrength.roundTo(2)}",
                            ),
                        )
                        SliderTextInputField(
                            value = state.subSeedStrength,
                            valueRange = SUB_SEED_STRENGTH_MIN..SUB_SEED_STRENGTH_MAX,
                            valueDiff = 0.01f,
                            sliderColors = sliderColors,
                            onValueChange = {
                                onEvent(GenerationInputFormEvent.UpdateSubSeedStrength(it))
                            },
                        )
                    }

                    else -> Unit
                }

                // Steps not available for providers with fixed sampler presets.
                if (state.mode != ServerSource.OPEN_AI) {
                    val stepsMin = if (state.mode == ServerSource.FAL_AI) {
                        state.falAiModel.minInferenceSteps
                    } else {
                        SAMPLING_STEPS_RANGE_MIN
                    }
                    val stepsMax = when (state.mode) {
                        ServerSource.LOCAL_MICROSOFT_ONNX -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.LOCAL_APPLE_BONSAI -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.STABILITY_AI -> SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
                        ServerSource.FAL_AI -> state.falAiModel.maxInferenceSteps
                        ServerSource.ARLI_AI -> SAMPLING_STEPS_RANGE_ARLI_AI_MAX
                        else -> SAMPLING_STEPS_RANGE_MAX
                    }
                    val steps = state.samplingSteps.coerceIn(stepsMin, stepsMax)
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = Localization.string("hint_sampling_steps", "$steps"),
                    )
                    SliderTextInputField(
                        value = steps * 1f,
                        valueRange = (stepsMin * 1f)..(stepsMax * 1f),
                        valueDiff = 1f,
                        steps = (abs(stepsMax - stepsMin) - 1).coerceAtLeast(0),
                        sliderColors = sliderColors,
                        fractionDigits = 0,
                        onValueChange = {
                            onEvent(
                                GenerationInputFormEvent.UpdateSamplingSteps(it.roundToInt()),
                            )
                        },
                    )
                }

                // CFG scale not available on providers with fixed guidance presets.
                when (state.mode) {
                    ServerSource.OPEN_AI,
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> Unit
                    else -> {
                        val cfgMin = if (state.mode == ServerSource.FAL_AI) {
                            state.falAiModel.minGuidanceScale
                        } else {
                            CFG_SCALE_RANGE_MIN * 1f
                        }
                        val cfgMax = if (state.mode == ServerSource.FAL_AI) {
                            state.falAiModel.maxGuidanceScale
                        } else {
                            CFG_SCALE_RANGE_MAX * 1f
                        }
                        val cfgScale = state.cfgScale.coerceIn(cfgMin, cfgMax)
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = Localization.string(
                                "hint_cfg_scale",
                                "${cfgScale.roundTo(2)}",
                            ),
                        )
                        SliderTextInputField(
                            value = cfgScale,
                            valueRange = cfgMin..cfgMax,
                            valueDiff = 0.5f,
                            steps = ((abs(cfgMax - cfgMin) * 2).roundToInt() - 1).coerceAtLeast(0),
                            sliderColors = sliderColors,
                            onValueChange = {
                                onEvent(GenerationInputFormEvent.UpdateCfgScale(it))
                            },
                        )
                    }
                }

                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI,
                    ServerSource.STABILITY_AI,
                    ServerSource.ARLI_AI,
                    ServerSource.HORDE,
                    ServerSource.LOCAL_APPLE_CORE_ML,
                    ServerSource.LOCAL_APPLE_BONSAI -> afterSlidersSection()

                    else -> Unit
                }

                if (state.mode == ServerSource.AUTOMATIC1111) {
                    GenerationInputA1111Options(
                        state = state,
                        isImg2Img = isImg2Img,
                        onEvent = onEvent,
                    )
                }
                // Batch is not available for any Local
                when (state.mode) {
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                    ServerSource.LOCAL_MICROSOFT_ONNX,
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                    ServerSource.LOCAL_APPLE_BONSAI,
                    -> Unit
                    else -> GenerationBatchComponent(state = state, onEvent = onEvent)
                }
                //Restore faces available only for A1111
                if (state.mode == ServerSource.AUTOMATIC1111) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = state.restoreFaces,
                            onCheckedChange = {
                                onEvent(GenerationInputFormEvent.UpdateRestoreFaces(it))
                            },
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = Localization.string("hint_restore_faces"),
                        )
                    }
                }
            }
        }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsState
import com.shifthackz.aisdv1.presentation.screen.settings.model.label
import com.shifthackz.aisdv1.presentation.screen.settings.model.shortTitle
import com.shifthackz.aisdv1.presentation.screen.settings.model.text
import com.shifthackz.aisdv1.presentation.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.widget.source.BetaBadge

/**
 * Server and app settings section, including usage summary entries that open standalone screens.
 *
 * @param state Settings state that provides provider, usage summary, and loading values.
 * @param headerModifier Modifier shared by Settings section headers.
 * @param itemModifier Modifier shared by Settings rows.
 * @param warningModifier Modifier used by the background-generation warning copy.
 * @param backgroundGenerationWarningKey Localization key for the current warning copy.
 * @param processIntent Intent sink used to route row actions back to SettingsViewModel.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsServerSection(
    state: SettingsState,
    headerModifier: Modifier,
    itemModifier: Modifier,
    warningModifier: Modifier,
    backgroundGenerationWarningKey: String,
    processIntent: (SettingsIntent) -> Unit,
) {
        if (!state.onBoardingDemo) {
            SettingsHeader(
                modifier = headerModifier,
                loading = state.loading,
                text = text("settings_header_server"),
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.SettingsEthernet,
                text = text("settings_item_config"),
                endValueText = state.serverSource.shortTitle().asUiText(),
                onClick = { processIntent(SettingsIntent.NavigateConfiguration) },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.Speed,
                text = text("settings_item_benchmark"),
                textSuffixContent = { BetaBadge() },
                onClick = { processIntent(SettingsIntent.NavigateBenchmark) },
            )
            if (state.showStabilityAiCredits) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    enabled = false,
                    startIcon = Icons.Default.Circle,
                    text = text("settings_item_stability_ai_credits"),
                    endValueText = state.stabilityAiCredits.roundTo(4).toString().asUiText(),
                )
            }
            if (state.showGenerationTimeout) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.Timer,
                    text = text("settings_generation_timeout"),
                    endValueText = state.generationRequestTimeout.label().asUiText(),
                    onClick = { processIntent(SettingsIntent.PickGenerationTimeout) },
                )
            }
            if (state.showSdModelSelector) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.AutoFixNormal,
                    text = text("settings_item_sd_model"),
                    endValueText = state.sdModelSelected.asUiText(),
                    onClick = { processIntent(SettingsIntent.SdModel.OpenChooser) },
                )
            }
            if (state.showLocalMicrosoftONNXUseNNAPI) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.AccountTree,
                    text = text("settings_item_local_nnapi"),
                    endValueText = state.sdModelSelected.asUiText(),
                    onClick = { processIntent(SettingsIntent.UpdateFlag.NNAPI(!state.localUseNNAPI)) },
                    endValueContent = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = state.localUseNNAPI,
                            onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.NNAPI(it)) },
                        )
                    },
                )
                AnimatedVisibility(visible = !state.loading) {
                    Text(
                        modifier = warningModifier,
                        text = Localization.string("settings_item_local_nnapi_warning"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            AnimatedVisibility(visible = state.loading || state.backgroundGenerationAvailable) {
                Column {
                    SettingsItem(
                        modifier = itemModifier,
                        loading = state.loading,
                        startIcon = Icons.Default.MiscellaneousServices,
                        text = text("settings_item_background_generation"),
                        textSuffixContent = { BetaBadge() },
                        onClick = {
                            processIntent(SettingsIntent.UpdateFlag.BackgroundGeneration(!state.backgroundGeneration))
                        },
                        endValueContent = {
                            Switch(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                checked = state.backgroundGeneration,
                                onCheckedChange = {
                                    processIntent(SettingsIntent.UpdateFlag.BackgroundGeneration(it))
                                },
                            )
                        },
                    )
                    AnimatedVisibility(visible = !state.loading) {
                        Text(
                            modifier = warningModifier,
                            text = Localization.string(backgroundGenerationWarningKey),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            AnimatedVisibility(visible = !state.loading && state.developerMode) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.DeveloperMode,
                    text = text("title_debug_menu"),
                    onClick = { processIntent(SettingsIntent.NavigateDeveloperMode) },
                )
            }

            SettingsHeader(
                modifier = headerModifier,
                loading = state.loading,
                text = text("settings_header_app"),
            )
            if (state.showMonitorConnectionOption) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.Refresh,
                    text = text("settings_item_monitor_connection"),
                    onClick = {
                        processIntent(SettingsIntent.UpdateFlag.MonitorConnection(!state.monitorConnectivity))
                    },
                    endValueContent = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = state.monitorConnectivity,
                            onCheckedChange = {
                                processIntent(SettingsIntent.UpdateFlag.MonitorConnection(it))
                            },
                        )
                    },
                )
            }
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.Save,
                text = (Localization.string("settings_item_auto_save") +
                    if (state.backgroundGeneration) "*" else "").asUiText(),
                onClick = {
                    processIntent(SettingsIntent.UpdateFlag.AutoSaveResult(!state.autoSaveAiResults))
                },
                endValueContent = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = state.autoSaveAiResults,
                        onCheckedChange = {
                            processIntent(SettingsIntent.UpdateFlag.AutoSaveResult(it))
                        },
                    )
                },
            )
            AnimatedVisibility(
                visible = !state.loading && state.backgroundGeneration,
            ) {
                Text(
                    modifier = warningModifier,
                    text = Localization.string("settings_item_auto_save_warning"),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.Folder,
                text = text("settings_item_auto_save_media_store"),
                onClick = {
                    processIntent(SettingsIntent.UpdateFlag.SaveToMediaStore(!state.saveToMediaStore))
                },
                endValueContent = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = state.saveToMediaStore,
                        onCheckedChange = {
                            processIntent(SettingsIntent.UpdateFlag.SaveToMediaStore(it))
                        },
                    )
                },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.Tag,
                text = text("settings_item_tagged_input"),
                onClick = {
                    processIntent(SettingsIntent.UpdateFlag.TaggedInput(!state.formPromptTaggedInput))
                },
                endValueContent = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = state.formPromptTaggedInput,
                        onCheckedChange = {
                            processIntent(SettingsIntent.UpdateFlag.TaggedInput(it))
                        },
                    )
                },
            )
            if (state.showFormAdvancedOption) {
                SettingsItem(
                    modifier = itemModifier,
                    loading = state.loading,
                    startIcon = Icons.Default.DynamicForm,
                    text = text("settings_item_advanced_form_default"),
                    onClick = {
                        processIntent(SettingsIntent.UpdateFlag.AdvancedFormVisibility(!state.formAdvancedOptionsAlwaysShow))
                    },
                    endValueContent = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = state.formAdvancedOptionsAlwaysShow,
                            onCheckedChange = {
                                processIntent(SettingsIntent.UpdateFlag.AdvancedFormVisibility(it))
                            },
                        )
                    },
                )
            }
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.Storage,
                text = text("settings_item_storage_usage"),
                endValueText = state.storageUsageBytes
                    .takeIf { it > 0L }
                    ?.formatSettingsUsageBytes()
                    .orEmpty()
                    .asUiText(),
                onClick = {
                    processIntent(SettingsIntent.NavigateStorageUsage)
                },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = Icons.Default.DataUsage,
                text = text("settings_item_network_usage"),
                endValueText = state.networkUsageBytes
                    .takeIf { it > 0L }
                    ?.formatSettingsUsageBytes()
                    .orEmpty()
                    .asUiText(),
                onClick = {
                    processIntent(SettingsIntent.NavigateNetworkUsage)
                },
            )
        }
}

/**
 * Compact byte formatter for Settings list row summaries.
 *
 * @receiver Raw byte count to render as a short Settings list value.
 *
 * @author Dmitriy Moroz
 */
private fun Long.formatSettingsUsageBytes(): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var unitIndex = 0
    var display = toDouble()
    while (display >= 1000.0 && unitIndex < units.lastIndex) {
        display /= 1000.0
        unitIndex++
    }
    return if (unitIndex == 0 || display >= 100.0) {
        "${display.toLong()} ${units[unitIndex]}"
    } else {
        "${(display * 10.0).toInt() / 10.0} ${units[unitIndex]}"
    }
}

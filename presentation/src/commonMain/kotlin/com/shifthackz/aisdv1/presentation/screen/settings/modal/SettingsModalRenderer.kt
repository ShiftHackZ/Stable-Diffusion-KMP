@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.modal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.modal.grid.GridBottomSheet
import com.shifthackz.aisdv1.presentation.modal.language.LanguageBottomSheet
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsModal
import com.shifthackz.aisdv1.presentation.screen.settings.model.text
import com.shifthackz.aisdv1.presentation.screen.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.widget.dialog.InfoDialog
import com.shifthackz.aisdv1.presentation.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.widget.input.DropdownTextField
import kotlinx.coroutines.launch

@Composable
internal fun SettingsModalRenderer(
    screenModal: SettingsModal,
    platformActions: SettingsPlatformActions,
    processIntent: (SettingsIntent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        processIntent(SettingsIntent.DismissDialog)
    }
    when (screenModal) {
        SettingsModal.None -> Unit
        SettingsModal.Communicating -> ProgressDialog(
            canDismiss = false,
        )

        SettingsModal.ClearAppCache -> DecisionInteractiveDialog(
            title = text("title_clear_app_cache"),
            text = text("interaction_cache_sub_title"),
            confirmActionText = text("yes"),
            dismissActionText = text("no"),
            onDismissRequest = dismiss,
            onConfirmAction = { processIntent(SettingsIntent.Action.ClearAppCache.Confirm) },
        )

        is SettingsModal.SelectSdModel -> {
            androidx.compose.runtime.key(screenModal.models, screenModal.selected) {
                var selectedItem = remember(screenModal.selected) {
                    androidx.compose.runtime.mutableStateOf(screenModal.selected)
                }
                DecisionInteractiveDialog(
                    title = text("title_select_sd_model"),
                    text = UiText.empty,
                    confirmActionText = text("action_select"),
                    onConfirmAction = {
                        processIntent(SettingsIntent.SdModel.Select(selectedItem.value))
                    },
                    onDismissRequest = dismiss,
                    content = {
                        DropdownTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = text("hint_sd_model"),
                            value = selectedItem.value,
                            items = screenModal.models,
                            onItemSelected = { selectedItem.value = it },
                        )
                    },
                )
            }
        }

        SettingsModal.Language -> ModalBottomSheet(
            onDismissRequest = dismiss,
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            LanguageBottomSheet(
                onLanguageSelected = { processIntent(SettingsIntent.Action.SetLanguage(it)) },
                onDismissRequest = dismiss,
            )
        }

        is SettingsModal.GalleryGrid -> ModalBottomSheet(
            onDismissRequest = dismiss,
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            GridBottomSheet(
                currentGrid = screenModal.grid,
                onSelected = {
                    processIntent(SettingsIntent.Action.GalleryGrid.Set(it))
                    dismiss()
                },
            )
        }

        is SettingsModal.GenerationTimeout -> ModalBottomSheet(
            onDismissRequest = dismiss,
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            NetworkTimeoutBottomSheet(
                selected = screenModal.selected,
                titleKey = "settings_generation_timeout",
                descriptionKey = "settings_generation_timeout_description",
                onSelected = { processIntent(SettingsIntent.SetGenerationTimeout(it)) },
            )
        }

        is SettingsModal.ConnectionTimeout -> ModalBottomSheet(
            onDismissRequest = dismiss,
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            NetworkTimeoutBottomSheet(
                selected = screenModal.selected,
                titleKey = "settings_connection_timeout",
                descriptionKey = if (screenModal.supported) {
                    "settings_connection_timeout_description"
                } else {
                    "settings_connection_timeout_ios_description"
                },
                onSelected = { processIntent(SettingsIntent.SetConnectionTimeout(it)) },
            )
        }

        is SettingsModal.ManualPermission -> InfoDialog(
            title = text("premission_rationale_title"),
            subTitle = Localization.string(
                "premission_rationale_sub_title",
                screenModal.permission,
            ).asUiText(),
            onDismissRequest = {
                dismiss()
                scope.launch { platformActions.openAppSettings() }
            },
        )
    }
}

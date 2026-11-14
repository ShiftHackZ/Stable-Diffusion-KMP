@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.screen.setup.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.bonsai.BonsaiModelSupport
import com.shifthackz.aisdv1.domain.feature.coreml.CoreMlModelSupport
import com.shifthackz.aisdv1.presentation.modal.download.DownloadDialog
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.widget.icon.BrandIcons
import com.shifthackz.aisdv1.presentation.widget.dialog.ErrorDialogContent
import com.shifthackz.aisdv1.presentation.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem

@Composable
internal fun SettingsAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    SettingsItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        startIcon = icon,
        text = text.asUiText(),
        onClick = onClick,
    )
}

@Composable
internal fun SwitchRow(
    text: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        startIcon = icon,
        text = text.asUiText(),
        showChevron = false,
        onClick = { onCheckedChange(!checked) },
        endValueContent = {
            Switch(
                modifier = Modifier.padding(horizontal = 8.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
internal fun ServerSetupModal(
    modal: ServerSetupState.Modal,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    when (modal) {
        ServerSetupState.Modal.ConnectionUnverified -> AlertDialog(
            onDismissRequest = { processIntent(ServerSetupIntent.AcknowledgeUnverifiedConnection) },
            title = { Text(strings.connectionUnverifiedTitle) },
            text = { Text(strings.connectionUnverifiedText) },
            confirmButton = {
                TextButton(onClick = { processIntent(ServerSetupIntent.AcknowledgeUnverifiedConnection) }) {
                    Text(strings.ok)
                }
            },
        )
        ServerSetupState.Modal.None -> Unit
        ServerSetupState.Modal.SourceFilters -> Unit
        ServerSetupState.Modal.SourceSort -> Unit
        ServerSetupState.Modal.Communicating -> ProgressDialog(
            title = strings.communicatingTitle.asUiText(),
            subTitle = strings.communicatingSubtitle.asUiText(),
            onDismissRequest = {},
        )

        ServerSetupState.Modal.ConnectLocalHost -> AlertDialog(
            onDismissRequest = {
                processIntent(ServerSetupIntent.DismissDialog)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        processIntent(ServerSetupIntent.ConnectToLocalHost)
                    },
                ) {
                    Text(text = strings.connect)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        processIntent(ServerSetupIntent.DismissDialog)
                    },
                ) {
                    Text(text = strings.cancel)
                }
            },
            title = { Text(text = strings.connectLocalHostTitle) },
            text = { Text(text = strings.connectLocalHostText) },
        )

        is ServerSetupState.Modal.Error -> ErrorDialogContent(
            title = strings.errorTitle,
            text = modal.message,
            okText = strings.ok,
            onDismissRequest = {
                processIntent(ServerSetupIntent.DismissDialog)
            },
        )

        is ServerSetupState.Modal.DeleteLocalModelConfirm -> AlertDialog(
            onDismissRequest = {
                processIntent(ServerSetupIntent.DismissDialog)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        processIntent(ServerSetupIntent.LocalModel.DeleteConfirm(modal.model))
                    },
                ) {
                    Text(text = Localization.string("yes"))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        processIntent(ServerSetupIntent.DismissDialog)
                    },
                ) {
                    Text(text = Localization.string("no"))
                }
            },
            title = { Text(text = strings.deleteLocalModelTitle) },
            text = {
                Text(
                    text = Localization.string(
                        "interaction_delete_local_model_sub_title",
                        modal.model.name,
                    ),
                )
            },
        )

        is ServerSetupState.Modal.SelectDownloadSource -> DownloadDialog(
            modelId = modal.modelId,
            onDismissRequest = {
                processIntent(ServerSetupIntent.DismissDialog)
            },
            onDownloadSourceSelected = { url ->
                processIntent(ServerSetupIntent.LocalModel.DownloadConfirm(modal.modelId, url))
            },
        )
    }
}

internal fun ServerSource.icon(platform: Platform): ImageVector = when (this) {
        ServerSource.AUTOMATIC1111,
        ServerSource.SWARM_UI,
        ServerSource.OPEN_AI_COMPATIBLE,
        -> Icons.Default.Computer

        ServerSource.HORDE,
        ServerSource.HUGGING_FACE,
        ServerSource.OPEN_AI,
        ServerSource.OPEN_AI_OAUTH,
        ServerSource.STABILITY_AI,
        ServerSource.FAL_AI,
        ServerSource.ARLI_AI,
        -> Icons.Default.Cloud

        ServerSource.LOCAL_MICROSOFT_ONNX,
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
        -> Icons.Default.Android

        ServerSource.LOCAL_APPLE_CORE_ML,
        -> BrandIcons.Apple

        ServerSource.LOCAL_APPLE_BONSAI -> when (platform) {
            Platform.ANDROID -> Icons.Default.Android
            Platform.IOS -> BrandIcons.Apple
        }
    }

internal fun ServerSource.title(strings: ServerSetupStrings, platform: Platform): String = when (this) {
    ServerSource.AUTOMATIC1111 -> strings.automaticTitle
    ServerSource.SWARM_UI -> strings.swarmTitle
    ServerSource.HORDE -> strings.hordeTitle
    ServerSource.HUGGING_FACE -> strings.huggingFaceTitle
    ServerSource.OPEN_AI_COMPATIBLE -> strings.openAiCompatibleTitle
    ServerSource.OPEN_AI_OAUTH -> Localization.string("srv_type_open_ai_oauth")
    ServerSource.OPEN_AI -> strings.openAiTitle
    ServerSource.STABILITY_AI -> strings.stabilityTitle
    ServerSource.FAL_AI -> strings.falAiTitle
    ServerSource.ARLI_AI -> strings.arliAiTitle
    ServerSource.LOCAL_MICROSOFT_ONNX -> strings.localDiffusionTitle
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> strings.mediaPipeTitle
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> strings.sdxlTitle
    ServerSource.LOCAL_APPLE_CORE_ML -> strings.coreMlTitle
    ServerSource.LOCAL_APPLE_BONSAI -> when (platform) {
        Platform.ANDROID -> strings.bonsaiAndroidTitle
        Platform.IOS -> strings.bonsaiTitle
    }
}

internal fun ServerSource.subtitle(strings: ServerSetupStrings, platform: Platform): String = when (this) {
    ServerSource.AUTOMATIC1111 -> strings.automaticSubtitle
    ServerSource.SWARM_UI -> strings.swarmSubtitle
    ServerSource.HORDE -> strings.hordeSubtitle
    ServerSource.HUGGING_FACE -> strings.huggingFaceSubtitle
    ServerSource.OPEN_AI_COMPATIBLE -> strings.openAiCompatibleSubtitle
    ServerSource.OPEN_AI_OAUTH -> Localization.string("open_ai_oauth_help")
    ServerSource.OPEN_AI -> strings.openAiSubtitle
    ServerSource.STABILITY_AI -> strings.stabilitySubtitle
    ServerSource.FAL_AI -> strings.falAiSubtitle
    ServerSource.ARLI_AI -> strings.arliAiSubtitle
    ServerSource.LOCAL_MICROSOFT_ONNX -> strings.localDiffusionSubtitle
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> strings.mediaPipeSubtitle
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> strings.sdxlSubtitle
    ServerSource.LOCAL_APPLE_CORE_ML -> strings.coreMlSubtitle
    ServerSource.LOCAL_APPLE_BONSAI -> when (platform) {
        Platform.ANDROID -> strings.bonsaiAndroidSubtitle
        Platform.IOS -> strings.bonsaiSubtitle
    }
}

internal fun ServerSetupState.ValidationError.message(
    strings: ServerSetupStrings,
): String = when (this) {
    ServerSetupState.ValidationError.EmptyField -> Localization.string("error_empty_field")
    ServerSetupState.ValidationError.EmptyUrl -> Localization.string("error_empty_url")
    ServerSetupState.ValidationError.InvalidScheme -> Localization.string("error_invalid_scheme")
    ServerSetupState.ValidationError.InvalidPort -> Localization.string("error_invalid_port")
    ServerSetupState.ValidationError.InvalidUrl -> Localization.string("error_invalid_url")
}

internal val ServerSetupState.mainButtonEnabled: Boolean
    get() = when (step) {
        ServerSetupState.Step.SOURCE -> true
        ServerSetupState.Step.CONFIGURE -> when (mode) {
            ServerSource.OPEN_AI_OAUTH -> openAiOAuthAccount != null && !openAiOAuthBusy
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxCustomModel ||
                localOnnxModels.any { it.selected && it.downloaded }

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> localMediaPipeCustomModel ||
                localMediaPipeModels.any { it.selected && it.downloaded }

            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlCustomModel ||
                localSdxlModels.any { it.selected && it.downloaded }

            ServerSource.LOCAL_APPLE_CORE_ML -> localCoreMlModels.any { model ->
                model.selected &&
                    model.downloaded &&
                    model.id !in CoreMlModelSupport.unsupportedModelIds
            }

            ServerSource.LOCAL_APPLE_BONSAI -> localBonsaiModels.any { model ->
                model.selected &&
                    model.downloaded &&
                    model.id !in BonsaiModelSupport.unsupportedModelIds
            }

            else -> true
        }
    }

internal val ServerSetupState.LocalModel.isCustom: Boolean
    get() = id == LocalAiModel.CustomOnnx.id ||
        id == LocalAiModel.CustomMediaPipe.id ||
        id == LocalAiModel.CustomSdxl.id ||
        id == LocalAiModel.CustomCoreMl.id ||
        id == LocalAiModel.CustomBonsai.id

package com.shifthackz.aisdv1.presentation.screen.setup.form.local

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Landslide
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.file.LOCAL_DIFFUSION_CUSTOM_PATH
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.setup.component.SwitchRow
import com.shifthackz.aisdv1.presentation.screen.setup.component.isCustom
import com.shifthackz.aisdv1.presentation.screen.setup.component.message
import com.shifthackz.aisdv1.presentation.screen.setup.component.subtitle
import com.shifthackz.aisdv1.presentation.screen.setup.component.title
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.ArliAiForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.Automatic1111Form
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.FalAiForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.FormTitle
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.HintText
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.HordeForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.HuggingFaceForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.OpenAiCompatibleForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.OpenAiForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.OpenAiOAuthForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.SetupTextField
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.StabilityAiForm
import com.shifthackz.aisdv1.presentation.screen.setup.form.remote.SwarmUiForm
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.platform.ServerSetupLocalPathPickerButton
import com.shifthackz.aisdv1.presentation.screen.setup.platform.isLocalGenerationSetupAvailable
import com.shifthackz.aisdv1.presentation.widget.scrollbar.verticalScrollbar

/**
 * Scrolls the selected provider's configuration form above the wizard's pinned actions.
 * @param state Current provider and editable configuration.
 * @param strings Shared setup labels.
 * @param listState Scroll position retained across recompositions.
 * @param processIntent Dispatches form changes through MVI.
 * @param modifier Scaffold-inset content bounds.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ConfigurationStep(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    listState: LazyListState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.verticalScrollbar(listState),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            when (state.mode) {
                ServerSource.AUTOMATIC1111 -> Automatic1111Form(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.SWARM_UI -> SwarmUiForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.HORDE -> HordeForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.HUGGING_FACE -> HuggingFaceForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.OPEN_AI_COMPATIBLE -> OpenAiCompatibleForm(state, strings, processIntent)
                ServerSource.OPEN_AI_OAUTH -> OpenAiOAuthForm(state)

                ServerSource.OPEN_AI -> OpenAiForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.FAL_AI -> FalAiForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.ARLI_AI -> ArliAiForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.STABILITY_AI -> StabilityAiForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )

                ServerSource.LOCAL_MICROSOFT_ONNX,
                ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                ServerSource.LOCAL_APPLE_CORE_ML,
                ServerSource.LOCAL_APPLE_BONSAI,
                -> LocalGenerationForm(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )
            }
        }
    }
}

@Composable
internal fun LocalGenerationForm(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FormTitle(
            title = state.mode.title(strings, state.platform),
            subtitle = state.mode.subtitle(strings, state.platform),
        )
        HintText(text = strings.localWarning)
        if (
            state.allowLocalCustomModels &&
            state.mode != ServerSource.LOCAL_APPLE_CORE_ML &&
            state.mode != ServerSource.LOCAL_APPLE_BONSAI
        ) {
            SwitchRow(
                icon = Icons.Outlined.Landslide,
                text = strings.localCustomSwitch,
                checked = state.localCustomModel,
                onCheckedChange = { processIntent(ServerSetupIntent.AllowLocalCustomModel(it)) },
            )
        }
        if (state.localCustomModel) {
            LocalCustomModelPathForm(
                state = state,
                strings = strings,
                processIntent = processIntent,
            )
        }
        state.visibleLocalModels.forEach { model ->
            LocalModelItem(
                model = model,
                selected = model.selected,
                strings = strings,
                onSelect = { processIntent(ServerSetupIntent.SelectLocalModel(model)) },
                onAction = { processIntent(ServerSetupIntent.LocalModel.ClickReduce(model)) },
            )
        }
    }
}

private val ServerSetupState.visibleLocalModels: List<ServerSetupState.LocalModel>
    get() {
        val customModels = localModels.filter(ServerSetupState.LocalModel::isCustom)
        val builtInModels = localModels.filterNot(ServerSetupState.LocalModel::isCustom)
        return if (localCustomModel) {
            customModels + builtInModels
        } else {
            builtInModels
        }
    }

@Composable
internal fun LocalCustomModelPathForm(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isLocalGenerationSetupAvailable()) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = strings.localPermissionHeader,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = strings.localPermissionTitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { processIntent(ServerSetupIntent.LaunchManageStoragePermission) },
            ) {
                Text(text = strings.localPermissionButton)
            }
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = strings.localPathHeader,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        SetupTextField(
            value = state.localCustomModelPath,
            onValueChange = { processIntent(ServerSetupIntent.SelectLocalModelPath(it)) },
            label = strings.localPathTitle,
            error = state.localCustomModelPathValidationError?.message(strings),
            trailingIcon = {
                IconButton(
                    onClick = {
                        processIntent(ServerSetupIntent.SelectLocalModelPath(LOCAL_DIFFUSION_CUSTOM_PATH))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = strings.reset,
                    )
                }
            },
        )
        ServerSetupLocalPathPickerButton(
            modifier = Modifier.fillMaxWidth(),
            text = strings.localPathButton,
            onPathSelected = { processIntent(ServerSetupIntent.SelectLocalModelPath(it)) },
        )
    }
}

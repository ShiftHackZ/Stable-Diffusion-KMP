@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.widget.engine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.di.initKoin
import com.shifthackz.aisdv1.presentation.theme.textFieldColors

/**
 * Renders the `EngineSelectionComponent` UI for the SDAI presentation layer.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @author Dmitriy Moroz
 */
@Composable
fun EngineSelectionComponent(
    modifier: Modifier = Modifier,
) {
    val koin = remember { initKoin() }
    val viewModel = remember(koin) {
        koin.get<EngineSelectionViewModel>()
    }
    MviComponent(
        viewModel = viewModel,
    ) { state, intentHandler ->
        EngineSelectionContent(
            state = state,
            modifier = modifier,
            processIntent = intentHandler,
        )
    }
}

/**
 * Renders the `EngineSelectionContent` UI for the SDAI presentation layer.
 *
 * @param state state rendered or processed by the component.
 * @param processIntent process intent value consumed by the API.
 * @param modifier Compose modifier applied to the rendered UI.
 * @author Dmitriy Moroz
 */
@Composable
fun EngineSelectionContent(
    state: EngineSelectionState,
    processIntent: (EngineSelectionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.mode) {
        ServerSource.AUTOMATIC1111 -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.selectedSdModel,
            items = state.sdModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it)) },
        )

        ServerSource.SWARM_UI -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.selectedSwarmModel,
            items = state.swarmModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it)) },
        )

        ServerSource.HUGGING_FACE -> DropdownTextField(
            label = Localization.string("hint_hugging_face_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.selectedHfModel,
            items = state.hfModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it)) },
        )

        ServerSource.STABILITY_AI -> DropdownTextField(
            label = Localization.string("hint_stability_ai_engine"),
            loading = state.loading,
            modifier = modifier,
            value = state.selectedStEngine,
            items = state.stEngines,
            onItemSelected = { processIntent(EngineSelectionIntent(it)) },
        )

        ServerSource.LOCAL_MICROSOFT_ONNX -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.localAiModels.firstOrNull { it.id == state.selectedLocalAiModelId },
            items = state.localAiModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it.id)) },
            displayDelegate = { it.name },
        )

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.localAiModels.firstOrNull { it.id == state.selectedLocalAiModelId },
            items = state.localAiModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it.id)) },
            displayDelegate = { it.name },
        )

        ServerSource.LOCAL_APPLE_CORE_ML -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.localAiModels.firstOrNull { it.id == state.selectedLocalAiModelId },
            items = state.localAiModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it.id)) },
            displayDelegate = { it.name },
        )

        ServerSource.LOCAL_APPLE_BONSAI -> DropdownTextField(
            label = Localization.string("hint_sd_model"),
            loading = state.loading,
            modifier = modifier,
            value = state.localAiModels.firstOrNull { it.id == state.selectedLocalAiModelId },
            items = state.localAiModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it.id)) },
            displayDelegate = { it.name },
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
        ServerSource.HORDE,
        ServerSource.OPEN_AI,
        ServerSource.OPEN_AI_COMPATIBLE,
        ServerSource.OPEN_AI_OAUTH,
        ServerSource.FAL_AI,
        ServerSource.ARLI_AI,
        -> Unit
    }
}

/**
 * Renders the `DropdownTextField` UI for the SDAI presentation layer.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param loading loading value consumed by the API.
 * @param label label value consumed by the API.
 * @param value value value consumed by the API.
 * @param items items value consumed by the API.
 * @param onItemSelected callback invoked by the component.
 * @param displayDelegate display delegate value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
private fun <T : Any> DropdownTextField(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    label: String,
    value: T?,
    items: List<T> = emptyList(),
    onItemSelected: (T) -> Unit = {},
    displayDelegate: (T) -> String = { it.toString() },
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        if (!loading) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = value?.let(displayDelegate) ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = textFieldColors,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer(),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded && !loading,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayDelegate(item)) },
                    onClick = {
                        expanded = false
                        if (value == item) return@DropdownMenuItem
                        onItemSelected(item)
                    },
                )
            }
        }
    }
}

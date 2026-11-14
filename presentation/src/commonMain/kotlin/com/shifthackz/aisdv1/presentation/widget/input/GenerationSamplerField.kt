package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler

/**
 * Displays sampler selection and an inline retry when AUTOMATIC1111 has no usable sampler list.
 * Loading feedback replaces the empty dropdown while the current attempt is running.
 *
 * @param state Shared generation form, including current sampler loading and failure state.
 * @param onEvent Receives sampler selection and retry actions for the owning generation screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationSamplerField(
    state: GenerationInputFormState,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    val automatic1111 = state.mode == ServerSource.AUTOMATIC1111
    Column(modifier = Modifier.fillMaxWidth()) {
        DropdownTextField(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            loading = automatic1111 && state.samplersLoading,
            label = Localization.string("hint_sampler").asUiText(),
            value = state.selectedSampler,
            items = state.availableSamplers,
            onItemSelected = { onEvent(GenerationInputFormEvent.UpdateSampler(it)) },
            displayDelegate = { value ->
                if (value == StabilityAiSampler.NONE.toString()) {
                    Localization.string("hint_autodetect").asUiText()
                } else {
                    value.asUiText()
                }
            },
        )
        if (automatic1111 && state.samplersLoadFailed) {
            Text(
                text = Localization.string("error_samplers_unavailable"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(
                enabled = !state.samplersLoading,
                onClick = { onEvent(GenerationInputFormEvent.RetrySamplers) },
            ) {
                Text(Localization.string("retry"))
            }
        }
    }
}

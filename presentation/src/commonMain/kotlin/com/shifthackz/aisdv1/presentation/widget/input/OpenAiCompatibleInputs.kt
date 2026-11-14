package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asString

/**
 * Displays arbitrary model and pixel dimensions without Stable Diffusion size restrictions.
 *
 * @param state Current model, dimension validation and batch settings.
 * @param onEvent Sends edits to the generation screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiCompatibleInputs(state: GenerationInputFormState, onEvent: (GenerationInputFormEvent) -> Unit) {
    Column {
        PlatformOutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            value = state.openAiCompatibleModel,
            onValueChange = { onEvent(GenerationInputFormEvent.UpdateOpenAiCompatibleModel(it)) },
            label = Localization.string("open_ai_compatible_model"),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurface,
            hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
            singleLine = true,
        )
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                value = state.width,
                onValueChange = { onEvent(GenerationInputFormEvent.UpdateWidth(it)) },
                label = { Text(Localization.string("width")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.widthValidationError != null,
                supportingText = { state.widthValidationError?.let { Text(it.asString()) } },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                value = state.height,
                onValueChange = { onEvent(GenerationInputFormEvent.UpdateHeight(it)) },
                label = { Text(Localization.string("height")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.heightValidationError != null,
                supportingText = { state.heightValidationError?.let { Text(it.asString()) } },
                singleLine = true,
            )
        }
        GenerationBatchComponent(state, onEvent)
    }
}

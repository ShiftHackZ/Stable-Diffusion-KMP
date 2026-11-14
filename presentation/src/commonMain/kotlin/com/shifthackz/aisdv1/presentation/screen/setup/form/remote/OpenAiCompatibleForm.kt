package com.shifthackz.aisdv1.presentation.screen.setup.form.remote

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.shifthackz.aisdv1.presentation.screen.setup.component.message
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState

/**
 * Configures a complete image API prefix and optional bearer token.
 *
 * @param state Current independent compatible-server form values.
 * @param strings Localized setup labels and help.
 * @param processIntent Sends field changes to the setup reducer.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiCompatibleForm(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    Column {
        FormTitle(strings.openAiCompatibleTitle, strings.openAiCompatibleSubtitle)
        SetupTextField(
            value = state.openAiCompatibleUrl,
            onValueChange = { processIntent(ServerSetupIntent.UpdateOpenAiCompatibleUrl(it)) },
            label = strings.openAiCompatibleUrl,
            keyboardType = KeyboardType.Uri,
            error = state.openAiCompatibleUrlValidationError?.message(strings),
        )
        SetupTextField(
            value = state.openAiCompatibleApiKey,
            onValueChange = { processIntent(ServerSetupIntent.UpdateOpenAiCompatibleApiKey(it)) },
            label = strings.optionalApiKey,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}

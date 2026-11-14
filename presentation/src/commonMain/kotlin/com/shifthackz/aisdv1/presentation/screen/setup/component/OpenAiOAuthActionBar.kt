package com.shifthackz.aisdv1.presentation.screen.setup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.model.OpenAiOAuthSetupPhase
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.model.openAiOAuthPhase
import com.shifthackz.aisdv1.presentation.theme.global.persistentBottomBarWindowInsets

/**
 * Pins only the actions available in the current OAuth phase below the scrolling form.
 * Its natural height accommodates wrapped labels and system font scaling above the safe area.
 * @param state Current setup and device-authorization state.
 * @param strings Shared wizard labels, including the final connection action.
 * @param processIntent Dispatches the selected configuration action through MVI.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiOAuthActionBar(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(persistentBottomBarWindowInsets())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (state.openAiOAuthPhase) {
            OpenAiOAuthSetupPhase.SIGNED_OUT -> OAuthPrimaryAction(
                label = Localization.string("open_ai_oauth_login"),
                enabled = !state.loadingConfiguration,
                onClick = { processIntent(ServerSetupIntent.OAuthLogin) },
            )

            OpenAiOAuthSetupPhase.REQUESTING_CODE -> OAuthSecondaryAction(
                label = Localization.string("cancel"),
                onClick = { processIntent(ServerSetupIntent.OAuthCancel) },
            )

            OpenAiOAuthSetupPhase.AWAITING_AUTHORIZATION -> {
                OAuthPrimaryAction(
                    label = Localization.string("open_ai_oauth_open_browser"),
                    onClick = { processIntent(ServerSetupIntent.OAuthOpenBrowser) },
                )
                OAuthSecondaryAction(
                    label = Localization.string("cancel"),
                    onClick = { processIntent(ServerSetupIntent.OAuthCancel) },
                )
            }

            OpenAiOAuthSetupPhase.SIGNED_IN -> {
                OAuthPrimaryAction(
                    label = strings.connect,
                    enabled = !state.loadingConfiguration && state.mainButtonEnabled,
                    onClick = { processIntent(ServerSetupIntent.MainButtonClick) },
                )
                OAuthSecondaryAction(
                    label = Localization.string("open_ai_oauth_logout"),
                    onClick = { processIntent(ServerSetupIntent.OAuthLogout) },
                )
            }

            OpenAiOAuthSetupPhase.STORAGE_ERROR -> OAuthPrimaryAction(
                label = Localization.string("open_ai_oauth_reset"),
                enabled = !state.loadingConfiguration,
                onClick = { processIntent(ServerSetupIntent.OAuthLogout) },
            )
        }
    }
}

/**
 * Renders one full-width primary action without constraining multiline label height.
 * @param label Localized visible and accessible action name.
 * @param enabled Whether the action can currently be submitted.
 * @param onClick Action callback.
 * @author Dmitriy Moroz
 */
@Composable
private fun OAuthPrimaryAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = enabled, onClick = onClick) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Renders a full-width secondary action with a minimum accessible touch target.
 * @param label Localized visible and accessible action name.
 * @param onClick Action callback.
 * @author Dmitriy Moroz
 */
@Composable
private fun OAuthSecondaryAction(label: String, onClick: () -> Unit) {
    OutlinedButton(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), onClick = onClick) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

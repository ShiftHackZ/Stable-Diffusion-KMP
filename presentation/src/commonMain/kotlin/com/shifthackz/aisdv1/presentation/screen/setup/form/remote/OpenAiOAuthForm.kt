package com.shifthackz.aisdv1.presentation.screen.setup.form.remote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.screen.setup.component.OpenAiOAuthCodePanel
import com.shifthackz.aisdv1.presentation.screen.setup.model.OpenAiOAuthSetupPhase
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.model.openAiOAuthPhase

/**
 * Displays OAuth configuration content and status; configuration actions belong to the pinned bar.
 * @param state Current transient device code and token-free account identity.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiOAuthForm(state: ServerSetupState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FormTitle(Localization.string("srv_type_open_ai_oauth"), Localization.string("open_ai_oauth_help"))
        when (state.openAiOAuthPhase) {
            OpenAiOAuthSetupPhase.SIGNED_OUT -> Unit
            OpenAiOAuthSetupPhase.SIGNED_IN -> Text(
                text = Localization.string("open_ai_oauth_signed_in"),
                style = MaterialTheme.typography.bodyMedium,
            )
            OpenAiOAuthSetupPhase.STORAGE_ERROR -> Text(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                text = Localization.string("open_ai_oauth_storage_error"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            OpenAiOAuthSetupPhase.REQUESTING_CODE,
            OpenAiOAuthSetupPhase.AWAITING_AUTHORIZATION,
            -> {
                if (state.openAiOAuthPhase == OpenAiOAuthSetupPhase.AWAITING_AUTHORIZATION) {
                    Text(
                        text = Localization.string("open_ai_oauth_code_help"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    state.openAiOAuthCode?.let { OpenAiOAuthCodePanel(code = it) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        text = Localization.string("open_ai_oauth_waiting"),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

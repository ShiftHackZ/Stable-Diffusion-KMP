package com.shifthackz.aisdv1.presentation.screen.setup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import kotlinx.coroutines.delay

/**
 * Displays the complete device code using theme typography, text selection and an explicit copy action.
 * Text wraps without ellipsis or a line limit as the available width or system font scale changes.
 * A completed clipboard write shows inline confirmation for two seconds; repeated copies restart it.
 * Feedback is discarded when the code changes or the panel leaves composition.
 * @param code Transient device authorization code; never an access or refresh credential.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION")
@Composable
internal fun OpenAiOAuthCodePanel(code: String) {
    val clipboard = LocalClipboardManager.current
    var copyFeedback by remember(code) { mutableIntStateOf(0) }
    LaunchedEffect(code, copyFeedback) {
        if (copyFeedback > 0) {
            delay(2_000)
            copyFeedback = 0
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectionContainer {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = code,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                )
            }
            TextButton(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                onClick = {
                    clipboard.setText(AnnotatedString(code))
                    copyFeedback++
                },
            ) {
                if (copyFeedback > 0) {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp).size(18.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                }
                Text(
                    text = Localization.string(if (copyFeedback > 0) "message_text_copied" else "action_copy"),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

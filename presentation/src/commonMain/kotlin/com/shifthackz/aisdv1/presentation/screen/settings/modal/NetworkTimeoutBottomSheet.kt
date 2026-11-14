package com.shifthackz.aisdv1.presentation.screen.settings.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.presentation.screen.settings.model.label
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem

/**
 * Picks a duration for one independent network timeout preference.
 *
 * @param selected Current persisted limit, highlighted in the choice list.
 * @param titleKey Localized title for the timeout being edited.
 * @param descriptionKey Localized explanation and any platform limitation.
 * @param onSelected Persists a choice and closes the sheet.
 * @author Dmitriy Moroz
 */
@Composable
internal fun NetworkTimeoutBottomSheet(
    selected: NetworkTimeout,
    titleKey: String,
    descriptionKey: String,
    onSelected: (NetworkTimeout) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = Localization.string(titleKey),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier.padding(vertical = 12.dp),
            text = Localization.string(descriptionKey),
            style = MaterialTheme.typography.bodyMedium,
        )
        NetworkTimeout.entries.forEach { option ->
            val isSelected = option == selected
            SettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .semantics {
                        role = Role.RadioButton
                        this.selected = isSelected
                    },
                text = option.label().asUiText(),
                selected = isSelected,
                showChevron = false,
                startIconContent = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        selected = isSelected,
                        onClick = null,
                    )
                },
                onClick = { onSelected(option) },
            )
        }
    }
}

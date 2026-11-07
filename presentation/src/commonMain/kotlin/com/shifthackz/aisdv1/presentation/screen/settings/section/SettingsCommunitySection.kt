@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.generated.resources.Res
import com.shifthackz.aisdv1.presentation.generated.resources.sdai_logo
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsState
import com.shifthackz.aisdv1.presentation.screen.settings.model.text
import com.shifthackz.aisdv1.presentation.widget.icon.BrandIcons
import com.shifthackz.aisdv1.presentation.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem
import org.jetbrains.compose.resources.painterResource

/**
 * Shows community and website links, using the shared SDAI artwork for the project website.
 *
 * @param state Settings state controlling loading indicators and onboarding visibility.
 * @param headerModifier Layout and styling applied to the section header.
 * @param itemModifier Layout and styling applied to each link row.
 * @param processIntent Handles navigation to the selected community or website.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsCommunitySection(
    state: SettingsState,
    headerModifier: Modifier,
    itemModifier: Modifier,
    processIntent: (SettingsIntent) -> Unit,
) {
        if (!state.onBoardingDemo) {
            SettingsHeader(
                modifier = headerModifier,
                loading = state.loading,
                text = text("settings_header_community"),
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = BrandIcons.Telegram,
                text = text("settings_item_telegram"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenTelegramCommunity) },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = BrandIcons.Discord,
                text = text("settings_item_discord"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenDiscordCommunity) },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIconContent = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.sdai_logo),
                            contentDescription = null,
                        )
                    }
                },
                text = text("settings_item_project_website"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenProjectWebsite) },
            )
            SettingsItem(
                modifier = itemModifier,
                loading = state.loading,
                startIcon = BrandIcons.Moroz,
                text = text("settings_item_developer_website"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenDeveloperWebsite) },
            )
        }
}

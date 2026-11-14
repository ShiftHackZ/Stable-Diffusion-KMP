package com.shifthackz.aisdv1.presentation.screen.setup.source

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState

/**
 * Keeps provider selection effects alive for the wizard lifetime, including the configuration step.
 * Only search/filter/sort or selection changes trigger scrolling; returning to the picker preserves
 * its saved index and offset. Initialization waits until configuration loading finishes.
 * @param state Current provider selection, search and filter state.
 * @param strings Localized provider labels used for search matching.
 * @param listState Picker scroll state retained by the wizard.
 * @param processIntent Updates selection when the active filters exclude the current provider.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceSelectionEffects(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    listState: LazyListState,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    val filteredSources = state.visibleSources(strings)
    LaunchedEffect(
        state.loadingConfiguration,
        state.sourceSearchQuery,
        state.sourceTypeFilter,
        state.sourceReadinessFilters,
        state.sourceTagFilters,
        state.sourceSortOrder,
    ) {
        if (!state.loadingConfiguration && filteredSources.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    LaunchedEffect(state.loadingConfiguration, filteredSources.map(ServerSource::key), state.mode) {
        if (!state.loadingConfiguration && filteredSources.isNotEmpty() && state.mode !in filteredSources) {
            processIntent(ServerSetupIntent.UpdateServerMode(filteredSources.first()))
        }
    }
    LaunchedEffect(state.loadingConfiguration, state.mode) {
        val selectedIndex = filteredSources.indexOf(state.mode)
        if (!state.loadingConfiguration && selectedIndex >= 0 && !listState.isItemFullyVisible(selectedIndex)) {
            listState.animateScrollToItem(selectedIndex)
        }
    }
}

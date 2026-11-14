package com.shifthackz.aisdv1.presentation.screen.setup.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.widget.scrollbar.verticalScrollbar
import kotlinx.coroutines.launch

/**
 * Renders a fixed discovery toolbar above the provider list with search/filter/sort applied.
 * Measuring both in one column keeps the list viewport stable when returning from configuration.
 *
 * @param state current setup state, including active provider filters.
 * @param strings localized provider labels and messages.
 * @param listState lazy-list state shared with provider auto-scroll behavior.
 * @param processIntent sends selection changes back to the setup MVI pipeline.
 * @param modifier list container modifier supplied by the host layout.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceSelectionStep(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    listState: LazyListState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredSources = state.visibleSources(strings)
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        SourceSelectionToolbar(state = state, strings = strings, processIntent = processIntent)
        LazyColumn(
            modifier = Modifier.weight(1f).verticalScrollbar(listState),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filteredSources.isEmpty()) {
                item(key = "source_empty") {
                    SourceEmptyState(text = strings.sourceNoResults)
                }
            }
            itemsIndexed(
                items = filteredSources,
                key = { _, source -> source.key },
            ) { index, source ->
                SourceModeItem(
                    source = source,
                    selected = state.mode == source,
                    platform = state.platform,
                    strings = strings,
                    onClick = {
                        coroutineScope.launch {
                            if (!listState.isItemFullyVisible(index)) {
                                listState.animateScrollToItem(index)
                            }
                        }
                        processIntent(ServerSetupIntent.UpdateServerMode(source))
                    },
                )
            }
        }
    }
}

/**
 * Checks whether a lazy-list item is entirely inside the current viewport.
 *
 * Used to avoid aggressive auto-scroll when the selected provider is already
 * visible to the user.
 *
 * @param index item index inside the lazy list.
 * @author Dmitriy Moroz
 */
internal fun LazyListState.isItemFullyVisible(index: Int): Boolean {
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    return item.offset >= layoutInfo.viewportStartOffset &&
        item.offset + item.size <= layoutInfo.viewportEndOffset
}

@Composable
private fun SourceEmptyState(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

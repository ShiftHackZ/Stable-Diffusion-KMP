@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.screen.setup.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.setup.component.ConfigurationStepBar
import com.shifthackz.aisdv1.presentation.screen.setup.component.OpenAiOAuthActionBar
import com.shifthackz.aisdv1.presentation.screen.setup.component.ServerSetupLoading
import com.shifthackz.aisdv1.presentation.screen.setup.component.ServerSetupModal
import com.shifthackz.aisdv1.presentation.screen.setup.component.mainButtonEnabled
import com.shifthackz.aisdv1.presentation.screen.setup.form.local.ConfigurationStep
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.source.SourceFiltersBottomSheet
import com.shifthackz.aisdv1.presentation.screen.setup.source.SourceSelectionEffects
import com.shifthackz.aisdv1.presentation.screen.setup.source.SourceSelectionStep
import com.shifthackz.aisdv1.presentation.screen.setup.source.SourceSortBottomSheet
import com.shifthackz.aisdv1.presentation.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.theme.global.persistentTopAppBarWindowInsets

/**
 * Hosts source selection and a scrolling configuration form with actions pinned above system insets.
 * @param state Current wizard, provider and form state.
 * @param processIntent Dispatches navigation, configuration and account actions through MVI.
 * @param modifier Bounds supplied by the navigation host.
 * @param strings Localized shared wizard labels.
 * @author Dmitriy Moroz
 */
@Composable
fun ServerSetupContent(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ServerSetupStrings = ServerSetupStrings(),
) {
    val sourceListState = rememberLazyListState()
    val configurationListState = rememberLazyListState()

    SourceSelectionEffects(state, strings, sourceListState, processIntent)

    LaunchedEffect(state.mode) {
        configurationListState.scrollToItem(0)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = strings.title,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        },
                        navigationIcon = {
                            if (state.showBackNavArrow || state.step != ServerSetupState.Step.SOURCE) {
                                IconButton(
                                    onClick = { processIntent(ServerSetupIntent.NavigateBack) },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = strings.backContentDescription,
                                    )
                                }
                            }
                        },
                        windowInsets = persistentTopAppBarWindowInsets(),
                    )
                    ConfigurationStepBar(
                        currentStep = state.step,
                        strings = strings,
                    )
                }
            },
            bottomBar = {
                if (state.step == ServerSetupState.Step.CONFIGURE && state.mode == ServerSource.OPEN_AI_OAUTH) {
                    OpenAiOAuthActionBar(state = state, strings = strings, processIntent = processIntent)
                } else {
                    Button(
                        modifier = Modifier
                            .windowInsetsPadding(persistentBottomBarWindowInsets())
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp, top = 8.dp),
                        enabled = !state.loadingConfiguration && state.mainButtonEnabled,
                        onClick = { processIntent(ServerSetupIntent.MainButtonClick) },
                    ) {
                        Text(
                            text = when (state.step) {
                                ServerSetupState.Step.SOURCE -> strings.next
                                ServerSetupState.Step.CONFIGURE -> when (state.mode) {
                                    ServerSource.LOCAL_MICROSOFT_ONNX,
                                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                                    -> strings.setup

                                    else -> strings.connect
                                }
                            },
                            color = LocalContentColor.current,
                        )
                    }
                }
            },
        ) { paddingValues ->
            if (state.loadingConfiguration) {
                ServerSetupLoading(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    text = strings.loadingConfiguration,
                )
            } else {
                when (state.step) {
                    ServerSetupState.Step.SOURCE -> SourceSelectionStep(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = state,
                        strings = strings,
                        listState = sourceListState,
                        processIntent = processIntent,
                    )

                    ServerSetupState.Step.CONFIGURE -> ConfigurationStep(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = state,
                        strings = strings,
                        listState = configurationListState,
                        processIntent = processIntent,
                    )
                }
            }
        }
        ServerSetupModal(
            modal = state.modal,
            strings = strings,
            processIntent = processIntent,
        )
        if (state.modal == ServerSetupState.Modal.SourceFilters) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { value -> value != SheetValue.PartiallyExpanded },
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { processIntent(ServerSetupIntent.DismissDialog) },
                shape = RectangleShape,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                SourceFiltersBottomSheet(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )
            }
        }
        if (state.modal == ServerSetupState.Modal.SourceSort) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { value -> value != SheetValue.PartiallyExpanded },
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { processIntent(ServerSetupIntent.DismissDialog) },
                shape = RectangleShape,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                SourceSortBottomSheet(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )
            }
        }
    }
}

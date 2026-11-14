package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.extensions.clearFocusOnTap
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.di.initKoin
import com.shifthackz.aisdv1.presentation.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageIntent
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.img2img.rememberImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageIntent
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.theme.global.AiSdAppTheme
import org.koin.core.parameter.parametersOf

/**
 * Renders the `AiSdApp` UI for the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun AiSdApp() {
    val koin = remember { initKoin() }
    val preferenceManager = remember(koin) {
        koin.get<PreferenceManager>()
    }
    remember(preferenceManager) {
        Localization.setLanguageCode(preferenceManager.languageCode.takeIf(String::isNotBlank))
    }
    val router = remember { RootAppRouter() }
    val route by router.route.collectAsState()
    val drawerOpen by router.drawerOpen.collectAsState()
    val canNavigateBack by router.canNavigateBack.collectAsState()
    val languageCode by Localization.languageCodeFlow.collectAsState()
    val urlLauncher = rememberExternalUrlLauncher()
    val imageToImagePlatformActions = rememberImageToImagePlatformActions()
    val textToImageViewModel = remember(koin, router) {
        koin.get<TextToImageViewModel> {
            parametersOf(router)
        }
    }
    val imageToImageViewModel = remember(koin, router, imageToImagePlatformActions) {
        koin.get<ImageToImageViewModel> {
            parametersOf(router, imageToImagePlatformActions)
        }
    }
    val visitedHomeRoutes = remember { mutableStateListOf<AppRoute>() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        textToImageViewModel.processIntent(TextToImageIntent.RetrySamplers)
        imageToImageViewModel.processIntent(ImageToImageIntent.RetrySamplers)
    }
    var lastHomeRoute by remember { mutableStateOf<AppRoute?>(null) }
    val currentRoute = route
    val selectedHomeRoute = currentRoute.asHomeTabRoute()

    LaunchedEffect(selectedHomeRoute) {
        selectedHomeRoute?.let { homeRoute ->
            lastHomeRoute = homeRoute
            if (!visitedHomeRoutes.contains(homeRoute)) {
                visitedHomeRoutes.add(homeRoute)
            }
        }
    }

    RootBackHandler(
        enabled = drawerOpen || canNavigateBack,
        onBack = router::navigateBack,
    )

    AiSdAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val shellHomeRoute = selectedHomeRoute ?: lastHomeRoute
                val renderedHomeRoutes = (visitedHomeRoutes + listOfNotNull(shellHomeRoute)).distinct()

                if (shellHomeRoute != null) {
                    HomeRoutesScaffold(
                        languageCode = languageCode,
                        activeRoute = shellHomeRoute,
                        renderedRoutes = renderedHomeRoutes,
                        router = router,
                        buildInfoProvider = koin.get<BuildInfoProvider>(),
                        preferenceManager = preferenceManager,
                        showHomeChrome = selectedHomeRoute != null,
                        textToImageViewModel = textToImageViewModel,
                        imageToImageViewModel = imageToImageViewModel,
                    )
                }

                if (selectedHomeRoute == null) {
                    OverlayRouteContent(
                        languageCode = languageCode,
                        route = currentRoute,
                        koin = koin,
                        router = router,
                        urlLauncher = urlLauncher,
                        imageToImageViewModel = imageToImageViewModel,
                    )
                }
            }
        }
    }
}

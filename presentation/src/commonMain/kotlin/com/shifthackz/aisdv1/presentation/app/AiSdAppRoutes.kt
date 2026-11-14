package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.screen.benchmark.BenchmarkScreen
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuScreen
import com.shifthackz.aisdv1.presentation.screen.donate.DonateScreen
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailScreen
import com.shifthackz.aisdv1.presentation.screen.gallery.list.GalleryScreen
import com.shifthackz.aisdv1.presentation.screen.history.HistoryScreenContent
import com.shifthackz.aisdv1.presentation.screen.history.HistoryViewModel
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageInPaintScreenContent
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageContent
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.loader.ConfigurationLoaderScreenContent
import com.shifthackz.aisdv1.presentation.screen.loader.ConfigurationLoaderViewModel
import com.shifthackz.aisdv1.presentation.screen.loader.toContentState
import com.shifthackz.aisdv1.presentation.screen.logger.LoggerScreen
import com.shifthackz.aisdv1.presentation.screen.networkusage.NetworkUsageScreen
import com.shifthackz.aisdv1.presentation.screen.onboarding.OnBoardingContent
import com.shifthackz.aisdv1.presentation.screen.onboarding.OnBoardingViewModel
import com.shifthackz.aisdv1.presentation.screen.report.ReportScreen
import com.shifthackz.aisdv1.presentation.screen.settings.SettingsScreen
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupContent
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupViewModel
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.platform.rememberServerSetupEffectHandler
import com.shifthackz.aisdv1.presentation.screen.splash.SplashScreenContent
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageScreen
import com.shifthackz.aisdv1.presentation.screen.splash.SplashViewModel
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageContent
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.txt2img.decodeBase64ImageBitmap
import com.shifthackz.aisdv1.presentation.screen.web.webui.WebUiScreen
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

/**
 * Hosts the persistent bottom-tab routes inside the app scaffold.
 *
 * Standalone overlay routes such as storage usage and network usage are deliberately excluded so
 * they can render without the bottom bar.
 *
 * @param languageCode Active localization code used by rendered home tabs.
 * @param activeRoute Route currently visible in the home scaffold.
 * @param renderedRoutes Home routes already created and kept alive.
 * @param router Root router shared by home tabs.
 * @param buildInfoProvider Build metadata consumed by scaffold-level UI.
 * @param preferenceManager Preferences source consumed by scaffold-level UI.
 * @param showHomeChrome Whether bottom tabs and home-level chrome should be visible.
 * @param textToImageViewModel Shared Text-to-Image ViewModel kept alive with its tab.
 * @param imageToImageViewModel Shared Image-to-Image ViewModel kept alive with its tab.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeRoutesScaffold(
    languageCode: String,
    activeRoute: AppRoute,
    renderedRoutes: List<AppRoute>,
    router: RootAppRouter,
    buildInfoProvider: BuildInfoProvider,
    preferenceManager: PreferenceManager,
    showHomeChrome: Boolean = true,
    textToImageViewModel: TextToImageViewModel,
    imageToImageViewModel: ImageToImageViewModel,
) {
    AppScaffold(
        currentRoute = activeRoute,
        router = router,
        buildInfoProvider = buildInfoProvider,
        preferenceManager = preferenceManager,
        showHomeChrome = showHomeChrome,
    ) { contentModifier ->
        Box(modifier = contentModifier.fillMaxSize()) {
            HomeRouteSlot(
                route = AppRoute.TextToImage,
                activeRoute = activeRoute,
                renderedRoutes = renderedRoutes,
            ) { modifier ->
                MviComponent(viewModel = textToImageViewModel) { state, processIntent ->
                    TextToImageContent(
                        modifier = modifier,
                        state = state,
                        processIntent = processIntent,
                        useDrawerNavigation = true,
                    )
                }
            }
            HomeRouteSlot(
                route = AppRoute.ImageToImage,
                activeRoute = activeRoute,
                renderedRoutes = renderedRoutes,
            ) { modifier ->
                MviComponent(viewModel = imageToImageViewModel) { state, processIntent ->
                    ImageToImageContent(
                        modifier = modifier,
                        state = state,
                        processIntent = processIntent,
                        useDrawerNavigation = true,
                    )
                }
            }
            HomeRouteSlot(
                route = AppRoute.Gallery,
                activeRoute = activeRoute,
                renderedRoutes = renderedRoutes,
            ) { modifier ->
                GalleryScreen(
                    modifier = modifier,
                    galleryRouter = router,
                    backHandlerEnabled = activeRoute == AppRoute.Gallery,
                )
            }
            HomeRouteSlot(
                route = AppRoute.Settings,
                activeRoute = activeRoute,
                renderedRoutes = renderedRoutes,
            ) { modifier ->
                SettingsScreen(
                    modifier = modifier,
                    router = router,
                )
            }
        }
    }
}

/**
 * Keeps previously rendered home tabs alive while hiding inactive tabs at zero size.
 *
 * @param route Home route represented by this slot.
 * @param activeRoute Currently visible home route.
 * @param renderedRoutes Home routes already instantiated by the root route host.
 * @param content Tab content that receives the slot modifier.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeRouteSlot(
    route: AppRoute,
    activeRoute: AppRoute,
    renderedRoutes: List<AppRoute>,
    content: @Composable (Modifier) -> Unit,
) {
    if (route !in renderedRoutes) return
    val visible = route == activeRoute
    Box(
        modifier = if (visible) {
            Modifier.fillMaxSize()
        } else {
            Modifier.requiredSize(0.dp)
        },
    ) {
        content(Modifier.fillMaxSize())
    }
}

/**
 * Renders routes that sit above the home scaffold and own their full-screen layout.
 *
 * @param languageCode Active localization code used by standalone route content.
 * @param route Route currently requested by the root router.
 * @param koin Presentation Koin instance used to resolve route dependencies.
 * @param router Root router passed to overlay screens.
 * @param urlLauncher Platform external URL launcher used by web/report flows.
 * @param imageToImageViewModel Shared Image-to-Image ViewModel reused by in-paint overlay flow.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun OverlayRouteContent(
    languageCode: String,
    route: AppRoute,
    koin: Koin,
    router: RootAppRouter,
    urlLauncher: ExternalUrlLauncher,
    imageToImageViewModel: ImageToImageViewModel,
) {
    when (route) {
        AppRoute.Home,
        AppRoute.TextToImage,
        AppRoute.ImageToImage,
        AppRoute.Gallery,
        AppRoute.Settings,
        -> Unit

        AppRoute.StorageUsage -> {
            StorageUsageScreen(
                modifier = Modifier.fillMaxSize(),
                router = router,
            )
        }

        AppRoute.NetworkUsage -> {
            NetworkUsageScreen(
                modifier = Modifier.fillMaxSize(),
                router = router,
            )
        }

        AppRoute.Splash -> {
            val splashViewModel = remember(koin, router) {
                koin.get<SplashViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(viewModel = splashViewModel) {
                SplashScreenContent()
            }
        }

        is AppRoute.OnBoarding -> {
            val source = route.source
            val onBoardingViewModel = remember(koin, router, source) {
                koin.get<OnBoardingViewModel> {
                    parametersOf(source, router)
                }
            }
            MviComponent(
                viewModel = onBoardingViewModel,
            ) { state, processIntent ->
                OnBoardingContent(
                    launchSource = onBoardingViewModel.launchSource,
                    state = state,
                    processIntent = processIntent,
                )
            }
        }

        is AppRoute.Setup -> {
            val source = route.source
            val setupViewModel = remember(koin, router, source) {
                koin.get<ServerSetupViewModel> {
                    parametersOf(source, router)
                }
            }
            val effectHandler = rememberServerSetupEffectHandler(urlLauncher)
            DisposableEffect(setupViewModel) {
                onDispose { setupViewModel.onRouteDisposed() }
            }
            MviComponent(
                viewModel = setupViewModel,
                processEffect = effectHandler,
            ) { state, processIntent ->
                RootBackHandler(
                    enabled = state.showBackNavArrow || state.step == ServerSetupState.Step.CONFIGURE,
                    onBack = { processIntent(ServerSetupIntent.NavigateBack) },
                )
                ServerSetupContent(
                    state = state,
                    processIntent = processIntent,
                )
            }
        }

        AppRoute.ConfigurationLoader -> {
            val loaderViewModel = remember(koin, router) {
                koin.get<ConfigurationLoaderViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(
                viewModel = loaderViewModel,
            ) { state ->
                ConfigurationLoaderScreenContent(
                    state = state.toContentState(),
                )
            }
        }

        AppRoute.ImageInPaint -> {
            val state by imageToImageViewModel.state.collectAsState()
            val image = remember(state.imageBase64) {
                state.imageBase64.decodeBase64ImageBitmap()
            }
            if (image == null) {
                LaunchedEffect(Unit) {
                    router.navigateBack()
                }
            } else {
                ImageInPaintScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    image = image,
                    state = state.inPaint,
                    processIntent = imageToImageViewModel::processIntent,
                    onClose = router::navigateBack,
                )
            }
        }

        is AppRoute.GalleryDetail -> {
            GalleryDetailScreen(
                itemId = route.itemId,
                router = router,
            )
        }

        AppRoute.History -> {
            val historyViewModel = remember(koin, router) {
                koin.get<HistoryViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(
                viewModel = historyViewModel,
            ) { state, processIntent ->
                HistoryScreenContent(
                    state = state,
                    processIntent = processIntent,
                )
            }
        }

        AppRoute.Benchmark -> {
            BenchmarkScreen(router = router)
        }

        is AppRoute.Report -> {
            ReportScreen(
                itemId = route.itemId,
                router = router,
            )
        }

        AppRoute.Debug -> {
            DebugMenuScreen(router = router)
        }

        AppRoute.Logger -> {
            LoggerScreen(router = router)
        }

        AppRoute.Donate -> {
            DonateScreen(router = router)
        }

        AppRoute.WebUi -> {
            WebUiScreen(router = router)
        }
    }
}

/**
 * Maps a route back to the home tab that should stay selected underneath it.
 *
 * @author Dmitriy Moroz
 */
internal fun AppRoute.asHomeTabRoute(): AppRoute? = when (this) {
    AppRoute.Home,
    AppRoute.TextToImage,
    -> AppRoute.TextToImage

    AppRoute.ImageToImage -> AppRoute.ImageToImage
    AppRoute.Gallery -> AppRoute.Gallery
    AppRoute.Settings -> AppRoute.Settings

    AppRoute.ImageInPaint,
    AppRoute.Benchmark,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
    is AppRoute.GalleryDetail,
    AppRoute.History,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
    -> null
}

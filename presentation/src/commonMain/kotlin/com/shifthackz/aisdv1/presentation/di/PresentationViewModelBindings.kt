package com.shifthackz.aisdv1.presentation.di

import com.shifthackz.aisdv1.presentation.modal.download.DownloadDialogViewModel
import com.shifthackz.aisdv1.presentation.modal.embedding.EmbeddingViewModel
import com.shifthackz.aisdv1.presentation.modal.extras.ExtrasViewModel
import com.shifthackz.aisdv1.presentation.modal.history.InputHistoryViewModel
import com.shifthackz.aisdv1.presentation.modal.tag.EditTagViewModel
import com.shifthackz.aisdv1.presentation.model.ExtraType
import com.shifthackz.aisdv1.presentation.model.LaunchSource
import com.shifthackz.aisdv1.presentation.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.presentation.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.presentation.navigation.router.DebugMenuRouter
import com.shifthackz.aisdv1.presentation.navigation.router.DonateRouter
import com.shifthackz.aisdv1.presentation.navigation.router.GalleryDetailRouter
import com.shifthackz.aisdv1.presentation.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.presentation.navigation.router.HistoryRouter
import com.shifthackz.aisdv1.presentation.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.presentation.navigation.router.LoggerRouter
import com.shifthackz.aisdv1.presentation.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.presentation.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.presentation.navigation.router.ReportRouter
import com.shifthackz.aisdv1.presentation.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.presentation.navigation.router.SplashRouter
import com.shifthackz.aisdv1.presentation.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.presentation.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.presentation.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.presentation.screen.benchmark.BenchmarkViewModel
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuViewModel
import com.shifthackz.aisdv1.presentation.screen.donate.DonateViewModel
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailViewModel
import com.shifthackz.aisdv1.presentation.screen.gallery.list.GalleryViewModel
import com.shifthackz.aisdv1.presentation.screen.history.HistoryViewModel
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.loader.ConfigurationLoaderViewModel
import com.shifthackz.aisdv1.presentation.screen.logger.LoggerViewModel
import com.shifthackz.aisdv1.presentation.screen.networkusage.NetworkUsageViewModel
import com.shifthackz.aisdv1.presentation.screen.onboarding.OnBoardingViewModel
import com.shifthackz.aisdv1.presentation.screen.report.ReportViewModel
import com.shifthackz.aisdv1.presentation.screen.settings.SettingsViewModel
import com.shifthackz.aisdv1.presentation.screen.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupViewModel
import com.shifthackz.aisdv1.presentation.screen.splash.SplashViewModel
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageViewModel
import com.shifthackz.aisdv1.presentation.screen.storageusage.platform.StorageUsagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.web.webui.WebUiViewModel
import com.shifthackz.aisdv1.presentation.theme.global.AiSdAppThemeViewModel
import com.shifthackz.aisdv1.presentation.widget.connectivity.ConnectivityViewModel
import com.shifthackz.aisdv1.presentation.widget.engine.EngineSelectionViewModel
import com.shifthackz.aisdv1.presentation.widget.work.BackgroundWorkViewModel
import org.koin.core.module.Module

/**
 * Registers ViewModel factories and shared presentation observers.
 *
 * The usage screens intentionally have separate ViewModels, while Settings receives only summary
 * observers so the app settings list stays lightweight and never owns destructive usage actions.
 *
 * @receiver Koin module receiving presentation ViewModel factories.
 *
 * @author Dmitriy Moroz
 */
internal fun Module.registerPresentationViewModelBindings() {
    single {
        StorageUsageObserver()
    }
    factory {
        AiSdAppThemeViewModel(
            dispatchersProvider = get(),
            preferenceManager = get(),
        )
    }
    factory { (splashRouter: SplashRouter) ->
        SplashViewModel(
            dispatchersProvider = get(),
            splashNavigationUseCase = get(),
            splashRouter = splashRouter,
        )
    }
    factory { (launchSource: LaunchSource, router: OnBoardingRouter) ->
        OnBoardingViewModel(
            launchSource = launchSource,
            dispatchersProvider = get(),
            router = router,
            splashNavigationUseCase = get(),
            preferenceManager = get(),
            buildInfoProvider = get(),
        )
    }
    factory { (router: ConfigurationLoaderRouter) ->
        ConfigurationLoaderViewModel(
            dispatchersProvider = get(),
            dataPreLoaderUseCase = get(),
            getConfigurationUseCase = get(),
            router = router,
        )
    }
    factory { (router: BenchmarkRouter) ->
        BenchmarkViewModel(
            dispatchersProvider = get(),
            benchmarkManager = get(),
            router = router,
            platformActions = get(),
        )
    }
    factory { (router: DonateRouter) ->
        DonateViewModel(
            dispatchersProvider = get(),
            fetchSupportersUseCase = get(),
            linksProvider = get(),
            router = router,
        )
    }
    factory { (router: DebugMenuRouter, platformActions: DebugMenuPlatformActions) ->
        DebugMenuViewModel(
            dispatchersProvider = get(),
            preferenceManager = get(),
            debugInsertBadBase64UseCase = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: LoggerRouter) ->
        LoggerViewModel(
            dispatchersProvider = get(),
            logReader = get(),
            platformActions = get(),
            router = router,
        )
    }
    factory { (router: SettingsRouter, platformActions: SettingsPlatformActions) ->
        SettingsViewModel(
            dispatchersProvider = get(),
            getStableDiffusionModelsUseCase = get(),
            observeStabilityAiCreditsUseCase = get(),
            selectStableDiffusionModelUseCase = get(),
            clearAppCacheUseCase = get(),
            getAllGalleryUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
            observeNetworkUsageUseCase = get(),
            storageUsageObserver = get(),
            preferenceManager = get(),
            debugMenuAccessor = get(),
            buildInfoProvider = get(),
            linksProvider = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: StorageUsageRouter, platformActions: StorageUsagePlatformActions) ->
        StorageUsageViewModel(
            dispatchersProvider = get(),
            getAllGalleryUseCase = get(),
            deleteAllGalleryUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
            deleteModelUseCase = get(),
            storageUsageObserver = get(),
            buildInfoProvider = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: NetworkUsageRouter) ->
        NetworkUsageViewModel(
            dispatchersProvider = get(),
            observeNetworkUsageUseCase = get(),
            resetNetworkUsageUseCase = get(),
            router = router,
        )
    }
    factory { (itemId: Long, router: ReportRouter) ->
        ReportViewModel(
            itemId = itemId,
            dispatchersProvider = get(),
            sendReportUseCase = get(),
            getGenerationResultUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            router = router,
            buildInfoProvider = get(),
        )
    }
    factory { (prompt: String, negativePrompt: String, type: ExtraType) ->
        ExtrasViewModel(
            dispatchersProvider = get(),
            fetchAndGetLorasUseCase = get(),
            fetchAndGetHyperNetworksUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            type = type,
        )
    }
    factory { (prompt: String, negativePrompt: String) ->
        EmbeddingViewModel(
            dispatchersProvider = get(),
            fetchAndGetEmbeddingsUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
        )
    }
    factory { (prompt: String, negativePrompt: String, tag: String, isNegative: Boolean) ->
        EditTagViewModel(
            dispatchersProvider = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            tag = tag,
            isNegative = isNegative,
        )
    }
    factory {
        InputHistoryViewModel(
            dispatchersProvider = get(),
            getGenerationResultPagedUseCase = get(),
        )
    }
    factory { (modelId: String) ->
        DownloadDialogViewModel(
            modelId = modelId,
            getLocalModelUseCase = get(),
            dispatchersProvider = get(),
        )
    }
    factory { (router: TextToImageRouter) ->
        TextToImageViewModel(
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            textToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            observeStableDiffusionCppProcessStatusUseCase = get(),
            observeCoreMlProcessStatusUseCase = get(),
            observeBonsaiProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            localGenerationBenchmarkGateProvider = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
        )
    }
    factory { (router: ImageToImageRouter, platformActions: ImageToImagePlatformActions) ->
        ImageToImageViewModel(
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            getRandomImageUseCase = get(),
            imageToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            localGenerationBenchmarkGateProvider = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: HistoryRouter) ->
        HistoryViewModel(
            dispatchersProvider = get(),
            getAllGalleryUseCase = get(),
            router = router,
        )
    }
    factory { (router: GalleryRouter) ->
        GalleryViewModel(
            dispatchersProvider = get(),
            getMediaStoreInfoUseCase = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            deleteAllGalleryUseCase = get(),
            deleteGalleryItemsUseCase = get(),
            setGalleryItemsVisibilityUseCase = get(),
            setGalleryItemsLikedUseCase = get(),
            getGenerationResultPagedUseCase = get(),
            galleryExportService = get(),
            galleryRouter = router,
        )
    }
    factory { (itemId: Long, router: GalleryDetailRouter, platformActions: GalleryDetailPlatformActions) ->
        GalleryDetailViewModel(
            itemId = itemId,
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            getGenerationResultUseCase = get(),
            getAllGalleryUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            deleteGalleryItemUseCase = get(),
            toggleImageVisibilityUseCase = get(),
            toggleImageLikeUseCase = get(),
            generationFormUpdateEvent = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (launchSource: LaunchSource, router: ServerSetupRouter) ->
        ServerSetupViewModel(
            launchSource = launchSource,
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            getConfigurationUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
            fetchHuggingFaceModelsUseCase = get(),
            urlValidator = get(),
            stringValidator = get(),
            filePathValidator = get(),
            connectToA1111UseCase = get(),
            connectToSwarmUiUseCase = get(),
            connectToHordeUseCase = get(),
            connectToHuggingFaceUseCase = get(),
            connectToLocalDiffusionUseCase = get(),
            connectToMediaPipeUseCase = get(),
            connectToSdxlUseCase = get(),
            connectToCoreMlUseCase = get(),
            connectToBonsaiUseCase = get(),
            connectToOpenAiUseCase = get(),
            connectToOpenAiCompatibleUseCase = get(),
            connectToStabilityAiUseCase = get(),
            connectToFalAiUseCase = get(),
            connectToArliAiUseCase = get(),
            downloadModelUseCase = get(),
            deleteModelUseCase = get(),
            downloadGuard = get(),
            storageUsageObserver = get(),
            linksProvider = get(),
            preferenceManager = get(),
            router = router,
        )
    }
    factory {
        EngineSelectionViewModel(
            dispatchersProvider = get(),
            fetchAndGetSwarmUiModelsUseCase = get(),
            observeLocalOnnxModelsUseCase = get(),
            observeLocalCoreMlModelsUseCase = get(),
            observeLocalBonsaiModelsUseCase = get(),
            observeLocalSdxlModelsUseCase = get(),
            fetchAndGetStabilityAiEnginesUseCase = get(),
            getHuggingFaceModelsUseCase = get(),
            preferenceManager = get(),
            getConfigurationUseCase = get(),
            selectStableDiffusionModelUseCase = get(),
            getStableDiffusionModelsUseCase = get(),
        )
    }
    factory { (router: WebUiRouter) ->
        WebUiViewModel(
            dispatchersProvider = get(),
            preferenceManager = get(),
            router = router,
        )
    }
    factory {
        ConnectivityViewModel(
            dispatchersProvider = get(),
            observeServerConnectivityUseCase = get(),
            getMonitorConnectivityUseCase = get(),
            observeMonitorConnectivityUseCase = get(),
        )
    }
    factory {
        BackgroundWorkViewModel(
            dispatchersProvider = get(),
            backgroundWorkObserver = get(),
            imageLoader = get(),
        )
    }
}

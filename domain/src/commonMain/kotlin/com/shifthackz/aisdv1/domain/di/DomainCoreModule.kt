package com.shifthackz.aisdv1.domain.di

import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiCompatibleUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToOpenAiCompatibleUseCaseImpl
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.feature.work.NoOpBackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.NoOpBackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.NoOpServerConnectivityGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.ServerConnectivityGateway
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActorImpl
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpBonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpCoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpLocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpMediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpNetworkUsageRepository
import com.shifthackz.aisdv1.domain.repository.NoOpStableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.repository.NoOpWakeLockRepository
import com.shifthackz.aisdv1.domain.repository.NetworkUsageRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.WakeLockRepository
import com.shifthackz.aisdv1.domain.usecase.caching.AppCacheCleaner
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.NoOpAppCacheCleaner
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestArliAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestFalAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestHordeApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestHuggingFaceApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestOpenAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestStabilityAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestSwarmUiConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestArliAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestFalAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestHordeApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestHuggingFaceApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestOpenAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestStabilityAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.debug.DebugInsertBadBase64UseCase
import com.shifthackz.aisdv1.domain.usecase.debug.DebugInsertBadBase64UseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.donate.FetchSupportersUseCase
import com.shifthackz.aisdv1.domain.usecase.donate.FetchSupportersUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetGalleryItemsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetGalleryItemsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetMediaStoreInfoUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsLikedUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsLikedUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsVisibilityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageLikeUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageLikeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultPagedUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptGenerationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.report.SendReportUseCase
import com.shifthackz.aisdv1.domain.usecase.report.SendReportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCase
import com.shifthackz.aisdv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdlora.FetchAndGetLorasUseCase
import com.shifthackz.aisdv1.domain.usecase.sdlora.FetchAndGetLorasUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCase
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToA1111UseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToArliAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToFalAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHordeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSwarmUiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToA1111UseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToArliAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToFalAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToHordeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToHuggingFaceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToOpenAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToStabilityAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToSwarmUiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultGetConfigurationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultSetServerConfigurationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ResetNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ResetNetworkUsageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.SetServerConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchStabilityAiEnginesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchSwarmUiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchSwarmUiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.wakelock.AcquireWakelockUseCase
import com.shifthackz.aisdv1.domain.usecase.wakelock.AcquireWakelockUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.wakelock.ReleaseWakeLockUseCase
import com.shifthackz.aisdv1.domain.usecase.wakelock.ReleaseWakeLockUseCaseImpl
import org.koin.dsl.module

/**
 * Domain-layer Koin module that provides default repositories and use case implementations.
 *
 * Network usage receives a no-op repository here so common presentation can resolve its contracts
 * before platform data modules override the binding with a persistent implementation.
 *
 * @author Dmitriy Moroz
 */
val coreDomainModule = module {
    single { ProviderSelectionGuard() }
    single<ServerConnectivityGateway> { NoOpServerConnectivityGateway }
    single<BackgroundWorkObserver> { NoOpBackgroundWorkObserver }
    single<BackgroundTaskManager> { NoOpBackgroundTaskManager }
    single<LocalDiffusionGenerationRepository> { NoOpLocalDiffusionGenerationRepository }
    single<MediaPipeGenerationRepository> { NoOpMediaPipeGenerationRepository }
    single<StableDiffusionCppGenerationRepository> { NoOpStableDiffusionCppGenerationRepository }
    single<CoreMlGenerationRepository> { NoOpCoreMlGenerationRepository }
    single<BonsaiGenerationRepository> { NoOpBonsaiGenerationRepository }
    single<WakeLockRepository> { NoOpWakeLockRepository }
    single<NetworkUsageRepository> { NoOpNetworkUsageRepository }
    single<AppCacheCleaner> { NoOpAppCacheCleaner }
    factory<AcquireWakelockUseCase> {
        AcquireWakelockUseCaseImpl(wakeLockRepository = get())
    }
    factory<ReleaseWakeLockUseCase> {
        ReleaseWakeLockUseCaseImpl(wakeLockRepository = get())
    }
    factory<WakeLockInterActor> {
        WakeLockInterActorImpl(
            acquireWakelockUseCase = get(),
            releaseWakeLockUseCase = get(),
        )
    }
    factory<GetMonitorConnectivityUseCase> {
        GetMonitorConnectivityUseCaseImpl(preferenceManager = get())
    }
    factory<ObserveMonitorConnectivityUseCase> {
        ObserveMonitorConnectivityUseCaseImpl(preferenceManager = get())
    }
    factory<PingStableDiffusionServiceUseCase> {
        PingStableDiffusionServiceUseCaseImpl(repository = get())
    }
    factory<ObserveSeverConnectivityUseCase> {
        ObserveSeverConnectivityUseCaseImpl(serverConnectivityGateway = get())
    }
    factory<TestConnectivityUseCase> {
        DefaultTestConnectivityUseCaseImpl(
            remoteDataSource = get(),
            authorizationStore = get(),
        )
    }
    factory<TestSwarmUiConnectivityUseCase> {
        DefaultTestSwarmUiConnectivityUseCaseImpl(
            remoteDataSource = get(),
            authorizationStore = get(),
        )
    }
    factory<TestHordeApiKeyUseCase> {
        DefaultTestHordeApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestHuggingFaceApiKeyUseCase> {
        DefaultTestHuggingFaceApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestOpenAiApiKeyUseCase> {
        DefaultTestOpenAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestStabilityAiApiKeyUseCase> {
        DefaultTestStabilityAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestFalAiApiKeyUseCase> {
        DefaultTestFalAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestArliAiApiKeyUseCase> {
        DefaultTestArliAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<DataPreLoaderUseCase> {
        DataPreLoaderUseCaseImpl(
            serverConfigurationRepository = get(),
            sdModelsRepository = get(),
            sdSamplersRepository = get(),
            sdLorasRepository = get(),
            sdHyperNetworksRepository = get(),
            sdEmbeddingsRepository = get(),
        )
    }
    factory<ClearAppCacheUseCase> {
        ClearAppCacheUseCaseImpl(
            appCacheCleaner = get(),
            repository = get(),
        )
    }
    factory<GetLastResultFromCacheUseCase> {
        GetLastResultFromCacheUseCaseImpl(temporaryGenerationResultRepository = get())
    }
    factory<SaveLastResultToCacheUseCase> {
        SaveLastResultToCacheUseCaseImpl(
            temporaryGenerationResultRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<DebugInsertBadBase64UseCase> {
        DebugInsertBadBase64UseCaseImpl(
            repository = get(),
            timeProvider = get(),
        )
    }
    factory<SplashNavigationUseCase> { SplashNavigationUseCaseImpl(preferenceManager = get()) }
    factory<ObserveLocalOnnxModelsUseCase> {
        ObserveLocalOnnxModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalCoreMlModelsUseCase> {
        ObserveLocalCoreMlModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalBonsaiModelsUseCase> {
        ObserveLocalBonsaiModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalSdxlModelsUseCase> {
        ObserveLocalSdxlModelsUseCaseImpl(repository = get())
    }
    factory<GetLocalOnnxModelsUseCase> {
        GetLocalOnnxModelsUseCaseImpl(downloadableModelRepository = get())
    }
    factory<GetLocalMediaPipeModelsUseCase> {
        GetLocalMediaPipeModelsUseCaseImpl(downloadableModelRepository = get())
    }
    factory<GetLocalSdxlModelsUseCase> {
        GetLocalSdxlModelsUseCaseImpl(downloadableModelRepository = get())
    }
    factory<GetLocalCoreMlModelsUseCase> {
        GetLocalCoreMlModelsUseCaseImpl(downloadableModelRepository = get())
    }
    factory<GetLocalBonsaiModelsUseCase> {
        GetLocalBonsaiModelsUseCaseImpl(downloadableModelRepository = get())
    }
    factory<GetLocalModelUseCase> {
        GetLocalModelUseCaseImpl(localDataSource = get())
    }
    factory<DownloadModelUseCase> {
        DownloadModelUseCaseImpl(downloadableModelRepository = get())
    }
    factory<DeleteModelUseCase> {
        DeleteModelUseCaseImpl(downloadableModelRepository = get())
    }
    factory<TextToImageUseCase> {
        TextToImageUseCaseImpl(
            stableDiffusionGenerationRepository = get(),
            hordeGenerationRepository = get(),
            huggingFaceGenerationRepository = get(),
            openAiGenerationRepository = get(),
            openAiCompatibleGenerationRepository = get(),
            openAiOAuthGenerationRepository = get(),
            stabilityAiGenerationRepository = get(),
            falAiGenerationRepository = get(),
            arliAiGenerationRepository = get(),
            swarmUiGenerationRepository = get(),
            localDiffusionGenerationRepository = get(),
            mediaPipeGenerationRepository = get(),
            stableDiffusionCppGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            bonsaiGenerationRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<ImageToImageUseCase> {
        ImageToImageUseCaseImpl(
            stableDiffusionGenerationRepository = get(),
            swarmUiGenerationRepository = get(),
            hordeGenerationRepository = get(),
            huggingFaceGenerationRepository = get(),
            stabilityAiGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            falAiGenerationRepository = get(),
            arliAiGenerationRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<GetRandomImageUseCase> {
        GetRandomImageUseCaseImpl(randomImageRepository = get())
    }
    factory<ObserveHordeProcessStatusUseCase> {
        ObserveHordeProcessStatusUseCaseImpl(hordeGenerationRepository = get())
    }
    factory<ObserveLocalDiffusionProcessStatusUseCase> {
        ObserveLocalDiffusionProcessStatusUseCaseImpl(localDiffusionGenerationRepository = get())
    }
    factory<ObserveCoreMlProcessStatusUseCase> {
        ObserveCoreMlProcessStatusUseCaseImpl(coreMlGenerationRepository = get())
    }
    factory<ObserveBonsaiProcessStatusUseCase> {
        ObserveBonsaiProcessStatusUseCaseImpl(bonsaiGenerationRepository = get())
    }
    factory<ObserveStableDiffusionCppProcessStatusUseCase> {
        ObserveStableDiffusionCppProcessStatusUseCaseImpl(
            stableDiffusionCppGenerationRepository = get(),
        )
    }
    factory<InterruptGenerationUseCase> {
        InterruptGenerationUseCaseImpl(
            stableDiffusionGenerationRepository = get(),
            hordeGenerationRepository = get(),
            localDiffusionGenerationRepository = get(),
            stableDiffusionCppGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            bonsaiGenerationRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<SaveGenerationResultUseCase> {
        SaveGenerationResultUseCaseImpl(repository = get())
    }
    factory<GetGenerationResultUseCase> {
        GetGenerationResultUseCaseImpl(repository = get())
    }
    factory<GetGenerationResultPagedUseCase> {
        GetGenerationResultPagedUseCaseImpl(repository = get())
    }
    factory<GetAllGalleryUseCase> {
        GetAllGalleryUseCaseImpl(repository = get())
    }
    factory<GetGalleryItemsUseCase> {
        GetGalleryItemsUseCaseImpl(generationResultRepository = get())
    }
    factory<DeleteGalleryItemUseCase> {
        DeleteGalleryItemUseCaseImpl(repository = get())
    }
    factory<DeleteGalleryItemsUseCase> {
        DeleteGalleryItemsUseCaseImpl(generationResultRepository = get())
    }
    factory<DeleteAllGalleryUseCase> {
        DeleteAllGalleryUseCaseImpl(generationResultRepository = get())
    }
    factory<SetGalleryItemsVisibilityUseCase> {
        SetGalleryItemsVisibilityUseCaseImpl(generationResultRepository = get())
    }
    factory<SetGalleryItemsLikedUseCase> {
        SetGalleryItemsLikedUseCaseImpl(generationResultRepository = get())
    }
    factory<GetMediaStoreInfoUseCase> {
        GetMediaStoreInfoUseCaseImpl(mediaStoreGateway = get())
    }
    factory<ToggleImageVisibilityUseCase> {
        ToggleImageVisibilityUseCaseImpl(repository = get())
    }
    factory<ToggleImageLikeUseCase> {
        ToggleImageLikeUseCaseImpl(repository = get())
    }
    factory<GetConfigurationUseCase> {
        DefaultGetConfigurationUseCaseImpl(
            configurationStore = get(),
            authorizationStore = get(),
        )
    }
    factory<SetServerConfigurationUseCase> {
        DefaultSetServerConfigurationUseCaseImpl(
            providerSelectionGuard = get(),
            configurationStore = get(),
            authorizationStore = get(),
            preferenceManager = get(),
        )
    }
    factory<ObserveNetworkUsageUseCase> {
        ObserveNetworkUsageUseCaseImpl(repository = get())
    }
    factory<ResetNetworkUsageUseCase> {
        ResetNetworkUsageUseCaseImpl(repository = get())
    }
    factory<ConnectToA1111UseCase> {
        DefaultConnectToA1111UseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testConnectivityUseCase = get(),
            dataPreLoaderUseCase = get(),
        )
    }
    factory<ConnectToSwarmUiUseCase> {
        DefaultConnectToSwarmUiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testSwarmUiConnectivityUseCase = get(),
        )
    }
    factory<ConnectToHordeUseCase> {
        DefaultConnectToHordeUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testHordeApiKeyUseCase = get(),
        )
    }
    factory<ConnectToHuggingFaceUseCase> {
        DefaultConnectToHuggingFaceUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testHuggingFaceApiKeyUseCase = get(),
        )
    }
    factory<ConnectToLocalDiffusionUseCase> {
        ConnectToLocalDiffusionUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToMediaPipeUseCase> {
        ConnectToMediaPipeUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToSdxlUseCase> {
        ConnectToSdxlUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToCoreMlUseCase> {
        ConnectToCoreMlUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToBonsaiUseCase> {
        ConnectToBonsaiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToOpenAiCompatibleUseCase> {
        DefaultConnectToOpenAiCompatibleUseCaseImpl(
            repository = get(),
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToOpenAiUseCase> {
        DefaultConnectToOpenAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testOpenAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToStabilityAiUseCase> {
        DefaultConnectToStabilityAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testStabilityAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToFalAiUseCase> {
        DefaultConnectToFalAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testFalAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToArliAiUseCase> {
        DefaultConnectToArliAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testArliAiApiKeyUseCase = get(),
        )
    }
    factory<FetchSupportersUseCase> {
        FetchSupportersUseCaseImpl(repository = get())
    }
    factory<FetchHuggingFaceModelsUseCase> {
        FetchHuggingFaceModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<SendReportUseCase> {
        SendReportUseCaseImpl(repository = get())
    }
    factory<FetchStabilityAiEnginesUseCase> {
        FetchStabilityAiEnginesUseCaseImpl(repository = get())
    }
    factory<FetchAndGetStabilityAiEnginesUseCase> {
        FetchAndGetStabilityAiEnginesUseCaseImpl(
            fetchStabilityAiEnginesUseCase = get(),
            preferenceManager = get(),
        )
    }
    factory<ObserveStabilityAiCreditsUseCase> {
        ObserveStabilityAiCreditsUseCaseImpl(repository = get())
    }
    factory<FetchSwarmUiModelsUseCase> {
        FetchSwarmUiModelsUseCaseImpl(remoteDataSource = get())
    }
    factory<FetchAndGetSwarmUiModelsUseCase> {
        FetchAndGetSwarmUiModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<FetchAndGetArliAiModelsUseCase> {
        FetchAndGetArliAiModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<FetchAndGetLorasUseCase> {
        FetchAndGetLorasUseCaseImpl(lorasRepository = get())
    }
    factory<FetchAndGetEmbeddingsUseCase> {
        FetchAndGetEmbeddingsUseCaseImpl(repository = get())
    }
    factory<FetchAndGetHyperNetworksUseCase> {
        FetchAndGetHyperNetworksUseCaseImpl(stableDiffusionHyperNetworksRepository = get())
    }
    factory<GetStableDiffusionModelsUseCase> {
        GetStableDiffusionModelsUseCaseImpl(
            preferenceManager = get(),
            serverConfigurationRepository = get(),
            sdModelsRepository = get(),
        )
    }
    factory<SelectStableDiffusionModelUseCase> {
        SelectStableDiffusionModelUseCaseImpl(
            serverConfigurationRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<GetStableDiffusionSamplersUseCase> {
        GetStableDiffusionSamplersUseCaseImpl(repository = get())
    }
    factory<GetForgeModulesUseCase> {
        GetForgeModulesUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<IsADetailerAvailableUseCase> {
        IsADetailerAvailableUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
}

package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.domain.repository.OpenAiCompatibleGenerationRepository
import com.shifthackz.aisdv1.domain.datasource.OpenAiCompatibleGenerationDataSource
import com.shifthackz.aisdv1.data.remote.KtorOpenAiCompatibleGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.repository.OpenAiCompatibleGenerationRepositoryImpl
import com.shifthackz.aisdv1.core.common.extensions.fixUrlSlashes
import com.shifthackz.aisdv1.data.gateway.ServerConnectivityGatewayImpl
import com.shifthackz.aisdv1.data.local.ArliAiModelsLocalDataSource
import com.shifthackz.aisdv1.data.local.DownloadableModelFileStore
import com.shifthackz.aisdv1.data.local.DownloadableModelLocalDataSource
import com.shifthackz.aisdv1.data.local.EmbeddingsLocalDataSource
import com.shifthackz.aisdv1.data.local.GenerationResultLocalDataSource
import com.shifthackz.aisdv1.data.local.HuggingFaceModelsLocalDataSource
import com.shifthackz.aisdv1.data.local.LorasLocalDataSource
import com.shifthackz.aisdv1.data.local.NoOpDownloadableModelFileStore
import com.shifthackz.aisdv1.data.local.ServerConfigurationLocalDataSource
import com.shifthackz.aisdv1.data.local.StabilityAiCreditsLocalDataSource
import com.shifthackz.aisdv1.data.local.StableDiffusionHyperNetworksLocalDataSource
import com.shifthackz.aisdv1.data.local.StableDiffusionModelsLocalDataSource
import com.shifthackz.aisdv1.data.local.StableDiffusionSamplersLocalDataSource
import com.shifthackz.aisdv1.data.local.SupportersLocalDataSource
import com.shifthackz.aisdv1.data.provider.ServerUrlProvider
import com.shifthackz.aisdv1.data.remote.DownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.remote.DownloadableModelRemoteDataSource
import com.shifthackz.aisdv1.data.remote.HordeStatusSource
import com.shifthackz.aisdv1.data.remote.KtorArliAiGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorArliAiModelsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorForgeModulesRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorFalAiGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorHordeGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorHuggingFaceGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorHuggingFaceModelsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorOpenAiGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorServerConfigurationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStabilityAiCreditsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStabilityAiEnginesRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStabilityAiGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionEmbeddingsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionHyperNetworksRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionLorasRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionModelsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionSamplersRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorStableDiffusionScriptsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorSupportersRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorSwarmUiEmbeddingsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorSwarmUiGenerationRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorSwarmUiLorasRemoteDataSource
import com.shifthackz.aisdv1.data.remote.KtorSwarmUiModelsRemoteDataSource
import com.shifthackz.aisdv1.data.remote.NoOpDownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.remote.RandomImageRemoteDataSource
import com.shifthackz.aisdv1.data.remote.ReportRemoteDataSource
import com.shifthackz.aisdv1.data.repository.ArliAiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.ArliAiModelsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.BonsaiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.CoreMlGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.DownloadableModelRepositoryImpl
import com.shifthackz.aisdv1.data.repository.EmbeddingsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.FalAiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.ForgeModulesRepositoryImpl
import com.shifthackz.aisdv1.data.repository.GenerationResultRepositoryImpl
import com.shifthackz.aisdv1.data.repository.HordeGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.HuggingFaceGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.HuggingFaceModelsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.LorasRepositoryImpl
import com.shifthackz.aisdv1.data.repository.NetworkUsageRepositoryImpl
import com.shifthackz.aisdv1.data.repository.OpenAiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.RandomImageRepositoryImpl
import com.shifthackz.aisdv1.data.repository.RemoteStabilityAiEnginesRepository
import com.shifthackz.aisdv1.data.repository.ReportRepositoryImpl
import com.shifthackz.aisdv1.data.repository.ServerConfigurationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StabilityAiCreditsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StabilityAiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionCppGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionHyperNetworksRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionModelsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionSamplersRepositoryImpl
import com.shifthackz.aisdv1.data.repository.StableDiffusionScriptsRepositoryImpl
import com.shifthackz.aisdv1.data.repository.SupportersRepositoryImpl
import com.shifthackz.aisdv1.data.repository.SwarmUiGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.TemporaryGenerationResultRepositoryImpl
import com.shifthackz.aisdv1.domain.datasource.ArliAiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.ArliAiModelsDataSource
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.datasource.EmbeddingsDataSource
import com.shifthackz.aisdv1.domain.datasource.FalAiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.ForgeModulesDataSource
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.datasource.HordeGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.HuggingFaceGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.HuggingFaceModelsDataSource
import com.shifthackz.aisdv1.domain.datasource.HuggingFaceModelsRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.LorasDataSource
import com.shifthackz.aisdv1.domain.datasource.OpenAiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.RandomImageDataSource
import com.shifthackz.aisdv1.domain.datasource.ReportDataSource
import com.shifthackz.aisdv1.domain.datasource.ServerConfigurationDataSource
import com.shifthackz.aisdv1.domain.datasource.StabilityAiCreditsDataSource
import com.shifthackz.aisdv1.domain.datasource.StabilityAiCreditsRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.StabilityAiEnginesRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.StabilityAiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionModelsDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionSamplersDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionScriptsDataSource
import com.shifthackz.aisdv1.domain.datasource.SupportersDataSource
import com.shifthackz.aisdv1.domain.datasource.SupportersRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.SwarmUiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.SwarmUiModelsRemoteDataSource
import com.shifthackz.aisdv1.domain.demo.ImageToImageDemo
import com.shifthackz.aisdv1.domain.demo.NoOpImageToImageDemo
import com.shifthackz.aisdv1.domain.demo.NoOpTextToImageDemo
import com.shifthackz.aisdv1.domain.demo.TextToImageDemo
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.NoOpMediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.ServerConnectivityGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.ArliAiModelsRepository
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import com.shifthackz.aisdv1.domain.repository.EmbeddingsRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.ForgeModulesRepository
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceModelsRepository
import com.shifthackz.aisdv1.domain.repository.LorasRepository
import com.shifthackz.aisdv1.domain.repository.NetworkUsageRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.RandomImageRepository
import com.shifthackz.aisdv1.domain.repository.ReportRepository
import com.shifthackz.aisdv1.domain.repository.ServerConfigurationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiCreditsRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiEnginesRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionScriptsRepository
import com.shifthackz.aisdv1.domain.repository.SupportersRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.TemporaryGenerationResultRepository
import com.shifthackz.aisdv1.network.connectivity.ConnectivityMonitor
import com.shifthackz.aisdv1.storage.di.databaseModule
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Data-layer Koin module that wires repositories, network gateways, and persistent data sources.
 *
 * This module binds [NetworkUsageRepositoryImpl] so low-level traffic counters are backed by Room
 * instead of volatile preferences or lifecycle refreshes.
 *
 * @author Dmitriy Moroz
 */
val coreDataModule = module {
    includes(databaseModule, preferenceDataModule, swarmUiModelsDataModule)

    single {
        ServerUrlProvider { endpoint ->
            val prefs = get<PreferenceManager>()
            val baseUrl = if (prefs.source == ServerSource.SWARM_UI) {
                prefs.swarmUiServerUrl
            } else {
                prefs.automatic1111ServerUrl
            }
            "${baseUrl.fixUrlSlashes()}/$endpoint"
        }
    }
    factory<ServerConnectivityGateway> {
        val lambda: () -> Boolean = {
            val prefs = get<PreferenceManager>()
            prefs.source == ServerSource.AUTOMATIC1111 || prefs.source == ServerSource.SWARM_UI
        }
        val monitor = get<ConnectivityMonitor> { parametersOf(lambda) }
        ServerConnectivityGatewayImpl(monitor, get())
    }
    single<MediaStoreGateway> {
        NoOpMediaStoreGateway
    }
    single<TextToImageDemo> {
        NoOpTextToImageDemo
    }
    single<ImageToImageDemo> {
        NoOpImageToImageDemo
    }
    single<GenerationResultDataSource.Local> {
        GenerationResultLocalDataSource(dao = get())
    }
    single<StableDiffusionModelsDataSource.Local> {
        StableDiffusionModelsLocalDataSource(dao = get())
    }
    single<ArliAiModelsDataSource.Local> {
        ArliAiModelsLocalDataSource(dao = get())
    }
    single<StableDiffusionSamplersDataSource.Local> {
        StableDiffusionSamplersLocalDataSource(dao = get())
    }
    single<LorasDataSource.Local> {
        LorasLocalDataSource(dao = get())
    }
    single<StableDiffusionHyperNetworksDataSource.Local> {
        StableDiffusionHyperNetworksLocalDataSource(dao = get())
    }
    single<EmbeddingsDataSource.Local> {
        EmbeddingsLocalDataSource(dao = get())
    }
    single<ServerConfigurationDataSource.Local> {
        ServerConfigurationLocalDataSource(dao = get())
    }
    single<StabilityAiCreditsDataSource.Local> {
        StabilityAiCreditsLocalDataSource()
    }
    single<HuggingFaceModelsDataSource.Local> {
        HuggingFaceModelsLocalDataSource(dao = get())
    }
    single<SupportersDataSource.Local> {
        SupportersLocalDataSource(dao = get())
    }
    single<NetworkUsageRepository> {
        NetworkUsageRepositoryImpl(dao = get())
    }
    single<DownloadableModelDataSource.Remote> {
        DownloadableModelRemoteDataSource(api = get(), fileDownloader = get())
    }
    single<DownloadableModelFileDownloader> {
        NoOpDownloadableModelFileDownloader
    }
    single<RandomImageDataSource.Remote> {
        RandomImageRemoteDataSource(api = get())
    }
    single<DownloadableModelDataSource.Local> {
        DownloadableModelLocalDataSource(
            dao = get(),
            preferenceManager = get(),
            buildInfoProvider = get(),
            fileStore = get(),
        )
    }
    single<DownloadableModelFileStore> {
        NoOpDownloadableModelFileStore
    }
    single<DownloadableModelRepository> {
        DownloadableModelRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            buildInfoProvider = get(),
        )
    }
    single<GenerationResultRepository> {
        GenerationResultRepositoryImpl(
            preferenceManager = get(),
            mediaStoreGateway = get(),
            localDataSource = get(),
        )
    }
    single<TemporaryGenerationResultRepository> {
        TemporaryGenerationResultRepositoryImpl()
    }
    single<SupportersRemoteDataSource> {
        KtorSupportersRemoteDataSource(api = get())
    }
    single<SupportersRepository> {
        SupportersRepositoryImpl(remoteDataSource = get(), localDataSource = get())
    }
    single<ReportDataSource.Remote> {
        ReportRemoteDataSource(api = get())
    }
    single<ReportRepository> {
        ReportRepositoryImpl(rds = get(), preferenceManager = get())
    }
    single<RandomImageRepository> {
        RandomImageRepositoryImpl(remoteDataSource = get())
    }
    single<HuggingFaceModelsRemoteDataSource> {
        KtorHuggingFaceModelsRemoteDataSource(api = get())
    }
    single<HuggingFaceModelsRepository> {
        HuggingFaceModelsRepositoryImpl(remoteDataSource = get(), localDataSource = get())
    }
    single<StabilityAiEnginesRemoteDataSource> {
        KtorStabilityAiEnginesRemoteDataSource(api = get())
    }
    single<StabilityAiCreditsRemoteDataSource> {
        KtorStabilityAiCreditsRemoteDataSource(api = get())
    }
    single<StabilityAiEnginesRepository> {
        RemoteStabilityAiEnginesRepository(remoteDataSource = get())
    }
    single<StabilityAiCreditsRepository> {
        StabilityAiCreditsRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
        )
    }
    single<OpenAiCompatibleGenerationDataSource.Remote> {
        KtorOpenAiCompatibleGenerationRemoteDataSource(api = get())
    }
    single<OpenAiCompatibleGenerationRepository> {
        OpenAiCompatibleGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            preferenceManager = get(),
            backgroundWorkObserver = get(),
            remoteDataSource = get(),
        )
    }
    single<OpenAiGenerationDataSource.Remote> {
        KtorOpenAiGenerationRemoteDataSource(api = get())
    }
    single<OpenAiGenerationRepository> {
        OpenAiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            preferenceManager = get(),
            backgroundWorkObserver = get(),
            remoteDataSource = get(),
        )
    }
    single<HordeGenerationDataSource.StatusSource> {
        HordeStatusSource()
    }
    single<HordeGenerationDataSource.Remote> {
        KtorHordeGenerationRemoteDataSource(api = get(), statusSource = get())
    }
    single<HordeGenerationRepository> {
        HordeGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            preferenceManager = get(),
            backgroundWorkObserver = get(),
            remoteDataSource = get(),
            statusSource = get(),
        )
    }
    single<HuggingFaceGenerationDataSource.Remote> {
        KtorHuggingFaceGenerationRemoteDataSource(api = get())
    }
    single<HuggingFaceGenerationRepository> {
        HuggingFaceGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            remoteDataSource = get(),
        )
    }
    single<FalAiGenerationDataSource.Remote> {
        KtorFalAiGenerationRemoteDataSource(api = get())
    }
    single<FalAiGenerationRepository> {
        FalAiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            remoteDataSource = get(),
        )
    }
    single<ArliAiGenerationDataSource.Remote> {
        KtorArliAiGenerationRemoteDataSource(api = get())
    }
    single<ArliAiModelsDataSource.Remote> {
        KtorArliAiModelsRemoteDataSource(api = get())
    }
    single<ArliAiModelsRepository> {
        ArliAiModelsRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
        )
    }
    single<ArliAiGenerationRepository> {
        ArliAiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            remoteDataSource = get(),
        )
    }
    single<StabilityAiGenerationDataSource.Remote> {
        KtorStabilityAiGenerationRemoteDataSource(api = get())
    }
    single<StabilityAiGenerationRepository> {
        StabilityAiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            backgroundWorkObserver = get(),
            localDataSource = get(),
            preferenceManager = get(),
            generationRds = get(),
            creditsRds = get(),
            creditsLds = get(),
        )
    }
    single<CoreMlGenerationRepository> {
        CoreMlGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            coreMlDiffusion = get(),
            downloadableLocalDataSource = get(),
            fileProviderDescriptor = get(),
        )
    }
    single<BonsaiGenerationRepository> {
        BonsaiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            bonsaiDiffusion = get(),
            downloadableLocalDataSource = get(),
            fileProviderDescriptor = get(),
        )
    }
    single<StableDiffusionCppGenerationRepository> {
        StableDiffusionCppGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            stableDiffusionCpp = get(),
            downloadableLocalDataSource = get(),
            modelFileStore = get(),
        )
    }
    single<StableDiffusionGenerationDataSource.Remote> {
        KtorStableDiffusionGenerationRemoteDataSource(api = get())
    }
    single<StableDiffusionGenerationRepository> {
        StableDiffusionGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            backgroundWorkObserver = get(),
            localDataSource = get(),
            remoteDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
            textToImageDemo = get(),
            imageToImageDemo = get(),
        )
    }
    single<StableDiffusionModelsDataSource.Remote> {
        KtorStableDiffusionModelsRemoteDataSource(api = get())
    }
    single<StableDiffusionModelsRepository> {
        StableDiffusionModelsRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<StableDiffusionSamplersDataSource.Remote> {
        KtorStableDiffusionSamplersRemoteDataSource(api = get())
    }
    single<StableDiffusionSamplersRepository> {
        StableDiffusionSamplersRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<ForgeModulesDataSource> {
        KtorForgeModulesRemoteDataSource(api = get())
    }
    single<ForgeModulesRepository> {
        ForgeModulesRepositoryImpl(
            remoteDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<StableDiffusionScriptsDataSource> {
        KtorStableDiffusionScriptsRemoteDataSource(api = get())
    }
    single<StableDiffusionScriptsRepository> {
        StableDiffusionScriptsRepositoryImpl(
            remoteDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<ServerConfigurationDataSource.Remote> {
        KtorServerConfigurationRemoteDataSource(api = get())
    }
    single<ServerConfigurationRepository> {
        ServerConfigurationRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<SwarmUiModelsRemoteDataSource> {
        KtorSwarmUiModelsRemoteDataSource(api = get())
    }
    single<SwarmUiGenerationDataSource.Remote> {
        KtorSwarmUiGenerationRemoteDataSource(api = get())
    }
    single<SwarmUiGenerationRepository> {
        SwarmUiGenerationRepositoryImpl(
            mediaStoreGateway = get(),
            localDataSource = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            sessionPreference = get(),
            authorizationStore = get(),
            swarmSessionRemoteDataSource = get(),
            remoteDataSource = get(),
        )
    }
    single<LorasDataSource.Remote.Automatic1111> {
        KtorStableDiffusionLorasRemoteDataSource(api = get())
    }
    single<LorasDataSource.Remote.SwarmUi> {
        KtorSwarmUiLorasRemoteDataSource(api = get())
    }
    single<LorasRepository> {
        LorasRepositoryImpl(
            rdsA1111 = get(),
            rdsSwarm = get(),
            swarmSessionRemoteDataSource = get(),
            lds = get(),
            preferenceManager = get(),
            sessionPreference = get(),
            authorizationStore = get(),
        )
    }
    single<EmbeddingsDataSource.Remote.Automatic1111> {
        KtorStableDiffusionEmbeddingsRemoteDataSource(api = get())
    }
    single<StableDiffusionHyperNetworksDataSource.Remote> {
        KtorStableDiffusionHyperNetworksRemoteDataSource(api = get())
    }
    single<StableDiffusionHyperNetworksRepository> {
        StableDiffusionHyperNetworksRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            preferenceManager = get(),
            authorizationStore = get(),
        )
    }
    single<EmbeddingsDataSource.Remote.SwarmUi> {
        KtorSwarmUiEmbeddingsRemoteDataSource(api = get())
    }
    single<EmbeddingsRepository> {
        EmbeddingsRepositoryImpl(
            rdsA1111 = get(),
            rdsSwarm = get(),
            swarmSessionRemoteDataSource = get(),
            lds = get(),
            preferenceManager = get(),
            sessionPreference = get(),
            authorizationStore = get(),
        )
    }
}

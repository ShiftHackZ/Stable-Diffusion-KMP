package com.shifthackz.aisdv1.presentation.screen.settings

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.model.Quadruple
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCase
import com.shifthackz.aisdv1.presentation.modal.language.applyAppLanguage
import com.shifthackz.aisdv1.presentation.model.LaunchSource
import com.shifthackz.aisdv1.presentation.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuAccessor
import com.shifthackz.aisdv1.presentation.screen.drawer.DrawerIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsModal
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsState
import com.shifthackz.aisdv1.presentation.screen.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.setupAllowedModes
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import com.shifthackz.aisdv1.presentation.model.shouldUseCoreMlModelStoreFallback
import com.shifthackz.aisdv1.presentation.model.storageTextByteSize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Settings tab.
 *
 * It owns the lightweight settings summaries, including observed storage and network usage byte
 * totals, and delegates detailed cleanup/statistics flows to standalone usage screens.
 *
 * @param dispatchersProvider App coroutine dispatchers used by the MVI base class and IO work.
 * @param getStableDiffusionModelsUseCase Reads provider model names for the Settings picker.
 * @param observeStabilityAiCreditsUseCase Observes Stability AI credits for Settings summary rows.
 * @param selectStableDiffusionModelUseCase Persists the selected remote model.
 * @param clearAppCacheUseCase Legacy cache use case retained for existing Settings flows.
 * @param getAllGalleryUseCase Reads generated gallery records for storage summaries.
 * @param getLocalOnnxModelsUseCase Reads locally configured ONNX models.
 * @param getLocalMediaPipeModelsUseCase Reads locally configured MediaPipe models.
 * @param getLocalSdxlModelsUseCase Reads locally configured SDXL models.
 * @param getLocalCoreMlModelsUseCase Reads locally configured Core ML models on supported builds.
 * @param getLocalBonsaiModelsUseCase Reads locally configured Bonsai models on supported builds.
 * @param observeNetworkUsageUseCase Live Room-backed network usage stream.
 * @param storageUsageObserver Shared invalidation stream for non-Room storage usage.
 * @param preferenceManager Preferences source observed for provider/model refreshes.
 * @param debugMenuAccessor Debug menu availability gate.
 * @param buildInfoProvider Build metadata used to filter platform-supported settings.
 * @param linksProvider External links shown from Settings.
 * @param router Settings navigation contract.
 * @param platformActions Platform permission and settings bridge.
 * @param onError Error callback forwarded to the app-level error handling pipeline.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    dispatchersProvider: DispatchersProvider,
    getStableDiffusionModelsUseCase: GetStableDiffusionModelsUseCase,
    observeStabilityAiCreditsUseCase: ObserveStabilityAiCreditsUseCase,
    private val selectStableDiffusionModelUseCase: SelectStableDiffusionModelUseCase,
    private val clearAppCacheUseCase: ClearAppCacheUseCase,
    private val getAllGalleryUseCase: GetAllGalleryUseCase,
    private val getLocalOnnxModelsUseCase: GetLocalOnnxModelsUseCase,
    private val getLocalMediaPipeModelsUseCase: GetLocalMediaPipeModelsUseCase,
    private val getLocalSdxlModelsUseCase: GetLocalSdxlModelsUseCase,
    private val getLocalCoreMlModelsUseCase: GetLocalCoreMlModelsUseCase,
    private val getLocalBonsaiModelsUseCase: GetLocalBonsaiModelsUseCase,
    private val observeNetworkUsageUseCase: ObserveNetworkUsageUseCase,
    private val storageUsageObserver: StorageUsageObserver,
    private val preferenceManager: PreferenceManager,
    private val debugMenuAccessor: DebugMenuAccessor,
    private val buildInfoProvider: BuildInfoProvider,
    private val linksProvider: LinksProvider,
    private val router: SettingsRouter,
    private val platformActions: SettingsPlatformActions,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<SettingsState, SettingsIntent, EmptyEffect>(
    initialState = SettingsState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    private val ioDispatcher = dispatchersProvider.io
    private val immediateDispatcher = dispatchersProvider.immediate

    private val appVersionProducer = flow { emit(buildInfoProvider.toString()) }

    private val sdModelsProducer = preferenceManager.observe()
        .map {
            SdModelsRefreshKey(
                source = it.source,
                serverUrl = it.serverUrl,
                demoMode = it.demoMode,
                sdModel = it.sdModel,
            )
        }
        .distinctUntilChanged()
        .map {
            runCatching {
                withTimeout(10_000L.milliseconds) { getStableDiffusionModelsUseCase() }
            }.getOrDefault(emptyList())
        }

    private val stabilityCreditsProducer = preferenceManager.observe()
        .map { it.source }
        .distinctUntilChanged()
        .flatMapLatest { source ->
            if (source == ServerSource.STABILITY_AI) {
                observeStabilityAiCreditsUseCase()
            } else {
                flowOf(0f)
            }
        }

    init {
        launch(ioDispatcher) {
            combine(
                appVersionProducer,
                sdModelsProducer,
                preferenceManager.observe(),
                stabilityCreditsProducer,
                ::Quadruple,
            )
                .catch { onError(it) }
                .collect { (version, modelData, settings, credits) ->
                    if (settings.backgroundGeneration && !platformActions.supportsBackgroundGeneration) {
                        preferenceManager.backgroundGeneration = false
                    }
                    val modelTitles = modelData.map { (model, _) -> model.title }
                    val selectedRemoteModel = modelData
                        .firstOrNull { (_, selected) -> selected }
                        ?.first
                        ?.title
                    updateState { state ->
                        state.copy(
                            loading = false,
                            serverSource = settings.source,
                            generationRequestTimeout = settings.generationRequestTimeout,
                            sdModels = modelTitles,
                            sdModelSelected = selectedRemoteModel
                                ?: settings.sdModel.takeIf(modelTitles::contains)
                                ?: modelTitles.firstOrNull()
                                ?: "",
                            stabilityAiCredits = credits,
                            localUseNNAPI = settings.localUseNNAPI,
                            backgroundGenerationAvailable = platformActions.supportsBackgroundGeneration,
                            backgroundGeneration = settings.backgroundGeneration &&
                                platformActions.supportsBackgroundGeneration,
                            monitorConnectivity = settings.monitorConnectivity,
                            autoSaveAiResults = settings.autoSaveAiResults,
                            saveToMediaStore = settings.saveToMediaStore,
                            formAdvancedOptionsAlwaysShow = settings.formAdvancedOptionsAlwaysShow,
                            formPromptTaggedInput = settings.formPromptTaggedInput,
                            useSystemColorPalette = settings.designUseSystemColorPalette,
                            useSystemDarkTheme = settings.designUseSystemDarkTheme,
                            darkTheme = settings.designDarkTheme,
                            colorToken = ColorToken.parse(settings.designColorToken),
                            darkThemeToken = DarkThemeToken.parse(settings.designDarkThemeToken),
                            galleryGrid = settings.galleryGrid,
                            developerMode = settings.developerMode,
                            appVersion = version,
                        )
                    }
                }
        }
        observeStorageUsageState()
        observeNetworkUsageState()
    }

    override fun processIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Action.AppVersion -> if (debugMenuAccessor()) {
                platformActions.showDeveloperModeUnlocked()
            }

            SettingsIntent.Action.ClearAppCache.Request -> updateState {
                it.copy(screenModal = SettingsModal.ClearAppCache)
            }

            SettingsIntent.Action.ClearAppCache.Confirm -> clearAppCache()

            SettingsIntent.Action.ReportProblem -> platformActions.shareLogFile()

            SettingsIntent.DismissDialog -> updateState {
                it.copy(screenModal = SettingsModal.None)
            }

            SettingsIntent.NavigateConfiguration -> router.navigateToServerSetup(
                LaunchSource.SETTINGS,
            )

            SettingsIntent.NavigateBenchmark -> router.navigateToBenchmark()

            SettingsIntent.PickGenerationTimeout -> updateState {
                it.copy(screenModal = SettingsModal.GenerationTimeout(it.generationRequestTimeout))
            }

            is SettingsIntent.SetGenerationTimeout -> {
                preferenceManager.generationRequestTimeout = intent.timeout
                updateState { it.copy(screenModal = SettingsModal.None) }
            }

            SettingsIntent.NavigateStorageUsage -> router.navigateToStorageUsage()

            SettingsIntent.NavigateNetworkUsage -> router.navigateToNetworkUsage()

            SettingsIntent.NavigateDeveloperMode -> router.navigateToDebugMenu()

            SettingsIntent.SdModel.OpenChooser -> updateState {
                it.copy(
                    screenModal = SettingsModal.SelectSdModel(
                        models = it.sdModels,
                        selected = it.sdModelSelected,
                    ),
                )
            }

            is SettingsIntent.SdModel.Select -> selectSdModel(intent.model)

            is SettingsIntent.UpdateFlag.AdvancedFormVisibility -> {
                preferenceManager.formAdvancedOptionsAlwaysShow = intent.flag
            }

            is SettingsIntent.UpdateFlag.AutoSaveResult -> {
                preferenceManager.autoSaveAiResults = intent.flag
            }

            is SettingsIntent.UpdateFlag.MonitorConnection -> {
                preferenceManager.monitorConnectivity = intent.flag
            }

            is SettingsIntent.UpdateFlag.NNAPI -> {
                preferenceManager.localOnnxUseNNAPI = intent.flag
            }

            is SettingsIntent.UpdateFlag.TaggedInput -> {
                preferenceManager.formPromptTaggedInput = intent.flag
            }

            is SettingsIntent.UpdateFlag.SaveToMediaStore -> {
                changeSaveToMediaStoreSetting(intent.flag)
            }

            SettingsIntent.LaunchUrl.OpenPolicy -> {
                platformActions.openUrl(linksProvider.privacyPolicyUrl)
            }

            SettingsIntent.LaunchUrl.OpenSourceCode -> {
                platformActions.openUrl(linksProvider.gitHubSourceUrl)
            }

            SettingsIntent.LaunchUrl.OpenProjectWebsite -> {
                platformActions.openUrl(linksProvider.projectWebsiteUrl)
            }

            SettingsIntent.LaunchUrl.OpenDeveloperWebsite -> {
                platformActions.openUrl(linksProvider.developerWebsiteUrl)
            }

            SettingsIntent.LaunchUrl.OpenLicense -> {
                platformActions.openUrl(linksProvider.licenseUrl)
            }

            SettingsIntent.LaunchUrl.OpenTelegramCommunity -> {
                platformActions.openUrl(linksProvider.telegramCommunityUrl)
            }

            SettingsIntent.LaunchUrl.OpenDiscordCommunity -> {
                platformActions.openUrl(linksProvider.discordCommunityUrl)
            }

            is SettingsIntent.UpdateFlag.DynamicColors -> {
                preferenceManager.designUseSystemColorPalette = intent.flag
            }

            is SettingsIntent.UpdateFlag.SystemDarkTheme -> {
                preferenceManager.designUseSystemDarkTheme = intent.flag
            }

            is SettingsIntent.UpdateFlag.DarkTheme -> {
                preferenceManager.designDarkTheme = intent.flag
            }

            is SettingsIntent.NewColorToken -> {
                preferenceManager.designColorToken = "${intent.token}"
            }

            is SettingsIntent.NewDarkThemeToken -> {
                preferenceManager.designDarkThemeToken = "${intent.token}"
            }

            SettingsIntent.Action.PickLanguage -> updateState {
                it.copy(screenModal = SettingsModal.Language)
            }

            is SettingsIntent.Action.SetLanguage -> {
                preferenceManager.languageCode = intent.languageCode
                applyAppLanguage(intent.languageCode)
                updateState { it.copy(screenModal = SettingsModal.None) }
            }

            SettingsIntent.Action.GalleryGrid.Pick -> updateState {
                it.copy(screenModal = SettingsModal.GalleryGrid(it.galleryGrid))
            }

            is SettingsIntent.Action.GalleryGrid.Set -> {
                preferenceManager.galleryGrid = intent.grid
                updateState { it.copy(screenModal = SettingsModal.None) }
            }

            is SettingsIntent.Drawer -> when (intent.intent) {
                DrawerIntent.Close -> router.closeDrawer()
                DrawerIntent.Open -> router.openDrawer()
            }

            SettingsIntent.Action.Donate -> router.navigateToDonate()

            SettingsIntent.Action.OnBoarding -> router.navigateToOnBoarding(
                source = LaunchSource.SETTINGS,
            )

            is SettingsIntent.UpdateFlag.BackgroundGeneration -> {
                changeBackgroundGenerationSetting(intent.flag)
            }
        }
    }

    private fun selectSdModel(value: String) {
        updateState { state ->
            state.copy(screenModal = SettingsModal.Communicating)
        }

        launch(ioDispatcher) {
            try {
                selectStableDiffusionModelUseCase(value)
            } catch (t: Throwable) {
                onError(t)
            } finally {
                processIntent(SettingsIntent.DismissDialog)
            }
        }
    }

    private fun clearAppCache() {
        updateState { it.copy(screenModal = SettingsModal.Communicating) }
        launch(ioDispatcher) {
            runCatching { clearAppCacheUseCase() }
                .onSuccess { storageUsageObserver.notifyChanged() }
                .onFailure(onError)
            processIntent(SettingsIntent.DismissDialog)
        }
    }

    private fun observeStorageUsageState() {
        launch(ioDispatcher) {
            storageUsageObserver.observe()
                .map {
                    runCatching { loadStorageUsageBytes() }
                        .onFailure(onError)
                        .getOrDefault(currentState.storageUsageBytes)
                }
                .distinctUntilChanged()
                .collect { storageUsageBytes ->
                    updateState { state ->
                        state.copy(storageUsageBytes = storageUsageBytes)
                    }
                }
        }
    }

    private fun observeNetworkUsageState() {
        launch(ioDispatcher) {
            observeNetworkUsageUseCase()
                .catch { t ->
                    onError(t)
                }
                .collect { usage ->
                    updateState { state ->
                        state.copy(networkUsageBytes = usage.totalBytes)
                    }
                }
        }
    }

    private suspend fun loadStorageUsageBytes(): Long {
        val onnxModels = getDownloadedModels(getLocalOnnxModelsUseCase::invoke)
        val mediaPipeModels = getDownloadedModels(getLocalMediaPipeModelsUseCase::invoke)
        val sdxlModels = getDownloadedModels(getLocalSdxlModelsUseCase::invoke)
        val coreMlModels = getDownloadedModels(getLocalCoreMlModelsUseCase::invoke)
        val bonsaiModels = getDownloadedModels(getLocalBonsaiModelsUseCase::invoke)
        val coreMlModelIds = coreMlModels.map(LocalAiModel::id)
        val allowedModes = buildInfoProvider.setupAllowedModes()
        val galleryBytes = getAllGalleryUseCase().sumOf { item ->
            item.image.storageTextByteSize() + item.inputImage.storageTextByteSize()
        }
        var modelBytes = 0L
        if (ServerSource.LOCAL_MICROSOFT_ONNX in allowedModes) {
            modelBytes += platformActions.mapStorageBytesForUi(
                platformActions.getDownloadedModelsBytes(onnxModels.map(LocalAiModel::id)),
            )
        }
        if (ServerSource.LOCAL_GOOGLE_MEDIA_PIPE in allowedModes) {
            modelBytes += platformActions.mapStorageBytesForUi(
                platformActions.getDownloadedModelsBytes(mediaPipeModels.map(LocalAiModel::id)),
            )
        }
        if (ServerSource.LOCAL_STABLE_DIFFUSION_CPP in allowedModes) {
            modelBytes += platformActions.mapStorageBytesForUi(
                platformActions.getDownloadedModelsBytes(sdxlModels.map(LocalAiModel::id)),
            )
        }
        if (ServerSource.LOCAL_APPLE_CORE_ML in allowedModes) {
            modelBytes += platformActions.mapStorageBytesForUi(
                getCoreMlModelBytes(
                    allowedModes = allowedModes,
                    modelIds = coreMlModelIds,
                ),
            )
        }
        if (ServerSource.LOCAL_APPLE_BONSAI in allowedModes) {
            modelBytes += platformActions.mapStorageBytesForUi(
                platformActions.getDownloadedModelsBytes(bonsaiModels.map(LocalAiModel::id)),
            )
        }
        val cacheBytes = platformActions.mapStorageBytesForUi(platformActions.getAppCacheBytes())
        return cacheBytes + galleryBytes + modelBytes
    }

    /**
     * Reads Core ML model bytes from the entire platform model store on iOS-style targets.
     *
     * @param allowedModes Providers available on the current platform.
     * @param modelIds Catalog model identifiers known to the app.
     * @return Filesystem byte count included in the Settings storage summary.
     * @author Dmitriy Moroz
     */
    private suspend fun getCoreMlModelBytes(
        allowedModes: List<ServerSource>,
        modelIds: List<String>,
    ): Long =
        if (allowedModes.shouldUseCoreMlModelStoreFallback()) {
            platformActions.getAllDownloadedModelsBytes()
        } else {
            platformActions.getDownloadedModelsBytes(modelIds)
        }

    private suspend fun getDownloadedModels(
        getModels: suspend () -> List<LocalAiModel>,
    ): List<LocalAiModel> = runCatching { getModels() }
        .onFailure(onError)
        .getOrDefault(emptyList())
        .filter { model ->
            model.downloaded &&
                model.id !in customModelIds
        }

    private fun changeSaveToMediaStoreSetting(value: Boolean) {
        if (!value) {
            preferenceManager.saveToMediaStore = false
            updateState { it.copy(saveToMediaStore = false) }
            return
        }

        if (!platformActions.requiresStoragePermissionForMediaStore) {
            preferenceManager.saveToMediaStore = true
            updateState { it.copy(saveToMediaStore = true) }
            return
        }

        launch(immediateDispatcher) {
            if (platformActions.requestStoragePermission()) {
                preferenceManager.saveToMediaStore = true
            } else {
                updateState {
                    it.copy(
                        screenModal = SettingsModal.ManualPermission(
                            permission = Localization.string("permission_storage"),
                        ),
                    )
                }
            }
        }
    }

    private fun changeBackgroundGenerationSetting(value: Boolean) {
        if (!platformActions.supportsBackgroundGeneration) {
            preferenceManager.backgroundGeneration = false
            updateState {
                it.copy(
                    backgroundGeneration = false,
                    backgroundGenerationAvailable = false,
                )
            }
            return
        }

        if (!value) {
            preferenceManager.backgroundGeneration = false
            updateState { it.copy(backgroundGeneration = false) }
            return
        }

        launch(immediateDispatcher) {
            if (platformActions.requestNotificationPermission()) {
                preferenceManager.backgroundGeneration = true
            } else {
                updateState {
                    it.copy(
                        screenModal = SettingsModal.ManualPermission(
                            permission = Localization.string("permission_notifications"),
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Minimal key that triggers remote model list refreshes when provider-relevant settings change.
 *
 * @param source Current generation provider.
 * @param serverUrl Provider URL whose changes require a model-list refresh.
 * @param demoMode Whether demo mode is active and remote refreshes should be skipped.
 * @param sdModel Currently selected model name used to keep list state stable.
 *
 * @author Dmitriy Moroz
 */
private data class SdModelsRefreshKey(
    val source: ServerSource,
    val serverUrl: String,
    val demoMode: Boolean,
    val sdModel: String,
)

/**
 * Custom local model placeholders are user-managed paths and must not count as downloaded files.
 *
 * @author Dmitriy Moroz
 */
private val customModelIds = setOf(
    LocalAiModel.CustomOnnx.id,
    LocalAiModel.CustomMediaPipe.id,
    LocalAiModel.CustomSdxl.id,
    LocalAiModel.CustomCoreMl.id,
    LocalAiModel.CustomBonsai.id,
)

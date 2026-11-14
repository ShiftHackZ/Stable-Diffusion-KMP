package com.shifthackz.aisdv1.presentation.screen.txt2img

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.feature.benchmark.LocalGenerationBenchmarkGate
import com.shifthackz.aisdv1.presentation.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.navigation.router.TextToImageRouter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

/**
 * View-model for the txt2img screen.
 *
 * It loads provider configuration, mirrors the shared generation form state,
 * gates local providers through benchmark recommendations, and delegates actual
 * generation work to the action handler.
 */
class TextToImageViewModel(
    private val dispatchersProvider: DispatchersProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val getStableDiffusionSamplersUseCase: GetStableDiffusionSamplersUseCase,
    private val getForgeModulesUseCase: GetForgeModulesUseCase,
    private val fetchAndGetArliAiModelsUseCase: FetchAndGetArliAiModelsUseCase,
    private val isADetailerAvailableUseCase: IsADetailerAvailableUseCase,
    private val textToImageUseCase: TextToImageUseCase,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    private val interruptGenerationUseCase: InterruptGenerationUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val observeStableDiffusionCppProcessStatusUseCase: ObserveStableDiffusionCppProcessStatusUseCase,
    private val observeCoreMlProcessStatusUseCase: ObserveCoreMlProcessStatusUseCase,
    private val observeBonsaiProcessStatusUseCase: ObserveBonsaiProcessStatusUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundTaskManager: BackgroundTaskManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val wakeLockInterActor: WakeLockInterActor,
    private val platformServices: GenerationPlatformServices,
    private val buildInfoProvider: BuildInfoProvider,
    private val generationFormUpdateEvent: GenerationFormUpdateEvent,
    private val dimensionValidator: DimensionValidator,
    private val localGenerationBenchmarkGateProvider: () -> LocalGenerationBenchmarkGate,
    private val imageSaver: ImageSaver,
    private val imageSharer: ImageSharer,
    private val router: TextToImageRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<TextToImageState, TextToImageIntent, EmptyEffect>(
    initialState = TextToImageState(
        openAiCompatibleModel = preferenceManager.openAiCompatibleModel,
        samplingSteps = preferenceManager.textToImageSamplingSteps,
        cfgScale = preferenceManager.textToImageCfgScale,
        platform = buildInfoProvider.platform,
        bonsaiBackendSelectionVisible = buildInfoProvider.platform == Platform.ANDROID,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        loadConfiguration()
        observeSettings()
        consumePendingGenerationFormUpdate()
        observeGenerationFormUpdate()
        observeGenerationProgress()
    }

    private var stableDiffusionSamplers: List<String>? = null
    private var stableDiffusionSamplersKey: StableDiffusionSamplersKey? = null
    private var forgeModules: List<ForgeModule>? = null
    private var forgeModulesKey: StableDiffusionSamplersKey? = null
    private var aDetailerAvailable: Boolean? = null
    private var aDetailerAvailabilityKey: StableDiffusionSamplersKey? = null
    private var arliAiModels: List<String>? = null
    private var arliAiModelsKey: String? = null

    private val actionHandler = TextToImageActionHandler(
        dispatchersProvider = dispatchersProvider,
        textToImageUseCase = textToImageUseCase,
        saveGenerationResultUseCase = saveGenerationResultUseCase,
        saveLastResultToCacheUseCase = saveLastResultToCacheUseCase,
        interruptGenerationUseCase = interruptGenerationUseCase,
        preferenceManager = preferenceManager,
        backgroundTaskManager = backgroundTaskManager,
        backgroundWorkObserver = backgroundWorkObserver,
        wakeLockInterActor = wakeLockInterActor,
        platformServices = platformServices,
        buildInfoProvider = buildInfoProvider,
        dimensionValidator = dimensionValidator,
        localGenerationBenchmarkGateProvider = localGenerationBenchmarkGateProvider,
        imageSaver = imageSaver,
        imageSharer = imageSharer,
        router = router,
        currentState = { currentState },
        emitState = ::emitState,
        updateState = { reducer -> updateState(reducer) },
        launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
        onError = onError,
    )

    private val intentProcessor = TextToImageIntentProcessor(
        router = router,
        updateState = { reducer -> updateState(reducer) },
        generate = actionHandler::generate,
        cancelGeneration = actionHandler::cancelGeneration,
        saveImage = actionHandler::saveImage,
        shareImage = actionHandler::shareImage,
        saveGenerationResults = actionHandler::saveGenerationResults,
        viewGenerationResult = actionHandler::viewGenerationResult,
        reportGenerationResult = actionHandler::reportGenerationResult,
        applyGenerationResult = ::applyGenerationResult,
    )

    override fun processIntent(intent: TextToImageIntent) {
        when (intent) {
            TextToImageIntent.RefreshADetailerAvailability -> loadADetailerAvailability(force = true)
            TextToImageIntent.OpenADetailerInstallInstructions -> Unit
            TextToImageIntent.RunBenchmarkFromPrompt -> actionHandler.runBenchmarkFromPrompt()
            TextToImageIntent.SkipBenchmarkPrompt -> actionHandler.skipBenchmarkPrompt()
            TextToImageIntent.ContinueAfterBenchmarkWarning ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = false)
            TextToImageIntent.SuppressBenchmarkWarningAndContinue ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = true)
            TextToImageIntent.DismissModal -> actionHandler.dismissBenchmarkDialog()
            is TextToImageIntent.UpdateOpenAiCompatibleModel -> {
                preferenceManager.openAiCompatibleModel = intent.value
                intentProcessor.process(intent)
            }
            is TextToImageIntent.UpdateArliAiModel -> {
                preferenceManager.arliAiModel = intent.value
                intentProcessor.process(intent)
            }
            else -> intentProcessor.process(intent)
        }
        persistGenerationParameters()
    }

    /**
     * Restores image metadata while retaining diffusion controls when sampling metadata is absent.
     * A zero step count marks results from providers that do not expose diffusion parameters.
     *
     * @param ai Gallery image whose available parameters are applied to the form.
     * @author Dmitriy Moroz
     */
    private fun applyGenerationResult(ai: com.shifthackz.aisdv1.domain.entity.AiGenerationResult) {
        val hasDiffusionParameters = ai.samplingSteps > 0
        updateState { state ->
            state.copy(
                advancedOptionsVisible = true,
                prompt = ai.prompt,
                negativePrompt = if (hasDiffusionParameters) ai.negativePrompt else state.negativePrompt,
                width = ai.width.toString(),
                height = ai.height.toString(),
                seed = if (hasDiffusionParameters) ai.seed else state.seed,
                subSeed = if (hasDiffusionParameters) ai.subSeed else state.subSeed,
                subSeedStrength = if (hasDiffusionParameters) ai.subSeedStrength else state.subSeedStrength,
                samplingSteps = if (hasDiffusionParameters) ai.samplingSteps else state.samplingSteps,
                cfgScale = if (hasDiffusionParameters) ai.cfgScale else state.cfgScale,
                restoreFaces = if (hasDiffusionParameters) ai.restoreFaces else state.restoreFaces,
                selectedSampler = ai.sampler.takeIf(state.availableSamplers::contains)
                    ?: state.selectedSampler,
                error = null,
                message = null,
            )
        }
        persistGenerationParameters()
    }

    /**
     * Saves effective form values after edits, model clamping, or applying a history result.
     * Unchanged values do not trigger preference writes.
     *
     * @author Dmitriy Moroz
     */
    private fun persistGenerationParameters() {
        val state = currentState
        if (state.mode == ServerSource.OPEN_AI_COMPATIBLE && preferenceManager.openAiCompatibleModel != state.openAiCompatibleModel) {
            preferenceManager.openAiCompatibleModel = state.openAiCompatibleModel
        }
        if (preferenceManager.textToImageSamplingSteps != state.samplingSteps) {
            preferenceManager.textToImageSamplingSteps = state.samplingSteps
        }
        if (preferenceManager.textToImageCfgScale != state.cfgScale) {
            preferenceManager.textToImageCfgScale = state.cfgScale
        }
    }

    private fun observeGenerationFormUpdate() {
        launch(dispatchersProvider.immediate) {
            generationFormUpdateEvent.observeTxt2ImgForm().collect { payload ->
                if (payload is GenerationFormUpdateEvent.Payload.T2IForm) {
                    applyGenerationResult(payload.ai)
                }
            }
        }
    }

    private fun consumePendingGenerationFormUpdate() {
        generationFormUpdateEvent.consumeTxt2ImgForm()?.let { payload ->
            applyGenerationResult(payload.ai)
        }
    }

    private fun observeGenerationProgress() {
        launch(dispatchersProvider.immediate) {
            observeHordeProcessStatusUseCase().collect { status ->
                updateState { state ->
                    val modal = state.screenModal
                    if (modal is GenerationModal.Communicating) {
                        state.copy(screenModal = modal.copy(hordeProcessStatus = status))
                    } else {
                        state
                    }
                }
            }
        }
        launch(dispatchersProvider.immediate) {
            observeLocalDiffusionProcessStatusUseCase().collect { status ->
                updateLocalGenerationStatus(status)
            }
        }
        launch(dispatchersProvider.immediate) {
            observeStableDiffusionCppProcessStatusUseCase().collect { status ->
                updateLocalGenerationStatus(status)
            }
        }
        launch(dispatchersProvider.immediate) {
            observeCoreMlProcessStatusUseCase().collect { status ->
                updateLocalGenerationStatus(status)
            }
        }
        launch(dispatchersProvider.immediate) {
            observeBonsaiProcessStatusUseCase().collect { status ->
                updateLocalGenerationStatus(status)
            }
        }
    }

    private fun updateLocalGenerationStatus(status: LocalDiffusionStatus) {
        updateState { state ->
            val modal = state.screenModal
            if (modal is GenerationModal.Generating) {
                state.copy(screenModal = modal.copy(status = status))
            } else {
                state
            }
        }
    }

    private fun loadConfiguration() {
        updateState { it.copy(loadingConfiguration = true, error = null) }
        launch(dispatchersProvider.io) {
            runCatching { getConfigurationUseCase() }
                .onSuccess { configuration ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loadingConfiguration = false,
                                openAiCompatibleModel = configuration.openAiCompatibleModel,
                            ).withSource(
                                source = configuration.source,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loadingConfiguration = false,
                                error = t.localizedMessageText(),
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    private fun observeSettings() {
        launch(dispatchersProvider.immediate) {
            preferenceManager
                .observe()
                .catch { t ->
                    updateState { it.copy(error = t.localizedMessageText()) }
                    onError(t)
                }
                .collect { settings ->
                    refreshStableDiffusionMetadataIfNeeded(settings)
                    refreshArliAiModelsIfNeeded(settings)
                    updateState { state ->
                        state.withSettings(
                            settings = settings,
                            stableDiffusionSamplers = stableDiffusionSamplers,
                            forgeModules = forgeModules,
                            aDetailerAvailable = aDetailerAvailable,
                            arliAiModels = arliAiModels,
                        )
                    }
                }
        }
    }

    private fun refreshStableDiffusionMetadataIfNeeded(settings: Settings) {
        if (settings.source != ServerSource.AUTOMATIC1111) {
            stableDiffusionSamplersKey = null
            forgeModulesKey = null
            forgeModules = null
            aDetailerAvailabilityKey = null
            aDetailerAvailable = null
            return
        }
        val key = StableDiffusionSamplersKey(
            serverUrl = settings.serverUrl,
            demoMode = settings.demoMode,
        )
        if (
            stableDiffusionSamplersKey == key &&
            forgeModulesKey == key &&
            aDetailerAvailabilityKey == key
        ) return

        stableDiffusionSamplersKey = key
        forgeModulesKey = key
        aDetailerAvailabilityKey = key
        stableDiffusionSamplers = emptyList()
        forgeModules = emptyList()
        aDetailerAvailable = null
        updateState {
            it.withSource(
                source = ServerSource.AUTOMATIC1111,
                stableDiffusionSamplers = stableDiffusionSamplers,
                forgeModules = forgeModules,
                aDetailerAvailable = aDetailerAvailable,
                arliAiModels = arliAiModels,
            )
        }
        loadSamplers()
        loadForgeModules()
        loadADetailerAvailability()
    }

    private fun loadSamplers() {
        launch(dispatchersProvider.io) {
            runCatching {
                getStableDiffusionSamplersUseCase().map(StableDiffusionSampler::name)
            }
                .onSuccess { samplers ->
                    stableDiffusionSamplers = samplers
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.withSource(
                                source = it.mode,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(error = t.localizedMessageText()) }
                    }
                    onError(t)
                }
        }
    }

    private fun loadForgeModules() {
        launch(dispatchersProvider.io) {
            runCatching { getForgeModulesUseCase() }
                .onSuccess { modules ->
                    forgeModules = modules
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.withSource(
                                source = it.mode,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    forgeModules = emptyList()
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.withSource(
                                source = it.mode,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    private fun loadADetailerAvailability(force: Boolean = false) {
        if (!force && aDetailerAvailable != null) return
        updateState { it.copy(aDetailerRefreshing = true) }
        launch(dispatchersProvider.io) {
            runCatching { isADetailerAvailableUseCase() }
                .onSuccess { available ->
                    aDetailerAvailable = available
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                aDetailerAvailable = available,
                                aDetailerRefreshing = false,
                                error = null,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    aDetailerAvailable = false
                    withContext(dispatchersProvider.immediate) {
                        updateState { state ->
                            state.copy(
                                aDetailerAvailable = false,
                                aDetailerRefreshing = false,
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    private fun refreshArliAiModelsIfNeeded(settings: Settings) {
        if (settings.source != ServerSource.ARLI_AI) {
            arliAiModelsKey = null
            return
        }
        val key = preferenceManager.arliAiApiKey
        if (arliAiModelsKey == key) return

        arliAiModelsKey = key
        arliAiModels = emptyList()
        updateState {
            it.withSource(
                source = ServerSource.ARLI_AI,
                stableDiffusionSamplers = stableDiffusionSamplers,
                forgeModules = forgeModules,
                aDetailerAvailable = aDetailerAvailable,
                arliAiModels = arliAiModels,
            )
        }
        loadArliAiModels()
    }

    private fun loadArliAiModels() {
        launch(dispatchersProvider.io) {
            runCatching {
                fetchAndGetArliAiModelsUseCase()
                    .map(StableDiffusionModel::arliAiCheckpointName)
                    .filter(String::isNotBlank)
                    .distinct()
            }
                .onSuccess { models ->
                    arliAiModels = models
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(arliAiModel = preferenceManager.arliAiModel)
                                .withSource(
                                    source = it.mode,
                                    stableDiffusionSamplers = stableDiffusionSamplers,
                                    forgeModules = forgeModules,
                                    aDetailerAvailable = aDetailerAvailable,
                                    arliAiModels = arliAiModels,
                                )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(error = t.localizedMessageText()) }
                    }
                    onError(t)
                }
        }
    }
}

private val StableDiffusionModel.arliAiCheckpointName: String
    get() = title.ifBlank { modelName }.ifBlank { filename }

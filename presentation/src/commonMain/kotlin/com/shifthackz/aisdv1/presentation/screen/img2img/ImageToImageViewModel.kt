package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ForgeModule
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
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.feature.benchmark.LocalGenerationBenchmarkGate
import com.shifthackz.aisdv1.presentation.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.presentation.screen.txt2img.ImageSaver
import com.shifthackz.aisdv1.presentation.screen.txt2img.ImageSharer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

/**
 * View-model for the img2img screen.
 *
 * It keeps source-image state, shared generation controls, provider
 * configuration, benchmark gating, and platform image-picking actions
 * coordinated for the image-to-image flow.
 */
class ImageToImageViewModel(
    private val dispatchersProvider: DispatchersProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val getStableDiffusionSamplersUseCase: GetStableDiffusionSamplersUseCase,
    private val getForgeModulesUseCase: GetForgeModulesUseCase,
    private val fetchAndGetArliAiModelsUseCase: FetchAndGetArliAiModelsUseCase,
    private val isADetailerAvailableUseCase: IsADetailerAvailableUseCase,
    private val getRandomImageUseCase: GetRandomImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    private val interruptGenerationUseCase: InterruptGenerationUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
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
    private val router: ImageToImageRouter,
    private val platformActions: ImageToImagePlatformActions,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ImageToImageState, ImageToImageIntent, EmptyEffect>(
    initialState = ImageToImageState(
        samplingSteps = preferenceManager.imageToImageSamplingSteps,
        cfgScale = preferenceManager.imageToImageCfgScale,
        platform = buildInfoProvider.platform,
        bonsaiBackendSelectionVisible = buildInfoProvider.platform == Platform.ANDROID,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    private var stableDiffusionSamplers: List<String>? = null
    private var stableDiffusionSamplersKey: StableDiffusionSamplersKey? = null
    private var forgeModules: List<ForgeModule>? = null
    private var forgeModulesKey: StableDiffusionSamplersKey? = null
    private var aDetailerAvailable: Boolean? = null
    private var aDetailerAvailabilityKey: StableDiffusionSamplersKey? = null
    private var arliAiModels: List<String>? = null
    private var arliAiModelsKey: String? = null
    private var samplersLoadJob: Job? = null

    init {
        loadConfiguration()
        observeSettings()
        consumePendingGenerationFormUpdate()
        observeGenerationFormUpdate()
        observeGenerationProgress()
    }

    private val actionHandler = ImageToImageActionHandler(
        dispatchersProvider = dispatchersProvider,
        getRandomImageUseCase = getRandomImageUseCase,
        imageToImageUseCase = imageToImageUseCase,
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
        platformActions = platformActions,
        currentState = { currentState },
        emitState = ::emitState,
        updateState = { reducer -> updateState(reducer) },
        launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
        onError = onError,
    )

    private val intentProcessor = ImageToImageIntentProcessor(
        router = router,
        updateState = { reducer -> updateState(reducer) },
        pickImage = actionHandler::pickImage,
        pickRandomImage = actionHandler::pickRandomImage,
        generate = actionHandler::generate,
        cancelGeneration = actionHandler::cancelGeneration,
        saveImage = actionHandler::saveImage,
        shareImage = actionHandler::shareImage,
        saveGenerationResults = actionHandler::saveGenerationResults,
        viewGenerationResult = actionHandler::viewGenerationResult,
        reportGenerationResult = actionHandler::reportGenerationResult,
        applyGenerationResult = ::applyGenerationResult,
    )

    /**
     * Routes form actions and retries missing samplers without duplicating an active request.
     *
     * @param intent User action or foreground recovery action for this generation screen.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: ImageToImageIntent) {
        when (intent) {
            ImageToImageIntent.RetrySamplers -> {
                if (stableDiffusionSamplers.isNullOrEmpty()) loadSamplers()
            }
            ImageToImageIntent.RefreshADetailerAvailability -> loadADetailerAvailability(force = true)
            ImageToImageIntent.OpenADetailerInstallInstructions -> Unit
            ImageToImageIntent.RunBenchmarkFromPrompt -> actionHandler.runBenchmarkFromPrompt()
            ImageToImageIntent.SkipBenchmarkPrompt -> actionHandler.skipBenchmarkPrompt()
            ImageToImageIntent.ContinueAfterBenchmarkWarning ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = false)
            ImageToImageIntent.SuppressBenchmarkWarningAndContinue ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = true)
            ImageToImageIntent.DismissModal -> actionHandler.dismissBenchmarkDialog()
            is ImageToImageIntent.UpdateArliAiModel -> {
                preferenceManager.arliAiModel = intent.value
                intentProcessor.process(intent)
            }
            else -> intentProcessor.process(intent)
        }
        persistGenerationParameters()
    }

    private fun applyGenerationResult(
        ai: AiGenerationResult,
        inputImage: Boolean,
    ) {
        applyGenerationResult(
            ai = ai,
            imageBase64 = if (inputImage) {
                ai.inputImage.ifBlank { ai.image }
            } else {
                null
            },
        )
    }

    /**
     * Restores available image metadata without replacing diffusion controls with absent values.
     * A zero step count marks results from providers that do not expose diffusion parameters.
     *
     * @param ai Gallery image providing the prompt, dimensions and any diffusion parameters.
     * @param imageBase64 Optional image replacing the current img2img input.
     * @author Dmitriy Moroz
     */
    private fun applyGenerationResult(
        ai: AiGenerationResult,
        imageBase64: String?,
    ) {
        val hasDiffusionParameters = ai.samplingSteps > 0
        updateState { state ->
            state.copy(
                advancedOptionsVisible = true,
                imageBase64 = imageBase64 ?: state.imageBase64,
                inPaint = if (imageBase64 == null) state.inPaint else ImageInPaintState(),
                results = if (imageBase64 == null) state.results else emptyList(),
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
                denoisingStrength = if (hasDiffusionParameters) ai.denoisingStrength else state.denoisingStrength,
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
        if (preferenceManager.imageToImageSamplingSteps != state.samplingSteps) {
            preferenceManager.imageToImageSamplingSteps = state.samplingSteps
        }
        if (preferenceManager.imageToImageCfgScale != state.cfgScale) {
            preferenceManager.imageToImageCfgScale = state.cfgScale
        }
    }

    private fun observeGenerationFormUpdate() {
        launch(dispatchersProvider.immediate) {
            generationFormUpdateEvent.observeImg2ImgForm().collect { payload ->
                if (payload is GenerationFormUpdateEvent.Payload.I2IForm) {
                    applyGenerationResult(payload)
                }
            }
        }
    }

    private fun consumePendingGenerationFormUpdate() {
        generationFormUpdateEvent.consumeImg2ImgForm()?.let(::applyGenerationResult)
    }

    private fun applyGenerationResult(payload: GenerationFormUpdateEvent.Payload.I2IForm) {
        applyGenerationResult(
            ai = payload.ai,
            imageBase64 = if (payload.inputImage && payload.ai.inputImage.isNotBlank()) {
                payload.ai.inputImage
            } else {
                payload.ai.image
            },
        )
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
                updateState { state ->
                    val modal = state.screenModal
                    if (modal is GenerationModal.Generating) {
                        state.copy(screenModal = modal.copy(status = status))
                    } else {
                        state
                    }
                }
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
                                error = t.message ?: "Unable to load configuration",
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
                    updateState { it.copy(error = t.message ?: Localization.string("error_invalid")) }
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

    /**
     * Invalidates metadata on provider or server changes and cancels the previous sampler attempt.
     *
     * @param settings Current provider selection and server address used to identify metadata.
     * @author Dmitriy Moroz
     */
    private fun refreshStableDiffusionMetadataIfNeeded(settings: Settings) {
        if (settings.source != ServerSource.AUTOMATIC1111) {
            samplersLoadJob?.cancel()
            updateState { it.copy(samplersLoading = false, samplersLoadFailed = false) }
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
        samplersLoadJob?.cancel()
        forgeModulesKey = key
        aDetailerAvailabilityKey = key
        stableDiffusionSamplers = emptyList()
        forgeModules = emptyList()
        aDetailerAvailable = null
        updateState {
            it.copy(samplersLoading = false, samplersLoadFailed = false).withSource(
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

    /**
     * Loads an empty sampler list once per attempt. Failed and empty results remain retryable.
     * Server changes cancel the previous load; its result cannot update the new server form.
     *
     * @author Dmitriy Moroz
     */
    private fun loadSamplers() {
        val key = stableDiffusionSamplersKey ?: return
        if (currentState.samplersLoading) return
        updateState { it.copy(samplersLoading = true, samplersLoadFailed = false) }
        samplersLoadJob = launch(dispatchersProvider.immediate) {
            try {
                val samplers = withContext(dispatchersProvider.io) {
                    getStableDiffusionSamplersUseCase().map(StableDiffusionSampler::name)
                        .filter(String::isNotBlank)
                        .distinct()
                }
                ensureActive()
                if (stableDiffusionSamplersKey != key) return@launch
                stableDiffusionSamplers = samplers
                updateState {
                    it.copy(
                        samplersLoading = false,
                        samplersLoadFailed = samplers.isEmpty(),
                    ).withSource(
                        source = it.mode,
                        stableDiffusionSamplers = samplers,
                        forgeModules = forgeModules,
                        aDetailerAvailable = aDetailerAvailable,
                        arliAiModels = arliAiModels,
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (stableDiffusionSamplersKey != key) return@launch
                updateState { it.copy(samplersLoading = false, samplersLoadFailed = true) }
                onError(error)
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
                        updateState { it.copy(error = t.localizedMessageString()) }
                    }
                    onError(t)
                }
        }
    }
}

private val StableDiffusionModel.arliAiCheckpointName: String
    get() = title.ifBlank { modelName }.ifBlank { filename }

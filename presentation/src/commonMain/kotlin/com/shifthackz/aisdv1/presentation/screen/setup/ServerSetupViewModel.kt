package com.shifthackz.aisdv1.presentation.screen.setup

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure
import com.shifthackz.aisdv1.presentation.model.localizedDescription
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.validation.common.CommonStringValidator
import com.shifthackz.aisdv1.core.validation.path.FilePathValidator
import com.shifthackz.aisdv1.core.validation.url.UrlValidator
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToA1111UseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToArliAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToFalAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHordeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiCompatibleUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSwarmUiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.presentation.model.LaunchSource
import com.shifthackz.aisdv1.presentation.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.screen.setup.model.HORDE_DEFAULT_API_KEY
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupEffect
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.model.toServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.platform.ServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.screen.setup.reducer.ServerSetupIntentProcessor
import com.shifthackz.aisdv1.presentation.screen.setup.validation.validateServerSetup
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.HUGGING_FACE_MODELS_TIMEOUT_MILLIS
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.setupAllowedModes
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Owns provider setup state, validation, local model downloads, and final connection side effects.
 *
 * @param launchSource Screen that opened setup, used to decide back navigation and completion route.
 * @param dispatchersProvider App coroutine dispatchers used by setup work.
 * @param buildInfoProvider Build metadata used to filter platform-supported local providers.
 * @param getConfigurationUseCase Reads the current provider configuration.
 * @param getLocalOnnxModelsUseCase Reads locally configured ONNX models.
 * @param getLocalMediaPipeModelsUseCase Reads locally configured MediaPipe models.
 * @param getLocalSdxlModelsUseCase Reads locally configured SDXL models.
 * @param getLocalCoreMlModelsUseCase Reads locally configured Core ML models on supported builds.
 * @param fetchHuggingFaceModelsUseCase Fetches Hugging Face model list for remote setup.
 * @param urlValidator Validates provider URL input.
 * @param stringValidator Validates text settings entered by the user.
 * @param filePathValidator Validates local file path settings.
 * @param connectToA1111UseCase Applies Automatic1111 configuration.
 * @param connectToSwarmUiUseCase Applies SwarmUI configuration.
 * @param connectToHordeUseCase Applies Horde configuration.
 * @param connectToHuggingFaceUseCase Applies Hugging Face configuration.
 * @param connectToLocalDiffusionUseCase Applies ONNX local diffusion configuration.
 * @param connectToMediaPipeUseCase Applies MediaPipe local diffusion configuration.
 * @param connectToSdxlUseCase Applies SDXL local diffusion configuration.
 * @param connectToCoreMlUseCase Applies Core ML local diffusion configuration.
 * @param connectToOpenAiCompatibleUseCase Validates and applies independent image-server configuration.
 * @param connectToOpenAiUseCase Applies OpenAI configuration.
 * @param connectToStabilityAiUseCase Applies Stability AI configuration.
 * @param connectToFalAiUseCase Applies Fal AI configuration.
 * @param downloadModelUseCase Downloads a selected local model.
 * @param deleteModelUseCase Deletes a selected local model.
 * @param downloadGuard Platform guard for model download prerequisites.
 * @param storageUsageObserver Invalidates Settings storage summaries after model changes.
 * @param linksProvider External links and demo mode URL used by setup.
 * @param preferenceManager Persists setup preferences.
 * @param router Setup navigation contract.
 * @param onError Error callback forwarded to the app-level error handling pipeline.
 *
 * @author Dmitriy Moroz
 */
class ServerSetupViewModel(
    private val launchSource: LaunchSource = LaunchSource.SPLASH,
    private val dispatchersProvider: DispatchersProvider,
    private val buildInfoProvider: BuildInfoProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val getLocalOnnxModelsUseCase: GetLocalOnnxModelsUseCase,
    private val getLocalMediaPipeModelsUseCase: GetLocalMediaPipeModelsUseCase,
    private val getLocalSdxlModelsUseCase: GetLocalSdxlModelsUseCase,
    private val getLocalCoreMlModelsUseCase: GetLocalCoreMlModelsUseCase,
    private val getLocalBonsaiModelsUseCase: GetLocalBonsaiModelsUseCase,
    private val fetchHuggingFaceModelsUseCase: FetchHuggingFaceModelsUseCase,
    private val urlValidator: UrlValidator,
    private val stringValidator: CommonStringValidator,
    private val filePathValidator: FilePathValidator,
    private val connectToA1111UseCase: ConnectToA1111UseCase,
    private val connectToSwarmUiUseCase: ConnectToSwarmUiUseCase,
    private val connectToHordeUseCase: ConnectToHordeUseCase,
    private val connectToHuggingFaceUseCase: ConnectToHuggingFaceUseCase,
    private val connectToLocalDiffusionUseCase: ConnectToLocalDiffusionUseCase,
    private val connectToMediaPipeUseCase: ConnectToMediaPipeUseCase,
    private val connectToSdxlUseCase: ConnectToSdxlUseCase,
    private val connectToCoreMlUseCase: ConnectToCoreMlUseCase,
    private val connectToBonsaiUseCase: ConnectToBonsaiUseCase,
    private val connectToOpenAiCompatibleUseCase: ConnectToOpenAiCompatibleUseCase,
    private val connectToOpenAiUseCase: ConnectToOpenAiUseCase,
    private val connectToStabilityAiUseCase: ConnectToStabilityAiUseCase,
    private val connectToFalAiUseCase: ConnectToFalAiUseCase,
    private val connectToArliAiUseCase: ConnectToArliAiUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val deleteModelUseCase: DeleteModelUseCase,
    private val downloadGuard: ServerSetupDownloadGuard,
    private val storageUsageObserver: StorageUsageObserver,
    private val linksProvider: LinksProvider,
    private val preferenceManager: PreferenceManager,
    private val router: ServerSetupRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ServerSetupState, ServerSetupIntent, ServerSetupEffect>(
    initialState = ServerSetupState(
        showBackNavArrow = launchSource == LaunchSource.SETTINGS,
        platform = buildInfoProvider.platform,
        allowedModes = buildInfoProvider.setupAllowedModes(),
        demoModeUrl = linksProvider.demoModeUrl,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    private val downloadJobs: MutableMap<String, Job> = mutableMapOf()

    init {
        launch(dispatchersProvider.io) {
            runCatching {
                val configuration = getConfigurationUseCase()
                val allowedModes = buildInfoProvider.setupAllowedModes()
                val onnxModels = if (ServerSource.LOCAL_MICROSOFT_ONNX in allowedModes) {
                    getLocalOnnxModelsUseCase()
                } else {
                    emptyList()
                }
                val mediaPipeModels = if (ServerSource.LOCAL_GOOGLE_MEDIA_PIPE in allowedModes) {
                    getLocalMediaPipeModelsUseCase()
                } else {
                    emptyList()
                }
                val sdxlModels = if (ServerSource.LOCAL_STABLE_DIFFUSION_CPP in allowedModes) {
                    getLocalSdxlModelsUseCase()
                } else {
                    emptyList()
                }
                val coreMlModels = if (ServerSource.LOCAL_APPLE_CORE_ML in allowedModes) {
                    getLocalCoreMlModelsUseCase()
                } else {
                    emptyList()
                }
                val bonsaiModels = if (ServerSource.LOCAL_APPLE_BONSAI in allowedModes) {
                    getLocalBonsaiModelsUseCase()
                } else {
                    emptyList()
                }
                val models = runCatching {
                    withTimeout(HUGGING_FACE_MODELS_TIMEOUT_MILLIS.milliseconds) {
                        fetchHuggingFaceModelsUseCase()
                    }
                }
                    .onFailure(onError)
                    .getOrElse { HuggingFaceModel.supportedHfInferenceTextToImageModels }
                    .ifEmpty { HuggingFaceModel.supportedHfInferenceTextToImageModels }
                    .map(HuggingFaceModel::alias)
                configuration.toServerSetupState(
                    allowedModes = allowedModes,
                    platform = buildInfoProvider.platform,
                    huggingFaceModels = models,
                    localOnnxModels = onnxModels,
                    localMediaPipeModels = mediaPipeModels,
                    localSdxlModels = sdxlModels,
                    localCoreMlModels = coreMlModels,
                    localBonsaiModels = bonsaiModels,
                    allowLocalCustomModels = buildInfoProvider.type != BuildType.PLAY,
                    demoModeUrl = linksProvider.demoModeUrl,
                    showBackNavArrow = launchSource == LaunchSource.SETTINGS,
                )
            }
                .onSuccess { state ->
                    withContext(dispatchersProvider.immediate) {
                        emitState(state)
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { state ->
                            state.copy(
                                loadingConfiguration = false,
                                modal = ServerSetupState.Modal.Error(
                                    t.message ?: "Unable to load configuration",
                                ),
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    private val intentProcessor = ServerSetupIntentProcessor(
        router = router,
        linksProvider = linksProvider,
        currentState = { currentState },
        updateState = { reducer -> updateState(reducer) },
        emitEffect = ::emitEffect,
        localModelDownloadClickReducer = ::localModelDownloadClickReducer,
        deleteLocalModel = ::deleteLocalModel,
        download = ::download,
        validateAndConnectToServer = ::validateAndConnectToServer,
        connectToServer = ::connectToServer,
    )

    override fun processIntent(intent: ServerSetupIntent) {
        if (intent == ServerSetupIntent.AcknowledgeUnverifiedConnection) {
            if (currentState.modal == ServerSetupState.Modal.ConnectionUnverified) finishConnection()
        } else {
            intentProcessor.process(intent)
        }
    }

    private fun validateAndConnectToServer() {
        if (!validate()) return
        connectToServer()
    }

    private fun validate(): Boolean {
        val result = currentState.validateServerSetup(
            urlValidator = urlValidator,
            stringValidator = stringValidator,
            filePathValidator = filePathValidator,
        )
        emitState(result.state)
        return result.isValid
    }

    private fun connectToServer() {
        emitEffect(ServerSetupEffect.HideKeyboard)
        updateState { it.copy(modal = ServerSetupState.Modal.Communicating) }
        launch(dispatchersProvider.io) {
            var connectionUnverified = false
            val result = try {
                when (currentState.mode) {
                    ServerSource.AUTOMATIC1111 -> connectToAutomaticInstance()
                    ServerSource.SWARM_UI -> connectToSwarmUi()
                    ServerSource.HORDE -> connectToHorde()
                    ServerSource.HUGGING_FACE -> connectToHuggingFace()
                    ServerSource.OPEN_AI_COMPATIBLE -> connectToOpenAiCompatibleUseCase(
                        url = currentState.openAiCompatibleUrl,
                        apiKey = currentState.openAiCompatibleApiKey,
                    ).map { outcome -> connectionUnverified = outcome == OpenAiCompatibleConnection.UNVERIFIED }
                    ServerSource.OPEN_AI -> connectToOpenAi()
                    ServerSource.STABILITY_AI -> connectToStabilityAi()
                    ServerSource.FAL_AI -> connectToFalAi()
                    ServerSource.ARLI_AI -> connectToArliAi()
                    ServerSource.LOCAL_MICROSOFT_ONNX -> connectToLocalDiffusion()
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> connectToMediaPipe()
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> connectToSdxl()
                    ServerSource.LOCAL_APPLE_CORE_ML -> connectToCoreMl()
                    ServerSource.LOCAL_APPLE_BONSAI -> connectToBonsai()
                }
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.failure(t)
            }
            withContext(dispatchersProvider.immediate) {
                result.fold(
                    onSuccess = {
                        if (connectionUnverified) {
                            updateState { it.copy(modal = ServerSetupState.Modal.ConnectionUnverified) }
                        } else {
                            finishConnection()
                        }
                    },
                    onFailure = { t ->
                        updateState { state ->
                            state.copy(
                                modal = ServerSetupState.Modal.Error(
                                    (t as? OpenAiCompatibleHttpFailure)?.localizedDescription() ?: t.message ?: Localization.string("error_invalid"),
                                ),
                            )
                        }
                        onError(t)
                    },
                )
            }
        }
    }

    private fun finishConnection() {
        preferenceManager.forceSetupAfterUpdate = false
        updateState { it.copy(modal = ServerSetupState.Modal.None) }
        router.navigateToPostSetupConfigLoader()
    }

    private suspend fun connectToAutomaticInstance(): Result<Unit> {
        val isDemo = currentState.demoMode
        val url = if (isDemo) linksProvider.demoModeUrl else currentState.serverUrl
        return connectToA1111UseCase(
            url = url,
            isDemo = isDemo,
            credentials = if (isDemo) AuthorizationCredentials.None else currentState.credentialsDomain(),
        )
    }

    private suspend fun connectToSwarmUi(): Result<Unit> = connectToSwarmUiUseCase(
        url = currentState.swarmUiUrl,
        credentials = currentState.credentialsDomain(),
    )

    private suspend fun connectToHorde(): Result<Unit> = connectToHordeUseCase(
        apiKey = if (currentState.hordeDefaultApiKey) {
            HORDE_DEFAULT_API_KEY
        } else {
            currentState.hordeApiKey
        },
    )

    private suspend fun connectToHuggingFace(): Result<Unit> = connectToHuggingFaceUseCase(
        apiKey = currentState.huggingFaceApiKey,
        model = currentState.huggingFaceModel,
    )

    private suspend fun connectToOpenAi(): Result<Unit> = connectToOpenAiUseCase(
        apiKey = currentState.openAiApiKey,
    )

    private suspend fun connectToStabilityAi(): Result<Unit> = connectToStabilityAiUseCase(
        apiKey = currentState.stabilityAiApiKey,
    )

    private suspend fun connectToFalAi(): Result<Unit> = connectToFalAiUseCase(
        apiKey = currentState.falAiApiKey,
    )

    private suspend fun connectToArliAi(): Result<Unit> = connectToArliAiUseCase(
        apiKey = currentState.arliAiApiKey,
    )

    private suspend fun connectToLocalDiffusion(): Result<Unit> = connectToLocalDiffusionUseCase(
        modelId = currentState.localOnnxModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localOnnxCustomModelPath,
    )

    private suspend fun connectToMediaPipe(): Result<Unit> = connectToMediaPipeUseCase(
        modelId = currentState.localMediaPipeModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localMediaPipeCustomModelPath,
    )

    private suspend fun connectToSdxl(): Result<Unit> = connectToSdxlUseCase(
        modelId = currentState.localSdxlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localSdxlCustomModelPath,
    )

    private suspend fun connectToCoreMl(): Result<Unit> = connectToCoreMlUseCase(
        modelId = currentState.localCoreMlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localCoreMlCustomModelPath,
    )

    private suspend fun connectToBonsai(): Result<Unit> {
        return connectToBonsaiUseCase(
            modelId = currentState.localBonsaiModels.find { it.selected }?.id.orEmpty(),
            modelPath = "",
        )
    }

    private fun localModelDownloadClickReducer(value: ServerSetupState.LocalModel) {
        fun localModel(): ServerSetupState.LocalModel =
            currentState.localModels.firstOrNull { it.id == value.id }
                ?.let { value.copy(selected = it.selected) }
                ?: value

        when {
            localModel().downloadState is DownloadState.Downloading -> {
                downloadJobs.remove(localModel().id)?.cancel()
                deleteLocalModel(localModel().id)
                updateState { state ->
                    state.withUpdatedLocalModel(
                        value = localModel().copy(downloadState = DownloadState.Unknown),
                    )
                }
            }

            localModel().downloaded -> updateState {
                it.copy(modal = ServerSetupState.Modal.DeleteLocalModelConfirm(localModel()))
            }

            else -> updateState {
                it.copy(modal = ServerSetupState.Modal.SelectDownloadSource(localModel().id))
            }
        }
    }

    private fun download(modelId: String, url: String) {
        val localModel = currentState.localModels.firstOrNull { it.id == modelId } ?: return

        updateState { state ->
            state
                .copy(modal = ServerSetupState.Modal.None)
                .withUpdatedLocalModel(
                    localModel.copy(downloadState = DownloadState.Downloading()),
                )
        }

        val job = scopeLaunchDownload(localModel, url)
        downloadJobs[localModel.id] = job
        job.start()
    }

    private fun scopeLaunchDownload(
        localModel: ServerSetupState.LocalModel,
        url: String,
    ): Job =
        launch(dispatchersProvider.io, start = CoroutineStart.LAZY) {
            downloadGuard.withDownload {
                try {
                    downloadModelUseCase(localModel.id, url)
                        .distinctUntilChanged()
                        .catch { t ->
                            if (t is CancellationException) throw t
                            onDownloadFailure(localModel, t)
                        }
                        .collect { downloadState ->
                            if (downloadState is DownloadState.Complete) {
                                storageUsageObserver.notifyChanged()
                            }
                            updateState { state ->
                                state.withUpdatedLocalModel(
                                    localModel.copy(
                                        downloadState = downloadState,
                                        downloaded = downloadState is DownloadState.Complete,
                                    ),
                                )
                            }
                        }
                } finally {
                    downloadJobs.remove(localModel.id)
                }
            }
        }

    private fun onDownloadFailure(localModel: ServerSetupState.LocalModel, t: Throwable) {
        updateState { state ->
            state
                .withUpdatedLocalModel(
                    localModel.copy(downloadState = DownloadState.Error(t)),
                )
                .copy(
                    modal = ServerSetupState.Modal.Error(
                        t.message ?: Localization.string("error_title"),
                    ),
                )
        }
        onError(t)
    }

    private fun deleteLocalModel(id: String) {
        launch(dispatchersProvider.io) {
            runCatching { deleteModelUseCase(id) }
                .onSuccess { storageUsageObserver.notifyChanged() }
                .onFailure(onError)
        }
    }

}

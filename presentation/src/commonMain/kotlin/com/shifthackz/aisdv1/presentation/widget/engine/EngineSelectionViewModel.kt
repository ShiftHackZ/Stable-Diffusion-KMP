package com.shifthackz.aisdv1.presentation.widget.engine

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiEngine
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.entity.SwarmUiModel
import com.shifthackz.aisdv1.domain.feature.bonsai.BonsaiModelSupport
import com.shifthackz.aisdv1.domain.feature.coreml.CoreMlModelSupport
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Coordinates `EngineSelectionViewModel` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EngineSelectionViewModel(
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Exposes the `fetchAndGetSwarmUiModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val fetchAndGetSwarmUiModelsUseCase: FetchAndGetSwarmUiModelsUseCase,
    /**
     * Exposes the `observeLocalOnnxModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val observeLocalOnnxModelsUseCase: ObserveLocalOnnxModelsUseCase,
    /**
     * Exposes the `observeLocalCoreMlModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val observeLocalCoreMlModelsUseCase: ObserveLocalCoreMlModelsUseCase,
    /**
     * Exposes the `observeLocalBonsaiModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val observeLocalBonsaiModelsUseCase: ObserveLocalBonsaiModelsUseCase,
    /**
     * Exposes the `observeLocalSdxlModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val observeLocalSdxlModelsUseCase: ObserveLocalSdxlModelsUseCase,
    /**
     * Exposes the `fetchAndGetStabilityAiEnginesUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val fetchAndGetStabilityAiEnginesUseCase: FetchAndGetStabilityAiEnginesUseCase,
    /**
     * Exposes the `getHuggingFaceModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val getHuggingFaceModelsUseCase: FetchHuggingFaceModelsUseCase,
    /**
     * Exposes the `preferenceManager` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    /**
     * Exposes the `getConfigurationUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val getConfigurationUseCase: GetConfigurationUseCase,
    /**
     * Exposes the `selectStableDiffusionModelUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val selectStableDiffusionModelUseCase: SelectStableDiffusionModelUseCase,
    /**
     * Exposes the `getStableDiffusionModelsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val getStableDiffusionModelsUseCase: GetStableDiffusionModelsUseCase,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<EngineSelectionState, EngineSelectionIntent, EmptyEffect>(
    initialState = EngineSelectionState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        val configuration = preferenceManager
            .observe()
            .map { getConfigurationUseCase() }
            .catch {
                onError(it)
                emit(Configuration())
            }

        val localAiModels = observeLocalOnnxModelsUseCase()
            .map { models ->
                models.filter { it.downloaded || it.id == LocalAiModel.CustomOnnx.id }
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localSdxlModels = observeLocalSdxlModelsUseCase()
            .map { models ->
                models.filter(LocalAiModel::downloaded)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localCoreMlModels = observeLocalCoreMlModelsUseCase()
            .map { models ->
                models
                    .filter(LocalAiModel::downloaded)
                    .filter(CoreMlModelSupport::isSupported)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localBonsaiModels = observeLocalBonsaiModelsUseCase()
            .map { models ->
                models
                    .filter(LocalAiModel::downloaded)
                    .filter(BonsaiModelSupport::isSupported)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }

        launch(dispatchersProvider.io) {
            configuration
                .combine(localAiModels) { config, localModels -> config to localModels }
                .combine(localSdxlModels) { (config, localModels), sdxlModels ->
                    Triple(config, localModels, sdxlModels)
                }
                .combine(localCoreMlModels) { (config, localModels, sdxlModels), coreMlModels ->
                    LocalModelOptions(config, localModels, sdxlModels, coreMlModels)
                }
                .combine(localBonsaiModels) { localModelOptions, bonsaiModels ->
                    val config = localModelOptions.config
                    val visibleLocalModels = when (config.source) {
                        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localModelOptions.sdxlModels
                        ServerSource.LOCAL_APPLE_CORE_ML -> localModelOptions.coreMlModels
                        ServerSource.LOCAL_APPLE_BONSAI -> bonsaiModels
                        else -> localModelOptions.onnxModels
                    }
                    config to visibleLocalModels
                }
                .catch { onError(it) }
                .collectLatest { (config, localModels) ->
                    updateState {
                        it.copy(
                            loading = true,
                            mode = config.source,
                            localAiModels = localModels,
                        )
                    }
                    val options = loadOptions(config)
                    updateState { state ->
                        state.copy(
                            loading = false,
                            mode = options.config.source,
                            sdModels = options.sdModels.map { it.first.title },
                            selectedSdModel = options.sdModels
                                .firstOrNull { it.second }
                                ?.first
                                ?.title
                                ?: state.selectedSdModel,
                            swarmModels = options.swarmModels.map { it.name },
                            selectedSwarmModel = options.swarmModels
                                .firstOrNull { it.name == options.config.swarmUiModel }
                                ?.name
                                ?: state.selectedSwarmModel,
                            hfModels = options.hfModels.map { it.alias },
                            selectedHfModel = options.config.huggingFaceModel.safeHuggingFaceModelAlias(),
                            stEngines = options.stEngines.map { it.id },
                            selectedStEngine = options.config.stabilityAiEngineId,
                            localAiModels = localModels,
                            selectedLocalAiModelId = localModels
                                .firstOrNull { it.id == options.config.selectedLocalModelId() }
                                ?.id
                                ?: state.selectedLocalAiModelId,
                        )
                    }
                }
        }
    }

    private suspend fun loadOptions(config: Configuration): RemoteOptions {
        val remoteOptions = RemoteOptions(config = config)
        return when (config.source) {
            ServerSource.AUTOMATIC1111 -> remoteOptions.copy(
                sdModels = if (config.hasA1111Endpoint()) {
                    loadList { getStableDiffusionModelsUseCase() }
                } else {
                    emptyList()
                },
            )

            ServerSource.SWARM_UI -> remoteOptions.copy(
                swarmModels = if (config.hasSwarmEndpoint()) {
                    loadList { fetchAndGetSwarmUiModelsUseCase() }
                } else {
                    emptyList()
                },
            )

            ServerSource.HUGGING_FACE -> remoteOptions.copy(
                hfModels = loadList { getHuggingFaceModelsUseCase() }
                    .ifEmpty { HuggingFaceModel.supportedHfInferenceTextToImageModels },
            )

            ServerSource.STABILITY_AI -> remoteOptions.copy(
                stEngines = if (config.stabilityAiApiKey.isBlank()) {
                    emptyList()
                } else {
                    loadList { fetchAndGetStabilityAiEnginesUseCase() }
                },
            )

            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI,
            ServerSource.HORDE,
            ServerSource.OPEN_AI,
        ServerSource.OPEN_AI_COMPATIBLE,
        ServerSource.OPEN_AI_OAUTH,
            ServerSource.FAL_AI,
            ServerSource.ARLI_AI,
            -> remoteOptions
        }
    }

    override fun processIntent(intent: EngineSelectionIntent) {
        when (currentState.mode) {
            ServerSource.AUTOMATIC1111 -> selectA1111Model(intent.value)
            ServerSource.SWARM_UI -> preferenceManager.swarmUiModel = intent.value
            ServerSource.HUGGING_FACE -> preferenceManager.huggingFaceModel =
                intent.value.safeHuggingFaceModelAlias()
            ServerSource.STABILITY_AI -> preferenceManager.stabilityAiEngineId = intent.value
            ServerSource.LOCAL_MICROSOFT_ONNX -> preferenceManager.localOnnxModelId = intent.value
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> preferenceManager.localSdxlModelId = intent.value
            ServerSource.LOCAL_APPLE_CORE_ML -> preferenceManager.localCoreMlModelId = intent.value
            ServerSource.LOCAL_APPLE_BONSAI -> preferenceManager.localBonsaiModelId = intent.value
            else -> Unit
        }
    }

    private fun selectA1111Model(value: String) {
        updateState {
            it.copy(
                loading = true,
                selectedSdModel = value,
            )
        }

        launch(dispatchersProvider.io) {
            runCatching {
                selectStableDiffusionModelUseCase(value)
                getStableDiffusionModelsUseCase()
            }
                .onFailure { throwable ->
                    onError(throwable)
                    updateState { it.copy(loading = false) }
                }
                .onSuccess { sdModels ->
                    updateState { state ->
                        state.copy(
                            loading = false,
                            sdModels = sdModels.map { it.first.title },
                            selectedSdModel = sdModels
                                .firstOrNull { it.second }
                                ?.first
                                ?.title
                                ?: state.selectedSdModel,
                        )
                    }
                }
        }
    }

    private suspend fun <T> loadList(block: suspend () -> List<T>): List<T> = runCatching {
        withTimeoutOrNull(REMOTE_OPTIONS_TIMEOUT_MILLIS.milliseconds) {
            block()
        } ?: emptyList()
    }
        .onFailure(onError)
        .getOrElse { emptyList() }
}

/**
 * Exposes the `REMOTE_OPTIONS_TIMEOUT_MILLIS` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val REMOTE_OPTIONS_TIMEOUT_MILLIS = 5_000L

/**
 * Executes the `selectedLocalModelId` step in the SDAI presentation layer.
 *
 * @return Result produced by `selectedLocalModelId`.
 * @author Dmitriy Moroz
 */
private fun Configuration.selectedLocalModelId(): String = when (source) {
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlModelId
    ServerSource.LOCAL_APPLE_CORE_ML -> localCoreMlModelId
    ServerSource.LOCAL_APPLE_BONSAI -> localBonsaiModelId
    else -> localOnnxModelId
}

private data class LocalModelOptions(
    val config: Configuration,
    val onnxModels: List<LocalAiModel>,
    val sdxlModels: List<LocalAiModel>,
    val coreMlModels: List<LocalAiModel>,
)

/**
 * Executes the `hasA1111Endpoint` step in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.hasA1111Endpoint(): Boolean = serverUrl.isMobileRemoteEndpoint()

/**
 * Executes the `hasSwarmEndpoint` step in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.hasSwarmEndpoint(): Boolean = swarmUiUrl.isMobileRemoteEndpoint()

/**
 * Executes the `isMobileRemoteEndpoint` step in the SDAI presentation layer.
 *
 * @return Result produced by `isMobileRemoteEndpoint`.
 * @author Dmitriy Moroz
 */
private fun String.isMobileRemoteEndpoint(): Boolean {
    val value = trim().lowercase()
    return value.isNotBlank() &&
        !value.startsWith("http://localhost") &&
        !value.startsWith("https://localhost") &&
        !value.startsWith("http://127.0.0.1") &&
        !value.startsWith("https://127.0.0.1") &&
        !value.startsWith("http://[::1]") &&
        !value.startsWith("https://[::1]")
}

/**
 * Executes the `safeHuggingFaceModelAlias` step in the SDAI presentation layer.
 *
 * @return Result produced by `safeHuggingFaceModelAlias`.
 * @author Dmitriy Moroz
 */
private fun String.safeHuggingFaceModelAlias(): String =
    takeIf(HuggingFaceModel.supportedHfInferenceTextToImageAliases::contains)
        ?: HuggingFaceModel.default.alias

/**
 * Carries `RemoteOptions` data through the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private data class RemoteOptions(
    /**
     * Exposes the `config` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val config: Configuration,
    /**
     * Exposes the `sdModels` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val sdModels: List<Pair<StableDiffusionModel, Boolean>> = emptyList(),
    /**
     * Exposes the `swarmModels` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val swarmModels: List<SwarmUiModel> = emptyList(),
    /**
     * Exposes the `hfModels` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val hfModels: List<HuggingFaceModel> = emptyList(),
    /**
     * Exposes the `stEngines` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val stEngines: List<StabilityAiEngine> = emptyList(),
)

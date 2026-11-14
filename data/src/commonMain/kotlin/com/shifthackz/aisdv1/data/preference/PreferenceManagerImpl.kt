package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.core.common.extensions.fixUrlSlashes
import com.shifthackz.aisdv1.core.common.file.LOCAL_DIFFUSION_CUSTOM_PATH
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implements `PreferenceManager` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class PreferenceManagerImpl(
    /**
     * Exposes the `keyValueStore` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val keyValueStore: KeyValueStore,
) : PreferenceManager {

    /**
     * Exposes the `preferencesChangedState` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferencesChangedState = MutableStateFlow(Any())

    /**
     * Retains txt2img sampling steps across process restarts without broadcasting form edits.
     *
     * @author Dmitriy Moroz
     */
    override var textToImageSamplingSteps: Int
        get() = keyValueStore.getInt(KEY_TEXT_TO_IMAGE_SAMPLING_STEPS, 20).takeIf { it > 0 } ?: 20
        set(value) = keyValueStore.putInt(KEY_TEXT_TO_IMAGE_SAMPLING_STEPS, value)

    /**
     * Stores txt2img CFG as a decimal string supported by both platform preference stores.
     *
     * @author Dmitriy Moroz
     */
    override var textToImageCfgScale: Float
        get() = readCfgScale(KEY_TEXT_TO_IMAGE_CFG_SCALE)
        set(value) = keyValueStore.putString(KEY_TEXT_TO_IMAGE_CFG_SCALE, value.toString())

    /**
     * Retains img2img sampling steps separately from the txt2img form.
     *
     * @author Dmitriy Moroz
     */
    override var imageToImageSamplingSteps: Int
        get() = keyValueStore.getInt(KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS, 20).takeIf { it > 0 } ?: 20
        set(value) = keyValueStore.putInt(KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS, value)

    /**
     * Stores img2img CFG as a decimal string without losing fractional precision.
     *
     * @author Dmitriy Moroz
     */
    override var imageToImageCfgScale: Float
        get() = readCfgScale(KEY_IMAGE_TO_IMAGE_CFG_SCALE)
        set(value) = keyValueStore.putString(KEY_IMAGE_TO_IMAGE_CFG_SCALE, value.toString())

    /**
     * Persists a separate connection limit and notifies settings observers.
     *
     * @author Dmitriy Moroz
     */
    override var connectionTimeout: NetworkTimeout
        get() = NetworkTimeout.fromSeconds(
            keyValueStore.getInt(KEY_CONNECTION_TIMEOUT_SECONDS, NetworkTimeout.TWO_MINUTES.seconds),
        )
        set(value) = putInt(KEY_CONNECTION_TIMEOUT_SECONDS, value.seconds)

    /**
     * Persists the generation waiting limit and notifies Settings observers immediately.
     *
     * @author Dmitriy Moroz
     */
    override var generationRequestTimeout: NetworkTimeout
        get() = NetworkTimeout.fromSeconds(
            keyValueStore.getInt(KEY_GENERATION_REQUEST_TIMEOUT_SECONDS, 120),
        )
        set(value) = putInt(KEY_GENERATION_REQUEST_TIMEOUT_SECONDS, value.seconds)

    /**
     * Exposes the `automatic1111ServerUrl` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var automatic1111ServerUrl: String
        get() = keyValueStore.getString(KEY_SERVER_URL).fixUrlSlashes()
        set(value) = putString(KEY_SERVER_URL, value.fixUrlSlashes())

    /**
     * Exposes the `swarmUiServerUrl` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiServerUrl: String
        get() = keyValueStore.getString(KEY_SWARM_SERVER_URL).fixUrlSlashes()
        set(value) = putString(KEY_SWARM_SERVER_URL, value.fixUrlSlashes())

    /**
     * Exposes the `swarmUiModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiModel: String
        get() = keyValueStore.getString(KEY_SWARM_MODEL)
        set(value) = putString(KEY_SWARM_MODEL, value)

    /**
     * Exposes the `demoMode` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var demoMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEMO_MODE)
        set(value) = putBoolean(KEY_DEMO_MODE, value)

    /**
     * Exposes the `developerMode` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var developerMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEVELOPER_MODE)
        set(value) = putBoolean(KEY_DEVELOPER_MODE, value)

    /**
     * Exposes the `localMediaPipeCustomModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeCustomModelPath: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localCoreMlCustomModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlCustomModelPath: String
        get() = keyValueStore.getString(KEY_CORE_ML_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_CORE_ML_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localBonsaiCustomModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiCustomModelPath: String
        get() = keyValueStore.getString(KEY_BONSAI_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_BONSAI_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localOnnxCustomModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxCustomModelPath: String
        get() = keyValueStore.getString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localSdxlCustomModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlCustomModelPath: String
        get() = keyValueStore.getString(KEY_SDXL_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_SDXL_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localOnnxAllowCancel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxAllowCancel: Boolean
        get() = keyValueStore.getBoolean(KEY_ALLOW_LOCAL_DIFFUSION_CANCEL)
        set(value) = putBoolean(KEY_ALLOW_LOCAL_DIFFUSION_CANCEL, value)

    /**
     * Exposes the `localOnnxSchedulerThread` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxSchedulerThread: SchedulersToken
        get() = enumValueOrDefault(
            index = keyValueStore.getInt(KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD, SchedulersToken.COMPUTATION.ordinal),
            default = SchedulersToken.COMPUTATION,
        )
        set(value) = putInt(KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD, value.ordinal)

    /**
     * Exposes the `monitorConnectivity` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var monitorConnectivity: Boolean
        get() {
            val value = keyValueStore.getBoolean(KEY_MONITOR_CONNECTIVITY)
            return if (!source.featureTags.contains(FeatureTag.OwnServer)) false else value
        }
        set(value) = putBoolean(KEY_MONITOR_CONNECTIVITY, value)

    /**
     * Exposes the `autoSaveAiResults` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var autoSaveAiResults: Boolean
        get() = keyValueStore.getBoolean(KEY_AI_AUTO_SAVE, true)
        set(value) = putBoolean(KEY_AI_AUTO_SAVE, value)

    /**
     * Exposes the `saveToMediaStore` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var saveToMediaStore: Boolean
        get() = keyValueStore.getBoolean(KEY_SAVE_TO_MEDIA_STORE, defaultSaveToMediaStore())
        set(value) = putBoolean(KEY_SAVE_TO_MEDIA_STORE, value)

    /**
     * Exposes the `formAdvancedOptionsAlwaysShow` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var formAdvancedOptionsAlwaysShow: Boolean
        get() = keyValueStore.getBoolean(KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS)
        set(value) = putBoolean(KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS, value)

    /**
     * Exposes the `formPromptTaggedInput` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var formPromptTaggedInput: Boolean
        get() = keyValueStore.getBoolean(KEY_FORM_PROMPT_TAGGED_INPUT)
        set(value) = putBoolean(KEY_FORM_PROMPT_TAGGED_INPUT, value)

    /**
     * Exposes the `source` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var source: ServerSource
        get() = ServerSource.parse(keyValueStore.getString(KEY_SERVER_SOURCE, ServerSource.AUTOMATIC1111.key))
        set(value) = putString(KEY_SERVER_SOURCE, value.key)

    /**
     * Exposes the `sdModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var sdModel: String
        get() = keyValueStore.getString(KEY_SD_MODEL)
        set(value) = putString(KEY_SD_MODEL, value)

    /**
     * Exposes the `hordeApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var hordeApiKey: String
        get() = keyValueStore.getString(KEY_HORDE_API_KEY)
        set(value) = putString(KEY_HORDE_API_KEY, value)

    /**
     * Exposes the `openAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var openAiApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_API_KEY)
        set(value) = putString(KEY_OPEN_AI_API_KEY, value)

    /**
     * Exposes the `huggingFaceApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceApiKey: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_API_KEY)
        set(value) = putString(KEY_HUGGING_FACE_API_KEY, value)

    /**
     * Exposes the `huggingFaceModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceModel: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_MODEL_KEY, HuggingFaceModel.default.alias)
        set(value) = putString(KEY_HUGGING_FACE_MODEL_KEY, value)

    /**
     * Exposes the `stabilityAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiApiKey: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_API_KEY)
        set(value) = putString(KEY_STABILITY_AI_API_KEY, value)

    /**
     * Exposes the `falAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var falAiApiKey: String
        get() = keyValueStore.getString(KEY_FAL_AI_API_KEY)
        set(value) = putString(KEY_FAL_AI_API_KEY, value)

    /**
     * Exposes the `arliAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiApiKey: String
        get() = keyValueStore.getString(KEY_ARLI_AI_API_KEY)
        set(value) = putString(KEY_ARLI_AI_API_KEY, value)

    /**
     * Exposes the `arliAiModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiModel: String
        get() = keyValueStore.getString(KEY_ARLI_AI_MODEL_KEY)
        set(value) = putString(KEY_ARLI_AI_MODEL_KEY, value)

    /**
     * Exposes the `stabilityAiEngineId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiEngineId: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_ENGINE_ID_KEY)
        set(value) = putString(KEY_STABILITY_AI_ENGINE_ID_KEY, value)

    /**
     * Exposes the `onBoardingComplete` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var onBoardingComplete: Boolean
        get() = keyValueStore.getBoolean(KEY_ON_BOARDING_COMPLETE)
        set(value) = keyValueStore.putBoolean(KEY_ON_BOARDING_COMPLETE, value)

    /**
     * Exposes the `forceSetupAfterUpdate` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var forceSetupAfterUpdate: Boolean
        get() = keyValueStore.getBoolean(KEY_FORCE_SETUP_AFTER_UPDATE, true)
        set(value) = putBoolean(KEY_FORCE_SETUP_AFTER_UPDATE, value)

    /**
     * Exposes the `localOnnxModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelId: String
        get() = keyValueStore.getString(KEY_LOCAL_MODEL_ID)
        set(value) = putString(KEY_LOCAL_MODEL_ID, value)

    /**
     * Exposes the `localOnnxUseNNAPI` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxUseNNAPI: Boolean
        get() = keyValueStore.getBoolean(KEY_LOCAL_NN_API)
        set(value) = putBoolean(KEY_LOCAL_NN_API, value)

    /**
     * Exposes the `localMediaPipeModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelId: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_MODEL_ID)
        set(value) = putString(KEY_MEDIA_PIPE_MODEL_ID, value)

    /**
     * Exposes the `localSdxlModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelId: String
        get() = keyValueStore.getString(KEY_SDXL_MODEL_ID)
        set(value) = putString(KEY_SDXL_MODEL_ID, value)

    /**
     * Exposes the `localCoreMlModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelId: String
        get() = keyValueStore.getString(KEY_CORE_ML_MODEL_ID)
        set(value) = putString(KEY_CORE_ML_MODEL_ID, value)

    /**
     * Exposes the `localBonsaiModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelId: String
        get() = keyValueStore.getString(KEY_BONSAI_MODEL_ID)
        set(value) = putString(KEY_BONSAI_MODEL_ID, value)

    /**
     * Exposes the `designUseSystemColorPalette` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var designUseSystemColorPalette: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_DYNAMIC_COLORS)
        set(value) = putBoolean(KEY_DESIGN_DYNAMIC_COLORS, value)

    /**
     * Exposes the `designUseSystemDarkTheme` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var designUseSystemDarkTheme: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_SYSTEM_DARK_THEME, true)
        set(value) = putBoolean(KEY_DESIGN_SYSTEM_DARK_THEME, value)

    /**
     * Exposes the `designDarkTheme` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var designDarkTheme: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_DARK_THEME, true)
        set(value) = putBoolean(KEY_DESIGN_DARK_THEME, value)

    /**
     * Exposes the `designColorToken` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var designColorToken: String
        get() = keyValueStore.getString(KEY_DESIGN_COLOR_TOKEN, "${ColorToken.MAUVE}")
        set(value) = putString(KEY_DESIGN_COLOR_TOKEN, value)

    /**
     * Exposes the `designDarkThemeToken` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var designDarkThemeToken: String
        get() = keyValueStore.getString(KEY_DESIGN_DARK_TOKEN, "${DarkThemeToken.FRAPPE}")
        set(value) = putString(KEY_DESIGN_DARK_TOKEN, value)

    /**
     * Exposes the `backgroundGeneration` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var backgroundGeneration: Boolean
        get() = keyValueStore.getBoolean(KEY_BACKGROUND_GENERATION)
        set(value) = putBoolean(KEY_BACKGROUND_GENERATION, value)

    /**
     * Exposes the `backgroundProcessCount` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var backgroundProcessCount: Int
        get() = keyValueStore.getInt(KEY_BACKGROUND_PROCESS_COUNT)
        set(value) = keyValueStore.putInt(KEY_BACKGROUND_PROCESS_COUNT, value)

    /**
     * Exposes the `localGenerationBenchmarkPromptAnswered` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localGenerationBenchmarkPromptAnswered: Boolean
        get() = keyValueStore.getBoolean(KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED)
        set(value) = putBoolean(KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED, value)

    /**
     * Exposes the `benchmarkRecommendationWarningSuppressed` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var benchmarkRecommendationWarningSuppressed: Boolean
        get() = keyValueStore.getBoolean(KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED)
        set(value) = putBoolean(KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED, value)

    /**
     * Exposes the `galleryGrid` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var galleryGrid: Grid
        get() = enumValueOrDefault(
            index = keyValueStore.getInt(KEY_GALLERY_GRID, Grid.entries.first().ordinal),
            default = Grid.entries.first(),
        )
        set(value) = putInt(KEY_GALLERY_GRID, value.ordinal)

    /**
     * Exposes the `languageCode` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var languageCode: String
        get() = keyValueStore.getString(KEY_LANGUAGE_CODE)
        set(value) = putString(KEY_LANGUAGE_CODE, value)

    /**
     * Loads SDAI data through `observe`.
     *
     * @return Result produced by `observe`.
     * @author Dmitriy Moroz
     */
    override fun observe(): Flow<Settings> = preferencesChangedState
        .map {
            Settings(
                connectionTimeout = connectionTimeout,
                generationRequestTimeout = generationRequestTimeout,
                serverUrl = automatic1111ServerUrl,
                sdModel = sdModel,
                demoMode = demoMode,
                developerMode = developerMode,
                localDiffusionAllowCancel = localOnnxAllowCancel,
                localDiffusionSchedulerThread = localOnnxSchedulerThread,
                monitorConnectivity = monitorConnectivity,
                backgroundGeneration = backgroundGeneration,
                autoSaveAiResults = autoSaveAiResults,
                saveToMediaStore = saveToMediaStore,
                formAdvancedOptionsAlwaysShow = formAdvancedOptionsAlwaysShow,
                formPromptTaggedInput = formPromptTaggedInput,
                source = source,
                hordeApiKey = hordeApiKey,
                arliAiModel = arliAiModel,
                localUseNNAPI = localOnnxUseNNAPI,
                designUseSystemColorPalette = designUseSystemColorPalette,
                designUseSystemDarkTheme = designUseSystemDarkTheme,
                designDarkTheme = designDarkTheme,
                designColorToken = designColorToken,
                designDarkThemeToken = designDarkThemeToken,
                galleryGrid = galleryGrid,
                languageCode = languageCode,
            )
        }

    /**
     * Executes the `refresh` step in the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun refresh() {
        preferencesChangedState.value = Any()
    }

    /**
     * Reads a stored CFG value, retaining the default for absent or invalid data.
     *
     * @param key Preference key for the generation form's decimal CFG value.
     * @author Dmitriy Moroz
     */
    private fun readCfgScale(key: String): Float = keyValueStore.getString(key)
        .toFloatOrNull()
        ?.takeIf { it.isFinite() && it >= 0f }
        ?: 7f

    /**
     * Executes the `putString` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    private fun putString(key: String, value: String) {
        keyValueStore.putString(key, value)
        onPreferencesChanged()
    }

    /**
     * Executes the `putBoolean` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    private fun putBoolean(key: String, value: Boolean) {
        keyValueStore.putBoolean(key, value)
        onPreferencesChanged()
    }

    /**
     * Executes the `putInt` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    private fun putInt(key: String, value: Int) {
        keyValueStore.putInt(key, value)
        onPreferencesChanged()
    }

    /**
     * Executes the `onPreferencesChanged` step in the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private fun onPreferencesChanged() {
        preferencesChangedState.value = Any()
    }

    /**
     * Executes the `function` step in the SDAI data layer.
     *
     * @param index index value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `function`.
     * @author Dmitriy Moroz
     */
    private inline fun <reified T : Enum<T>> enumValueOrDefault(index: Int, default: T): T =
        enumValues<T>().getOrNull(index) ?: default

    /**
     * Provides the `companion object` singleton used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    companion object {
        /**
         * Txt2img sampling step preference.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_TEXT_TO_IMAGE_SAMPLING_STEPS = "key_text_to_image_sampling_steps"

        /**
         * Txt2img CFG preference, encoded as a decimal string.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_TEXT_TO_IMAGE_CFG_SCALE = "key_text_to_image_cfg_scale"

        /**
         * Img2img sampling step preference.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS = "key_image_to_image_sampling_steps"

        /**
         * Img2img CFG preference, encoded as a decimal string.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_IMAGE_TO_IMAGE_CFG_SCALE = "key_image_to_image_cfg_scale"

        /**
         * Independent provider connection timeout in seconds, with zero meaning no limit.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CONNECTION_TIMEOUT_SECONDS = "key_connection_timeout_seconds"

        /**
         * Stores seconds, with zero representing an unlimited generation wait.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_GENERATION_REQUEST_TIMEOUT_SECONDS = "key_generation_request_timeout_seconds"

        /**
         * Exposes the `KEY_SERVER_URL` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SERVER_URL = "key_server_url"
        /**
         * Exposes the `KEY_SWARM_SERVER_URL` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SWARM_SERVER_URL = "key_swarm_server_url"
        /**
         * Exposes the `KEY_SWARM_MODEL` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SWARM_MODEL = "key_swarm_model"
        /**
         * Exposes the `KEY_DEMO_MODE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DEMO_MODE = "key_demo_mode"
        /**
         * Exposes the `KEY_DEVELOPER_MODE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DEVELOPER_MODE = "key_developer_mode"
        /**
         * Exposes the `KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH = "key_local_diffusion_custom_model_path"
        /**
         * Exposes the `KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH = "key_mediapipe_custom_model_path"
        /**
         * Exposes the `KEY_CORE_ML_CUSTOM_MODEL_PATH` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CORE_ML_CUSTOM_MODEL_PATH = "key_core_ml_custom_model_path"
        /**
         * Exposes the `KEY_BONSAI_CUSTOM_MODEL_PATH` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BONSAI_CUSTOM_MODEL_PATH = "key_bonsai_custom_model_path"
        /**
         * Exposes the `KEY_SDXL_CUSTOM_MODEL_PATH` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SDXL_CUSTOM_MODEL_PATH = "key_sdxl_custom_model_path"
        /**
         * Exposes the `KEY_ALLOW_LOCAL_DIFFUSION_CANCEL` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ALLOW_LOCAL_DIFFUSION_CANCEL = "key_allow_local_diffusion_cancel"
        /**
         * Exposes the `KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD = "key_local_diffusion_scheduler_thread"
        /**
         * Exposes the `KEY_MONITOR_CONNECTIVITY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MONITOR_CONNECTIVITY = "key_monitor_connection"
        /**
         * Exposes the `KEY_AI_AUTO_SAVE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_AI_AUTO_SAVE = "key_ai_auto_save"
        /**
         * Exposes the `KEY_SAVE_TO_MEDIA_STORE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SAVE_TO_MEDIA_STORE = "key_save_to_media_store"
        /**
         * Exposes the `KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS = "key_always_show_advanced_options"
        /**
         * Exposes the `KEY_FORM_PROMPT_TAGGED_INPUT` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORM_PROMPT_TAGGED_INPUT = "key_prompt_tagged_input_kb"
        /**
         * Exposes the `KEY_SERVER_SOURCE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SERVER_SOURCE = "key_server_source"
        /**
         * Exposes the `KEY_SD_MODEL` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SD_MODEL = "key_sd_model"
        /**
         * Exposes the `KEY_HORDE_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HORDE_API_KEY = "key_horde_api_key"
        /**
         * Exposes the `KEY_OPEN_AI_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_OPEN_AI_API_KEY = "key_open_ai_api_key"
        /**
         * Exposes the `KEY_HUGGING_FACE_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HUGGING_FACE_API_KEY = "key_hugging_face_api_key"
        /**
         * Exposes the `KEY_HUGGING_FACE_MODEL_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HUGGING_FACE_MODEL_KEY = "key_hugging_face_model_key"
        /**
         * Exposes the `KEY_STABILITY_AI_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_API_KEY = "key_stability_ai_api_key"
        /**
         * Exposes the `KEY_FAL_AI_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FAL_AI_API_KEY = "key_fal_ai_api_key"
        /**
         * Exposes the `KEY_ARLI_AI_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ARLI_AI_API_KEY = "key_arli_ai_api_key"
        /**
         * Exposes the `KEY_ARLI_AI_MODEL_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ARLI_AI_MODEL_KEY = "key_arli_ai_model_key"
        /**
         * Exposes the `KEY_STABILITY_AI_ENGINE_ID_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_ENGINE_ID_KEY = "key_stability_ai_engine_id_key"
        /**
         * Exposes the `KEY_ON_BOARDING_COMPLETE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ON_BOARDING_COMPLETE = "key_on_boarding_complete"
        /**
         * Exposes the `KEY_FORCE_SETUP_AFTER_UPDATE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORCE_SETUP_AFTER_UPDATE = "force_upd_setup_v0.x.x-v0.6.2"
        /**
         * Exposes the `KEY_MEDIA_PIPE_MODEL_ID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MEDIA_PIPE_MODEL_ID = "key_mediapipe_model_id"
        /**
         * Exposes the `KEY_CORE_ML_MODEL_ID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CORE_ML_MODEL_ID = "key_core_ml_model_id"
        /**
         * Exposes the `KEY_BONSAI_MODEL_ID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BONSAI_MODEL_ID = "key_bonsai_model_id"
        /**
         * Exposes the `KEY_SDXL_MODEL_ID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SDXL_MODEL_ID = "key_sdxl_model_id"
        /**
         * Exposes the `KEY_LOCAL_MODEL_ID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_MODEL_ID = "key_local_model_id"
        /**
         * Exposes the `KEY_LOCAL_NN_API` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_NN_API = "key_local_nn_api"
        /**
         * Exposes the `KEY_DESIGN_DYNAMIC_COLORS` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DYNAMIC_COLORS = "key_design_dynamic_colors"
        /**
         * Exposes the `KEY_DESIGN_SYSTEM_DARK_THEME` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_SYSTEM_DARK_THEME = "key_design_system_dark_theme"
        /**
         * Exposes the `KEY_DESIGN_DARK_THEME` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DARK_THEME = "key_design_dark_theme"
        /**
         * Exposes the `KEY_DESIGN_COLOR_TOKEN` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_COLOR_TOKEN = "key_design_color_token_theme"
        /**
         * Exposes the `KEY_DESIGN_DARK_TOKEN` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DARK_TOKEN = "key_design_dark_color_token_theme"
        /**
         * Exposes the `KEY_BACKGROUND_GENERATION` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BACKGROUND_GENERATION = "key_background_generation"
        /**
         * Exposes the `KEY_BACKGROUND_PROCESS_COUNT` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BACKGROUND_PROCESS_COUNT = "key_background_process_count"
        /**
         * Exposes the `KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED = "key_local_generation_benchmark_prompt_answered"
        /**
         * Exposes the `KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED = "key_benchmark_recommendation_warning_suppressed"
        /**
         * Exposes the `KEY_GALLERY_GRID` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_GALLERY_GRID = "key_gallery_grid"
        /**
         * Exposes the `KEY_LANGUAGE_CODE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LANGUAGE_CODE = "key_language_code"
    }
}

/**
 * Executes the `defaultSaveToMediaStore` step in the SDAI data layer.
 *
 * @return Result produced by `defaultSaveToMediaStore`.
 * @author Dmitriy Moroz
 */
internal expect fun defaultSaveToMediaStore(): Boolean

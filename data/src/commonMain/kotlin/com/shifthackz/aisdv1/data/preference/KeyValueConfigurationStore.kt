package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore

/**
 * Coordinates `KeyValueConfigurationStore` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class KeyValueConfigurationStore(
    /**
     * Exposes the `keyValueStore` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val keyValueStore: KeyValueStore,
) : ConfigurationStore {

    /**
     * Exposes the `automatic1111ServerUrl` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var automatic1111ServerUrl: String
        get() = keyValueStore.getString(KEY_SERVER_URL).fixUrlSlashes()
        set(value) = keyValueStore.putString(KEY_SERVER_URL, value.fixUrlSlashes())

    /**
     * Exposes the `swarmUiServerUrl` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiServerUrl: String
        get() = keyValueStore.getString(KEY_SWARM_SERVER_URL).fixUrlSlashes()
        set(value) = keyValueStore.putString(KEY_SWARM_SERVER_URL, value.fixUrlSlashes())

    /**
     * Exposes the `swarmUiModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiModel: String
        get() = keyValueStore.getString(KEY_SWARM_MODEL)
        set(value) = keyValueStore.putString(KEY_SWARM_MODEL, value)

    /**
     * Exposes the `demoMode` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var demoMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEMO_MODE)
        set(value) = keyValueStore.putBoolean(KEY_DEMO_MODE, value)

    /**
     * Exposes the `source` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var source: ServerSource
        get() = ServerSource.parse(keyValueStore.getString(KEY_SERVER_SOURCE, ServerSource.AUTOMATIC1111.key))
        set(value) = keyValueStore.putString(KEY_SERVER_SOURCE, value.key)

    /**
     * Exposes the `hordeApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var hordeApiKey: String
        get() = keyValueStore.getString(KEY_HORDE_API_KEY)
        set(value) = keyValueStore.putString(KEY_HORDE_API_KEY, value)

    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleUrl: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_URL, "")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_URL, value)

    /**
     * Optional bearer token for the independent compatible server.
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_API_KEY, "")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_API_KEY, value)

    /**
     * Editable image model identifier supplied to the compatible server.
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleModel: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_MODEL, "gpt-image-2")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_MODEL, value)

    /**
     * Exposes the `openAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var openAiApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_OPEN_AI_API_KEY, value)

    /**
     * Exposes the `huggingFaceApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceApiKey: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_API_KEY)
        set(value) = keyValueStore.putString(KEY_HUGGING_FACE_API_KEY, value)

    /**
     * Exposes the `huggingFaceModel` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceModel: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_MODEL_KEY, HuggingFaceModel.default.alias)
        set(value) = keyValueStore.putString(KEY_HUGGING_FACE_MODEL_KEY, value)

    /**
     * Exposes the `stabilityAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiApiKey: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_STABILITY_AI_API_KEY, value)

    /**
     * Exposes the `falAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var falAiApiKey: String
        get() = keyValueStore.getString(KEY_FAL_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_FAL_AI_API_KEY, value)

    /**
     * Exposes the `arliAiApiKey` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiApiKey: String
        get() = keyValueStore.getString(KEY_ARLI_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_ARLI_AI_API_KEY, value)

    /**
     * Exposes the `stabilityAiEngineId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiEngineId: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_ENGINE_ID_KEY)
        set(value) = keyValueStore.putString(KEY_STABILITY_AI_ENGINE_ID_KEY, value)

    /**
     * Exposes the `localOnnxModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelId: String
        get() = keyValueStore.getString(KEY_LOCAL_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_LOCAL_MODEL_ID, value)

    /**
     * Exposes the `localOnnxModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelPath: String
        get() = keyValueStore.getString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localMediaPipeModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelId: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_MODEL_ID, value)

    /**
     * Exposes the `localMediaPipeModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelPath: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localSdxlModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelId: String
        get() = keyValueStore.getString(KEY_SDXL_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_SDXL_MODEL_ID, value)

    /**
     * Exposes the `localSdxlModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelPath: String
        get() = keyValueStore.getString(KEY_SDXL_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_SDXL_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localCoreMlModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelId: String
        get() = keyValueStore.getString(KEY_CORE_ML_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_CORE_ML_MODEL_ID, value)

    /**
     * Exposes the `localCoreMlModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelPath: String
        get() = keyValueStore.getString(KEY_CORE_ML_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_CORE_ML_CUSTOM_MODEL_PATH, value)

    /**
     * Exposes the `localBonsaiModelId` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelId: String
        get() = keyValueStore.getString(KEY_BONSAI_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_BONSAI_MODEL_ID, value)

    /**
     * Exposes the `localBonsaiModelPath` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelPath: String
        get() = keyValueStore.getString(KEY_BONSAI_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_BONSAI_CUSTOM_MODEL_PATH, value)

    /**
     * Executes the `fixUrlSlashes` step in the SDAI data layer.
     *
     * @return Result produced by `fixUrlSlashes`.
     * @author Dmitriy Moroz
     */
    private fun String.fixUrlSlashes(): String {
        if (isEmpty()) return this
        return replace("://", URL_PROTOCOL_MARKER)
            .replace("//", "/")
            .replace(URL_PROTOCOL_MARKER, "://")
            .trimEnd('/')
    }

    /**
     * Provides the `companion object` singleton used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
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
         * Exposes the `KEY_SERVER_SOURCE` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SERVER_SOURCE = "key_server_source"
        /**
         * Exposes the `KEY_HORDE_API_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HORDE_API_KEY = "key_horde_api_key"
        const val KEY_OPEN_AI_COMPATIBLE_URL = "key_open_ai_compatible_url"
        const val KEY_OPEN_AI_COMPATIBLE_API_KEY = "key_open_ai_compatible_api_key"
        const val KEY_OPEN_AI_COMPATIBLE_MODEL = "key_open_ai_compatible_model"
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
         * Exposes the `KEY_STABILITY_AI_ENGINE_ID_KEY` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_ENGINE_ID_KEY = "key_stability_ai_engine_id_key"
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
         * Exposes the `URL_PROTOCOL_MARKER` value used by the SDAI data layer.
         *
         * @author Dmitriy Moroz
         */
        const val URL_PROTOCOL_MARKER = "SDAI_URL_PROTOCOL_MARKER"
    }
}

package com.shifthackz.aisdv1.domain.preference

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Defines the `ConfigurationStore` contract for the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
interface ConfigurationStore {
    /**
     * Exposes the `automatic1111ServerUrl` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var automatic1111ServerUrl: String
    /**
     * Exposes the `swarmUiServerUrl` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var swarmUiServerUrl: String
    /**
     * Exposes the `swarmUiModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var swarmUiModel: String
    /**
     * Exposes the `demoMode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var demoMode: Boolean
    /**
     * Exposes the `source` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var source: ServerSource
    /**
     * Exposes the `hordeApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var hordeApiKey: String
    /**
     * Exposes the `openAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var openAiApiKey: String
    /**
     * Editable image model identifier supplied to the compatible server.
     * @author Dmitriy Moroz
     */
    var openAiCompatibleModel: String
    /**
     * Optional bearer token for the independent compatible server.
     * @author Dmitriy Moroz
     */
    var openAiCompatibleApiKey: String
    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     * @author Dmitriy Moroz
     */
    var openAiCompatibleUrl: String
    /**
     * Exposes the `huggingFaceApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var huggingFaceApiKey: String
    /**
     * Exposes the `huggingFaceModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var huggingFaceModel: String
    /**
     * Exposes the `stabilityAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiApiKey: String
    /**
     * Exposes the `stabilityAiEngineId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiEngineId: String
    /**
     * Exposes the `falAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var falAiApiKey: String
    /**
     * Exposes the `arliAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var arliAiApiKey: String
    /**
     * Exposes the `localOnnxModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelId: String
    /**
     * Exposes the `localOnnxModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelPath: String
    /**
     * Exposes the `localMediaPipeModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelId: String
    /**
     * Exposes the `localMediaPipeModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelPath: String
    /**
     * Exposes the `localSdxlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelId: String
    /**
     * Exposes the `localSdxlModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelPath: String
    /**
     * Exposes the `localCoreMlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelId: String
    /**
     * Exposes the `localCoreMlModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelPath: String
    /**
     * Exposes the `localBonsaiModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelId: String
    /**
     * Exposes the `localBonsaiModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelPath: String

    /**
     * Loads SDAI data through `getConfiguration`.
     *
     * @param authCredentials auth credentials value consumed by the API.
     * @author Dmitriy Moroz
     */
    fun getConfiguration(
        authCredentials: AuthorizationCredentials,
    ): Configuration = Configuration(
        serverUrl = automatic1111ServerUrl,
        swarmUiUrl = swarmUiServerUrl,
        swarmUiModel = swarmUiModel,
        demoMode = demoMode,
        source = source,
        hordeApiKey = hordeApiKey,
        openAiApiKey = openAiApiKey,
        openAiCompatibleModel = openAiCompatibleModel,
        openAiCompatibleApiKey = openAiCompatibleApiKey,
        openAiCompatibleUrl = openAiCompatibleUrl,
        huggingFaceApiKey = huggingFaceApiKey,
        huggingFaceModel = huggingFaceModel,
        stabilityAiApiKey = stabilityAiApiKey,
        stabilityAiEngineId = stabilityAiEngineId,
        falAiApiKey = falAiApiKey,
        arliAiApiKey = arliAiApiKey,
        authCredentials = authCredentials,
        localOnnxModelId = localOnnxModelId,
        localOnnxModelPath = localOnnxModelPath,
        localMediaPipeModelId = localMediaPipeModelId,
        localMediaPipeModelPath = localMediaPipeModelPath,
        localSdxlModelId = localSdxlModelId,
        localSdxlModelPath = localSdxlModelPath,
        localCoreMlModelId = localCoreMlModelId,
        localCoreMlModelPath = localCoreMlModelPath,
        localBonsaiModelId = localBonsaiModelId,
        localBonsaiModelPath = localBonsaiModelPath,
    )

    /**
     * Executes the `setConfiguration` step in the SDAI domain layer.
     *
     * @param configuration configuration value consumed by the API.
     * @author Dmitriy Moroz
     */
    fun setConfiguration(configuration: Configuration) {
        source = configuration.source
        automatic1111ServerUrl = configuration.serverUrl
        swarmUiServerUrl = configuration.swarmUiUrl
        swarmUiModel = configuration.swarmUiModel
        demoMode = configuration.demoMode
        hordeApiKey = configuration.hordeApiKey
        openAiApiKey = configuration.openAiApiKey
        openAiCompatibleModel = configuration.openAiCompatibleModel
        openAiCompatibleApiKey = configuration.openAiCompatibleApiKey
        openAiCompatibleUrl = configuration.openAiCompatibleUrl
        huggingFaceApiKey = configuration.huggingFaceApiKey
        huggingFaceModel = configuration.huggingFaceModel
        stabilityAiApiKey = configuration.stabilityAiApiKey
        stabilityAiEngineId = configuration.stabilityAiEngineId
        falAiApiKey = configuration.falAiApiKey
        arliAiApiKey = configuration.arliAiApiKey
        localOnnxModelId = configuration.localOnnxModelId
        localOnnxModelPath = configuration.localOnnxModelPath
        localMediaPipeModelId = configuration.localMediaPipeModelId
        localMediaPipeModelPath = configuration.localMediaPipeModelPath
        localSdxlModelId = configuration.localSdxlModelId
        localSdxlModelPath = configuration.localSdxlModelPath
        localCoreMlModelId = configuration.localCoreMlModelId
        localCoreMlModelPath = configuration.localCoreMlModelPath
        localBonsaiModelId = configuration.localBonsaiModelId
        localBonsaiModelPath = configuration.localBonsaiModelPath
    }
}

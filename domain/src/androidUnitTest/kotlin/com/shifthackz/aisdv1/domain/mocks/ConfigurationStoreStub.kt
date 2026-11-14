package com.shifthackz.aisdv1.domain.mocks

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore

class ConfigurationStoreStub(
    configuration: Configuration = mockConfiguration,
) : ConfigurationStore {
    override var automatic1111ServerUrl: String = configuration.serverUrl
    override var swarmUiServerUrl: String = configuration.swarmUiUrl
    override var swarmUiModel: String = configuration.swarmUiModel
    override var demoMode: Boolean = configuration.demoMode
    override var source: ServerSource = configuration.source
    override val providerSelectionRevision: Long = configuration.providerSelectionRevision
    override var hordeApiKey: String = configuration.hordeApiKey
    override var openAiCompatibleUrl: String = configuration.openAiCompatibleUrl
    override var openAiCompatibleApiKey: String = configuration.openAiCompatibleApiKey
    override var openAiCompatibleModel: String = configuration.openAiCompatibleModel
    override var openAiApiKey: String = configuration.openAiApiKey
    override var huggingFaceApiKey: String = configuration.huggingFaceApiKey
    override var huggingFaceModel: String = configuration.huggingFaceModel
    override var stabilityAiApiKey: String = configuration.stabilityAiApiKey
    override var stabilityAiEngineId: String = configuration.stabilityAiEngineId
    override var falAiApiKey: String = configuration.falAiApiKey
    override var arliAiApiKey: String = configuration.arliAiApiKey
    override var localOnnxModelId: String = configuration.localOnnxModelId
    override var localOnnxModelPath: String = configuration.localOnnxModelPath
    override var localMediaPipeModelId: String = configuration.localMediaPipeModelId
    override var localMediaPipeModelPath: String = configuration.localMediaPipeModelPath
    override var localSdxlModelId: String = configuration.localSdxlModelId
    override var localSdxlModelPath: String = configuration.localSdxlModelPath
    override var localCoreMlModelId: String = configuration.localCoreMlModelId
    override var localCoreMlModelPath: String = configuration.localCoreMlModelPath
    override var localBonsaiModelId: String = configuration.localBonsaiModelId
    override var localBonsaiModelPath: String = configuration.localBonsaiModelPath
}

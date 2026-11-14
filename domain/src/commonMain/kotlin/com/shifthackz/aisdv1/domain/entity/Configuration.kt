package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Complete provider configuration read by setup and generation screens.
 * @param providerSelectionRevision Durable revision of the selected source; changes on source replacement.
 *
 * @author Dmitriy Moroz
 */
data class Configuration(
    /** Revision read from provider-selection storage; configuration writes never restore an old revision.
     * @author Dmitriy Moroz */
    val providerSelectionRevision: Long = 0L,
    /**
     * Exposes the `serverUrl` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String = "",
    /**
     * Exposes the `swarmUiUrl` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val swarmUiUrl: String = "",
    /**
     * Exposes the `swarmUiModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val swarmUiModel: String = "",
    /**
     * Exposes the `demoMode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean = false,
    /**
     * Exposes the `source` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    /**
     * Exposes the `hordeApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val hordeApiKey: String = "",
    /**
     * Exposes the `openAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val openAiApiKey: String = "",
    /**
     * Editable image model identifier supplied to the compatible server.
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    /**
     * Optional bearer token for the independent compatible server.
     * @author Dmitriy Moroz
     */
    val openAiCompatibleApiKey: String = "",
    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     * @author Dmitriy Moroz
     */
    val openAiCompatibleUrl: String = "",
    /**
     * Exposes the `huggingFaceApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val huggingFaceApiKey: String = "",
    /**
     * Exposes the `huggingFaceModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val huggingFaceModel: String = "",
    /**
     * Exposes the `stabilityAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiApiKey: String = "",
    /**
     * Exposes the `stabilityAiEngineId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiEngineId: String = "",
    /**
     * Exposes the `falAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val falAiApiKey: String = "",
    /**
     * Exposes the `arliAiApiKey` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val arliAiApiKey: String = "",
    /**
     * Exposes the `authCredentials` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val authCredentials: AuthorizationCredentials = AuthorizationCredentials.None,
    /**
     * Exposes the `localOnnxModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localOnnxModelId: String = "",
    /**
     * Exposes the `localOnnxModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localOnnxModelPath: String = "",
    /**
     * Exposes the `localMediaPipeModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localMediaPipeModelId: String = "",
    /**
     * Exposes the `localMediaPipeModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localMediaPipeModelPath: String = "",
    /**
     * Exposes the `localSdxlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localSdxlModelId: String = "",
    /**
     * Exposes the `localSdxlModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localSdxlModelPath: String = "",
    /**
     * Exposes the `localCoreMlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localCoreMlModelId: String = "",
    /**
     * Exposes the `localCoreMlModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localCoreMlModelPath: String = "",
    /**
     * Exposes the `localBonsaiModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localBonsaiModelId: String = "",
    /**
     * Exposes the `localBonsaiModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localBonsaiModelPath: String = "",
)

package com.shifthackz.aisdv1.domain.preference

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Defines the `PreferenceManager` contract for the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
interface PreferenceManager {
    /**
     * Last sampling step count in the txt2img form; defaults to 20 on first launch.
     *
     * @author Dmitriy Moroz
     */
    var textToImageSamplingSteps: Int

    /**
     * Last CFG scale in the txt2img form; defaults to 7.0 on first launch.
     *
     * @author Dmitriy Moroz
     */
    var textToImageCfgScale: Float

    /**
     * Last sampling step count in the img2img form, stored independently of txt2img.
     *
     * @author Dmitriy Moroz
     */
    var imageToImageSamplingSteps: Int

    /**
     * Last CFG scale in the img2img form, stored independently of txt2img.
     *
     * @author Dmitriy Moroz
     */
    var imageToImageCfgScale: Float

    /**
     * Time allowed to establish provider connections, independent of generation waiting limits.
     *
     * @author Dmitriy Moroz
     */
    var connectionTimeout: NetworkTimeout

    /**
     * Waiting limit shared by all network-based AI generation providers.
     *
     * @author Dmitriy Moroz
     */
    var generationRequestTimeout: NetworkTimeout

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
     * Exposes the `developerMode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var developerMode: Boolean
    /**
     * Exposes the `localMediaPipeCustomModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeCustomModelPath: String
    /**
     * Exposes the `localCoreMlCustomModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlCustomModelPath: String
    /**
     * Exposes the `localBonsaiCustomModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiCustomModelPath: String
    /**
     * Exposes the `localOnnxCustomModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxCustomModelPath: String
    /**
     * Exposes the `localSdxlCustomModelPath` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlCustomModelPath: String
    /**
     * Exposes the `localOnnxAllowCancel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxAllowCancel: Boolean
    /**
     * Exposes the `localOnnxSchedulerThread` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxSchedulerThread: SchedulersToken
    /**
     * Exposes the `monitorConnectivity` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var monitorConnectivity: Boolean
    /**
     * Exposes the `autoSaveAiResults` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var autoSaveAiResults: Boolean
    /**
     * Exposes the `saveToMediaStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var saveToMediaStore: Boolean
    /**
     * Exposes the `formAdvancedOptionsAlwaysShow` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var formAdvancedOptionsAlwaysShow: Boolean
    /**
     * Exposes the `formPromptTaggedInput` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var formPromptTaggedInput: Boolean
    /**
     * Exposes the `source` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var source: ServerSource
    /**
     * Exposes the `sdModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var sdModel: String
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
     * Exposes the `arliAiModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var arliAiModel: String
    /**
     * Exposes the `stabilityAiEngineId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiEngineId: String
    /**
     * Exposes the `onBoardingComplete` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var onBoardingComplete: Boolean
    /**
     * Exposes the `forceSetupAfterUpdate` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var forceSetupAfterUpdate: Boolean
    /**
     * Exposes the `localOnnxModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelId: String
    /**
     * Exposes the `localOnnxUseNNAPI` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxUseNNAPI: Boolean
    /**
     * Exposes the `localMediaPipeModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelId: String
    /**
     * Exposes the `localSdxlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelId: String
    /**
     * Exposes the `localCoreMlModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelId: String
    /**
     * Exposes the `localBonsaiModelId` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelId: String
    /**
     * Exposes the `designUseSystemColorPalette` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var designUseSystemColorPalette: Boolean
    /**
     * Exposes the `designUseSystemDarkTheme` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var designUseSystemDarkTheme: Boolean
    /**
     * Exposes the `designDarkTheme` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var designDarkTheme: Boolean
    /**
     * Exposes the `designColorToken` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var designColorToken: String
    /**
     * Exposes the `designDarkThemeToken` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var designDarkThemeToken: String
    /**
     * Exposes the `backgroundGeneration` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var backgroundGeneration: Boolean
    /**
     * Exposes the `backgroundProcessCount` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var backgroundProcessCount: Int
    /**
     * Tracks whether the first local generation benchmark prompt has already
     * been answered.
     *
     * @author Dmitriy Moroz
     */
    var localGenerationBenchmarkPromptAnswered: Boolean
    /**
     * Tracks whether benchmark recommendation warnings are muted until the next
     * benchmark run.
     *
     * @author Dmitriy Moroz
     */
    var benchmarkRecommendationWarningSuppressed: Boolean
    /**
     * Exposes the `galleryGrid` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var galleryGrid: Grid
    /**
     * Exposes the `languageCode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    var languageCode: String

    /**
     * Loads SDAI data through `observe`.
     *
     * @return Result produced by `observe`.
     * @author Dmitriy Moroz
     */
    fun observe(): Flow<Settings>

    /**
     * Executes the `refresh` step in the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    suspend fun refresh()
}

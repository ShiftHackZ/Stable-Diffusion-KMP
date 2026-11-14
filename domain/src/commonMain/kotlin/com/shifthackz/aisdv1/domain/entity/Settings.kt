package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken

/**
 * Observable application preferences including the selected generation source.
 * @param providerSelectionRevision Durable source revision used to invalidate earlier queued work.
 *
 * @author Dmitriy Moroz
 */
data class Settings(
    /** Durable source-selection identity used to invalidate work even after an away/back switch.
     * @author Dmitriy Moroz */
    val providerSelectionRevision: Long = 0L,
    /**
     * Time allowed to establish network provider connections; defaults to 120 seconds.
     *
     * @author Dmitriy Moroz
     */
    val connectionTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    /**
     * Request and socket waiting limit for network image generation.
     *
     * @author Dmitriy Moroz
     */
    val generationRequestTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    /**
     * Exposes the `serverUrl` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String = "",
    /**
     * Exposes the `sdModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val sdModel: String = "",
    /**
     * Exposes the `demoMode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean = false,
    /**
     * Exposes the `developerMode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val developerMode: Boolean = false,
    /**
     * Exposes the `localDiffusionAllowCancel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localDiffusionAllowCancel: Boolean = false,
    /**
     * Exposes the `localDiffusionSchedulerThread` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localDiffusionSchedulerThread: SchedulersToken = SchedulersToken.COMPUTATION,
    /**
     * Exposes the `monitorConnectivity` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val monitorConnectivity: Boolean = false,
    /**
     * Exposes the `backgroundGeneration` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val backgroundGeneration: Boolean = false,
    /**
     * Exposes the `autoSaveAiResults` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val autoSaveAiResults: Boolean = false,
    /**
     * Exposes the `saveToMediaStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val saveToMediaStore: Boolean = false,
    /**
     * Exposes the `formAdvancedOptionsAlwaysShow` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val formAdvancedOptionsAlwaysShow: Boolean = false,
    /**
     * Exposes the `formPromptTaggedInput` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val formPromptTaggedInput: Boolean = false,
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
     * Exposes the `arliAiModel` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val arliAiModel: String = "",
    /**
     * Exposes the `localUseNNAPI` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val localUseNNAPI: Boolean = false,
    /**
     * Exposes the `designUseSystemColorPalette` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val designUseSystemColorPalette: Boolean = false,
    /**
     * Exposes the `designUseSystemDarkTheme` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val designUseSystemDarkTheme: Boolean = false,
    /**
     * Exposes the `designDarkTheme` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val designDarkTheme: Boolean = false,
    /**
     * Exposes the `designColorToken` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val designColorToken: String = "",
    /**
     * Exposes the `designDarkThemeToken` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val designDarkThemeToken: String = "",
    /**
     * Exposes the `galleryGrid` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val galleryGrid: Grid = Grid.Fixed2,
    /**
     * Exposes the `languageCode` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val languageCode: String = "",
)

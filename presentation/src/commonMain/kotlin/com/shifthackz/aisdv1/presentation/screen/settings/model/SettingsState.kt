package com.shifthackz.aisdv1.presentation.screen.settings.model

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType

/**
 * Immutable UI model for the Settings screen.
 *
 * The fields mirror persisted app preferences, runtime provider status, and the
 * currently visible modal. Derived `show...` properties keep section visibility
 * rules close to the state they depend on.
 *
 * @param loading True while Settings should show placeholder rows.
 * @param onBoardingDemo Whether demo onboarding mode hides provider configuration actions.
 * @param screenModal Modal currently shown above Settings.
 * @param serverSource Current generation provider.
 * @param generationRequestTimeout Persisted waiting limit for network image generation requests.
 * @param connectionTimeout Persisted limit for establishing network provider connections.
 * @param sdModels Provider model names shown in the Settings picker.
 * @param sdModelSelected Current provider model selection.
 * @param stabilityAiCredits Current Stability AI balance shown in Settings.
 * @param localUseNNAPI Whether ONNX local diffusion should use NNAPI acceleration.
 * @param backgroundGenerationAvailable Whether background generation is supported by this target.
 * @param backgroundGeneration Whether background generation is currently enabled.
 * @param monitorConnectivity Whether server connectivity monitoring is enabled.
 * @param autoSaveAiResults Whether generated images should be saved automatically.
 * @param saveToMediaStore Whether generated images should be saved to the public gallery.
 * @param formAdvancedOptionsAlwaysShow Whether advanced generation options are always expanded.
 * @param formPromptTaggedInput Whether tagged prompt input mode is enabled.
 * @param useSystemColorPalette Whether system dynamic colors should be used.
 * @param useSystemDarkTheme Whether system dark theme should drive app theme.
 * @param darkTheme Whether dark theme is currently enabled.
 * @param colorToken Selected static color palette token.
 * @param darkThemeToken Selected dark theme palette token.
 * @param galleryGrid Selected gallery grid density.
 * @param developerMode Whether developer-only Settings rows are visible.
 * @param appVersion Version string shown in Settings.
 * @param storageUsageBytes Current total storage bytes shown in the Settings summary row.
 * @param networkUsageBytes Current total network bytes shown in the Settings summary row.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class SettingsState(
    val loading: Boolean = true,
    val onBoardingDemo: Boolean = false,
    val screenModal: SettingsModal = SettingsModal.None,
    val serverSource: ServerSource = ServerSource.AUTOMATIC1111,
    val generationRequestTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    val connectionTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    val sdModels: List<String> = emptyList(),
    val sdModelSelected: String = "",
    val stabilityAiCredits: Float = 0f,
    val localUseNNAPI: Boolean = false,
    val backgroundGenerationAvailable: Boolean = false,
    val backgroundGeneration: Boolean = false,
    val monitorConnectivity: Boolean = false,
    val autoSaveAiResults: Boolean = false,
    val saveToMediaStore: Boolean = false,
    val formAdvancedOptionsAlwaysShow: Boolean = false,
    val formPromptTaggedInput: Boolean = false,
    val useSystemColorPalette: Boolean = false,
    val useSystemDarkTheme: Boolean = false,
    val darkTheme: Boolean = false,
    val colorToken: ColorToken = ColorToken.MAUVE,
    val darkThemeToken: DarkThemeToken = DarkThemeToken.FRAPPE,
    val galleryGrid: Grid = Grid.Fixed2,
    val developerMode: Boolean = false,
    val appVersion: String = "",
    val storageUsageBytes: Long = 0L,
    val networkUsageBytes: Long = 0L,
) : MviState {

    val showStabilityAiCredits: Boolean
        get() = serverSource == ServerSource.STABILITY_AI

    val showLocalMicrosoftONNXUseNNAPI: Boolean
        get() = serverSource == ServerSource.LOCAL_MICROSOFT_ONNX

    val showSdModelSelector: Boolean
        get() = serverSource == ServerSource.AUTOMATIC1111

    val showMonitorConnectionOption: Boolean
        get() = serverSource == ServerSource.AUTOMATIC1111 || serverSource == ServerSource.SWARM_UI

    /**
     * Shows the waiting-limit control only for providers whose generation calls use it.
     *
     * @author Dmitriy Moroz
     */
    val showNetworkTimeoutSettings: Boolean
        get() = serverSource.type != ServerSourceType.LOCAL

    val showFormAdvancedOption: Boolean
        get() = serverSource != ServerSource.OPEN_AI && serverSource != ServerSource.OPEN_AI_COMPATIBLE && serverSource != ServerSource.OPEN_AI_OAUTH

    val showUseSystemColorPalette: Boolean = false
}

/**
 * Modal destinations that can be displayed on top of the Settings screen.
 *
 * @author Dmitriy Moroz
 */
sealed interface SettingsModal {
    /**
     * No modal is visible.
     *
     * @author Dmitriy Moroz
     */
    data object None : SettingsModal
    /**
     * Confirmation dialog before deleting local app cache.
     *
     * @author Dmitriy Moroz
     */
    data object ClearAppCache : SettingsModal
    /**
     * Blocking progress dialog while a long settings action is running.
     *
     * @author Dmitriy Moroz
     */
    data object Communicating : SettingsModal
    /**
     * Bottom sheet for choosing the app language.
     *
     * @author Dmitriy Moroz
     */
    data object Language : SettingsModal
    data class SelectSdModel(val models: List<String>, val selected: String) : SettingsModal
    data class ManualPermission(val permission: String) : SettingsModal
    data class GalleryGrid(val grid: Grid) : SettingsModal

    /**
     * Waiting-limit picker initialized with the current preference.
     *
     * @param selected Current network image generation timeout.
     * @author Dmitriy Moroz
     */
    data class GenerationTimeout(val selected: NetworkTimeout) : SettingsModal

    /**
     * Independent connection-limit picker with an explicit notice for unsupported engines.
     *
     * @param selected Current persisted connection limit.
     * @param supported Whether the platform engine enforces an independent connection limit.
     * @author Dmitriy Moroz
     */
    data class ConnectionTimeout(val selected: NetworkTimeout, val supported: Boolean) : SettingsModal
}

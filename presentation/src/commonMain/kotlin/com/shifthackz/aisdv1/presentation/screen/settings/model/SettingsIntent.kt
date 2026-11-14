package com.shifthackz.aisdv1.presentation.screen.settings.model

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.presentation.screen.drawer.DrawerIntent

/**
 * Describes user actions and screen side-effect requests handled by SettingsViewModel.
 *
 * @author Dmitriy Moroz
 */
sealed interface SettingsIntent : MviIntent {

    /**
     * Opens the provider configuration flow from the Settings server section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateConfiguration : SettingsIntent

    /**
     * Opens the benchmark screen from the Settings server section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBenchmark : SettingsIntent

    data object NavigateStorageUsage : SettingsIntent

    data object NavigateNetworkUsage : SettingsIntent

    /**
     * Opens the debug/developer screen after developer mode is unlocked.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateDeveloperMode : SettingsIntent

    /**
     * Groups actions for the Automatic1111 Stable Diffusion model selector.
     *
     * @author Dmitriy Moroz
     */
    sealed interface SdModel : SettingsIntent {
        /**
         * Opens the currently known remote model list in a modal picker.
         *
         * @author Dmitriy Moroz
         */
        data object OpenChooser : SdModel

        data class Select(val model: String) : SdModel
    }

    /**
     * Groups one-off Settings commands that are not simple boolean updates.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Action : SettingsIntent {
        enum class ClearAppCache : Action {
            Request, Confirm
        }

        data object ReportProblem : Action

        data object AppVersion : Action

        data object PickLanguage : Action

        data class SetLanguage(val languageCode: String) : Action

        sealed interface GalleryGrid : Action {
            data object Pick : GalleryGrid

            data class Set(val grid: Grid) : GalleryGrid
        }

        sealed interface RequestTimeout : Action {
            data object Edit : RequestTimeout

            data class Set(val seconds: Int) : RequestTimeout
        }

        data object Donate : Action

        data object OnBoarding : Action
    }

    /**
     * Groups external web links opened from Settings.
     *
     * @author Dmitriy Moroz
     */
    sealed interface LaunchUrl : SettingsIntent {
        data object OpenPolicy : LaunchUrl

        data object OpenSourceCode : LaunchUrl

        data object OpenProjectWebsite : LaunchUrl

        data object OpenDeveloperWebsite : LaunchUrl

        data object OpenLicense : LaunchUrl

        data object OpenTelegramCommunity : LaunchUrl

        data object OpenDiscordCommunity : LaunchUrl
    }

    /**
     * Groups boolean Settings toggles persisted via PreferenceManager.
     *
     * @author Dmitriy Moroz
     */
    sealed interface UpdateFlag : SettingsIntent {
        /**
         * Desired value of the toggled preference.
         *
         * @author Dmitriy Moroz
         */
        val flag: Boolean

        data class NNAPI(override val flag: Boolean) : UpdateFlag

        data class MonitorConnection(override val flag: Boolean) : UpdateFlag

        data class AutoSaveResult(override val flag: Boolean) : UpdateFlag

        data class BackgroundGeneration(override val flag: Boolean) : UpdateFlag

        data class SaveToMediaStore(override val flag: Boolean) : UpdateFlag

        data class TaggedInput(override val flag: Boolean) : UpdateFlag

        data class AdvancedFormVisibility(override val flag: Boolean) : UpdateFlag

        data class DynamicColors(override val flag: Boolean) : UpdateFlag

        data class SystemDarkTheme(override val flag: Boolean) : UpdateFlag

        data class DarkTheme(override val flag: Boolean) : UpdateFlag
    }

    data class NewColorToken(val token: ColorToken) : SettingsIntent

    data class NewDarkThemeToken(val token: DarkThemeToken) : SettingsIntent

    /**
     * Closes the currently visible Settings modal, if any.
     *
     * @author Dmitriy Moroz
     */
    data object DismissDialog : SettingsIntent

    /**
     * Forwards drawer navigation commands emitted from the Settings top bar.
     *
     * @param intent intent to process in the MVI workflow.
     * @author Dmitriy Moroz
     */
    data class Drawer(val intent: DrawerIntent) : SettingsIntent
}

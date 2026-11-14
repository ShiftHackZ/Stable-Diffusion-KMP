package com.shifthackz.aisdv1.presentation.screen.settings.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.presentation.platform.IosStorageUsageFileSystem
import com.shifthackz.aisdv1.presentation.platform.IosUrlOpener
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * Creates iOS-specific settings actions for links and settings navigation.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberSettingsPlatformActions(): SettingsPlatformActions = remember {
    IosSettingsPlatformActions(IosStorageUsageFileSystem())
}

/**
 * iOS bridge used by Settings for links, settings navigation, and storage usage summaries.
 *
 * @param fileSystem Shared iOS storage helper that knows the app cache and local model paths.
 *
 * @author Dmitriy Moroz
 */
private class IosSettingsPlatformActions(
    private val fileSystem: IosStorageUsageFileSystem,
) : SettingsPlatformActions {
    override val requiresStoragePermissionForMediaStore: Boolean = false
    override val supportsBackgroundGeneration: Boolean = true
    override val supportsConnectionTimeout: Boolean = false
    override val backgroundGenerationWarningKey = "settings_item_background_generation_warning_ios"

    override fun mapStorageBytesForUi(bytes: Long): Long =
        fileSystem.mapStorageBytesForUi(bytes)

    override suspend fun requestStoragePermission(): Boolean = true

    override suspend fun requestNotificationPermission(): Boolean = true

    override suspend fun getAppCacheBytes(): Long = fileSystem.getAppCacheBytes()

    override suspend fun clearAppCache() {
        fileSystem.clearAppCache()
    }

    override suspend fun getAllDownloadedModelsBytes(): Long =
        fileSystem.getAllDownloadedModelsBytes()

    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long =
        fileSystem.getDownloadedModelsBytes(modelIds)

    override fun openUrl(url: String) {
        IosUrlOpener.openUrl(url)
    }

    override fun shareLogFile() {
        IosUrlOpener.openUrl(REPORT_PROBLEM_MAILTO)
    }

    override fun showDeveloperModeUnlocked() = Unit

    override fun openAppSettings() {
        IosUrlOpener.openUrl(UIApplicationOpenSettingsURLString)
    }

    /**
     * Static iOS support email URL used by Settings report action.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        /**
         * Mailto URL opened when the user chooses to report a problem from Settings.
         *
         * @author Dmitriy Moroz
         */
        const val REPORT_PROBLEM_MAILTO =
            "mailto:sdai@moroz.cc?subject=SDAI%20-%20Problem%20report&body=SDAI%20iOS"
    }
}

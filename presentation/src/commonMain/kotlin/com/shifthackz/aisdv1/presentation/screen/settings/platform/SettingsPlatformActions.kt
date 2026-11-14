package com.shifthackz.aisdv1.presentation.screen.settings.platform

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.model.StorageUsageByteMapper

/**
 * Platform bridge for Settings actions that cannot be implemented in common code.
 *
 * Implementations request Android/iOS permissions, open external URLs or app settings,
 * and expose platform capability flags used to hide unsupported Settings rows.
 *
 * @author Dmitriy Moroz
 */
interface SettingsPlatformActions : StorageUsageByteMapper {
    /**
     * True when saving generated images through MediaStore still needs a runtime storage permission.
     *
     * @author Dmitriy Moroz
     */
    val requiresStoragePermissionForMediaStore: Boolean
    /**
     * True when this target can continue generation work after the app leaves foreground.
     *
     * @author Dmitriy Moroz
     */
    val supportsBackgroundGeneration: Boolean
    /**
     * True when the networking engine supports the connection timeout setting shown in Settings.
     *
     * @author Dmitriy Moroz
     */
    val supportsConnectionTimeout: Boolean
    /**
     * Localization key for the warning shown under the background generation toggle.
     *
     * @author Dmitriy Moroz
     */
    val backgroundGenerationWarningKey: String

    /**
     * Requests storage/media permission when the platform requires it for gallery saves.
     *
     * @author Dmitriy Moroz
     */
    suspend fun requestStoragePermission(): Boolean

    /**
     * Requests notification permission when the platform requires it for background generation.
     *
     * @author Dmitriy Moroz
     */
    suspend fun requestNotificationPermission(): Boolean

    /**
     * Returns current app cache byte size for the Settings summary row.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAppCacheBytes(): Long

    /**
     * Clears app cache files after a caller has shown destructive confirmation.
     *
     * @author Dmitriy Moroz
     */
    suspend fun clearAppCache()

    /**
     * Returns total bytes occupied by every downloaded model directory visible to the platform.
     *
     * Settings uses this as a fallback when platform storage contains downloaded models before
     * the model catalog provides their ids.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllDownloadedModelsBytes(): Long

    /**
     * Returns total bytes occupied by downloaded model directories for [modelIds].
     *
     * @param modelIds Local model identifiers whose downloaded directories should be measured.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long

    /**
     * Opens an external URL through the platform.
     *
     * @param url Absolute URL or platform-supported URI to open.
     *
     * @author Dmitriy Moroz
     */
    fun openUrl(url: String)

    /**
     * Opens the problem-report channel with diagnostic context prepared by the platform.
     *
     * @author Dmitriy Moroz
     */
    fun shareLogFile()

    /**
     * Shows platform feedback after developer mode is unlocked.
     *
     * @author Dmitriy Moroz
     */
    fun showDeveloperModeUnlocked()

    /**
     * Opens this app's platform settings page.
     *
     * @author Dmitriy Moroz
     */
    fun openAppSettings()
}

/**
 * Common fallback used in previews/tests and unsupported targets.
 *
 * @author Dmitriy Moroz
 */
object NoOpSettingsPlatformActions : SettingsPlatformActions {
    override val requiresStoragePermissionForMediaStore = false
    override val supportsBackgroundGeneration = false
    override val supportsConnectionTimeout = false
    override val backgroundGenerationWarningKey = "settings_item_background_generation_warning"

    override suspend fun requestStoragePermission(): Boolean = true

    override suspend fun requestNotificationPermission(): Boolean = true

    override suspend fun getAppCacheBytes(): Long = 0L

    override suspend fun clearAppCache() = Unit

    override suspend fun getAllDownloadedModelsBytes(): Long = 0L

    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long = 0L

    override fun openUrl(url: String) = Unit

    override fun shareLogFile() = Unit

    override fun showDeveloperModeUnlocked() = Unit

    override fun openAppSettings() = Unit
}

@Composable
expect fun rememberSettingsPlatformActions(): SettingsPlatformActions

package com.shifthackz.aisdv1.presentation.screen.settings.platform

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shifthackz.aisdv1.core.common.extensions.openAppSettings
import com.shifthackz.aisdv1.core.common.extensions.openUrl
import com.shifthackz.aisdv1.core.common.extensions.showToast
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.utils.PermissionUtil
import com.shifthackz.aisdv1.presentation.utils.ReportProblemEmailComposer
import java.io.File
import kotlinx.coroutines.CompletableDeferred

/**
 * Creates Android-specific settings actions for permissions, links, logs, and usage summaries.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberSettingsPlatformActions(): SettingsPlatformActions {
    val context = LocalContext.current
    val storagePermissionResult = remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }
    val notificationPermissionResult = remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        storagePermissionResult.value?.complete(!result.values.any { !it })
        storagePermissionResult.value = null
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionResult.value?.complete(granted)
        notificationPermissionResult.value = null
    }

    return remember(context, storagePermissionLauncher, notificationPermissionLauncher) {
        AndroidSettingsPlatformActions(
            context = context,
            launchStoragePermission = storagePermissionLauncher::launch,
            launchNotificationPermission = notificationPermissionLauncher::launch,
            storagePermissionResult = storagePermissionResult,
            notificationPermissionResult = notificationPermissionResult,
        )
    }
}

/**
 * Android bridge used by Settings for permissions plus cache/model byte summaries.
 *
 * @param context Android context used for permissions, app-private storage, links, and toasts.
 * @param launchStoragePermission Launcher for Android storage/media runtime permissions.
 * @param launchNotificationPermission Launcher for Android notification runtime permission.
 * @param storagePermissionResult Pending storage permission continuation.
 * @param notificationPermissionResult Pending notification permission continuation.
 *
 * @author Dmitriy Moroz
 */
private class AndroidSettingsPlatformActions(
    private val context: Context,
    private val launchStoragePermission: (Array<String>) -> Unit,
    private val launchNotificationPermission: (String) -> Unit,
    private val storagePermissionResult: MutableState<CompletableDeferred<Boolean>?>,
    private val notificationPermissionResult: MutableState<CompletableDeferred<Boolean>?>,
) : SettingsPlatformActions {

    override val requiresStoragePermissionForMediaStore: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2

    override val supportsBackgroundGeneration: Boolean = true
    override val supportsConnectionTimeout: Boolean = true
    override val backgroundGenerationWarningKey = "settings_item_background_generation_warning"

    override suspend fun requestStoragePermission(): Boolean {
        if (PermissionUtil.checkStoragePermission(context)) return true

        val result = CompletableDeferred<Boolean>()
        storagePermissionResult.value = result
        var launched = false
        val granted = PermissionUtil.checkStoragePermission(context) { permissions ->
            launched = true
            launchStoragePermission(permissions)
        }
        if (granted) {
            storagePermissionResult.value = null
            return true
        }
        if (!launched) {
            storagePermissionResult.value = null
            return false
        }
        return result.await()
    }

    override suspend fun requestNotificationPermission(): Boolean {
        if (PermissionUtil.checkNotificationPermission(context)) return true

        val result = CompletableDeferred<Boolean>()
        notificationPermissionResult.value = result
        var launched = false
        val granted = PermissionUtil.checkNotificationPermission(context) { permission ->
            launched = true
            launchNotificationPermission(permission)
        }
        if (granted) {
            notificationPermissionResult.value = null
            return true
        }
        if (!launched) {
            notificationPermissionResult.value = null
            return false
        }
        return result.await()
    }

    override suspend fun getAppCacheBytes(): Long = context.cacheDir.directorySize()

    override suspend fun clearAppCache() {
        context.cacheDir.listFiles()?.forEach { file ->
            runCatching { file.deleteRecursively() }
        }
    }

    override suspend fun getAllDownloadedModelsBytes(): Long =
        File(context.filesDir, LOCAL_MODEL_DIRECTORY).directorySize()

    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long {
        val modelDir = File(context.filesDir, LOCAL_MODEL_DIRECTORY)
        return modelIds
            .distinct()
            .sumOf { id -> File(modelDir, id).directorySize() }
    }

    override fun openUrl(url: String) {
        context.openUrl(url)
    }

    override fun shareLogFile() {
        ReportProblemEmailComposer().invoke(context)
    }

    override fun showDeveloperModeUnlocked() {
        context.showToast(Localization.string("debug_action_unlock"))
    }

    override fun openAppSettings() {
        context.openAppSettings()
    }
}


/**
 * Recursively calculates file or directory byte size, returning zero for missing paths.
 *
 * @receiver File or directory inside app-private storage.
 *
 * @author Dmitriy Moroz
 */
private fun File.directorySize(): Long {
    if (!exists()) return 0L
    if (isFile) return length()
    return listFiles()
        ?.sumOf { child -> child.directorySize() }
        ?: 0L
}

private const val LOCAL_MODEL_DIRECTORY = "model"

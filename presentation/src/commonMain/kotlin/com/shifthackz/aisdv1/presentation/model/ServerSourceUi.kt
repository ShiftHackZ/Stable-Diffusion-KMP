package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness

/**
 * Returns whether the provider should be selectable on the current platform.
 */
internal fun ServerSource.isAvailableOn(platform: Platform): Boolean =
    platform in allowedPlatforms

/**
 * Picks the user-facing readiness badge for the current platform.
 */
internal fun ServerSource.readinessFor(platform: Platform): ServerSourceReadiness =
    readiness[platform]

/**
 * Full provider name for setup and source selection screens.
 *
 * Bonsai keeps one domain source id for persistence, but the label is platform
 * aware: iOS shows the Silicon Diffusion wording and Android shows Local
 * Diffusion while preserving the same underlying provider configuration.
 */
internal fun ServerSource.displayName(platform: Platform): String = when (this) {
    ServerSource.AUTOMATIC1111 -> Localization.string("srv_type_own")
    ServerSource.SWARM_UI -> Localization.string("srv_type_swarm_ui")
    ServerSource.HORDE -> Localization.string("srv_type_horde")
    ServerSource.HUGGING_FACE -> Localization.string("srv_type_hugging_face")
    ServerSource.OPEN_AI_COMPATIBLE -> Localization.string("srv_type_open_ai_compatible")
    ServerSource.OPEN_AI_OAUTH -> Localization.string("srv_type_open_ai_oauth")
    ServerSource.OPEN_AI -> Localization.string("srv_type_open_ai")
    ServerSource.STABILITY_AI -> Localization.string("srv_type_stability_ai")
    ServerSource.FAL_AI -> Localization.string("srv_type_fal_ai")
    ServerSource.ARLI_AI -> Localization.string("srv_type_arli_ai")
    ServerSource.LOCAL_MICROSOFT_ONNX -> Localization.string("srv_type_local")
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> Localization.string("srv_type_media_pipe")
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> Localization.string("srv_type_sdxl")
    ServerSource.LOCAL_APPLE_CORE_ML -> "Silicon Diffusion Core ML"
    ServerSource.LOCAL_APPLE_BONSAI -> Localization.string(
        when (platform) {
            Platform.ANDROID -> "srv_type_bonsai_android"
            Platform.IOS -> "srv_type_bonsai"
        },
    )
}

/**
 * Short provider label used where the full setup name would be too long.
 */
internal fun ServerSource.shortDisplayName(): String = when (this) {
    ServerSource.AUTOMATIC1111 -> Localization.string("srv_type_own_short")
    ServerSource.SWARM_UI -> Localization.string("srv_type_swarm_ui")
    ServerSource.HORDE -> Localization.string("srv_type_horde_short")
    ServerSource.HUGGING_FACE -> Localization.string("srv_type_hugging_face_short")
    ServerSource.OPEN_AI_COMPATIBLE -> Localization.string("srv_type_open_ai_compatible")
    ServerSource.OPEN_AI_OAUTH -> Localization.string("srv_type_open_ai_oauth")
    ServerSource.OPEN_AI -> Localization.string("srv_type_open_ai")
    ServerSource.STABILITY_AI -> Localization.string("srv_type_stability_ai")
    ServerSource.FAL_AI -> Localization.string("srv_type_fal_ai")
    ServerSource.ARLI_AI -> Localization.string("srv_type_arli_ai")
    ServerSource.LOCAL_MICROSOFT_ONNX -> Localization.string("srv_type_local_short")
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> Localization.string("srv_type_media_pipe_short")
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> Localization.string("srv_type_sdxl_short")
    ServerSource.LOCAL_APPLE_CORE_ML -> "Core ML"
    ServerSource.LOCAL_APPLE_BONSAI -> Localization.string("srv_type_bonsai_short")
}

/**
 * Compact label for chips and small controls.
 */
internal fun ServerSource.compactDisplayName(platform: Platform): String = when (this) {
    ServerSource.LOCAL_MICROSOFT_ONNX,
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
    ServerSource.LOCAL_APPLE_CORE_ML,
    -> shortDisplayName()

    else -> displayName(platform)
}

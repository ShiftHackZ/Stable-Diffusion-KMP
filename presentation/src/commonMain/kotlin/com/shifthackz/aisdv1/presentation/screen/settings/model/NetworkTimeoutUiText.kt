package com.shifthackz.aisdv1.presentation.screen.settings.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout

/**
 * Formats either connection or generation limits for Settings in the active language.
 *
 * @receiver Persisted timeout duration choice.
 * @author Dmitriy Moroz
 */
internal fun NetworkTimeout.label(): String = when (this) {
    NetworkTimeout.NO_LIMIT -> Localization.string("settings_generation_timeout_unlimited")
    else -> Localization.string("settings_generation_timeout_minutes", seconds / 60)
}

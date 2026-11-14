package com.shifthackz.aisdv1.presentation.screen.settings.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.GenerationRequestTimeout

/**
 * Formats a generation waiting limit for the Settings row and picker in the active language.
 *
 * @receiver Persisted generation timeout choice.
 * @author Dmitriy Moroz
 */
internal fun GenerationRequestTimeout.label(): String = when (this) {
    GenerationRequestTimeout.NO_LIMIT -> Localization.string("settings_generation_timeout_unlimited")
    else -> Localization.string("settings_generation_timeout_minutes", seconds / 60)
}

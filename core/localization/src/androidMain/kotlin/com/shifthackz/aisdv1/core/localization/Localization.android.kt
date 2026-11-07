package com.shifthackz.aisdv1.core.localization

import java.util.Locale

/**
 * Returns the Android flag drawable associated with a language in the localization catalog.
 *
 * @param code ISO 639-1 language code used by the localization catalog.
 * @return Flag drawable resource ID, or `null` when no flag is available for the code.
 * @author Dmitriy Moroz
 */
internal actual fun countryFlagDrawableResId(code: String): Int? = when (code) {
    "en" -> R.drawable.gb
    "uk" -> R.drawable.ua
    "tr" -> R.drawable.tr
    "ru" -> R.drawable.ru
    "zh" -> R.drawable.cn
    "de" -> R.drawable.de
    else -> null
}

/**
 * Executes the `platformLanguageCode` step in the SDAI localization layer.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformLanguageCode(): String? = Locale.getDefault().language

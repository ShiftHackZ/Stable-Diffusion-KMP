package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure

/**
 * Selects safe localized guidance without rendering token-bearing server responses.
 * @author Dmitriy Moroz
 */
internal fun OpenAiOAuthFailure.localizedDescription(): String = Localization.string(when {
    category == "LOGIN_REQUIRED" || statusCode == 401 -> "open_ai_oauth_login_required"
    category == "LOGIN_UNAVAILABLE" -> "open_ai_oauth_unavailable"
    category == "LOGIN_EXPIRED" -> "open_ai_oauth_expired"
    category == "IDENTITY_CHANGED" -> "open_ai_oauth_identity_changed"
    category == "STORAGE" -> "open_ai_oauth_storage_error"
    statusCode == 429 -> "open_ai_compatible_http_rate"
    else -> "open_ai_oauth_error"
})

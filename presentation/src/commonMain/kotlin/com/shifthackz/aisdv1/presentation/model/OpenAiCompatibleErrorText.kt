package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure

/**
 * Explains status failures without exposing arbitrary server response text.
 *
 * @author Dmitriy Moroz
 */
internal fun OpenAiCompatibleHttpFailure.localizedDescription(): String = when (statusCode) {
    401, 403 -> Localization.string("open_ai_compatible_http_auth")
    429 -> Localization.string("open_ai_compatible_http_rate")
    400, 404, 405, 422 -> Localization.string("open_ai_compatible_http_request")
    in 500..599 -> Localization.string("open_ai_compatible_http_server")
    else -> Localization.string("open_ai_compatible_http_error", statusCode)
}

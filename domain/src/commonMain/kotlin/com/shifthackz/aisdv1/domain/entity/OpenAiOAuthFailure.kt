package com.shifthackz.aisdv1.domain.entity

/**
 * Safe OAuth failure category mapped to localized application copy.
 * Never carries provider response bodies, credential values or credential-bearing cause chains.
 * @param category Stable bundled-feature failure category, such as LOGIN_REQUIRED or STORAGE.
 * @param statusCode Optional HTTP status used to distinguish safe provider failure categories.
 * @author Dmitriy Moroz
 */
class OpenAiOAuthFailure(
    val category: String,
    val statusCode: Int? = null,
) : Exception(category)

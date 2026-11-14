package com.shifthackz.aisdv1.domain.entity

/**
 * Safe server failure translated into localized guidance by the presentation layer.
 *
 * @param statusCode HTTP status; raw server bodies are never included.
 * @author Dmitriy Moroz
 */
class OpenAiCompatibleHttpFailure(val statusCode: Int) : IllegalStateException("Image server request failed (HTTP $statusCode)")

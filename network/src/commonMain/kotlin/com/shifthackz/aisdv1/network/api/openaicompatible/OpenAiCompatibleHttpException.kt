package com.shifthackz.aisdv1.network.api.openaicompatible

/**
 * HTTP failure without server response text or credential-bearing URLs.
 *
 * @param statusCode Status used by the data layer to create a domain failure.
 * @author Dmitriy Moroz
 */
class OpenAiCompatibleHttpException(val statusCode: Int) : IllegalStateException("Image server request failed (HTTP $statusCode)")

package com.shifthackz.aisdv1.domain.entity

/**
 * Image transport or decoding failed without retaining URL-bearing messages or causes.
 * Presentation can safely display localized guidance and workers can log this exception.
 *
 * @author Dmitriy Moroz
 */
class OpenAiCompatibleImageFailure : IllegalStateException("Unable to receive or decode the image")

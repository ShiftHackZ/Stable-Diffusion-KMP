package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Image-generation response; SDAI consumes the first item only.
 *
 * @param data Returned image alternatives.
 * @author Dmitriy Moroz
 */
@Serializable
data class OpenAiCompatibleResponse(val data: List<OpenAiCompatibleImage> = emptyList())

/**
 * Image bytes or a remotely downloadable image.
 *
 * @param base64 Preferred Base64 image payload.
 * @param url Image download URL, fetched without provider credentials.
 * @author Dmitriy Moroz
 */
@Serializable
data class OpenAiCompatibleImage(
    @SerialName("b64_json") val base64: String? = null,
    val url: String? = null,
)

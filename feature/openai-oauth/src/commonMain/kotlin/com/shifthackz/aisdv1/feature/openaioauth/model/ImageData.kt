package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Image bytes supplied by the upstream image endpoint.
 * @param base64 Base64 image validated by the application platform decoder.
 * @author Dmitriy Moroz
 */
@Serializable
internal class ImageData(@SerialName("b64_json") val base64: String)

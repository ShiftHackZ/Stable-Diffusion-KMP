package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.Serializable

/**
 * Base64 image response; arbitrary download URLs are intentionally unsupported.
 * @param data Generated image data; only the first item is consumed.
 * @author Dmitriy Moroz
 */
@Serializable
internal class ImageResponse(val data: List<ImageData>)

package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.Serializable

/**
 * Minimal single-image request sent directly to the fixed Codex image endpoint.
 * @param prompt User's image description.
 * @param size Requested pixel dimensions formatted as WIDTHxHEIGHT.
 * @param model Image model supported by the OAuth backend.
 * @param n One image per request; application batches are sequential.
 * @author Dmitriy Moroz
 */
@Serializable
internal class ImageRequest(
    val prompt: String,
    val size: String,
    val model: String = "gpt-image-2",
    val n: Int = 1,
)

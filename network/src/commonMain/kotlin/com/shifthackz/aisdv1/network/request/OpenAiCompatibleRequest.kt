package com.shifthackz.aisdv1.network.request

import kotlinx.serialization.Serializable

/**
 * Minimal image-generation contract; optional provider-specific controls are omitted.
 *
 * @param model Arbitrary model identifier accepted by the server.
 * @param prompt User's image description.
 * @param size Positive pixel dimensions encoded as WIDTHxHEIGHT.
 * @param n Always one; SDAI performs batches sequentially.
 * @author Dmitriy Moroz
 */
@Serializable
data class OpenAiCompatibleRequest(
    val model: String,
    val prompt: String,
    val size: String,
    val n: Int = 1,
)

package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.network.request.OpenAiCompatibleRequest

/**
 * Maps only interoperable generation fields, with one image requested per call.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImagePayload.toOpenAiCompatibleRequest(): OpenAiCompatibleRequest {
    require(openAiCompatibleModel.isNotBlank()) { "Image model is required" }
    require(width > 0 && height > 0) { "Image dimensions must be positive" }
    return OpenAiCompatibleRequest(model = openAiCompatibleModel.trim(), prompt = prompt, size = "${width}x${height}")
}

/**
 * Records only parameters sent to this API and the dimensions of the decoded image.
 * Unsupported Stable Diffusion fields stay neutral rather than inheriting hidden form values.
 *
 * @param image Canonical Base64-encoded validated image bytes.
 * @param width Actual decoded pixel width.
 * @param height Actual decoded pixel height.
 * @param createdAtMillis Creation timestamp in milliseconds.
 * @author Dmitriy Moroz
 */
internal fun TextToImagePayload.toOpenAiCompatibleResult(
    image: String,
    width: Int,
    height: Int,
    createdAtMillis: Long,
): AiGenerationResult = AiGenerationResult(
    id = 0L,
    image = image,
    inputImage = "",
    createdAt = createdAtMillis,
    type = AiGenerationResult.Type.TEXT_TO_IMAGE,
    denoisingStrength = 0f,
    prompt = prompt,
    negativePrompt = "",
    width = width,
    height = height,
    samplingSteps = 0,
    cfgScale = 0f,
    restoreFaces = false,
    sampler = "",
    seed = "",
    subSeed = "",
    subSeedStrength = 0f,
    hidden = false,
    modelName = openAiCompatibleModel.trim(),
)

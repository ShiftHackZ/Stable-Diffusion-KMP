package com.shifthackz.aisdv1.domain.entity

/**
 * Generated image, metadata and transient guards used by gallery persistence.
 * @param openAiOAuthIdentity Original OAuth account/revision for an unsaved result; not persisted in gallery rows.
 * @param providerSelectionRevision Source-selection revision captured during OAuth generation; null for other providers.
 *
 * @author Dmitriy Moroz
 */
data class AiGenerationResult(
    /**
     * Exposes the `id` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val id: Long,
    /**
     * Exposes the `image` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val image: String,
    /**
     * Exposes the `inputImage` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val inputImage: String,
    /**
     * Exposes the `createdAt` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val createdAt: Long,
    /**
     * Exposes the `type` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val type: Type,
    /**
     * Exposes the `prompt` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val prompt: String,
    /**
     * Exposes the `negativePrompt` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val negativePrompt: String,
    /**
     * Exposes the `width` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val width: Int,
    /**
     * Exposes the `height` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val height: Int,
    /**
     * Exposes the `samplingSteps` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val samplingSteps: Int,
    /**
     * Exposes the `cfgScale` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val cfgScale: Float,
    /**
     * Exposes the `restoreFaces` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val restoreFaces: Boolean,
    /**
     * Exposes the `sampler` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val sampler: String,
    /**
     * Exposes the `seed` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val seed: String,
    /**
     * Exposes the `subSeed` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val subSeed: String,
    /**
     * Exposes the `subSeedStrength` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val subSeedStrength: Float,
    /**
     * Exposes the `denoisingStrength` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val denoisingStrength: Float,
    /**
     * Exposes the `hidden` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val hidden: Boolean,
    /**
     * Exposes the `liked` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val liked: Boolean = false,
    /**
     * Exposes the `modelName` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    val modelName: String = "",
    /** Transient identity guard for unsaved OAuth results. Gallery storage deliberately omits it.
     * @author Dmitriy Moroz */
    val openAiOAuthIdentity: OpenAiOAuthIdentity? = null,
    /** Transient source-selection guard for an unsaved OAuth result; gallery rows omit this field.
     * @author Dmitriy Moroz */
    val providerSelectionRevision: Long? = null,
) {
    /**
     * Coordinates `Type` behavior in the SDAI domain layer.
     *
     * @param key key value consumed by the API.
     * @author Dmitriy Moroz
     */
    enum class Type(val key: String) {
        TEXT_TO_IMAGE("txt2img"),
        IMAGE_TO_IMAGE("img2img");

        /**
         * Provides the `companion object` singleton used by the SDAI domain layer.
         *
         * @author Dmitriy Moroz
         */
        companion object {
            /**
             * Executes the `parse` step in the SDAI domain layer.
             *
             * @param input input value consumed by the API.
             * @author Dmitriy Moroz
             */
            fun parse(input: String?) = entries
                .find { it.key == input } ?: TEXT_TO_IMAGE
        }
    }
}

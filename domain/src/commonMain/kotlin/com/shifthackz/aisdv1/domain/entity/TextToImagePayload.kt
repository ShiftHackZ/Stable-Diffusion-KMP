package com.shifthackz.aisdv1.domain.entity

/**
 * Provider-neutral request model for text-to-image generation.
 *
 * The payload intentionally carries superset fields from remote APIs and local
 * runtimes. Repository and feature implementations pick the fields relevant to
 * their provider, including Android local runtime backend choices such as SDXL
 * and Bonsai.
 * @param sourceSnapshot Source selected at submission; null preserves legacy queued-work routing.
 * @param providerSelectionRevision Durable source-selection revision; mandatory for native OAuth work.
 * @param openAiOAuthAccountId Token-free ChatGPT account identifier captured at submission.
 * @param openAiOAuthRevision Stable protected-session revision; replacement logins invalidate earlier work.
 * @author Dmitriy Moroz
 */
data class TextToImagePayload(
    val prompt: String,
    val negativePrompt: String,
    val samplingSteps: Int,
    val cfgScale: Float,
    val width: Int,
    val height: Int,
    val restoreFaces: Boolean,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: Float,
    val sampler: String,
    val scheduler: Scheduler = Scheduler.AUTOMATIC,
    val nsfw: Boolean,
    val batchCount: Int,
    val style: String?,
    val quality: String?,
    val openAiModel: OpenAiModel?,
    val stabilityAiClipGuidance: StabilityAiClipGuidance?,
    val stabilityAiStylePreset: StabilityAiStylePreset?,
    val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    val hires: HiresConfig = HiresConfig.DISABLED,
    val forgeModules: List<ForgeModule> = emptyList(),
    val falAiModel: FalAiModel = FalAiModel.defaultTextToImage,
    val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    val sdxlBackend: SdxlBackend = SdxlBackend.AUTO,
    val bonsaiBackend: BonsaiBackend = BonsaiBackend.AUTO,
    val falAiSyncMode: Boolean = false,
    val arliAiModel: String = "",
    /**
     * Arbitrary image model ID used only by OPEN_AI_COMPATIBLE.
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    /** Selected source at submission; null preserves legacy queued-work routing.
     * @author Dmitriy Moroz */
    val sourceSnapshot: ServerSource? = null,
    /** Durable provider selection revision captured with this work; null marks an older payload.
     * @author Dmitriy Moroz */
    val providerSelectionRevision: Long? = null,
    /** Token-free account identifier captured when OAuth work is submitted.
     * @author Dmitriy Moroz */
    val openAiOAuthAccountId: String? = null,
    /** Stable login revision; stale queued work must never use a replacement session.
     * @author Dmitriy Moroz */
    val openAiOAuthRevision: String? = null,
)

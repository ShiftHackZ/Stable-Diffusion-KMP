package com.shifthackz.aisdv1.work.mappers

import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * JSON codec for background txt2img payload handoff.
 *
 * Unknown keys stay ignored so queued work from an older app version can still
 * be read after new provider fields, such as local runtime backend choices, are
 * added to the DTO.
 */
@OptIn(ExperimentalSerializationApi::class)
internal val payloadJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Serializes the domain payload for WorkManager cache storage.
 */
internal fun TextToImagePayload.toByteArray(): ByteArray {
    return payloadJson
        .encodeToString(TextToImagePayloadDto.from(this))
        .encodeToByteArray()
}

/**
 * Restores a cached WorkManager payload into the domain request model.
 */
internal fun ByteArray.toTextToImagePayload(): TextToImagePayload? {
    return runCatching {
        payloadJson
            .decodeFromString<TextToImagePayloadDto>(decodeToString())
            .toPayload()
    }.getOrNull()
}

/**
 * Stable serialized form for txt2img background work.
 *
 * The DTO stores provider-specific options as strings so enum aliases can be
 * parsed with backwards-compatible defaults when app versions change.
 */
@Serializable
private data class TextToImagePayloadDto(
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
    val scheduler: String? = null,
    val nsfw: Boolean,
    val batchCount: Int,
    val style: String?,
    val quality: String?,
    val openAiModel: String?,
    /**
     * Arbitrary compatible-server model retained across the WorkManager handoff.
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    val stabilityAiClipGuidance: String?,
    val stabilityAiStylePreset: String?,
    val aDetailer: ADetailerConfigDto = ADetailerConfigDto(),
    val hires: HiresConfigDto = HiresConfigDto(),
    val forgeModules: List<ForgeModuleDto> = emptyList(),
    val falAiModel: String? = null,
    val falAiImageSize: String? = null,
    val falAiAcceleration: String? = null,
    val sdxlBackend: String? = null,
    val bonsaiBackend: String? = null,
    val falAiSyncMode: Boolean = false,
) {
    fun toPayload(): TextToImagePayload = TextToImagePayload(
        prompt = prompt,
        negativePrompt = negativePrompt,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        width = width,
        height = height,
        restoreFaces = restoreFaces,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength,
        sampler = sampler,
        scheduler = scheduler?.let(Scheduler::fromAlias) ?: Scheduler.AUTOMATIC,
        nsfw = nsfw,
        batchCount = batchCount,
        style = style,
        quality = quality,
        openAiModel = openAiModel?.let(OpenAiModel::parse),
        openAiCompatibleModel = openAiCompatibleModel,
        stabilityAiClipGuidance = stabilityAiClipGuidance.parseEnumOrNull<StabilityAiClipGuidance>(),
        stabilityAiStylePreset = stabilityAiStylePreset.parseEnumOrNull<StabilityAiStylePreset>(),
        aDetailer = aDetailer.toDomain(),
        hires = hires.toDomain(),
        forgeModules = forgeModules.map(ForgeModuleDto::toDomain),
        falAiModel = FalAiModel.parse(falAiModel, FalAiModel.defaultTextToImage),
        falAiImageSize = FalAiImageSize.parse(falAiImageSize),
        falAiAcceleration = FalAiAcceleration.parse(falAiAcceleration),
        sdxlBackend = SdxlBackend.parse(sdxlBackend),
        bonsaiBackend = BonsaiBackend.parse(bonsaiBackend),
        falAiSyncMode = falAiSyncMode,
    )

    companion object {
        fun from(payload: TextToImagePayload): TextToImagePayloadDto = TextToImagePayloadDto(
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            width = payload.width,
            height = payload.height,
            restoreFaces = payload.restoreFaces,
            seed = payload.seed,
            subSeed = payload.subSeed,
            subSeedStrength = payload.subSeedStrength,
            sampler = payload.sampler,
            scheduler = payload.scheduler.alias,
            nsfw = payload.nsfw,
            batchCount = payload.batchCount,
            style = payload.style,
            quality = payload.quality,
            openAiModel = payload.openAiModel?.alias,
            openAiCompatibleModel = payload.openAiCompatibleModel,
            stabilityAiClipGuidance = payload.stabilityAiClipGuidance?.name,
            stabilityAiStylePreset = payload.stabilityAiStylePreset?.name,
            aDetailer = ADetailerConfigDto.from(payload.aDetailer),
            hires = HiresConfigDto.from(payload.hires),
            forgeModules = payload.forgeModules.map(ForgeModuleDto::from),
            falAiModel = payload.falAiModel.alias,
            falAiImageSize = payload.falAiImageSize.key,
            falAiAcceleration = payload.falAiAcceleration.key,
            sdxlBackend = payload.sdxlBackend.key,
            bonsaiBackend = payload.bonsaiBackend.key,
            falAiSyncMode = payload.falAiSyncMode,
        )
    }
}

/**
 * Parses enum names from cached payload fields while keeping unknown values nullable.
 */
internal inline fun <reified T : Enum<T>> String?.parseEnumOrNull(): T? {
    return this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
}

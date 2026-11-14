package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ArliAiSampler
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler
import com.shifthackz.aisdv1.presentation.model.GenerationModal

/**
 * Exposes the `MIN_STEPS` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_STEPS = 1
/**
 * Exposes the `MAX_STEPS` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_STEPS = 150
/**
 * Exposes the `MIN_BATCH_COUNT` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_BATCH_COUNT = 1
/**
 * Exposes the `MAX_BATCH_COUNT` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_BATCH_COUNT = 20
/**
 * Exposes the `MIN_CFG_SCALE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_CFG_SCALE = 1f
/**
 * Exposes the `MAX_CFG_SCALE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_CFG_SCALE = 35f
/**
 * Exposes the `MIN_SUB_SEED_STRENGTH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_SUB_SEED_STRENGTH = 0f
/**
 * Exposes the `MAX_SUB_SEED_STRENGTH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_SUB_SEED_STRENGTH = 1f
/**
 * Exposes the `MIN_DENOISING_STRENGTH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_DENOISING_STRENGTH = 0f
/**
 * Exposes the `MAX_DENOISING_STRENGTH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_DENOISING_STRENGTH = 1f

/**
 * Carries `StableDiffusionSamplersKey` data through the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal data class StableDiffusionSamplersKey(
    /**
     * Exposes the `serverUrl` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String,
    /**
     * Exposes the `demoMode` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean,
)

/**
 * Executes the `progressModal` step in the SDAI presentation layer.
 *
 * @param canCancelLocalGeneration can cancel local generation value consumed by the API.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.progressModal(
    canCancelLocalGeneration: Boolean,
): GenerationModal = if (
    mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
    mode == ServerSource.LOCAL_GOOGLE_MEDIA_PIPE ||
    mode == ServerSource.LOCAL_APPLE_CORE_ML
) {
    GenerationModal.Generating(
        title = if (mode == ServerSource.LOCAL_APPLE_CORE_ML) {
            Localization.string("communicating_core_ml_title").asUiText()
        } else {
            null
        },
        canCancel = canCancelLocalGeneration,
    )
} else {
    GenerationModal.Communicating()
}

/**
 * Converts SDAI data with `toImageToImageResultModal`.
 *
 * @param autoSaveEnabled auto save enabled value consumed by the API.
 * @param reportEnabled report enabled value consumed by the API.
 * @return Result produced by `toImageToImageResultModal`.
 * @author Dmitriy Moroz
 */
internal fun List<AiGenerationResult>.toImageToImageResultModal(
    autoSaveEnabled: Boolean,
    reportEnabled: Boolean,
): GenerationModal =
    takeIf(List<AiGenerationResult>::isNotEmpty)
        ?.let {
            GenerationModal.Image.create(
                list = it,
                autoSaveEnabled = autoSaveEnabled,
                reportEnabled = reportEnabled,
            )
        }
        ?: GenerationModal.Error(Localization.string("error_invalid").asUiText())

/**
 * Executes the `validated` step in the SDAI presentation layer.
 *
 * @param dimensionValidator dimension validator value consumed by the API.
 * @return Result produced by `validated`.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.validated(
    dimensionValidator: DimensionValidator,
): ImageToImageState {
    val widthResult = dimensionValidator(width)
    val heightResult = dimensionValidator(height)
    val sourceError = when {
        mode == ServerSource.OPEN_AI_COMPATIBLE -> Localization.string("open_ai_compatible_img2img_unsupported")
        !sourceSupportsImageToImage && mode == ServerSource.OPEN_AI ->
            Localization.string("error_img2img_openai_unsupported")
        !sourceSupportsImageToImage ->
            Localization.string("error_img2img_local_android_only")
        mode == ServerSource.ARLI_AI && arliAiModel.isBlank() ->
            Localization.string("error_invalid")
        imageBase64.isBlank() ->
            Localization.string("error_img2img_select_input")
        else -> null
    }
    return copy(
        promptValidationError = null,
        widthValidationError = widthResult.errorText(),
        heightValidationError = heightResult.errorText(),
        error = sourceError,
    )
}

/**
 * Executes the `function` step in the SDAI presentation layer.
 *
 * @return Result produced by `function`.
 * @author Dmitriy Moroz
 */
internal fun ValidationResult<DimensionValidator.Error>.errorText(): UiText? {
    if (isValid) return null
    return UiText.Static(
        when (val error = validationError) {
            DimensionValidator.Error.Empty -> "Required"
            is DimensionValidator.Error.LessThanMinimum -> "Minimum ${error.min}px"
            is DimensionValidator.Error.BiggerThanMaximum -> "Maximum ${error.max}px"
            DimensionValidator.Error.Unexpected,
            null,
            -> "Invalid value"
        },
    )
}

/**
 * Executes the `withSettings` step in the SDAI presentation layer.
 *
 * @param settings settings value consumed by the API.
 * @param stableDiffusionSamplers stable diffusion samplers value consumed by the API.
 * @return Result produced by `withSettings`.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.withSettings(
    settings: Settings,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): ImageToImageState =
    copy(arliAiModel = settings.arliAiModel)
        .withSource(settings.source, stableDiffusionSamplers, forgeModules, aDetailerAvailable, arliAiModels)
        .copy(
        advancedToggleButtonVisible = !settings.formAdvancedOptionsAlwaysShow,
        advancedOptionsVisible = if (settings.formAdvancedOptionsAlwaysShow) {
            true
        } else {
            advancedOptionsVisible
        },
        formPromptTaggedInput = settings.formPromptTaggedInput,
    )

/**
 * Executes the `withSource` step in the SDAI presentation layer.
 *
 * @param source source value consumed by the API.
 * @param stableDiffusionSamplers stable diffusion samplers value consumed by the API.
 * @param forgeModules forge modules value consumed by the API.
 * @return Result produced by `withSource`.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.withSource(
    source: ServerSource,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): ImageToImageState {
    val samplers = when (source) {
        ServerSource.STABILITY_AI -> StabilityAiSampler.entries.map { "$it" }
        ServerSource.ARLI_AI -> ArliAiSampler.supported
        else -> stableDiffusionSamplers.orEmpty()
    }
    val modules = forgeModules.orEmpty().takeIf {
        source == ServerSource.AUTOMATIC1111
    }.orEmpty()
    val arliModels = arliAiModels.orEmpty()
    return copy(
        mode = source,
        availableSamplers = samplers,
        selectedSampler = selectedSampler.takeIf { sampler -> samplers.contains(sampler) }
            ?: samplers.firstOrNull()
            ?: selectedSampler,
        availableForgeModules = modules,
        selectedForgeModules = selectedForgeModules.filter(modules::contains),
        arliAiModels = arliModels.takeIf { source == ServerSource.ARLI_AI }.orEmpty(),
        arliAiModel = if (source == ServerSource.ARLI_AI) {
            arliAiModel.takeIf(arliModels::contains)
                ?: arliModels.firstOrNull()
                ?: arliAiModel
        } else {
            arliAiModel
        },
        aDetailerAvailable = if (source == ServerSource.AUTOMATIC1111) {
            aDetailerAvailable ?: this.aDetailerAvailable
        } else {
            true
        },
        aDetailerRefreshing = if (source == ServerSource.AUTOMATIC1111) {
            aDetailerRefreshing
        } else {
            false
        },
    )
}

/**
 * Executes the `localizedMessageText` step in the SDAI presentation layer.
 *
 * @return Result produced by `localizedMessageText`.
 * @author Dmitriy Moroz
 */
internal fun Throwable.localizedMessageText(): UiText {
    val message = message
    val text = when {
        message == null -> Localization.string("error_invalid")
        message.contains("out of memory", ignoreCase = true) ||
            message.contains("OutOfMemoryError", ignoreCase = true) ->
            Localization.string("error_generation_out_of_memory")
        else -> message
    }
    return UiText.Static(text)
}

/**
 * Executes the `localizedMessageString` step in the SDAI presentation layer.
 *
 * @return Result produced by `localizedMessageString`.
 * @author Dmitriy Moroz
 */
internal fun Throwable.localizedMessageString(): String {
    val message = message
    return when {
        message == null -> Localization.string("error_invalid")
        message.contains("out of memory", ignoreCase = true) ||
            message.contains("OutOfMemoryError", ignoreCase = true) ->
            Localization.string("error_generation_out_of_memory")
        else -> message
    }
}

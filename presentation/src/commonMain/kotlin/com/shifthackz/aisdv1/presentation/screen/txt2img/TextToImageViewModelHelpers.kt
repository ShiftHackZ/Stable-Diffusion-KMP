package com.shifthackz.aisdv1.presentation.screen.txt2img

import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleImageFailure
import com.shifthackz.aisdv1.presentation.model.localizedDescription
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
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
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
 * Exposes the `BONSAI_DEFAULT_SIZE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val BONSAI_DEFAULT_SIZE = 128
/**
 * Exposes the `BONSAI_SIZE_MULTIPLE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal const val BONSAI_SIZE_MULTIPLE = 32
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
internal fun TextToImageState.progressModal(
    canCancelLocalGeneration: Boolean,
): GenerationModal = when {
    mode == ServerSource.LOCAL_APPLE_CORE_ML -> GenerationModal.Generating(
        title = Localization.string("communicating_core_ml_title").asUiText(),
        canCancel = canCancelLocalGeneration,
    )
    mode == ServerSource.LOCAL_APPLE_BONSAI -> GenerationModal.Generating(
        title = "Bonsai Image".asUiText(),
        canCancel = canCancelLocalGeneration,
    )
    localSourceSelected -> GenerationModal.Generating(canCancel = canCancelLocalGeneration)
    else -> GenerationModal.Communicating()
}

/**
 * Converts SDAI data with `toTextToImageResultModal`.
 *
 * @param autoSaveEnabled auto save enabled value consumed by the API.
 * @param reportEnabled report enabled value consumed by the API.
 * @return Result produced by `toTextToImageResultModal`.
 * @author Dmitriy Moroz
 */
internal fun List<AiGenerationResult>.toTextToImageResultModal(
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
internal fun TextToImageState.validated(
    dimensionValidator: DimensionValidator,
): TextToImageState {
    val compatible = mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH
    val validateDimensions = mode != ServerSource.OPEN_AI && !compatible
    val widthResult = dimensionValidator(width).takeIf { validateDimensions }
    val heightResult = dimensionValidator(height).takeIf { validateDimensions }
    val sourceError = when {
        mode == ServerSource.OPEN_AI_COMPATIBLE && openAiCompatibleModel.isBlank() -> Localization.string("open_ai_compatible_model_required").asUiText()
        mode == ServerSource.ARLI_AI && arliAiModel.isBlank() -> Localization.string("error_invalid").asUiText()
        else -> null
    }
    val validateBonsaiDimensions = mode == ServerSource.LOCAL_APPLE_BONSAI
    return copy(
        promptValidationError = null,
        widthValidationError = widthResult?.errorMessage()
            ?: width.bonsaiSizeError().takeIf { validateBonsaiDimensions }
            ?: Localization.string("open_ai_compatible_positive_size").asUiText().takeIf { compatible && (width.toIntOrNull() ?: 0) <= 0 },
        heightValidationError = heightResult?.errorMessage()
            ?: height.bonsaiSizeError().takeIf { validateBonsaiDimensions }
            ?: Localization.string("open_ai_compatible_positive_size").asUiText().takeIf { compatible && (height.toIntOrNull() ?: 0) <= 0 },
        error = sourceError,
    )
}

/**
 * Executes the `canGenerate` step in the SDAI presentation layer.
 *
 * @return Result produced by `canGenerate`.
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.canGenerate(): Boolean =
    promptValidationError == null &&
        widthValidationError == null &&
        heightValidationError == null &&
        error == null

/**
 * Executes the `function` step in the SDAI presentation layer.
 *
 * @return Result produced by `function`.
 * @author Dmitriy Moroz
 */
internal fun ValidationResult<DimensionValidator.Error>.errorMessage(): UiText? {
    if (isValid) return null
    return UiText.Static(
        when (val error = validationError) {
            DimensionValidator.Error.Empty -> Localization.string("error_empty")
            is DimensionValidator.Error.LessThanMinimum -> Localization.string("error_min_size", error.min)
            is DimensionValidator.Error.BiggerThanMaximum -> Localization.string("error_max_size", error.max)
            DimensionValidator.Error.Unexpected,
            null,
            -> Localization.string("error_invalid")
        },
    )
}

private fun String.bonsaiSizeError(): UiText? {
    val size = toIntOrNull() ?: return null
    if (size % BONSAI_SIZE_MULTIPLE == 0) return null
    return UiText.Static(Localization.string("error_size_multiple", BONSAI_SIZE_MULTIPLE))
}

/**
 * Executes the `withSettings` step in the SDAI presentation layer.
 *
 * @param settings settings value consumed by the API.
 * @param stableDiffusionSamplers stable diffusion samplers value consumed by the API.
 * @return Result produced by `withSettings`.
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.withSettings(
    settings: Settings,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): TextToImageState =
    copy(
        arliAiModel = settings.arliAiModel,
        providerSelectionRevision = settings.providerSelectionRevision,
        screenModal = if (mode == ServerSource.OPEN_AI_OAUTH && providerSelectionRevision != settings.providerSelectionRevision) GenerationModal.None else screenModal,
        results = if (mode == ServerSource.OPEN_AI_OAUTH && providerSelectionRevision != settings.providerSelectionRevision) emptyList() else results,
    )
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
internal fun TextToImageState.withSource(
    source: ServerSource,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): TextToImageState {
    val samplers = when (source) {
        ServerSource.STABILITY_AI -> StabilityAiSampler.entries.map { "$it" }
        ServerSource.ARLI_AI -> ArliAiSampler.supported
        else -> stableDiffusionSamplers.orEmpty()
    }
    val modules = forgeModules.orEmpty().takeIf {
        source == ServerSource.AUTOMATIC1111
    }.orEmpty()
    val arliModels = arliAiModels.orEmpty()
    val switchingToBonsai = source == ServerSource.LOCAL_APPLE_BONSAI &&
        mode != ServerSource.LOCAL_APPLE_BONSAI
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
        width = if (switchingToBonsai) {
            BONSAI_DEFAULT_SIZE.toString()
        } else {
            width
        },
        height = if (switchingToBonsai) {
            BONSAI_DEFAULT_SIZE.toString()
        } else {
            height
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
    this is OpenAiOAuthFailure -> localizedDescription()
        this is OpenAiCompatibleHttpFailure -> localizedDescription()
        this is OpenAiCompatibleImageFailure -> Localization.string("open_ai_compatible_image_failure")
        message == null -> Localization.string("error_invalid")
        message.contains("out of memory", ignoreCase = true) ||
            message.contains("OutOfMemoryError", ignoreCase = true) ->
            Localization.string("error_generation_out_of_memory")
        else -> message
    }
    return UiText.Static(text)
}

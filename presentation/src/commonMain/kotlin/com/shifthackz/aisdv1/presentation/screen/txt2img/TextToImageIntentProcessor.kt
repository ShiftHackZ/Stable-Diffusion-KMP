package com.shifthackz.aisdv1.presentation.screen.txt2img

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.presentation.widget.input.GenerationAspectRatio
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Applies synchronous txt2img UI intents.
 *
 * Navigation, form mutations, and result actions are handled here so the
 * view-model can keep asynchronous generation and configuration loading logic
 * separate from simple state transitions.
 */
internal class TextToImageIntentProcessor(
    private val router: TextToImageRouter,
    private val updateState: (((TextToImageState) -> TextToImageState) -> Unit),
    private val generate: () -> Unit,
    private val cancelGeneration: () -> Unit,
    private val saveImage: (String) -> Unit,
    private val shareImage: (String) -> Unit,
    private val saveGenerationResults: (List<AiGenerationResult>) -> Unit,
    private val viewGenerationResult: (AiGenerationResult) -> Unit,
    private val reportGenerationResult: (AiGenerationResult) -> Unit,
    private val applyGenerationResult: (AiGenerationResult) -> Unit,
) {

    fun process(intent: TextToImageIntent) {
        when (intent) {
            TextToImageIntent.OpenDrawer -> router.openDrawer()
            TextToImageIntent.NavigateBack -> router.navigateBack()
            TextToImageIntent.ConfigureProvider -> router.navigateToServerSetup()
            TextToImageIntent.Generate -> generate()
            TextToImageIntent.RunBenchmarkFromPrompt -> Unit
            TextToImageIntent.SkipBenchmarkPrompt -> Unit
            TextToImageIntent.ContinueAfterBenchmarkWarning -> Unit
            TextToImageIntent.SuppressBenchmarkWarningAndContinue -> Unit
            TextToImageIntent.DismissModal -> updateState { it.copy(screenModal = com.shifthackz.aisdv1.presentation.model.GenerationModal.None) }
            TextToImageIntent.CancelGeneration -> cancelGeneration()
            TextToImageIntent.DismissError -> updateState { it.copy(error = null) }
            TextToImageIntent.DismissMessage -> updateState { it.copy(message = null) }
            TextToImageIntent.DismissEditTag -> updateState { it.copy(editTag = null) }
            is TextToImageIntent.SaveResult -> saveImage(intent.base64)
            is TextToImageIntent.ShareResult -> shareImage(intent.base64)
            is TextToImageIntent.SaveGenerationResults -> saveGenerationResults(intent.results)
            is TextToImageIntent.ViewGenerationResult -> viewGenerationResult(intent.result)
            is TextToImageIntent.ReportGenerationResult -> reportGenerationResult(intent.result)
            is TextToImageIntent.ShowEditTag -> updateState {
                it.copy(
                    editTag = PromptTagEditRequest(
                        prompt = intent.prompt,
                        negativePrompt = intent.negativePrompt,
                        tag = intent.tag,
                        isNegative = intent.isNegative,
                    ),
                )
            }
            is TextToImageIntent.ApplyPrompts -> updateState {
                it.copy(
                    prompt = intent.prompt,
                    negativePrompt = intent.negativePrompt,
                    editTag = null,
                    message = null,
                )
            }
            is TextToImageIntent.ApplyGenerationResult -> applyGenerationResult(intent.ai)
            is TextToImageIntent.UpdateAdvancedOptionsVisibility -> updateState {
                it.copy(advancedOptionsVisible = intent.visible)
            }
            is TextToImageIntent.UpdatePrompt -> updateState {
                it.copy(prompt = intent.value, promptValidationError = null, error = null, message = null)
            }
            is TextToImageIntent.UpdateNegativePrompt -> updateState {
                it.copy(negativePrompt = intent.value, message = null)
            }
            is TextToImageIntent.UpdateWidth -> updateState {
                it.copy(
                    width = intent.value.filter(Char::isDigit),
                    widthValidationError = null,
                    error = null,
                    message = null,
                )
            }
            is TextToImageIntent.UpdateHeight -> updateState {
                it.copy(
                    height = intent.value.filter(Char::isDigit),
                    heightValidationError = null,
                    error = null,
                    message = null,
                )
            }
            TextToImageIntent.SwapDimensions -> updateState {
                it.copy(
                    width = it.height,
                    height = it.width,
                    widthValidationError = null,
                    heightValidationError = null,
                    error = null,
                    message = null,
                )
            }
            is TextToImageIntent.ApplyAspectRatio -> updateState {
                val (width, height) = it.dimensionsFor(intent.ratio)
                it.copy(
                    width = width.toString(),
                    height = height.toString(),
                    widthValidationError = null,
                    heightValidationError = null,
                    error = null,
                    message = null,
                )
            }
            is TextToImageIntent.UpdateSamplingSteps -> updateState {
                val minSteps = it.falAiModel.minInferenceSteps.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MIN_STEPS
                val maxSteps = when (it.mode) {
                    ServerSource.FAL_AI -> it.falAiModel.maxInferenceSteps
                    ServerSource.ARLI_AI -> MAX_ARLI_AI_STEPS
                    ServerSource.LOCAL_APPLE_BONSAI -> GenerationInputFormConstants.SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                    else -> MAX_STEPS
                }
                it.copy(samplingSteps = intent.value.coerceIn(minSteps, maxSteps), message = null)
            }
            is TextToImageIntent.UpdateCfgScale -> updateState {
                val minCfg = it.falAiModel.minGuidanceScale.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MIN_CFG_SCALE
                val maxCfg = it.falAiModel.maxGuidanceScale.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MAX_CFG_SCALE
                it.copy(cfgScale = intent.value.coerceIn(minCfg, maxCfg), message = null)
            }
            is TextToImageIntent.UpdateRestoreFaces -> updateState {
                it.copy(restoreFaces = intent.value, message = null)
            }
            is TextToImageIntent.UpdateSeed -> updateState {
                it.copy(seed = intent.value.filter(Char::isDigit), message = null)
            }
            is TextToImageIntent.UpdateSubSeed -> updateState {
                it.copy(subSeed = intent.value.filter(Char::isDigit), message = null)
            }
            is TextToImageIntent.UpdateSubSeedStrength -> updateState {
                it.copy(
                    subSeedStrength = intent.value.coerceIn(MIN_SUB_SEED_STRENGTH, MAX_SUB_SEED_STRENGTH),
                    message = null,
                )
            }
            is TextToImageIntent.UpdateSampler -> updateState {
                it.copy(selectedSampler = intent.value, message = null)
            }
            is TextToImageIntent.UpdateScheduler -> updateState {
                it.copy(selectedScheduler = intent.value, message = null)
            }
            is TextToImageIntent.UpdateForgeModules -> updateState {
                it.copy(selectedForgeModules = intent.value, message = null)
            }
            is TextToImageIntent.UpdateNsfw -> updateState {
                it.copy(nsfw = intent.value, message = null)
            }
            is TextToImageIntent.UpdateBatchCount -> updateState {
                val maxBatch = if (it.mode == ServerSource.FAL_AI) {
                    MAX_FAL_AI_BATCH_COUNT
                } else {
                    MAX_BATCH_COUNT
                }
                it.copy(batchCount = intent.value.coerceIn(MIN_BATCH_COUNT, maxBatch), message = null)
            }
            is TextToImageIntent.UpdateOpenAiModel -> updateState { state ->
                val size = if (state.openAiSize.supportedModels.contains(intent.value)) {
                    state.openAiSize
                } else {
                    OpenAiSize
                        .entries
                        .first { it.supportedModels.contains(intent.value) }
                }
                state.copy(openAiModel = intent.value, openAiSize = size, message = null)
            }
            is TextToImageIntent.UpdateOpenAiSize -> updateState {
                it.copy(openAiSize = intent.value, message = null)
            }
            is TextToImageIntent.UpdateOpenAiQuality -> updateState {
                it.copy(openAiQuality = intent.value, message = null)
            }
            is TextToImageIntent.UpdateFalAiModel -> updateState {
                val acceleration = it.falAiAcceleration.takeIf(intent.value.supportedAccelerations::contains)
                    ?: intent.value.supportedAccelerations.first()
                it.copy(
                    falAiModel = intent.value,
                    falAiAcceleration = acceleration,
                    samplingSteps = it.samplingSteps.coerceIn(
                        intent.value.minInferenceSteps,
                        intent.value.maxInferenceSteps,
                    ),
                    cfgScale = it.cfgScale.coerceIn(
                        intent.value.minGuidanceScale,
                        intent.value.maxGuidanceScale,
                    ),
                    message = null,
                )
            }
            is TextToImageIntent.UpdateFalAiImageSize -> updateState {
                it.copy(
                    falAiImageSize = intent.value,
                    width = intent.value.width.toString(),
                    height = intent.value.height.toString(),
                    widthValidationError = null,
                    heightValidationError = null,
                    message = null,
                )
            }
            is TextToImageIntent.UpdateFalAiAcceleration -> updateState {
                it.copy(falAiAcceleration = intent.value, message = null)
            }
            is TextToImageIntent.UpdateSdxlBackend -> updateState {
                it.copy(sdxlBackend = intent.value, message = null)
            }
            is TextToImageIntent.UpdateBonsaiBackend -> updateState {
                it.copy(bonsaiBackend = intent.value, message = null)
            }
            is TextToImageIntent.UpdateFalAiSyncMode -> updateState {
                it.copy(falAiSyncMode = intent.value, message = null)
            }
            is TextToImageIntent.UpdateOpenAiCompatibleModel -> updateState {
                it.copy(openAiCompatibleModel = intent.value, message = null, error = null)
            }
            is TextToImageIntent.UpdateArliAiModel -> updateState {
                it.copy(arliAiModel = intent.value, message = null, error = null)
            }
            is TextToImageIntent.UpdateStabilityAiStyle -> updateState {
                it.copy(selectedStylePreset = intent.value, message = null)
            }
            is TextToImageIntent.UpdateStabilityAiClipGuidance -> updateState {
                it.copy(selectedClipGuidancePreset = intent.value, message = null)
            }
            is TextToImageIntent.UpdateHiresConfig -> updateState {
                it.copy(hires = intent.value, message = null)
            }
            is TextToImageIntent.UpdateADetailerConfig -> updateState {
                it.copy(aDetailer = intent.value, message = null)
            }
            TextToImageIntent.RefreshADetailerAvailability -> Unit
            TextToImageIntent.OpenADetailerInstallInstructions -> Unit
        }
    }
}

private fun TextToImageState.dimensionsFor(ratio: GenerationAspectRatio): Pair<Int, Int> {
    val currentWidth = width.toIntOrNull() ?: DEFAULT_SIZE
    val currentHeight = height.toIntOrNull() ?: DEFAULT_SIZE
    val longSide = max(currentWidth, currentHeight).coerceIn(MIN_SIZE, MAX_SIZE)
    return if (ratio.width >= ratio.height) {
        longSide to (longSide * ratio.height.toFloat() / ratio.width)
            .roundToStableDiffusionSize()
    } else {
        (longSide * ratio.width.toFloat() / ratio.height)
            .roundToStableDiffusionSize() to longSide
    }
}

private fun Float.roundToStableDiffusionSize(): Int =
    (this / SIZE_STEP).roundToInt()
        .coerceAtLeast(1)
        .times(SIZE_STEP)
        .coerceIn(MIN_SIZE, MAX_SIZE)

private const val MIN_SIZE = 64
private const val MAX_SIZE = 2048
private const val SIZE_STEP = 64
private const val MAX_FAL_AI_BATCH_COUNT = 4
private const val MAX_ARLI_AI_STEPS = 40

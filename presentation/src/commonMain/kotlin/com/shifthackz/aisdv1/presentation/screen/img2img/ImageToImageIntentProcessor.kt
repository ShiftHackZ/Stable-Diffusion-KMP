package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.presentation.widget.input.GenerationAspectRatio
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Coordinates `ImageToImageIntentProcessor` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
internal class ImageToImageIntentProcessor(
    /**
     * Exposes the `router` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val router: ImageToImageRouter,
    /**
     * Exposes the `updateState` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val updateState: (((ImageToImageState) -> ImageToImageState) -> Unit),
    /**
     * Exposes the `pickImage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val pickImage: (ImageToImagePickSource) -> Unit,
    /**
     * Exposes the `pickRandomImage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val pickRandomImage: () -> Unit,
    /**
     * Exposes the `generate` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val generate: () -> Unit,
    /**
     * Exposes the `cancelGeneration` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val cancelGeneration: () -> Unit,
    /**
     * Exposes the `saveImage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val saveImage: (String) -> Unit,
    /**
     * Exposes the `shareImage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val shareImage: (String) -> Unit,
    /**
     * Exposes the `saveGenerationResults` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val saveGenerationResults: (List<AiGenerationResult>) -> Unit,
    /**
     * Exposes the `viewGenerationResult` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val viewGenerationResult: (AiGenerationResult) -> Unit,
    /**
     * Exposes the `reportGenerationResult` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val reportGenerationResult: (AiGenerationResult) -> Unit,
    /**
     * Exposes the `applyGenerationResult` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val applyGenerationResult: (AiGenerationResult, Boolean) -> Unit,
) {

    /**
     * Executes the `process` step in the SDAI presentation layer.
     *
     * @param intent intent to process in the MVI workflow.
     * @author Dmitriy Moroz
     */
    fun process(intent: ImageToImageIntent) {
        when (intent) {
            ImageToImageIntent.OpenDrawer -> router.openDrawer()
            ImageToImageIntent.NavigateBack -> router.navigateBack()
            ImageToImageIntent.ConfigureProvider -> router.navigateToServerSetup()
            ImageToImageIntent.NavigateToInPaint -> router.navigateToImageInPaint()
            ImageToImageIntent.PickCamera -> pickImage(ImageToImagePickSource.Camera)
            ImageToImageIntent.PickGallery -> pickImage(ImageToImagePickSource.Gallery)
            ImageToImageIntent.PickRandom -> pickRandomImage()
            ImageToImageIntent.ClearImageInput -> updateState {
                it.copy(
                    imageBase64 = "",
                    inPaint = ImageInPaintState(),
                    results = emptyList(),
                    screenModal = GenerationModal.None,
                    error = null,
                    message = null,
                )
            }
            ImageToImageIntent.Generate -> generate()
            ImageToImageIntent.RunBenchmarkFromPrompt -> Unit
            ImageToImageIntent.SkipBenchmarkPrompt -> Unit
            ImageToImageIntent.ContinueAfterBenchmarkWarning -> Unit
            ImageToImageIntent.SuppressBenchmarkWarningAndContinue -> Unit
            ImageToImageIntent.DismissModal -> updateState { it.copy(screenModal = GenerationModal.None) }
            ImageToImageIntent.CancelGeneration -> cancelGeneration()
            ImageToImageIntent.UndoInPaintStroke -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(
                        strokes = state.inPaint.strokes.dropLast(1),
                    ),
                    message = null,
                )
            }
            ImageToImageIntent.ClearInPaintMask -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(strokes = emptyList()),
                    message = null,
                )
            }
            ImageToImageIntent.DismissError -> updateState { it.copy(error = null) }
            ImageToImageIntent.DismissMessage -> updateState { it.copy(message = null) }
            ImageToImageIntent.DismissEditTag -> updateState { it.copy(editTag = null) }
            is ImageToImageIntent.DrawInPaintStroke -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(
                        strokes = state.inPaint.strokes + intent.stroke,
                    ),
                    error = null,
                    message = null,
                )
            }
            is ImageToImageIntent.SaveResult -> saveImage(intent.base64)
            is ImageToImageIntent.ShareResult -> shareImage(intent.base64)
            is ImageToImageIntent.SaveGenerationResults -> saveGenerationResults(intent.results)
            is ImageToImageIntent.ViewGenerationResult -> viewGenerationResult(intent.result)
            is ImageToImageIntent.ReportGenerationResult -> reportGenerationResult(intent.result)
            is ImageToImageIntent.ShowEditTag -> updateState {
                it.copy(
                    editTag = PromptTagEditRequest(
                        prompt = intent.prompt,
                        negativePrompt = intent.negativePrompt,
                        tag = intent.tag,
                        isNegative = intent.isNegative,
                    ),
                )
            }
            is ImageToImageIntent.ApplyPrompts -> updateState {
                it.copy(
                    prompt = intent.prompt,
                    negativePrompt = intent.negativePrompt,
                    editTag = null,
                    message = null,
                )
            }
            is ImageToImageIntent.ApplyGenerationResult -> applyGenerationResult(intent.ai, intent.inputImage)
            is ImageToImageIntent.UpdateAdvancedOptionsVisibility -> updateState {
                it.copy(advancedOptionsVisible = intent.visible)
            }
            is ImageToImageIntent.UpdatePrompt -> updateState {
                it.copy(prompt = intent.value, promptValidationError = null, error = null, message = null)
            }
            is ImageToImageIntent.UpdateNegativePrompt -> updateState {
                it.copy(negativePrompt = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateWidth -> updateState {
                it.copy(
                    width = intent.value.filter(Char::isDigit),
                    widthValidationError = null,
                    error = null,
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateHeight -> updateState {
                it.copy(
                    height = intent.value.filter(Char::isDigit),
                    heightValidationError = null,
                    error = null,
                    message = null,
                )
            }
            ImageToImageIntent.SwapDimensions -> updateState {
                it.copy(
                    width = it.height,
                    height = it.width,
                    widthValidationError = null,
                    heightValidationError = null,
                    error = null,
                    message = null,
                )
            }
            is ImageToImageIntent.ApplyAspectRatio -> updateState {
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
            is ImageToImageIntent.UpdateSamplingSteps -> updateState {
                val minSteps = it.falAiModel.minInferenceSteps.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MIN_STEPS
                val maxSteps = it.falAiModel.maxInferenceSteps.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: if (it.mode == ServerSource.ARLI_AI) MAX_ARLI_AI_STEPS else MAX_STEPS
                it.copy(samplingSteps = intent.value.coerceIn(minSteps, maxSteps), message = null)
            }
            is ImageToImageIntent.UpdateCfgScale -> updateState {
                val minCfg = it.falAiModel.minGuidanceScale.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MIN_CFG_SCALE
                val maxCfg = it.falAiModel.maxGuidanceScale.takeIf { _ ->
                    it.mode == ServerSource.FAL_AI
                } ?: MAX_CFG_SCALE
                it.copy(cfgScale = intent.value.coerceIn(minCfg, maxCfg), message = null)
            }
            is ImageToImageIntent.UpdateRestoreFaces -> updateState {
                it.copy(restoreFaces = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateSeed -> updateState {
                it.copy(seed = intent.value.filter(Char::isDigit), message = null)
            }
            is ImageToImageIntent.UpdateSubSeed -> updateState {
                it.copy(subSeed = intent.value.filter(Char::isDigit), message = null)
            }
            is ImageToImageIntent.UpdateSubSeedStrength -> updateState {
                it.copy(
                    subSeedStrength = intent.value.coerceIn(MIN_SUB_SEED_STRENGTH, MAX_SUB_SEED_STRENGTH),
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateSampler -> updateState {
                it.copy(selectedSampler = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateScheduler -> updateState {
                it.copy(selectedScheduler = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateNsfw -> updateState {
                it.copy(nsfw = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateBatchCount -> updateState {
                val maxBatch = if (it.mode == ServerSource.FAL_AI) {
                    MAX_FAL_AI_BATCH_COUNT
                } else {
                    MAX_BATCH_COUNT
                }
                it.copy(batchCount = intent.value.coerceIn(MIN_BATCH_COUNT, maxBatch), message = null)
            }
            is ImageToImageIntent.UpdateOpenAiModel -> updateState { state ->
                val size = if (state.openAiSize.supportedModels.contains(intent.value)) {
                    state.openAiSize
                } else {
                    OpenAiSize.entries.first { it.supportedModels.contains(intent.value) }
                }
                state.copy(openAiModel = intent.value, openAiSize = size, message = null)
            }
            is ImageToImageIntent.UpdateOpenAiSize -> updateState {
                it.copy(openAiSize = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateOpenAiQuality -> updateState {
                it.copy(openAiQuality = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateFalAiModel -> updateState {
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
            is ImageToImageIntent.UpdateFalAiImageSize -> updateState {
                it.copy(
                    falAiImageSize = intent.value,
                    width = intent.value.width.toString(),
                    height = intent.value.height.toString(),
                    widthValidationError = null,
                    heightValidationError = null,
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateFalAiAcceleration -> updateState {
                it.copy(falAiAcceleration = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateFalAiSyncMode -> updateState {
                it.copy(falAiSyncMode = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateArliAiModel -> updateState {
                it.copy(arliAiModel = intent.value, message = null, error = null)
            }
            is ImageToImageIntent.UpdateStabilityAiStyle -> updateState {
                it.copy(selectedStylePreset = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateStabilityAiClipGuidance -> updateState {
                it.copy(selectedClipGuidancePreset = intent.value, message = null)
            }
            is ImageToImageIntent.UpdateADetailerConfig -> updateState {
                it.copy(aDetailer = intent.value, message = null)
            }
            ImageToImageIntent.RefreshADetailerAvailability -> Unit
            ImageToImageIntent.RetrySamplers -> Unit
            ImageToImageIntent.OpenADetailerInstallInstructions -> Unit
            is ImageToImageIntent.UpdateDenoisingStrength -> updateState {
                it.copy(
                    denoisingStrength = intent.value.coerceIn(MIN_DENOISING_STRENGTH, MAX_DENOISING_STRENGTH),
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateInPaintBrushSize -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(
                        brushSize = intent.value.coerceIn(
                            IN_PAINT_BRUSH_SIZE_MIN,
                            IN_PAINT_BRUSH_SIZE_MAX,
                        ),
                    ),
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateInPaintMaskBlur -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(
                        maskBlur = intent.value.coerceIn(
                            IN_PAINT_MASK_BLUR_MIN,
                            IN_PAINT_MASK_BLUR_MAX,
                        ),
                    ),
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateInPaintOnlyMaskedPadding -> updateState { state ->
                state.copy(
                    inPaint = state.inPaint.copy(
                        onlyMaskedPaddingPx = intent.value.coerceIn(
                            IN_PAINT_MASK_PADDING_MIN,
                            IN_PAINT_MASK_PADDING_MAX,
                        ),
                    ),
                    message = null,
                )
            }
            is ImageToImageIntent.UpdateInPaintMaskMode -> updateState { state ->
                state.copy(inPaint = state.inPaint.copy(maskMode = intent.value), message = null)
            }
            is ImageToImageIntent.UpdateInPaintMaskContent -> updateState { state ->
                state.copy(inPaint = state.inPaint.copy(maskContent = intent.value), message = null)
            }
            is ImageToImageIntent.UpdateInPaintArea -> updateState { state ->
                state.copy(inPaint = state.inPaint.copy(area = intent.value), message = null)
            }
        }
    }
}

private fun ImageToImageState.dimensionsFor(ratio: GenerationAspectRatio): Pair<Int, Int> {
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

package com.shifthackz.aisdv1.domain.usecase.generation

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiCompatibleGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository

/**
 * Implements `TextToImageUseCase` behavior in the SDAI domain layer.
 *
 * @param openAiCompatibleGenerationRepository Independent compatible image-server repository.
 * @throws IllegalStateException when the current state is invalid.
 * @author Dmitriy Moroz
 */
internal class TextToImageUseCaseImpl(
    /**
     * Exposes the `stableDiffusionGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    /**
     * Exposes the `hordeGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val hordeGenerationRepository: HordeGenerationRepository,
    /**
     * Exposes the `huggingFaceGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,
    /**
     * Exposes the `openAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val openAiGenerationRepository: OpenAiGenerationRepository,
    private val openAiCompatibleGenerationRepository: OpenAiCompatibleGenerationRepository,
    /**
     * Exposes the `stabilityAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,
    /**
     * Exposes the `falAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val falAiGenerationRepository: FalAiGenerationRepository,
    /**
     * Exposes the `arliAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val arliAiGenerationRepository: ArliAiGenerationRepository,
    /**
     * Exposes the `swarmUiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,
    /**
     * Exposes the `localDiffusionGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,
    /**
     * Exposes the `mediaPipeGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val mediaPipeGenerationRepository: MediaPipeGenerationRepository,
    /**
     * Exposes the `stableDiffusionCppGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val stableDiffusionCppGenerationRepository: StableDiffusionCppGenerationRepository,
    /**
     * Exposes the `coreMlGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val coreMlGenerationRepository: CoreMlGenerationRepository,
    /**
     * Exposes the `bonsaiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val bonsaiGenerationRepository: BonsaiGenerationRepository,
    /**
     * Exposes the `preferenceManager` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the current state is invalid.
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
) : TextToImageUseCase {

    /**
     * Executes the `invoke` step in the SDAI domain layer.
     *
     * @param payload generation payload used by the operation.
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke(
        payload: TextToImagePayload,
    ): List<AiGenerationResult> = when (preferenceManager.source) {
        ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromText(payload)
        ServerSource.FAL_AI -> falAiGenerationRepository.generateFromText(payload)
        ServerSource.ARLI_AI -> arliAiGenerationRepository.generateFromText(payload)
        else -> List(payload.batchCount.coerceAtLeast(1)) {
            coroutineContext.ensureActive()
            generateSingle(payload)
        }
    }

    /**
     * Executes the `generateSingle` step in the SDAI domain layer.
     *
     * @param payload generation payload used by the operation.
     * @author Dmitriy Moroz
     */
    private suspend fun generateSingle(payload: TextToImagePayload) = when (preferenceManager.source) {
        ServerSource.HORDE -> hordeGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_MICROSOFT_ONNX -> localDiffusionGenerationRepository.generateFromText(payload)
        ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromText(payload)
        ServerSource.OPEN_AI -> openAiGenerationRepository.generateFromText(payload)
        ServerSource.OPEN_AI_COMPATIBLE -> openAiCompatibleGenerationRepository.generateFromText(payload)
        ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromText(payload)
        ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> mediaPipeGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> stableDiffusionCppGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_APPLE_CORE_ML -> coreMlGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_APPLE_BONSAI -> bonsaiGenerationRepository.generateFromText(payload)
        ServerSource.AUTOMATIC1111 -> error("Automatic1111 batch must be generated through generateFromText(payload).")
        ServerSource.FAL_AI -> error("Fal.ai batch must be generated through generateFromText(payload).")
        ServerSource.ARLI_AI -> error("ArliAI batch must be generated through generateFromText(payload).")
    }
}

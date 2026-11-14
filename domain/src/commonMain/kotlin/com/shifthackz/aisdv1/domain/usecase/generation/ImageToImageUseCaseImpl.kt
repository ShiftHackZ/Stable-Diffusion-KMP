package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository

/**
 * Implements `ImageToImageUseCase` behavior in the SDAI domain layer.
 *
 * @throws IllegalStateException when the delegated operation cannot complete.
 * @author Dmitriy Moroz
 */
internal class ImageToImageUseCaseImpl(
    /**
     * Exposes the `stableDiffusionGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    /**
     * Exposes the `swarmUiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,
    /**
     * Exposes the `hordeGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val hordeGenerationRepository: HordeGenerationRepository,
    /**
     * Exposes the `huggingFaceGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,
    /**
     * Exposes the `stabilityAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,
    /**
     * Exposes the `coreMlGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val coreMlGenerationRepository: CoreMlGenerationRepository,
    /**
     * Exposes the `falAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val falAiGenerationRepository: FalAiGenerationRepository,
    /**
     * Exposes the `arliAiGenerationRepository` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val arliAiGenerationRepository: ArliAiGenerationRepository,
    /**
     * Exposes the `preferenceManager` value used by the SDAI domain layer.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
) : ImageToImageUseCase {

    /**
     * Executes the `invoke` step in the SDAI domain layer.
     *
     * @param payload generation payload used by the operation.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(payload: ImageToImagePayload) = when (preferenceManager.source) {
        ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromImage(payload)
        ServerSource.FAL_AI -> falAiGenerationRepository.generateFromImage(payload)
        ServerSource.ARLI_AI -> arliAiGenerationRepository.generateFromImage(payload)
        else -> List(payload.batchCount.coerceAtLeast(1)) {
            generateSingle(payload)
        }
    }

    /**
     * Executes the `generateSingle` step in the SDAI domain layer.
     *
     * @param payload generation payload used by the operation.
     * @author Dmitriy Moroz
     */
    private suspend fun generateSingle(payload: ImageToImagePayload) = when (preferenceManager.source) {
        ServerSource.OPEN_AI_COMPATIBLE -> error("This image server supports text-to-image only")
        ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromImage(payload)
        ServerSource.HORDE -> hordeGenerationRepository.generateFromImage(payload)
        ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromImage(payload)
        ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromImage(payload)
        ServerSource.LOCAL_APPLE_CORE_ML -> coreMlGenerationRepository.generateFromImage(payload)
        ServerSource.AUTOMATIC1111 -> error("Automatic1111 batch must be generated through generateFromImage(payload).")
        ServerSource.FAL_AI -> error("Fal.ai batch must be generated through generateFromImage(payload).")
        ServerSource.ARLI_AI -> error("ArliAI batch must be generated through generateFromImage(payload).")
        else -> throw IllegalStateException("Img2Img not yet supported on ${preferenceManager.source}!")
    }
}

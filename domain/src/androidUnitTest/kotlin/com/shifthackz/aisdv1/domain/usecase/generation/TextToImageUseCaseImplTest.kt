package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TextToImageUseCaseImplTest {

    private val stubException = RuntimeException("Unable to generate image.")
    private val stubStableDiffusionGenerationRepository = mockk<StableDiffusionGenerationRepository>()
    private val stubHordeGenerationRepository = mockk<HordeGenerationRepository>()
    private val stubHuggingFaceGenerationRepository = mockk<HuggingFaceGenerationRepository>()
    private val stubOpenAiGenerationRepository = mockk<OpenAiGenerationRepository>()
    private val stubStabilityAiGenerationRepository = mockk<StabilityAiGenerationRepository>()
    private val stubFalAiGenerationRepository = mockk<FalAiGenerationRepository>()
    private val stubArliAiGenerationRepository = mockk<ArliAiGenerationRepository>()
    private val stubSwarmUiGenerationRepository = mockk<SwarmUiGenerationRepository>()
    private val stubLocalDiffusionGenerationRepository = mockk<LocalDiffusionGenerationRepository>()
    private val stubMediaPipeGenerationRepository = mockk<MediaPipeGenerationRepository>()
    private val stubStableDiffusionCppGenerationRepository = mockk<StableDiffusionCppGenerationRepository>()
    private val stubCoreMlGenerationRepository = mockk<CoreMlGenerationRepository>()
    private val stubBonsaiGenerationRepository = mockk<BonsaiGenerationRepository>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    private val useCase = TextToImageUseCaseImpl(
        stableDiffusionGenerationRepository = stubStableDiffusionGenerationRepository,
        hordeGenerationRepository = stubHordeGenerationRepository,
        huggingFaceGenerationRepository = stubHuggingFaceGenerationRepository,
        openAiGenerationRepository = stubOpenAiGenerationRepository,
        openAiCompatibleGenerationRepository = mockk(),
        openAiOAuthGenerationRepository = mockk(),
        stabilityAiGenerationRepository = stubStabilityAiGenerationRepository,
        falAiGenerationRepository = stubFalAiGenerationRepository,
        arliAiGenerationRepository = stubArliAiGenerationRepository,
        localDiffusionGenerationRepository = stubLocalDiffusionGenerationRepository,
        swarmUiGenerationRepository = stubSwarmUiGenerationRepository,
        mediaPipeGenerationRepository = stubMediaPipeGenerationRepository,
        stableDiffusionCppGenerationRepository = stubStableDiffusionCppGenerationRepository,
        coreMlGenerationRepository = stubCoreMlGenerationRepository,
        bonsaiGenerationRepository = stubBonsaiGenerationRepository,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given source is AUTOMATIC1111, expected stable diffusion generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } returns listOf(mockAiGenerationResult)

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is HORDE, expected horde generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.HORDE
        coEvery { stubHordeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubHordeGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is HUGGING_FACE, expected hugging face generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.HUGGING_FACE
        coEvery { stubHuggingFaceGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubHuggingFaceGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is OPEN_AI, expected open ai generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.OPEN_AI
        coEvery { stubOpenAiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubOpenAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is STABILITY_AI, expected stability ai generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.STABILITY_AI
        coEvery { stubStabilityAiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubStabilityAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is FAL_AI, expected fal ai generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.FAL_AI
        coEvery { stubFalAiGenerationRepository.generateFromText(any()) } returns listOf(mockAiGenerationResult)

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is ARLI_AI, expected arli ai generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
        coEvery { stubArliAiGenerationRepository.generateFromText(any()) } returns listOf(mockAiGenerationResult)

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is SWARM_UI, expected swarm ui generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
        coEvery { stubSwarmUiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubSwarmUiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is LOCAL_MICROSOFT_ONNX, expected local diffusion generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.LOCAL_MICROSOFT_ONNX
        coEvery { stubLocalDiffusionGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubLocalDiffusionGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is LOCAL_GOOGLE_MEDIA_PIPE, expected media pipe generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.LOCAL_GOOGLE_MEDIA_PIPE
        coEvery { stubMediaPipeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubMediaPipeGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is LOCAL_STABLE_DIFFUSION_CPP, expected stable diffusion cpp generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.LOCAL_STABLE_DIFFUSION_CPP
        coEvery { stubStableDiffusionCppGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubStableDiffusionCppGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is LOCAL_APPLE_CORE_ML, expected core ml generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.LOCAL_APPLE_CORE_ML
        coEvery { stubCoreMlGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubCoreMlGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is LOCAL_APPLE_BONSAI, expected bonsai generation`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.LOCAL_APPLE_BONSAI
        coEvery { stubBonsaiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubBonsaiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given automatic1111 batch count is 10, expected batch generated by repository`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } returns List(10) { mockAiGenerationResult }

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 10))

        assertEquals(List(10) { mockAiGenerationResult }, actual)
        coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given non batch source batch count is 10, expected generation repeated 10 times`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.HORDE
        coEvery { stubHordeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 10))

        assertEquals(List(10) { mockAiGenerationResult }, actual)
        coVerify(exactly = 10) { stubHordeGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given fal ai batch count is 4, expected batch generated by repository`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.FAL_AI
        coEvery { stubFalAiGenerationRepository.generateFromText(any()) } returns List(4) { mockAiGenerationResult }

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 4))

        assertEquals(List(4) { mockAiGenerationResult }, actual)
        coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given arli ai batch count is 4, expected batch generated by repository`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
        coEvery { stubArliAiGenerationRepository.generateFromText(any()) } returns List(4) { mockAiGenerationResult }

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 4))

        assertEquals(List(4) { mockAiGenerationResult }, actual)
        coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given generation fails, expected error propagated`() = runTest {
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } throws stubException

        val actual = runCatching { useCase(mockTextToImagePayload.copy(batchCount = 1)) }

        assertSame(stubException, actual.exceptionOrNull())
    }
}

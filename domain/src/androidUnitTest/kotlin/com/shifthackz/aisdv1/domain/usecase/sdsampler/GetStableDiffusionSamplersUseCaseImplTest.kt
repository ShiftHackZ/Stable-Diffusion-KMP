package com.shifthackz.aisdv1.domain.usecase.sdsampler

import com.shifthackz.aisdv1.domain.mocks.mockStableDiffusionSamplers
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetStableDiffusionSamplersUseCaseImplTest {

    private val stubStableDiffusionSamplersRepository = mockk<StableDiffusionSamplersRepository>()

    private val useCase = GetStableDiffusionSamplersUseCaseImpl(
        repository = stubStableDiffusionSamplersRepository,
    )

    /**
     * Models a successful refresh for cache-read tests; an unstubbed fetch is a failure.
     *
     * @author Dmitriy Moroz
     */
    @Before
    fun initialize() {
        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } returns Unit
    }

    @Test
    fun `given got samplers from repository, expected valid samplers value`() = runTest {
        coEvery {
            stubStableDiffusionSamplersRepository.getSamplers()
        } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, useCase())
    }

    @Test
    fun `given got empty list from repository, expected empty value`() = runTest {
        coEvery {
            stubStableDiffusionSamplersRepository.getSamplers()
        } returns emptyList()

        assertEquals(emptyList<Any>(), useCase())
    }

    @Test
    fun `given got error from repository, expected the same error`() = runTest {
        val stubException = RuntimeException("Error query database.")

        coEvery {
            stubStableDiffusionSamplersRepository.getSamplers()
        } throws stubException

        val actual = runCatching { useCase() }.exceptionOrNull()

        assertTrue(actual === stubException)
    }
}

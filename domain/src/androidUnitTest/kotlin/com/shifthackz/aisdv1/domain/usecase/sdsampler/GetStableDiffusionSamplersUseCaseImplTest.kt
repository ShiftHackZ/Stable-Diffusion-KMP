package com.shifthackz.aisdv1.domain.usecase.sdsampler

import com.shifthackz.aisdv1.domain.mocks.mockStableDiffusionSamplers
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies refresh ordering, cache fallback and recovery without swallowing cancellation.
 *
 * @author Dmitriy Moroz
 */
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

    @Test
    fun `successful request refreshes before reading the cache`() = runTest {
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, useCase())

        coVerifyOrder {
            stubStableDiffusionSamplersRepository.fetchSamplers()
            stubStableDiffusionSamplersRepository.getSamplers()
        }
    }

    @Test
    fun `server failure without cache propagates the original failure`() = runTest {
        val failure = IllegalStateException("HTTP 503")
        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } throws failure
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns emptyList()

        assertSame(failure, runCatching { useCase() }.exceptionOrNull())
    }

    @Test
    fun `server failure preserves a populated sampler cache`() = runTest {
        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } throws IllegalStateException("HTTP 503")
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, useCase())
    }

    @Test
    fun `a failed attempt does not prevent a later successful refresh`() = runTest {
        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } throws IllegalStateException("HTTP 503")
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns emptyList()
        assertTrue(runCatching { useCase() }.isFailure)

        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } returns Unit
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, useCase())
        coVerify(exactly = 2) { stubStableDiffusionSamplersRepository.fetchSamplers() }
    }

    @Test
    fun `an empty response does not prevent a later populated refresh`() = runTest {
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns emptyList()
        assertTrue(useCase().isEmpty())

        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, useCase())
        coVerify(exactly = 2) { stubStableDiffusionSamplersRepository.fetchSamplers() }
    }

    @Test
    fun `cancellation propagates without reading even a populated cache`() = runTest {
        val cancellation = CancellationException("Screen replaced")
        coEvery { stubStableDiffusionSamplersRepository.fetchSamplers() } throws cancellation
        coEvery { stubStableDiffusionSamplersRepository.getSamplers() } returns mockStableDiffusionSamplers

        assertSame(cancellation, runCatching { useCase() }.exceptionOrNull())
        coVerify(exactly = 0) { stubStableDiffusionSamplersRepository.getSamplers() }
    }
}

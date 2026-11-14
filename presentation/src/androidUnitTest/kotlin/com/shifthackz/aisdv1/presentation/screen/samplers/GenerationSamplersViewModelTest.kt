package com.shifthackz.aisdv1.presentation.screen.samplers

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Exercises sampler recovery through both production generation view models and their public intents.
 *
 * @param type Generation screen to exercise with the same failure and lifecycle recovery scenarios.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class GenerationSamplersViewModelTest(private val type: AiGenerationResult.Type) {

    private val dispatcher = StandardTestDispatcher()
    private val getSamplers = mockk<GetStableDiffusionSamplersUseCase>()
    private val fixtures = mutableListOf<SamplerRecoveryViewModelFixture>()
    private val samplers = listOf("Euler a", "DPM++ 2M").map {
        StableDiffusionSampler(name = it, aliases = emptyList(), options = emptyMap())
    }

    @Before
    fun initialize() {
        Dispatchers.setMain(dispatcher)
        coEvery { getSamplers() } returns samplers
    }

    @After
    fun cleanUp() {
        fixtures.forEach(SamplerRecoveryViewModelFixture::close)
        Dispatchers.resetMain()
    }

    @Test
    fun `initial refresh populates the sampler choices and selects a usable sampler`() = runTest {
        val fixture = createFixture()
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        assertTrue(fixture.state.selectedSampler in fixture.state.availableSamplers)
        assertFalse(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        assertTrue(fixture.errors.isEmpty())
    }

    @Test
    fun `repeated recovery intents do not duplicate an active request`() = runTest {
        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { getSamplers() } coAnswers { response.await() }
        val fixture = createFixture()
        runCurrent()

        assertTrue(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        fixture.retry()
        fixture.retry()
        runCurrent()
        coVerify(exactly = 1) { getSamplers() }

        response.complete(samplers)
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        assertFalse(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
    }

    @Test
    fun `a failed initial refresh recovers through the retry and resume intent`() = runTest {
        val failure = IllegalStateException("HTTP 503")
        coEvery { getSamplers() } throws failure
        val fixture = createFixture()
        advanceUntilIdle()

        assertTrue(fixture.state.availableSamplers.isEmpty())
        assertTrue(fixture.state.hasSamplerLoadFailed)
        assertFalse(fixture.state.isLoadingSamplers)
        assertEquals(1, fixture.errors.size)
        assertEquals(failure.message, fixture.errors.single().message)
        assertTrue(fixture.errors.single() is IllegalStateException)

        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { getSamplers() } coAnswers { response.await() }
        fixture.retry()
        runCurrent()

        assertTrue(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        fixture.retry()
        runCurrent()
        coVerify(exactly = 2) { getSamplers() }

        response.complete(samplers)
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        assertFalse(fixture.state.isLoadingSamplers)
        coVerify(exactly = 2) { getSamplers() }
    }

    @Test
    fun `empty responses and failed retries remain recoverable`() = runTest {
        coEvery { getSamplers() } returns emptyList()
        val fixture = createFixture()
        advanceUntilIdle()
        assertTrue(fixture.state.hasSamplerLoadFailed)

        coEvery { getSamplers() } throws IllegalStateException("HTTP 503")
        fixture.retry()
        advanceUntilIdle()
        assertTrue(fixture.state.hasSamplerLoadFailed)
        assertFalse(fixture.state.isLoadingSamplers)

        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { getSamplers() } coAnswers { response.await() }
        fixture.retry()
        runCurrent()

        assertTrue(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        fixture.retry()
        runCurrent()
        coVerify(exactly = 3) { getSamplers() }

        response.complete(samplers)
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        assertFalse(fixture.state.isLoadingSamplers)
        coVerify(exactly = 3) { getSamplers() }
    }

    @Test
    fun `eager settings collection during construction retains the retry key`() = runTest {
        coEvery { getSamplers() } returns emptyList()
        val fixture = createFixture(UnconfinedTestDispatcher(testScheduler))
        advanceUntilIdle()
        assertTrue(fixture.state.hasSamplerLoadFailed)

        coEvery { getSamplers() } returns samplers
        fixture.retry()
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        assertFalse(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        coVerify(exactly = 2) { getSamplers() }
    }

    @Test
    fun `a recreated view model fetches fresh samplers`() = runTest {
        val original = createFixture()
        advanceUntilIdle()
        assertEquals(samplers.map { it.name }, original.state.availableSamplers)
        original.close()

        val replacement = samplers.map { it.copy(name = "new-${it.name}") }
        coEvery { getSamplers() } returns replacement
        val recreated = createFixture()
        advanceUntilIdle()

        assertEquals(replacement.map { it.name }, recreated.state.availableSamplers)
        assertFalse(recreated.state.hasSamplerLoadFailed)
        coVerify(exactly = 2) { getSamplers() }
    }

    @Test
    fun `recovery intents and unrelated settings do not refetch populated samplers`() = runTest {
        val fixture = createFixture()
        advanceUntilIdle()

        fixture.retry()
        fixture.settings.value = fixture.settings.value.copy(sdModel = "another-checkpoint")
        advanceUntilIdle()

        assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
        coVerify(exactly = 1) { getSamplers() }
    }

    @Test
    fun `switching provider cancels the pending sampler load without surfacing failure`() = runTest {
        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { getSamplers() } coAnswers { response.await() }
        val fixture = createFixture()
        runCurrent()

        fixture.settings.value = fixture.settings.value.copy(source = ServerSource.STABILITY_AI)
        runCurrent()
        response.complete(samplers)
        fixture.retry()
        advanceUntilIdle()

        assertEquals(ServerSource.STABILITY_AI, fixture.state.mode)
        assertFalse(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        assertTrue(fixture.errors.isEmpty())
        coVerify(exactly = 1) { getSamplers() }
    }

    @Test
    fun `a late response from the previous server cannot overwrite the new server choices`() = runTest {
        val oldResponse = CompletableDeferred<List<StableDiffusionSampler>>()
        val replacement = samplers.map { it.copy(name = "new-${it.name}") }
        var attempts = 0
        coEvery { getSamplers() } coAnswers {
            if (++attempts == 1) withContext(NonCancellable) { oldResponse.await() }
            else replacement
        }
        val fixture = createFixture()
        runCurrent()

        fixture.settings.value = fixture.settings.value.copy(serverUrl = "http://server-b:7860")
        runCurrent()
        assertEquals(replacement.map { it.name }, fixture.state.availableSamplers)

        oldResponse.complete(samplers)
        advanceUntilIdle()

        assertEquals(replacement.map { it.name }, fixture.state.availableSamplers)
        assertFalse(fixture.state.isLoadingSamplers)
        assertFalse(fixture.state.hasSamplerLoadFailed)
        assertTrue(fixture.errors.isEmpty())
        coVerify(exactly = 2) { getSamplers() }
    }

    /**
     * Registers the production view model for scope cleanup after each parameterized test case.
     *
     * @param immediateDispatcher Entry dispatcher; IO continues to use the controlled test dispatcher.
     * @author Dmitriy Moroz
     */
    private fun createFixture(immediateDispatcher: CoroutineDispatcher = dispatcher) =
        SamplerRecoveryViewModelFixture(type, dispatcher, getSamplers, immediateDispatcher)
        .also(fixtures::add)

    companion object {
        /**
         * Runs every regression against both generation screens sharing the sampler field contract.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generationTypes() = listOf(
            AiGenerationResult.Type.TEXT_TO_IMAGE,
            AiGenerationResult.Type.IMAGE_TO_IMAGE,
        )
    }
}

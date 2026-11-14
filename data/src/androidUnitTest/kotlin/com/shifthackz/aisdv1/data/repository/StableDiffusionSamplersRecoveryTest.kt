package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mocks.mockStableDiffusionSamplers
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionSamplersDataSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies sampler cache ownership and complete snapshots during concurrent refresh and recovery.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StableDiffusionSamplersRecoveryTest {

    private val remote = mockk<StableDiffusionSamplersDataSource.Remote>()
    private val local = mockk<StableDiffusionSamplersDataSource.Local>()
    private val preferences = mockk<PreferenceManager>()
    private val authorization = mockk<AuthorizationStore>()
    private var serverUrl = "http://server-a:7860"
    private var credentials: AuthorizationCredentials = AuthorizationCredentials.None
    private var cachedSamplers = emptyList<StableDiffusionSampler>()
    private val repository = createRepository()

    @Before
    fun initialize() {
        every { preferences.automatic1111ServerUrl } answers { serverUrl }
        every { authorization.getAuthorizationCredentials() } answers { credentials }
        coEvery { remote.fetchSamplers(any(), any()) } returns mockStableDiffusionSamplers
        coEvery { local.getSamplers() } answers { cachedSamplers }
        coEvery { local.insertSamplers(any()) } coAnswers { cachedSamplers = firstArg() }
    }

    @Test
    fun `an unowned cache is not accepted by a recreated repository`() = runTest {
        repository.fetchSamplers()
        assertEquals(mockStableDiffusionSamplers, repository.getSamplers())

        assertTrue(createRepository().getSamplers().isEmpty())
    }

    @Test
    fun `failed refresh retains cache only for the same server`() = runTest {
        repository.fetchSamplers()
        coEvery { remote.fetchSamplers(any(), any()) } throws IllegalStateException("HTTP 503")

        assertTrue(runCatching { repository.fetchSamplers() }.isFailure)
        assertEquals(mockStableDiffusionSamplers, repository.getSamplers())

        serverUrl = "http://server-b:7860"
        assertTrue(runCatching { repository.fetchSamplers() }.isFailure)
        assertTrue(repository.getSamplers().isEmpty())
    }

    @Test
    fun `changed credentials cannot reuse the previous account cache`() = runTest {
        repository.fetchSamplers()
        credentials = AuthorizationCredentials.HttpBasic("another-account", "fixture-only")
        coEvery { remote.fetchSamplers(any(), any()) } throws IllegalStateException("HTTP 401")

        assertTrue(runCatching { repository.fetchSamplers() }.isFailure)
        assertTrue(repository.getSamplers().isEmpty())
    }

    @Test
    fun `a response from a replaced server is discarded`() = runTest {
        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { remote.fetchSamplers(any(), any()) } coAnswers { response.await() }
        val request = async { repository.fetchSamplers() }
        runCurrent()
        coVerify(exactly = 1) { remote.fetchSamplers("http://server-a:7860", AuthorizationCredentials.None) }

        serverUrl = "http://server-b:7860"
        response.complete(mockStableDiffusionSamplers)
        request.await()

        assertTrue(repository.getSamplers().isEmpty())
        coVerify(exactly = 0) { local.insertSamplers(any()) }
    }

    @Test
    fun `a response from replaced credentials is discarded`() = runTest {
        val response = CompletableDeferred<List<StableDiffusionSampler>>()
        coEvery { remote.fetchSamplers(any(), any()) } coAnswers { response.await() }
        val request = async { repository.fetchSamplers() }
        runCurrent()

        credentials = AuthorizationCredentials.HttpBasic("another-account", "fixture-only")
        response.complete(mockStableDiffusionSamplers)
        request.await()

        assertTrue(repository.getSamplers().isEmpty())
        coVerify(exactly = 0) { local.insertSamplers(any()) }
    }

    @Test
    fun `readers wait until cache replacement is complete`() = runTest {
        repository.fetchSamplers()
        val replacement = mockStableDiffusionSamplers.map { it.copy(name = "new-${it.name}") }
        val replacementStarted = CompletableDeferred<Unit>()
        val releaseReplacement = CompletableDeferred<Unit>()
        coEvery { remote.fetchSamplers(any(), any()) } returns replacement
        coEvery { local.insertSamplers(any()) } coAnswers {
            cachedSamplers = emptyList()
            replacementStarted.complete(Unit)
            releaseReplacement.await()
            cachedSamplers = firstArg()
        }
        val refresh = async { repository.fetchSamplers() }
        replacementStarted.await()

        val reader = async(start = CoroutineStart.UNDISPATCHED) { repository.getSamplers() }
        assertFalse(reader.isCompleted)
        releaseReplacement.complete(Unit)

        refresh.await()
        assertEquals(replacement, reader.await())
    }

    @Test
    fun `a failed cache write invalidates ownership and the next refresh recovers`() = runTest {
        repository.fetchSamplers()
        coEvery { local.insertSamplers(any()) } throws IllegalStateException("Cache write failed")

        assertTrue(runCatching { repository.fetchSamplers() }.isFailure)
        assertTrue(repository.getSamplers().isEmpty())

        val replacement = mockStableDiffusionSamplers.map { it.copy(name = "new-${it.name}") }
        coEvery { remote.fetchSamplers(any(), any()) } returns replacement
        coEvery { local.insertSamplers(any()) } coAnswers { cachedSamplers = firstArg() }
        repository.fetchSamplers()

        assertEquals(replacement, repository.getSamplers())
    }

    @Test
    fun `a cancelled cache replacement does not expose partially written data`() = runTest {
        repository.fetchSamplers()
        val partialReplacement = mockStableDiffusionSamplers.take(1).map { it.copy(name = "partial-${it.name}") }
        val replacementStarted = CompletableDeferred<Unit>()
        val releaseReplacement = CompletableDeferred<Unit>()
        coEvery { local.insertSamplers(any()) } coAnswers {
            cachedSamplers = partialReplacement
            replacementStarted.complete(Unit)
            releaseReplacement.await()
        }
        val refresh = async { repository.fetchSamplers() }
        replacementStarted.await()

        refresh.cancelAndJoin()

        assertTrue(refresh.isCancelled)
        assertEquals(partialReplacement, cachedSamplers)
        assertTrue(repository.getSamplers().isEmpty())
    }

    /**
     * Creates a repository with independent in-memory ownership over the shared datasource fixture.
     *
     * @author Dmitriy Moroz
     */
    private fun createRepository() = StableDiffusionSamplersRepositoryImpl(
        remoteDataSource = remote,
        localDataSource = local,
        preferenceManager = preferences,
        authorizationStore = authorization,
    )
}

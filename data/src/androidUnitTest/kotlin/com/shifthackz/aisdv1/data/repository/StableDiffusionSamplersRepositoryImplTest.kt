package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mocks.mockStableDiffusionSamplers
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionSamplersDataSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StableDiffusionSamplersRepositoryImplTest {

    private val stubBaseUrl = "http://192.168.0.1:7860"
    private val stubCredentials = AuthorizationCredentials.HttpBasic("user", "password")
    private val stubException = RuntimeException("Something went wrong.")
    private val stubRemoteDataSource = mockk<StableDiffusionSamplersDataSource.Remote>()
    private val stubLocalDataSource = mockk<StableDiffusionSamplersDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubAuthorizationStore = mockk<AuthorizationStore>()

    private val repository = StableDiffusionSamplersRepositoryImpl(
        remoteDataSource = stubRemoteDataSource,
        localDataSource = stubLocalDataSource,
        preferenceManager = stubPreferenceManager,
        authorizationStore = stubAuthorizationStore,
    )

    @Before
    fun initialize() {
        every { stubPreferenceManager.automatic1111ServerUrl } returns stubBaseUrl
        every { stubAuthorizationStore.getAuthorizationCredentials() } returns stubCredentials
    }

    @Test
    fun `given attempt to fetch samplers, remote returns data, local insert success, expected complete value`() = runTest {
        coEvery {
            stubRemoteDataSource.fetchSamplers(stubBaseUrl, stubCredentials)
        } returns mockStableDiffusionSamplers

        coEvery {
            stubLocalDataSource.insertSamplers(mockStableDiffusionSamplers)
        } returns Unit

        val actual = runCatching { repository.fetchSamplers() }

        assertTrue(actual.isSuccess)
    }

    @Test
    fun `given attempt to fetch samplers, remote throws exception, local insert success, expected error value`() = runTest {
        coEvery {
            stubRemoteDataSource.fetchSamplers(stubBaseUrl, stubCredentials)
        } throws stubException

        val actual = runCatching { repository.fetchSamplers() }.exceptionOrNull()

        assertTrue(actual === stubException)
    }

    @Test
    fun `given attempt to fetch samplers, remote returns data, local insert fails, expected error value`() = runTest {
        coEvery {
            stubRemoteDataSource.fetchSamplers(stubBaseUrl, stubCredentials)
        } returns mockStableDiffusionSamplers

        coEvery {
            stubLocalDataSource.insertSamplers(mockStableDiffusionSamplers)
        } throws stubException

        val actual = runCatching { repository.fetchSamplers() }.exceptionOrNull()

        assertTrue(actual === stubException)
    }

    @Test
    fun `given attempt to get samplers, local data source returns list, expected valid domain models list value`() = runTest {
        populateCache()
        coEvery {
            stubLocalDataSource.getSamplers()
        } returns mockStableDiffusionSamplers

        assertEquals(mockStableDiffusionSamplers, repository.getSamplers())
    }

    @Test
    fun `given attempt to get samplers, local data source returns empty list, expected empty domain models list value`() = runTest {
        populateCache()
        coEvery {
            stubLocalDataSource.getSamplers()
        } returns emptyList()

        assertEquals(emptyList<Any>(), repository.getSamplers())
    }

    @Test
    fun `given attempt to get samplers, local data source throws exception, expected error value`() = runTest {
        populateCache()
        coEvery {
            stubLocalDataSource.getSamplers()
        } throws stubException

        val actual = runCatching { repository.getSamplers() }.exceptionOrNull()

        assertTrue(actual === stubException)
    }

    /**
     * Associates the local cache fixture with the configured connection before reading it.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun populateCache() {
        coEvery {
            stubRemoteDataSource.fetchSamplers(stubBaseUrl, stubCredentials)
        } returns mockStableDiffusionSamplers
        coEvery {
            stubLocalDataSource.insertSamplers(mockStableDiffusionSamplers)
        } returns Unit
        repository.fetchSamplers()
    }
}

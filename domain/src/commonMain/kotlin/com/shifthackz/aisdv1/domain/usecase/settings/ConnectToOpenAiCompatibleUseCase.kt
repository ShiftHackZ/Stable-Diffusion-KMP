package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.isOpenAiCompatibleEndpoint
import com.shifthackz.aisdv1.domain.repository.OpenAiCompatibleGenerationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Validates a candidate before replacing the saved connection.
 *
 * @author Dmitriy Moroz
 */
interface ConnectToOpenAiCompatibleUseCase {
    /**
     * Accepts missing model discovery (404/405) as explicitly unverified.
     *
     * @param url Complete API prefix, including version and reverse-proxy path.
     * @param apiKey Optional bearer token, independent from the official OpenAI key.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(url: String, apiKey: String): Result<OpenAiCompatibleConnection>
}

/**
 * Publishes a configuration snapshot only after a successful candidate probe.
 *
 * @param repository Candidate connection validator.
 * @param getConfigurationUseCase Reads all providers' current settings.
 * @param setServerConfigurationUseCase Writes a complete configuration and refreshes preferences.
 * @author Dmitriy Moroz
 */
internal class DefaultConnectToOpenAiCompatibleUseCaseImpl(
    private val repository: OpenAiCompatibleGenerationRepository,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
) : ConnectToOpenAiCompatibleUseCase {
    /**
     * Saves only a reachable or explicitly unverified candidate, restoring the snapshot on failure.
     *
     * @param url Full HTTP(S) API prefix without credentials, query or fragment.
     * @param apiKey Optional bearer token.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(url: String, apiKey: String): Result<OpenAiCompatibleConnection> = try {
        val candidate = url.trim().trimEnd('/')
        require(isOpenAiCompatibleEndpoint(candidate)) { "Invalid HTTP(S) API prefix" }
        val outcome = repository.probe(candidate, apiKey.trim())
        coroutineContext.ensureActive()
        val previous = getConfigurationUseCase()
        try {
            setServerConfigurationUseCase(
                previous.copy(
                    source = ServerSource.OPEN_AI_COMPATIBLE,
                    demoMode = false,
                    openAiCompatibleUrl = candidate,
                    openAiCompatibleApiKey = apiKey.trim(),
                ),
            )
        } catch (error: Exception) {
            withContext(NonCancellable) { setServerConfigurationUseCase(previous) }
            throw error
        }
        Result.success(outcome)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}

package com.shifthackz.aisdv1.data.repository

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.datasource.OpenAiCompatibleGenerationDataSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.OpenAiCompatibleGenerationRepository

/**
 * Generates validated compatible-server images through shared gallery/autosave persistence.
 *
 * @param mediaStoreGateway Normal media export gateway.
 * @param localDataSource Gallery persistence.
 * @param preferenceManager Current independent provider settings.
 * @param backgroundWorkObserver Shared background-generation observer.
 * @param remoteDataSource Validates and resolves server image responses.
 * @author Dmitriy Moroz
 */
internal class OpenAiCompatibleGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    private val preferenceManager: PreferenceManager,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val remoteDataSource: OpenAiCompatibleGenerationDataSource.Remote,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
), OpenAiCompatibleGenerationRepository {

    /**
     * Probes candidate settings without modifying preferences.
     *
     * @param url Full API prefix.
     * @param apiKey Optional bearer token.
     * @author Dmitriy Moroz
     */
    override suspend fun probe(url: String, apiKey: String) = remoteDataSource.probe(url, apiKey)

    /**
     * Saves a validated image through the shared gallery and autosave path.
     *
     * @param payload One image request with the user-selected model ID.
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload) = remoteDataSource
        .textToImage(preferenceManager.openAiCompatibleUrl, preferenceManager.openAiCompatibleApiKey, payload)
        .let { ai ->
            coroutineContext.ensureActive()
            insertGenerationResult(ai)
        }
}

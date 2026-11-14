package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.feature.openaioauth.oauthCall
import com.shifthackz.aisdv1.data.image.decodedImageDimensions
import com.shifthackz.aisdv1.data.mappers.toOpenAiCompatibleResult
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.OpenAiOAuthGenerationRepository
import com.shifthackz.aisdv1.feature.openaioauth.auth.OAuthSessionManager
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthIdentity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.time.Clock

/**
 * Decodes direct OAuth images and guards gallery insertion against account/source replacement.
 * @param mediaStoreGateway Normal application export adapter.
 * @param localDataSource Gallery database adapter.
 * @param preferenceManager Current selected provider and shared autosave settings.
 * @param backgroundWorkObserver Existing background task observer.
 * @param sessions Protected token lifecycle and direct transport.
 * @param selectionGuard Serializes final persistence with provider replacement.
 * @author Dmitriy Moroz
 */
internal class OpenAiOAuthGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    private val preferenceManager: PreferenceManager,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val sessions: OAuthSessionManager,
    private val selectionGuard: ProviderSelectionGuard,
) : CoreGenerationRepository(mediaStoreGateway, localDataSource, preferenceManager, backgroundWorkObserver), OpenAiOAuthGenerationRepository {
    /**
     * Generates and decodes under the submitted account; identity/source changes cancel the request.
     * @param payload Prompt, positive dimensions and immutable token-free work identity.
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload) = oauthCall {
        val identity = OAuthIdentity(payload.openAiOAuthAccountId ?: throw OpenAiOAuthFailure("LOGIN_REQUIRED"),
            payload.openAiOAuthRevision ?: throw OpenAiOAuthFailure("LOGIN_REQUIRED"))
        selectionGuard.withSelection { requireSelection(payload.providerSelectionRevision, payload.sourceSnapshot) }
        sessions.currentIdentity()
        coroutineScope {
            val watcher = launch {
                combine(sessions.identity, preferenceManager.observe()) { current, _ ->
                    current != identity || preferenceManager.source != ServerSource.OPEN_AI_OAUTH ||
                        preferenceManager.providerSelectionRevision != payload.providerSelectionRevision
                }.first { it }
                throw OpenAiOAuthFailure("IDENTITY_CHANGED")
            }
            try {
                selectionGuard.withSelection { requireSelection(payload.providerSelectionRevision, payload.sourceSnapshot) }
                val encoded = sessions.generate(identity, payload.prompt, payload.width, payload.height)
                val bytes = try { Base64.Default.decode(encoded.filterNot(Char::isWhitespace)) }
                    catch (_: Exception) { throw OpenAiOAuthFailure("INVALID_RESPONSE") }
                val dimensions = try { decodedImageDimensions(bytes) }
                    catch (_: Exception) { throw OpenAiOAuthFailure("INVALID_RESPONSE") }
                val candidate = payload.copy(openAiCompatibleModel = "gpt-image-2").toOpenAiCompatibleResult(
                    Base64.Default.encode(bytes), dimensions.first, dimensions.second, Clock.System.now().toEpochMilliseconds(),
                )
                selectionGuard.withSelection {
                    sessions.withIdentity(identity) {
                        requireSelection(payload.providerSelectionRevision, payload.sourceSnapshot)
                        insertGenerationResult(candidate.copy(
                            openAiOAuthIdentity = OpenAiOAuthIdentity(identity.accountId, identity.revision),
                            providerSelectionRevision = payload.providerSelectionRevision,
                        ))
                    }
                }
            } finally { watcher.cancel() }
        }
    }

    /**
     * Rejects old queued work after any committed provider replacement, including away/back switches.
     * @param revision Durable selection revision captured when the user submitted this work.
     * @param source Source captured with the same revision.
     * @author Dmitriy Moroz
     */
    private fun requireSelection(revision: Long?, source: ServerSource?) {
        if (source != ServerSource.OPEN_AI_OAUTH || revision == null ||
            preferenceManager.source != source || preferenceManager.providerSelectionRevision != revision)
            throw OpenAiOAuthFailure("IDENTITY_CHANGED")
    }
}

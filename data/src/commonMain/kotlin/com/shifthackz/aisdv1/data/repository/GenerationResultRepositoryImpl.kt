package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.core.CoreMediaStoreRepository
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.AiGenerationResultPreview
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.feature.openaioauth.auth.OAuthSessionManager
import com.shifthackz.aisdv1.feature.openaioauth.model.OAuthIdentity
import com.shifthackz.aisdv1.data.feature.openaioauth.oauthCall
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import kotlinx.coroutines.flow.Flow

/**
 * Persists and exports gallery results while rejecting stale OAuth account/source snapshots.
 * @param preferenceManager Current source selection and export preferences.
 * @param mediaStoreGateway Platform media export adapter.
 * @param localDataSource Gallery database operations.
 * @param oauthSessions Lazily resolves the protected session guard for OAuth results.
 * @param selectionGuard Serializes final writes with provider replacement.
 *
 * @author Dmitriy Moroz
 */
internal class GenerationResultRepositoryImpl(
    private val preferenceManager: PreferenceManager,
    mediaStoreGateway: MediaStoreGateway,
    /**
     * Exposes the `localDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val localDataSource: GenerationResultDataSource.Local,
    private val oauthSessions: (() -> OAuthSessionManager)? = null,
    private val selectionGuard: ProviderSelectionGuard = ProviderSelectionGuard(),
) : CoreMediaStoreRepository(
    preferenceManager,
    mediaStoreGateway,
), GenerationResultRepository {

    /**
     * Loads SDAI data through `getAll`.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAll(): List<AiGenerationResult> = localDataSource.queryAll()

    /**
     * Loads SDAI data through `getAllIds`.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllIds(): List<Long> = localDataSource.queryIds()

    /**
     * Loads SDAI data through `getPage`.
     *
     * @param limit limit value consumed by the API.
     * @param offset offset value consumed by the API.
     * @return Result produced by `getPage`.
     * @author Dmitriy Moroz
     */
    override suspend fun getPage(limit: Int, offset: Int): List<AiGenerationResult> =
        localDataSource.queryPage(limit, offset)

    /**
     * Loads SDAI data through `getPagePreview`.
     *
     * @param limit limit value consumed by the API.
     * @param offset offset value consumed by the API.
     * @return Result produced by `getPagePreview`.
     * @author Dmitriy Moroz
     */
    override suspend fun getPagePreview(limit: Int, offset: Int): List<AiGenerationResultPreview> =
        localDataSource.queryPagePreview(limit, offset)

    /**
     * Loads SDAI data through `observePage`.
     *
     * @param limit limit value consumed by the API.
     * @param offset offset value consumed by the API.
     * @return Result produced by `observePage`.
     * @author Dmitriy Moroz
     */
    override fun observePage(limit: Int, offset: Int): Flow<List<AiGenerationResult>> =
        localDataSource.observePage(limit, offset)

    /**
     * Loads SDAI data through `observePagePreview`.
     *
     * @param limit limit value consumed by the API.
     * @param offset offset value consumed by the API.
     * @return Result produced by `observePagePreview`.
     * @author Dmitriy Moroz
     */
    override fun observePagePreview(limit: Int, offset: Int): Flow<List<AiGenerationResultPreview>> =
        localDataSource.observePagePreview(limit, offset)

    /**
     * Loads SDAI data through `observeCount`.
     *
     * @author Dmitriy Moroz
     */
    override fun observeCount(): Flow<Int> = localDataSource.observeCount()

    /**
     * Loads SDAI data through `getById`.
     *
     * @param id identifier of the target entity.
     * @author Dmitriy Moroz
     */
    override suspend fun getById(id: Long): AiGenerationResult = localDataSource.queryById(id)

    /**
     * Loads SDAI data through `getByIds`.
     *
     * @param idList id list value consumed by the API.
     * @return Result produced by `getByIds`.
     * @author Dmitriy Moroz
     */
    override suspend fun getByIds(idList: List<Long>): List<AiGenerationResult> =
        localDataSource.queryByIdList(idList)

    /**
     * Inserts and optionally exports a result under its original OAuth account and selection revision.
     * Rejects stale unsaved OAuth results before persistence; ordinary gallery results use normal storage.
     * @param result Image and transient identity snapshot captured during generation.
     * @return Database identifier of the persisted gallery result.
     * @throws OpenAiOAuthFailure When the result's login or provider selection is no longer current.
     * @author Dmitriy Moroz
     */
    override suspend fun insert(result: AiGenerationResult): Long {
        val identity = result.openAiOAuthIdentity
        if (identity != null) return oauthCall {
            selectionGuard.withSelection {
                val sessions = oauthSessions?.invoke() ?: throw OpenAiOAuthFailure("LOGIN_REQUIRED")
                sessions.currentIdentity()
                sessions.withIdentity(OAuthIdentity(identity.accountId, identity.revision)) {
                    if (preferenceManager.source != ServerSource.OPEN_AI_OAUTH || result.providerSelectionRevision == null ||
                        preferenceManager.providerSelectionRevision != result.providerSelectionRevision)
                        throw OpenAiOAuthFailure("IDENTITY_CHANGED")
                    insertAndExport(result)
                }
            }
        }
        return insertAndExport(result)
    }

    /**
     * Performs the SDAI side effect handled by `deleteById`.
     *
     * @param id identifier of the target entity.
     * @author Dmitriy Moroz
     */
    override suspend fun deleteById(id: Long) {
        localDataSource.deleteById(id)
    }

    /**
     * Performs the SDAI side effect handled by `deleteByIdList`.
     *
     * @param idList id list value consumed by the API.
     * @author Dmitriy Moroz
     */
    override suspend fun deleteByIdList(idList: List<Long>) {
        localDataSource.deleteByIdList(idList)
    }

    /**
     * Performs the SDAI side effect handled by `deleteAll`.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun deleteAll() {
        localDataSource.deleteAll()
    }

    /**
     * Performs the SDAI side effect handled by `setVisibilityByIds`.
     *
     * @param ids ids value consumed by the API.
     * @param hidden hidden value consumed by the API.
     * @author Dmitriy Moroz
     */
    override suspend fun setVisibilityByIds(ids: List<Long>, hidden: Boolean) {
        localDataSource.updateHiddenByIdList(ids, hidden)
    }

    /**
     * Performs the SDAI side effect handled by `setLikedByIds`.
     *
     * @param ids ids value consumed by the API.
     * @param liked liked value consumed by the API.
     * @author Dmitriy Moroz
     */
    override suspend fun setLikedByIds(ids: List<Long>, liked: Boolean) {
        localDataSource.updateLikedByIdList(ids, liked)
    }

    /**
     * Converts SDAI data with `toggleVisibility`.
     *
     * @param id identifier of the target entity.
     * @return Result produced by `toggleVisibility`.
     * @author Dmitriy Moroz
     */
    override suspend fun toggleVisibility(id: Long): Boolean {
        val updated = localDataSource.queryById(id).let { it.copy(hidden = !it.hidden) }
        localDataSource.insert(updated)
        return localDataSource.queryById(id).hidden
    }

    /**
     * Converts SDAI data with `toggleLike`.
     *
     * @param id identifier of the target entity.
     * @return Result produced by `toggleLike`.
     * @author Dmitriy Moroz
     */
    override suspend fun toggleLike(id: Long): Boolean {
        val updated = localDataSource.queryById(id).let { it.copy(liked = !it.liked) }
        localDataSource.insert(updated)
        return localDataSource.queryById(id).liked
    }

    /**
     * Writes the gallery row and applies normal media export preferences.
     * @param result Decoded image candidate whose identity has already been checked when applicable.
     * @author Dmitriy Moroz
     */
    private suspend fun insertAndExport(result: AiGenerationResult): Long {
        val id = localDataSource.insert(result)
        exportToMediaStoreAsync(result)
        return id
    }
}

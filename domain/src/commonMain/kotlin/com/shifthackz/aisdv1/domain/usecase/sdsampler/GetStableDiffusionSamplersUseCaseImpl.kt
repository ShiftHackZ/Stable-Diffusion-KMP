package com.shifthackz.aisdv1.domain.usecase.sdsampler

import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import kotlinx.coroutines.CancellationException

/**
 * Refreshes the sampler cache and preserves a usable cached list during transient server failures.
 *
 * @param repository Fetches samplers from the configured server and reads their cache.
 * @author Dmitriy Moroz
 */
class GetStableDiffusionSamplersUseCaseImpl(
    private val repository: StableDiffusionSamplersRepository,
) : GetStableDiffusionSamplersUseCase {

    /**
     * Returns refreshed samplers or a populated fallback without swallowing cancellation.
     *
     * @return Server samplers, or cached samplers if fetching fails.
     * @throws Exception When fetching fails and the cache is empty.
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke(): List<StableDiffusionSampler> {
        try {
            repository.fetchSamplers()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            return repository.getSamplers().ifEmpty { throw error }
        }
        return repository.getSamplers()
    }
}

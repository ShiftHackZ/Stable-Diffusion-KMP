package com.shifthackz.aisdv1.domain.usecase.sdsampler

import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler

/**
 * Refreshes AUTOMATIC1111 samplers, falling back to a populated cache when the server is unavailable.
 *
 * @author Dmitriy Moroz
 */
interface GetStableDiffusionSamplersUseCase {
    /**
     * Fetches the current server list and returns cached samplers after a recoverable fetch failure.
     * A failure with no cached samplers is propagated so callers can offer recovery.
     *
     * @return The latest sampler list, or a nonempty cached list when refreshing fails.
     * @throws Exception When refreshing fails and no cached samplers are available.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): List<StableDiffusionSampler>
}

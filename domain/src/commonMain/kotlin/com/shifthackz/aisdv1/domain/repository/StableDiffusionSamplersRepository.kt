package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler

/**
 * Refreshes and reads disposable AUTOMATIC1111 sampler metadata for the selected connection.
 *
 * @author Dmitriy Moroz
 */
interface StableDiffusionSamplersRepository {
    /**
     * Refreshes sampler metadata without publishing results from a connection that was replaced.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchSamplers()

    /**
     * Reads a complete cache snapshot belonging to the current server and credentials.
     *
     * @return Cached samplers, or an empty list when this connection has no valid cache.
     * @author Dmitriy Moroz
     */
    suspend fun getSamplers(): List<StableDiffusionSampler>
}

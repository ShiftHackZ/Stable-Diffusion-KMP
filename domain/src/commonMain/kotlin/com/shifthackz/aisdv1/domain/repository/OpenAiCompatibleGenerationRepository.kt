package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Independent image-server operations and normal gallery persistence.
 *
 * @author Dmitriy Moroz
 */
interface OpenAiCompatibleGenerationRepository {
    /**
     * Probes candidate credentials without changing saved configuration.
     *
     * @param url Complete API prefix.
     * @param apiKey Optional bearer token; blank omits Authorization.
     * @author Dmitriy Moroz
     */
    suspend fun probe(url: String, apiKey: String): OpenAiCompatibleConnection

    /**
     * Generates and persists one validated image using current compatible-server settings.
     *
     * @param payload Image request with an arbitrary model ID and positive dimensions.
     * @author Dmitriy Moroz
     */
    suspend fun generateFromText(payload: TextToImagePayload): AiGenerationResult
}

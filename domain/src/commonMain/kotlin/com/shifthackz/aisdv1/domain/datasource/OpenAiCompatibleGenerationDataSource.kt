package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Data boundaries for the generic image server.
 *
 * @author Dmitriy Moroz
 */
interface OpenAiCompatibleGenerationDataSource {
    /**
     * Remote operations return only images that decode successfully.
     *
     * @author Dmitriy Moroz
     */
    interface Remote {
        /**
         * Probes model discovery without publishing candidate configuration.
         *
         * @param url Complete HTTP(S) API prefix.
         * @param apiKey Optional bearer token.
         * @author Dmitriy Moroz
         */
        suspend fun probe(url: String, apiKey: String): OpenAiCompatibleConnection

        /**
         * Generates one image and resolves either Base64 or a credential-free download.
         *
         * @param url Complete HTTP(S) API prefix.
         * @param apiKey Optional bearer token.
         * @param payload Generation parameters.
         * @author Dmitriy Moroz
         */
        suspend fun textToImage(url: String, apiKey: String, payload: TextToImagePayload): AiGenerationResult
    }
}

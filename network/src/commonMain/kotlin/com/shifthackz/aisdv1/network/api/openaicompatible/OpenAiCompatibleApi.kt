package com.shifthackz.aisdv1.network.api.openaicompatible

import com.shifthackz.aisdv1.network.request.OpenAiCompatibleRequest
import com.shifthackz.aisdv1.network.response.OpenAiCompatibleResponse

/**
 * Transport for complete user-selected API prefixes.
 *
 * @author Dmitriy Moroz
 */
interface OpenAiCompatibleApi {
    /**
     * Returns false only when model discovery is unsupported (404/405).
     *
     * @param baseUrl Complete HTTP(S) API prefix.
     * @param apiKey Optional bearer token.
     * @author Dmitriy Moroz
     */
    suspend fun probe(baseUrl: String, apiKey: String): Boolean

    /**
     * Sends one generation request without adding an API version to the prefix.
     *
     * @param baseUrl Complete HTTP(S) API prefix.
     * @param apiKey Optional bearer token.
     * @param request Minimal image generation payload.
     * @author Dmitriy Moroz
     */
    suspend fun generate(baseUrl: String, apiKey: String, request: OpenAiCompatibleRequest): OpenAiCompatibleResponse

    /**
     * Downloads encoded image bytes with no provider Authorization, including redirects.
     *
     * @param url Absolute HTTP(S) image URL without user information.
     * @author Dmitriy Moroz
     */
    suspend fun downloadImage(url: String): ByteArray
}

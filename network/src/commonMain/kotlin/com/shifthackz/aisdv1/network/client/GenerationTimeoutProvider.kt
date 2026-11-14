package com.shifthackz.aisdv1.network.client

import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder

/**
 * Supplies the current generation request limit without coupling networking to preferences.
 *
 * @author Dmitriy Moroz
 */
fun interface GenerationTimeoutProvider {
    /**
     * Reads the limit when a request starts; zero disables request and socket timeouts.
     *
     * @author Dmitriy Moroz
     */
    fun timeoutMillis(): Long

    companion object {
        /**
         * Two-minute default for clients constructed outside the application dependency graph.
         *
         * @author Dmitriy Moroz
         */
        val Default = GenerationTimeoutProvider { 120_000L }
    }
}

/**
 * Overrides generation waiting limits while retaining the client's connection timeout.
 *
 * @param provider Source of the latest persisted timeout, evaluated once for this request.
 * @author Dmitriy Moroz
 */
internal fun HttpRequestBuilder.applyGenerationTimeout(provider: GenerationTimeoutProvider) {
    val limit = provider.timeoutMillis().takeIf { it > 0L } ?: HttpTimeoutConfig.INFINITE_TIMEOUT_MS
    timeout {
        requestTimeoutMillis = limit
        socketTimeoutMillis = limit
    }
}

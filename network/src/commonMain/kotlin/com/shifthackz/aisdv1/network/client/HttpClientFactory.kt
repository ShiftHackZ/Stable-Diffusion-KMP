package com.shifthackz.aisdv1.network.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Executes the `platformHttpClientEngine` step in the SDAI network layer.
 *
 * @return Result produced by `platformHttpClientEngine`.
 * @author Dmitriy Moroz
 */
expect fun platformHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

/**
 * Creates the SDAI value produced by `createConfiguredHttpClient`.
 *
 * @param json json value consumed by the API.
 * @param installContentNegotiation install content negotiation value consumed by the API.
 * @param configure configure value consumed by the API.
 * @author Dmitriy Moroz
 */
fun createConfiguredHttpClient(
    json: Json = defaultNetworkJson,
    installContentNegotiation: Boolean = true,
    configure: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
): HttpClient = HttpClient(platformHttpClientEngine()) {
    expectSuccess = true

    if (installContentNegotiation) {
        install(ContentNegotiation) {
            json(json)
            json(json, contentType = ContentType.Application.OctetStream)
            json(json, contentType = ContentType.Text.Plain)
        }
    }

    install(HttpTimeout) {
        connectTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        requestTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        socketTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
    }
    install(ConfigurableRequestTimeout)

    install(Logging) {
        level = LogLevel.INFO
    }

    configure()
}

/**
 * Exposes the `defaultNetworkJson` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
val defaultNetworkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

/**
 * Exposes the `DEFAULT_TIMEOUT_MILLIS` value used by the SDAI network layer.
 *
 * @author Dmitriy Moroz
 */
private const val DEFAULT_TIMEOUT_MILLIS = 120_000L

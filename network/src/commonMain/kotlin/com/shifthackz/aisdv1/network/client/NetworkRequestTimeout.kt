package com.shifthackz.aisdv1.network.client

import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.timeout
import kotlin.concurrent.Volatile

/**
 * Process-wide, user-configurable request timeout applied to every SDAI HTTP request.
 *
 * Slow self-hosted backends (for example an A1111-compatible server that cold-loads a large
 * model, or generates with CPU offload) can take longer than the default to answer a single
 * txt2img/img2img call. The value is read per request, so changes apply without rebuilding
 * HTTP clients.
 *
 * @author Dmitriy Moroz
 */
object NetworkRequestTimeout {

    /** Default request timeout, in seconds. */
    const val DEFAULT_SECONDS = 120

    /** Value meaning "no request timeout". */
    const val NO_LIMIT = 0

    @Volatile
    private var seconds: Int = DEFAULT_SECONDS

    /**
     * Updates the request timeout.
     *
     * @param value timeout in seconds; [NO_LIMIT] (or any non-positive value) disables it.
     */
    fun setSeconds(value: Int) {
        seconds = value.coerceAtLeast(NO_LIMIT)
    }

    /** Current timeout in milliseconds, or [HttpTimeoutConfig.INFINITE_TIMEOUT_MS] for no limit. */
    val millis: Long
        get() = seconds.let { if (it == NO_LIMIT) HttpTimeoutConfig.INFINITE_TIMEOUT_MS else it * 1_000L }
}

/**
 * Applies [NetworkRequestTimeout] to the request and socket timeouts of each outgoing call.
 * Requires the `HttpTimeout` plugin to be installed on the client.
 */
internal val ConfigurableRequestTimeout = createClientPlugin("ConfigurableRequestTimeout") {
    onRequest { request, _ ->
        val millis = NetworkRequestTimeout.millis
        request.timeout {
            requestTimeoutMillis = millis
            socketTimeoutMillis = millis
        }
    }
}

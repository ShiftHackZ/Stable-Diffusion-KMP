package com.shifthackz.aisdv1.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkRequestTimeoutTest {

    @After
    fun tearDown() {
        NetworkRequestTimeout.setSeconds(NetworkRequestTimeout.DEFAULT_SECONDS)
    }

    @Test
    fun `given default, expected 120 seconds`() {
        assertEquals(120_000L, NetworkRequestTimeout.millis)
    }

    @Test
    fun `given zero, expected infinite timeout`() {
        NetworkRequestTimeout.setSeconds(0)
        assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, NetworkRequestTimeout.millis)
    }

    @Test
    fun `given negative, expected infinite timeout`() {
        NetworkRequestTimeout.setSeconds(-5)
        assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, NetworkRequestTimeout.millis)
    }

    @Test
    fun `given configured value, expected applied to each request`() = runBlocking {
        val seen = mutableListOf<Pair<Long?, Long?>>()
        val client = HttpClient(MockEngine { request ->
            val config = request.getCapabilityOrNull(HttpTimeoutCapabilityKey)
            seen += config?.requestTimeoutMillis to config?.socketTimeoutMillis
            respondOk()
        }) {
            install(HttpTimeout) { requestTimeoutMillis = 120_000L }
            install(ConfigurableRequestTimeout)
        }

        NetworkRequestTimeout.setSeconds(600)
        client.get("http://localhost/a")
        NetworkRequestTimeout.setSeconds(0)
        client.get("http://localhost/b")

        assertEquals(
            listOf(
                600_000L to 600_000L,
                HttpTimeoutConfig.INFINITE_TIMEOUT_MS to HttpTimeoutConfig.INFINITE_TIMEOUT_MS,
            ),
            seen,
        )
    }
}

private val HttpTimeoutCapabilityKey = io.ktor.client.plugins.HttpTimeoutCapability

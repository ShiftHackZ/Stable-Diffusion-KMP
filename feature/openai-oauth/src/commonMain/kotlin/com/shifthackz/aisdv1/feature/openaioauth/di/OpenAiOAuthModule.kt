package com.shifthackz.aisdv1.feature.openaioauth.di

import com.shifthackz.aisdv1.feature.openaioauth.api.ImageApi
import com.shifthackz.aisdv1.feature.openaioauth.api.OAuthApi
import com.shifthackz.aisdv1.feature.openaioauth.auth.OAuthSessionManager
import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import org.koin.mp.KoinPlatform
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Credential-safe feature wiring, selected by the application host.
 * @author Dmitriy Moroz
 */
val openAiOAuthModule = module {
    includes(platformSessionModule())
    single {
        val client = createConfiguredHttpClient(connectionTimeoutProvider = get()) {
            followRedirects = false
            install(Logging) { level = LogLevel.NONE }
        }
        OAuthSessionManager(OAuthApi(client), ImageApi(client, get()), get())
    }
}

/** Platform protected storage factory. @author Dmitriy Moroz */
internal expect fun platformSessionModule(): Module

/**
 * Idempotent host registration after the shared graph is initialized, before composing UI.
 * @author Dmitriy Moroz
 */
fun registerOpenAiOAuthFeature() {
    if (KoinPlatform.getKoin().getOrNull<OAuthSessionManager>() == null) loadKoinModules(openAiOAuthModule)
}

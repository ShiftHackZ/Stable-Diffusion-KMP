package com.shifthackz.aisdv1.feature.openaioauth.di

import com.shifthackz.aisdv1.feature.openaioauth.storage.SessionStore
import com.shifthackz.aisdv1.feature.openaioauth.storage.SecureSessionStore
import org.koin.dsl.module

/**
 * Binds one device-only Keychain session store to the feature.
 * @return iOS protected-storage Koin bindings without defaults fallback.
 * @author Dmitriy Moroz
 */
internal actual fun platformSessionModule() = module { single<SessionStore> { SecureSessionStore() } }

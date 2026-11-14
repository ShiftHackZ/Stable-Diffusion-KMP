package com.shifthackz.aisdv1.feature.openaioauth.di

import com.shifthackz.aisdv1.feature.openaioauth.storage.SessionStore
import com.shifthackz.aisdv1.feature.openaioauth.storage.SecureSessionStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Binds one application-scoped Keystore-encrypted session store to the feature.
 * @return Android protected-storage Koin bindings without plaintext fallback.
 * @author Dmitriy Moroz
 */
internal actual fun platformSessionModule() = module { single<SessionStore> { SecureSessionStore(androidContext()) } }

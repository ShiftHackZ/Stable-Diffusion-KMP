package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.data.preference.KeyValueAuthorizationStore
import com.shifthackz.aisdv1.data.preference.KeyValueConfigurationStore
import com.shifthackz.aisdv1.data.preference.PreferenceManagerImpl
import com.shifthackz.aisdv1.data.preference.SessionPreferenceImpl
import com.shifthackz.aisdv1.data.preference.createKeyValueStore
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.preference.SessionPreference
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import org.koin.dsl.module

/**
 * Exposes the `preferenceDataModule` value used by the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
val preferenceDataModule = module {
    single<GenerationTimeoutProvider> {
        val preferences = get<PreferenceManager>()
        GenerationTimeoutProvider { preferences.generationRequestTimeout.seconds * 1000L }
    }
    single<PreferenceManager> {
        PreferenceManagerImpl(
            keyValueStore = createKeyValueStore(KEY_PREFERENCE_MANAGER),
        )
    }
    single<ConfigurationStore> {
        KeyValueConfigurationStore(
            keyValueStore = createKeyValueStore(KEY_PREFERENCE_MANAGER),
        )
    }
    single<AuthorizationStore> {
        KeyValueAuthorizationStore(
            keyValueStore = createKeyValueStore(KEY_PREFERENCE_AUTHORIZATION),
        )
    }
    single<SessionPreference> {
        SessionPreferenceImpl()
    }
}

/**
 * Exposes the `KEY_PREFERENCE_MANAGER` value used by the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
private const val KEY_PREFERENCE_MANAGER = "aisdv1_preference_manager"
/**
 * Exposes the `KEY_PREFERENCE_AUTHORIZATION` value used by the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
private const val KEY_PREFERENCE_AUTHORIZATION = "sdai_authorization_preference"

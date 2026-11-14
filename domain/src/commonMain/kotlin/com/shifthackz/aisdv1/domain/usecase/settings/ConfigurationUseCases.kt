package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard

/**
 * Implements `DefaultGetConfigurationUseCase` behavior in the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultGetConfigurationUseCaseImpl(
    /**
     * Exposes the `configurationStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val configurationStore: ConfigurationStore,
    /**
     * Exposes the `authorizationStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val authorizationStore: AuthorizationStore,
) : GetConfigurationUseCase {

    /**
     * Executes the `invoke` step in the SDAI domain layer.
     *
     * @return Result produced by `invoke`.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): Configuration =
        configurationStore.getConfiguration(
            authCredentials = authorizationStore.getAuthorizationCredentials(),
        )
}

/**
 * Replaces provider configuration while serializing identity-sensitive gallery/UI publication.
 * @param configurationStore Persists provider configuration and the durable source revision.
 * @param authorizationStore Stores the provider's application authorization settings.
 * @param preferenceManager Publishes the completed configuration to observers.
 * @param providerSelectionGuard Prevents source replacement during guarded result publication.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultSetServerConfigurationUseCaseImpl(
    /**
     * Exposes the `configurationStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val configurationStore: ConfigurationStore,
    /**
     * Exposes the `authorizationStore` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val authorizationStore: AuthorizationStore,
    /**
     * Exposes the `preferenceManager` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    private val providerSelectionGuard: ProviderSelectionGuard = ProviderSelectionGuard(),
) : SetServerConfigurationUseCase {

    /**
     * Commits source selection and refreshes observers under the shared publication guard.
     * A source replacement advances its durable revision so older queued work remains invalid.
     * @param configuration Complete provider configuration to persist.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(configuration: Configuration) = providerSelectionGuard.withSelection {
        authorizationStore.storeAuthorizationCredentials(configuration.authCredentials)
        configurationStore.setConfiguration(configuration)
        preferenceManager.refresh()
    }
}

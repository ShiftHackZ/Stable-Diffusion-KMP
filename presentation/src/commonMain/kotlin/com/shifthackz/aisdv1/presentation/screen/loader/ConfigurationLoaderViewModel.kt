package com.shifthackz.aisdv1.presentation.screen.loader

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.EmptyIntent
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.presentation.navigation.router.ConfigurationLoaderRouter
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * Coordinates `ConfigurationLoaderViewModel` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
class ConfigurationLoaderViewModel(
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Exposes the `dataPreLoaderUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dataPreLoaderUseCase: DataPreLoaderUseCase,
    /**
     * Exposes the `getConfigurationUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val getConfigurationUseCase: GetConfigurationUseCase,
    /**
     * Exposes the `router` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val router: ConfigurationLoaderRouter,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ConfigurationLoaderState, EmptyIntent, EmptyEffect>(
    initialState = ConfigurationLoaderState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            withContext(dispatchersProvider.immediate) {
                emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Fetching))
            }

            runCatching {
                val configuration = getConfigurationUseCase()
                if (configuration.requiresRemotePreload()) {
                    withTimeout(STARTUP_PRELOAD_TIMEOUT_MILLIS.milliseconds) {
                        dataPreLoaderUseCase()
                    }
                }
                withContext(dispatchersProvider.immediate) {
                    emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Launching))
                    router.navigateToHomeScreen()
                }
            }.onFailure { t ->
                withContext(dispatchersProvider.immediate) {
                    emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Failed))
                    router.navigateToHomeScreen()
                }
                onError(t)
            }
        }
    }

    override fun processIntent(intent: EmptyIntent) = Unit
}

/**
 * Exposes the `STARTUP_PRELOAD_TIMEOUT_MILLIS` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val STARTUP_PRELOAD_TIMEOUT_MILLIS = 3_000L

/**
 * Executes the `requiresRemotePreload` step in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.requiresRemotePreload(): Boolean = when (source) {
    ServerSource.AUTOMATIC1111 -> serverUrl.isMobileRemoteEndpoint()
    ServerSource.SWARM_UI -> swarmUiUrl.isMobileRemoteEndpoint()
    ServerSource.HORDE,
    ServerSource.HUGGING_FACE,
    ServerSource.OPEN_AI,
    ServerSource.OPEN_AI_COMPATIBLE,
    ServerSource.STABILITY_AI,
    ServerSource.FAL_AI,
    ServerSource.ARLI_AI,
    ServerSource.LOCAL_MICROSOFT_ONNX,
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
    ServerSource.LOCAL_APPLE_CORE_ML,
    ServerSource.LOCAL_APPLE_BONSAI,
    -> false
}

/**
 * Executes the `isMobileRemoteEndpoint` step in the SDAI presentation layer.
 *
 * @return Result produced by `isMobileRemoteEndpoint`.
 * @author Dmitriy Moroz
 */
private fun String.isMobileRemoteEndpoint(): Boolean {
    val value = trim().lowercase()
    return value.isNotBlank() &&
        !value.startsWith("http://localhost") &&
        !value.startsWith("https://localhost") &&
        !value.startsWith("http://127.0.0.1") &&
        !value.startsWith("https://127.0.0.1") &&
        !value.startsWith("http://[::1]") &&
        !value.startsWith("https://[::1]")
}

package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Keeps identity subscription alive independently from protected-storage initialization.
 * A temporarily locked Keychain is retried; explicit login/reset can recover a corrupt envelope.
 * @param gateway Token-free session boundary.
 * @param onIdentity Receives login/logout changes for the lifetime of the owning screen scope.
 * @param onUnavailable Receives a safe initialization error once per failure category.
 * @param onReady Clears any initialization warning after protected storage becomes readable.
 * @author Dmitriy Moroz
 */
internal suspend fun observeOpenAiOAuthIdentity(
    gateway: OpenAiOAuthGateway,
    onIdentity: (OpenAiOAuthIdentity?) -> Unit,
    onUnavailable: (OpenAiOAuthFailure) -> Unit,
    onReady: () -> Unit = {},
): Nothing = coroutineScope {
    launch { gateway.observeIdentity().collect(onIdentity) }
    var lastFailure: String? = null
    while (true) {
        try {
            gateway.currentIdentity()
            onReady()
            break
        } catch (error: CancellationException) {
            throw error
        } catch (error: OpenAiOAuthFailure) {
            if (error.category != lastFailure) onUnavailable(error)
            lastFailure = error.category
            delay(5_000)
        }
    }
    awaitCancellation()
}

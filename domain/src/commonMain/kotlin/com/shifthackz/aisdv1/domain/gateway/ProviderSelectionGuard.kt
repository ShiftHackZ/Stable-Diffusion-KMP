package com.shifthackz.aisdv1.domain.gateway

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes provider replacement with final identity-sensitive gallery writes.
 * Network calls must run outside this short critical section.
 * @author Dmitriy Moroz
 */
class ProviderSelectionGuard {
    private val mutex = Mutex()

    /**
     * Runs a configuration replacement or final persistence action exclusively.
     * @param block Short application state transition or gallery transaction.
     * @author Dmitriy Moroz
     */
    suspend fun <T> withSelection(block: suspend () -> T): T = mutex.withLock { block() }
}

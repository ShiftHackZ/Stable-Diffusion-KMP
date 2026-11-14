package com.shifthackz.aisdv1.feature.openaioauth.storage

/**
 * Persists a complete credential envelope atomically in platform protected storage.
 * No plaintext fallback is permitted; failures must propagate.
 * @author Dmitriy Moroz
 */
internal interface SessionStore {
    /**
     * Reads the complete protected envelope without exposing it outside the feature.
     * @return Encoded session, or null when no protected item exists.
     * @throws Exception When protected storage is unavailable or its envelope is invalid.
     * @author Dmitriy Moroz
     */
    fun read(): String?

    /**
     * Atomically replaces or deletes the protected envelope before reporting success.
     * A failed operation must preserve the prior envelope and must not use plaintext storage.
     * @param value Complete encoded envelope; null deletes it.
     * @throws Exception When protected storage cannot complete the replacement or deletion.
     * @author Dmitriy Moroz
     */
    fun replace(value: String?)
}

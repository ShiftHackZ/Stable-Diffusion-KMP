package com.shifthackz.aisdv1.data.preference

/**
 * Defines the `KeyValueStore` contract for the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal interface KeyValueStore {
    /**
     * Loads SDAI data through `getString`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getString`.
     * @author Dmitriy Moroz
     */
    fun getString(key: String, default: String = ""): String
    /**
     * Executes the `putString` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    fun putString(key: String, value: String)
    /**
     * Commits an identity-sensitive value before reporting success to a queued-work producer.
     * @param key Stable application preference key.
     * @param value Complete atomic value to persist.
     * @author Dmitriy Moroz
     */
    fun putStringDurably(key: String, value: String) = putString(key, value)
    /**
     * Loads SDAI data through `getBoolean`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getBoolean`.
     * @author Dmitriy Moroz
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean
    /**
     * Executes the `putBoolean` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    fun putBoolean(key: String, value: Boolean)
    /**
     * Loads SDAI data through `getInt`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getInt`.
     * @author Dmitriy Moroz
     */
    fun getInt(key: String, default: Int = 0): Int
    /**
     * Executes the `putInt` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    fun putInt(key: String, value: Int)
}

/**
 * Creates the SDAI value produced by `createKeyValueStore`.
 *
 * @param name name value consumed by the API.
 * @return Result produced by `createKeyValueStore`.
 * @author Dmitriy Moroz
 */
internal expect fun createKeyValueStore(name: String): KeyValueStore

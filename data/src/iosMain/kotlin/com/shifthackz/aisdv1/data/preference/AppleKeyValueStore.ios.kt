package com.shifthackz.aisdv1.data.preference

import platform.Foundation.NSUserDefaults

/**
 * Coordinates `NsUserDefaultsKeyValueStore` behavior in the SDAI data layer.
 *
 * @param name name value consumed by the API.
 * @author Dmitriy Moroz
 */
private class NsUserDefaultsKeyValueStore(
    name: String,
) : KeyValueStore {

    /**
     * Exposes the `defaults` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val defaults = NSUserDefaults(suiteName = name)
        ?: NSUserDefaults.standardUserDefaults

    /**
     * Loads SDAI data through `getString`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getString`.
     * @author Dmitriy Moroz
     */
    override fun getString(key: String, default: String): String =
        defaults.stringForKey(key) ?: default

    /**
     * Executes the `putString` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    /**
     * Synchronizes a complete preference value before returning; synchronization failure throws.
     * @param key Stable preference key.
     * @param value Complete source/revision envelope to persist.
     * @author Dmitriy Moroz
     */
    override fun putStringDurably(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        check(defaults.synchronize()) { "Provider selection could not be persisted" }
    }

    /**
     * Loads SDAI data through `getBoolean`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getBoolean`.
     * @author Dmitriy Moroz
     */
    override fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)

    /**
     * Executes the `putBoolean` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    /**
     * Loads SDAI data through `getInt`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getInt`.
     * @author Dmitriy Moroz
     */
    override fun getInt(key: String, default: Int): Int =
        if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key).toInt()

    /**
     * Executes the `putInt` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}

/**
 * Creates the SDAI value produced by `createKeyValueStore`.
 *
 * @param name name value consumed by the API.
 * @return Result produced by `createKeyValueStore`.
 * @author Dmitriy Moroz
 */
internal actual fun createKeyValueStore(name: String): KeyValueStore =
    NsUserDefaultsKeyValueStore(name)

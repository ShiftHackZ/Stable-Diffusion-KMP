package com.shifthackz.aisdv1.data.preference

import android.content.Context
import android.content.SharedPreferences
import org.koin.mp.KoinPlatform

/**
 * Coordinates `AndroidPreferencesKeyValueStore` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
private class AndroidPreferencesKeyValueStore(
    /**
     * Exposes the `preferences` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferences: SharedPreferences,
) : KeyValueStore {

    /**
     * Loads SDAI data through `getString`.
     *
     * @param key key value consumed by the API.
     * @param default default value consumed by the API.
     * @return Result produced by `getString`.
     * @author Dmitriy Moroz
     */
    override fun getString(key: String, default: String): String =
        preferences.getString(key, default) ?: default

    /**
     * Executes the `putString` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    /**
     * Commits an atomic preference value synchronously; a failed disk write throws.
     * @param key Stable preference key.
     * @param value Complete source/revision envelope to commit.
     * @author Dmitriy Moroz
     */
    override fun putStringDurably(key: String, value: String) {
        check(preferences.edit().putString(key, value).commit()) { "Provider selection could not be persisted" }
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
        preferences.getBoolean(key, default)

    /**
     * Executes the `putBoolean` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
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
        preferences.getInt(key, default)

    /**
     * Executes the `putInt` step in the SDAI data layer.
     *
     * @param key key value consumed by the API.
     * @param value value value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }
}

/**
 * Creates the SDAI value produced by `createKeyValueStore`.
 *
 * @param name name value consumed by the API.
 * @return Result produced by `createKeyValueStore`.
 * @author Dmitriy Moroz
 */
internal actual fun createKeyValueStore(name: String): KeyValueStore {
    val context = KoinPlatform.getKoin().get<Context>()
    return AndroidPreferencesKeyValueStore(
        preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE),
    )
}

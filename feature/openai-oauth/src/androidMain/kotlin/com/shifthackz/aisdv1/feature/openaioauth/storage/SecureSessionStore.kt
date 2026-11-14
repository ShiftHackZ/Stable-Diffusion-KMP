package com.shifthackz.aisdv1.feature.openaioauth.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Keystore-encrypted session envelope. A synchronous single-key commit replaces/deletes atomically.
 * @param context Application context used only for protected app-private storage.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION")
internal class SecureSessionStore(context: Context) : SessionStore {
    private val preferences by lazy { EncryptedSharedPreferences.create(
        context,
        "openai_oauth_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    ) }

    /**
     * Decrypts the stored envelope with the Android Keystore-backed preferences.
     * @return Complete session envelope, or null when signed out; decryption failures propagate.
     * @author Dmitriy Moroz
     */
    override fun read(): String? = preferences.getString("session", null)

    /**
     * Commits one encrypted session entry synchronously; storage failures propagate to the caller.
     * @param value Complete encoded envelope, or null to delete the protected entry.
     * @author Dmitriy Moroz
     */
    override fun replace(value: String?) {
        check(preferences.edit().apply {
            if (value == null) remove("session") else putString("session", value)
        }.commit()) { "Protected session write failed" }
    }
}

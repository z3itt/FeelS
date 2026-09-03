package com.feels.core.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getDatabasePassphrase(): ByteArray {
        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) {
            return existing.toByteArray(Charsets.UTF_8)
        }
        val generated = generatePassphrase()
        prefs.edit().putString(KEY_PASSPHRASE, generated).apply()
        return generated.toByteArray(Charsets.UTF_8)
    }

    fun markEncryptedDatabaseReady() {
        prefs.edit().putBoolean(KEY_ENCRYPTED_READY, true).apply()
    }

    fun isEncryptedDatabaseReady(): Boolean = prefs.getBoolean(KEY_ENCRYPTED_READY, false)

    private fun generatePassphrase(): String {
        val bytes = ByteArray(PASSPHRASE_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    companion object {
        private const val PREFS_NAME = "feels_db_keys"
        private const val KEY_PASSPHRASE = "db_passphrase"
        private const val KEY_ENCRYPTED_READY = "db_encrypted_ready"
        private const val PASSPHRASE_BYTES = 32
    }
}

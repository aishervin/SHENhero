package com.example.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePreferences(context: Context) {
    private val appContext = context.applicationContext
    private val secrets = appContext.getSharedPreferences(SECURE_FILE, Context.MODE_PRIVATE)

    init {
        migrateLegacyValues()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        val encrypted = secrets.getString(key, null) ?: return defaultValue
        return decryptValue(encrypted)
    }

    fun encryptValue(value: String): String {
        if (value.isEmpty()) return value
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return ENCRYPTED_PREFIX + Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptValue(value: String): String {
        if (!value.startsWith(ENCRYPTED_PREFIX)) return value
        val payload = Base64.decode(value.removePrefix(ENCRYPTED_PREFIX), Base64.NO_WRAP)
        require(payload.size > IV_SIZE) { "Stored secret is invalid" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, payload.copyOfRange(0, IV_SIZE)))
        return String(cipher.doFinal(payload.copyOfRange(IV_SIZE, payload.size)), Charsets.UTF_8)
    }

    fun putString(key: String, value: String) {
        if (value.isBlank()) {
            secrets.edit().remove(key).apply()
            return
        }
        secrets.edit().putString(key, encryptValue(value)).apply()
    }

    private fun migrateLegacyValues() {
        val legacy = appContext.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE)
        val oldValues = legacy.all.filterValues { it is String }
        oldValues.forEach { (key, value) ->
            if (!secrets.contains(key)) putString(key, value as String)
            legacy.edit().remove(key).apply()
        }
    }

    @Synchronized
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "shen-secrets-v1"
        const val SECURE_FILE = "shen_secure_secrets"
        const val LEGACY_FILE = "shen_prefs"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val ENCRYPTED_PREFIX = "enc:v1:"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
    }
}

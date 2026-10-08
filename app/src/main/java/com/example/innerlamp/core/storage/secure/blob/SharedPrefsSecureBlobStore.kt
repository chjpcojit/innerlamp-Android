package com.example.innerlamp.core.storage.secure.blob

import android.content.SharedPreferences

/**
 * Cài đặt [SecureBlobStore] trên một file `SharedPreferences` riêng (`innerlamp_secure`,
 * `MODE_PRIVATE`), chỉ chứa `key → base64(iv ‖ ciphertext)`.
 *
 * Không dùng `EncryptedSharedPreferences` (đã deprecated): khoá thật nằm trong
 * AndroidKeyStore, prefs chỉ là nơi đặt bản mã.
 */
class SharedPrefsSecureBlobStore(
    private val preferences: SharedPreferences,
) : SecureBlobStore {

    override fun read(key: String): String? = preferences.getString(key, null)

    override fun write(key: String, encoded: String) {
        preferences.edit().putString(key, encoded).apply()
    }

    override fun delete(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun keys(): Set<String> = preferences.all.keys

    override fun clearAll() {
        preferences.edit().clear().apply()
    }
}

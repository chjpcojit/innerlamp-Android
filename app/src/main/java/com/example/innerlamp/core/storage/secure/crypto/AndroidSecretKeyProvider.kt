package com.example.innerlamp.core.storage.secure.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import com.example.innerlamp.core.storage.secure.SecureLevel
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Cài đặt [SecretKeyProvider] trên AndroidKeyStore: 2 alias riêng cho 2 level.
 *
 * - `DEVICE_UNLOCKED`: không yêu cầu xác thực người dùng.
 * - `USER_AUTH`: yêu cầu xác thực (`BIOMETRIC_STRONG` hoặc `DEVICE_CREDENTIAL`,
 *   API 30+ / `setUserAuthenticationValidityDurationSeconds` cho API 29), bị vô
 *   hiệu khi thay đổi sinh trắc học.
 *
 * StrongBox best-effort → fallback về TEE khi thiết bị không hỗ trợ.
 */
class AndroidSecretKeyProvider : SecretKeyProvider {

    override fun getOrCreate(level: SecureLevel): SecretKey {
        val keyStore = loadKeyStore()
        val alias = aliasFor(level)
        (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return generateWithStrongBoxFallback(level, alias)
    }

    override fun deleteAlias(level: SecureLevel) {
        loadKeyStore().deleteEntry(aliasFor(level))
    }

    private fun generateWithStrongBoxFallback(level: SecureLevel, alias: String): SecretKey =
        try {
            generateKey(level, alias, strongBox = true)
        } catch (e: StrongBoxUnavailableException) {
            generateKey(level, alias, strongBox = false)
        }

    private fun generateKey(level: SecureLevel, alias: String, strongBox: Boolean): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(buildKeyGenSpec(level, alias, strongBox))
        return keyGenerator.generateKey()
    }

    @Suppress("DEPRECATION") // setUserAuthenticationValidityDurationSeconds chỉ dùng cho API 29.
    private fun buildKeyGenSpec(
        level: SecureLevel,
        alias: String,
        strongBox: Boolean,
    ): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)

        if (level == SecureLevel.USER_AUTH) {
            builder
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Cửa sổ 1 giây ⇒ gần như per-read; không dùng CryptoObject để có
                // một đường code duy nhất cho cả sinh trắc học lẫn PIN/pattern.
                builder.setUserAuthenticationParameters(
                    1,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                )
            } else {
                builder.setUserAuthenticationValidityDurationSeconds(1)
            }
        }

        if (strongBox) {
            builder.setIsStrongBoxBacked(true)
        }
        return builder.build()
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun aliasFor(level: SecureLevel): String = when (level) {
        SecureLevel.DEVICE_UNLOCKED -> ALIAS_DEVICE_UNLOCKED
        SecureLevel.USER_AUTH -> ALIAS_USER_AUTH
    }

    private companion object {
        const val ALIAS_DEVICE_UNLOCKED = "innerlamp_key_device_unlocked"
        const val ALIAS_USER_AUTH = "innerlamp_key_user_auth"
    }
}

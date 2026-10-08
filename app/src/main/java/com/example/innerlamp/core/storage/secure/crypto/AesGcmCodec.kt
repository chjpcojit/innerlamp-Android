package com.example.innerlamp.core.storage.secure.crypto

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Mã hoá/giải mã AES-256-GCM cho tầng bảo mật.
 *
 * - IV 12 byte, sinh mới cho mỗi lần ghi (không tái sử dụng).
 * - Tag 128-bit (GCM).
 * - Kết quả mã hoá là `base64(iv ‖ ciphertext)`; giải mã tách IV đúng cách.
 */
object AesGcmCodec {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128

    /** Mã hoá [plaintext] bằng [key], trả về `base64(iv ‖ ciphertext)`. */
    fun encrypt(plaintext: ByteArray, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv // 12 byte, do provider sinh ngẫu nhiên
        val ciphertext = cipher.doFinal(plaintext)
        return Base64.getEncoder().encodeToString(iv + ciphertext)
    }

    /** Giải mã [encoded] (`base64(iv ‖ ciphertext)`) bằng [key]. */
    fun decrypt(encoded: String, key: SecretKey): ByteArray {
        val raw = Base64.getDecoder().decode(encoded)
        val iv = raw.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = raw.copyOfRange(IV_LENGTH_BYTES, raw.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ciphertext)
    }
}

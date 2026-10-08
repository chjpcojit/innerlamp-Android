package com.example.innerlamp.core.storage.secure.crypto

import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Test JVM cho [AesGcmCodec] dùng khoá AES thuần (không cần AndroidKeyStore).
 */
class AesGcmCodecTest {

    private fun newKey(): SecretKey =
        KeyGenerator.getInstance("AES").run { init(256); generateKey() }

    @Test
    fun encryptThenDecrypt_roundTrip() {
        val key = newKey()
        val plaintext = "bí mật của tôi".toByteArray(Charsets.UTF_8)

        val encoded = AesGcmCodec.encrypt(plaintext, key)
        val decoded = AesGcmCodec.decrypt(encoded, key)

        assertArrayEquals(plaintext, decoded)
    }

    @Test
    fun encrypt_samePlaintext_producesDifferentCiphertext() {
        val key = newKey()
        val plaintext = "same".toByteArray(Charsets.UTF_8)

        val first = AesGcmCodec.encrypt(plaintext, key)
        val second = AesGcmCodec.encrypt(plaintext, key)

        // IV mới mỗi lần ghi ⇒ bản mã khác nhau.
        assertNotEquals(first, second)
    }

    @Test
    fun decrypt_corruptCiphertext_throws() {
        val key = newKey()
        val encoded = AesGcmCodec.encrypt("secret".toByteArray(Charsets.UTF_8), key)

        val raw = Base64.getDecoder().decode(encoded)
        // Làm hỏng một byte trong phần ciphertext (sau 12 byte IV).
        raw[raw.size - 1] = (raw[raw.size - 1].toInt() xor 0xFF).toByte()
        val corrupted = Base64.getEncoder().encodeToString(raw)

        assertThrows(Exception::class.java) {
            AesGcmCodec.decrypt(corrupted, key)
        }
    }

    @Test
    fun decrypt_invalidBase64_throws() {
        val key = newKey()
        assertThrows(Exception::class.java) {
            AesGcmCodec.decrypt("not-valid-base64!!!", key)
        }
    }
}

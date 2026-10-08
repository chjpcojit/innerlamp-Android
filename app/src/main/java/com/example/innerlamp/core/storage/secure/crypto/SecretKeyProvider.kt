package com.example.innerlamp.core.storage.secure.crypto

import com.example.innerlamp.core.storage.secure.SecureLevel
import javax.crypto.SecretKey

/**
 * Tạo/lấy/xoá khoá AES-256-GCM trong AndroidKeyStore cho từng [SecureLevel].
 * Cài đặt thật là [AndroidSecretKeyProvider].
 */
interface SecretKeyProvider {

    /** Lấy khoá hiện có, hoặc tạo mới nếu chưa có. */
    fun getOrCreate(level: SecureLevel): SecretKey

    /** Xoá alias khoá của [level] khỏi AndroidKeyStore. */
    fun deleteAlias(level: SecureLevel)
}

package com.example.innerlamp.core.storage.secure

/**
 * Tầng bảo mật: lưu/đọc secret mã hoá AES-256-GCM, khoá nằm trong AndroidKeyStore.
 *
 * Bắt buộc truyền [SecureLevel] ở mọi thao tác ghi/đọc (không có giá trị mặc định)
 * để không secret nào vô tình rơi vào `DEVICE_UNLOCKED`.
 */
interface SecureStorage {

    suspend fun put(level: SecureLevel, key: String, value: String): Result<Unit>

    suspend fun get(level: SecureLevel, key: String): Result<String>

    /** Xoá secret theo [key] (ở cả hai level). */
    suspend fun delete(key: String): Result<Unit>

    /** Xoá toàn bộ secret thuộc [level]. */
    suspend fun clear(level: SecureLevel): Result<Unit>

    /**
     * `true` nếu [level] có thể dùng trên thiết bị này (ví dụ `USER_AUTH` cần
     * thiết bị có khoá màn hình).
     */
    suspend fun isAvailable(level: SecureLevel): Boolean
}

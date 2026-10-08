package com.example.innerlamp.core.storage.secure.blob

/**
 * Kho chứa ciphertext (blob) đã mã hoá — chỉ biết `key → base64(iv ‖ ciphertext)`,
 * không biết gì về khoá hay thuật toán. Cài đặt thật là [SharedPrefsSecureBlobStore].
 *
 * Key lưu ở đây do `SecureStorage` quyết định (đã namespace theo level).
 */
interface SecureBlobStore {

    fun read(key: String): String?

    fun write(key: String, encoded: String)

    fun delete(key: String)

    /** Liệt kê toàn bộ key hiện có (để xoá theo prefix level). */
    fun keys(): Set<String>

    /** Xoá toàn bộ blob. */
    fun clearAll()
}

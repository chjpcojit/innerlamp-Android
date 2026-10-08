package com.example.innerlamp.core.network.auth

/**
 * Port do `core/network` sở hữu, cung cấp token hiện tại.
 *
 * Đồng bộ, chỉ đọc cache in-memory — `Interceptor` chạy đồng bộ nên tuyệt đối
 * không I/O keystore/disk ở đây. Impl thật (AndroidKeyStore) thuộc spec riêng
 * `AuthTokenStorage`, nối vào qua DI.
 */
interface AuthTokenProvider {
    /** Token hiện tại, hoặc `null` nếu chưa đăng nhập. */
    fun currentToken(): String?
}

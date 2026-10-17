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

    /**
     * Id of the current session; changes on sign-in and sign-out but NOT on a token refresh. A request
     * is tagged with it so a late `401` is never retried with another account's token. /
     * Id của phiên hiện tại; đổi khi đăng nhập/đăng xuất, KHÔNG đổi khi refresh token. Request được gắn
     * id này để `401` đến muộn không bị retry bằng token của tài khoản khác.
     */
    fun currentGeneration(): Long = 0L
}

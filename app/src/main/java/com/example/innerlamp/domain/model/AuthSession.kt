package com.example.innerlamp.domain.model

/**
 * Account info returned by sign-in / registration. / Thông tin tài khoản trả về khi đăng nhập / đăng ký.
 */
data class AuthUser(
    val id: Long,
    val email: String,
    val fullName: String?,
    val dharmaName: String?,
)

/** Signed-in session: token pair plus the user. / Phiên đăng nhập: cặp token và người dùng. */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUser,
)

/**
 * Timing of an emailed code, both values come from the server. /
 * Thời hạn của mã gửi qua email, cả hai giá trị lấy từ server.
 */
data class CodeTiming(
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int,
)

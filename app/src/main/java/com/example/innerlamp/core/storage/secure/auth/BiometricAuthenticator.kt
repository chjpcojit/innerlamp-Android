package com.example.innerlamp.core.storage.secure.auth

/** Kết quả của một lần xác thực sinh trắc học/PIN. */
sealed interface AuthResult {
    data object Success : AuthResult
    data object Cancelled : AuthResult
    data object Unavailable : AuthResult
    data object Failed : AuthResult
}

/**
 * Port xác thực người dùng trước khi giải mã secret `USER_AUTH`.
 * Tách khỏi UI để `core/storage` không phụ thuộc Compose/Activity trực tiếp
 * (cài đặt thật là [AndroidBiometricAuthenticator]).
 */
interface BiometricAuthenticator {
    suspend fun authenticate(reason: String): AuthResult
}

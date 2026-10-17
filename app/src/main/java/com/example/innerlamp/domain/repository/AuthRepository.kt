package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.CodeTiming

/**
 * Contract for sign-in, sign-out and password reset. Failures are `AuthException`. /
 * Hợp đồng đăng nhập, đăng xuất, quên mật khẩu. Lỗi là `AuthException`.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<AuthSession>

    /** Calls the backend only; clearing the device is `AuthSessionStore`'s job. / Chỉ gọi backend; xoá dữ liệu máy do `AuthSessionStore`. */
    suspend fun signOut(): Result<Unit>

    suspend fun requestResetCode(email: String): Result<CodeTiming>

    suspend fun confirmReset(email: String, code: String, newPassword: String): Result<Unit>
}

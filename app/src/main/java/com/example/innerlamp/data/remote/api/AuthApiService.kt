package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.auth.Authenticated
import com.example.innerlamp.data.remote.dto.AuthResultDto
import com.example.innerlamp.data.remote.dto.EmailRequestDto
import com.example.innerlamp.data.remote.dto.LoginRequestDto
import com.example.innerlamp.data.remote.dto.OtpChallengeDto
import com.example.innerlamp.data.remote.dto.PasswordResetConfirmRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Sign-in, sign-out and password reset. Mobile sends neither `X-API-Key` nor the client-IP header. /
 * Đăng nhập, đăng xuất, quên mật khẩu. Mobile không gửi `X-API-Key` hay header client-IP.
 */
interface AuthApiService {

    @POST("v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): ApiResponse<AuthResultDto>

    @Authenticated
    @POST("v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @POST("v1/auth/password-reset/request-otp")
    suspend fun requestPasswordReset(@Body body: EmailRequestDto): ApiResponse<OtpChallengeDto>

    @POST("v1/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(@Body body: PasswordResetConfirmRequestDto): ApiResponse<Unit>
}

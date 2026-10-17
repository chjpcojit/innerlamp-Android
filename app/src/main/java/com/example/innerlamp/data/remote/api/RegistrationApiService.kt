package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.AuthResultDto
import com.example.innerlamp.data.remote.dto.EmailRequestDto
import com.example.innerlamp.data.remote.dto.OtpChallengeDto
import com.example.innerlamp.data.remote.dto.RegisterRequestDto
import com.example.innerlamp.data.remote.dto.TempleSuggestionRequestDto
import com.example.innerlamp.data.remote.dto.VerifyOtpRequestDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

/** Email registration plus the temple proposal. All public, none `@Authenticated`. / Đăng ký bằng email và đề xuất chùa; đều public. */
interface RegistrationApiService {

    @POST("v1/auth/register/request-otp")
    suspend fun requestOtp(@Body body: RegisterRequestDto): ApiResponse<OtpChallengeDto>

    @POST("v1/auth/register/resend-otp")
    suspend fun resendOtp(@Body body: EmailRequestDto): ApiResponse<OtpChallengeDto>

    @POST("v1/auth/register/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): ApiResponse<AuthResultDto>

    /** `201 { id, status }`; the answer is not used. */
    @POST("v1/temple-suggestions")
    suspend fun suggestTemple(@Body body: TempleSuggestionRequestDto): ApiResponse<JsonElement>
}

package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format of `/v1/auth/...` and `/v1/temple-suggestions`. Shapes follow the backend contract
 * (`openapi.yaml` 2.1.1). Request fields are passed explicitly (the shared `Json` does not encode
 * defaults). / Định dạng wire của các route auth; field request truyền tường minh vì `Json` dùng chung không encode giá trị mặc định.
 */

/** `device` of the sign-in request; the server adds IP / user agent itself. */
@Serializable
data class DeviceInfoDto(
    val platform: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String?,
    @SerialName("app_version") val appVersion: String?,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    val device: DeviceInfoDto,
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

/** Body of `register/resend-otp` and `password-reset/request-otp`. */
@Serializable
data class EmailRequestDto(val email: String)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    @SerialName("phone_country") val phoneCountry: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("dharma_name") val dharmaName: String?,
    @SerialName("ordination_temple_id") val ordinationTempleId: String?,
)

@Serializable
data class VerifyOtpRequestDto(
    val email: String,
    @SerialName("otp_code") val otpCode: String,
)

@Serializable
data class PasswordResetConfirmRequestDto(
    val email: String,
    @SerialName("otp_code") val otpCode: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class TempleSuggestionRequestDto(
    val name: String,
    @SerialName("province_code") val provinceCode: String,
    @SerialName("ward_code") val wardCode: String,
    val address: String,
)

/** `data` of sign-in and `register/verify-otp`. */
@Serializable
data class AuthResultDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: AuthUserDto,
)

@Serializable
data class AuthUserDto(
    val id: Long,
    val email: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("dharma_name") val dharmaName: String? = null,
)

/** `data` of `refresh` — no `user`. */
@Serializable
data class RefreshResultDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

/** `data` of the OTP-sending routes. */
@Serializable
data class OtpChallengeDto(
    @SerialName("expires_in_seconds") val expiresInSeconds: Int,
    @SerialName("resend_after_seconds") val resendAfterSeconds: Int,
)

package com.example.innerlamp.data.mapper

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.data.remote.dto.AuthResultDto
import com.example.innerlamp.data.remote.dto.AuthUserDto
import com.example.innerlamp.data.remote.dto.OtpChallengeDto
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.AuthUser
import com.example.innerlamp.domain.model.CodeTiming
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

fun AuthUserDto.toDomain() = AuthUser(id = id, email = email, fullName = fullName, dharmaName = dharmaName)

fun AuthResultDto.toDomain() = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain(),
)

fun OtpChallengeDto.toDomain() = CodeTiming(expiresInSeconds = expiresInSeconds, resendAfterSeconds = resendAfterSeconds)

/**
 * Maps a transport error to the auth business error. The backend's `message` is never used. /
 * Map lỗi đường truyền sang lỗi nghiệp vụ auth. Không dùng `message` của backend.
 */
fun AppError.toAuthError(): AuthError = when (this) {
    AppError.NoConnection, AppError.NotConfigured -> AuthError.NoConnection
    AppError.Timeout -> AuthError.Timeout
    is AppError.ApiError -> when (code) {
        "INVALID_CREDENTIALS" -> AuthError.InvalidCredentials
        "LOGIN_LOCKED" -> AuthError.LoginLocked(details.string("locked_until"))
        "RATE_LIMITED" -> AuthError.RateLimited(details.int("retry_after_seconds"))
        "INVALID_EMAIL" -> AuthError.InvalidEmail
        "INVALID_PASSWORD" -> AuthError.InvalidPassword
        "INVALID_PHONE" -> AuthError.InvalidPhone
        "INVALID_FULL_NAME" -> AuthError.InvalidFullName
        "INVALID_DHARMA_NAME" -> AuthError.InvalidDharmaName
        "INVALID_TEMPLE" -> AuthError.InvalidTemple
        "PHONE_TAKEN" -> AuthError.PhoneTaken
        "REGISTRATION_IN_PROGRESS" -> AuthError.RegistrationInProgress(details.int("retry_after_seconds"))
        "OTP_RESEND_TOO_SOON" -> AuthError.OtpResendTooSoon(details.int("retry_after_seconds"))
        "OTP_SEND_LIMITED" -> AuthError.OtpSendLimited(details.string("locked_until"))
        "OTP_LOCKED" -> AuthError.OtpLocked(details.string("locked_until"))
        "OTP_INVALID" -> AuthError.OtpInvalid
        "OTP_EXPIRED" -> AuthError.OtpExpired
        "PENDING_REGISTRATION_EXPIRED" -> AuthError.PendingRegistrationExpired
        "SERVICE_UNAVAILABLE" -> AuthError.ServiceUnavailable
        else -> AuthError.Generic
    }
    is AppError.HttpError -> if (code == 503) AuthError.ServiceUnavailable else AuthError.Generic
    AppError.Unauthorized, AppError.Serialization, is AppError.Unknown -> AuthError.Generic
}

private fun Map<String, JsonElement>?.string(key: String): String? =
    (this?.get(key) as? JsonPrimitive)?.contentOrNull

private fun Map<String, JsonElement>?.int(key: String): Int? =
    (this?.get(key) as? JsonPrimitive)?.intOrNull

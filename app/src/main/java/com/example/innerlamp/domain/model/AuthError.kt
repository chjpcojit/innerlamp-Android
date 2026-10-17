package com.example.innerlamp.domain.model

/**
 * Business and transport errors of the auth flows (sign-in, registration, password reset).
 * The UI maps them to app-owned wording, never to the backend's `message`. /
 * Lỗi nghiệp vụ và đường truyền của các luồng auth. UI map sang câu chữ của app, không dùng `message` của backend.
 */
sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data class LoginLocked(val lockedUntil: String?) : AuthError
    data class RateLimited(val retryAfterSeconds: Int?) : AuthError

    data object InvalidEmail : AuthError
    data object InvalidPassword : AuthError
    data object InvalidPhone : AuthError
    data object InvalidFullName : AuthError
    data object InvalidDharmaName : AuthError
    data object InvalidTemple : AuthError
    data object PhoneTaken : AuthError
    data class RegistrationInProgress(val retryAfterSeconds: Int?) : AuthError

    data class OtpResendTooSoon(val retryAfterSeconds: Int?) : AuthError
    data class OtpSendLimited(val lockedUntil: String?) : AuthError
    data class OtpLocked(val lockedUntil: String?) : AuthError
    data object OtpInvalid : AuthError
    data object OtpExpired : AuthError
    data object PendingRegistrationExpired : AuthError

    /** Tokens could not be written to secure storage. / Không ghi được token vào secure storage. */
    data object SessionStoreFailed : AuthError

    data object NoConnection : AuthError
    data object Timeout : AuthError
    data object ServiceUnavailable : AuthError
    data object Generic : AuthError
}

/** Carries an [AuthError] through `Result.failure`. / Mang [AuthError] qua `Result.failure`. */
class AuthException(val error: AuthError) : Exception(error.toString())

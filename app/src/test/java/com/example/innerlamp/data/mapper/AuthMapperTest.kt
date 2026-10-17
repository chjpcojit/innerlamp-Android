package com.example.innerlamp.data.mapper

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.domain.model.AuthError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthMapperTest {

    private fun api(code: String, details: Map<String, JsonElement>? = null) =
        AppError.ApiError(code = code, message = "ignored", type = "t", details = details)

    @Test
    fun invalidCredentials_isMapped() {
        assertEquals(AuthError.InvalidCredentials, api("INVALID_CREDENTIALS").toAuthError())
    }

    @Test
    fun loginLocked_carriesLockedUntil() {
        val error = api("LOGIN_LOCKED", mapOf("locked_until" to JsonPrimitive("2026-10-17T10:00:00Z")))
        assertEquals(AuthError.LoginLocked("2026-10-17T10:00:00Z"), error.toAuthError())
    }

    @Test
    fun rateLimited_carriesRetryAfter() {
        val error = api("RATE_LIMITED", mapOf("retry_after_seconds" to JsonPrimitive(30)))
        assertEquals(AuthError.RateLimited(30), error.toAuthError())
    }

    @Test
    fun registrationInProgress_carriesRetryAfter() {
        val error = api("REGISTRATION_IN_PROGRESS", mapOf("retry_after_seconds" to JsonPrimitive(12)))
        assertEquals(AuthError.RegistrationInProgress(12), error.toAuthError())
    }

    @Test
    fun otpCodes_areMapped() {
        assertEquals(AuthError.OtpInvalid, api("OTP_INVALID").toAuthError())
        assertEquals(AuthError.OtpExpired, api("OTP_EXPIRED").toAuthError())
        assertEquals(AuthError.PendingRegistrationExpired, api("PENDING_REGISTRATION_EXPIRED").toAuthError())
        assertEquals(AuthError.PhoneTaken, api("PHONE_TAKEN").toAuthError())
    }

    @Test
    fun unknownCode_isGeneric() {
        assertEquals(AuthError.Generic, api("SOMETHING_NEW").toAuthError())
    }

    @Test
    fun transportErrors_areMapped() {
        assertEquals(AuthError.NoConnection, AppError.NoConnection.toAuthError())
        assertEquals(AuthError.Timeout, AppError.Timeout.toAuthError())
        assertEquals(AuthError.ServiceUnavailable, AppError.HttpError(503).toAuthError())
    }
}

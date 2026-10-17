package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.data.remote.api.AuthApiService
import com.example.innerlamp.data.remote.dto.AuthResultDto
import com.example.innerlamp.data.remote.dto.AuthUserDto
import com.example.innerlamp.data.remote.dto.DeviceInfoDto
import com.example.innerlamp.data.remote.dto.EmailRequestDto
import com.example.innerlamp.data.remote.dto.LoginRequestDto
import com.example.innerlamp.data.remote.dto.OtpChallengeDto
import com.example.innerlamp.data.remote.dto.PasswordResetConfirmRequestDto
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    /** Runs the block; [failure] simulates a transport/business error. */
    private class FakeNetworkClient(private val failure: AppError? = null) : NetworkClient {
        override fun <T : Any> createService(clazz: Class<T>): T = error("unused")

        @Suppress("UNCHECKED_CAST")
        override suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T> {
            if (failure != null) return NetworkResult.Failure(failure)
            return NetworkResult.Success(block().data ?: (Unit as T))
        }
    }

    private class FakeAuthApi : AuthApiService {
        var login: LoginRequestDto? = null
        var reset: PasswordResetConfirmRequestDto? = null

        override suspend fun login(body: LoginRequestDto): ApiResponse<AuthResultDto> {
            login = body
            return ApiResponse(
                success = true,
                data = AuthResultDto("a", "r", AuthUserDto(7, body.email, "Nguyen A", null)),
            )
        }

        override suspend fun logout(): ApiResponse<Unit> = ApiResponse(success = true)

        override suspend fun requestPasswordReset(body: EmailRequestDto): ApiResponse<OtpChallengeDto> =
            ApiResponse(success = true, data = OtpChallengeDto(600, 60))

        override suspend fun confirmPasswordReset(body: PasswordResetConfirmRequestDto): ApiResponse<Unit> {
            reset = body
            return ApiResponse(success = true)
        }
    }

    private val device = DeviceInfoDto("android", "Pixel", "15", "1.0")

    @Test
    fun signIn_sendsEmailPasswordAndDevice_andMapsSession() = runTest {
        val api = FakeAuthApi()
        val result = AuthRepositoryImpl(api, FakeNetworkClient(), device).signIn("a@b.vn", "pw")

        assertEquals(LoginRequestDto("a@b.vn", "pw", device), api.login)
        val session = result.getOrThrow()
        assertEquals("a", session.accessToken)
        assertEquals(7L, session.user.id)
    }

    @Test
    fun signIn_failure_isAuthException() = runTest {
        val client = FakeNetworkClient(AppError.ApiError("INVALID_CREDENTIALS", "x", "t"))
        val result = AuthRepositoryImpl(FakeAuthApi(), client, device).signIn("a@b.vn", "pw")

        val error = (result.exceptionOrNull() as AuthException).error
        assertEquals(AuthError.InvalidCredentials, error)
    }

    @Test
    fun requestResetCode_mapsTiming() = runTest {
        val timing = AuthRepositoryImpl(FakeAuthApi(), FakeNetworkClient(), device)
            .requestResetCode("a@b.vn").getOrThrow()
        assertEquals(600, timing.expiresInSeconds)
        assertEquals(60, timing.resendAfterSeconds)
    }

    @Test
    fun confirmReset_sendsBody_andSucceedsWithoutData() = runTest {
        val api = FakeAuthApi()
        val result = AuthRepositoryImpl(api, FakeNetworkClient(), device).confirmReset("a@b.vn", "123456", "newpass12")

        assertTrue(result.isSuccess)
        assertEquals(PasswordResetConfirmRequestDto("a@b.vn", "123456", "newpass12"), api.reset)
    }
}

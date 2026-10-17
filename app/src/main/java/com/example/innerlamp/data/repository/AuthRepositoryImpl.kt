package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.AuthApiService
import com.example.innerlamp.data.remote.dto.DeviceInfoDto
import com.example.innerlamp.data.remote.dto.EmailRequestDto
import com.example.innerlamp.data.remote.dto.LoginRequestDto
import com.example.innerlamp.data.remote.dto.PasswordResetConfirmRequestDto
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.repository.AuthRepository

/** [AuthRepository] over `/v1/auth/...` via `safeCall`. / Cài đặt qua `safeCall`. */
class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val networkClient: NetworkClient,
    private val device: DeviceInfoDto,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<AuthSession> =
        networkClient.safeCall { api.login(LoginRequestDto(email, password, device)) }
            .toAuthResult { it.toDomain() }

    override suspend fun signOut(): Result<Unit> =
        networkClient.safeCall { api.logout() }.toAuthResult { }

    override suspend fun requestResetCode(email: String): Result<CodeTiming> =
        networkClient.safeCall { api.requestPasswordReset(EmailRequestDto(email)) }
            .toAuthResult { it.toDomain() }

    override suspend fun confirmReset(email: String, code: String, newPassword: String): Result<Unit> =
        networkClient.safeCall {
            api.confirmPasswordReset(PasswordResetConfirmRequestDto(email, code, newPassword))
        }.toAuthResult { }
}

package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.network.auth.RefreshOutcome
import com.example.innerlamp.core.network.auth.TokenRefresher
import com.example.innerlamp.data.remote.api.RefreshApiService
import com.example.innerlamp.data.remote.dto.RefreshRequestDto
import com.example.innerlamp.domain.repository.AuthSessionStore

/**
 * Calls `POST /v1/auth/refresh` and stores the new pair BEFORE reporting success, so the retried
 * request and concurrent calls see it. Only `REFRESH_TOKEN_INVALID` / `401` ends the session;
 * any other failure keeps it. /
 * Gọi refresh và lưu cặp mới TRƯỚC khi báo thành công. Chỉ `REFRESH_TOKEN_INVALID` / `401` mới kết thúc phiên.
 */
class SessionTokenRefresher(
    private val api: RefreshApiService,
    private val networkClient: NetworkClient,
    private val store: AuthSessionStore,
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String): RefreshOutcome =
        when (val result = networkClient.safeCall { api.refresh(RefreshRequestDto(refreshToken)) }) {
            is NetworkResult.Success ->
                store.updateTokens(result.data.accessToken, result.data.refreshToken).fold(
                    onSuccess = { RefreshOutcome.Success(result.data.accessToken) },
                    onFailure = { RefreshOutcome.Failed },
                )
            is NetworkResult.Failure -> when (val e = result.error) {
                AppError.Unauthorized -> RefreshOutcome.Rejected
                is AppError.ApiError ->
                    if (e.code == "REFRESH_TOKEN_INVALID") RefreshOutcome.Rejected else RefreshOutcome.Failed
                else -> RefreshOutcome.Failed
            }
        }
}

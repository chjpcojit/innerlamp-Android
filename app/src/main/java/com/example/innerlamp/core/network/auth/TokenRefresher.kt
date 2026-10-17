package com.example.innerlamp.core.network.auth

/**
 * Port that exchanges a refresh token for a new pair and persists it. The implementation lives in
 * `data/` (it knows the endpoint). /
 * Port đổi refresh token lấy cặp mới và lưu lại. Cài đặt nằm ở `data/` (biết endpoint).
 */
fun interface TokenRefresher {
    suspend fun refresh(refreshToken: String): RefreshOutcome
}

sealed interface RefreshOutcome {
    /** New pair already persisted and cached. / Cặp mới đã được lưu và cache. */
    data class Success(val accessToken: String) : RefreshOutcome

    /** The refresh token is no longer valid: the session is over. / Refresh token không còn hợp lệ: phiên kết thúc. */
    data object Rejected : RefreshOutcome

    /** No network / timeout / 5xx / rate limit: keep the session. / Mất mạng / timeout / 5xx / giới hạn: giữ phiên. */
    data object Failed : RefreshOutcome
}

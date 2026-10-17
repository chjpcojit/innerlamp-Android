package com.example.innerlamp.core.network.auth

import com.example.innerlamp.core.util.DispatcherProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Refreshes the access token once on `401` for requests that carried a Bearer token, then retries.
 *
 * - Single-flight: one lock; a request arriving later that sees the cached token already changed
 *   retries with it without refreshing again.
 * - A guest (no refresh token) or a public request (no Authorization header) is left alone.
 * - Rejected refresh → clears the cache and emits [SessionEvents.ended].
 * - A retried request that still gets `401` is not refreshed a second time. /
 *
 * Refresh khi `401` một lần cho request đã gắn Bearer rồi retry. Single-flight bằng một lock; khách
 * (không có refresh token) hoặc request public thì bỏ qua. Refresh bị từ chối → xoá cache và phát
 * [SessionEvents.ended]; request retry vẫn `401` thì không refresh lần hai.
 */
class TokenRefreshAuthenticator(
    private val cache: SessionTokenCache,
    private val refresher: TokenRefresher,
    private val events: SessionEvents,
    private val dispatchers: DispatcherProvider,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val sentHeader = response.request.header(AUTHORIZATION) ?: return null
        if (response.priorResponse != null) return null
        val sentToken = sentHeader.removePrefix(BEARER)

        synchronized(lock) {
            val refreshToken = cache.refreshToken() ?: return null
            val current = cache.currentToken()
            if (current != null && current != sentToken) return retryWith(response, current)

            return when (val outcome = runBlocking(dispatchers.io) { refresher.refresh(refreshToken) }) {
                is RefreshOutcome.Success -> retryWith(response, outcome.accessToken)
                RefreshOutcome.Rejected -> {
                    cache.clear()
                    events.emitEnded()
                    null
                }
                RefreshOutcome.Failed -> null
            }
        }
    }

    private fun retryWith(response: Response, token: String): Request =
        response.request.newBuilder().header(AUTHORIZATION, "$BEARER$token").build()

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer "
    }
}

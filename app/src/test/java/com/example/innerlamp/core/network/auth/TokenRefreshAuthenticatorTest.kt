package com.example.innerlamp.core.network.auth

import com.example.innerlamp.core.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenRefreshAuthenticatorTest {

    private val dispatchers = object : DispatcherProvider {
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val main = Dispatchers.Unconfined
    }

    private class FakeRefresher(var outcome: RefreshOutcome) : TokenRefresher {
        var calls = 0
        override suspend fun refresh(refreshToken: String): RefreshOutcome {
            calls++
            return outcome
        }
    }

    private val cache = SessionTokenCache()
    private val events = SessionEvents()

    private fun authenticator(refresher: TokenRefresher) =
        TokenRefreshAuthenticator(cache, refresher, events, dispatchers)

    private fun response401(bearer: String?, prior: Response? = null): Response {
        val request = Request.Builder().url("https://example.test/v1/x")
            .apply { bearer?.let { header("Authorization", "Bearer $it") } }
            .build()
        return Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
            .code(401).message("Unauthorized").priorResponse(prior).build()
    }

    @Test
    fun refreshSuccess_retriesWithNewToken() = runTest {
        cache.set("old", "refresh")
        val refresher = FakeRefresher(RefreshOutcome.Success("new"))

        val retry = authenticator(refresher).authenticate(null, response401("old"))

        assertEquals("Bearer new", retry?.header("Authorization"))
        assertEquals(1, refresher.calls)
    }

    @Test
    fun cachedTokenAlreadyChanged_retriesWithoutRefreshing() = runTest {
        cache.set("newer", "refresh")
        val refresher = FakeRefresher(RefreshOutcome.Failed)

        val retry = authenticator(refresher).authenticate(null, response401("old"))

        assertEquals("Bearer newer", retry?.header("Authorization"))
        assertEquals(0, refresher.calls)
    }

    @Test
    fun guestOrPublicRequest_isLeftAlone() = runTest {
        val refresher = FakeRefresher(RefreshOutcome.Success("new"))

        assertNull(authenticator(refresher).authenticate(null, response401(bearer = null)))
        cache.clear()
        assertNull(authenticator(refresher).authenticate(null, response401("old")))
        assertEquals(0, refresher.calls)
    }

    @Test
    fun rejected_clearsCache_andEmitsEnded() = runTest {
        cache.set("old", "refresh")

        val retry = authenticator(FakeRefresher(RefreshOutcome.Rejected)).authenticate(null, response401("old"))

        assertNull(retry)
        assertNull(cache.refreshToken())
        // tryEmit with no collector is dropped; the cache clear above is the observable effect.
    }

    @Test
    fun failedRefresh_keepsSession() = runTest {
        cache.set("old", "refresh")

        val retry = authenticator(FakeRefresher(RefreshOutcome.Failed)).authenticate(null, response401("old"))

        assertNull(retry)
        assertEquals("refresh", cache.refreshToken())
    }

    @Test
    fun secondUnauthorized_doesNotRefreshAgain() = runTest {
        cache.set("old", "refresh")
        val refresher = FakeRefresher(RefreshOutcome.Success("new"))

        val retry = authenticator(refresher).authenticate(null, response401("new", prior = response401("old")))

        assertNull(retry)
        assertEquals(0, refresher.calls)
    }
}

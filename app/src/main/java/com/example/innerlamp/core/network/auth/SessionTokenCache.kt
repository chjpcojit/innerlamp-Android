package com.example.innerlamp.core.network.auth

/**
 * In-memory token pair so interceptors can read synchronously (no I/O on the OkHttp thread).
 * Persistence is `AuthSessionStore`'s job. /
 * Cache token trong bộ nhớ để interceptor đọc đồng bộ (không I/O). Lưu bền do `AuthSessionStore` lo.
 */
class SessionTokenCache : AuthTokenProvider {

    @Volatile
    private var access: String? = null

    @Volatile
    private var refresh: String? = null

    override fun currentToken(): String? = access

    fun refreshToken(): String? = refresh

    @Synchronized
    fun set(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
    }

    @Synchronized
    fun clear() {
        access = null
        refresh = null
    }
}

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

    @Volatile
    private var generation = 0L

    override fun currentToken(): String? = access

    override fun currentGeneration(): Long = generation

    fun refreshToken(): String? = refresh

    /** A new session (sign-in / registration / app start): starts a new generation. / Phiên mới: sang generation mới. */
    @Synchronized
    fun set(accessToken: String, refreshToken: String) {
        generation++
        access = accessToken
        refresh = refreshToken
    }

    /** Replaces the pair after a refresh; same session, same generation. / Thay cặp token sau refresh; cùng phiên. */
    @Synchronized
    fun rotate(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
    }

    @Synchronized
    fun clear() {
        generation++
        access = null
        refresh = null
    }
}

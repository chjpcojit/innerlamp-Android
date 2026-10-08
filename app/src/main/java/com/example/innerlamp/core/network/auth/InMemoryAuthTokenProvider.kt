package com.example.innerlamp.core.network.auth

import java.util.concurrent.atomic.AtomicReference

/**
 * Impl tạm giữ token trong [AtomicReference], dùng để wiring + test trước khi có
 * `AuthTokenStorage` thật (AndroidKeyStore).
 */
class InMemoryAuthTokenProvider : AuthTokenProvider {
    private val token = AtomicReference<String?>(null)

    override fun currentToken(): String? = token.get()

    /** Cập nhật token (vd sau login). Truyền `null` để xoá. */
    fun update(token: String?) {
        this.token.set(token)
    }

    /** Xoá token (vd sau logout). */
    fun clear() {
        token.set(null)
    }
}

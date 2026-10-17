package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.auth.SessionTokenCache
import com.example.innerlamp.core.storage.prefs.PreferencesStorage
import com.example.innerlamp.core.storage.secure.SecureLevel
import com.example.innerlamp.core.storage.secure.SecureStorage
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.repository.AccountDataCleaner
import com.example.innerlamp.domain.repository.AuthSessionStore

/**
 * Tokens live in [SecureStorage] (`DEVICE_UNLOCKED`) and in [SessionTokenCache]; the user info in
 * [PreferencesStorage] under `account.*` keys. Device settings (`onboarding_completed`, intro
 * choices) are never touched. /
 * Token nằm trong [SecureStorage] và [SessionTokenCache]; thông tin user ở [PreferencesStorage]
 * (khoá `account.*`). Không đụng cài đặt thiết bị.
 */
class AuthSessionStoreImpl(
    private val secure: SecureStorage,
    private val prefs: PreferencesStorage,
    private val cache: SessionTokenCache,
    private val cleaners: List<AccountDataCleaner> = emptyList(),
) : AuthSessionStore {

    override suspend fun save(session: AuthSession): Result<Unit> {
        val tokens = writeTokens(session.accessToken, session.refreshToken)
        if (tokens.isFailure) return tokens
        cache.set(session.accessToken, session.refreshToken)
        prefs.putLong(KEY_USER_ID, session.user.id)
        prefs.putString(KEY_EMAIL, session.user.email)
        session.user.fullName?.let { prefs.putString(KEY_FULL_NAME, it) } ?: prefs.remove(KEY_FULL_NAME)
        session.user.dharmaName?.let { prefs.putString(KEY_DHARMA_NAME, it) } ?: prefs.remove(KEY_DHARMA_NAME)
        return Result.success(Unit)
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String): Result<Unit> =
        writeTokens(accessToken, refreshToken).onSuccess { cache.rotate(accessToken, refreshToken) }

    private suspend fun writeTokens(accessToken: String, refreshToken: String): Result<Unit> {
        val written = secure.put(SecureLevel.DEVICE_UNLOCKED, KEY_ACCESS, accessToken)
            .mapCatching { secure.put(SecureLevel.DEVICE_UNLOCKED, KEY_REFRESH, refreshToken).getOrThrow() }
        if (written.isFailure) {
            // Remove what was written so no half-session remains. / Xoá phần đã ghi để không còn phiên dở dang.
            secure.delete(KEY_ACCESS)
            secure.delete(KEY_REFRESH)
            cache.clear()
        }
        return written
    }

    override suspend fun load() {
        val access = secure.get(SecureLevel.DEVICE_UNLOCKED, KEY_ACCESS).getOrNull()
        val refresh = secure.get(SecureLevel.DEVICE_UNLOCKED, KEY_REFRESH).getOrNull()
        // A read failure counts as no session. / Đọc lỗi coi như chưa có phiên.
        if (access != null && refresh != null) cache.set(access, refresh) else cache.clear()
    }

    override suspend fun clear() {
        cache.clear()
        secure.delete(KEY_ACCESS)
        secure.delete(KEY_REFRESH)
        ACCOUNT_KEYS.forEach { prefs.remove(it) }
        cleaners.forEach { it.clear() }
    }

    override fun hasSession(): Boolean = cache.refreshToken() != null

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
        const val KEY_USER_ID = "account.user_id"
        const val KEY_EMAIL = "account.email"
        const val KEY_FULL_NAME = "account.full_name"
        const val KEY_DHARMA_NAME = "account.dharma_name"
        val ACCOUNT_KEYS = listOf(KEY_USER_ID, KEY_EMAIL, KEY_FULL_NAME, KEY_DHARMA_NAME)
    }
}

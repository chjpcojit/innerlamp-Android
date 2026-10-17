package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.AuthSession

/**
 * The single place that writes and deletes the session on the device. /
 * Nơi duy nhất ghi và xoá phiên đăng nhập trên máy.
 */
interface AuthSessionStore {
    /** Stores tokens in secure storage and the user in preferences. / Lưu token vào secure storage, user vào prefs. */
    suspend fun save(session: AuthSession): Result<Unit>

    /** Replaces the token pair only (after a refresh); the user info is kept. / Chỉ thay cặp token (sau refresh); giữ thông tin user. */
    suspend fun updateTokens(accessToken: String, refreshToken: String): Result<Unit>

    /** Loads the stored tokens into memory at app start. / Nạp token đã lưu vào bộ nhớ khi mở app. */
    suspend fun load()

    /** Removes tokens and account data; keeps device settings. / Xoá token và dữ liệu tài khoản; giữ cài đặt thiết bị. */
    suspend fun clear()

    fun hasSession(): Boolean
}

/** A feature registers one to delete its account-scoped data on sign-out. / Mỗi feature đăng ký một cleaner để xoá dữ liệu theo tài khoản khi đăng xuất. */
fun interface AccountDataCleaner {
    suspend fun clear()
}

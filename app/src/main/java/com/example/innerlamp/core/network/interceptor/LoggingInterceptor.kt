package com.example.innerlamp.core.network.interceptor

import com.example.innerlamp.core.network.NetworkConfig
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Cấu hình logging cho OkHttp: bật body ở debug, tắt hẳn ở release để không lộ
 * dữ liệu nhạy cảm (token, payload).
 *
 * Luôn che header `Authorization` và không log gì cho `/v1/auth/...` (mật khẩu, mã, token) và
 * `/v1/memorials...` (họ tên, ghi chú, `photo_url` của sổ giỗ) kể cả ở debug. /
 * Always redacts `Authorization` and logs nothing for `/v1/auth/...` (passwords, codes, tokens) or
 * `/v1/memorials...` (memorial names, notes and `photo_url`), even in debug builds.
 */
object LoggingInterceptor {

    private const val AUTH_PATH_PREFIX = "/v1/auth/"
    private const val MEMORIAL_PATH = "/v1/memorials"

    /** `false` for paths that must never reach the logger (PII, tokens). / `false` với path cấm log. */
    internal fun shouldLog(encodedPath: String): Boolean = when {
        encodedPath.startsWith(AUTH_PATH_PREFIX) -> false
        encodedPath == MEMORIAL_PATH || encodedPath.startsWith("$MEMORIAL_PATH/") -> false
        else -> true
    }

    /** Tạo interceptor logging theo môi trường build. */
    fun create(isDebug: Boolean = NetworkConfig.isDebugLogging): Interceptor {
        val delegate = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
        return Interceptor { chain ->
            val path = chain.request().url.encodedPath
            if (shouldLog(path)) {
                delegate.intercept(chain)
            } else {
                chain.proceed(chain.request())
            }
        }
    }
}

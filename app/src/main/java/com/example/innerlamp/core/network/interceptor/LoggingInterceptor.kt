package com.example.innerlamp.core.network.interceptor

import com.example.innerlamp.core.network.NetworkConfig
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Cấu hình logging cho OkHttp: bật body ở debug, tắt hẳn ở release để không lộ
 * dữ liệu nhạy cảm (token, payload).
 *
 * Luôn che header `Authorization` và không log gì cho `/v1/auth/...` (mật khẩu, mã, token) kể cả
 * ở debug. / Always redacts `Authorization` and never logs `/v1/auth/...` (passwords, codes, tokens),
 * even in debug builds.
 */
object LoggingInterceptor {

    private const val AUTH_PATH_PREFIX = "/v1/auth/"

    /** Tạo interceptor logging theo môi trường build. */
    fun create(isDebug: Boolean = NetworkConfig.isDebugLogging): Interceptor {
        val delegate = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
        return Interceptor { chain ->
            if (chain.request().url.encodedPath.startsWith(AUTH_PATH_PREFIX)) {
                chain.proceed(chain.request())
            } else {
                delegate.intercept(chain)
            }
        }
    }
}

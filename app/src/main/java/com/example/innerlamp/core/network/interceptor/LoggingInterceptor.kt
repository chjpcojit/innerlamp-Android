package com.example.innerlamp.core.network.interceptor

import com.example.innerlamp.core.network.NetworkConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Cấu hình logging cho OkHttp: bật body ở debug, tắt hẳn ở release để không lộ
 * dữ liệu nhạy cảm (token, payload).
 */
object LoggingInterceptor {

    /** Tạo [HttpLoggingInterceptor] theo môi trường build. */
    fun create(isDebug: Boolean = NetworkConfig.isDebugLogging): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
}

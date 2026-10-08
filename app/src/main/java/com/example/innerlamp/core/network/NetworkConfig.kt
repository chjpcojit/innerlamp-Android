package com.example.innerlamp.core.network

import com.example.innerlamp.BuildConfig

/**
 * Cấu hình bất biến cho lớp network.
 *
 * [baseUrl] đọc từ `BuildConfig.BASE_URL` do product flavor sinh ra tại thời
 * điểm build (xem `Docs/Specs/Networking/design.md` mục "Build variants").
 * URL rỗng → request bị chặn và trả [AppError.NotConfigured].
 */
object NetworkConfig {

    /** Base URL của backend. Rỗng nghĩa là chưa cấu hình → `NotConfigured`. */
    val baseUrl: String = BuildConfig.BASE_URL

    /** Timeout kết nối TCP (ms). */
    const val connectTimeoutMillis: Long = 30_000L

    /** Timeout đọc response (ms). */
    const val readTimeoutMillis: Long = 30_000L

    /** Timeout ghi request body (ms). */
    const val writeTimeoutMillis: Long = 30_000L

    /** Bật log request/response body chỉ ở build debug. */
    val isDebugLogging: Boolean = BuildConfig.DEBUG
}

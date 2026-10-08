package com.example.innerlamp.core.network

/**
 * Mặt tiền duy nhất tầng `data` dùng để gọi backend.
 *
 * `core/network` không biết endpoint nào; service do feature khai báo ở
 * `data/remote/api/` và được tạo qua [createService].
 */
interface NetworkClient {

    /** Tạo Retrofit service cho endpoint của từng feature. */
    fun <T : Any> createService(clazz: Class<T>): T

    /**
     * Gọi API an toàn: bóc envelope [ApiResponse], map lỗi → [AppError], trả
     * [NetworkResult] và không bao giờ để lộ kiểu retrofit/okhttp.
     */
    suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T>
}

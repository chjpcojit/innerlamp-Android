package com.example.innerlamp.core.network

import kotlinx.serialization.json.JsonElement

/**
 * Kiểu lỗi trung tính với thư viện mạng, dùng chung cho toàn app.
 * Không để lộ `retrofit2.*`/`okhttp3.*`/`java.io.*` ra ngoài `core/network`.
 */
sealed class AppError {

    /** Base URL chưa được cấu hình (rỗng). */
    data object NotConfigured : AppError()

    /** Không có kết nối mạng (host không phân giải được / không tới được server). */
    data object NoConnection : AppError()

    /** Request vượt quá timeout đã cấu hình. */
    data object Timeout : AppError()

    /** Server trả HTTP 401 — token hết hạn/sai, chưa đăng nhập. */
    data object Unauthorized : AppError()

    /**
     * Lỗi nghiệp vụ từ backend (envelope `success == false`), kể cả khi HTTP 2xx.
     *
     * @param code mã lỗi do backend định nghĩa (vd `VALIDATION_FAILED`).
     * @param message mô tả lỗi để hiển thị cho người dùng.
     * @param type phân loại lỗi (do backend quyết định).
     * @param details map field → lỗi (khi có lỗi theo từng trường).
     * @param retryAfterSeconds giá trị header `Retry-After` (giây) nếu server gửi, ngược lại `null`.
     */
    data class ApiError(
        val code: String,
        val message: String,
        val type: String,
        val details: Map<String, JsonElement>? = null,
        val retryAfterSeconds: Int? = null,
    ) : AppError()

    /** HTTP 4xx/5xx nhưng body không parse được theo envelope. */
    data class HttpError(
        val code: Int,
        val serverMessage: String? = null,
        val retryAfterSeconds: Int? = null,
    ) : AppError()

    /** Response body không deserialize được (sai kiểu / JSON hỏng). */
    data object Serialization : AppError()

    /** Lỗi không xác định. */
    data class Unknown(val cause: Throwable? = null) : AppError()
}

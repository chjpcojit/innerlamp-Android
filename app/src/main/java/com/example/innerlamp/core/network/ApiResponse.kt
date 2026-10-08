package com.example.innerlamp.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Chi tiết lỗi nằm trong envelope [ApiResponse].
 * Contract Go của backend: `ErrorDetail { code, message, type }`.
 */
@Serializable
data class ApiErrorDetail(
    val code: String,
    val message: String,
    val type: String,
)

/**
 * Envelope chung mọi response của backend.
 *
 * Mọi field trừ [success] đều optional (default `null`) nên envelope deserialize
 * được bất kể thứ tự hay sự hiện diện của từng field.
 *
 * @param success `true` → dùng [data]; `false` → dùng [error].
 * @param data payload thật (chỉ có nghĩa khi [success] là `true`).
 * @param error chi tiết lỗi (chỉ có nghĩa khi [success] là `false`).
 * @param details map field → lỗi (chỉ dùng khi [success] là `false`).
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorDetail? = null,
    val details: Map<String, JsonElement>? = null,
)

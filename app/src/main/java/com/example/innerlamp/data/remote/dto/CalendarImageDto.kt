package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * DTO of `GET /v1/calendar/image` (`data` object, contract 2.2.0). Lenient: optional fields default;
 * `id` is a [JsonElement] because its JSON type is not confirmed (string or number). /
 * DTO của `GET /v1/calendar/image`. Khoan dung: field tuỳ chọn có default; `id` là [JsonElement] vì
 * kiểu JSON chưa xác nhận (chuỗi hay số).
 */
@Serializable
data class CalendarImageDto(
    val date: String,
    val id: JsonElement? = null,
    @SerialName("image_url") val imageUrl: String,
    val author: String = "",
    val license: String = "",
    @SerialName("license_url") val licenseUrl: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
)

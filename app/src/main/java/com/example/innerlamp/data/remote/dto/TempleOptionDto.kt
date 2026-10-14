package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `data` của `GET /v1/temples/options`. */
@Serializable
data class TempleOptionPageDto(
    val items: List<TempleOptionDto>,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class TempleOptionDto(
    val id: String,
    val name: String,
    val province: String,
    // null khi chùa chưa được re-seed theo địa chỉ có cấu trúc (hoặc backend chưa deploy field này).
    @SerialName("province_code") val provinceCode: String? = null,
)

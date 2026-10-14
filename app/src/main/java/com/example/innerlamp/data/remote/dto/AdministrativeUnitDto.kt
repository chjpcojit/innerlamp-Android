package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Khung `data` chung của các endpoint danh sách đơn vị hành chính: `{ "items": [...] }`. */
@Serializable
data class ItemsDto<T>(
    val items: List<T>,
)

@Serializable
data class ProvinceDto(
    val code: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val type: String,
)

@Serializable
data class DistrictDto(
    val code: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val type: String,
    @SerialName("province_code") val provinceCode: String,
)

/** Xã/phường; `district_code` chỉ có ở hệ cũ. */
@Serializable
data class WardDto(
    val code: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val type: String,
    @SerialName("province_code") val provinceCode: String,
    @SerialName("district_code") val districtCode: String? = null,
)

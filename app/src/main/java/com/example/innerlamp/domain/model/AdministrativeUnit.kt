package com.example.innerlamp.domain.model

/**
 * Tỉnh/thành phố.
 *
 * @param type `"province"` (tỉnh) hoặc `"city"` (thành phố trực thuộc trung ương).
 */
data class Province(
    val code: String,
    val name: String,
    val fullName: String,
    val type: String,
)

/**
 * Quận/huyện — chỉ tồn tại ở hệ hành chính cũ (3 cấp).
 */
data class District(
    val code: String,
    val name: String,
    val fullName: String,
    val type: String,
    val provinceCode: String,
)

/**
 * Xã/phường/đặc khu.
 *
 * @param type ở hệ mới: `"ward"`, `"commune"` hoặc `"special_zone"`.
 * @param districtCode mã huyện; chỉ có ở hệ cũ, `null` ở hệ mới (không có cấp huyện).
 */
data class Ward(
    val code: String,
    val name: String,
    val fullName: String,
    val type: String,
    val provinceCode: String,
    val districtCode: String? = null,
)

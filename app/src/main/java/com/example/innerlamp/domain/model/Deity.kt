package com.example.innerlamp.domain.model

/**
 * `type` of `/v1/deities*` — an open enum: an unknown value is [Unknown] and still shown. /
 * `type` của `/v1/deities*` — enum mở: giá trị lạ là [Unknown] và vẫn được hiển thị.
 */
enum class DeityType { Buddha, Bodhisattva, Deva, Unknown }

/** Pill label ("Phật", "Bồ Tát", "Thiên"); `null` for [DeityType.Unknown]. / Nhãn pill; loại lạ → `null`. */
val DeityType.kindLabel: String?
    get() = when (this) {
        DeityType.Buddha -> "Phật"
        DeityType.Bodhisattva -> "Bồ Tát"
        DeityType.Deva -> "Thiên"
        DeityType.Unknown -> null
    }

/**
 * One deity card (home block, Library strip, catalogue) — `DeitySummary` of the contract; an empty
 * string is `null`. /
 * Một thẻ chư Phật / Bồ Tát (khối Trang chủ, dải Thư viện, danh mục); chuỗi rỗng là `null`.
 */
data class DeitySummary(
    val id: String,
    val type: DeityType,
    val nameVi: String,
    val nameSanskrit: String?,
    val titleHonorific: String?,
    val thumbnailUrl: String?,
    val imageUrl: String?,
    val coreVirtue: String?,
    val pureLand: String?,
) {
    val kindLabel: String? get() = type.kindLabel

    /** The short virtue before the parenthesis, e.g. "Đại Bi". / Hạnh nguyện ngắn trước dấu ngoặc. */
    val shortVirtue: String?
        get() = coreVirtue?.substringBefore(" (")?.trim()?.takeIf { it.isNotEmpty() }
}

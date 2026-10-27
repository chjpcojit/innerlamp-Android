package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn "Ảnh đẹp" (`photos`), map từ `/v1/gallery*` (contract 3.0.0). Pure Kotlin. /
 * Types of the gallery screen, mapped from `/v1/gallery*`.
 */

/**
 * Phân loại ảnh — dùng cho chip lọc; [code] là `type` của contract. "Tất cả" không phải một category
 * (UI dùng `null`).
 */
enum class PhotoCategory(val label: String, val code: String) {
    Temple("Chùa", "CHUA"),
    BuddhaStatue("Tượng Phật", "TUONG_PHAT"),
    Meditation("Cảnh thiền", "THIEN"),
    Festival("Lễ hội", "LE_HOI"),
    ;

    companion object {
        /** Open enum: an unknown code is `null` (shown only under "Tất cả"). / Mã lạ → `null`. */
        fun fromCode(code: String): PhotoCategory? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Một bức ảnh trong lưới masonry.
 *
 * [heightDp] là chiều cao ô ảnh (dp) — the contract has no image size, so the mapper cycles the
 * prototype heights. / Contract không có kích thước ảnh nên mapper xoay vòng chiều cao của prototype.
 */
data class TemplePhoto(
    val id: String,
    /** `null` when the type is unknown. / `null` khi loại lạ. */
    val category: PhotoCategory?,
    val title: String,
    val place: String,
    val heightDp: Int,
    val thumbnailUrl: String? = null,
    val imageUrl: String? = null,
)

/** Credit of one photo (`GET /v1/gallery/{id}`); CC BY / BY-SA require it. / Ghi công một ảnh. */
data class PhotoCredit(
    val author: String?,
    val license: String?,
    val sourceUrl: String?,
)

/** Home "Ảnh đẹp" block: up to three photos + how many more exist. / Khối ảnh ở Trang chủ. */
data class HomePhotos(
    val photos: List<TemplePhoto>,
    val total: Int,
) {
    /** "+N ảnh" on the last tile. / Số ảnh còn lại. */
    val more: Int get() = (total - photos.size).coerceAtLeast(0)
}

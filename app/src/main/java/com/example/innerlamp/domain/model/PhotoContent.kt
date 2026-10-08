package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn "Ảnh chùa đẹp" (`photos`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Tầng `domain` sở hữu các kiểu
 * này; tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/**
 * Phân loại ảnh chùa — dùng cho chip lọc. "Tất cả" không phải một category
 * (nó là trạng thái "không lọc", được tầng UI xử lý bằng `null`).
 */
enum class PhotoCategory(val label: String) {
    Temple("Chùa"),
    BuddhaStatue("Tượng Phật"),
    Meditation("Cảnh thiền"),
    Festival("Lễ hội"),
}

/**
 * Một bức ảnh chùa trong lưới masonry.
 *
 * [heightDp] là chiều cao của ô ảnh (đơn vị dp) — khớp `p.h` của prototype
 * (`Docs/design_handoff_innerlamp/`, màn `photos`). Giá trị là số thuần để tầng
 * `data` cung cấp tỉ lệ khung ảnh; `ui` quy đổi thành `height`.
 */
data class TemplePhoto(
    val id: String,
    val category: PhotoCategory,
    val title: String,
    val place: String,
    val heightDp: Int,
)

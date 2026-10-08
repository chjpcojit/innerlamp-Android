package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu cho màn "Trang chùa" (`temple`).
 *
 * Chỉ chứa hình dạng thuần Kotlin — không import Android/Compose/Room. Tầng UI tự ánh
 * xạ [TempleTone]/[TempleGlyph] sang bộ màu + hình tượng của mình (xem `TempleScreen`).
 */

/** Tông màu của một thời khóa (được UI ánh xạ sang `IlTone`). */
enum class TempleTone { Gold, Green, Rose, Ink }

/** Hình tượng của một thời khóa (được UI ánh xạ sang `IlRowGlyph`). */
enum class TempleGlyph { Leaf, Book, Circle, Dot }

/**
 * Một thời khóa sắp tới của chùa (khối "Thời khóa sắp tới").
 *
 * [trailing] là trạng thái ghi danh ở bên phải hàng, vd `"Đã ghi danh"` / `"Ghi danh"`.
 */
data class TempleSchedule(
    val id: String,
    val title: String,
    /** Phụ đề (thời gian · địa điểm / người hướng dẫn). */
    val sub: String,
    val trailing: String,
    val tone: TempleTone,
    val glyph: TempleGlyph,
)

/** Một cột trong khối thống kê 3 cột ("Phật tử / Thời khóa / Năm lập"). */
data class TempleStat(
    val value: String,
    val label: String,
)

/**
 * Toàn bộ nội dung một trang chùa (màn `temple`).
 *
 * Khác với [Temple] (bản tóm tắt trong carousel "Chùa Việt Nam" ở Trang chủ): đây là
 * trang chi tiết đầy đủ — hero, thống kê, giới thiệu, thời khóa sắp tới và địa chỉ.
 */
data class TempleDetail(
    val id: String,
    val name: String,
    /** Địa chỉ ngắn, vd `"Q. Bình Thạnh, TP.HCM"`. */
    val place: String,
    /** Hệ phái, vd `"Hệ phái Bắc tông"`. */
    val sect: String,
    val stats: List<TempleStat>,
    val description: String,
    val address: String,
    val upcoming: List<TempleSchedule>,
)

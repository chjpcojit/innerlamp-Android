package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn Thư viện (`lib`).
 *
 * Pure Kotlin — không phụ thuộc Android. Tầng `domain` sở hữu các kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/** Phân loại nội dung trong thư viện — dùng cho chip lọc và nhóm bài kinh. */
enum class LibraryCategory(val label: String) {
    Sutra("Kinh văn"),
    Meditation("Thiền dẫn"),
    Talk("Pháp thoại"),
}

/** Tông màu của bài kinh (ánh xạ sang `IlTone` ở tầng UI): vàng/xanh/hồng. */
enum class SutraTone { Gold, Green, Rose }

/** Một bài kinh hiển thị trong danh sách "Kinh thường tụng". */
data class Sutra(
    val id: String,
    val name: String,
    val sub: String,
    /** Thời lượng dạng chuỗi (vd "38 phút", "2g 10p") — trailing của hàng. */
    val time: String,
    val tone: SutraTone,
    val category: LibraryCategory = LibraryCategory.Sutra,
)

/** Thẻ pháp thoại nổi bật (hero) ở đầu màn Thư viện. */
data class FeaturedTalk(
    val id: String,
    /** Nhãn kicker in hoa (vd "PHÁP THOẠI MÙA VU LAN"). */
    val kicker: String,
    /** Tiêu đề Lora, có thể chứa xuống dòng `\n`. */
    val title: String,
    val durationMinutes: Int,
)

/** Toàn bộ catalog của màn Thư viện: các mục lọc + thẻ nổi bật + danh sách kinh. */
data class LibraryCatalog(
    val categories: List<LibraryCategory>,
    val featuredTalk: FeaturedTalk,
    val sutras: List<Sutra>,
)

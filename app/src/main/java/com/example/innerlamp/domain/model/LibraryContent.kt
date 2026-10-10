package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn Thư viện (`lib`).
 *
 * Pure Kotlin — không phụ thuộc Android. Tầng `domain` sở hữu các kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/** Bốn lối vào của lưới 2×2 ở đầu màn Thư viện (prototype: `libTiles`). */
enum class LibraryHub(val label: String, val sub: String, val glyph: String) {
    Sutras("Kinh sách", "Kinh văn · trì chú", "經"),
    Meditation("Thiền định", "Thiền chuông · thiền dẫn", "禪"),
    Talks("Pháp thoại", "Bài giảng của quý thầy", "法"),
    Buddhas("Phật & Bồ Tát", "Tiểu sử · đại nguyện", "佛"),
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

/** Toàn bộ catalog của màn Thư viện: lối vào + thẻ nổi bật + danh sách kinh. */
data class LibraryCatalog(
    val hubs: List<LibraryHub>,
    val featuredTalk: FeaturedTalk,
    val sutras: List<Sutra>,
)

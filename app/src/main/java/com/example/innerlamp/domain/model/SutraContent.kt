package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn "Chi tiết bài kinh" (`detail`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Tầng `domain` sở hữu các kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/** Kiểu trình bày của một nhãn phụ trên hero chi tiết kinh. */
enum class SutraBadgeStyle {
    /** Nền đầy (vd "Tụng hằng ngày"). */
    Filled,

    /** Chỉ viền (vd "Có phiên âm"). */
    Outlined,
}

/** Một nhãn phụ trên hero chi tiết kinh (gắn trạng thái của bài kinh). */
data class SutraBadge(
    val label: String,
    val style: SutraBadgeStyle,
)

/** Một phẩm (chương) trong mục lục của bài kinh. */
data class SutraChapter(
    /** Số thứ tự La Mã (vd "I", "II"…). */
    val num: String,
    /** Tên phẩm (vd "Phẩm Song Yếu"). */
    val name: String,
    /** Phạm vi kệ (vd "Kệ 1 – 20"). */
    val sub: String,
    /** Thời lượng (vd "9 phút"). */
    val time: String,
    /** Đang tụng (phẩm đầu tiên) — tô nền vàng nhạt + đổi màu số. */
    val isActive: Boolean = false,
)

/**
 * Toàn bộ nội dung màn "Chi tiết bài kinh" — dùng cho màn `detail`.
 *
 * Khác với [Sutra] (chỉ là hàng tóm tắt trong Thư viện), đây là nội dung đầy đủ:
 * hero, mô tả và mục lục các phẩm. Vị trí đang tụng và tiến độ đọc là state trình
 * bày nên nằm ở ViewModel, không đưa vào model này (xem `SutraUiState`).
 */
data class SutraDetail(
    val id: String,
    /** Nhãn kicker in hoa (vd "KINH VĂN · SONG NGỮ"). */
    val kicker: String,
    /** Tên bài kinh (vd "Kinh Pháp Cú"). */
    val title: String,
    /** Dòng thông tin phụ (vd "26 phẩm · 423 kệ · Thầy Minh Đạo tụng · 2 giờ 10 phút"). */
    val meta: String,
    /** Các nhãn phụ trên hero (đầy / viền). */
    val badges: List<SutraBadge>,
    /** Mô tả bài kinh. */
    val description: String,
    /** Số phẩm hiển thị cạnh tiêu đề "Mục lục" (vd "26 phẩm"). */
    val chapterCount: String,
    /** Các phẩm trong mục lục (theo thứ tự). */
    val chapters: List<SutraChapter>,
)

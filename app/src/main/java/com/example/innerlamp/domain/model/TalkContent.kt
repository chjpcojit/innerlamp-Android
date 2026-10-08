package com.example.innerlamp.domain.model

/**
 * Kiểu dữ liệu của màn "Pháp thoại" (`talk`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Tầng `domain` sở hữu kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/** Một phần (chương) của buổi pháp thoại, hiển thị trong danh sách "Các phần". */
data class TalkChapter(
    /** Mốc thời gian bắt đầu phần (vd "00:00"). */
    val timestamp: String,
    /** Tên phần (vd "Vì sao nói hiếu là gốc"). */
    val title: String,
)

/**
 * Toàn bộ nội dung một buổi pháp thoại — dùng cho màn `talk`.
 *
 * Khác với [FeaturedTalk] (chỉ là thẻ tóm tắt ở đầu màn Thư viện), đây là nội dung
 * đầy đủ: hero, mô tả và danh sách chương. Tiến độ phát, thời gian đã nghe và trạng
 * thái play/pause là state trình bày nên nằm ở ViewModel, không đưa vào model này.
 */
data class TalkDetail(
    val id: String,
    /** Nhãn kicker in hoa (vd "PHÁP THOẠI · MÙA VU LAN"). */
    val kicker: String,
    /** Tiêu đề Lora, có thể chứa xuống dòng `\n`. */
    val title: String,
    /** Tên giảng sư (vd "Thầy Minh Đạo"). */
    val speaker: String,
    /** Chữ viết tắt tên giảng sư (hiển thị trong avatar tròn 32dp). */
    val initials: String,
    /** Thời lượng buổi giảng (phút). */
    val durationMinutes: Int,
    /** Số lượt nghe dạng chuỗi (vd "2.4k lượt nghe"). */
    val listenCount: String,
    /** Tóm tắt nội dung buổi giảng. */
    val description: String,
    /** Các phần của buổi giảng (theo thứ tự). */
    val chapters: List<TalkChapter>,
)

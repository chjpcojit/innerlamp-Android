package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu của màn "Tụng kinh" (`reader`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Tầng `domain` sở hữu các kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/** Một khổ kinh song ngữ đang được tụng (nguyên văn Việt + bản dịch). */
data class ChantVerse(
    /** Nguyên văn tiếng Việt; các dòng cách nhau bằng `\n`. */
    val vietnamese: String,
    /** Bản dịch (Anh ngữ) in nghiêng bên dưới câu Việt. */
    val translation: String,
)

/**
 * Toàn bộ nội dung màn "Tụng kinh" (`reader`) — một khổ kinh song ngữ đang được
 * tụng, kèm thanh phát dính đáy màn.
 *
 * Tiến độ phát, thời gian đã tụng và trạng thái play/pause là state trình bày nên
 * nằm ở ViewModel, không đưa vào model này (xem `ReaderUiState`).
 */
data class SutraReader(
    val id: String,
    /** Tên bài kinh hiển thị ở header (vd "Kinh Pháp Cú"). */
    val title: String,
    /** Phụ đề header — phẩm đang tụng (vd "Phẩm Song Yếu · kệ 1–2"). */
    val subtitle: String,
    /** Khổ kinh đang tụng (Việt + bản dịch). */
    val verse: ChantVerse,
    /** Khổ kinh kế tiếp (hiển thị mờ để báo trước nội dung sắp tới). */
    val nextVersePreview: String,
    /** Người tụng (vd "Thầy Minh Đạo"). */
    val teacher: String,
    /** Tốc độ phát hiển thị trên chip (vd "1.0×"). */
    val speed: String,
)

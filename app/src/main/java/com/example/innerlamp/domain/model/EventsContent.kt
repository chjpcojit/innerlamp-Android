package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu cho màn "Sự kiện & khóa học" (`events`).
 *
 * Chỉ chứa hình dạng thuần Kotlin — không import Android/Compose/Room. Tầng UI
 * tự ánh xạ [EventTone] sang bộ màu của mình (xem `EventsScreen`).
 */

/** Tab lọc danh sách trên màn "Sự kiện & khóa học". */
enum class EventTab { Upcoming, Courses, Registered }

/** Tông màu cho một sự kiện / khóa học (được UI ánh xạ sang `IlTone`). */
enum class EventTone { Gold, Green, Rose, Ink }

/**
 * Sự kiện nổi bật (thẻ hero **luôn tối** ở đầu màn). Luôn hiển thị sự kiện
 * "Sắp diễn ra" gần nhất, kèm nút ghi danh.
 */
data class FeaturedEvent(
    /** Ngày, vd `"14"`. */
    val day: String,
    /** Tháng viết tắt, vd `"TH9"`. */
    val month: String,
    /** Nhãn mắt (eyebrow) vàng, vd `"SẮP DIỄN RA"`. */
    val eyebrow: String,
    /** Tiêu đề Lora, có thể xuống dòng, vd `"Khóa tu một ngày\nan lạc"`. */
    val title: String,
    /** Chi tiết (thời gian · địa điểm / người hướng dẫn), có thể nhiều dòng. */
    val detail: String,
    /** Nhãn chỗ trống còn lại, vd `"còn 12 chỗ"`. */
    val remainingLabel: String,
)

/**
 * Một sự kiện / khóa học trong danh sách.
 *
 * Khác với [BuddhistEvent] (sự kiện lễ vía/trai giới ở trang chủ): đây là các
 * hoạt động của đạo tràng — tụng kinh, pháp thoại, khóa học nhiều buổi — mà
 * người dùng có thể ghi danh tham dự.
 */
data class EventItem(
    val id: String,
    /** Ngày, vd `"15"` (hoặc số buổi, vd `"8"` cho khóa học nhiều buổi). */
    val day: String,
    /** Tháng viết tắt, vd `"TH9"` / `"TUẦN"`. */
    val month: String,
    val name: String,
    /** Phụ đề (thời gian · địa điểm · người hướng dẫn). */
    val sub: String,
    /** Trạng thái hiển thị bên phải, vd `"Ghi danh"` / `"Còn 4 chỗ"` / `"Đã ghi danh"`. */
    val status: String,
    val tone: EventTone,
)

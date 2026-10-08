package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu cho màn Đạo tràng (`sangha`).
 *
 * Chỉ chứa hình dạng thuần Kotlin — không import Android/Compose/Room. Tầng UI
 * tự ánh xạ [SanghaTone] sang bộ màu của mình (xem `SanghaScreen`).
 */

/** Tông màu cho avatar/bài đăng của bạn đồng tu (được UI ánh xạ sang `IlTone`). */
enum class SanghaTone { Gold, Green, Rose, Ink }

/**
 * Một bài chia sẻ của bạn đồng tu trong bảng tin Đạo tràng.
 *
 * Khác với bài đăng đầy đủ ở màn `post`: ở đây chỉ là bản xem trước rút gọn
 * (tác giả + dòng thời gian + nội dung + lượt tùy hỷ/hồi âm).
 */
data class SanghaPost(
    val id: String,
    val author: String,
    /** Chữ viết tắt hiển thị trên avatar. */
    val initials: String,
    val tone: SanghaTone,
    /** Thời gian tương đối, vd "2 giờ trước". */
    val time: String,
    /** Phụ đề bên cạnh thời gian, vd "41 ngày tinh tấn" / "Chia sẻ pháp". */
    val meta: String,
    val body: String,
    /** Lượt tùy hỷ. */
    val likes: Int,
    /** Lượt hồi âm (bình luận). */
    val comments: Int,
)

/** Nhóm đang ngồi thiền trực tuyến (thẻ "Đang cùng ngồi thiền"). */
data class MeditationGroup(
    /** Số người đang ngồi. */
    val count: Int,
    /** Mô tả, vd "người trên khắp Việt Nam · phiên 20 phút". */
    val label: String,
)

/** Đạo tràng người dùng đang tham gia (khối "Đạo tràng của tôi"). */
data class SanghaSummary(
    val name: String,
    val memberCount: Int,
    val place: String,
    /** Thông báo gần đây của đạo tràng. */
    val announcement: String,
)

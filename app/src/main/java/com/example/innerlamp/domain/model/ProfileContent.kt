package com.example.innerlamp.domain.model

/**
 * Các kiểu dữ liệu cho màn Hồ sơ (`profile` / tab "Tôi").
 *
 * Chỉ chứa hình dạng thuần Kotlin — không import Android/Compose/Room. Tầng UI
 * tự ánh xạ [BadgeTone] sang bộ màu của mình (xem `ProfileScreen`).
 */

/** Tông màu cho dấu ấn tu tập (được UI ánh xạ sang `IlTone`). */
enum class BadgeTone { Gold, Green, Rose }

/** Thông tin định danh ở header hồ sơ. */
data class ProfileUser(
    val fullName: String,
    /** Chữ cái hiển thị trên avatar (Lora). */
    val initials: String,
    val dharmaName: String,
    /** Vd "Phật tử tại gia". */
    val role: String,
)

/**
 * Một cột số liệu thực hành. [value] là chuỗi để giữ định dạng của prototype
 * (vd "9.7k" thay vì một số thô).
 */
data class PracticeStat(
    val value: String,
    val label: String,
)

/** Một dấu ấn tu tập (xem trước 3 trong tổng 18 ở màn `awards`). */
data class ProfileBadge(
    val name: String,
    val sub: String,
    val tone: BadgeTone,
)

/** Công đức đã hồi hướng (thẻ nổi bật ở hồ sơ). */
data class MeritSummary(
    /** Số lần cúng dường. */
    val offeringCount: Int,
    /** Số ngọn đèn (vd đèn Vu Lan). */
    val lampCount: Int,
)

package com.example.innerlamp.domain.model

import com.example.innerlamp.domain.calendar.LunarDate
import java.time.LocalDate

/** Loại ngày hiển thị ở chip trên bìa: Rằm / Mùng Một ưu tiên hơn hoàng đạo / hắc đạo. */
enum class LunarDayKind { Full, New, Good, Bad }

/** Một giờ hoàng đạo (tên chi, con giáp, khung giờ "23h – 01h"). */
data class AuspiciousHour(
    val chi: String,
    val animal: String,
    val range: String,
)

/** Một tuổi xung khắc (can chi + con giáp). */
data class ClashAge(
    val name: String,
    val animal: String,
)

/** Hướng xuất hành: [role] là Hỷ thần / Tài thần / Hắc thần. */
data class TravelDirection(
    val role: String,
    val direction: String,
    val meaning: String,
    val glyph: String,
)

/** Toàn bộ số liệu màn "Chi tiết ngày âm", tính từ một ngày dương lịch. */
data class LunarDayDetail(
    val date: LocalDate,
    val lunar: LunarDate,
    val kind: LunarDayKind,
    /** Can chi ngày, vd "Giáp Tý". */
    val dayCanChi: String,
    /** Can chi tháng (kèm "(nhuận)" nếu có). */
    val monthCanChi: String,
    val yearCanChi: String,
    val hours: List<AuspiciousHour>,
    val clashAges: List<ClashAge>,
    val clashNote: String,
    val directions: List<TravelDirection>,
    /** Lời khuyên trong ngày (Mùng Một / Rằm / ngày trai / ngày thường). */
    val advice: String,
)

package com.example.innerlamp.domain.model

import java.time.LocalDate
import java.time.LocalTime

/** Loại lịch của ngày mất: giỗ tính theo âm lịch (phổ biến) hoặc dương lịch. */
enum class MemorialCalendarType { Lunar, Solar }

/** Mối quan hệ với người đã khuất — khớp danh sách chip `GIO_REL` của prototype. */
enum class MemorialRelation(val label: String) {
    PaternalGrandfather("Ông nội"),
    PaternalGrandmother("Bà nội"),
    MaternalGrandfather("Ông ngoại"),
    MaternalGrandmother("Bà ngoại"),
    Father("Cha"),
    Mother("Mẹ"),
    Spouse("Vợ / Chồng"),
    Sibling("Anh chị em"),
    Child("Con"),
    Other("Khác"),
}

/**
 * Một người thân trong Sổ giỗ chạp.
 *
 * @property day ngày mất (âm: 1–30, dương: 1–31).
 * @property month tháng mất (1–12, theo [calendar]).
 * @property deathYear năm mất; `null` = không rõ (không đếm được giỗ lần thứ mấy).
 * @property remind bật nhắc trước ngày giỗ.
 * @property leadDays nhắc trước bao nhiêu ngày (0 = đúng ngày).
 * @property remindTime giờ nhắc trong ngày.
 * @property syncReminders đồng bộ lời nhắc ra ứng dụng nhắc của hệ thống.
 */
data class MemorialEntry(
    val id: String,
    val name: String,
    val relation: MemorialRelation,
    val calendar: MemorialCalendarType,
    val day: Int,
    val month: Int,
    val deathYear: Int?,
    val note: String,
    val remind: Boolean,
    val leadDays: Int,
    val remindTime: LocalTime,
    val syncReminders: Boolean,
)

/**
 * Kỳ giỗ kế tiếp của một [MemorialEntry].
 *
 * @property date ngày dương lịch của kỳ giỗ.
 * @property daysUntil số ngày từ hôm nay đến [date] (0 = hôm nay).
 * @property ordinal giỗ lần thứ mấy (1 = giỗ đầu, 2 = giỗ hết); `null` khi không rõ năm mất.
 */
data class MemorialOccurrence(
    val date: LocalDate,
    val daysUntil: Int,
    val ordinal: Int?,
)

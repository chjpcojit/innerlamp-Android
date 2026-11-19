package com.example.innerlamp.domain.model

import java.time.LocalDate
import java.time.LocalTime

/** Loại lịch của ngày mất: giỗ tính theo âm lịch (phổ biến) hoặc dương lịch. */
enum class MemorialCalendarType { Lunar, Solar }

/**
 * Mối quan hệ với người đã khuất — khớp danh sách chip `GIO_REL` của prototype.
 *
 * @property label nhãn trên UI.
 * @property apiCode giá trị `relationship` của contract (map tường minh, không suy từ nhãn).
 * @property promptNoun danh từ trong câu thông báo nhắc giỗ ("giỗ cha", "giỗ ông nội").
 */
enum class MemorialRelation(val label: String, val apiCode: String, val promptNoun: String) {
    PaternalGrandfather("Ông nội", "GRANDFATHER_PATERNAL", "ông nội"),
    PaternalGrandmother("Bà nội", "GRANDMOTHER_PATERNAL", "bà nội"),
    MaternalGrandfather("Ông ngoại", "GRANDFATHER_MATERNAL", "ông ngoại"),
    MaternalGrandmother("Bà ngoại", "GRANDMOTHER_MATERNAL", "bà ngoại"),
    Father("Cha", "FATHER", "cha"),
    Mother("Mẹ", "MOTHER", "mẹ"),
    Spouse("Vợ / Chồng", "SPOUSE", "vợ/chồng"),
    Sibling("Anh chị em", "SIBLING", "anh chị em"),
    Child("Con", "CHILD", "con"),
    Other("Khác", "OTHER", "người thân"),
}

/**
 * Một người thân trong Sổ giỗ chạp.
 *
 * @property id id của server (`Long`, `0` = mục mới chưa lưu).
 * @property day ngày mất (âm: 1–30, dương: 1–31).
 * @property month tháng mất (1–12, theo [calendar]).
 * @property deathYear năm mất trong chính lịch của mục (âm lịch nếu [calendar] là âm); `null` = không rõ.
 * @property leapMonth người mất trong tháng nhuận (chỉ âm lịch); giỗ luôn tính ở tháng thường.
 * @property note ghi chú; rỗng = không có.
 * @property remind bật nhắc trước ngày giỗ.
 * @property leadDays nhắc trước bao nhiêu ngày (0/1/3/7).
 * @property remindTime giờ nhắc trong ngày (06/07/12/19 giờ).
 * @property photoUrl signed URL 15 phút — chỉ giữ trong bộ nhớ, không lưu cache.
 */
data class MemorialEntry(
    val id: Long,
    val name: String,
    val relation: MemorialRelation,
    val calendar: MemorialCalendarType,
    val day: Int,
    val month: Int,
    val deathYear: Int?,
    val leapMonth: Boolean = false,
    val note: String = "",
    val remind: Boolean = true,
    val leadDays: Int = 1,
    val remindTime: LocalTime = LocalTime.of(7, 0),
    val photoUrl: String? = null,
) {
    /** A new entry has no server id yet. / Mục mới chưa có id server. */
    val isNew: Boolean get() = id == 0L
}

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

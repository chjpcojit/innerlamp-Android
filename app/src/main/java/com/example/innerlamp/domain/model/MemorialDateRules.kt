package com.example.innerlamp.domain.model

import com.example.innerlamp.domain.calendar.MemorialSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Field-level error of the memorial form. / Lỗi theo trường của form sổ giỗ. */
enum class MemorialFormError {
    NameEmpty,
    NameTooLong,
    NoteTooLong,
    DateNotExist,
    LeapMonthNotInYear,
    YearBefore1900,
    DateInFuture,
}

/**
 * Validation rules of a memorial entry before it is sent (BR-MEM-02/03/04, FR-MEM-012/013). Pure Kotlin,
 * the page passes "today" so the rules are testable. /
 * Luật kiểm dữ liệu trước khi gửi. Thuần Kotlin, trang truyền "hôm nay" để test được.
 */
object MemorialDateRules {

    const val NAME_MAX = 100
    const val NOTE_MAX = 1000
    const val MIN_YEAR = 1900

    /** Vietnam time — the contract checks "not in the future" in this zone. / Múi giờ để chặn ngày tương lai. */
    val vietnamZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    /** Trimmed name must be 1–100 characters. / Tên sau trim phải 1–100 ký tự. */
    fun nameError(raw: String): MemorialFormError? = when {
        raw.trim().isEmpty() -> MemorialFormError.NameEmpty
        raw.trim().length > NAME_MAX -> MemorialFormError.NameTooLong
        else -> null
    }

    /** Note may be blank (stored as none) but not longer than 1000. / Ghi chú được rỗng, tối đa 1000. */
    fun noteError(raw: String): MemorialFormError? =
        if (raw.trim().length > NOTE_MAX) MemorialFormError.NoteTooLong else null

    /**
     * Checks the date of death. With a known year the date must exist in that calendar (leap month
     * included), be from 1900 and not after [today]; without a year only day/month ranges are checked and
     * 29 February is allowed (contract §2.3). /
     * Kiểm ngày mất. Có năm: ngày phải tồn tại trong lịch đó (kể cả tháng nhuận), từ 1900 và không sau
     * [today]. Không năm: chỉ kiểm khoảng ngày/tháng, cho phép 29/02.
     */
    fun dateError(entry: MemorialEntry, today: LocalDate = LocalDate.now(vietnamZone)): MemorialFormError? {
        val year = entry.deathYear
        if (year != null) {
            if (year < MIN_YEAR) return MemorialFormError.YearBefore1900
            if (year > today.year) return MemorialFormError.DateInFuture
            val solar = when (entry.calendar) {
                MemorialCalendarType.Solar -> runCatching { LocalDate.of(year, entry.month, entry.day) }.getOrNull()
                    ?: return MemorialFormError.DateNotExist
                MemorialCalendarType.Lunar -> {
                    if (entry.month !in 1..12 || entry.day !in 1..30) return MemorialFormError.DateNotExist
                    if (entry.leapMonth && !MemorialSchedule.hasLeapMonth(year, entry.month)) {
                        return MemorialFormError.LeapMonthNotInYear
                    }
                    MemorialSchedule.lunarToSolarExact(entry.day, entry.month, year, entry.leapMonth)
                        ?: return MemorialFormError.DateNotExist
                }
            }
            if (solar.isAfter(today)) return MemorialFormError.DateInFuture
            return null
        }
        return when (entry.calendar) {
            MemorialCalendarType.Lunar ->
                if (entry.day !in 1..30 || entry.month !in 1..12) MemorialFormError.DateNotExist else null
            MemorialCalendarType.Solar -> {
                // 29 February is allowed because the year is unknown; 31 April is not.
                // Cho phép 29/02 vì không rõ năm; 31/04 thì không.
                val probe = runCatching { LocalDate.of(2000, entry.month, entry.day) }.getOrNull()
                if (probe == null || entry.month !in 1..12) MemorialFormError.DateNotExist else null
            }
        }
    }

    /** First error of the form in the order the fields appear. / Lỗi đầu tiên theo thứ tự trường. */
    fun firstError(entry: MemorialEntry, today: LocalDate = LocalDate.now(vietnamZone)): MemorialFormError? =
        nameError(entry.name) ?: noteError(entry.note) ?: dateError(entry, today)

    /** Reminder values outside the four choices are shown as the nearest one. / Giá trị cũ → gần nhất. */
    fun withNearestReminder(entry: MemorialEntry): MemorialEntry = entry.copy(
        leadDays = MemorialReminderChoices.nearestLeadDays(entry.leadDays),
        remindTime = MemorialReminderChoices.nearestTime(entry.remindTime),
    )

    /** Default values of a new entry (FR-MEM-003). / Giá trị mặc định của mục mới. */
    fun newEntry(relation: MemorialRelation = MemorialRelation.PaternalGrandfather): MemorialEntry = MemorialEntry(
        id = 0L,
        name = "",
        relation = relation,
        calendar = MemorialCalendarType.Lunar,
        day = 1,
        month = 1,
        deathYear = null,
        note = "",
        remind = true,
        leadDays = 1,
        remindTime = LocalTime.of(7, 0),
    )
}

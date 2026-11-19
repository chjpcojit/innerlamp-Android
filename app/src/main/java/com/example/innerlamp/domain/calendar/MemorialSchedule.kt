package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialOccurrence
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Tính kỳ giỗ kế tiếp — port `gioNext`/`gioL2S` của prototype.
 *
 * Giỗ âm đổi sang dương mỗi năm một khác nên phải quy đổi lại theo từng năm âm.
 * Thuần Kotlin, không phụ thuộc Android — test được trên JVM.
 */
object MemorialSchedule {

    /**
     * Kỳ giỗ gần nhất từ [today] trở đi (gồm cả hôm nay). Nếu mọi ứng viên đều đã qua
     * thì lấy ứng viên cuối cùng (khớp prototype).
     */
    fun next(entry: MemorialEntry, today: LocalDate): MemorialOccurrence = upcoming(entry, today, 1).first()

    /**
     * [count] kỳ giỗ kế tiếp, sớm nhất trước. Dùng để đặt trước nhiều lần nhắc. /
     * The next [count] anniversaries, earliest first; used to schedule several reminders ahead.
     */
    fun upcoming(entry: MemorialEntry, today: LocalDate, count: Int): List<MemorialOccurrence> {
        require(count > 0) { "count must be positive / count phải dương" }
        val candidates: List<Pair<LocalDate, Int>> = when (entry.calendar) {
            MemorialCalendarType.Lunar -> {
                val lunarYear = LunarCalendar.toLunar(today).year
                (lunarYear - 1..lunarYear + count + 1).mapNotNull { year ->
                    lunarToSolar(entry.day, entry.month, year)?.let { it to year }
                }
            }
            MemorialCalendarType.Solar -> (today.year..today.year + count).map { year ->
                val length = LocalDate.of(year, entry.month, 1).lengthOfMonth()
                LocalDate.of(year, entry.month, minOf(entry.day, length)) to year
            }
        }
        // Tháng âm thường (không nhuận) luôn tồn tại trong mọi năm âm nên không thể rỗng.
        val upcoming = candidates.filter { it.first >= today }.take(count)
        val picked = if (upcoming.size == count) {
            upcoming
        } else {
            // Sổ cạn ứng viên (chỉ khi count lớn hơn số năm đã dựng) → bù bằng ứng viên cuối.
            // Ran out of candidates (only when count exceeds the years built) → pad with the last one.
            upcoming + List(count - upcoming.size) { candidates.last() }
        }
        return picked.map { (date, year) ->
            MemorialOccurrence(
                date = date,
                daysUntil = ChronoUnit.DAYS.between(today, date).toInt(),
                ordinal = entry.deathYear?.let { year - it },
            )
        }
    }

    /**
     * Ngày dương của ngày [day] tháng [month] (tháng thường, không nhuận) trong năm âm [lunarYear].
     * Ngày 30 của tháng thiếu rơi vào ngày 29 cuối tháng — cùng quy ước với
     * [LunarCalendar.matchesLunarDay]. Trả `null` nếu không tìm thấy.
     */
    fun lunarToSolar(day: Int, month: Int, lunarYear: Int): LocalDate? {
        // Một năm âm trải từ cuối tháng 1 dương năm đó đến tháng 2 dương năm sau.
        var date = LocalDate.of(lunarYear, 1, 1)
        val end = LocalDate.of(lunarYear + 1, 3, 1)
        var lastOfMonth: LocalDate? = null
        while (date.isBefore(end)) {
            val lunar = LunarCalendar.toLunar(date)
            if (lunar.year == lunarYear && lunar.month == month && !lunar.isLeapMonth) {
                if (lunar.day == day) return date
                lastOfMonth = date
            } else if (lastOfMonth != null) {
                break // đã qua hết tháng cần tìm
            }
            date = date.plusDays(1)
        }
        return if (day == LunarCalendar.DAY_30) lastOfMonth else null
    }

    /**
     * Exact conversion of a lunar date that must exist as written (leap month included), used before
     * sending a date of death. `null` when that lunar year has no such day — including day 30 of a
     * 29-day month, which the anniversary calculation folds onto day 29. /
     * Quy đổi chính xác một ngày âm phải tồn tại đúng như đã nhập (kể cả tháng nhuận), dùng trước khi
     * gửi ngày mất. `null` khi năm âm đó không có ngày đó — kể cả ngày 30 của tháng thiếu.
     */
    fun lunarToSolarExact(day: Int, month: Int, year: Int, leap: Boolean): LocalDate? {
        if (year < 1900 || month !in 1..12 || day !in 1..30) return null
        var date = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year + 1, 3, 1)
        while (date.isBefore(end)) {
            val lunar = LunarCalendar.toLunar(date)
            if (lunar.year == year && lunar.month == month &&
                lunar.isLeapMonth == leap && lunar.day == day
            ) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    /** `true` when that lunar year really has a leap [month]. / Năm âm đó có tháng nhuận [month] không. */
    fun hasLeapMonth(year: Int, month: Int): Boolean {
        if (month !in 1..12) return false
        var date = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year + 1, 3, 1)
        while (date.isBefore(end)) {
            val lunar = LunarCalendar.toLunar(date)
            if (lunar.year == year && lunar.month == month && lunar.isLeapMonth) return true
            date = date.plusDays(1)
        }
        return false
    }
}

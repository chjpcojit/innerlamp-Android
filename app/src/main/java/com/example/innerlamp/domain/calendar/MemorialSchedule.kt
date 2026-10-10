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
    fun next(entry: MemorialEntry, today: LocalDate): MemorialOccurrence {
        val candidates: List<Pair<LocalDate, Int>> = when (entry.calendar) {
            MemorialCalendarType.Lunar -> {
                val lunarYear = LunarCalendar.toLunar(today).year
                (lunarYear - 1..lunarYear + 1).mapNotNull { year ->
                    lunarToSolar(entry.day, entry.month, year)?.let { it to year }
                }
            }
            MemorialCalendarType.Solar -> (today.year..today.year + 1).map { year ->
                val length = LocalDate.of(year, entry.month, 1).lengthOfMonth()
                LocalDate.of(year, entry.month, minOf(entry.day, length)) to year
            }
        }
        // Tháng âm thường (không nhuận) luôn tồn tại trong mọi năm âm nên không thể rỗng.
        val (date, year) = candidates.firstOrNull { it.first >= today }
            ?: candidates.lastOrNull()
            ?: error("Không quy đổi được ngày giỗ ${entry.day}/${entry.month} (${entry.calendar})")
        val ordinal = entry.deathYear?.let { year - it }
        return MemorialOccurrence(
            date = date,
            daysUntil = ChronoUnit.DAYS.between(today, date).toInt(),
            ordinal = ordinal,
        )
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
}

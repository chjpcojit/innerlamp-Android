package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.CalendarOrdering
import com.example.innerlamp.domain.calendar.CalendarTime
import com.example.innerlamp.domain.calendar.HomeEventBlock
import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.ChuaFastDay
import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.model.Recurrence
import com.example.innerlamp.domain.repository.CalendarEventRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The [count] nearest Buddhist-group days from [today] for the "Ngày lễ & ngày chay sắp tới" block of
 * the Chùa tab, read from [CalendarEventRepository] (BR-CAL-12 order; Mùng 1 / Rằm are recognised by
 * `Recurrence.LunarMonthly`). /
 * [count] ngày lễ / ngày chay gần nhất từ [today] cho khối ở tab Chùa, đọc từ [CalendarEventRepository]
 * (thứ tự BR-CAL-12; Mùng 1 / Rằm nhận biết qua `Recurrence.LunarMonthly`).
 */
class GetUpcomingFastDaysUseCase(private val repository: CalendarEventRepository) {

    /**
     * Loads the current year (and the next one when too few days remain). A failure of the first year
     * is returned as a failure; a failure of the next year only shortens the list. /
     * Tải năm hiện tại (và năm sau khi còn ít ngày). Lỗi năm đầu trả failure; lỗi năm sau chỉ làm ngắn danh sách.
     */
    suspend operator fun invoke(today: LocalDate, count: Int = DEFAULT_COUNT): Result<List<ChuaFastDay>> {
        val upcoming = mutableListOf<EventOccurrence>()
        for (year in today.year..minOf(today.year + 1, CalendarTime.MAX_YEAR)) {
            val data = repository.cachedInMemory(year)
                ?: repository.load(year).getOrElse { error ->
                    if (year == today.year) return Result.failure(error) else break
                }.year
            upcoming += pick(data.entries, today)
            if (upcoming.size >= count) break
        }
        return Result.success(upcoming.take(count).map { it.toFastDay(today) })
    }

    /** Same list built only from what is already on the device; null when the year is missing. / Chỉ từ dữ liệu có sẵn trên máy. */
    suspend fun fromDeviceCopy(today: LocalDate, count: Int = DEFAULT_COUNT): List<ChuaFastDay>? {
        val data = repository.cachedInMemory(today.year) ?: repository.deviceCopy(today.year) ?: return null
        return pick(data.entries, today).take(count).map { it.toFastDay(today) }
    }

    private fun pick(entries: List<EventOccurrence>, today: LocalDate): List<EventOccurrence> =
        CalendarOrdering.sort(
            entries.filter {
                HomeEventBlock.BUDDHIST_GROUP in it.event.groupCodes && !it.occurrence.solarDate.isBefore(today)
            },
        )

    private fun EventOccurrence.toFastDay(today: LocalDate): ChuaFastDay {
        val lunar = occurrence.lunar
        return ChuaFastDay(
            date = occurrence.solarDate,
            title = event.name,
            lunarDay = lunar.day,
            lunarMonthName = LunarCalendar.MONTH_NAMES[(lunar.month - 1).coerceIn(0, 11)],
            daysAway = ChronoUnit.DAYS.between(today, occurrence.solarDate).toInt(),
            isFestival = event.isMajor,
            suggestion = when {
                event.isMajor -> "Gợi ý: tụng kinh và hồi hướng công đức"
                event.recurrence == Recurrence.LunarMonthly && lunar.day == 15 -> "Gợi ý: ăn chay, tụng Kinh Phổ Môn"
                else -> "Gợi ý: ăn chay, lễ sám hối"
            },
        )
    }

    companion object {
        const val DEFAULT_COUNT = 4
    }
}

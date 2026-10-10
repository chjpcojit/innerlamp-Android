package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.ChuaFastDay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * [count] ngày lễ/ngày chay gần nhất kể từ [today] (khối "Ngày lễ & ngày chay sắp tới"
 * ở tab Chùa; prototype `chuaVals`). Ngày âm tính thật bằng [LunarCalendar].
 */
class GetUpcomingFastDaysUseCase(
    private val events: GetBuddhistEventMonthsUseCase = GetBuddhistEventMonthsUseCase(),
) {

    operator fun invoke(today: LocalDate, count: Int = DEFAULT_COUNT): List<ChuaFastDay> =
        events(today, months = SEARCH_MONTHS)
            .flatMap { it.events }
            .filter { !it.date.isBefore(today) }
            .take(count)
            .map { event ->
                val lunar = LunarCalendar.toLunar(event.date)
                ChuaFastDay(
                    date = event.date,
                    title = event.title,
                    lunarDay = lunar.day,
                    lunarMonthName = lunar.monthName,
                    daysAway = ChronoUnit.DAYS.between(today, event.date).toInt(),
                    isFestival = event.isFestival,
                    suggestion = when {
                        event.isFestival -> "Gợi ý: tụng kinh và hồi hướng công đức"
                        lunar.day == 15 -> "Gợi ý: ăn chay, tụng Kinh Phổ Môn"
                        else -> "Gợi ý: ăn chay, lễ sám hối"
                    },
                )
            }

    companion object {
        const val DEFAULT_COUNT = 4

        /** Tìm trong 4 tháng tới — đủ để có ≥ 4 ngày chay (mỗi tháng có Mùng Một + Rằm). */
        private const val SEARCH_MONTHS = 4
    }
}

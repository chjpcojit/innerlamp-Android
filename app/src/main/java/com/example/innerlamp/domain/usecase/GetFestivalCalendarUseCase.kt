package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.FestivalEntry
import com.example.innerlamp.domain.model.FestivalMonth
import com.example.innerlamp.domain.model.Temple
import java.time.LocalDate

/**
 * Lịch lễ Phật giáo cho [years] năm liên tiếp, từ tháng 1 của [startYear]
 * (màn "Lịch lễ Phật giáo"; prototype `festVals`).
 *
 * Chùa tổ chức là dữ liệu mẫu theo prototype: đại lễ → mọi chùa; lễ vía khác → chùa đầu danh bạ
 * cộng hai chùa chọn ổn định theo ngày âm; Mùng Một/Rằm → chùa đầu danh bạ.
 * TODO(il-data-builder): thay bằng dữ liệu thật khi backend có.
 */
class GetFestivalCalendarUseCase(
    private val events: GetBuddhistEventMonthsUseCase = GetBuddhistEventMonthsUseCase(),
    private val temples: () -> List<Temple> = { GetTemplesUseCase()() },
) {

    operator fun invoke(startYear: Int, years: Int = DEFAULT_YEARS): List<FestivalMonth> {
        val directory = temples()
        return events(LocalDate.of(startYear, 1, 1), months = years * 12)
            .filter { it.events.isNotEmpty() }
            .map { month ->
                FestivalMonth(
                    year = month.year,
                    month = month.month,
                    entries = month.events.map { event ->
                        val lunar = LunarCalendar.toLunar(event.date)
                        val key = "${lunar.day}/${lunar.month}"
                        FestivalEntry(
                            event = event,
                            isMajor = key in MAJOR_KEYS,
                            hosts = hostsFor(key, event.isFestival, directory),
                        )
                    },
                )
            }
    }

    private fun hostsFor(key: String, isFestival: Boolean, directory: List<Temple>): List<Temple> {
        if (directory.size < 2) return directory
        if (key in MAJOR_KEYS) return directory
        if (!isFestival) return listOf(directory.first())
        val hash = key.fold(0) { acc, ch -> (acc * 31 + ch.code) % 97 }
        val others = directory.size - 1
        return listOf(
            directory.first(),
            directory[1 + hash % others],
            directory[1 + (hash + 3) % others],
        ).distinct()
    }

    companion object {
        const val DEFAULT_YEARS = 3

        /** Đại lễ — khớp `BIG` trong prototype. */
        private val MAJOR_KEYS = setOf("15/1", "15/2", "19/2", "15/4", "19/6", "15/7", "19/9", "8/12")
    }
}

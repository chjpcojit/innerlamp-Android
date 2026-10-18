package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.model.CalendarEvent
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.model.Recurrence
import com.example.innerlamp.domain.calendar.HomeEventBlock
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.HomeMonthCard
import com.example.innerlamp.domain.usecase.HomeMonthOutcome
import java.time.LocalDate
import java.time.YearMonth

/**
 * Sample data for `@Preview` only (never shipped as a data source). /
 * Dữ liệu mẫu chỉ cho `@Preview` (không dùng làm nguồn dữ liệu).
 */
internal object CalendarPreviewData {
    private val groups = listOf(CalendarGroup("buddhist", "Phật giáo"), CalendarGroup("folk", "Dân gian"))

    private fun event(code: String, name: String, major: Boolean, order: Int, group: String = "buddhist") = CalendarEvent(
        code = code,
        name = name,
        shortDescription = "Mô tả ngắn của $name.",
        longDescription = "Đoạn một của $name.\nĐoạn hai của $name.",
        guidance = "Ăn chay\nTụng kinh\nHồi hướng công đức",
        imageUrl = null,
        groupCodes = listOf(group),
        isPublicHoliday = false,
        isMajor = major,
        sortOrder = order,
        recurrence = Recurrence.Lunar,
    )

    val events = listOf(
        event("vu_lan", "Lễ Vu Lan", major = true, order = 1),
        event("via_quan_am", "Vía Quan Thế Âm", major = false, order = 2),
        event("trung_thu", "Tết Trung thu", major = false, order = 3, group = "folk"),
    )

    val year2026 = CalendarYear(
        year = 2026,
        groups = groups,
        events = events,
        occurrences = listOf(
            CalendarOccurrence("via_quan_am", LocalDate.of(2026, 10, 7), LunarDay(19, 8, 2026, false)),
            CalendarOccurrence("trung_thu", LocalDate.of(2026, 10, 25), LunarDay(15, 8, 2026, true)),
            CalendarOccurrence("vu_lan", LocalDate.of(2026, 11, 3), LunarDay(15, 7, 2026, false)),
        ),
    )

    /** Six month cards for the Home preview (one empty month, one failed). */
    fun homeCards(): List<HomeMonthCard> {
        val start = YearMonth.of(2026, 10)
        val entries = year2026.entries
        return (0 until 6).map { i ->
            val month = start.plusMonths(i.toLong())
            val inMonth = entries.filter { YearMonth.from(it.occurrence.solarDate) == month }
            val outcome = when {
                i == 5 -> HomeMonthOutcome.Failed(CalendarLoadError.Offline)
                else -> HomeEventBlock.select(inMonth).let { HomeMonthOutcome.Loaded(it.shown, it.more, mayBeOutOfDate = false) }
            }
            HomeMonthCard(month, outcome)
        }
    }

    fun festivalsState() = FestivalsUiState(
        today = LocalDate.of(2026, 10, 17),
        years = listOf(year2026),
        loadState = FestivalsLoadState.Loaded(),
    )
}

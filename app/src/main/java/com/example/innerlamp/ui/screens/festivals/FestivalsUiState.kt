package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.LocalDate

/** Chip filter of the festival calendar (AC10). / Bộ lọc của lịch lễ. */
sealed interface CalendarFilter {
    /** "Tất cả" */
    data object All : CalendarFilter

    /** "Lễ lớn" = events flagged `isMajor` (BR-CAL-10). */
    data object Major : CalendarFilter

    /** One chip per group of the server. / Một chip cho mỗi nhóm của server. */
    data class Group(val code: String) : CalendarFilter
}

/** State of loading the first (current) year. / Trạng thái tải năm hiện tại. */
sealed interface FestivalsLoadState {
    /** Nothing to show yet. / Chưa có gì để hiện. */
    data object Loading : FestivalsLoadState

    /** Data is shown; [refreshing] while the network call still runs after the device copy. */
    data class Loaded(val refreshing: Boolean = false, val mayBeOutOfDate: Boolean = false) : FestivalsLoadState

    /** No data at all: offline / rate limited / failed + "Thử lại". */
    data class Error(val error: CalendarLoadError) : FestivalsLoadState
}

/** State of appending the next year at the end of the list (AC12, AC13). / Trạng thái nạp năm kế tiếp. */
sealed interface NextYearState {
    data object Idle : NextYearState
    data class Loading(val year: Int) : NextYearState
    data class Error(val year: Int, val error: CalendarLoadError) : NextYearState

    /** The last year (2100) is loaded. / Đã tới năm 2100. */
    data object End : NextYearState
}

/** A chip with its count. / Một chip kèm số lượng. */
data class FilterChip(val filter: CalendarFilter, val label: String, val count: Int)

/** One flat row of the list: month heading or an occurrence. / Một dòng của danh sách phẳng. */
sealed interface FestivalRow {
    data class Header(val year: Int, val month: Int) : FestivalRow
    data class Item(val entry: EventOccurrence, val groupLabel: String) : FestivalRow
}

/**
 * Render state of the festival calendar. [today] is the device date (countdown, scroll target). /
 * State render của lịch lễ. [today] là ngày trên máy (đếm ngược, cuộn tới tháng hiện tại).
 */
data class FestivalsUiState(
    val today: LocalDate,
    val years: List<CalendarYear> = emptyList(),
    val filter: CalendarFilter = CalendarFilter.All,
    val loadState: FestivalsLoadState = FestivalsLoadState.Loading,
    val nextYearState: NextYearState = NextYearState.Idle,
) {
    /** Groups of all loaded years, first occurrence order, de-duplicated by code. */
    val groups: List<CalendarGroup>
        get() = years.flatMap { it.groups }.distinctBy { it.code }

    private val entries: List<EventOccurrence>
        get() = years.flatMap { it.entries }

    private fun matches(entry: EventOccurrence, filter: CalendarFilter) = when (filter) {
        CalendarFilter.All -> true
        CalendarFilter.Major -> entry.event.isMajor
        is CalendarFilter.Group -> filter.code in entry.event.groupCodes
    }

    /** "Tất cả", "Lễ lớn", then one chip per group, each with its count (AC10). */
    val chips: List<FilterChip>
        get() {
            val all = entries
            return buildList {
                add(FilterChip(CalendarFilter.All, "Tất cả", all.size))
                add(FilterChip(CalendarFilter.Major, "Lễ lớn", all.count { it.event.isMajor }))
                groups.forEach { g ->
                    val f = CalendarFilter.Group(g.code)
                    add(FilterChip(f, g.name, all.count { matches(it, f) }))
                }
            }
        }

    /** Flat rows (month headings + occurrences) of the active filter, in server order. */
    val rows: List<FestivalRow>
        get() {
            val names = groups.associate { it.code to it.name }
            val out = mutableListOf<FestivalRow>()
            var last: Pair<Int, Int>? = null
            entries.filter { matches(it, filter) }.forEach { e ->
                val d = e.occurrence.solarDate
                val key = d.year to d.monthValue
                if (key != last) {
                    out += FestivalRow.Header(d.year, d.monthValue)
                    last = key
                }
                val label = e.event.groupCodes.mapNotNull { names[it] }.joinToString(" · ")
                out += FestivalRow.Item(e, label)
            }
            return out
        }

    /** Index of the heading of the current month (or the nearest later one) in [rows]. */
    val todayRowIndex: Int
        get() {
            val r = rows
            val i = r.indexOfFirst {
                it is FestivalRow.Header && it.year * 100 + it.month >= today.year * 100 + today.monthValue
            }
            return if (i >= 0) i else r.lastIndex.coerceAtLeast(0)
        }

    /** Last loaded year, or null before anything is loaded. / Năm cuối đã tải. */
    val lastYear: Int? get() = years.maxOfOrNull { it.year }
}

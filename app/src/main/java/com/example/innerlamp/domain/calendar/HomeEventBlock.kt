package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.model.Recurrence

/**
 * BR-CAL-15: the rows of one month card of the Home block. /
 * BR-CAL-15: các dòng của một thẻ tháng ở khối Home.
 *
 * Buddhist-group occurrences only; at most [MAX_ROWS]; tier 1 = major, tier 2 = other named events,
 * tier 3 = Mùng 1 / Rằm (`recurrence == LunarMonthly`, never by `code`); BR-CAL-12 order in a tier;
 * the rest is counted in [Selection.more]. An unknown recurrence counts as a named event.
 */
object HomeEventBlock {
    /** Stable group code of the Buddhist group (BR-CAL-09). / Mã nhóm Phật giáo. */
    const val BUDDHIST_GROUP = "buddhist"
    const val MAX_ROWS = 3

    data class Selection(val shown: List<EventOccurrence>, val more: Int)

    fun select(monthOccurrences: List<EventOccurrence>): Selection {
        val buddhist = monthOccurrences.filter { BUDDHIST_GROUP in it.event.groupCodes }
        val ordered = buddhist
            .sortedWith(compareBy<EventOccurrence> { tier(it) }.then(CalendarOrdering.comparator))
        val shown = ordered.take(MAX_ROWS)
        return Selection(shown, buddhist.size - shown.size)
    }

    private fun tier(e: EventOccurrence): Int = when {
        e.event.isMajor -> 1
        e.event.recurrence == Recurrence.LunarMonthly -> 3
        else -> 2
    }
}

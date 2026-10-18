package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.EventOccurrence

/**
 * BR-CAL-12: order of occurrences: solar date, then the event's `sort_order`, then its `code`. /
 * BR-CAL-12: thứ tự occurrence: ngày dương, rồi `sort_order` của sự kiện, rồi `code`.
 */
object CalendarOrdering {
    val comparator: Comparator<EventOccurrence> =
        compareBy<EventOccurrence> { it.occurrence.solarDate }
            .thenBy { it.event.sortOrder }
            .thenBy { it.event.code }

    fun sort(entries: List<EventOccurrence>): List<EventOccurrence> = entries.sortedWith(comparator)
}

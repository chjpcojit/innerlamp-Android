package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.EventOccurrence
import java.time.LocalDate

/**
 * BR-CAL-11 (AC32): a public holiday is marked only on the occurrence's own solar day. The app never
 * derives or shows substitute days off (nghỉ bù) and adds no occurrence for them. /
 * BR-CAL-11 (AC32): ngày nghỉ lễ chỉ được đánh dấu đúng ngày của occurrence; app không suy ra hay hiện
 * ngày nghỉ bù và không thêm occurrence cho chúng.
 */
object PublicHoliday {

    /** The mark of an occurrence: its event is a public holiday (on its own day by construction). */
    fun isMarked(entry: EventOccurrence): Boolean = entry.event.isPublicHoliday

    /** Public-holiday occurrences falling exactly on [date]; empty for any other day. */
    fun on(year: CalendarYear, date: LocalDate): List<EventOccurrence> =
        year.entries.filter { it.occurrence.solarDate == date && isMarked(it) }
}

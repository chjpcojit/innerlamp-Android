package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.CalendarYear
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** BR-CAL-11 / AC32: marked only on its own day, no substitute days off. */
class PublicHolidayTest {

    private val tet = event("tet-nguyen-dan", isPublicHoliday = true, groups = listOf("vietnam"))
    private val year = CalendarYear(
        year = 2026,
        groups = emptyList(),
        events = listOf(tet, event("via-di-lac")),
        occurrences = listOf(occurrence("tet-nguyen-dan", "2026-02-17"), occurrence("via-di-lac", "2026-02-17")),
    )

    @Test
    fun marksOnlyTheOwnDay() {
        assertEquals(listOf("tet-nguyen-dan"), PublicHoliday.on(year, LocalDate.parse("2026-02-17")).map { it.event.code })
    }

    @Test
    fun noSubstituteDayOff_theFollowingDaysHaveNoMarkAndNoOccurrence() {
        for (d in listOf("2026-02-18", "2026-02-19", "2026-02-20")) {
            assertTrue(PublicHoliday.on(year, LocalDate.parse(d)).isEmpty())
        }
        assertEquals(2, year.entries.size)
        assertFalse(PublicHoliday.isMarked(year.entries.first { it.event.code == "via-di-lac" }))
        assertTrue(PublicHoliday.isMarked(year.entries.first { it.event.code == "tet-nguyen-dan" }))
    }
}

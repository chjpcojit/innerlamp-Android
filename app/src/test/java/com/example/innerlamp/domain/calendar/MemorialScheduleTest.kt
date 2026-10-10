package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemorialScheduleTest {

    private fun entry(
        calendar: MemorialCalendarType,
        day: Int,
        month: Int,
        deathYear: Int? = null,
    ) = MemorialEntry(
        id = "t", name = "Test", relation = MemorialRelation.Other, calendar = calendar,
        day = day, month = month, deathYear = deathYear, note = "", remind = false,
        leadDays = 0, remindTime = LocalTime.of(7, 0), syncReminders = false,
    )

    @Test
    fun lunar_new_year_converts_to_known_solar_dates() {
        assertEquals(LocalDate.of(2024, 2, 10), MemorialSchedule.lunarToSolar(1, 1, 2024))
        assertEquals(LocalDate.of(2026, 2, 17), MemorialSchedule.lunarToSolar(1, 1, 2026))
    }

    @Test
    fun lunar_to_solar_round_trips_through_to_lunar() {
        for (month in 1..12) {
            val date = MemorialSchedule.lunarToSolar(15, month, 2025)
            assertNotNull("tháng $month", date)
            val back = LunarCalendar.toLunar(date!!)
            assertEquals(15, back.day)
            assertEquals(month, back.month)
            assertEquals(2025, back.year)
            assertFalse(back.isLeapMonth)
        }
    }

    @Test
    fun day_30_falls_on_last_day_of_short_month() {
        for (month in 1..12) {
            val date = MemorialSchedule.lunarToSolar(30, month, 2026)
            assertNotNull("tháng $month", date)
            val back = LunarCalendar.toLunar(date!!)
            assertEquals(month, back.month)
            assertTrue("ngày ${back.day}", back.day == 29 || back.day == 30)
            // Ngày kế tiếp phải sang tháng mới.
            assertEquals(1, LunarCalendar.toLunar(date.plusDays(1)).day)
        }
    }

    @Test
    fun lunar_to_solar_returns_null_for_impossible_day() {
        assertNull(MemorialSchedule.lunarToSolar(31, 1, 2026))
    }

    @Test
    fun next_solar_occurrence_this_year_with_ordinal() {
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Solar, 14, 12, deathYear = 2019), LocalDate.of(2026, 10, 9))
        assertEquals(LocalDate.of(2026, 12, 14), next.date)
        assertEquals(66, next.daysUntil)
        assertEquals(7, next.ordinal)
    }

    @Test
    fun next_solar_occurrence_rolls_to_next_year_after_the_day() {
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Solar, 14, 12), LocalDate.of(2026, 12, 15))
        assertEquals(LocalDate.of(2027, 12, 14), next.date)
        assertNull(next.ordinal)
    }

    @Test
    fun solar_occurrence_today_counts_as_zero_days() {
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Solar, 14, 12), LocalDate.of(2026, 12, 14))
        assertEquals(0, next.daysUntil)
    }

    @Test
    fun solar_feb_29_is_clamped_in_non_leap_year() {
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Solar, 29, 2), LocalDate.of(2026, 1, 1))
        assertEquals(LocalDate.of(2026, 2, 28), next.date)
    }

    @Test
    fun next_lunar_occurrence_is_never_in_the_past_and_matches_lunar_day() {
        val today = LocalDate.of(2026, 10, 9)
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Lunar, 23, 8, deathYear = 2015), today)
        assertTrue(next.date >= today)
        val lunar = LunarCalendar.toLunar(next.date)
        assertEquals(23, lunar.day)
        assertEquals(8, lunar.month)
        assertEquals(lunar.year - 2015, next.ordinal)
    }

    @Test
    fun lunar_occurrence_on_new_years_day_before_tet_uses_previous_lunar_year() {
        // 2026-02-16 vẫn thuộc năm âm 2025 → giỗ 12/3 âm kế tiếp rơi vào năm âm 2026.
        val next = MemorialSchedule.next(entry(MemorialCalendarType.Lunar, 12, 3, deathYear = 2009), LocalDate.of(2026, 2, 16))
        assertEquals(2026, LunarCalendar.toLunar(next.date).year)
        assertEquals(17, next.ordinal)
    }
}

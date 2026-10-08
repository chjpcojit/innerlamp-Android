package com.example.innerlamp.domain.calendar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LunarCalendarTest {

    private fun lunar(y: Int, m: Int, d: Int) = LunarCalendar.toLunar(LocalDate.of(y, m, d))

    @Test
    fun tet_2024_is_lunar_new_year() {
        val l = lunar(2024, 2, 10)
        assertEquals(1, l.day)
        assertEquals(1, l.month)
        assertEquals(2024, l.year)
        assertEquals("Giáp Thìn", l.canChiYear)
    }

    @Test
    fun tet_2025_and_2026() {
        assertEquals(Triple(1, 1, 2025), lunar(2025, 1, 29).let { Triple(it.day, it.month, it.year) })
        assertEquals(Triple(1, 1, 2026), lunar(2026, 2, 17).let { Triple(it.day, it.month, it.year) })
    }

    @Test
    fun day_before_tet_belongs_to_previous_lunar_year() {
        val l = lunar(2024, 2, 9)
        assertEquals(30, l.day)
        assertEquals(12, l.month)
        assertEquals(2023, l.year)
    }

    @Test
    fun trung_thu_2024_is_ram_thang_tam() {
        val l = lunar(2024, 9, 17)
        assertEquals(15, l.day)
        assertEquals(8, l.month)
    }

    @Test
    fun vu_lan_2024_is_ram_thang_bay() {
        val l = lunar(2024, 8, 18)
        assertEquals(15, l.day)
        assertEquals(7, l.month)
    }

    @Test
    fun leap_month_2023_is_detected() {
        // 2023 nhuận tháng Hai: 22/3/2023 là mùng Một tháng Hai nhuận.
        val l = lunar(2023, 3, 22)
        assertEquals(1, l.day)
        assertEquals(2, l.month)
        assertTrue(l.isLeapMonth)
        assertFalse(lunar(2023, 2, 20).isLeapMonth)
    }

    @Test
    fun good_day_pattern_repeats_every_twelve_days_within_a_lunar_month() {
        // Bảng tra 12 chi ngày có đúng 6 ngày hoàng đạo — bất biến bất kể mốc thời gian,
        // miễn 12 ngày liên tiếp không cắt ngang đầu tháng âm (đổi tháng đổi mốc tra).
        val start = LocalDate.of(2026, 3, 2) // giữa tháng âm, đủ chỗ cho 12 ngày liên tiếp
        val days = (0 until 12).map { start.plusDays(it.toLong()) }
        assertTrue(days.all { LunarCalendar.toLunar(it).month == LunarCalendar.toLunar(start).month })
        assertEquals(6, days.count { LunarCalendar.isGoodDay(it) })
    }

    @Test
    fun good_day_is_deterministic_for_the_same_date() {
        val d = LocalDate.of(2026, 9, 25)
        assertEquals(LunarCalendar.isGoodDay(d), LunarCalendar.isGoodDay(d))
    }

    @Test
    fun month_name_and_can_chi_helpers() {
        assertEquals("Chạp", LunarDateSample(month = 12).monthName)
        assertEquals("Mười Một", LunarDateSample(month = 11).monthName)
    }

    private val scanDays: List<LocalDate> =
        generateSequence(LocalDate.of(2021, 1, 1)) { it.plusDays(1) }
            .takeWhile { it <= LocalDate.of(2030, 12, 31) }
            .toList()

    @Test
    fun last_day_of_short_month_iff_day_29_and_next_day_is_first() {
        scanDays.filter { it.year in 2023..2025 }.forEach { d ->
            val expected = lunar(d.year, d.monthValue, d.dayOfMonth).day == 29 &&
                LunarCalendar.toLunar(d.plusDays(1)).day == 1
            assertEquals("$d", expected, LunarCalendar.isLastDayOfShortMonth(LunarCalendar.toLunar(d), d))
        }
    }

    @Test
    fun last_day_of_every_lunar_month_is_a_fast_day() {
        scanDays.forEach { d ->
            val l = LunarCalendar.toLunar(d)
            if (LunarCalendar.toLunar(d.plusDays(1)).day == 1) {
                assertTrue("$d", LunarCalendar.isFastDay(l, d))
            }
        }
    }

    @Test
    fun day_30_target_matches_exactly_one_day_per_lunar_month() {
        // Mỗi (năm âm, tháng âm, nhuận) có đúng 1 ngày khớp mốc 30, và đó là ngày cuối tháng.
        val matches = scanDays.filter {
            LunarCalendar.matchesLunarDay(30, LunarCalendar.toLunar(it), it)
        }
        val keys = matches.map { LunarCalendar.toLunar(it).let { l -> Triple(l.year, l.month, l.isLeapMonth) } }
        assertEquals(keys.size, keys.toSet().size)
        matches.forEach { assertEquals(1, LunarCalendar.toLunar(it.plusDays(1)).day) }
    }

    @Test
    fun leap_month_first_day_is_still_a_fast_day() {
        val d = LocalDate.of(2023, 3, 22)
        assertTrue(LunarCalendar.isFastDay(LunarCalendar.toLunar(d), d))
    }

    private fun LunarDateSample(month: Int) = LunarDate(1, month, 2024, false)
}

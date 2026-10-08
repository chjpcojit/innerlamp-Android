package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetBuddhistEventMonthsUseCaseTest {

    private val useCase = GetBuddhistEventMonthsUseCase()

    @Test
    fun returns_requested_number_of_consecutive_months_from_start_month() {
        val months = useCase(LocalDate.of(2026, 11, 20), months = 4)
        assertEquals(
            listOf(2026 to 11, 2026 to 12, 2027 to 1, 2027 to 2),
            months.map { it.year to it.month },
        )
    }

    @Test
    fun default_is_six_months() {
        assertEquals(6, useCase(LocalDate.of(2026, 9, 25)).size)
    }

    @Test
    fun events_are_sorted_and_stay_inside_their_month() {
        useCase(LocalDate.of(2026, 9, 25)).forEach { m ->
            val dates = m.events.map { it.date }
            assertEquals(dates.sorted(), dates)
            assertTrue(dates.all { it.year == m.year && it.monthValue == m.month })
        }
    }

    @Test
    fun vu_lan_2024_is_a_festival() {
        val august = useCase(LocalDate.of(2024, 8, 1), months = 1).single()
        val vuLan = august.events.firstOrNull { it.date == LocalDate.of(2024, 8, 18) }
        assertNotNull(vuLan)
        assertEquals("Đại lễ Vu Lan Báo Hiếu", vuLan!!.title)
        assertTrue(vuLan.isFestival)
        assertEquals("15/7 âm lịch", vuLan.lunarLabel)
    }

    @Test
    fun plain_ram_and_mung_mot_are_not_festivals() {
        // 17/9/2024 = Rằm tháng Tám: chỉ là ngày trai giới.
        val september = useCase(LocalDate.of(2024, 9, 1), months = 1).single()
        val ram = september.events.first { it.date == LocalDate.of(2024, 9, 17) }
        assertFalse(ram.isFestival)
        assertEquals("Rằm tháng Tám — ngày trai giới", ram.title)
    }

    @Test
    fun dia_tang_30_7_appears_once_per_lunar_year_on_last_day_of_month_7() {
        (2021..2030).forEach { year ->
            val hits = useCase(LocalDate.of(year, 1, 1), months = 12).flatMap { it.events }
                .filter { it.title == "Vía Địa Tạng Bồ Tát" }
            // Tháng 7 âm nằm trong năm dương [year]; mỗi năm đúng 1 lần.
            assertEquals("$year", 1, hits.size)
            val d = hits.single().date
            assertEquals(7, LunarCalendar.toLunar(d).month)
            assertEquals(1, LunarCalendar.toLunar(d.plusDays(1)).day)
        }
    }

    @Test
    fun leap_month_does_not_repeat_festivals_but_keeps_fast_days() {
        // 2023 nhuận tháng Hai: mùng 1 nhuận (22/3) là ngày trai, không phải lễ; 15 nhuận không phải Nhập Niết bàn.
        val events = useCase(LocalDate.of(2023, 3, 1), months = 2).flatMap { it.events }
        val leapFirst = events.first { it.date == LocalDate.of(2023, 3, 22) }
        assertFalse(leapFirst.isFestival)
        val leapFestivals = events.filter {
            LunarCalendar.toLunar(it.date).isLeapMonth && it.isFestival
        }
        assertTrue(leapFestivals.isEmpty())
    }
}

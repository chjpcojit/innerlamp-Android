package com.example.innerlamp.domain.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * BR-CAL-13: the countdown label of an occurrence relative to the device date. /
 * BR-CAL-13: nhãn đếm ngược của một lần diễn ra so với ngày trên máy.
 */
object CalendarCountdown {

    sealed interface Label {
        /** "Hôm nay" */
        data object Today : Label

        /** "Ngày mai" */
        data object Tomorrow : Label

        /** "Còn n ngày" */
        data class DaysLeft(val days: Int) : Label

        /** "Đã qua" */
        data object Passed : Label
    }

    data class Countdown(val days: Int, val label: Label)

    fun of(today: LocalDate, solarDate: LocalDate): Countdown {
        val days = ChronoUnit.DAYS.between(today, solarDate).toInt()
        val label = when {
            days < 0 -> Label.Passed
            days == 0 -> Label.Today
            days == 1 -> Label.Tomorrow
            else -> Label.DaysLeft(days)
        }
        return Countdown(days, label)
    }
}

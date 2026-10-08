package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.calendar.LunarDate
import com.example.innerlamp.domain.model.BuddhistEvent
import com.example.innerlamp.domain.model.EventMonth
import java.time.LocalDate

/**
 * Gom sự kiện Phật giáo (lễ vía + Mùng Một/Rằm) cho [months] tháng dương lịch
 * bắt đầu từ tháng của [from]. Ngày âm tính thật bằng [LunarCalendar].
 */
class GetBuddhistEventMonthsUseCase {

    operator fun invoke(from: LocalDate, months: Int = DEFAULT_MONTHS): List<EventMonth> =
        List(months) { offset ->
            val first = from.withDayOfMonth(1).plusMonths(offset.toLong())
            val events = (1..first.lengthOfMonth()).mapNotNull { day ->
                eventOn(first.withDayOfMonth(day))
            }
            EventMonth(first.year, first.monthValue, events)
        }

    private fun eventOn(date: LocalDate): BuddhistEvent? {
        val lunar = LunarCalendar.toLunar(date)
        val festival = FESTIVALS.firstOrNull { it.matches(lunar, date) }?.title
        val title = festival ?: when (lunar.day) {
            15 -> "Rằm tháng ${lunar.monthName} — ngày trai giới"
            1 -> "Mùng Một tháng ${lunar.monthName} — ngày trai giới"
            else -> return null
        }
        return BuddhistEvent(
            date = date,
            title = title,
            lunarLabel = "${lunar.day}/${lunar.month} âm lịch",
            isFestival = festival != null,
        )
    }

    companion object {
        const val DEFAULT_MONTHS = 6

        /**
         * Lễ vía theo ngày/tháng âm (nguồn: `LEVENTS` trong prototype).
         * Mốc ngày 30 của tháng thiếu rơi vào ngày 29; tháng nhuận không lặp lại lễ.
         */
        private val FESTIVALS: List<Festival> = listOf(
            Festival(1, 1, "Vía Đức Di Lặc — Tết Nguyên Đán"),
            Festival(15, 1, "Rằm tháng Giêng — Tết Nguyên Tiêu"),
            Festival(8, 2, "Đức Phật Thích Ca xuất gia"),
            Festival(15, 2, "Đức Phật Thích Ca nhập Niết bàn"),
            Festival(19, 2, "Vía Quán Thế Âm đản sinh"),
            Festival(21, 2, "Vía Phổ Hiền Bồ Tát"),
            Festival(4, 4, "Vía Văn Thù Bồ Tát"),
            Festival(15, 4, "Đại lễ Phật Đản"),
            Festival(16, 4, "Khai kỳ An cư kiết hạ"),
            Festival(19, 6, "Vía Quán Thế Âm thành đạo"),
            Festival(13, 7, "Vía Đại Thế Chí Bồ Tát"),
            Festival(15, 7, "Đại lễ Vu Lan Báo Hiếu"),
            Festival(30, 7, "Vía Địa Tạng Bồ Tát"),
            Festival(22, 8, "Vía Nhiên Đăng Cổ Phật"),
            Festival(19, 9, "Vía Quán Thế Âm xuất gia"),
            Festival(30, 9, "Vía Phật Dược Sư"),
            Festival(5, 10, "Vía Đạt Ma Tổ Sư"),
            Festival(17, 11, "Vía Phật A Di Đà"),
            Festival(8, 12, "Đức Phật Thích Ca thành đạo"),
        )
    }

    /** Một lễ vía lặp lại hằng năm; [leapEligible] cho phép lặp trong tháng nhuận. */
    private data class Festival(
        val day: Int,
        val month: Int,
        val title: String,
        val leapEligible: Boolean = false,
    ) {
        fun matches(lunar: LunarDate, date: LocalDate): Boolean =
            month == lunar.month &&
                (leapEligible || !lunar.isLeapMonth) &&
                LunarCalendar.matchesLunarDay(day, lunar, date)
    }
}

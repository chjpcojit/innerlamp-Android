package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.LunarDayCell
import com.example.innerlamp.domain.model.LunarMonthPage
import java.time.LocalDate

/**
 * Dựng các trang tháng cho carousel lịch âm (màn `cal`): [months] tháng dương liên
 * tiếp kể từ tháng của [from]. Mỗi trang là lưới 7 cột (Chủ Nhật đầu tuần, khớp
 * `Date.getDay()` của prototype) với nhãn ngày âm tính thật bằng [LunarCalendar]
 * và ngày trai được đánh dấu.
 *
 * Tiêu đề tháng (tên tháng âm + can chi) lấy âm lịch của ngày 15 — đại diện cho
 * tháng — giống hàm `monthMeta` trong prototype.
 */
class GetLunarMonthPagesUseCase {

    operator fun invoke(from: LocalDate, months: Int = DEFAULT_MONTHS): List<LunarMonthPage> {
        val base = from.withDayOfMonth(1)
        return List(months) { offset ->
            val first = base.plusMonths(offset.toLong())
            val cells = buildMonthCells(first)
            val midLunar = LunarCalendar.toLunar(first.withDayOfMonth(15))
            LunarMonthPage(
                title = "Tháng ${first.monthValue} · ${first.year}",
                lunarMonthName = "tháng ${midLunar.calMonthName}",
                canChiYear = midLunar.canChiYear,
                days = cells,
            )
        }
    }

    /** Các ô ngày của [first]: ô trống chừa đầu tháng + đủ ngày trong tháng. */
    private fun buildMonthCells(first: LocalDate): List<LunarDayCell> {
        // Số ô trống trước ngày 1 (Chủ Nhật = 0 → dayOfWeek.value % 7 khớp `getDay()`).
        val leading = first.dayOfWeek.value % 7
        return buildList {
            repeat(leading) { add(blankCell()) }
            for (day in 1..first.lengthOfMonth()) {
                val date = first.withDayOfMonth(day)
                val lunar = LunarCalendar.toLunar(date)
                add(
                    LunarDayCell(
                        date = date,
                        dayNumber = day,
                        lunarLabel = when (lunar.day) {
                            1 -> "Mùng 1"
                            15 -> "Rằm"
                            else -> lunar.day.toString()
                        },
                        isFastDay = LunarCalendar.isFastDay(lunar, date),
                    ),
                )
            }
        }
    }

    private fun blankCell() = LunarDayCell(date = null, dayNumber = 0, lunarLabel = "", isFastDay = false)

    companion object {
        const val DEFAULT_MONTHS = 6
    }
}

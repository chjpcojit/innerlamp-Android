package com.example.innerlamp.ui.screens.calendar

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.LunarMonthPage
import java.time.LocalDate

/**
 * State render cho màn Lịch âm (prototype `cal`).
 *
 * Lưới tháng ([months]) tính bằng use case từ domain; chọn ngày chỉ tô đậm ô đang
 * chọn ([selectedDate]) — thẻ "Hôm nay" luôn theo ngày thực tế ([today]).
 */
data class CalUiState(
    val today: LocalDate,
    val selectedDate: LocalDate,
    val months: List<LunarMonthPage>,
    /** Kicker thẻ "Hôm nay": "HÔM NAY · 16 THÁNG BẢY" (tính từ ngày âm thực tế). */
    val todayKicker: String,
    /** Tiêu đề / đoạn giới thiệu hôm nay (dữ liệu biên tập mẫu — xem [CalSampleData]). */
    val todayTitle: String,
    val todayBody: String,
) {
    companion object {
        /** Dựng state ban đầu cho [today]; ngày đang chọn = hôm nay. */
        fun create(today: LocalDate, months: List<LunarMonthPage>): CalUiState {
            val lunar = LunarCalendar.toLunar(today)
            return CalUiState(
                today = today,
                selectedDate = today,
                months = months,
                todayKicker = "HÔM NAY · ${lunar.day} THÁNG ${lunar.monthName.uppercase()}",
                todayTitle = CalSampleData.TODAY_NOTE_TITLE,
                todayBody = CalSampleData.TODAY_NOTE_BODY,
            )
        }
    }
}

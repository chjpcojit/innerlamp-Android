package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.calendar.CalendarCountdown
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.LocalDate

/**
 * User-facing Vietnamese texts of the calendar feature (product §3.1.1 messages table and the
 * Error → UI table of `Docs/Specs/Calendar/design.md`). /
 * Chuỗi tiếng Việt cho người dùng của tính năng lịch lễ (bảng thông báo §3.1.1 và bảng Error → UI).
 */
object CalendarText {
    const val STALE = "Dữ liệu có thể chưa được cập nhật."
    const val OFFLINE = "Không có kết nối mạng. Vui lòng kiểm tra và thử lại."
    const val FAILED = "Không tải được lịch lễ."
    const val NOT_FOUND = "Không tìm thấy ngày lễ này."
    const val EMPTY_MONTH = "Tháng này không có ngày lễ Phật giáo."
    const val RETRY = "Thử lại"

    // ⚠️ Not confirmed (product): wording of the 429 message is an Android default.
    // Chưa xác nhận (product): câu thông báo 429 là mặc định của Android.
    const val RATE_LIMITED = "Hệ thống đang bận. Vui lòng đợi một lát rồi thử lại."

    /** Message for an error when there is no device copy to show. / Thông báo lỗi khi không có bản trên máy. */
    fun message(error: CalendarLoadError): String = when (error) {
        CalendarLoadError.Offline -> OFFLINE
        is CalendarLoadError.RateLimited -> error.retryAfterSeconds?.takeIf { it > 0 }
            ?.let { "Hệ thống đang bận. Vui lòng đợi khoảng $it giây rồi thử lại." } ?: RATE_LIMITED
        CalendarLoadError.Failed -> FAILED
    }

    /** "Không tải được năm {year}." (next year of the festival calendar). */
    fun nextYearFailed(year: Int) = "Không tải được năm $year."

    /** "15/4" with "(nhuận)" on the lunar date, never on the event name (AC7). / "(nhuận)" gắn vào ngày âm. */
    fun lunarDate(lunar: LunarDay): String =
        "${lunar.day}/${lunar.month}" + if (lunar.isLeapMonth) " (nhuận)" else ""

    /** "Âm lịch 15/4" / "Âm lịch 15/4 (nhuận)". */
    fun lunarLabel(lunar: LunarDay): String = "Âm lịch ${lunarDate(lunar)}"

    /** BR-CAL-13 label text. / Chữ nhãn đếm ngược. */
    fun countdown(label: CalendarCountdown.Label): String = when (label) {
        CalendarCountdown.Label.Today -> "Hôm nay"
        CalendarCountdown.Label.Tomorrow -> "Ngày mai"
        is CalendarCountdown.Label.DaysLeft -> "Còn ${label.days} ngày"
        CalendarCountdown.Label.Passed -> "Đã qua"
    }

    fun weekdayLong(date: LocalDate): String =
        if (date.dayOfWeek.value == 7) "Chủ Nhật" else "Thứ ${date.dayOfWeek.value + 1}"

    fun dayMonth(date: LocalDate): String = "%02d-%02d".format(date.dayOfMonth, date.monthValue)
}

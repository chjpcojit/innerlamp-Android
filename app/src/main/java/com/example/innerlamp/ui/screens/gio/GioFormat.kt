package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.ui.screens.home.weekdayFull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Chuỗi hiển thị của Sổ giỗ (tiếng Việt, khớp `gioVals` của prototype). */
internal object GioFormat {

    private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val DateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** "23 tháng Tám âm lịch" hoặc "14/12 dương lịch". */
    fun dateLabel(entry: MemorialEntry): String = when (entry.calendar) {
        MemorialCalendarType.Lunar ->
            "${entry.day} tháng ${LunarCalendar.MONTH_NAMES[entry.month - 1]} âm lịch"
        MemorialCalendarType.Solar -> "%02d/%02d dương lịch".format(entry.day, entry.month)
    }

    /** "Thứ Hai, 05/10/2026". */
    fun fullDate(date: LocalDate): String = "${date.weekdayFull()}, ${date.format(DateFormat)}"

    /** "Hôm nay" · "Ngày mai" · "còn 12 ngày". */
    fun daysLabel(days: Int): String = when (days) {
        0 -> "Hôm nay"
        1 -> "Ngày mai"
        else -> "còn $days ngày"
    }

    /** "Giỗ đầu" · "Giỗ hết" · "Giỗ lần thứ N"; rỗng nếu không rõ năm mất. */
    fun ordinalLabel(ordinal: Int?): String = when {
        ordinal == null || ordinal < 1 -> ""
        ordinal == 1 -> "Giỗ đầu"
        ordinal == 2 -> "Giỗ hết"
        else -> "Giỗ lần thứ $ordinal"
    }

    /** "Trước 3 ngày · 07:00" hoặc "Chưa bật". */
    fun reminderLabel(entry: MemorialEntry): String {
        if (!entry.remind) return "Chưa bật"
        val lead = if (entry.leadDays > 0) "Trước ${entry.leadDays} ngày" else "Đúng ngày"
        val sync = if (entry.syncReminders) " · có trong Lời nhắc" else ""
        return "$lead · ${entry.remindTime.format(TimeFormat)}$sync"
    }

    /** Nhãn giờ nhắc "07:00". */
    fun timeLabel(entry: MemorialEntry): String = entry.remindTime.format(TimeFormat)

    /** Thẻ nổi bật: "GIỖ HÔM NAY" · "GIỖ NGÀY MAI" · "GIỖ SẮP TỚI · CÒN N NGÀY". */
    fun featuredTag(days: Int): String = when (days) {
        0 -> "GIỖ HÔM NAY"
        1 -> "GIỖ NGÀY MAI"
        else -> "GIỖ SẮP TỚI · CÒN $days NGÀY"
    }

    /** Nhãn ngày trong ô chọn: âm "Mùng 1…9", "10…30"; dương "1…31". */
    fun dayOptionLabel(day: Int, lunar: Boolean): String =
        if (lunar && day < 10) "Mùng $day" else day.toString()

    /** Nhãn tháng trong ô chọn: âm "Tháng Giêng/Hai/…/Mười Một/Chạp", dương "Tháng N". */
    fun monthOptionLabel(month: Int, lunar: Boolean): String = "Tháng " + when {
        !lunar -> month.toString()
        month == 1 -> "Giêng"
        else -> LunarCalendar.MONTH_NAMES[month - 1]
    }

    /** Hai dòng xem trước dưới bộ chọn ngày mất. */
    fun preview(entry: MemorialEntry, today: LocalDate): GioPreview {
        val next = MemorialSchedule.next(entry, today)
        val ordinal = ordinalLabel(next.ordinal).let { if (it.isEmpty()) "" else " · $it" }
        val headline = "Kỳ giỗ tới: ${fullDate(next.date)} · ${daysLabel(next.daysUntil).lowercase()}"
        val detail = when (entry.calendar) {
            MemorialCalendarType.Lunar ->
                "Tức ${dateLabel(entry)}$ordinal. Tháng thiếu không có ngày 30 sẽ nhắc vào ngày 29."
            MemorialCalendarType.Solar -> {
                val lunar = LunarCalendar.toLunar(next.date)
                "Năm nay nhằm ${lunar.day} tháng ${lunar.monthName} âm lịch$ordinal"
            }
        }
        return GioPreview(headline, detail)
    }
}

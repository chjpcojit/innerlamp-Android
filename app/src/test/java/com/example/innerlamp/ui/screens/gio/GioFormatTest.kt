package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GioFormatTest {

    private fun entry(
        calendar: MemorialCalendarType = MemorialCalendarType.Lunar,
        day: Int = 23,
        month: Int = 8,
        year: Int? = 2015,
        leap: Boolean = false,
        remind: Boolean = true,
        lead: Int = 3,
        time: LocalTime = LocalTime.of(7, 0),
    ) = MemorialEntry(
        id = 1L, name = "Trần Thị Mai", relation = MemorialRelation.MaternalGrandmother,
        calendar = calendar, day = day, month = month, deathYear = year, leapMonth = leap,
        note = "", remind = remind, leadDays = lead, remindTime = time,
    )

    @Test
    fun date_label_marks_a_leap_month() {
        assertEquals("23 tháng Tám âm lịch", GioFormat.dateLabel(entry()))
        assertEquals("23 tháng Tám (nhuận) âm lịch", GioFormat.dateLabel(entry(leap = true)))
        assertEquals("14/12 dương lịch", GioFormat.dateLabel(entry(calendar = MemorialCalendarType.Solar, day = 14, month = 12)))
    }

    @Test
    fun reminder_label_has_no_reminders_switch_text() {
        assertEquals("Trước 3 ngày · 07:00", GioFormat.reminderLabel(entry()))
        assertEquals("Đúng ngày · 19:00", GioFormat.reminderLabel(entry(lead = 0, time = LocalTime.of(19, 0))))
        assertEquals("Chưa bật", GioFormat.reminderLabel(entry(remind = false)))
        assertFalse(GioFormat.reminderLabel(entry()).contains("Lời nhắc"))
    }

    @Test
    fun days_and_ordinal_labels() {
        assertEquals("Hôm nay", GioFormat.daysLabel(0))
        assertEquals("Ngày mai", GioFormat.daysLabel(1))
        assertEquals("còn 12 ngày", GioFormat.daysLabel(12))
        assertEquals("", GioFormat.ordinalLabel(null))
        assertEquals("", GioFormat.ordinalLabel(0))
        assertEquals("Giỗ đầu", GioFormat.ordinalLabel(1))
        assertEquals("Giỗ hết", GioFormat.ordinalLabel(2))
        assertEquals("Giỗ lần thứ 10", GioFormat.ordinalLabel(10))
    }

    @Test
    fun featured_tag_uses_the_days_left() {
        assertEquals("GIỖ HÔM NAY", GioFormat.featuredTag(0))
        assertEquals("GIỖ NGÀY MAI", GioFormat.featuredTag(1))
        assertEquals("GIỖ SẮP TỚI · CÒN 9 NGÀY", GioFormat.featuredTag(9))
    }

    @Test
    fun preview_shows_the_next_anniversary() {
        // Lunar 23/8/2026 already passed on 14/10; the next one is 23/8/2027 = 23/09/2027, 344 days away.
        // 23/8 âm 2026 đã qua ngày 14/10; kỳ tới là 23/8 âm 2027 = 23/09/2027, cách 344 ngày.
        val preview = GioFormat.preview(entry(), LocalDate.of(2026, 10, 14))
        assertTrue(preview.headline.startsWith("Kỳ giỗ tới:"))
        assertTrue(preview.headline.contains("còn 344 ngày"))
        assertTrue(preview.detail.contains("23 tháng Tám âm lịch"))
        assertTrue(preview.detail.contains("Giỗ lần thứ 12"))
    }
}

package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class GioFormatTest {

    private fun entry(
        calendar: MemorialCalendarType = MemorialCalendarType.Lunar,
        day: Int = 23,
        month: Int = 8,
        remind: Boolean = true,
        leadDays: Int = 1,
        sync: Boolean = false,
    ) = MemorialEntry(
        id = "t", name = "Test", relation = MemorialRelation.Other, calendar = calendar,
        day = day, month = month, deathYear = 2015, note = "", remind = remind,
        leadDays = leadDays, remindTime = LocalTime.of(7, 0), syncReminders = sync,
    )

    @Test
    fun date_label_for_lunar_and_solar() {
        assertEquals("23 tháng Tám âm lịch", GioFormat.dateLabel(entry()))
        assertEquals("12 tháng Mười Một âm lịch", GioFormat.dateLabel(entry(day = 12, month = 11)))
        assertEquals("04/09 dương lịch", GioFormat.dateLabel(entry(MemorialCalendarType.Solar, 4, 9)))
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
        assertEquals("Giỗ lần thứ 11", GioFormat.ordinalLabel(11))
    }

    @Test
    fun featured_tag_variants() {
        assertEquals("GIỖ HÔM NAY", GioFormat.featuredTag(0))
        assertEquals("GIỖ NGÀY MAI", GioFormat.featuredTag(1))
        assertEquals("GIỖ SẮP TỚI · CÒN 5 NGÀY", GioFormat.featuredTag(5))
    }

    @Test
    fun reminder_label_variants() {
        assertEquals("Chưa bật", GioFormat.reminderLabel(entry(remind = false)))
        assertEquals("Đúng ngày · 07:00", GioFormat.reminderLabel(entry(leadDays = 0)))
        assertEquals("Trước 3 ngày · 07:00 · có trong Lời nhắc", GioFormat.reminderLabel(entry(leadDays = 3, sync = true)))
    }

    @Test
    fun picker_option_labels() {
        assertEquals("Mùng 1", GioFormat.dayOptionLabel(1, lunar = true))
        assertEquals("10", GioFormat.dayOptionLabel(10, lunar = true))
        assertEquals("1", GioFormat.dayOptionLabel(1, lunar = false))
        assertEquals("Tháng Giêng", GioFormat.monthOptionLabel(1, lunar = true))
        assertEquals("Tháng Chạp", GioFormat.monthOptionLabel(12, lunar = true))
        assertEquals("Tháng 1", GioFormat.monthOptionLabel(1, lunar = false))
    }

    @Test
    fun full_date_uses_vietnamese_weekday() {
        // 2026-10-09 là thứ Sáu.
        assertEquals("Thứ Sáu, 09/10/2026", GioFormat.fullDate(LocalDate.of(2026, 10, 9)))
    }

    @Test
    fun initials_from_name() {
        assertEquals("TM", initialsOf("Trần Thị Mai"))
        assertEquals("A", initialsOf("  an "))
        assertEquals("?", initialsOf("   "))
    }
}

package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.CalendarEvent
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.model.Recurrence
import java.time.LocalDate

/** Builders for calendar tests. / Hàm dựng dữ liệu cho test lịch lễ. */
internal fun event(
    code: String,
    sortOrder: Int = 1,
    groups: List<String> = listOf("buddhist"),
    isMajor: Boolean = false,
    isPublicHoliday: Boolean = false,
    recurrence: Recurrence = Recurrence.Lunar,
    longDescription: String = "",
    guidance: String = "",
) = CalendarEvent(
    code = code,
    name = code,
    shortDescription = "",
    longDescription = longDescription,
    guidance = guidance,
    imageUrl = null,
    groupCodes = groups,
    isPublicHoliday = isPublicHoliday,
    isMajor = isMajor,
    sortOrder = sortOrder,
    recurrence = recurrence,
)

internal fun occurrence(code: String, date: String) = CalendarOccurrence(
    eventCode = code,
    solarDate = LocalDate.parse(date),
    lunar = LunarDay(1, 1, 2026, false),
)

internal fun entry(event: CalendarEvent, date: String) = EventOccurrence(event, occurrence(event.code, date))

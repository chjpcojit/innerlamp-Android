package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs of `GET /v1/calendar/events` (`CalendarEventsPage`, contract 2.2.0). Lenient: every field has a
 * default so a missing field never breaks decoding; `recurrence` is a plain string (open-ended). /
 * DTO của `GET /v1/calendar/events`. Khoan dung: mọi field có default; `recurrence` là chuỗi (mở).
 */
@Serializable
data class CalendarEventsPageDto(
    val year: Int,
    val groups: List<CalendarGroupDto> = emptyList(),
    val events: List<CalendarEventDto> = emptyList(),
    val occurrences: List<CalendarOccurrenceDto> = emptyList(),
)

@Serializable
data class CalendarGroupDto(val code: String, val name: String = "")

@Serializable
data class CalendarEventDto(
    val code: String,
    val name: String = "",
    @SerialName("short_description") val shortDescription: String = "",
    @SerialName("long_description") val longDescription: String = "",
    val guidance: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    val groups: List<String> = emptyList(),
    @SerialName("is_public_holiday") val isPublicHoliday: Boolean = false,
    @SerialName("is_major") val isMajor: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = Int.MAX_VALUE,
    val recurrence: String = "",
)

@Serializable
data class CalendarOccurrenceDto(
    @SerialName("event_code") val eventCode: String,
    @SerialName("solar_date") val solarDate: String,
    @SerialName("lunar_date") val lunarDate: CalendarLunarDateDto,
)

@Serializable
data class CalendarLunarDateDto(
    val day: Int,
    val month: Int,
    val year: Int,
    @SerialName("is_leap_month") val isLeapMonth: Boolean = false,
)

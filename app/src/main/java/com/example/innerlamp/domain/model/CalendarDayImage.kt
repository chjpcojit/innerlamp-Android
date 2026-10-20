package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * The Buddha image of one calendar day (`GET /v1/calendar/image`, contract 2.2.0 `calendarImage`).
 * The same [date] always gives the same image (100-day cycle). /
 * Ảnh Phật của một ngày (`GET /v1/calendar/image`). Cùng [date] luôn cho cùng một ảnh (chu kỳ 100 ngày).
 *
 * @param date the date the server actually used. / Ngày server thực sự dùng.
 * @param license licence name, e.g. "CC BY-SA 4.0"; may be blank. / Tên giấy phép; có thể rỗng.
 * @param licenseUrl link to the licence text; may be null. / Liên kết giấy phép; có thể null.
 */
data class CalendarDayImage(
    val date: LocalDate,
    val id: String,
    val imageUrl: String,
    val author: String,
    val license: String,
    val licenseUrl: String?,
    val sourceUrl: String?,
) {
    /** Author + licence must be shown on screen (CC BY / CC BY-SA). / Phải hiện tác giả + giấy phép. */
    val hasAttribution: Boolean get() = author.isNotBlank() || license.isNotBlank()
}

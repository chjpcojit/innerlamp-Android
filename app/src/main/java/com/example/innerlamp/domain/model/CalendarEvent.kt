package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * Calendar domain model (Buddhist / Vietnamese / folk events of one solar year), exactly as the
 * server returns it; the app computes no date. /
 * Mô hình miền của lịch lễ (sự kiện Phật giáo / Việt Nam / dân gian của một năm dương), đúng như
 * server trả về; app không tự tính ngày nào.
 */

/** A display group (e.g. `buddhist`); the name comes from the server, never hard-coded. / Nhóm hiển thị; tên lấy từ server. */
data class CalendarGroup(val code: String, val name: String)

/**
 * Recurrence kind of an event. Open-ended: any value the app does not know decodes to [Unknown]. /
 * Kiểu lặp của sự kiện. Mở: giá trị lạ về [Unknown].
 *
 * Mùng 1 / Rằm are recognised ONLY by [LunarMonthly], never by comparing `code`. /
 * Mùng 1 / Rằm chỉ nhận biết qua [LunarMonthly], không so `code`.
 */
enum class Recurrence { Solar, Lunar, LunarMonthly, LunarLastDay, SolarTerm, Unknown }

/** Where a loaded year came from. / Nguồn của một năm đã tải. */
enum class CalendarYearSource { Network, DeviceCopy }

/** Lunar date of an occurrence (the leap flag belongs to the date, not to the event name). / Ngày âm của một lần diễn ra. */
data class LunarDay(val day: Int, val month: Int, val year: Int, val isLeapMonth: Boolean)

/** Event content, once per event; `groupCodes` only holds codes present in the year's groups. / Nội dung sự kiện. */
data class CalendarEvent(
    val code: String,
    val name: String,
    val shortDescription: String,
    val longDescription: String,
    val guidance: String,
    val imageUrl: String?,
    val groupCodes: List<String>,
    val isPublicHoliday: Boolean,
    val isMajor: Boolean,
    val sortOrder: Int,
    val recurrence: Recurrence,
)

/** One occurrence of an event on a solar day, pointing back at [CalendarEvent.code]. / Một lần diễn ra. */
data class CalendarOccurrence(
    val eventCode: String,
    val solarDate: LocalDate,
    val lunar: LunarDay,
)

/** An occurrence joined with its event content. / Occurrence ghép với nội dung sự kiện. */
data class EventOccurrence(val event: CalendarEvent, val occurrence: CalendarOccurrence)

/** One solar year of the calendar. / Một năm dương của lịch. */
data class CalendarYear(
    val year: Int,
    val groups: List<CalendarGroup>,
    val events: List<CalendarEvent>,
    val occurrences: List<CalendarOccurrence>,
) {
    private val eventsByCode: Map<String, CalendarEvent> by lazy { events.associateBy { it.code } }

    /** Looks an event up by code. / Tìm sự kiện theo code. */
    fun eventOf(code: String): CalendarEvent? = eventsByCode[code]

    /**
     * Occurrences joined with their event, in the server order (BR-CAL-12); an occurrence whose event
     * is missing is skipped. / Occurrence ghép sự kiện, giữ thứ tự server; bỏ occurrence mất sự kiện.
     */
    val entries: List<EventOccurrence> by lazy {
        occurrences.mapNotNull { o -> eventOf(o.eventCode)?.let { EventOccurrence(it, o) } }
    }
}

/**
 * Result of loading a year. [mayBeOutOfDate] is true only when the network failed and the device
 * copy is shown instead (AC24). /
 * Kết quả tải một năm. [mayBeOutOfDate] chỉ true khi mạng lỗi và đang hiện bản trên máy (AC24).
 */
data class CalendarYearLoad(
    val year: CalendarYear,
    val source: CalendarYearSource,
    val mayBeOutOfDate: Boolean,
)

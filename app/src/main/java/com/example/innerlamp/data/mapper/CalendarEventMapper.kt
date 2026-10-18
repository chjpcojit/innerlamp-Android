package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.CalendarEventDto
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import com.example.innerlamp.data.remote.dto.CalendarOccurrenceDto
import com.example.innerlamp.domain.model.CalendarEvent
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.model.Recurrence
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Wire value → [Recurrence]; any unknown value → [Recurrence.Unknown]. / Giá trị lạ → Unknown. */
fun recurrenceFromWire(value: String): Recurrence = when (value) {
    "solar" -> Recurrence.Solar
    "lunar" -> Recurrence.Lunar
    "lunar_monthly" -> Recurrence.LunarMonthly
    "lunar_last_day" -> Recurrence.LunarLastDay
    "solar_term" -> Recurrence.SolarTerm
    else -> Recurrence.Unknown
}

/**
 * Maps a year payload. A group code absent from `groups` is ignored; an occurrence whose
 * `solar_date` does not parse is skipped. No date is computed. /
 * Map payload một năm. Mã nhóm không có trong `groups` bị bỏ; occurrence có `solar_date` hỏng bị bỏ.
 */
fun CalendarEventsPageDto.toDomain(): CalendarYear {
    val knownGroups = groups.map { CalendarGroup(it.code, it.name) }
    val known = knownGroups.mapTo(HashSet()) { it.code }
    return CalendarYear(
        year = year,
        groups = knownGroups,
        events = events.map { it.toDomain(known) },
        occurrences = occurrences.mapNotNull { it.toDomainOrNull() },
    )
}

private fun CalendarEventDto.toDomain(knownGroups: Set<String>) = CalendarEvent(
    code = code,
    name = name,
    shortDescription = shortDescription,
    longDescription = longDescription,
    guidance = guidance,
    imageUrl = imageUrl,
    groupCodes = groups.filter { it in knownGroups },
    isPublicHoliday = isPublicHoliday,
    isMajor = isMajor,
    sortOrder = sortOrder,
    recurrence = recurrenceFromWire(recurrence),
)

private fun CalendarOccurrenceDto.toDomainOrNull(): CalendarOccurrence? {
    val date = try {
        LocalDate.parse(solarDate)
    } catch (_: DateTimeParseException) {
        return null
    }
    return CalendarOccurrence(
        eventCode = eventCode,
        solarDate = date,
        lunar = LunarDay(lunarDate.day, lunarDate.month, lunarDate.year, lunarDate.isLeapMonth),
    )
}

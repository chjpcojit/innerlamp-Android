package com.example.innerlamp.data.mapper

import com.example.innerlamp.core.database.memorial.MemorialCacheEntity
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.data.remote.dto.MemorialDto
import com.example.innerlamp.data.remote.dto.MemorialReminderDto
import com.example.innerlamp.data.remote.dto.MemorialRequestDto
import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialDateRules
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** A payload that cannot be mapped without guessing (unknown enum, bad date). / Payload không map được. */
class MemorialMappingException(message: String, cause: Throwable? = null) : Exception(message, cause)

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** `lunar`/`solar` only; anything else is a controlled mapping error. / Chỉ hai giá trị, lạ là lỗi. */
internal fun String.toMemorialCalendar(): MemorialCalendarType = when (this) {
    "lunar" -> MemorialCalendarType.Lunar
    "solar" -> MemorialCalendarType.Solar
    else -> throw MemorialMappingException("unknown death_calendar: $this")
}

internal fun MemorialCalendarType.toWire(): String = when (this) {
    MemorialCalendarType.Lunar -> "lunar"
    MemorialCalendarType.Solar -> "solar"
}

/** Two-way map by the explicit `apiCode` table — never by the label. / Map hai chiều bằng bảng apiCode. */
internal fun String.toMemorialRelation(): MemorialRelation =
    MemorialRelation.entries.firstOrNull { it.apiCode == this }
        ?: throw MemorialMappingException("unknown relationship: $this")

/**
 * Response → domain. A date with a year is converted back to the calendar the user entered (that is how the
 * leap marker is recovered); without a year the stored day/month/leap are kept as they are. Reminder values
 * outside the four choices are shown as the nearest one but the stored value is not rewritten here. /
 * Response → domain. Ngày có năm được quy đổi lại về lịch người dùng đã nhập (đó là cách khôi phục dấu
 * nhuận); không năm thì giữ nguyên ngày/tháng/nhuận. Giá trị nhắc ngoài lựa chọn được hiện gần nhất.
 */
fun MemorialDto.toDomain(): MemorialEntry {
    val calendar = deathCalendar.toMemorialCalendar()
    val (day, month, year, leap) = readDate(calendar)
    val reminder = reminder
    val entry = MemorialEntry(
        id = id,
        name = fullName,
        relation = relationship.toMemorialRelation(),
        calendar = calendar,
        day = day,
        month = month,
        deathYear = year,
        leapMonth = leap,
        note = note.orEmpty(),
        remind = reminder != null,
        leadDays = reminder?.daysBefore ?: 1,
        remindTime = reminder?.time?.let(::parseTime) ?: LocalTime.of(7, 0),
        photoUrl = photoUrl,
    )
    return MemorialDateRules.withNearestReminder(entry)
}

private fun MemorialDto.readDate(calendar: MemorialCalendarType): DateParts {
    val date = deathDate
    if (date != null) {
        val solar = try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            throw MemorialMappingException("bad death_date: $date", e)
        }
        return if (calendar == MemorialCalendarType.Lunar) {
            val lunar = LunarCalendar.toLunar(solar)
            DateParts(lunar.day, lunar.month, lunar.year, lunar.isLeapMonth)
        } else {
            DateParts(solar.dayOfMonth, solar.monthValue, solar.year, false)
        }
    }
    val day = deathDay ?: throw MemorialMappingException("memorial $id has no date")
    val month = deathMonth ?: throw MemorialMappingException("memorial $id has no month")
    return DateParts(day, month, null, deathLeapMonth ?: false)
}

private data class DateParts(
    val day: Int,
    val month: Int,
    val year: Int?,
    val leap: Boolean,
)

private fun parseTime(value: String): LocalTime =
    runCatching { LocalTime.parse(value) }.getOrElse { LocalTime.of(7, 0) }

/**
 * Domain → request body. With a year the date is always sent as a solar `YYYY-MM-DD` (the lunar one is
 * converted exactly); without a year day/month (+ leap for lunar) are sent and `death_date` is omitted. /
 * Domain → body. Có năm: luôn gửi ngày dương `YYYY-MM-DD`; không năm: gửi ngày/tháng (+ nhuận nếu âm).
 */
fun MemorialEntry.toRequestDto(): MemorialRequestDto {
    val body = MemorialRequestDto(
        fullName = name.trim(),
        relationship = relation.apiCode,
        deathCalendar = calendar.toWire(),
        note = note.trim().ifEmpty { null },
        reminder = if (remind) {
            MemorialReminderDto(daysBefore = leadDays, time = remindTime.format(TimeFormat))
        } else {
            null
        },
    )
    val year = deathYear ?: return body.copy(
        deathDay = day,
        deathMonth = month,
        deathLeapMonth = if (calendar == MemorialCalendarType.Lunar) leapMonth else null,
    )
    val solar = when (calendar) {
        MemorialCalendarType.Solar -> try {
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            throw MemorialMappingException("bad solar date $day/$month/$year", e)
        }
        MemorialCalendarType.Lunar -> MemorialSchedule.lunarToSolarExact(day, month, year, leapMonth)
            ?: throw MemorialMappingException("bad lunar date $day/$month/$year leap=$leapMonth")
    }
    // `death_leap_month` is null when `death_date` is present (contract §2.1).
    // `death_leap_month` phải null khi có `death_date`.
    return body.copy(deathDate = solar.toString())
}

/** Device copy of an entry; the signed `photo_url` is never stored (D8). / Bản trên máy, không lưu URL ảnh. */
fun MemorialEntry.toCacheEntity(ownerId: Long): MemorialCacheEntity = MemorialCacheEntity(
    ownerId = ownerId,
    id = id,
    name = name,
    relationCode = relation.apiCode,
    calendarCode = calendar.toWire(),
    day = day,
    month = month,
    deathYear = deathYear,
    leapMonth = leapMonth,
    note = note,
    remind = remind,
    leadDays = leadDays,
    remindTime = remindTime.format(TimeFormat),
)

fun MemorialCacheEntity.toDomain(): MemorialEntry = MemorialEntry(
    id = id,
    name = name,
    relation = relationCode.toMemorialRelation(),
    calendar = calendarCode.toMemorialCalendar(),
    day = day,
    month = month,
    deathYear = deathYear,
    leapMonth = leapMonth,
    note = note,
    remind = remind,
    leadDays = leadDays,
    remindTime = parseTime(remindTime),
    photoUrl = null,
)

/**
 * `AppError` → feature error, by `error.code`/HTTP status, never by message (contract §0.6). /
 * `AppError` → lỗi tính năng, theo `error.code`/HTTP status, không theo message.
 */
internal fun AppError.toMemorialError(): MemorialError = when (this) {
    AppError.Unauthorized -> MemorialError.Unauthorized
    AppError.NoConnection, AppError.Timeout -> MemorialError.Offline
    AppError.NotConfigured, AppError.Serialization, is AppError.Unknown -> MemorialError.Failed
    is AppError.HttpError -> when (code) {
        401 -> MemorialError.Unauthorized
        404 -> MemorialError.NotFound
        409 -> MemorialError.LimitReached
        413 -> MemorialError.TooLarge
        415 -> MemorialError.UnsupportedPhoto
        else -> MemorialError.Failed
    }
    is AppError.ApiError -> when (code) {
        "MEMORIAL_NOT_FOUND" -> MemorialError.NotFound
        "MEMORIAL_LIMIT_REACHED" -> MemorialError.LimitReached
        "PHOTO_TOO_LARGE", "PAYLOAD_TOO_LARGE" -> MemorialError.TooLarge
        "PHOTO_UNSUPPORTED_TYPE" -> MemorialError.UnsupportedPhoto
        "UNAUTHORIZED" -> MemorialError.Unauthorized
        "VALIDATION_ERROR" -> MemorialError.Validation(details?.get("field")?.jsonPrimitive?.contentOrNull)
        else -> MemorialError.Failed
    }
}

package com.example.innerlamp.data.mapper

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.JsonProvider
import com.example.innerlamp.data.remote.dto.MemorialDto
import com.example.innerlamp.data.remote.dto.MemorialReminderDto
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MemorialMapperTest {

    private fun dto(
        id: Long = 7,
        calendar: String = "lunar",
        deathDate: String? = "2026-11-23",
        day: Int? = null,
        month: Int? = null,
        leap: Boolean? = null,
        reminder: MemorialReminderDto? = MemorialReminderDto(3, "07:00"),
    ) = MemorialDto(
        id = id,
        fullName = "Trần Thị Mai",
        relationship = "GRANDMOTHER_MATERNAL",
        deathCalendar = calendar,
        deathDate = deathDate,
        deathDay = day,
        deathMonth = month,
        deathLeapMonth = leap,
        note = "Cúng chay",
        photoUrl = "https://example.test/p.jpg",
        reminder = reminder,
    )

    @Test
    fun a_lunar_date_with_a_year_recovers_the_leap_marker() {
        val entry = dto(deathDate = "2025-08-03").toDomain()
        assertEquals(MemorialCalendarType.Lunar, entry.calendar)
        assertEquals(10, entry.day)
        assertEquals(6, entry.month)
        assertEquals(2025, entry.deathYear)
        assertTrue(entry.leapMonth)
        assertEquals(MemorialRelation.MaternalGrandmother, entry.relation)
        assertEquals(3, entry.leadDays)
        assertEquals(LocalTime.of(7, 0), entry.remindTime)
    }

    @Test
    fun a_solar_date_with_a_year_keeps_its_day_and_month() {
        val entry = dto(calendar = "solar", deathDate = "2020-02-29").toDomain()
        assertEquals(MemorialCalendarType.Solar, entry.calendar)
        assertEquals(29, entry.day)
        assertEquals(2, entry.month)
        assertEquals(2020, entry.deathYear)
        assertFalse(entry.leapMonth)
    }

    @Test
    fun a_date_without_a_year_keeps_day_month_and_the_leap_flag() {
        val entry = dto(deathDate = null, day = 10, month = 6, leap = true).toDomain()
        assertNull(entry.deathYear)
        assertEquals(10, entry.day)
        assertEquals(6, entry.month)
        assertTrue(entry.leapMonth)
    }

    @Test
    fun a_null_reminder_turns_reminders_off() {
        val entry = dto(reminder = null).toDomain()
        assertFalse(entry.remind)
    }

    @Test
    fun an_unknown_relationship_is_a_mapping_error_not_other() {
        val bad = dto().copy(relationship = "GREAT_UNCLE")
        assertThrows(MemorialMappingException::class.java) { bad.toDomain() }
    }

    @Test
    fun stored_lead_and_time_outside_the_choices_are_shown_as_the_nearest() {
        val entry = dto(reminder = MemorialReminderDto(5, "09:30")).toDomain()
        assertEquals(7, entry.leadDays)
        assertEquals(LocalTime.of(7, 0), entry.remindTime)
    }

    private fun entry(
        calendar: MemorialCalendarType = MemorialCalendarType.Lunar,
        year: Int? = 2026,
        leap: Boolean = false,
    ) = MemorialEntry(
        id = 1L, name = "  Trần Thị Mai  ", relation = MemorialRelation.Father,
        calendar = calendar, day = 15, month = 10, deathYear = year, leapMonth = leap,
        note = "   ", remind = true, leadDays = 3, remindTime = LocalTime.of(12, 0),
    )

    @Test
    fun a_lunar_date_with_a_year_is_sent_as_the_exact_solar_date() {
        val body = entry().toRequestDto()
        assertEquals("2026-11-23", body.deathDate)
        assertNull(body.deathDay)
        assertNull(body.deathLeapMonth)
        assertEquals("Trần Thị Mai", body.fullName)
        assertNull(body.note)
        assertEquals(3, body.reminder?.daysBefore)
        assertEquals("12:00", body.reminder?.time)
    }

    @Test
    fun a_date_without_a_year_sends_day_month_and_the_leap_flag() {
        val body = entry(year = null, leap = true).toRequestDto()
        assertNull(body.deathDate)
        assertEquals(15, body.deathDay)
        assertEquals(10, body.deathMonth)
        assertEquals(true, body.deathLeapMonth)
        assertEquals("lunar", body.deathCalendar)
    }

    @Test
    fun a_solar_date_without_a_year_never_sends_the_leap_flag() {
        val body = entry(calendar = MemorialCalendarType.Solar, year = null, leap = true).toRequestDto()
        assertNull(body.deathLeapMonth)
        assertEquals("solar", body.deathCalendar)
    }

    @Test
    fun reminders_off_sends_a_null_reminder_that_the_json_drops() {
        val body = entry().copy(remind = false).toRequestDto()
        assertNull(body.reminder)
        val json = JsonProvider.json.encodeToString(body)
        assertFalse(json.contains("\"reminder\""))
        assertFalse(json.contains("\"death_day\""))
        assertFalse(json.contains("timezone"))
        assertFalse(json.contains("add_to_os_reminder"))
    }

    @Test
    fun a_named_validation_error_is_carried_by_the_field() {
        val error = AppError.ApiError(
            code = "VALIDATION_ERROR",
            message = "bad",
            type = "validation",
            details = mapOf("field" to JsonPrimitive("note")),
        )
        assertEquals(MemorialError.Validation("note"), error.toMemorialError())
    }

    @Test
    fun http_statuses_map_to_the_feature_errors() {
        assertEquals(MemorialError.NotFound, AppError.HttpError(404).toMemorialError())
        assertEquals(MemorialError.TooLarge, AppError.HttpError(413).toMemorialError())
        assertEquals(MemorialError.UnsupportedPhoto, AppError.HttpError(415).toMemorialError())
        assertEquals(MemorialError.LimitReached, AppError.HttpError(409).toMemorialError())
        assertEquals(MemorialError.Unauthorized, AppError.HttpError(401).toMemorialError())
        assertEquals(MemorialError.Failed, AppError.HttpError(500).toMemorialError())
        assertEquals(MemorialError.Offline, AppError.NoConnection.toMemorialError())
        assertEquals(MemorialError.TooLarge, AppError.ApiError("PHOTO_TOO_LARGE", "", "").toMemorialError())
        assertEquals(
            MemorialError.UnsupportedPhoto,
            AppError.ApiError("PHOTO_UNSUPPORTED_TYPE", "", "").toMemorialError(),
        )
    }

    @Test
    fun a_lunar_date_that_does_not_exist_cannot_be_sent() {
        val body = entry().copy(day = 30, month = 7)
        assertThrows(MemorialMappingException::class.java) { body.toRequestDto() }
    }
}

package com.example.innerlamp.data.mapper

import com.example.innerlamp.core.network.JsonProvider
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import com.example.innerlamp.domain.model.Recurrence
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarEventMapperTest {

    private val json = """
        {"year":2026,
         "groups":[{"code":"buddhist","name":"Phật giáo"},{"code":"vietnam","name":"Việt Nam"}],
         "events":[
          {"code":"phat-dan","name":"Phật đản","short_description":"s","long_description":"l","guidance":"g",
           "image_url":null,"groups":["buddhist","ghost"],"is_public_holiday":false,"is_major":true,
           "sort_order":17,"recurrence":"lunar","future_field":1},
          {"code":"tet","name":"Tết","short_description":"","long_description":"","guidance":"",
           "image_url":"https://x/y.png","groups":["vietnam"],"is_public_holiday":true,"is_major":false,
           "sort_order":4,"recurrence":"lunar_monthly"},
          {"code":"new","name":"Mới","short_description":"","long_description":"","guidance":"","image_url":null,
           "groups":["buddhist"],"is_public_holiday":false,"is_major":false,"sort_order":9,"recurrence":"brand_new_kind"}
         ],
         "occurrences":[
          {"event_code":"phat-dan","solar_date":"2026-05-31","lunar_date":{"day":15,"month":4,"year":2026,"is_leap_month":true}},
          {"event_code":"tet","solar_date":"not-a-date","lunar_date":{"day":1,"month":1,"year":2026,"is_leap_month":false}}
         ]}
    """.trimIndent()

    private val year = JsonProvider.json.decodeFromString(CalendarEventsPageDto.serializer(), json).toDomain()

    @Test
    fun mapsContract22Fields() {
        val e = year.eventOf("phat-dan")!!
        assertTrue(e.isMajor)
        assertEquals(17, e.sortOrder)
        assertEquals(Recurrence.Lunar, e.recurrence)
        assertNull(e.imageUrl)
        assertEquals(listOf("buddhist", "vietnam"), year.groups.map { it.code })
        assertTrue(year.eventOf("tet")!!.isPublicHoliday)
        assertEquals(Recurrence.LunarMonthly, year.eventOf("tet")!!.recurrence)
        assertEquals("https://x/y.png", year.eventOf("tet")!!.imageUrl)
    }

    @Test
    fun leapMonthFlagStaysOnTheLunarDate_notInTheName() {
        val o = year.occurrences.single()
        assertEquals(LocalDate.parse("2026-05-31"), o.solarDate)
        assertTrue(o.lunar.isLeapMonth)
        assertEquals("Phật đản", year.eventOf("phat-dan")!!.name)
    }

    @Test
    fun unknownRecurrence_isKeptAsUnknown() {
        assertEquals(Recurrence.Unknown, year.eventOf("new")!!.recurrence)
        assertEquals(Recurrence.Unknown, recurrenceFromWire(""))
        assertEquals(Recurrence.SolarTerm, recurrenceFromWire("solar_term"))
        assertEquals(Recurrence.LunarLastDay, recurrenceFromWire("lunar_last_day"))
        assertEquals(Recurrence.Solar, recurrenceFromWire("solar"))
    }

    @Test
    fun unknownGroupCode_isIgnored() {
        assertEquals(listOf("buddhist"), year.eventOf("phat-dan")!!.groupCodes)
    }

    @Test
    fun occurrenceWithUnparsableDate_isSkipped_andEntriesJoinEvents() {
        assertEquals(1, year.occurrences.size)
        assertEquals("phat-dan", year.entries.single().event.code)
        assertFalse(year.entries.any { it.event.code == "tet" })
    }
}

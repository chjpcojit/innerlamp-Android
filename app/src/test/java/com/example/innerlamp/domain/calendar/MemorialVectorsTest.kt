package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.ui.screens.gio.GioFormat
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Runs the three shared product vector files unchanged (NFR-MEM-03, D6). Cases are never copied by hand. /
 * Chạy nguyên ba file vector dùng chung của product. Không chép case bằng tay.
 */
class MemorialVectorsTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun cases(file: String): List<JsonObject> {
        val text = checkNotNull(javaClass.classLoader?.getResourceAsStream("product/memorial/rules/$file.json")) {
            "missing vector file $file.json"
        }.bufferedReader().use { it.readText() }
        val root = json.parseToJsonElement(text).jsonObject
        return (root["cases"] as kotlinx.serialization.json.JsonArray).map { it.jsonObject }
    }

    private fun name(case: JsonObject): String = case["name"]!!.jsonPrimitive.content

    @Test
    fun death_date_conversion_matches_product_vectors() {
        cases("death-date-conversion").forEach { case ->
            val input = case["input"]!!.jsonObject
            val expected = case["expected"]!!.jsonObject
            if (input.containsKey("solar")) {
                val solar = LocalDate.parse(input["solar"]!!.jsonPrimitive.content)
                val lunar = LunarCalendar.toLunar(solar)
                val want = expected["lunar"]!!.jsonObject
                assertEquals(name(case), want["day"]!!.jsonPrimitive.int, lunar.day)
                assertEquals(name(case), want["month"]!!.jsonPrimitive.int, lunar.month)
                assertEquals(name(case), want["year"]!!.jsonPrimitive.int, lunar.year)
                assertEquals(name(case), want["leap_month"]!!.jsonPrimitive.boolean, lunar.isLeapMonth)
            } else {
                val lunar = input["lunar"]!!.jsonObject
                val actual = MemorialSchedule.lunarToSolarExact(
                    day = lunar["day"]!!.jsonPrimitive.int,
                    month = lunar["month"]!!.jsonPrimitive.int,
                    year = lunar["year"]!!.jsonPrimitive.int,
                    leap = lunar["leap_month"]!!.jsonPrimitive.boolean,
                )
                val solar = expected["solar"]
                if (solar != null && solar !is JsonNull) {
                    assertEquals(name(case), LocalDate.parse(solar.jsonPrimitive.content), actual)
                } else {
                    assertFalse(name(case), expected["valid"]!!.jsonPrimitive.boolean)
                    assertNull(name(case), actual)
                }
            }
        }
    }

    @Test
    fun next_anniversary_matches_product_vectors() {
        cases("next-anniversary").forEach { case ->
            val input = case["input"]!!.jsonObject
            val expected = case["expected"]!!.jsonObject
            val entry = MemorialEntry(
                id = 1L,
                name = "vector",
                relation = MemorialRelation.Father,
                calendar = if (input["calendar"]!!.jsonPrimitive.content == "lunar") {
                    MemorialCalendarType.Lunar
                } else {
                    MemorialCalendarType.Solar
                },
                day = input["day"]!!.jsonPrimitive.int,
                month = input["month"]!!.jsonPrimitive.int,
                deathYear = input["death_year"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.int,
                leapMonth = input["leap_month"]!!.jsonPrimitive.boolean,
            )
            val actual = MemorialSchedule.next(entry, LocalDate.parse(input["today"]!!.jsonPrimitive.content))
            assertEquals(name(case), LocalDate.parse(expected["next_date"]!!.jsonPrimitive.content), actual.date)
            assertEquals(name(case), expected["days_left"]!!.jsonPrimitive.int, actual.daysUntil)
            val ordinal = expected["ordinal"]
            if (ordinal == null || ordinal is JsonNull) {
                assertNull(name(case), actual.ordinal)
            } else {
                assertEquals(name(case), ordinal.jsonPrimitive.int, actual.ordinal)
            }
        }
    }

    @Test
    fun ordinal_labels_match_product_vectors() {
        cases("anniversary-ordinal").forEach { case ->
            val input = case["input"]!!.jsonObject
            val expected = case["expected"]!!.jsonObject
            val value = input["ordinal"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.int
            assertEquals(name(case), expected["label"]!!.jsonPrimitive.content, GioFormat.ordinalLabel(value))
        }
    }

    @Test
    fun upcoming_returns_each_anniversary_after_today() {
        val entry = MemorialEntry(
            id = 1L, name = "x", relation = MemorialRelation.Mother,
            calendar = MemorialCalendarType.Lunar, day = 15, month = 10, deathYear = 2025,
        )
        val occurrences = MemorialSchedule.upcoming(entry, LocalDate.of(2026, 10, 14), 3)
        assertEquals(3, occurrences.size)
        assertTrue(occurrences.zipWithNext().all { (a, b) -> a.date.isBefore(b.date) })
        assertEquals(LocalDate.of(2026, 11, 23), occurrences.first().date)
        assertEquals(1, occurrences.first().ordinal)
        assertEquals(2, occurrences[1].ordinal)
    }
}

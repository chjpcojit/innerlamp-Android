package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.Recurrence
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Runs EVERY case of the shared product vectors (AC30) copied by `scripts/sync-product-vectors.sh`
 * (never edited by hand). Fails when a file's `product_spec_version` differs from `SOURCE`. /
 * Chạy MỌI case của vector dùng chung; fail khi `product_spec_version` lệch `SOURCE`.
 */
class CalendarProductVectorsTest {

    private fun text(name: String): String =
        javaClass.getResourceAsStream("/product/calendar/rules/$name")!!.bufferedReader().use { it.readText() }

    private fun load(name: String): JsonObject {
        val root = Json.parseToJsonElement(text("$name.json")).jsonObject
        val expected = text("SOURCE").lines()
            .first { it.startsWith("$name.json ") }.substringAfter("product_spec_version=")
        assertEquals("$name.json product_spec_version differs from SOURCE", expected,
            root.getValue("product_spec_version").jsonPrimitive.content)
        return root
    }

    private fun JsonElement.str(key: String) = jsonObject.getValue(key).jsonPrimitive.content

    @Test
    fun countdown_allCases() {
        val cases = load("countdown").getValue("cases").jsonArray
        assertTrue(cases.isNotEmpty())
        for (c in cases) {
            val input = c.jsonObject.getValue("input")
            val expected = c.jsonObject.getValue("expected")
            val r = CalendarCountdown.of(LocalDate.parse(input.str("today")), LocalDate.parse(input.str("solar_date")))
            val name = c.str("name")
            assertEquals(name, expected.jsonObject.getValue("days").jsonPrimitive.int, r.days)
            val label = when (r.label) {
                CalendarCountdown.Label.Today -> "today"
                CalendarCountdown.Label.Tomorrow -> "tomorrow"
                is CalendarCountdown.Label.DaysLeft -> "days_left"
                CalendarCountdown.Label.Passed -> "passed"
            }
            assertEquals(name, expected.str("label"), label)
            if (r.label is CalendarCountdown.Label.DaysLeft) {
                assertEquals(name, r.days, (r.label as CalendarCountdown.Label.DaysLeft).days)
            }
        }
    }

    @Test
    fun ordering_allCases() {
        val cases = load("ordering").getValue("cases").jsonArray
        assertTrue(cases.isNotEmpty())
        for (c in cases) {
            val entries = c.jsonObject.getValue("input").jsonObject.getValue("occurrences").jsonArray.map {
                entry(event(it.str("event"), sortOrder = it.jsonObject.getValue("display_order").jsonPrimitive.int), it.str("solar_date"))
            }
            val expected = c.jsonObject.getValue("expected").jsonObject.getValue("order").jsonArray.map { it.jsonPrimitive.content }
            // Sort the reversed input to prove the comparator, not the input order, decides.
            val actual = CalendarOrdering.sort(entries.reversed()).map { it.event.code }
            assertEquals(c.str("name"), expected, actual)
        }
    }

    @Test
    fun homeBlock_allCases() {
        val cases = load("home-block").getValue("cases").jsonArray
        assertTrue(cases.isNotEmpty())
        for (c in cases) {
            val entries = c.jsonObject.getValue("input").jsonObject.getValue("occurrences").jsonArray.map {
                val o = it.jsonObject
                val ev = event(
                    code = it.str("event"),
                    sortOrder = o.getValue("display_order").jsonPrimitive.int,
                    groups = (o.getValue("groups") as JsonArray).map { g -> g.jsonPrimitive.content },
                    isMajor = o.getValue("major").jsonPrimitive.boolean,
                    recurrence = if (o.getValue("every_lunar_month").jsonPrimitive.boolean) Recurrence.LunarMonthly else Recurrence.Lunar,
                )
                entry(ev, it.str("solar_date"))
            }
            val expected = c.jsonObject.getValue("expected").jsonObject
            val selection = HomeEventBlock.select(entries)
            val shown = selection.shown.map { listOf(it.event.code, it.occurrence.solarDate.toString()) }
            val expectedShown = expected.getValue("shown").jsonArray.map { p -> p.jsonArray.map { it.jsonPrimitive.content } }
            val name = c.str("name")
            assertEquals(name, expectedShown, shown)
            assertEquals(name, expected.getValue("more").jsonPrimitive.int, selection.more)
        }
    }

    @Test
    fun homeBlock_unknownRecurrenceIsANamedEvent_andCodeNeverIdentifiesRam() {
        val unknown = entry(event("x-new", sortOrder = 5, recurrence = Recurrence.Unknown), "2026-05-02")
        // Code looks like Rằm but recurrence is not lunar_monthly: stays a named event (tier 2).
        val fakeRam = entry(event("ram-am-lich", sortOrder = 1, recurrence = Recurrence.Lunar), "2026-05-03")
        val ram = entry(event("ram", sortOrder = 99, recurrence = Recurrence.LunarMonthly), "2026-05-01")
        val shown = HomeEventBlock.select(listOf(ram, unknown, fakeRam)).shown.map { it.event.code }
        assertEquals(listOf("x-new", "ram-am-lich", "ram"), shown)
    }
}

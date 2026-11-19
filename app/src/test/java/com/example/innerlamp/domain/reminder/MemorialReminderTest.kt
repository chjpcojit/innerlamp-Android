package com.example.innerlamp.domain.reminder

import com.example.innerlamp.core.reminder.MemorialNotificationText
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.domain.model.MemorialReminderChoices
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemorialReminderTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val entry = MemorialEntry(
        id = 5L, name = "Trần Thị Mai", relation = MemorialRelation.Father,
        calendar = MemorialCalendarType.Lunar, day = 15, month = 10, deathYear = null,
        remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0),
    )

    @Test
    fun nearest_lead_breaks_a_tie_towards_the_earlier_choice() {
        assertEquals(3, MemorialReminderChoices.nearestLeadDays(2))
        assertEquals(7, MemorialReminderChoices.nearestLeadDays(5))
        assertEquals(0, MemorialReminderChoices.nearestLeadDays(0))
        assertEquals(7, MemorialReminderChoices.nearestLeadDays(30))
    }

    @Test
    fun nearest_time_breaks_a_tie_towards_the_earlier_choice() {
        assertEquals(LocalTime.of(7, 0), MemorialReminderChoices.nearestTime(LocalTime.of(9, 30)))
        assertEquals(LocalTime.of(12, 0), MemorialReminderChoices.nearestTime(LocalTime.of(15, 30)))
        assertEquals(LocalTime.of(6, 0), MemorialReminderChoices.nearestTime(LocalTime.of(3, 0)))
        assertEquals(LocalTime.of(19, 0), MemorialReminderChoices.nearestTime(LocalTime.of(22, 0)))
    }

    @Test
    fun all_sixteen_lead_and_time_pairs_land_on_the_expected_moment() {
        val now = Instant.parse("2026-10-14T02:00:00Z") // 09:00 in Vietnam
        val anniversary = LocalDate.of(2026, 11, 23)
        MemorialReminderChoices.leadDays.forEach { lead ->
            MemorialReminderChoices.times.forEach { time ->
                val plan = MemorialReminderPlanner.plan(
                    entry.copy(leadDays = lead, remindTime = time),
                    now,
                    zone,
                )
                val expected = anniversary.minusDays(lead.toLong()).atTime(time).atZone(zone).toInstant()
                assertEquals("lead=$lead time=$time", expected, plan.first().moment)
                assertEquals(3, plan.size)
                assertEquals("5:$anniversary", plan.first().key)
                assertEquals(lead, plan.first().daysBefore)
            }
        }
    }

    @Test
    fun a_passed_moment_is_caught_up_as_soon_as_the_sync_sees_it() {
        val now = Instant.parse("2026-11-22T13:00:00Z") // 22/11 20:00; the 07:00 lead-1 moment already passed
        val plan = MemorialReminderPlanner.plan(entry, now, zone)
        assertEquals(now, plan.first().moment)
        assertEquals("5:2026-11-23", plan.first().key)
        assertEquals(1, plan.first().daysBefore)
    }

    @Test
    fun a_lead_one_moment_from_the_day_before_is_caught_up_on_the_anniversary_day() {
        val now = Instant.parse("2026-11-23T03:00:00Z") // 10:00 on the anniversary; the moment was 22/11 07:00
        val plan = MemorialReminderPlanner.plan(entry, now, zone)
        assertEquals(now, plan.first().moment)
        assertEquals("5:2026-11-23", plan.first().key)
    }

    @Test
    fun after_the_anniversary_day_the_occurrence_is_dropped_and_the_next_one_is_planned() {
        val now = Instant.parse("2026-11-24T01:00:00Z") // 08:00 on 24/11, the anniversary is over
        val plan = MemorialReminderPlanner.plan(entry, now, zone)
        val next = MemorialSchedule.upcoming(entry, LocalDate.of(2026, 11, 24), 3).first()
        assertEquals(MemorialReminderPlanner.keyOf(5L, next.date), plan.first().key)
        assertEquals(next.date.minusDays(1).atTime(7, 0).atZone(zone).toInstant(), plan.first().moment)
        assertTrue(plan.none { it.key == "5:2026-11-23" })
        assertTrue(plan.first().moment.isAfter(now))
    }

    @Test
    fun the_anniversary_day_is_caught_up_once() {
        val now = Instant.parse("2026-11-23T01:00:00Z") // 08:00 on the anniversary, 07:00 passed
        val plan = MemorialReminderPlanner.plan(entry.copy(leadDays = 0), now, zone)
        assertEquals(now, plan.first().moment)
        assertEquals("5:2026-11-23", plan.first().key)
        assertEquals(1, plan.count { it.key == "5:2026-11-23" })

        val delivered = MemorialReminderPlanner.plan(entry.copy(leadDays = 0), now, zone, setOf("5:2026-11-23"))
        assertTrue(delivered.none { it.key == "5:2026-11-23" })
        assertTrue(delivered.first().moment.isAfter(now))
    }

    @Test
    fun reminders_off_or_an_unsaved_entry_has_no_plan() {
        assertTrue(MemorialReminderPlanner.plan(entry.copy(remind = false), Instant.now(), zone).isEmpty())
        assertTrue(MemorialReminderPlanner.plan(entry.copy(id = 0), Instant.now(), zone).isEmpty())
    }

    @Test
    fun notification_copy_uses_the_lead_and_never_the_name() {
        assertEquals("Hôm nay là giỗ cha", MemorialNotificationText.title(0, MemorialRelation.Father))
        assertEquals("Ngày mai là giỗ cha", MemorialNotificationText.title(1, MemorialRelation.Father))
        assertEquals("Còn 3 ngày nữa là giỗ cha", MemorialNotificationText.title(3, MemorialRelation.Father))
        assertEquals("Còn 7 ngày nữa là giỗ cha", MemorialNotificationText.title(7, MemorialRelation.Father))
        assertEquals("Hôm nay là giỗ người thân", MemorialNotificationText.title(0, MemorialRelation.Other))
        assertTrue(MemorialNotificationText.title(1, MemorialRelation.Mother).let { !it.contains("Trần") })
    }

    @Test
    fun a_caught_up_reminder_titles_the_anniversary_by_the_real_days_left() {
        fun titleAt(lead: Int, now: String): String {
            val instant = Instant.parse(now)
            val caughtUp = MemorialReminderPlanner.plan(entry.copy(leadDays = lead), instant, zone).first()
            val days = MemorialNotificationText.daysUntil(caughtUp.key, instant.atZone(zone).toLocalDate())!!
            return MemorialNotificationText.title(days, MemorialRelation.Father)
        }

        // Lead 1, the moment fell yesterday: today is the anniversary. / Mốc rơi hôm trước, hôm nay là giỗ.
        assertEquals("Hôm nay là giỗ cha", titleAt(lead = 1, now = "2026-11-23T03:00:00Z"))
        // Exactly one day left. / Còn đúng 1 ngày.
        assertEquals("Ngày mai là giỗ cha", titleAt(lead = 7, now = "2026-11-22T03:00:00Z"))
        // Lead 3 with two days left. / Lead 3 còn 2 ngày.
        assertEquals("Còn 2 ngày nữa là giỗ cha", titleAt(lead = 3, now = "2026-11-21T03:00:00Z"))
    }
}

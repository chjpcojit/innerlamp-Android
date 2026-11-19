package com.example.innerlamp.domain.reminder

import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * One reminder the device must fire. [key] is `"<entryId>:<solar date>"` — stable per anniversary, so a
 * reschedule never duplicates it and a fired reminder is remembered by that key. /
 * Một nhắc máy phải bắn. [key] là `"<id>:<ngày dương>"` — ổn định theo kỳ giỗ nên đặt lại không sinh
 * trùng và nhắc đã bắn được nhớ bằng key này.
 */
data class MemorialReminder(
    val key: String,
    val entryId: Long,
    val moment: Instant,
    val daysBefore: Int,
)

/**
 * Turns entries into reminder moments (decision D1). Pure: the clock and the zone are given, so the rule
 * is unit-testable. No entry name is ever carried. /
 * Đổi danh sách mục thành thời điểm nhắc (quyết định D1). Thuần: đồng hồ và múi giờ được truyền vào nên
 * test được. Không mang tên người thân.
 */
object MemorialReminderPlanner {

    /** How many anniversaries ahead are scheduled per entry. / Số kỳ giỗ đặt trước cho mỗi mục. */
    const val OCCURRENCES = 3

    fun plan(
        entry: MemorialEntry,
        now: Instant,
        zone: ZoneId,
        delivered: Set<String> = emptySet(),
    ): List<MemorialReminder> {
        if (!entry.remind || entry.isNew) return emptyList()
        val today = now.atZone(zone).toLocalDate()
        return MemorialSchedule.upcoming(entry, today, OCCURRENCES).mapNotNull { occurrence ->
            val key = keyOf(entry.id, occurrence.date)
            val moment = occurrence.date
                .minusDays(entry.leadDays.toLong())
                .atTime(entry.remindTime)
                .atZone(zone)
                .toInstant()
            when {
                moment.isAfter(now) -> MemorialReminder(key, entry.id, moment, entry.leadDays)
                // A moment that already passed still fires once while its anniversary is not over —
                // including a lead-1 moment that fell the day before (BR-MEM-13). `upcoming` never
                // returns a past anniversary, so an elapsed anniversary is dropped here.
                // Mốc đã qua vẫn bắn bù một lần khi kỳ giỗ chưa hết — kể cả mốc lead 1 rơi từ hôm trước.
                // `upcoming` không trả kỳ đã qua nên kỳ hết hạn bị bỏ, kỳ sau mới được đặt.
                key !in delivered -> MemorialReminder(key, entry.id, now, entry.leadDays)
                else -> null
            }
        }
    }

    fun keyOf(entryId: Long, anniversary: LocalDate): String = "$entryId:$anniversary"
}

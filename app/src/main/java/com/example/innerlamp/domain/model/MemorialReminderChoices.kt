package com.example.innerlamp.domain.model

import java.time.Duration
import java.time.LocalTime
import kotlin.math.abs

/**
 * The four reminder leads and four reminder times the UI may offer (BR-MEM-11). Any other stored value
 * is shown as the nearest choice but is not rewritten until the user saves. /
 * Bốn mốc ngày và bốn giờ nhắc UI được phép cho chọn. Giá trị cũ khác đi được hiện bằng lựa chọn gần
 * nhất nhưng không bị ghi đè cho tới khi người dùng lưu.
 */
object MemorialReminderChoices {

    val leadDays: List<Int> = listOf(0, 1, 3, 7)

    val times: List<LocalTime> = listOf(
        LocalTime.of(6, 0),
        LocalTime.of(7, 0),
        LocalTime.of(12, 0),
        LocalTime.of(19, 0),
    )

    /** Nearest lead; a tie goes to the earlier (larger) lead: 2 → 3, 5 → 7. / Hoà chọn mốc sớm hơn. */
    fun nearestLeadDays(value: Int): Int = leadDays.minByOrNull { candidate ->
        val distance = abs(candidate - value)
        // Tie → the larger lead (remind earlier). / Hoà → mốc lớn hơn (nhắc sớm hơn).
        distance * 1_000 - candidate
    } ?: leadDays.first()

    /**
     * Nearest time; a tie goes to the earlier time: 09:30 → 07:00, 15:30 → 12:00. Before 06:00 → 06:00,
     * after 19:00 → 19:00. / Hoà chọn giờ sớm hơn; trước 06:00 → 06:00, sau 19:00 → 19:00.
     */
    fun nearestTime(value: LocalTime): LocalTime = times.minByOrNull { candidate ->
        val distance = abs(Duration.between(candidate, value).toMinutes())
        // Tie → the earlier time. / Hoà → giờ sớm hơn (giá trị nhỏ hơn thắng).
        distance * 100_000 + candidate.toSecondOfDay()
    } ?: times.first()
}

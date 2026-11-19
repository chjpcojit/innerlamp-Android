package com.example.innerlamp.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The one notification channel of memorial reminders. It is created on demand (before any notification is
 * posted) with default importance, so Doze may delay it but never silently drop it. /
 * Kênh thông báo duy nhất của nhắc giỗ, tạo khi cần với importance mặc định.
 */
object NotificationChannels {

    const val MEMORIAL_REMINDERS = "memorial_reminders"

    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(MEMORIAL_REMINDERS) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                MEMORIAL_REMINDERS,
                "Nhắc giỗ",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Nhắc trước ngày giỗ của người thân trong sổ giỗ."
            },
        )
    }
}

/**
 * Notification copy by the real days left and relationship, never the person's name (NFR-MEM-02, MEM-RMD-03). /
 * Câu thông báo theo số ngày thật còn lại và quan hệ, không bao giờ có tên người thân.
 */
object MemorialNotificationText {

    /**
     * Whole days from [today] to the anniversary in [anniversaryKey] (`"<entryId>:<yyyy-MM-dd>"`), never
     * negative; `null` when the key carries no readable date so the caller can fall back. /
     * Số ngày thật tới ngày giỗ trong key, không âm; `null` khi key không có ngày đọc được.
     */
    fun daysUntil(anniversaryKey: String, today: LocalDate): Int? {
        val anniversary = runCatching { LocalDate.parse(anniversaryKey.substringAfter(':')) }
            .getOrNull() ?: return null
        return ChronoUnit.DAYS.between(today, anniversary).toInt().coerceAtLeast(0)
    }

    fun title(daysBefore: Int, relation: MemorialRelation): String = when (daysBefore) {
        0 -> "Hôm nay là giỗ ${relation.promptNoun}"
        1 -> "Ngày mai là giỗ ${relation.promptNoun}"
        else -> "Còn $daysBefore ngày nữa là giỗ ${relation.promptNoun}"
    }
}

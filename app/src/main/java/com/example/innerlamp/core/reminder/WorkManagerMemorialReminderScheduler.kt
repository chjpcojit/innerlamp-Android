package com.example.innerlamp.core.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.innerlamp.core.database.memorial.MemorialCacheDao
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.reminder.MemorialReminderPlanner
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import java.time.Clock
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Interim reminder implementation (decision D1): one one-time [MemorialReminderWorker] per anniversary with
 * an `initialDelay` to the moment. No exact alarm, so Doze may run it late — recorded as TD-MEM-CLIENT-04.
 * Every call replaces the previous plan, so a changed date/time never leaves an old reminder behind. /
 * Bản tạm (D1): một work one-time cho mỗi kỳ giỗ, `initialDelay` tới thời điểm. Không dùng exact alarm nên
 * Doze có thể trễ. Mỗi lần gọi thay toàn bộ kế hoạch cũ nên đổi ngày/giờ không để lại nhắc cũ.
 */
class WorkManagerMemorialReminderScheduler(
    private val context: Context,
    private val dao: MemorialCacheDao,
    private val workManager: WorkManager = WorkManager.getInstance(context),
    private val clock: Clock = Clock.systemDefaultZone(),
) : MemorialReminderScheduler {

    override suspend fun sync(entries: List<MemorialEntry>) {
        NotificationChannels.ensure(context)
        val delivered = runCatching { dao.deliveredKeys().toSet() }.getOrDefault(emptySet())
        val now = clock.instant()
        workManager.cancelAllWorkByTag(TAG)
        entries.forEach { entry ->
            MemorialReminderPlanner.plan(entry, now, clock.zone, delivered).forEach { reminder ->
                val delay = Duration.between(now, reminder.moment).toMillis().coerceAtLeast(0L)
                // No name or relationship in the input (A3); the worker reads the entry from Room.
                // Không có tên/quan hệ trong input (A3); worker đọc mục từ Room.
                val payload = MemorialReminderPayload(
                    entryId = reminder.entryId,
                    daysBefore = reminder.daysBefore,
                    time = entry.remindTime.format(TimeFormat),
                    anniversaryKey = reminder.key,
                )
                val request = OneTimeWorkRequestBuilder<MemorialReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(payload.toData())
                    .addTag(TAG)
                    .build()
                workManager.enqueueUniqueWork(
                    "${TAG}_${reminder.key}",
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            }
        }
    }

    override suspend fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG)
    }

    private companion object {
        /** Tag of every memorial reminder; sign-out and reschedule cancel by this tag. / Tag của mọi nhắc giỗ. */
        const val TAG = "memorial_reminder"
        val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

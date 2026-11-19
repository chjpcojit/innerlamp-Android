package com.example.innerlamp.core.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.innerlamp.MainActivity
import com.example.innerlamp.R
import com.example.innerlamp.data.mapper.toMemorialRelation
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.di.MemorialModule
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.ui.navigation.MemorialDeepLink
import java.time.LocalDate

/**
 * Fires one memorial reminder. No name and no relationship travel in the work input (A3): the entry is read
 * from the device copy, and the text falls back to "người thân" when the relationship cannot be read.
 * A key already announced is skipped, so a reschedule is harmless (MEM-RMD-03 "không bắn hai lần").
 * The notification is `VISIBILITY_PRIVATE` and its PendingIntent is immutable (A5, A6). /
 * Bắn một nhắc giỗ. Input không có tên/quan hệ (A3): đọc mục từ bản trên máy, quan hệ lạ thì dùng
 * "người thân". Key đã bắn thì bỏ qua. Thông báo PRIVATE, PendingIntent immutable (A5, A6).
 */
class MemorialReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    // The permission is checked in [canNotify] and notify() is wrapped in runCatching.
    // Quyền được kiểm trong [canNotify] và notify() nằm trong runCatching.
    @android.annotation.SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val key = inputData.getString(MemorialReminderPayload.KEY_ANNIVERSARY) ?: return Result.failure()
        val entryId = inputData.getLong(MemorialReminderPayload.KEY_ENTRY_ID, 0L)
        if (entryId <= 0L) return Result.failure()
        val daysBefore = inputData.getInt(MemorialReminderPayload.KEY_DAYS_BEFORE, 0)

        val dao = MemorialModule.provideDatabase(applicationContext).memorialCacheDao()
        if (dao.isDelivered(key)) return Result.success()
        // Only the signed-in account's entry may be announced; a deleted entry or a cleared copy is silent.
        // Chỉ mục của tài khoản đang đăng nhập mới được bắn; mục đã xoá hoặc bản đã xoá thì im lặng.
        val ownerId = AuthModule.provideSessionStore().currentUserId() ?: return Result.success()
        val entry = dao.getById(ownerId, entryId) ?: return Result.success()
        val relation = runCatching { entry.relationCode.toMemorialRelation() }
            .getOrDefault(MemorialRelation.Other)

        if (!canNotify(applicationContext)) {
            // Denied permission: keep the key unmarked so it is not lost if the user enables it later.
            // Bị từ chối quyền: không đánh dấu để không mất nhắc nếu sau này bật quyền.
            return Result.success()
        }
        NotificationChannels.ensure(applicationContext)
        val open = PendingIntent.getActivity(
            applicationContext,
            key.hashCode(),
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(MemorialDeepLink.EXTRA_OPEN_MEMORIAL_ID, entryId)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // The copy counts the real days left, not the lead: a BR-MEM-13 catch-up can fire on the
        // anniversary day even when leadDays is 1. / Câu chữ đếm số ngày thật còn lại, không dùng lead.
        val daysUntil = MemorialNotificationText.daysUntil(key, LocalDate.now()) ?: daysBefore
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.MEMORIAL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(MemorialNotificationText.title(daysUntil, relation))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Lock screen shows only the app name, never a name or note (A5, NFR-MEM-02).
            // Màn hình khoá chỉ hiện tên app, không hiện tên người hay ghi chú (A5).
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        val posted = runCatching {
            NotificationManagerCompat.from(applicationContext).notify(key.hashCode(), notification)
        }
        if (posted.isFailure) {
            // No AndroidRuntimeException crash, no delivered key: retrying later is still possible.
            // Không crash và không đánh dấu đã bắn để lần sau còn bắn được.
            Log.w(TAG, "Memorial reminder could not be posted; retried on the next sync.")
            return Result.success()
        }
        dao.markDelivered(
            com.example.innerlamp.core.database.memorial.MemorialReminderDeliveryEntity(key),
        )
        return Result.success()
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val TAG = "InnerLampReminder"
    }
}

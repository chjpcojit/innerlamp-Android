package com.example.innerlamp.domain.reminder

import com.example.innerlamp.domain.model.MemorialEntry

/**
 * Port that puts the account's reminders on the device. The interim implementation is
 * [com.example.innerlamp.core.reminder.WorkManagerMemorialReminderScheduler]; cutover to server push swaps
 * this provider only (decision D1). /
 * Port đặt nhắc của tài khoản lên máy. Bản tạm dùng WorkManager; khi chuyển sang push chỉ đổi provider.
 */
interface MemorialReminderScheduler {

    /** Replaces the pending reminders of [entries] (cancelled entries are dropped). / Đặt lại nhắc. */
    suspend fun sync(entries: List<MemorialEntry>)

    /** Cancels every memorial reminder on the device (sign-out, account switch). / Huỷ toàn bộ nhắc. */
    suspend fun cancelAll()
}

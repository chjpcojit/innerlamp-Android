package com.example.innerlamp.data.repository

import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import com.example.innerlamp.domain.repository.AccountDataCleaner
import com.example.innerlamp.domain.repository.MemorialRepository

/**
 * Sign-out / account switch: cancel the device reminders of the old account and drop its read copy, so the
 * next account never sees them (AC25, AC35, MEM-RMD-06). /
 * Đăng xuất/đổi tài khoản: huỷ nhắc của tài khoản cũ và xoá bản đọc, để tài khoản sau không thấy.
 */
class MemorialAccountCleaner(
    private val repository: MemorialRepository,
    private val scheduler: MemorialReminderScheduler,
) : AccountDataCleaner {
    override suspend fun clear() {
        scheduler.cancelAll()
        repository.clearCache()
    }
}

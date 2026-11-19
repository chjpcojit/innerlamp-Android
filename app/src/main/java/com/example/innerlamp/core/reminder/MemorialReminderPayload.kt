package com.example.innerlamp.core.reminder

import androidx.work.Data

/**
 * Everything the reminder worker gets. It deliberately has **no relationship and no name**: the worker reads
 * the entry from the device copy and falls back to "người thân" when the relationship is unknown, so a leaked
 * work input (or a WorkManager dump) carries no personal data. /
 * Toàn bộ dữ liệu worker nhận. Cố ý **không có quan hệ và không có tên**: worker đọc mục từ bản trên máy và
 * lùi về "người thân" khi quan hệ lạ, nên input của WorkManager không chứa dữ liệu cá nhân.
 */
data class MemorialReminderPayload(
    val entryId: Long,
    val daysBefore: Int,
    val time: String,
    val anniversaryKey: String,
) {
    /** The only four keys the worker may receive. / Bốn key duy nhất worker được nhận. */
    fun toData(): Data = Data.Builder()
        .putLong(KEY_ENTRY_ID, entryId)
        .putInt(KEY_DAYS_BEFORE, daysBefore)
        .putString(KEY_TIME, time)
        .putString(KEY_ANNIVERSARY, anniversaryKey)
        .build()

    companion object {
        const val KEY_ENTRY_ID = "entry_id"
        const val KEY_DAYS_BEFORE = "days_before"
        const val KEY_TIME = "time"
        const val KEY_ANNIVERSARY = "anniversary_key"

        val KEYS: Set<String> = setOf(KEY_ENTRY_ID, KEY_DAYS_BEFORE, KEY_TIME, KEY_ANNIVERSARY)
    }
}

package com.example.innerlamp.core.database.memorial

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Separate DB for the device copy of the memorial book. It is only a cache, so a destructive migration is
 * safe and `innerlamp.db` stays untouched. The whole file is excluded from cloud backup and device
 * transfer because it holds personal names and notes. /
 * DB riêng cho bản sổ giỗ trên máy. Chỉ là cache nên destructive migration an toàn, `innerlamp.db` không
 * bị đụng. Cả file bị loại khỏi cloud backup và chuyển máy vì chứa họ tên và ghi chú.
 */
@Database(
    entities = [MemorialCacheEntity::class, MemorialReminderDeliveryEntity::class],
    version = MemorialCacheDatabase.VERSION,
    exportSchema = true,
)
abstract class MemorialCacheDatabase : RoomDatabase() {

    abstract fun memorialCacheDao(): MemorialCacheDao

    companion object {
        const val VERSION = 1
        const val NAME = "innerlamp_memorial.db"
    }
}

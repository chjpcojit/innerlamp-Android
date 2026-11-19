package com.example.innerlamp.core.database.memorial

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Account-scoped device copy. Every read and write carries the owner, so signing in as another account
 * never shows the previous copy (AC35). /
 * Bản trên máy theo tài khoản. Mọi đọc/ghi đều kèm owner nên tài khoản khác không thấy bản của người trước.
 */
@Dao
abstract class MemorialCacheDao {

    @Query("SELECT * FROM memorial_cache WHERE ownerId = :ownerId ORDER BY id DESC")
    abstract suspend fun entries(ownerId: Long): List<MemorialCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: MemorialCacheEntity)

    /** The worker's lookup, scoped to the signed-in account (A3/G1). / Worker đọc mục trong tài khoản. */
    @Query("SELECT * FROM memorial_cache WHERE ownerId = :ownerId AND id = :id LIMIT 1")
    abstract suspend fun getById(ownerId: Long, id: Long): MemorialCacheEntity?

    @Query("DELETE FROM memorial_cache WHERE ownerId = :ownerId AND id = :id")
    abstract suspend fun deleteById(ownerId: Long, id: Long)

    @Query("DELETE FROM memorial_cache WHERE ownerId = :ownerId")
    abstract suspend fun deleteOwner(ownerId: Long)

    @Query("DELETE FROM memorial_cache WHERE ownerId != :ownerId")
    abstract suspend fun deleteOtherOwners(ownerId: Long)

    @Query("DELETE FROM memorial_cache")
    abstract suspend fun deleteAll()

    /** Replaces the whole copy of [ownerId] in one transaction. / Thay toàn bộ bản của [ownerId] nguyên tử. */
    @Transaction
    open suspend fun replaceAll(ownerId: Long, items: List<MemorialCacheEntity>) {
        deleteOwner(ownerId)
        items.forEach { upsert(it) }
    }

    /** Removes every account's copy and every delivered key. / Xoá bản mọi tài khoản và key đã bắn. */
    @Transaction
    open suspend fun clearAll() {
        deleteAll()
        deleteAllDeliveries()
    }

    // ── Reminder delivery log ────────────────────────────────────────────────

    @Query("SELECT delivery_key FROM memorial_reminder_delivery")
    abstract suspend fun deliveredKeys(): List<String>

    @Query("SELECT COUNT(*) FROM memorial_reminder_delivery WHERE delivery_key = :key")
    abstract suspend fun isDelivered(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun markDelivered(entity: MemorialReminderDeliveryEntity)

    @Query("DELETE FROM memorial_reminder_delivery WHERE delivery_key = :key")
    abstract suspend fun forgetDelivery(key: String)

    @Query("DELETE FROM memorial_reminder_delivery")
    abstract suspend fun deleteAllDeliveries()
}

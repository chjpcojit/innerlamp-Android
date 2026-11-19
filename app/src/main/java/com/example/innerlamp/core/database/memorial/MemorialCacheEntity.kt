package com.example.innerlamp.core.database.memorial

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * On-device read copy of one entry, scoped to the account that loaded it so another account can never see
 * it. `photo_url` is absent on purpose (signed for 15 minutes, D8). /
 * Bản đọc trên máy của một mục, gắn với tài khoản đã tải để tài khoản khác không bao giờ thấy. Cố ý không
 * có `photo_url` (URL ký 15 phút).
 */
@Entity(tableName = "memorial_cache", primaryKeys = ["ownerId", "id"])
data class MemorialCacheEntity(
    @ColumnInfo(name = "ownerId") val ownerId: Long,
    val id: Long,
    val name: String,
    @ColumnInfo(name = "relation_code") val relationCode: String,
    @ColumnInfo(name = "calendar_code") val calendarCode: String,
    val day: Int,
    val month: Int,
    @ColumnInfo(name = "death_year") val deathYear: Int?,
    @ColumnInfo(name = "leap_month") val leapMonth: Boolean,
    val note: String,
    val remind: Boolean,
    @ColumnInfo(name = "lead_days") val leadDays: Int,
    @ColumnInfo(name = "remind_time") val remindTime: String,
)

/**
 * Anniversary keys already announced on this device, so reopening the app cannot announce the same
 * anniversary twice. Excluded from backup with the rest of this database. /
 * Key kỳ giỗ đã bắn trên máy này để mở lại app không bắn hai lần. Cùng DB nên cũng bị loại khỏi backup.
 */
@Entity(tableName = "memorial_reminder_delivery")
data class MemorialReminderDeliveryEntity(
    @PrimaryKey @ColumnInfo(name = "delivery_key") val key: String,
)

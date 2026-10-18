package com.example.innerlamp.core.database.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * On-device copy of one loaded calendar year: the `data` payload as JSON, so new contract fields need
 * no migration. /
 * Bản sao trên máy của một năm lịch đã tải: payload `data` dạng JSON, thêm field mới không cần migration.
 */
@Entity(tableName = "calendar_year_cache")
data class CalendarYearCacheEntity(
    @PrimaryKey val year: Int,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    @ColumnInfo(name = "fetched_at_epoch_ms") val fetchedAtEpochMs: Long,
)

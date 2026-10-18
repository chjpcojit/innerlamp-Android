package com.example.innerlamp.core.database.calendar

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Separate DB for the calendar device copy. It is only a cache, so a destructive migration is safe and
 * `innerlamp.db` stays untouched. Schema exported to `app/schemas`. /
 * DB riêng cho bản sao lịch trên máy. Chỉ là cache nên destructive migration an toàn, `innerlamp.db`
 * không bị đụng tới.
 */
@Database(
    entities = [CalendarYearCacheEntity::class],
    version = CalendarCacheDatabase.VERSION,
    exportSchema = true,
)
abstract class CalendarCacheDatabase : RoomDatabase() {

    abstract fun calendarYearCacheDao(): CalendarYearCacheDao

    companion object {
        const val VERSION = 1
        const val NAME = "innerlamp_calendar.db"
    }
}

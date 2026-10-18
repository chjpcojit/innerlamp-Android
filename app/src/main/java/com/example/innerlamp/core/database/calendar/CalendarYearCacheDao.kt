package com.example.innerlamp.core.database.calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Abstract class (not an interface) so [replaceYear] can have a transactional body. /
 * Là abstract class (không phải interface) để [replaceYear] có thân chạy trong transaction.
 */
@Dao
abstract class CalendarYearCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: CalendarYearCacheEntity)

    @Query("SELECT * FROM calendar_year_cache WHERE year = :year")
    abstract suspend fun get(year: Int): CalendarYearCacheEntity?

    @Query("DELETE FROM calendar_year_cache WHERE year < :year")
    abstract suspend fun deleteBefore(year: Int)

    /** Upsert [entity] and delete years before [keepFromYear] atomically (AC22). / Thực hiện nguyên tử. */
    @Transaction
    open suspend fun replaceYear(entity: CalendarYearCacheEntity, keepFromYear: Int) {
        upsert(entity)
        deleteBefore(keepFromYear)
    }
}

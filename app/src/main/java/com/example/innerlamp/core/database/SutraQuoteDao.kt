package com.example.innerlamp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SutraQuoteDao {

    @Query("SELECT COUNT(*) FROM SutraQuote")
    suspend fun count(): Int

    @Query("SELECT sequence FROM SutraQuote")
    suspend fun allSequences(): List<Int>

    /** IGNORE: `sequence` trùng bị bỏ qua, không sửa dòng cũ. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quotes: List<SutraQuoteEntity>)

    @Query("SELECT * FROM SutraQuote ORDER BY sequence ASC LIMIT 1 OFFSET :offset")
    suspend fun atOffset(offset: Int): SutraQuoteEntity?

    @Query("SELECT * FROM SutraQuote ORDER BY sequence ASC")
    suspend fun getAll(): List<SutraQuoteEntity>
}

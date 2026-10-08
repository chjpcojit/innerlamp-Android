package com.example.innerlamp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/** DB local của app (Room). Schema export vào `app/schemas` để review migration. */
@Database(entities = [SutraQuoteEntity::class], version = InnerLampDatabase.VERSION, exportSchema = true)
abstract class InnerLampDatabase : RoomDatabase() {

    abstract fun sutraQuoteDao(): SutraQuoteDao

    companion object {
        const val VERSION = 1
        const val NAME = "innerlamp.db"
    }
}

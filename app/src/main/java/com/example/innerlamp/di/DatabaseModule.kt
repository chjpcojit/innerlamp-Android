package com.example.innerlamp.di

import android.content.Context
import androidx.room.Room
import com.example.innerlamp.core.database.InnerLampDatabase
import com.example.innerlamp.data.quote.RoomSutraQuoteRepository
import com.example.innerlamp.domain.repository.SutraQuoteRepository
import com.example.innerlamp.domain.usecase.SutraQuoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wiring thủ công cho DB local (không thêm thư viện DI). */
object DatabaseModule {

    private const val SUTRA_QUOTES_ASSET = "sutra_quotes_300.json"

    @Volatile
    private var database: InnerLampDatabase? = null

    fun provideDatabase(context: Context): InnerLampDatabase =
        database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                InnerLampDatabase::class.java,
                InnerLampDatabase.NAME,
            ).build().also { database = it }
        }

    fun provideSutraQuoteRepository(context: Context): SutraQuoteRepository {
        val appContext = context.applicationContext
        return RoomSutraQuoteRepository(
            dao = provideDatabase(appContext).sutraQuoteDao(),
            loadCatalog = {
                withContext(Dispatchers.IO) {
                    appContext.assets.open(SUTRA_QUOTES_ASSET).bufferedReader().use { it.readText() }
                }
            },
        )
    }

    fun provideSutraQuoteUseCase(context: Context): SutraQuoteUseCase =
        SutraQuoteUseCase(provideSutraQuoteRepository(context))
}

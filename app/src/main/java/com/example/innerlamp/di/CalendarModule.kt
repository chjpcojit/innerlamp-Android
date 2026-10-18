package com.example.innerlamp.di

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.innerlamp.core.database.calendar.CalendarCacheDatabase
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.remote.api.CalendarApiService
import com.example.innerlamp.data.repository.CalendarEventRepositoryImpl
import com.example.innerlamp.domain.repository.CalendarEventRepository
import com.example.innerlamp.domain.usecase.GetEventOccurrenceUseCase
import com.example.innerlamp.domain.usecase.GetHomeEventBlockUseCase
import com.example.innerlamp.InnerLampApplication
import com.example.innerlamp.domain.usecase.GetUpcomingFastDaysUseCase
import com.example.innerlamp.domain.usecase.LoadCalendarYearUseCase
import com.example.innerlamp.ui.screens.chua.ChuaViewModel
import com.example.innerlamp.ui.screens.festivals.FestivalDetailViewModel
import com.example.innerlamp.ui.screens.festivals.FestivalsViewModel
import java.time.Clock

/**
 * Manual wiring for the calendar feature (no DI library). The repository owns the in-memory cache, so
 * keep ONE instance: `InnerLampApplication.calendarEventRepository`. /
 * Wiring thủ công cho lịch lễ. Repository giữ cache bộ nhớ nên chỉ giữ MỘT instance, ở
 * `InnerLampApplication.calendarEventRepository`.
 */
object CalendarModule {

    @Volatile
    private var database: CalendarCacheDatabase? = null

    fun provideDatabase(context: Context): CalendarCacheDatabase =
        database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                CalendarCacheDatabase::class.java,
                CalendarCacheDatabase.NAME,
            ).fallbackToDestructiveMigration(dropAllTables = true).build().also { database = it }
        }

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): CalendarApiService = networkClient.createService(CalendarApiService::class.java)

    fun provideCalendarEventRepository(
        context: Context,
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    ): CalendarEventRepository = CalendarEventRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
        dao = provideDatabase(context).calendarYearCacheDao(),
        json = NetworkModule.provideJson(),
        dispatchers = dispatchers,
        logger = { message, cause -> Log.w("InnerLampCalendar", message, cause) },
    )

    fun provideLoadCalendarYearUseCase(
        repository: CalendarEventRepository,
        clock: Clock = Clock.systemUTC(),
    ) = LoadCalendarYearUseCase(repository, clock)

    fun provideGetHomeEventBlockUseCase(
        repository: CalendarEventRepository,
        clock: Clock = Clock.systemUTC(),
    ) = GetHomeEventBlockUseCase(repository, clock)

    fun provideGetEventOccurrenceUseCase(repository: CalendarEventRepository) =
        GetEventOccurrenceUseCase(repository)

    fun provideGetUpcomingFastDaysUseCase(repository: CalendarEventRepository) =
        GetUpcomingFastDaysUseCase(repository)

    // ── ViewModel factories (the single repository comes from the Application) ──
    // ── Factory ViewModel (repository duy nhất lấy từ Application) ──

    private fun androidx.lifecycle.viewmodel.CreationExtras.repository(): CalendarEventRepository =
        (this[APPLICATION_KEY] as InnerLampApplication).calendarEventRepository

    fun festivalsViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { FestivalsViewModel(provideLoadCalendarYearUseCase(repository())) }
    }

    fun festivalDetailViewModelFactory(eventCode: String, solarDate: String): ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                FestivalDetailViewModel(eventCode, solarDate, provideGetEventOccurrenceUseCase(repository()))
            }
        }

    fun chuaViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { ChuaViewModel(getUpcomingFastDays = provideGetUpcomingFastDaysUseCase(repository())) }
    }
}

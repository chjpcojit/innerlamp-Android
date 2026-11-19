package com.example.innerlamp.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.innerlamp.core.database.memorial.MemorialCacheDatabase
import com.example.innerlamp.core.media.MemorialPhotoNormalizer
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.reminder.WorkManagerMemorialReminderScheduler
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.remote.api.MemorialApiService
import com.example.innerlamp.data.repository.MemorialAccountCleaner
import com.example.innerlamp.data.repository.MemorialRepositoryImpl
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import com.example.innerlamp.domain.repository.AccountDataCleaner
import com.example.innerlamp.domain.repository.MemorialRepository
import com.example.innerlamp.domain.usecase.DeleteMemorialUseCase
import com.example.innerlamp.domain.usecase.LoadMemorialsUseCase
import com.example.innerlamp.domain.usecase.SaveMemorialUseCase
import com.example.innerlamp.ui.screens.gio.GioViewModel

/**
 * Manual wiring of the memorial feature (no DI library). One database per process; the repository keeps no
 * state, so a new instance per use is fine. /
 * Wiring thủ công cho sổ giỗ. Một database cho mỗi process; repository không giữ state nên tạo mới mỗi lần
 * dùng được.
 */
object MemorialModule {

    @Volatile
    private var database: MemorialCacheDatabase? = null

    fun provideDatabase(context: Context): MemorialCacheDatabase =
        database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                MemorialCacheDatabase::class.java,
                MemorialCacheDatabase.NAME,
            ).fallbackToDestructiveMigration(dropAllTables = true).build().also { database = it }
        }

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): MemorialApiService = networkClient.createService(MemorialApiService::class.java)

    fun provideRepository(
        context: Context,
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    ): MemorialRepository = MemorialRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
        dao = provideDatabase(context).memorialCacheDao(),
        // The owner comes from the session, so one account never reads another's copy.
        // Owner lấy từ phiên đăng nhập để tài khoản này không đọc bản của tài khoản khác.
        currentOwnerId = { AuthModule.provideSessionStore().currentUserId() },
        dispatchers = dispatchers,
    )

    /**
     * Interim local reminders (D1). Cutover to server push replaces this one line with a no-op scheduler. /
     * Nhắc local tạm thời. Khi chuyển sang push chỉ đổi một dòng này thành scheduler rỗng.
     */
    fun provideReminderScheduler(context: Context): MemorialReminderScheduler =
        WorkManagerMemorialReminderScheduler(
            context = context.applicationContext,
            dao = provideDatabase(context).memorialCacheDao(),
        )

    fun provideAccountCleaner(context: Context): AccountDataCleaner = MemorialAccountCleaner(
        repository = provideRepository(context),
        scheduler = provideReminderScheduler(context),
    )

    fun provideLoadMemorialsUseCase(context: Context) = LoadMemorialsUseCase(
        repository = provideRepository(context),
        scheduler = provideReminderScheduler(context),
    )

    fun provideSaveMemorialUseCase(context: Context) = SaveMemorialUseCase(provideRepository(context))

    fun provideDeleteMemorialUseCase(context: Context) = DeleteMemorialUseCase(
        repository = provideRepository(context),
        scheduler = provideReminderScheduler(context),
    )

    /** The whole feature for the two screens that share one ViewModel. / Toàn bộ tính năng cho hai màn. */
    fun gioViewModelFactory(context: Context): ViewModelProvider.Factory {
        val appContext = context.applicationContext
        return viewModelFactory {
            initializer {
                GioViewModel(
                    loadMemorials = provideLoadMemorialsUseCase(appContext),
                    saveMemorial = provideSaveMemorialUseCase(appContext),
                    deleteMemorial = provideDeleteMemorialUseCase(appContext),
                    scheduler = provideReminderScheduler(appContext),
                    repository = provideRepository(appContext),
                    photoNormalizer = MemorialPhotoNormalizer(DefaultDispatcherProvider),
                )
            }
        }
    }
}

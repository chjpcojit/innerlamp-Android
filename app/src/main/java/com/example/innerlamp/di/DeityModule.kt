package com.example.innerlamp.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.remote.api.DeityApiService
import com.example.innerlamp.data.repository.DeityRepositoryImpl
import com.example.innerlamp.domain.repository.DeityRepository
import com.example.innerlamp.domain.usecase.GetBuddhaDetailUseCase
import com.example.innerlamp.domain.usecase.GetDeityCatalogUseCase
import com.example.innerlamp.domain.usecase.GetFeaturedDeitiesUseCase
import com.example.innerlamp.ui.screens.buddha.BuddhaDetailViewModel
import com.example.innerlamp.ui.screens.buddha.BuddhaListViewModel
import com.example.innerlamp.ui.screens.library.LibraryViewModel

/** Manual wiring of the deity catalogue (no DI library). / Wiring thủ công cho chư Phật & Bồ Tát. */
object DeityModule {

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): DeityApiService = networkClient.createService(DeityApiService::class.java)

    /** No cache inside, so a new instance per use is fine. / Không giữ cache nên tạo mới mỗi lần được. */
    fun provideDeityRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): DeityRepository = DeityRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )

    fun provideGetFeaturedDeitiesUseCase(repository: DeityRepository = provideDeityRepository()) =
        GetFeaturedDeitiesUseCase(repository)

    fun buddhaListViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { BuddhaListViewModel(GetDeityCatalogUseCase(provideDeityRepository())) }
    }

    fun buddhaDetailViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            BuddhaDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                getDetail = GetBuddhaDetailUseCase(provideDeityRepository()),
            )
        }
    }

    fun libraryViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { LibraryViewModel(getFeaturedDeities = provideGetFeaturedDeitiesUseCase()) }
    }
}

package com.example.innerlamp.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.remote.api.TempleApiService
import com.example.innerlamp.data.repository.TempleOptionRepositoryImpl
import com.example.innerlamp.data.repository.TempleRepositoryImpl
import com.example.innerlamp.domain.repository.TempleOptionRepository
import com.example.innerlamp.domain.repository.TempleRepository
import com.example.innerlamp.domain.usecase.GetFeaturedTemplesUseCase
import com.example.innerlamp.domain.usecase.GetTempleDetailUseCase
import com.example.innerlamp.domain.usecase.GetTempleDirectoryUseCase
import com.example.innerlamp.ui.screens.temple.TempleViewModel
import com.example.innerlamp.ui.screens.templelist.TempleListViewModel

/** Wiring thủ công cho data layer của chùa (không thêm thư viện DI). */
object TempleModule {

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): TempleApiService = networkClient.createService(TempleApiService::class.java)

    fun provideTempleOptionRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): TempleOptionRepository = TempleOptionRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )

    /** No cache inside, so a new instance per use is fine. / Không giữ cache nên tạo mới mỗi lần được. */
    fun provideTempleRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): TempleRepository = TempleRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )

    fun provideGetFeaturedTemplesUseCase(repository: TempleRepository = provideTempleRepository()) =
        GetFeaturedTemplesUseCase(repository)

    fun templeListViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { TempleListViewModel(GetTempleDirectoryUseCase(provideTempleRepository())) }
    }

    fun templeViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            TempleViewModel(
                savedStateHandle = createSavedStateHandle(),
                getDetail = GetTempleDetailUseCase(provideTempleRepository()),
            )
        }
    }
}

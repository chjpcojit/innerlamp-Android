package com.example.innerlamp.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.remote.api.GalleryApiService
import com.example.innerlamp.data.repository.GalleryRepositoryImpl
import com.example.innerlamp.domain.repository.GalleryRepository
import com.example.innerlamp.domain.usecase.GetHomePhotosUseCase
import com.example.innerlamp.domain.usecase.GetPhotoCreditUseCase
import com.example.innerlamp.domain.usecase.GetTemplePhotosUseCase
import com.example.innerlamp.ui.screens.photos.PhotosViewModel

/** Manual wiring of the gallery (no DI library). / Wiring thủ công cho Ảnh đẹp. */
object GalleryModule {

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): GalleryApiService = networkClient.createService(GalleryApiService::class.java)

    /** No cache inside, so a new instance per use is fine. / Không giữ cache nên tạo mới mỗi lần được. */
    fun provideGalleryRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): GalleryRepository = GalleryRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )

    fun provideGetHomePhotosUseCase(repository: GalleryRepository = provideGalleryRepository()) =
        GetHomePhotosUseCase(repository)

    fun photosViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val repository = provideGalleryRepository()
            PhotosViewModel(
                getPhotos = GetTemplePhotosUseCase(repository),
                getCredit = GetPhotoCreditUseCase(repository),
            )
        }
    }
}

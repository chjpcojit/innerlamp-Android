package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.HomePhotos
import com.example.innerlamp.domain.repository.GalleryRepository

/** The three photos of the home block + the "+N" count. / Ba ảnh của khối Trang chủ + số "+N". */
class GetHomePhotosUseCase(private val repository: GalleryRepository) {
    suspend operator fun invoke(): Result<HomePhotos> = repository.getHomePhotos(HOME_PHOTO_COUNT)

    private companion object {
        const val HOME_PHOTO_COUNT = 3
    }
}

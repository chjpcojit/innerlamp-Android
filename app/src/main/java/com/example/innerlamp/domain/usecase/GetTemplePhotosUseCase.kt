package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.domain.repository.GalleryRepository

/**
 * Every photo of the "Ảnh đẹp" screen (12 today, one page); the chips filter on the device. /
 * Toàn bộ ảnh của màn "Ảnh đẹp" (hiện 12 ảnh, một trang); chip lọc trên máy.
 */
class GetTemplePhotosUseCase(private val repository: GalleryRepository) {
    suspend operator fun invoke(): Result<List<TemplePhoto>> = repository.getAll()
}

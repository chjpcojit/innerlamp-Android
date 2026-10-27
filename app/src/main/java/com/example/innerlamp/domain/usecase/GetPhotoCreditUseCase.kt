package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.repository.GalleryRepository

/** Credit shown in the lightbox (lists do not carry it yet). / Ghi công hiện trong lightbox. */
class GetPhotoCreditUseCase(private val repository: GalleryRepository) {
    suspend operator fun invoke(photoId: String): Result<PhotoCredit> = repository.getCredit(photoId)
}

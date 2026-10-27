package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.repository.DeityRepository

/** Featured deities for the Home and Library blocks. / Chư Phật nổi bật cho khối Trang chủ và Thư viện. */
class GetFeaturedDeitiesUseCase(private val repository: DeityRepository) {
    suspend operator fun invoke(): Result<List<DeitySummary>> = repository.getFeatured()
}

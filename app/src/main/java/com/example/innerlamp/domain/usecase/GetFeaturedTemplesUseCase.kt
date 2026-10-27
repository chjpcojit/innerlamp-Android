package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.repository.TempleRepository

/** Featured temples for the home block. / Chùa nổi bật cho khối ở Trang chủ. */
class GetFeaturedTemplesUseCase(private val repository: TempleRepository) {
    suspend operator fun invoke(): Result<List<Temple>> = repository.getFeatured()
}

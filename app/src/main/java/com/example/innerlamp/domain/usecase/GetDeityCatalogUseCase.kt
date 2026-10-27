package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.repository.DeityRepository

/**
 * The whole catalogue for "Chư Phật & Bồ Tát" (20 entries today, one page); the chips filter on the device. /
 * Toàn bộ danh mục cho màn "Chư Phật & Bồ Tát" (hiện 20 vị, một trang); chip lọc trên máy.
 */
class GetDeityCatalogUseCase(private val repository: DeityRepository) {
    suspend operator fun invoke(): Result<List<DeitySummary>> = repository.getAll()
}

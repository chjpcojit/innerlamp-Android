package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.repository.TempleRepository

/**
 * The whole directory for "Tất cả chùa". The contract has no `q`/`region` on `GET /v1/temples` yet, so the
 * screen loads every page and filters on the device. /
 * Toàn bộ danh bạ cho "Tất cả chùa". Contract chưa có `q`/`region` nên màn tải mọi trang rồi lọc trên máy.
 */
class GetTempleDirectoryUseCase(private val repository: TempleRepository) {
    suspend operator fun invoke(): Result<List<Temple>> = repository.getAll()
}

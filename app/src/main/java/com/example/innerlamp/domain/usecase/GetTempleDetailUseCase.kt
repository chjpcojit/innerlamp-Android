package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.repository.TempleRepository

/** Content of one temple page. / Nội dung một trang chùa. */
class GetTempleDetailUseCase(private val repository: TempleRepository) {
    suspend operator fun invoke(templeId: String): Result<TempleDetail> = repository.getDetail(templeId)
}

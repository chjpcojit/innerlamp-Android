package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.TempleOptionPage
import com.example.innerlamp.domain.repository.TempleOptionRepository

/**
 * Tìm chùa cho ô "Nơi xuất gia" (đăng ký/hồ sơ).
 *
 * Từ khoá rỗng/chỉ khoảng trắng nghĩa là không tìm (trả trang đầu theo thứ tự backend).
 * Threading do repository/data layer lo; lỗi trả qua `Result.failure`.
 */
class SearchTempleOptionsUseCase(
    private val repository: TempleOptionRepository,
) {
    suspend operator fun invoke(query: String, page: Int = 1): Result<TempleOptionPage> =
        repository.getOptions(page = page, query = query.trim().ifEmpty { null })
}

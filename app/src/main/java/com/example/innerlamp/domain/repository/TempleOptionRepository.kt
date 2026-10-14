package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.TempleOptionPage

/**
 * Hợp đồng do tầng `domain` sở hữu, tầng `data` implement. Lỗi trả qua `Result.failure`.
 */
interface TempleOptionRepository {

    /**
     * Lấy một trang lựa chọn chùa.
     *
     * Thứ tự do backend quyết định (chùa cổ/di sản trước), không theo ABC. Backend không có
     * bộ lọc theo tỉnh — muốn lọc theo tỉnh thì lọc phía client bằng `provinceCode`.
     *
     * @param page số trang, bắt đầu từ 1.
     * @param query từ khoá tìm theo tên chùa; `null`/rỗng nghĩa là không tìm kiếm.
     */
    suspend fun getOptions(page: Int = 1, query: String? = null): Result<TempleOptionPage>
}

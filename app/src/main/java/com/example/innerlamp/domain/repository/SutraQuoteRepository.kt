package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.SutraQuote

/**
 * Hợp đồng do tầng `domain` sở hữu, tầng `data` implement. Mọi truy vấn sắp theo
 * `sequence` tăng dần.
 */
interface SutraQuoteRepository {

    /**
     * Nạp catalog bundled vào DB, idempotent: chỉ thêm quote có `sequence` chưa có,
     * không sửa/xoá dòng cũ.
     */
    suspend fun seedIfNeeded()

    suspend fun count(): Int

    /** Quote ở vị trí [index] (0-based) khi sắp theo `sequence` tăng dần; `null` nếu ngoài phạm vi. */
    suspend fun quoteAt(index: Int): SutraQuote?

    /** Toàn bộ quote, sắp theo `sequence` tăng dần. */
    suspend fun getAll(): List<SutraQuote>
}

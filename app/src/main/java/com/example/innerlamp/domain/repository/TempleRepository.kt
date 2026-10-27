package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.TempleDetail

/**
 * Public temple directory (`/v1/temples*`). Failures are [com.example.innerlamp.domain.model.ContentError]. /
 * Danh bạ chùa công khai. Lỗi trả về là `ContentError`.
 */
interface TempleRepository {

    /** `GET /v1/temples/top` — the home "Chùa Việt Nam" block. / Khối chùa nổi bật ở Trang chủ. */
    suspend fun getFeatured(): Result<List<Temple>>

    /** Every page of `GET /v1/temples`, in server order. / Mọi trang của danh bạ, giữ thứ tự server. */
    suspend fun getAll(): Result<List<Temple>>

    /** `GET /v1/temples/{id}`; unknown id → `ContentError.NotFound`. / Id lạ → `NotFound`. */
    suspend fun getDetail(id: String): Result<TempleDetail>
}

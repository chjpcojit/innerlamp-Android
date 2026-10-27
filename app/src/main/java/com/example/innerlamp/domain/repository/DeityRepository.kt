package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.DeitySummary

/**
 * Public deity catalogue (`/v1/deities*`). Failures are [com.example.innerlamp.domain.model.ContentError]. /
 * Danh mục chư Phật & Bồ Tát công khai. Lỗi trả về là `ContentError`.
 */
interface DeityRepository {

    /** `GET /v1/deities/top`. */
    suspend fun getFeatured(): Result<List<DeitySummary>>

    /** Every page of `GET /v1/deities` (all types), in server order. / Mọi trang, mọi loại. */
    suspend fun getAll(): Result<List<DeitySummary>>

    /** `GET /v1/deities/{id}`; unknown id → `ContentError.NotFound`. */
    suspend fun getProfile(id: String): Result<BuddhaProfile>
}

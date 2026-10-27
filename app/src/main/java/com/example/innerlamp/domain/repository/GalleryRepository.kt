package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.HomePhotos
import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.model.TemplePhoto

/**
 * Public gallery (`/v1/gallery*`). Failures are [com.example.innerlamp.domain.model.ContentError]. /
 * Thư viện ảnh công khai. Lỗi trả về là `ContentError`.
 */
interface GalleryRepository {

    /** Every page of `GET /v1/gallery` (all types), in server order. / Mọi trang, mọi loại. */
    suspend fun getAll(): Result<List<TemplePhoto>>

    /** First page of `GET /v1/gallery`: the first [count] photos and `total`. / Trang đầu: [count] ảnh + tổng. */
    suspend fun getHomePhotos(count: Int): Result<HomePhotos>

    /** `GET /v1/gallery/{id}` → author / license / source. / Ghi công của một ảnh. */
    suspend fun getCredit(id: String): Result<PhotoCredit>
}

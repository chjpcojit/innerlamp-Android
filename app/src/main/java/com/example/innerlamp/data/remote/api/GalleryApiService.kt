package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.GalleryDetailDto
import com.example.innerlamp.data.remote.dto.GalleryPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Public gallery routes — no `@Authenticated`, no `X-API-Key`. / Route ảnh công khai. */
interface GalleryApiService {

    /** All types when [type] is `null`. / `null` = mọi loại. */
    @GET("v1/gallery")
    suspend fun getList(
        @Query("page") page: Int,
        @Query("type") type: String? = null,
    ): ApiResponse<GalleryPageDto>

    /** `404 GALLERY_IMAGE_NOT_FOUND` for an unknown id. / Id lạ trả 404. */
    @GET("v1/gallery/{id}")
    suspend fun getDetail(@Path("id") id: String): ApiResponse<GalleryDetailDto>
}

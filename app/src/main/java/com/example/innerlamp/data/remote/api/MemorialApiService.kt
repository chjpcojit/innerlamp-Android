package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.auth.Authenticated
import com.example.innerlamp.data.remote.dto.MemorialDto
import com.example.innerlamp.data.remote.dto.MemorialListDto
import com.example.innerlamp.data.remote.dto.MemorialRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * The seven memorial routes (client contract v1 §3). All need a Bearer token; nothing else adds headers.
 * The two photo routes stay unreachable until the backend deploys them (D4) and the build flag is off. /
 * Bảy route sổ giỗ. Tất cả cần Bearer. Hai route ảnh chưa dùng được cho tới khi BE deploy (D4) và cờ tắt.
 */
interface MemorialApiService {

    @Authenticated
    @GET("v1/memorials")
    suspend fun list(): ApiResponse<MemorialListDto>

    @Authenticated
    @GET("v1/memorials/{id}")
    suspend fun get(@Path("id") id: Long): ApiResponse<MemorialDto>

    @Authenticated
    @POST("v1/memorials")
    suspend fun create(@Body body: MemorialRequestDto): ApiResponse<MemorialDto>

    @Authenticated
    @PUT("v1/memorials/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: MemorialRequestDto): ApiResponse<MemorialDto>

    /** `data` is absent on success. / Thành công không có `data`. */
    @Authenticated
    @DELETE("v1/memorials/{id}")
    suspend fun delete(@Path("id") id: Long): ApiResponse<Unit>

    /** Multipart with exactly one part named `photo` (contract §3.6). / Đúng một part tên `photo`. */
    @Authenticated
    @Multipart
    @PUT("v1/memorials/{id}/photo")
    suspend fun uploadPhoto(@Path("id") id: Long, @Part photo: MultipartBody.Part): ApiResponse<MemorialDto>

    @Authenticated
    @DELETE("v1/memorials/{id}/photo")
    suspend fun deletePhoto(@Path("id") id: Long): ApiResponse<MemorialDto>
}

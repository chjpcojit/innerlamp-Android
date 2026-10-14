package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.TempleOptionPageDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Endpoint chùa. Mọi route ở đây là public — không gắn `@Authenticated`, không gửi `X-API-Key`. */
interface TempleApiService {

    /**
     * @param query để `null` khi không tìm kiếm (Retrofit bỏ hẳn tham số); chuỗi rỗng bị
     * backend từ chối bằng `400 INVALID_FILTER`.
     */
    @GET("v1/temples/options")
    suspend fun getOptions(
        @Query("page") page: Int,
        @Query("q") query: String?,
    ): ApiResponse<TempleOptionPageDto>
}

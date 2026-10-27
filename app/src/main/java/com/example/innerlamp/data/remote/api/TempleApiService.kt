package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.TempleDetailDto
import com.example.innerlamp.data.remote.dto.TempleItemsDto
import com.example.innerlamp.data.remote.dto.TempleOptionPageDto
import com.example.innerlamp.data.remote.dto.TemplePageDto
import retrofit2.http.GET
import retrofit2.http.Path
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

    /** Up to 10 featured temples. / Tối đa 10 chùa nổi bật. */
    @GET("v1/temples/top")
    suspend fun getTop(): ApiResponse<TempleItemsDto>

    /** One page of 20 (`page_size` is read from the response). / Một trang 20 mục. */
    @GET("v1/temples")
    suspend fun getList(@Query("page") page: Int): ApiResponse<TemplePageDto>

    /** `404 TEMPLE_NOT_FOUND` for an unknown id. / Id lạ trả `404 TEMPLE_NOT_FOUND`. */
    @GET("v1/temples/{id}")
    suspend fun getDetail(@Path("id") id: String): ApiResponse<TempleDetailDto>
}

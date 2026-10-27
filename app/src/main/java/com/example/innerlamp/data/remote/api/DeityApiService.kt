package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.DeityDetailDto
import com.example.innerlamp.data.remote.dto.DeityItemsDto
import com.example.innerlamp.data.remote.dto.DeityPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Public deity routes — no `@Authenticated`, no `X-API-Key`. / Route chư Phật công khai. */
interface DeityApiService {

    @GET("v1/deities/top")
    suspend fun getTop(): ApiResponse<DeityItemsDto>

    /** All types when [type] is `null` (Retrofit drops the parameter). / `null` = mọi loại. */
    @GET("v1/deities")
    suspend fun getList(
        @Query("page") page: Int,
        @Query("type") type: String? = null,
    ): ApiResponse<DeityPageDto>

    /** `404 DEITY_NOT_FOUND` for an unknown id. / Id lạ trả `404 DEITY_NOT_FOUND`. */
    @GET("v1/deities/{id}")
    suspend fun getDetail(@Path("id") id: String): ApiResponse<DeityDetailDto>
}

package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.DistrictDto
import com.example.innerlamp.data.remote.dto.ItemsDto
import com.example.innerlamp.data.remote.dto.ProvinceDto
import com.example.innerlamp.data.remote.dto.WardDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Metadata đơn vị hành chính (public). `new` = hệ 2 cấp (từ 01/07/2025), `old` = hệ 3 cấp.
 * Backend có thêm các endpoint `.../conversion` (đổi cũ ↔ mới) — chưa dùng nên chưa khai báo.
 */
interface AdministrativeUnitApiService {

    @GET("v1/administrative-units/new/provinces")
    suspend fun getProvinces(): ApiResponse<ItemsDto<ProvinceDto>>

    /** @param query để `null` khi không lọc; chuỗi rỗng bị backend từ chối `400 INVALID_FILTER`. */
    @GET("v1/administrative-units/new/provinces/{code}/wards")
    suspend fun getWards(
        @Path("code") provinceCode: String,
        @Query("q") query: String?,
    ): ApiResponse<ItemsDto<WardDto>>

    @GET("v1/administrative-units/old/provinces")
    suspend fun getLegacyProvinces(): ApiResponse<ItemsDto<ProvinceDto>>

    @GET("v1/administrative-units/old/provinces/{code}/districts")
    suspend fun getLegacyDistricts(
        @Path("code") provinceCode: String,
    ): ApiResponse<ItemsDto<DistrictDto>>

    @GET("v1/administrative-units/old/districts/{code}/wards")
    suspend fun getLegacyWards(
        @Path("code") districtCode: String,
    ): ApiResponse<ItemsDto<WardDto>>
}

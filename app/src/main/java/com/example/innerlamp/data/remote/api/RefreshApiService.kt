package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.RefreshRequestDto
import com.example.innerlamp.data.remote.dto.RefreshResultDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Token refresh. Must be created from a client WITHOUT the refresh authenticator (separate client)
 * so a rejected refresh cannot recurse. / Phải tạo từ client KHÔNG có authenticator refresh để tránh đệ quy.
 */
interface RefreshApiService {

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): ApiResponse<RefreshResultDto>
}

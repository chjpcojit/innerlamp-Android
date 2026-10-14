package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkException
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.TempleApiService
import com.example.innerlamp.domain.model.TempleOptionPage
import com.example.innerlamp.domain.repository.TempleOptionRepository

/**
 * Cài đặt [TempleOptionRepository] qua `GET /v1/temples/options`. Không cache: kết quả phụ
 * thuộc `page` + từ khoá tìm kiếm của người dùng.
 */
class TempleOptionRepositoryImpl(
    private val api: TempleApiService,
    private val networkClient: NetworkClient,
) : TempleOptionRepository {

    override suspend fun getOptions(page: Int, query: String?): Result<TempleOptionPage> {
        // Backend từ chối `q` rỗng/chỉ khoảng trắng → bỏ hẳn tham số; và giới hạn 100 ký tự.
        val q = query?.trim()?.take(MAX_QUERY_LENGTH)?.takeIf { it.isNotEmpty() }
        return when (val result = networkClient.safeCall { api.getOptions(page, q) }) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Failure -> Result.failure(NetworkException(result.error))
        }
    }

    private companion object {
        const val MAX_QUERY_LENGTH = 100
    }
}

package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkException
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.ElapsedClock
import com.example.innerlamp.core.util.SystemElapsedClock
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.AdministrativeUnitApiService
import com.example.innerlamp.data.remote.dto.ItemsDto
import com.example.innerlamp.domain.model.District
import com.example.innerlamp.domain.model.Province
import com.example.innerlamp.domain.model.Ward
import com.example.innerlamp.domain.repository.AdministrativeUnitRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * Cài đặt [AdministrativeUnitRepository] qua `GET /v1/administrative-units/...`.
 *
 * Backend trả `Cache-Control: public, max-age=86400` và không có ETag/version, nên cache
 * trong bộ nhớ theo TTL 24h (đo bằng [ElapsedClock]). Chỉ cache kết quả thành công; truy vấn
 * có từ khoá `q` không cache vì không gian khoá không giới hạn.
 */
class AdministrativeUnitRepositoryImpl(
    private val api: AdministrativeUnitApiService,
    private val networkClient: NetworkClient,
    private val clock: ElapsedClock = SystemElapsedClock,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) : AdministrativeUnitRepository {

    private class CacheEntry(val value: Any, val storedAtMillis: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun getProvinces(): Result<List<Province>> =
        cached("new/provinces") {
            networkClient.safeCall { api.getProvinces() }.mapList { it.toDomain() }
        }

    override suspend fun getWards(provinceCode: String, query: String?): Result<List<Ward>> {
        // Backend từ chối `q` rỗng → bỏ hẳn tham số; giới hạn 100 ký tự.
        val q = query?.trim()?.take(MAX_QUERY_LENGTH)?.takeIf { it.isNotEmpty() }
        val fetch: suspend () -> Result<List<Ward>> = {
            networkClient.safeCall { api.getWards(provinceCode, q) }.mapList { it.toDomain() }
        }
        return if (q == null) cached("new/provinces/$provinceCode/wards", fetch) else fetch()
    }

    override suspend fun getLegacyProvinces(): Result<List<Province>> =
        cached("old/provinces") {
            networkClient.safeCall { api.getLegacyProvinces() }.mapList { it.toDomain() }
        }

    override suspend fun getLegacyDistricts(provinceCode: String): Result<List<District>> =
        cached("old/provinces/$provinceCode/districts") {
            networkClient.safeCall { api.getLegacyDistricts(provinceCode) }.mapList { it.toDomain() }
        }

    override suspend fun getLegacyWards(districtCode: String): Result<List<Ward>> =
        cached("old/districts/$districtCode/wards") {
            networkClient.safeCall { api.getLegacyWards(districtCode) }.mapList { it.toDomain() }
        }

    /** Trả giá trị còn hạn trong cache; nếu không thì gọi [fetch] và lưu khi thành công. */
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(key: String, fetch: suspend () -> Result<T>): Result<T> {
        val now = clock.elapsedMillis()
        val hit = cache[key]
        if (hit != null && now - hit.storedAtMillis < ttlMillis) {
            return Result.success(hit.value as T)
        }
        return fetch().onSuccess { cache[key] = CacheEntry(it, clock.elapsedMillis()) }
    }

    private fun <D : Any, T : Any> NetworkResult<ItemsDto<D>>.mapList(
        transform: (D) -> T,
    ): Result<List<T>> = when (this) {
        is NetworkResult.Success -> Result.success(data.items.map(transform))
        is NetworkResult.Failure -> Result.failure(NetworkException(error))
    }

    private companion object {
        const val MAX_QUERY_LENGTH = 100
        const val DEFAULT_TTL_MILLIS = 24L * 60 * 60 * 1000
    }
}

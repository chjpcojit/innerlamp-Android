package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.ElapsedClock
import com.example.innerlamp.data.remote.api.AdministrativeUnitApiService
import com.example.innerlamp.data.remote.dto.DistrictDto
import com.example.innerlamp.data.remote.dto.ItemsDto
import com.example.innerlamp.data.remote.dto.ProvinceDto
import com.example.innerlamp.data.remote.dto.WardDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdministrativeUnitRepositoryImplTest {

    private class FakeClock(var now: Long = 0L) : ElapsedClock {
        override fun elapsedMillis(): Long = now
    }

    private class FakeNetworkClient : NetworkClient {
        var failure: AppError? = null

        override fun <T : Any> createService(clazz: Class<T>): T = error("không dùng")

        override suspend fun <T : Any> safeCall(
            block: suspend () -> ApiResponse<T>,
        ): NetworkResult<T> =
            failure?.let { NetworkResult.Failure(it) } ?: NetworkResult.Success(block().data!!)
    }

    private class FakeApi : AdministrativeUnitApiService {
        var provinceCalls = 0
        var wardCalls = 0
        var lastWardQuery: String? = "chưa gọi"

        override suspend fun getProvinces(): ApiResponse<ItemsDto<ProvinceDto>> {
            provinceCalls++
            return ApiResponse(
                success = true,
                data = ItemsDto(listOf(ProvinceDto("79", "Hồ Chí Minh", "Thành phố Hồ Chí Minh", "city"))),
            )
        }

        override suspend fun getWards(
            provinceCode: String,
            query: String?,
        ): ApiResponse<ItemsDto<WardDto>> {
            wardCalls++
            lastWardQuery = query
            return ApiResponse(
                success = true,
                data = ItemsDto(listOf(WardDto("26737", "Tân Định", "Phường Tân Định", "ward", provinceCode))),
            )
        }

        override suspend fun getLegacyProvinces(): ApiResponse<ItemsDto<ProvinceDto>> = getProvinces()

        override suspend fun getLegacyDistricts(provinceCode: String): ApiResponse<ItemsDto<DistrictDto>> =
            ApiResponse(
                success = true,
                data = ItemsDto(listOf(DistrictDto("760", "Quận 1", "Quận 1", "district", provinceCode))),
            )

        override suspend fun getLegacyWards(districtCode: String): ApiResponse<ItemsDto<WardDto>> =
            ApiResponse(
                success = true,
                data = ItemsDto(
                    listOf(WardDto("26734", "Bến Nghé", "Phường Bến Nghé", "ward", "79", districtCode)),
                ),
            )
    }

    private val api = FakeApi()
    private val network = FakeNetworkClient()
    private val clock = FakeClock()
    private val repo = AdministrativeUnitRepositoryImpl(api, network, clock, ttlMillis = 1_000L)

    @Test
    fun getProvinces_cachesWithinTtl_thenRefetchesAfterExpiry() = runTest {
        repo.getProvinces()
        clock.now = 999L
        repo.getProvinces()
        assertEquals(1, api.provinceCalls)

        clock.now = 1_000L
        repo.getProvinces()
        assertEquals(2, api.provinceCalls)
    }

    @Test
    fun getProvinces_mapsDtoToDomain() = runTest {
        val province = repo.getProvinces().getOrThrow().single()

        assertEquals("79", province.code)
        assertEquals("Thành phố Hồ Chí Minh", province.fullName)
        assertEquals("city", province.type)
    }

    @Test
    fun getWards_withoutQuery_isCachedPerProvince() = runTest {
        repo.getWards("79")
        repo.getWards("79", query = "  ")
        assertEquals(1, api.wardCalls)
        assertNull(api.lastWardQuery)

        repo.getWards("01")
        assertEquals(2, api.wardCalls)
    }

    @Test
    fun getWards_withQuery_isNotCached() = runTest {
        repo.getWards("79", query = "Tân")
        repo.getWards("79", query = "Tân")

        assertEquals(2, api.wardCalls)
        assertEquals("Tân", api.lastWardQuery)
    }

    @Test
    fun failure_isNotCached() = runTest {
        network.failure = AppError.NoConnection
        assertTrue(repo.getProvinces().isFailure)

        network.failure = null
        assertTrue(repo.getProvinces().isSuccess)
        assertEquals(1, api.provinceCalls)
    }

    @Test
    fun legacyWards_carryDistrictCode() = runTest {
        val ward = repo.getLegacyWards("760").getOrThrow().single()

        assertEquals("760", ward.districtCode)
    }
}

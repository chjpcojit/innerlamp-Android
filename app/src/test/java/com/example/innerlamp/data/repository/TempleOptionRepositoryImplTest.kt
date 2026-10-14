package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkException
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.data.remote.api.TempleApiService
import com.example.innerlamp.data.remote.dto.TempleOptionDto
import com.example.innerlamp.data.remote.dto.TempleOptionPageDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TempleOptionRepositoryImplTest {

    /** [NetworkClient] giả: chạy thẳng block, lỗi được dựng sẵn qua [failure]. */
    private class FakeNetworkClient(private val failure: AppError? = null) : NetworkClient {
        override fun <T : Any> createService(clazz: Class<T>): T = error("không dùng")

        override suspend fun <T : Any> safeCall(
            block: suspend () -> ApiResponse<T>,
        ): NetworkResult<T> =
            failure?.let { NetworkResult.Failure(it) } ?: NetworkResult.Success(block().data!!)
    }

    private class FakeTempleApi : TempleApiService {
        var lastPage: Int? = null
        var lastQuery: String? = "chưa gọi"

        override suspend fun getOptions(page: Int, query: String?): ApiResponse<TempleOptionPageDto> {
            lastPage = page
            lastQuery = query
            return ApiResponse(
                success = true,
                data = TempleOptionPageDto(
                    items = listOf(TempleOptionDto("t1", "Chùa Một Cột", "Hà Nội", "01")),
                    page = page,
                    pageSize = 100,
                    total = 250,
                    totalPages = 3,
                ),
            )
        }
    }

    @Test
    fun getOptions_mapsDtoToDomain_andComputesHasMore() = runTest {
        val api = FakeTempleApi()
        val repo = TempleOptionRepositoryImpl(api, FakeNetworkClient())

        val page = repo.getOptions(page = 2).getOrThrow()

        assertEquals(2, api.lastPage)
        assertEquals("Chùa Một Cột", page.items.single().name)
        assertEquals("Hà Nội", page.items.single().provinceName)
        assertEquals("01", page.items.single().provinceCode)
        assertTrue(page.hasMore)
    }

    @Test
    fun getOptions_blankQuery_isOmitted() = runTest {
        val api = FakeTempleApi()
        val repo = TempleOptionRepositoryImpl(api, FakeNetworkClient())

        repo.getOptions(query = "   ")

        assertNull(api.lastQuery)
    }

    @Test
    fun getOptions_query_isTrimmedAndCappedAt100() = runTest {
        val api = FakeTempleApi()
        val repo = TempleOptionRepositoryImpl(api, FakeNetworkClient())

        repo.getOptions(query = "  " + "a".repeat(150) + "  ")

        assertEquals("a".repeat(100), api.lastQuery)
    }

    @Test
    fun getOptions_failure_isWrappedInNetworkException() = runTest {
        val repo = TempleOptionRepositoryImpl(FakeTempleApi(), FakeNetworkClient(AppError.NoConnection))

        val exception = repo.getOptions().exceptionOrNull()

        assertTrue(exception is NetworkException)
        assertEquals(AppError.NoConnection, (exception as NetworkException).error)
    }
}

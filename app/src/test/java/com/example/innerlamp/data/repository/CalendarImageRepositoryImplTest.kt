package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.remote.api.CalendarApiService
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import com.example.innerlamp.data.remote.dto.CalendarImageDto
import com.example.innerlamp.domain.repository.CalendarImageError
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarImageRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    /** Runs the block; a prepared [failure] short-circuits it (like the real client's mapping). */
    private class FakeClient(var failure: AppError? = null) : NetworkClient {
        var calls = 0
        override fun <T : Any> createService(clazz: Class<T>): T = error("unused")
        override suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T> {
            calls++
            failure?.let { return NetworkResult.Failure(it) }
            return NetworkResult.Success(block().data!!)
        }
    }

    private class FakeApi : CalendarApiService {
        val dates = mutableListOf<String>()
        var make: (String) -> CalendarImageDto = { Companion.dto(it) }
        override suspend fun getEvents(year: Int): ApiResponse<CalendarEventsPageDto> = error("unused")
        override suspend fun getImage(date: String): ApiResponse<CalendarImageDto> {
            dates += date
            return ApiResponse(true, data = make(date))
        }
    }

    private companion object {
        fun dto(date: String) = CalendarImageDto(
            date = date,
            id = JsonPrimitive(7),
            imageUrl = "https://img.example/$date.jpg",
            author = " Jane Doe ",
            license = "CC BY-SA 4.0",
            licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/",
            sourceUrl = "",
        )
    }

    private val client = FakeClient()
    private val api = FakeApi()
    private fun repo() = CalendarImageRepositoryImpl(api, client, dispatchers)
    private val date = LocalDate.of(2026, 10, 20)

    private fun error(r: Result<*>) = r.exceptionOrNull() as CalendarImageError

    @Test
    fun success_sendsExplicitIsoDate_andMapsFields() = runTest {
        val image = repo().load(date).getOrThrow()
        assertEquals(listOf("2026-10-20"), api.dates)
        assertEquals(date, image.date)
        assertEquals("7", image.id)
        assertEquals("Jane Doe", image.author)
        assertEquals("CC BY-SA 4.0", image.license)
        assertEquals(null, image.sourceUrl)
    }

    @Test
    fun sameDate_isNotRefetched() = runTest {
        val repo = repo()
        val first = repo.load(date).getOrThrow()
        val second = repo.load(date).getOrThrow()
        assertSame(first, second)
        assertEquals(1, client.calls)
    }

    @Test
    fun differentDates_eachCallTheServer() = runTest {
        val repo = repo()
        repo.load(date).getOrThrow()
        repo.load(date.plusDays(1)).getOrThrow()
        assertEquals(2, client.calls)
    }

    @Test
    fun noConnectionAndTimeout_mapToOffline() = runTest {
        client.failure = AppError.NoConnection
        assertEquals(CalendarImageError.Offline, error(repo().load(date)))
        client.failure = AppError.Timeout
        assertEquals(CalendarImageError.Offline, error(repo().load(date)))
    }

    @Test
    fun invalidDateApiError_mapsToInvalidDate() = runTest {
        client.failure = AppError.ApiError("INVALID_DATE", "bad", "validation")
        assertEquals(CalendarImageError.InvalidDate, error(repo().load(date)))
    }

    @Test
    fun otherErrors_mapToFailed_andAreNotCached() = runTest {
        client.failure = AppError.HttpError(500)
        val repo = repo()
        assertEquals(CalendarImageError.Failed, error(repo.load(date)))
        client.failure = AppError.Serialization
        assertEquals(CalendarImageError.Failed, error(repo.load(date)))
        client.failure = null
        repo.load(date).getOrThrow()
        assertEquals(3, client.calls)
    }

    @Test
    fun badDateInPayload_isFailed() = runTest {
        api.make = { dto("not-a-date") }
        assertEquals(CalendarImageError.Failed, error(repo().load(date)))
    }
}

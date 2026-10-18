package com.example.innerlamp.data.repository

import com.example.innerlamp.core.database.calendar.CalendarYearCacheDao
import com.example.innerlamp.core.database.calendar.CalendarYearCacheEntity
import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.JsonProvider
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.remote.api.CalendarApiService
import com.example.innerlamp.data.remote.dto.CalendarEventDto
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import com.example.innerlamp.data.remote.dto.CalendarGroupDto
import com.example.innerlamp.data.remote.dto.CalendarLunarDateDto
import com.example.innerlamp.data.remote.dto.CalendarOccurrenceDto
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Query

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarEventRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    // 2026-10-17 in Vietnam time. / 17/10/2026 giờ Việt Nam.
    private val clock = Clock.fixed(Instant.parse("2026-10-17T03:00:00Z"), ZoneOffset.UTC)

    private class FakeDao : CalendarYearCacheDao() {
        val rows = linkedMapOf<Int, CalendarYearCacheEntity>()
        var failOnWrite = false
        override suspend fun upsert(entity: CalendarYearCacheEntity) { rows[entity.year] = entity }
        override suspend fun get(year: Int) = rows[year]
        override suspend fun deleteBefore(year: Int) { rows.keys.removeAll { it < year } }
        override suspend fun replaceYear(entity: CalendarYearCacheEntity, keepFromYear: Int) {
            if (failOnWrite) error("disk full")
            upsert(entity)
            deleteBefore(keepFromYear)
        }
    }

    /** Fake client: runs the block; a prepared [failure] short-circuits it (like the real one's mapping). */
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
        val years = mutableListOf<Int>()
        override suspend fun getEvents(year: Int): ApiResponse<CalendarEventsPageDto> {
            years += year
            return ApiResponse(true, data = page(year))
        }
    }

    private companion object {
        fun page(year: Int) = CalendarEventsPageDto(
            year = year,
            groups = listOf(CalendarGroupDto("buddhist", "Phật giáo")),
            events = listOf(CalendarEventDto(code = "phat-dan", groups = listOf("buddhist"), isMajor = true, sortOrder = 17, recurrence = "lunar")),
            occurrences = listOf(CalendarOccurrenceDto("phat-dan", "$year-05-31", CalendarLunarDateDto(15, 4, year, false))),
        )
    }

    private val dao = FakeDao()
    private val client = FakeClient()
    private val api = FakeApi()
    private val logged = mutableListOf<String>()

    private fun repo() = CalendarEventRepositoryImpl(
        api, client, dao, JsonProvider.json, dispatchers, clock, logger = { m, _ -> logged += m },
    )

    private fun seedCopy(year: Int) {
        dao.rows[year] = CalendarYearCacheEntity(
            year, JsonProvider.json.encodeToString(CalendarEventsPageDto.serializer(), page(year)), 1L,
        )
    }

    private fun failure(r: Result<*>) = r.exceptionOrNull() as CalendarLoadError

    @Test
    fun success_writesRoom_deletesOlderYears_andReturnsNetwork() = runTest {
        seedCopy(2025)
        seedCopy(2026)
        val r = repo().load(2026).getOrThrow()
        assertEquals(CalendarYearSource.Network, r.source)
        assertEquals(false, r.mayBeOutOfDate)
        assertEquals(listOf(2026), dao.rows.keys.toList())
        assertEquals(clock.millis(), dao.rows.getValue(2026).fetchedAtEpochMs)
        assertEquals("phat-dan", r.year.entries.single().event.code)
    }

    @Test
    fun loadedYear_isNotRefetched_andConcurrentCallsShareOne() = runTest {
        val repo = repo()
        val first = repo.load(2026).getOrThrow().year
        val second = repo.load(2026).getOrThrow().year
        assertSame(first, second)
        assertSame(first, repo.cachedInMemory(2026))
        assertEquals(1, client.calls)
        assertEquals(listOf(2026), api.years)
    }

    @Test
    fun deviceCopy_readsRoomWithoutNetwork() = runTest {
        seedCopy(2027)
        assertEquals(2027, repo().deviceCopy(2027)!!.year)
        assertNull(repo().deviceCopy(2028))
        assertEquals(0, client.calls)
    }

    @Test
    fun roomWriteFailure_isLogged_andDataStillReturned() = runTest {
        dao.failOnWrite = true
        val r = repo().load(2026)
        assertTrue(r.isSuccess)
        assertEquals(listOf("Write calendar copy 2026 failed"), logged)
    }

    @Test
    fun pastYear_isNotStored() = runTest {
        repo().load(2025).getOrThrow()
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun anyFailureWithCopy_keepsCopyAndFlagsOutOfDate() = runTest {
        val errors = listOf(
            AppError.NoConnection, AppError.Timeout, AppError.Unauthorized, AppError.Serialization,
            AppError.HttpError(503), AppError.Unknown(), AppError.ApiError("RATE_LIMITED", "m", "business"),
            AppError.HttpError(429, retryAfterSeconds = 3),
        )
        for (e in errors) {
            seedCopy(2026)
            client.failure = e
            val r = repo().load(2026).getOrThrow()
            assertEquals(e.toString(), CalendarYearSource.DeviceCopy, r.source)
            assertTrue(e.toString(), r.mayBeOutOfDate)
        }
    }

    @Test
    fun failureWithoutCopy_mapsPerErrorToUiTable() = runTest {
        fun map(e: AppError) = failure(run { client.failure = e; kotlinx.coroutines.runBlocking { repo().load(2026) } })
        assertEquals(CalendarLoadError.Offline, map(AppError.NoConnection))
        assertEquals(CalendarLoadError.Offline, map(AppError.Timeout))
        assertEquals(CalendarLoadError.Failed, map(AppError.HttpError(503)))
        assertEquals(CalendarLoadError.Failed, map(AppError.ApiError("INVALID_YEAR", "m", "business")))
        assertEquals(CalendarLoadError.Failed, map(AppError.Serialization))
        assertEquals(CalendarLoadError.Failed, map(AppError.Unknown()))
        assertEquals(CalendarLoadError.Failed, map(AppError.NotConfigured))
        // 401 is an unexpected generic failure; nothing touches the session. / 401 chỉ là lỗi chung.
        assertEquals(CalendarLoadError.Failed, map(AppError.Unauthorized))
    }

    private fun rateLimited(e: AppError): CalendarLoadError.RateLimited {
        client.failure = e
        return failure(kotlinx.coroutines.runBlocking { repo().load(2026) }) as CalendarLoadError.RateLimited
    }

    @Test
    fun rateLimited_readsDetailsFirst_thenHeader_thenNull() {
        val details = mapOf<String, JsonElement>("retry_after_seconds" to JsonPrimitive(12))
        assertEquals(12, rateLimited(AppError.ApiError("RATE_LIMITED", "m", "business", details, retryAfterSeconds = 99)).retryAfterSeconds)
        assertEquals(99, rateLimited(AppError.ApiError("RATE_LIMITED", "m", "business", null, retryAfterSeconds = 99)).retryAfterSeconds)
        assertEquals(7, rateLimited(AppError.HttpError(429, retryAfterSeconds = 7)).retryAfterSeconds)
        assertNull(rateLimited(AppError.ApiError("RATE_LIMITED", "m", "business")).retryAfterSeconds)
        assertNull(rateLimited(AppError.HttpError(429)).retryAfterSeconds)
    }

    @Test
    fun rateLimited_isNotRetriedAutomatically_andNextCallIsUserDriven() = runTest {
        client.failure = AppError.HttpError(429, retryAfterSeconds = 4)
        val repo = repo()
        assertEquals(CalendarLoadError.RateLimited(4), failure(repo.load(2026)))
        assertEquals(1, client.calls)
        client.failure = null
        assertTrue(repo.load(2026).isSuccess) // the user's "Thử lại"
        assertEquals(2, client.calls)
    }

    @Test
    fun calendarEndpoint_isPublic_noAuthenticatedAnnotation_noApiKeyHeader() {
        val method = CalendarApiService::class.java.getMethod("getEvents", Int::class.javaPrimitiveType, kotlin.coroutines.Continuation::class.java)
        assertNull(method.getAnnotation(com.example.innerlamp.core.network.auth.Authenticated::class.java))
        assertEquals("v1/calendar/events", method.getAnnotation(GET::class.java)!!.value)
        assertTrue(method.parameters[0].isAnnotationPresent(Query::class.java))
        // No @Header / @Headers on the endpoint. / Không có @Header / @Headers.
        assertTrue(method.annotations.none { it.annotationClass.simpleName in setOf("Headers", "Header") })
        assertTrue(method.parameters.none { p -> p.annotations.any { it.annotationClass.simpleName == "Header" } })
    }
}

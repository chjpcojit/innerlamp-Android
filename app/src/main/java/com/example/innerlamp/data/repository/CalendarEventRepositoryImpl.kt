package com.example.innerlamp.data.repository

import com.example.innerlamp.core.database.calendar.CalendarYearCacheDao
import com.example.innerlamp.core.database.calendar.CalendarYearCacheEntity
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.CalendarApiService
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import com.example.innerlamp.domain.calendar.CalendarTime
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearLoad
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarEventRepository
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * [CalendarEventRepository] over `GET /v1/calendar/events`: memory → Room → `safeCall`. Hold ONE
 * instance for the whole app (it owns the in-memory cache). /
 * Cài đặt [CalendarEventRepository]: bộ nhớ → Room → `safeCall`. Giữ MỘT instance cho cả app (giữ cache
 * bộ nhớ).
 *
 * - A year loaded successfully is never refetched in the session; concurrent loads of one year share
 *   one call (per-year lock) to stay under the IP rate limit (AC31).
 * - Success → write Room + delete older years in one transaction (AC22); a Room failure is logged and
 *   never surfaced.
 * - Failure → the device copy with `mayBeOutOfDate`, else a [CalendarLoadError]. 429 is split out of
 *   the generic HTTP error. A 401 is just [CalendarLoadError.Failed]: the session is not touched.
 * - Never retries automatically.
 *
 * Năm đã tải thành công không tải lại trong phiên; tải đồng thời cùng một năm dùng chung một lần gọi.
 * Thành công → ghi Room + xoá năm cũ trong một transaction; lỗi Room chỉ log. Thất bại → bản trên máy
 * kèm `mayBeOutOfDate`, không thì [CalendarLoadError]. 429 tách riêng; 401 chỉ là Failed, không đụng
 * phiên. Không tự retry.
 */
class CalendarEventRepositoryImpl(
    private val api: CalendarApiService,
    private val networkClient: NetworkClient,
    private val dao: CalendarYearCacheDao,
    private val json: Json,
    private val dispatchers: DispatcherProvider,
    private val clock: Clock = Clock.systemUTC(),
    private val logger: (message: String, cause: Throwable) -> Unit = { _, _ -> },
) : CalendarEventRepository {

    private val memory = ConcurrentHashMap<Int, CalendarYear>()
    private val locks = ConcurrentHashMap<Int, Mutex>()

    override fun cachedInMemory(year: Int): CalendarYear? = memory[year]

    override suspend fun deviceCopy(year: Int): CalendarYear? = memory[year] ?: readRoom(year)

    override suspend fun load(year: Int): Result<CalendarYearLoad> =
        locks.getOrPut(year) { Mutex() }.withLock {
            memory[year]?.let {
                return@withLock Result.success(CalendarYearLoad(it, CalendarYearSource.Network, false))
            }
            when (val result = networkClient.safeCall { api.getEvents(year) }) {
                is NetworkResult.Success -> {
                    val domain = result.data.toDomain()
                    persist(year, result.data)
                    memory[year] = domain
                    Result.success(CalendarYearLoad(domain, CalendarYearSource.Network, false))
                }
                is NetworkResult.Failure -> {
                    val copy = readRoom(year)
                    if (copy != null) {
                        Result.success(CalendarYearLoad(copy, CalendarYearSource.DeviceCopy, true))
                    } else {
                        Result.failure(result.error.toLoadError())
                    }
                }
            }
        }

    private suspend fun readRoom(year: Int): CalendarYear? = withContext(dispatchers.io) {
        try {
            dao.get(year)?.let {
                json.decodeFromString(CalendarEventsPageDto.serializer(), it.payloadJson).toDomain()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger("Read calendar copy $year failed", e)
            null
        }
    }

    private suspend fun persist(year: Int, dto: CalendarEventsPageDto) {
        val currentYear = CalendarTime.todayInVietnam(clock).year
        // A past year would be deleted at once, so do not store it. / Năm đã qua sẽ bị xoá ngay nên không lưu.
        if (year < currentYear) return
        withContext(dispatchers.io) {
            try {
                val entity = CalendarYearCacheEntity(
                    year = year,
                    payloadJson = json.encodeToString(CalendarEventsPageDto.serializer(), dto),
                    fetchedAtEpochMs = clock.millis(),
                )
                dao.replaceYear(entity, keepFromYear = currentYear)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger("Write calendar copy $year failed", e)
            }
        }
    }

    private fun AppError.toLoadError(): CalendarLoadError = when (this) {
        AppError.NoConnection, AppError.Timeout -> CalendarLoadError.Offline
        is AppError.ApiError ->
            if (code == RATE_LIMITED) CalendarLoadError.RateLimited(retryAfter(details, retryAfterSeconds))
            else CalendarLoadError.Failed
        is AppError.HttpError ->
            if (code == HTTP_TOO_MANY_REQUESTS) CalendarLoadError.RateLimited(retryAfter(null, retryAfterSeconds))
            else CalendarLoadError.Failed
        else -> CalendarLoadError.Failed
    }

    /** `details.retry_after_seconds` first, then the `Retry-After` header, else null. */
    private fun retryAfter(
        details: Map<String, kotlinx.serialization.json.JsonElement>?,
        header: Int?,
    ): Int? = details?.get("retry_after_seconds")
        ?.let { runCatching { it.jsonPrimitive.intOrNull }.getOrNull() }
        ?: header

    private companion object {
        const val RATE_LIMITED = "RATE_LIMITED"
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}

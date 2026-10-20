package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.CalendarApiService
import com.example.innerlamp.domain.model.CalendarDayImage
import com.example.innerlamp.domain.repository.CalendarImageError
import com.example.innerlamp.domain.repository.CalendarImageRepository
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * [CalendarImageRepository] over `GET /v1/calendar/image?date=`: memory → `safeCall`. Always sends an
 * explicit `date` (the response is cacheable only then). Hold ONE instance (it owns the memory cache).
 * The image URL itself is cached by Coil. /
 * Cài đặt [CalendarImageRepository]: bộ nhớ → `safeCall`. Luôn gửi `date` tường minh (chỉ khi đó response
 * mới cache được). Giữ MỘT instance (giữ cache bộ nhớ). Bản thân ảnh do Coil cache.
 */
class CalendarImageRepositoryImpl(
    private val api: CalendarApiService,
    private val networkClient: NetworkClient,
    private val dispatchers: DispatcherProvider,
) : CalendarImageRepository {

    private val memory = ConcurrentHashMap<LocalDate, CalendarDayImage>()

    override suspend fun load(date: LocalDate): Result<CalendarDayImage> {
        memory[date]?.let { return Result.success(it) }
        return when (val result = networkClient.safeCall { api.getImage(date.toString()) }) {
            is NetworkResult.Success -> withContext(dispatchers.default) {
                try {
                    val image = result.data.toDomain()
                    memory[date] = image
                    Result.success(image)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Unparseable payload → generic failure. / Payload hỏng → lỗi chung.
                    Result.failure(CalendarImageError.Failed)
                }
            }
            is NetworkResult.Failure -> Result.failure(result.error.toImageError())
        }
    }

    private fun AppError.toImageError(): CalendarImageError = when (this) {
        AppError.NoConnection, AppError.Timeout -> CalendarImageError.Offline
        is AppError.ApiError ->
            if (code == INVALID_DATE) CalendarImageError.InvalidDate else CalendarImageError.Failed
        else -> CalendarImageError.Failed
    }

    private companion object {
        const val INVALID_DATE = "INVALID_DATE"
    }
}

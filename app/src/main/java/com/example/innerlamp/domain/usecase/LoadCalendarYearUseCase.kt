package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.CalendarTime
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearLoad
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarEventRepository
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Loads one calendar year (AC2, AC12, AC13, AC23). The year must be in
 * [current Vietnam year, [CalendarTime.MAX_YEAR]]. /
 * Tải một năm lịch. Năm phải nằm trong [năm hiện tại theo giờ VN, 2100].
 *
 * Threading is owned by the repository (it switches to its injected `DispatcherProvider`). /
 * Việc chuyển luồng do repository đảm nhiệm (qua `DispatcherProvider` được inject).
 */
class LoadCalendarYearUseCase(
    private val repository: CalendarEventRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    /** The current year in Vietnam time. / Năm hiện tại theo giờ Việt Nam. */
    fun currentYear(): Int = CalendarTime.todayInVietnam(clock).year

    /** The year after [year], or null at [CalendarTime.MAX_YEAR]. / Năm kế tiếp, null ở 2100. */
    fun nextYear(year: Int): Int? = (year + 1).takeIf { it <= CalendarTime.MAX_YEAR }

    /** On-device copy (memory or Room) to show at once, without the out-of-date line. */
    suspend fun deviceCopy(year: Int): CalendarYear? =
        if (isAllowed(year)) repository.deviceCopy(year) else null

    /** Loads [year] (memory → API, falling back to the device copy). Never retries by itself. */
    suspend operator fun invoke(year: Int = currentYear()): Result<CalendarYearLoad> {
        if (!isAllowed(year)) {
            return Result.failure(IllegalArgumentException("year $year is out of range"))
        }
        return repository.load(year)
    }

    /**
     * Progressive load (AC23): emits the device copy first (when one exists and the year is not yet
     * loaded this session), then the final [invoke] result. /
     * Tải từng bước: phát bản trên máy trước (nếu có và năm chưa tải trong phiên), rồi kết quả [invoke].
     */
    fun observe(year: Int = currentYear()): Flow<Result<CalendarYearLoad>> = flow {
        if (isAllowed(year) && repository.cachedInMemory(year) == null) {
            repository.deviceCopy(year)?.let {
                emit(Result.success(CalendarYearLoad(it, CalendarYearSource.DeviceCopy, mayBeOutOfDate = false)))
            }
        }
        emit(invoke(year))
    }

    private fun isAllowed(year: Int): Boolean = year in currentYear()..CalendarTime.MAX_YEAR
}

package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearLoad

/**
 * Domain errors of [CalendarEventRepository.load] (carried by `Result.failure`). /
 * Lỗi miền của [CalendarEventRepository.load] (nằm trong `Result.failure`).
 */
sealed class CalendarLoadError(message: String) : Exception(message) {
    /** No connection / timeout. / Không có mạng / quá thời gian. */
    data object Offline : CalendarLoadError("offline")

    /** HTTP 429; [retryAfterSeconds] from `details.retry_after_seconds`, else `Retry-After`, else null. */
    data class RateLimited(val retryAfterSeconds: Int?) : CalendarLoadError("rate limited")

    /** Anything else (5xx, unexpected 401, parse error, ...). / Mọi lỗi còn lại. */
    data object Failed : CalendarLoadError("failed")
}

/**
 * Source of calendar years: memory, then the device copy, then the public API. /
 * Nguồn dữ liệu lịch theo năm: bộ nhớ, rồi bản trên máy, rồi API công khai.
 */
interface CalendarEventRepository {

    /** The year if already loaded successfully in this session (no I/O). / Năm đã tải thành công trong phiên. */
    fun cachedInMemory(year: Int): CalendarYear?

    /** The year from memory or from the on-device copy; null when neither exists. / Bản trong bộ nhớ hoặc trên máy. */
    suspend fun deviceCopy(year: Int): CalendarYear?

    /**
     * Loads [year]. A year already loaded in this session is returned without a network call. On
     * network failure: the device copy with `mayBeOutOfDate = true` when there is one, otherwise
     * `Result.failure(CalendarLoadError)`. Never retries by itself. /
     * Tải [year]. Năm đã tải trong phiên trả ngay, không gọi mạng. Mạng lỗi: có bản trên máy thì trả
     * kèm `mayBeOutOfDate = true`, không thì `Result.failure(CalendarLoadError)`. Không tự retry.
     */
    suspend fun load(year: Int): Result<CalendarYearLoad>
}

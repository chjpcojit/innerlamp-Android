package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.CalendarDayImage
import java.time.LocalDate

/**
 * Domain errors of [CalendarImageRepository.load] (carried by `Result.failure`). /
 * Lỗi miền của [CalendarImageRepository.load] (nằm trong `Result.failure`).
 */
sealed class CalendarImageError(message: String) : Exception(message) {
    /** No connection / timeout. / Không có mạng / quá thời gian. */
    data object Offline : CalendarImageError("offline")

    /** `400 INVALID_DATE` (bad format or outside 1900..2100). / Ngày sai định dạng hoặc ngoài 1900..2100. */
    data object InvalidDate : CalendarImageError("invalid date")

    /** Anything else (5xx, parse error, ...). / Mọi lỗi còn lại. */
    data object Failed : CalendarImageError("failed")
}

/**
 * Source of the daily Buddha image (public API, no auth). /
 * Nguồn ảnh Phật theo ngày (API công khai, không cần đăng nhập).
 */
interface CalendarImageRepository {

    /**
     * Image for [date]. A date already loaded in this session is returned without a network call
     * (the image of a date never changes). Never retries by itself. /
     * Ảnh của [date]. Ngày đã tải trong phiên trả ngay, không gọi mạng (ảnh của một ngày không đổi).
     * Không tự retry.
     */
    suspend fun load(date: LocalDate): Result<CalendarDayImage>
}

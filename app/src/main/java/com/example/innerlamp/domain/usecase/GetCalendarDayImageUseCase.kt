package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.CalendarDayImage
import com.example.innerlamp.domain.repository.CalendarImageError
import com.example.innerlamp.domain.repository.CalendarImageRepository
import java.time.LocalDate

/**
 * Loads the Buddha image of a day. Dates outside the server range (1900..2100) fail locally with
 * [CalendarImageError.InvalidDate], saving a call that is certain to return 400. /
 * Tải ảnh Phật của một ngày. Ngày ngoài 1900..2100 lỗi ngay tại máy, đỡ một lần gọi chắc chắn bị 400.
 */
class GetCalendarDayImageUseCase(private val repository: CalendarImageRepository) {

    suspend operator fun invoke(date: LocalDate): Result<CalendarDayImage> =
        if (date.year !in MIN_YEAR..MAX_YEAR) {
            Result.failure(CalendarImageError.InvalidDate)
        } else {
            repository.load(date)
        }

    private companion object {
        const val MIN_YEAR = 1900
        const val MAX_YEAR = 2100
    }
}

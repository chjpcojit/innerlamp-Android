package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.repository.CalendarEventRepository
import java.time.LocalDate

/**
 * An occurrence together with the display groups of its event (names come from the server). /
 * Occurrence kèm các nhóm hiển thị của sự kiện (tên lấy từ server).
 */
data class EventOccurrenceDetail(val entry: EventOccurrence, val groups: List<CalendarGroup>)

/**
 * Finds `(eventCode, solarDate)` for the detail screen (AC14, AC18): year from memory, then device
 * copy, then loaded if absent. `Result.success(null)` = year available but no such occurrence. /
 * Tìm `(eventCode, solarDate)` cho màn chi tiết: năm từ bộ nhớ, rồi bản trên máy, rồi tải nếu thiếu.
 * `Result.success(null)` = có dữ liệu năm nhưng không có occurrence đó.
 */
class GetEventOccurrenceUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(eventCode: String, solarDate: LocalDate): Result<EventOccurrence?> =
        detail(eventCode, solarDate).map { it?.entry }

    /** Same lookup, plus the groups of the event's year. / Cùng tra cứu, kèm các nhóm của năm đó. */
    suspend fun detail(eventCode: String, solarDate: LocalDate): Result<EventOccurrenceDetail?> {
        val year = solarDate.year
        val data = repository.cachedInMemory(year)
            ?: repository.deviceCopy(year)
            ?: repository.load(year).getOrElse { return Result.failure(it) }.year
        val found = data.entries.firstOrNull {
            it.event.code == eventCode && it.occurrence.solarDate == solarDate
        } ?: return Result.success(null)
        val groups = found.event.groupCodes.mapNotNull { code -> data.groups.firstOrNull { it.code == code } }
        return Result.success(EventOccurrenceDetail(found, groups))
    }
}

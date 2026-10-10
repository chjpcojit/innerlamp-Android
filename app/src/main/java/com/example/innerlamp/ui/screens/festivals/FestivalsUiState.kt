package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.model.FestivalEntry
import com.example.innerlamp.domain.model.FestivalMonth
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Tab lọc của màn "Lịch lễ Phật giáo" (prototype `festTabs`). */
enum class FestivalFilter(val label: String) {
    All("Tất cả"),
    Major("Lễ lớn"),
    Fasting("Rằm & Mùng Một"),
}

/** Một dòng của danh sách phẳng: tiêu đề tháng hoặc một ngày lễ. */
sealed interface FestivalRow {
    data class Header(val year: Int, val month: Int) : FestivalRow
    data class Item(val entry: FestivalEntry) : FestivalRow
}

/** Nhãn đếm ngược bên phải tiêu đề lễ. */
data class FestivalCountdown(val label: String, val isToday: Boolean, val isSoon: Boolean, val isPast: Boolean)

/**
 * State render cho màn "Lịch lễ Phật giáo".
 *
 * @property today dùng để đếm ngược và chọn tháng cuộn tới.
 */
data class FestivalsUiState(
    val months: List<FestivalMonth>,
    val today: LocalDate,
    val filter: FestivalFilter = FestivalFilter.All,
) {
    private val visibleMonths: List<FestivalMonth>
        get() = months.mapNotNull { m ->
            val kept = m.entries.filter { it.matches(filter) }
            if (kept.isEmpty()) null else m.copy(entries = kept)
        }

    val rows: List<FestivalRow>
        get() = visibleMonths.flatMap { m ->
            listOf<FestivalRow>(FestivalRow.Header(m.year, m.month)) + m.entries.map { FestivalRow.Item(it) }
        }

    /** Vị trí tiêu đề của tháng hiện tại (hoặc tháng gần nhất sau đó) trong [rows]. */
    val todayRowIndex: Int
        get() {
            val index = rows.indexOfFirst {
                it is FestivalRow.Header && it.year * 100 + it.month >= today.year * 100 + today.monthValue
            }
            return if (index >= 0) index else (rows.lastIndex).coerceAtLeast(0)
        }

    val subtitle: String
        get() {
            val first = months.firstOrNull()?.year ?: today.year
            val last = months.lastOrNull()?.year ?: first
            val days = visibleMonths.sumOf { it.entries.size }
            return "$first – $last · $days ngày"
        }

    fun countdown(date: LocalDate): FestivalCountdown {
        val diff = ChronoUnit.DAYS.between(today, date)
        return when {
            diff < 0 -> FestivalCountdown("Đã qua", isToday = false, isSoon = false, isPast = true)
            diff == 0L -> FestivalCountdown("Hôm nay", isToday = true, isSoon = true, isPast = false)
            diff == 1L -> FestivalCountdown("Ngày mai", isToday = false, isSoon = true, isPast = false)
            else -> FestivalCountdown("còn $diff ngày", isToday = false, isSoon = false, isPast = false)
        }
    }

    private fun FestivalEntry.matches(filter: FestivalFilter) = when (filter) {
        FestivalFilter.All -> true
        FestivalFilter.Major -> event.isFestival
        FestivalFilter.Fasting -> !event.isFestival
    }
}

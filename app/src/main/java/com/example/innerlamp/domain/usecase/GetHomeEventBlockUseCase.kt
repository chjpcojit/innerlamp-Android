package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.CalendarTime
import com.example.innerlamp.domain.calendar.HomeEventBlock
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.EventOccurrence
import com.example.innerlamp.domain.repository.CalendarEventRepository
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.Clock
import java.time.YearMonth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** The outcome of one Home month card. / Kết quả của một thẻ tháng ở Home. */
sealed interface HomeMonthOutcome {
    /** Rows (no countdown) + "+N"; [mayBeOutOfDate] when the device copy stands in (AC24). */
    data class Loaded(
        val shown: List<EventOccurrence>,
        val more: Int,
        val mayBeOutOfDate: Boolean,
    ) : HomeMonthOutcome

    /** The month's year has no data (AC27). / Năm của tháng này không có dữ liệu. */
    data class Failed(val error: CalendarLoadError) : HomeMonthOutcome
}

data class HomeMonthCard(val month: YearMonth, val outcome: HomeMonthOutcome)

/**
 * Home block (AC19, AC27): six months from the current Vietnam-time month, one or two years loaded
 * in parallel, rows chosen by [HomeEventBlock.select]. A failing year only fails its own months. /
 * Khối Home: sáu tháng từ tháng hiện tại (giờ VN), tải một hoặc hai năm song song, chọn dòng bằng
 * [HomeEventBlock.select]. Năm lỗi chỉ làm lỗi các tháng của năm đó.
 */
class GetHomeEventBlockUseCase(
    private val repository: CalendarEventRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    private fun months(): List<YearMonth> {
        val start = CalendarTime.currentMonthInVietnam(clock)
        return (0 until MONTHS).map { start.plusMonths(it.toLong()) }
    }

    /**
     * Cards built only from what is already on the device (memory or Room); null unless every needed
     * year is available. For showing something at once (AC23). /
     * Thẻ dựng chỉ từ dữ liệu đã có trên máy; null nếu thiếu năm nào đó.
     */
    suspend fun fromDeviceCopy(): List<HomeMonthCard>? {
        val months = months()
        val years = months.map { it.year }.distinct()
            .associateWith { repository.cachedInMemory(it) ?: repository.deviceCopy(it) ?: return null }
        return months.map { card(it, years.getValue(it.year), mayBeOutOfDate = false) }
    }

    suspend operator fun invoke(): List<HomeMonthCard> = coroutineScope {
        val months = months()
        val loads = months.map { it.year }.distinct().associateWith { y -> async { repository.load(y) } }
        months.map { m ->
            val result = loads.getValue(m.year).await()
            result.fold(
                onSuccess = { card(m, it.year, it.mayBeOutOfDate) },
                onFailure = {
                    val error = it as? CalendarLoadError ?: CalendarLoadError.Failed
                    HomeMonthCard(m, HomeMonthOutcome.Failed(error))
                },
            )
        }
    }

    private fun card(month: YearMonth, year: CalendarYear, mayBeOutOfDate: Boolean): HomeMonthCard {
        val inMonth = year.entries.filter { YearMonth.from(it.occurrence.solarDate) == month }
        val selection = HomeEventBlock.select(inMonth)
        return HomeMonthCard(month, HomeMonthOutcome.Loaded(selection.shown, selection.more, mayBeOutOfDate))
    }

    private companion object {
        const val MONTHS = 6
    }
}

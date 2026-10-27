package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.BuddhaDetail
import com.example.innerlamp.domain.model.BuddhaViaDay
import com.example.innerlamp.domain.repository.DeityRepository
import java.time.Clock
import java.time.LocalDate

/**
 * Content of the `buddha` screen: the profile from the backend plus the solar date of the next main via
 * (searched in the next 400 days, leap months skipped). The contract has no `next_via` yet, so the app
 * computes it from `lunar_memorial_days`. /
 * Nội dung màn `buddha`: hồ sơ từ backend + ngày dương của lần vía chính kế tiếp (tìm trong 400 ngày tới,
 * bỏ tháng nhuận). Contract chưa có `next_via` nên app tự tính từ `lunar_memorial_days`.
 *
 * @param clock inject để test cố định "hôm nay".
 */
class GetBuddhaDetailUseCase(
    private val repository: DeityRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    suspend operator fun invoke(buddhaId: String): Result<BuddhaDetail> =
        repository.getProfile(buddhaId).map { profile ->
            BuddhaDetail(profile = profile, nextVia = profile.mainVia?.let { nextVia(it, LocalDate.now(clock)) })
        }

    internal fun nextVia(via: BuddhaViaDay, from: LocalDate): LocalDate? =
        (0 until SEARCH_DAYS).asSequence()
            .map { from.plusDays(it.toLong()) }
            .firstOrNull { date ->
                val lunar = LunarCalendar.toLunar(date)
                lunar.day == via.day && lunar.month == via.month && !lunar.isLeapMonth
            }

    private companion object {
        const val SEARCH_DAYS = 400
    }
}

package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.BuddhaDetail
import com.example.innerlamp.domain.model.BuddhaProfile
import java.time.Clock
import java.time.LocalDate

/**
 * Trả về nội dung màn `buddha` (Chi tiết Phật / Bồ Tát): hồ sơ của vị được chọn kèm ngày dương lịch
 * của lần vía kế tiếp (tìm trong 400 ngày tới, bỏ qua tháng nhuận — như prototype `buddhaVals`).
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 */
class GetBuddhaDetailUseCase(
    private val getProfiles: GetBuddhaProfilesUseCase = GetBuddhaProfilesUseCase(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /** Id không khớp (route sai/preview) thì dùng vị đầu tiên, giống `list[0]` của prototype. */
    operator fun invoke(buddhaId: String): BuddhaDetail {
        val profiles = getProfiles()
        val profile = profiles.firstOrNull { it.id == buddhaId } ?: profiles.first()
        return BuddhaDetail(profile = profile, nextVia = nextVia(profile, LocalDate.now(clock)))
    }

    private fun nextVia(profile: BuddhaProfile, from: LocalDate): LocalDate? =
        (0 until SEARCH_DAYS).asSequence()
            .map { from.plusDays(it.toLong()) }
            .firstOrNull { date ->
                val lunar = LunarCalendar.toLunar(date)
                lunar.day == profile.viaDay && lunar.month == profile.viaMonth && !lunar.isLeapMonth
            }

    private companion object {
        const val SEARCH_DAYS = 400
    }
}

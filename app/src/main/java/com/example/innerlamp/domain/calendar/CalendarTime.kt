package com.example.innerlamp.domain.calendar

import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Time helpers of the calendar feature. /
 * Tiện ích thời gian của tính năng lịch lễ.
 */
object CalendarTime {
    /** Vietnam time zone: the current year / month follow it (FR-CAL-002, BR-CAL-15). / Múi giờ Việt Nam. */
    val VIETNAM_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    /** Last year the calendar offers (AC13). / Năm cuối cùng được tải. */
    const val MAX_YEAR = 2100

    /** Today in Vietnam time. / Hôm nay theo giờ Việt Nam. */
    fun todayInVietnam(clock: Clock): LocalDate = LocalDate.now(clock.withZone(VIETNAM_ZONE))

    /** Current month in Vietnam time. / Tháng hiện tại theo giờ Việt Nam. */
    fun currentMonthInVietnam(clock: Clock): YearMonth = YearMonth.from(todayInVietnam(clock))
}

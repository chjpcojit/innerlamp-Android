package com.example.innerlamp.ui.screens.buddha

import com.example.innerlamp.domain.model.BuddhaDetail
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.screens.home.weekdayFull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * State render cho màn "Chi tiết Phật / Bồ Tát" (`buddha`). [detail] is set only when [status] is
 * `Loaded`. / [detail] chỉ có khi [status] là `Loaded`.
 *
 * Các cờ mở rộng (`bioExpanded`, `vowsExpanded`) là state trình bày — prototype `bdBio` / `bdVow`.
 */
data class BuddhaDetailUiState(
    val today: LocalDate,
    val status: ContentStatus = ContentStatus.Loading,
    val detail: BuddhaDetail? = null,
    val bioExpanded: Boolean = false,
    val vowsExpanded: Boolean = false,
) {
    val profile: BuddhaProfile? get() = detail?.profile

    private val vows: List<String> get() = profile?.vows.orEmpty()

    /** Đại nguyện đang hiển thị (cắt còn [COLLAPSED_VOW_COUNT] khi thu gọn). */
    val visibleVows: List<String>
        get() = if (hasMoreVows && !vowsExpanded) vows.take(COLLAPSED_VOW_COUNT) else vows

    val hasMoreVows: Boolean get() = vows.size > COLLAPSED_VOW_COUNT

    val vowToggleLabel: String
        get() = if (vowsExpanded) "Thu gọn" else "Xem đủ ${vows.size} đại nguyện"

    /** Nhãn đếm cạnh tiêu đề "ĐẠI NGUYỆN" (vd "12 đại nguyện"). */
    val vowCountLabel: String get() = "${vows.size} đại nguyện"

    val bioToggleLabel: String get() = if (bioExpanded) "Thu gọn" else "Đọc thêm"

    /** Ô ngày vía: số ngày "19" và nhãn tháng "THÁNG 2" / "THÁNG GIÊNG" / "THÁNG CHẠP"; `""` khi không có vía. */
    val viaDayText: String get() = profile?.mainVia?.let { "%02d".format(it.day) }.orEmpty()
    val viaMonthText: String
        get() = profile?.mainVia?.let { via ->
            "THÁNG " + when (via.month) {
                1 -> "GIÊNG"
                12 -> "CHẠP"
                else -> via.month.toString()
            }
        }.orEmpty()

    /** Dòng "Còn 12 ngày · Thứ Hai, 20-10-2026 · Vía khác: 19/06, 19/09". */
    val nextViaLabel: String
        get() {
            val next = detail?.nextVia?.let {
                val days = ChronoUnit.DAYS.between(today, it)
                val relative = when (days) {
                    0L -> "Hôm nay"
                    1L -> "Ngày mai"
                    else -> "Còn $days ngày"
                }
                "$relative · ${it.weekdayFull()}, %02d-%02d-%d".format(it.dayOfMonth, it.monthValue, it.year)
            }
            val others = profile?.viaDays?.drop(1).orEmpty()
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ", prefix = "Vía khác: ") { it.label }
            return listOfNotNull(next, others).joinToString(" · ")
        }

    private companion object {
        /** Số đại nguyện hiển thị khi đang thu gọn. */
        const val COLLAPSED_VOW_COUNT = 5
    }
}

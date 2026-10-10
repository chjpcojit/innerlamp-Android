package com.example.innerlamp.ui.screens.buddha

import com.example.innerlamp.domain.model.BuddhaDetail
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.ui.screens.home.weekdayFull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * State render cho màn "Chi tiết Phật / Bồ Tát" (`buddha`).
 *
 * Các cờ mở rộng (`bioExpanded`, `vowsExpanded`) là state trình bày — prototype `bdBio` / `bdVow`.
 */
data class BuddhaDetailUiState(
    val detail: BuddhaDetail,
    val today: LocalDate,
    val bioExpanded: Boolean = false,
    val vowsExpanded: Boolean = false,
) {
    val profile: BuddhaProfile get() = detail.profile

    private val vowNoun: String get() = profile.vowUnit.replace(LEADING_NUMBER, "")

    /** Đại nguyện đang hiển thị (cắt còn [COLLAPSED_VOW_COUNT] khi thu gọn). */
    val visibleVows: List<String>
        get() = if (hasMoreVows && !vowsExpanded) profile.vows.take(COLLAPSED_VOW_COUNT) else profile.vows

    val hasMoreVows: Boolean get() = profile.vows.size > COLLAPSED_VOW_COUNT

    val vowToggleLabel: String
        get() = if (vowsExpanded) "Thu gọn" else "Xem đủ ${profile.vows.size} $vowNoun"

    /** Nhãn đếm cạnh tiêu đề "ĐẠI NGUYỆN" (vd "48 đại nguyện · trích 5"). */
    val vowCountLabel: String
        get() = profile.vowTotal
            ?.let { "$it đại nguyện · trích ${profile.vows.size}" }
            ?: "${profile.vows.size} $vowNoun"

    val bioToggleLabel: String get() = if (bioExpanded) "Thu gọn" else "Đọc thêm"

    /** Ô ngày vía: số ngày "19" và nhãn tháng "THÁNG 2" / "THÁNG GIÊNG" / "THÁNG CHẠP". */
    val viaDayText: String get() = "%02d".format(profile.viaDay)
    val viaMonthText: String
        get() = "THÁNG " + when (profile.viaMonth) {
            1 -> "GIÊNG"
            12 -> "CHẠP"
            else -> profile.viaMonth.toString()
        }

    /** Dòng "Còn 12 ngày · Thứ Hai, 20-10-2026 · <ngày vía khác>". */
    val nextViaLabel: String
        get() {
            val date = detail.nextVia
            val next = date?.let {
                val days = ChronoUnit.DAYS.between(today, it)
                val relative = when (days) {
                    0L -> "Hôm nay"
                    1L -> "Ngày mai"
                    else -> "Còn $days ngày"
                }
                "$relative · ${it.weekdayFull()}, %02d-%02d-%d".format(it.dayOfMonth, it.monthValue, it.year)
            }
            return listOfNotNull(next, profile.extraVia).joinToString(" · ")
        }

    private companion object {
        /** Số đại nguyện hiển thị khi đang thu gọn. */
        const val COLLAPSED_VOW_COUNT = 5
        val LEADING_NUMBER = Regex("^\\d+\\s")
    }
}

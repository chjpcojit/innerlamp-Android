package com.example.innerlamp.ui.screens.home

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.calendar.LunarDate
import com.example.innerlamp.domain.model.Article
import com.example.innerlamp.domain.model.BuddhaFigure
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.Verse
import com.example.innerlamp.domain.usecase.HomeMonthCard
import com.example.innerlamp.domain.usecase.HomeMonthOutcome
import java.time.DayOfWeek
import java.time.LocalDate

/** Phân loại chấm màu dưới mỗi ô ngày: Mùng Một/Rằm ưu tiên hơn hoàng đạo/hắc đạo. */
enum class WeekDayMark { Special, Good, Bad }

/** Một ô ngày trong dải tuần. */
data class WeekDay(
    val date: LocalDate,
    /** Nhãn ngày âm: "1/7" cho Mùng Một, còn lại chỉ số ngày. */
    val lunarLabel: String,
    val mark: WeekDayMark,
)

/** Một trang (tuần) của dải lịch cuộn ngang. */
data class WeekPage(
    /** Vd "Tháng 9, 2026" hoặc "Tháng 9 – Tháng 10, 2026". */
    val label: String,
    val days: List<WeekDay>,
)

/** Nội dung 3 khối chùa / Phật & Bồ Tát / bài viết lấy từ nguồn (API). */
data class HomeSectionsContent(
    val temples: List<Temple>,
    val buddhas: List<BuddhaFigure>,
    val articles: List<Article>,
)

/**
 * State của trang chủ.
 *
 * Các số liệu thực hành ([todayMinutes], [streakDays], [weekActivity]) đang là dữ
 * liệu mẫu từ prototype — chưa có repository thực hành để nối.
 */
data class HomeUiState(
    /** Pháp danh; `null` → chào "quý Phật tử". */
    val displayName: String? = null,
    /** Phần của ngày: "Chào buổi sáng"... */
    val greeting: String,
    val today: LocalDate,
    val selectedDate: LocalDate,
    val weekPages: List<WeekPage>,
    val selectedLunar: LunarDate,
    val verse: Verse,
    /** Six month cards of "Sự kiện Phật giáo" from the calendar (AC19); empty until loaded. */
    val eventCards: List<HomeMonthCard> = emptyList(),
    /** `true` while the cards load and none is shown yet. */
    val eventsLoading: Boolean = false,
    val photoMore: Int = HomeSampleData.PHOTO_MORE,
    val temples: List<Temple> = HomeSampleData.temples,
    val buddhas: List<BuddhaFigure> = HomeSampleData.buddhas,
    val articles: List<Article> = HomeSampleData.articles,
    /** `true` → 3 khối chùa/Phật/bài viết hiện skeleton; `false` + danh sách rỗng → thẻ rỗng. */
    val isContentLoading: Boolean = false,
    val hasUnreadNotifications: Boolean = true,
    val todayMinutes: Int = 12,
    val goalMinutes: Int = 20,
    val streakDays: Int = 42,
    /** Độ đậm 7 ngày gần nhất (0–1) cho thanh chuỗi ngày tinh tấn. */
    val weekActivity: List<Float> = listOf(0.25f, 0.5f, 0.7f, 0.4f, 0.85f, 1f, 0.55f),
    val beads: Int = 27,
) {
    /** Tên hiển thị trong lời chào. */
    val greetingLine: String get() = "$greeting, ${displayName?.takeIf { it.isNotBlank() } ?: "quý Phật tử"}"

    /** The device copy stands in for at least one card ("Dữ liệu có thể chưa được cập nhật.", AC24). */
    val eventsMayBeOutOfDate: Boolean
        get() = eventCards.any { (it.outcome as? HomeMonthOutcome.Loaded)?.mayBeOutOfDate == true }

    /** Chữ cái trên avatar. */
    val avatarInitial: String get() = displayName?.trim()?.firstOrNull()?.uppercase() ?: "A"

    /** Tiến độ 0–1 của tràng hạt 108. */
    val beadProgress: Float get() = beads / BEADS_PER_ROUND.toFloat()

    /** Tiến độ 0–1 của mục tiêu thời khóa hôm nay. */
    val dailyProgress: Float get() = (todayMinutes / goalMinutes.toFloat()).coerceIn(0f, 1f)

    companion object {
        const val BEADS_PER_ROUND = 108

        /** Dựng state ban đầu cho ngày [today]; ngày đang chọn = hôm nay. */
        fun create(
            today: LocalDate,
            hour: Int,
            eventCards: List<HomeMonthCard> = emptyList(),
            displayName: String? = null,
        ): HomeUiState = HomeUiState(
            displayName = displayName,
            greeting = greetingFor(hour),
            today = today,
            selectedDate = today,
            weekPages = buildWeekPages(today),
            selectedLunar = LunarCalendar.toLunar(today),
            verse = verseFor(LunarCalendar.toLunar(today)),
            eventCards = eventCards,
        )

        fun greetingFor(hour: Int): String = when {
            hour < 11 -> "Chào buổi sáng"
            hour < 14 -> "Chào buổi trưa"
            hour < 18 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }

        /** Câu kinh của một ngày âm: xoay vòng theo `(ngày + tháng) % số câu`. */
        fun verseFor(lunar: LunarDate): Verse {
            val verses = HomeSampleData.verses
            return verses[(lunar.day + lunar.month) % verses.size]
        }

        /**
         * 5 tuần (thứ Hai → Chủ Nhật) quanh tuần chứa [today]: 2 tuần trước, tuần này,
         * 2 tuần sau. Trang giữa (index 2) là tuần hiện tại.
         */
        fun buildWeekPages(today: LocalDate): List<WeekPage> {
            val monday = today.with(DayOfWeek.MONDAY)
            return (-2..2).map { page ->
                val days = (0 until 7).map { i ->
                    val date = monday.plusDays(page * 7L + i)
                    val lunar = LunarCalendar.toLunar(date)
                    val mark = when {
                        lunar.day == 1 || lunar.day == 15 -> WeekDayMark.Special
                        LunarCalendar.isGoodDay(date) -> WeekDayMark.Good
                        else -> WeekDayMark.Bad
                    }
                    WeekDay(
                        date = date,
                        lunarLabel = if (lunar.day == 1) "1/${lunar.month}" else lunar.day.toString(),
                        mark = mark,
                    )
                }
                val months = days.map { it.date.monthValue }.distinct()
                WeekPage(
                    label = months.joinToString(" – ") { "Tháng $it" } + ", ${days.first().date.year}",
                    days = days,
                )
            }
        }
    }
}

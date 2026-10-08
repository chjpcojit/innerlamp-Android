package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * Một ô ngày trong lưới tháng của màn Lịch âm (prototype `cal`).
 */
data class LunarDayCell(
    /** Ngày dương; `null` cho ô trống chừa đầu tháng để căn lưới 7 cột. */
    val date: LocalDate?,
    /** Số ngày dương (1–31); 0 cho ô trống. */
    val dayNumber: Int,
    /** Nhãn ngày âm: "Mùng 1", "Rằm", hoặc số ngày âm. */
    val lunarLabel: String,
    /** `true` nếu là ngày trai giới → tô nền vàng nhạt. */
    val isFastDay: Boolean,
)

/**
 * Một trang tháng trong carousel lịch âm cuộn ngang.
 *
 * @property title tiêu đề header: "Tháng 9 · 2026".
 * @property lunarMonthName tên tháng âm: "tháng Bảy".
 * @property canChiYear can chi năm âm: "Bính Ngọ".
 * @property days các ô ngày (kể cả ô trống đầu tháng).
 */
data class LunarMonthPage(
    val title: String,
    val lunarMonthName: String,
    val canChiYear: String,
    val days: List<LunarDayCell>,
)

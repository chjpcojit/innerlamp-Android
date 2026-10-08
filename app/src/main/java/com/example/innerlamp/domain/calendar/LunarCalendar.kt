package com.example.innerlamp.domain.calendar

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Ngày âm lịch Việt Nam (múi giờ GMT+7).
 *
 * @property day ngày âm (1–30).
 * @property month tháng âm (1–12).
 * @property year năm âm.
 * @property isLeapMonth `true` nếu là tháng nhuận.
 */
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean,
) {
    /** Tên tháng âm: "Một", "Hai", ... "Chạp". */
    val monthName: String get() = LunarCalendar.MONTH_NAMES[month - 1]

    /** Tên tháng âm kiểu "Giêng" (dùng cho tiêu đề màn Lịch âm). */
    val calMonthName: String get() = LunarCalendar.CAL_MONTH_NAMES[month - 1]

    /** Can chi của năm âm, vd "Giáp Thìn". */
    val canChiYear: String
        get() = "${LunarCalendar.CAN[(year + 6) % 10]} ${LunarCalendar.CHI[(year + 8) % 12]}"
}

/**
 * Chuyển dương lịch → âm lịch theo thuật toán của Hồ Ngọc Đức (astronomical,
 * tính trăng non + kinh độ mặt trời). Port từ prototype
 * `Docs/design_handoff_innerlamp/design/InnerLamp Prototype.dc.html`
 * để kết quả khớp 1–1 với bản thiết kế.
 *
 * Thuần Kotlin, không phụ thuộc Android — test được trên JVM.
 */
object LunarCalendar {

    /** Múi giờ Việt Nam (GMT+7) — hằng số của thuật toán, không lấy từ máy. */
    private const val VIETNAM_TZ = 7

    private const val SYNODIC_MONTH = 29.530588853
    private const val JD_EPOCH = 2415021.076998695

    val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    val MONTH_NAMES = listOf(
        "Một", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười Một", "Chạp",
    )

    /** Tên tháng âm kiểu "Giêng" (màn Lịch âm dùng cho tiêu đề; khớp `MN` của prototype). */
    val CAL_MONTH_NAMES = listOf(
        "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp",
    )

    /** Các ngày trai giới cố định trong tháng âm (khớp `fast` của prototype), chưa gồm ngày 30. */
    private val FAST_DAYS = setOf(1, 8, 14, 15, 18, 23)

    /** Mốc "ngày 30 âm" — tháng thiếu (29 ngày) không có ngày này nên rơi vào ngày cuối tháng. */
    const val DAY_30 = 30

    /** Chuỗi 12 chi ngày hoàng đạo(1)/hắc đạo(0), khớp bảng tra của prototype. */
    private const val GOOD_DAY_PATTERN = "110011010010"

    /** Đổi một ngày dương lịch sang âm lịch. */
    fun toLunar(date: LocalDate): LunarDate =
        solarToLunar(date.dayOfMonth, date.monthValue, date.year, VIETNAM_TZ)

    /** `true` nếu [lunar] (ứng với [date]) là ngày 29 cuối cùng của một tháng thiếu. */
    fun isLastDayOfShortMonth(lunar: LunarDate, date: LocalDate): Boolean =
        lunar.day == 29 && toLunar(date.plusDays(1)).day == 1

    /**
     * `true` nếu [lunar] (ứng với [date]) khớp mốc ngày âm [target]. Mốc 30 của tháng
     * thiếu rơi vào ngày 29 cuối tháng. Chỉ tính thêm một lần đổi âm khi ngày là 29.
     */
    fun matchesLunarDay(target: Int, lunar: LunarDate, date: LocalDate): Boolean =
        lunar.day == target || (target == DAY_30 && isLastDayOfShortMonth(lunar, date))

    /** `true` nếu là ngày trai giới; ngày cuối tháng thiếu (29) cũng là ngày trai. */
    fun isFastDay(lunar: LunarDate, date: LocalDate): Boolean =
        lunar.day in FAST_DAYS || matchesLunarDay(DAY_30, lunar, date)

    /**
     * `true` nếu [date] là ngày hoàng đạo (tốt), `false` nếu là ngày hắc đạo (xấu) —
     * tra theo chi ngày ứng với tháng âm (khớp `good` của prototype `home`/`cal`).
     */
    fun isGoodDay(date: LocalDate): Boolean {
        val lunar = toLunar(date)
        val dayChi = (jdFromDate(date.dayOfMonth, date.monthValue, date.year) + 1) % 12
        val index = (dayChi - ((lunar.month - 1) % 6) * 2 + 12) % 12
        return GOOD_DAY_PATTERN[index] == '1'
    }

    // ── Julian day ─────────────────────────────────────────────────────────

    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        val jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        return if (jd < 2299161) dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083 else jd
    }

    // ── Trăng non & kinh độ mặt trời ────────────────────────────────────────

    private fun newMoonJd(k: Int): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 -= 0.4068 * sin(mpr * dr) - 0.0161 * sin(dr * 2 * mpr)
        c1 -= 0.0004 * sin(dr * 3 * mpr)
        c1 += 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 += -0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 += -0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 += 0.001 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltat = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltat
    }

    private fun sunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525
        val t2 = t * t
        val dr = PI / 180
        val m = 357.5291 + 35999.0503 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.9146 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.00029 * sin(dr * 3 * m)
        val l = (l0 + dl) * dr
        return l - PI * 2 * floor(l / (PI * 2))
    }

    private fun sunLong12(dayNumber: Int, tz: Int): Int =
        floor(sunLongitude(dayNumber - 0.5 - tz / 24.0) / PI * 6).toInt()

    private fun newMoonDay(k: Int, tz: Int): Int = floor(newMoonJd(k) + 0.5 + tz / 24.0).toInt()

    // ── Tháng 11 âm & tháng nhuận ───────────────────────────────────────────

    private fun lunarMonth11(yy: Int, tz: Int): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / SYNODIC_MONTH).toInt()
        var nm = newMoonDay(k, tz)
        if (sunLong12(nm, tz) >= 9) nm = newMoonDay(k - 1, tz)
        return nm
    }

    private fun leapMonthOffset(a11: Int, tz: Int): Int {
        val k = floor((a11 - JD_EPOCH) / SYNODIC_MONTH + 0.5).toInt()
        var last: Int
        var i = 1
        var arc = sunLong12(newMoonDay(k + i, tz), tz)
        do {
            last = arc
            i++
            arc = sunLong12(newMoonDay(k + i, tz), tz)
        } while (arc != last && i < 14)
        return i - 1
    }

    private fun solarToLunar(dd: Int, mm: Int, yy: Int, tz: Int): LunarDate {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - JD_EPOCH) / SYNODIC_MONTH).toInt()
        var monthStart = newMoonDay(k + 1, tz)
        if (monthStart > dayNumber) monthStart = newMoonDay(k, tz)
        var a11 = lunarMonth11(yy, tz)
        var b11 = a11
        var lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = lunarMonth11(yy - 1, tz)
        } else {
            lunarYear = yy + 1
            b11 = lunarMonth11(yy + 1, tz)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = (monthStart - a11) / 29
        var leap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val lmo = leapMonthOffset(a11, tz)
            if (diff >= lmo) {
                lunarMonth = diff + 10
                if (diff == lmo) leap = true
            }
        }
        if (lunarMonth > 12) lunarMonth -= 12
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1
        return LunarDate(lunarDay, lunarMonth, lunarYear, leap)
    }
}

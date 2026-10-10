package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.AuspiciousHour
import com.example.innerlamp.domain.model.ClashAge
import com.example.innerlamp.domain.model.LunarDayDetail
import com.example.innerlamp.domain.model.LunarDayKind
import com.example.innerlamp.domain.model.TravelDirection
import java.time.LocalDate

/**
 * Tính số liệu "Chi tiết ngày âm" cho một ngày dương lịch — port từ khối `lnInfo/lnHours/
 * lnClash/lnDirs` của prototype `InnerLamp Prototype.dc.html` (màn `lunar`). Thuần Kotlin.
 */
class GetLunarDayDetailUseCase {

    operator fun invoke(date: LocalDate): LunarDayDetail {
        val lunar = LunarCalendar.toLunar(date)
        val jd = LunarCalendar.julianDay(date)
        val can = LunarCalendar.CAN
        val chi = LunarCalendar.CHI

        val dayCan = (jd + 9) % 10
        val dayChi = (jd + 1) % 12
        val monthCan = (lunar.year * 12 + lunar.month + 3) % 10
        val monthChi = (lunar.month + 1) % 12

        val pattern = HOUR_PATTERNS[dayChi % 6]
        val hours = (0 until 12).filter { pattern[it] == '1' }.map { i ->
            val start = (23 + 2 * i) % 24
            AuspiciousHour(chi[i], ANIMALS[i], "%02dh – %02dh".format(start, (start + 2) % 24))
        }

        val clashChi = (dayChi + 6) % 12
        val clashAges = listOf(4, 6).map { ClashAge("${can[(dayCan + it) % 10]} ${chi[clashChi]}", ANIMALS[clashChi]) }
        val dayName = "${can[dayCan]} ${chi[dayChi]}"

        val kind = when {
            lunar.day == 15 -> LunarDayKind.Full
            lunar.day == 1 -> LunarDayKind.New
            LunarCalendar.isGoodDay(date) -> LunarDayKind.Good
            else -> LunarDayKind.Bad
        }

        return LunarDayDetail(
            date = date,
            lunar = lunar,
            kind = kind,
            dayCanChi = dayName,
            monthCanChi = "${can[monthCan]} ${chi[monthChi]}" + if (lunar.isLeapMonth) " (nhuận)" else "",
            yearCanChi = lunar.canChiYear,
            hours = hours,
            clashAges = clashAges,
            clashNote = "Ngày $dayName xung với tuổi ${chi[clashChi]}. Người tuổi này nên thận trọng, " +
                "dành ngày để tụng kinh, sám hối và giữ tâm an.",
            directions = listOf(
                TravelDirection("Hỷ thần", HY_THAN[dayCan % 5], "Đón niềm vui, may mắn", "喜"),
                TravelDirection("Tài thần", TAI_THAN[dayCan], "Cầu tài lộc, công việc", "財"),
                blackGod(jd),
            ),
            advice = when {
                lunar.day == 1 -> "Mùng Một — ngày trai giới, nên tụng kinh"
                lunar.day == 15 -> "Rằm — ngày trai giới, nên phóng sinh, tụng kinh"
                lunar.day in setOf(8, 14, 23, 30) -> "Ngày trai — giữ giới, ăn chay"
                else -> "Ngày thường — giữ chánh niệm trong mọi việc"
            },
        )
    }

    /** Hắc thần: 60 ngày chia 8 cung (6,5,6,5…) bắt đầu từ ngày Giáp Tý; dư thì "Trên trời". */
    private fun blackGod(jd: Int): TravelDirection {
        val k = (((jd + 49) % 60 - 45) % 60 + 60) % 60
        var acc = 0
        var direction: String? = null
        for (i in 0 until 8) {
            acc += if (i % 2 == 1) 5 else 6
            if (k < acc) { direction = BLACK_GOD_DIRECTIONS[i]; break }
        }
        return TravelDirection(
            role = "Hắc thần",
            direction = direction ?: "Trên trời",
            meaning = if (direction != null) "Nên tránh hướng này" else "Không kỵ hướng nào",
            glyph = "黑",
        )
    }

    private companion object {
        val ANIMALS = listOf("🐀", "🐃", "🐅", "🐈", "🐉", "🐍", "🐎", "🐐", "🐒", "🐓", "🐕", "🐖")
        val HOUR_PATTERNS = listOf(
            "110100101100", "001101001011", "110011010010",
            "101100110100", "001011001101", "010010110011",
        )
        val HY_THAN = listOf("Đông Bắc", "Tây Bắc", "Tây Nam", "Chính Nam", "Đông Nam")
        val TAI_THAN = listOf(
            "Đông Nam", "Đông Nam", "Chính Đông", "Chính Đông", "Chính Bắc",
            "Chính Nam", "Tây Nam", "Tây Nam", "Chính Tây", "Tây Bắc",
        )
        val BLACK_GOD_DIRECTIONS = listOf(
            "Đông Bắc", "Chính Đông", "Đông Nam", "Chính Nam",
            "Tây Nam", "Chính Tây", "Tây Bắc", "Chính Bắc",
        )
    }
}

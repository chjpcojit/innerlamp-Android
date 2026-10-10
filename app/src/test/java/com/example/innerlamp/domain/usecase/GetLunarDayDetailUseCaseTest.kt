package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.LunarDayKind
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLunarDayDetailUseCaseTest {

    private val useCase = GetLunarDayDetailUseCase()

    @Test
    fun everyDayHasSixAuspiciousHoursAndTwoClashAges() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(120) {
            val detail = useCase(date)
            assertEquals("giờ hoàng đạo $date", 6, detail.hours.size)
            assertEquals(2, detail.clashAges.size)
            assertEquals(3, detail.directions.size)
            date = date.plusDays(1)
        }
    }

    @Test
    fun fullMoonAndNewMoonTakePriorityOverGoodBad() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(60) {
            val d = useCase(date)
            if (d.lunar.day == 15) assertEquals(LunarDayKind.Full, d.kind)
            if (d.lunar.day == 1) assertEquals(LunarDayKind.New, d.kind)
            date = date.plusDays(1)
        }
    }

    @Test
    fun canChiOfKnownDay() {
        // 2000-01-01 là ngày Mậu Ngọ (mốc Julian 2451545).
        assertTrue(useCase(LocalDate.of(2000, 1, 1)).dayCanChi == "Mậu Ngọ")
    }
}

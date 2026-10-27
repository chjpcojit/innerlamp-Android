package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.DeityDetailDto
import com.example.innerlamp.data.remote.dto.DeityMantrasDto
import com.example.innerlamp.data.remote.dto.DeityMemorialDayDto
import com.example.innerlamp.data.remote.dto.DeitySummaryDto
import com.example.innerlamp.domain.model.BuddhaViaDay
import com.example.innerlamp.domain.model.DeityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeityMapperTest {

    @Test
    fun summary_maps_type_and_short_virtue() {
        val deity = DeitySummaryDto(
            id = "quanam",
            type = "BODHISATTVA",
            nameVi = "Quán Thế Âm Bồ Tát",
            thumbnailUrl = "",
            coreVirtue = "Đại Bi (lòng từ bi)",
        ).toDomain()
        assertEquals(DeityType.Bodhisattva, deity.type)
        assertEquals("Bồ Tát", deity.kindLabel)
        assertEquals("Đại Bi", deity.shortVirtue)
        assertNull(deity.thumbnailUrl)
    }

    @Test
    fun unknown_type_is_kept_without_a_label() {
        val deity = DeitySummaryDto(id = "x", type = "ARHAT", nameVi = "A").toDomain()
        assertEquals(DeityType.Unknown, deity.type)
        assertNull(deity.kindLabel)
    }

    @Test
    fun detail_keeps_valid_via_days_in_order_and_builds_mantra_cards() {
        val profile = DeityDetailDto(
            id = "quanam",
            type = "BODHISATTVA",
            nameVi = "Quán Thế Âm",
            titleHonorific = "Nam Mô Đại Từ Đại Bi Quán Thế Âm Bồ Tát",
            lunarMemorialDays = listOf(
                DeityMemorialDayDto(19, 2, "Đản sinh"),
                DeityMemorialDayDto(0, 6, "hỏng"),
                DeityMemorialDayDto(19, 9, ""),
            ),
            greatVows = listOf("Cứu khổ", ""),
            mantras = DeityMantrasDto(shortMantraSinoVietnamese = "Án ma ni bát di hồng", shortMantraSanskrit = "Om mani padme hum"),
        ).toDomain()
        assertEquals("Nam Mô Đại Từ Đại Bi Quán Thế Âm Bồ Tát", profile.name)
        assertEquals("Quán Thế Âm", profile.shortName)
        assertEquals(listOf(BuddhaViaDay(19, 2, "Đản sinh"), BuddhaViaDay(19, 9, "Ngày vía")), profile.viaDays)
        assertEquals("19/02", profile.mainVia?.label)
        assertEquals(listOf("Cứu khổ"), profile.vows)
        assertEquals(listOf("TÂM CHÚ"), profile.mantras.map { it.label })
        assertEquals("Om mani padme hum", profile.mantras.single().sanskrit)
        assertTrue(profile.sutras.isEmpty())
    }
}

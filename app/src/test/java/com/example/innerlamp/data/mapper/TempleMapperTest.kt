package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.TempleAmenitiesDto
import com.example.innerlamp.data.remote.dto.TempleDetailDto
import com.example.innerlamp.data.remote.dto.TempleLocationDto
import com.example.innerlamp.data.remote.dto.TempleSummaryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TempleMapperTest {

    private val location = TempleLocationDto(lat = 20.27, lng = 105.86, address = "Gia Sinh, Gia Viễn", province = "Ninh Bình")

    @Test
    fun summary_maps_labels_and_prefers_the_heritage_rank_as_note() {
        val temple = TempleSummaryDto(
            id = "t1",
            name = "Chùa Bái Đính",
            type = "CHUA",
            region = "north",
            thumbnailUrl = "https://cdn/t1.jpg",
            heritageRank = "DI_TICH_QUOC_GIA",
            location = location,
        ).toDomain()
        assertEquals("Ninh Bình", temple.place)
        assertEquals("Miền Bắc", temple.region)
        assertEquals("Di tích quốc gia", temple.note)
        assertEquals("https://cdn/t1.jpg", temple.imageUrl)
    }

    @Test
    fun summary_skips_not_ranked_and_keeps_unknown_codes() {
        val temple = TempleSummaryDto(
            id = "t2",
            name = "Thiền viện X",
            type = "THIEN_VIEN",
            region = "west",
            thumbnailUrl = " ",
            heritageRank = "CHUA_XEP_HANG",
            location = TempleLocationDto(address = "Đà Lạt"),
        ).toDomain()
        assertEquals("Đà Lạt", temple.place)
        assertEquals("west", temple.region)
        assertEquals("Thiền viện", temple.note)
        assertNull(temple.imageUrl)
    }

    @Test
    fun detail_turns_empty_strings_into_null_and_keeps_amenities() {
        val detail = TempleDetailDto(
            id = "t1",
            name = "Chùa Bái Đính",
            type = "CHUA",
            region = "north",
            sect = "BẮC_TÔNG",
            heritageRank = "",
            establishedYear = 0,
            thumbnailUrl = "https://cdn/thumb.jpg",
            location = location,
            aliases = listOf("Bái Đính", ""),
            founder = "",
            operatingHours = "07:00 - 18:00",
            amenities = TempleAmenitiesDto(parkingCar = true, vegetarianMeals = true),
            visitingNotes = listOf("Ăn mặc kín đáo", " "),
        ).toDomain()
        assertEquals("Bắc tông", detail.sect)
        assertNull(detail.heritageRank)
        assertNull(detail.establishedYear)
        assertNull(detail.founder)
        assertEquals("https://cdn/thumb.jpg", detail.imageUrl)
        assertEquals(listOf("Bái Đính"), detail.aliases)
        assertEquals(listOf("Ăn mặc kín đáo"), detail.visitingNotes)
        assertTrue(detail.amenities.parkingCar && detail.amenities.vegetarianMeals)
        assertEquals(20.27, detail.lat, 0.0)
    }
}

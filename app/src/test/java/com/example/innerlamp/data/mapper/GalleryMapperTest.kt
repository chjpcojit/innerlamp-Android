package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.GalleryDetailDto
import com.example.innerlamp.data.remote.dto.GallerySummaryDto
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.PhotoCredit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GalleryMapperTest {

    @Test
    fun summary_maps_category_and_falls_back_to_the_thumbnail() {
        val photo = GallerySummaryDto(
            id = "g1",
            type = "TUONG_PHAT",
            title = "Tượng Phật",
            location = "Bái Đính",
            thumbnailUrl = "https://cdn/g1-t.jpg",
            imageUrl = "",
        ).toDomain(index = 1)
        assertEquals(PhotoCategory.BuddhaStatue, photo.category)
        assertEquals("https://cdn/g1-t.jpg", photo.imageUrl)
        assertEquals(PHOTO_TILE_HEIGHTS[1], photo.heightDp)
    }

    @Test
    fun unknown_type_has_no_category_and_heights_cycle() {
        val photo = GallerySummaryDto(id = "g2", type = "KHAC").toDomain(index = PHOTO_TILE_HEIGHTS.size)
        assertNull(photo.category)
        assertEquals(PHOTO_TILE_HEIGHTS[0], photo.heightDp)
    }

    @Test
    fun credit_drops_empty_fields() {
        assertEquals(
            PhotoCredit(author = "Nguyễn A", license = null, sourceUrl = null),
            GalleryDetailDto(id = "g1", author = "Nguyễn A", license = " ").toCredit(),
        )
    }
}

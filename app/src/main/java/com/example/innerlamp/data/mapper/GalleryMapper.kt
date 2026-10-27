package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.GalleryDetailDto
import com.example.innerlamp.data.remote.dto.GallerySummaryDto
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.model.TemplePhoto

/** Tile heights of the prototype masonry, cycled by position. / Chiều cao ô của prototype, xoay vòng. */
internal val PHOTO_TILE_HEIGHTS = listOf(220, 160, 180, 240, 150, 200)

/** [index] = position in the full list, so heights stay stable across filters. / Vị trí trong danh sách đầy đủ. */
fun GallerySummaryDto.toDomain(index: Int): TemplePhoto = TemplePhoto(
    id = id,
    category = PhotoCategory.fromCode(type),
    title = title,
    place = location,
    heightDp = PHOTO_TILE_HEIGHTS[index % PHOTO_TILE_HEIGHTS.size],
    thumbnailUrl = thumbnailUrl.nullIfBlank(),
    imageUrl = imageUrl.nullIfBlank() ?: thumbnailUrl.nullIfBlank(),
)

fun GalleryDetailDto.toCredit(): PhotoCredit = PhotoCredit(
    author = author.nullIfBlank(),
    license = license.nullIfBlank(),
    sourceUrl = sourceUrl.nullIfBlank(),
)

package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs of `GET /v1/gallery` and `GET /v1/gallery/{id}` (openapi 3.0.0). Every field has a default. /
 * DTO của hai route ảnh công khai. Mọi field có default.
 */
@Serializable
data class GalleryPageDto(
    val items: List<GallerySummaryDto> = emptyList(),
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 0,
    val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 0,
)

@Serializable
data class GallerySummaryDto(
    val id: String,
    val type: String = "",
    val title: String = "",
    val location: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class GalleryDetailDto(
    val id: String,
    val author: String = "",
    val license: String = "",
    @SerialName("source_url") val sourceUrl: String = "",
)

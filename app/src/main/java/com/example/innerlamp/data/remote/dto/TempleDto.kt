package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs of `GET /v1/temples/top`, `GET /v1/temples` and `GET /v1/temples/{id}` (openapi 3.0.0). Every field
 * has a default so a missing field never breaks decoding. /
 * DTO của ba route chùa công khai. Mọi field có default để thiếu field không làm hỏng decode.
 */
@Serializable
data class TempleItemsDto(val items: List<TempleSummaryDto> = emptyList())

@Serializable
data class TemplePageDto(
    val items: List<TempleSummaryDto> = emptyList(),
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 0,
    val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 0,
)

@Serializable
data class TempleLocationDto(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = "",
    val province: String = "",
)

@Serializable
data class TempleSummaryDto(
    val id: String,
    val name: String,
    val type: String = "",
    val region: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val sect: String = "",
    @SerialName("heritage_rank") val heritageRank: String = "",
    @SerialName("established_year") val establishedYear: Int? = null,
    val location: TempleLocationDto = TempleLocationDto(),
)

@Serializable
data class TempleAmenitiesDto(
    @SerialName("parking_car") val parkingCar: Boolean = false,
    @SerialName("parking_motorcycle") val parkingMotorcycle: Boolean = false,
    @SerialName("vegetarian_meals") val vegetarianMeals: Boolean = false,
    @SerialName("summer_retreat") val summerRetreat: Boolean = false,
)

@Serializable
data class TempleDetailDto(
    val id: String,
    val name: String,
    val type: String = "",
    val region: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val sect: String = "",
    @SerialName("heritage_rank") val heritageRank: String = "",
    @SerialName("established_year") val establishedYear: Int? = null,
    val location: TempleLocationDto = TempleLocationDto(),
    val aliases: List<String> = emptyList(),
    val founder: String = "",
    @SerialName("current_abbot") val currentAbbot: String = "",
    @SerialName("operating_hours") val operatingHours: String = "",
    val amenities: TempleAmenitiesDto = TempleAmenitiesDto(),
    val introduction: String = "",
    @SerialName("architecture_heritage") val architectureHeritage: String = "",
    @SerialName("visiting_notes") val visitingNotes: List<String> = emptyList(),
)

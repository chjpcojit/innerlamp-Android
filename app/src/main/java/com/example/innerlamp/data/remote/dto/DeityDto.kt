package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs of `GET /v1/deities/top`, `GET /v1/deities` and `GET /v1/deities/{id}` (openapi 3.0.0). Every field
 * has a default so a missing field never breaks decoding. /
 * DTO của ba route chư Phật công khai. Mọi field có default để thiếu field không làm hỏng decode.
 */
@Serializable
data class DeityItemsDto(val items: List<DeitySummaryDto> = emptyList())

@Serializable
data class DeityPageDto(
    val items: List<DeitySummaryDto> = emptyList(),
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 0,
    val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 0,
)

@Serializable
data class DeitySummaryDto(
    val id: String,
    val type: String = "",
    @SerialName("name_vi") val nameVi: String = "",
    @SerialName("name_sanskrit") val nameSanskrit: String = "",
    @SerialName("name_chinese") val nameChinese: String = "",
    @SerialName("title_honorific") val titleHonorific: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("core_virtue") val coreVirtue: String = "",
    @SerialName("pure_land") val pureLand: String = "",
)

@Serializable
data class DeityMemorialDayDto(
    @SerialName("lunar_day") val lunarDay: Int = 0,
    @SerialName("lunar_month") val lunarMonth: Int = 0,
    @SerialName("event_name") val eventName: String = "",
)

@Serializable
data class DeityIconographyDto(
    val posture: String = "",
    @SerialName("mount_or_throne") val mountOrThrone: String = "",
    @SerialName("sacred_artifacts") val sacredArtifacts: List<String> = emptyList(),
    @SerialName("symbolic_meaning") val symbolicMeaning: String = "",
)

@Serializable
data class DeityMantrasDto(
    @SerialName("short_mantra_sanskrit") val shortMantraSanskrit: String = "",
    @SerialName("short_mantra_sino_vietnamese") val shortMantraSinoVietnamese: String = "",
    @SerialName("long_dharani_name") val longDharaniName: String = "",
    @SerialName("dharani_phonetic_snippet") val dharaniPhoneticSnippet: String = "",
)

@Serializable
data class DeityDetailDto(
    val id: String,
    val type: String = "",
    @SerialName("name_vi") val nameVi: String = "",
    @SerialName("name_sanskrit") val nameSanskrit: String = "",
    @SerialName("name_chinese") val nameChinese: String = "",
    @SerialName("title_honorific") val titleHonorific: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("core_virtue") val coreVirtue: String = "",
    @SerialName("pure_land") val pureLand: String = "",
    @SerialName("canonical_source") val canonicalSource: String = "",
    @SerialName("lunar_memorial_days") val lunarMemorialDays: List<DeityMemorialDayDto> = emptyList(),
    val iconography: DeityIconographyDto = DeityIconographyDto(),
    @SerialName("biography_summary") val biographySummary: String = "",
    @SerialName("great_vows") val greatVows: List<String> = emptyList(),
    val mantras: DeityMantrasDto = DeityMantrasDto(),
)

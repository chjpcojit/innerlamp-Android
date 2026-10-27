package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.TempleDetailDto
import com.example.innerlamp.data.remote.dto.TempleSummaryDto
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.TempleAmenities
import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.temple.TempleLabels

/**
 * Summary → the card model: `place` = province (address when empty), `region` = region label (raw code
 * when unknown), `note` = heritage rank, else type label. /
 * Tóm tắt → model thẻ: `place` = tỉnh (trống thì địa chỉ), `region` = nhãn miền (mã lạ giữ nguyên),
 * `note` = hạng di tích, không có thì loại cơ sở.
 */
fun TempleSummaryDto.toDomain(): Temple = Temple(
    id = id,
    name = name,
    place = location.province.ifBlank { location.address },
    region = TempleLabels.region(region) ?: region,
    note = listOfNotNull(TempleLabels.heritageRank(heritageRank), TempleLabels.type(type))
        .firstOrNull { it != NOT_RANKED }
        .orEmpty(),
    imageUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
)

fun TempleDetailDto.toDomain(): TempleDetail = TempleDetail(
    id = id,
    name = name,
    place = location.province.ifBlank { location.address },
    region = TempleLabels.region(region),
    type = TempleLabels.type(type) ?: type.nullIfBlank(),
    sect = TempleLabels.sect(sect) ?: sect.nullIfBlank(),
    heritageRank = TempleLabels.heritageRank(heritageRank),
    establishedYear = establishedYear?.takeIf { it > 0 },
    imageUrl = imageUrl.nullIfBlank() ?: thumbnailUrl.nullIfBlank(),
    address = location.address,
    lat = location.lat,
    lng = location.lng,
    aliases = aliases.filter { it.isNotBlank() },
    founder = founder.nullIfBlank(),
    currentAbbot = currentAbbot.nullIfBlank(),
    operatingHours = operatingHours.nullIfBlank(),
    amenities = TempleAmenities(
        parkingCar = amenities.parkingCar,
        parkingMotorcycle = amenities.parkingMotorcycle,
        vegetarianMeals = amenities.vegetarianMeals,
        summerRetreat = amenities.summerRetreat,
    ),
    introduction = introduction.nullIfBlank(),
    architectureHeritage = architectureHeritage.nullIfBlank(),
    visitingNotes = visitingNotes.filter { it.isNotBlank() },
)

internal fun String?.nullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** "Chưa xếp hạng" says nothing on a card. / "Chưa xếp hạng" không đáng hiện trên thẻ. */
private const val NOT_RANKED = "Chưa xếp hạng"

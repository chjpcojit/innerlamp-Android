package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.CalendarImageDto
import com.example.innerlamp.domain.model.CalendarDayImage
import java.time.LocalDate
import kotlinx.serialization.json.jsonPrimitive

/**
 * Maps the DTO to the domain model. Throws (`DateTimeParseException`) on a bad `date`; the repository
 * turns that into a failure. Blank URLs become null. /
 * Map DTO sang domain. Ném lỗi khi `date` sai; repository đổi thành thất bại. URL rỗng thành null.
 */
fun CalendarImageDto.toDomain(): CalendarDayImage = CalendarDayImage(
    date = LocalDate.parse(date),
    id = id?.jsonPrimitive?.content.orEmpty(),
    imageUrl = imageUrl,
    author = author.trim(),
    license = license.trim(),
    licenseUrl = licenseUrl?.takeIf { it.isNotBlank() },
    sourceUrl = sourceUrl?.takeIf { it.isNotBlank() },
)

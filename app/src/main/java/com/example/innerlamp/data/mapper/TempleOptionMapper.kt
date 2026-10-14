package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.TempleOptionDto
import com.example.innerlamp.data.remote.dto.TempleOptionPageDto
import com.example.innerlamp.domain.model.TempleOption
import com.example.innerlamp.domain.model.TempleOptionPage

fun TempleOptionDto.toDomain(): TempleOption = TempleOption(
    id = id,
    name = name,
    provinceName = province,
    provinceCode = provinceCode,
)

fun TempleOptionPageDto.toDomain(): TempleOptionPage = TempleOptionPage(
    items = items.map { it.toDomain() },
    page = page,
    pageSize = pageSize,
    total = total,
    totalPages = totalPages,
)

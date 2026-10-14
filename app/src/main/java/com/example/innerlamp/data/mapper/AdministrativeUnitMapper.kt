package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.DistrictDto
import com.example.innerlamp.data.remote.dto.ProvinceDto
import com.example.innerlamp.data.remote.dto.WardDto
import com.example.innerlamp.domain.model.District
import com.example.innerlamp.domain.model.Province
import com.example.innerlamp.domain.model.Ward

fun ProvinceDto.toDomain(): Province = Province(
    code = code,
    name = name,
    fullName = fullName,
    type = type,
)

fun DistrictDto.toDomain(): District = District(
    code = code,
    name = name,
    fullName = fullName,
    type = type,
    provinceCode = provinceCode,
)

fun WardDto.toDomain(): Ward = Ward(
    code = code,
    name = name,
    fullName = fullName,
    type = type,
    provinceCode = provinceCode,
    districtCode = districtCode,
)

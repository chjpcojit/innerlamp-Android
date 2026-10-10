package com.example.innerlamp.domain.model

/** Một ngày lễ trong lịch lễ Phật giáo kèm các chùa tổ chức. */
data class FestivalEntry(
    val event: BuddhistEvent,
    /** `true` = đại lễ (mọi chùa đều tổ chức) — nhóm lọc "Lễ lớn". */
    val isMajor: Boolean,
    val hosts: List<Temple>,
)

/** Các ngày lễ của một tháng dương lịch (chỉ chứa tháng có sự kiện). */
data class FestivalMonth(
    val year: Int,
    val month: Int,
    val entries: List<FestivalEntry>,
)

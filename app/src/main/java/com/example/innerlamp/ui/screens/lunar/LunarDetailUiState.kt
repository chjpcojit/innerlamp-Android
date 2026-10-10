package com.example.innerlamp.ui.screens.lunar

import com.example.innerlamp.domain.model.LunarDayDetail
import com.example.innerlamp.domain.model.Verse

/** State màn Chi tiết ngày âm: số liệu của ngày + câu kinh trong ngày. */
data class LunarDetailUiState(
    val detail: LunarDayDetail,
    val verse: Verse,
)

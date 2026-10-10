package com.example.innerlamp.ui.screens.lunar

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.usecase.GetLunarDayDetailUseCase
import com.example.innerlamp.ui.screens.home.HomeUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel màn Chi tiết ngày âm. Toàn bộ số liệu tính cục bộ từ [date] (thuần Kotlin),
 * nên state được dựng một lần, không có trạng thái tải.
 */
class LunarDetailViewModel(
    date: LocalDate,
    getDetail: GetLunarDayDetailUseCase = GetLunarDayDetailUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        getDetail(date).let { LunarDetailUiState(detail = it, verse = HomeUiState.verseFor(it.lunar)) },
    )
    val uiState: StateFlow<LunarDetailUiState> = _uiState.asStateFlow()
}

package com.example.innerlamp.ui.screens.festivals

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.usecase.GetFestivalCalendarUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn "Lịch lễ Phật giáo": lịch lễ từ tháng 1 của năm hiện tại cộng 2 năm kế tiếp.
 *
 * @param clock inject để test cố định "hôm nay".
 */
class FestivalsViewModel(
    clock: Clock = Clock.systemDefaultZone(),
    getFestivals: GetFestivalCalendarUseCase = GetFestivalCalendarUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LocalDate.now(clock).let { today ->
            FestivalsUiState(months = getFestivals(startYear = today.year), today = today)
        },
    )
    val uiState: StateFlow<FestivalsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: FestivalFilter) = _uiState.update { it.copy(filter = filter) }
}

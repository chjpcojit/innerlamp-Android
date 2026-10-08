package com.example.innerlamp.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.sourceLabel
import com.example.innerlamp.domain.usecase.SutraQuoteUseCase
import kotlinx.coroutines.launch
import com.example.innerlamp.domain.usecase.GetLunarMonthPagesUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn Lịch âm (cal).
 *
 * Lưới tháng tính từ [GetLunarMonthPagesUseCase] (thuần Kotlin, không cần data
 * layer); thẻ "Hôm nay" + hàng nhắc dùng dữ liệu biên tập mẫu ([CalSampleData])
 * chờ `il-data-builder` nối backend.
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 */
class CalViewModel(
    clock: Clock = Clock.systemDefaultZone(),
    getMonths: GetLunarMonthPagesUseCase = GetLunarMonthPagesUseCase(),
    quotes: SutraQuoteUseCase? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(clock, getMonths))
    val uiState: StateFlow<CalUiState> = _uiState.asStateFlow()

    init {
        if (quotes != null) {
            viewModelScope.launch {
                // Lỗi/rỗng thì giữ ghi chú mẫu.
                val quote = runCatching { quotes.randomQuote() }.getOrNull() ?: return@launch
                _uiState.update { it.copy(todayTitle = quote.content, todayBody = quote.sourceLabel) }
            }
        }
    }

    /** Chọn một ngày trong lưới → tô đậm ô đó. */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { if (it.selectedDate == date) it else it.copy(selectedDate = date) }
    }

    private companion object {
        fun initialState(clock: Clock, getMonths: GetLunarMonthPagesUseCase): CalUiState {
            val today = LocalDateTime.now(clock).toLocalDate()
            return CalUiState.create(today, getMonths(today))
        }
    }
}

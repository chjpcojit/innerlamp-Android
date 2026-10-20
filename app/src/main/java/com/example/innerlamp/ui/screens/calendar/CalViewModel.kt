package com.example.innerlamp.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.sourceLabel
import com.example.innerlamp.domain.repository.CalendarImageError
import com.example.innerlamp.domain.usecase.GetCalendarDayImageUseCase
import com.example.innerlamp.domain.usecase.SutraQuoteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
 * Ảnh Phật theo ngày: mỗi lần chọn ngày tải lại ảnh của ngày đó (huỷ lần tải cũ nếu còn chạy);
 * [retryImage] tải lại khi lỗi. Ảnh của một ngày không đổi nên repository cache theo ngày.
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 * @param getDayImage null → không tải ảnh (preview/test), trạng thái ảnh giữ `Loading`.
 */
class CalViewModel(
    clock: Clock = Clock.systemDefaultZone(),
    getMonths: GetLunarMonthPagesUseCase = GetLunarMonthPagesUseCase(),
    quotes: SutraQuoteUseCase? = null,
    private val getDayImage: GetCalendarDayImageUseCase? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(clock, getMonths))
    val uiState: StateFlow<CalUiState> = _uiState.asStateFlow()

    private var imageJob: Job? = null

    init {
        loadImage(_uiState.value.selectedDate)
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
        if (_uiState.value.selectedDate == date) return
        _uiState.update { it.copy(selectedDate = date) }
        loadImage(date)
    }

    /** Thử lại tải ảnh của ngày đang chọn. */
    fun retryImage() = loadImage(_uiState.value.selectedDate)

    private fun loadImage(date: LocalDate) {
        val useCase = getDayImage ?: return
        imageJob?.cancel()
        _uiState.update { it.copy(dayImage = DayImageUiState.Loading) }
        imageJob = viewModelScope.launch {
            val state = try {
                useCase(date).fold(
                    onSuccess = { DayImageUiState.Loaded(it) },
                    onFailure = { if (it is CalendarImageError.Offline) DayImageUiState.Offline else DayImageUiState.Failed },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DayImageUiState.Failed
            }
            _uiState.update { if (it.selectedDate == date) it.copy(dayImage = state) else it }
        }
    }

    private companion object {
        fun initialState(clock: Clock, getMonths: GetLunarMonthPagesUseCase): CalUiState {
            val today = LocalDateTime.now(clock).toLocalDate()
            return CalUiState.create(today, getMonths(today))
        }
    }
}

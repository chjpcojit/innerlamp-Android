package com.example.innerlamp.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.GetEventOccurrenceUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel of the festival detail: finds `(eventCode, solarDate)` through [GetEventOccurrenceUseCase].
 * [solarDate] is the ISO `YYYY-MM-DD` string of the route; a malformed one is "not found". /
 * ViewModel chi tiết ngày lễ: tìm `(eventCode, solarDate)`. [solarDate] là chuỗi ISO của route; sai
 * định dạng thì coi như không tìm thấy.
 */
class FestivalDetailViewModel(
    private val eventCode: String,
    solarDate: String,
    private val getOccurrence: GetEventOccurrenceUseCase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val date: LocalDate? = try {
        LocalDate.parse(solarDate)
    } catch (_: DateTimeParseException) {
        null
    }

    private val _uiState = MutableStateFlow<FestivalDetailUiState>(FestivalDetailUiState.Loading)
    val uiState: StateFlow<FestivalDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** "Thử lại" after a load error (user tap only). / Thử lại sau lỗi tải (chỉ khi người dùng bấm). */
    fun onRetry() {
        if (_uiState.value is FestivalDetailUiState.Loading) return
        _uiState.value = FestivalDetailUiState.Loading
        load()
    }

    /** ON_RESUME: keep the countdown right after midnight. / Giữ đếm ngược đúng sau nửa đêm. */
    fun onResume() = _uiState.update {
        if (it is FestivalDetailUiState.Found) it.copy(today = LocalDate.now(clock)) else it
    }

    private fun load() {
        val solarDate = date ?: run {
            _uiState.value = FestivalDetailUiState.NotFound
            return
        }
        viewModelScope.launch {
            getOccurrence.detail(eventCode, solarDate).fold(
                onSuccess = { found ->
                    _uiState.value = found?.let { FestivalDetailUiState.Found(it, LocalDate.now(clock)) }
                        ?: FestivalDetailUiState.NotFound
                },
                onFailure = {
                    _uiState.value = FestivalDetailUiState.Error(it as? CalendarLoadError ?: CalendarLoadError.Failed)
                },
            )
        }
    }
}

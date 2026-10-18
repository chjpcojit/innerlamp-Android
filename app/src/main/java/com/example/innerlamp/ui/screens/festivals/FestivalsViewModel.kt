package com.example.innerlamp.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.LoadCalendarYearUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel of the festival calendar: the current Vietnam-time year (device copy first, then the
 * network), the next year when the user reaches the end of the list, and nothing past 2100. It never
 * retries by itself: a failure waits for a user tap (AC31). /
 * ViewModel của lịch lễ: năm hiện tại (giờ VN; bản trên máy trước, rồi mạng), năm kế tiếp khi cuộn tới
 * cuối danh sách, dừng ở 2100. Không tự retry: lỗi chờ người dùng bấm "Thử lại".
 *
 * @param clock device clock for "today" (countdown, scroll target); injected for tests. /
 *   đồng hồ máy cho "hôm nay"; inject để test.
 */
class FestivalsViewModel(
    private val loadYear: LoadCalendarYearUseCase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(FestivalsUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<FestivalsUiState> = _uiState.asStateFlow()

    private var baseYear: Int = loadYear.currentYear()
    private var firstJob: Job? = null
    private var nextJob: Job? = null

    init {
        loadFirstYear()
    }

    fun onFilterSelected(filter: CalendarFilter) = _uiState.update { it.copy(filter = filter) }

    /** "Thử lại" of the full-screen error or of the out-of-date message. / Thử lại ở lỗi toàn màn hình. */
    fun onRetry() {
        val current = _uiState.value.loadState
        if (current is FestivalsLoadState.Loaded && current.refreshing) return
        loadFirstYear()
    }

    /** The end of the list is on screen: append the next year once (AC12, AC13). / Cuối danh sách hiện ra. */
    fun onReachedEnd() {
        val state = _uiState.value
        if (state.loadState !is FestivalsLoadState.Loaded || state.nextYearState != NextYearState.Idle) return
        appendNextYear(state)
    }

    /** "Thử lại" row at the end of the list. / Hàng "Thử lại" ở cuối danh sách. */
    fun onRetryNextYear() {
        val state = _uiState.value
        if (state.nextYearState !is NextYearState.Error) return
        appendNextYear(state)
    }

    /** ON_RESUME: follow a new day; a new Vietnam-time year restarts the load (AC20). */
    fun onResume() {
        val today = LocalDate.now(clock)
        if (today != _uiState.value.today) _uiState.update { it.copy(today = today) }
        val year = loadYear.currentYear()
        if (year != baseYear) {
            baseYear = year
            nextJob?.cancel()
            _uiState.update { it.copy(years = emptyList(), nextYearState = NextYearState.Idle) }
            loadFirstYear()
        }
    }

    private fun loadFirstYear() {
        firstJob?.cancel()
        val hasData = _uiState.value.years.isNotEmpty()
        _uiState.update {
            it.copy(
                loadState = if (hasData) FestivalsLoadState.Loaded(refreshing = true) else FestivalsLoadState.Loading,
            )
        }
        firstJob = viewModelScope.launch {
            loadYear.observe(baseYear).collect { result ->
                result.fold(
                    onSuccess = { load ->
                        val deviceCopyOnly = load.source == CalendarYearSource.DeviceCopy && !load.mayBeOutOfDate
                        _uiState.update {
                            it.copy(
                                years = it.years.withYear(load.year),
                                loadState = FestivalsLoadState.Loaded(
                                    refreshing = deviceCopyOnly,
                                    mayBeOutOfDate = load.mayBeOutOfDate,
                                ),
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            if (it.years.any { y -> y.year == baseYear }) {
                                it.copy(loadState = FestivalsLoadState.Loaded(mayBeOutOfDate = true))
                            } else {
                                it.copy(loadState = FestivalsLoadState.Error(e.asCalendarError()))
                            }
                        }
                    },
                )
            }
        }
    }

    private fun appendNextYear(state: FestivalsUiState) {
        val next = loadYear.nextYear(state.lastYear ?: return)
        if (next == null) {
            _uiState.update { it.copy(nextYearState = NextYearState.End) }
            return
        }
        _uiState.update { it.copy(nextYearState = NextYearState.Loading(next)) }
        nextJob = viewModelScope.launch {
            loadYear(next).fold(
                onSuccess = { load ->
                    _uiState.update {
                        it.copy(
                            years = it.years.withYear(load.year),
                            nextYearState = if (loadYear.nextYear(next) == null) NextYearState.End else NextYearState.Idle,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(nextYearState = NextYearState.Error(next, e.asCalendarError())) }
                },
            )
        }
    }

    private fun List<CalendarYear>.withYear(year: CalendarYear): List<CalendarYear> =
        (filterNot { it.year == year.year } + year).sortedBy { it.year }

    private fun Throwable.asCalendarError(): CalendarLoadError = this as? CalendarLoadError ?: CalendarLoadError.Failed
}

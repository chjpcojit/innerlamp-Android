package com.example.innerlamp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.toVerse
import com.example.innerlamp.domain.usecase.SutraQuoteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.usecase.GetBuddhistEventMonthsUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel trang chủ.
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Ngày đang chọn ở dải tuần quyết định thẻ âm lịch + câu kinh.
 * - Số hạt tràng hạt chỉ để hiển thị (dữ liệu mẫu); lần hạt nằm ở màn Niệm Phật, chưa có
 *   repository thực hành để chia sẻ số hạt giữa hai màn.
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 */
class HomeViewModel(
    clock: Clock = Clock.systemDefaultZone(),
    getEventMonths: GetBuddhistEventMonthsUseCase = GetBuddhistEventMonthsUseCase(),
    private val quotes: SutraQuoteUseCase? = null,
    private val contentLoader: (suspend () -> HomeSectionsContent)? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(clock, getEventMonths, loading = contentLoader != null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRandomVerse()
        loadContent()
    }

    /** Chọn một ngày ở dải tuần → cập nhật thẻ âm lịch + câu kinh (random). */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { state ->
            if (state.selectedDate == date) return@update state
            val lunar = LunarCalendar.toLunar(date)
            state.copy(selectedDate = date, selectedLunar = lunar, verse = HomeUiState.verseFor(lunar))
        }
        loadRandomVerse()
    }

    /** Bốc quote ngẫu nhiên từ DB local; lỗi/rỗng thì giữ câu kinh hiện tại. */
    private fun loadRandomVerse() {
        val useCase = quotes ?: return
        viewModelScope.launch {
            val quote = runCatching { useCase.randomQuote() }.getOrNull() ?: return@launch
            _uiState.update { it.copy(verse = quote.toVerse()) }
        }
    }

    /** "Thử lại" ở thẻ rỗng: quay lại skeleton rồi gọi lại nguồn dữ liệu. */
    fun onReload() {
        if (_uiState.value.isContentLoading) return
        _uiState.update { it.copy(isContentLoading = true) }
        loadContent()
    }

    /**
     * Gọi nguồn dữ liệu cho 3 khối: skeleton → có dữ liệu thì hiện, rỗng/lỗi thì danh sách rỗng
     * (UI hiện thẻ rỗng). Không có [contentLoader] thì giữ dữ liệu mẫu.
     */
    private fun loadContent() {
        val loader = contentLoader ?: return
        viewModelScope.launch {
            val content = try {
                loader()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            _uiState.update {
                it.copy(
                    isContentLoading = false,
                    temples = content?.temples.orEmpty(),
                    buddhas = content?.buddhas.orEmpty(),
                    articles = content?.articles.orEmpty(),
                )
            }
        }
    }

    private companion object {
        fun initialState(clock: Clock, getEventMonths: GetBuddhistEventMonthsUseCase, loading: Boolean): HomeUiState {
            val now = LocalDateTime.now(clock)
            return HomeUiState.create(
                today = now.toLocalDate(),
                hour = now.hour,
                eventMonths = getEventMonths.forYear(now.year),
            ).let { state ->
                if (loading) state.copy(isContentLoading = true, temples = emptyList(), buddhas = emptyList(), articles = emptyList()) else state
            }
        }
    }
}

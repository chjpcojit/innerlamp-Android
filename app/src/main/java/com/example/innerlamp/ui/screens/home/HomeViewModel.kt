package com.example.innerlamp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.toVerse
import com.example.innerlamp.domain.usecase.SutraQuoteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.usecase.GetHomeEventBlockUseCase
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
    private val clock: Clock = Clock.systemDefaultZone(),
    private val getHomeEventBlock: GetHomeEventBlockUseCase? = null,
    private val quotes: SutraQuoteUseCase? = null,
    private val contentLoader: (suspend () -> HomeSectionsContent)? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(clock, loading = contentLoader != null, eventsLoading = getHomeEventBlock != null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var eventsJob: Job? = null

    init {
        loadRandomVerse()
        loadContent()
        loadEvents()
    }

    /**
     * ON_RESUME: when a new day has started, move "today" and rebuild the event block (AC20). /
     * ON_RESUME: sang ngày mới thì đổi "hôm nay" và dựng lại khối sự kiện.
     */
    fun onResume() {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()
        val state = _uiState.value
        if (today == state.today) return
        _uiState.update {
            it.copy(
                today = today,
                greeting = HomeUiState.greetingFor(now.hour),
                weekPages = HomeUiState.buildWeekPages(today),
                selectedDate = if (it.selectedDate == it.today) today else it.selectedDate,
            )
        }
        loadEvents()
    }

    /** "Thử lại" on a month card: reload the missing year(s) (user tap only). / Thử lại ở thẻ tháng. */
    fun onReloadEvents() {
        if (_uiState.value.eventsLoading) return
        _uiState.update { it.copy(eventsLoading = true) }
        loadEvents()
    }

    /** Device copy first (when every needed year is on the device), then the loaded cards (AC19, AC23). */
    private fun loadEvents() {
        val useCase = getHomeEventBlock ?: return
        eventsJob?.cancel()
        eventsJob = viewModelScope.launch {
            useCase.fromDeviceCopy()?.let { copy ->
                _uiState.update { it.copy(eventCards = copy) }
            }
            val cards = useCase()
            _uiState.update { it.copy(eventCards = cards, eventsLoading = false) }
        }
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
                    photos = content?.photos,
                )
            }
        }
    }

    private companion object {
        fun initialState(clock: Clock, loading: Boolean, eventsLoading: Boolean): HomeUiState {
            val now = LocalDateTime.now(clock)
            return HomeUiState.create(
                today = now.toLocalDate(),
                hour = now.hour,
            ).let { state ->
                state.copy(eventsLoading = eventsLoading).let {
                    if (loading) it.copy(isContentLoading = true, temples = emptyList(), buddhas = emptyList(), articles = emptyList()) else it
                }
            }
        }
    }
}

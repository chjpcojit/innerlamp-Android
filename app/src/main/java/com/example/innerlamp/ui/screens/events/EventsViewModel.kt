package com.example.innerlamp.ui.screens.events

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.EventTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Sự kiện & khóa học" (`events`).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Dữ liệu hiện lấy từ [EventsSampleData] vì chưa có repository sự kiện.
 *   TODO(il-data-builder): nối vào use case `GetEvents` khi data layer có.
 *
 * @author innerlamp
 */
class EventsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        EventsUiState(
            selectedTab = EventTab.Upcoming,
            featured = EventsSampleData.featured,
            eventsByTab = EventsSampleData.eventsByTab,
        ),
    )
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    /** Chọn tab lọc (Sắp diễn ra / Khóa học / Đã ghi danh). */
    fun onTabSelected(tab: EventTab) {
        _uiState.update { state ->
            if (state.selectedTab == tab) state else state.copy(selectedTab = tab)
        }
    }
}

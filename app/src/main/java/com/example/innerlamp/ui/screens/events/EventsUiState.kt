package com.example.innerlamp.ui.screens.events

import com.example.innerlamp.domain.model.EventItem
import com.example.innerlamp.domain.model.EventTab
import com.example.innerlamp.domain.model.FeaturedEvent

/**
 * State render cho màn "Sự kiện & khóa học" (`events`).
 *
 * Giữ tab đang chọn + dữ liệu của từng tab. Dữ liệu hiện tĩnh từ [EventsSampleData];
 * TODO(il-data-builder): nối vào use case lấy danh sách sự kiện khi data layer có.
 */
data class EventsUiState(
    /** Tab đang chọn. */
    val selectedTab: EventTab,
    /** Thẻ hero luôn tối ở đầu màn. */
    val featured: FeaturedEvent,
    /** Danh sách sự kiện theo từng tab (khớp `events[evTab]` của prototype). */
    val eventsByTab: Map<EventTab, List<EventItem>>,
) {
    /** Danh sách sự kiện của tab đang chọn. */
    val events: List<EventItem> = eventsByTab[selectedTab].orEmpty()
}

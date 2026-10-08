package com.example.innerlamp.ui.screens.events

import com.example.innerlamp.domain.model.EventItem
import com.example.innerlamp.domain.model.EventTab
import com.example.innerlamp.domain.model.EventTone
import com.example.innerlamp.domain.model.FeaturedEvent

/**
 * Dữ liệu mẫu cho màn "Sự kiện & khóa học", lấy từ prototype
 * `Docs/design_handoff_innerlamp/` (màn `events`).
 *
 * TODO(il-data-builder): thay bằng repository/API sự kiện (danh sách theo tab,
 * trạng thái chỗ còn trống, sự kiện nổi bật). Giữ ở đây để UI dựng được độc lập,
 * giống `SanghaSampleData`.
 */
internal object EventsSampleData {

    /** Thẻ hero "Sắp diễn ra" (luôn tối). */
    val featured = FeaturedEvent(
        day = "14",
        month = "TH9",
        eyebrow = "SẮP DIỄN RA",
        title = "Khóa tu một ngày\nan lạc",
        detail = "Chủ Nhật · 6:30 – 17:00 · Chánh điện\nSư cô Tâm Nguyện hướng dẫn",
        remainingLabel = "còn 12 chỗ",
    )

    /** Danh sách sự kiện theo từng tab (khớp `events[evTab]` của prototype). */
    val eventsByTab: Map<EventTab, List<EventItem>> = mapOf(
        EventTab.Upcoming to listOf(
            EventItem(
                id = "ev-vulan",
                day = "15",
                month = "TH9",
                name = "Tụng Kinh Vu Lan",
                sub = "Rằm tháng Bảy · 19:00 · chánh điện",
                status = "Ghi danh",
                tone = EventTone.Gold,
            ),
            EventItem(
                id = "ev-hieudao",
                day = "18",
                month = "TH9",
                name = "Pháp thoại: Hiếu đạo",
                sub = "Thứ Tư · 19:30 · trực tuyến",
                status = "Ghi danh",
                tone = EventTone.Green,
            ),
            EventItem(
                id = "ev-batquantrai",
                day = "22",
                month = "TH9",
                name = "Thọ bát quan trai",
                sub = "Chủ Nhật · 5:00 – 19:00",
                status = "Còn 4 chỗ",
                tone = EventTone.Rose,
            ),
            EventItem(
                id = "ev-phongsinh",
                day = "30",
                month = "TH9",
                name = "Phóng sinh đầu tháng",
                sub = "Mùng 1 · 6:30 · bến Bình Đông",
                status = "Ghi danh",
                tone = EventTone.Ink,
            ),
        ),
        EventTab.Courses to listOf(
            EventItem(
                id = "co-thien",
                day = "8",
                month = "TUẦN",
                name = "Thiền chánh niệm căn bản",
                sub = "Tối thứ Ba · sư cô Tâm Nguyện",
                status = "Đang mở",
                tone = EventTone.Green,
            ),
            EventItem(
                id = "co-phapcu",
                day = "6",
                month = "TUẦN",
                name = "Học Kinh Pháp Cú",
                sub = "Sáng Chủ Nhật · thầy Minh Đạo",
                status = "Đang mở",
                tone = EventTone.Gold,
            ),
            EventItem(
                id = "co-niemphat",
                day = "4",
                month = "TUẦN",
                name = "Niệm Phật cho người mới",
                sub = "Tối thứ Sáu · trực tuyến",
                status = "Sắp mở",
                tone = EventTone.Rose,
            ),
        ),
        EventTab.Registered to listOf(
            EventItem(
                id = "reg-khoatu",
                day = "14",
                month = "TH9",
                name = "Khóa tu một ngày an lạc",
                sub = "Chủ Nhật · 6:30 · đã xác nhận",
                status = "Đã ghi danh",
                tone = EventTone.Green,
            ),
            EventItem(
                id = "reg-vulan",
                day = "15",
                month = "TH9",
                name = "Tụng Kinh Vu Lan",
                sub = "Rằm tháng Bảy · 19:00",
                status = "Đã ghi danh",
                tone = EventTone.Gold,
            ),
        ),
    )
}

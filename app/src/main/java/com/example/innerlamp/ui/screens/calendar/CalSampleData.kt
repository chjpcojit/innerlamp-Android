package com.example.innerlamp.ui.screens.calendar

/**
 * Dữ liệu mẫu (biên tập) cho thẻ "Hôm nay" và các hàng nhắc trên màn Lịch âm.
 *
 * TODO(il-data-builder): thay bằng dữ liệu thật từ backend/API — ghi chú "hôm nay"
 * theo ngày âm (lễ vía, ngày trai, sự kiện đạo tràng) và lịch nhắc cá nhân. Giữ ở
 * đây để UI dựng được độc lập.
 */
internal object CalSampleData {

    /** Tiêu đề / đoạn giới thiệu của thẻ "Hôm nay" (dữ liệu mẫu, theo ngày âm thực tế). */
    const val TODAY_NOTE_TITLE = "Sau lễ Vu Lan Báo Hiếu"
    const val TODAY_NOTE_BODY = "Tiếp tục hồi hướng công đức cho cửu huyền thất tổ trong bảy ngày."

    // Hàng nhắc thực hành (mẫu). Hàng "Sám hối cuối tháng" dẫn sang trang chùa (`temple`).
    const val FAST_REMINDER_TITLE = "Ngày trai · ăn chay"
    const val FAST_REMINDER_SUB = "18 âm · nhắc lúc 06:00"
    const val REPENTANCE_TITLE = "Sám hối cuối tháng"
    const val REPENTANCE_SUB = "30 âm · Chùa Từ Quang 19:00"
}

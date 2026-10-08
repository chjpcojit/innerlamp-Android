package com.example.innerlamp.ui.screens.settings

import com.example.innerlamp.domain.model.ProfileUser

/**
 * Dữ liệu mẫu cho màn Cài đặt, lấy từ prototype `Docs/design_handoff_innerlamp/`
 * (màn `settings`).
 *
 * TODO(il-data-builder): thay bằng use case đọc cài đặt người dùng — các switch
 * (chuông, nhắc ngày trai, song ngữ, ẩn nhật ký), cỡ chữ, chế độ giao diện và âm
 * thanh chuông đều nên lưu ở `core/storage` (prefs), không phải hardcode ở đây.
 */
internal object SettingsSampleData {

    /** Tài khoản ở thẻ đầu màn (khớp header Hồ sơ). */
    val user = ProfileUser(
        fullName = "Nguyễn Bình An",
        initials = "A",
        dharmaName = "Tâm Minh",
        role = "Phật tử tại gia",
    )

    const val PHONE = "+84 912 345 678"

    /** Các mốc chuông tỉnh thức (khớp `bells` của prototype). */
    const val REMINDER_SUMMARY = "05:30 · 12:00 · 21:30"

    const val BELL_SOUND = "Khánh đồng"

    const val DOWNLOADS_SUMMARY = "8 mục đã tải · 412 MB"

    const val APP_VERSION = "InnerLamp 1.4"
}

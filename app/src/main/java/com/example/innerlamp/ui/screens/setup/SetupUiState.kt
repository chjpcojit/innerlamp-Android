package com.example.innerlamp.ui.screens.setup

import com.example.innerlamp.domain.model.MeditationSetup

/**
 * State render cho màn "Chuẩn bị thiền" (prototype `setup`).
 *
 * Chỉ giữ mức thời lượng đang chọn; tiêu đề buổi thiền và cấu hình chuông hiện
 * là hằng số hiển thị trong màn (prototype không tương tác ở các hàng chuông).
 * TODO(il-data-builder): đọc cấu hình chuông + thời lượng đã lưu từ repository
 * thực hành để giữ giữa các lần mở màn.
 */
data class SetupUiState(
    /** Chỉ số mức thời lượng đang chọn, trỏ vào [MeditationSetup.durationOptions]. */
    val durationIndex: Int = MeditationSetup.defaultDurationIndex,
) {
    /** Các mức thời lượng hiển thị ở dải chip. */
    val durationOptions: List<Int> get() = MeditationSetup.durationOptions

    /** Số phút của mức đang chọn. */
    val durationMinutes: Int get() = MeditationSetup.durationOptions[durationIndex]

    /** Nhãn cho nút "Bắt đầu", vd "20 phút". */
    val durationLabel: String get() = MeditationSetup.durationLabel(durationIndex)
}

package com.example.innerlamp.ui.screens.mala

import com.example.innerlamp.domain.model.Mala

/**
 * State render cho màn Niệm Phật (prototype `mala`).
 *
 * Chỉ giữ các trường thật sự cần render; tiến độ + nhãn tràng tính bằng computed
 * property để dễ test. Chuỗi hạt hiện là trạng thái local-first trong ViewModel,
 * chưa lưu xuống repository (xem TODO trong [MalaViewModel]).
 */
data class MalaUiState(
    /** Số hạt đã niệm trong tràng hiện tại (0..108). */
    val beads: Int = 0,
    /** Số thứ tự tràng đang niệm (bắt đầu từ 1). */
    val round: Int = 1,
    /** Cờ hiệu ứng "pop" khi nhấn hạt: số lớn thụt nhẹ + vòng đánh dấu sáng lên. */
    val pop: Boolean = false,
) {
    /** Tiến độ 0..1 của tràng hiện tại. */
    val progress: Float get() = Mala.progress(beads)

    /** Phụ đề header: "1 tràng" / "2 tràng"... */
    val roundsLabel: String get() = "$round tràng"
}

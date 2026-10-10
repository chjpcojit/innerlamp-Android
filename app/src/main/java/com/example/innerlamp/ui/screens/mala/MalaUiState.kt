package com.example.innerlamp.ui.screens.mala

import com.example.innerlamp.domain.model.Mala

/**
 * State render cho màn Niệm Phật (prototype `mala`).
 *
 * Chỉ giữ các trường thật sự cần render; số biến + tiến độ + nhãn vòng tính bằng computed
 * property để dễ test. Chuỗi hạt hiện là trạng thái local-first trong ViewModel,
 * chưa lưu xuống repository (xem TODO trong [MalaViewModel]).
 */
data class MalaUiState(
    /** Số hạt đã niệm (đếm liên tục; về 0 khi bắt đầu lại / niệm tiếp vòng mới). */
    val beads: Int = 0,
    /** Số thứ tự vòng đang niệm (bắt đầu từ 1). */
    val round: Int = 1,
    /** Số biến của một vòng: 21 / 54 / 108. */
    val target: Int = Mala.BEADS_PER_ROUND,
    /** Câu niệm hiển thị dưới chuỗi hạt. */
    val mantra: String = DefaultMantra,
    /** Đã niệm tròn [target] biến — hiện hộp thoại hồi hướng. */
    val done: Boolean = false,
    /** Cờ hiệu ứng "pop" khi nhấn hạt: vòng đánh dấu sáng lên. */
    val pop: Boolean = false,
) {
    /** Số biến trong vòng hiện tại (1..[target]). */
    val count: Int get() = Mala.count(beads, target)

    /** Tiến độ 0..1 của vòng hiện tại. */
    val progress: Float get() = (count.toFloat() / target).coerceIn(0f, 1f)

    /** Phụ đề header: "Vòng 1 · biến 27 / 108". */
    val roundsLabel: String get() = "Vòng $round · biến $count / $target"

    companion object {
        const val DefaultMantra = "Nam mô A Di Đà Phật"
    }
}

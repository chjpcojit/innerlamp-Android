package com.example.innerlamp.ui.screens.sutra

import com.example.innerlamp.domain.model.SutraDetail

/**
 * State render cho màn "Chi tiết bài kinh" (`detail`).
 *
 * Giữ nội dung bài kinh + vị trí đọc cục bộ (đang ở phẩm nào, tiến độ bao nhiêu %).
 * Các giá trị hiện là mẫu khớp prototype (34%, "Phẩm Song Yếu · kệ 1");
 * TODO(il-data-builder): nối engine đọc/tụng thật khi có tầng player, lúc đó vị trí
 * đọc được cập nhật theo thời gian thực.
 */
data class SutraUiState(
    /** Nội dung bài kinh. */
    val sutra: SutraDetail,
    /** Vị trí đang đọc (vd "Bạn đang ở Phẩm Song Yếu · kệ 1"). */
    val progressLabel: String,
    /** Tiến độ đọc (0f..1f) — prototype đang dừng ở 34%. */
    val progress: Float,
) {
    companion object {
        /** Dựng state ban đầu từ một bài kinh. Vị trí đọc là mẫu tĩnh (34%). */
        fun initial(sutra: SutraDetail): SutraUiState = SutraUiState(
            sutra = sutra,
            progressLabel = "Bạn đang ở Phẩm Song Yếu · kệ 1",
            progress = 0.34f,
        )
    }
}

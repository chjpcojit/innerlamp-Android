package com.example.innerlamp.ui.screens.reader

import com.example.innerlamp.domain.model.SutraReader

/**
 * State render cho màn "Tụng kinh" (`reader`).
 *
 * Giữ nội dung khổ kinh + trạng thái trình phát cục bộ (tiến độ, thời gian,
 * play/pause). Các giá trị thời gian hiện là mẫu khớp prototype (34%);
 * TODO(il-data-builder): nối engine phát audio thật (Media3) khi có tầng player,
 * lúc đó tiến độ và thời gian được cập nhật theo thời gian thực.
 */
data class ReaderUiState(
    /** Nội dung khổ kinh đang tụng. */
    val reader: SutraReader,
    /** Tiến độ phát (0f..1f) — prototype đang dừng ở 34%. */
    val progress: Float,
    /** Thời gian đã tụng (vd "12:04"). */
    val currentTime: String,
    /** Đang tụng (`true`) hay đang tạm dừng (`false`). */
    val isPlaying: Boolean,
) {
    companion object {
        /** Dựng state ban đầu từ một khổ kinh. Tiến độ/thời gian là mẫu tĩnh (34%). */
        fun initial(reader: SutraReader): ReaderUiState = ReaderUiState(
            reader = reader,
            progress = 0.34f,
            currentTime = "12:04",
            isPlaying = true,
        )
    }
}

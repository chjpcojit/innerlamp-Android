package com.example.innerlamp.ui.screens.talk

import com.example.innerlamp.domain.model.TalkDetail

/**
 * State render cho màn "Pháp thoại" (`talk`).
 *
 * Giữ nội dung buổi giảng + trạng thái trình phát cục bộ (tiến độ, thời gian,
 * play/pause). Các giá trị thời gian hiện là mẫu khớp prototype (28% của 46 phút);
 * TODO(il-data-builder): nối engine phát audio thật (ExoPlayer/Media3) khi có tầng
 * player, lúc đó tiến độ và thời gian được cập nhật theo thời gian thực.
 */
data class TalkUiState(
    /** Nội dung buổi pháp thoại. */
    val talk: TalkDetail,
    /** Tiến độ phát (0f..1f) — prototype đang dừng ở 28%. */
    val progress: Float,
    /** Thời gian đã nghe (vd "12:48"). */
    val currentTime: String,
    /** Thời gian còn lại (vd "-33:12"). */
    val remainingTime: String,
    /** Đang phát (`true`) hay đang tạm dừng (`false`). */
    val isPlaying: Boolean,
) {
    companion object {
        /**
         * Dựng state ban đầu từ một buổi pháp thoại. Tiến độ/thời gian là mẫu tĩnh
         * lấy từ prototype (vị trí 28%).
         */
        fun initial(talk: TalkDetail): TalkUiState = TalkUiState(
            talk = talk,
            progress = 0.28f,
            currentTime = "12:48",
            remainingTime = "-33:12",
            isPlaying = true,
        )
    }
}

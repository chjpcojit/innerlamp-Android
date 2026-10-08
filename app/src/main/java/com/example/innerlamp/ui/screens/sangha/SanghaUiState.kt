package com.example.innerlamp.ui.screens.sangha

import com.example.innerlamp.domain.model.MeditationGroup
import com.example.innerlamp.domain.model.SanghaPost
import com.example.innerlamp.domain.model.SanghaSummary

/**
 * State render cho màn Đạo tràng (`sangha`).
 *
 * Màn này gần như tĩnh: mọi tương tác đều là điều hướng (qua [SanghaCallbacks]),
 * nên state chỉ gom dữ liệu hiển thị — không có form hay trạng thái tải cục bộ.
 */
data class SanghaUiState(
    /** Thẻ "Đang cùng ngồi thiền". */
    val meditation: MeditationGroup,
    /** Khối "Đạo tràng của tôi". */
    val mySangha: SanghaSummary,
    /** Bảng tin "Chia sẻ của bạn đồng tu". */
    val posts: List<SanghaPost>,
)

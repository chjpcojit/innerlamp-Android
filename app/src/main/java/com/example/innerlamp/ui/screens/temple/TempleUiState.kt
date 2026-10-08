package com.example.innerlamp.ui.screens.temple

import com.example.innerlamp.domain.model.TempleDetail

/**
 * State render cho màn "Trang chùa" (`temple`).
 *
 * Giữ nội dung chùa + trạng thái theo dõi. `isFollowing` là trạng thái trình bày cục bộ
 * (prototype chưa có handler "Theo dõi"); TODO(il-data-builder): lưu danh sách chùa đang
 * theo dõi xuống repository khi có data layer.
 */
data class TempleUiState(
    /** Nội dung trang chùa. */
    val detail: TempleDetail,
    /** Đang theo dõi chùa hay chưa — đổi nhãn nút "Theo dõi" ↔ "Đang theo dõi". */
    val isFollowing: Boolean,
) {
    /** Nhãn nút theo dõi theo trạng thái. */
    val followLabel: String get() = if (isFollowing) "Đang theo dõi" else "Theo dõi"
}

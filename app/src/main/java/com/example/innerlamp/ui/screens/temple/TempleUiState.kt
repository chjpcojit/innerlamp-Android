package com.example.innerlamp.ui.screens.temple

import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.ui.components.ContentStatus

/**
 * Render state of the "Trang chùa" screen. [detail] is set only when [status] is `Loaded`. /
 * State render của màn "Trang chùa". [detail] chỉ có khi [status] là `Loaded`.
 */
data class TempleUiState(
    val status: ContentStatus = ContentStatus.Loading,
    val detail: TempleDetail? = null,
    val introExpanded: Boolean = false,
) {
    /** Three-column stats: founding year, type, region (missing → "—"). / Ba cột thống kê. */
    val stats: List<Pair<String, String>>
        get() {
            val d = detail ?: return emptyList()
            return listOf(
                (d.establishedYear?.toString() ?: NONE) to "Năm lập",
                (d.type ?: NONE) to "Loại",
                (d.region ?: NONE) to "Miền",
            )
        }

    /** Hero subtitle "Ninh Bình · Bắc tông". / Phụ đề hero. */
    val subtitle: String
        get() = detail?.let { listOfNotNull(it.place.takeIf(String::isNotBlank), it.sect).joinToString(" · ") }.orEmpty()

    /** Label/value rows of the "Thông tin" card; empty values are left out. / Các hàng "Thông tin". */
    val infoRows: List<Pair<String, String>>
        get() {
            val d = detail ?: return emptyList()
            return listOfNotNull(
                d.aliases.takeIf { it.isNotEmpty() }?.let { "Tên khác" to it.joinToString(", ") },
                d.heritageRank?.let { "Xếp hạng" to it },
                d.founder?.let { "Người sáng lập" to it },
                d.currentAbbot?.let { "Trụ trì" to it },
                "Giờ mở cửa" to (d.operatingHours ?: UPDATING),
            )
        }

    /** Amenities the temple has, as labels; empty → "Đang cập nhật" on screen. / Tiện ích chùa có. */
    val amenityLabels: List<String>
        get() {
            val a = detail?.amenities ?: return emptyList()
            return listOfNotNull(
                "Bãi đỗ ô tô".takeIf { a.parkingCar },
                "Gửi xe máy".takeIf { a.parkingMotorcycle },
                "Cơm chay".takeIf { a.vegetarianMeals },
                "Khóa tu mùa hè".takeIf { a.summerRetreat },
            )
        }

    private companion object {
        const val NONE = "—"
        const val UPDATING = "Đang cập nhật"
    }
}

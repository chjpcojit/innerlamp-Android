package com.example.innerlamp.ui.screens.buddha

import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.ui.components.ContentStatus

/**
 * State render cho màn "Chư Phật & Bồ Tát" (`buddhas`). The catalogue is small (one page), so the chips
 * filter on the device. / Danh mục nhỏ (một trang) nên chip lọc trên máy.
 *
 * @property type chip lọc đang chọn; `null` = "Tất cả".
 */
data class BuddhaListUiState(
    val deities: List<DeitySummary> = emptyList(),
    val status: ContentStatus = ContentStatus.Loading,
    val type: DeityType? = null,
) {
    /** Danh sách sau khi lọc theo chip. */
    val filtered: List<DeitySummary>
        get() = if (type == null) deities else deities.filter { it.type == type }

    /** Phụ đề header (prototype `bdSub`). */
    val subtitle: String
        get() = if (status == ContentStatus.Loaded) "${deities.size} vị · tiểu sử, đại nguyện, thần chú" else "Tiểu sử, đại nguyện, thần chú"
}

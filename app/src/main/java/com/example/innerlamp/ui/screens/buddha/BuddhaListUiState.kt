package com.example.innerlamp.ui.screens.buddha

import com.example.innerlamp.domain.model.BuddhaCategory
import com.example.innerlamp.domain.model.BuddhaProfile

/**
 * State render cho màn "Chư Phật & Bồ Tát" (`buddhas`).
 *
 * @property category chip lọc đang chọn; `null` = "Tất cả".
 */
data class BuddhaListUiState(
    val profiles: List<BuddhaProfile>,
    val category: BuddhaCategory? = null,
) {
    /** Danh sách sau khi lọc theo chip. */
    val filtered: List<BuddhaProfile>
        get() = if (category == null) profiles else profiles.filter { it.category == category }

    /** Phụ đề header (prototype `bdSub`). */
    val subtitle: String get() = "${profiles.size} vị · tiểu sử, đại nguyện, thần chú"
}

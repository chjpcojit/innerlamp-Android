package com.example.innerlamp.ui.screens.sutralist

import com.example.innerlamp.domain.model.SutraCatalogItem
import com.example.innerlamp.domain.model.SutraLengthFilter
import com.example.innerlamp.domain.model.SutraTagFilter
import com.example.innerlamp.domain.model.stripVietnamese

/**
 * State màn "Kinh sách": danh mục + 3 bộ lọc (từ khóa, chủ đề/tông phái, độ dài).
 */
data class SutraListUiState(
    val catalog: List<SutraCatalogItem> = emptyList(),
    val query: String = "",
    val tag: SutraTagFilter = SutraTagFilter.All,
    val length: SutraLengthFilter = SutraLengthFilter.Any,
) {
    /** Danh sách sau khi áp dụng cả ba bộ lọc (prototype `skList`). */
    val visible: List<SutraCatalogItem>
        get() {
            val q = query.stripVietnamese().trim()
            return catalog.filter { item ->
                tag.matches(item) &&
                    length.matches(item.minutes) &&
                    (q.isEmpty() || item.searchText().contains(q))
            }
        }

    val isFiltering: Boolean
        get() = tag != SutraTagFilter.All || length != SutraLengthFilter.Any || query.isNotBlank()

    /** Phụ đề header, vd "11 bộ · Bắc tông & Nam tông". */
    val subtitle: String get() = "${catalog.size} bộ · Bắc tông & Nam tông"

    /** Dòng đếm kết quả, vd "3 bản kinh phù hợp". */
    val countLabel: String
        get() = "${visible.size} bản kinh" + if (isFiltering) " phù hợp" else ""

    private fun SutraCatalogItem.searchText(): String =
        "$name $original $description ${tags.joinToString(" ")}".stripVietnamese()
}

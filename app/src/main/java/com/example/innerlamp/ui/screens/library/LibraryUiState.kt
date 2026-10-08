package com.example.innerlamp.ui.screens.library

import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryCatalog
import com.example.innerlamp.domain.model.LibraryCategory
import com.example.innerlamp.domain.model.Sutra

/**
 * State của màn Thư viện.
 *
 * `selectedCategory == null` nghĩa là chip "Tất cả" đang được chọn (không lọc).
 * Danh sách hiển thị `filteredSutras` là dẫn xuất — không lưu riêng.
 */
data class LibraryUiState(
    val categories: List<LibraryCategory>,
    val selectedCategory: LibraryCategory?,
    val featuredTalk: FeaturedTalk,
    val sutras: List<Sutra>,
) {
    /** Bài kinh sau khi lọc theo chip đang chọn. */
    val filteredSutras: List<Sutra>
        get() = if (selectedCategory == null) sutras
        else sutras.filter { it.category == selectedCategory }

    companion object {
        /** Dựng state ban đầu từ catalog; chip mặc định là "Tất cả". */
        fun from(catalog: LibraryCatalog): LibraryUiState = LibraryUiState(
            categories = catalog.categories,
            selectedCategory = null,
            featuredTalk = catalog.featuredTalk,
            sutras = catalog.sutras,
        )
    }
}

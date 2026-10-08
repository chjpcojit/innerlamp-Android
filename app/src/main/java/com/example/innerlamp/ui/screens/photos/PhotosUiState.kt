package com.example.innerlamp.ui.screens.photos

import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.TemplePhoto

/**
 * State render cho màn "Ảnh chùa đẹp" (`photos`).
 *
 * `selectedCategory == null` nghĩa là chip "Tất cả" đang được chọn (không lọc).
 * Danh sách hiển thị `filteredPhotos` và phụ đề header `countLabel` đều là dẫn xuất —
 * không lưu riêng.
 */
data class PhotosUiState(
    /** Các mục lọc (không gồm "Tất cả"). */
    val categories: List<PhotoCategory>,
    /** Chip đang chọn; `null` = "Tất cả". */
    val selectedCategory: PhotoCategory?,
    /** Toàn bộ ảnh (chưa lọc). */
    val photos: List<TemplePhoto>,
) {
    /** Ảnh sau khi lọc theo chip đang chọn. */
    val filteredPhotos: List<TemplePhoto>
        get() = if (selectedCategory == null) photos
        else photos.filter { it.category == selectedCategory }

    /** Phụ đề header: "N ảnh" (+ " · <danh mục>" khi đang lọc). */
    val countLabel: String
        get() = buildString {
            append(filteredPhotos.size)
            append(" ảnh")
            selectedCategory?.let { append(" · ").append(it.label) }
        }

    companion object {
        /** Dựng state ban đầu từ danh sách ảnh; chip mặc định là "Tất cả". */
        fun from(photos: List<TemplePhoto>): PhotosUiState = PhotosUiState(
            categories = PhotoCategory.entries,
            selectedCategory = null,
            photos = photos,
        )
    }
}

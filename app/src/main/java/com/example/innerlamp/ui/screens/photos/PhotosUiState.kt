package com.example.innerlamp.ui.screens.photos

import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.ui.components.ContentStatus

/**
 * Render state of the "Ảnh đẹp" screen (`photos`), read from `GET /v1/gallery`. /
 * State render cho màn "Ảnh đẹp" (`photos`), đọc từ `GET /v1/gallery`.
 *
 * `selectedCategory == null` means the "Tất cả" chip (no filter). `filteredPhotos` and `countLabel` are
 * derived, not stored. /
 * `selectedCategory == null` nghĩa là chip "Tất cả" (không lọc). `filteredPhotos` và `countLabel` là
 * dẫn xuất, không lưu riêng.
 *
 * @property status load status of the list. / trạng thái tải danh sách.
 * @property viewing photo open in the lightbox, `null` when closed. / ảnh đang mở trong lightbox.
 * @property credit credit of [viewing] once `GET /v1/gallery/{id}` answers; `null` while loading or on
 * error (the lightbox then shows no credit line). / ghi công của ảnh đang xem; `null` khi đang tải hoặc lỗi.
 */
data class PhotosUiState(
    val photos: List<TemplePhoto> = emptyList(),
    val status: ContentStatus = ContentStatus.Loading,
    /** Chip đang chọn; `null` = "Tất cả". */
    val selectedCategory: PhotoCategory? = null,
    val viewing: TemplePhoto? = null,
    val credit: PhotoCredit? = null,
) {
    /** Filter chips (without "Tất cả"). / Các mục lọc (không gồm "Tất cả"). */
    val categories: List<PhotoCategory> get() = PhotoCategory.entries

    /** Ảnh sau khi lọc theo chip đang chọn. */
    val filteredPhotos: List<TemplePhoto>
        get() = if (selectedCategory == null) photos
        else photos.filter { it.category == selectedCategory }

    /**
     * Header subtitle: "N ảnh" (+ " · <category>" when filtered); a generic line until the list loads. /
     * Phụ đề header: "N ảnh" (+ " · <danh mục>" khi đang lọc); câu chung khi chưa tải xong.
     */
    val countLabel: String
        get() = if (status != ContentStatus.Loaded) {
            "Ảnh chùa, tượng Phật, cảnh thiền"
        } else {
            buildString {
                append(filteredPhotos.size)
                append(" ảnh")
                selectedCategory?.let { append(" · ").append(it.label) }
            }
        }

    /** Credit line of the lightbox, e.g. "Ảnh: Nguyễn A · CC BY-SA 4.0". / Dòng ghi công của lightbox. */
    val creditLine: String?
        get() {
            val c = credit ?: return null
            val parts = listOfNotNull(c.author?.let { "Ảnh: $it" }, c.license)
            return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        }
}

package com.example.innerlamp.ui.screens.article

import com.example.innerlamp.domain.model.ArticleDetail

/**
 * State render cho màn "Bài viết Phật pháp" (`article`).
 *
 * Giữ bài đang đọc, 2 bài gợi ý "Đọc tiếp" và trạng thái lưu. `isSaved` là trạng
 * thái trình bày cục bộ (prototype `toggleArtSave`); TODO(il-data-builder): lưu
 * danh sách bài đã lưu xuống repository khi có data layer.
 */
data class ArticleUiState(
    /** Bài đang đọc. */
    val article: ArticleDetail,
    /** Hai bài gợi ý ở khối "Đọc tiếp" (theo vòng tròn sau bài hiện tại). */
    val nextArticles: List<ArticleDetail>,
    /** Đã lưu bài hay chưa — đổi nhãn nút "Lưu" ↔ "Đã lưu". */
    val isSaved: Boolean,
) {
    /** Nhãn nút lưu theo trạng thái (khớp `artSaveLabel` của prototype). */
    val saveLabel: String get() = if (isSaved) "Đã lưu" else "Lưu"

    companion object {
        /**
         * Dựng state từ danh sách bài viết + id bài đang chọn. Hai bài "Đọc tiếp"
         * lấy theo vòng tròn sau bài hiện tại (khớp `nextArts` của prototype:
         * `(artIdx + k) % 5`).
         */
        fun from(articles: List<ArticleDetail>, articleId: String): ArticleUiState {
            val index = articles.indexOfFirst { it.id == articleId }.coerceAtLeast(0)
            val next = (1..2).map { k -> articles[(index + k) % articles.size] }
            return ArticleUiState(
                article = articles[index],
                nextArticles = next,
                isSaved = false,
            )
        }
    }
}

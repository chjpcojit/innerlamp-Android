package com.example.innerlamp.ui.screens.articlelist

import com.example.innerlamp.domain.model.Article

/**
 * State màn "Tất cả bài viết".
 *
 * @property all toàn bộ bài viết sẵn có (nguồn phân trang phía client).
 * @property visibleCount số bài đã nạp vào danh sách.
 * @property isLoadingMore đang nạp thêm một lượt.
 */
data class ArticleListUiState(
    val all: List<Article> = emptyList(),
    val visibleCount: Int = PAGE_SIZE,
    val isLoadingMore: Boolean = false,
) {
    val visible: List<Article> get() = all.take(visibleCount)

    val hasMore: Boolean get() = visibleCount < all.size

    /** Hết danh sách và không còn đang tải → hiện "Bạn đã xem hết bài viết". */
    val isEnd: Boolean get() = all.isNotEmpty() && !hasMore && !isLoadingMore

    /** Phụ đề header, vd "20 / 60 bài" (prototype `artsSub`). */
    val subtitle: String get() = "${visible.size} / ${all.size} bài"

    companion object {
        const val PAGE_SIZE = 20
    }
}

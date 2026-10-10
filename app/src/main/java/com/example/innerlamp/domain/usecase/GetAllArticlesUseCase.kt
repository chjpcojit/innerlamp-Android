package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Article

/**
 * Trả về toàn bộ bài viết Phật pháp cho màn "Tất cả bài viết" ([TOTAL] bài).
 *
 * Hiện là dữ liệu mẫu: lặp vòng 5 bài biên tập của [GetArticlesUseCase] (như prototype
 * `allArts`, `n % 5`) — nên `id` lặp lại để mở đúng bài chi tiết.
 *
 * TODO(il-data-builder): chuyển sang `ArticleRepository` có phân trang thật (page/cursor).
 */
class GetAllArticlesUseCase(
    private val getArticles: GetArticlesUseCase = GetArticlesUseCase(),
) {
    operator fun invoke(): List<Article> {
        val base = getArticles()
        return List(TOTAL) { n ->
            val a = base[n % base.size]
            Article(a.id, a.category, a.title, a.author, a.readTime)
        }
    }

    companion object {
        const val TOTAL = 60
    }
}

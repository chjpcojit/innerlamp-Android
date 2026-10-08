package com.example.innerlamp.ui.screens.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.ArticleDetail
import com.example.innerlamp.domain.usecase.GetArticlesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Bài viết Phật pháp" (`article`).
 *
 * - Nhận id bài viết từ route arg (`ArticleRoute.articleId`) qua [SavedStateHandle].
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Nội dung lấy từ [GetArticlesUseCase] (hiện là dữ liệu mẫu trong domain).
 *   TODO(il-data-builder): nối `ArticleRepository` khi data layer có; lúc đó use case
 *   trả `Result<List<ArticleDetail>>` để UI xử lý được lỗi mạng.
 *
 * @author innerlamp
 */
class ArticleViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val getArticles = GetArticlesUseCase()
    private val articles: List<ArticleDetail> = getArticles()

    // id bài viết từ route; không có (preview/fallback) thì dùng bài đầu tiên.
    private val articleId: String = savedStateHandle.get<String>(KEY_ARTICLE_ID)
        ?: articles.first().id

    private val _uiState = MutableStateFlow(ArticleUiState.from(articles, articleId))
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    /** Đổi trạng thái "Lưu" ↔ "Đã lưu" (prototype `toggleArtSave`). */
    fun toggleSaved() {
        _uiState.update { it.copy(isSaved = !it.isSaved) }
    }

    private companion object {
        /** Tên arg route — khớp property `articleId` của `ArticleRoute`. */
        const val KEY_ARTICLE_ID = "articleId"
    }
}

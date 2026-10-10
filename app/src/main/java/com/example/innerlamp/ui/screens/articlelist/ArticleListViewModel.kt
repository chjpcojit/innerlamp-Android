package com.example.innerlamp.ui.screens.articlelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.GetAllArticlesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel màn "Tất cả bài viết": nạp thêm [ArticleListUiState.PAGE_SIZE] bài mỗi lần cuộn tới cuối. */
class ArticleListViewModel(
    getAllArticles: GetAllArticlesUseCase = GetAllArticlesUseCase(),
    private val loadDelayMs: Long = 900,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState(all = getAllArticles()))
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    /** Gọi khi cuộn gần cuối; bỏ qua nếu đang tải hoặc đã hết. */
    fun loadMore() {
        val s = _uiState.value
        if (s.isLoadingMore || !s.hasMore) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            delay(loadDelayMs) // mô phỏng độ trễ mạng như prototype
            _uiState.update {
                it.copy(
                    visibleCount = (it.visibleCount + ArticleListUiState.PAGE_SIZE).coerceAtMost(it.all.size),
                    isLoadingMore = false,
                )
            }
        }
    }
}

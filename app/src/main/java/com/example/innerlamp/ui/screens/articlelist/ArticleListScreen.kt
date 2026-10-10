package com.example.innerlamp.ui.screens.articlelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.usecase.GetAllArticlesUseCase
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.ArticleCard
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Tất cả bài viết" — dựng từ prototype `Docs/innerlamp-project/` (màn `arts`):
// header dán (tiêu đề + "n / tổng bài"), danh sách thẻ bài viết cuộn vô hạn, dòng
// "Đang tải thêm bài viết…" khi nạp và "Bạn đã xem hết bài viết" khi hết.
// ─────────────────────────────────────────────────────────────────────────────

/** Các điểm điều hướng đi ra khỏi màn "Tất cả bài viết". */
@Immutable
class ArticleListCallbacks(
    val onBack: () -> Unit = {},
    val onArticle: (articleId: String) -> Unit = {},
)

@Composable
fun ArticleListScreen(
    modifier: Modifier = Modifier,
    callbacks: ArticleListCallbacks = ArticleListCallbacks(),
    viewModel: ArticleListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArticleListContent(state, callbacks, viewModel::loadMore, modifier)
}

/** Phần stateless của [ArticleListScreen] — dùng cho preview và test. */
@Composable
internal fun ArticleListContent(
    state: ArticleListUiState,
    callbacks: ArticleListCallbacks,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val listState = rememberLazyListState()
    val loadMore by rememberUpdatedState(onLoadMore)

    // Cuộn tới 3 thẻ cuối thì nạp thêm (ViewModel tự bỏ qua nếu đang tải / đã hết).
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - 3
        }.collect { nearEnd -> if (nearEnd) loadMore() }
    }

    // Header đè lên danh sách, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = {
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE / 2
        },
        header = {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(title = "Bài viết Phật pháp hay", sub = state.subtitle, showBack = true, onBack = callbacks.onBack)
            }
        },
        modifier = modifier,
    ) { headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = headerHeight + 16.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(state.visible, key = { index, _ -> index }) { _, article ->
                ArticleCard(article = article, onClick = { callbacks.onArticle(article.id) })
            }
            if (state.isLoadingMore) {
                item(key = "loading") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = colors.link,
                            trackColor = colors.ink.copy(alpha = 0.12f),
                        )
                        Text("Đang tải thêm bài viết…", style = footnote(colors.ink.copy(alpha = 0.45f)))
                    }
                }
            }
            if (state.isEnd) {
                item(key = "end") {
                    Text(
                        text = "Bạn đã xem hết bài viết",
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).navigationBarsPadding(),
                        textAlign = TextAlign.Center,
                        style = footnote(colors.ink.copy(alpha = 0.45f)),
                    )
                }
            }
        }
    }
}

private fun footnote(color: androidx.compose.ui.graphics.Color) =
    TextStyle(fontFamily = BeVietnamPro, fontSize = 12.sp, color = color)

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ArticleListPreview() {
    InnerLampTheme {
        ArticleListContent(
            state = ArticleListUiState(all = GetAllArticlesUseCase()(), isLoadingMore = true),
            callbacks = ArticleListCallbacks(),
            onLoadMore = {},
        )
    }
}

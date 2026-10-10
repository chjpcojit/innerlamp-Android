package com.example.innerlamp.ui.screens.article

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.ArticleDetail
import com.example.innerlamp.domain.usecase.GetArticlesUseCase
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Bài viết Phật pháp" (`article`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `article`, mảng `Component.ARTICLES`).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Các điểm điều hướng đi ra khỏi màn "Bài viết Phật pháp". Màn không giữ
 * `NavController`: mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class ArticleCallbacks(
    val onBack: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onArticleClick: (articleId: String) -> Unit = {},
)

/**
 * Màn hình "Bài viết Phật pháp" — đọc một bài biên tập.
 *
 * Bố cục (trên → dưới): header dán (danh mục + thời lượng đọc) → ảnh minh họa →
 * tiêu đề → hàng tác giả → thân bài → trích dẫn (viền vàng) → nút Lưu / Chia sẻ →
 * khối "Đọc tiếp" (2 bài gợi ý).
 *
 * @author innerlamp
 */
@Composable
fun ArticleScreen(
    modifier: Modifier = Modifier,
    callbacks: ArticleCallbacks = ArticleCallbacks(),
    viewModel: ArticleViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArticleContent(
        state = state,
        callbacks = callbacks,
        onToggleSaved = viewModel::toggleSaved,
        modifier = modifier,
    )
}

/** Phần stateless của [ArticleScreen] — dùng cho preview và test. */
@Composable
internal fun ArticleContent(
    state: ArticleUiState,
    callbacks: ArticleCallbacks,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    // Header đè lên nội dung, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            ArticleHeader(
                category = state.article.category,
                sub = state.article.readTime,
                onBack = callbacks.onBack,
            )
        },
        modifier = modifier,
    ) { headerHeight ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(headerHeight + 18.dp))
            ArticleHero()

            Spacer(Modifier.height(20.dp))
            Text(
                text = state.article.title,
                style = serif(26f, colors.ink, lineHeight = 33.8f.sp),
            )

            Spacer(Modifier.height(14.dp))
            AuthorRow(article = state.article)

            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.08f)))

            Spacer(Modifier.height(4.dp))
            state.article.paragraphs.forEach { paragraph ->
                Spacer(Modifier.height(14.dp))
                Text(
                    text = paragraph,
                    style = serif(16f, colors.ink.copy(alpha = 0.8f), lineHeight = 29.6f.sp),
                )
            }

            Spacer(Modifier.height(22.dp))
            QuoteBlock(article = state.article)

            Spacer(Modifier.height(28.dp))
            SaveShareRow(
                saveLabel = state.saveLabel,
                onSave = onToggleSaved,
                onShare = callbacks.onShare,
            )

            Spacer(Modifier.height(30.dp))
            Text(
                text = "Đọc tiếp",
                style = sans(13.5f, colors.ink, FontWeight.Medium),
            )

            Spacer(Modifier.height(6.dp))
            state.nextArticles.forEach { next ->
                NextArticleRow(
                    article = next,
                    onClick = { callbacks.onArticleClick(next.id) },
                )
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** Header: `IlNavBar` (danh mục + thời lượng đọc). Blur + hairline do [IlBlurHeaderScaffold] lo. */
@Composable
private fun ArticleHeader(
    category: String,
    sub: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 8.dp)) {
        IlNavBar(title = category, sub = sub, showBack = true, onBack = onBack)
    }
}

/**
 * Ảnh minh họa bài viết (210dp, bo 22dp). Prototype dùng `<image-slot>`; app chưa có
 * ảnh thật nên vẽ gradient `sunk2 → tint` + nhãn gợi ý.
 *
 * TODO(il-data-builder): thay bằng ảnh thật (Coil/`painterResource`) khi có asset/API.
 */
@Composable
private fun ArticleHero(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Ảnh minh họa bài viết",
            style = sans(11f, colors.ink.copy(alpha = 0.42f), FontWeight.Medium, letterSpacing = 1.2f.sp),
        )
    }
}

/** Hàng tác giả: avatar tròn 32dp (chữ viết tắt) + tên + "ngày · thời lượng đọc". */
@Composable
private fun AuthorRow(
    article: ArticleDetail,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(IlGold.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = article.initials,
                style = sans(11.5f, colors.link, FontWeight.Medium),
            )
        }
        Column {
            Text(
                text = article.author,
                style = sans(12.5f, colors.ink, FontWeight.Medium),
            )
            Text(
                text = "${article.date} · ${article.readTime}",
                modifier = Modifier.padding(top = 1.dp),
                style = sans(11f, colors.ink.copy(alpha = 0.45f)),
            )
        }
    }
}

/** Trích dẫn cuối bài: thanh vàng bên trái + chữ Lora nghiêng + nguồn trích. */
@Composable
private fun QuoteBlock(
    article: ArticleDetail,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Box(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(IlGold.copy(alpha = 0.5f)),
        )
        Column(Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
            Text(
                text = article.quote,
                style = serif(18f, colors.ink, lineHeight = 28.8f.sp, italic = true),
            )
            Text(
                text = article.quoteSource,
                modifier = Modifier.padding(top = 8.dp),
                style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            )
        }
    }
}

/** Hai nút song song (48dp): Lưu (ghost) + Chia sẻ (primary). */
@Composable
private fun SaveShareRow(
    saveLabel: String,
    onSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ArticlePillButton(
            label = saveLabel,
            primary = false,
            onClick = onSave,
            modifier = Modifier.weight(1f),
        )
        ArticlePillButton(
            label = "Chia sẻ",
            primary = true,
            onClick = onShare,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Nút pill cao 48dp của màn bài viết (khác `IlButton` 50dp — giữ đúng pixel prototype). */
@Composable
private fun ArticlePillButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val bg = if (primary) colors.ink else colors.card
    val fg = if (primary) colors.canvas else colors.ink
    val bd = if (primary) colors.ink else colors.ink.copy(alpha = 0.18f)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(999.dp))
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = sans(13f, fg, FontWeight.Medium),
        )
    }
}

/** Hàng "Đọc tiếp": eyebrow danh mục + tiêu đề + chevron, viền `ink-07` ở đáy. */
@Composable
private fun NextArticleRow(
    article: ArticleDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = colors.ink.copy(alpha = 0.07f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(top = 14.dp, bottom = 14.dp)
            .tappable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = article.category.uppercase(),
                style = sans(9.5f, colors.link, FontWeight.SemiBold, letterSpacing = 1.14f.sp),
            )
            Text(
                text = article.title,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = serif(15f, colors.ink, lineHeight = 21f.sp),
            )
        }
        Text(
            text = "›",
            style = sans(18f, colors.ink.copy(alpha = 0.35f)),
        )
    }
}

/** Vùng chạm không có ripple (prototype chỉ đổi nền, không ripple). */
@Composable
private fun Modifier.tappable(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    role = Role.Button,
    onClick = onClick,
)

private fun sans(
    size: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    color = color,
)

private fun serif(
    size: Float,
    color: Color,
    lineHeight: TextUnit = TextUnit.Unspecified,
    italic: Boolean = false,
) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight,
    color = color,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun ArticlePreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        ArticleContent(
            state = previewState(),
            callbacks = ArticleCallbacks(),
            onToggleSaved = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun ArticlePreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        ArticleContent(
            state = previewState(),
            callbacks = ArticleCallbacks(),
            onToggleSaved = {},
        )
    }
}

private fun previewState(): ArticleUiState =
    ArticleUiState.from(GetArticlesUseCase()(), "art-1")

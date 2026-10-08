package com.example.innerlamp.ui.screens.sutra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.SutraBadge
import com.example.innerlamp.domain.model.SutraBadgeStyle
import com.example.innerlamp.domain.model.SutraChapter
import com.example.innerlamp.domain.model.SutraDetail
import com.example.innerlamp.domain.usecase.GetSutraDetailUseCase
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Chi tiết bài kinh" (`detail`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `detail`).
// ─────────────────────────────────────────────────────────────────────────────

/** Khoảng đỉnh hero để nhường chỗ header dán (94px = 56 status + 38 nav của prototype). */
private val HeroTopPadding = 94.dp

/** Đường kính nút play tròn ở thanh CTA đáy màn (prototype `52px`). */
private val PlayButtonSize = 52.dp

/**
 * Các điểm điều hướng đi ra khỏi màn "Chi tiết bài kinh". Màn không giữ
 * `NavController`: mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class SutraCallbacks(
    val onBack: () -> Unit = {},
    val onDownload: () -> Unit = {},
    val onReader: () -> Unit = {},
    val onPlayer: () -> Unit = {},
)

/**
 * Màn hình "Chi tiết bài kinh" — trang giới thiệu một bài kinh.
 *
 * Bố cục (trên → dưới): hero gradient (quầng sáng + kicker + tiêu đề + meta + nhãn,
 * tan vào `canvas`) → thẻ tiến độ đọc → mô tả → "Mục lục" các phẩm → thanh CTA dán
 * đáy ("Tụng theo" + nút phát). Header trong suốt nằm đè trên hero, nút "Tải về máy".
 *
 * @author innerlamp
 */
@Composable
fun SutraScreen(
    modifier: Modifier = Modifier,
    callbacks: SutraCallbacks = SutraCallbacks(),
    viewModel: SutraViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SutraContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier,
    )
}

/** Phần stateless của [SutraScreen] — dùng cho preview và test. */
@Composable
internal fun SutraContent(
    state: SutraUiState,
    callbacks: SutraCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SutraHero(detail = state.sutra)

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                ReadingProgressCard(state = state)

                Spacer(Modifier.height(18.dp))
                Text(
                    text = state.sutra.description,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 24.5f.sp,
                        color = colors.ink.copy(alpha = 0.72f),
                    ),
                )

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "Mục lục",
                        style = sans(13.5f, colors.ink, FontWeight.Medium),
                    )
                    Text(
                        text = state.sutra.chapterCount,
                        style = sans(11.5f, colors.ink.copy(alpha = 0.45f)),
                    )
                }

                Spacer(Modifier.height(12.dp))
                ChapterList(chapters = state.sutra.chapters, onChapterClick = callbacks.onReader)
            }
            // Nhường chỗ cho thanh CTA dán đáy (14 + 50 + 30).
            Spacer(Modifier.height(94.dp))
        }

        // Header trong suốt đè lên hero (back + "Tải về máy"), ghim trên cùng khi cuộn.
        IlNavBar(
            title = "",
            right = "Tải về máy",
            dark = colors.isDark,
            showBack = true,
            onBack = callbacks.onBack,
            onAction = callbacks.onDownload,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        )

        BottomBar(
            onReader = callbacks.onReader,
            onPlayer = callbacks.onPlayer,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Hero: gradient `--il-th-bg` (tan vào canvas) + quầng sáng giữa đỉnh + kicker +
 * tiêu đề Lora + meta + các nhãn phụ (đầy/viền).
 */
@Composable
private fun SutraHero(
    detail: SutraDetail,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to colors.thBgTop,
                    0.58f to colors.thBgMid,
                    1f to colors.canvas,
                ),
            ),
    ) {
        // Quầng sáng 320dp giữa đỉnh, trồi lên trên 90dp (khớp `top:-90px` của prototype).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-90).dp)
                .size(320.dp)
                .background(Brush.radialGradient(listOf(colors.thGlow, Color.Transparent)), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = HeroTopPadding, bottom = 30.dp),
        ) {
            // Kicker có margin-top 30px so với padding hero (khớp prototype).
            Spacer(Modifier.height(30.dp))
            Text(
                text = detail.kicker,
                style = sans(9.5f, colors.featKicker, FontWeight.SemiBold, letterSpacing = 1.52f.sp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = detail.title,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 29.sp,
                    lineHeight = 37.sp,
                    color = colors.featTitle, // --il-th-ink
                ),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = detail.meta,
                style = sans(12.5f, colors.featSub, lineHeight = 20.sp),
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.badges.forEach { badge ->
                    HeroBadge(badge = badge)
                }
            }
        }
    }
}

/** Nhãn phụ trên hero: `Filled` (nền `--il-feat-glow` + chữ `--il-hero-acc`) / `Outlined` (viền). */
@Composable
private fun HeroBadge(
    badge: SutraBadge,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Pill)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (badge.style == SutraBadgeStyle.Filled) {
                    Modifier.background(colors.featGlow)
                } else {
                    Modifier.border(1.dp, colors.featAcc2, shape)
                },
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge.label,
            style = sans(
                10.5f,
                if (badge.style == SutraBadgeStyle.Filled) colors.featKicker else colors.featSub,
                FontWeight.Medium,
            ),
        )
    }
}

/** Thẻ tiến độ đọc: vị trí hiện tại + phần trăm + thanh tiến độ (34%). */
@Composable
private fun ReadingProgressCard(
    state: SutraUiState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = state.progressLabel,
                style = sans(12.5f, colors.ink, FontWeight.Medium),
            )
            Text(
                text = "${(state.progress * 100).toInt()}%",
                style = sans(11.5f, colors.ink.copy(alpha = 0.45f)),
            )
        }
        Spacer(Modifier.height(12.dp))
        // Track ink-09 + fill 34% gradient vàng (khớp `linear-gradient(90deg,#D9963C,#B8862F)`).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.ink.copy(alpha = 0.09f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progress)
                    .height(5.dp)
                    .background(Brush.horizontalGradient(listOf(IlGoldBright, IlGold))),
            )
        }
    }
}

/** Danh sách "Mục lục": phẩm đang tụng (đầu tiên) nền vàng nhạt + số nổi link. */
@Composable
private fun ChapterList(
    chapters: List<SutraChapter>,
    onChapterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
    ) {
        chapters.forEachIndexed { index, chapter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (chapter.isActive) Modifier.background(IlGold.copy(alpha = 0.08f)) else Modifier,
                    )
                    .then(
                        if (index < chapters.lastIndex) {
                            Modifier.drawBottomHairline(colors.ink.copy(alpha = 0.06f))
                        } else Modifier,
                    )
                    .tappable(onClick = onChapterClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = chapter.num,
                    modifier = Modifier.width(26.dp),
                    style = if (chapter.isActive) {
                        sans(11.5f, colors.link, FontWeight.Medium)
                    } else {
                        sans(11.5f, colors.ink.copy(alpha = 0.45f), FontWeight.Medium)
                    },
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = chapter.name,
                        style = sans(13f, colors.ink, FontWeight.Medium),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (chapter.isActive) "${chapter.sub} · đang tụng" else chapter.sub,
                        style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
                    )
                }
                Text(
                    text = chapter.time,
                    style = sans(11f, colors.ink.copy(alpha = 0.40f)),
                )
            }
        }
    }
}

/** Thanh CTA dán đáy: nút "Tụng theo" + nút phát tròn, trên nền gradient tan vào canvas. */
@Composable
private fun BottomBar(
    onReader: () -> Unit,
    onPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to colors.canvas.copy(alpha = 0f),
                    0.3f to colors.canvas,
                    1f to colors.canvas,
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IlButton(
                label = "Tụng theo",
                modifier = Modifier.weight(1f),
                full = false,
                onClick = onReader,
            )
            PlayCircle(onClick = onPlayer)
        }
    }
}

/** Nút phát tròn 52dp (viền `ink-18`, nền `card`) với tam giác phát. */
@Composable
private fun PlayCircle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .size(PlayButtonSize)
            .clip(CircleShape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.18f), CircleShape)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PlayTriangle(colors.ink)
    }
}

/** Tam giác phát (play) — khớp `border-left:12px solid ink` của prototype. */
@Composable
private fun PlayTriangle(color: Color) {
    Canvas(Modifier.size(15.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, 0f)
            lineTo(w, h * 0.5f)
            lineTo(w * 0.2f, h)
            close()
        }
        drawPath(path, color)
    }
}

/** Vẽ hairline ở đáy một modifier (viền `ink-06` ngăn cách các hàng). */
private fun Modifier.drawBottomHairline(color: Color): Modifier =
    this.drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx(),
        )
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

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun SutraPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SutraContent(
            state = previewState(),
            callbacks = SutraCallbacks(),
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun SutraPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SutraContent(
            state = previewState(),
            callbacks = SutraCallbacks(),
        )
    }
}

private fun previewState(): SutraUiState =
    SutraUiState.initial(GetSutraDetailUseCase()("k-kinh-phap-cu"))

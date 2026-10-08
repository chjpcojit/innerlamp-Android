package com.example.innerlamp.ui.screens.talk

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.TalkChapter
import com.example.innerlamp.domain.model.TalkDetail
import com.example.innerlamp.domain.usecase.GetTalkDetailUseCase
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Pháp thoại" (`talk`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `talk`).
// ─────────────────────────────────────────────────────────────────────────────

/** Khoảng đỉnh hero để nhường chỗ header dán (56 status + 38 nav + chừa). */
private val HeroTopPadding = 104.dp

/** Đường kính nút play/pause ở thẻ trình phát (prototype `58px`). */
private val PlayButtonSize = 58.dp

/**
 * Các điểm điều hướng đi ra khỏi màn "Pháp thoại". Màn không giữ `NavController`: mọi
 * điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class TalkCallbacks(
    val onBack: () -> Unit = {},
    val onMeditate: () -> Unit = {},
)

/**
 * Màn hình "Pháp thoại" — trang chi tiết một buổi giảng.
 *
 * Bố cục (trên → dưới): hero gradient (quầng sáng + kicker + tiêu đề + hàng giảng sư,
 * tan vào `canvas`) → thẻ trình phát (tiến độ + nút play) → mô tả → "Các phần" →
 * CTA "Nghe xong, ngồi thiền 10 phút". Header back trong suốt nằm đè trên hero.
 *
 * @author innerlamp
 */
@Composable
fun TalkScreen(
    modifier: Modifier = Modifier,
    callbacks: TalkCallbacks = TalkCallbacks(),
    viewModel: TalkViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TalkContent(
        state = state,
        callbacks = callbacks,
        onTogglePlay = viewModel::togglePlay,
        modifier = modifier,
    )
}

/** Phần stateless của [TalkScreen] — dùng cho preview và test. */
@Composable
internal fun TalkContent(
    state: TalkUiState,
    callbacks: TalkCallbacks,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TalkHero(talk = state.talk)

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                PlayerCard(
                    state = state,
                    onTogglePlay = onTogglePlay,
                )

                Spacer(Modifier.height(20.dp))
                Text(
                    text = state.talk.description,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 24.5f.sp,
                        color = colors.ink.copy(alpha = 0.72f),
                    ),
                )

                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Các phần",
                    style = sans(13.5f, colors.ink, FontWeight.Medium),
                )

                Spacer(Modifier.height(12.dp))
                ChapterList(chapters = state.talk.chapters)

                Spacer(Modifier.height(18.dp))
                MeditateCta(
                    onClick = callbacks.onMeditate,
                )
            }
            Spacer(Modifier.height(30.dp))
        }

        // Header trong suốt đè lên hero (chỉ chip back), luôn ghim trên cùng khi cuộn.
        IlNavBar(
            title = "Pháp thoại",
            dark = colors.isDark,
            showBack = true,
            onBack = callbacks.onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        )
    }
}

/**
 * Hero: gradient `--il-th-bg` (tan vào canvas) + quầng sáng góc phải + kicker +
 * tiêu đề Lora + hàng giảng sư (avatar tròn 32dp).
 */
@Composable
private fun TalkHero(
    talk: TalkDetail,
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
        // Quầng sáng 230dp lệch góc phải-trên (khớp prototype `right:-60px;top:-40px`).
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .size(230.dp)
                .background(Brush.radialGradient(listOf(colors.thGlow, Color.Transparent)), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = HeroTopPadding, bottom = 34.dp),
        ) {
            Spacer(Modifier.height(22.dp))
            Text(
                text = talk.kicker,
                style = sans(9.5f, colors.featKicker, FontWeight.SemiBold, letterSpacing = 1.52f.sp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = talk.title,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 27.sp,
                    lineHeight = 35.6f.sp,
                    color = colors.featTitle, // --il-th-ink
                ),
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colors.featGlow, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = talk.initials,
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = colors.featKicker, // --il-hero-acc
                        ),
                    )
                }
                Text(
                    text = "${talk.speaker} · ${talk.durationMinutes} phút · ${talk.listenCount}",
                    style = sans(12.5f, colors.featSub), // --il-th-sub
                )
            }
        }
    }
}

/**
 * Thẻ trình phát: thanh tiến độ (28%) + thời gian đã nghe/còn lại + nút play giữa
 * hai nhãn tua 15s/30s.
 */
@Composable
private fun PlayerCard(
    state: TalkUiState,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = colors.ink.copy(alpha = 0.06f),
                spotColor = colors.ink.copy(alpha = 0.06f),
            )
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.10f), shape)
            .padding(18.dp),
    ) {
        // Thanh tiến độ: track ink-10 + fill 28% gradient vàng.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.ink.copy(alpha = 0.10f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progress)
                    .height(4.dp)
                    .background(Brush.horizontalGradient(listOf(IlGoldBright, IlGold))),
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = state.currentTime,
                style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
            )
            Text(
                text = state.remainingTime,
                style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "15s",
                style = sans(12f, colors.ink.copy(alpha = 0.55f), FontWeight.Medium),
            )
            Spacer(Modifier.width(22.dp))
            PlayButton(
                isPlaying = state.isPlaying,
                onClick = onTogglePlay,
            )
            Spacer(Modifier.width(22.dp))
            Text(
                text = "30s",
                style = sans(12f, colors.ink.copy(alpha = 0.55f), FontWeight.Medium),
            )
        }
    }
}

/** Nút play/pause tròn 58dp, nền `ink` + bóng; hiển thị hai thanh (pause) hoặc tam giác (play). */
@Composable
private fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .size(PlayButtonSize)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = colors.ink.copy(alpha = 0.22f),
                spotColor = colors.ink.copy(alpha = 0.22f),
            )
            .background(colors.ink, CircleShape)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlaying) {
            PauseGlyph(IlGoldBright2)
        } else {
            PlayGlyph(IlGoldBright2)
        }
    }
}

/** Hai thanh dừng (pause) — màu `#F3C673`. */
@Composable
private fun PauseGlyph(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(width = 4.5.dp, height = 19.dp).background(color, RoundedCornerShape(2.dp)))
        Box(Modifier.size(width = 4.5.dp, height = 19.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

/** Tam giác phát (play). */
@Composable
private fun PlayGlyph(color: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.14f)
            lineTo(w * 0.86f, h * 0.5f)
            lineTo(w * 0.28f, h * 0.86f)
            close()
        }
        drawPath(path, color)
    }
}

/** Danh sách "Các phần": phần đầu (đang nghe) nổi vàng, phần sau viền hairline `ink-06`. */
@Composable
private fun ChapterList(
    chapters: List<TalkChapter>,
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
            val isActive = index == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isActive) Modifier.background(IlGold.copy(alpha = 0.08f)) else Modifier,
                    )
                    .then(
                        if (index < chapters.lastIndex) {
                            Modifier.drawBottomHairline(colors.ink.copy(alpha = 0.06f))
                        } else Modifier,
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = chapter.timestamp,
                    modifier = Modifier.width(42.dp),
                    style = if (isActive) {
                        sans(11.5f, colors.link, FontWeight.Medium)
                    } else {
                        sans(11.5f, colors.ink.copy(alpha = 0.45f))
                    },
                )
                Text(
                    text = chapter.title,
                    modifier = Modifier.weight(1f),
                    style = if (isActive) {
                        sans(13f, colors.ink, FontWeight.Medium)
                    } else {
                        sans(13f, colors.ink)
                    },
                )
            }
        }
    }
}

/** CTA "Nghe xong, ngồi thiền 10 phút": nền `--il-feat-*` + ngọn lửa trong vòng vàng. */
@Composable
private fun MeditateCta(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .tappable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Vòng vàng 38dp chứa ngọn lửa (quầng sáng `--il-th-flame` phía sau).
        Box(
            modifier = Modifier
                .size(38.dp)
                .border(1.dp, IlGoldBright.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        Brush.radialGradient(listOf(colors.featFlameGlow, Color.Transparent)),
                        CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(10.dp, 14.dp)
                    .background(IlGoldBright, FlameShape),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = "Nghe xong, ngồi thiền 10 phút",
                style = sans(13f, colors.featTitle, FontWeight.Medium),
            )
            Text(
                text = "Quán niệm ân cha mẹ",
                modifier = Modifier.padding(top = 2.dp),
                style = sans(11f, colors.featSub),
            )
        }
        Text(
            text = "›",
            style = sans(18f, colors.featSub),
        )
    }
}

/** Hình ngọn lửa (teardrop bo góc theo CSS `50% 50% 45% 45% / 62% 62% 38% 38%`). */
private val FlameShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val topRx = 0.50f * w
        val topRy = 0.62f * h
        val botRx = 0.45f * w
        val botRy = 0.38f * h
        val k = 0.5522847498f
        val path = Path().apply {
            moveTo(topRx, 0f)
            lineTo(w - topRx, 0f)
            cubicTo(w - topRx + topRx * k, 0f, w, topRy - topRy * k, w, topRy)
            lineTo(w, h - botRy)
            cubicTo(w, h - botRy + botRy * k, w - botRx + botRx * k, h, w - botRx, h)
            lineTo(botRx, h)
            cubicTo(botRx - botRx * k, h, 0f, h - botRy + botRy * k, 0f, h - botRy)
            lineTo(0f, topRy)
            cubicTo(0f, topRy - topRy * k, topRx - topRx * k, 0f, topRx, 0f)
            close()
        }
        return Outline.Generic(path)
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
private fun TalkPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        TalkContent(
            state = previewState(),
            callbacks = TalkCallbacks(),
            onTogglePlay = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun TalkPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        TalkContent(
            state = previewState(),
            callbacks = TalkCallbacks(),
            onTogglePlay = {},
        )
    }
}

private fun previewState(): TalkUiState =
    TalkUiState.initial(GetTalkDetailUseCase()("tl-hieu-la-dao"))

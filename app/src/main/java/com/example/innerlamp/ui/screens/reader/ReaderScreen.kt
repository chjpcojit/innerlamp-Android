package com.example.innerlamp.ui.screens.reader

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.usecase.GetSutraReaderUseCase
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Tụng kinh" (`reader`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `reader`).
// ─────────────────────────────────────────────────────────────────────────────

/** Đường kính nút play/pause ở thanh phát đáy màn (prototype `46px`). */
private val PlayButtonSize = 46.dp

/**
 * Các điểm điều hướng đi ra khỏi màn "Tụng kinh". Màn không giữ `NavController`: mọi
 * điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class ReaderCallbacks(
    val onBack: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onDownload: () -> Unit = {},
    val onPlayer: () -> Unit = {},
    val onTextSettings: () -> Unit = {},
)

/**
 * Màn hình "Tụng kinh" — trang đọc một khổ kinh song ngữ.
 *
 * Bố cục (trên → dưới): header dán (title + phụ đề + nút "Aa") → thanh tiến độ →
 * khổ kinh song ngữ (Việt + bản dịch, viền vàng bên trái + 2 chip hành động) → khổ
 * kinh kế tiếp mờ → thanh phát dính đáy (play/pause + tiến độ + tốc độ).
 *
 * @author innerlamp
 */
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    callbacks: ReaderCallbacks = ReaderCallbacks(),
    viewModel: ReaderViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReaderContent(
        state = state,
        callbacks = callbacks,
        onTogglePlay = viewModel::togglePlay,
        modifier = modifier,
    )
}

/** Phần stateless của [ReaderScreen] — dùng cho preview và test. */
@Composable
internal fun ReaderContent(
    state: ReaderUiState,
    callbacks: ReaderCallbacks,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.card2),
    ) {
        // Header dán trên cùng: nhường status bar + nền card2 (không có line dưới).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card2)
                .statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 10.dp),
        ) {
            IlNavBar(
                title = state.reader.title,
                sub = state.reader.subtitle,
                right = "Aa",
                showBack = true,
                onBack = callbacks.onBack,
                onAction = callbacks.onTextSettings,
            )
        }

        // Phần cuộn: thanh tiến độ + khổ kinh (header nằm ngoài nên cuộn chui bên dưới).
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            ReaderProgress(state = state)

            Spacer(Modifier.height(24.dp))
            VerseBlock(state = state, callbacks = callbacks)

            Spacer(Modifier.height(26.dp))
            NextVersePreview(text = state.reader.nextVersePreview)

            Spacer(Modifier.height(26.dp))
        }

        PlayerBar(
            state = state,
            onTogglePlay = onTogglePlay,
            onPlayer = callbacks.onPlayer,
        )
    }
}

/** Thanh tiến độ đọc (34% gradient vàng) nằm dưới header. */
@Composable
private fun ReaderProgress(
    state: ReaderUiState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.ink.copy(alpha = 0.09f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(state.progress)
                .height(3.dp)
                .background(Brush.horizontalGradient(listOf(IlGoldBright, IlGold))),
        )
    }
}

/**
 * Khối khổ kinh đang tụng: viền vàng bên trái + nguyên văn Lora + bản dịch nghiêng
 * + hai chip "Chia sẻ câu kinh" / "Tải để nghe offline".
 */
@Composable
private fun VerseBlock(
    state: ReaderUiState,
    callbacks: ReaderCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Viền trái 2px vàng 50% (khớp `border-left:2px solid rgba(184,134,47,.5)`).
                drawLine(
                    color = IlGold.copy(alpha = 0.5f),
                    start = Offset(1.dp.toPx(), 0f),
                    end = Offset(1.dp.toPx(), size.height),
                    strokeWidth = 2.dp.toPx(),
                )
            }
            .padding(start = 16.dp),
    ) {
        Text(
            text = state.reader.verse.vietnamese,
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 21.sp,
                lineHeight = 33.6f.sp, // 1.6
                color = colors.ink,
            ),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = state.reader.verse.translation,
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 13.5.sp,
                lineHeight = 22.95f.sp, // 1.7
                color = colors.ink.copy(alpha = 0.5f),
            ),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IlChip(label = "Chia sẻ câu kinh", onClick = callbacks.onShare)
            IlChip(label = "Tải để nghe offline", onClick = callbacks.onDownload)
        }
    }
}

/** Khổ kinh kế tiếp, hiển thị mờ (opacity .45) để báo trước nội dung. */
@Composable
private fun NextVersePreview(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = Lora,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 32.sp, // 1.6
            color = colors.ink.copy(alpha = 0.45f),
        ),
    )
}

/**
 * Thanh phát dính đáy: nền gradient tan vào card2 → thẻ (nút play + tiêu đề "Đang
 * tụng · thầy" + tiến độ nhỏ + chip tốc độ).
 */
@Composable
private fun PlayerBar(
    state: ReaderUiState,
    onTogglePlay: () -> Unit,
    onPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to colors.card2.copy(alpha = 0f),
                    0.26f to colors.card2,
                    1f to colors.card2,
                ),
            ),
    ) {
        PlayerCard(
            state = state,
            onTogglePlay = onTogglePlay,
            onPlayer = onPlayer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 30.dp),
        )
    }
}

/** Thẻ trình phát (radius 22 + bóng nhẹ): nút play 46dp + thông tin + chip tốc độ. */
@Composable
private fun PlayerCard(
    state: ReaderUiState,
    onTogglePlay: () -> Unit,
    onPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = colors.ink.copy(alpha = 0.07f),
                spotColor = colors.ink.copy(alpha = 0.07f),
            )
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.10f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PlayButton(isPlaying = state.isPlaying, onClick = onTogglePlay)

        Column(Modifier.weight(1f)) {
            Text(
                text = "${if (state.isPlaying) "Đang tụng" else "Tạm dừng"} · ${state.reader.teacher}",
                maxLines = 1,
                style = sans(12.5f, colors.ink, FontWeight.Medium),
            )
            Spacer(Modifier.height(7.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Track ink-10 + fill vàng (đặc, không gradient — khớp prototype).
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.ink.copy(alpha = 0.10f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(state.progress)
                            .height(3.dp)
                            .background(IlGold),
                    )
                }
                Text(
                    text = state.currentTime,
                    style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
                )
            }
        }

        SpeedChip(label = state.reader.speed, onClick = onPlayer)
    }
}

/** Nút play/pause tròn 46dp, nền `ink`; hai thanh (pause) hoặc tam giác (play). */
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

/** Hai thanh dừng (pause) — màu `#F3C673`, khớp prototype `4×16px`. */
@Composable
private fun PauseGlyph(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(width = 4.dp, height = 16.dp).background(color, RoundedCornerShape(2.dp)))
        Box(Modifier.size(width = 4.dp, height = 16.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

/** Tam giác phát (play). */
@Composable
private fun PlayGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.30f, h * 0.16f)
            lineTo(w * 0.84f, h * 0.5f)
            lineTo(w * 0.30f, h * 0.84f)
            close()
        }
        drawPath(path, color)
    }
}

/** Chip tốc độ "1.0×": nền vàng nhạt `rgba(184,134,47,.14)`, chữ link → mở trình phát. */
@Composable
private fun SpeedChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(IlGold.copy(alpha = 0.14f))
            .tappable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = sans(11f, colors.link, FontWeight.Medium),
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

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun ReaderPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        ReaderContent(
            state = previewState(),
            callbacks = ReaderCallbacks(),
            onTogglePlay = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun ReaderPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        ReaderContent(
            state = previewState(),
            callbacks = ReaderCallbacks(),
            onTogglePlay = {},
        )
    }
}

private fun previewState(): ReaderUiState =
    ReaderUiState.initial(GetSutraReaderUseCase()("k-kinh-phap-cu"))

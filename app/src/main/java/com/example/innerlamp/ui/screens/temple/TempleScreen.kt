package com.example.innerlamp.ui.screens.temple

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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.model.TempleGlyph
import com.example.innerlamp.domain.model.TempleSchedule
import com.example.innerlamp.domain.model.TempleStat
import com.example.innerlamp.domain.model.TempleTone
import com.example.innerlamp.domain.usecase.GetTempleDetailUseCase
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Trang chùa" (`temple`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `temple`).
// ─────────────────────────────────────────────────────────────────────────────

/** Chiều cao hero `--il-th-bg` (290dp) + phần nút nhô xuống (28dp). */
private val HeroHeight = 290.dp

/** Chiều cao nút "Theo dõi" / "Cúng dường" (prototype `height:46px`). */
private val PillHeight = 46.dp

/**
 * Các điểm điều hướng đi ra khỏi màn "Trang chùa". Màn không giữ `NavController`: mọi
 * điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class TempleCallbacks(
    val onBack: () -> Unit = {},
    val onOffer: () -> Unit = {},
    val onSchedule: (TempleSchedule) -> Unit = {},
    val onDirections: () -> Unit = {},
)

/**
 * Màn hình "Trang chùa" — trang chi tiết một ngôi chùa.
 *
 * Bố cục (trên → dưới): hero gradient (quầng sáng + hai ngọn lửa + tên chùa + phụ đề,
 * tan vào `canvas`) → 2 nút "Theo dõi" / "Cúng dường" (nhô lên chồm hero) → thống kê
 * 3 cột → giới thiệu → "Thời khóa sắp tới" → địa chỉ. Header back trong suốt nằm đè
 * trên hero.
 *
 * @author innerlamp
 */
@Composable
fun TempleScreen(
    modifier: Modifier = Modifier,
    callbacks: TempleCallbacks = TempleCallbacks(),
    viewModel: TempleViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TempleContent(
        state = state,
        callbacks = callbacks,
        onToggleFollow = viewModel::toggleFollow,
        modifier = modifier,
    )
}

/** Phần stateless của [TempleScreen] — dùng cho preview và test. */
@Composable
internal fun TempleContent(
    state: TempleUiState,
    callbacks: TempleCallbacks,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Hero + 2 nút: nút nhô 28dp xuống dưới hero (chồm lên hero 18dp).
            Box(Modifier.fillMaxWidth().height(HeroHeight + 28.dp)) {
                TempleHero(
                    detail = state.detail,
                    modifier = Modifier.fillMaxWidth().height(HeroHeight),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TemplePillButton(
                        label = state.followLabel,
                        primary = true,
                        onClick = onToggleFollow,
                        modifier = Modifier.weight(1f),
                    )
                    TemplePillButton(
                        label = "Cúng dường",
                        primary = false,
                        onClick = callbacks.onOffer,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(18.dp))
                TempleStats(stats = state.detail.stats)

                Spacer(Modifier.height(20.dp))
                Text(
                    text = state.detail.description,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 23.8f.sp,
                        color = colors.ink.copy(alpha = 0.72f),
                    ),
                )

                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Thời khóa sắp tới",
                    style = sans(13.5f, colors.ink, FontWeight.Medium),
                )

                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.detail.upcoming.forEach { schedule ->
                        IlRow(
                            title = schedule.title,
                            sub = schedule.sub,
                            trailing = schedule.trailing,
                            tone = schedule.tone.toIlTone(),
                            glyph = schedule.glyph.toIlRowGlyph(),
                            onClick = { callbacks.onSchedule(schedule) },
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                TempleAddress(
                    address = state.detail.address,
                    onClick = callbacks.onDirections,
                )
            }
            Spacer(Modifier.height(30.dp))
        }

        // Header trong suốt đè lên hero (chỉ chip back), luôn ghim trên cùng khi cuộn.
        IlNavBar(
            title = "",
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
 * Hero 290dp: gradient `--il-th-bg` (tan vào canvas) + quầng sáng lớn ở giữa-trên +
 * hai ngọn lửa + tên chùa + phụ đề (canh đáy).
 */
@Composable
private fun TempleHero(
    detail: TempleDetail,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to colors.thBgTop,
                    0.58f to colors.thBgMid,
                    1f to colors.canvas,
                ),
            ),
    ) {
        // Quầng sáng 340dp ở giữa-trên (khớp `radial-gradient(circle, --il-th-glow, transparent 66%)`).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
                .size(340.dp)
                .background(Brush.radialGradient(listOf(colors.thGlow, Color.Transparent)), CircleShape),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TempleFlame()
                TempleFlame()
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = detail.name,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 26.sp,
                    color = colors.featTitle, // --il-th-ink
                ),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "${detail.place} · ${detail.sect}",
                style = sans(12f, colors.featSub), // --il-th-sub
            )
        }
    }
}

/**
 * Ngọn lửa 16×22 (teardrop bo góc theo CSS `50% 50% 45% 45% / 62% 62% 38% 38%`),
 * gradient `#F3C673 → #D9963C` + quầng sáng nhỏ phía sau (`--il-th-flame`).
 */
@Composable
private fun TempleFlame(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Box(modifier.size(16.dp, 22.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .background(
                    Brush.radialGradient(listOf(colors.featFlameGlow, Color.Transparent)),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(IlGoldBright2, IlGoldBright)),
                    TempleFlameShape,
                ),
        )
    }
}

/** Khối thống kê 3 cột, ngăn cách bằng hairline `ink-09`. */
@Composable
private fun TempleStats(
    stats: List<TempleStat>,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(vertical = 16.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                Box(Modifier.width(1.dp).height(36.dp).background(colors.ink.copy(alpha = 0.09f)))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stat.value,
                    style = sans(19f, colors.ink, FontWeight.Medium),
                )
                Text(
                    text = stat.label,
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(10.5f, colors.ink.copy(alpha = 0.50f)),
                )
            }
        }
    }
}

/** Hàng địa chỉ nền xanh nhạt: chấm xanh + địa chỉ + chevron. */
@Composable
private fun TempleAddress(
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IlGreen.copy(alpha = 0.12f))
            .tappable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(8.dp).background(IlGreen, CircleShape))
        Text(
            text = "$address · chỉ đường",
            modifier = Modifier.weight(1f),
            style = sans(12f, IlGreenText, lineHeight = 18.6f.sp),
        )
        Text(
            text = "›",
            style = sans(18f, IlGreenText.copy(alpha = 0.6f)),
        )
    }
}

/** Nút pill 46dp của màn chùa (khác `IlButton` 50dp — giữ đúng pixel prototype). */
@Composable
private fun TemplePillButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Pill)
    val bg = if (primary) colors.ink else colors.card
    val fg = if (primary) colors.canvas else colors.ink
    val bd = if (primary) colors.ink else colors.ink.copy(alpha = 0.14f)
    Box(
        modifier = modifier
            .then(
                // Nút primary có bóng `0 8px 22px --il-ink-2` (giống prototype).
                if (primary) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = colors.ink.copy(alpha = 0.20f),
                        spotColor = colors.ink.copy(alpha = 0.20f),
                    )
                } else Modifier
            )
            .height(PillHeight)
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = sans(13.5f, fg, FontWeight.Medium),
        )
    }
}

/** Hình ngọn lửa (teardrop bo góc trên rộng, đáy thon). */
private val TempleFlameShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        // Bán kính bo góc theo CSS: trên 50% / 62%, dưới 45% / 38%.
        val topRx = 0.50f * w
        val topRy = 0.62f * h
        val botRx = 0.45f * w
        val botRy = 0.38f * h
        // Hằng số vòng cung Bézier ≈ 1/4 ellipse.
        val k = 0.5522847498f
        val path = Path().apply {
            moveTo(topRx, 0f)
            lineTo(w - topRx, 0f)
            // Góc trên phải.
            cubicTo(w - topRx + topRx * k, 0f, w, topRy - topRy * k, w, topRy)
            lineTo(w, h - botRy)
            // Góc dưới phải.
            cubicTo(w, h - botRy + botRy * k, w - botRx + botRx * k, h, w - botRx, h)
            lineTo(botRx, h)
            // Góc dưới trái.
            cubicTo(botRx - botRx * k, h, 0f, h - botRy + botRy * k, 0f, h - botRy)
            lineTo(0f, topRy)
            // Góc trên trái.
            cubicTo(0f, topRy - topRy * k, topRx - topRx * k, 0f, topRx, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Ánh xạ tông domain → `IlTone`. */
private fun TempleTone.toIlTone(): IlTone = when (this) {
    TempleTone.Gold -> IlTone.Gold
    TempleTone.Green -> IlTone.Green
    TempleTone.Rose -> IlTone.Rose
    TempleTone.Ink -> IlTone.Ink
}

/** Ánh xạ hình tượng domain → `IlRowGlyph`. */
private fun TempleGlyph.toIlRowGlyph(): IlRowGlyph = when (this) {
    TempleGlyph.Leaf -> IlRowGlyph.Leaf
    TempleGlyph.Book -> IlRowGlyph.Book
    TempleGlyph.Circle -> IlRowGlyph.Circle
    TempleGlyph.Dot -> IlRowGlyph.Dot
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
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    color = color,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun TemplePreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        TempleContent(
            state = previewState(),
            callbacks = TempleCallbacks(),
            onToggleFollow = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun TemplePreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        TempleContent(
            state = previewState(),
            callbacks = TempleCallbacks(),
            onToggleFollow = {},
        )
    }
}

private fun previewState(): TempleUiState =
    TempleUiState(
        detail = GetTempleDetailUseCase()("tu-quang"),
        isFollowing = false,
    )

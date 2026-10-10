package com.example.innerlamp.ui.screens.setup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlSettingKind
import com.example.innerlamp.ui.components.IlSettingRow
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn hình Chuẩn bị thiền (prototype `setup`). Nền card2 → sunk, header dán +
// nút ✕, chọn thời lượng, cấu hình chuông, CTA "Bắt đầu" ghim đáy màn.
// ─────────────────────────────────────────────────────────────────────────────

/** Tiêu đề buổi thiền (prototype `setup` — cố định, chưa nối data). */
private const val SetupTitle = "Thiền hơi thở\nbuổi sáng"

/** Phụ đề mô tả buổi thiền. TODO(il-data-builder): lấy từ pattern thực hành. */
private const val SetupSubtitle = "Thầy Minh Đạo · giọng Nam · có nhạc nền"

/**
 * Màn hình Chuẩn bị thiền — dựng lại từ prototype `Docs/design_handoff_innerlamp/`
 * (màn `setup`).
 *
 * Bố cục (trên → dưới): header dán "TRƯỚC KHI NGỒI" + nút ✕ → tiêu đề buổi thiền
 * → THỜI LƯỢNG (5/12/20/30/45 phút) → CHUÔNG (chuông mở đầu / giữa giờ / nền âm
 * thanh) → gợi ý tư thế → CTA "Bắt đầu · N phút" ghim đáy.
 *
 * Màn không giữ `NavController`: đóng qua [onClose], bắt đầu phiên qua [onStart]
 * (có hướng dẫn) / [onStartSilent] (ngồi im lặng).
 */
@Composable
fun SetupScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onStart: (durationMinutes: Int) -> Unit = { },
    onStartSilent: (durationMinutes: Int) -> Unit = { },
    viewModel: SetupViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SetupContent(
        state = state,
        onClose = onClose,
        onStart = onStart,
        onStartSilent = onStartSilent,
        onDurationSelect = viewModel::onDurationSelect,
        modifier = modifier,
    )
}

/** Phần stateless của [SetupScreen] — dùng cho preview và test. */
@Composable
internal fun SetupContent(
    state: SetupUiState,
    onClose: () -> Unit,
    onStart: (Int) -> Unit,
    onStartSilent: (Int) -> Unit,
    onDurationSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            IlNavBar(
                title = "TRƯỚC KHI NGỒI",
                showBack = false,
                showClose = true,
                onClose = onClose,
                modifier = Modifier.padding(horizontal = 22.dp),
            )
        },
        modifier = modifier,
    ) { headerHeight ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // linear-gradient(180deg, card2, sunk) — nền form/đọc kinh.
                .background(Brush.verticalGradient(listOf(colors.card2, colors.sunk)))
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                ),
        ) {
            // Nội dung cuộn được (co giãn để CTA luôn ghim đáy).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
            ) {
                Spacer(Modifier.height(headerHeight))
                Text(
                    text = SetupTitle,
                    modifier = Modifier.padding(top = 22.dp),
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 30.sp,
                        lineHeight = 37.sp,
                        color = colors.ink,
                    ),
                )
                Text(
                    text = SetupSubtitle,
                    modifier = Modifier.padding(top = 8.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = colors.ink.copy(alpha = 0.5f),
                    ),
                )

                SectionLabel("THỜI LƯỢNG", modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                DurationSelector(
                    options = state.durationOptions,
                    selectedIndex = state.durationIndex,
                    onSelect = onDurationSelect,
                )
                Text(
                    text = "phút · bạn thường ngồi 20 phút vào buổi sáng",
                    modifier = Modifier.padding(top = 8.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = colors.ink.copy(alpha = 0.42f),
                    ),
                )

                SectionLabel("CHUÔNG", modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
                BellCard()

                TipBox(modifier = Modifier.padding(top = 16.dp))
                Spacer(Modifier.height(20.dp))
            }

            // CTA ghim đáy màn.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, bottom = 30.dp),
            ) {
                StartButton(
                    label = "Bắt đầu · ${state.durationLabel}",
                    onClick = { onStart(state.durationMinutes) },
                )
                Text(
                    text = "Ngồi im lặng, không hướng dẫn",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { onStartSilent(state.durationMinutes) },
                        )
                        .padding(top = 14.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = colors.ink.copy(alpha = 0.45f),
                    ),
                )
            }
        }
    }
}

/** Tiêu đề nhóm (eyebrow) — 600 10px, letter-spacing .14em, `ink-42`. */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            color = colors.ink.copy(alpha = 0.42f),
        ),
    )
}

/** Dải chip thời lượng: 5 mức, mỗi chip chiếm đều (flex:1), chọn → đảo nền/chữ. */
@Composable
private fun DurationSelector(
    options: List<Int>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, minutes ->
            val selected = index == selectedIndex
            val bg = if (selected) colors.ink else colors.card
            val fg = if (selected) colors.canvas else colors.ink
            val bd = if (selected) colors.ink else colors.ink.copy(alpha = 0.12f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .border(1.dp, bd, RoundedCornerShape(16.dp))
                    .pressable(onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = minutes.toString(),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = fg,
                    ),
                )
            }
        }
    }
}

/** Thẻ cấu hình chuông: 3 hàng value (chuông mở đầu / giữa giờ / nền âm thanh). */
@Composable
private fun BellCard(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
    ) {
        IlSettingRow(title = "Chuông mở đầu", kind = IlSettingKind.Value, value = "3 tiếng khánh")
        IlSettingRow(title = "Chuông giữa giờ", kind = IlSettingKind.Value, value = "mỗi 5 phút")
        IlSettingRow(title = "Nền âm thanh", kind = IlSettingKind.Value, value = "Mưa hiên chùa", last = true)
    }
}

/** Gợi ý tư thế: biểu tượng ngọn lửa trong vòng tròn + câu nhắc. */
@Composable
private fun TipBox(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(IlGold.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(1.dp, IlGold.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            FlameGlyph(modifier = Modifier.size(width = 7.dp, height = 10.dp))
        }
        Text(
            text = "Ngồi thẳng lưng, buông hai vai. Chuông sẽ dẫn bạn về hơi thở.",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 11.5.sp,
                lineHeight = 18.sp,
                color = colors.link,
            ),
        )
    }
}

/** Ngọn lửa nhỏ (teardrop nhọn đỉnh) — giống nút đèn ở bottom nav. */
@Composable
private fun FlameGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            cubicTo(size.width * 0.98f, size.height * 0.5f, size.width * 0.95f, size.height * 0.82f, size.width * 0.5f, size.height)
            cubicTo(size.width * 0.05f, size.height * 0.82f, size.width * 0.02f, size.height * 0.5f, size.width * 0.5f, 0f)
            close()
        }
        drawPath(path, IlGoldBright)
    }
}

/** Nút CTA "Bắt đầu · N phút" — pill 54px, nền ink + chữ canvas (prototype `setup`). */
@Composable
private fun StartButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.978f else 1f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(54.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = colors.ink.copy(alpha = 0.16f),
                spotColor = colors.ink.copy(alpha = 0.16f),
            )
            .clip(RoundedCornerShape(999.dp))
            .background(colors.ink)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colors.canvas,
            ),
        )
    }
}

/** Bấm được, không ripple, có hiệu ứng nhấn (scale) + hover. */
@Composable
private fun Modifier.pressable(
    pressedScale: Float = 0.96f,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    return this
        .graphicsLayer {
            val s = if (pressed) pressedScale else 1f
            scaleX = s
            scaleY = s
        }
        .hoverable(interaction)
        .clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SetupPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SetupContent(
            state = SetupUiState(),
            onClose = {},
            onStart = {},
            onStartSilent = {},
            onDurationSelect = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SetupPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SetupContent(
            state = SetupUiState(),
            onClose = {},
            onStart = {},
            onStartSilent = {},
            onDurationSelect = {},
        )
    }
}

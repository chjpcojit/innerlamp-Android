package com.example.innerlamp.ui.screens.bell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.BellEveryOptions
import com.example.innerlamp.domain.model.BellMode
import com.example.innerlamp.domain.model.BellPresets
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlMedColors
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.LocalIlMedColors
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn hình Chuông mõ (prototype `bell`). Nền immersive dùng bộ token `--il-med-*`.
// ─────────────────────────────────────────────────────────────────────────────

/** `--il-med-5` — chữ phụ (light .58 / dark .5). */
private fun IlMedColors.subtle(): Color = ink.copy(alpha = if (isDark) 0.5f else 0.58f)

/** `--il-med-45` — chữ mờ (light .52 / dark .45). */
private fun IlMedColors.faint(): Color = ink.copy(alpha = if (isDark) 0.45f else 0.52f)

/**
 * Màn hình Chuông mõ — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `bell`).
 *
 * Bố cục: NavBar "Chuông mõ" → dải preset cuộn ngang → chọn chế độ (Mõ/Chuông/Mõ + chuông)
 * → tấm gõ lớn + đếm tiếng → ô điều chỉnh nhịp/khoảng cách (+ mốc thỉnh chuông) →
 * hàng nút (Đặt lại/Dồn mõ · phát/tạm dừng · Thỉnh chuông).
 *
 * Màn không giữ `NavController`: điều hướng quay lại qua [onBack].
 *
 * @author innerlamp
 */
@Composable
fun BellScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: BellViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BellContent(
        state = state,
        onBack = onBack,
        onPresetSelect = viewModel::onPresetSelect,
        onModeSelect = viewModel::onModeSelect,
        onEverySelect = viewModel::onEverySelect,
        onPadTap = viewModel::onPadTap,
        onStrikeChuong = viewModel::onStrikeChuong,
        onLeftAction = viewModel::onLeftAction,
        onRateDown = viewModel::onRateDown,
        onRateUp = viewModel::onRateUp,
        onToggleRun = viewModel::onToggleRun,
        modifier = modifier,
    )
}

/** Phần stateless của [BellScreen] — dùng cho preview và test. */
@Composable
internal fun BellContent(
    state: BellUiState,
    onBack: () -> Unit,
    onPresetSelect: (Int) -> Unit,
    onModeSelect: (BellMode) -> Unit,
    onEverySelect: (Int) -> Unit,
    onPadTap: () -> Unit,
    onStrikeChuong: () -> Unit,
    onLeftAction: () -> Unit,
    onRateDown: () -> Unit,
    onRateUp: () -> Unit,
    onToggleRun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            // radial-gradient(circle at 50% 32%, bgTop, bgBottom 74%) — nền immersive.
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(med.bgTop, med.bgBottom),
                        center = Offset(size.width * 0.5f, size.height * 0.32f),
                        radius = maxOf(size.width, size.height) * 1.1f,
                    ),
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
        ) {
            IlNavBar(title = "Chuông mõ", sub = state.subtitle, showBack = true, onBack = onBack)

            PresetStrip(
                selectedIndex = state.presetIndex,
                onSelect = onPresetSelect,
                modifier = Modifier.padding(top = 18.dp),
            )

            ModeSegment(
                mode = state.mode,
                onSelect = onModeSelect,
                modifier = Modifier.padding(top = 14.dp),
            )

            // Khối giữa: tấm gõ + đếm tiếng (co giãn để chiếm khoảng trống còn lại).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Pad(state = state, onTap = onPadTap)
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    CountStat(label = "Tiếng mõ", value = state.moCount)
                    CountStat(label = "Tiếng chuông", value = state.chuongCount)
                }
            }

            RateCard(
                state = state,
                onRateDown = onRateDown,
                onRateUp = onRateUp,
                onEverySelect = onEverySelect,
            )

            ControlRow(
                state = state,
                onLeftAction = onLeftAction,
                onToggleRun = onToggleRun,
                onStrikeChuong = onStrikeChuong,
                modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
            )
        }
    }
}

/** Dải preset cuộn ngang (Tụng kinh / Niệm Phật / ...). */
@Composable
private fun PresetStrip(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BellPresets.all.forEachIndexed { index, preset ->
            SelectChip(
                label = preset.label,
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
            )
        }
    }
}

/** Thanh chọn chế độ 3 phân đoạn (Mõ / Chuông / Mõ + chuông). */
@Composable
private fun ModeSegment(
    mode: BellMode,
    onSelect: (BellMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(med.ink.copy(alpha = 0.04f))
            .border(1.dp, med.ink.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(3.dp),
    ) {
        BellMode.entries.forEach { option ->
            val selected = mode == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) med.ink.copy(alpha = 0.12f) else Color.Transparent)
                    .noRipple(onClick = { onSelect(option) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.label,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = if (selected) med.ink else med.subtle(),
                    ),
                )
            }
        }
    }
}

/**
 * Tấm gõ lớn: quầng sáng + vòng ngoài + mặt tròn 188dp. Nhấn mõ/chạm chuông làm
 * tấm co nhẹ (scale .95) và quầng sáng bừng lên (opacity .55 mõ / 1 chuông).
 */
@Composable
private fun Pad(
    state: BellUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val pressed = state.hitMo || state.hitChuong

    val padScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(120),
        label = "bellPadScale",
    )
    val haloOpacity by animateFloatAsState(
        targetValue = when {
            state.hitChuong -> 1f
            state.hitMo -> 0.55f
            else -> 0.18f
        },
        animationSpec = tween(500),
        label = "bellHaloOpacity",
    )
    val haloScale by animateFloatAsState(
        targetValue = if (state.hitChuong) 1.08f else 1f,
        animationSpec = tween(500),
        label = "bellHaloScale",
    )
    val padBorder by animateColorAsState(
        targetValue = if (pressed) IlGoldBright.copy(alpha = 0.75f) else med.ink.copy(alpha = 0.22f),
        animationSpec = tween(200),
        label = "bellPadBorder",
    )

    Box(modifier = modifier.size(236.dp), contentAlignment = Alignment.Center) {
        // Quầng sáng: radial-gradient(rgba(217,150,60,.32), transparent 68%).
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloOpacity
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IlGoldBright.copy(alpha = 0.32f), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.34f,
                        ),
                        radius = size.minDimension * 0.34f,
                        center = center,
                    )
                },
        )
        // Vòng ngoài (inset 6px).
        Box(
            Modifier
                .fillMaxSize()
                .padding(6.dp)
                .border(1.dp, med.ink.copy(alpha = 0.12f), CircleShape),
        )
        // Mặt gõ.
        Box(
            modifier = Modifier
                .size(188.dp)
                .graphicsLayer {
                    scaleX = padScale
                    scaleY = padScale
                }
                .background(med.card, CircleShape)
                .border(1.5.dp, padBorder, CircleShape)
                .noRipple(onClick = onTap),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.padTag,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.52.sp, // .16em
                        color = med.gold,
                    ),
                )
                Text(
                    text = state.padNumber.toString(),
                    modifier = Modifier.padding(top = 8.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Light,
                        fontSize = 54.sp,
                        lineHeight = 54.sp,
                        color = med.ink,
                    ),
                )
                Text(
                    text = state.padHint,
                    modifier = Modifier.padding(top = 8.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = med.faint(),
                    ),
                )
            }
        }
    }
}

/** Dòng "Tiếng mõ N" / "Tiếng chuông N" dưới tấm gõ. */
@Composable
private fun CountStat(label: String, value: Int, modifier: Modifier = Modifier) {
    val med = LocalIlMedColors.current
    Row(modifier = modifier) {
        Text(
            text = "$label ",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = med.subtle(),
            ),
        )
        Text(
            text = value.toString(),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = med.ink,
            ),
        )
    }
}

/** Ô điều chỉnh nhịp/khoảng cách + mốc "thỉnh chuông sau mỗi". */
@Composable
private fun RateCard(
    state: BellUiState,
    onRateDown: () -> Unit,
    onRateUp: () -> Unit,
    onEverySelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(med.card, shape)
            .border(1.dp, med.ink.copy(alpha = 0.12f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.rateLabel,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = med.subtle(),
                    ),
                )
                Text(
                    text = state.rateValue,
                    modifier = Modifier.padding(top = 3.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = med.ink,
                    ),
                )
            }
            RoundButton(label = "−", onClick = onRateDown)
            RoundButton(label = "+", onClick = onRateUp)
        }

        if (state.showEvery) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(med.ink.copy(alpha = 0.12f)),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Thỉnh chuông sau mỗi",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = med.subtle(),
                    ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BellEveryOptions.all.forEachIndexed { index, value ->
                        SelectChip(
                            label = BellEveryOptions.label(value),
                            selected = state.everyIndex == index,
                            compact = true,
                            onClick = { onEverySelect(index) },
                        )
                    }
                }
            }
        }
    }
}

/** Hàng nút đáy: trái (Đặt lại/Dồn mõ) · giữa phát/tạm dừng · phải Thỉnh chuông. */
@Composable
private fun ControlRow(
    state: BellUiState,
    onLeftAction: () -> Unit,
    onToggleRun: () -> Unit,
    onStrikeChuong: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PillButton(
            label = state.leftLabel,
            bg = Color.Transparent,
            bd = med.ink.copy(alpha = 0.22f),
            fg = med.ink,
            onClick = onLeftAction,
            modifier = Modifier.weight(1f),
        )
        PlayButton(running = state.running, onClick = onToggleRun)
        PillButton(
            label = "Thỉnh chuông",
            bg = IlGoldBright.copy(alpha = 0.16f),
            bd = IlGoldBright.copy(alpha = 0.4f),
            fg = colors.link,
            onClick = onStrikeChuong,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Chip lựa chọn (preset / mốc thỉnh chuông): chọn → nền vàng nhạt + viền vàng. */
@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val med = LocalIlMedColors.current
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(999.dp)
    val bg = if (selected) IlGoldBright.copy(alpha = 0.16f) else Color.Transparent
    val bd = if (selected) IlGoldBright.copy(alpha = 0.5f) else med.ink.copy(alpha = 0.12f)
    val fg = if (selected) colors.link else med.ink.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .pressable(onClick = onClick)
            .padding(
                horizontal = if (compact) 9.dp else 14.dp,
                vertical = if (compact) 6.dp else 8.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = if (compact) 11.5.sp else 12.sp,
                color = fg,
            ),
        )
    }
}

/** Nút tròn 40dp (nút − / + trong ô điều chỉnh). */
@Composable
private fun RoundButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, med.ink.copy(alpha = 0.22f), CircleShape)
            .pressable(pressedScale = 0.94f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = med.ink,
            ),
        )
    }
}

/** Nút pill 48dp cho hàng đáy (Đặt lại / Thỉnh chuông). */
@Composable
private fun PillButton(
    label: String,
    bg: Color,
    bd: Color,
    fg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(999.dp))
            .pressable(pressedScale = 0.978f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = fg,
            ),
        )
    }
}

/** Nút tròn 64dp phát/tạm dừng (vàng `#D9963C`, biểu tượng `#1A1611` — cố định hai chế độ). */
@Composable
private fun PlayButton(
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(IlGoldBright)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = IlGoldBright.copy(alpha = 0.28f),
                spotColor = IlGoldBright.copy(alpha = 0.28f),
            )
            .pressable(pressedScale = 0.978f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (running) PauseGlyph() else PlayGlyph()
    }
}

/** Hai thanh tạm dừng (5×20, gap 5). */
@Composable
private fun PauseGlyph(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(width = 5.dp, height = 20.dp).background(Color(0xFF1A1611), RoundedCornerShape(2.dp)))
        Box(Modifier.size(width = 5.dp, height = 20.dp).background(Color(0xFF1A1611), RoundedCornerShape(2.dp)))
    }
}

/** Tam giác phát (17×22, nhọn sang phải). */
@Composable
private fun PlayGlyph(modifier: Modifier = Modifier) {
    val color = Color(0xFF1A1611)
    androidx.compose.foundation.Canvas(modifier = modifier.size(width = 17.dp, height = 22.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color)
    }
}

// ── Modifier helpers ─────────────────────────────────────────────────────

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

/** Bấm được, không ripple (không có hiệu ứng nhấn — tấm gõ tự animate theo state). */
@Composable
private fun Modifier.noRipple(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        role = Role.Button,
        onClick = onClick,
    )

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BellPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        BellContent(
            state = BellUiState(),
            onBack = {},
            onPresetSelect = {},
            onModeSelect = {},
            onEverySelect = {},
            onPadTap = {},
            onStrikeChuong = {},
            onLeftAction = {},
            onRateDown = {},
            onRateUp = {},
            onToggleRun = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BellPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        BellContent(
            state = BellUiState(),
            onBack = {},
            onPresetSelect = {},
            onModeSelect = {},
            onEverySelect = {},
            onPadTap = {},
            onStrikeChuong = {},
            onLeftAction = {},
            onRateDown = {},
            onRateUp = {},
            onToggleRun = {},
        )
    }
}

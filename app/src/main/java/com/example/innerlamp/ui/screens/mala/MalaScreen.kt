package com.example.innerlamp.ui.screens.mala

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.Mala
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlMedColors
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.LocalIlMedColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Màn hình Niệm Phật (prototype `mala`). Nền immersive dùng bộ token `--il-med-*`.
// Tương tác: chạm hoặc kéo lên/xuống một lần → đúng một hạt về giữa vạch, đếm +1.
// ─────────────────────────────────────────────────────────────────────────────

// Hình học chuỗi hạt (px → dp), khớp hàm `strand()` trong prototype.
private val BeadPitch = 52.dp      // bước nhảy giữa hai hạt
private val MarkerDiameter = 74.dp  // vòng đánh dấu ở giữa vạch
private val DragThreshold = 22.dp   // kéo ≥ 22px → tiến một hạt
private val TapThreshold = 6.dp     // kéo < 6px → tính là chạm

// ── Màu phụ của token `--il-med-*` (tra `<helmet>` prototype) ─────────────────

/** `--il-med-38` — nhãn "/ 108" (light .48 / dark .38). */
private fun IlMedColors.dim(): Color = ink.copy(alpha = if (isDark) 0.38f else 0.48f)

/** `--il-med-42` — gợi ý in nghiêng (light .5 / dark .42). */
private fun IlMedColors.soft(): Color = ink.copy(alpha = if (isDark) 0.42f else 0.50f)

/** `--il-med-3` — chú thích cuối màn (light .42 / dark .3). */
private fun IlMedColors.hint(): Color = ink.copy(alpha = if (isDark) 0.30f else 0.42f)

/** `--il-med-22` — viền nút "Bắt đầu lại" (light .2 / dark .22). */
private fun IlMedColors.ghostBorder(): Color = ink.copy(alpha = if (isDark) 0.22f else 0.20f)

/**
 * Màn hình Niệm Phật — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `mala`).
 *
 * Bố cục (trên → dưới): NavBar "Niệm Phật" + số tràng → số lớn + "/ 108" → thanh tiến
 * trình → chuỗi hạt kéo tương tác (co giãn) → câu niệm + gợi ý → hàng nút "Bắt đầu lại" /
 * "Xong" → chú thích rung/chuông.
 *
 * Màn không giữ `NavController`: điều hướng quay lại qua [onBack], hoàn tất qua [onDone].
 */
@Composable
fun MalaScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: MalaViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MalaContent(
        state = state,
        onBack = onBack,
        onDone = onDone,
        onBeadAdvance = viewModel::onBeadAdvance,
        onReset = viewModel::onReset,
        modifier = modifier,
    )
}

/** Phần stateless của [MalaScreen] — dùng cho preview và test. */
@Composable
internal fun MalaContent(
    state: MalaUiState,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onBeadAdvance: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val colors = LocalIlColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            // radial-gradient(circle at 50% 32%, bgTop, bgBottom 74%) — nền immersive.
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(med.bgTop, med.bgBottom),
                        center = Offset(size.width * 0.5f, size.height * 0.32f),
                        radius = max(size.width, size.height) * 1.1f,
                    ),
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IlNavBar(
                title = "Niệm Phật",
                sub = state.roundsLabel,
                showBack = true,
                onBack = onBack,
                modifier = Modifier.padding(top = 14.dp),
            )

            CountRow(beads = state.beads, pulse = state.pop, modifier = Modifier.padding(top = 26.dp))

            ProgressBar(
                progress = state.progress,
                modifier = Modifier.padding(top = 14.dp),
            )

            BeadStrand(
                beads = state.beads,
                pulse = state.pop,
                onAdvance = onBeadAdvance,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 14.dp),
            )

            Text(
                text = "Nam mô A Di Đà Phật",
                modifier = Modifier.padding(top = 16.dp),
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    color = med.ink,
                ),
            )
            Text(
                text = "Kéo lên hoặc xuống một lần — đúng một hạt về giữa vạch",
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = med.soft(),
                ),
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PillButton(
                    label = "Bắt đầu lại",
                    bg = Color.Transparent,
                    bd = med.ghostBorder(),
                    fg = med.ink,
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    label = "Xong",
                    bg = IlGoldBright.copy(alpha = 0.16f),
                    bd = IlGoldBright.copy(alpha = 0.4f),
                    fg = colors.link,
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = "Rung nhẹ mỗi 27 hạt · chuông ở hạt 108",
                modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = med.hint(),
                ),
            )
        }
    }
}

/** Số hạt lớn (58sp) + "/ 108"; số thụt nhẹ (scale .93) khi nhấn hạt. */
@Composable
private fun CountRow(
    beads: Int,
    pulse: Boolean,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val bigScale by animateFloatAsState(if (pulse) 0.93f else 1f, tween(120), label = "malaBigScale")
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = beads.toString(),
            modifier = Modifier.graphicsLayer { scaleX = bigScale; scaleY = bigScale },
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Light,
                fontSize = 58.sp,
                lineHeight = 58.sp,
                color = med.ink,
            ),
        )
        Text(
            text = "/ 108",
            modifier = Modifier.padding(bottom = 8.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = med.dim(),
            ),
        )
    }
}

/** Thanh tiến trình 150×2: track `--il-med-12`, fill vàng `--il-med-gold`. */
@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    Box(
        modifier = modifier
            .width(150.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(med.ink.copy(alpha = 0.12f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(med.gold, RoundedCornerShape(2.dp)),
        )
    }
}

/**
 * Chuỗi hạt tương tác: vạch giữa + vòng đánh dấu + các hạt trượt dọc.
 *
 * Chạm hoặc kéo ≥ 22dp (lên hoặc xuống) → đúng một hạt trượt về giữa rồi đếm +1;
 * kéo nhẹ < 22dp thì chuỗi bám theo ngón tay và nảy về 0 khi thả.
 */
@Composable
private fun BeadStrand(
    beads: Int,
    pulse: Boolean,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val flickPx = with(density) { DragThreshold.toPx() }
    val tapPx = with(density) { TapThreshold.toPx() }
    val pitchPx = with(density) { BeadPitch.toPx() }

    // Độ lệch chuỗi hạt khi kéo (px). Animatable để trượt/nảy mượt.
    val beadOff = remember { Animatable(0f) }
    val currentAdvance by rememberUpdatedState(onAdvance)
    val currentBeads by rememberUpdatedState(beads)

    val markerScale by animateFloatAsState(if (pulse) 1.1f else 1f, tween(120), label = "malaMarkerScale")
    val markerColor by animateColorAsState(
        if (pulse) IlGoldBright.copy(alpha = 0.72f) else IlGoldBright.copy(alpha = 0.28f),
        tween(120),
        label = "malaMarkerColor",
    )
    val off = beadOff.value
    val scope = rememberCoroutineScope()

    // Easing trượt/nảy chuỗi hạt (khớp `animBeadOff` dùng ease-out-cubic của prototype).
    val beadEasing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Đã niệm $beads trên 108 hạt" }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    var total = 0f
                    var lastY = down.position.y
                    var fired = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        val dy = change.position.y - lastY
                        lastY = change.position.y
                        total += dy
                        if (!fired) {
                            if (abs(total) >= flickPx) {
                                fired = true
                                val dir = if (total > 0f) -1f else 1f
                                scope.launch {
                                    beadOff.animateTo(dir * pitchPx, tween(230, easing = beadEasing))
                                    beadOff.snapTo(0f)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentAdvance()
                                }
                            } else {
                                scope.launch { beadOff.snapTo(-total * 0.5f) }
                            }
                        }
                        change.consume()
                    }
                    if (!fired) {
                        if (abs(total) < tapPx) {
                            // Chạm nhẹ = tiến một hạt (khớp `snapBead(1)` của prototype).
                            scope.launch {
                                beadOff.animateTo(pitchPx, tween(230, easing = beadEasing))
                                beadOff.snapTo(0f)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentAdvance()
                            }
                        } else {
                            // Kéo nhẹ chưa đủ ngưỡng: nảy về 0, không đếm.
                            scope.launch { beadOff.animateTo(0f, tween(230, easing = beadEasing)) }
                        }
                    }
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // mask mờ hai đầu (khớp `mask-image` của prototype): nội dung chỉ hiện rõ
                // ở vùng giữa, tan dần về 16% mép trên/dưới.
                .drawWithContent {
                    val bounds = Rect(Offset.Zero, size)
                    drawContext.canvas.saveLayer(bounds, Paint())
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.16f to Color.Black,
                            0.84f to Color.Black,
                            1f to Color.Transparent,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                    drawContext.canvas.restore()
                },
        ) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val centerY = size.height / 2f

            // Vạch giữa: gradient vàng mờ hai đầu.
            drawLine(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.14f to IlGoldBright.copy(alpha = 0.26f),
                    0.86f to IlGoldBright.copy(alpha = 0.26f),
                    1f to Color.Transparent,
                ),
                start = Offset(c.x, 0f),
                end = Offset(c.x, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )

            // Vạch đánh dấu hai bên (22dp, ở giữa).
            val tickStart = 8.dp.toPx()
            val tickEnd = (8.dp + 22.dp).toPx()
            val tickColor = IlGoldBright.copy(alpha = 0.3f)
            drawLine(tickColor, Offset(tickStart, centerY), Offset(tickEnd, centerY), strokeWidth = 1.dp.toPx())
            drawLine(
                tickColor,
                Offset(size.width - tickStart, centerY),
                Offset(size.width - tickEnd, centerY),
                strokeWidth = 1.dp.toPx(),
            )

            // Vòng đánh dấu 74dp quanh hạt hiện tại (vẽ trước hạt → nằm phía sau).
            drawCircle(
                color = markerColor,
                radius = MarkerDiameter.toPx() / 2f * markerScale,
                center = c,
                style = Stroke(width = 1.dp.toPx()),
            )

            // Các hạt: chỉ vẽ những hạt trong cửa sổ hiển thị (± vài bước quanh tâm).
            val span = ceil(centerY / pitchPx).toInt() + 1
            val baseSize = 27.dp.toPx()
            val sizeRange = 13.dp.toPx()
            val fadeDist = 72.dp.toPx()
            for (i in -span..span) {
                val y = centerY + i * pitchPx - off
                val d = abs(y - centerY)
                val size = baseSize + sizeRange * max(0f, 1f - d / fadeDist)
                val alpha = max(0.16f, 1f - d / (centerY * 0.95f))
                val mother = Mala.isMotherBead(currentBeads - i)
                val beadCenter = Offset(c.x, y)
                val radius = size / 2f

                // Thân hạt: radial-gradient (hạt mẫu vàng, hạt thường gỗ nâu) — luôn tối.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = if (mother) {
                            listOf(Color(0xFFF3CE8C), Color(0xFFC68A31), Color(0xFF8A5A18))
                        } else {
                            listOf(Color(0xFF6B543C), Color(0xFF3D2F21), Color(0xFF241A11))
                        },
                        center = Offset(beadCenter.x - radius * 0.32f, beadCenter.y - radius * 0.44f),
                        radius = radius * 1.15f,
                    ),
                    radius = radius,
                    center = beadCenter,
                    alpha = alpha,
                )
                drawCircle(
                    color = if (mother) Color(0x80F6D79B) else IlGoldBright.copy(alpha = 0.16f),
                    radius = radius,
                    center = beadCenter,
                    alpha = alpha,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
    }
}

/** Nút pill 48dp cho hàng đáy (Bắt đầu lại / Xong). */
@Composable
private fun PillButton(
    label: String,
    bg: Color,
    bd: Color,
    fg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.978f else 1f
    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(999.dp))
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
                fontSize = 13.sp,
                color = fg,
            ),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MalaPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        MalaContent(
            state = MalaUiState(beads = 27, round = 2),
            onBack = {},
            onDone = {},
            onBeadAdvance = {},
            onReset = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MalaPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        MalaContent(
            state = MalaUiState(beads = 27, round = 2),
            onBack = {},
            onDone = {},
            onBeadAdvance = {},
            onReset = {},
        )
    }
}

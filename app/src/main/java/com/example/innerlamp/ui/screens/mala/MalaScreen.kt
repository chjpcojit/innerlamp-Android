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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
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
import com.example.innerlamp.ui.theme.IlMedColors
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.LocalIlMedColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Màn hình Niệm Phật (prototype `mala`). Nền immersive dùng bộ token `--il-med-*`.
// Tương tác: chạm hoặc kéo lên/xuống một lần → đúng một hạt về giữa vạch, đếm +1.
// Đủ số biến đã chọn (21/54/108) → hộp thoại hồi hướng.
// ─────────────────────────────────────────────────────────────────────────────

// Hình học chuỗi hạt (px → dp), khớp hàm `strand()` trong prototype.
private val BeadPitch = 52.dp      // bước nhảy giữa hai hạt
private val MarkerDiameter = 74.dp  // vòng đánh dấu ở giữa vạch
private val DragThreshold = 22.dp   // kéo ≥ 22px → tiến một hạt
private val TapThreshold = 6.dp     // kéo < 6px → tính là chạm

private val Amber = Color(0xFFD9963C)
private val AmberLight = Color(0xFFF3C673)
private val OnAmber = Color(0xFF1A1611)

/** `--il-med-8` — chữ chip chưa chọn (alpha .8). */
private fun IlMedColors.strong(): Color = ink.copy(alpha = 0.8f)

/** `--il-med-22` — viền nút "Bắt đầu lại" (light .2 / dark .22). */
private fun IlMedColors.ghostBorder(): Color = ink.copy(alpha = if (isDark) 0.22f else 0.20f)

/**
 * Màn hình Niệm Phật — dựng lại từ prototype `Docs/innerlamp-project/` (màn `mala`).
 *
 * Bố cục (trên → dưới): NavBar "Niệm Phật" (phụ đề "Vòng N · biến n / m", nút "Xong") →
 * chip 21/54/108 biến → thanh tiến trình → chuỗi hạt kéo tương tác (co giãn, có hạt bụi
 * vàng nền) → câu niệm → nút "Bắt đầu lại". Niệm tròn số biến thì hiện hộp thoại hồi hướng.
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
        onTargetSelect = viewModel::onTargetSelect,
        onContinue = viewModel::onContinue,
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
    onTargetSelect: (Int) -> Unit = {},
    onContinue: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val fx = remember { MalaFx() }

    // Mỗi hạt mới → bắn tia sáng; hạt thứ 27 (hạt mẫu) → bắn lớn.
    var prevBeads by remember { mutableIntStateOf(state.beads) }
    LaunchedEffect(state.beads) {
        if (state.beads > prevBeads) fx.burst(big = state.beads % Mala.MOTHER_BEAD_EVERY == 0)
        prevBeads = state.beads
    }

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
            },
    ) {
        // Lớp hạt bụi vàng + tia sáng khi lần hạt (nằm sau nội dung).
        MalaFxLayer(fx = fx, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IlNavBar(
                title = "Niệm Phật",
                sub = state.roundsLabel,
                right = "Xong",
                showBack = true,
                onBack = onBack,
                onAction = onDone,
                modifier = Modifier.padding(top = 14.dp),
            )

            TargetChips(
                selected = state.target,
                onSelect = onTargetSelect,
                modifier = Modifier.padding(top = 18.dp),
            )

            ProgressBar(
                progress = state.progress,
                modifier = Modifier.padding(top = 14.dp),
            )

            BeadStrand(
                beads = state.beads,
                count = state.count,
                target = state.target,
                pulse = state.pop,
                onAdvance = onBeadAdvance,
                onCenter = fx::setCenter,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 14.dp),
            )

            Text(
                text = state.mantra,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 27.sp,
                    color = med.ink,
                ),
            )

            Spacer(Modifier.height(18.dp))

            PillButton(
                label = "Bắt đầu lại",
                bg = Color.Transparent,
                bd = med.ghostBorder(),
                fg = med.ink,
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            )
        }

        if (state.done) {
            DoneDialog(
                target = state.target,
                round = state.round,
                onContinue = onContinue,
                onDone = onDone,
            )
        }
    }
}

/** Chip chọn số biến một vòng: 21 / 54 / 108. */
@Composable
private fun TargetChips(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val med = LocalIlMedColors.current
    val colors = LocalIlColors.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Mala.TARGETS.forEach { value ->
            val on = selected == value
            val shape = RoundedCornerShape(999.dp)
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(if (on) Amber.copy(alpha = 0.16f) else Color.Transparent)
                    .border(1.dp, if (on) Amber.copy(alpha = 0.6f) else med.ink.copy(alpha = 0.12f), shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { onSelect(value) },
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "$value biến",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (on) colors.link else med.strong(),
                    ),
                )
            }
        }
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
                .background(med.gold),
        )
    }
}

/**
 * Chuỗi hạt tương tác: dây đỏ giữa + vòng đánh dấu + các hạt trượt dọc.
 *
 * Chạm hoặc kéo ≥ 22dp (lên hoặc xuống) → đúng một hạt trượt về giữa rồi đếm +1;
 * kéo nhẹ < 22dp thì chuỗi bám theo ngón tay và nảy về 0 khi thả.
 */
@Composable
private fun BeadStrand(
    beads: Int,
    count: Int,
    target: Int,
    pulse: Boolean,
    onAdvance: () -> Unit,
    onCenter: (Offset) -> Unit,
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
        if (pulse) Amber.copy(alpha = 0.72f) else Amber.copy(alpha = 0.28f),
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
            .onGloballyPositioned { onCenter(it.boundsInRoot().center) }
            .semantics { contentDescription = "Đã niệm $count trên $target biến" }
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

            drawCord(c.x)

            // Vạch đánh dấu hai bên (22dp, ở giữa).
            val tickStart = 8.dp.toPx()
            val tickEnd = (8.dp + 22.dp).toPx()
            val tickColor = Amber.copy(alpha = 0.3f)
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
                val grow = baseSize + sizeRange * max(0f, 1f - d / fadeDist)
                val alpha = max(0.16f, 1f - d / (centerY * 0.95f))
                val k = currentBeads - i
                val mother = Mala.isMotherBead(k)
                drawBead(
                    center = Offset(c.x, y),
                    diameter = if (mother) grow * 1.12f else grow,
                    mother = mother,
                    seed = beadSeed(k),
                    alpha = alpha,
                )
            }
        }
    }
}

/** Số giả ngẫu nhiên 0..1 ổn định theo vị trí hạt (khớp `sd` trong prototype). */
private fun beadSeed(k: Int): Float {
    val v = abs(sin(k * 12.9898) * 43758.5453)
    return (v - kotlin.math.floor(v)).toFloat()
}

/** Dây đỏ chạy dọc giữa chuỗi: vạch sọc 3dp (`#7a2a1c` / `#a33a26` / `#5e1f14`). */
private fun DrawScope.drawCord(x: Float) {
    val w = 3.dp.toPx()
    val left = x - w / 2f
    val bands = listOf(
        3.dp.toPx() to Color(0xFF7A2A1C),
        2.dp.toPx() to Color(0xFFA33A26),
        2.dp.toPx() to Color(0xFF5E1F14),
    )
    var y = 0f
    var i = 0
    while (y < size.height) {
        val (h, color) = bands[i % bands.size]
        drawRect(color, Offset(left, y), Size(w, minOf(h, size.height - y)))
        y += h
        i++
    }
}

/**
 * Một hạt gỗ: bóng đổ → thân (hạt mẫu vàng / gỗ nâu đỏ đổi sắc theo [seed]) → ánh phản
 * chiếu cam dưới → điểm sáng trên-trái → viền tối (thay cho `inset box-shadow` của CSS).
 */
private fun DrawScope.drawBead(
    center: Offset,
    diameter: Float,
    mother: Boolean,
    seed: Float,
    alpha: Float,
) {
    val r = diameter / 2f
    val topLeft = Offset(center.x - r, center.y - r)
    fun at(fx: Float, fy: Float) = Offset(topLeft.x + diameter * fx, topLeft.y + diameter * fy)

    // Bóng đổ: 0 6px 12px rgba(8,6,4,.55)
    drawCircle(
        brush = Brush.radialGradient(
            0.55f to Color(0xFF080604).copy(alpha = 0.55f),
            1f to Color.Transparent,
            center = Offset(center.x, center.y + 6.dp.toPx()),
            radius = r * 1.35f,
        ),
        radius = r * 1.35f,
        center = Offset(center.x, center.y + 6.dp.toPx()),
        alpha = alpha,
    )

    // Thân hạt. CSS `radial-gradient(circle at 40% 34%, …)` có bán kính farthest-corner ≈ 0.892·d.
    val baseCenter = at(0.40f, 0.34f)
    val baseRadius = diameter * 0.892f
    val body = if (mother) {
        Brush.radialGradient(
            0f to Color(0xFFFFD98A),
            0.42f to Color(0xFFE0A43A),
            0.76f to Color(0xFFA8661A),
            1f to Color(0xFF5E3408),
            center = baseCenter,
            radius = baseRadius,
        )
    } else {
        val l = (seed * 10f).roundToInt()
        Brush.radialGradient(
            0f to Color.hsl((18f + l), 0.48f, (40f + l) / 100f),
            0.52f to Color.hsl((14f + l), 0.58f, (24f + l / 2f) / 100f),
            0.88f to Color.hsl(10f, 0.60f, 0.11f),
            1f to Color.hsl(10f, 0.60f, 0.11f),
            center = baseCenter,
            radius = baseRadius,
        )
    }
    drawCircle(brush = body, radius = r, center = center, alpha = alpha)

    // Viền tối phía dưới (inset 0 -4px 9px rgba(0,0,0,.55)).
    drawCircle(
        brush = Brush.radialGradient(
            0.6f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.5f),
            center = at(0.5f, 0.42f),
            radius = r * 1.05f,
        ),
        radius = r,
        center = center,
        alpha = alpha,
    )

    // Ánh cam dưới-phải.
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0xFFFFB46E).copy(alpha = 0.28f),
            1f to Color.Transparent,
            center = at(0.66f, 0.88f),
            radius = diameter * 0.374f,
        ),
        radius = r,
        center = center,
        alpha = alpha,
    )

    // Điểm sáng trên-trái.
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0xFFFFF8E8).copy(alpha = 0.85f),
            0.45f to Color(0xFFFFF0D2).copy(alpha = 0.35f),
            1f to Color.Transparent,
            center = at(0.32f, 0.24f),
            radius = diameter * 0.2f,
        ),
        radius = r,
        center = center,
        alpha = alpha,
    )
}

/** Hộp thoại hồi hướng khi niệm tròn [target] biến. */
@Composable
private fun DoneDialog(
    target: Int,
    round: Int,
    onContinue: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080604).copy(alpha = 0.62f))
            // Chặn chạm xuyên xuống chuỗi hạt khi hộp thoại đang mở.
            .pointerInput(Unit) { awaitEachGesture { awaitFirstDown().consume() } }
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
                .border(1.dp, colors.featBorder, shape)
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.featGlow, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                // Ngọn lửa nhỏ: 14×19, góc trên tròn hơn góc dưới.
                Box(
                    Modifier
                        .size(width = 14.dp, height = 19.dp)
                        .background(
                            Brush.verticalGradient(listOf(AmberLight, Amber)),
                            RoundedCornerShape(
                                topStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 45,
                                bottomStartPercent = 45,
                            ),
                        ),
                )
            }
            Text(
                text = "Tròn $target biến",
                modifier = Modifier.padding(top = 16.dp),
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    color = colors.featTitle,
                ),
            )
            Text(
                text = "Vòng $round đã xong. Tùy hỷ công đức — xin hồi hướng cho tất cả chúng sanh.",
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = colors.featSub,
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PillButton(
                    label = "Niệm tiếp vòng mới",
                    bg = Amber,
                    bd = Color.Transparent,
                    fg = OnAmber,
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    label = "Xong",
                    bg = Color.Transparent,
                    bd = colors.featAcc2,
                    fg = colors.featTitle,
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Hạt bụi vàng + tia sáng (canvas `mlFx` của prototype) ───────────────────

/** Hạt bụi vàng bay lên chậm ở nền. */
private class Mote(
    var x: Float,
    var y: Float,
    val r: Float,
    val vy: Float,
    val vx: Float,
    var ph: Float,
    val sp: Float,
    val a: Float,
)

/** Hạt tia sáng khi lần hạt, hoặc vòng sóng ([ring]). */
private class Spark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val r: Float,
    val a: Float,
    var t: Float,
    val life: Float,
    val ring: Boolean = false,
    val r0: Float = 0f,
    val r1: Float = 0f,
)

/**
 * Trạng thái hiệu ứng nền màn Niệm Phật. Đơn vị vận tốc/tuổi thọ tính theo "frame 60Hz"
 * như prototype; [step] nhân theo `dt` để chạy đúng trên màn 90/120Hz.
 */
private class MalaFx {
    private val motes = ArrayList<Mote>()
    private val sparks = ArrayList<Spark>()
    private var width = 0f
    private var height = 0f
    private var canvasOrigin = Offset.Zero
    private var center = Offset.Zero

    /** Đổi mỗi frame để buộc canvas vẽ lại. */
    var frame by mutableLongStateOf(0L)
        private set

    fun onCanvasPlaced(origin: Offset, w: Float, h: Float) {
        canvasOrigin = origin
        if (w != width || h != height) {
            width = w
            height = h
            if (motes.isEmpty() && w > 0f && h > 0f) {
                repeat(46) {
                    motes += Mote(
                        x = Random.nextFloat() * w,
                        y = Random.nextFloat() * h,
                        r = 0.6f + Random.nextFloat() * 2.2f,
                        vy = -(0.08f + Random.nextFloat() * 0.28f),
                        vx = (Random.nextFloat() - 0.5f) * 0.12f,
                        ph = Random.nextFloat() * 6.28f,
                        sp = 0.01f + Random.nextFloat() * 0.025f,
                        a = 0.18f + Random.nextFloat() * 0.4f,
                    )
                }
            }
        }
    }

    /** Tâm chuỗi hạt theo toạ độ root. */
    fun setCenter(rootCenter: Offset) {
        center = rootCenter
    }

    fun burst(big: Boolean) {
        val x = center.x - canvasOrigin.x
        val y = center.y - canvasOrigin.y
        val n = if (big) 54 else 26
        repeat(n) {
            val a = Random.nextFloat() * 6.283f
            val v = (if (big) 2.2f else 1.6f) + Random.nextFloat() * (if (big) 4.2f else 2.6f)
            sparks += Spark(
                x = x + cos(a) * 18f, y = y + sin(a) * 18f,
                vx = cos(a) * v, vy = sin(a) * v,
                r = 0.8f + Random.nextFloat() * (if (big) 2.6f else 1.8f),
                a = 0.6f + Random.nextFloat() * 0.4f,
                t = 0f,
                life = 50f + Random.nextFloat() * (if (big) 60f else 36f),
            )
        }
        sparks += Spark(x, y, 0f, 0f, 0f, 0f, 0f, if (big) 70f else 46f, ring = true, r0 = 22f, r1 = if (big) 170f else 96f)
        if (big) sparks += Spark(x, y, 0f, 0f, 0f, 0f, -10f, 60f, ring = true, r0 = 22f, r1 = 120f)
    }

    /** Tiến mô phỏng [f] "frame 60Hz". */
    fun step(f: Float) {
        for (m in motes) {
            m.y += m.vy * f
            m.x += (m.vx + sin(m.ph) * 0.12f) * f
            m.ph += m.sp * f
            if (m.y < -6f) {
                m.y = height + 6f
                m.x = Random.nextFloat() * width
            }
        }
        val it = sparks.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.t += f
            if (p.t / p.life >= 1f) {
                it.remove()
                continue
            }
            if (!p.ring) {
                p.vx *= 0.955f.pow(f)
                p.vy = p.vy * 0.955f.pow(f) - 0.012f * f
                p.x += p.vx * f
                p.y += p.vy * f
            }
        }
        frame++
    }

    fun draw(scope: DrawScope) = with(scope) {
        val glow = Color(0xFFF0BE6E)
        for (m in motes) {
            val al = m.a * (0.55f + 0.45f * sin(m.ph * 1.7f))
            val rr = m.r * 3f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(glow.copy(alpha = al), glow.copy(alpha = 0f)),
                    center = Offset(m.x, m.y),
                    radius = rr,
                ),
                radius = rr,
                center = Offset(m.x, m.y),
            )
        }
        for (p in sparks) {
            if (p.t < 0f) continue
            val k = p.t / p.life
            if (p.ring) {
                val e = 1f - (1f - k).pow(3)
                drawCircle(
                    color = Color(0xFFE8B05C).copy(alpha = 0.55f * (1f - k)),
                    radius = p.r0 + (p.r1 - p.r0) * e,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 1.5f * (1f - k) + 0.3f),
                )
            } else {
                val al = (1f - k) * p.a
                val rr = p.r * (1f - k * 0.4f) * 2.6f
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color(0xFFFFE2A0).copy(alpha = al),
                        0.4f to Color(0xFFE8AA50).copy(alpha = al * 0.7f),
                        1f to Amber.copy(alpha = 0f),
                        center = Offset(p.x, p.y),
                        radius = rr,
                    ),
                    radius = rr,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}

/** Canvas hiệu ứng: vòng lặp theo frame, vẽ bụi vàng + tia sáng. */
@Composable
private fun MalaFxLayer(fx: MalaFx, modifier: Modifier = Modifier) {
    LaunchedEffect(fx) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) fx.step(((now - last) / 16_666_667f).coerceIn(0f, 3f))
                last = now
            }
        }
    }
    Canvas(
        modifier = modifier.onGloballyPositioned {
            fx.onCanvasPlaced(it.positionInRoot(), it.size.width.toFloat(), it.size.height.toFloat())
        },
    ) {
        fx.frame // đọc để buộc vẽ lại mỗi frame
        fx.draw(this)
    }
}

/** Nút pill 48dp cho hàng đáy / hộp thoại. */
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
            state = MalaUiState(beads = 108, round = 1, done = true),
            onBack = {},
            onDone = {},
            onBeadAdvance = {},
            onReset = {},
        )
    }
}

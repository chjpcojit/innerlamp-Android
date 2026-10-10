package com.example.innerlamp.ui.screens.bell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.R
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
import kotlinx.coroutines.launch

private val StageSize = 236.dp
private const val MalletRest = -8f

/** Góc xoay + tịnh tiến ngang của thân chuông rung tắt dần (prototype `_bAnim`). */
private val WobbleKeys = listOf(0f, 0f, 1.8f, -1.5f, 1.2f, -0.95f, 0.75f, -0.55f, 0.4f, -0.28f, 0.18f, -0.1f, 0f)

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
                PadReadout(state = state, modifier = Modifier.padding(top = 10.dp))
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
 * Sân khấu gõ: quầng sáng + vùng chạm tròn 188dp + ảnh mõ/chuông, búa gỗ và vòng sóng.
 * Mõ: búa giáng xuống, mõ nhún nhẹ, một vòng sóng. Chuông: búa chạm, thân chuông rung tắt
 * dần, ba vòng sóng lan. Quầng sáng bừng lên theo `hitMo` (.55) / `hitChuong` (1).
 */
@Composable
private fun Pad(
    state: BellUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haloOpacity by animateFloatAsState(
        targetValue = when {
            state.hitChuong -> 1f
            state.hitMo -> 0.55f
            else -> 0f
        },
        animationSpec = tween(500),
        label = "bellHaloOpacity",
    )
    val haloScale by animateFloatAsState(
        targetValue = if (state.hitChuong) 1.08f else 1f,
        animationSpec = tween(500),
        label = "bellHaloScale",
    )

    Box(modifier = modifier.size(StageSize), contentAlignment = Alignment.Center) {
        // Quầng sáng: radial-gradient(circle, rgba(217,150,60,.32), transparent 68%).
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloOpacity
                }
                .drawBehind {
                    val radius = size.maxDimension * 0.7071f * 0.68f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IlGoldBright.copy(alpha = 0.32f), Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                        center = center,
                    )
                },
        )
        // Vùng chạm tròn 188dp (trong suốt). Nhận ngay khi ngón tay chạm xuống
        // (`onPointerDown` của prototype) để gõ mõ liên tục không bị trễ nhịp.
        val currentOnTap by rememberUpdatedState(onTap)
        Box(
            modifier = Modifier
                .size(188.dp)
                .clip(CircleShape)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    onClick { currentOnTap(); true }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { currentOnTap() })
                },
        )
        BellStage(state = state)
    }
}

/** Mặt mõ/chuông + búa + sóng, vẽ trên hệ toạ độ 236×236dp của prototype. */
@Composable
private fun BellStage(state: BellUiState, modifier: Modifier = Modifier) {
    val chuong = state.showsChuong
    val haptic = LocalHapticFeedback.current

    val malletRot = remember { Animatable(MalletRest) }
    val moScale = remember { Animatable(1f) }
    val moShift = remember { Animatable(0f) }
    val wobble = remember { Animatable(0f) }
    val ripples = remember { mutableStateListOf<Ripple>() }
    var nextRippleId by remember { mutableIntStateOf(0) }

    var prevMo by remember { mutableIntStateOf(state.moCount) }
    var prevChuong by remember { mutableIntStateOf(state.chuongCount) }

    // Tiếng mõ mới → búa + mặt mõ chỉ chuyển động khi đang hiển thị mõ; sóng luôn có.
    LaunchedEffect(state.moCount) {
        val fire = state.moCount > prevMo
        prevMo = state.moCount
        if (!fire) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        ripples += Ripple(nextRippleId++, delayMillis = 70, mo = true)
        if (!chuong) {
            val d = state.moStrikeMillis
            launch { malletRot.animateTo(MalletRest, malletKeys(d, 0.27f, 0.5f)) }
            launch {
                moScale.snapTo(1f)
                moScale.animateTo(1f, keyframes {
                    durationMillis = d
                    1f at 0
                    1f at (d * 0.27f).toInt()
                    0.962f at (d * 0.38f).toInt()
                    1.01f at (d * 0.62f).toInt()
                    1f at d
                })
            }
            launch {
                moShift.animateTo(0f, keyframes {
                    durationMillis = d
                    0f at 0
                    0f at (d * 0.27f).toInt()
                    -2f at (d * 0.38f).toInt()
                    0f at (d * 0.62f).toInt()
                    0f at d
                })
            }
        }
    }

    // Tiếng chuông mới → ba vòng sóng; búa + thân chuông rung chỉ khi đang hiển thị chuông.
    LaunchedEffect(state.chuongCount) {
        val fire = state.chuongCount > prevChuong
        prevChuong = state.chuongCount
        if (!fire) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        listOf(70, 420, 820).forEach { ripples += Ripple(nextRippleId++, delayMillis = it, mo = false) }
        if (chuong) {
            launch { malletRot.animateTo(MalletRest, malletKeys(480, 0.15f, 0.32f)) }
            launch {
                wobble.animateTo(0f, keyframes {
                    durationMillis = 2200
                    val n = WobbleKeys.size
                    WobbleKeys.forEachIndexed { i, r ->
                        val t = when (i) {
                            0 -> 0f
                            1 -> 0.035f
                            else -> 0.035f + (i - 1f) / (n - 2f) * 0.965f
                        }
                        r at (t * 2200).toInt() using LinearEasing
                    }
                })
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ripples.toList().forEach { ripple ->
            key(ripple.id) {
                RippleRing(ripple = ripple, onDone = { ripples.remove(ripple) })
            }
        }

        // Ảnh mõ/chuông 200dp tại (18,18).
        Image(
            painter = painterResource(if (chuong) R.drawable.bell_chuong else R.drawable.bell_mo),
            contentDescription = if (chuong) "Chuông" else "Mõ",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(18.dp, 18.dp)
                .size(200.dp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, if (chuong) 0.30f else 0.85f)
                    if (chuong) {
                        rotationZ = wobble.value
                        translationX = wobble.value * 0.6f * density
                    } else {
                        scaleX = moScale.value
                        scaleY = moScale.value
                        translationX = moShift.value * density
                        translationY = -moShift.value * density
                    }
                },
        )

        Mallet(rotation = malletRot.value, chuong = chuong)
    }
}

/** Keyframes búa: nghỉ -8° → ngả +2° (10%) → giáng -37° → nảy -26° → về -8°. */
private fun malletKeys(durationMs: Int, hitAt: Float, reboundAt: Float): KeyframesSpec<Float> =
    keyframes {
        durationMillis = durationMs
        MalletRest at 0
        2f at (durationMs * 0.1f).toInt()
        -37f at (durationMs * hitAt).toInt() using CubicBezierEasing(0.2f, 0f, 0.4f, 1f)
        -26f at (durationMs * reboundAt).toInt()
        MalletRest at durationMs
    }

/** Cán búa gỗ 10×110dp tại (221,126), xoay quanh đáy; đầu búa đỏ (chuông) hoặc gỗ tròn (mõ). */
@Composable
private fun Mallet(rotation: Float, chuong: Boolean) {
    val density = LocalDensity.current.density
    val wood = Brush.horizontalGradient(
        0f to Color(0xFF4A2C14),
        0.40f to Color(0xFFA8743C),
        0.55f to Color(0xFFC99560),
        1f to Color(0xFF5E3A1C),
    )
    Box(
        modifier = Modifier
            .offset(221.dp, 126.dp)
            .size(width = 10.dp, height = 110.dp)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                rotationZ = rotation
            },
    ) {
        Box(Modifier.fillMaxSize().background(wood, RoundedCornerShape(5.dp)))
        if (chuong) {
            Box(
                Modifier
                    .offset((-4).dp, (-6).dp)
                    .size(width = 18.dp, height = 30.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color(0xFF6E1D14),
                            0.45f to Color(0xFFC0392B),
                            0.58f to Color(0xFFE0604A),
                            1f to Color(0xFF7A2318),
                        ),
                        RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp, bottomEnd = 6.dp, bottomStart = 6.dp),
                    ),
            )
        } else {
            Box(
                Modifier
                    .offset((-9).dp, (-14).dp)
                    .size(28.dp)
                    .background(
                        Brush.radialGradient(
                            0f to Color(0xFFD9A46A),
                            0.55f to Color(0xFF8A5527),
                            1f to Color(0xFF3E230F),
                            center = Offset(28f * 0.34f * density, 28f * 0.30f * density),
                            radius = 28f * 0.72f * density,
                        ),
                        CircleShape,
                    ),
            )
        }
    }
}

/** Một vòng sóng lan ra từ mặt mõ/chuông. */
private class Ripple(val id: Int, val delayMillis: Int, val mo: Boolean)

@Composable
private fun RippleRing(ripple: Ripple, onDone: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            tween(
                durationMillis = if (ripple.mo) 520 else 2600,
                delayMillis = ripple.delayMillis,
                easing = CubicBezierEasing(0.2f, 0.6f, 0.3f, 1f),
            ),
        )
        onDone()
    }
    val p = progress.value
    // Phân đoạn tuyến tính khớp keyframes WAAPI: (scale, opacity) tại các mốc.
    val (scale, alpha) = if (ripple.mo) {
        if (p < 0.15f) lerp(1f, 1.02f, p / 0.15f) to lerp(0f, 0.8f, p / 0.15f)
        else lerp(1.02f, 1.24f, (p - 0.15f) / 0.85f) to lerp(0.8f, 0f, (p - 0.15f) / 0.85f)
    } else {
        if (p < 0.08f) lerp(1f, 1.05f, p / 0.08f) to lerp(0f, 0.7f, p / 0.08f)
        else lerp(1.05f, 2f, (p - 0.08f) / 0.92f) to lerp(0.7f, 0f, (p - 0.08f) / 0.92f)
    }
    val color = if (ripple.mo) Color(0xFFD9963C).copy(alpha = 0.55f) else Color(0xFFE8B05C).copy(alpha = 0.6f)
    Box(
        Modifier
            .offset(34.dp, 34.dp)
            .size(168.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = if (p <= 0f) 0f else alpha
            }
            .border((if (ripple.mo) 1.5f else 2f).dp, color, CircleShape),
    )
}

/** Nhãn MÕ/CHUÔNG + số đếm lớn + gợi ý, dưới sân khấu. */
@Composable
private fun PadReadout(state: BellUiState, modifier: Modifier = Modifier) {
    val med = LocalIlMedColors.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.padTag,
                modifier = Modifier.padding(bottom = 4.dp),
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
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Light,
                    fontSize = 34.sp,
                    lineHeight = 34.sp,
                    color = med.ink,
                ),
            )
        }
        Text(
            text = state.padHint,
            modifier = Modifier.padding(top = 6.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 11.5.sp,
                color = med.faint(),
            ),
        )
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

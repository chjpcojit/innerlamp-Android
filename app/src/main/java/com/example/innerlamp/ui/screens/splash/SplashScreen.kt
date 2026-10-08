package com.example.innerlamp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────────────────────
// Màn splash ("Màn hình chờ") — có light + dark, đổi theo theme app.
// Nguồn: prototype `Docs/design_handoff_innerlamp/` (màn `splash`) + launch/README.md.
// Màu lấy từ token `--il-spl-*` (có 2 bộ Light/Dark trong `<helmet>` của prototype).
// ─────────────────────────────────────────────────────────────────────────────

// ── Token `--il-spl-*` theo theme (light/dark) ────────────────────────────────
private data class SplashColors(
    val bgTop: Color,
    val bgBottom: Color,
    val ink: Color,
    val sub: Color,
    val ring: Color,
    val ring2: Color,
    val ring3: Color,
    val glow: Color,
    val halo: Color,
    val border: Color,
    val ray: Color,
    val ray2: Color,
    val flameShadow: Color,   // quầng sáng sau ngọn lửa (box-shadow)
)

private val LightSplashColors = SplashColors(
    bgTop = Color(0xFFFFFCF4),
    bgBottom = Color(0xFFF0E6D4),
    ink = Color(0xFF221E19),
    sub = Color(0xFF221E19).copy(alpha = 0.72f),         // rgba(34,30,25,.72)
    ring = Color(0xFF8A5A18).copy(alpha = 0.34f),         // rgba(138,90,24,.34)
    ring2 = Color(0xFF8A5A18).copy(alpha = 0.26f),
    ring3 = Color(0xFF8A5A18).copy(alpha = 0.18f),
    glow = Color(0xFFB8862F).copy(alpha = 0.2f),          // rgba(184,134,47,.2)
    halo = Color(0xFFB8862F).copy(alpha = 0.22f),
    border = Color(0xFF8A5A18).copy(alpha = 0.38f),       // rgba(138,90,24,.38)
    ray = Color(0xFFB8862F).copy(alpha = 0.3f),
    ray2 = Color(0xFFB8862F).copy(alpha = 0.2f),
    flameShadow = Color(0xFF8A5A18).copy(alpha = 0.4f),   // 0 6px 22px rgba(138,90,24,.4)
)

private val DarkSplashColors = SplashColors(
    bgTop = Color(0xFF2A241C),
    bgBottom = Color(0xFF100E0B),
    ink = Color(0xFFF5EEE0),
    sub = Color(0xFFF5EEE0).copy(alpha = 0.55f),          // rgba(245,238,224,.55)
    ring = Color(0xFFD9963C).copy(alpha = 0.5f),          // rgba(217,150,60,.5)
    ring2 = Color(0xFFD9963C).copy(alpha = 0.4f),
    ring3 = Color(0xFFD9963C).copy(alpha = 0.3f),
    glow = Color(0xFFD9963C).copy(alpha = 0.26f),
    halo = Color(0xFFD9963C).copy(alpha = 0.28f),
    border = Color(0xFFB8862F).copy(alpha = 0.5f),        // rgba(184,134,47,.5)
    ray = Color(0xFFF6D79B).copy(alpha = 0.5f),
    ray2 = Color(0xFFF6D79B).copy(alpha = 0.35f),
    flameShadow = Color(0xFFD9963C).copy(alpha = 0.9f),   // 0 0 30px rgba(217,150,60,.9)
)

/** Truy cập bộ màu splash hiện tại trong composable. */
private val LocalSplashColors = staticCompositionLocalOf { LightSplashColors }

// ── Màu cố định (không đổi theo theme) ────────────────────────────────────────
private val SplashFlameTop = Color(0xFFF6D79B)
private val SplashFlameBottom = Color(0xFFD9963C)
private val SplashDot = Color(0xFFD9963C)
private val Ember1 = Color(0xFFF6D79B)
private val Ember2 = Color(0xFFE8B46A)
private val Ember3 = Color(0xFFF3C673)

/**
 * Màn hình chờ: nền vầng sáng + vòng đèn + ngọn lửa + wordmark + slogan.
 * Tự phát hiệu ứng đốt lửa/ripple/glow như prototype, rồi gọi [onFinished] khi hết
 * thời lượng (xem [SplashViewModel]). Màu đổi theo theme app (Light/Dark/System).
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    viewModel: SplashViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Hết thời lượng → nhường điều hướng (crossfade do NavHost/MainActivity xử lý).
    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    val splashColors = if (LocalIlColors.current.isDark) DarkSplashColors else LightSplashColors
    CompositionLocalProvider(LocalSplashColors provides splashColors) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind { drawSplashBackground(splashColors) },
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SplashEmblem()
                Spacer(Modifier.height(26.dp))
                Wordmark()
                Spacer(Modifier.height(12.dp))
                Slogan()
            }

            LoadingDots(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 62.dp),
            )
        }
    }
}

// ── Nền: radial-gradient(circle at 50% 42%, bgTop, bgBottom 72%) ──────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSplashBackground(
    colors: SplashColors,
) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.42f
    val dx = max(cx, size.width - cx)
    val dy = max(cy, size.height - cy)
    val radius = sqrt(dx * dx + dy * dy)
    drawRect(
        Brush.radialGradient(
            *arrayOf(
                0f to colors.bgTop,
                0.72f to colors.bgBottom,
                1f to colors.bgBottom,
            ),
            center = Offset(cx, cy),
            radius = radius,
        ),
    )
}

// ── Khối trung tâm: quầng sáng + vòng lan + tia quét + vòng đèn ────────────────

@Composable
private fun SplashEmblem(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(420.dp), contentAlignment = Alignment.Center) {
        Glow(Modifier.size(420.dp))
        RippleRing(level = 0, startDelayMs = 0, Modifier.size(300.dp))
        RippleRing(level = 1, startDelayMs = 1100, Modifier.size(300.dp))
        RippleRing(level = 2, startDelayMs = 2200, Modifier.size(300.dp))
        Rays(Modifier.align(Alignment.TopCenter))
        LampRing(Modifier.size(96.dp))
    }
}

// ── Quầng sáng (ilglow): opacity .5↔1 + scale .96↔1.06, 3.4s ──────────────────

@Composable
private fun Glow(modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )
    val alpha = 0.5f + 0.5f * pulse          // 0.5 ↔ 1.0
    val scale = 0.96f + 0.10f * pulse        // 0.96 ↔ 1.06
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        *arrayOf(
                            0f to colors.glow,
                            0.62f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                        center = center,
                        radius = size.minDimension * 0.5f,
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center,
                )
            },
    )
}

// ── Vòng lan (ilripple): scale .34→2.5, opacity 0→.55(14%)→0, 3.4s ─────────────

@Composable
private fun RippleRing(
    level: Int,
    startDelayMs: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSplashColors.current
    val color = when (level) {
        0 -> colors.ring
        1 -> colors.ring2
        else -> colors.ring3
    }
    val transition = rememberInfiniteTransition(label = "ripple_$startDelayMs")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = CubicBezierEasing(0.22f, 0.7f, 0.3f, 1f)),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(startDelayMs),
        ),
        label = "rippleProgress_$startDelayMs",
    )
    val scale = 0.34f + (2.5f - 0.34f) * progress
    val alpha = rippleOpacity(progress)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .border(1.dp, color, CircleShape),
    )
}

/** Đường cong opacity của `ilripple`: 0 → .55 (tại 14%) → 0. */
private fun rippleOpacity(progress: Float): Float = when {
    progress < 0.14f -> (progress / 0.14f) * 0.55f
    else -> 0.55f * (1f - (progress - 0.14f) / 0.86f)
}

// ── Tia quét (ilray): 2 tia mảnh, xoay 0↔28°, 2.6s ease-out .3s ────────────────

@Composable
private fun Rays(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        progress.animateTo(1f, tween(2600, easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)))
    }
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Ray(progress.value, reverse = false, Modifier.align(Alignment.TopCenter).offset(y = 40.dp))
        Ray(progress.value, reverse = true, Modifier.align(Alignment.TopCenter).offset(y = 40.dp))
    }
}

@Composable
private fun Ray(progress: Float, reverse: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val p = if (reverse) 1f - progress else progress
    val angle = 28f * p * (if (reverse) -1f else 1f)
    val scaleY = 0.4f + 0.6f * p
    val alpha = rayOpacity(p)
    Box(
        modifier = modifier
            .width(2.dp)
            .height(190.dp)
            .graphicsLayer {
                rotationZ = angle
                this.scaleY = scaleY
                this.alpha = alpha
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, if (reverse) colors.ray2 else colors.ray, Color.Transparent),
                ),
            ),
    )
}

/** Đường cong opacity của `ilray`: 0 → .5 (tại 30%) → 0. */
private fun rayOpacity(progress: Float): Float = when {
    progress < 0.30f -> (progress / 0.30f) * 0.5f
    else -> 0.5f * (1f - (progress - 0.30f) / 0.70f)
}

// ── Vòng đèn 96px + ngọn lửa + tàn lửa (ilrise → ilignite → ilpulse/ilember) ───

@Composable
private fun LampRing(modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val density = LocalDensity.current
    val rise = remember { Animatable(0f) }
    LaunchedEffect(Unit) { rise.animateTo(1f, tween(700)) }

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = rise.value
                translationY = (1f - rise.value) * 10f * density.density
            }
            .drawBehind {
                // Quầng sáng quanh ngọn lửa (halo): radial tại đáy vòng.
                drawCircle(
                    brush = Brush.radialGradient(
                        *arrayOf(
                            0f to colors.halo,
                            0.68f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, size.height * 0.72f),
                        radius = size.minDimension * 0.5f,
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center,
                )
            }
            .border(1.dp, colors.border, CircleShape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Flame(Modifier.padding(bottom = 22.dp))

        // Ba tàn lửa bay lên, lệch pha (left 40% / 50% / 60%).
        Ember(Ember1, sizeDp = 3f, durationMs = 4200, delayMs = 900, offsetX = (-10).dp, Modifier.align(Alignment.BottomCenter))
        Ember(Ember2, sizeDp = 2f, durationMs = 4600, delayMs = 1900, offsetX = 0.dp, Modifier.align(Alignment.BottomCenter))
        Ember(Ember3, sizeDp = 2.5f, durationMs = 5000, delayMs = 2800, offsetX = 10.dp, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Flame(modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // ignite: lửa bùng — scale .2 → 1.22 → .92 → 1, opacity 0 → 1 (42%).
        delay(250)
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = keyframes {
                        durationMillis = 1100
                        1.22f at 462
                        0.92f at 770
                    },
                )
            }
            launch { alpha.animateTo(1f, tween(462)) }
        }
        // pulse: nhịp thở opacity .55 ↔ .95 (ilpulse 2.6s), nối tiếp liền mạch.
        alpha.snapTo(0.55f)
        while (true) {
            alpha.animateTo(0.95f, tween(1300, easing = FastOutSlowInEasing))
            alpha.animateTo(0.55f, tween(1300, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier.size(14.dp, 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Quầng sáng sau ngọn lửa (thay cho box-shadow của prototype).
        Box(
            Modifier
                .size(34.dp)
                .background(
                    Brush.radialGradient(
                        listOf(colors.flameShadow, Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        // Ngọn lửa.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .background(
                    Brush.verticalGradient(listOf(SplashFlameTop, SplashFlameBottom)),
                    SplashFlameShape,
                ),
        )
    }
}

@Composable
private fun Ember(
    color: Color,
    sizeDp: Float,
    durationMs: Int,
    delayMs: Int,
    offsetX: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition(label = "ember_$delayMs")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delayMs),
        ),
        label = "emberProgress_$delayMs",
    )
    val alpha = when {
        progress < 0.18f -> (progress / 0.18f) * 0.85f
        else -> 0.85f * (1f - (progress - 0.18f) / 0.82f)
    }
    Box(
        modifier = modifier
            .offset(x = offsetX, y = (-30).dp)
            .graphicsLayer {
                translationY = -190f * density.density * progress
                this.alpha = alpha
            }
            .size(sizeDp.dp)
            .background(color, CircleShape),
    )
}

// ── Wordmark (ilword): Lora 34, fade + letter-spacing .34em → 0, 1.1s .55s ─────

@Composable
private fun Wordmark(modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(550)
        a.animateTo(1f, tween(1100, easing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f)))
    }
    // .34em của 34px ≈ 11.56sp — thu dần về 0 (prototype cũng blur 6px → 0, bỏ blur).
    val spacing = 11.56f * (1f - a.value)
    Text(
        text = "InnerLamp",
        modifier = modifier.graphicsLayer { alpha = a.value },
        style = TextStyle(
            fontFamily = Lora,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            letterSpacing = spacing.sp,
            color = colors.ink,
        ),
    )
}

// ── Slogan (ilrise): Lora italic 15, nổi lên 10px, .8s .95s ────────────────────

@Composable
private fun Slogan(modifier: Modifier = Modifier) {
    val colors = LocalSplashColors.current
    val density = LocalDensity.current
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(950)
        a.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    Text(
        text = "Tự thắp đèn tâm, tự soi lối về",
        modifier = modifier.graphicsLayer {
            alpha = a.value
            translationY = (1f - a.value) * 10f * density.density
        },
        style = TextStyle(
            fontFamily = Lora,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 15.sp,
            letterSpacing = 0.15.sp,
            color = colors.sub,
        ),
    )
}

// ── Ba chấm tải (ildot): opacity .25↔.9, lệch pha 0/.2/.4s ─────────────────────

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        repeat(3) { i ->
            val transition = rememberInfiniteTransition(label = "dot_$i")
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 200),
                ),
                label = "dotAlpha_$i",
            )
            Box(
                Modifier
                    .size(5.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .background(SplashDot, CircleShape),
            )
        }
    }
}

// ── Hình ngọn lửa (giọt tròn đỉnh, thuôn đáy) — border-radius 50%/62%...38% ────

private object SplashFlameShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Đỉnh tròn rộng (50% ngang / 62% dọc).
            moveTo(0f, h * 0.50f)
            cubicTo(0f, h * 0.18f, w * 0.22f, 0f, w * 0.50f, 0f)
            cubicTo(w * 0.78f, 0f, w, h * 0.18f, w, h * 0.50f)
            // Đáy thuôn tròn (46% ngang / 38% dọc).
            cubicTo(w, h * 0.82f, w * 0.72f, h, w * 0.50f, h)
            cubicTo(w * 0.28f, h, 0f, h * 0.82f, 0f, h * 0.50f)
            close()
        }
        return Outline.Generic(path)
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreviewLight() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SplashScreen(onFinished = {}, viewModel = SplashViewModel())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SplashScreen(onFinished = {}, viewModel = SplashViewModel())
    }
}

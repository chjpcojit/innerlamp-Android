package com.example.innerlamp.ui.screens.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Màn onboarding 3 bước (giới thiệu → chọn ý hướng → chuông tỉnh thức),
 * dựng lại đúng pixel từ prototype `Docs/design_handoff_innerlamp/`.
 *
 * Navigation được đẩy ra ngoài qua callback để màn hình không phụ thuộc NavHost:
 * - [onComplete] — nhấn "Tạo tài khoản" ở bước cuối (→ màn signup).
 * - [onSkip] — nhấn "Bỏ qua" (→ màn signup).
 * - [onLogin] — nhấn "Tôi đã có tài khoản" ở bước đầu (→ màn login).
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onLogin: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalIlColors.current

    val ctaLabel = when (state.step) {
        0 -> "Bắt đầu"
        1 -> "Tiếp tục"
        else -> "Tạo tài khoản"
    }
    val backLabel = if (state.step == 0) "Tôi đã có tài khoản" else "Quay lại"
    val onBackClick: () -> Unit =
        if (state.step == 0) onLogin else viewModel::back

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.card2, colors.paper))),
    ) {
        // Quầng đèn phía trên, lệch ra ngoài khung (prototype: top:-120px, 420px).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-120).dp)
                .size(420.dp)
                .drawBehind {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IlGoldBright.copy(alpha = 0.20f), Color.Transparent),
                            center = center,
                            radius = size.width * 0.62f,
                        ),
                        radius = size.width / 2f,
                        center = center,
                    )
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp)
                .padding(top = 20.dp, bottom = 18.dp),
        ) {
            OnboardingHeader(onSkip = onSkip)

            Spacer(Modifier.height(26.dp))

            ProgressBar(step = state.step)

            Crossfade(
                targetState = state.step,
                animationSpec = tween(170),
                label = "onboarding_step",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { step ->
                when (step) {
                    0 -> IntroStep(Modifier.fillMaxSize())
                    1 -> IntentStep(
                        selected = state.selectedIntents,
                        onToggle = viewModel::toggleIntent,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> BellStep(
                        enabled = state.enabledBells,
                        onToggle = viewModel::toggleBell,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            OnboardingCta(
                label = ctaLabel,
                onClick = { if (state.isLastStep) onComplete() else viewModel.next() },
            )

            OnboardingBack(label = backLabel, onClick = onBackClick)
        }
    }
}

// ── Header: logo + wordmark + nút "Bỏ qua" ────────────────────────────────────

@Composable
private fun OnboardingHeader(onSkip: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.4.dp, IlGold, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .background(IlGoldBright, CircleShape),
                )
            }
            Spacer(Modifier.width(9.dp))
            Text(
                text = "INNERLAMP",
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 2.07.sp,
                    color = colors.ink,
                ),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Bỏ qua",
            modifier = Modifier.clickable(onClick = onSkip),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = colors.ink.copy(alpha = 0.45f),
            ),
        )
    }
}

// ── Thanh tiến trình 3 đoạn ──────────────────────────────────────────────────

@Composable
private fun ProgressBar(step: Int, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (i in 0 until OnboardingStepCount) {
            val filled = i == 0 || step >= i
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(
                        if (filled) IlGold else colors.ink.copy(alpha = 0.14f),
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

// ── Bước 0: giới thiệu ───────────────────────────────────────────────────────

@Composable
private fun IntroStep(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        LampRing()
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Thắp ngọn đèn\ntrong tâm",
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 46.sp,
                color = colors.ink,
            ),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Light the lamp within",
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = colors.ink.copy(alpha = 0.5f),
            ),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Một nơi tĩnh lặng để bạn thiền định, niệm Phật, tụng kinh và ghi lại hành trình tu tập mỗi ngày.",
            modifier = Modifier.widthIn(max = 300.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Light,
                fontSize = 15.sp,
                lineHeight = 25.sp,
                color = colors.ink.copy(alpha = 0.66f),
            ),
        )
    }
}

// ── Bước 1: chọn ý hướng (chip) ──────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentStep(
    selected: Set<PracticeIntent>,
    onToggle: (PracticeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Bạn mong cầu\nđiều gì?",
            style = stepTitleStyle(colors.ink),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Chọn một hoặc nhiều",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = colors.ink.copy(alpha = 0.5f),
            ),
        )
        Spacer(Modifier.height(26.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PracticeIntent.entries.forEach { intent ->
                IntentChip(
                    intent = intent,
                    selected = intent in selected,
                    onClick = { onToggle(intent) },
                )
            }
        }
    }
}

@Composable
private fun IntentChip(
    intent: PracticeIntent,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val bg = if (selected) colors.ink else colors.card.copy(alpha = 0.8f)
    val fg = if (selected) colors.onInk else colors.ink.copy(alpha = 0.75f)
    val bd = if (selected) colors.ink else colors.ink.copy(alpha = 0.16f)
    Text(
        text = intent.label,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 11.dp),
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            color = fg,
        ),
    )
}

// ── Bước 2: chuông tỉnh thức (hàng toggle) ───────────────────────────────────

@Composable
private fun BellStep(
    enabled: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Chuông tỉnh thức",
            style = stepTitleStyle(colors.ink),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Tiếng chuông sẽ nhắc bạn trở về hơi thở",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = colors.ink.copy(alpha = 0.5f),
            ),
        )
        Spacer(Modifier.height(26.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BellOptions.forEachIndexed { index, bell ->
                BellRow(
                    bell = bell,
                    on = index in enabled,
                    onClick = { onToggle(index) },
                )
            }
        }
    }
}

@Composable
private fun BellRow(
    bell: BellOption,
    on: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val borderColor = if (on) IlGold.copy(alpha = 0.55f) else colors.ink.copy(alpha = 0.10f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hình chuông (glyph) đặt trong vòng tròn.
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (on) IlGold.copy(alpha = 0.18f) else colors.ink.copy(alpha = 0.06f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(
                        if (on) IlGold else colors.ink.copy(alpha = 0.35f),
                        RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomEnd = 5.dp, bottomStart = 5.dp),
                    ),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = bell.label,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = colors.ink,
                ),
            )
            Text(
                text = bell.sub,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = colors.ink.copy(alpha = 0.45f),
                ),
            )
        }
        ToggleSwitch(on = on)
    }
}

@Composable
private fun ToggleSwitch(on: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val track = if (on) IlGold else colors.ink.copy(alpha = 0.16f)
    Box(
        modifier = modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(track),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 3.dp)
                .offset(x = if (on) 18.dp else 0.dp)
                .size(20.dp)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

// ── CTA + nhãn quay lại ──────────────────────────────────────────────────────

@Composable
private fun OnboardingCta(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = colors.ink.copy(alpha = 0.16f),
                spotColor = colors.ink.copy(alpha = 0.16f),
            )
            .clip(RoundedCornerShape(999.dp))
            .background(colors.ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colors.onInk,
            ),
        )
    }
}

@Composable
private fun OnboardingBack(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clickable(onClick = onClick),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            color = colors.ink.copy(alpha = 0.42f),
        ),
    )
}

// ── Minh họa: vòng đèn + ngọn lửa ────────────────────────────────────────────

@Composable
private fun LampRing(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val glow = remember(density) {
        with(density) {
            Brush.radialGradient(
                colors = listOf(IlGoldBright.copy(alpha = 0.26f), Color.Transparent),
                center = Offset(52.dp.toPx(), 77.dp.toPx()),
                radius = 68.dp.toPx(),
            )
        }
    }
    Box(
        modifier = modifier
            .size(104.dp)
            .background(glow, CircleShape)
            .border(1.dp, IlGold.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Flame(Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun Flame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(18.dp, 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Quầng sáng sau ngọn lửa (thay cho box-shadow của prototype).
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    Brush.radialGradient(
                        listOf(IlGoldBright.copy(alpha = 0.85f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        // Ngọn lửa.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(IlGoldBright2, IlGoldBright)),
                    FlameShape,
                ),
        )
    }
}

/** Hình ngọn lửa (teardrop nhọn ở đỉnh). */
private object FlameShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.98f, h * 0.32f, w * 0.92f, h * 0.70f, w * 0.5f, h)
            cubicTo(w * 0.08f, h * 0.70f, w * 0.02f, h * 0.32f, w * 0.5f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

// ── Tiện ích ─────────────────────────────────────────────────────────────────

private fun stepTitleStyle(color: Color): TextStyle = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    color = color,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        OnboardingScreen(
            onComplete = {},
            onSkip = {},
            onLogin = {},
            viewModel = OnboardingViewModel(),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        OnboardingScreen(
            onComplete = {},
            onSkip = {},
            onLogin = {},
            viewModel = OnboardingViewModel(),
        )
    }
}

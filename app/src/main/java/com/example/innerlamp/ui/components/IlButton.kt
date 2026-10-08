package com.example.innerlamp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlColors
import com.example.innerlamp.ui.theme.IlDanger
import com.example.innerlamp.ui.theme.IlElevation
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlSize
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

/** Biến thể của [IlButton] (prop `variant` của prototype). */
enum class IlButtonVariant { Primary, Gold, Ghost, Light, OnDark, Danger }

// ─────────────────────────────────────────────────────────────────────────────
// Nút chung (prototype IL-Btn). Trạng thái loading/disabled thay vì enum `state`:
// `loading` ưu tiên hơn `enabled`. Hover đổi nền, pressed thu nhỏ scale(.978).
// ─────────────────────────────────────────────────────────────────────────────

private data class BtnPalette(
    val bg: Color,
    val fg: Color,
    val bd: Color,
    val hover: Color,
    val shadow: Color?,
    val spinnerTrack: Color,
)

private fun btnPalette(variant: IlButtonVariant, colors: IlColors): BtnPalette = when (variant) {
    IlButtonVariant.Primary -> BtnPalette(
        bg = colors.ink,
        fg = colors.canvas,
        bd = colors.ink,
        hover = lerp(colors.ink, Color.White, if (colors.isDark) 0.06f else 0.09f),
        shadow = colors.ink.copy(alpha = 0.16f),
        spinnerTrack = Color(0x47FFFFFF), // rgba(255,255,255,.28)
    )
    IlButtonVariant.Gold -> BtnPalette(
        bg = IlGold,
        fg = colors.card,
        bd = IlGold,
        hover = Color(0xFFA2751F),
        shadow = IlGold.copy(alpha = 0.24f),
        spinnerTrack = Color(0x47FFFFFF),
    )
    IlButtonVariant.Ghost -> BtnPalette(
        bg = colors.card,
        fg = colors.ink,
        bd = colors.ink.copy(alpha = 0.18f),
        hover = colors.hover,
        shadow = null,
        spinnerTrack = colors.ink.copy(alpha = 0.18f),
    )
    IlButtonVariant.Light -> BtnPalette(
        bg = Color(0xFFF5EEE0),
        fg = Color(0xFF221E19),
        bd = Color(0xFFF5EEE0),
        hover = Color(0xFFE9DFCB),
        shadow = null,
        spinnerTrack = colors.ink.copy(alpha = 0.18f),
    )
    IlButtonVariant.OnDark -> BtnPalette(
        bg = Color.Transparent,
        fg = Color(0xFFF5EEE0),
        bd = Color(0x3DF2EBDD), // rgba(242,235,221,.24)
        hover = Color(0x1AF5EEE0), // rgba(245,238,224,.1)
        shadow = null,
        spinnerTrack = colors.ink.copy(alpha = 0.18f),
    )
    IlButtonVariant.Danger -> BtnPalette(
        bg = colors.card,
        fg = IlDanger,
        bd = IlDanger.copy(alpha = 0.40f),
        hover = Color(0x24C2857F), // rgba(194,133,127,.14)
        shadow = null,
        spinnerTrack = colors.ink.copy(alpha = 0.18f),
    )
}

@Composable
fun IlButton(
    label: String,
    modifier: Modifier = Modifier,
    variant: IlButtonVariant = IlButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingLabel: String = "Đang xử lý…",
    full: Boolean = true,
    onClick: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    val palette = btnPalette(variant, colors)
    val off = !enabled || loading
    val shape = RoundedCornerShape(IlRadius.Pill)

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()

    val bg = when {
        !enabled -> colors.ink.copy(alpha = 0.12f)
        hovered && !off -> palette.hover
        else -> palette.bg
    }
    val fg = if (!enabled) colors.ink.copy(alpha = 0.38f) else palette.fg
    val bd = if (!enabled) Color.Transparent else palette.bd
    val showShadow = enabled && !loading && !pressed && palette.shadow != null
    val scale = if (pressed && enabled && !loading) 0.978f else 1f

    Row(
        modifier = modifier
            .then(
                if (showShadow) {
                    Modifier.shadow(
                        elevation = IlElevation.Primary,
                        shape = shape,
                        ambientColor = palette.shadow!!,
                        spotColor = palette.shadow!!,
                    )
                } else Modifier
            )
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(IlSize.Button)
            .then(if (full) Modifier.fillMaxWidth() else Modifier)
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !off,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            Spinner(color = fg, track = palette.spinnerTrack)
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text = if (loading) loadingLabel else label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                letterSpacing = 0.14.sp,
                color = fg,
            ),
        )
    }
}

/** Spinner 14px, vòng 2px quay 0.9s (khớp keyframe `ilspin` của prototype). */
@Composable
private fun Spinner(color: Color, track: Color) {
    val transition = rememberInfiniteTransition(label = "btn_spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "btn_spinner_angle",
    )
    Canvas(Modifier.size(14.dp)) {
        val stroke = 2.dp.toPx()
        drawCircle(color = track, style = Stroke(width = stroke))
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun IlButtonPreview() {
    InnerLampTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IlButton("Bắt đầu thời khóa")
            IlButton("Cúng dường", variant = IlButtonVariant.Gold)
            IlButton("Đăng ký", variant = IlButtonVariant.Ghost)
            IlButton("Đang xử lý…", loading = true)
            IlButton("Xoá dữ liệu", variant = IlButtonVariant.Danger)
        }
    }
}

package com.example.innerlamp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlDarkInkSoft
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.IlLampFlame
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Logo InnerLamp (prototype IL-Logo): vòng tròn viền vàng + quầng sáng + ngọn lửa hai lớp.
// Mọi kích thước tính theo tỉ lệ `size` như `renderVals()` của prototype.
// ─────────────────────────────────────────────────────────────────────────────

/** Khung viewBox 40×56 của SVG ngọn lửa trong prototype. */
private const val FLAME_VB_W = 40f
private const val FLAME_VB_H = 56f
private const val FLAME_OUTER =
    "M23 1C25 11 36 19 36 34c0 12-8 20-16 20S4 47 4 37c0-9 6-14 9-21 2 7 5 10 8 11-2-9-2-17 2-26z"
private const val FLAME_INNER =
    "M21 30c2 5 5 8 5 12a6 6 0 0 1-12 0c0-3 2-5 3-8 1 2 2 3 3 3-1-2-1-4 1-7z"

/**
 * @param size đường kính vòng tròn (prototype `size`, mặc định 32).
 * @param wordmark hiện chữ "INNERLAMP" bên phải.
 * @param dark dùng trên nền luôn tối (viền vàng sáng, chữ kem).
 */
@Composable
fun IlLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    wordmark: Boolean = false,
    dark: Boolean = false,
) {
    val colors = LocalIlColors.current
    val n = size.value
    val flameW = n * 0.31f
    val flameH = flameW * FLAME_VB_H / FLAME_VB_W
    val outer = remember { PathParser().parsePathString(FLAME_OUTER).toPath() }
    val inner = remember { PathParser().parsePathString(FLAME_INNER).toPath() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((n * 0.3f).dp),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .drawBehind {
                    // radial-gradient(circle at 50% 74%, rgba(217,150,60,.22), transparent 62%)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IlGoldBright.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(this.size.width * 0.5f, this.size.height * 0.74f),
                            radius = this.size.width * 0.62f,
                        ),
                    )
                }
                .border((n * 1.5f / 32f).dp, if (dark) IlGoldBright else IlGold, CircleShape),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Canvas(
                Modifier
                    .padding(bottom = (n * 0.22f).dp)
                    .size(width = flameW.dp, height = flameH.dp),
            ) {
                // Quầng drop-shadow quanh ngọn lửa.
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(IlGoldBright.copy(alpha = 0.5f), Color.Transparent),
                        center = center,
                        radius = this.size.width,
                    ),
                    radius = this.size.width,
                )
                scale(this.size.width / FLAME_VB_W, this.size.height / FLAME_VB_H, pivot = Offset.Zero) {
                    drawPath(outer, IlLampFlame)
                    drawPath(inner, IlGoldBright2)
                }
            }
        }
        if (wordmark) {
            Text(
                text = "INNERLAMP",
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 2.6.sp,
                    color = if (dark) IlDarkInkSoft else colors.ink,
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 260, heightDp = 140)
@Composable
private fun IlLogoPreview() {
    InnerLampTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IlLogo(size = 56.dp)
            IlLogo(size = 100.dp)
        }
    }
}

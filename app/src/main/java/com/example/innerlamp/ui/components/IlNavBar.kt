package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlSize
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Header chung (prototype IL-NavBar): chip back tròn 38px trái, tiêu đề/phụ đề
// căn giữa, nút hành động pill và/hoặc nút ✕ phải. `dark` cho hero nền tối.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlNavBar(
    title: String,
    modifier: Modifier = Modifier,
    sub: String = "",
    right: String = "",
    showBack: Boolean = true,
    showClose: Boolean = false,
    dark: Boolean = false,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onAction: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    val titleColor = if (dark) Color(0xFFF2EBDD) else colors.ink
    val dim = if (dark) Color(0x80F2EBDD) else colors.ink.copy(alpha = 0.48f)
    val glyph = if (dark) Color(0xFFF2EBDD) else colors.ink
    val btnBg = if (dark) Color(0x14F2EBDD) else colors.card
    val btnBd = if (dark) Color(0x2EF2EBDD) else colors.ink.copy(alpha = 0.10f)
    val btnHover = if (dark) Color(0x29F2EBDD) else colors.hover
    val hasSub = sub.isNotBlank()
    val hasRight = right.isNotBlank()

    Row(
        modifier = modifier.fillMaxWidth().height(IlSize.NavBar),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Bên trái: chip back (38px) — luôn giữ chỗ để tiêu đề căn giữa.
        Box(Modifier.size(IlSize.NavBar)) {
            if (showBack) {
                NavBarChip(glyph = glyph, bg = btnBg, bd = btnBd, hover = btnHover, onClick = onBack) {
                    BackGlyph(glyph)
                }
            }
        }
        // Tiêu đề căn giữa.
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = titleColor,
                ),
            )
            if (hasSub) {
                Text(
                    text = sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.5.sp,
                        color = dim,
                    ),
                )
            }
        }
        // Bên phải: nút hành động pill +/hoặc ✕. Chiều rộng tối thiểu 38dp để giữ
        // đối xứng với chip back (tiêu đề căn giữa), nhưng cho phép nút hành động
        // dài (vd "Tải về máy") nới rộng thay vì bị cắt.
        Box(
            modifier = Modifier
                .height(IlSize.NavBar)
                .widthIn(min = IlSize.NavBar),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (hasRight) {
                    Box(
                        modifier = Modifier
                            .height(IlSize.NavBar)
                            .clip(RoundedCornerShape(999.dp))
                            .background(btnBg)
                            .border(1.dp, btnBd, RoundedCornerShape(999.dp))
                            .clickable(onClick = onAction)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = right,
                            style = TextStyle(
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = titleColor,
                            ),
                        )
                    }
                }
                if (showClose) {
                    NavBarChip(glyph = glyph, bg = btnBg, bd = btnBd, hover = btnHover, onClick = onClose) {
                        CloseGlyph(glyph)
                    }
                }
            }
        }
    }
}

/** Chip tròn 38px có viền, đổi nền khi hover. */
@Composable
private fun NavBarChip(
    glyph: Color,
    bg: Color,
    bd: Color,
    hover: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .size(IlSize.NavBar)
            .clip(CircleShape)
            .background(if (hovered) hover else bg)
            .border(1.dp, bd, CircleShape)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Mũi tên quay lại "‹" (nét 1.7px). */
@Composable
private fun BackGlyph(color: Color) {
    Canvas(Modifier.size(8.dp)) {
        val stroke = 1.7.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.62f, size.height * 0.18f)
            lineTo(size.width * 0.36f, size.height * 0.5f)
            lineTo(size.width * 0.62f, size.height * 0.82f)
        }
        drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Nút đóng "✕" (hai nét chéo 1.7px). */
@Composable
private fun CloseGlyph(color: Color) {
    Canvas(Modifier.size(12.dp)) {
        val stroke = 1.7.dp.toPx()
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 120)
@Composable
private fun IlNavBarPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .background(LocalIlColors.current.canvas),
        ) {
            IlNavBar(title = "Thư viện", right = "Aa", onBack = {}, onAction = {})
            IlNavBar(title = "Chia sẻ câu kinh", sub = "Bước 2/3", showClose = true, onClose = {})
        }
    }
}

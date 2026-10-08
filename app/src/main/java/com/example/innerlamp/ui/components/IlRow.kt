package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlSize
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

/** Hình tượng của hàng (prop `glyph` của prototype). */
enum class IlRowGlyph { Leaf, Book, Circle, Dot }

// ─────────────────────────────────────────────────────────────────────────────
// Hàng danh sách (prototype IL-Row): tile icon + tiêu đề/phụ đề + trailing.
// `selected` tô nền gold nhạt + viền gold; `enabled=false` mờ (opacity .5).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlRow(
    title: String,
    modifier: Modifier = Modifier,
    sub: String = "",
    trailing: String = "",
    tone: IlTone = IlTone.Gold,
    glyph: IlRowGlyph = IlRowGlyph.Leaf,
    selected: Boolean = false,
    enabled: Boolean = true,
    dark: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    val toneColor = tone.base(colors)
    val shape = RoundedCornerShape(IlRadius.Row)

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()

    val cardBg = when {
        selected -> if (dark) Color(0xFF2A241C) else IlGold.copy(alpha = 0.10f)
        hovered && enabled -> if (dark) Color(0xFF25211C) else colors.rowHover
        dark -> Color(0xFF1E1B17)
        else -> colors.card
    }
    val cardBd = when {
        selected -> IlGold.copy(alpha = 0.55f)
        hovered && enabled -> if (dark) Color(0x29F2EBDD) else colors.ink.copy(alpha = 0.16f)
        dark -> Color(0x14F2EBDD) // rgba(242,235,221,.08)
        else -> colors.ink.copy(alpha = 0.08f)
    }
    val titleColor = if (dark) Color(0xFFF2EBDD) else colors.ink
    val subColor = if (dark) Color(0x6BF2EBDD) else colors.ink.copy(alpha = 0.50f)
    val trailColor = if (dark) Color(0x59F2EBDD) else colors.ink.copy(alpha = 0.42f)
    val scale = if (pressed && enabled) 0.99f else 1f

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.5f }
            .clip(shape)
            .background(cardBg)
            .border(1.dp, cardBd, shape)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Tile icon 44px, glyph 15px bên trong.
        Box(
            modifier = Modifier
                .size(IlSize.IconTile)
                .background(tone.tint(colors), RoundedCornerShape(IlRadius.IconTile)),
            contentAlignment = Alignment.Center,
        ) {
            RowGlyphIcon(glyph = glyph, color = toneColor)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = titleColor,
                ),
            )
            if (sub.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = subColor,
                    ),
                )
            }
        }
        if (trailing.isNotEmpty()) {
            Text(
                text = trailing,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    color = trailColor,
                ),
            )
        }
    }
}

/** Glyph 15px dạng hình khối (khớp bảng `shapes` của prototype). */
@Composable
private fun RowGlyphIcon(glyph: IlRowGlyph, color: Color) {
    val size = 15.dp
    when (glyph) {
        IlRowGlyph.Leaf -> Box(Modifier.size(size).background(color, LeafShape))
        IlRowGlyph.Book -> Box(
            Modifier.size(size).border(1.5.dp, color, RoundedCornerShape(2.dp)),
        )
        IlRowGlyph.Circle -> Box(Modifier.size(size).border(1.5.dp, color, CircleShape))
        IlRowGlyph.Dot -> Box(Modifier.size(size).background(color, CircleShape))
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 260)
@Composable
private fun IlRowPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IlRow("Thiền hơi thở buổi sáng", sub = "12 phút · Thầy Minh Đạo", trailing = "▶")
            IlRow("Tụng kinh Diệu Pháp Liên Hoa", sub = "Phẩm Tựa", tone = IlTone.Green, glyph = IlRowGlyph.Book)
            IlRow("Niệm Phật", tone = IlTone.Rose, glyph = IlRowGlyph.Circle, selected = true)
        }
    }
}

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
import androidx.compose.foundation.layout.Row
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
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Chip chung (prototype IL-Chip). `selected` đảo nền/chữ, `enabled=false` mờ.
// Hỗ trợ chấm `dot` và nhãn đếm `count`. `dark` cho chip trên nền tối cố định.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    count: String? = null,
    dot: Boolean = false,
    dark: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    val on = selected
    val shape = RoundedCornerShape(IlRadius.Pill)

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()

    val bg = when {
        !enabled -> if (dark) Color(0x0AF2EBDD) else colors.ink.copy(alpha = 0.04f)
        dark && on -> Color(0xFFF5EEE0)
        on -> colors.ink
        hovered -> if (dark) Color(0x1FF2EBDD) else colors.hover
        dark -> Color(0x12F2EBDD)
        else -> colors.card
    }
    val fg = when {
        !enabled -> if (dark) Color(0x4DF2EBDD) else colors.ink.copy(alpha = 0.32f)
        dark && on -> Color(0xFF221E19)
        on -> colors.onInk
        dark -> Color(0x9EF2EBDD) // rgba(242,235,221,.62)
        else -> colors.ink.copy(alpha = 0.70f) // --il-ink-70
    }
    val bd = when {
        !enabled -> Color.Transparent
        dark && on -> Color(0xFFF5EEE0)
        on -> colors.ink
        hovered -> if (dark) Color(0x47F2EBDD) else colors.ink.copy(alpha = 0.24f)
        dark -> Color(0x29F2EBDD) // rgba(242,235,221,.16)
        else -> colors.ink.copy(alpha = 0.14f)
    }
    val countColor = if (dark) {
        if (on) Color(0x803E2219) /* rgba(34,30,25,.5) */ else Color(0x6BF2EBDD)
    } else {
        if (on) colors.onInk.copy(alpha = 0.6f) else colors.ink.copy(alpha = 0.42f)
    }
    val dotColor = if (on && !dark) IlGoldBright else IlGold
    val scale = if (pressed && enabled) 0.96f else 1f

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 15.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (dot) {
            Box(Modifier.size(7.dp).background(dotColor, CircleShape))
        }
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = fg,
            ),
        )
        if (!count.isNullOrEmpty()) {
            Text(
                text = count,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = countColor,
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 220)
@Composable
private fun IlChipPreview() {
    InnerLampTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IlChip("Sắp diễn ra", selected = true)
            IlChip("Tất cả", count = "12")
            IlChip("Đã khoá", enabled = false)
            IlChip("Có mới", dot = true)
        }
    }
}

package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

/** Kiểu hàng cài đặt (prop `kind` của prototype). */
enum class IlSettingKind { Switch, Value, Chevron }

// ─────────────────────────────────────────────────────────────────────────────
// Hàng cài đặt (prototype IL-SettingRow): tiêu đề/phụ đề + switch | value | chevron.
// Có hairline chia dòng; `last` bỏ viền dưới.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    sub: String = "",
    kind: IlSettingKind = IlSettingKind.Chevron,
    value: String = "",
    on: Boolean = false,
    last: Boolean = false,
    dark: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    val titleColor = if (dark) Color(0xFFF2EBDD) else colors.ink
    val subColor = if (dark) Color(0x73F2EBDD) else colors.ink.copy(alpha = 0.45f)
    val divider = when {
        last -> Color.Transparent
        dark -> Color(0x14F2EBDD)
        else -> colors.ink.copy(alpha = 0.06f)
    }
    val chevColor = if (dark) Color(0x4DF2EBDD) else colors.ink.copy(alpha = 0.30f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (!last) {
                    drawLine(
                        color = divider,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = titleColor,
                ),
            )
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = subColor,
                    ),
                )
            }
        }
        when (kind) {
            IlSettingKind.Switch -> Switch(on = on)
            IlSettingKind.Value -> if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = subColor,
                    ),
                )
            }
            IlSettingKind.Chevron -> Text(
                text = "›",
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = chevColor,
                ),
            )
        }
    }
}

/** Switch 44×26, knob 20px dịch 18px khi bật (khớp prototype). */
@Composable
private fun Switch(on: Boolean) {
    val colors = LocalIlColors.current
    val track = if (on) IlGold else colors.ink.copy(alpha = 0.16f)
    Box(
        modifier = Modifier
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

@Preview(showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun IlSettingRowPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(LocalIlColors.current.card),
        ) {
            IlSettingRow("Chuông tỉnh thức", sub = "05:30 · 12:00 · 21:30", kind = IlSettingKind.Switch, on = true)
            IlSettingRow("Ngôn ngữ", value = "Tiếng Việt", kind = IlSettingKind.Value)
            IlSettingRow("Chế độ tối", value = "Theo máy", kind = IlSettingKind.Chevron, last = true)
        }
    }
}

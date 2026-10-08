package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

/** Loại trạng thái (prop `kind` của prototype). */
enum class IlStateKind { Empty, Error, Offline, Done }

// ─────────────────────────────────────────────────────────────────────────────
// Trạng thái trống/lỗi/mất mạng/hoàn thành (prototype IL-State): vòng 88px +
// glyph + tiêu đề Lora + mô tả. Dùng cho màn empty/error/offline/done.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlState(
    title: String,
    modifier: Modifier = Modifier,
    body: String = "",
    kind: IlStateKind = IlStateKind.Empty,
    dark: Boolean = false,
) {
    val colors = LocalIlColors.current
    val accent = when (kind) {
        IlStateKind.Empty -> IlGold
        IlStateKind.Error -> IlRose
        IlStateKind.Offline -> colors.mid2
        IlStateKind.Done -> IlGreen
    }
    val tint = accent.copy(alpha = if (dark) 0.14f else 0.12f)
    val titleColor = if (dark) Color(0xFFF5EEE0) else colors.ink
    val bodyColor = if (dark) Color(0x8CF2EBDD) else colors.ink.copy(alpha = 0.55f)

    Column(
        modifier = modifier.padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(tint, CircleShape)
                .border(1.dp, accent.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            StateGlyph(kind = kind, color = accent)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                color = titleColor,
            ),
        )
        if (body.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 280.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = bodyColor,
                ),
            )
        }
    }
}

/** Glyph 26px theo kind (khớp bảng `kinds` của prototype). */
@Composable
private fun StateGlyph(kind: IlStateKind, color: Color) {
    val size = 26.dp
    val border = 1.6.dp
    when (kind) {
        IlStateKind.Empty -> Box(Modifier.size(size).border(border, color, CircleShape))
        IlStateKind.Error -> Box(Modifier.size(size).border(border, color, RoundedCornerShape(2.dp)))
        IlStateKind.Offline -> Box(Modifier.size(size).border(border, color, LeafShape))
        IlStateKind.Done -> Box(Modifier.size(size).background(color, CircleShape))
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 300)
@Composable
private fun IlStatePreview() {
    InnerLampTheme {
        IlState(
            title = "Chưa có gì ở đây",
            body = "Khi bạn bắt đầu thời khóa, mọi ghi nhận sẽ hiện ở đây.",
            kind = IlStateKind.Empty,
        )
    }
}

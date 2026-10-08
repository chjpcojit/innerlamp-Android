package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Avatar chữ viết tắt (prototype IL-Avatar). `tone` đổi bộ nền/chữ/viền,
// `size` 24–96px. Cỡ chữ tự suy = 34% kích thước.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    tone: IlTone = IlTone.Gold,
    size: Dp = 40.dp,
) {
    val colors = LocalIlColors.current
    val base = tone.base(colors)
    Box(
        modifier = modifier
            .size(size)
            .background(tone.tint(colors), CircleShape)
            .border(1.dp, base.copy(alpha = 0.32f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = (size.value * 0.34f).sp,
                letterSpacing = 0.02f.sp,
                color = tone.fg(colors),
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 140)
@Composable
private fun IlAvatarPreview() {
    InnerLampTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IlAvatar("DT")
            IlAvatar("AN", tone = IlTone.Green, size = 64.dp)
            IlAvatar("HĐ", tone = IlTone.Rose)
            IlAvatar("—", tone = IlTone.Ink)
        }
    }
}

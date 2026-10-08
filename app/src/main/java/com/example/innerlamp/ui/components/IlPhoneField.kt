package com.example.innerlamp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.domain.model.PhoneCountry
import com.example.innerlamp.domain.model.formatE164
import com.example.innerlamp.domain.model.isComplete
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Ô nhập số điện thoại (prototype màn Đăng nhập / Đăng ký):
//   thẻ bo 16 · nhãn 10.5 trong thẻ · [cờ +84 ⌄] │ ô nhập 15 │ ✓ tròn xanh 18
//   viền vàng .55 khi chưa đủ số, ink-12 khi đủ · gợi ý 10.5 lệch trái 16 bên dưới.
// Chạm cụm mã vùng → `onCountryClick` (mở sheet "Chọn quốc gia").
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    country: PhoneCountry,
    onCountryClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Số điện thoại",
    placeholder: String = "0949 123 456",
) {
    val colors = LocalIlColors.current
    val ok = country.isComplete(value)
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (ok) colors.ink.copy(alpha = 0.12f) else IlGold.copy(alpha = 0.55f)
    val hint = if (ok) {
        "Mã xác thực sẽ gửi tới ${country.formatE164(value)}"
    } else {
        "Nhập 0949… hoặc 949… — chúng tôi tự thêm ${country.dial}"
    }
    val textStyle = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = colors.ink,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, shape)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.5.sp,
                    color = colors.ink.copy(alpha = 0.45f),
                ),
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Cụm mã vùng: cờ + dial + chevron.
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCountryClick,
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(text = country.flag, style = TextStyle(fontSize = 19.sp, lineHeight = 22.sp))
                    Text(
                        text = country.dial,
                        style = textStyle.copy(fontWeight = FontWeight.Medium),
                    )
                    ChevronDown(colors.ink.copy(alpha = 0.45f))
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(colors.ink.copy(alpha = 0.12f)),
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(IlGold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle.copy(color = colors.ink.copy(alpha = 0.36f)),
                            )
                        }
                        inner()
                    },
                )
                // Dấu ✓ tròn xanh — luôn giữ chỗ, chỉ đổi độ mờ (như `opacity` của prototype).
                CheckBadge(Modifier.alpha(if (ok) 1f else 0f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = hint,
            modifier = Modifier.padding(start = 16.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 10.5.sp,
                color = colors.ink.copy(alpha = 0.45f),
            ),
        )
    }
}

/** Chevron ⌄ 6px, nét 1.4dp (border-right/bottom xoay 45° của prototype). */
@Composable
private fun ChevronDown(color: Color) {
    Canvas(Modifier.size(width = 9.dp, height = 6.dp)) {
        val s = 1.4.dp.toPx()
        val path = Path().apply {
            moveTo(s / 2, s / 2)
            lineTo(size.width / 2, size.height - s / 2)
            lineTo(size.width - s / 2, s / 2)
        }
        drawPath(path, color, style = Stroke(s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Vòng tròn xanh 18dp + dấu tích trắng 1.5dp. */
@Composable
private fun CheckBadge(modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        drawCircle(IlGreenText)
        val w = size.width
        val path = Path().apply {
            moveTo(w * 0.29f, w * 0.52f)
            lineTo(w * 0.44f, w * 0.66f)
            lineTo(w * 0.72f, w * 0.36f)
        }
        drawPath(
            path,
            Color.White,
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 260)
@Composable
private fun IlPhoneFieldPreview() {
    InnerLampTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IlPhoneField(
                value = "0912 345 678",
                onValueChange = {},
                country = PhoneCountries.default,
                onCountryClick = {},
            )
            IlPhoneField(
                value = "0912",
                onValueChange = {},
                country = PhoneCountries.default,
                onCountryClick = {},
            )
        }
    }
}

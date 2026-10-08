package com.example.innerlamp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.model.PasswordStrength
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.IlRoseText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Ô nhập mật khẩu: IlField + nút Hiện/Ẩn bên phải + thanh độ mạnh 3 đoạn bên dưới
// (prototype màn Đăng ký). Tự giữ trạng thái hiện/ẩn.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mật khẩu",
    placeholder: String = "••••••••",
    showStrength: Boolean = true,
) {
    var visible by remember { mutableStateOf(false) }
    val tooShort = value.isNotEmpty() && value.length < PasswordStrength.MIN_LENGTH
    val accepted = value.length >= PasswordStrength.MIN_LENGTH

    Column(modifier = modifier.fillMaxWidth()) {
        IlField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isError = tooShort,
            error = "Mật khẩu phải ≥ ${PasswordStrength.MIN_LENGTH} ký tự",
            isSuccess = accepted,
            trailing = if (accepted) "✓" else "",
            maskPassword = !visible,
            trailingAction = {
                IlFieldTextAction(
                    label = if (visible) "Ẩn" else "Hiện",
                    onClick = { visible = !visible },
                )
            },
        )
        if (showStrength) {
            Spacer(Modifier.height(10.dp))
            IlPasswordStrengthBar(strength = PasswordStrength.of(value))
        }
    }
}

/** Thanh 3 đoạn cao 3dp + nhãn bên phải; đoạn chưa tô dùng `ink` 12%. */
@Composable
fun IlPasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val fill = when (strength) {
        PasswordStrength.Weak -> IlRose
        PasswordStrength.Fair, PasswordStrength.Strong -> IlGreen
        PasswordStrength.None -> Color.Transparent
    }
    val labelColor = when (strength) {
        PasswordStrength.Weak -> IlRoseText
        else -> IlGreenText
    }
    val track = colors.ink.copy(alpha = 0.12f)
    val shape = RoundedCornerShape(2.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { index ->
            val target = if (index < strength.segments) fill else track
            val color by animateColorAsState(target, label = "strengthSegment$index")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(color, shape),
            )
        }
        // Nhãn có chỗ cố định để thanh không giật khi đổi "Yếu"/"Khá tốt"/"Mạnh".
        Text(
            text = strength.label,
            modifier = Modifier.width(48.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 10.5.sp,
                color = labelColor,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 420)
@Composable
private fun IlPasswordFieldPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IlPasswordField(value = "", onValueChange = {})
            IlPasswordField(value = "abc", onValueChange = {})
            IlPasswordField(value = "matkhau1", onValueChange = {})
            IlPasswordField(value = "MatKhau@2026x", onValueChange = {})
        }
    }
}

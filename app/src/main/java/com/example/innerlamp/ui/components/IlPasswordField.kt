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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.model.PasswordStrength
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlFormError
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Ô mật khẩu mới (prototype màn Đăng ký / Đặt mật khẩu mới): nhãn trong khung, "Hiện/Ẩn",
// dòng lỗi đỏ, thanh độ mạnh 3 đoạn chỉ hiện khi đã nhập và không có lỗi.
// Tự giữ trạng thái hiện/ẩn.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mật khẩu",
    placeholder: String = "8–64 ký tự",
    error: String? = null,
    showStrength: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        IlInsetField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            isError = error != null,
            masked = !visible,
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            trailingAction = { IlRevealToggle(revealed = visible, onToggle = { visible = !visible }) },
        )
        if (error != null) {
            IlFieldError(error)
        } else if (showStrength && value.isNotEmpty()) {
            IlPasswordStrengthBar(
                strength = PasswordStrength.of(value),
                modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 8.dp),
            )
        }
    }
}

/** Thanh 3 đoạn cao 3dp + nhãn bên phải; đoạn chưa tô dùng `ink` 12%. Yếu đỏ · Khá tốt vàng · Mạnh xanh. */
@Composable
fun IlPasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val fill = when (strength) {
        PasswordStrength.Weak -> IlFormError
        PasswordStrength.Fair -> IlGold
        PasswordStrength.Strong -> IlGreenText
        PasswordStrength.None -> Color.Transparent
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
        // Nhãn có chỗ tối thiểu để thanh không giật khi đổi "Yếu"/"Khá tốt"/"Mạnh".
        Text(
            text = strength.label,
            modifier = Modifier
                .padding(start = 6.dp)
                .widthIn(min = 44.dp),
            textAlign = TextAlign.End,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 10.5.sp,
                color = fill,
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
            IlPasswordField(value = "abc", onValueChange = {}, error = "Mật khẩu cần ít nhất 8 ký tự.")
            IlPasswordField(value = "matkhau1", onValueChange = {})
            IlPasswordField(value = "MatKhau@2026x", onValueChange = {})
            Spacer(Modifier.height(4.dp))
        }
    }
}

package com.example.innerlamp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.LocalIlColors

/**
 * Validity countdown on the left, "Gửi lại mã" on the right (disabled while the resend wait runs).
 * Shared by registration and password reset. /
 * Đếm ngược hiệu lực bên trái, "Gửi lại mã" bên phải (mờ khi còn thời gian chờ). Dùng chung đăng ký và quên mật khẩu.
 */
@Composable
fun IlCodeTimerRow(
    expirySeconds: Int,
    resendSeconds: Int,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
) {
    val colors = LocalIlColors.current
    val small = TextStyle(fontFamily = BeVietnamPro, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = colors.ink.copy(alpha = 0.55f))
    val canResend = resendSeconds == 0 && !busy
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (expirySeconds > 0) "Mã còn hiệu lực %d:%02d".format(expirySeconds / 60, expirySeconds % 60) else "Mã đã hết hạn",
            style = small,
        )
        Text(
            text = if (resendSeconds > 0) "Gửi lại mã sau ${resendSeconds}s" else "Gửi lại mã",
            modifier = Modifier.clickable(enabled = canResend, onClick = onResend).padding(vertical = 8.dp),
            style = small.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (canResend) colors.link else colors.ink.copy(alpha = 0.35f),
            ),
        )
    }
}

package com.example.innerlamp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlFormError
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Bộ khối form của nhóm màn xác thực (Đăng nhập, Đăng ký, Quên/Đặt lại mật khẩu) theo
// prototype `InnerLamp Prototype.dc.html`: ô nhập nhãn-bên-trong, dòng lỗi đỏ, banner
// thông báo có chấm màu, CTA pill 54dp có spinner.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Ô nhập nền card, bo 16dp. Có [label] thì nhãn 10.5sp nằm TRONG khung phía trên giá trị
 * (padding 12/16); không có nhãn thì chỉ có ô nhập (padding 14/16).
 *
 * @param labelTrailing chữ nhỏ bên phải nhãn (vd "không bắt buộc").
 * @param masked ẩn ký tự (mật khẩu).
 * @param showCheck hiện huy hiệu ✓ xanh (luôn giữ chỗ, chỉ đổi độ mờ như prototype).
 * @param trailingAction slot nút bên phải (Hiện/Ẩn, Gợi ý…).
 */
@Composable
fun IlInsetField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    labelTrailing: String = "",
    placeholder: String = "",
    isError: Boolean = false,
    masked: Boolean = false,
    showCheck: Boolean? = null,
    maxLength: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(16.dp)
    val textStyle = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = colors.ink,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card, shape)
            .border(1.dp, if (isError) IlFormError else colors.ink.copy(alpha = 0.12f), shape)
            .padding(horizontal = 16.dp, vertical = if (label != null) 12.dp else 14.dp),
    ) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = label, style = smallStyle(10.5f, colors.ink.copy(alpha = 0.45f)))
                if (labelTrailing.isNotEmpty()) {
                    Text(text = labelTrailing, style = smallStyle(10f, colors.ink.copy(alpha = 0.38f)))
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(IlGold),
                visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle.copy(color = colors.ink.copy(alpha = 0.36f)))
                    }
                    inner()
                },
            )
            if (showCheck != null) IlCheckBadge(Modifier.alpha(if (showCheck) 1f else 0f))
            trailingAction?.invoke()
        }
    }
}

/** Nút chữ "Hiện"/"Ẩn" bên phải ô mật khẩu — 12sp Medium màu link. */
@Composable
fun IlRevealToggle(
    revealed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Text(
        text = if (revealed) "Ẩn" else "Hiện",
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = colors.link,
        ),
    )
}

/** Nhãn uppercase 12sp đặt NGOÀI ô nhập (vd "EMAIL" ở màn Đăng nhập). */
@Composable
fun IlOuterLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = smallStyle(12f, colors.ink.copy(alpha = 0.45f)).copy(letterSpacing = 0.72.sp),
    )
}

/** Dòng lỗi đỏ dưới ô nhập (11.5sp, thụt 16dp); [action] là link nối tiếp (vd "Đăng nhập"). */
@Composable
fun IlFieldError(
    text: String,
    modifier: Modifier = Modifier,
    action: Pair<String, () -> Unit>? = null,
) {
    val colors = LocalIlColors.current
    val style = smallStyle(11.5f, IlFormError).copy(lineHeight = 16.7.sp)
    Row(modifier = modifier.padding(start = 16.dp, top = 6.dp)) {
        Text(text = text, style = style, modifier = Modifier.weight(1f, fill = action == null))
        if (action != null) {
            Text(
                text = " " + action.first,
                modifier = Modifier.clickable(onClick = action.second),
                style = style.copy(
                    color = colors.link,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                ),
            )
        }
    }
}

/** Dòng gợi ý xám dưới ô nhập (10.5sp, thụt 16dp). */
@Composable
fun IlFieldHint(text: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = text,
        modifier = modifier.padding(start = 16.dp, top = 6.dp),
        style = smallStyle(10.5f, colors.ink.copy(alpha = 0.45f)),
    )
}

/** Tông màu banner thông báo: vàng (lưu ý), xanh (thành công), đỏ (lỗi). */
enum class IlNoticeTone(internal val dot: Color, internal val bgAlpha: Float, internal val bdAlpha: Float) {
    Gold(IlGold, 0.10f, 0.30f),
    Green(IlGreenText, 0.10f, 0.32f),
    Error(IlFormError, 0.08f, 0.28f),
}

/**
 * Banner bo 18dp: chấm màu 8dp + (tiêu đề đậm) + nội dung 12sp; [onRetry] hiện link "Thử lại".
 */
@Composable
fun IlNotice(
    body: String,
    tone: IlNoticeTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(18.dp)
    val bodyStyle = smallStyle(12f, colors.ink.copy(alpha = 0.62f)).copy(lineHeight = 18.sp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tone.dot.copy(alpha = tone.bgAlpha), shape)
            .border(1.dp, tone.dot.copy(alpha = tone.bdAlpha), shape)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .padding(top = 5.dp)
                .size(8.dp)
                .background(tone.dot, CircleShape),
        )
        Column(Modifier.weight(1f)) {
            if (title != null) {
                Text(text = title, style = bodyStyle.copy(color = colors.ink, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(2.dp))
            }
            Text(text = body, style = bodyStyle)
        }
        if (onRetry != null) {
            Text(
                text = "Thử lại",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable(onClick = onRetry)
                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.link,
                ),
            )
        }
    }
}

/**
 * CTA chính của nhóm màn xác thực: pill 54dp nền `ink`, chữ 15sp, bóng `ink` 18%.
 * [busy] → spinner + mờ .72 (vẫn nhận chạm nhưng ViewModel bỏ qua); [dimmed] → mờ .4 (vd đang khoá).
 */
@Composable
fun IlAuthCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    dimmed: Boolean = false,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (busy) 0.72f else if (dimmed) 0.4f else 1f)
            .shadow(16.dp, shape, ambientColor = colors.ink.copy(alpha = 0.18f), spotColor = colors.ink.copy(alpha = 0.18f))
            .height(54.dp)
            .background(colors.ink, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (busy) {
            IlRingSpinner(color = colors.onInk)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colors.onInk,
            ),
        )
    }
}

/** Spinner 16dp: vòng 2dp hở đỉnh, quay 0.8s (keyframe `ilSpin` của prototype). */
@Composable
fun IlRingSpinner(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ring_spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "ring_spinner_angle",
    )
    Canvas(
        modifier
            .size(16.dp)
            .rotate(angle),
    ) {
        val stroke = 2.dp.toPx()
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(stroke),
        )
    }
}

/** Vòng tròn xanh 18dp + dấu tích trắng 1.5dp (ô hợp lệ). */
@Composable
fun IlCheckBadge(modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        drawCircle(IlGreenText)
        val w = size.width
        val path = Path().apply {
            moveTo(w * 0.29f, w * 0.52f)
            lineTo(w * 0.44f, w * 0.66f)
            lineTo(w * 0.72f, w * 0.36f)
        }
        drawPath(path, Color.White, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun smallStyle(size: Float, color: Color) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    color = color,
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun IlAuthFormPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IlInsetField(value = "", onValueChange = {}, label = "Họ và tên", placeholder = "VD: Nguyễn Bình An")
            Column {
                IlInsetField(value = "abc@", onValueChange = {}, label = "Email", isError = true, showCheck = false)
                IlFieldError("Email chưa đúng định dạng, ví dụ: ten@email.com.")
            }
            IlInsetField(
                value = "matkhau",
                onValueChange = {},
                placeholder = "Mật khẩu",
                masked = true,
                trailingAction = { IlRevealToggle(revealed = false, onToggle = {}) },
            )
            IlNotice(body = "Phiên đăng nhập đã hết, vui lòng đăng nhập lại.", tone = IlNoticeTone.Gold)
            IlNotice(
                title = "Không kết nối được",
                body = "Kiểm tra mạng rồi thử lại.",
                tone = IlNoticeTone.Error,
                onRetry = {},
            )
            IlAuthCta(label = "Đăng nhập", onClick = {})
            IlAuthCta(label = "Đăng nhập", onClick = {}, busy = true)
        }
    }
}

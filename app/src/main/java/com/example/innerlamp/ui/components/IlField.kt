package com.example.innerlamp.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlDanger
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.IlSize
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Ô nhập chung (prototype IL-Field). Trạng thái suy ra từ cờ thay vì enum `state`:
// `isError` > `isSuccess` > focus. Có nhãn uppercase, trailing, đếm ký tự, hint.
// `trailingAction` là slot nút bấm bên phải (Gợi ý, Hiện/Ẩn…) — dùng [IlFieldPillAction]
// hoặc [IlFieldTextAction]; luôn nằm sau `trailing` (✓ trạng thái).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    error: String = "",
    hint: String = "",
    trailing: String = "",
    counter: String = "",
    dark: Boolean = false,
    maskPassword: Boolean = false,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val colors = LocalIlColors.current
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(IlRadius.Input)
    val filled = value.isNotEmpty()

    // Viền: error → success → focused → mặc định.
    val bd = when {
        !enabled -> if (dark) Color(0x14F2EBDD) else colors.ink.copy(alpha = 0.07f)
        isError -> IlRose
        isSuccess -> IlGreen
        focused -> IlGold
        dark -> Color(0x24F2EBDD) // rgba(242,235,221,.14)
        else -> colors.ink.copy(alpha = 0.12f)
    }
    // Vòng sáng ngoài (box-shadow `0 0 0 3px` của prototype).
    val ring = when {
        focused -> IlGold.copy(alpha = 0.16f)
        isError -> IlRose.copy(alpha = 0.14f)
        else -> null
    }
    val bg = when {
        !enabled -> if (dark) Color(0x08F5EEE0) else colors.ink.copy(alpha = 0.035f)
        dark -> Color(0x0FF5EEE0) // rgba(245,238,224,.06)
        else -> colors.card
    }
    val labelColor = if (dark) Color(0x80F2EBDD) else colors.ink.copy(alpha = 0.48f)
    val valueColor = when {
        !enabled -> if (dark) Color(0x47F2EBDD) else colors.ink.copy(alpha = 0.30f)
        dark -> if (filled) Color(0xFFF2EBDD) else Color(0x61F2EBDD)
        else -> if (filled) colors.ink else colors.ink.copy(alpha = 0.36f)
    }
    val placeholderColor = if (dark) Color(0x61F2EBDD) else colors.ink.copy(alpha = 0.36f)
    val trailingColor = when {
        isError -> IlDanger
        isSuccess -> IlGreenText
        dark -> Color(0x66F2EBDD)
        else -> colors.ink.copy(alpha = 0.42f)
    }
    val hintText = when {
        isError -> error.ifEmpty { hint.ifEmpty { "Thông tin chưa hợp lệ" } }
        else -> hint
    }
    val hintColor = when {
        isError -> IlDanger
        isSuccess -> IlGreenText
        dark -> Color(0x66F2EBDD)
        else -> colors.ink.copy(alpha = 0.45f)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp,
                    color = labelColor,
                ),
            )
            if (counter.isNotEmpty()) {
                Text(
                    text = counter,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.5.sp,
                        color = if (dark) Color(0x66F2EBDD) else colors.ink.copy(alpha = 0.40f),
                    ),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (ring != null) {
                        Modifier.shadow(
                            elevation = 3.dp,
                            shape = shape,
                            ambientColor = ring,
                            spotColor = ring,
                        )
                    } else Modifier
                )
                .heightIn(min = IlSize.Input)
                .background(bg, shape)
                .border(1.dp, bd, shape)
                .onFocusChanged { focused = it.isFocused }
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                visualTransformation = if (maskPassword) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = valueColor,
                ),
                cursorBrush = SolidColor(IlGold),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = placeholderColor,
                            ),
                        )
                    }
                    innerTextField()
                },
            )
            if (trailing.isNotEmpty()) {
                Text(
                    text = trailing,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = trailingColor,
                    ),
                )
            }
            trailingAction?.invoke()
        }
        if (hintText.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = hintText,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    color = hintColor,
                ),
            )
        }
    }
}

/** Nút chữ bên phải ô nhập (vd "Hiện"/"Ẩn" của mật khẩu) — chữ link, 11.5sp Medium. */
@Composable
fun IlFieldTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            color = colors.link,
        ),
    )
}

/**
 * Nút pill bên phải ô nhập (vd "Gợi ý" của pháp danh): nền vàng nhạt, viền vàng,
 * biểu tượng vòng tròn xoay thêm 180° mỗi lần bấm (prototype `rollAngle`).
 */
@Composable
fun IlFieldPillAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    var turns by remember { mutableIntStateOf(0) }
    val angle by animateFloatAsState(
        targetValue = turns * 180f,
        animationSpec = tween(450, easing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f)),
        label = "pillActionAngle",
    )
    val shape = RoundedCornerShape(IlRadius.Pill)
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(IlGold.copy(alpha = 0.14f), shape)
            .border(1.dp, IlGold.copy(alpha = 0.30f), shape)
            .clickable {
                turns++
                onClick()
            }
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Vòng tròn hở đỉnh (border-top transparent) như prototype.
        Box(
            modifier = Modifier
                .size(13.dp)
                .rotate(angle)
                .drawBehind {
                    drawArc(
                        color = colors.link,
                        startAngle = -45f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Butt),
                    )
                },
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                color = colors.link,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 340)
@Composable
private fun IlFieldPreview() {
    InnerLampTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IlField(
                label = "Pháp danh hoặc tên gọi",
                value = "Diệu Tâm",
                onValueChange = {},
                counter = "0/24",
                trailingAction = { IlFieldPillAction(label = "Gợi ý", onClick = {}) },
            )
            IlField(
                label = "Số điện thoại",
                value = "0912",
                onValueChange = {},
                isError = true,
                error = "Số điện thoại cần đủ 10 số",
            )
            IlField(
                label = "Mật khẩu",
                value = "matkhau123",
                onValueChange = {},
                maskPassword = true,
                trailingAction = { IlFieldTextAction(label = "Hiện", onClick = {}) },
            )
        }
    }
}

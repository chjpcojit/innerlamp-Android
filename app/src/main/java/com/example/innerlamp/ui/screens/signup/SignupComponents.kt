package com.example.innerlamp.ui.screens.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.model.TempleOption
import com.example.innerlamp.ui.components.IlRingSpinner
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlFormError
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Khối riêng của màn Đăng ký: ô chọn "Nơi xuất gia" (dropdown có tìm kiếm) và dòng
// checkbox đồng ý Điều khoản / Chính sách (2 link mở sheet pháp lý).
// ─────────────────────────────────────────────────────────────────────────────

/** Số chùa hiển thị khi chưa gõ từ khoá (prototype `rows.slice(0, 6)`). */
private const val DEFAULT_ROWS = 6

/**
 * Ô "Nơi xuất gia" (không bắt buộc). Bấm khung → mở panel: ô tìm "Tìm tên chùa, tỉnh thành",
 * danh sách (chưa gõ: "Chưa xuất gia" + 6 chùa đầu), dòng cuối "Không thấy chùa của bạn?".
 *
 * Prototype vẽ panel nổi đè lên các ô bên dưới; ở đây panel mở rộng ngay dưới khung
 * (đẩy các ô sau xuống) để cuộn và bàn phím hoạt động ổn định trong form dài.
 */
@Composable
fun OrdinationField(
    state: OrdinationPickerState,
    onToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelect: (OrdinationChoice) -> Unit,
    onRetry: () -> Unit,
    onAddTemple: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(16.dp)
    val chevron by animateFloatAsState(if (state.expanded) 180f else 0f, label = "ordChevron")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, shape)
                .border(1.dp, if (state.expanded) IlGold.copy(alpha = 0.55f) else colors.ink.copy(alpha = 0.12f), shape)
                .clip(shape)
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("Nơi xuất gia", style = small(10.5f, colors.ink.copy(alpha = 0.45f)))
                    Text("không bắt buộc", style = small(10f, colors.ink.copy(alpha = 0.38f)))
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = state.selected?.label ?: "Chọn chùa hoặc tu viện",
                    style = small(15f, if (state.selected != null) colors.ink else colors.ink.copy(alpha = 0.38f)),
                )
            }
            Chevron(color = colors.ink.copy(alpha = 0.45f), modifier = Modifier.rotate(chevron))
        }

        AnimatedVisibility(
            visible = state.expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            OrdinationPanel(
                state = state,
                onQueryChange = onQueryChange,
                onSelect = onSelect,
                onRetry = onRetry,
                onAddTemple = onAddTemple,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun OrdinationPanel(
    state: OrdinationPickerState,
    onQueryChange: (String) -> Unit,
    onSelect: (OrdinationChoice) -> Unit,
    onRetry: () -> Unit,
    onAddTemple: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(16.dp)
    val divider = colors.ink.copy(alpha = 0.07f)
    val searching = state.query.isNotBlank()
    val rows: List<OrdinationChoice> = buildList {
        if (!searching) add(OrdinationChoice.None)
        val temples = if (searching) state.results else state.results.take(DEFAULT_ROWS)
        temples.forEach { add(OrdinationChoice.Temple(it)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Color(0x2E221E19), spotColor = Color(0x2E221E19))
            .background(colors.card, shape)
            .border(1.dp, colors.ink.copy(alpha = 0.12f), shape)
            .clip(shape),
    ) {
        // ── Ô tìm kiếm ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchGlyph(colors.ink.copy(alpha = 0.42f))
            val style = small(14f, colors.ink)
            BasicTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(IlGold),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { inner ->
                    if (state.query.isEmpty()) {
                        Text("Tìm tên chùa, tỉnh thành", style = style.copy(color = colors.ink.copy(alpha = 0.36f)))
                    }
                    inner()
                },
            )
            if (state.query.isNotEmpty()) {
                Text(
                    text = "Xóa",
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
                    style = small(12f, colors.link).copy(fontWeight = FontWeight.Medium),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(divider))

        // ── Danh sách ─────────────────────────────────────────────────
        Column(
            Modifier
                .heightIn(max = 264.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            rows.forEachIndexed { index, choice ->
                val on = choice == state.selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (on) IlGold.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { onSelect(choice) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(choice.label, style = small(14f, colors.ink))
                        Spacer(Modifier.height(2.dp))
                        Text(choice.subtitle(), style = small(10.5f, colors.ink.copy(alpha = 0.45f)))
                    }
                    RadioDot(on = on)
                }
                if (index < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
            }
            when {
                state.isLoading -> Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    IlRingSpinner(color = colors.link)
                    Text("Đang tải danh sách chùa…", style = small(13f, colors.ink.copy(alpha = 0.5f)))
                }
                state.loadFailed -> Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Không tải được danh sách chùa.",
                        modifier = Modifier.weight(1f),
                        style = small(13f, IlFormError),
                    )
                    Text(
                        text = "Thử lại",
                        modifier = Modifier
                            .clickable(onClick = onRetry)
                            .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                        style = small(13f, colors.link).copy(fontWeight = FontWeight.Medium),
                    )
                }
                searching && state.results.isEmpty() -> Text(
                    text = "Chưa có chùa nào khớp “${state.query.trim()}”. Bạn có thể thêm thông tin chùa bên dưới.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = small(13f, colors.ink.copy(alpha = 0.5f)).copy(lineHeight = 20.sp),
                )
            }
        }

        // ── Thêm chùa ─────────────────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddTemple)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Không thấy chùa của bạn?", style = small(13f, colors.link).copy(fontWeight = FontWeight.Medium))
                Spacer(Modifier.height(2.dp))
                Text("Thêm thông tin chùa", style = small(11f, colors.ink.copy(alpha = 0.5f)))
            }
            PlusGlyph(colors.link)
        }
    }
}

/** Dòng phụ dưới tên lựa chọn. */
private fun OrdinationChoice.subtitle(): String = when (this) {
    OrdinationChoice.None -> "Cư sĩ tu tại gia"
    is OrdinationChoice.Temple -> option.provinceName
}

/**
 * Checkbox 20dp bo 6 (nền `ink` khi chọn) + "Tôi đồng ý Điều khoản sử dụng và Chính sách quyền
 * riêng tư của InnerLamp." — 2 cụm gạch chân là link mở sheet pháp lý; chạm phần còn lại = tick.
 */
@Composable
fun TermsRow(
    accepted: Boolean,
    isError: Boolean,
    onToggle: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val boxShape = RoundedCornerShape(6.dp)
    val linkStyles = TextLinkStyles(SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline))
    val text = buildAnnotatedString {
        append("Tôi đồng ý ")
        withLink(LinkAnnotation.Clickable("terms", linkStyles) { onOpenTerms() }) { append("Điều khoản sử dụng") }
        append(" và ")
        withLink(LinkAnnotation.Clickable("privacy", linkStyles) { onOpenPrivacy() }) {
            append("Chính sách quyền riêng tư")
        }
        append(" của InnerLamp.")
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (accepted) colors.ink else Color.Transparent, boxShape)
                .border(
                    1.dp,
                    when {
                        isError -> IlFormError
                        accepted -> colors.ink
                        else -> colors.ink.copy(alpha = 0.28f)
                    },
                    boxShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (accepted) Tick(colors.canvas)
        }
        Spacer(Modifier.width(11.dp))
        Text(
            text = text,
            style = small(12f, colors.ink.copy(alpha = 0.6f)).copy(lineHeight = 18.sp),
        )
    }
}

// ── Glyph vẽ tay ─────────────────────────────────────────────────────────────

@Composable
private fun Chevron(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 11.dp, height = 7.dp)) {
        val s = 1.5.dp.toPx()
        val path = Path().apply {
            moveTo(s, s)
            lineTo(size.width / 2, size.height - s)
            lineTo(size.width - s, s)
        }
        drawPath(path, color, style = Stroke(s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun SearchGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val u = size.width / 24f
        val stroke = Stroke(1.8f * u, cap = StrokeCap.Round)
        drawCircle(color, radius = 7f * u, center = Offset(11f * u, 11f * u), style = stroke)
        drawLine(color, Offset(16.5f * u, 16.5f * u), Offset(20f * u, 20f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
    }
}

@Composable
private fun PlusGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val u = size.width / 24f
        drawLine(color, Offset(12f * u, 5f * u), Offset(12f * u, 19f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
        drawLine(color, Offset(5f * u, 12f * u), Offset(19f * u, 12f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
    }
}

/** Vòng radio 16dp; chọn → viền vàng + chấm vàng 7dp. */
@Composable
private fun RadioDot(on: Boolean) {
    val colors = LocalIlColors.current
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.5.dp, if (on) IlGold else colors.ink.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (on) Box(Modifier.size(7.dp).background(IlGold, CircleShape))
    }
}

@Composable
private fun Tick(color: Color) {
    Canvas(Modifier.size(10.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.4f, size.height * 0.78f)
            lineTo(size.width * 0.92f, size.height * 0.22f)
        }
        drawPath(path, color, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun small(size: Float, color: Color) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    color = color,
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SignupComponentsPreview() {
    InnerLampTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OrdinationField(
                state = OrdinationPickerState(
                    expanded = true,
                    selected = OrdinationChoice.None,
                    results = listOf(
                        TempleOption("1", "Chùa Từ Quang", "TP. Hồ Chí Minh", "79"),
                        TempleOption("2", "Thiền viện Trúc Lâm", "Lâm Đồng", "68"),
                    ),
                ),
                onToggle = {}, onQueryChange = {}, onSelect = {}, onRetry = {}, onAddTemple = {},
            )
            TermsRow(accepted = true, isError = false, onToggle = {}, onOpenTerms = {}, onOpenPrivacy = {})
            TermsRow(accepted = false, isError = true, onToggle = {}, onOpenTerms = {}, onOpenPrivacy = {})
        }
    }
}

package com.example.innerlamp.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.domain.model.PhoneCountry
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Sheet toàn màn "Chọn quốc gia" (prototype `data-screen-label="Chọn quốc gia"`):
// IL-NavBar + ô tìm kiếm (bo 14) + danh sách hàng bo 14, hàng đang chọn nền `tint` + ✓ link.
// Bên gọi tự bọc trong AnimatedVisibility (trượt từ đáy 280ms) nếu muốn hiệu ứng.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlCountryPicker(
    selectedIso: String,
    onSelect: (PhoneCountry) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    var query by remember { mutableStateOf("") }
    val countries = remember(query) { PhoneCountries.search(query) }

    BackHandler(onBack = onDismiss)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.card2, colors.sunk)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card2)
                .padding(horizontal = 26.dp)
                .padding(bottom = 10.dp),
        ) {
            IlNavBar(title = "Chọn quốc gia", showBack = true, onBack = onDismiss)
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .background(colors.card, RoundedCornerShape(14.dp))
                    .border(1.dp, colors.ink.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(13.dp)
                        .border(1.5.dp, colors.ink.copy(alpha = 0.45f), CircleShape),
                )
                val searchStyle = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = colors.ink,
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = searchStyle,
                    cursorBrush = SolidColor(IlGold),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Tìm quốc gia hoặc mã vùng",
                                style = searchStyle.copy(color = colors.ink.copy(alpha = 0.36f)),
                            )
                        }
                        inner()
                    },
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))

        if (countries.isEmpty()) {
            Text(
                text = "Không tìm thấy quốc gia phù hợp",
                modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = colors.ink.copy(alpha = 0.45f),
                ),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 26.dp, end = 26.dp, top = 16.dp, bottom = 30.dp,
                ),
            ) {
                // Khóa theo iso: US và CA cùng +1 nên không thể khóa theo dial.
                items(countries, key = { it.iso }) { c ->
                    CountryRow(c, selected = c.iso == selectedIso, onClick = { onSelect(c) })
                }
            }
        }
    }
}

@Composable
private fun CountryRow(country: PhoneCountry, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.tint else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Text(text = country.flag, style = TextStyle(fontSize = 22.sp, lineHeight = 26.sp))
        Text(
            text = country.name,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.ink,
            ),
        )
        Text(
            text = country.dial,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                color = colors.ink.copy(alpha = 0.50f),
            ),
        )
        Box(Modifier.width(16.dp), contentAlignment = Alignment.CenterEnd) {
            if (selected) {
                val link = colors.link
                Canvas(Modifier.size(width = 10.dp, height = 8.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.05f, size.height * 0.55f)
                        lineTo(size.width * 0.38f, size.height * 0.9f)
                        lineTo(size.width * 0.95f, size.height * 0.1f)
                    }
                    drawPath(
                        path,
                        link,
                        style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun IlCountryPickerPreview() {
    InnerLampTheme {
        IlCountryPicker(selectedIso = "VN", onSelect = {}, onDismiss = {})
    }
}

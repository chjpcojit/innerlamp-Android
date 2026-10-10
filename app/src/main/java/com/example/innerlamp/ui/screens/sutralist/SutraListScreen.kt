package com.example.innerlamp.ui.screens.sutralist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.SutraCatalogItem
import com.example.innerlamp.domain.model.SutraLengthFilter
import com.example.innerlamp.domain.model.SutraTagFilter
import com.example.innerlamp.domain.model.formatSutraDuration
import com.example.innerlamp.domain.usecase.GetSutraCatalogUseCase
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Kinh sách" — dựng từ prototype `Docs/innerlamp-project/` (màn `sutras`):
// header dán (nav bar + ô tìm không dấu + 2 hàng chip lọc) → dòng đếm → danh sách
// thẻ kinh → trạng thái rỗng "Chưa có bản kinh phù hợp".
// ─────────────────────────────────────────────────────────────────────────────

/** Các điểm điều hướng đi ra khỏi màn "Kinh sách". */
@Immutable
class SutraListCallbacks(
    val onBack: () -> Unit = {},
    val onSutra: (sutraId: String) -> Unit = {},
    val onRequestSutra: () -> Unit = {},
)

@Composable
fun SutraListScreen(
    modifier: Modifier = Modifier,
    callbacks: SutraListCallbacks = SutraListCallbacks(),
    viewModel: SutraListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SutraListContent(
        state = state,
        callbacks = callbacks,
        onQueryChange = viewModel::onQueryChange,
        onTagSelected = viewModel::onTagSelected,
        onLengthSelected = viewModel::onLengthSelected,
        modifier = modifier,
    )
}

/** Phần stateless của [SutraListScreen] — dùng cho preview và test. */
@Composable
internal fun SutraListContent(
    state: SutraListUiState,
    callbacks: SutraListCallbacks,
    onQueryChange: (String) -> Unit,
    onTagSelected: (SutraTagFilter) -> Unit,
    onLengthSelected: (SutraLengthFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val items = state.visible

    val listState = rememberLazyListState()
    // Chưa rời item đầu thì lấy offset của nó; đã qua item đầu thì coi như cuộn tối đa.
    val scrollOffset = {
        if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE / 2
    }

    // Header (nav + tìm kiếm + chip lọc) đè lên danh sách, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = scrollOffset,
        header = {
            Column(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp)) {
                Box(Modifier.padding(horizontal = 20.dp)) {
                    IlNavBar(title = "Kinh sách", sub = state.subtitle, showBack = true, onBack = callbacks.onBack)
                }
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 10.dp),
                )
                FilterRow(
                    labels = SutraTagFilter.entries.map { it.label },
                    selected = state.tag.ordinal,
                    onSelect = { onTagSelected(SutraTagFilter.entries[it]) },
                    shape = RoundedCornerShape(999.dp),
                    textSize = 11.5f,
                    weight = FontWeight.Medium,
                    vPad = 7.dp,
                    hPad = 12.dp,
                    modifier = Modifier.padding(top = 10.dp),
                )
                FilterRow(
                    labels = SutraLengthFilter.entries.map { it.label },
                    selected = state.length.ordinal,
                    onSelect = { onLengthSelected(SutraLengthFilter.entries[it]) },
                    shape = RoundedCornerShape(8.dp),
                    textSize = 11f,
                    weight = FontWeight.Normal,
                    vPad = 5.dp,
                    hPad = 10.dp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        modifier = modifier,
    ) { headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = headerHeight + 14.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "count") {
                Text(
                    text = state.countLabel,
                    style = sans(11.5f, colors.ink.copy(alpha = 0.45f)),
                    modifier = Modifier.padding(bottom = 0.dp),
                )
            }
            items(items, key = { it.id }) { sutra ->
                SutraCard(sutra = sutra, onClick = { callbacks.onSutra(sutra.id) })
            }
            if (items.isEmpty()) {
                item(key = "empty") { EmptyResult(onRequest = callbacks.onRequestSutra) }
            }
            item(key = "inset") { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

/** Ô tìm kiếm 44dp: biểu tượng kính lúp (vòng tròn như prototype), input, nút xóa `×`. */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(13.dp)
                .border(1.5.dp, colors.ink.copy(alpha = 0.4f), CircleShape),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = sans(13.5f, colors.ink),
            cursorBrush = SolidColor(colors.ink),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Tên kinh, chú, từ khóa — có dấu hay không dấu",
                            style = sans(13.5f, colors.ink.copy(alpha = 0.42f)),
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.ink.copy(alpha = 0.12f))
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Text("×", style = sans(13f, colors.ink.copy(alpha = 0.72f)))
            }
        }
    }
}

/** Hàng chip lọc cuộn ngang; chip chọn nền `ink`, chữ `canvas`. */
@Composable
private fun FilterRow(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    shape: RoundedCornerShape,
    textSize: Float,
    weight: FontWeight,
    vPad: androidx.compose.ui.unit.Dp,
    hPad: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            Text(
                text = label,
                style = sans(textSize, if (on) colors.canvas else colors.ink.copy(alpha = 0.65f), weight),
                modifier = Modifier
                    .clip(shape)
                    .background(if (on) colors.ink else Color.Transparent)
                    .border(1.dp, if (on) colors.ink else colors.ink.copy(alpha = 0.16f), shape)
                    .clickable { onSelect(i) }
                    .padding(horizontal = hPad, vertical = vPad),
            )
        }
    }
}

/** Thẻ một bản kinh: tên + tên gốc + thời lượng, dịch giả · tông, các nhãn trạng thái. */
@Composable
private fun SutraCard(sutra: SutraCatalogItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = sutra.name,
                    style = TextStyle(fontFamily = Lora, fontSize = 16.sp, lineHeight = 21.6.sp, color = colors.ink),
                )
                Text(
                    text = sutra.original,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        lineHeight = 17.4.sp,
                        color = colors.ink.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Text(
                text = formatSutraDuration(sutra.minutes),
                style = sans(12f, colors.ink, FontWeight.Medium),
                maxLines = 1,
            )
        }
        Text(
            text = "${sutra.translator} · ${sutra.sect.label}",
            style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.padding(top = 10.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (sutra.isDownloaded) {
                Badge("✓ Đã tải về", fg = Color(0xFF4E7A5E), bg = Color(0x296E8F7A))
            }
            if (sutra.hasAudio) {
                Badge("Có audio dẫn", fg = colors.link, bg = colors.featGlow)
            }
            sutra.tags.forEach { tag ->
                Badge(tag, fg = colors.ink.copy(alpha = 0.65f), border = colors.ink.copy(alpha = 0.12f), weight = FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun Badge(
    label: String,
    fg: Color,
    bg: Color = Color.Transparent,
    border: Color? = null,
    weight: FontWeight = FontWeight.Medium,
) {
    val shape = RoundedCornerShape(999.dp)
    Text(
        text = label,
        style = sans(10.5f, fg, weight),
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

@Composable
private fun EmptyResult(onRequest: () -> Unit) {
    val colors = LocalIlColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Chưa có bản kinh phù hợp",
            style = TextStyle(fontFamily = Lora, fontSize = 19.sp, color = colors.ink),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Thử bỏ bớt bộ lọc hoặc gõ tên khác.",
            style = sans(12.5f, colors.ink.copy(alpha = 0.5f), lineHeight = 20f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Gửi yêu cầu bản kinh",
            style = sans(12f, colors.link, FontWeight.Medium),
            modifier = Modifier.padding(top = 14.dp).clickable(onClick = onRequest),
        )
    }
}

private fun sans(
    size: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: Float = 0f,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    color = color,
    lineHeight = if (lineHeight > 0f) lineHeight.sp else TextStyle.Default.lineHeight,
)

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun SutraListPreview() {
    InnerLampTheme {
        SutraListContent(
            state = SutraListUiState(catalog = GetSutraCatalogUseCase()()),
            callbacks = SutraListCallbacks(),
            onQueryChange = {},
            onTagSelected = {},
            onLengthSelected = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun SutraListEmptyPreview() {
    InnerLampTheme {
        SutraListContent(
            state = SutraListUiState(catalog = GetSutraCatalogUseCase()(), query = "xyz"),
            callbacks = SutraListCallbacks(),
            onQueryChange = {},
            onTagSelected = {},
            onLengthSelected = {},
        )
    }
}

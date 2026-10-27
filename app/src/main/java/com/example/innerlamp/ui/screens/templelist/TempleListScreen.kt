package com.example.innerlamp.ui.screens.templelist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.TempleModule
import com.example.innerlamp.domain.usecase.GetTemplesUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.IlContentError
import com.example.innerlamp.ui.components.IlContentLoading
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Tất cả chùa" — dựng từ prototype `Docs/innerlamp-project/` (màn `findtp`, chế độ xem):
// header dán (thanh tìm kiếm + chip miền) và danh sách chùa trong một thẻ.
// Luồng "Đề xuất thêm chùa" / "Chọn chùa tu tập" của prototype chưa làm (đã làm ở tab Chùa).
// ─────────────────────────────────────────────────────────────────────────────

/** Các điểm điều hướng đi ra khỏi màn "Tất cả chùa". */
@Immutable
class TempleListCallbacks(
    val onBack: () -> Unit = {},
    val onTemple: (templeId: String) -> Unit = {},
)

@Composable
fun TempleListScreen(
    modifier: Modifier = Modifier,
    callbacks: TempleListCallbacks = TempleListCallbacks(),
    viewModel: TempleListViewModel = viewModel(factory = TempleModule.templeListViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TempleListContent(
        state = state,
        callbacks = callbacks,
        onQueryChange = viewModel::onQueryChange,
        onRegionSelected = viewModel::onRegionSelected,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

/** Phần stateless của [TempleListScreen] — dùng cho preview và test. */
@Composable
internal fun TempleListContent(
    state: TempleListUiState,
    callbacks: TempleListCallbacks,
    onQueryChange: (String) -> Unit,
    onRegionSelected: (String?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    val results = state.filtered
    val scroll = rememberScrollState()
    // Header (nav + tìm kiếm + chip vùng) đè lên danh sách, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    IlNavBar(title = "Tất cả chùa", sub = state.subtitle, showBack = true, onBack = callbacks.onBack)
                }
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IlChip("Tất cả", selected = state.region == null, onClick = { onRegionSelected(null) })
                    state.regions.forEach { region ->
                        IlChip(region, selected = state.region == region, onClick = { onRegionSelected(region) })
                    }
                }
            }
        },
        modifier = modifier,
    ) { headerHeight ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(headerHeight + 6.dp))
            when (state.status) {
                ContentStatus.Loading -> IlContentLoading()
                ContentStatus.Loaded -> Unit
                else -> IlContentError(status = state.status, onRetry = onRetry)
            }
            if (state.status == ContentStatus.Loaded) Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
            ) {
                if (results.isEmpty()) {
                    Text(
                        text = state.emptyMessage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        style = sans(13f, colors.ink.copy(alpha = 0.50f), lineHeight = 20.8f),
                    )
                }
                results.forEachIndexed { index, temple ->
                    if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.07f)))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .tappable(onClickLabel = temple.name, onClick = { callbacks.onTemple(temple.id) })
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.sunk2),
                        ) {
                            temple.imageUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(temple.name, style = sans(14f, colors.ink, FontWeight.Medium))
                            Text(
                                text = listOf(temple.place, temple.region).filter { it.isNotBlank() }.joinToString(" · "),
                                modifier = Modifier.padding(top = 3.dp),
                                style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding().height(30.dp))
        }
    }
}

/** Ô tìm kiếm 46dp bo 14dp, icon kính lúp + nút "Xóa" khi có chữ (prototype `ftQ`). */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(14.dp)
    val iconColor = colors.ink.copy(alpha = 0.42f)
    Row(
        modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.14f), shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(Modifier.size(16.dp)) {
            val w = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(iconColor, radius = size.width * 0.29f, center = Offset(size.width * 0.46f, size.height * 0.46f), style = w)
            drawLine(iconColor, Offset(size.width * 0.83f, size.height * 0.83f), Offset(size.width * 0.68f, size.height * 0.68f), w.width, StrokeCap.Round)
        }
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Tên chùa, địa danh", style = sans(14f, colors.ink.copy(alpha = 0.42f)))
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = sans(14f, colors.ink),
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Text(
                text = "Xóa",
                modifier = Modifier.tappable { onQueryChange("") }.padding(vertical = 6.dp),
                style = sans(12f, colors.link, FontWeight.Medium),
            )
        }
    }
}

private fun sans(
    size: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: Float? = null,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
    color = color,
)

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun TempleListPreview() {
    InnerLampTheme {
        TempleListContent(
            state = TempleListUiState(temples = GetTemplesUseCase()(), status = ContentStatus.Loaded),
            callbacks = TempleListCallbacks(),
            onQueryChange = {},
            onRegionSelected = {},
            onRetry = {},
        )
    }
}

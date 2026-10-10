package com.example.innerlamp.ui.screens.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.FestivalEntry
import com.example.innerlamp.domain.usecase.GetFestivalCalendarUseCase
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.screens.home.weekdayShort
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Lịch lễ Phật giáo" — dựng từ prototype `Docs/innerlamp-project/` (màn `fests`):
// header dán (NavBar + 3 chip lọc), đoạn mô tả, rồi danh sách theo tháng; mỗi ngày lễ là
// một thẻ có đếm ngược và các chùa tổ chức. Phạm vi: tháng 1 năm nay → hết năm thứ ba.
// ─────────────────────────────────────────────────────────────────────────────

/** Các điểm điều hướng đi ra khỏi màn "Lịch lễ Phật giáo". */
@Immutable
class FestivalsCallbacks(
    val onBack: () -> Unit = {},
    val onTemple: (templeId: String) -> Unit = {},
)

@Composable
fun FestivalsScreen(
    modifier: Modifier = Modifier,
    callbacks: FestivalsCallbacks = FestivalsCallbacks(),
    viewModel: FestivalsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FestivalsContent(
        state = state,
        callbacks = callbacks,
        onFilterSelected = viewModel::onFilterSelected,
        modifier = modifier,
    )
}

/** Phần stateless của [FestivalsScreen] — dùng cho preview và test. */
@Composable
internal fun FestivalsContent(
    state: FestivalsUiState,
    callbacks: FestivalsCallbacks,
    onFilterSelected: (FestivalFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val rows = state.rows
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.todayRowIndex)
    // Đổi tab lọc → danh sách đổi, về lại tháng hiện tại.
    LaunchedEffect(state.filter) { listState.scrollToItem(state.todayRowIndex) }

    Column(modifier.fillMaxSize().background(colors.canvas)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.canvas)
                .statusBarsPadding(),
        ) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(title = "Lịch lễ Phật giáo", sub = state.subtitle, showBack = true, onBack = callbacks.onBack)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FestivalFilter.entries.forEach { filter ->
                    IlChip(filter.label, selected = state.filter == filter, onClick = { onFilterSelected(filter) })
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp),
        ) {
            item(key = "intro") {
                Text(
                    text = "Ngày lễ chung của Phật giáo, tính theo âm lịch. Mỗi lễ hiện các chùa đang tổ chức — chạm để xem chùa.",
                    style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 19.2f),
                )
            }
            if (rows.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "Chưa có ngày lễ nào trong mục này.",
                        modifier = Modifier.padding(top = 22.dp),
                        style = sans(13f, colors.ink.copy(alpha = 0.50f)),
                    )
                }
            }
            rows.forEach { row ->
                when (row) {
                    is FestivalRow.Header -> item(key = "h-${row.year}-${row.month}") {
                        Text(
                            text = "Tháng ${row.month}, ${row.year}",
                            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
                            style = serif(18f, colors.ink),
                        )
                    }
                    is FestivalRow.Item -> item(key = "e-${row.entry.event.date.toEpochDay()}") {
                        FestivalCard(
                            entry = row.entry,
                            countdown = state.countdown(row.entry.event.date),
                            onTemple = callbacks.onTemple,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
            item(key = "bottom") { Spacer(Modifier.navigationBarsPadding().height(34.dp)) }
        }
    }
}

@Composable
private fun FestivalCard(
    entry: FestivalEntry,
    countdown: FestivalCountdown,
    onTemple: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(18.dp)
    val event = entry.event
    val border = if (countdown.isToday) IlGoldBright else colors.ink.copy(alpha = 0.08f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (countdown.isPast) 0.55f else 1f)
            .clip(shape)
            .background(colors.card)
            .border(1.dp, border, shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d".format(event.date.dayOfMonth),
                style = serif(20f, if (event.isFestival) colors.link else colors.ink),
            )
            Text(
                text = event.date.weekdayShort(),
                modifier = Modifier.padding(top = 2.dp),
                style = sans(9.5f, colors.ink.copy(alpha = 0.45f), FontWeight.Medium).copy(letterSpacing = 0.08.em),
                textAlign = TextAlign.Center,
            )
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = event.title,
                    modifier = Modifier.weight(1f),
                    style = sans(13.5f, colors.ink, FontWeight.Medium, lineHeight = 18.9f),
                )
                Text(
                    text = countdown.label,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = sans(10.5f, if (countdown.isSoon) colors.link else colors.ink.copy(alpha = 0.45f), FontWeight.Medium),
                )
            }
            Text(
                text = "${event.date.weekdayLong()}, ${event.date.dayMonth()} · Âm lịch ${event.lunarLabel.removeSuffix(" âm lịch")} · ${entry.hosts.size} chùa tổ chức",
                modifier = Modifier.padding(top = 3.dp),
                style = sans(11f, colors.ink.copy(alpha = 0.45f)),
            )
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                entry.hosts.take(MAX_HOSTS).forEach { temple ->
                    Text(
                        text = temple.name,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.sunk2)
                            .tappable(onClickLabel = temple.name, onClick = { onTemple(temple.id) })
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        style = sans(11f, colors.ink),
                    )
                }
                val more = entry.hosts.size - MAX_HOSTS
                if (more > 0) {
                    Text(
                        text = "+$more chùa",
                        modifier = Modifier.padding(vertical = 5.dp),
                        style = sans(11f, colors.ink.copy(alpha = 0.45f)),
                    )
                }
            }
        }
    }
}

private const val MAX_HOSTS = 3

private fun LocalDate.weekdayLong(): String =
    if (dayOfWeek.value == 7) "Chủ Nhật" else "Thứ ${dayOfWeek.value + 1}"

private fun LocalDate.dayMonth(): String = "%02d-%02d".format(dayOfMonth, monthValue)

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

private fun serif(size: Float, color: Color) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    color = color,
)

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun FestivalsPreview() {
    InnerLampTheme {
        val today = LocalDate.of(2026, 10, 8)
        FestivalsContent(
            state = FestivalsUiState(months = GetFestivalCalendarUseCase()(startYear = 2026), today = today),
            callbacks = FestivalsCallbacks(),
            onFilterSelected = {},
        )
    }
}

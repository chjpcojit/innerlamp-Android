package com.example.innerlamp.ui.screens.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.di.CalendarModule
import com.example.innerlamp.domain.calendar.CalendarCountdown
import com.example.innerlamp.domain.calendar.PublicHoliday
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlState
import com.example.innerlamp.ui.components.IlStateKind
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.screens.home.weekdayShort
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// "Lịch lễ Phật giáo" (prototype `festVals`): sticky header (NavBar + chips with counts), then the
// list by month from the API. Each row opens the detail screen.
// Lịch lễ Phật giáo: header dán (NavBar + chip kèm số lượng), rồi danh sách theo tháng từ API.
// ─────────────────────────────────────────────────────────────────────────────

/** Navigation out of the festival calendar. / Các điểm điều hướng đi ra khỏi lịch lễ. */
@Immutable
class FestivalsCallbacks(
    val onBack: () -> Unit = {},
    /** Opens the detail of `(eventCode, solarDate)`. / Mở chi tiết của `(eventCode, solarDate)`. */
    val onEventDetail: (eventCode: String, solarDate: LocalDate) -> Unit = { _, _ -> },
)

@Composable
fun FestivalsScreen(
    modifier: Modifier = Modifier,
    callbacks: FestivalsCallbacks = FestivalsCallbacks(),
    viewModel: FestivalsViewModel = viewModel(factory = CalendarModule.festivalsViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // A new day / year is picked up when the app returns to the foreground (AC20).
    // Ngày / năm mới được cập nhật khi app quay lại foreground.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    FestivalsContent(
        state = state,
        callbacks = callbacks,
        onFilterSelected = viewModel::onFilterSelected,
        onRetry = viewModel::onRetry,
        onReachedEnd = viewModel::onReachedEnd,
        onRetryNextYear = viewModel::onRetryNextYear,
        modifier = modifier,
    )
}

/** Stateless part of [FestivalsScreen] — for previews and tests. / Phần stateless. */
@Composable
internal fun FestivalsContent(
    state: FestivalsUiState,
    callbacks: FestivalsCallbacks,
    onFilterSelected: (CalendarFilter) -> Unit,
    onRetry: () -> Unit,
    onReachedEnd: () -> Unit,
    onRetryNextYear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val loaded = state.loadState as? FestivalsLoadState.Loaded
    val subtitle = state.years.takeIf { it.isNotEmpty() }?.let { "${it.first().year} – ${it.last().year}" }.orEmpty()

    Column(modifier.fillMaxSize().background(colors.canvas)) {
        Column(Modifier.fillMaxWidth().background(colors.canvas).statusBarsPadding()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(title = "Lịch lễ Phật giáo", sub = subtitle, showBack = true, onBack = callbacks.onBack)
            }
            if (loaded != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.chips.forEach { chip ->
                        IlChip(
                            label = chip.label,
                            count = chip.count.toString(),
                            selected = state.filter == chip.filter,
                            onClick = { onFilterSelected(chip.filter) },
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
        }
        when (val load = state.loadState) {
            FestivalsLoadState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = colors.link, strokeWidth = 2.dp)
                    Text("Đang tải lịch lễ…", style = sans(13f, colors.ink.copy(alpha = 0.50f)))
                }
            }
            is FestivalsLoadState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CalendarErrorState(error = load.error, onRetry = onRetry)
            }
            is FestivalsLoadState.Loaded -> FestivalList(
                state = state,
                mayBeOutOfDate = load.mayBeOutOfDate,
                onEventDetail = callbacks.onEventDetail,
                onRetry = onRetry,
                onReachedEnd = onReachedEnd,
                onRetryNextYear = onRetryNextYear,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FestivalList(
    state: FestivalsUiState,
    mayBeOutOfDate: Boolean,
    onEventDetail: (String, LocalDate) -> Unit,
    onRetry: () -> Unit,
    onReachedEnd: () -> Unit,
    onRetryNextYear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val rows = state.rows
    val listState = rememberLazyListState()
    // Scroll to the current month on open and on chip change (AC6).
    // Cuộn tới tháng hiện tại khi mở và khi đổi chip.
    LaunchedEffect(state.filter, rows.isNotEmpty()) {
        if (rows.isNotEmpty()) listState.scrollToItem(state.todayRowIndex + if (mayBeOutOfDate) 1 else 0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp),
    ) {
        if (mayBeOutOfDate) {
            item(key = "stale") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(CalendarText.STALE, modifier = Modifier.weight(1f), style = sans(12f, colors.ink.copy(alpha = 0.55f), lineHeight = 18f))
                    Text(
                        CalendarText.RETRY,
                        modifier = Modifier.tappable(onClick = onRetry),
                        style = sans(12f, colors.link, FontWeight.Medium),
                    )
                }
            }
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
                is FestivalRow.Item -> item(key = "e-${row.entry.event.code}-${row.entry.occurrence.solarDate}") {
                    FestivalCard(
                        row = row,
                        countdown = CalendarCountdown.of(state.today, row.entry.occurrence.solarDate),
                        onClick = { onEventDetail(row.entry.event.code, row.entry.occurrence.solarDate) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
        }
        item(key = "footer") {
            // Reaching the footer loads the next year; the key re-fires it after each append (AC12).
            // Chạm tới footer thì nạp năm kế tiếp; key làm nó chạy lại sau mỗi lần nối năm.
            LaunchedEffect(state.years.size, state.nextYearState) { onReachedEnd() }
            NextYearFooter(state = state.nextYearState, onRetry = onRetryNextYear)
        }
        item(key = "bottom") { Spacer(Modifier.navigationBarsPadding().height(34.dp)) }
    }
}

@Composable
private fun NextYearFooter(state: NextYearState, onRetry: () -> Unit) {
    val colors = LocalIlColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (state) {
            NextYearState.Idle -> Unit
            is NextYearState.Loading -> Text("Đang tải năm ${state.year}…", style = sans(12f, colors.ink.copy(alpha = 0.50f)))
            is NextYearState.Error -> {
                Text(
                    text = if (state.error is CalendarLoadError.RateLimited) CalendarText.message(state.error) else CalendarText.nextYearFailed(state.year),
                    textAlign = TextAlign.Center,
                    style = sans(12f, colors.ink.copy(alpha = 0.55f)),
                )
                Text(
                    text = CalendarText.RETRY,
                    modifier = Modifier.padding(top = 8.dp).tappable(onClick = onRetry).padding(horizontal = 12.dp, vertical = 6.dp),
                    style = sans(13f, colors.link, FontWeight.Medium),
                )
            }
            NextYearState.End -> Text("Đã hiển thị đến hết năm 2100.", style = sans(12f, colors.ink.copy(alpha = 0.40f)))
        }
    }
}

/** Full-area error (no device copy) with a "Thử lại" button; shared with the detail screen. */
@Composable
internal fun CalendarErrorState(error: CalendarLoadError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        IlState(
            title = CalendarText.message(error),
            kind = if (error == CalendarLoadError.Offline) IlStateKind.Offline else IlStateKind.Error,
        )
        IlButton(label = CalendarText.RETRY, variant = IlButtonVariant.Ghost, full = false, onClick = onRetry)
    }
}

@Composable
private fun FestivalCard(
    row: FestivalRow.Item,
    countdown: CalendarCountdown.Countdown,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(18.dp)
    val event = row.entry.event
    val occurrence = row.entry.occurrence
    val date = occurrence.solarDate
    val isToday = countdown.label == CalendarCountdown.Label.Today
    val isSoon = isToday || countdown.label == CalendarCountdown.Label.Tomorrow
    val border = if (isToday) IlGoldBright else colors.ink.copy(alpha = 0.08f)
    val tags = listOfNotNull(
        row.groupLabel.takeIf { it.isNotBlank() },
        "Nghỉ lễ".takeIf { PublicHoliday.isMarked(row.entry) },
    ).joinToString(" · ")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (countdown.label == CalendarCountdown.Label.Passed) 0.55f else 1f)
            .clip(shape)
            .background(colors.card)
            .border(1.dp, border, shape)
            .tappable(onClickLabel = event.name, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d".format(date.dayOfMonth),
                style = serif(20f, if (event.isMajor) colors.link else colors.ink),
            )
            Text(
                text = date.weekdayShort(),
                modifier = Modifier.padding(top = 2.dp),
                style = sans(9.5f, colors.ink.copy(alpha = 0.45f), FontWeight.Medium).copy(letterSpacing = 0.08.em),
                textAlign = TextAlign.Center,
            )
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = event.name,
                    modifier = Modifier.weight(1f),
                    style = sans(13.5f, colors.ink, FontWeight.Medium, lineHeight = 18.9f),
                )
                Text(
                    text = CalendarText.countdown(countdown.label),
                    maxLines = 1,
                    style = sans(10.5f, if (isSoon) colors.link else colors.ink.copy(alpha = 0.45f), FontWeight.Medium),
                )
            }
            Text(
                text = "${CalendarText.weekdayLong(date)}, ${CalendarText.dayMonth(date)} · ${CalendarText.lunarLabel(occurrence.lunar)}",
                modifier = Modifier.padding(top = 3.dp),
                style = sans(11f, colors.ink.copy(alpha = 0.45f)),
            )
            if (event.isMajor || tags.isNotEmpty()) {
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (event.isMajor) MajorMark()
                    if (tags.isNotEmpty()) Text(tags, style = sans(11f, colors.ink.copy(alpha = 0.55f)))
                }
            }
        }
    }
}

/** Gold "Lễ lớn" mark of a major event (BR-CAL-10). / Dấu vàng "Lễ lớn". */
@Composable
internal fun MajorMark(modifier: Modifier = Modifier) {
    Text(
        text = "Lễ lớn",
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(IlGoldBright.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = sans(10f, LocalIlColors.current.link, FontWeight.Medium),
    )
}

internal fun sans(
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

internal fun serif(size: Float, color: Color) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    color = color,
)

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun FestivalsPreview() {
    InnerLampTheme {
        FestivalsContent(
            state = CalendarPreviewData.festivalsState(),
            callbacks = FestivalsCallbacks(),
            onFilterSelected = {},
            onRetry = {},
            onReachedEnd = {},
            onRetryNextYear = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun FestivalsErrorPreview() {
    InnerLampTheme {
        FestivalsContent(
            state = FestivalsUiState(
                today = LocalDate.of(2026, 10, 17),
                loadState = FestivalsLoadState.Error(CalendarLoadError.RateLimited(30)),
            ),
            callbacks = FestivalsCallbacks(),
            onFilterSelected = {},
            onRetry = {},
            onReachedEnd = {},
            onRetryNextYear = {},
        )
    }
}

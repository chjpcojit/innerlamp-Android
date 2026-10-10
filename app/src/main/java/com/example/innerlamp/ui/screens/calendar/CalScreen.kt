package com.example.innerlamp.ui.screens.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.DatabaseModule
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.innerlamp.domain.model.LunarDayCell
import com.example.innerlamp.domain.model.LunarMonthPage
import com.example.innerlamp.domain.usecase.GetLunarMonthPagesUseCase
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlDarkInk
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import java.time.LocalDate
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Màn hình Lịch âm (prototype `cal`): header dán + carousel tháng cuộn ngang (snap
// từng tháng) + thẻ "Hôm nay" (luôn tối) + các hàng nhắc ngày trai.
// ─────────────────────────────────────────────────────────────────────────────

/** Nhãn thứ trong tuần, Chủ Nhật đầu (khớp `WD` của prototype). */
private val DOW_LABELS = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

/** Hai màu nền của thẻ "Hôm nay" — luôn tối, hardcode (không đi qua theme). */
private val TodayCardTop = Color(0xFF2A2620)
private val TodayCardBottom = Color(0xFF181510)

/**
 * Màn hình Lịch âm — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `cal`).
 *
 * Màn không giữ `NavController`: đóng bằng nút ✕ qua [onClose]; hàng "Sám hối cuối
 * tháng" dẫn sang trang chùa qua [onTemple].
 */
@Composable
fun CalScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onTemple: () -> Unit = {},
    viewModel: CalViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CalViewModel(quotes = DatabaseModule.provideSutraQuoteUseCase(this[APPLICATION_KEY]!!)) }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalContent(
        state = state,
        onClose = onClose,
        onTemple = onTemple,
        onDateSelected = viewModel::onDateSelected,
        modifier = modifier,
    )
}

/** Phần stateless của [CalScreen] — dùng cho preview và test. */
@Composable
internal fun CalContent(
    state: CalUiState,
    onClose: () -> Unit,
    onTemple: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val pagerState = rememberPagerState(pageCount = { state.months.size })
    val scope = rememberCoroutineScope()
    val page = state.months.getOrNull(pagerState.currentPage)

    val scroll = rememberScrollState()
    // Header (✕ đóng) đè lên nội dung, blur dần khi cuộn. Tiêu đề theo tháng đang xem.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            IlNavBar(
                title = page?.title.orEmpty(),
                sub = page?.let { "Âm lịch ${it.canChiYear} · ${it.lunarMonthName}" }.orEmpty(),
                showBack = false,
                showClose = true,
                onClose = onClose,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
            )
        },
        modifier = modifier,
    ) { headerHeight ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .verticalScroll(scroll),
        ) {
            Spacer(Modifier.height(headerHeight))
            MonthNavRow(
                onPrev = {
                    scope.launch {
                        pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                    }
                },
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(state.months.lastIndex))
                    }
                },
                modifier = Modifier.padding(top = 18.dp),
            )

            MonthCarousel(
                months = state.months,
                today = state.today,
                selectedDate = state.selectedDate,
                pagerState = pagerState,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(top = 14.dp),
            )

            TodayCard(state = state, modifier = Modifier.padding(top = 20.dp))

            ReminderRows(
                onTemple = onTemple,
                modifier = Modifier.padding(top = 18.dp),
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Hàng nút ‹ › + gợi ý "Trượt ngang để xem tháng khác". */
@Composable
private fun MonthNavRow(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChevronButton(direction = -1, onClick = onPrev)
        Text(
            text = "Trượt ngang để xem tháng khác",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = colors.ink.copy(alpha = 0.5f),
            ),
        )
        ChevronButton(direction = 1, onClick = onNext)
    }
}

/** Nút tròn 34px (nền card + viền ink-10) để lật tháng trước/sau. */
@Composable
private fun ChevronButton(
    direction: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.10f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(7.dp)) {
            val stroke = 1.6.dp.toPx()
            val path = Path().apply {
                if (direction < 0) {
                    // Mũi tên trái: nét trái + nét dưới.
                    moveTo(size.width * 0.66f, size.height * 0.2f)
                    lineTo(size.width * 0.34f, size.height * 0.5f)
                    lineTo(size.width * 0.66f, size.height * 0.8f)
                } else {
                    // Mũi tên phải: nét phải + nét trên.
                    moveTo(size.width * 0.34f, size.height * 0.2f)
                    lineTo(size.width * 0.66f, size.height * 0.5f)
                    lineTo(size.width * 0.34f, size.height * 0.8f)
                }
            }
            drawPath(path, colors.ink, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Carousel tháng: mỗi thẻ rộng ~86% màn, có khoảng hở hai bên (thấy 1.5 thẻ). */
@Composable
private fun MonthCarousel(
    months: List<LunarMonthPage>,
    today: LocalDate,
    selectedDate: LocalDate,
    pagerState: PagerState,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val peek = maxWidth * 0.07f
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fixed(maxWidth - peek * 2),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = peek),
        ) { index ->
            MonthCard(
                page = months[index],
                today = today,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Một thẻ tháng: hàng thứ + lưới ngày (ô vuông) với nhãn ngày âm. */
@Composable
private fun MonthCard(
    page: LunarMonthPage,
    today: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        // Hàng nhãn thứ (CN → T7).
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DOW_LABELS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        color = colors.ink.copy(alpha = 0.4f),
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Lưới ngày, mỗi hàng 7 ô vuông.
        page.days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { cell ->
                    DayCell(
                        cell = cell,
                        isSelected = cell.date != null && cell.date == selectedDate,
                        isToday = cell.date != null && cell.date == today,
                        onClick = { cell.date?.let(onDateSelected) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Chừa ô trống cuối tháng để giữ lưới vuông vắn.
                repeat(7 - week.size) {
                    Spacer(Modifier.weight(1f).aspectRatio(1f))
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Một ô ngày: số dương + nhãn âm. Nền đổi theo trạng thái (chọn/hôm nay/ngày trai). */
@Composable
private fun DayCell(
    cell: LunarDayCell,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    if (cell.date == null) {
        Spacer(modifier.aspectRatio(1f))
        return
    }
    val bg = when {
        isSelected -> colors.ink
        isToday -> colors.sunk2
        cell.isFastDay -> IlGold.copy(alpha = 0.14f)
        else -> Color.Transparent
    }
    val fg = if (isSelected) colors.canvas else colors.ink
    // Nhãn âm tô màu nhấn; khi chọn đổi sang nền `canvas` (đảo theo theme) ở 60%.
    val lunarFg = if (isSelected) colors.canvas.copy(alpha = 0.6f) else colors.link

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = cell.dayNumber.toString(),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = fg,
            ),
        )
        Spacer(Modifier.height(1.dp))
        Text(
            text = cell.lunarLabel,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 8.5.sp,
                color = lunarFg,
            ),
        )
    }
}

/** Thẻ "Hôm nay" — luôn tối (nền gradient cố định) với kicker + tiêu đề + đoạn giới thiệu. */
@Composable
private fun TodayCard(
    state: CalUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(TodayCardTop, TodayCardBottom),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = state.todayKicker,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.52.sp, // .16em
                    color = IlGoldBright.copy(alpha = 0.9f),
                ),
            )
            Text(
                text = state.todayTitle,
                modifier = Modifier.padding(top = 10.dp),
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 28.sp, // 1.4
                    color = IlDarkInk,
                ),
            )
            Text(
                text = state.todayBody,
                modifier = Modifier.padding(top = 8.dp),
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp, // 1.6
                    color = IlDarkInk.copy(alpha = 0.55f),
                ),
            )
        }
    }
}

/** Hai hàng nhắc thực hành: ngày trai (ăn chay) + sám hối cuối tháng. */
@Composable
private fun ReminderRows(
    onTemple: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IlRow(
            title = CalSampleData.FAST_REMINDER_TITLE,
            sub = CalSampleData.FAST_REMINDER_SUB,
            tone = IlTone.Green,
            glyph = IlRowGlyph.Leaf,
        )
        IlRow(
            title = CalSampleData.REPENTANCE_TITLE,
            sub = CalSampleData.REPENTANCE_SUB,
            tone = IlTone.Gold,
            glyph = IlRowGlyph.Circle,
            onClick = onTemple,
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CalPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        val today = LocalDate.of(2026, 9, 25)
        CalContent(
            state = CalUiState.create(today, GetLunarMonthPagesUseCase()(today)),
            onClose = {},
            onTemple = {},
            onDateSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CalPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        val today = LocalDate.of(2026, 9, 25)
        CalContent(
            state = CalUiState.create(today, GetLunarMonthPagesUseCase()(today)),
            onClose = {},
            onTemple = {},
            onDateSelected = {},
        )
    }
}

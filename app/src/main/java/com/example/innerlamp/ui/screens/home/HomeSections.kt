package com.example.innerlamp.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.domain.calendar.LunarDate
import com.example.innerlamp.domain.model.Article
import com.example.innerlamp.domain.model.BuddhaFigure
import com.example.innerlamp.domain.model.BuddhistEvent
import com.example.innerlamp.domain.model.EventMonth
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.Verse
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlDarkBgBottom
import com.example.innerlamp.ui.theme.IlDarkBgTop
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldSoft
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Các khối nội dung của trang chủ (prototype `home`). Kích thước/màu lấy 1–1 từ
// prototype: px → dp/sp, `--il-ink-NN` → `ink` với alpha .NN.
// ─────────────────────────────────────────────────────────────────────────────

/** Lề ngang chuẩn của màn (README "Spacing & shape"). */
private val Gutter = 20.dp

/** Chữ trên nền tối của thẻ hero (không đổi theo theme). */
private val HeroInk = Color(0xFFF7F1E4)
private val HeroInkSoft = Color(0xFFE9DFCC)

private val WEEKDAYS_FULL = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
private val WEEKDAYS_SHORT = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

/** Nhãn hàng đầu dải tuần (Thứ Hai → Chủ Nhật), khớp `weekHead` của prototype. */
private val WEEK_HEAD_LABELS = listOf("Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "CN")

/** Xanh/đỏ của chấm hoàng đạo/hắc đạo — màu riêng của khối này, không đổi theo theme. */
private val ZodiacGood = Color(0xFF5E8A6C)
private val ZodiacGoodSelected = Color(0xFF8FC19E)
private val ZodiacBad = Color(0xFFC64545)
private val ZodiacBadSelected = Color(0xFFE88A80)

/** Chủ Nhật = 0 (khớp `Date.getDay()` của prototype). */
internal fun LocalDate.weekdayFull(): String = WEEKDAYS_FULL[dayOfWeek.value % 7]
internal fun LocalDate.weekdayShort(): String = WEEKDAYS_SHORT[dayOfWeek.value % 7]

private fun sans(
    size: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    color = color,
)

private fun serif(
    size: Float,
    color: Color,
    lineHeight: TextUnit = TextUnit.Unspecified,
    italic: Boolean = false,
) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight,
    color = color,
)

/** Vùng chạm không có ripple (prototype chỉ đổi nền/scale nhẹ, không ripple). */
@Composable
internal fun Modifier.tappable(onClickLabel: String? = null, onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClickLabel = onClickLabel,
        role = Role.Button,
        onClick = onClick,
    )

/**
 * Chỗ đặt ảnh. Prototype dùng `<image-slot>`; app chưa có thư viện tải ảnh và chưa có ảnh thật,
 * nên vẽ nền gradient `sunk2 → tint`.
 *
 * TODO(il-data-builder): thay bằng ảnh thật (Coil/`painterResource`) khi có asset/API.
 */
@Composable
private fun PlaceholderImage(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Box(modifier.background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint))))
}

/** Tiêu đề khối + link hành động bên phải ("Tất cả", "Xem tất cả"...). */
@Composable
private fun SectionHeader(
    title: String,
    action: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = sans(13.5f, colors.ink, FontWeight.Medium))
        if (action != null) {
            Text(
                text = action,
                modifier = Modifier.tappable(onClick = onAction),
                style = sans(12f, colors.link),
            )
        }
    }
}

/**
 * Thẻ trạng thái rỗng của một khối carousel/danh sách (viền nét đứt, huy hiệu vàng, tiêu đề,
 * mô tả, link hành động) — prototype `homeNoData`.
 */
@Composable
private fun HomeEmptyCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val dashColor = colors.ink.copy(alpha = 0.16f)
    val shape = RoundedCornerShape(IlRadius.Card)
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .padding(top = 12.dp)
            .clip(shape)
            .drawBehind {
                val stroke = with(density) { 1.dp.toPx() }
                val dash = with(density) { 4.dp.toPx() }
                drawRoundRect(
                    color = dashColor,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(IlRadius.Card.toPx()),
                    style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))),
                )
            }
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(IlGold.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, IlGold.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(14.dp).border(1.6.dp, IlGold, CircleShape))
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = 12.dp),
            style = serif(16f, colors.ink, lineHeight = 21.6f.sp),
        )
        Text(
            text = body,
            modifier = Modifier.padding(top = 6.dp).widthIn(max = 260.dp),
            textAlign = TextAlign.Center,
            style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 19.2f.sp),
        )
        Text(
            text = actionLabel,
            modifier = Modifier.padding(top = 12.dp).tappable(onClick = onAction),
            style = sans(12f, colors.link, FontWeight.Medium),
        )
    }
}

// ── Dải tuần ─────────────────────────────────────────────────────────────────

/**
 * Dải lịch tuần cuộn ngang, snap từng tuần (5 tuần, mở ở tuần hiện tại). Chọn một ngày
 * → cập nhật thẻ âm lịch bên dưới. Hàng chữ thứ và chú thích hoàng đạo/hắc đạo đứng yên
 * (không cuộn theo trang), chỉ các ô ngày mới nằm trong pager — khớp prototype.
 */
@Composable
internal fun WeekStrip(
    pages: List<WeekPage>,
    today: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onCalendarClick: () -> Unit,
    onGioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val pagerState = rememberPagerState(initialPage = pages.size / 2) { pages.size }
    val currentLabel = pages[pagerState.currentPage.coerceIn(pages.indices)].label

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(currentLabel, style = sans(12.5f, colors.ink, FontWeight.Medium))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Sổ giỗ",
                    modifier = Modifier.tappable(onClick = onGioClick),
                    style = sans(11.5f, colors.link),
                )
                Text(
                    text = "Lịch âm",
                    modifier = Modifier.tappable(onClick = onCalendarClick),
                    style = sans(11.5f, colors.link),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            WEEK_HEAD_LABELS.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = sans(11f, if (index == 6) ZodiacBad else colors.ink.copy(alpha = 0.50f), FontWeight.Medium),
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { index ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                pages[index].days.forEach { day ->
                    WeekDayCell(
                        day = day,
                        isSelected = day.date == selected,
                        isToday = day.date == today,
                        onClick = { onSelect(day.date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WeekLegendItem(ZodiacGood, "Hoàng đạo")
            WeekLegendItem(ZodiacBad, "Hắc đạo")
            WeekLegendItem(IlGoldBright, "Rằm · Mùng Một")
        }
    }
}

@Composable
private fun WeekLegendItem(dot: Color, label: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(6.dp).background(dot, CircleShape))
        Text(label, style = sans(10.5f, colors.ink.copy(alpha = 0.50f)))
    }
}

@Composable
private fun WeekDayCell(
    day: WeekDay,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(16.dp)
    val bg by animateColorAsState(
        when {
            isSelected -> colors.ink
            isToday -> colors.card
            else -> Color.Transparent
        },
        tween(160),
        label = "weekDayBg",
    )
    val border by animateColorAsState(
        when {
            isSelected -> colors.ink
            isToday -> colors.ink.copy(alpha = 0.14f)
            else -> colors.ink.copy(alpha = 0.08f)
        },
        tween(160),
        label = "weekDayBorder",
    )
    // Chữ trên nền `ink` đảo theo theme: prototype dùng `canvas` + trắng ngà alpha.
    val onSel = colors.canvas
    val fg = if (isSelected) onSel else colors.ink
    val lunarFg = if (isSelected) Color(0x8CF7F2E8) else colors.ink.copy(alpha = 0.42f)
    val dot = when (day.mark) {
        WeekDayMark.Special -> if (isSelected) IlGoldSoft else IlGoldBright
        WeekDayMark.Good -> if (isSelected) ZodiacGoodSelected else ZodiacGood
        WeekDayMark.Bad -> if (isSelected) ZodiacBadSelected else ZodiacBad
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .tappable(onClickLabel = "Chọn ngày ${day.date.dayOfMonth}", onClick = onClick)
            .padding(top = 9.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(day.date.dayOfMonth.toString(), style = sans(16f, fg, FontWeight.Medium, lineHeight = 17.6f.sp))
        Text(day.lunarLabel, style = sans(9.5f, lunarFg))
        Box(Modifier.size(5.dp).background(dot, CircleShape))
    }
}

// ── Thẻ âm lịch + câu kinh ───────────────────────────────────────────────────

private val LunarHeroHeight = 440.dp

/** Thẻ hero tối (luôn tối ở cả hai theme): ngày âm lớn + câu kinh của ngày. */
@Composable
internal fun LunarHeroCard(
    date: LocalDate,
    lunar: LunarDate,
    verse: Verse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(IlRadius.CardLarge)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .tappable(onClickLabel = "Xem chi tiết ngày âm", onClick = onClick)
            // Thẻ luôn tối: nền tối cố định (không đi qua token) để light mode không sáng lên.
            .background(Brush.verticalGradient(listOf(IlDarkBgTop, IlDarkBgBottom))),
    ) {
        // TODO(il-data-builder): ảnh tượng Phật làm nền (`home-buddha`); hiện chỉ có lớp phủ tối.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x6B2A241C),
                        0.58f to Color(0xC71A1611),
                        1f to Color(0xE614110D),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Chiều cao cố định: câu kinh đổi theo ngày (số dòng xuống hàng khác nhau)
                // không được làm thẻ co giãn và đẩy các khối phía dưới nhảy theo.
                .height(LunarHeroHeight)
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "${date.weekdayFull()}, %02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year),
                    style = sans(13f, HeroInk, FontWeight.Medium),
                )
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    MoonPhaseIcon()
                    Text(
                        text = "ÂM LỊCH",
                        style = sans(9.5f, IlGoldBright.copy(alpha = 0.95f), FontWeight.SemiBold, letterSpacing = 1.71f.sp),
                    )
                }
                Text(
                    text = "%02d".format(lunar.day),
                    modifier = Modifier.padding(top = 10.dp),
                    style = serif(52f, HeroInk, lineHeight = 52.sp),
                )
                Text(
                    text = "Tháng %02d".format(lunar.month) + if (lunar.isLeapMonth) " (nhuận)" else "",
                    modifier = Modifier.padding(top = 6.dp),
                    style = serif(17f, HeroInkSoft, lineHeight = 23.8f.sp),
                )
                Text(
                    text = "Năm ${lunar.canChiYear}",
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(13f, HeroInk.copy(alpha = 0.62f)),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, start = 4.dp, end = 4.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                verse.lines.forEach { line ->
                    Text(line, style = serif(18.5f, HeroInk, lineHeight = 28.7f.sp), textAlign = TextAlign.Center)
                }
                Text(
                    text = verse.source,
                    modifier = Modifier.padding(top = 10.dp),
                    style = serif(12f, HeroInk.copy(alpha = 0.5f), italic = true),
                    textAlign = TextAlign.Center,
                )
            }
            // Đệm đáy 20px của prototype (vùng co giãn dưới câu kinh).
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Trăng khuyết 16dp: viền vàng mờ + lưỡi liềm ở mép phải (inset shadow -4px của prototype). */
@Composable
private fun MoonPhaseIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(16.dp)) {
        val r = size.minDimension / 2
        drawCircle(IlGoldBright.copy(alpha = 0.55f), radius = r - 0.5.dp.toPx(), style = Stroke(1.dp.toPx()))
        val disc = Path().apply { addOval(Rect(Offset.Zero, size)) }
        val shifted = Path().apply {
            addOval(Rect(Offset(-4.dp.toPx(), 0f), size))
        }
        val crescent = Path.combine(PathOperation.Difference, disc, shifted)
        drawPath(crescent, IlGoldBright.copy(alpha = 0.95f))
    }
}

// ── Sự kiện Phật giáo ────────────────────────────────────────────────────────

/** Carousel sự kiện theo tháng (snap, thấy ~1.2 thẻ) + chấm chỉ tháng. */
@Composable
internal fun EventsSection(
    months: List<EventMonth>,
    /** Tháng (1–12) hiển thị lần đầu — thường là tháng hiện tại. */
    initialMonth: Int,
    onAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOfFirst { it.month == initialMonth }.coerceAtLeast(0),
    )
    val scope = rememberCoroutineScope()
    val page by remember { derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, months.lastIndex) } }

    Column(modifier) {
        SectionHeader("Sự kiện Phật giáo", "Tất cả", onAllClick)
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            val cardWidth = (maxWidth - Gutter * 2) * 0.84f
            // LazyRow không stretch được như flex của prototype: đồng bộ chiều cao theo thẻ cao nhất đã đo.
            var tallestPx by remember { mutableIntStateOf(0) }
            val tallest = with(LocalDensity.current) { tallestPx.toDp() }
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(listState),
                contentPadding = PaddingValues(horizontal = Gutter),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(months, key = { _, m -> m.year * 100 + m.month }) { _, month ->
                    EventMonthCard(
                        month = month,
                        onClick = onAllClick,
                        modifier = Modifier
                            .width(cardWidth)
                            .heightIn(min = tallest)
                            .onSizeChanged { if (it.height > tallestPx) tallestPx = it.height },
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            months.indices.forEach { i ->
                val active = i == page
                val width by animateDpAsState(if (active) 16.dp else 5.dp, tween(200), label = "dotWidth")
                val dotColor by animateColorAsState(
                    if (active) colors.link else colors.ink.copy(alpha = 0.16f),
                    tween(200),
                    label = "dotColor",
                )
                Box(
                    Modifier
                        .height(5.dp)
                        .width(width)
                        .clip(RoundedCornerShape(3.dp))
                        .background(dotColor)
                        .tappable(onClickLabel = "Tháng ${months[i].month}") {
                            scope.launch { listState.animateScrollToItem(i) }
                        },
                )
            }
        }
    }
}

@Composable
private fun EventMonthCard(
    month: EventMonth,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    val shown = month.events.take(3)
    val extra = month.events.size - shown.size
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tháng ${month.month}, ${month.year}", style = sans(12.5f, colors.ink, FontWeight.Medium))
            Text("${month.events.size} sự kiện", style = sans(10.5f, colors.ink.copy(alpha = 0.42f)))
        }
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            shown.forEachIndexed { index, event ->
                EventRow(event = event, showDivider = index > 0, onClick = onClick)
            }
        }
        if (extra > 0) {
            Column(Modifier.padding(top = 8.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.08f)))
                Text(
                    text = "+$extra sự kiện khác",
                    modifier = Modifier.padding(top = 10.dp).tappable(onClick = onClick),
                    style = sans(11.5f, colors.link, FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun EventRow(event: BuddhistEvent, showDivider: Boolean, onClick: () -> Unit) {
    val colors = LocalIlColors.current
    Column(Modifier.fillMaxWidth().tappable(onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (showDivider) colors.ink.copy(alpha = 0.08f) else Color.Transparent),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%02d".format(event.date.dayOfMonth),
                    style = sans(
                        16f,
                        if (event.isFestival) colors.link else colors.ink,
                        FontWeight.Medium,
                        lineHeight = 16.sp,
                    ),
                )
                Text(
                    text = event.date.weekdayShort(),
                    modifier = Modifier.padding(top = 2.dp),
                    style = sans(9.5f, colors.ink.copy(alpha = 0.42f)),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(event.title, style = sans(13.5f, colors.ink, lineHeight = 18.2f.sp))
                Text(
                    text = event.lunarLabel,
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
                )
            }
        }
    }
}

// ── Ảnh chùa đẹp ─────────────────────────────────────────────────────────────

/** Lưới ảnh: 1 ảnh lớn + 2 ảnh nhỏ, ảnh cuối phủ "+N ảnh". */
@Composable
internal fun PhotosSection(
    photoMore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionHeader("Ảnh chùa đẹp", "Xem tất cả", onClick)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Gutter, end = Gutter, top = 12.dp)
                .height(232.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlaceholderImage(
                Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(IlRadius.Card))
                    .tappable(onClick = onClick),
            )
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderImage(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .tappable(onClick = onClick),
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .tappable(onClick = onClick),
                ) {
                    PlaceholderImage(Modifier.fillMaxSize())
                    Column(
                        modifier = Modifier.fillMaxSize().background(Color(0x85140F0D)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("+$photoMore", style = serif(22f, HeroInk))
                        Text(
                            text = "ảnh",
                            modifier = Modifier.padding(top = 2.dp),
                            style = sans(11f, HeroInk.copy(alpha = 0.8f)),
                        )
                    }
                }
            }
        }
    }
}

// ── Chùa Việt Nam ────────────────────────────────────────────────────────────

/** Carousel chùa (snap, thấy ~1.5 thẻ). */
@Composable
internal fun TemplesSection(
    temples: List<Temple>,
    isLoading: Boolean,
    onTempleClick: (String) -> Unit,
    onAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(modifier) {
        SectionHeader("Chùa Việt Nam", "Xem tất cả", onAllClick)
        if (isLoading) {
            TemplesSkeleton()
            return@Column
        }
        if (temples.isEmpty()) {
            HomeEmptyCard(
                title = "Chưa có chùa nào",
                body = "Danh sách chùa đang được cập nhật. Bạn có thể tìm chùa gần mình hoặc thử lại sau.",
                actionLabel = "Xem tất cả",
                onAction = onAllClick,
            )
            return@Column
        }
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            val cardWidth = (maxWidth - Gutter * 2) * 0.62f
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(listState),
                contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(temples, key = { _, t -> t.id }) { _, temple ->
                    TempleCard(temple = temple, onClick = { onTempleClick(temple.id) }, modifier = Modifier.width(cardWidth))
                }
            }
        }
    }
}

@Composable
private fun TempleCard(temple: Temple, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClickLabel = temple.name, onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            PlaceholderImage(Modifier.fillMaxSize())
            Text(
                text = temple.region,
                modifier = Modifier
                    .padding(10.dp)
                    .background(Color(0x99140F0D), RoundedCornerShape(IlRadius.Pill))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                style = sans(10f, HeroInk, FontWeight.Medium),
            )
        }
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
            Text(temple.name, style = serif(15f, colors.ink, lineHeight = 20.25f.sp))
            Text(
                text = temple.place,
                modifier = Modifier.padding(top = 3.dp),
                style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
            )
            Text(
                text = temple.note,
                modifier = Modifier.padding(top = 8.dp),
                style = sans(11f, colors.ink.copy(alpha = 0.45f)),
            )
        }
    }
}

// ── Thư viện Phật & Bồ Tát ───────────────────────────────────────────────────

/** Carousel Phật/Bồ Tát (9 vị, khớp `bdList` của prototype). */
@Composable
internal fun BuddhasSection(
    buddhas: List<BuddhaFigure>,
    isLoading: Boolean,
    onBuddhaClick: (String) -> Unit,
    onAllClick: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(modifier) {
        SectionHeader("Thư viện Phật & Bồ Tát", "Xem tất cả", onAllClick)
        if (isLoading) {
            BuddhasSkeleton()
            return@Column
        }
        if (buddhas.isEmpty()) {
            HomeEmptyCard(
                title = "Thư viện đang trống",
                body = "Chưa tải được thông tin chư Phật và Bồ Tát. Kéo xuống để làm mới.",
                actionLabel = "Thử lại",
                onAction = onReload,
            )
            return@Column
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            itemsIndexed(buddhas, key = { _, b -> b.id }) { _, buddha ->
                BuddhaCard(buddha = buddha, onClick = { onBuddhaClick(buddha.id) })
            }
        }
    }
}

@Composable
private fun BuddhaCard(buddha: BuddhaFigure, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Row)
    Column(modifier.width(124.dp).tappable(onClickLabel = buddha.shortName, onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(164.dp)
                .clip(shape)
                .background(colors.card)
                .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
        ) {
            PlaceholderImage(Modifier.fillMaxSize())
            Text(
                text = buddha.kind,
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color(0x99140F0D), RoundedCornerShape(IlRadius.Pill))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = sans(9.5f, HeroInk, FontWeight.Medium),
            )
        }
        Text(
            text = buddha.shortName,
            modifier = Modifier.padding(top = 8.dp),
            style = serif(14f, colors.ink, lineHeight = 18.2f.sp),
        )
        Text(
            text = "Vía ${buddha.viaLabel} âm lịch",
            modifier = Modifier.padding(top = 2.dp),
            style = sans(10.5f, colors.ink.copy(alpha = 0.45f)),
        )
    }
}

// ── Thời khóa hôm nay + chuỗi ngày ───────────────────────────────────────────

@Composable
internal fun PracticeStatsRow(
    state: HomeUiState,
    onJournalClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(shape)
                .background(colors.card)
                .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                .tappable(onClick = onJournalClick)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 18.dp),
        ) {
            Text("Thời khóa hôm nay", style = sans(11.5f, colors.ink.copy(alpha = 0.50f)))
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(state.todayMinutes.toString(), style = sans(26f, colors.ink, FontWeight.Medium))
                Text(
                    text = "/ ${state.goalMinutes} phút",
                    modifier = Modifier.padding(bottom = 5.dp),
                    style = sans(12f, colors.ink.copy(alpha = 0.50f)),
                )
            }
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.ink.copy(alpha = 0.09f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(state.dailyProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(IlGoldBright, IlGold))),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(shape)
                .background(colors.card)
                .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                .tappable(onClick = onStatsClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Chuỗi ngày tinh tấn", style = sans(11.5f, colors.ink.copy(alpha = 0.50f)))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(state.streakDays.toString(), style = sans(26f, colors.ink, FontWeight.Medium))
                Text(
                    text = "ngày",
                    modifier = Modifier.padding(bottom = 5.dp),
                    style = sans(12f, colors.ink.copy(alpha = 0.50f)),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                state.weekActivity.forEach { level ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(IlGold.copy(alpha = level)),
                    )
                }
            }
        }
    }
}

// ── Tràng hạt ────────────────────────────────────────────────────────────────

/**
 * Thẻ tràng hạt: cả thẻ là một lối tắt vào màn Niệm Phật (giữ nguyên số hạt), không còn lần hạt
 * hay "Bắt đầu lại" ngay trên Trang chủ — khớp prototype `goMalaKeep`.
 */
@Composable
internal fun MalaSection(
    beads: Int,
    progress: Float,
    onKeepClick: () -> Unit,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Column(modifier) {
        SectionHeader("Tràng hạt · Niệm Phật", "Toàn màn hình", onFullScreenClick)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Gutter, end = Gutter, top = 12.dp)
                .clip(shape)
                .background(colors.card)
                .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                .tappable(onClickLabel = "Mở màn Niệm Phật", onClick = onKeepClick)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BeadRing(beads = beads, progress = progress)
            Column(Modifier.weight(1f)) {
                Text("Nam mô A Di Đà Phật", style = serif(17f, colors.ink, lineHeight = 23.8f.sp))
                Text(
                    text = "Chạm để vào lần chuỗi niệm Phật.",
                    modifier = Modifier.padding(top = 6.dp),
                    style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 18.sp),
                )
            }
            Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
        }
    }
}

/** Vòng 92dp chỉ để hiển thị: cung vàng chạy theo tiến độ (conic-gradient của prototype). */
@Composable
private fun BeadRing(
    beads: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val track = colors.ink.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .size(92.dp)
            .semantics { contentDescription = "Đã niệm $beads trên 108" }
            .drawBehind {
                drawCircle(track)
                drawArc(
                    color = IlGoldBright,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = true,
                )
            }
            .padding(6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas, CircleShape),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(beads.toString(), style = sans(24f, colors.ink, FontWeight.Medium, lineHeight = 24.sp))
            Text(
                text = "/ 108",
                modifier = Modifier.padding(top = 2.dp),
                style = sans(10f, colors.ink.copy(alpha = 0.45f)),
            )
        }
    }
}

// ── Chuông mõ ────────────────────────────────────────────────────────────────

@Composable
internal fun BellCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(IlGold.copy(alpha = 0.14f), CircleShape)
                .border(1.dp, IlGold.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(30.dp)) {
                val c = center
                drawCircle(IlGold.copy(alpha = 0.12f), radius = 14.5.dp.toPx(), center = c)
                drawCircle(colors.card, radius = 9.5.dp.toPx(), center = c)
                drawCircle(IlGold, radius = 8.75.dp.toPx(), center = c, style = Stroke(1.5.dp.toPx()))
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Chuông mõ", style = serif(17f, colors.ink, lineHeight = 23f.sp))
            Text(
                text = "Gõ mõ giữ nhịp tụng kinh, thỉnh chuông khi thiền",
                modifier = Modifier.padding(top = 3.dp),
                style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 18.sp),
            )
        }
        Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
    }
}

// ── Thời khóa gợi ý ──────────────────────────────────────────────────────────

@Composable
internal fun SuggestedRoutineSection(
    onSetupClick: () -> Unit,
    onReaderClick: () -> Unit,
    onOfferClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier.padding(horizontal = Gutter)) {
        Text("Thời khóa gợi ý", style = sans(13.5f, colors.ink, FontWeight.Medium))
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IlRow(
                title = "Thiền hơi thở buổi sáng",
                sub = "12 phút · Thầy Minh Đạo",
                trailing = "Ngồi",
                tone = IlTone.Green,
                glyph = IlRowGlyph.Leaf,
                onClick = onSetupClick,
                modifier = Modifier.fillMaxWidth(),
            )
            IlRow(
                title = "Kinh Vu Lan Báo Hiếu",
                sub = "Tụng cùng 2.418 người",
                trailing = "Vào",
                tone = IlTone.Gold,
                glyph = IlRowGlyph.Book,
                onClick = onReaderClick,
                modifier = Modifier.fillMaxWidth(),
            )
            IlRow(
                title = "Cúng dường đèn — Vu Lan",
                sub = "Hồi hướng công đức cho cha mẹ",
                trailing = "Mở",
                tone = IlTone.Rose,
                glyph = IlRowGlyph.Circle,
                onClick = onOfferClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Bài viết Phật pháp hay ───────────────────────────────────────────────────

@Composable
internal fun ArticlesSection(
    articles: List<Article>,
    isLoading: Boolean,
    onArticleClick: (String) -> Unit,
    onAllClick: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionHeader("Bài viết Phật pháp hay", "Xem tất cả", onAllClick)
        if (isLoading) {
            ArticlesSkeleton()
            return@Column
        }
        if (articles.isEmpty()) {
            HomeEmptyCard(
                title = "Chưa có bài viết",
                body = "Bài viết Phật pháp mới sẽ hiện ở đây khi được đăng.",
                actionLabel = "Thử lại",
                onAction = onReload,
            )
            return@Column
        }
        Column(
            modifier = Modifier.padding(start = Gutter, end = Gutter, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            articles.forEach { article ->
                ArticleCard(article = article, onClick = { onArticleClick(article.id) })
            }
        }
    }
}

/** Thẻ bài viết (ảnh 84dp + chuyên mục, tiêu đề, tác giả) — dùng chung cho Trang chủ và "Tất cả bài viết". */
@Composable
internal fun ArticleCard(article: Article, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClickLabel = article.title, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PlaceholderImage(Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f).height(84.dp)) {
            Text(
                text = article.category.uppercase(),
                style = sans(9.5f, colors.link, FontWeight.SemiBold, letterSpacing = 1.14f.sp),
            )
            Text(
                text = article.title,
                modifier = Modifier.padding(top = 4.dp),
                style = serif(15f, colors.ink, lineHeight = 20.25f.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${article.author} · ${article.readTime}",
                style = sans(11f, colors.ink.copy(alpha = 0.45f)),
            )
        }
    }
}

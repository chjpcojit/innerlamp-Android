package com.example.innerlamp.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.EventItem
import com.example.innerlamp.domain.model.EventTab
import com.example.innerlamp.domain.model.EventTone
import com.example.innerlamp.domain.model.FeaturedEvent
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlColors
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.IlRoseText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ── Bảng màu thẻ hero "Sắp diễn ra" (luôn tối, không theo theme) ─────────────
// Nguồn: prototype màn `events` — `linear-gradient(150deg,#2A2620,#161310)` +
// chữ `#F5EEE0` / `rgba(242,235,221,*)`. Hardcode để không sáng lên ở light mode.
private val FeaturedBgStart = Color(0xFF2A2620)
private val FeaturedBgEnd = Color(0xFF161310)
private val FeaturedBgText = Color(0xFFF5EEE0)      // tiêu đề + số ngày
private val FeaturedBgSub = Color(0x99F2EBDD)       // rgba(242,235,221,.6) — chi tiết
private val FeaturedBgDim = Color(0x8CF2EBDD)       // rgba(242,235,221,.55) — tháng
private val FeaturedBgFaint = Color(0x73F2EBDD)     // rgba(242,235,221,.45) — chỗ còn lại
private val FeaturedBgKicker = Color(0xFFE8B061)    // eyebrow vàng sáng
private val FeaturedBgGlow = Color(0x38D9963C)      // rgba(217,150,60,.22) — quầng sáng
private val FeaturedBgDateTile = Color(0x14F5EEE0)  // rgba(245,238,224,.08) — ô ngày
private val FeaturedBgDateBd = Color(0x4DD9963C)    // rgba(217,150,60,.3) — viền ô ngày

/**
 * Các điểm điều hướng đi ra khỏi màn "Sự kiện & khóa học". Màn không giữ
 * `NavController`: mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class EventsCallbacks(
    val onBack: () -> Unit = {},
    val onRegister: () -> Unit = {},
)

/**
 * Màn hình "Sự kiện & khóa học" — dựng lại từ prototype
 * `Docs/design_handoff_innerlamp/` (màn `events`).
 *
 * Bố cục (trên → dưới): header dán ("Sự kiện & khóa học" + "Đạo tràng Từ Quang") →
 * 3 tab lọc (Sắp diễn ra / Khóa học / Đã ghi danh) → thẻ hero sự kiện nổi bật
 * (luôn tối, nút "Ghi danh") → tiêu đề danh sách → danh sách sự kiện.
 *
 * @author innerlamp
 */
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    callbacks: EventsCallbacks = EventsCallbacks(),
    viewModel: EventsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventsContent(
        state = state,
        callbacks = callbacks,
        onTabSelected = viewModel::onTabSelected,
        modifier = modifier,
    )
}

/** Phần stateless của [EventsScreen] — dùng cho preview và test. */
@Composable
internal fun EventsContent(
    state: EventsUiState,
    callbacks: EventsCallbacks,
    onTabSelected: (EventTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        EventsHeader(onBack = callbacks.onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(18.dp))
            // 3 tab lọc danh sách.
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventTab.entries.forEach { tab ->
                    IlChip(
                        label = tab.label(),
                        selected = tab == state.selectedTab,
                        onClick = { onTabSelected(tab) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            FeaturedEventCard(
                event = state.featured,
                onRegister = callbacks.onRegister,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = state.selectedTab.sectionTitle(),
                modifier = Modifier.padding(horizontal = 20.dp),
                style = sans(13f, colors.ink, FontWeight.Medium),
            )

            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.events.forEach { event ->
                    EventRow(event = event, onClick = callbacks.onRegister)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Header dán: `IlNavBar` "Sự kiện & khóa học" + hairline `ink-06` ở đáy. */
@Composable
private fun EventsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth().background(colors.canvas)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 8.dp),
        ) {
            IlNavBar(
                title = "Sự kiện & khóa học",
                sub = "Đạo tràng Từ Quang",
                showBack = true,
                onBack = onBack,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
    }
}

/** Thẻ hero sự kiện nổi bật: nền gradient tối + quầng sáng + ô ngày + nút ghi danh. */
@Composable
private fun FeaturedEventCard(
    event: FeaturedEvent,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            // linear-gradient(150deg,#2A2620,#161310) — thẻ hero luôn tối.
            .background(Brush.linearGradient(listOf(FeaturedBgStart, FeaturedBgEnd))),
    ) {
        // Quầng sáng ở góc phải-trên, tràn khỏi thẻ (bị clip che phần ngoài).
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-60).dp)
                .size(240.dp)
                .background(
                    Brush.radialGradient(listOf(FeaturedBgGlow, Color.Transparent)),
                    CircleShape,
                ),
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Ô ngày 58px: số ngày Lora + tháng viết tắt.
                Column(
                    modifier = Modifier
                        .width(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(FeaturedBgDateTile)
                        .border(1.dp, FeaturedBgDateBd, RoundedCornerShape(16.dp))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = event.day,
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp,
                            color = FeaturedBgText,
                        ),
                    )
                    Text(
                        text = event.month,
                        modifier = Modifier.padding(top = 2.dp),
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = FeaturedBgDim,
                        ),
                    )
                }
                // Eyebrow + tiêu đề + chi tiết.
                Column(Modifier.weight(1f)) {
                    Text(
                        text = event.eyebrow,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.6.sp,
                            color = FeaturedBgKicker,
                        ),
                    )
                    Text(
                        text = event.title,
                        modifier = Modifier.padding(top = 7.dp),
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 21.sp,
                            lineHeight = 27.sp,
                            color = FeaturedBgText,
                        ),
                    )
                    Text(
                        text = event.detail,
                        modifier = Modifier.padding(top = 8.dp),
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 20.4f.sp,
                            color = FeaturedBgSub,
                        ),
                    )
                }
            }
            // Nút ghi danh (variant light) + chỗ còn lại.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.weight(1f)) {
                    IlButton(
                        label = "Ghi danh",
                        variant = IlButtonVariant.Light,
                        onClick = onRegister,
                    )
                }
                Text(
                    text = event.remainingLabel,
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = FeaturedBgFaint,
                    ),
                )
            }
        }
    }
}

/** Hàng sự kiện trong danh sách: ô ngày 46px + tên + phụ đề + trạng thái. */
@Composable
private fun EventRow(
    event: EventItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val tint = event.tone.tint(colors)
    val fg = event.tone.fg(colors)
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .ilTap(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Ô ngày 46px.
        Column(
            modifier = Modifier
                .width(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(tint)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = event.day,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = fg,
                ),
            )
            Text(
                text = event.month,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    letterSpacing = 0.9.sp,
                    color = fg.copy(alpha = 0.7f),
                ),
            )
        }
        // Tên + phụ đề.
        Column(Modifier.weight(1f)) {
            Text(
                text = event.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = sans(14f, colors.ink, FontWeight.Medium),
            )
            Text(
                text = event.sub,
                modifier = Modifier.padding(top = 3.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
            )
        }
        // Trạng thái.
        Text(
            text = event.status,
            maxLines = 1,
            style = sans(11f, fg, FontWeight.Medium),
        )
    }
}

/** Nhãn của từng tab lọc. */
private fun EventTab.label(): String = when (this) {
    EventTab.Upcoming -> "Sắp diễn ra"
    EventTab.Courses -> "Khóa học"
    EventTab.Registered -> "Đã ghi danh"
}

/** Tiêu đề danh sách theo tab đang chọn (khớp `evListTitle` của prototype). */
private fun EventTab.sectionTitle(): String = when (this) {
    EventTab.Upcoming -> "Trong tháng này"
    EventTab.Courses -> "Khóa học nhiều buổi"
    EventTab.Registered -> "Bạn đã ghi danh"
}

/** Nền tint của ô ngày theo tông (khớp `tint` trong `events.map` của prototype). */
private fun EventTone.tint(colors: IlColors): Color = when (this) {
    EventTone.Gold -> IlGold.copy(alpha = 0.14f)
    EventTone.Green -> IlGreen.copy(alpha = 0.16f)
    EventTone.Rose -> IlRose.copy(alpha = 0.18f)
    EventTone.Ink -> colors.ink.copy(alpha = 0.07f)
}

/** Màu chữ của ô ngày / trạng thái theo tông (khớp `fg` của prototype). */
private fun EventTone.fg(colors: IlColors): Color = when (this) {
    EventTone.Gold -> colors.link
    EventTone.Green -> IlGreenText
    EventTone.Rose -> IlRoseText
    EventTone.Ink -> colors.mid
}

/** Vùng chạm không có ripple (prototype chỉ đổi nền, không ripple). */
@Composable
private fun Modifier.ilTap(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    role = Role.Button,
    onClick = onClick,
)

private fun sans(
    size: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    color = color,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun EventsPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        EventsContent(state = previewState(), callbacks = EventsCallbacks(), onTabSelected = {})
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun EventsPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        EventsContent(state = previewState(), callbacks = EventsCallbacks(), onTabSelected = {})
    }
}

private fun previewState(): EventsUiState = EventsUiState(
    selectedTab = EventTab.Upcoming,
    featured = EventsSampleData.featured,
    eventsByTab = EventsSampleData.eventsByTab,
)

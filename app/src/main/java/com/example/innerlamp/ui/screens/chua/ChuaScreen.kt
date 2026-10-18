package com.example.innerlamp.ui.screens.chua

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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.di.CalendarModule
import com.example.innerlamp.ui.screens.festivals.CalendarText
import com.example.innerlamp.domain.model.ChuaEvent
import com.example.innerlamp.domain.model.ChuaEventAction
import com.example.innerlamp.domain.model.ChuaFastDay
import com.example.innerlamp.domain.model.ChuaTemple
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlField
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import java.time.LocalDate

/**
 * Các điểm điều hướng đi ra khỏi tab Chùa. Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class ChuaCallbacks(
    val onTemple: (templeId: String) -> Unit = {},
    val onEvents: () -> Unit = {},
    val onTalk: () -> Unit = {},
    val onOffer: () -> Unit = {},
    val onFestivals: () -> Unit = {},
    val onMeditate: () -> Unit = {},
    val onSutra: () -> Unit = {},
    val onTab: (IlBottomTab) -> Unit = {},
)

/**
 * Tab Chùa — dựng từ prototype `Docs/innerlamp-project/` (màn `sangha`, nhãn "Chùa").
 *
 * Hai trạng thái theo `myTemple`:
 * - **Có chùa tu tập**: thẻ chùa → thông báo → khóa tu & lễ sắp tới → pháp thoại → công đức → số Phật tử.
 * - **Chưa có chùa**: thẻ mời chọn → ô tìm → danh sách chùa gần bạn (+ đề xuất thêm chùa)
 *   → đạo tràng trực tuyến → khóa tu sắp diễn ra.
 *
 * Cả hai kết thúc bằng "Ngày lễ & ngày chay sắp tới" và bottom nav (tab Chùa).
 *
 * @author innerlamp
 */
@Composable
fun ChuaScreen(
    modifier: Modifier = Modifier,
    callbacks: ChuaCallbacks = ChuaCallbacks(),
    viewModel: ChuaViewModel = viewModel(factory = CalendarModule.chuaViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChuaContent(
        state = state,
        actions = ChuaActions(
            onChangeTemple = viewModel::onChangeTemple,
            onCancelChange = viewModel::onCancelChange,
            onQueryChange = viewModel::onQueryChange,
            onPickTemple = viewModel::onPickTemple,
            onToggleEvent = viewModel::onToggleEventAction,
            onToggleDay = viewModel::onToggleDayReminder,
            onOpenProposal = viewModel::onOpenProposal,
            onCancelProposal = viewModel::onCancelProposal,
            onProposalChange = viewModel::onProposalChange,
            onSubmitProposal = viewModel::onSubmitProposal,
            onRetryFastDays = viewModel::onRetryFastDays,
        ),
        callbacks = callbacks,
        modifier = modifier,
    )
}

/** Các sự kiện người dùng gửi xuống [ChuaViewModel]; mặc định no-op để preview/test dựng được. */
@Immutable
internal class ChuaActions(
    val onChangeTemple: () -> Unit = {},
    val onCancelChange: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onPickTemple: (String) -> Unit = {},
    val onToggleEvent: (eventId: String, isReserve: Boolean) -> Unit = { _, _ -> },
    val onToggleDay: (LocalDate) -> Unit = {},
    val onOpenProposal: () -> Unit = {},
    val onCancelProposal: () -> Unit = {},
    val onProposalChange: ((ChuaProposal) -> ChuaProposal) -> Unit = {},
    val onSubmitProposal: () -> Unit = {},
    val onRetryFastDays: () -> Unit = {},
)

/** Phần stateless của [ChuaScreen] — dùng cho preview và test. */
@Composable
internal fun ChuaContent(
    state: ChuaUiState,
    actions: ChuaActions,
    callbacks: ChuaCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        ChuaHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            val mine = state.myTemple
            if (mine != null) {
                MyTempleSection(state = state, mine = mine, actions = actions, callbacks = callbacks)
            } else {
                FindTempleSection(state = state, actions = actions, callbacks = callbacks)
            }
            FastDaysSection(state = state, actions = actions, onFestivals = callbacks.onFestivals)
            Spacer(Modifier.height(20.dp))
        }

        IlBottomNav(active = IlBottomTab.Chua, onNav = callbacks.onTab)
    }
}

/** Header dán: tiêu đề "Chùa" (Lora 26), không đường kẻ dưới. */
@Composable
private fun ChuaHeader(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = "Chùa",
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        style = TextStyle(
            fontFamily = Lora,
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
            lineHeight = 31.sp,
            color = colors.ink,
        ),
    )
}

// ── Trạng thái CÓ chùa tu tập ────────────────────────────────────────────────

@Composable
private fun MyTempleSection(
    state: ChuaUiState,
    mine: ChuaTemple,
    actions: ChuaActions,
    callbacks: ChuaCallbacks,
) {
    val colors = LocalIlColors.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "CHÙA TU TẬP CỦA TÔI",
            style = sans(10f, colors.ink.copy(alpha = 0.42f), FontWeight.SemiBold, letterSpacing = 1.4f.sp),
        )
        Text(
            "⇄ Đổi chùa",
            modifier = Modifier.heightIn32().tappable(onClick = actions.onChangeTemple).padding(horizontal = 2.dp),
            style = sans(12f, colors.link, FontWeight.Medium),
        )
    }

    TempleCard(
        state = state,
        mine = mine,
        onClick = { if (mine.isVerified) callbacks.onTemple(mine.id) },
        modifier = Modifier.padding(top = 6.dp),
    )

    if (!state.isVerified) {
        // Chùa tự đề xuất: chưa có lịch lễ/thông báo cho tới khi được xác minh.
        Text(
            text = "Lịch lễ, thông báo, pháp thoại và công đức sẽ hiện sau khi chùa được xác minh. " +
                "InnerLamp thường liên hệ ban hộ tự trong vòng 3–5 ngày.",
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .border(1.dp, IlGold.copy(alpha = 0.5f), RoundedCornerShape(IlRadius.Card))
                .padding(16.dp),
            style = sans(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.8f.sp),
        )
    } else {
        SectionTitle("Thông báo từ chùa", topPadding = 24.dp)
        CardList(modifier = Modifier.padding(top = 12.dp)) {
            state.news.forEachIndexed { i, n ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(n.time, style = sans(10.5f, colors.ink.copy(alpha = 0.42f)))
                    Text(
                        n.text,
                        modifier = Modifier.padding(top = 4.dp),
                        style = sans(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.8f.sp),
                    )
                }
                if (i < state.news.lastIndex) Divider()
            }
        }

        SectionTitle("Khóa tu & lễ sắp tới", topPadding = 24.dp, trailing = "Tất cả", onTrailing = callbacks.onEvents)
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.events.forEach { e ->
                val on = if (e.action == ChuaEventAction.Reserve) e.id in state.reservedEvents else e.id in state.remindedEvents
                EventCard(
                    event = e,
                    on = on,
                    onOpen = callbacks.onEvents,
                    onAction = { actions.onToggleEvent(e.id, e.action == ChuaEventAction.Reserve) },
                )
            }
        }

        SectionTitle("Pháp thoại tại chùa", topPadding = 24.dp)
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IlRow("Hiếu là đạo", sub = "${state.teacher} · 46 phút", tone = IlTone.Green, glyph = IlRowGlyph.Dot, onClick = callbacks.onTalk)
            IlRow("Giảng giải Lăng Nghiêm · buổi 1", sub = "${state.teacher} · 52 phút", tone = IlTone.Green, glyph = IlRowGlyph.Dot, onClick = callbacks.onTalk)
        }

        Column(Modifier.padding(top = 24.dp)) {
            IlRow(
                "Công đức · cúng dường",
                sub = "Gửi thẳng đến ban hộ tự ${mine.name}",
                tone = IlTone.Rose,
                glyph = IlRowGlyph.Circle,
                onClick = callbacks.onOffer,
            )
        }
    }

    PeersCard(label = state.peersLabel, modifier = Modifier.padding(top = 12.dp))
}

/** Thẻ chùa tu tập: ảnh bìa (giữ chỗ) + tên/địa chỉ + trạng thái mở cửa + trụ trì. */
@Composable
private fun TempleCard(
    state: ChuaUiState,
    mine: ChuaTemple,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick),
    ) {
        // TODO(il-data-builder): ảnh bìa chùa từ backend; hiện là khung giữ chỗ như prototype.
        Box(
            Modifier.fillMaxWidth().height(140.dp).background(colors.sunk2),
            contentAlignment = Alignment.Center,
        ) {
            Text("Ảnh chùa tu tập", style = sans(11f, colors.ink.copy(alpha = 0.35f)))
        }
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        mine.name,
                        style = TextStyle(
                            fontFamily = Lora,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            color = colors.ink,
                        ),
                    )
                    Text(
                        mine.place,
                        modifier = Modifier.padding(top = 4.dp),
                        style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 18.sp),
                    )
                }
                StatusPill(label = state.statusLabel, dot = statusDot(state))
            }
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.ink.copy(alpha = 0.07f)),
            )
            Text(
                "Trụ trì: ${mine.abbot ?: "Đang cập nhật"}",
                modifier = Modifier.padding(top = 12.dp),
                style = sans(12f, colors.ink.copy(alpha = 0.50f)),
            )
        }
    }
}

@Composable
private fun statusDot(state: ChuaUiState): Color = when {
    !state.isVerified -> IlGold
    state.isOpenNow -> Color(0xFF5E8A6C)
    else -> LocalIlColors.current.ink.copy(alpha = 0.35f)
}

@Composable
private fun StatusPill(label: String, dot: Color, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .border(1.dp, colors.ink.copy(alpha = 0.08f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).background(dot, CircleShape))
        Text(label, style = sans(11f, colors.ink.copy(alpha = 0.72f), FontWeight.Medium))
    }
}

/** Thẻ sự kiện: ô ngày + tiêu đề/phụ đề + nút pill (Nhắc tôi / Giữ chỗ). */
@Composable
private fun EventCard(
    event: ChuaEvent,
    on: Boolean,
    onOpen: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    val gold = event.action == ChuaEventAction.Remind
    val label = when (event.action) {
        ChuaEventAction.Remind -> if (on) "Đã nhắc" else "Nhắc tôi"
        ChuaEventAction.Reserve -> if (on) "Đã giữ chỗ" else "Giữ chỗ"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DateBadge(day = event.day, month = event.month, dayColor = colors.ink)
        Column(Modifier.weight(1f)) {
            Text(event.title, style = sans(14f, colors.ink, FontWeight.Medium))
            Text(event.sub, modifier = Modifier.padding(top = 3.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.50f)))
        }
        PillButton(label = label, on = on, gold = gold, onClick = onAction)
    }
}

@Composable
private fun DateBadge(day: String, month: String, dayColor: Color, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Column(modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, style = sans(18f, dayColor, FontWeight.Medium, lineHeight = 18.sp))
        Text(
            month,
            modifier = Modifier.padding(top = 4.dp),
            style = sans(9.5f, colors.ink.copy(alpha = 0.42f), FontWeight.SemiBold, letterSpacing = 0.95f.sp),
        )
    }
}

/** Nút pill 32dp: `on` + `gold` → nền vàng nhạt; `on` → nền ink; tắt → viền mảnh. */
@Composable
private fun PillButton(
    label: String,
    on: Boolean,
    gold: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val bg: Color
    val fg: Color
    val bd: Color
    when {
        on && gold -> { bg = IlGold.copy(alpha = 0.14f); fg = colors.link; bd = IlGold.copy(alpha = 0.45f) }
        on -> { bg = colors.ink; fg = colors.canvas; bd = colors.ink }
        else -> { bg = Color.Transparent; fg = colors.ink; bd = colors.ink.copy(alpha = 0.14f) }
    }
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, bd, CircleShape)
            .tappable(onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = sans(11.5f, fg, FontWeight.Medium), maxLines = 1)
    }
}

@Composable
private fun PeersCard(label: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Ba avatar ẩn danh xếp chồng — chỉ hiện số Phật tử, không lộ danh tính.
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(colors.tint, colors.tint2, colors.tint3).forEach { tint ->
                Box(Modifier.size(26.dp).background(tint, CircleShape).border(1.5.dp, colors.card, CircleShape))
            }
        }
        Text(label, modifier = Modifier.weight(1f), style = sans(12.5f, colors.ink.copy(alpha = 0.72f), lineHeight = 18.75f.sp))
    }
}

// ── Trạng thái CHƯA có chùa ──────────────────────────────────────────────────

@Composable
private fun FindTempleSection(
    state: ChuaUiState,
    actions: ChuaActions,
    callbacks: ChuaCallbacks,
) {
    val colors = LocalIlColors.current
    val changing = state.previousTemple != null
    val promptShape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(promptShape)
            .background(colors.card)
            .border(1.dp, IlGold.copy(alpha = 0.35f), promptShape)
            .padding(20.dp),
    ) {
        Text(
            if (changing) "Đổi chùa tu tập" else "Bạn chưa chọn chùa tu tập",
            style = TextStyle(fontFamily = Lora, fontSize = 19.sp, lineHeight = 26.6f.sp, color = colors.ink),
        )
        Text(
            "Chọn một ngôi chùa để nhận thông báo, lịch lễ, khóa tu và pháp thoại của chùa ngay tại đây.",
            modifier = Modifier.padding(top = 6.dp),
            style = sans(13f, colors.ink.copy(alpha = 0.50f), lineHeight = 20.8f.sp),
        )
        if (changing) {
            Text(
                "Giữ ${state.previousTemple?.name}",
                modifier = Modifier.padding(top = 10.dp).tappable(onClick = actions.onCancelChange),
                style = sans(12f, colors.link, FontWeight.Medium),
            )
        }
    }

    SearchBox(
        query = state.query,
        onQueryChange = actions.onQueryChange,
        modifier = Modifier.padding(top = 20.dp),
    )

    val proposal = state.proposal
    if (proposal != null) {
        ProposalForm(
            proposal = proposal,
            onChange = actions.onProposalChange,
            onCancel = actions.onCancelProposal,
            onSubmit = actions.onSubmitProposal,
            modifier = Modifier.padding(top = 16.dp),
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(state.listTitle, style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(state.listSub, style = sans(11.5f, colors.ink.copy(alpha = 0.42f)))
        }
        if (!state.isSearching) {
            // TODO(il-data-builder): bản đồ chùa gần bạn (cần quyền vị trí/GPS); hiện là khung giữ chỗ.
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(IlRadius.Card))
                    .background(colors.sunk2)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(IlRadius.Card)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Bản đồ chùa gần bạn", style = sans(11f, colors.ink.copy(alpha = 0.35f)))
            }
        }
        CardList(modifier = Modifier.padding(top = 10.dp)) {
            val list = state.nearby
            if (state.isSearching && list.isEmpty()) {
                Text(
                    state.emptyMessage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    style = sans(13f, colors.ink.copy(alpha = 0.50f), lineHeight = 20.8f.sp),
                )
            }
            list.forEachIndexed { i, t ->
                if (i > 0) Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tappable { callbacks.onTemple(t.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(t.name, style = sans(14f, colors.ink, FontWeight.Medium))
                        Text(
                            t.subtitle(),
                            modifier = Modifier.padding(top = 3.dp),
                            style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
                        )
                    }
                    PillButton(label = "Chọn", on = false, gold = false, onClick = { actions.onPickTemple(t.id) })
                }
            }
            if (list.isNotEmpty()) Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tappable(onClick = actions.onOpenProposal)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Không thấy chùa của bạn?", style = sans(13.5f, colors.link, FontWeight.Medium))
                    Text(
                        "Đề xuất thêm chùa và chọn ngay",
                        modifier = Modifier.padding(top = 3.dp),
                        style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
                    )
                }
                Text("+", style = sans(20f, colors.link))
            }
        }
    }

    SectionTitle("Đạo tràng trực tuyến", topPadding = 24.dp)
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.online.forEach { o ->
            val shape = RoundedCornerShape(IlRadius.Card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                    .tappable(onClick = if (o.isDaily) callbacks.onMeditate else callbacks.onSutra)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).background(if (o.isDaily) IlGoldBright else colors.ink.copy(alpha = 0.35f), CircleShape))
                    Text(o.tag, style = sans(9.5f, colors.ink.copy(alpha = 0.42f), FontWeight.SemiBold, letterSpacing = 1.33f.sp))
                }
                Text(o.title, modifier = Modifier.padding(top = 8.dp), style = sans(14f, colors.ink, FontWeight.Medium))
                Text(o.sub, modifier = Modifier.padding(top = 3.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.50f)))
            }
        }
    }

    SectionTitle("Khóa tu sắp diễn ra", topPadding = 24.dp)
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.retreats.forEach { r ->
            val shape = RoundedCornerShape(IlRadius.Card)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                    .tappable { callbacks.onTemple(r.templeId) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DateBadge(day = r.day, month = r.month, dayColor = colors.ink)
                Column(Modifier.weight(1f)) {
                    Text(r.title, style = sans(14f, colors.ink, FontWeight.Medium))
                    Text(r.sub, modifier = Modifier.padding(top = 3.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.50f)))
                }
                Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
            }
        }
    }
}

private fun ChuaTemple.subtitle(): String =
    place + (distanceKm?.let { " · " + formatKm(it) } ?: "")

/** `4,8 km` (< 10 km, dấu phẩy) hoặc `1.048 km` (làm tròn, dấu chấm nghìn). */
private fun formatKm(km: Double): String =
    if (km < 10) String.format(java.util.Locale.forLanguageTag("vi-VN"), "%.1f km", km)
    else String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,d km", Math.round(km))

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Input)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.14f), shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("⌕", style = sans(16f, colors.ink.copy(alpha = 0.42f)))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    "Tìm theo tên chùa, tên khác, tỉnh thành",
                    style = sans(14f, colors.ink.copy(alpha = 0.35f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = sans(14f, colors.ink),
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Text(
                "Xóa",
                modifier = Modifier.tappable { onQueryChange("") }.padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
                style = sans(12f, colors.link, FontWeight.Medium),
            )
        }
    }
}

/** Form "Đề xuất thêm chùa": 4 ô, báo lỗi đỏ ở ô tên/tỉnh, Hủy + "Gửi và chọn chùa này". */
@Composable
private fun ProposalForm(
    proposal: ChuaProposal,
    onChange: ((ChuaProposal) -> ChuaProposal) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, IlGold.copy(alpha = 0.35f), shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text(
                "Đề xuất thêm chùa",
                style = TextStyle(fontFamily = Lora, fontSize = 18.sp, lineHeight = 25.2f.sp, color = colors.ink),
            )
            Text(
                "Bạn chọn được chùa này ngay. InnerLamp sẽ liên hệ ban hộ tự để xác minh và bổ sung lịch lễ, khóa tu.",
                modifier = Modifier.padding(top = 4.dp),
                style = sans(12f, colors.ink.copy(alpha = 0.50f), lineHeight = 19.2f.sp),
            )
        }
        IlField(
            label = "Tên chùa",
            value = proposal.name,
            onValueChange = { v -> onChange { it.copy(name = v) } },
            placeholder = "VD: Chùa Pháp Hoa",
            isError = proposal.errorField == ChuaProposalField.Name,
        )
        IlField(
            label = "Địa chỉ",
            value = proposal.address,
            onValueChange = { v -> onChange { it.copy(address = v) } },
            placeholder = "Số nhà, đường, phường/xã",
        )
        IlField(
            label = "Tỉnh / thành phố",
            value = proposal.province,
            onValueChange = { v -> onChange { it.copy(province = v) } },
            placeholder = "VD: TP. Hồ Chí Minh",
            isError = proposal.errorField == ChuaProposalField.Province,
        )
        IlField(
            label = "Số điện thoại chùa (không bắt buộc)",
            value = proposal.contact,
            onValueChange = { v -> onChange { it.copy(contact = v) } },
            placeholder = "Giúp xác minh nhanh hơn",
        )
        if (proposal.error.isNotEmpty()) {
            Text(proposal.error, style = sans(12f, Color(0xFFB4533A)))
        }
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IlButton("Hủy", variant = IlButtonVariant.Ghost, onClick = onCancel, modifier = Modifier.weight(1f))
            IlButton("Gửi và chọn chùa này", onClick = onSubmit, modifier = Modifier.weight(2f))
        }
    }
}

// ── Khối chung ───────────────────────────────────────────────────────────────

/** "Ngày lễ & ngày chay sắp tới": 4 ngày gần nhất, mỗi ngày có nút "Nhắc tôi". */
@Composable
private fun FastDaysSection(
    state: ChuaUiState,
    actions: ChuaActions,
    onFestivals: () -> Unit,
) {
    val colors = LocalIlColors.current
    SectionTitle("Ngày lễ & ngày chay sắp tới", topPadding = 24.dp, trailing = "Lịch lễ", onTrailing = onFestivals)
    CardList(modifier = Modifier.padding(top = 12.dp)) {
        if (state.fastDays.isEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val error = state.fastDaysError
                Text(
                    text = when {
                        state.fastDaysLoading -> "Đang tải ngày lễ…"
                        error != null -> CalendarText.message(error)
                        else -> "Chưa có ngày lễ sắp tới."
                    },
                    modifier = Modifier.weight(1f),
                    style = sans(12.5f, colors.ink.copy(alpha = 0.55f), lineHeight = 18.sp),
                )
                if (error != null && !state.fastDaysLoading) {
                    Text(
                        CalendarText.RETRY,
                        modifier = Modifier.tappable(onClick = actions.onRetryFastDays),
                        style = sans(12.5f, colors.link, FontWeight.Medium),
                    )
                }
            }
        }
        state.fastDays.forEachIndexed { i, d ->
            if (i > 0) Divider()
            val on = d.date in state.remindedDays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tappable(onClick = onFestivals)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DateBadge(
                    day = d.date.dayOfMonth.toString().padStart(2, '0'),
                    month = "TH${d.date.monthValue}",
                    dayColor = if (d.isFestival) colors.link else colors.ink,
                )
                Column(Modifier.weight(1f)) {
                    Text(d.title, style = sans(13.5f, colors.ink, FontWeight.Medium, lineHeight = 18.9f.sp))
                    Text(
                        d.subLabel(state.myTemple?.name),
                        modifier = Modifier.padding(top = 3.dp),
                        style = sans(11.5f, colors.ink.copy(alpha = 0.50f), lineHeight = 17.25f.sp),
                    )
                    Text(d.suggestion, modifier = Modifier.padding(top = 6.dp), style = sans(12f, colors.link, lineHeight = 18.sp))
                }
                PillButton(
                    label = if (on) "Đã nhắc" else "Nhắc tôi",
                    on = on,
                    gold = true,
                    onClick = { actions.onToggleDay(d.date) },
                )
            }
        }
    }
}

private fun ChuaFastDay.subLabel(templeName: String?): String {
    val away = when (daysAway) {
        0 -> "hôm nay"
        1 -> "ngày mai"
        else -> "còn $daysAway ngày"
    }
    return "$lunarDay tháng $lunarMonthName âm · $away" + (templeName?.let { " · $it có lễ" } ?: "")
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp,
    trailing: String? = null,
    onTrailing: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth().padding(top = topPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = sans(13.5f, colors.ink, FontWeight.Medium))
        if (trailing != null) {
            Text(trailing, modifier = Modifier.tappable(onClick = onTrailing), style = sans(12f, colors.link))
        }
    }
}

/** Khung thẻ bo 20dp, viền `ink-08`, các hàng bên trong cách nhau bởi [Divider]. */
@Composable
private fun CardList(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
    ) { content() }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(LocalIlColors.current.ink.copy(alpha = 0.07f)))
}

/** Vùng chạm cao tối thiểu 32dp cho link chữ. */
private fun Modifier.heightIn32(): Modifier = defaultMinSize(minHeight = 32.dp)

/** Vùng chạm không có ripple (prototype chỉ đổi nền, không ripple). */
@Composable
private fun Modifier.tappable(onClick: () -> Unit): Modifier = clickable(
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
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    color = color,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun ChuaPreviewHasTemple() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        ChuaContent(state = previewState(hasTemple = true), actions = ChuaActions(), callbacks = ChuaCallbacks())
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun ChuaPreviewNoTempleDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        ChuaContent(state = previewState(hasTemple = false), actions = ChuaActions(), callbacks = ChuaCallbacks())
    }
}

private fun previewState(hasTemple: Boolean): ChuaUiState {
    val dir = ChuaSampleData.directory
    val mine = dir.first { it.id == ChuaSampleData.DEFAULT_TEMPLE_ID }
    return ChuaUiState(
        directory = dir,
        myTemple = if (hasTemple) mine else null,
        minuteOfDay = 10 * 60,
        news = ChuaSampleData.news,
        events = ChuaSampleData.events,
        online = ChuaSampleData.online,
        retreats = ChuaSampleData.retreats,
        fastDays = listOf(
            ChuaFastDay(LocalDate.of(2026, 10, 25), "Tết Trung thu", 15, "Tám", 8, isFestival = false, suggestion = "Gợi ý: ăn chay, tụng Kinh Phổ Môn"),
            ChuaFastDay(LocalDate.of(2026, 11, 3), "Lễ Vu Lan", 15, "Bảy", 17, isFestival = true, suggestion = "Gợi ý: tụng kinh và hồi hướng công đức"),
        ),
        peerCount = ChuaSampleData.peerCount(mine),
    )
}

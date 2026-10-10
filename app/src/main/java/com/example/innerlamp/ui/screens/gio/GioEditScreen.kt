package com.example.innerlamp.ui.screens.gio

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.ui.components.IlAvatar
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlField
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate
import java.time.LocalTime

private val Gutter = 20.dp
private val LeadOptions = listOf(0 to "Đúng ngày", 1 to "Trước 1 ngày", 3 to "Trước 3 ngày", 7 to "Trước 7 ngày")
private val TimeOptions = listOf(LocalTime.of(6, 0), LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(19, 0))

/** Số năm gần đây có thể chọn làm năm mất (hiện tại lùi 96 năm). */
private const val YearSpan = 97

/** Các thao tác trên form Thêm/sửa; tách khỏi [GioViewModel] để preview/test dựng được. */
internal class GioEditActions(
    val onBack: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onRelation: (MemorialRelation) -> Unit = {},
    val onCalendar: (MemorialCalendarType) -> Unit = {},
    val onDay: (Int) -> Unit = {},
    val onMonth: (Int) -> Unit = {},
    val onYear: (Int?) -> Unit = {},
    val onNote: (String) -> Unit = {},
    val onRemindToggle: () -> Unit = {},
    val onLead: (Int) -> Unit = {},
    val onTime: (LocalTime) -> Unit = {},
    val onSyncToggle: () -> Unit = {},
)

/**
 * Màn "Thêm/sửa ngày giỗ" — dựng lại từ prototype `Docs/innerlamp-project/` (màn `gioEdit`).
 *
 * Mọi lối thoát (lưu, xóa, back) đều đi qua [GioViewModel] và làm `draft` về `null`; màn
 * thấy `draft == null` thì gọi [onClose] đúng một lần — nên không bao giờ pop hai lần.
 */
@Composable
fun GioEditScreen(
    viewModel: GioViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Giữ bản nháp cuối để khung hình còn lại khi chuyển cảnh (draft đã null) không bị trống.
    var lastDraft by remember { mutableStateOf(state.draft) }
    state.draft?.let { lastDraft = it }

    LaunchedEffect(state.draft == null) {
        if (state.draft == null) onClose()
    }
    BackHandler { viewModel.discardDraft() }

    val draft = lastDraft ?: return
    GioEditContent(
        draft = draft,
        currentYear = remember { LocalDate.now().year },
        actions = GioEditActions(
            onBack = viewModel::discardDraft,
            onSave = { viewModel.save() },
            onDelete = viewModel::delete,
            onName = viewModel::onNameChange,
            onRelation = viewModel::onRelationChange,
            onCalendar = viewModel::onCalendarChange,
            onDay = viewModel::onDayChange,
            onMonth = viewModel::onMonthChange,
            onYear = viewModel::onDeathYearChange,
            onNote = viewModel::onNoteChange,
            onRemindToggle = viewModel::onRemindToggle,
            onLead = viewModel::onLeadDaysChange,
            onTime = viewModel::onRemindTimeChange,
            onSyncToggle = viewModel::onSyncToggle,
        ),
        modifier = modifier,
    )
}

/** Phần stateless của [GioEditScreen] — dùng cho preview và test. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GioEditContent(
    draft: GioDraft,
    currentYear: Int,
    actions: GioEditActions,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val entry = draft.entry
    val lunar = entry.calendar == MemorialCalendarType.Lunar

    Column(modifier.fillMaxSize().background(colors.card2).imePadding()) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Gutter, vertical = 6.dp)) {
            IlNavBar(
                title = if (draft.isNew) "Thêm người thân" else "Sửa thông tin",
                right = "Lưu",
                onBack = actions.onBack,
                onAction = actions.onSave,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter)
                .navigationBarsPadding()
                .padding(bottom = 34.dp),
        ) {
            // Ảnh chân dung: chưa có bộ chọn ảnh, tạm dùng chữ viết tắt (xem Docs/gio_assumptions.md).
            Box(Modifier.fillMaxWidth().padding(top = 22.dp), contentAlignment = Alignment.Center) {
                IlAvatar(initials = initialsOf(entry.name), size = 104.dp)
            }

            IlField(
                label = "Họ và tên",
                value = entry.name,
                onValueChange = actions.onName,
                placeholder = "VD: Nguyễn Văn An",
                isError = draft.showNameError,
                error = if (draft.showNameError) "Vui lòng nhập họ và tên" else "",
                modifier = Modifier.padding(top = 22.dp),
            )

            SectionLabel("Mối quan hệ", Modifier.padding(top = 22.dp))
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MemorialRelation.entries.forEach { relation ->
                    IlChip(
                        label = relation.label,
                        selected = entry.relation == relation,
                        onClick = { actions.onRelation(relation) },
                    )
                }
            }

            SectionLabel("Ngày mất", Modifier.padding(top = 24.dp))
            CalendarSegment(
                selected = entry.calendar,
                onSelect = actions.onCalendar,
                modifier = Modifier.padding(top = 10.dp),
            )
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PickerField(
                    label = "Ngày",
                    valueLabel = GioFormat.dayOptionLabel(entry.day, lunar),
                    options = (1..if (lunar) 30 else 31).map { it to GioFormat.dayOptionLabel(it, lunar) },
                    onSelect = actions.onDay,
                    modifier = Modifier.weight(1f),
                )
                PickerField(
                    label = "Tháng",
                    valueLabel = GioFormat.monthOptionLabel(entry.month, lunar),
                    options = (1..12).map { it to GioFormat.monthOptionLabel(it, lunar) },
                    onSelect = actions.onMonth,
                    modifier = Modifier.weight(1.3f),
                )
                PickerField(
                    label = "Năm mất",
                    valueLabel = entry.deathYear?.toString() ?: "Không rõ",
                    options = listOf<Pair<Int?, String>>(null to "Không rõ") +
                        (0 until YearSpan).map { (currentYear - it).let { year -> year to year.toString() } },
                    onSelect = actions.onYear,
                    modifier = Modifier.weight(1.2f),
                )
            }
            draft.preview?.let { PreviewBox(it, Modifier.padding(top = 10.dp)) }

            SectionLabel("Ghi chú", Modifier.padding(top = 24.dp))
            NoteField(
                value = entry.note,
                onValueChange = actions.onNote,
                modifier = Modifier.padding(top = 7.dp),
            )

            SectionLabel("Nhắc nhở", Modifier.padding(top = 24.dp))
            ReminderCard(entry = entry, actions = actions, modifier = Modifier.padding(top = 10.dp))

            Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                IlButton(label = if (draft.isNew) "Lưu vào sổ giỗ" else "Lưu thay đổi", onClick = actions.onSave)
                if (!draft.isNew) {
                    IlButton(label = "Xóa khỏi sổ giỗ", variant = IlButtonVariant.Danger, onClick = actions.onDelete)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = sans(11f, colors.ink.copy(alpha = 0.48f), letterSpacing = 0.66.sp),
    )
}

/** Công tắc Âm lịch / Dương lịch dạng viên thuốc. */
@Composable
private fun CalendarSegment(
    selected: MemorialCalendarType,
    onSelect: (MemorialCalendarType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IlRadius.Pill))
            .background(colors.ink.copy(alpha = 0.06f))
            .padding(3.dp),
    ) {
        listOf(MemorialCalendarType.Lunar to "Âm lịch", MemorialCalendarType.Solar to "Dương lịch")
            .forEach { (type, label) ->
                val on = type == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .then(if (on) Modifier.shadow(1.dp, RoundedCornerShape(IlRadius.Pill)) else Modifier)
                        .clip(RoundedCornerShape(IlRadius.Pill))
                        .background(if (on) colors.card else Color.Transparent)
                        .tappable { onSelect(type) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = sans(12.5f, if (on) colors.ink else colors.ink.copy(alpha = 0.5f), FontWeight.Medium),
                    )
                }
            }
    }
}

/** Ô chọn một giá trị từ danh sách (thay `<select>` của prototype) bằng menu thả xuống. */
@Composable
private fun <T> PickerField(
    label: String,
    valueLabel: String,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, modifier = Modifier.padding(bottom = 5.dp), style = sans(10.5f, colors.ink.copy(alpha = 0.45f)))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(IlRadius.Input))
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.12f), RoundedCornerShape(IlRadius.Input))
                    .tappable { expanded = true }
                    .padding(start = 14.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = valueLabel,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = sans(14f, colors.ink),
                )
                Text("⌄", style = sans(14f, colors.ink.copy(alpha = 0.45f)))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (value, text) ->
                    DropdownMenuItem(
                        text = { Text(text, style = sans(14f, colors.ink)) },
                        onClick = {
                            expanded = false
                            onSelect(value)
                        },
                    )
                }
            }
        }
    }
}

/** Hộp vàng nhạt: "Kỳ giỗ tới: …" và diễn giải âm/dương. */
@Composable
private fun PreviewBox(preview: GioPreview, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IlRadius.Input))
            .background(IlGold.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(preview.headline, style = sans(12f, colors.ink, lineHeight = 19.sp))
        Text(preview.detail, style = sans(12f, colors.ink.copy(alpha = 0.52f), lineHeight = 19.sp))
    }
}

/** Ô ghi chú nhiều dòng (3 dòng) — `IlField` chỉ hỗ trợ một dòng. */
@Composable
private fun NoteField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Input)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = 3,
        maxLines = 6,
        textStyle = sans(14f, colors.ink, lineHeight = 22.sp),
        cursorBrush = SolidColor(IlGold),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.12f), shape),
        decorationBox = { inner ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = "Mâm cúng, người cần báo, nơi thờ phụng…",
                        style = sans(14f, colors.ink.copy(alpha = 0.32f), lineHeight = 22.sp),
                    )
                }
                inner()
            }
        },
    )
}

/** Thẻ nhắc nhở: bật/tắt, thời điểm nhắc, giờ nhắc, đồng bộ Lời nhắc. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderCard(entry: MemorialEntry, actions: GioEditActions, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(16.dp),
    ) {
        ToggleRow(
            title = "Nhắc trước ngày giỗ",
            body = "Thông báo để kịp chuẩn bị mâm cúng, báo con cháu.",
            on = entry.remind,
            onToggle = actions.onRemindToggle,
        )
        if (entry.remind) {
            Divider()
            Text("Thời điểm nhắc", style = sans(11.5f, colors.ink.copy(alpha = 0.5f)))
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LeadOptions.forEach { (days, label) ->
                    IlChip(label = label, selected = entry.leadDays == days, onClick = { actions.onLead(days) })
                }
            }
            Text("Giờ nhắc", modifier = Modifier.padding(top = 14.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.5f)))
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimeOptions.forEach { time ->
                    IlChip(
                        label = GioFormat.timeLabel(entry.copy(remindTime = time)),
                        selected = entry.remindTime == time,
                        onClick = { actions.onTime(time) },
                    )
                }
            }
            Divider()
            ToggleRow(
                title = "Thêm vào Lời nhắc (Reminders)",
                body = "Tạo lời nhắc cho kỳ giỗ tới. Ngày âm đổi sang dương mỗi năm nên app sẽ tự cập nhật lời nhắc sau mỗi kỳ.",
                on = entry.syncReminders,
                onToggle = actions.onSyncToggle,
            )
        }
    }
}

@Composable
private fun Divider() {
    val colors = LocalIlColors.current
    Box(Modifier.padding(vertical = 14.dp).fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
}

@Composable
private fun ToggleRow(title: String, body: String, on: Boolean, onToggle: () -> Unit) {
    val colors = LocalIlColors.current
    Row(
        modifier = Modifier.fillMaxWidth().tappable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(body, modifier = Modifier.padding(top = 2.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.5f), lineHeight = 17.sp))
        }
        GioSwitch(on)
    }
}

/** Công tắc 40×24, núm 20px dịch 16px khi bật (khớp prototype `remBg/remX`). */
@Composable
private fun GioSwitch(on: Boolean) {
    val colors = LocalIlColors.current
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(IlRadius.Pill))
            .background(if (on) IlGold else colors.ink.copy(alpha = 0.16f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(start = 2.dp)
                .offset(x = if (on) 16.dp else 0.dp)
                .size(20.dp)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

private fun previewDraft(isNew: Boolean, showNameError: Boolean = false) = GioDraft(
    entry = MemorialEntry(
        id = "preview", name = if (showNameError) "" else "Trần Thị Mai",
        relation = MemorialRelation.MaternalGrandmother, calendar = MemorialCalendarType.Lunar,
        day = 23, month = 8, deathYear = 2015, note = "Cúng chay.",
        remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0), syncReminders = true,
    ),
    isNew = isNew,
    showNameError = showNameError,
    preview = GioPreview(
        headline = "Kỳ giỗ tới: Chủ Nhật, 04/10/2026 · còn 25 ngày",
        detail = "Tức 23 tháng Tám âm lịch · Giỗ lần thứ 11. Tháng thiếu không có ngày 30 sẽ nhắc vào ngày 29.",
    ),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 1200)
@Composable
private fun GioEditPreview() {
    InnerLampTheme { GioEditContent(previewDraft(isNew = false), currentYear = 2026, actions = GioEditActions()) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun GioEditNewErrorPreview() {
    InnerLampTheme {
        GioEditContent(previewDraft(isNew = true, showNameError = true), currentYear = 2026, actions = GioEditActions())
    }
}

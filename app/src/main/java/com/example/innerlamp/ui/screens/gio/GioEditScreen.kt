package com.example.innerlamp.ui.screens.gio

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.innerlamp.BuildConfig
import com.example.innerlamp.core.media.PhotoEncoding
import com.example.innerlamp.di.StorageModule
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
import com.example.innerlamp.ui.theme.IlFormError
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Gutter = 20.dp
private val LeadOptions = listOf(0 to "Đúng ngày", 1 to "Trước 1 ngày", 3 to "Trước 3 ngày", 7 to "Trước 7 ngày")
private val TimeOptions = listOf(LocalTime.of(6, 0), LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(19, 0))

/** Năm mất chọn được: 1900 tới năm nay (BR-MEM-03). / Years allowed: 1900 through this year. */
private const val MIN_YEAR = 1900

/** Đã hỏi quyền thông báo một lần chưa (chỉ hỏi một lần). / Whether the notification prompt was shown. */
private const val ASKED_NOTIFICATIONS_KEY = "memorial.notifications.asked"

/** Các thao tác trên form Thêm/sửa; tách khỏi [GioViewModel] để preview/test dựng được. */
internal class GioEditActions(
    val onBack: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onRequestDelete: () -> Unit = {},
    val onCancelDelete: () -> Unit = {},
    val onConfirmDelete: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onRelation: (MemorialRelation) -> Unit = {},
    val onCalendar: (MemorialCalendarType) -> Unit = {},
    val onDay: (Int) -> Unit = {},
    val onMonth: (Int) -> Unit = {},
    val onYear: (Int?) -> Unit = {},
    val onLeapToggle: () -> Unit = {},
    val onNote: (String) -> Unit = {},
    val onRemindToggle: () -> Unit = {},
    val onLead: (Int) -> Unit = {},
    val onTime: (LocalTime) -> Unit = {},
    val onPickPhoto: () -> Unit = {},
    val onDeletePhoto: () -> Unit = {},
    val onOpenNotificationSettings: () -> Unit = {},
    val onRelogin: () -> Unit = {},
)

/**
 * Màn "Thêm/sửa ngày giỗ".
 *
 * - Quyền `POST_NOTIFICATIONS` (API 33+) được xin **một lần** khi mở form có nhắc bật; từ chối không chặn
 *   việc lưu và vẫn hiện "Có nhắc" ở danh sách (AC22, MEM-RMD-07).
 * - Ảnh chân dung nằm sau cờ `BuildConfig.MEMORIAL_PHOTO_ENABLED` (D4) — mặc định tắt.
 * - Mọi lối thoát (lưu, xóa, back) đều đi qua [GioViewModel] và làm `draft` về `null`; màn thấy
 *   `draft == null` thì gọi [onClose] đúng một lần.
 */
@Composable
fun GioEditScreen(
    viewModel: GioViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onLogin: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context) { StorageModule.providePreferencesStorage(context) }
    var notificationsBlocked by remember { mutableStateOf(!canNotify(context)) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsBlocked = !granted && !canNotify(context)
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            // The 5 MiB check happens on the bytes actually sent; only the raw file read is capped here so a
            // huge file cannot exhaust memory. / Chỉ chặn file gốc quá lớn; 5 MiB kiểm trên bytes thực gửi.
            val rawLength = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } }
                    .getOrNull()
            }
            if (rawLength != null && rawLength > PhotoEncoding.MAX_RAW_BYTES) {
                viewModel.onPhotoTooLarge()
                return@launch
            }
            val bytes = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            if (bytes == null || bytes.isEmpty()) {
                viewModel.onPhotoUnreadable()
            } else {
                viewModel.uploadPhoto(bytes)
            }
        }
    }

    // Ask at most once for the notification permission when a reminded entry is open.
    // Chỉ xin quyền thông báo một lần khi mở mục có nhắc.
    val remind = state.draft?.entry?.remind == true
    LaunchedEffect(remind) {
        if (!remind || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || canNotify(context)) return@LaunchedEffect
        if (prefs.getBoolean(ASKED_NOTIFICATIONS_KEY)) return@LaunchedEffect
        prefs.putBoolean(ASKED_NOTIFICATIONS_KEY, true)
        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Giữ bản nháp cuối để khung hình còn lại khi chuyển cảnh (draft đã null) không bị trống.
    var lastDraft by remember { mutableStateOf(state.draft) }
    state.draft?.let { lastDraft = it }

    LaunchedEffect(state.draft == null) {
        if (state.draft == null) onClose()
    }
    BackHandler { viewModel.discardDraft() }

    val draft = lastDraft ?: return

    if (state.confirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Xóa khỏi sổ giỗ?") },
            text = { Text("Mục “${draft.entry.name}” sẽ bị xóa khỏi sổ giỗ của bạn.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Xóa", color = IlFormError) }
            },
            dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("Hủy") } },
        )
    }

    GioEditContent(
        draft = draft,
        currentYear = remember { LocalDate.now().year },
        notificationsBlocked = notificationsBlocked && draft.entry.remind,
        photoEnabled = BuildConfig.MEMORIAL_PHOTO_ENABLED,
        actions = GioEditActions(
            onBack = viewModel::discardDraft,
            onSave = { viewModel.save() },
            onRequestDelete = viewModel::requestDelete,
            onCancelDelete = viewModel::cancelDelete,
            onConfirmDelete = viewModel::confirmDelete,
            onName = viewModel::onNameChange,
            onRelation = viewModel::onRelationChange,
            onCalendar = viewModel::onCalendarChange,
            onDay = viewModel::onDayChange,
            onMonth = viewModel::onMonthChange,
            onYear = viewModel::onDeathYearChange,
            onLeapToggle = viewModel::onLeapMonthToggle,
            onNote = viewModel::onNoteChange,
            onRemindToggle = viewModel::onRemindToggle,
            onLead = viewModel::onLeadDaysChange,
            onTime = viewModel::onRemindTimeChange,
            onPickPhoto = {
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDeletePhoto = viewModel::deletePhoto,
            onOpenNotificationSettings = { openNotificationSettings(context) },
            onRelogin = onLogin,
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
    notificationsBlocked: Boolean = false,
    photoEnabled: Boolean = false,
) {
    val colors = LocalIlColors.current
    val entry = draft.entry
    val lunar = entry.calendar == MemorialCalendarType.Lunar
    val busy = draft.saving

    Column(modifier.fillMaxSize().background(colors.card2).imePadding()) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Gutter, vertical = 6.dp)) {
            IlNavBar(
                title = if (draft.isNew) "Thêm người thân" else "Sửa thông tin",
                right = if (busy) "" else "Lưu",
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
            Box(Modifier.fillMaxWidth().padding(top = 22.dp), contentAlignment = Alignment.Center) {
                PhotoAvatar(entry = entry, size = 104.dp)
            }

            if (photoEnabled) {
                SectionLabel("Ảnh chân dung", Modifier.padding(top = 12.dp))
                if (draft.isNew) {
                    Text(
                        text = "Lưu mục trước, sau đó thêm ảnh.",
                        modifier = Modifier.padding(top = 6.dp),
                        style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        IlButton(
                            label = if (entry.photoUrl == null) "Thêm ảnh" else "Đổi ảnh",
                            variant = IlButtonVariant.Ghost,
                            enabled = !busy,
                            full = false,
                            onClick = actions.onPickPhoto,
                            modifier = Modifier.weight(1f),
                        )
                        if (entry.photoUrl != null) {
                            IlButton(
                                label = "Gỡ ảnh",
                                variant = IlButtonVariant.Ghost,
                                enabled = !busy,
                                full = false,
                                onClick = actions.onDeletePhoto,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                if (draft.photoError.isNotEmpty()) {
                    ErrorText(draft.photoError, Modifier.padding(top = 6.dp))
                }
            }

            IlField(
                label = "Họ và tên",
                value = entry.name,
                onValueChange = actions.onName,
                placeholder = "VD: Nguyễn Văn An",
                enabled = !busy,
                isError = draft.nameError.isNotEmpty(),
                error = draft.nameError,
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
                        enabled = !busy,
                        onClick = { actions.onRelation(relation) },
                    )
                }
            }

            SectionLabel("Ngày mất", Modifier.padding(top = 24.dp))
            CalendarSegment(
                selected = entry.calendar,
                enabled = !busy,
                onSelect = actions.onCalendar,
                modifier = Modifier.padding(top = 10.dp),
            )
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PickerField(
                    label = "Ngày",
                    valueLabel = GioFormat.dayOptionLabel(entry.day, lunar),
                    options = (1..if (lunar) 30 else 31).map { it to GioFormat.dayOptionLabel(it, lunar) },
                    enabled = !busy,
                    onSelect = actions.onDay,
                    modifier = Modifier.weight(1f),
                )
                PickerField(
                    label = "Tháng",
                    valueLabel = GioFormat.monthOptionLabel(entry.month, lunar),
                    options = (1..12).map { it to GioFormat.monthOptionLabel(it, lunar) },
                    enabled = !busy,
                    onSelect = actions.onMonth,
                    modifier = Modifier.weight(1.3f),
                )
                PickerField(
                    label = "Năm mất",
                    valueLabel = entry.deathYear?.toString() ?: "Không rõ",
                    options = listOf<Pair<Int?, String>>(null to "Không rõ") +
                        (currentYear downTo MIN_YEAR).map { year -> year to year.toString() },
                    enabled = !busy,
                    onSelect = actions.onYear,
                    modifier = Modifier.weight(1.2f),
                )
            }
            if (lunar) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Tháng nhuận", style = sans(13f, colors.ink, FontWeight.Medium))
                        Text(
                            text = "Người mất trong tháng nhuận của năm đó. Giỗ vẫn tính ở tháng thường.",
                            modifier = Modifier.padding(top = 2.dp),
                            style = sans(11.5f, colors.ink.copy(alpha = 0.5f), lineHeight = 17.sp),
                        )
                    }
                    GioSwitch(on = entry.leapMonth, enabled = !busy, onToggle = actions.onLeapToggle)
                }
            }
            if (draft.dateError.isNotEmpty()) {
                ErrorText(draft.dateError, Modifier.padding(top = 6.dp))
            }
            draft.preview?.let { PreviewBox(it, Modifier.padding(top = 10.dp)) }

            SectionLabel("Ghi chú", Modifier.padding(top = 24.dp))
            NoteField(
                value = entry.note,
                enabled = !busy,
                onValueChange = actions.onNote,
                modifier = Modifier.padding(top = 7.dp),
            )
            if (draft.noteError.isNotEmpty()) {
                ErrorText(draft.noteError, Modifier.padding(top = 6.dp))
            }

            SectionLabel("Nhắc nhở", Modifier.padding(top = 24.dp))
            ReminderCard(
                entry = entry,
                busy = busy,
                notificationsBlocked = notificationsBlocked,
                actions = actions,
                modifier = Modifier.padding(top = 10.dp),
            )

            if (draft.sessionExpired) {
                SessionExpiredNotice(onRelogin = actions.onRelogin, Modifier.padding(top = 16.dp))
            } else if (draft.formError.isNotEmpty()) {
                ErrorText(draft.formError, Modifier.padding(top = 16.dp))
            }

            Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                IlButton(
                    label = if (draft.isNew) "Lưu vào sổ giỗ" else "Lưu thay đổi",
                    enabled = !busy,
                    loading = busy,
                    onClick = actions.onSave,
                )
                if (!draft.isNew) {
                    IlButton(
                        label = "Xóa khỏi sổ giỗ",
                        variant = IlButtonVariant.Danger,
                        enabled = !busy,
                        onClick = actions.onRequestDelete,
                    )
                }
            }
        }
    }
}

/**
 * A write returned 401: the form stays exactly as typed and the user decides when to sign in again. /
 * Ghi gặp 401: form giữ nguyên, người dùng tự quyết định đăng nhập lại.
 */
@Composable
private fun SessionExpiredNotice(onRelogin: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IlRadius.Input))
            .background(IlFormError.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
            style = sans(12f, colors.ink, lineHeight = 18.sp),
        )
        Text(
            text = "Đăng nhập lại",
            modifier = Modifier.padding(top = 8.dp).tappable { onRelogin() },
            style = sans(12.5f, colors.link, FontWeight.Medium),
        )
    }
}

@Composable
private fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = sans(11.5f, IlFormError, lineHeight = 17.sp))
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

/** Ảnh chân dung từ URL ký (chỉ trong bộ nhớ); không có thì chữ viết tắt. */
@Composable
private fun PhotoAvatar(entry: MemorialEntry, size: androidx.compose.ui.unit.Dp) {
    val url = entry.photoUrl
    if (url.isNullOrBlank()) {
        IlAvatar(initials = initialsOf(entry.name), size = size)
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(RoundedCornerShape(size / 2)),
        )
    }
}

/** Công tắc Âm lịch / Dương lịch dạng viên thuốc. */
@Composable
private fun CalendarSegment(
    selected: MemorialCalendarType,
    onSelect: (MemorialCalendarType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
                        .tappable(enabled) { onSelect(type) },
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
    enabled: Boolean = true,
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
                    .tappable(enabled) { expanded = true }
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
private fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Input)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
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

/**
 * Thẻ nhắc nhở: bật/tắt, thời điểm nhắc, giờ nhắc, và câu báo khi máy không cho phép thông báo.
 * Công tắc "Thêm vào Lời nhắc (Reminders)" đã bỏ (FR-MEM-009 Removed, AC31).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderCard(
    entry: MemorialEntry,
    busy: Boolean,
    notificationsBlocked: Boolean,
    actions: GioEditActions,
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
            .padding(16.dp),
    ) {
        ToggleRow(
            title = "Nhắc trước ngày giỗ",
            body = "Thông báo để kịp chuẩn bị mâm cúng, báo con cháu.",
            on = entry.remind,
            enabled = !busy,
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
                    IlChip(
                        label = label,
                        selected = entry.leadDays == days,
                        enabled = !busy,
                        onClick = { actions.onLead(days) },
                    )
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
                        enabled = !busy,
                        onClick = { actions.onTime(time) },
                    )
                }
            }
            if (notificationsBlocked) {
                Divider()
                Text(
                    text = "Bạn sẽ không nhận được lời nhắc giỗ cho tới khi bật thông báo. " +
                        "Mở Cài đặt để bật thông báo.",
                    style = sans(11.5f, colors.ink.copy(alpha = 0.6f), lineHeight = 17.sp),
                )
                Text(
                    text = "Mở Cài đặt",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .tappable { actions.onOpenNotificationSettings() },
                    style = sans(12.5f, colors.link, FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    val colors = LocalIlColors.current
    Box(Modifier.padding(vertical = 14.dp).fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
}

@Composable
private fun ToggleRow(
    title: String,
    body: String,
    on: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = Modifier.fillMaxWidth().tappable(enabled, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(body, modifier = Modifier.padding(top = 2.dp), style = sans(11.5f, colors.ink.copy(alpha = 0.5f), lineHeight = 17.sp))
        }
        GioSwitch(on, enabled)
    }
}

/** Công tắc 40×24, núm 20px dịch 16px khi bật (khớp prototype `remBg/remX`). */
@Composable
private fun GioSwitch(on: Boolean, enabled: Boolean = true, onToggle: (() -> Unit)? = null) {
    val colors = LocalIlColors.current
    Box(
        modifier = Modifier
            .then(if (onToggle != null) Modifier.tappable(enabled, onClick = onToggle) else Modifier)
            .width(40.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(IlRadius.Pill))
            .background(
                when {
                    !enabled -> colors.ink.copy(alpha = 0.10f)
                    on -> IlGold
                    else -> colors.ink.copy(alpha = 0.16f)
                },
            ),
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

/** Máy có cho phép thông báo của app không (quyền trên API 33+, công tắc/kênh trong Cài đặt). */
private fun canNotify(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    return NotificationManagerCompatSafe.enabled(context)
}

/** Small wrapper so the deprecated `NotificationManagerCompat` call stays in one place. */
private object NotificationManagerCompatSafe {
    fun enabled(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() ?: true
}

/** Opens this app's notification settings page (MEM-RMD-07). / Mở trang cài đặt thông báo của app. */
private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

// ── Preview ──────────────────────────────────────────────────────────────────

private fun previewDraft(isNew: Boolean, nameError: String = "") = GioDraft(
    entry = MemorialEntry(
        id = if (isNew) 0L else 1L, name = if (nameError.isEmpty()) "Trần Thị Mai" else "",
        relation = MemorialRelation.MaternalGrandmother, calendar = MemorialCalendarType.Lunar,
        day = 23, month = 8, deathYear = 2015, note = "Cúng chay.",
        remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0),
    ),
    isNew = isNew,
    nameError = nameError,
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
        GioEditContent(
            previewDraft(isNew = true, nameError = "Vui lòng nhập họ và tên"),
            currentYear = 2026,
            actions = GioEditActions(),
        )
    }
}

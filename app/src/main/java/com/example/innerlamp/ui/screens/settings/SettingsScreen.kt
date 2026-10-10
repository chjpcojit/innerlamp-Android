package com.example.innerlamp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.ProfileUser
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlSettingKind
import com.example.innerlamp.ui.components.IlSettingRow
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Các điểm điều hướng đi ra khỏi màn Cài đặt. Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class SettingsCallbacks(
    val onBack: () -> Unit = {},
    val onProfile: () -> Unit = {},
    val onDarkMode: () -> Unit = {},
    val onDownloads: () -> Unit = {},
    val onLanguage: () -> Unit = {},
    val onHelp: () -> Unit = {},
    val onExportData: () -> Unit = {},
    val onOffline: () -> Unit = {},
)

/**
 * Màn hình Cài đặt — dựng lại từ prototype `Docs/design_handoff_innerlamp/`
 * (màn `settings`).
 *
 * Bố cục (trên → dưới): header dán ("Cài đặt") → thẻ tài khoản → THỜI KHÓA
 * (chuông, nhắc ngày trai, tiếng chuông) → HIỂN THỊ & NGÔN NGỮ (cỡ chữ, song
 * ngữ, chế độ tối) → NỘI DUNG & TRỢ GIÚP → RIÊNG TƯ → chân màn (phiên bản,
 * điều khoản, trạng thái ngoại tuyến).
 *
 * @author innerlamp
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    callbacks: SettingsCallbacks = SettingsCallbacks(),
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        callbacks = callbacks,
        onToggleReminder = viewModel::toggleReminder,
        onToggleFastingReminder = viewModel::toggleFastingReminder,
        onToggleBilingual = viewModel::toggleBilingual,
        onToggleHideJournal = viewModel::toggleHideJournal,
        onFontSizeChange = viewModel::onFontSizeChange,
        modifier = modifier,
    )
}

/** Phần stateless của [SettingsScreen] — dùng cho preview và test. */
@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    callbacks: SettingsCallbacks,
    onToggleReminder: () -> Unit,
    onToggleFastingReminder: () -> Unit,
    onToggleBilingual: () -> Unit,
    onToggleHideJournal: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = { SettingsHeader(onBack = callbacks.onBack) },
        modifier = modifier.background(colors.canvas),
    ) { headerHeight ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        ) {
            Spacer(Modifier.height(headerHeight + 20.dp))
            AccountCard(
                user = state.user,
                sub = state.accountSub,
                onClick = callbacks.onProfile,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            SectionLabel("Thời khóa")
            SettingsCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                IlSettingRow(
                    title = "Chuông tỉnh thức",
                    sub = state.reminderSummary,
                    kind = IlSettingKind.Switch,
                    on = state.reminderEnabled,
                    onClick = onToggleReminder,
                )
                IlSettingRow(
                    title = "Nhắc ngày trai & rằm",
                    sub = "Nhắc trước một ngày, lúc 20:00",
                    kind = IlSettingKind.Switch,
                    on = state.fastingReminderEnabled,
                    onClick = onToggleFastingReminder,
                )
                IlSettingRow(
                    title = "Tiếng chuông",
                    kind = IlSettingKind.Value,
                    value = state.bellSound,
                    last = true,
                )
            }

            SectionLabel("Hiển thị & ngôn ngữ")
            SettingsCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                FontSizeRow(
                    fraction = state.fontSizeFraction,
                    onFontSizeChange = onFontSizeChange,
                )
                IlSettingRow(
                    title = "Song ngữ Việt – Anh",
                    kind = IlSettingKind.Switch,
                    on = state.bilingualEnabled,
                    onClick = onToggleBilingual,
                )
                IlSettingRow(
                    title = "Chế độ tối",
                    kind = IlSettingKind.Value,
                    value = state.themeLabel,
                    onClick = callbacks.onDarkMode,
                    last = true,
                )
            }

            SectionLabel("Nội dung & trợ giúp")
            SettingsCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                IlSettingRow(
                    title = "Nghe không cần mạng",
                    sub = state.downloadsSummary,
                    kind = IlSettingKind.Chevron,
                    onClick = callbacks.onDownloads,
                )
                IlSettingRow(
                    title = "Ngôn ngữ & hiển thị",
                    sub = state.languageSummary,
                    kind = IlSettingKind.Chevron,
                    onClick = callbacks.onLanguage,
                )
                IlSettingRow(
                    title = "Trợ giúp & câu hỏi thường gặp",
                    kind = IlSettingKind.Chevron,
                    onClick = callbacks.onHelp,
                    last = true,
                )
            }

            SectionLabel("Riêng tư")
            SettingsCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                IlSettingRow(
                    title = "Ẩn nhật ký khỏi đạo tràng",
                    sub = "Chỉ mình bạn đọc được",
                    kind = IlSettingKind.Switch,
                    on = state.hideJournalEnabled,
                    onClick = onToggleHideJournal,
                )
                IlSettingRow(
                    title = "Tải dữ liệu tu tập của tôi",
                    kind = IlSettingKind.Chevron,
                    onClick = callbacks.onExportData,
                    last = true,
                )
            }

            Footer(appVersion = state.appVersion, onOffline = callbacks.onOffline)

            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Header: `IlNavBar` "Cài đặt". Blur + hairline do [IlBlurHeaderScaffold] lo. */
@Composable
private fun SettingsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 8.dp)) {
        IlNavBar(title = "Cài đặt", showBack = true, onBack = onBack)
    }
}

/** Thẻ tài khoản: avatar 46px + tên + pháp danh/SĐT + chevron. */
@Composable
private fun AccountCard(
    user: ProfileUser,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Avatar: nền gradient sunk2→tint, viền vàng, chữ cái Lora.
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.sunk2, colors.tint)))
                .border(1.dp, IlGold.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.initials,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = colors.link,
                ),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = user.fullName,
                style = sans(14f, colors.ink, FontWeight.Medium),
            )
            Text(
                text = sub,
                modifier = Modifier.padding(top = 2.dp),
                style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            )
        }
        Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
    }
}

/** Tiêu đề nhóm (eyebrow) — 600 10px, letter-spacing .14em, `ink-42`, lùi 4px so với mép thẻ. */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = text,
        modifier = modifier.padding(start = 24.dp, top = 22.dp, bottom = 10.dp),
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            color = colors.ink.copy(alpha = 0.42f),
        ),
    )
}

/** Thẻ nhóm cài đặt: nền card, viền `ink-08`, bo 20dp. */
@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
        content = content,
    )
}

/** Hàng "Cỡ chữ kinh văn": nhãn + thanh trượt A→A, có hairline đáy. */
@Composable
private fun FontSizeRow(
    fraction: Float,
    onFontSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = colors.ink.copy(alpha = 0.06f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Text("Cỡ chữ kinh văn", style = sans(13.5f, colors.ink))
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "A",
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = colors.ink.copy(alpha = 0.45f),
                ),
            )
            FontSizeSlider(
                fraction = fraction,
                onFontSizeChange = onFontSizeChange,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "A",
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = colors.ink,
                ),
            )
        }
    }
}

/**
 * Thanh trượt cỡ chữ: track 3px + fill vàng + núm 20px. Chạm/kéo ngang để chọn
 * một trong [SettingsFontSizeSteps] bậc; cuộn dọc không bị chặn.
 */
@Composable
private fun FontSizeSlider(
    fraction: Float,
    onFontSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val clamped = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(28.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val width = size.width.toFloat()
                    onFontSizeChange(down.position.x.toFontIndex(width))
                    var dragging = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val dx = change.position.x - down.position.x
                        val dy = change.position.y - down.position.y
                        if (!dragging) {
                            when {
                                abs(dx) > viewConfiguration.touchSlop && abs(dx) > abs(dy) ->
                                    dragging = true
                                abs(dy) > viewConfiguration.touchSlop -> break // cuộn dọc → nhả cho scroll
                            }
                        }
                        if (dragging) {
                            change.consume()
                            onFontSizeChange(change.position.x.toFontIndex(width))
                        }
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track nền (3px).
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.ink.copy(alpha = 0.12f)),
        )
        // Fill vàng tới vị trí núm.
        Box(
            Modifier
                .fillMaxWidth(clamped)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(IlGold),
        )
        // Núm 20px, tâm nằm đúng vị trí fill.
        Box(
            Modifier.fillMaxWidth(clamped),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .border(1.5.dp, IlGold, CircleShape)
                    .shadow(1.dp, CircleShape),
            )
        }
    }
}

/** Chân màn: phiên bản + điều khoản + lối tắt xem trạng thái ngoại tuyến. */
@Composable
private fun Footer(
    appVersion: String,
    onOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Text(
        text = "$appVersion · Điều khoản · Quyền riêng tư\nXem trạng thái ngoại tuyến →",
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp)
            .tappable(onClick = onOffline),
        textAlign = TextAlign.Center,
        style = sans(11f, colors.ink.copy(alpha = 0.38f), lineHeight = 18.7f.sp),
    )
}

/** Vị trí chạm → bậc cỡ chữ (snap vào n bậc, 0..n-1). */
private fun Float.toFontIndex(trackWidth: Float): Int {
    val ratio = (this / trackWidth).coerceIn(0f, 1f)
    return (ratio * (SettingsFontSizeSteps - 1)).roundToInt()
}

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
private fun SettingsPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SettingsContent(
            state = previewState(),
            callbacks = SettingsCallbacks(),
            onToggleReminder = {},
            onToggleFastingReminder = {},
            onToggleBilingual = {},
            onToggleHideJournal = {},
            onFontSizeChange = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun SettingsPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SettingsContent(
            state = previewState(),
            callbacks = SettingsCallbacks(),
            onToggleReminder = {},
            onToggleFastingReminder = {},
            onToggleBilingual = {},
            onToggleHideJournal = {},
            onFontSizeChange = {},
        )
    }
}

private fun previewState(): SettingsUiState = SettingsUiState(
    user = SettingsSampleData.user,
    phone = SettingsSampleData.PHONE,
    reminderEnabled = true,
    reminderSummary = SettingsSampleData.REMINDER_SUMMARY,
    fastingReminderEnabled = true,
    bellSound = SettingsSampleData.BELL_SOUND,
    fontSizeIndex = 1,
    bilingualEnabled = false,
    themeLabel = "Sáng",
    downloadsSummary = SettingsSampleData.DOWNLOADS_SUMMARY,
    hideJournalEnabled = true,
    appVersion = SettingsSampleData.APP_VERSION,
)

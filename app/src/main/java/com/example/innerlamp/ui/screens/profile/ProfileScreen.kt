package com.example.innerlamp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.AuthModule
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.BadgeTone
import com.example.innerlamp.domain.model.PracticeStat
import com.example.innerlamp.domain.model.ProfileBadge
import com.example.innerlamp.domain.model.ProfileUser
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.components.IlSettingKind
import com.example.innerlamp.ui.components.IlSettingRow
import com.example.innerlamp.ui.components.LeafShape
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/** Số cột của lưới nhiệt "Nhật ký 12 tuần qua" (84 ô = 21 cột × 4 hàng). */
private const val HEAT_COLUMNS = 21

/** Ngọn lửa nhỏ (bầu tròn, hơi thon đáy) — prototype `border-radius:50% 50% 45% 45%`. */
private val FlameShape = RoundedCornerShape(
    topStartPercent = 50,
    topEndPercent = 50,
    bottomEndPercent = 45,
    bottomStartPercent = 45,
)

/**
 * Các điểm điều hướng đi ra khỏi màn Hồ sơ. Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class ProfileCallbacks(
    val onEdit: () -> Unit = {},
    val onStats: () -> Unit = {},
    val onAwards: () -> Unit = {},
    val onOffer: () -> Unit = {},
    val onPaywall: () -> Unit = {},
    val onRemind: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onNotifications: () -> Unit = {},
    val onTemple: () -> Unit = {},
    val onTab: (IlBottomTab) -> Unit = {},
    /** Đã đăng xuất xong (sau khi xác nhận) — mở Login làm gốc. */
    val onSignedOut: () -> Unit = {},
)

/**
 * Màn hình Hồ sơ — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `profile`,
 * tab gốc "Tôi").
 *
 * Bố cục (trên → dưới): header (avatar + tên + "Sửa hồ sơ") → 3 cột số liệu → "Nhật ký
 * 12 tuần qua" (heatmap) → "Dấu ấn tu tập" → thẻ "Công đức đã hồi hướng" → thẻ "Sen Vàng"
 * → nhóm cài đặt → bottom nav (tab Tôi).
 *
 * @author innerlamp
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    callbacks: ProfileCallbacks = ProfileCallbacks(),
    viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory { initializer { ProfileViewModel(AuthModule.provideSignOutUseCase()) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    ProfileContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier,
        onSignOut = { confirmSignOut = true },
    )
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Đăng xuất?") },
            text = { Text("Bạn sẽ cần đăng nhập lại để dùng các tính năng của tài khoản.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    viewModel.signOut(callbacks.onSignedOut)
                }) { Text("Đăng xuất") }
            },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Huỷ") } },
        )
    }
}

/** Phần stateless của [ProfileScreen] — dùng cho preview và test. */
@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    callbacks: ProfileCallbacks,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        ProfileHeader(user = state.user, onEdit = callbacks.onEdit)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(22.dp))
            StatsRow(stats = state.stats, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(22.dp))
            JournalCard(
                label = state.journalLabel,
                heat = state.heat,
                onClick = callbacks.onStats,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(22.dp))
            BadgesSection(badges = state.badges, onSeeAll = callbacks.onAwards)

            Spacer(Modifier.height(22.dp))
            MeritCard(
                meritLine = state.meritLine,
                onClick = callbacks.onOffer,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(14.dp))
            PaywallCard(onClick = callbacks.onPaywall, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(14.dp))
            SettingsCard(
                state = state,
                callbacks = callbacks,
                onSignOut = onSignOut,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(20.dp))
        }

        IlBottomNav(active = IlBottomTab.Me, onNav = callbacks.onTab)
    }
}

/** Header dán: avatar 72px + tên/pháp danh + nút pill "Sửa hồ sơ". Hairline `ink-06`. */
@Composable
private fun ProfileHeader(
    user: ProfileUser,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth().background(colors.canvas)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Avatar: nền gradient sunk2→tint, viền vàng, chữ cái Lora.
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                        fontSize = 26.sp,
                        color = colors.link,
                    ),
                )
            }
            // Tên + "Pháp danh Tâm Minh · Phật tử tại gia".
            Column(Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        color = colors.ink,
                    ),
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.ink.copy(alpha = 0.5f))) { append("Pháp danh ") }
                        withStyle(SpanStyle(color = colors.link)) { append(user.dharmaName) }
                        withStyle(SpanStyle(color = colors.ink.copy(alpha = 0.5f))) { append(" · ${user.role}") }
                    },
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(12.5f, colors.ink),
                )
            }
            // Nút pill "Sửa hồ sơ".
            Text(
                text = "Sửa hồ sơ",
                modifier = Modifier
                    .clip(RoundedCornerShape(IlRadius.Pill))
                    .border(1.dp, colors.ink.copy(alpha = 0.16f), RoundedCornerShape(IlRadius.Pill))
                    .tappable(onClick = onEdit)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = sans(11.5f, colors.ink, FontWeight.Medium),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
    }
}

/** Ba cột số liệu (giờ tu tập / ngày liên tục / câu niệm Phật) với đường chia. */
@Composable
private fun StatsRow(
    stats: List<PracticeStat>,
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
            .padding(vertical = 18.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(colors.ink.copy(alpha = 0.09f)),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stat.value, style = sans(22f, colors.ink, FontWeight.Medium))
                Text(
                    text = stat.label,
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(11f, colors.ink.copy(alpha = 0.5f)),
                )
            }
        }
    }
}

/** Khối "Nhật ký 12 tuần qua": tiêu đề + nhãn + lưới nhiệt 21×4. */
@Composable
private fun JournalCard(
    label: String,
    heat: List<Float>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Nhật ký 12 tuần qua", style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(label, style = sans(11.5f, colors.ink.copy(alpha = 0.45f)))
        }
        Column(
            modifier = Modifier.padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            heat.chunked(HEAT_COLUMNS).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { opacity ->
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(IlGold.copy(alpha = opacity.coerceIn(0f, 1f))),
                        )
                    }
                }
            }
        }
    }
}

/** Tiêu đề "Dấu ấn tu tập" + link "Xem cả 18 dấu ấn" + 3 thẻ dấu ấn. */
@Composable
private fun BadgesSection(
    badges: List<ProfileBadge>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Dấu ấn tu tập", style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(
                text = "Xem cả 18 dấu ấn",
                modifier = Modifier.tappable(onClick = onSeeAll),
                style = sans(11.5f, colors.link),
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            badges.forEach { badge ->
                BadgeCard(badge = badge, onClick = onSeeAll, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Thẻ dấu ấn: vòng tròn tông màu + hình giọt + tên + phụ đề. */
@Composable
private fun BadgeCard(
    badge: ProfileBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .tappable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(badge.tone.tint(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(12.dp).background(badge.tone.color(), LeafShape))
        }
        Text(
            text = badge.name,
            modifier = Modifier.padding(top = 8.dp),
            style = sans(11.5f, colors.ink, FontWeight.Medium),
        )
        Text(
            text = badge.sub,
            modifier = Modifier.padding(top = 2.dp),
            style = sans(10f, colors.ink.copy(alpha = 0.45f)),
        )
    }
}

/** Thẻ nổi bật "Công đức đã hồi hướng" (nền gradient feat + ngọn lửa). */
@Composable
private fun MeritCard(
    meritLine: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .tappable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Vòng tròn ngọn lửa: viền `featAcc2`, ngọn lửa vàng có quầng sáng.
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, colors.featAcc2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = 11.dp, height = 15.dp)
                    .shadow(6.dp, FlameShape, ambientColor = colors.featFlameGlow, spotColor = colors.featFlameGlow)
                    .background(IlGoldBright, FlameShape),
            )
        }
        Column(Modifier.weight(1f)) {
            Text("Công đức đã hồi hướng", style = sans(13.5f, colors.featTitle, FontWeight.Medium))
            Text(
                text = meritLine,
                modifier = Modifier.padding(top = 3.dp),
                style = sans(11.5f, colors.featSub),
            )
        }
        Text("›", style = sans(18f, colors.featSub))
    }
}

/** Thẻ "Sen Vàng · mở trọn thư viện" (viền vàng, dẫn tới `paywall`). */
@Composable
private fun PaywallCard(
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
            .border(1.dp, IlGold.copy(alpha = 0.45f), shape)
            .tappable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(IlGold.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(14.dp).background(IlGold, LeafShape))
        }
        Column(Modifier.weight(1f)) {
            Text("Sen Vàng · mở trọn thư viện", style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(
                text = "Dùng thử 7 ngày miễn phí",
                modifier = Modifier.padding(top = 3.dp),
                style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            )
        }
        Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
    }
}

/** Nhóm cài đặt: chuông tỉnh thức / cài đặt / thông báo / chùa của tôi. */
@Composable
private fun SettingsCard(
    state: ProfileUiState,
    callbacks: ProfileCallbacks,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
    ) {
        IlSettingRow(
            title = "Chuông tỉnh thức",
            kind = IlSettingKind.Value,
            value = state.reminderSummary,
            onClick = callbacks.onRemind,
        )
        IlSettingRow(title = "Cài đặt", kind = IlSettingKind.Chevron, onClick = callbacks.onSettings)
        IlSettingRow(title = "Thông báo", kind = IlSettingKind.Chevron, onClick = callbacks.onNotifications)
        IlSettingRow(
            title = "Chùa của tôi",
            kind = IlSettingKind.Value,
            value = state.templeName,
            onClick = callbacks.onTemple,
        )
        IlSettingRow(title = "Đăng xuất", kind = IlSettingKind.Chevron, last = true, onClick = onSignOut)
    }
}

/** Màu chính (hình giọt) của dấu ấn. */
private fun BadgeTone.color(): Color = when (this) {
    BadgeTone.Gold -> IlGold
    BadgeTone.Green -> IlGreen
    BadgeTone.Rose -> IlRose
}

/** Nền tint của dấu ấn (khớp prototype `badges[].bg`). */
private fun BadgeTone.tint(): Color = when (this) {
    BadgeTone.Gold -> IlGold.copy(alpha = 0.16f)
    BadgeTone.Green -> IlGreen.copy(alpha = 0.18f)
    BadgeTone.Rose -> IlRose.copy(alpha = 0.20f)
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
private fun ProfilePreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        ProfileContent(state = previewState(), callbacks = ProfileCallbacks())
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun ProfilePreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        ProfileContent(state = previewState(), callbacks = ProfileCallbacks())
    }
}

private fun previewState(): ProfileUiState = ProfileUiState(
    user = ProfileSampleData.user,
    stats = ProfileSampleData.stats,
    journalLoggedDays = ProfileSampleData.JOURNAL_LOGGED_DAYS,
    journalTotalDays = ProfileSampleData.JOURNAL_TOTAL_DAYS,
    heat = ProfileSampleData.heat,
    badges = ProfileSampleData.badges,
    merit = ProfileSampleData.merit,
    reminderSummary = ProfileSampleData.REMINDER_SUMMARY,
    templeName = ProfileSampleData.TEMPLE_NAME,
)

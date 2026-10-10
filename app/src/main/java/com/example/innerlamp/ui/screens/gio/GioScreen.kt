package com.example.innerlamp.ui.screens.gio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.ui.components.IlAvatar
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

private const val FlashDurationMillis = 3600L
private val Gutter = 20.dp

// Ô "譜" (phổ) của hàng "Lấy từ gia phả": đỏ son + chữ giấy — lấy từ prototype.
private val GiaPhaTileBg = Color(0xFF9E3B2C)
private val GiaPhaTileFg = Color(0xFFF4EBD6)

/**
 * Các điểm điều hướng đi ra khỏi màn "Sổ giỗ chạp". Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 *
 * @property onEdit mở form Thêm/sửa — màn đã gọi `startNew()`/`startEdit()` trước đó.
 * @property onGiaPha mở Gia phả để chọn người đã khuất (chưa có màn Gia phả).
 */
@Immutable
class GioCallbacks(
    val onBack: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onGiaPha: () -> Unit = {},
)

/**
 * Màn "Sổ giỗ chạp" — dựng lại từ prototype `Docs/innerlamp-project/` (màn `gio`, `gioEmpty`).
 *
 * Bố cục: header → (thông báo xanh) → thẻ nổi bật kỳ giỗ gần nhất → danh sách "Theo ngày
 * giỗ sắp tới"; sổ trống thì hiện minh họa + 3 lợi ích + hàng "Lấy từ gia phả". Nút
 * "Thêm người thân" dính đáy.
 */
@Composable
fun GioScreen(
    viewModel: GioViewModel,
    modifier: Modifier = Modifier,
    callbacks: GioCallbacks = GioCallbacks(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.flash) {
        if (state.flash.isNotEmpty()) {
            delay(FlashDurationMillis)
            viewModel.dismissFlash()
        }
    }

    GioContent(
        state = state,
        callbacks = callbacks,
        onAdd = {
            viewModel.startNew()
            callbacks.onEdit()
        },
        onOpen = { id ->
            viewModel.startEdit(id)
            callbacks.onEdit()
        },
        modifier = modifier,
    )
}

/** Phần stateless của [GioScreen] — dùng cho preview và test. */
@Composable
internal fun GioContent(
    state: GioUiState,
    callbacks: GioCallbacks,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier.fillMaxSize().background(colors.canvas)) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Gutter, vertical = 6.dp)) {
            IlNavBar(title = "Sổ giỗ chạp", sub = state.subtitle, showBack = true, onBack = callbacks.onBack)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = Gutter, end = Gutter, bottom = 20.dp),
        ) {
            if (state.flash.isNotEmpty()) {
                FlashBanner(state.flash, Modifier.padding(top = 14.dp))
            }
            val next = state.nextUp
            if (next == null) {
                EmptyBook(onGiaPha = callbacks.onGiaPha)
            } else {
                FeaturedCard(item = next, onClick = { onOpen(next.entry.id) }, modifier = Modifier.padding(top = 18.dp))
                Text(
                    text = "THEO NGÀY GIỖ SẮP TỚI",
                    modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
                    style = sans(10f, colors.ink.copy(alpha = 0.42f), FontWeight.SemiBold, letterSpacing = 1.4.sp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.items.forEach { item ->
                        MemorialRow(item = item, onClick = { onOpen(item.entry.id) })
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.canvas)
                .navigationBarsPadding(),
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
            IlButton(
                label = "Thêm người thân",
                onClick = onAdd,
                modifier = Modifier.padding(start = Gutter, end = Gutter, top = 12.dp, bottom = 16.dp),
            )
        }
    }
}

/** Thông báo xanh sau khi lưu/xóa: chấm tròn xanh + nội dung. */
@Composable
private fun FlashBanner(message: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IlGreen.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(18.dp).background(IlGreenText, CircleShape), contentAlignment = Alignment.Center) {
            Text("✓", style = sans(10f, Color.White, FontWeight.SemiBold))
        }
        Text(message, modifier = Modifier.weight(1f), style = sans(12.5f, colors.ink, lineHeight = 19.sp))
    }
}

/** Thẻ nổi bật cho kỳ giỗ gần nhất (nền gradient `feat`). */
@Composable
private fun FeaturedCard(item: GioItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val entry = item.entry
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .tappable(onClick = onClick)
            .padding(20.dp),
    ) {
        Text(
            text = GioFormat.featuredTag(item.occurrence.daysUntil),
            style = sans(9.5f, colors.featKicker, FontWeight.SemiBold, letterSpacing = 1.5.sp),
        )
        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IlAvatar(initials = initialsOf(entry.name), size = 64.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = TextStyle(fontFamily = Lora, fontSize = 22.sp, lineHeight = 27.sp, color = colors.featTitle),
                )
                val ordinal = GioFormat.ordinalLabel(item.occurrence.ordinal)
                Text(
                    text = entry.relation.label + if (ordinal.isEmpty()) "" else " · $ordinal",
                    modifier = Modifier.padding(top = 4.dp),
                    style = sans(12f, colors.featSub),
                )
            }
        }
        Box(Modifier.padding(top = 16.dp, bottom = 12.dp).fillMaxWidth().height(1.dp).background(colors.featBorder))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            FeaturedFact("Ngày giỗ", GioFormat.dateLabel(entry))
            FeaturedFact("Nhằm", GioFormat.fullDate(item.occurrence.date))
            FeaturedFact("Nhắc nhở", GioFormat.reminderLabel(entry))
        }
    }
}

@Composable
private fun FeaturedFact(label: String, value: String) {
    val colors = LocalIlColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, modifier = Modifier.width(72.dp), style = sans(12f, colors.featSub, lineHeight = 18.sp))
        Text(value, modifier = Modifier.weight(1f), style = sans(12f, colors.featTitle, lineHeight = 18.sp))
    }
}

/** Một hàng trong danh sách "Theo ngày giỗ sắp tới". */
@Composable
private fun MemorialRow(item: GioItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val entry = item.entry
    val days = item.occurrence.daysUntil
    val shape = RoundedCornerShape(IlRadius.Row)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IlAvatar(initials = initialsOf(entry.name), size = 46.dp)
        Column(Modifier.weight(1f)) {
            Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = sans(14f, colors.ink, FontWeight.Medium))
            Text(
                text = "${entry.relation.label} · ${GioFormat.dateLabel(entry)}",
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = GioFormat.daysLabel(days),
                style = sans(11.5f, if (days <= 7) colors.link else colors.ink.copy(alpha = 0.42f), FontWeight.Medium),
            )
            if (entry.remind) {
                Text("Có nhắc", style = sans(10.5f, colors.ink.copy(alpha = 0.42f)))
            }
        }
    }
}

/** Trạng thái sổ trống: ngọn đèn, tiêu đề, 3 lợi ích và lối tắt "Lấy từ gia phả". */
@Composable
private fun EmptyBook(onGiaPha: () -> Unit) {
    val colors = LocalIlColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 44.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LampIllustration()
        Text(
            text = "Sổ giỗ còn trống",
            modifier = Modifier.padding(top = 18.dp),
            style = TextStyle(fontFamily = Lora, fontSize = 24.sp, lineHeight = 31.sp, color = colors.ink),
        )
        Text(
            text = "Ghi lại ngày mất của ông bà, cha mẹ để không lỡ ngày giỗ nào.",
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 300.dp),
            textAlign = TextAlign.Center,
            style = sans(13f, colors.ink.copy(alpha = 0.52f), lineHeight = 21.sp),
        )
    }

    val cardShape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = Modifier
            .padding(top = 26.dp)
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), cardShape)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        EmptyBenefits.forEachIndexed { index, (title, body) ->
            Row(
                modifier = Modifier.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(26.dp).background(IlGold.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${index + 1}", style = sans(11.5f, IlGold, FontWeight.SemiBold))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = sans(13.5f, colors.ink, FontWeight.Medium))
                    Text(body, modifier = Modifier.padding(top = 3.dp), style = sans(12f, colors.ink.copy(alpha = 0.5f), lineHeight = 18.sp))
                }
            }
            if (index < EmptyBenefits.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), cardShape)
            .tappable(onClick = onGiaPha)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(38.dp).background(GiaPhaTileBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("譜", style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GiaPhaTileFg))
        }
        Column(Modifier.weight(1f)) {
            Text("Lấy từ gia phả", style = sans(13.5f, colors.ink, FontWeight.Medium))
            Text(
                text = "Chọn người đã khuất trong cây gia phả để thêm nhanh",
                modifier = Modifier.padding(top = 2.dp),
                style = sans(11.5f, colors.ink.copy(alpha = 0.5f)),
            )
        }
        Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
    }
}

private val EmptyBenefits = listOf(
    "Tự đổi âm sang dương" to "Nhập ngày giỗ theo âm lịch, mỗi năm app tự tính ngày dương tương ứng.",
    "Nhắc trước để chuẩn bị" to "Chọn nhắc trước 1–7 ngày, kèm giờ nhắc phù hợp với gia đình.",
    "Đếm giỗ đầu, giỗ hết" to "Ghi năm mất để biết đây là giỗ lần thứ mấy.",
)

/** Ngọn đèn nhỏ trong vầng sáng vàng (hình minh họa của trạng thái trống). */
@Composable
private fun LampIllustration() {
    Box(
        modifier = Modifier
            .size(112.dp)
            .background(
                Brush.radialGradient(
                    0f to IlGoldBright.copy(alpha = 0.20f),
                    0.62f to IlGoldBright.copy(alpha = 0.05f),
                    0.72f to Color.Transparent,
                ),
                CircleShape,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Ngọn lửa.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 22.dp)
                .size(width = 14.dp, height = 20.dp)
                .background(
                    Brush.verticalGradient(listOf(IlGoldBright2, IlGoldBright)),
                    RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
                ),
        )
        // Bấc đèn.
        Box(
            Modifier
                .padding(bottom = 36.dp)
                .size(width = 4.dp, height = 22.dp)
                .background(Color(0xFF8A6A45), RoundedCornerShape(2.dp)),
        )
        // Đế đèn.
        Box(
            Modifier
                .padding(bottom = 22.dp)
                .size(width = 56.dp, height = 16.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFC99A52), Color(0xFF8A6A45))),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                ),
        )
    }
}

/** Chữ cái đầu của họ và tên: "Trần Thị Mai" → "TM". */
internal fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when (words.size) {
        0 -> "?"
        1 -> words[0].take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

internal fun sans(
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

private val PreviewToday = LocalDate.of(2026, 10, 9)

private fun previewItem(
    id: String,
    name: String,
    relation: MemorialRelation,
    calendar: MemorialCalendarType,
    day: Int,
    month: Int,
    year: Int,
): GioItem {
    val entry = MemorialEntry(
        id, name, relation, calendar, day, month, year, note = "",
        remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0), syncReminders = false,
    )
    return GioItem(entry, MemorialSchedule.next(entry, PreviewToday))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun GioContentPreview() {
    InnerLampTheme {
        GioContent(
            state = GioUiState(
                items = listOf(
                    previewItem("1", "Trần Thị Mai", MemorialRelation.MaternalGrandmother, MemorialCalendarType.Lunar, 23, 8, 2015),
                    previewItem("2", "Nguyễn Văn Minh", MemorialRelation.Father, MemorialCalendarType.Solar, 14, 12, 2019),
                ).sortedBy { it.occurrence.daysUntil },
                flash = "Đã lưu vào sổ giỗ.",
            ),
            callbacks = GioCallbacks(),
            onAdd = {},
            onOpen = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun GioEmptyPreview() {
    InnerLampTheme {
        GioContent(state = GioUiState(), callbacks = GioCallbacks(), onAdd = {}, onOpen = {})
    }
}

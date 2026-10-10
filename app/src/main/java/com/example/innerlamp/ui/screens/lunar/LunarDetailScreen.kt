package com.example.innerlamp.ui.screens.lunar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.domain.model.AuspiciousHour
import com.example.innerlamp.domain.model.ClashAge
import com.example.innerlamp.domain.model.LunarDayDetail
import com.example.innerlamp.domain.model.LunarDayKind
import com.example.innerlamp.domain.model.TravelDirection
import com.example.innerlamp.domain.model.Verse
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.weekdayFull
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate

/** Ngưỡng cuộn (px của prototype) để header chuyển sang nền đặc. */
private val HeaderSolidThreshold = 110.dp
private val HeroHeight = 380.dp

private val GoodDot = Color(0xFF5E8A6C)
private val BadDot = Color(0xFFC64545)
private val SpecialDot = Color(0xFFD9963C)

/** Callback của màn Chi tiết ngày âm. */
class LunarDetailCallbacks(
    val onBack: () -> Unit = {},
)

/**
 * Màn Chi tiết ngày âm — dựng từ prototype (màn `lunar`): bìa ngày âm lớn, bảng can chi,
 * câu kinh, giờ hoàng đạo, tuổi xung khắc, hướng xuất hành. Ảnh bìa (`lunar-hero`) chưa có
 * asset nên dùng gradient `thBg` của theme.
 */
@Composable
fun LunarDetailScreen(
    date: LocalDate,
    modifier: Modifier = Modifier,
    callbacks: LunarDetailCallbacks = LunarDetailCallbacks(),
    viewModel: LunarDetailViewModel = viewModel(
        key = "lunar-$date",
        factory = viewModelFactory {
            initializer { LunarDetailViewModel(date) }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LunarDetailContent(state = state, callbacks = callbacks, modifier = modifier)
}

/** Phần stateless của [LunarDetailScreen] — dùng cho preview và test. */
@Composable
internal fun LunarDetailContent(
    state: LunarDetailUiState,
    callbacks: LunarDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    val detail = state.detail

    // Header trong suốt đè lên bìa; cuộn quá bìa thì blur dần (nội dung chui xuống dưới).
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(title = "", dark = colors.isDark, onBack = callbacks.onBack)
            }
        },
        modifier = modifier.background(colors.canvas),
    ) { _ ->
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Hero(detail)
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 36.dp)) {
                InfoCard(detail, Modifier.padding(top = 6.dp))
                Text(detail.advice, style = sans(12.5f, colors.ink.copy(alpha = 0.5f), lineHeight = 19.sp), modifier = Modifier.padding(top = 12.dp))
                VerseCard(state.verse, Modifier.padding(top = 22.dp))
                SectionTitle("Giờ hoàng đạo")
                HoursGrid(detail.hours)
                SectionTitle("Tuổi xung khắc")
                ClashCard(detail.clashAges, detail.clashNote)
                SectionTitle("Hướng xuất hành")
                DirectionsRow(detail.directions)
            }
        }
    }
}

@Composable
private fun Hero(detail: LunarDayDetail) {
    val colors = LocalIlColors.current
    val lunar = detail.lunar
    Box(
        Modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .background(Brush.verticalGradient(listOf(colors.thBgTop, colors.thBgMid, colors.canvas))),
    ) {
        Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 14.dp)) {
            Text("ÂM LỊCH", style = sans(9.5f, colors.featKicker, FontWeight.SemiBold, letterSpacing = 1.7f.sp))
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("%02d".format(lunar.day), style = serif(72f, colors.featTitle, lineHeight = 65.sp))
                Column(Modifier.padding(bottom = 4.dp)) {
                    Text(
                        "Tháng %02d".format(lunar.month) + if (lunar.isLeapMonth) " (nhuận)" else "",
                        style = serif(18f, colors.featTitle.copy(alpha = 0.85f), lineHeight = 23.sp),
                    )
                    Text("Năm ${lunar.canChiYear}", style = sans(13f, colors.featSub), modifier = Modifier.padding(top = 2.dp))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val d = detail.date
                Text(
                    "${d.weekdayFull()}, %02d/%02d/%d".format(d.dayOfMonth, d.monthValue, d.year),
                    style = sans(13f, colors.featTitle, FontWeight.Medium),
                )
                KindChip(detail.kind)
            }
        }
    }
}

@Composable
private fun KindChip(kind: LunarDayKind) {
    val colors = LocalIlColors.current
    val (dot, label) = when (kind) {
        LunarDayKind.Full -> SpecialDot to "Ngày Rằm"
        LunarDayKind.New -> SpecialDot to "Mùng Một"
        LunarDayKind.Good -> GoodDot to "Ngày hoàng đạo"
        LunarDayKind.Bad -> BadDot to "Ngày hắc đạo"
    }
    Row(
        Modifier
            .clip(CircleShape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.size(7.dp).background(dot, CircleShape))
        Text(label, style = sans(11.5f, colors.ink, FontWeight.Medium))
    }
}

@Composable
private fun InfoCard(detail: LunarDayDetail, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    val rows = listOf(
        "Ngày" to detail.dayCanChi,
        "Tháng" to detail.monthCanChi,
        "Năm" to detail.yearCanChi,
        "Dương lịch" to "%02d/%02d/%d".format(detail.date.dayOfMonth, detail.date.monthValue, detail.date.year),
    )
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .padding(horizontal = 18.dp, vertical = 4.dp),
    ) {
        rows.forEach { (k, v) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(k, style = sans(12.5f, colors.featSub))
                Text(v, style = serif(16f, colors.featTitle), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun VerseCard(verse: Verse, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        verse.lines.forEach {
            Text(it, style = serif(17f, colors.featTitle, lineHeight = 26.sp), textAlign = TextAlign.Center)
        }
        Text(
            verse.source,
            modifier = Modifier.padding(top = 8.dp),
            style = serif(12f, colors.featSub, italic = true),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = LocalIlColors.current
    Text(text, style = sans(13.5f, colors.ink, FontWeight.Medium), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
}

@Composable
private fun HoursGrid(hours: List<AuspiciousHour>) {
    val colors = LocalIlColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        hours.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { h ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.card)
                            .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(h.animal, style = TextStyle(fontSize = 22.sp, color = IlGoldBright))
                        Text(h.chi, style = serif(17f, colors.ink), modifier = Modifier.padding(top = 8.dp))
                        Text(h.range, style = sans(11f, colors.ink.copy(alpha = 0.5f)), modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClashCard(ages: List<ClashAge>, note: String) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(16.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ages.forEach { age ->
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(BadDot.copy(alpha = 0.08f))
                        .border(1.dp, BadDot.copy(alpha = 0.24f), CircleShape)
                        .padding(start = 10.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(age.animal, style = TextStyle(fontSize = 17.sp))
                    Text(age.name, style = sans(12.5f, BadDot, FontWeight.Medium))
                }
            }
        }
        Text(note, style = sans(12f, colors.ink.copy(alpha = 0.5f), lineHeight = 19.sp), modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun DirectionsRow(directions: List<TravelDirection>) {
    val colors = LocalIlColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        directions.forEach { d ->
            val tone = when (d.role) {
                "Hỷ thần" -> BadDot
                "Tài thần" -> colors.featKicker
                else -> BadDot
            }
            val tileBg = if (d.role == "Tài thần") colors.featGlow else BadDot.copy(alpha = 0.08f)
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(tileBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(d.glyph, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = tone))
                }
                Text(d.role, style = sans(12.5f, colors.ink, FontWeight.Medium), modifier = Modifier.padding(top = 8.dp))
                Text(d.direction, style = serif(17f, colors.ink), modifier = Modifier.padding(top = 10.dp))
                Text(d.meaning, style = sans(11f, colors.ink.copy(alpha = 0.5f), lineHeight = 16.5f.sp), modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

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

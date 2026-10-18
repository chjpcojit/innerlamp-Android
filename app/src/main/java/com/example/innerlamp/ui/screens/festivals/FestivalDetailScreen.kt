package com.example.innerlamp.ui.screens.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.CalendarModule
import com.example.innerlamp.domain.usecase.EventOccurrenceDetail
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlState
import com.example.innerlamp.ui.components.IlStateKind
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// "Chi tiết ngày lễ" (prototype `festDetail`): image, group + major mark, name, lunar date,
// description paragraphs, solar date card + countdown, numbered practices. No sutras / temples /
// reminder / previous-next (AC17).
// Chi tiết ngày lễ: ảnh, nhóm + dấu lễ lớn, tên, ngày âm, mô tả, thẻ ngày dương + đếm ngược, việc nên làm.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FestivalDetailScreen(
    eventCode: String,
    solarDate: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FestivalDetailViewModel = viewModel(
        factory = CalendarModule.festivalDetailViewModelFactory(eventCode, solarDate),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    FestivalDetailContent(state = state, onBack = onBack, onRetry = viewModel::onRetry, modifier = modifier)
}

/** Stateless part — for previews and tests. / Phần stateless. */
@Composable
internal fun FestivalDetailContent(
    state: FestivalDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier.fillMaxSize().background(colors.canvas)) {
        Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 6.dp)) {
            IlNavBar(title = "Chi tiết ngày lễ", showBack = true, onBack = onBack)
        }
        when (state) {
            FestivalDetailUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.link, strokeWidth = 2.dp)
            }
            FestivalDetailUiState.NotFound -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    IlState(title = CalendarText.NOT_FOUND, kind = IlStateKind.Empty)
                    IlButton(label = "Quay lại", variant = IlButtonVariant.Ghost, full = false, onClick = onBack)
                }
            }
            is FestivalDetailUiState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CalendarErrorState(error = state.error, onRetry = onRetry)
            }
            is FestivalDetailUiState.Found -> FoundBody(state, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FoundBody(state: FestivalDetailUiState.Found, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val event = state.detail.entry.event
    val occurrence = state.detail.entry.occurrence
    val date = occurrence.solarDate
    val kinds = state.groupNames.joinToString(" · ").uppercase()

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        // Image with a placeholder behind it: a missing / failed image keeps the placeholder (AC16).
        // Ảnh có nền placeholder phía sau: không có ảnh / lỗi ảnh thì vẫn thấy placeholder.
        Box(
            Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.sunk2),
        ) {
            event.imageUrl?.takeIf { it.isNotBlank() }?.let {
                AsyncImage(
                    model = it,
                    contentDescription = event.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(
            Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (kinds.isNotEmpty()) Text(kinds, style = sans(10.5f, colors.link, FontWeight.SemiBold))
            if (event.isMajor) MajorMark()
        }
        Text(event.name, modifier = Modifier.padding(top = 10.dp), style = serif(30f, colors.ink))
        Text(
            text = CalendarText.lunarLabel(occurrence.lunar),
            modifier = Modifier.padding(top = 10.dp),
            style = sans(14f, colors.link, FontWeight.Medium),
        )

        if (state.paragraphs.isNotEmpty()) {
            Column(Modifier.padding(top = 20.dp).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.08f)))
                state.paragraphs.forEach { p ->
                    Text(p, modifier = Modifier.padding(top = 14.dp), style = sans(15f, colors.ink.copy(alpha = 0.72f), lineHeight = 26f))
                }
            }
        }

        DateCard(state = state, date = date, modifier = Modifier.padding(top = 26.dp))

        if (state.practices.isNotEmpty()) {
            Text(
                "PHẬT TỬ THƯỜNG LÀM GÌ",
                modifier = Modifier.padding(top = 26.dp),
                style = sans(10.5f, colors.ink.copy(alpha = 0.42f), FontWeight.SemiBold),
            )
            Column(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp),
            ) {
                state.practices.forEachIndexed { i, text ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.07f)))
                    Row(
                        Modifier.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            Modifier.size(24.dp).border(1.dp, colors.link.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", style = sans(12f, colors.link, FontWeight.Medium))
                        }
                        Text(text, modifier = Modifier.weight(1f), style = sans(14f, colors.ink, lineHeight = 21f))
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding().height(34.dp))
    }
}

@Composable
private fun DateCard(state: FestivalDetailUiState.Found, date: LocalDate, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("%02d".format(date.dayOfMonth), modifier = Modifier.width(58.dp), textAlign = TextAlign.Center, style = serif(34f, colors.link))
        Column(Modifier.weight(1f)) {
            Text(
                "${CalendarText.weekdayLong(date)}, Tháng ${date.monthValue}, ${date.year}",
                style = sans(13f, colors.ink, FontWeight.Medium),
            )
            Text("Dương lịch", modifier = Modifier.padding(top = 4.dp), style = sans(12f, colors.ink.copy(alpha = 0.50f)))
        }
        Text(
            text = CalendarText.countdown(state.countdown.label),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.link.copy(alpha = 0.14f))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            style = sans(12f, colors.link, FontWeight.Medium),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FestivalDetailPreview() {
    InnerLampTheme {
        val entry = CalendarPreviewData.year2026.entries.first { it.event.code == "vu_lan" }
        FestivalDetailContent(
            state = FestivalDetailUiState.Found(
                EventOccurrenceDetail(entry, CalendarPreviewData.year2026.groups.take(1)),
                today = LocalDate.of(2026, 10, 17),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}

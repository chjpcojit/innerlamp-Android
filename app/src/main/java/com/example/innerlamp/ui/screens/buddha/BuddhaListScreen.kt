package com.example.innerlamp.ui.screens.buddha

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.BuddhaCategory
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.usecase.GetBuddhaProfilesUseCase
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Chư Phật & Bồ Tát" (`buddhas`) — dựng lại từ prototype
// `Docs/innerlamp-project/` (màn `buddhas`): header dán + chip lọc, lưới 2 cột thẻ.
// ─────────────────────────────────────────────────────────────────────────────

/** Chiều cao vùng ảnh của thẻ (prototype `height:170px`). */
private val CardImageHeight = 170.dp

/** Nền pill "Phật / Bồ Tát" trên ảnh và màu chữ — cố định, không đổi theo theme. */
private val KindPillBg = Color(0x99140F0D)
private val KindPillInk = Color(0xFFF7F1E4)

/**
 * Các điểm điều hướng đi ra khỏi màn "Chư Phật & Bồ Tát". Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback.
 */
@Immutable
class BuddhaListCallbacks(
    val onBack: () -> Unit = {},
    val onBuddha: (buddhaId: String) -> Unit = {},
)

/** Màn danh sách chư Phật & Bồ Tát. */
@Composable
fun BuddhaListScreen(
    modifier: Modifier = Modifier,
    callbacks: BuddhaListCallbacks = BuddhaListCallbacks(),
    viewModel: BuddhaListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BuddhaListContent(
        state = state,
        callbacks = callbacks,
        onCategorySelected = viewModel::onCategorySelected,
        modifier = modifier,
    )
}

/** Phần stateless của [BuddhaListScreen] — dùng cho preview và test. */
@Composable
internal fun BuddhaListContent(
    state: BuddhaListUiState,
    callbacks: BuddhaListCallbacks,
    onCategorySelected: (BuddhaCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    // Header (nav + chip lọc) đè lên danh sách, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            Header(
                subtitle = state.subtitle,
                selected = state.category,
                onBack = callbacks.onBack,
                onSelect = onCategorySelected,
            )
        },
        modifier = modifier,
    ) { headerHeight ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(headerHeight + 16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Lưới 2 cột: 9 phần tử nên dựng bằng hàng 2 ô thay vì LazyGrid lồng trong cuộn dọc.
                state.filtered.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { profile ->
                            BuddhaCard(
                                profile = profile,
                                onClick = { callbacks.onBuddha(profile.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding().height(34.dp))
        }
    }
}

/** Nội dung header: `IlNavBar` + dãy chip lọc (blur/hairline do [IlBlurHeaderScaffold] lo). */
@Composable
private fun Header(
    subtitle: String,
    selected: BuddhaCategory?,
    onBack: () -> Unit,
    onSelect: (BuddhaCategory?) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)) {
            IlNavBar(title = "Chư Phật & Bồ Tát", sub = subtitle, showBack = true, onBack = onBack)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IlChip("Tất cả", selected = selected == null, onClick = { onSelect(null) })
            IlChip("Chư Phật", selected = selected == BuddhaCategory.Buddha, onClick = { onSelect(BuddhaCategory.Buddha) })
            IlChip("Bồ Tát", selected = selected == BuddhaCategory.Bodhisattva, onClick = { onSelect(BuddhaCategory.Bodhisattva) })
        }
    }
}

@Composable
private fun BuddhaCard(profile: BuddhaProfile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClickLabel = profile.name, onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(CardImageHeight)) {
            // TODO(il-data-builder): ảnh tôn tượng thật (prototype `image-slot`); hiện là nền gradient.
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint))))
            Text(
                text = profile.kindLabel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(KindPillBg, RoundedCornerShape(IlRadius.Pill))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                style = sans(10f, KindPillInk, FontWeight.Medium),
            )
        }
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
            Text(profile.name, style = serif(15f, colors.ink, lineHeight = 19.5f.sp))
            Text(
                text = profile.role,
                modifier = Modifier.padding(top = 4.dp),
                style = sans(11f, colors.ink.copy(alpha = 0.50f), lineHeight = 16.sp),
            )
            Text(
                text = "Vía ${profile.viaLabel} âm lịch",
                modifier = Modifier.padding(top = 8.dp),
                style = sans(10.5f, colors.link, FontWeight.Medium),
            )
        }
    }
}

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

private fun serif(size: Float, color: Color, lineHeight: TextUnit = TextUnit.Unspecified) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight,
    color = color,
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BuddhaListPreview() {
    InnerLampTheme {
        BuddhaListContent(
            state = BuddhaListUiState(profiles = GetBuddhaProfilesUseCase()()),
            callbacks = BuddhaListCallbacks(),
            onCategorySelected = {},
        )
    }
}

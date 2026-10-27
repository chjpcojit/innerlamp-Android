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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.DeityModule
import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.IlContentError
import com.example.innerlamp.ui.components.IlContentLoading
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
    viewModel: BuddhaListViewModel = viewModel(factory = DeityModule.buddhaListViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BuddhaListContent(
        state = state,
        callbacks = callbacks,
        onTypeSelected = viewModel::onTypeSelected,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

/** Phần stateless của [BuddhaListScreen] — dùng cho preview và test. */
@Composable
internal fun BuddhaListContent(
    state: BuddhaListUiState,
    callbacks: BuddhaListCallbacks,
    onTypeSelected: (DeityType?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    // Header (nav + chip lọc) đè lên danh sách, blur dần khi cuộn.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            Header(
                subtitle = state.subtitle,
                selected = state.type,
                onBack = callbacks.onBack,
                onSelect = onTypeSelected,
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
            when (state.status) {
                ContentStatus.Loading -> IlContentLoading()
                ContentStatus.Loaded -> Unit
                else -> IlContentError(status = state.status, onRetry = onRetry)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Lưới 2 cột: ~20 phần tử nên dựng bằng hàng 2 ô thay vì LazyGrid lồng trong cuộn dọc.
                state.filtered.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { deity ->
                            BuddhaCard(
                                deity = deity,
                                onClick = { callbacks.onBuddha(deity.id) },
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
    selected: DeityType?,
    onBack: () -> Unit,
    onSelect: (DeityType?) -> Unit,
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
            IlChip("Chư Phật", selected = selected == DeityType.Buddha, onClick = { onSelect(DeityType.Buddha) })
            IlChip("Bồ Tát", selected = selected == DeityType.Bodhisattva, onClick = { onSelect(DeityType.Bodhisattva) })
        }
    }
}

@Composable
private fun BuddhaCard(deity: DeitySummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClickLabel = deity.nameVi, onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(CardImageHeight)) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint))))
            deity.thumbnailUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            deity.kindLabel?.let { kind ->
                Text(
                    text = kind,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(KindPillBg, RoundedCornerShape(IlRadius.Pill))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    style = sans(10f, KindPillInk, FontWeight.Medium),
                )
            }
        }
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
            Text(deity.nameVi, style = serif(15f, colors.ink, lineHeight = 19.5f.sp))
            deity.coreVirtue?.let { virtue ->
                Text(
                    text = virtue,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = sans(11f, colors.ink.copy(alpha = 0.50f), lineHeight = 16.sp),
                )
            }
            deity.pureLand?.let { land ->
                Text(
                    text = land,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = sans(10.5f, colors.link, FontWeight.Medium),
                )
            }
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
            state = BuddhaListUiState(
                deities = listOf(
                    DeitySummary(
                        id = "buddha_shakyamuni",
                        type = DeityType.Buddha,
                        nameVi = "Thích Ca Mâu Ni Phật",
                        nameSanskrit = "Śākyamuni Buddha",
                        titleHonorific = "Bổn Sư Thích Ca Mâu Ni Phật",
                        thumbnailUrl = null,
                        imageUrl = null,
                        coreVirtue = "Toàn Giác",
                        pureLand = "Cõi Ta Bà",
                    ),
                ),
                status = ContentStatus.Loaded,
            ),
            callbacks = BuddhaListCallbacks(),
            onTypeSelected = {},
            onRetry = {},
        )
    }
}

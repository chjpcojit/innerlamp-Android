package com.example.innerlamp.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryHub
import com.example.innerlamp.domain.model.Sutra
import com.example.innerlamp.domain.model.SutraTone
import com.example.innerlamp.domain.usecase.GetBuddhaProfilesUseCase
import com.example.innerlamp.domain.usecase.GetLibraryCatalogUseCase
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.IlRoseText
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/** Bán kính thẻ nổi bật (prototype: `border-radius:22px`). */
private val FeatCardRadius = 22.dp

/** Màu chữ của nút "Nghe · ..." đặt trên nền vàng `#D9963C` (giữ cứng, không đổi theme). */
private val OnGoldInk = Color(0xFF1A1611)

/**
 * Các điểm điều hướng đi ra khỏi màn Thư viện. Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback (giống `HomeScreen`); route chưa có thì để mặc định no-op.
 */
@Immutable
class LibraryCallbacks(
    val onSearch: () -> Unit = {},
    val onFeaturedTalk: (talkId: String) -> Unit = {},
    val onSutra: (sutraId: String) -> Unit = {},
    val onHub: (LibraryHub) -> Unit = {},
    val onBuddha: (buddhaId: String) -> Unit = {},
    val onBuddhaList: () -> Unit = {},
    val onSutraList: () -> Unit = {},
    val onTab: (IlBottomTab) -> Unit = {},
)

/**
 * Màn hình Thư viện — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `lib`).
 *
 * Bố cục (trên → dưới): header dán ("Thư viện") → ô tìm kiếm → lưới 4 lối vào 2×2
 * (Kinh sách / Thiền định / Pháp thoại / Phật & Bồ Tát) → thẻ pháp thoại nổi bật →
 * "Chư Phật & Bồ Tát" (cuộn ngang) → "Kinh thường tụng" → bottom nav (tab Thư viện active).
 */
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    callbacks: LibraryCallbacks = LibraryCallbacks(),
    viewModel: LibraryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier,
    )
}

/** Phần stateless của [LibraryScreen] — dùng cho preview và test. */
@Composable
internal fun LibraryContent(
    state: LibraryUiState,
    callbacks: LibraryCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        // Header đè lên nội dung, blur dần khi cuộn.
        IlBlurHeaderScaffold(
            scrollOffset = { scroll.value },
            header = { LibraryHeader() },
            modifier = Modifier.weight(1f),
        ) { headerHeight ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(headerHeight + 14.dp))
                SearchBar(onClick = callbacks.onSearch)

                Spacer(Modifier.height(14.dp))
                HubGrid(hubs = state.hubs, onHub = callbacks.onHub)

                Spacer(Modifier.height(20.dp))
                FeaturedTalkCard(
                    talk = state.featuredTalk,
                    onClick = { callbacks.onFeaturedTalk(state.featuredTalk.id) },
                )

                Spacer(Modifier.height(22.dp))
                SectionHeader("Chư Phật & Bồ Tát", "Xem tất cả", callbacks.onBuddhaList)
                Spacer(Modifier.height(12.dp))
                BuddhaStrip(buddhas = state.buddhas, onBuddha = callbacks.onBuddha)

                Spacer(Modifier.height(22.dp))
                SectionHeader("Kinh thường tụng", "Kinh sách", callbacks.onSutraList)
                Spacer(Modifier.height(12.dp))
                SutraList(sutras = state.sutras, onSutraClick = callbacks.onSutra)
                Spacer(Modifier.height(20.dp))
            }
        }

        IlBottomNav(active = IlBottomTab.Lib, onNav = callbacks.onTab)
    }
}

/** Header của tab gốc: `IlNavBar` "Thư viện" (không back). Blur + hairline do [IlBlurHeaderScaffold] lo. */
@Composable
private fun LibraryHeader(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        IlNavBar(title = "Thư viện", showBack = false)
    }
}

/** Ô tìm kiếm (prototype là nút giả — bấm mở màn `search`). */
@Composable
private fun SearchBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IlRadius.Input))
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.10f), RoundedCornerShape(IlRadius.Input))
            .ilTap(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Kính lúp dạng khối bo tròn (prototype dùng vòng tròn CSS, chưa có icon set).
        Box(
            Modifier
                .size(14.dp)
                .border(1.5.dp, colors.ink.copy(alpha = 0.35f), CircleShape),
        )
        Text(
            text = "Tìm kinh, bài giảng, thiền dẫn…",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                color = colors.ink.copy(alpha = 0.42f),
            ),
        )
    }
}

/** Thẻ pháp thoại nổi bật: nền gradient + quầng sáng + kicker + tiêu đề + nút "Nghe". */
@Composable
private fun FeaturedTalkCard(
    talk: FeaturedTalk,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FeatCardRadius))
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, RoundedCornerShape(FeatCardRadius))
            .ilTap(onClick = onClick),
    ) {
        // Quầng sáng đặt lệch góc phải-dưới, tràn khỏi thẻ (bị clip che phần ngoài).
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 60.dp)
                .size(180.dp)
                .background(
                    Brush.radialGradient(listOf(colors.featGlow, Color.Transparent)),
                    CircleShape,
                ),
        )
        Column(Modifier.padding(22.dp)) {
            Text(
                text = talk.kicker,
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.5.sp,
                    color = colors.featKicker,
                ),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = talk.title,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    color = colors.featTitle,
                ),
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(IlGoldBright)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) {
                Text(
                    text = "Nghe · ${talk.durationMinutes} phút",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = OnGoldInk,
                    ),
                )
            }
        }
    }
}

/** Tiêu đề khối (500 · 13.5px) + link nhấn bên phải (400 · 12px). */
@Composable
private fun SectionHeader(
    title: String,
    linkLabel: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = colors.ink,
            ),
        )
        Text(
            text = linkLabel,
            modifier = Modifier.ilTap(onClick = onLinkClick),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = colors.link,
            ),
        )
    }
}

/** Lưới 2×2 bốn lối vào: chấm glyph Hán + nhãn Lora + dòng phụ. */
@Composable
private fun HubGrid(
    hubs: List<LibraryHub>,
    onHub: (LibraryHub) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        hubs.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { hub ->
                    HubTile(hub = hub, onClick = { onHub(hub) }, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HubTile(hub: LibraryHub, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    val (badgeBg, badgeFg) = when (hub) {
        LibraryHub.Sutras -> colors.featGlow to colors.link
        LibraryHub.Meditation -> IlGreen.copy(alpha = 0.16f) to IlGreenText
        LibraryHub.Talks -> IlRose.copy(alpha = 0.18f) to IlRoseText
        LibraryHub.Buddhas -> colors.ink.copy(alpha = 0.07f) to colors.ink
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .ilTap(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).background(badgeBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = hub.glyph,
                style = TextStyle(fontFamily = Lora, fontSize = 14.sp, color = badgeFg),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = hub.label,
            style = TextStyle(fontFamily = Lora, fontSize = 16.sp, color = colors.ink),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = hub.sub,
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontSize = 11.sp,
                color = colors.ink.copy(alpha = 0.50f),
            ),
        )
    }
}

/** Dải thẻ Phật/Bồ Tát cuộn ngang, tràn mép màn (bù padding 20dp của cột cha). */
@Composable
private fun BuddhaStrip(
    buddhas: List<BuddhaProfile>,
    onBuddha: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .layout { measurable, constraints ->
                // Nới chiều rộng thêm 40dp để dải tràn hai mép (-20dp mỗi bên) như prototype.
                val extra = 40.dp.roundToPx()
                val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra))
                layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
            }
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        buddhas.forEach { b ->
            Column(Modifier.width(118.dp).ilTap(onClick = { onBuddha(b.id) })) {
                // TODO(il-data-builder): ảnh tôn tượng thật (prototype `image-slot`); hiện là nền gradient.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(IlRadius.Card))
                        .background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint))),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = b.shortName,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = colors.ink,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Vía ${b.viaLabel} âm",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontSize = 10.5.sp,
                        color = colors.ink.copy(alpha = 0.45f),
                    ),
                )
            }
        }
    }
}

/** Danh sách bài kinh — mỗi hàng là một [IlRow] glyph sách, cách nhau 10dp. */
@Composable
private fun SutraList(
    sutras: List<Sutra>,
    onSutraClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sutras.forEach { sutra ->
            IlRow(
                title = sutra.name,
                sub = sutra.sub,
                trailing = sutra.time,
                tone = sutra.tone.toIlTone(),
                glyph = IlRowGlyph.Book,
                onClick = { onSutraClick(sutra.id) },
            )
        }
    }
}

/** Ánh xạ tông màu domain sang tông màu component (giữ domain không phụ thuộc UI). */
private fun SutraTone.toIlTone(): IlTone = when (this) {
    SutraTone.Gold -> IlTone.Gold
    SutraTone.Green -> IlTone.Green
    SutraTone.Rose -> IlTone.Rose
}

/** Vùng chạm không ripple (giống `tappable` của Home, không tạo phụ thuộc chéo feature). */
@Composable
private fun Modifier.ilTap(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick,
)

@Preview(showBackground = true, widthDp = 402, heightDp = 800)
@Composable
private fun LibraryPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        LibraryContent(
            state = previewState(),
            callbacks = LibraryCallbacks(),
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 800)
@Composable
private fun LibraryPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        LibraryContent(
            state = previewState(),
            callbacks = LibraryCallbacks(),
        )
    }
}

private fun previewState(): LibraryUiState =
    LibraryUiState.from(GetLibraryCatalogUseCase()(), GetBuddhaProfilesUseCase()())

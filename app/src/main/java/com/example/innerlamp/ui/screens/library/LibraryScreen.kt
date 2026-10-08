package com.example.innerlamp.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryCategory
import com.example.innerlamp.domain.model.Sutra
import com.example.innerlamp.domain.model.SutraTone
import com.example.innerlamp.domain.usecase.GetLibraryCatalogUseCase
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
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
    val onTab: (IlBottomTab) -> Unit = {},
)

/**
 * Màn hình Thư viện — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `lib`).
 *
 * Bố cục (trên → dưới): header dán ("Thư viện") → ô tìm kiếm → chip lọc
 * (Tất cả / Kinh văn / Thiền dẫn / Pháp thoại) → thẻ pháp thoại nổi bật →
 * danh sách "Kinh thường tụng" → bottom nav (tab Thư viện active).
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
        onCategorySelected = viewModel::onCategorySelected,
        modifier = modifier,
    )
}

/** Phần stateless của [LibraryScreen] — dùng cho preview và test. */
@Composable
internal fun LibraryContent(
    state: LibraryUiState,
    callbacks: LibraryCallbacks,
    onCategorySelected: (LibraryCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        LibraryHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(14.dp))
            SearchBar(onClick = callbacks.onSearch)

            Spacer(Modifier.height(14.dp))
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = onCategorySelected,
            )

            Spacer(Modifier.height(20.dp))
            FeaturedTalkCard(
                talk = state.featuredTalk,
                onClick = { callbacks.onFeaturedTalk(state.featuredTalk.id) },
            )

            Spacer(Modifier.height(22.dp))
            SectionHeader("Kinh thường tụng")

            Spacer(Modifier.height(12.dp))
            if (state.filteredSutras.isEmpty()) {
                EmptyHint()
            } else {
                SutraList(
                    sutras = state.filteredSutras,
                    onSutraClick = callbacks.onSutra,
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        IlBottomNav(active = IlBottomTab.Lib, onNav = callbacks.onTab)
    }
}

/** Header dán: tiêu đề "Thư viện" + hairline `ink-06` ở đáy (như header tab gốc của prototype). */
@Composable
private fun LibraryHeader(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth().background(colors.canvas)) {
        Text(
            text = "Thư viện",
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 12.dp),
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 26.sp,
                lineHeight = 31.sp,
                color = colors.ink,
            ),
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
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

/** Dãy chip lọc: "Tất cả" + các mục [LibraryCategory]. "Tất cả" = `selected == null`. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    categories: List<LibraryCategory>,
    selected: LibraryCategory?,
    onSelect: (LibraryCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IlChip(label = "Tất cả", selected = selected == null, onClick = { onSelect(null) })
        categories.forEach { category ->
            IlChip(
                label = category.label,
                selected = selected == category,
                onClick = { onSelect(category) },
            )
        }
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

/** Tiêu đề khối "Kinh thường tụng" (500 · 13.5px). */
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = title,
        modifier = modifier,
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            color = colors.ink,
        ),
    )
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

/** Ghi chú khi chip lọc không có nội dung (dữ liệu mẫu chưa có mục này). */
@Composable
private fun EmptyHint(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Text(
        text = "Nội dung đang được bổ sung dần. Bạn có thể gửi yêu cầu để chúng tôi thỉnh bản dịch về.",
        modifier = modifier.padding(vertical = 10.dp),
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            color = colors.ink.copy(alpha = 0.55f),
        ),
    )
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
            onCategorySelected = {},
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
            onCategorySelected = {},
        )
    }
}

private fun previewState(): LibraryUiState =
    LibraryUiState.from(GetLibraryCatalogUseCase()())

package com.example.innerlamp.ui.screens.buddha

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.DeityModule
import com.example.innerlamp.domain.model.BuddhaDetail
import com.example.innerlamp.domain.model.BuddhaMantra
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.BuddhaSutraRef
import com.example.innerlamp.domain.model.BuddhaViaDay
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.ContentText
import com.example.innerlamp.ui.components.IlContentError
import com.example.innerlamp.ui.components.IlContentLoading
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlRow
import com.example.innerlamp.ui.components.IlRowGlyph
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.screens.home.tappable
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Chi tiết Phật / Bồ Tát" (`buddha`) — dựng lại từ prototype
// `Docs/innerlamp-project/` (màn `buddha`).
// ─────────────────────────────────────────────────────────────────────────────

private val HeroHeight = 420.dp

/** Cuộn quá ngưỡng này thì tiêu đề tên ngắn hiện trên header (prototype `hdrSolid`). */
private val TitleThreshold = 110.dp

/** Số dòng tiểu sử khi thu gọn (prototype `max-height:152px` ≈ 6 dòng × 25dp). */
private const val BioCollapsedLines = 6

/** Chữ nút trên nền vàng (`#1A1611`). */
private val OnGold = Color(0xFF1A1611)

/**
 * Các điểm điều hướng đi ra khỏi màn "Chi tiết Phật / Bồ Tát". Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class BuddhaDetailCallbacks(
    val onBack: () -> Unit = {},
    /** "Lịch lễ" cạnh ngày vía. */
    val onFestivals: () -> Unit = {},
    /** "Trì niệm với tràng hạt" — nhận câu niệm để đưa sang màn Niệm Phật. */
    val onChant: (BuddhaMantra) -> Unit = {},
    val onShare: (BuddhaMantra) -> Unit = {},
    val onSutra: (BuddhaSutraRef) -> Unit = {},
)

/** Màn chi tiết một vị Phật / Bồ Tát. Id lấy từ route arg `buddhaId` qua `SavedStateHandle`. */
@Composable
fun BuddhaDetailScreen(
    modifier: Modifier = Modifier,
    callbacks: BuddhaDetailCallbacks = BuddhaDetailCallbacks(),
    viewModel: BuddhaDetailViewModel = viewModel(factory = DeityModule.buddhaDetailViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile
    if (profile == null) {
        // Loading / error / not found: back chip + status. / Đang tải / lỗi / không tìm thấy.
        Column(modifier.fillMaxSize().background(LocalIlColors.current.canvas).statusBarsPadding()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(title = "", onBack = callbacks.onBack)
            }
            if (state.status == ContentStatus.Loading) {
                IlContentLoading(Modifier.padding(top = 40.dp))
            } else {
                IlContentError(
                    status = state.status,
                    onRetry = viewModel::onRetry,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    notFoundTitle = "Không tìm thấy vị Phật / Bồ Tát này.",
                )
            }
        }
        return
    }
    BuddhaDetailContent(
        state = state,
        profile = profile,
        callbacks = callbacks,
        onToggleBio = viewModel::toggleBio,
        onToggleVows = viewModel::toggleVows,
        modifier = modifier,
    )
}

/** Phần stateless của [BuddhaDetailScreen] — dùng cho preview và test. */
@Composable
internal fun BuddhaDetailContent(
    state: BuddhaDetailUiState,
    profile: BuddhaProfile,
    callbacks: BuddhaDetailCallbacks,
    onToggleBio: () -> Unit,
    onToggleVows: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val scroll = rememberScrollState()
    val thresholdPx = with(LocalDensity.current) { TitleThreshold.toPx() }
    val showTitle by remember(thresholdPx) { derivedStateOf { scroll.value > thresholdPx } }

    // Header trong suốt đè lên bìa; cuộn quá bìa thì blur dần và hiện tên ngắn.
    IlBlurHeaderScaffold(
        scrollOffset = { scroll.value },
        header = {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                IlNavBar(
                    title = if (showTitle) profile.shortName else "",
                    dark = colors.isDark,
                    onBack = callbacks.onBack,
                )
            }
        },
        modifier = modifier,
    ) { _ ->
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Hero(profile)
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 34.dp)) {
                profile.mainVia?.let { via ->
                    ViaCard(state, via = via, onFestivals = callbacks.onFestivals, modifier = Modifier.padding(top = 16.dp))
                }
                profile.role?.let { role ->
                    Text(
                        text = role,
                        modifier = Modifier.padding(top = 10.dp),
                        style = sans(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.8f.sp),
                    )
                }

                SectionLabel("TIỂU SỬ")
                Text(
                    text = profile.bio ?: ContentText.UPDATING,
                    modifier = Modifier.padding(top = 10.dp),
                    maxLines = if (state.bioExpanded) Int.MAX_VALUE else BioCollapsedLines,
                    overflow = TextOverflow.Ellipsis,
                    style = serif(14.5f, colors.ink.copy(alpha = 0.72f), lineHeight = 25.375f.sp),
                )
                if (profile.bio != null) {
                    ToggleLink(state.bioToggleLabel, onToggleBio, Modifier.padding(top = 6.dp))
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 26.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("ĐẠI NGUYỆN", style = kicker(colors.ink.copy(alpha = 0.45f)))
                    Text(state.vowCountLabel, style = sans(11f, colors.ink.copy(alpha = 0.45f)))
                }
                profile.canonicalSource?.let { source ->
                    Text(
                        text = "Theo $source",
                        modifier = Modifier.padding(top = 10.dp),
                        style = serif(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.8f.sp, italic = true),
                    )
                }
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.visibleVows.forEachIndexed { index, vow -> VowRow(number = index + 1, text = vow) }
                }
                if (state.hasMoreVows) {
                    ToggleLink(state.vowToggleLabel, onToggleVows, Modifier.padding(top = 8.dp))
                }

                if (profile.mantras.isNotEmpty()) SectionLabel("THẦN CHÚ & DANH HIỆU")
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    profile.mantras.forEach { mantra ->
                        MantraCard(
                            mantra = mantra,
                            onChant = { callbacks.onChant(mantra) },
                            onShare = { callbacks.onShare(mantra) },
                        )
                    }
                }

                if (profile.sutras.isNotEmpty()) SectionLabel("KINH LIÊN QUAN")
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    profile.sutras.forEach { sutra ->
                        IlRow(
                            title = sutra.title,
                            sub = sutra.sub,
                            trailing = "Mở",
                            tone = IlTone.Gold,
                            glyph = IlRowGlyph.Book,
                            onClick = { callbacks.onSutra(sutra) },
                        )
                    }
                }

                Text(
                    text = "Nội dung do Ban biên tập tổng hợp từ kinh điển",
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    textAlign = TextAlign.Center,
                    style = sans(11f, colors.ink.copy(alpha = 0.45f), lineHeight = 17.6f.sp),
                )
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

/**
 * Cover 420dp: the deity image (when present) under the `th-bg` gradient that melts into the canvas, with
 * the type/realm chips, name and Sanskrit name at the bottom. /
 * Bìa 420dp: ảnh tôn tượng (nếu có) dưới gradient `th-bg` tan vào canvas, chip loại/cõi, tên và tên
 * Sanskrit canh đáy.
 */
@Composable
private fun Hero(profile: BuddhaProfile) {
    val colors = LocalIlColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .background(colors.thBgTop),
    ) {
        profile.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = profile.shortName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        val overlay = if (profile.imageUrl != null) 0.35f else 1f
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to colors.thBgTop.copy(alpha = overlay * 0.4f),
                        0.5f to colors.thBgMid.copy(alpha = overlay),
                        1f to colors.canvas,
                    ),
                ),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 30.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                profile.kindLabel?.let { HeroChip(it, colors.featKicker) }
                profile.realm?.let { HeroChip(it, colors.featTitle) }
            }
            Text(
                text = profile.name,
                modifier = Modifier.padding(top = 12.dp),
                style = serif(28f, colors.featTitle, lineHeight = 33.6f.sp),
            )
            profile.sanskrit?.let { sanskrit ->
                Text(
                    text = sanskrit,
                    modifier = Modifier.padding(top = 6.dp),
                    style = serif(13f, colors.featTitle.copy(alpha = 0.8f), lineHeight = 19.5f.sp, italic = true),
                )
            }
        }
    }
}

@Composable
private fun HeroChip(label: String, color: Color) {
    val colors = LocalIlColors.current
    Text(
        text = label,
        modifier = Modifier
            .border(1.dp, colors.featAcc2, RoundedCornerShape(IlRadius.Pill))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = sans(10.5f, color, FontWeight.Medium),
    )
}

/** Thẻ ngày vía: ô ngày/tháng âm + tên vía + lần vía kế tiếp + link "Lịch lễ". */
@Composable
private fun ViaCard(state: BuddhaDetailUiState, via: BuddhaViaDay, onFestivals: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.featGlow)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.viaDayText, style = serif(20f, colors.link))
            Text(state.viaMonthText, style = sans(9f, colors.link, FontWeight.Medium, letterSpacing = 0.72f.sp))
        }
        Column(Modifier.weight(1f)) {
            Text(via.name, style = sans(13.5f, colors.ink, FontWeight.Medium))
            if (state.nextViaLabel.isNotEmpty()) {
                Text(
                    text = state.nextViaLabel,
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
                )
            }
        }
        Text(
            text = "Lịch lễ",
            modifier = Modifier.tappable(onClick = onFestivals),
            style = sans(12f, colors.link),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 26.dp),
        style = kicker(LocalIlColors.current.ink.copy(alpha = 0.45f)),
    )
}

@Composable
private fun ToggleLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = label,
        modifier = modifier.tappable(onClick = onClick),
        style = sans(12f, LocalIlColors.current.link, FontWeight.Medium),
    )
}

@Composable
private fun VowRow(number: Int, text: String) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(number.toString(), modifier = Modifier.width(24.dp), style = serif(15f, colors.link))
        Text(text, modifier = Modifier.weight(1f), style = sans(13f, colors.ink, lineHeight = 20.15f.sp))
    }
}

/** Thẻ thần chú / danh hiệu nền "feat" (vàng nhạt) với 2 nút: trì niệm và chia sẻ. */
@Composable
private fun MantraCard(mantra: BuddhaMantra, onChant: () -> Unit, onShare: () -> Unit) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.featBgStart, colors.featBgEnd)))
            .border(1.dp, colors.featBorder, shape)
            .padding(18.dp),
    ) {
        Text(mantra.label, style = kicker(colors.featKicker, spacing = 1.33f))
        Text(
            text = mantra.text,
            modifier = Modifier.padding(top = 8.dp),
            style = serif(19f, colors.featTitle, lineHeight = 27.55f.sp),
        )
        mantra.sanskrit?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 6.dp),
                style = serif(13f, colors.featSub, lineHeight = 19.5f.sp, italic = true),
            )
        }
        Text(
            text = mantra.note,
            modifier = Modifier.padding(top = 10.dp),
            style = sans(12f, colors.featSub, lineHeight = 19.2f.sp),
        )
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill("Trì niệm với tràng hạt", filled = true, onClick = onChant)
            ActionPill("Chia sẻ", filled = false, onClick = onShare)
        }
    }
}

@Composable
private fun ActionPill(label: String, filled: Boolean, onClick: () -> Unit) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Pill)
    Box(
        Modifier
            .height(38.dp)
            .clip(shape)
            .then(if (filled) Modifier.background(IlGoldBright) else Modifier.border(1.dp, colors.featAcc2, shape))
            .tappable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = sans(12f, if (filled) OnGold else colors.featTitle, FontWeight.Medium))
    }
}

/** Nhãn mục in hoa nhỏ (prototype `font:600 9.5px; letter-spacing:.16em`). */
private fun kicker(color: Color, spacing: Float = 1.52f) =
    sans(9.5f, color, FontWeight.SemiBold, letterSpacing = spacing.sp)

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

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun BuddhaDetailPreview() {
    InnerLampTheme {
        val today = LocalDate.of(2026, 10, 8)
        val profile = BuddhaProfile(
            id = "bodhisattva_avalokiteshvara",
            name = "Đại Bi Quán Thế Âm Bồ Tát",
            shortName = "Quán Thế Âm Bồ Tát",
            sanskrit = "Avalokiteśvara Bodhisattva",
            type = DeityType.Bodhisattva,
            kindLabel = "Bồ Tát",
            realm = "Phổ Đà Lạc Ca Sơn",
            role = "Đại Bi (Lắng nghe âm thanh cứu khổ tìm đến giải thoát)",
            imageUrl = null,
            viaDays = listOf(BuddhaViaDay(19, 2, "Ngày Đản sinh Quán Thế Âm"), BuddhaViaDay(19, 6, "Ngày Thành đạo Quán Thế Âm")),
            bio = "Là hiện thân của lòng đại bi vô tận.",
            canonicalSource = "Kinh Diệu Pháp Liên Hoa (Phẩm Phổ Môn)",
            vows = listOf("Khi chúng sinh gặp khổ nạn ách tai, nhất tâm xưng tụng danh hiệu liền được cứu vớt."),
            mantras = listOf(BuddhaMantra("TÂM CHÚ", "Án Ma Ni Bát Mê Hồng", "Oṃ Maṇi Padme Hūṃ", "Trì niệm hằng ngày.")),
            sutras = emptyList(),
        )
        BuddhaDetailContent(
            state = BuddhaDetailUiState(
                today = today,
                status = ContentStatus.Loaded,
                detail = BuddhaDetail(profile, LocalDate.of(2027, 3, 26)),
            ),
            profile = profile,
            callbacks = BuddhaDetailCallbacks(),
            onToggleBio = {},
            onToggleVows = {},
        )
    }
}

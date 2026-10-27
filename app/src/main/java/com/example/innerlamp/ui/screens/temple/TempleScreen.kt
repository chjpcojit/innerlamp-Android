package com.example.innerlamp.ui.screens.temple

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.TempleModule
import com.example.innerlamp.domain.model.TempleAmenities
import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.ContentText
import com.example.innerlamp.ui.components.IlContentError
import com.example.innerlamp.ui.components.IlContentLoading
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Màn "Trang chùa" (`temple`) — dựng lại từ prototype
// `Docs/design_handoff_innerlamp/` (màn `temple`).
// ─────────────────────────────────────────────────────────────────────────────

/** Chiều cao hero `--il-th-bg` (290dp) + phần nút nhô xuống (28dp). */
private val HeroHeight = 290.dp

/** Chiều cao nút "Chỉ đường" (prototype `height:46px`). */
private val PillHeight = 46.dp

/** Lines of the introduction while collapsed. / Số dòng giới thiệu khi thu gọn. */
private const val IntroCollapsedLines = 5

/**
 * Exits of the "Trang chùa" screen. Directions open the maps app from the screen itself. /
 * Các điểm thoát của màn "Trang chùa". Chỉ đường do màn tự mở app bản đồ.
 */
@Immutable
class TempleCallbacks(
    val onBack: () -> Unit = {},
)

/**
 * "Trang chùa" — one temple from `GET /v1/temples/{id}`: hero (image + name), "Chỉ đường", three stats,
 * introduction, architecture & heritage, visiting notes, info, amenities and the address. /
 * Màn "Trang chùa" đọc từ `GET /v1/temples/{id}`: hero (ảnh + tên), "Chỉ đường", thống kê 3 cột, giới
 * thiệu, kiến trúc & di sản, lưu ý khi viếng, thông tin, tiện ích và địa chỉ.
 */
@Composable
fun TempleScreen(
    modifier: Modifier = Modifier,
    callbacks: TempleCallbacks = TempleCallbacks(),
    viewModel: TempleViewModel = viewModel(factory = TempleModule.templeViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    TempleContent(
        state = state,
        callbacks = callbacks,
        onRetry = viewModel::onRetry,
        onToggleIntro = viewModel::toggleIntro,
        onDirections = { detail -> TempleIntents.openDirections(context, detail) },
        modifier = modifier,
    )
}

/** Phần stateless của [TempleScreen] — dùng cho preview và test. */
@Composable
internal fun TempleContent(
    state: TempleUiState,
    callbacks: TempleCallbacks,
    onRetry: () -> Unit,
    onToggleIntro: () -> Unit,
    onDirections: (TempleDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val detail = state.detail
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        if (detail == null) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().padding(top = 64.dp, start = 20.dp, end = 20.dp),
            ) {
                if (state.status == ContentStatus.Loading) {
                    IlContentLoading()
                } else {
                    IlContentError(status = state.status, onRetry = onRetry, notFoundTitle = "Không tìm thấy chùa này.")
                }
            }
        } else {
            TempleBody(state = state, detail = detail, onToggleIntro = onToggleIntro, onDirections = onDirections)
        }

        // Header trong suốt đè lên hero (chỉ chip back), luôn ghim trên cùng khi cuộn.
        IlNavBar(
            title = "",
            dark = colors.isDark,
            showBack = true,
            onBack = callbacks.onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TempleBody(
    state: TempleUiState,
    detail: TempleDetail,
    onToggleIntro: () -> Unit,
    onDirections: (TempleDetail) -> Unit,
) {
    val colors = LocalIlColors.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero + nút "Chỉ đường" nhô 28dp xuống dưới hero.
        Box(Modifier.fillMaxWidth().height(HeroHeight + 28.dp)) {
            TempleHero(
                name = detail.name,
                subtitle = state.subtitle,
                imageUrl = detail.imageUrl,
                modifier = Modifier.fillMaxWidth().height(HeroHeight),
            )
            TemplePillButton(
                label = "Chỉ đường",
                primary = true,
                onClick = { onDirections(detail) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(18.dp))
            TempleStats(stats = state.stats)

            Section("Giới thiệu") {
                val intro = detail.introduction
                if (intro == null) {
                    BodyText(ContentText.UPDATING)
                } else {
                    var overflows by remember(intro) { mutableStateOf(false) }
                    Text(
                        text = intro,
                        maxLines = if (state.introExpanded) Int.MAX_VALUE else IntroCollapsedLines,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (!state.introExpanded) overflows = it.hasVisualOverflow },
                        style = bodyStyle(colors.ink.copy(alpha = 0.72f)),
                    )
                    if (overflows || state.introExpanded) {
                        Text(
                            text = if (state.introExpanded) "Thu gọn" else "Đọc thêm",
                            modifier = Modifier.padding(top = 6.dp).tappable(onClick = onToggleIntro),
                            style = sans(12.5f, colors.link, FontWeight.Medium),
                        )
                    }
                }
            }

            Section("Kiến trúc & di sản") {
                BodyText(detail.architectureHeritage ?: ContentText.UPDATING)
            }

            if (detail.visitingNotes.isNotEmpty()) {
                Section("Lưu ý khi viếng chùa") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        detail.visitingNotes.forEachIndexed { index, note ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("%02d".format(index + 1), style = sans(12f, colors.link, FontWeight.Medium))
                                Text(note, modifier = Modifier.weight(1f), style = sans(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.sp))
                            }
                        }
                    }
                }
            }

            Section("Thông tin") {
                InfoCard(rows = state.infoRows)
            }

            Section("Tiện ích") {
                BodyText(state.amenityLabels.takeIf { it.isNotEmpty() }?.joinToString(" · ") ?: ContentText.UPDATING)
            }

            Spacer(Modifier.height(18.dp))
            if (detail.address.isNotBlank()) {
                TempleAddress(
                    address = detail.address,
                    onClick = { onDirections(detail) },
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding().height(30.dp))
    }
}

/** Section title + content, 22dp above. / Tiêu đề mục + nội dung, cách trên 22dp. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val colors = LocalIlColors.current
    Spacer(Modifier.height(22.dp))
    Text(text = title, style = sans(13.5f, colors.ink, FontWeight.Medium))
    Spacer(Modifier.height(10.dp))
    content()
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, style = bodyStyle(LocalIlColors.current.ink.copy(alpha = 0.72f)))
}

private fun bodyStyle(color: Color) = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 23.8f.sp,
    color = color,
)

/** Label/value rows in a card, hairline between rows. / Hàng nhãn/giá trị trong thẻ. */
@Composable
private fun InfoCard(rows: List<Pair<String, String>>) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
    ) {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.07f)))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(label, modifier = Modifier.width(112.dp), style = sans(12f, colors.ink.copy(alpha = 0.50f)))
                Text(value, modifier = Modifier.weight(1f), style = sans(13f, colors.ink, lineHeight = 19.sp))
            }
        }
    }
}

/**
 * Hero 290dp: temple image (when present) under the `--il-th-bg` gradient that melts into the canvas, a
 * glow, two flames, name and subtitle at the bottom. /
 * Hero 290dp: ảnh chùa (nếu có) dưới gradient `--il-th-bg` tan vào canvas, quầng sáng, hai ngọn lửa, tên
 * và phụ đề ở đáy.
 */
@Composable
private fun TempleHero(
    name: String,
    subtitle: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(modifier = modifier.background(colors.thBgTop)) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to colors.thBgTop.copy(alpha = if (imageUrl != null) 0.15f else 1f),
                        0.58f to colors.thBgMid.copy(alpha = if (imageUrl != null) 0.55f else 1f),
                        1f to colors.canvas,
                    ),
                ),
        )
        // Quầng sáng 340dp ở giữa-trên (khớp `radial-gradient(circle, --il-th-glow, transparent 66%)`).
        if (imageUrl == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
                    .size(340.dp)
                    .background(Brush.radialGradient(listOf(colors.thGlow, Color.Transparent)), CircleShape),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TempleFlame()
                TempleFlame()
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = name,
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 26.sp,
                    color = colors.featTitle, // --il-th-ink
                ),
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = sans(12f, colors.featSub), // --il-th-sub
                )
            }
        }
    }
}

/**
 * Ngọn lửa 16×22 (teardrop bo góc theo CSS `50% 50% 45% 45% / 62% 62% 38% 38%`),
 * gradient `#F3C673 → #D9963C` + quầng sáng nhỏ phía sau (`--il-th-flame`).
 */
@Composable
private fun TempleFlame(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Box(modifier.size(16.dp, 22.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .background(
                    Brush.radialGradient(listOf(colors.featFlameGlow, Color.Transparent)),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(IlGoldBright2, IlGoldBright)),
                    TempleFlameShape,
                ),
        )
    }
}

/** Khối thống kê 3 cột, ngăn cách bằng hairline `ink-09`. */
@Composable
private fun TempleStats(
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .padding(vertical = 16.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(Modifier.width(1.dp).height(36.dp).background(colors.ink.copy(alpha = 0.09f)))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = sans(if (value.length > 6) 14f else 19f, colors.ink, FontWeight.Medium),
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(top = 3.dp),
                    style = sans(10.5f, colors.ink.copy(alpha = 0.50f)),
                )
            }
        }
    }
}

/** Hàng địa chỉ nền xanh nhạt: chấm xanh + địa chỉ + chevron. */
@Composable
private fun TempleAddress(
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IlGreen.copy(alpha = 0.12f))
            .tappable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(8.dp).background(IlGreen, CircleShape))
        Text(
            text = "$address · chỉ đường",
            modifier = Modifier.weight(1f),
            style = sans(12f, IlGreenText, lineHeight = 18.6f.sp),
        )
        Text(
            text = "›",
            style = sans(18f, IlGreenText.copy(alpha = 0.6f)),
        )
    }
}

/** Nút pill 46dp của màn chùa (khác `IlButton` 50dp — giữ đúng pixel prototype). */
@Composable
private fun TemplePillButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Pill)
    val bg = if (primary) colors.ink else colors.card
    val fg = if (primary) colors.canvas else colors.ink
    val bd = if (primary) colors.ink else colors.ink.copy(alpha = 0.14f)
    Box(
        modifier = modifier
            .then(
                // Nút primary có bóng `0 8px 22px --il-ink-2` (giống prototype).
                if (primary) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = colors.ink.copy(alpha = 0.20f),
                        spotColor = colors.ink.copy(alpha = 0.20f),
                    )
                } else Modifier
            )
            .height(PillHeight)
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = sans(13.5f, fg, FontWeight.Medium),
        )
    }
}

/** Hình ngọn lửa (teardrop bo góc trên rộng, đáy thon). */
private val TempleFlameShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        // Bán kính bo góc theo CSS: trên 50% / 62%, dưới 45% / 38%.
        val topRx = 0.50f * w
        val topRy = 0.62f * h
        val botRx = 0.45f * w
        val botRy = 0.38f * h
        // Hằng số vòng cung Bézier ≈ 1/4 ellipse.
        val k = 0.5522847498f
        val path = Path().apply {
            moveTo(topRx, 0f)
            lineTo(w - topRx, 0f)
            // Góc trên phải.
            cubicTo(w - topRx + topRx * k, 0f, w, topRy - topRy * k, w, topRy)
            lineTo(w, h - botRy)
            // Góc dưới phải.
            cubicTo(w, h - botRy + botRy * k, w - botRx + botRx * k, h, w - botRx, h)
            lineTo(botRx, h)
            // Góc dưới trái.
            cubicTo(botRx - botRx * k, h, 0f, h - botRy + botRy * k, 0f, h - botRy)
            lineTo(0f, topRy)
            // Góc trên trái.
            cubicTo(0f, topRy - topRy * k, topRx - topRx * k, 0f, topRx, 0f)
            close()
        }
        return Outline.Generic(path)
    }
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
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
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
private fun TemplePreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        TempleContent(
            state = previewState(),
            callbacks = TempleCallbacks(),
            onRetry = {},
            onToggleIntro = {},
            onDirections = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun TemplePreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        TempleContent(
            state = previewState(),
            callbacks = TempleCallbacks(),
            onRetry = {},
            onToggleIntro = {},
            onDirections = {},
        )
    }
}

private fun previewState(): TempleUiState = TempleUiState(
    status = ContentStatus.Loaded,
    detail = TempleDetail(
        id = "temple_north_011",
        name = "Chùa Bích Động",
        place = "Ninh Bình",
        region = "Miền Bắc",
        type = "Chùa",
        sect = "Bắc tông",
        heritageRank = "Di sản thế giới",
        establishedYear = 1428,
        imageUrl = null,
        address = "Xã Ninh Hải, Huyện Hoa Lư, Tỉnh Ninh Bình",
        lat = 20.2185,
        lng = 105.9177,
        aliases = listOf("Bạch Ngọc Thạch Sơn Động"),
        founder = "Hai nhà sư Trí Kiên và Trí Thể",
        currentAbbot = "Thích Đức Lợi",
        operatingHours = "07:00 - 18:00",
        amenities = TempleAmenities(parkingCar = true, parkingMotorcycle = true, vegetarianMeals = false, summerRetreat = false),
        introduction = "Chùa Bích Động nằm trong dãy núi đá vôi ở Hoa Lư, Ninh Bình, khởi dựng năm 1428 thời Lê sơ.",
        architectureHeritage = null,
        visitingNotes = listOf("Đường lên chùa Thượng có nhiều bậc đá dốc; nên đi giày dép chống trơn."),
    ),
)

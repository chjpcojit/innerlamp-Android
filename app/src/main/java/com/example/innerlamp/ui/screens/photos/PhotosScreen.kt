package com.example.innerlamp.ui.screens.photos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.innerlamp.di.GalleryModule
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlContentError
import com.example.innerlamp.ui.components.IlContentLoading
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlDarkBgBottom
import com.example.innerlamp.ui.theme.IlDarkBgTop
import com.example.innerlamp.ui.theme.IlDarkInk
import com.example.innerlamp.ui.theme.IlDarkInkSoft
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

// ── Bảng màu lightbox (luôn tối, không theo theme) ────────────────────────────
// Nguồn: README "Ảnh chùa đẹp … lightbox xem ảnh" — nền tối + chữ sáng, dùng bảng
// `#2A241C → #100E0B` + `#F5EEE0` riêng để không bị sáng lên ở light mode.
private val LightboxScrim = Color(0xE6100E0B)         // nền phủ tối phía sau
private val LightboxChipBg = Color(0x14F2EBDD)        // nền nút đóng (rgba .08)
private val LightboxChipBd = Color(0x2EF2EBDD)        // viền nút đóng (rgba .18)

/**
 * Các điểm điều hướng đi ra khỏi màn "Ảnh chùa đẹp". Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class PhotosCallbacks(
    val onBack: () -> Unit = {},
)

/**
 * Màn hình "Ảnh chùa đẹp" — dựng lại từ prototype
 * `Docs/design_handoff_innerlamp/` (màn `photos`).
 *
 * Bố cục (trên → dưới): header dán ("Ảnh chùa đẹp" + phụ đề số ảnh + dãy chip lọc
 * Tất cả / Chùa / Tượng Phật / Cảnh thiền / Lễ hội) → lưới 2 cột ảnh (masonry).
 * Chạm một ảnh mở lightbox toàn màn, chạm lần nữa để đóng.
 */
@Composable
fun PhotosScreen(
    modifier: Modifier = Modifier,
    callbacks: PhotosCallbacks = PhotosCallbacks(),
    viewModel: PhotosViewModel = viewModel(factory = GalleryModule.photosViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        PhotosContent(
            state = state,
            callbacks = callbacks,
            onCategorySelected = viewModel::onCategorySelected,
            onPhotoClick = viewModel::onPhotoOpened,
            onRetry = viewModel::onRetry,
        )

        state.viewing?.let { photo ->
            PhotoLightbox(photo = photo, creditLine = state.creditLine, onDismiss = viewModel::onLightboxDismissed)
        }
    }
}

/** Phần stateless của [PhotosScreen] — dùng cho preview và test. */
@Composable
internal fun PhotosContent(
    state: PhotosUiState,
    callbacks: PhotosCallbacks,
    onCategorySelected: (PhotoCategory?) -> Unit,
    onPhotoClick: (TemplePhoto) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        PhotosHeader(
            sub = state.countLabel,
            categories = state.categories,
            selected = state.selectedCategory,
            onBack = callbacks.onBack,
            onSelect = onCategorySelected,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            when (state.status) {
                ContentStatus.Loading -> IlContentLoading()
                ContentStatus.Loaded -> if (state.filteredPhotos.isEmpty()) {
                    EmptyCategory(label = state.selectedCategory?.label)
                } else {
                    PhotoGrid(photos = state.filteredPhotos, onPhotoClick = onPhotoClick)
                }
                else -> IlContentError(status = state.status, onRetry = onRetry)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Header dán: `IlNavBar` "Ảnh chùa đẹp" + phụ đề, rồi dãy chip lọc cuộn ngang,
 * hairline `ink-06` ở đáy (khớp prototype — chip nằm trong dải header dán).
 */
@Composable
private fun PhotosHeader(
    sub: String,
    categories: List<PhotoCategory>,
    selected: PhotoCategory?,
    onBack: () -> Unit,
    onSelect: (PhotoCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier.fillMaxWidth().background(colors.canvas)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        ) {
            IlNavBar(title = "Ảnh chùa đẹp", sub = sub, showBack = true, onBack = onBack)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
    }
}

/** Empty filter result. / Bộ lọc không có ảnh nào. */
@Composable
private fun EmptyCategory(label: String?, modifier: Modifier = Modifier) {
    Text(
        text = if (label != null) "Chưa có ảnh nào ở mục $label." else "Chưa có ảnh nào.",
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = BeVietnamPro,
            fontSize = 13.sp,
            color = LocalIlColors.current.ink.copy(alpha = 0.55f),
        ),
    )
}

/** Lưới masonry 2 cột: chia ảnh xen kẽ vào hai cột (giữ chiều cao từng ô khác nhau). */
@Composable
private fun PhotoGrid(
    photos: List<TemplePhoto>,
    onPhotoClick: (TemplePhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val left = photos.filterIndexed { index, _ -> index % 2 == 0 }
    val right = photos.filterIndexed { index, _ -> index % 2 == 1 }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            left.forEach { PhotoTile(photo = it, onClick = onPhotoClick) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            right.forEach { PhotoTile(photo = it, onClick = onPhotoClick) }
        }
    }
}

/**
 * One tile: thumbnail (radius 18dp, gradient + category label underneath as placeholder) + Lora title +
 * place. / Một ô ảnh: thumbnail (bo 18dp, nền gradient + nhãn danh mục làm placeholder) + tiêu đề + địa điểm.
 */
@Composable
private fun PhotoTile(
    photo: TemplePhoto,
    onClick: (TemplePhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(photo.heightDp.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(colors.sunk2, colors.tint)))
                .ilTap(onClick = { onClick(photo) }),
            contentAlignment = Alignment.Center,
        ) {
            photo.category?.let { category ->
                Text(
                    text = category.label,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.ink.copy(alpha = 0.45f),
                    ),
                )
            }
            AsyncImage(
                model = photo.thumbnailUrl ?: photo.imageUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = photo.title,
            modifier = Modifier.padding(top = 8.dp),
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                color = colors.ink,
            ),
        )
        Text(
            text = photo.place,
            modifier = Modifier.padding(top = 2.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = colors.ink.copy(alpha = 0.50f),
            ),
        )
    }
}

/**
 * Full-screen lightbox (dark): full image (`image_url`), title, place and the credit line once loaded
 * (CC BY / BY-SA require it). Tap anywhere (or ✕) to close. /
 * Lightbox toàn màn (nền tối): ảnh gốc, tiêu đề, địa điểm và dòng ghi công khi tải xong (CC BY / BY-SA
 * bắt buộc ghi công). Chạm bất kỳ đâu (hoặc nút ✕) để đóng.
 */
@Composable
private fun PhotoLightbox(
    photo: TemplePhoto,
    creditLine: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightboxScrim)
            .ilTap(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.verticalGradient(listOf(IlDarkBgTop, IlDarkBgBottom))),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = photo.imageUrl ?: photo.thumbnailUrl,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                text = photo.title,
                modifier = Modifier.padding(top = 20.dp),
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    color = IlDarkInk,
                ),
            )
            Text(
                text = photo.place,
                modifier = Modifier.padding(top = 8.dp),
                style = TextStyle(
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = IlDarkInkSoft.copy(alpha = 0.7f),
                ),
            )
            creditLine?.let { line ->
                Text(
                    text = line,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontSize = 11.sp,
                        color = IlDarkInkSoft.copy(alpha = 0.55f),
                    ),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 20.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(LightboxChipBg)
                .border(1.dp, LightboxChipBd, CircleShape)
                .ilTap(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            CloseGlyph(IlDarkInk)
        }
    }
}

/** Nút đóng "✕" (hai nét chéo 1.7px) — khớp glyph của `IlNavBar`. */
@Composable
private fun CloseGlyph(color: Color) {
    Canvas(Modifier.size(12.dp)) {
        val stroke = 1.7.dp.toPx()
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/** Vùng chạm không ripple (prototype chỉ đổi nền/scale nhẹ, không ripple). */
@Composable
private fun Modifier.ilTap(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick,
)

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 402, heightDp = 900)
@Composable
private fun PhotosPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        PhotosContent(
            state = previewState(),
            callbacks = PhotosCallbacks(),
            onCategorySelected = {},
            onPhotoClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 900)
@Composable
private fun PhotosPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        PhotosContent(
            state = previewState(),
            callbacks = PhotosCallbacks(),
            onCategorySelected = {},
            onPhotoClick = {},
        )
    }
}

private fun previewState(): PhotosUiState = PhotosUiState(
    photos = List(6) { i ->
        TemplePhoto(
            id = "p$i",
            category = PhotoCategory.entries[i % PhotoCategory.entries.size],
            title = "Chánh điện lúc bình minh",
            place = "Chùa Bái Đính · Ninh Bình",
            heightDp = listOf(200, 150, 180)[i % 3],
        )
    },
    status = ContentStatus.Loaded,
)

package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.innerlamp.ui.theme.LocalIlColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private val MaxBlurRadius = 24.dp

/** Độ đậm lớp phủ màu nền khi blur đạt tối đa (0 → 0.3) — vẫn thấy nội dung mờ mờ bên dưới. */
private const val BlurredTintAlpha = 0.3f

/**
 * Khung màn hình có header blur theo cuộn.
 *
 * - Lúc nằm ở đầu trang (`scrollOffset() == 0`): header trong suốt, không blur → trông cùng màu
 *   nền trang, liền mạch với nội dung.
 * - Khi cuộn lên: nội dung chui xuống dưới header; blur tăng dần theo [scrollOffset] và đạt tối đa khi nội dung
 *   cuộn lên chạm mép trên màn hình (cuộn bằng chiều cao header), kèm đường kẻ chân mờ.
 * - Cuộn ngược về đầu trang: header trở lại như ban đầu (blur tính liên tục từ offset).
 *
 * Header đè lên nội dung, nên [content] nhận chiều cao header (đã gồm status bar) để chừa chỗ
 * ở đầu danh sách (vd `Spacer(Modifier.height(headerHeight))`).
 *
 * Blur nền dùng thư viện Haze (RenderEffect, cần API 31+ = minSdk của app).
 *
 * @param scrollOffset offset cuộn hiện tại (px), đọc trong lambda để không recompose cả màn.
 */
@Composable
fun IlBlurHeaderScaffold(
    scrollOffset: () -> Int,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (headerHeight: Dp) -> Unit,
) {
    val colors = LocalIlColors.current
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    // Blur đạt max khi nội dung cuộn lên chạm mép trên màn hình = cuộn đúng bằng chiều cao header
    // (nội dung đầu trang nằm ngay dưới header). Tránh chia 0 trước khi header đo xong.
    val progress = remember {
        derivedStateOf { (scrollOffset().toFloat() / headerHeightPx.coerceAtLeast(1)).coerceIn(0f, 1f) }
    }

    Box(modifier.fillMaxSize()) {
        // Nền canvas phải nằm TRONG vùng hazeSource: nếu để ngoài, ảnh blur chỉ gồm chữ/thẻ với
        // phần còn lại trong suốt → chữ sắc nét bên dưới vẫn lộ qua header.
        Box(Modifier.fillMaxSize().hazeSource(hazeState).background(colors.canvas)) {
            content(with(density) { headerHeightPx.toDp() })
        }
        Box(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .onSizeChanged { headerHeightPx = it.height },
        ) {
            BlurLayer(hazeState, progress, Modifier.matchParentSize())
            Box(Modifier.statusBarsPadding()) { header() }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer { alpha = progress.value }
                    .background(colors.ink.copy(alpha = 0.06f)),
            )
        }
    }
}

/**
 * Lớp blur nằm sau nội dung header. Đọc [progress] ngay trong composition (không trong block của
 * `hazeEffect`) để chắc chắn mỗi lần giá trị đổi thì Haze nhận bán kính/tint mới — đồng thời chỉ
 * lớp này recompose, không đụng tới nội dung header hay màn hình.
 */
@Composable
private fun BlurLayer(hazeState: HazeState, progress: State<Float>, modifier: Modifier = Modifier) {
    val canvas = LocalIlColors.current.canvas
    val p = progress.value
    Box(
        modifier.hazeEffect(hazeState) {
            blurRadius = MaxBlurRadius * p
            // Lớp phủ màu nền đi từ 0 (trong suốt hẳn) lên BlurredTintAlpha.
            tints = listOf(HazeTint(canvas.copy(alpha = BlurredTintAlpha * p)))
            noiseFactor = 0f
        },
    )
}

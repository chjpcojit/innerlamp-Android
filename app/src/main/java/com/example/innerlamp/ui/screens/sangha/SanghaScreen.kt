package com.example.innerlamp.ui.screens.sangha

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.MeditationGroup
import com.example.innerlamp.domain.model.SanghaPost
import com.example.innerlamp.domain.model.SanghaSummary
import com.example.innerlamp.domain.model.SanghaTone
import com.example.innerlamp.ui.components.IlAvatar
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.components.IlChip
import com.example.innerlamp.ui.components.IlTone
import com.example.innerlamp.ui.components.LeafShape
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Các điểm điều hướng đi ra khỏi màn Đạo tràng. Màn không giữ `NavController`:
 * mọi điều hướng đi qua callback; route chưa có thì để mặc định no-op.
 */
@Immutable
class SanghaCallbacks(
    val onFindSangha: () -> Unit = {},
    val onSitTogether: () -> Unit = {},
    val onCompose: () -> Unit = {},
    val onEvents: () -> Unit = {},
    val onTemple: () -> Unit = {},
    val onPost: (postId: String) -> Unit = {},
    val onTab: (IlBottomTab) -> Unit = {},
)

/**
 * Màn hình Đạo tràng — dựng lại từ prototype `Docs/design_handoff_innerlamp/` (màn `sangha`).
 *
 * Bố cục (trên → dưới): header dán ("Đạo tràng" + "Tìm đạo tràng") → thẻ "Đang cùng
 * ngồi thiền" → 2 chip hành động → "Đạo tràng của tôi" → "Chia sẻ của bạn đồng tu" →
 * bottom nav (tab Đạo tràng).
 *
 * @author innerlamp
 */
@Composable
fun SanghaScreen(
    modifier: Modifier = Modifier,
    callbacks: SanghaCallbacks = SanghaCallbacks(),
    viewModel: SanghaViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SanghaContent(state = state, callbacks = callbacks, modifier = modifier)
}

/** Phần stateless của [SanghaScreen] — dùng cho preview và test. */
@Composable
internal fun SanghaContent(
    state: SanghaUiState,
    callbacks: SanghaCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        SanghaHeader(onFindSangha = callbacks.onFindSangha)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))
            MeditationCard(
                meditation = state.meditation,
                onSitTogether = callbacks.onSitTogether,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IlChip(label = "Viết bài chia sẻ", onClick = callbacks.onCompose)
                IlChip(label = "Sự kiện & khóa học", onClick = callbacks.onEvents)
            }

            Spacer(Modifier.height(22.dp))
            SectionTitle("Đạo tràng của tôi")

            Spacer(Modifier.height(12.dp))
            MySanghaCard(
                summary = state.mySangha,
                onClick = callbacks.onTemple,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle("Chia sẻ của bạn đồng tu")

            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.posts.forEach { post ->
                    PostCard(post = post, onClick = { callbacks.onPost(post.id) })
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        IlBottomNav(active = IlBottomTab.Sangha, onNav = callbacks.onTab)
    }
}

/** Header dán: tiêu đề "Đạo tràng" + link "Tìm đạo tràng". Hairline `ink-06` ở đáy. */
@Composable
private fun SanghaHeader(
    onFindSangha: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth().background(colors.canvas)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Đạo tràng",
                style = TextStyle(
                    fontFamily = Lora,
                    fontWeight = FontWeight.Normal,
                    fontSize = 26.sp,
                    lineHeight = 31.sp,
                    color = colors.ink,
                ),
            )
            Text(
                text = "Tìm đạo tràng",
                modifier = Modifier
                    .tappable(onClick = onFindSangha)
                    .padding(bottom = 3.dp),
                style = sans(12f, colors.link),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.ink.copy(alpha = 0.06f)))
    }
}

/** Thẻ "Đang cùng ngồi thiền": số người + nhóm avatar + nút "Ngồi cùng". */
@Composable
private fun MeditationCard(
    meditation: MeditationGroup,
    onSitTogether: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, IlGold.copy(alpha = 0.35f), shape)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Chấm trạng thái vàng, có quầng phát sáng như prototype.
            Box(
                Modifier
                    .size(10.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(IlGoldBright.copy(alpha = 0.9f), Color.Transparent),
                                center = center,
                                radius = 12.dp.toPx(),
                            ),
                        )
                    }
                    .background(IlGoldBright, CircleShape),
            )
            Text("Đang cùng ngồi thiền", style = sans(13.5f, colors.ink, FontWeight.Medium))
        }
        Text(
            text = meditation.count.toString(),
            modifier = Modifier.padding(top = 12.dp),
            style = sans(38f, colors.ink, lineHeight = 38.sp),
        )
        Text(
            text = meditation.label,
            modifier = Modifier.padding(top = 6.dp),
            style = sans(12f, colors.ink.copy(alpha = 0.50f)),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Nhóm avatar xếp chồng (lần lượt tint / tint2 / tint3).
            Row(horizontalArrangement = Arrangement.spacedBy((-9).dp)) {
                listOf(colors.tint, colors.tint2, colors.tint3).forEach { tint ->
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(tint, CircleShape)
                            .border(1.5.dp, colors.card, CircleShape),
                    )
                }
            }
            // Nút "Ngồi cùng" (pill ink, 44px, chiếm khoảng trống còn lại).
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(IlRadius.Pill))
                    .background(colors.ink)
                    .tappable(onClick = onSitTogether),
                contentAlignment = Alignment.Center,
            ) {
                Text("Ngồi cùng", style = sans(13.5f, colors.canvas, FontWeight.Medium))
            }
        }
    }
}

/** Tiêu đề khối ("Đạo tràng của tôi", "Chia sẻ của bạn đồng tu"). */
@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Text(
        text = title,
        modifier = modifier.padding(horizontal = 20.dp),
        style = sans(13.5f, colors.ink, FontWeight.Medium),
    )
}

/** Thẻ "Đạo tràng của tôi": tên + thành viên + thông báo, bấm vào mở trang chùa. */
@Composable
private fun MySanghaCard(
    summary: SanghaSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Icon tile 42px: lá bồ đề xanh trên nền tint xanh.
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(IlGreen.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(15.dp).background(IlGreen, LeafShape))
            }
            Column(Modifier.weight(1f)) {
                Text(summary.name, style = sans(14f, colors.ink, FontWeight.Medium))
                Text(
                    text = "${summary.memberCount} thành viên · ${summary.place}",
                    modifier = Modifier.padding(top = 2.dp),
                    style = sans(11.5f, colors.ink.copy(alpha = 0.50f)),
                )
            }
            Text("›", style = sans(18f, colors.ink.copy(alpha = 0.35f)))
        }
        // Đường kẻ + thông báo đạo tràng.
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(1.dp)
                .background(colors.ink.copy(alpha = 0.07f)),
        )
        Text(
            text = summary.announcement,
            modifier = Modifier.padding(top = 14.dp),
            style = sans(13f, colors.ink.copy(alpha = 0.72f), lineHeight = 20.8f.sp),
        )
    }
}

/** Thẻ bài chia sẻ của bạn đồng tu: avatar + tác giả + nội dung + tùy hỷ/hồi âm. */
@Composable
private fun PostCard(
    post: SanghaPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
            .tappable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IlAvatar(initials = post.initials, tone = post.tone.toIlTone(), size = 32.dp)
            Column(Modifier.weight(1f)) {
                Text(post.author, style = sans(12.5f, colors.ink, FontWeight.Medium))
                Text(
                    text = "${post.time} · ${post.meta}",
                    modifier = Modifier.padding(top = 1.dp),
                    style = sans(10.5f, colors.ink.copy(alpha = 0.42f)),
                )
            }
        }
        Text(
            text = post.body,
            modifier = Modifier.padding(top = 12.dp),
            style = TextStyle(
                fontFamily = Lora,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 22.4f.sp,
                color = colors.ink.copy(alpha = 0.75f),
            ),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Tùy hỷ · ${post.likes}", style = sans(11.5f, colors.ink.copy(alpha = 0.45f)))
            Text("Hồi âm · ${post.comments}", style = sans(11.5f, colors.ink.copy(alpha = 0.45f)))
        }
    }
}

/** Ánh xạ tông màu domain sang tông màu của component library. */
private fun SanghaTone.toIlTone(): IlTone = when (this) {
    SanghaTone.Gold -> IlTone.Gold
    SanghaTone.Green -> IlTone.Green
    SanghaTone.Rose -> IlTone.Rose
    SanghaTone.Ink -> IlTone.Ink
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
    lineHeight: TextUnit = TextUnit.Unspecified,
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
private fun SanghaPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SanghaContent(state = previewState(), callbacks = SanghaCallbacks())
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1200)
@Composable
private fun SanghaPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SanghaContent(state = previewState(), callbacks = SanghaCallbacks())
    }
}

private fun previewState(): SanghaUiState = SanghaUiState(
    meditation = SanghaSampleData.meditation,
    mySangha = SanghaSampleData.mySangha,
    posts = SanghaSampleData.posts,
)

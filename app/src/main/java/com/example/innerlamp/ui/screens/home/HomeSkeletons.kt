package com.example.innerlamp.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.innerlamp.ui.theme.IlRadius
import com.example.innerlamp.ui.theme.LocalIlColors

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton (shimmer) cho 3 khối chùa / Phật & Bồ Tát / bài viết khi đang tải. Kích thước
// khớp card thật để nội dung không nhảy bố cục khi dữ liệu về.
// ─────────────────────────────────────────────────────────────────────────────

private val SkeletonGutter = 20.dp

/** Nền shimmer: dải sáng quét ngang liên tục trên nền `ink` alpha thấp. */
private fun Modifier.shimmer(): Modifier = composed {
    val colors = LocalIlColors.current
    val base = colors.ink.copy(alpha = 0.06f)
    val highlight = colors.ink.copy(alpha = 0.12f)
    val progress by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )
    val travel = 600f
    val start = -travel + progress * travel * 2
    background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(start, 0f),
            end = Offset(start + travel, 0f),
        ),
    )
}

@Composable
private fun SkeletonBlock(width: Dp?, height: Dp, modifier: Modifier = Modifier, radius: Dp = 6.dp) {
    val sized = if (width != null) modifier.width(width) else modifier.fillMaxWidth()
    Box(sized.height(height).clip(RoundedCornerShape(radius)).shimmer())
}

/** Skeleton carousel chùa: 1 thẻ đầy đủ + thẻ thứ hai bị cắt, như carousel thật. */
@Composable
internal fun TemplesSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clearAndSetSemantics {},
    ) {
        val cardWidth = (maxWidth - SkeletonGutter * 2) * 0.62f
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState(), enabled = false)
                .padding(start = SkeletonGutter, end = SkeletonGutter, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(2) {
                Column(
                    Modifier
                        .width(cardWidth)
                        .clip(shape)
                        .background(colors.card)
                        .border(1.dp, colors.ink.copy(alpha = 0.08f), shape),
                ) {
                    Box(Modifier.fillMaxWidth().height(150.dp).shimmer())
                    Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
                        SkeletonBlock(width = cardWidth * 0.7f, height = 15.dp)
                        SkeletonBlock(width = cardWidth * 0.5f, height = 11.dp, modifier = Modifier.padding(top = 8.dp))
                        SkeletonBlock(width = cardWidth * 0.4f, height = 10.dp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}

/** Skeleton carousel Phật & Bồ Tát: thẻ 124×164 + 2 dòng chữ. */
@Composable
internal fun BuddhasSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .horizontalScroll(rememberScrollState(), enabled = false)
            .padding(start = SkeletonGutter, end = SkeletonGutter, bottom = 2.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(4) {
            Column(Modifier.width(124.dp)) {
                Box(Modifier.fillMaxWidth().height(164.dp).clip(RoundedCornerShape(IlRadius.Row)).shimmer())
                SkeletonBlock(width = 90.dp, height = 14.dp, modifier = Modifier.padding(top = 8.dp))
                SkeletonBlock(width = 70.dp, height = 10.dp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/** Skeleton danh sách bài viết: 3 thẻ ảnh vuông 84dp + tiêu đề 2 dòng. */
@Composable
internal fun ArticlesSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(IlRadius.Card)
    Column(
        modifier = modifier
            .padding(start = SkeletonGutter, end = SkeletonGutter, top = 12.dp)
            .clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.card)
                    .border(1.dp, colors.ink.copy(alpha = 0.08f), shape)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)).shimmer())
                Column(Modifier.weight(1f).height(84.dp)) {
                    SkeletonBlock(width = 60.dp, height = 9.dp)
                    SkeletonBlock(width = null, height = 14.dp, modifier = Modifier.padding(top = 8.dp))
                    SkeletonBlock(width = 140.dp, height = 14.dp, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.weight(1f))
                    SkeletonBlock(width = 100.dp, height = 10.dp)
                }
            }
        }
    }
}

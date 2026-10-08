package com.example.innerlamp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlLampFlame
import com.example.innerlamp.ui.theme.IlLampFlameDark
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors

/** Tab gốc của bottom nav (prop `active` của prototype). */
enum class IlBottomTab { Home, Lib, Lamp, Sangha, Me }

// ─────────────────────────────────────────────────────────────────────────────
// Bottom nav (prototype IL-BottomNav): 4 tab + nút đèn nổi ở giữa. Icon là SVG
// nét 1.6px (22×22) lấy nguyên từ prototype — mái chùa, kinh sách mở, người trong
// nhóm, người. Tab active tô nét `ink`/gold + fill nhẹ. `hidden` trượt xuống.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IlBottomNav(
    active: IlBottomTab,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    hidden: Boolean = false,
    onNav: (IlBottomTab) -> Unit = {},
) {
    val colors = LocalIlColors.current
    val on = if (dark) IlGoldBright else colors.ink
    val off = if (dark) Color(0x66F2EBDD) else colors.ink.copy(alpha = 0.40f)
    // Fill nhẹ của tab active (prototype: `--il-ink-12` light / `rgba(217,150,60,.22)` dark).
    val fillOn = if (dark) Color(0x38D9963C) else colors.ink.copy(alpha = 0.12f)
    val fade = if (dark) {
        Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF14120F)))
    } else {
        Brush.verticalGradient(listOf(Color.Transparent, colors.canvas))
    }
    val c = { tab: IlBottomTab -> if (tab == active) on else off }
    val f = { tab: IlBottomTab -> if (tab == active) fillOn else null }

    AnimatedVisibility(
        visible = !hidden,
        modifier = modifier.fillMaxWidth(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Box(Modifier.fillMaxWidth().background(fade)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabItem("Trang chủ", c(IlBottomTab.Home), onClick = { onNav(IlBottomTab.Home) }) {
                    HomeTabIcon(c(IlBottomTab.Home), f(IlBottomTab.Home))
                }
                TabItem("Thư viện", c(IlBottomTab.Lib), onClick = { onNav(IlBottomTab.Lib) }) {
                    LibTabIcon(c(IlBottomTab.Lib), f(IlBottomTab.Lib))
                }
                LampButton(dark = dark, onClick = { onNav(IlBottomTab.Lamp) })
                TabItem("Đạo tràng", c(IlBottomTab.Sangha), onClick = { onNav(IlBottomTab.Sangha) }) {
                    SanghaTabIcon(c(IlBottomTab.Sangha), f(IlBottomTab.Sangha))
                }
                TabItem("Tôi", c(IlBottomTab.Me), onClick = { onNav(IlBottomTab.Me) }) {
                    MeTabIcon(c(IlBottomTab.Me), f(IlBottomTab.Me))
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    label: String,
    color: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Text(
            text = label,
            modifier = Modifier.padding(top = 5.dp),
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                color = color,
            ),
        )
    }
}

/** Nút đèn nổi ở giữa (48px): ngọn lửa + đài sen trong vòng tròn. */
@Composable
private fun RowScope.LampButton(dark: Boolean, onClick: () -> Unit) {
    val colors = LocalIlColors.current
    val bg = if (dark) IlGoldBright else colors.ink
    val shadow = if (dark) Color(0x73D9963C) else colors.ink.copy(alpha = 0.28f)
    val flame = if (dark) IlLampFlameDark else IlLampFlame
    val icon = remember(flame) { lampIcon(flame) }
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(10.dp, CircleShape, ambientColor = shadow, spotColor = shadow)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icon tab — SVG 22×22 nét 1.6px (viewBox 0 0 24 24) lấy nguyên từ prototype.
// Mỗi icon: các path nét (stroke) + path có fill nhẹ khi active.
// ─────────────────────────────────────────────────────────────────────────────

private const val HomeSpire = "M12 2.5v2"
private const val HomeRoof = "M4 11.5c3-1 5.5-3.3 8-6.5 2.5 3.2 5 5.5 8 6.5"
private const val HomeBody = "M6 11v9h12v-9"
private const val HomeDoor = "M10 20v-4.5a2 2 0 0 1 4 0V20"

private const val LibSpine = "M12 6.5v13"
private const val LibBook = "M12 6.5C10 5 7 4.5 3.5 5v13c3.5-.5 6.5 0 8.5 1.5 2-1.5 5-2 8.5-1.5V5C17 4.5 14 5 12 6.5z"

private const val SanghaCenterBody = "M6 20c0-3.5 2.7-6 6-6s6 2.5 6 6"
private const val SanghaLeftHead = "M2.9 10a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0-3.8 0"
private const val SanghaRightHead = "M17.3 10a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0-3.8 0"
private const val SanghaLeftBody = "M2 18.5c0-2 1.2-3.6 3-4"
private const val SanghaRightBody = "M22 18.5c0-2-1.2-3.6-3-4"
private const val SanghaCenterHead = "M9 7.5a3 3 0 1 0 6 0a3 3 0 1 0-6 0"

private const val MeBody = "M4.5 20.5c.8-4 3.8-6.5 7.5-6.5s6.7 2.5 7.5 6.5"
private const val MeHead = "M8.4 8a3.6 3.6 0 1 0 7.2 0a3.6 3.6 0 1 0-7.2 0"

private const val LampFlamePath = "M12 2.5c2.6 3 3.6 5.2 3.6 7.1a3.6 3.6 0 0 1-7.2 0c0-1.9 1-4.1 3.6-7.1z"
private const val LampLotusPath = "M4.5 15.5c2 3 4.6 4.5 7.5 4.5s5.5-1.5 7.5-4.5c-2.6.3-5 .2-7.5-1.2-2.5 1.4-4.9 1.5-7.5 1.2z"

/** Dựng icon tab: các path nét + path có fill (fill = null khi không active). */
private fun tabIcon(
    name: String,
    stroke: Color,
    fill: Color?,
    strokePaths: List<String>,
    fillPaths: List<String>,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 22.dp,
    defaultHeight = 22.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    strokePaths.forEach { d ->
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            stroke = SolidColor(stroke),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    fillPaths.forEach { d ->
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = fill?.let { SolidColor(it) },
            stroke = SolidColor(stroke),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
}.build()

/** Nút đèn: ngọn lửa + đài sen, chỉ fill (không nét). */
private fun lampIcon(flame: Color): ImageVector = ImageVector.Builder(
    name = "Lamp",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    listOf(LampFlamePath, LampLotusPath).forEach { d ->
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = SolidColor(flame),
        )
    }
}.build()

@Composable
private fun HomeTabIcon(color: Color, fill: Color?) {
    val icon = remember(color, fill) {
        tabIcon("Home", color, fill, listOf(HomeSpire, HomeRoof, HomeBody), listOf(HomeDoor))
    }
    Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
}

@Composable
private fun LibTabIcon(color: Color, fill: Color?) {
    val icon = remember(color, fill) {
        tabIcon("Lib", color, fill, listOf(LibSpine), listOf(LibBook))
    }
    Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
}

@Composable
private fun SanghaTabIcon(color: Color, fill: Color?) {
    val icon = remember(color, fill) {
        tabIcon(
            "Sangha",
            color,
            fill,
            listOf(SanghaCenterBody, SanghaLeftHead, SanghaRightHead, SanghaLeftBody, SanghaRightBody),
            listOf(SanghaCenterHead),
        )
    }
    Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
}

@Composable
private fun MeTabIcon(color: Color, fill: Color?) {
    val icon = remember(color, fill) {
        tabIcon("Me", color, fill, listOf(MeBody), listOf(MeHead))
    }
    Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
}

@Preview(showBackground = true, widthDp = 400, heightDp = 130)
@Composable
private fun IlBottomNavPreview() {
    InnerLampTheme {
        Box(Modifier.background(LocalIlColors.current.canvas)) {
            IlBottomNav(active = IlBottomTab.Home)
        }
    }
}

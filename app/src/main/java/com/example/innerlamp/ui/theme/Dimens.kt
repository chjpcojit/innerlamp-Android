package com.example.innerlamp.ui.theme

import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Khoảng cách theo bậc 4 · 8 · 12 · 20 · 26 · 44 (Design System "Nhịp & bo góc").
// Lề màn 20px; khoảng cách giữa các khối 20–24px.
// ─────────────────────────────────────────────────────────────────────────────

object IlSpacing {
    val Xxxs = 4.dp
    val Xxs = 8.dp
    val Xs = 12.dp
    val Sm = 20.dp
    val Md = 26.dp
    val Lg = 44.dp
}

object IlInset {
    val Screen = 20.dp   // lề ngang màn
    val Group = 24.dp    // khoảng cách giữa các khối
}

// Chiều cao chuẩn (README "Spacing & shape").
object IlSize {
    val Button = 50.dp     // nút chính
    val Input = 48.dp      // ô nhập
    val ListRow = 72.dp    // hàng danh sách
    val IconTile = 44.dp   // tile icon
    val NavBar = 38.dp     // chip back / nút header
    val BottomNav = 110.dp // bottom nav (gồm safe area)
}

// Đổ bóng (hiếm): chỉ nút chính + thanh phát nổi.
object IlElevation {
    val Primary = 8.dp   // 0 8px 24px ink-16
    val Floating = 6.dp  // 0 6px 20px ink-07
}

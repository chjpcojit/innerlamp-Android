package com.example.innerlamp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Bo góc theo handoff "Spacing & shape":
//   hàng/thẻ nhỏ 18–20 · thẻ lớn 22–26 · input 14 · icon-tile 13 · pill 999.
// Bậc radii trong Design System ("Nhịp & bo góc"): 8 · 14 · 20 · 26 · 999.
// ─────────────────────────────────────────────────────────────────────────────

object IlRadius {
    val Xs = 8.dp          // chip/icon nhỏ
    val IconTile = 13.dp   // tile icon (IL-Row / avatar)
    val Input = 14.dp      // ô nhập (IL-Field)
    val Row = 18.dp        // hàng, thẻ nhỏ
    val Card = 20.dp       // thẻ
    val CardLarge = 26.dp  // thẻ lớn, sheet
    val Pill = 999.dp      // nút, chip, toggle
}

/** Ánh xạ bo góc cho component Material (chỉ dùng khi không vẽ tay). */
val IlShapes = Shapes(
    extraSmall = RoundedCornerShape(IlRadius.Xs),
    small = RoundedCornerShape(IlRadius.Input),
    medium = RoundedCornerShape(IlRadius.Row),
    large = RoundedCornerShape(IlRadius.Card),
    extraLarge = RoundedCornerShape(IlRadius.CardLarge),
)

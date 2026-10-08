package com.example.innerlamp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.innerlamp.ui.theme.IlColors
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldDeep
import com.example.innerlamp.ui.theme.IlGreen
import com.example.innerlamp.ui.theme.IlGreenText
import com.example.innerlamp.ui.theme.IlRose
import com.example.innerlamp.ui.theme.IlRoseText

// ─────────────────────────────────────────────────────────────────────────────
// Bộ tông màu + hình tượng dùng chung cho các component (prop `tone`, `glyph`).
// "Icon" trong prototype là hình khối CSS — giữ nguyên để không thêm dependency
// icon set; khi cần có thể thay bằng Lucide/Phosphor sau.
// ─────────────────────────────────────────────────────────────────────────────

/** Tông màu của icon/avatar/hàng (prop `tone` của prototype). */
enum class IlTone { Gold, Green, Rose, Ink }

/** Màu chính (nét/glyph) của tông. `Ink` theo `link` để đảo light/dark. */
internal fun IlTone.base(colors: IlColors): Color = when (this) {
    IlTone.Gold -> IlGold
    IlTone.Green -> IlGreen
    IlTone.Rose -> IlRose
    IlTone.Ink -> colors.link
}

/** Màu chữ phụ của tông (đặt trên nền tint). */
internal fun IlTone.fg(colors: IlColors): Color = when (this) {
    IlTone.Gold -> colors.link
    IlTone.Green -> IlGreenText
    IlTone.Rose -> IlRoseText
    IlTone.Ink -> colors.mid
}

/** Nền tint của tông (icon-tile, avatar). */
internal fun IlTone.tint(colors: IlColors): Color = when (this) {
    IlTone.Gold -> IlGold.copy(alpha = 0.16f)
    IlTone.Green -> IlGreen.copy(alpha = 0.18f)
    IlTone.Rose -> IlRose.copy(alpha = 0.18f)
    IlTone.Ink -> colors.link.copy(alpha = 0.14f)
}

/**
 * Hình giọt / lá bồ đề (`border-radius: 50% 0 50% 50%`) — 3 góc bo tròn,
 * 1 góc nhọn ở top-phải.
 */
internal val LeafShape: Shape = RoundedCornerShape(
    topStartPercent = 50,
    topEndPercent = 0,
    bottomEndPercent = 50,
    bottomStartPercent = 50,
)

/** Ngọn lửa nhỏ (teardrop nhọn ở đỉnh) — dùng cho nút đèn ở bottom nav. */
internal object LampFlameShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.98f, h * 0.5f, w * 0.95f, h * 0.82f, w * 0.5f, h)
            cubicTo(w * 0.05f, h * 0.82f, w * 0.02f, h * 0.5f, w * 0.5f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

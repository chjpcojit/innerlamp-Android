package com.example.innerlamp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Bộ token `--il-med-*` cho các màn immersive (Phiên thiền, Hoàn thành, Niệm Phật,
// Gói Sen Vàng, Chuông mõ). Đổi theo theme (light/dark) như `--il-*`, nhưng tách
// riêng vì 4 màn này dùng nền radial-gradient + bảng riêng (xem README "Dark/Light").
// Nguồn chuẩn: `<helmet>` của prototype, selector `[data-il-theme="light"|"dark"]`.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bộ màu immersive (màn thiền/chuông mõ). Chữ phụ/viền vẫn dùng [ink] với alpha —
 * vd `--il-med-12` = `ink.copy(alpha = 0.12f)` (tra alpha ở `<helmet>` prototype).
 */
data class IlMedColors(
    val bgTop: Color,     // đỉnh radial-gradient nền (50% 32%)
    val bgBottom: Color,  // đáy radial-gradient nền
    val ink: Color,       // chữ chính (`--il-med-ink`)
    val card: Color,      // thẻ trên nền med (`--il-med-card`)
    val bar: Color,       // dải header dán (`--il-med-bar`)
    val gold: Color,      // nhấn vàng (`--il-med-gold`)
    val onGold: Color,    // chữ trên nền gold (`--il-med-onGold`)
    val ring: Color,      // vòng ngoài (`--il-med-ring`)
    val ring2: Color,     // vòng trong (`--il-med-ring2`)
    val halo: Color,      // quầng đèn (`--il-med-halo`)
    val glow: Color,      // bóng nút gold (`--il-med-glow`)
    val isDark: Boolean,
)

val LightIlMedColors = IlMedColors(
    bgTop = Color(0xFFFFFDF8),
    bgBottom = Color(0xFFEFE6D5),
    ink = Color(0xFF221E19),
    card = Color(0xFFFFFDF8),
    bar = Color(0xFFFBF7EF),
    gold = Color(0xFFB8862F),
    onGold = Color(0xFFFFFDF8),
    ring = Color(0x4D8A5A18),  // rgba(138,90,24,.3)
    ring2 = Color(0x6B8A5A18), // rgba(138,90,24,.42)
    halo = Color(0x33D9963C),  // rgba(217,150,60,.2)
    glow = Color(0x478A5A18),  // rgba(138,90,24,.28)
    isDark = false,
)

val DarkIlMedColors = IlMedColors(
    bgTop = Color(0xFF241F19),
    bgBottom = Color(0xFF100E0B),
    ink = Color(0xFFF5EEE0),
    card = Color(0x0AF5EEE0),  // rgba(245,238,224,.04)
    bar = Color(0xFF221D17),
    gold = Color(0xFFD9963C),
    onGold = Color(0xFF1A1611),
    ring = Color(0x40B8862F),  // rgba(184,134,47,.25)
    ring2 = Color(0x66B8862F), // rgba(184,134,47,.4)
    halo = Color(0x47D9963C),  // rgba(217,150,60,.28)
    glow = Color(0x80D9963C),  // rgba(217,150,60,.5)
    isDark = true,
)

/** Truy cập bộ màu med hiện tại trong composable: `LocalIlMedColors.current.ink`, ... */
val LocalIlMedColors = staticCompositionLocalOf { LightIlMedColors }

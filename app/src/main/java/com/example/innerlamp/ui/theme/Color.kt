package com.example.innerlamp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Bảng màu thương hiệu InnerLamp, dịch 1–1 từ token `--il-*` của prototype.
// Nguồn chuẩn: Docs/design_handoff_innerlamp/README.md (mục "Design Tokens").
// ─────────────────────────────────────────────────────────────────────────────

// ── Nhấn vàng — giữ nguyên ở cả light/dark ───────────────────────────────────
val IlGold = Color(0xFFB8862F)          // nhấn chính, progress, focus ring
val IlGoldBright = Color(0xFFD9963C)    // ánh đèn, chấm trạng thái, glow
val IlGoldBright2 = Color(0xFFF3C673)   // phần sáng của ngọn lửa
val IlGoldSoft = Color(0xFFE8B061)      // nhấn vàng sáng (bằng link dark)
val IlGoldDeep = Color(0xFF8A5A18)      // chữ nhấn trên nền sáng (bằng link light)
val IlLampFlame = Color(0xFFE6A94E)     // ngọn lửa + đài sen nút đèn (light, trên nền ink)
val IlLampFlameDark = Color(0xFF1A1611) // ngọn lửa + đài sen nút đèn (dark, trên nền gold)

// ── Xanh (thiền/thành công) & hồng (niệm Phật/cảnh báo mềm) — cố định ─────────
val IlGreen = Color(0xFF6E8F7A)
val IlGreenText = Color(0xFF4E6C59)
val IlRose = Color(0xFFC2857F)
val IlRoseText = Color(0xFF9A5F58)
val IlDanger = Color(0xFFA7443F)

// ── Bảng màu của các màn LUÔN TỐI (splash, player, hero tối, thiệp chia sẻ) ──
// Không đi qua token — hardcode để không bị sáng lên ở light mode.
val IlDarkBgTop = Color(0xFF2A241C)
val IlDarkBgBottom = Color(0xFF100E0B)
val IlDarkInk = Color(0xFFF5EEE0)
val IlDarkInkSoft = Color(0xFFF2EBDD)

/**
 * Bộ màu đổi theo theme (light/dark). Tên giữ sát token `--il-*` để đối chiếu.
 *
 * Chữ phụ / viền dùng `ink` với alpha — vd `ink.copy(alpha = 0.42f)` tương đương
 * `--il-ink-42` (xem định nghĩa biến trong `<helmet>` của prototype để tra alpha).
 */
data class IlColors(
    val canvas: Color,   // nền màn danh sách
    val card: Color,     // thẻ, hàng, input
    val card2: Color,    // nền form / đọc kinh
    val sunk: Color,     // đáy gradient màn
    val sunk2: Color,
    val paper: Color,    // nền trang kinh
    val tint: Color,     // track, cột biểu đồ
    val tint2: Color,
    val tint3: Color,
    val ink: Color,      // chữ chính, nút primary
    val onInk: Color,    // chữ TRÊN nền `ink` (đảo cùng lúc)
    val mid: Color,      // icon trung tính
    val mid2: Color,
    val link: Color,     // chữ nhấn, link
    val hover: Color,
    val rowHover: Color,
    // Thẻ nổi bật (Pháp thoại ở Thư viện / Công đức ở Hồ sơ) — token `--il-feat-*`.
    // Kicker dùng `--il-hero-acc`, tiêu đề dùng `--il-th-ink` (reuse từ prototype).
    val featBgStart: Color,
    val featBgEnd: Color,
    val featBorder: Color,
    val featGlow: Color,
    val featKicker: Color,
    val featTitle: Color,
    // Phần còn lại của thẻ "Công đức đã hồi hướng" ở Hồ sơ: phụ đề (`--il-th-sub`),
    // viền vòng ngọn lửa (`--il-hero-acc2`) và quầng sáng ngọn lửa (`--il-th-flame`).
    val featSub: Color,
    val featAcc2: Color,
    val featFlameGlow: Color,
    // Header Trang chùa / hero có nền gradient (token `--il-th-bg`) + quầng sáng
    // (`--il-th-glow`). Gradient `180deg` tan vào `canvas` — không có đường cắt.
    val thBgTop: Color,
    val thBgMid: Color,
    val thGlow: Color,
    val isDark: Boolean,
)

val LightIlColors = IlColors(
    canvas = Color(0xFFF7F2E8),
    card = Color(0xFFFFFDF8),
    card2 = Color(0xFFFBF7EF),
    sunk = Color(0xFFF1E8D8),
    sunk2 = Color(0xFFEFE6D5),
    paper = Color(0xFFF2EBDD),
    tint = Color(0xFFE2D4BB),
    tint2 = Color(0xFFD9C7A6),
    tint3 = Color(0xFFCBB68F),
    ink = Color(0xFF221E19),
    onInk = Color(0xFFF7F2E8),
    mid = Color(0xFF4A443C),
    mid2 = Color(0xFF6A655C),
    link = Color(0xFF8A5A18),
    hover = Color(0xFFF3ECDE),
    rowHover = Color(0xFFFFFFFF),
    // --il-feat-bg: linear-gradient(140deg,#F3E8D4,#E6D5B7); kicker #74500F; title #221E19.
    featBgStart = Color(0xFFF3E8D4),
    featBgEnd = Color(0xFFE6D5B7),
    featBorder = Color(0x298A5A18), // rgba(138,90,24,.16)
    featGlow = Color(0x4DC4923C),   // rgba(196,146,60,.3)
    featKicker = Color(0xFF74500F),
    featTitle = Color(0xFF221E19),
    featSub = Color(0x99221E19),     // --il-th-sub: rgba(34,30,25,.6)
    featAcc2 = Color(0x8074500F),    // --il-hero-acc2: rgba(116,80,15,.5)
    featFlameGlow = Color(0x8CB8862F), // --il-th-flame: rgba(184,134,47,.55)
    // --il-th-bg: linear-gradient(180deg,#EBDDC3 0%,#F1E7D6 58%,var(--il-canvas) 100%).
    thBgTop = Color(0xFFEBDDC3),
    thBgMid = Color(0xFFF1E7D6),
    // --il-th-glow: rgba(196,146,60,.26).
    thGlow = Color(0x42C4923C),
    isDark = false,
)

val DarkIlColors = IlColors(
    canvas = Color(0xFF17140F),
    card = Color(0xFF211D17),
    card2 = Color(0xFF1E1A15),
    sunk = Color(0xFF191510),
    sunk2 = Color(0xFF1B1712),
    paper = Color(0xFF1F1B15),
    tint = Color(0xFF322B22),
    tint2 = Color(0xFF3E352A),
    tint3 = Color(0xFF4A4033),
    ink = Color(0xFFF2EBDD),
    onInk = Color(0xFF17140F),
    mid = Color(0xFFB8B1A5),
    mid2 = Color(0xFF9C958A),
    link = Color(0xFFE8B061),
    hover = Color(0xFF2A251E),
    rowHover = Color(0xFF2A251E),
    // --il-feat-bg: linear-gradient(140deg,#2A2620,#181510); kicker rgba(217,150,60,.95); title #F5EEE0.
    featBgStart = Color(0xFF2A2620),
    featBgEnd = Color(0xFF181510),
    featBorder = Color.Transparent,
    featGlow = Color(0x47D9963C),   // rgba(217,150,60,.28)
    featKicker = Color(0xF2D9963C), // rgba(217,150,60,.95)
    featTitle = Color(0xFFF5EEE0),
    featSub = Color(0x8CF5EEE0),    // --il-th-sub: rgba(245,238,224,.55)
    featAcc2 = Color(0x8CD9963C),   // --il-hero-acc2: rgba(217,150,60,.55)
    featFlameGlow = Color(0xCCD9963C), // --il-th-flame: rgba(217,150,60,.8)
    // --il-th-bg: linear-gradient(180deg,#2A2620 0%,#1D1914 58%,var(--il-canvas) 100%).
    thBgTop = Color(0xFF2A2620),
    thBgMid = Color(0xFF1D1914),
    // --il-th-glow: rgba(217,150,60,.22).
    thGlow = Color(0x38D9963C),
    isDark = true,
)

/** Truy cập bộ màu hiện tại trong composable: `LocalIlColors.current.ink`, ... */
val LocalIlColors = staticCompositionLocalOf { LightIlColors }

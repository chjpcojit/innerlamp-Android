# InnerLamp Design System → Compose

Bản dịch từ prototype (`Docs/design_handoff_innerlamp/`) sang Compose. Đây là bản rút gọn cho việc
code; khi có mâu thuẫn, prototype README là nguồn chuẩn.

## Color tokens → `Color.kt`

Tên token giữ sát `--il-*` để đối chiếu 1–1 với prototype. Khai báo một `val` cho mỗi token; token
đổi theo theme cần hai giá trị (Light / Dark).

### Thay đổi theo theme

| Token | Light | Dark | Compose |
|---|---|---|---|
| canvas | `#F7F2E8` | `#17140F` | `Color(0xFFF7F2E8)` / `Color(0xFF17140F)` |
| card | `#FFFDF8` | `#211D17` | `Color(0xFFFFFDF8)` / `Color(0xFF211D17)` |
| card2 | `#FBF7EF` | `#1E1A15` | `Color(0xFFFBF7EF)` / `Color(0xFF1E1A15)` |
| sunk / sunk2 | `#F1E8D8` / `#EFE6D5` | `#191510` / `#1B1712` | dùng cho gradient đáy màn |
| paper | `#F2EBDD` | `#1F1B15` | nền trang kinh |
| tint → tint3 | `#E2D4BB` → `#CBB68F` | `#322B22` → `#4A4033` | track, cột biểu đồ |
| ink | `#221E19` | `#F2EBDD` | chữ chính, nút primary |
| mid / mid2 | `#4A443C` / `#6A655C` | `#B8B1A5` / `#9C958A` | icon trung tính |
| link | `#8A5A18` | `#E8B061` | chữ nhấn, link |
| onink | `#F7F2E8` | `#17140F` | chữ **trên** nền `ink` (đảo cùng lúc) |
| hover / rowhover | `#F3ECDE` / `#FFFFFF` | `#2A251E` | trạng thái hover |

Chữ phụ/viền dùng `ink` với alpha theo 39 mức: `Color(0xFF221E19).copy(alpha = NN/100f)` (light) /
`Color(0xFFF2EBDD).copy(alpha = NN/100f)` (dark).

### Không đổi theo theme (giữ nguyên ở cả hai chế độ)

- Nhấn vàng `#B8862F`, `#D9963C`, `#F3C673`, `#E8B061`; xanh `#4E6C59`/`#6E8F7A`;
  hồng `#9A5F58`/`#C2857F`; đỏ lỗi `#A7443F`.
- Các màn **luôn tối**: `splash`, `player`, thẻ hero tối, thiệp chia sẻ kiểu 1. Chúng dùng bảng
  `#2A241C → #100E0B` + `#F5EEE0` riêng, không đi qua token (hardcode để không sáng lên ở light mode).
- Bộ `--il-med-*` cho 4 màn immersive (Phiên thiền, Hoàn thành, Niệm Phật, Gói Sen Vàng):
  `--il-med-bg`, `--il-med-ink`, `--il-med-bar`, `--il-med-gold`, `--il-med-ring` — có light + dark.

Nền tối: `radial-gradient(circle at 50% 30%, #2A241C, #100E0B 74%)` cho màn thiền/player.
Quầng đèn: `radial-gradient(circle, rgba(217,150,60,.22), transparent 66%)` →
`Brush.radialGradient(listOf(Color(0x38D9963C), Color.Transparent))`.

## Typography → `Type.kt`

- Display / kinh văn: **Lora** (serif) weight 400 — tiêu đề màn 26–30sp, câu kinh 15–21sp
  (line-height 1.6–1.95), số lớn.
- Body / UI: **Be Vietnam Pro** weight 400/500/600 — 10sp nhãn caps (letter-spacing .14em),
  11–11.5sp phụ, 12.5–14sp body, 13.5sp nhãn nút.
- Tiêu đề section 500 13sp; Eyebrow 600 10sp, letter-spacing .14em, uppercase,
  `ink` alpha `.42`.
- Cả hai font từ Google Fonts, subset `vietnamese`.

## Spacing & shape → `Dp`

- Padding màn `0 / 20 / 30` (status-bar chừa nằm trong header dán `padding-top: 56px`).
- Khoảng cách nhóm 18–24dp; giữa các hàng trong nhóm 10dp.
- Bo góc: hàng/thẻ nhỏ 18–20dp, thẻ lớn 22–26dp, input 14dp, icon-tile 13dp, nút pill 999dp.
- Chiều cao: nút 50dp, input 48dp, hàng danh sách 72dp, icon-tile 44dp, bottom nav 110dp (gồm safe
  area).
- Shadow (hiếm): nút primary `0 8px 24px rgba(34,30,25,.16)`; thanh phát nổi `0 6px 20px rgba(34,30,25,.07)`.

## Components → `ui/components/`

Đặt tên `IlXxx`. Dùng icon set thật (Lucide/Phosphor, nét mảnh) 13–15sp trong tile 42–44dp.

| Component | Props | States cần làm |
|---|---|---|
| `IlButton` | `label, variant(primary\|gold\|ghost\|light\|onDark\|danger), state(default\|loading\|disabled), loadingLabel, full, onTap` | hover đổi nền, pressed `scale(.978)`, loading (spinner 14dp + nhãn riêng), disabled (nền `ink` `.12`, chữ `.38`) |
| `IlChip` | `label, state(default\|selected\|disabled), count, dot, dark, onTap` | pressed `scale(.96)`, selected (đảo nền/chữ), disabled |
| `IlField` | `label, value, placeholder, state(default\|focused\|error\|success\|disabled), error, hint, trailing, counter, dark` | focused (viền gold + ring `rgba(184,134,47,.16)`), error (viền `#C2857F`, chữ `#A7443F`), success `#6E8F7A`, disabled |
| `IlRow` | `title, sub, trailing, tone(gold\|green\|rose\|ink), glyph, state, dark, onTap` | selected (nền `rgba(184,134,47,.10)`, viền gold), disabled opacity `.5` |
| `IlSettingRow` | `title, sub, kind(chevron\|switch\|value), trailing, on, last, dark, onTap` | switch on/off (track `#B8862F`, knob dịch 18dp) |
| `IlNavBar` | `title, sub, right, showBack, showClose, dark, onBack, onClose, onAction` | header dán, chip back 38dp, full-bleed, hairline `ink` `.06`; hero tối dùng kính mờ `rgba(21,18,16,.72)` + blur |
| `IlBottomNav` | `active(home\|lib\|lamp\|sangha\|me), onNav` | tab active, nút đèn nổi giữa |
| `IlAvatar` | `initials, tone, size(24–96)` | — |
| `IlState` | `kind(empty\|error\|offline\|done), title, body, dark` | — |

## Interactions

- Chuyển màn crossfade mặc định (fade out 200ms → fade in); modal trượt lên từ đáy (paywall, ghi
  danh, viết bài, chia sẻ) kèm nút ✕.
- Animation riêng: vòng thở phiên thiền (scale 1→1.12, 4s vào/6s ra); hạt nhấn `scale(.93)` 120ms;
  skeleton `ilpulse` (opacity .55↔.95, 1.6s); spinner nút 0.9s linear.
- Form: OTP 6 số; SĐT 10 số; mật khẩu ≥ 8 ký tự; CTA disabled tới khi tick điều khoản.
- Màn `kit` (Bộ component) hiển thị toàn bộ state — dùng làm checklist khi code component library.

## Theme mode

Màn `lang` có 3 lựa chọn Sáng/Tối/Theo máy → nối vào state `theme` (0/1/2). *Theo máy* đọc
`isSystemInDarkTheme()` và lắng nghe đổi khi đang mở app. `dynamicColor = false` (không để Material
You đè bảng màu thương hiệu).

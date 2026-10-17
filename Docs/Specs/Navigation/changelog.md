# Changelog — Navigation Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.1 (2026-10-17)

- `requirements.md`, `design.md`, `tasks.md`: the centre lamp button now opens Thắp đèn (see `../LightLamp/`) instead of the meditation setup screen; `SetupRoute` stays reachable from Home and the Meditation area. Only the lamp-button lines changed; the rest of the files is untouched.
- Reason: product spec `docs/product/lamp/product.md` v1.0 (decision of 2026-10-16) and the merged `LightLamp` spec. The edit is surgical and the older Vietnamese prose is not translated yet (no translation-only change).

## v1.0 (2026-09-25)

- `requirements.md`: khởi tạo (mode **mixed**) — user story (lớp điều hướng type-safe + hệ transition
  chuyển màn), 18 tiêu chí EARS chia 7 nhóm (route type-safe, transition chuyển màn, back stack &
  start destination, decoupling, bottom-nav shell, modal, thuần client-side), Out of scope (animation
  nội màn, nội dung từng màn, route từng feature, deep link, predictive back, Navigation 3), Pending;
  mọi câu hỏi đã chốt ở "Open questions (resolved)": gộp 1 spec `Navigation`, mode mixed, animation
  scope chỉ transition chuyển màn, crossfade chuẩn **200ms**.
- `design.md`: khởi tạo — kiến trúc cross-section navigation ở `ui/navigation/` (Navigation Compose
  type-safe + `NavTransitionSpec`); bảng Layers & components (6 file đã có + 2 file mới
  `BottomNavShell.kt`/`ModalDestination.kt`); sơ đồ Navigation flow (Splash → Onboarding → 5 tab);
  mục "Transition contract" với bảng 5 preset + quy tắc push/pop + `sizeTransform` + quyết định
  200ms; mục "Bottom nav shell" (đề xuất single NavHost + saveState/restoreState) và "Modal
  presentation" (slide-up); nêu rõ Step-by-step Flow **không áp dụng** (thuần client-side, không có
  gọi mạng); mục "Deliberately stubbed" cho TODO onboarding, bottom nav chưa nối, modal chưa có,
  crossfade 220ms.
- `tasks.md`: khởi tạo — 6 Phase (Đã có retroactive `[x]` → sửa crossfade 200ms → bottom nav shell →
  route đầy đủ + modal → test → docs), phần chưa làm giữ `[ ]`; mục Deferred cho deep link,
  predictive back, Navigation 3, animation nội màn.
- Reason: viết spec mixed cho lớp điều hướng + hệ transition chuyển màn. Người dùng chốt: gộp "base
  navigation" + "custom animation" thành một spec `Navigation` (vì `NavTransitionSpec` nằm trong
  `ui/navigation/`); mode mixed (phần đã có đánh `[x]`, phần thiếu prospective); animation scope chỉ
  transition chuyển màn; crossfade chuẩn 200ms theo design handoff (code đang 220ms, cần sửa). Còn 1
  `⚠️ Not confirmed` được ghi lại: mô hình back-stack bottom nav (single NavHost + saveState/
  restoreState vs nested NavHost vs Navigation 3 Scenes) — chặn `tasks.md` Phase 3.

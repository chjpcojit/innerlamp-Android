# Tasks — Navigation

Version: 1.1
Last updated: 2026-10-17
Status: in-progress — phần đã có (retroactive) đánh `[x]`; bottom-nav shell/route/modal còn `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Mọi task dưới đây trace trực tiếp về bảng **Layers & components** và/hoặc sơ đồ **Navigation flow**
trong `design.md`. Phần đã implement được đánh `[x]` (đã kiểm tra file thật); phần còn thiếu giữ
`[ ]`.

## Phase 1 — Đã có (retroactive, `[x]`)

- [x] `ui/navigation/IlDestination.kt` — interface `transitions: NavTransitionSpec` default `Crossfade`.
- [x] `ui/navigation/NavTransitionSpec.kt` — 4 lambda (enter/exit/popEnter/popExit) + `sizeTransform`; 5
      preset `Crossfade`/`FadeThrough`/`SlideHorizontal`/`SlideVertical`/`None`; tự dựng tuỳ ý.
- [x] `ui/navigation/Routes.kt` — `SplashRoute`, `OnboardingRoute` (`@Serializable object`).
- [x] `ui/navigation/AppNavHost.kt` — global crossfade + extension `ilComposable(screen, content)`.
- [x] `MainActivity.kt` — `AppNavHost(startDestination = SplashRoute)`, nối Splash → Onboarding
      (`popUpTo<SplashRoute> { inclusive = true }`).
- [x] `ui/components/IlBottomNav.kt` — component 5 tab + nút đèn nổi (chỉ UI, chưa nối nav).

## Phase 2 — Sửa crossfade 200ms (đã chốt)

- [ ] Sửa `DefaultCrossfadeDurationMs` trong `AppNavHost.kt`: `220` → `200`.
- [ ] Sửa `NavTransitionSpec.Crossfade` (`NavTransitionSpec.kt`): cả 4 `tween(220)` → `tween(200)`.
- [ ] Đồng bộ comment "Khớp với prototype `tween(200)`" cho khớp giá trị thật.
- [ ] Build xanh + kiểm tra bằng mắt crossfade không đổi cảm nhận (vẫn mượt).

## Phase 3 — Bottom nav shell (prospective)

- [ ] Chốt mô hình back-stack tab — xem `design.md` mục "Bottom nav shell" (`⚠️ Not confirmed`: single
      NavHost + `saveState`/`restoreState` vs nested NavHost vs Navigation 3 Scenes).
- [ ] Tạo `ui/navigation/BottomNavShell.kt` theo mô hình đã chốt: `Scaffold(bottomBar = IlBottomNav)`
      + 5 graph tab (Home/Lib/Lamp/Sangha/Me), mỗi tab `navigation(...)` có `startDestination` riêng.
- [ ] Nối `IlBottomNav.onNav` → `navigate(...)` với `popUpTo(graphStart){saveState}` +
      `launchSingleTop` + `restoreState`; `active` tính theo route hiện tại.
- [ ] Bấm lại tab đang active → `popBackStack(graphStart, inclusive = false)` (về gốc tab).
- [ ] Nút đèn (Lamp) → màn Thắp đèn (`../LightLamp/`); thiền vào được từ Home và khu Thiền.
- [ ] Ẩn bottom nav ở splash/onboarding/login/signup/modal (chỉ hiện ở 5 tab gốc).
- [ ] Xác nhận giữ back stack + scroll state khi chuyển tab và xoay màn hình (state restore).

## Phase 4 — Route đầy đủ + modal (prospective)

- [ ] Thêm route luồng auth vào `Routes.kt`: `LoginRoute`, `SignupRoute`, `OtpRoute`, `ForgotRoute`
      (mỗi route `@Serializable`, implement `IlDestination`, chọn transition phù hợp).
- [ ] Thêm route 5 tab gốc + màn con (`HomeRoute`, `LibraryRoute`, `SetupRoute`, `SanghaRoute`,
      `MeRoute`, …) khi các feature tương ứng có màn.
- [ ] Nối `onComplete`/`onSkip`/`onLogin` (TODO trong `MainActivity`) → route thật.
- [ ] Tạo `ui/navigation/ModalDestination.kt`: cơ chế modal slide-up cho paywall/ghi danh/viết
      bài/chia sẻ (`composable(dialog = true)` với transition trượt lên từ đáy + nút ✕), không đẩy
      vào stack push thông thường.
- [ ] Xác nhận back ở modal đóng modal (không pop nhầm màn bên dưới).

## Phase 5 — Test

- [ ] Unit test JVM `NavTransitionSpec`: mỗi preset trả transition không `null` đúng hướng (ví dụ
      `SlideHorizontal.enter` dùng `SlideDirection.Left`, `popExit` dùng `SlideDirection.Right`).
- [ ] Unit test `IlDestination` default = `NavTransitionSpec.Crossfade`; override trả preset khác.
- [ ] Unit test `NavTransitionSpec.None` trả `EnterTransition.None`/`ExitTransition.None`.
- [ ] (Nếu dùng route `data class`) test tham số route serialize/deserialize đúng.
- [ ] Manual QA: push/pop animation đúng hướng; modal slide-up + nút ✕; chuyển tab giữ stack;
      bấm lại tab active về gốc; crossfade 200ms.

## Phase 6 — Documentation

- [ ] Tạo `ui/navigation/README.md`: cách khai báo route type-safe, cách override `transitions`, bảng
      preset + duration, quy tắc back stack (splash popUpTo, tab saveState/restoreState), cơ chế modal.
- [ ] Cập nhật `AGENTS.md` (+ `CLAUDE.md` nếu còn stale): `ui/navigation/` là nơi điều hướng chuẩn
      (type-safe + `IlDestination`/`NavTransitionSpec`); màn hình nhận callback, không giữ
      `NavController`.
- [ ] (Sau khi shell end-to-end) ghi chú OKF cho navigation dưới `Docs/Knowledge/` — chỉ làm khi hành
      vi đã end-to-end, không mô tả stub.

## Deferred (feature khác sở hữu, không block Navigation)

- **Deep link / universal link** — chưa trong phạm vi v1.
- **Predictive back animation (`androidx.navigationevent`)** — chưa trong phạm vi v1.
- **Navigation 3 Scenes / multi-pane adaptive** — chưa trong phạm vi v1 (nếu cần sẽ là spec riêng).
- **Animation nội màn** (splash flame/ripple, vòng thở phiên thiền 4s/6s, skeleton `ilpulse`, spinner
  nút, transition component `.16s/.12s`) — thuộc từng màn/component, xem `requirements.md` "Out of
  scope".
- **Route cụ thể của từng feature** — mỗi feature tự khai báo khi build màn của nó.

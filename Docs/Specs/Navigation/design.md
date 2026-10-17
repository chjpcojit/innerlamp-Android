# Design — Navigation

Version: 1.1
Last updated: 2026-10-17
Status: in-progress — type-safe navigation + transition đã có; bottom-nav shell, route đầy đủ, modal còn thiếu

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Lớp điều hướng dùng chung ở tầng UI, đặt trong `ui/navigation/`, thuộc tầng presentation của Clean
Architecture hiện tại: `ui → domain ← data → core/{storage,network}`. Đây là **cross-section
navigation** (shell gốc của app), được sở hữu tại `MainActivity` + `ui/navigation/` — không thuộc
một feature riêng; mọi màn hình đều đi qua đây.

Dùng **Navigation Compose type-safe** (`androidx.navigation.compose`) + một "hợp đồng" transition
`NavTransitionSpec` mà mỗi route (`IlDestination`) tự khai báo. Màn hình không phụ thuộc
`NavController` — nhận callback và để lớp navigation lo việc chuyển màn (đúng pattern
`SplashScreen`/`OnboardingScreen` hiện có).

## Layers & components

| Layer | File | Responsibility |
|---|---|---|
| ui/navigation | `ui/navigation/IlDestination.kt` | **Interface** cho mọi destination: `val transitions: NavTransitionSpec` default `Crossfade`. Route `@Serializable object`/`data class` implement interface này. |
| ui/navigation | `ui/navigation/NavTransitionSpec.kt` | **"Hợp đồng" animation chuyển màn**: 4 lambda `enter`/`exit`/`popEnter`/`popExit` (receiver `AnimatedContentTransitionScope`) + `sizeTransform`. Preset trong `companion`: `Crossfade`, `FadeThrough`, `SlideHorizontal`, `SlideVertical`, `None`; tự dựng `NavTransitionSpec(...)` tuỳ ý. |
| ui/navigation | `ui/navigation/Routes.kt` | Tập trung route type-safe (`@Serializable object/data class`). Hiện có `SplashRoute`, `OnboardingRoute`; sẽ thêm Login/Signup/OTP + 5 tab gốc + màn con. |
| ui/navigation | `ui/navigation/AppNavHost.kt` | `NavHost` chính: global default crossfade (enter/exit/popEnter/popExit) + extension `ilComposable(screen, content)` tự đọc `screen.transitions`. |
| app / host | `MainActivity.kt` | Dựng `rememberNavController()` + `AppNavHost(startDestination = SplashRoute)`, nối Splash → Onboarding. **Sửa file** (thêm route mới). |
| ui/components | `ui/components/IlBottomNav.kt` | Component bottom nav 5 tab (`IlBottomTab`: Home/Lib/Lamp/Sangha/Me) + nút đèn nổi (mở Thắp đèn, xem `../LightLamp/`), `hidden` trượt xuống. Chỉ là UI; **chưa nối** vào nav. |
| ui/navigation | `ui/navigation/BottomNavShell.kt` (mới) | **Shell bottom nav**: 5 graph tab (mỗi tab một `navigation(...)`), `saveState`/`restoreState`, `popUpTo` gốc khi bấm lại tab, nút đèn → Thắp đèn (`../LightLamp/`). |
| ui/navigation | `ui/navigation/ModalDestination.kt` (mới) | Cơ chế trình bày modal slide-up (dialog destination / `composable(dialog = true)`) cho paywall/ghi danh/viết bài/chia sẻ. |

## Navigation flow

```
MainActivity (FragmentActivity)
  └─ AppNavHost(navController, startDestination = SplashRoute)
       ├─ SplashRoute      → onFinished → navigate(OnboardingRoute) { popUpTo<SplashRoute>{inclusive=true} }
       ├─ OnboardingRoute  → onComplete/onSkip → SignupRoute   // TODO (Phase 4)
       │                     onLogin             → LoginRoute    // TODO (Phase 4)
       └─ [prospective] BottomNavShell — 5 tab gốc (Home / Lib / Lamp / Sangha / Me)
             ├─ Trang chủ   (HomeRoute)
             ├─ Thư viện    (LibraryRoute)
             ├─ Đèn         (LampRoute — nút đèn giữa, xem ../LightLamp/)
             ├─ Đạo tràng   (SanghaRoute)
             └─ Tôi         (MeRoute)
```

- Màn modal (paywall, ghi danh, viết bài, chia sẻ) **không** nằm trong stack push thông thường mà
  trình bày trượt lên từ đáy (Phase 4).

## Transition contract (custom animation)

`NavTransitionSpec` là trung tâm của phần "custom animation". Mỗi route implement `IlDestination` và
override `transitions` để khai báo transition riêng; không override → dùng `Crossfade` (khớp global
default của `AppNavHost`).

| Preset | enter | exit | popEnter | popExit | Dùng cho |
|---|---|---|---|---|---|
| `Crossfade` | fadeIn 200ms | fadeOut 200ms | fadeIn 200ms | fadeOut 200ms | mặc định toàn app |
| `FadeThrough` | fadeIn + scale .96→1 (300ms) | fadeOut + scale 1→.96 (300ms) | như enter | như exit | chuyển tab / section |
| `SlideHorizontal` | slide vào từ phải (300ms) | trượt ra ⅓ trái | slide vào từ trái | trượt ra phải | push màn chi tiết |
| `SlideVertical` | slide lên từ dưới (300ms) | trượt lên ⅓ | slide xuống | trượt xuống | modal dạng full-screen |
| `None` | `EnterTransition.None` | `ExitTransition.None` | — | — | chuyển tức thì |

Quy tắc:

- **Mỗi lambda nhận `AnimatedContentTransitionScope<NavBackStackEntry>`**, nên có thể đọc
  `initialState`/`targetState` để quyết định animation động (không bị giới hạn vào preset). Trả
  `null` = không animate hướng đó.
- **`sizeTransform`** cho phép animate kích thước giữa hai màn (dạng shared-element/size); mặc định
  `null` = giữ nguyên hành vi NavHost.
- **push vs pop dùng cặp khác nhau** (`enter`/`exit` cho push, `popEnter`/`popExit` cho back), để
  back luôn đảo đúng hướng.
- `ilComposable(screen)` đọc `screen.transitions` và truyền đủ 4 transition + `sizeTransform` vào
  `composable<T>(...)` — màn hình route `data class` (có tham số, không có singleton) phải truyền
  transition tường minh khi đăng ký (xem doc `IlDestination`).

### Thời lượng crossfade — 200ms (đã chốt)

Design handoff (`Docs/design_handoff_innerlamp/README.md`) ghi crossfade mặc định **200ms**; code
hiện tại đang dùng **220ms** (`DefaultCrossfadeDurationMs` trong `AppNavHost` và
`NavTransitionSpec.Crossfade`). Chốt (2026-09-25): dùng **200ms**; cần sửa 2 chỗ (xem `tasks.md`
Phase 2). Các duration khác (FadeThrough/Slide 300ms, onboarding step crossfade 170ms) giữ nguyên —
onboarding step crossfade là animation **nội màn**, không thuộc spec này.

## Step-by-step Flow

Không áp dụng — navigation/animation là **thuần client-side**, không có lời gọi API/network nào nên
rule 5 (Step-by-step Flow cho luồng ≥2 lời gọi mạng) **không kích hoạt**. Hành vi điều hướng đã được
mô tả đầy đủ bằng sơ đồ "Navigation flow" và các tiêu chí EARS ở `requirements.md`.

## Bottom nav shell (prospective)

Nối `IlBottomNav` (component đã có) vào nav để tạo shell 5 tab gốc. Cấu trúc đề xuất:

```
BottomNavShell
  └─ Scaffold(bottomBar = { IlBottomNav(active = currentTab, onNav = ...) })
       └─ NavHost
            ├─ navigation("home", start = HomeRoute) { ... }      // saveState/restoreState
            ├─ navigation("lib",  start = LibraryRoute) { ... }
            ├─ navigation("lamp", start = LampRoute) { ... }
            ├─ navigation("sangha", start = SanghaRoute) { ... }
            └─ navigation("me",   start = MeRoute) { ... }
```

- Chuyển tab: `navigate(tabGraph) { popUpTo(graphStart) { saveState = true }; launchSingleTop = true;
  restoreState = true }` → giữ back stack + scroll của từng tab.
- Bấm lại tab đang active → `popBackStack(graphStart, inclusive = false)` để về gốc tab.
- Nút đèn (Lamp) → màn Thắp đèn (`../LightLamp/`); thiền vào được từ Home và khu Thiền (`SetupRoute`).
- Bottom nav chỉ hiện ở 5 tab gốc; ẩn ở splash/onboarding/login/signup/modal.

> ⚠️ Not confirmed: mô hình back-stack của bottom nav — dùng **single `NavHost` + `saveState`/
> `restoreState`** (đề xuất, khớp Navigation Compose hiện có) hay **nested `NavHost` mỗi tab** hay
> **Navigation 3 Scenes** (multi-back-stack mới)? — chặn `tasks.md` Phase 3 (chọn sai phải đổi cấu
> trúc shell).

## Modal presentation (prospective)

Màn modal (paywall, ghi danh, viết bài, chia sẻ) trình bày **trượt lên từ đáy** theo design handoff
("modal trượt lên từ đáy, có nút ✕"). Trong Navigation Compose: đăng ký bằng `composable(dialog =
true)` với transition `SlideVertical`/slide-up + nút ✕ để đóng; hoặc dùng `dialog {}` nếu cần lớp
`Dialog`. Chọn đúng hình thức ở `tasks.md` Phase 4.

## Deliberately stubbed for now (if applicable)

- `MainActivity` đã nối Splash → Onboarding, nhưng `onComplete`/`onSkip`/`onLogin` của onboarding vẫn
  là `TODO` (chưa có route Signup/Login) — theo dõi ở `tasks.md` Phase 4.
- `IlBottomNav` chỉ là component UI, **chưa** nối vào nav (chưa có `BottomNavShell`) — `tasks.md`
  Phase 3.
- Chưa có cơ chế modal slide-up — `tasks.md` Phase 4.
- Crossfade đang 220ms, chưa sửa về 200ms — `tasks.md` Phase 2.
- `NavTransitionSpec` đã có 5 preset; chưa có test nào xác nhận hành vi push/pop — `tasks.md` Phase 5.

> Còn **1** `⚠️ Not confirmed` (mô hình back-stack bottom nav) — xem mục "Bottom nav shell". Các câu
> hỏi còn lại đã chốt ở `requirements.md` mục "Open questions (resolved)".

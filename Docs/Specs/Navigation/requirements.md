# Requirements — Navigation

Version: 1.1
Last updated: 2026-10-17
Status: in-progress — type-safe navigation + transition đã có (Splash/Onboarding); bottom-nav shell, route đầy đủ, modal slide-up còn thiếu
Feature folder: `app/src/main/java/com/example/innerlamp/ui/navigation/`

> See `changelog.md` in this folder for the version history of this spec.

Spec này viết **mixed** — retroactive cho phần đã implement (cơ chế route type-safe + hệ transition
chuyển màn, đã nối Splash → Onboarding trong `MainActivity`) và prospective cho phần còn thiếu
(bottom-nav shell 5 tab, bộ route đầy đủ, modal slide-up). Đây **không** phải một màn hình — là
**lớp điều hướng + hệ animation chuyển màn dùng chung** ở tầng UI, phục vụ mọi màn hình sau này.

Phạm vi "custom animation" đã chốt (xem mục Open questions): **chỉ transition chuyển màn**
(`NavTransitionSpec`). Animation **nội màn** (splash flame, vòng thở, skeleton pulse, component
transition) thuộc từng màn/component, **không** nằm trong spec này.

## User Story

Là lập trình viên của InnerLamp, tôi muốn một lớp điều hướng type-safe (Navigation Compose) kèm hệ
transition chuyển màn cho từng màn hình tự khai báo, để mọi màn hình điều hướng nhất quán, không lặp
lại code nav/transition, và mỗi màn hình tự quyết định animation vào/ra của nó.

## Acceptance Criteria (EARS notation)

### Cơ chế route type-safe

- WHEN một màn hình được đăng ký vào nav graph, THE SYSTEM SHALL khai báo route bằng kiểu
  `@Serializable` implement `IlDestination` (`object` cho màn không tham số, `data class` cho màn có
  tham số), sao cho `navigate(...)` được kiểm tra **lúc compile** (không dùng route chuỗi).
- WHEN một màn hình có tham số, THE SYSTEM SHALL khai báo route là `@Serializable data class` với
  các tham số là field của class, để đối số truyền vào được type-check.

### Transition chuyển màn (custom animation)

- WHEN một destination không override `transitions`, THE SYSTEM SHALL chuyển màn bằng **crossfade mặc
  định 200ms** cho cả 4 hướng (enter / exit / popEnter / popExit).
- WHEN một destination override `transitions`, THE SYSTEM SHALL dùng transition riêng của destination
  đó thay cho crossfade mặc định.
- WHEN một màn hình muốn khai báo transition, THE SYSTEM SHALL cung cấp sẵn các preset `Crossfade`,
  `FadeThrough`, `SlideHorizontal`, `SlideVertical`, `None`, và SHALL cho phép tự dựng
  `NavTransitionSpec(...)` tuỳ ý (không giới hạn vào preset).
- WHEN người dùng quay lại (back/pop), THE SYSTEM SHALL chạy `popEnter`/`popExit` đảo đúng hướng của
  push (ví dụ push trượt vào từ cạnh phải thì pop lùi ra cạnh phải), không tái dùng `enter`/`exit`.
- WHEN một destination cần animate kích thước (dạng shared-element/size), THE SYSTEM SHALL hỗ trợ qua
  `sizeTransform` trong `NavTransitionSpec`; màn không cần thì bỏ trống.
- WHEN một hướng không muốn animate, THE SYSTEM SHALL cho phép trả `null` cho hướng đó (không
  animate hướng đó), thay vì bắt buộc phải có transition.

### Back stack & start destination

- WHEN app khởi động, THE SYSTEM SHALL bắt đầu từ `SplashRoute`.
- WHEN splash hoàn tất và điều hướng sang onboarding, THE SYSTEM SHALL xoá splash khỏi back stack
  (`popUpTo<SplashRoute> { inclusive = true }`), để bấm Back ở onboarding **không** quay lại splash.
- WHEN người dùng bấm Back ở màn gốc của một flow, THE SYSTEM SHALL pop hoặc kết thúc flow theo quy
  tắc đã định, không để kẹt back stack (không tạo màn trùng lặp).

### Decoupling (màn hình không phụ thuộc NavController)

- WHEN một màn hình composable cần điều hướng, THE SYSTEM SHALL nhận callback
  (`onFinished`/`onComplete`/`onSkip`/`onLogin`…) thay vì gọi trực tiếp `NavController`, để màn hình
  không phụ thuộc lớp navigation (giữ đúng pattern `SplashScreen`/`OnboardingScreen` hiện có).

### Bottom nav shell (prospective)

- WHEN người dùng ở khu vực chính (đã đăng nhập), THE SYSTEM SHALL hiển thị bottom nav 5 mục gốc —
  Trang chủ / Thư viện / Đèn / Đạo tràng / Tôi — với nút đèn nổi ở giữa (`IlBottomNav`).
- WHEN người dùng chuyển tab, THE SYSTEM SHALL giữ back stack riêng của từng tab (quay lại tab cũ
  giữ nguyên stack và vị trí cuộn), và SHALL **không** đẩy tab đó lên đầu stack mới mỗi lần bấm.
- WHEN người dùng bấm lại tab đang active, THE SYSTEM SHALL pop tab đó về màn gốc của nó (hành vi
  chuẩn bottom-nav), không đẩy thêm bản sao.
- WHEN the user taps the centre lamp button, THE SYSTEM SHALL open the Thắp đèn (light a lamp) screen; the behavior is specified in `../LightLamp/` and meditation stays reachable from Home and the Meditation area.

### Modal (prospective)

- WHEN một màn hình là modal (paywall, ghi danh, viết bài, chia sẻ…), THE SYSTEM SHALL trình bày
  **trượt lên từ đáy** (không phải push crossfade/slide ngang), có nút ✕ để đóng.

### Thuần client-side

- WHEN navigation/transition hoạt động, THE SYSTEM SHALL **không** thực hiện bất kỳ lời gọi mạng nào;
  toàn bộ hành vi điều hướng/animation là thuần client-side.

## Out of scope (ranh giới feature này, không phải việc của feature khác)

- **Animation nội màn** (splash flame/ripple/glow, vòng thở phiên thiền 4s/6s, skeleton `ilpulse`,
  spinner nút, transition component `background/border .16s`): thuộc từng màn/component — đã chốt
  (xem Open questions), **không** nằm trong Navigation.
- **Nội dung từng màn hình** (composable, ViewModel, UiState của Splash/Onboarding/Login/…): mỗi
  feature tự sở hữu; Navigation chỉ host và quyết định transition vào/ra.
- **Route cụ thể của từng feature** (Login, Signup, OTP, Home, Library, Setup, Sangha, Me…): mỗi
  feature tự khai báo route khi build; spec này chỉ định nghĩa **cơ chế** + **shell** + quy tắc.
- **Deep link / universal link**: chưa trong phạm vi v1 (Deferred).
- **Predictive back animation (`androidx.navigationevent`)**: chưa trong phạm vi v1 (Deferred).
- **Navigation 3 Scenes / multi-pane adaptive**: chưa trong phạm vi v1 (Deferred).

## Pending (not yet implemented)

- Bottom-nav shell 5 tab (nối `IlBottomNav` vào nav, back stack mỗi tab) — theo dõi ở `tasks.md`
  Phase 3.
- Bộ route đầy đủ cho luồng auth + 5 tab gốc + màn con — `tasks.md` Phase 4.
- Modal slide-up (paywall/ghi danh/viết bài/chia sẻ) — `tasks.md` Phase 4.
- Sửa crossfade 220ms → **200ms** (theo design handoff, chốt 2026-09-25) — `tasks.md` Phase 2.
- Test transition + quy tắc back stack — `tasks.md` Phase 5.

## Open questions (resolved during implementation)

- ~~Gộp "base navigation" + "custom animation" thành một spec hay tách hai?~~ → **gộp 1 spec
  `Navigation`**, vì `NavTransitionSpec` nằm trong `ui/navigation/` và chỉ dùng để animate chuyển
  màn. Resolved 2026-09-25.
- ~~Mode prospective hay retroactive?~~ → **mixed**: retroactive cho phần đã có
  (`IlDestination`/`NavTransitionSpec`/`Routes`/`AppNavHost`/nối Splash→Onboarding), prospective cho
  phần còn thiếu (bottom-nav shell, route đầy đủ, modal). Resolved 2026-09-25.
- ~~Phạm vi "custom animation"?~~ → **chỉ transition chuyển màn** (`NavTransitionSpec`); animation
  nội màn đưa vào Out of scope. Resolved 2026-09-25.
- ~~Thời lượng crossfade chuẩn?~~ → **200ms** theo design handoff (code đang dùng 220ms, cần sửa).
  Resolved 2026-09-25.

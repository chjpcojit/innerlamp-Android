# Design — LightLamp

Version: 1.0
Last updated: 2026-10-17
Status: not started — no lamp screen, route, domain, data or DI exists; the centre bottom-nav button opens the meditation setup. Contract part for the v2.0 changes (optional recipient, `is_mine`) is not in `openapi.yaml` yet, so the UI is built on a fake repository first.

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Two screens on one online-only feature: the light-a-lamp screen (`LampScreen`, with the form and, after
lighting, the confirmation as a state of the same screen) and the garden (`LampGardenScreen`). Data goes
`ViewModel → use case → LampRepository (domain) → LampRepositoryImpl (data) → NetworkClient.safeCall →
LampApiService`, both calls `@Authenticated`. There is no Room table and no preference: a lamp is never
stored on the device (product §3.7). The server decides which lamps are lit, the totals and the order;
the app computes only three things itself, all pure Kotlin in `domain/lamp/`: the input check and
normalization (`LampInputRules`, BR-LMP-01/02/06/07), the remaining-time label (`LampRemainingLabel`,
BR-LMP-12) and the end-date formatting (device time zone, BR-LMP-03). The architecture stays
`ui → domain ← data → core/{network,storage}`, manual DI in `di/`, thread switching through
`DispatcherProvider`, "now" through an injected `java.time.Clock`.

### Current state (code and specs read on 2026-10-17)

- **No lamp code.** `grep` for a lamp screen, route, repository or API service finds nothing under
  `app/src/main`. The only lamp-related text is copy: `SplashScreen.kt` ("Tự thắp đèn tâm, tự soi lối về")
  and `LegalSheet.kt` (see "Legacy to clean up" below). The prototype has the word "Thắp đèn" only as the
  name of the centre tab; it has **no** light-a-lamp form, confirmation or garden screen.
- **The centre button exists and is wired to meditation.** `ui/components/IlBottomNav.kt` has
  `IlBottomTab { Home, Lib, Lamp, Chua, Me }` and a floating `LampButton` that calls
  `onNav(IlBottomTab.Lamp)`. In `MainActivity.kt` the four screens that show the bar (Home, Library, Chùa,
  Profile) each handle `IlBottomTab.Lamp -> navController.navigate(SetupRoute) { launchSingleTop = true }`
  (lines 253, 281, 304, 319 at read time). There is no `BottomNavShell`; each screen draws its own bar.
- **`../Navigation/` still says the lamp button opens the meditation setup** (requirements, design and
  tasks); that is the superseded behavior, listed in the report for the cleanup change.
- **Meditation stays reachable** without the button: `HomeCallbacks.onSetup`, `ChuaCallbacks.onMeditate`
  and `TalkCallbacks.onMeditate` all navigate to `SetupRoute` (read in `MainActivity.kt`).
- **Auth pieces that exist:** `AuthSessionStore.hasSession()`, `@Authenticated` Retrofit calls with
  `AuthInterceptor`, `TokenRefreshAuthenticator`, `SessionEvents` (session end → Login), `AppError`.
  **Missing, owned by `../Login/`:** the shared `AuthGate` / `SignInPrompt`; the guest preview still enters Home
  as if signed in (`MainActivity.kt`, "TODO: chế độ xem trước"), so `hasSession()` is the only reliable
  signed-in test.
- **Debug logging** (`LoggingInterceptor`) skips `/v1/auth/` bodies only; the bodies of `/v1/lamps` would be
  logged in debug builds (criterion 30).
- The closest model for the data layer: `TempleApiService` + `TempleOptionRepositoryImpl` +
  `di/TempleModule.kt`; for a cursor-less paged list with chips and states: `../Sutra/design.md`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-17.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Screen | `ui/screens/lamp/LampScreen.kt` | The form (lamp type, intention, recipient + visibility notice, prayer, duration chips, "Ẩn tên tôi" switch, "Thắp đèn", garden entry), the igniting animation and the confirmation (two actions only), or — for a guest — the introduction + "Đăng nhập để thắp đèn"; `modifier: Modifier = Modifier`, previews in `InnerLampTheme` (AC3, 5–7, 9–14) | not created |
| UI / Screen | `ui/screens/lamp/LampArtwork.kt` | Unlit and lit lamp artwork for the three lamp types, used by the form, the confirmation and the garden cards | not created — ⚠️ no design (below) |
| UI / State | `ui/screens/lamp/LampUiState.kt` | `session: Guest \| SignedIn`; `form` (`lampType`, `intention`, `durationDays`, `recipient`, `prayer`, `hideName`), `step: Editing \| Lighting \| Lit(LitLamp)`, `error: LampFormError?` | not created |
| UI / ViewModel | `ui/screens/lamp/LampViewModel.kt` | `SavedStateHandle` keeps the form (AC15); reads `AuthSessionStore.hasSession()`; `submit()` validates through `LampInputRules`, calls `LightLampUseCase`, ignores a second tap while `Lighting` (AC10); `lightAnother()` keeps type, intention, duration (AC12) | not created |
| UI / Screen | `ui/screens/lamp/LampGardenScreen.kt` | Header "N ngọn đèn đang sáng", chips, `LazyColumn` of cards keyed by lamp id, "Xem thêm đèn", empty / error states, way back to lighting (AC16–25); guest → same introduction as `LampScreen` | not created |
| UI / State | `ui/screens/lamp/LampGardenUiState.kt` | `filter: LampType?`, `items`, `total`, `counts`, `hasMore`, `cursor`, `load: Loading \| Loaded \| Empty \| Error`, `loadingMore`, `moreError` | not created |
| UI / ViewModel | `ui/screens/lamp/LampGardenViewModel.kt` | `SavedStateHandle` keeps `filter` (AC20); `filter.collectLatest` cancels the older request; `onLoadMore()` one page at a time; recomputes labels on `onResume()` (AC26); drops items already listed (AC21) | not created |
| UI / Format | `ui/screens/lamp/LampCopy.kt` | All lamp copy as Vietnamese constants (the repo keeps screen copy in code, `res/values/strings.xml` is empty): messages of product §3.1.1, "Tất cả chúng sanh", "Của bạn", chip and lamp type names, the label text "Còn N ngày" / "Còn dưới 1 ngày" built from `LampRemainingLabel`, end-date formatter (device time zone) | not created |
| UI / Guest | `ui/components/auth/SignInPrompt.kt` + `AuthGate` (Login spec) | The lamp introduction reuses the shared gate with lamp-specific copy and the single button | not created — owned by `../Login/` (its Phase for guest mode); until it exists the lamp screens render their own minimal introduction |
| UI / Navigation | `ui/navigation/Routes.kt` | Add `object LampRoute : IlDestination` and `object LampGardenRoute : IlDestination` (default crossfade) | not created |
| UI / Navigation | `MainActivity.kt` | Register both routes with `ilComposable`; in the four `onTab` handlers change `IlBottomTab.Lamp -> navigate(SetupRoute)` to `navigate(LampRoute)` (AC1); `LampScreen` callbacks: `onGarden`, `onLogin` (`LoginRoute()`), `onBack`; `LampGardenScreen`: `onLight` (pop to or navigate `LampRoute`), `onLogin`, `onBack`. Keep `HomeCallbacks.onSetup`, `ChuaCallbacks.onMeditate`, `TalkCallbacks.onMeditate` untouched (AC2) | exists — needs change |
| UI / Components | `ui/components/IlBottomNav.kt` | No change: `IlBottomTab.Lamp` and `LampButton` already exist | exists |
| Domain / Model | `domain/model/Lamp.kt` | `LampType { OilLamp, LotusCandle, FlowerLantern }`, `LampIntention { Peace, Salvation, MeritDedication, Gratitude }`, `LampDuration { Day1, Day7, Day21, Day49 }` (closed sets, BR-LMP-01/02), `LampDraft`, `LitLamp` (type, intention, recipient?, prayer?, duration, lighter name, `litAt`, `endAt: Instant`), `GardenLamp` (`id`, type, recipient?, lighterName, `endAt`, `isMine`), `GardenPage` (`items`, `total`, `counts: Map<LampType, Int>`, `hasMore`, `nextCursor: String?`); unknown wire value → the lamp is dropped from the page and logged without text | not created |
| Domain / Rules | `domain/lamp/LampInputRules.kt` | Pure Kotlin (`java.text.Normalizer`, no Android): trim, NFC, count code points, recipient ≤ 100, prayer ≤ 300, blank → `null`; returns a normalized draft or the rejected field (BR-LMP-06/07); runs `lamp-input-validation.json` | not created |
| Domain / Rules | `domain/lamp/LampRemainingLabel.kt` | `of(endAt, now): Days(n) \| UnderOneDay \| Out` — seconds left, ≥ 86 400 → `ceil` days, > 0 → under one day, ≤ 0 → out (BR-LMP-12); runs `lamp-remaining-label.json` | not created |
| Domain / Repository | `domain/repository/LampRepository.kt` | `light(draft: LampDraft): Result<LitLamp>`, `garden(type: LampType?, cursor: String?): Result<GardenPage>`; errors `LampLimitReachedException`, `LampInvalidInputException`, the rest `NetworkException(AppError)` | not created |
| Domain / Use case | `domain/usecase/lamp/LightLampUseCase.kt`, `GetLampGardenUseCase.kt` | `operator fun invoke`; light = `LampInputRules` → repository; garden = repository, pure pass-through of the order and numbers received; dispatcher via `DispatcherProvider` | not created |
| Data / Remote | `data/remote/api/LampApiService.kt`, `data/remote/dto/LampDto.kt` | Retrofit `POST /v1/lamps` and `GET /v1/lamps`, both `@Authenticated`; `@Serializable` DTOs; shape per the contract, built once the contract is confirmed (the v2.0 fields are ⚠️) | not created |
| Data / Mapper | `data/mapper/LampMapper.kt` | DTO → domain; RFC 3339 → `Instant`; missing recipient → `null`; unknown lamp type → dropped | not created |
| Data / Repository | `data/repository/LampRepositoryImpl.kt` | Implements the interface via `safeCall`; maps the limit error and the validation error to the domain exceptions | not created |
| Data / Fake | `data/repository/FakeLampRepository.kt` | In-memory stand-in used until the 3.0.0 contract exists: lights a lamp with an end moment from the injected clock, serves a garden with a few lamps of every type including some `isMine` and a blank recipient; removed when the real repository is wired | not created |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | Also skip logging for `/v1/lamps` (AC30) | exists — small change |
| DI | `di/LampModule.kt` | Manual wiring: API service, repository (fake first, real later), use cases, `Clock`, ViewModel factories | not created |
| Tests | `app/src/test/java/com/example/innerlamp/…`, `app/src/test/resources/product/lamp/rules/`, `scripts/sync-product-vectors.sh` | `LampInputRulesTest`, `LampRemainingLabelTest`, `LampProductVectorsTest`, mapper / repository / use case / ViewModel tests | not created (the script is planned by `../Calendar/` and `../Memorial/`; create it if still absent) |

## Navigation flow

```
Any screen with the bottom nav (Home / Library / Chùa / Profile)
  └─ centre lamp button ──▶ LampRoute (launchSingleTop)            [was: SetupRoute]
       ├─ guest ──▶ introduction + "Đăng nhập để thắp đèn" ──▶ LoginRoute()
       ├─ signed in: form ── "Thắp đèn" ──▶ igniting ──▶ confirmation (state of LampRoute)
       │      ├─ "Thắp thêm đèn" ──▶ empty form (same screen)
       │      └─ "Vào vườn đèn" ──▶ LampGardenRoute
       ├─ garden entry ──▶ LampGardenRoute
       │      ├─ "Thắp đèn" / empty-state button ──▶ LampRoute
       │      └─ (back) ──▶ previous screen
       └─ (back) ──▶ previous screen

Meditation (unchanged): Home onSetup / Chùa onMeditate / Talk onMeditate ──▶ SetupRoute
```

> ⚠️ Not confirmed (design): whether `LampRoute` shows the bottom nav (as the tab roots Home / Library / Chùa / Profile do) or is a full-screen flow — depends on the bottom-nav model still open in `../Navigation/` Phase 3 (single `NavHost` + `saveState` vs nested graphs); blocks `tasks.md` Phase 2 item "bar on Thắp đèn"; default: no bar on `LampRoute` (it is a task screen with a form and a back button), revisited when the shell is decided.

## Step-by-step Flow — Light a lamp

```
Step 1 — Open Thắp đèn (this feature)
- Trigger: tap the centre lamp button of the bottom nav
- Pre-condition: the user is on a screen that shows the bar
- Where: IlBottomNav.onNav(Lamp) → MainActivity onTab → navigate(LampRoute) { launchSingleTop } → LampViewModel (AuthSessionStore.hasSession())
- API: none
    - Success (session stored) → form with defaults (oil lamp, peace, 7 days, hide-name off), unlit lamp
    - Failure (no session: guest) → introduction + "Đăng nhập để thắp đèn"; no form, no garden, no backend call

Step 2 — Check the input (this feature)
- Trigger: tap "Thắp đèn"
- Pre-condition: step is Editing (a second tap while Lighting is ignored)
- Where: LampViewModel.submit() → LightLampUseCase → LampInputRules
- API: none
    - Success → step becomes Lighting (button shows progress); recipient and prayer trimmed, NFC-normalized, blank → none
    - Failure (recipient > 100 or prayer > 300 characters) → stay Editing, invalid-input message, form kept, no request

Step 3 — Light the lamp (this feature)
- Trigger: step 2 succeeded
- Pre-condition: access token present (AuthInterceptor attaches it)
- Where: LightLampUseCase → LampRepositoryImpl (safeCall) → LampApiService
- API: POST /v1/lamps (lamp type, intention, burn duration, recipient and prayer when not blank, hide-name flag; contract 2.1.1 — recipient optional is ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0))
    - Success (201 new lamp, or 200 the identical lamp within the dedupe window — treated the same) → step becomes Lit(lamp): igniting animation, then the confirmation with the response's end moment shown in the device time zone; nothing is stored on the device
    - Failure (limit reached) → back to Editing, limit message, form kept
    - Failure (input rejected) → back to Editing, invalid-input message, form kept
    - Failure (AppError.NoConnection / Timeout / 5xx / other) → back to Editing, "Chưa thắp được đèn. Vui lòng thử lại.", form kept
    - Failure (401) → Step 4

Step 4 — Refresh the session (core/network, out of scope — see ../Login/)
- Trigger: the call in step 3 gets 401
- API: POST /v1/auth/refresh
    - Success → new tokens stored, step 3 is retried once with the new token
    - Failure (refresh rejected) → session ended: account data cleared, LoginRoute opens with "Phiên đăng nhập đã hết…"; the lamp form is not kept

Step 5 — After the confirmation (this feature)
- Trigger: tap an action of the confirmation
- Where: LampViewModel
- API: none
    - "Thắp thêm đèn" → Editing with an unlit lamp, same lamp type / intention / duration, empty recipient and prayer, hide-name back to off (⚠️ Not confirmed (product), requirements criterion 12)
    - "Vào vườn đèn" → navigate(LampGardenRoute)
```

## Step-by-step Flow — The garden

```
Step 1 — Open the garden (this feature)
- Trigger: "Vào vườn đèn" / garden entry / navigating to LampGardenRoute
- Pre-condition: a session is stored (else the guest introduction, no call)
- Where: LampGardenViewModel → GetLampGardenUseCase → LampRepositoryImpl (safeCall) → LampApiService
- API: GET /v1/lamps (no filter, no cursor, no limit); `is_mine` and nullable recipient are ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0)
    - Success with items → cards in the received order, header total, chip counts; cards whose label is Out are not drawn
    - Success with no items → "Vườn đèn còn trống" with the button to light a lamp
    - Failure (AppError.NoConnection / Timeout / 5xx / other) → "Chưa tải được vườn đèn. Vui lòng thử lại sau." with retry; lighting stays available
    - Failure (401) → refresh as in the lighting flow, Step 4

Step 2 — Filter (this feature)
- Trigger: tap a chip
- Pre-condition: garden screen shown
- Where: LampGardenViewModel (filter → collectLatest)
- API: GET /v1/lamps (lamp type filter, first page)
    - Success → list replaced; header total and chip counts come from this response and do not depend on the filter
    - Failure → as Step 1; the previous chip stays selected only if the user retries it

Step 3 — Load more (this feature)
- Trigger: tap "Xem thêm đèn"
- Pre-condition: the last response said more lamps exist; no page is loading
- Where: LampGardenViewModel.onLoadMore()
- API: GET /v1/lamps (current filter, the cursor of the last response — opaque)
    - Success → page appended (an id already listed is dropped); the cursor and the "more" flag are replaced
    - Failure → loaded items kept, garden error under the list with retry, same cursor
```

## Local storage

None. No Room table, no preference, nothing in secure storage: a lamp is never stored on the device and
there is no offline copy (product §3.7). Only transient UI state: `SavedStateHandle` keeps the form
choices, the typed recipient and prayer, and the chosen chip across rotation and process death
(criteria 15, 20); the confirmation lives in the ViewModel only.

## Deliberately stubbed for now

- **Fake repository** (`FakeLampRepository`) until the backend ships openapi 3.0.0 (optional recipient,
  `is_mine`); the real `LampRepositoryImpl` replaces it in `tasks.md` Phase 3.
- **Lamp artwork and screens have no design.** The prototype and the design handoff have no
  light-a-lamp form, confirmation or garden screen; the artwork (three lamp types, unlit and lit, and the
  igniting animation) and the layout need a design hand-off before `il-ui-builder` builds Phase 2.
  > ⚠️ Not confirmed (design): the Thắp đèn screens and the lamp artwork — blocks `tasks.md` Phase 2 beyond a plain Material3 layout; hand off to design. The product spec makes the prototype the owner of artwork and animation (§1.2).
- **Guest introduction** reuses `AuthGate` / `SignInPrompt` once `../Login/` builds them; until then a minimal
  introduction local to the lamp screens stands in.

## Legacy to clean up (listed for the later cleanup change; deleted nowhere in this change)

- `Docs/Specs/Navigation/requirements.md` (the criterion "WHEN the user taps the lamp button, THE SYSTEM SHALL
  navigate to the meditation prepare screen"), `design.md` (the `IlBottomNav` row text "nút đèn → màn chuẩn bị thiền",
  the `BottomNavShell` row, the tree entry "Đèn (SetupRoute — nút đèn giữa)", the `navigation("lamp", start = SetupRoute)`
  line and the bullet "Nút đèn (Lamp) → SetupRoute"), `tasks.md` (the Phase 3 item "Nút đèn (Lamp) → SetupRoute").
- `app/src/main/java/com/example/innerlamp/ui/screens/signup/LegalSheet.kt` line 283: the privacy text says prayers are private
  by default and "only shown in the lamp garden when you choose public" — the product shows no prayer in the garden at all
  (BR-LMP-10) and has no public choice.
- `Docs/InnerLamp icon consistency/design_handoff_innerlamp/Mo-ta-tinh-nang.md` (line 16, the bottom-nav description) and
  `design_handoff_innerlamp/README.md` (line 163, "Đèn (→ chuẩn bị thiền)"), plus their copies under
  `Docs/InnerLamp icon consistency/uploads/…`: the centre button "→ Chuẩn bị thiền".

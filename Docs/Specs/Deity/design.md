# Design — Deity

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — UI for the four entry points (Home block, Library block, catalogue, a deity's page) exists on 9 sample profiles; the contract-based domain, repository, data layer, pagination, Hình tượng (iconography), sharing and passing the recitation phrase do not exist yet; page size 12 / `date` / `next_via` / `via_events` wait for backend deity v3.0 to land in `openapi.yaml`

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Four entry points into one public, read-only catalogue: the "Thư viện Phật & Bồ Tát" (Buddha &
Bodhisattva library) block on Home (`HomeViewModel`), the "Chư Phật & Bồ Tát" (Buddhas &
Bodhisattvas) block on Library (`LibraryViewModel`), the catalogue screen (`BuddhaListScreen`) and a
deity's page (`BuddhaDetailScreen`). Data goes
`ViewModel → use case → DeityRepository (domain) → DeityRepositoryImpl (data) → NetworkClient.safeCall →
DeityApiService`; no Bearer. **Online-only**: every open is one call, no Room, no offline copy (product
§3.6 only says "may" — chosen not to do it in v1.0, like `../Temple/`). Order, filtering, paging, the
ngày vía (a deity's commemoration day; "via" below) and the next via occurrence are all done by the
server; the app only: sends `date` = the device date, turns `next_via.solar_date` into a countdown
through `domain/calendar/CalendarCountdown` (of `../Calendar/`, BR-CAL-13), and formats the lunar date
the server returns.

Current state (read on 2026-10-15): `GetBuddhaProfilesUseCase` holds 9 sample `BuddhaProfile`
(prototype `BUDDHAS`), `GetBuddhaDetailUseCase` scans `LunarCalendar` for 400 days to find the next via
occurrence; Home uses sample `BuddhaFigure` in `HomeSampleData`; `LibraryViewModel` reads synchronously.
The closest model for the data layer: `TempleApiService` + `TempleOptionRepositoryImpl` +
`di/TempleModule.kt`; for a paged catalogue: `../Temple/design.md` (the temple directory, same
`items/page/page_size/total/total_pages` shape). Coil 3 is already declared in `app/build.gradle.kts`
(shared plan with `../Temple/`).

Code names keep the `ui/screens/buddha/` folder and the `BuddhaListRoute`/`BuddhaRoute` routes (product
§1.5); the new domain models are named `Deity*` per the product and contract, replacing
`BuddhaProfile`/`BuddhaFigure`.

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, thread
switching through `DispatcherProvider`, "today" through an injected `java.time.Clock`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-15.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`BuddhasSection`, `BuddhaCard`), `HomeSkeletons.kt` | Card: `thumbnailUrl` image (Coil `AsyncImage`, placeholder when `null`/error), type label, `nameVi`, "Vía dd/MM âm lịch" (Via dd/MM lunar) from `nextVia` (AC1, AC5); empty card + "Thử lại" (Retry) (AC4) | exists — needs change |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt` | The Buddha block has its own state (loading / loaded / empty-error) read from `GetFeaturedDeitiesUseCase`, "Thử lại" reloads only this block; remove `buddhas` from `HomeSampleData` (same direction `../Temple/` takes with the temple block) | exists — needs change |
| UI / Library block | `ui/screens/library/LibraryScreen.kt` (`BuddhaStrip`), `LibraryUiState.kt`, `LibraryViewModel.kt` | Card strip from `GetFeaturedDeitiesUseCase` (async), loading / loaded / empty-error state + "Thử lại" (AC2, AC4 ⚠️ copy) | exists — needs change |
| UI / Catalogue screen | `ui/screens/buddha/BuddhaListScreen.kt` | Header (subtitle N from `total`), three fixed chips (AC9), 2-column grid with `LazyVerticalGrid` (replacing `chunked(2)` inside `verticalScroll`) that loads the next page when ~4 cells remain (AC8); `buddhasEmpty` state (AC10, AC11); "Thử lại" at the end of the list (AC12); card with `coreVirtue` (AC5) | exists — needs change |
| UI / Catalogue state | `ui/screens/buddha/BuddhaListUiState.kt` | `type: DeityType?`, `items`, `total`, `page`, `totalPages`, `loadState` (initial / more / initial empty-error / more error / done); **remove** `filtered` (on-device filtering) and the subtitle that counts all samples | exists — rewrite |
| UI / Catalogue VM | `ui/screens/buddha/BuddhaListViewModel.kt` | `SavedStateHandle` keeps `type` (AC13); `typeFlow.collectLatest` (cancels the old request, AC9); loads the next page one request at a time (AC8) | exists — rewrite |
| UI / Detail screen | `ui/screens/buddha/BuddhaDetailScreen.kt` (+ split out `BuddhaDetailSections.kt` if long) | The sections per FR-DTY-005 (AC15); the biography collapses when it overflows 6 lines — use `onTextLayout.hasVisualOverflow` to decide whether to show "Đọc thêm" (Read more) (AC16); via card + via day list (AC17, AC18); great vows (AC19); **Hình tượng** (AC20, new); three mantra cards + **one** button row (AC21, AC22); related sutras (AC25, AC26); footer without "Báo thông tin sai" (Report wrong information) (AC27); loading / error / not-found states (AC14); `LifecycleResumeEffect` tells the ViewModel to check the date (AC29) | exists — needs major change |
| UI / Detail state | `ui/screens/buddha/BuddhaDetailUiState.kt` | `loadState` (loading / loaded / not found / error), `detail: DeityDetail?`, `sutras: List<RelatedSutra>` (empty = hidden), `today: LocalDate` (the date sent), `countdown` through `CalendarCountdown.of(today, nextVia.solarDate)`, `bioExpanded`, `vowsExpanded`; **remove** `vowNoun`/`vowUnit`, `extraVia`, the self-computed `nextViaLabel` | exists — rewrite |
| UI / Detail VM | `ui/screens/buddha/BuddhaDetailViewModel.kt` | `buddhaId` from `SavedStateHandle`; calls `GetDeityDetailUseCase(id, today)` and `GetRelatedSutrasUseCase(id)` in parallel (Step-by-step Flow); `onResume()` reloads when `LocalDate.now(clock) != today` (AC29); the ViewModel is created through the `di/DeityModule` factory | exists — rewrite |
| UI / Format | `ui/screens/buddha/DeityFormat.kt` | Display strings: the BR-DTY-01 type label (`DeityType.Unknown` → no label, AC6), "Vía dd/MM âm lịch" (⚠️ leap month, AC5), "THÁNG GIÊNG/n/CHẠP" (first / n-th / twelfth lunar month), the solar date with weekday (reuse `weekdayFull()` of `ui/screens/home/`), the countdown label "Hôm nay / Ngày mai / Còn n ngày" (Today / Tomorrow / n days left) from `CalendarCountdown`, "Xem đủ N đại nguyện" (See all N great vows) / "Thu gọn" (Collapse), the share text (AC24) | not created (logic currently spread across `BuddhaDetailUiState`, `BuddhaProfile.viaLabel`) |
| UI / Share | `ui/screens/buddha/DeityShare.kt` | `Intent(ACTION_SEND)` `text/plain` + `Intent.createChooser`; catches `ActivityNotFoundException` (AC24) | not created |
| UI / Navigation | `ui/navigation/Routes.kt` (`BuddhaListRoute`, `BuddhaRoute(buddhaId)`, `MalaRoute`), wiring in `MainActivity.kt` | Keep the two deity routes; `MalaRoute` becomes `data class MalaRoute(val mantra: String? = null)` (register `composable<MalaRoute>`; the old entry from Home passes `null`); `onChant` passes the short mantra (AC23); `onShare` calls `DeityShare` (AC24); `onSutra(id)` → `SutraRoute(id)` (AC25) | exists — needs change |
| UI / Mala (another feature, receiving point only) | `ui/screens/mala/MalaViewModel.kt`, `MalaUiState.kt` | Reads `mantra` from `SavedStateHandle`: present → `MalaUiState.mantra = mantra`, `beads = 0`; `null` → keep `DefaultMantra` as today (AC23) | exists — needs change (small) |
| Domain / Model | `domain/model/Deity.kt` | `DeityType { Buddha, Bodhisattva, Deva, Unknown }`; `DeitySummary` (`id`, `type`, `nameVi`, `nameSanskrit?`, `nameChinese?`, `titleHonorific?`, `thumbnailUrl?`, `imageUrl?`, `coreVirtue?`, `pureLand?`, `nextVia: DeityVia?`); `DeityVia` (`eventCode`, `name`, `solarDate: LocalDate`, `lunar: LunarDay`); `DeityViaEvent` (`eventCode`, `name`, `next: DeityVia?`); `DeityIconography` (`posture?`, `mountOrThrone?`, `sacredArtifacts`, `symbolicMeaning?`, `isEmpty`); `DeityMantras` (`shortSinoVietnamese?`, `shortSanskrit?`, `dharaniName?`, `dharaniSnippet?`); `DeityDetail` (`summary`, `canonicalSource?`, `biography?`, `greatVows`, `iconography`, `mantras`, `viaEvents`); `DeityPage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`); `RelatedSutra` (`id`, `title`, `collection?`). The contract's empty string → `null` (BR-DTY-04). `LunarDay(day, month, year, isLeapMonth)` shared with `../Calendar/` (`domain/model/CalendarEvent.kt`; whichever feature comes first creates it) | not created |
| Domain / Delete | `domain/model/BuddhaProfile.kt` (`BuddhaCategory`, `BuddhaMantra`, `BuddhaSutraRef`, `BuddhaProfile`, `BuddhaDetail`), `BuddhaFigure` in `domain/model/HomeContent.kt`, `domain/usecase/GetBuddhaProfilesUseCase.kt` (9 sample profiles), `GetBuddhaDetailUseCase.kt` (400-day lunar scan) | Sample data and on-device via computation — **drift BR-DTY-05**; delete once Phase 2/3 run. `domain/calendar/LunarCalendar.kt` stays (other features still use it) | exists — delete |
| Domain / Repository | `domain/repository/DeityRepository.kt` | `getFeatured(today: LocalDate): Result<List<DeitySummary>>`, `getPage(page: Int, type: DeityType?, today: LocalDate): Result<DeityPage>`, `getDetail(id: String, today: LocalDate): Result<DeityDetail>`, `getRelatedSutras(id: String): Result<List<RelatedSutra>>` (merges all pages); 404 → `DeityNotFoundException` | not created |
| Domain / UseCase | `domain/usecase/GetFeaturedDeitiesUseCase.kt`, `GetDeityPageUseCase.kt`, `GetDeityDetailUseCase.kt`, `GetRelatedSutrasUseCase.kt` | `operator fun invoke` → `Result`; dispatcher through `DispatcherProvider`; `today` taken from the injected `Clock` (AC28) and returned with the result so the UI counts down from the same date that was sent | not created |
| Domain / Rules | `domain/calendar/CalendarCountdown.kt` | BR-CAL-13 — **owned by `../Calendar/`** (Phase 1 there); this spec only uses it | not created (outside this spec) |
| Data / Remote | `data/remote/api/DeityApiService.kt` | `getTop(date)`, `getList(page, type: String?, date)`, `getDetail(id, date)`, `getSutras(id, page)` — return `ApiResponse<…>`, no `@Authenticated` | not created |
| Data / Remote | `data/remote/dto/DeityDto.kt` | `@Serializable` DTOs per `openapi.yaml` + backend deity `requirements.md` v3.0 (the "Hình dạng" (Shape) table) — the v3.0 part is declared nullable/default so the old contract still parses (`JsonProvider` already has `ignoreUnknownKeys`); `lunar_memorial_days` is **not** declared. Related-sutra DTO: only the `SutraByDeityItem` fields the mapper needs | not created |
| Data / Mapper | `data/mapper/DeityMapper.kt` | DTO → domain: `type` → `DeityType` (unknown → `Unknown`, the item is not dropped — contract "open enum"); `""` → `null`; `solar_date` `YYYY-MM-DD` → `LocalDate` (unparseable → treat `next_via`/`next_occurrence` as `null`); `DeityType` → the `type` parameter (`Unknown` is never sent) | not created |
| Data / Repository | `data/repository/DeityRepositoryImpl.kt` | Calls the API through `NetworkClient.safeCall`; `date` = `today.toString()`; `AppError.ApiError(code = DEITY_NOT_FOUND)` → `DeityNotFoundException`; the rest `NetworkException(AppError)`; `getRelatedSutras` calls pages 1…`total_pages` in turn; no cache | not created |
| DI | `di/DeityModule.kt` | `provideDeityRepository()`, the four use cases, factories for `BuddhaListViewModel`, `BuddhaDetailViewModel` and the Buddha block source of `HomeViewModel`/`LibraryViewModel` | not created |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Mapper; repository; ViewModel (pagination, chip change cancels the old request, empty/error, parallel detail, date change on resume); `DeityFormat`; Mala receives the recitation phrase; `GetBuddhaDetailUseCaseTest` (exists — tests the lunar scan, to be removed) | partly exists (to be removed) |

## Navigation flow

```
HomeRoute ("Thư viện Phật & Bồ Tát" block ← GET /v1/deities/top)
  ├─ (tap card) ──▶ BuddhaRoute(buddhaId)
  └─ (Xem tất cả) ──▶ BuddhaListRoute
LibraryRoute ("Chư Phật & Bồ Tát" block ← GET /v1/deities/top; hub tile "Phật & Bồ Tát")
  ├─ (tap card) ──▶ BuddhaRoute(buddhaId)
  └─ (Xem tất cả | hub tile) ──▶ BuddhaListRoute
BuddhaListRoute (GET /v1/deities, pages 1…n; type)
  ├─ (tap card) ──▶ BuddhaRoute(buddhaId)
  ├─ (empty state: Xem lịch lễ vía) ──▶ FestivalsRoute
  └─ (back) ──▶ previous screen
BuddhaRoute(buddhaId) (GET /v1/deities/{id} ‖ GET /v1/deities/{id}/sutras)
  ├─ (Lịch lễ) ──▶ FestivalsRoute
  ├─ (Trì niệm với tràng hạt) ──▶ MalaRoute(mantra = Sino-Vietnamese short mantra)
  ├─ (Chia sẻ) ──▶ system share sheet (ACTION_SEND)
  ├─ (tap sutra) ──▶ SutraRoute(sutraId)
  └─ (back, or "back" in the not-found state) ──▶ previous screen
```

"Xem tất cả" = See all; "Xem lịch lễ vía" = See the via festival calendar; "Lịch lễ" = Festival
calendar; "Trì niệm với tràng hạt" = Recite with the mala; "Chia sẻ" = Share.

Default transition (crossfade) as today. `BuddhaListViewModel` is attached to the `BuddhaListRoute`
entry so back from `BuddhaRoute` keeps the scroll position and the pages already loaded (AC3).

## Step-by-step Flow — Catalogue: first page, chip change, next page

```
Step 1 — First page (this feature)
- Trigger: open BuddhaListRoute; or change chip; or "Thử lại" in the empty state
- Pre-condition: type read from SavedStateHandle (null = "Tất cả" (All)); today = LocalDate.now(clock)
- Where: BuddhaListViewModel (collectLatest — the previous request is cancelled) → GetDeityPageUseCase → DeityRepositoryImpl (safeCall)
- API: GET /v1/deities?page=1[&type=BUDDHA|BODHISATTVA]&date=<today>
  ⚠️ Not confirmed: contract pending — `date`, page size 12 and `next_via` per backend deity requirements.md
  v3.0 (contract not yet in openapi.yaml); until the server has them, cards have no via line (next_via = null) and
  the page size is 20 — the app still reads page_size from the response.
    - Success, total > 0 → replace items, total, page = 1, totalPages; subtitle "{total} vị · …" ({total} deities · …)
    - Success, total = 0 → empty state AC10 ("Tất cả": hide chips) / AC11 (type chip: keep chips)
    - Failure (any AppError, including 400 INVALID_PAGE / INVALID_DEITY_TYPE / INVALID_DATE) → empty state AC10 + "Thử lại"

Step 2 — Next page (this feature)
- Trigger: the last visible cell is ≤ 4 from the end of the list; or "Thử lại" at the end of the list
- Pre-condition: not loading; page < totalPages; type unchanged since Step 1 (changed → back to Step 1, cancel Step 2); use the same today as Step 1
- Where: BuddhaListViewModel → GetDeityPageUseCase → DeityRepositoryImpl
- API: GET /v1/deities?page=N+1 (same type, date)
    - Success → append items, page = N+1; empty items → treat as the end (AC8)
    - Failure (any error) → keep items, "Thử lại" at the end (AC12)
```

## Step-by-step Flow — A deity's page: detail, related sutras, date change

```
Step 1 — Load the detail (this feature)
- Trigger: open BuddhaRoute(buddhaId); "Thử lại"; or Step 4
- Pre-condition: buddhaId from SavedStateHandle; today = LocalDate.now(clock), stored in the UiState
- Where: BuddhaDetailViewModel → GetDeityDetailUseCase → DeityRepositoryImpl (safeCall)
- API: GET /v1/deities/{id}?date=<today>
  ⚠️ Not confirmed: contract pending — `next_via`, `via_events`, `date` per backend deity requirements.md
  v3.0 AC 8, 24–29 (contract not yet in openapi.yaml); until then, the via card is hidden (BR-DTY-04) — the app does not
  fall back to `lunar_memorial_days` and does not compute it itself.
    - Success → loadState = loaded; countdown = CalendarCountdown.of(today, nextVia.solarDate) if nextVia is present
    - Failure (404 DEITY_NOT_FOUND) → "not found" + back (AC14; ⚠️ copy)
    - Failure (other) → error + "Thử lại" (AC14; ⚠️ copy); Step 2 is not shown even if it succeeds

Step 2 — Related sutras (this feature, runs in parallel with Step 1)
- Trigger: at the same time as Step 1 (async in viewModelScope); not re-run in Step 4
- Where: BuddhaDetailViewModel → GetRelatedSutrasUseCase → DeityRepositoryImpl
- API: GET /v1/deities/{id}/sutras?page=1, then page=2…total_pages sequentially (no role)
    - Success → sutras = all items in the order received (empty → hide the section, AC25)
    - Failure (any page, any error, including 404) → sutras = empty, hide the section (AC26; ⚠️ product)

Step 3 — Actions (this feature, no backend call)
- Recite: callback onChant(mantra) → MainActivity navigate(MalaRoute(mantra)) → MalaViewModel reads SavedStateHandle, beads = 0 (AC23)
- Share: DeityFormat.shareText(detail) → DeityShare.open(context, text) (AC24)

Step 4 — Date change on return (this feature)
- Trigger: ON_RESUME (LifecycleResumeEffect → BuddhaDetailViewModel.onResume())
- Pre-condition: loadState = loaded; LocalDate.now(clock) != UiState.today
- Where: as Step 1 with the new today (keep the old content on screen while loading; error → keep the old content, do not switch to the error state)
```

## Local storage

- No Room, no prefs, no offline copy (online-only, see Overview). Images: Coil's default cache.
- The catalogue's selected chip: `SavedStateHandle` (AC13), not prefs.
- The recitation phrase passed to Niệm Phật (Buddha-name recitation): route arg `MalaRoute.mantra`
  (stored in the back stack, survives process death).

## Deliberately stubbed for now

- **The v3.0 part of the contract** (⚠️ contract pending — backend deity requirements.md v3.0, backend
  `tasks.md` Phase 8–10; the openapi update is Phase 9): the DTOs declare `next_via`, `via_events`
  nullable / default empty and the repository always sends `date` (the current server ignores unknown
  parameters — not yet checked against the handler, confirm when the backend ships). While waiting: no
  via line on cards, no via card on the page, page size 20. There is **no** fallback that computes the
  via on the device or reads `lunar_memorial_days` (BR-DTY-05). When the contract lands in
  `openapi.yaml`: update the `Contract:` line of `requirements.md`, remove the marker.
- **Copy still waiting on product**: the Library block empty state, not found / error on a deity's page,
  leap month, a via day with no occurrence, sharing when there is no short mantra, a link in the share
  text, the sutra secondary line — `DeityFormat` keeps the proposed defaults from `requirements.md`,
  clearly marked as temporary.
- **Opening a sutra**: `SutraRoute(id)` with a backend id will not match the sutra screen's sample sutra
  catalogue until the sutra feature has a data layer (outside this spec).
- **UI and data layer**: the profile part, `type`, `page` and related sutras are already in the contract,
  so Phase 3 is not blocked for them — do it in the same batch as Phase 2, no new sample data added to
  `main`. `FakeDeityRepository` lives only in `app/src/test/…`; `@Preview` builds the `UiState` directly
  for the stateless parts (`BuddhaListContent`, `BuddhaDetailContent`).

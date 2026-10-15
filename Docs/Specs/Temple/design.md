# Design — Temple

Version: 1.2
Last updated: 2026-10-15
Status: planned (drift) — UI for the three entry points (featured block, directory, temple page) exists on sample data; contract-shaped domain, repository, data layer for `top`/list/detail, opening hours, distance and directions are missing; search/filter and the new fields wait on the contract

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Three entry points into one public, read-only temple directory: the home "Chùa Việt Nam" block
(`HomeViewModel`), the "Tất cả chùa" screen (`TempleListScreen`) and the "Trang chùa" screen
(`TempleScreen`). Data flows
`ViewModel → use case → TempleRepository (domain) → TempleRepositoryImpl (data) → NetworkClient.safeCall
→ TempleApiService`; no Bearer, no `X-API-Key`. **Online-only**: every open is one call, no Room, no
offline copy (product §3.6 only says "may" — not done in v1). Order, search and filtering are done by
the server; the app computes only two things itself, in pure Kotlin under `domain/temple/`: the open
status in Vietnam time + lunar calendar (BR-TPL-09, using the existing `domain/calendar/LunarCalendar`)
and the haversine distance (BR-TPL-07). Location goes through a port `domain/location/LocationProvider`,
implemented in `core/location/` with `android.location.LocationManager` (no Play Services added).

Current state (read on 2026-10-15): all three entry points read `GetTemplesUseCase` /
`GetTempleDetailUseCase`, which return sample data synchronously; `TempleListUiState` filters on the
device; `TempleScreen` is the prototype's old layout (follow, offering, stats, schedule). Already there
and reused: `TempleApiService` (`getOptions`), `TempleOptionRepositoryImpl`, `di/TempleModule.kt` — the
closest pattern for the new repository. Coil 3 (`libs.coil.compose`, `libs.coil.network.okhttp`) is
declared in `app/build.gradle.kts` but not used anywhere yet. `GetTemplesUseCase` is still used as
sample data by `GetFestivalCalendarUseCase` and `ChuaSampleData` — this feature stops using it but does
**not delete** it until those two features switch source.

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, threads
switched via `DispatcherProvider`, "now" via an injected `java.time.Clock` (tests can fix the time).

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. State read on 2026-10-15.

| Layer | File | Responsibility | State |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`TemplesSection`, `TempleCard`), `HomeSkeletons.kt` | Card: `thumbnail_url` image (Coil `AsyncImage`, placeholder when `null`/failed), name, type, province, heritage rank, sect (AC4); one empty state used both for an empty list (AC2) and for any load failure (AC3) — no error text, no retry | exists — needs changes |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt` | The temple block has its own state (loading / content / empty) read from `GetFeaturedTemplesUseCase`; a failed `Result` maps to empty (AC3), so there is no error state and the block needs no reload action; Buddha and articles keep their current source; remove `temples` from `HomeSampleData` | exists — needs changes |
| UI / Directory screen | `ui/screens/templelist/TempleListScreen.kt` | Search field ("Xóa" button), four fixed chips (AC12), number-found line, `LazyColumn` loading the next page when ~3 items remain (AC6), first-page error / later-page error / no match (AC7, AC8, AC10) | exists — needs changes |
| UI / Directory state | `ui/screens/templelist/TempleListUiState.kt` | `query`, `region: TempleRegion?`, `items`, `total`, `page`, `totalPages`, `loadState` (first / more / first failed / more failed / done); **remove** `filtered`, data-derived `regions`, `foldVietnamese` | exists — rewrite |
| UI / Directory VM | `ui/screens/templelist/TempleListViewModel.kt` | `SavedStateHandle` keeps `query`/`region` (AC14); `queryFlow.debounce(300 ms)` + `distinctUntilChanged` + `collectLatest` (cancels the old request, AC9); next page one request at a time (AC6) | exists — rewrite |
| UI / Temple screen | `ui/screens/temple/TempleScreen.kt` (+ split `TempleSections.kt` if long) | Sections per FR-TPL-007 (AC18–AC26), with the later fields ("Kinh sách", architecture style, materials, highlights, photo set) rendered as visible "Đang cập nhật" rows (AC21, AC22); action row with only "Chỉ đường" (AC25); loading / error / not-found states (AC17); location permission via `rememberLauncherForActivityResult(RequestMultiplePermissions)` (AC31); `LifecycleResumeEffect` + minute ticker to recompute hours (AC28) | exists — rewrite |
| UI / Temple state | `ui/screens/temple/TempleUiState.kt` | `loadState` (loading / content / not found / offline error / other error), `detail`, `openStatus`, `distanceKm: Double?`, `introExpanded`; **remove** `isFollowing`, `followLabel` | exists — rewrite |
| UI / Temple VM | `ui/screens/temple/TempleViewModel.kt` | `templeId` from `SavedStateHandle`; calls `GetTempleDetailUseCase`; `refreshOpenStatus()` via `TempleOpenStatusRule` + `Clock`; `onLocationPermissionResult()` → `LocationProvider` → `TempleDistance`; **remove** `toggleFollow` | exists — rewrite |
| UI / Format | `ui/screens/temple/TempleFormat.kt` | Display strings: status labels (`Đang mở cửa · đóng lúc HH:MM` …, BR-TPL-09), the "Rằm & Mùng Một" row text (holiday hours, or the normal hours when the temple has none — AC27), "Cách bạn X km" (decimal comma, BR-TPL-07), "Cập nhật MM/YYYY", ordinals "01", "Đang cập nhật"/"Không có" (later fields always "Đang cập nhật", AC21, AC22); labels for `type`/`sect`/`heritage_rank`/region (⚠️ product, AC4) | not created |
| UI / Intents | `ui/screens/temple/TempleIntents.kt` | `geo:lat,lng?q=lat,lng(name)`; fallback `https://www.google.com/maps/dir/?api=1&destination=lat,lng`; `ACTION_DIAL tel:`; `ACTION_VIEW https:`; `ACTION_SENDTO mailto:`; catches `ActivityNotFoundException` (AC33, AC34) | not created |
| UI / Navigation | `ui/navigation/Routes.kt` (`TempleListRoute`, `TempleRoute(templeId)`), wiring in `MainActivity.kt` | Keep the routes; remove the `onOffer`, `onSchedule` callbacks; wire `onDirections` → `TempleIntents`; ViewModels created via the `di/TempleModule` factory | exists — needs changes |
| UI / Navigation (other features) | `MainActivity.kt` — profile, lunar calendar (`TempleRoute("tu-quang")`), festival calendar, Chùa tab | Pass sample ids; with the real backend they end in "not found" (AC17) — fixed in those features' specs | outside this spec |
| Manifest | `AndroidManifest.xml` | `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` (AC31); `<queries>` for `geo`, `tel`, `mailto`, `https` so `resolveActivity` sees the apps (Android 11+) | exists — needs changes |
| Domain / Model | `domain/model/Temple.kt` | `Temple` (summary: `id`, `name`, `type`, `region: TempleRegion`, `thumbnailUrl`, `imageUrl`, `sect`, `heritageRank`, `establishedYear`, `location`), `TempleLocation` (`lat`, `lng`, `province`, `ward?`, `fullAddress`), `TempleRegion { North, Central, South }` (BR-TPL-04), `TemplePage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`). Replaces the old `Temple` in `HomeContent.kt` (`place`, `region: String`, `note`) | exists — replace |
| Domain / Model | `domain/model/TempleContent.kt` → `TempleDetail` | Summary + `aliases`, `founder`, `currentAbbot`, `sanghaBody`, `hours: OpeningHours?`, `holidayHours: OpeningHours?`, `hoursText` (raw, AC29), `amenities`, `introduction`, `architectureHeritage`, `visitingNotes`, `contacts` (`hotline`, `fanpage`, `website`, `email`, all `String?`), `contentUpdatedMonth: YearMonth?`; an empty contract string → `null`. **Remove** `TempleStat`, `TempleSchedule`, `TempleTone`, `TempleGlyph` (`sangha` content) | exists — rewrite |
| Domain / Rule | `domain/temple/TempleOpenStatusRule.kt` | `evaluate(now: Instant, hours, holidayHours): TempleOpenStatus` — converts `now` to `Asia/Ho_Chi_Minh`, lunar day via `LunarCalendar.toLunar`, applies BR-TPL-09 (no holiday hours → normal hours on lunar days 1 and 15); returns `Hidden` / `Open(closesAt)` / `Closed(reopensAt)` + `todayRow` | not created |
| Domain / Rule | `domain/temple/TempleDistance.kt` | `haversineKm(from, to)` (radius 6371 km as the vector file describes) | not created |
| Domain / Port | `domain/location/LocationProvider.kt` | `suspend fun current(): GeoPoint?` — `null` when no permission / location off / timeout | not created |
| Domain / Repository | `domain/repository/TempleRepository.kt` | `getFeatured(): Result<List<Temple>>`, `getPage(page: Int, query: String?, region: TempleRegion?): Result<TemplePage>`, `getDetail(id: String): Result<TempleDetail>`; 404 → `TempleNotFoundException` | not created |
| Domain / Repository | `domain/repository/TempleOptionRepository.kt` | Short list (FR-TPL-005) | exists |
| Domain / UseCase | `domain/usecase/GetFeaturedTemplesUseCase.kt`, `GetTemplePageUseCase.kt`, `GetTempleDetailUseCase.kt` | `operator fun invoke` → `Result`; dispatcher via `DispatcherProvider`; `GetTemplePageUseCase` trims + cuts to 100 + empty → `null` (AC9, AC11). `GetTempleDetailUseCase` currently returns sample data → rewrite | exists (detail, sample) — rewrite; the rest not created |
| Domain / UseCase | `domain/usecase/GetTemplesUseCase.kt` | Sample data — this feature stops using it; kept for `GetFestivalCalendarUseCase`, `ChuaSampleData` | exists — keep |
| Data / Remote | `data/remote/api/TempleApiService.kt` | Add `getTop()`, `getList(page, q: String?, region: String?)`, `getDetail(id)`; `getOptions` unchanged | exists — needs changes |
| Data / Remote | `data/remote/dto/TempleDto.kt` | `@Serializable` `TempleSummaryDto`, `TempleLocationDto`, `TemplePageDto`, `TempleDetailDto`, `TempleAmenitiesDto`, `TempleContactsDto` per `openapi.yaml` (new fields ⚠️ contract, declared nullable/default so the old shape still parses — `JsonProvider` already has `ignoreUnknownKeys`) | not created |
| Data / Mapper | `data/mapper/TempleMapper.kt` | DTO → domain: `region` → `TempleRegion` (unknown value → drop the item + log without user data); `""` → `null`; `"HH:MM - HH:MM"` → `OpeningHours` (unparseable → `null`, keep `hoursText`, AC29); `YYYY-MM` → `YearMonth` | not created |
| Data / Repository | `data/repository/TempleRepositoryImpl.kt` | Calls the API via `NetworkClient.safeCall`; `AppError.ApiError(code = TEMPLE_NOT_FOUND)` → `TempleNotFoundException`; the rest `NetworkException(AppError)`; no cache | not created |
| core/location | `core/location/AndroidLocationProvider.kt` | `ContextCompat.checkSelfPermission` (fine **or** coarse); `LocationManager.getCurrentLocation(provider, CancellationSignal, executor, consumer)` (API 30+, minSdk 31) with a 5 s limit, fallback `getLastKnownLocation`; no storing, no logging (AC32) | not created |
| DI | `di/TempleModule.kt` | Add `provideTempleRepository()`, use cases, `provideLocationProvider(context)`, factories for `TempleListViewModel`, `TempleViewModel` and the temple source of `HomeViewModel` | exists — needs changes |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Vectors BR-TPL-07, BR-TPL-09; mapper; repository; ViewModels (debounce, cancelling old requests, paging, errors; home block error → empty); `TempleOptionRepositoryImplTest` (exists); `TempleListUiStateTest` (exists — tests on-device filtering, to be removed) | partly exists |

## Navigation flow

```
HomeRoute ("Chùa Việt Nam" block ← GET /v1/temples/top; failure → empty state)
  ├─ (tap card) ──▶ TempleRoute(templeId)
  └─ (Xem tất cả) ──▶ TempleListRoute (GET /v1/temples, pages 1…n; q, region)
                         ├─ (tap card) ──▶ TempleRoute(templeId)
                         └─ (back) ──▶ HomeRoute
TempleRoute(templeId)  (GET /v1/temples/{id})
  ├─ (Chỉ đường) ──▶ maps app (geo:) | Google Maps in the browser
  ├─ (Hotline / Fanpage / Website / Email) ──▶ dialer | browser | mail
  └─ (back, or "back" in the not-found state) ──▶ previous screen
ChuaRoute / CalRoute / FestivalsRoute / ProfileRoute ──▶ TempleRoute(id)   (other features)
```

Default transition (crossfade) as today. `TempleListViewModel` is scoped to the `TempleListRoute`
entry, so back from `TempleRoute` keeps the scroll position and loaded pages (AC15).

## Step-by-step Flow — Directory: search, filter, paging

```
Step 1 — First page (this feature)
- Trigger: open TempleListRoute; or query (after 300 ms debounce) / region changes; or "Thử lại" on the first-page error
- Pre-condition: query trimmed, cut to 100 characters; empty → null (AC9, AC11)
- Where: TempleListViewModel (collectLatest — the previous request is cancelled) → GetTemplePageUseCase → TempleRepositoryImpl (safeCall)
- API: GET /v1/temples?page=1[&q=][&region=north|central|south]
  ⚠️ Not confirmed (contract): q, region, pages of 12 are not in openapi.yaml @ 0eb7d1f — until they are,
  only page is honoured; the search field and chips still show and send the same request (not filtered yet) — see "Deliberately stubbed".
    - Success → replace items, total, page = 1, totalPages; total = 0 → "Không tìm thấy chùa" (with q or region) / empty (without)
    - Failure (NoConnection / Timeout) → first-page error, offline text + "Thử lại" (AC7)
    - Failure (other, including 400 INVALID_FILTER / INVALID_PAGE) → first-page error, generic text + "Thử lại" (AC7, AC13)

Step 2 — Next page (this feature)
- Trigger: the last visible item is ≤ 3 from the end of the list; or "Thử lại" at the end of the list
- Pre-condition: loadState is not loading; page < totalPages; query/region unchanged since Step 1 (changed → back to Step 1, cancel Step 2)
- Where: TempleListViewModel → GetTemplePageUseCase → TempleRepositoryImpl
- API: GET /v1/temples?page=N+1 (same q, region)
    - Success → append items, page = N+1; empty items → treated as the end (AC6)
    - Failure (any error) → keep items, "Thử lại" at the end (AC8)
```

## Step-by-step Flow — Temple page: load, opening hours, distance

```
Step 1 — Load the temple (this feature)
- Trigger: open TempleRoute(templeId); or "Thử lại"
- Pre-condition: templeId from SavedStateHandle
- Where: TempleViewModel → GetTempleDetailUseCase → TempleRepositoryImpl (safeCall)
- API: GET /v1/temples/{id}
    - Success → loadState = content; run Step 2; if location permission is already granted run Step 4
    - Failure (404 TEMPLE_NOT_FOUND) → "temple not found" + back (AC17)
    - Failure (NoConnection / Timeout) → error with the offline text + "Thử lại"
    - Failure (other) → error with the generic text + "Thử lại"

Step 2 — Open status (this feature, no network call)
- Trigger: Step 1 succeeded; ON_RESUME; a new minute starts (AC28)
- Where: TempleViewModel → TempleOpenStatusRule.evaluate(clock.instant(), hours, holidayHours)
    - hours = null → status hidden, hours row "Đang cập nhật" or the raw hoursText (AC27, AC29)
    - otherwise → labels via TempleFormat, "Hôm nay" marked from todayRow; holidayHours = null → the
      "Rằm & Mùng Một" row shows the normal hours and the rule uses them on lunar days 1 and 15 (AC27)

Step 3 — Ask for location permission (this feature)
- Trigger: Step 1 succeeded, the app has never asked (prefs flag `location_permission_asked`), permission not granted
  ⚠️ Not confirmed: when to ask for the permission (AC31)
- Where: TempleScreen → RequestMultiplePermissions(COARSE, FINE) → TempleViewModel.onLocationPermissionResult; write the asked flag to core/storage prefs
    - Granted (fine or coarse) → Step 4
    - Denied → distance line hidden, never asked again

Step 4 — Distance (this feature, no backend call)
- Pre-condition: location permission granted
- Where: TempleViewModel → LocationProvider.current() (AndroidLocationProvider, 5 s limit) → TempleDistance.haversineKm
    - GeoPoint → distanceKm → TempleFormat.distanceLabel (AC30); the location is kept only in a local variable, never in UiState/logs (AC32)
    - null / timeout / error → line hidden
```

## Local storage

- No Room, no offline copy (online-only, see Overview). Images: Coil's default cache (memory + disk in
  the app's cache directory).
- `core/storage` prefs: one flag `location_permission_asked` (Boolean) for AC31 — not a secret.
- User location: **not** stored anywhere (AC32, product §3.5).
- Directory `query`/`region`: `SavedStateHandle` (AC14), not prefs.

## Deliberately stubbed for now

- **Search/filter and pages of 12** (⚠️ contract, Phase 3): `TempleApiService.getList` declares
  `q`/`region` nullable from the start; until backend v2.0 AC 39–44 are in `openapi.yaml`,
  `TempleRepositoryImpl` still sends them (`TempleHandler.List` currently reads only `page` and ignores
  other parameters — read in innerlamp-be @ 0eb7d1f) — i.e. the directory is **not filtered yet** even
  though the UI has the search field and chips. No on-device filtering fallback (BR-TPL-05,
  NFR-TPL-02). Page size is always read from `page_size` (currently 20).
- **New temple-page fields** (⚠️ contract): the DTO declares them nullable; while absent, the Rằm hours
  row shows the normal hours (BR-TPL-09), the sangha body and contacts show "Đang cập nhật"
  (BR-TPL-06), and the footer is hidden.
- **Later fields** ("Kinh sách", architecture style, materials, highlights, photo set — BR-TPL-06): not
  in the contract, no DTO field; the UI renders fixed "Đang cập nhật" rows until the product and
  contract add them.
- **Display labels for the** `type`/`sect`/`heritage_rank` **codes** (⚠️ product, AC4): `TempleFormat`
  keeps a provisional table, marked as provisional, until product answers; unknown codes show the raw
  code.
- **Sự kiện & khóa học**: always the empty state (FR-TPL-009) until `sangha` has an Android spec.
- **UI and data layer**: `top`, the paged list and the detail are already in the contract, so Phase 3
  for those three operations is not blocked — done together with Phase 2, no new sample data added to
  `main`. `FakeTempleRepository` lives only in `app/src/test/…`; `@Preview` builds `UiState` directly
  for the stateless parts (`TempleContent`, …).

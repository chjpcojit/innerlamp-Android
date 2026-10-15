# Tasks — Temple

Version: 1.2
Last updated: 2026-10-15
Status: planned (drift) — only `TempleOptionRepository` (short list) and the routes match the spec; everything else `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Every item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when verified to exist **and to match what product spec v1.2 requires** in the code on
2026-10-15; an item that has code but drifts from the product stays `[ ]` and is noted "(has code,
drift: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Already there, keep

- [x] `domain/repository/TempleOptionRepository.kt` + `data/repository/TempleOptionRepositoryImpl.kt`
      (`GET /v1/temples/options` via `safeCall`, drops an empty `q`, cuts to 100 characters) +
      `di/TempleModule.kt` `provideTempleOptionRepository` — used by `../Onboarding/` (FR-TPL-005, AC16)
- [x] `TempleListRoute`, `TempleRoute(templeId)` in `ui/navigation/Routes.kt` and the home → directory
      → temple page wiring in `MainActivity.kt` (AC15; the `onOffer`/`onSchedule` callbacks must go —
      Phase 2)

## Phase 1 — Domain

- [ ] `domain/model/Temple.kt`: `Temple`, `TempleLocation`, `TempleRegion` (three regions, BR-TPL-04),
      `TemplePage`; remove the old `Temple` from `domain/model/HomeContent.kt` (update
      `GetTemplesUseCase`, `GetFestivalCalendarUseCase`, `ChuaSampleData` so they still compile — they
      keep their own sample data)
- [ ] `domain/model/TempleContent.kt` → `TempleDetail` per `design.md` (has code, drift: stats,
      schedules, `TempleTone`/`TempleGlyph` — remove); `OpeningHours`, `TempleAmenities`,
      `TempleContacts`
- [ ] `domain/temple/TempleOpenStatusRule.kt` — BR-TPL-09, Vietnam time, `LunarCalendar`, normal hours
      on lunar days 1 and 15 when there are no holiday hours (AC27)
- [ ] `domain/temple/TempleDistance.kt` — haversine (BR-TPL-07, AC30)
- [ ] `domain/location/LocationProvider.kt` (port, AC30)
- [ ] `domain/repository/TempleRepository.kt` + `TempleNotFoundException` (AC17)
- [ ] `domain/usecase/GetFeaturedTemplesUseCase.kt`, `GetTemplePageUseCase.kt` (trim, cut to 100,
      empty → `null` — AC9, AC11); rewrite `GetTempleDetailUseCase.kt` to take the repository and
      return `Result` (has code, drift: returns one sample temple for every id)

## Phase 2 — UI (`il-ui-builder`)

- [ ] Featured block: `HomeViewModel` reads `GetFeaturedTemplesUseCase`, the block has its own state
      (loading / content / empty); card per AC4 (Coil image + placeholder); empty (AC2); a failed load
      shows the same empty state — no error text, no retry, no sample temples (AC3); remove temples from
      `HomeSampleData` (has code, drift: sample data, card has `note`)
- [ ] Display labels for `type`/`sect`/`heritage_rank`/region in `ui/screens/temple/TempleFormat.kt`
      (AC4; ⚠️ product — provisional table marked as provisional)
- [ ] Directory: rewrite `TempleListUiState` + `TempleListViewModel` — paging (AC5, AC6, AC8), debounce
      + cancelling old requests (AC9), clearing the search (AC11), four fixed chips (AC12),
      `SavedStateHandle` (AC14) (has code, drift: on-device filtering by name + place, data-derived
      chips including "Tây Nguyên", no paging)
- [ ] `TempleListScreen`: number-found line, next page at the end, "Thử lại" at the end of the list,
      first-page error state (offline / generic), "Không tìm thấy chùa" + hint (AC7, AC8, AC10, AC13;
      ⚠️ wording)
- [ ] Temple page: rewrite `TempleUiState`/`TempleViewModel` (remove `isFollowing`, `toggleFollow`);
      loading / error / not-found states (AC17)
- [ ] `TempleScreen`: sections per FR-TPL-007 (AC18), introduction collapsed to 5 lines (AC19),
      numbered notes (AC20), amenities with "Kinh sách" always "Đang cập nhật" (AC21), architecture
      with style / materials / highlights / photo set as visible "Đang cập nhật" rows (AC22), location &
      contacts (AC23), footer (AC24); remove "Theo dõi", "Cúng dường", stats, "Thời khóa sắp tới" (AC25;
      has code, drift: BR-TPL-08)
- [ ] "Sự kiện & khóa học": fixed empty state, no follow hint, no "Xem tất cả" (AC26)
- [ ] Hours card: status label, two hour rows, "Hôm nay"; the "Rằm & Mùng Một" row shows the normal
      hours when the temple has no holiday hours; recompute on `ON_RESUME` and every minute (AC27,
      AC28, AC29)
- [ ] Location permission: `ACCESS_COARSE_LOCATION` + `ACCESS_FINE_LOCATION` in `AndroidManifest.xml`;
      ask once (prefs flag `location_permission_asked`), never ask again after a denial (AC31;
      ⚠️ when to ask); "Cách bạn …" line (AC30)
- [ ] `ui/screens/temple/TempleIntents.kt` + `<queries>` in the manifest: `geo:` directions + Google
      Maps web fallback (AC33); dialer, browser, mail; no crash without an app (AC34); wire
      `onDirections` in `MainActivity.kt` (has code, drift: no-op)
- [ ] `MainActivity.kt`: remove `onOffer`, `onSchedule` from `TempleCallbacks`; create ViewModels via
      the `di/TempleModule` factory

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Confirm each operation in innerlamp-be `docs/specs/api/openapi.yaml` (cite, never copy the
      schema): `top`, list, detail exist @ 0eb7d1f; `q`/`region`/pages of 12 and the new detail fields
      do **not** (⚠️ contract — backend hand-off, "Deliberately stubbed")
- [ ] `data/remote/api/TempleApiService.kt`: `getTop`, `getList(page, q, region)`, `getDetail(id)`
- [ ] `data/remote/dto/TempleDto.kt` (new fields nullable/default so the old shape still parses)
- [ ] `data/mapper/TempleMapper.kt`: region, `""` → `null`, `HH:MM - HH:MM` hours (AC29; ⚠️ contract,
      backend AC46), `YYYY-MM`
- [ ] `data/repository/TempleRepositoryImpl.kt` via `NetworkClient.safeCall`; `TEMPLE_NOT_FOUND` →
      `TempleNotFoundException`; other `AppError`s per the Error → UI table
- [ ] `core/location/AndroidLocationProvider.kt` (`LocationManager.getCurrentLocation`, 5 s limit,
      last-known fallback; no storing/logging — AC30, AC32)
- [ ] `di/TempleModule.kt`: repository, use cases, location provider, ViewModel factories
- [ ] When the backend has `q`/`region`/pages of 12 and the new fields in `openapi.yaml`: update the
      `Contract:` line of `requirements.md`, remove the ⚠️ (contract) markers, recheck AC9–AC12, AC27,
      AC29

## Phase 4 — Tests

- [ ] Script `scripts/sync-product-vectors.sh` (shared with `../Memorial/`): copies innerlamp-be
      `docs/product/temple/rules/{temple-open-status,temple-distance}.json` to
      `app/src/test/resources/product/temple/rules/` with a `SOURCE` file (commit +
      `product_spec_version`); the copies are never edited by hand
- [ ] Run every case of `rules/temple-open-status.json` (BR-TPL-09) through `TempleOpenStatusRule` +
      `TempleFormat` — including the time-zone, lunar-day and "Rằm without holiday hours uses normal
      hours" cases (AC35)
- [ ] Run every case of `rules/temple-distance.json` (BR-TPL-07) through `TempleFormat.distanceLabel`;
      a separate test for `TempleDistance.haversineKm` (AC35)
- [ ] Mapper: `""` → `null`, unparseable hours, unknown region, `null` month
- [ ] Repository: success, 404 `TEMPLE_NOT_FOUND`, `NoConnection`, 5xx, `400 INVALID_FILTER`
- [ ] `TempleListViewModel`: debounce, cancelling old requests, clearing the search drops `q`, a region
      change returns to page 1, one request per next page, stop at the last page, a later-page error
      keeps the list, `SavedStateHandle` (AC5–AC14)
- [ ] `TempleViewModel`: load / 404 / error; open status with a fixed `Clock`; distance with
      `FakeLocationProvider` (value / `null`) (AC17, AC27–AC30)
- [ ] `HomeViewModel`: temple block content / empty / failure → the same empty state, no error, no
      retry (AC1–AC3)
- [ ] Remove `TempleListUiStateTest` (tests on-device filtering — behavior dropped per BR-TPL-05)
- [ ] Measure AC36 (NFR-TPL-01) by hand on a real device, 4G, once the backend has `q`/`region`
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (run by the user — see `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Report the Android column of innerlamp-be `docs/product/temple/product.md` §4.2 to `ba-writer`
      (this spec does not edit the backend repo)
- [x] Remove the temple part of `Docs/home_assumptions.md` lines #1 (temple images / temple carousel),
      #3 (temples from `HomeSampleData`), #13 (temple block) — v1.1, done early in step 4 of the docs
      process: those lines now point to this spec (the current drift is in the state table of
      `requirements.md`); the Buddha / article parts stay
- [ ] OKF concept via `il-okf-writer` once the feature is verified end-to-end

## Deferred (not blocking Temple)

- Offline reading (product §3.6 "may") — only if the product owner wants it.
- "Sự kiện & khóa học" content once `sangha` has an Android spec.
- Switching sample ids (`tu-quang`, `tp-…`) in profile, lunar calendar, festival calendar and the Chùa
  tab to real ids — those features' specs.
- Real data for architecture style, materials, highlights, "Kinh sách" and the photo set once the
  contract has them (BR-TPL-06 "later"; until then they show "Đang cập nhật" — Phase 2).

# Tasks — Calendar

Version: 1.2
Last updated: 2026-10-17
Status: in-progress — entry points, the Home carousel and the per-month list exist on the local catalogue; the new domain, detail screen, API + device copy and vectors are still `[ ]`; Phase 3 is unblocked (contract 2.2.0 is final, but the backend commit is not pushed yet)

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when the code was checked on 2026-10-15 (`main` @ bebaa9c) to exist **and** to match what
product spec v1.2 requires; an item that has code but deviates from the product spec stays `[ ]` with
"(code exists, deviates: …)". The v1.2 re-sync (2026-10-17) was docs only and did not re-read the code.
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 1 — Domain

- [ ] `domain/model/CalendarEvent.kt` — `CalendarYear`, `CalendarGroup`, `CalendarEvent` (with `isMajor`,
      `sortOrder`, `recurrence`), `Recurrence` (`Solar`, `Lunar`, `LunarMonthly`, `LunarLastDay`,
      `SolarTerm`, **`Unknown`** for any value the app does not know — open-ended decode),
      `CalendarOccurrence`, `LunarDay` (with `isLeapMonth`), `CalendarYearSource`. Mùng 1 / Rằm are
      recognised by `recurrence == LunarMonthly`, never by comparing `code` or computing locally
- [ ] `domain/calendar/CalendarCountdown.kt` — BR-CAL-13 (AC21; code exists, deviates: the logic lives in
      `FestivalsUiState.countdown`, and the word "còn" is lowercase)
- [ ] `domain/calendar/CalendarOrdering.kt` — comparator of BR-CAL-12 (AC5)
- [ ] `domain/calendar/HomeEventBlock.kt` — row selection + "+N" per BR-CAL-15, `BUDDHIST_GROUP`
      (AC19; `buddhist` is a stable code, confirmed by backend 2026-10-17). "Current month" is Vietnam time
      (`Asia/Ho_Chi_Minh`); the block shows no countdown
- [ ] `domain/calendar/EventContentFormat.kt` — description paragraphs, ritual items per BR-CAL-14 (AC15).
      Split on newlines only, never on comma / semicolon: `guidance` line = one numbered action,
      `long_description` line = one paragraph; an empty long description is hidden, never replaced by the
      short one
- [ ] `domain/repository/CalendarEventRepository.kt` (interface: `deviceCopy`, `load`, `cachedInMemory`;
      `CalendarLoadError.Offline` / **`RateLimited(retryAfterSeconds: Int?)`** / `Failed`)
- [ ] `domain/usecase/LoadCalendarYearUseCase.kt` — current year in `Asia/Ho_Chi_Minh`, cap at 2100
      (AC2, AC12, AC13; Step 1–2). A year already loaded in the session is never refetched unless the
      user taps retry (AC31)
- [ ] `domain/usecase/GetHomeEventBlockUseCase.kt` — six months from the current month, one or two years
      in parallel, a missing year → an error on its cards only (AC19, AC27)
- [ ] `domain/usecase/GetEventOccurrenceUseCase.kt` — find `(eventCode, solarDate)` in the year (AC14,
      AC18)
- [ ] Public-holiday rule (BR-CAL-11) in the festival list / detail mapping: a public holiday is marked
      only on the event's own day; never show substitute days off (no nghỉ bù) (AC32)
- [ ] Delete `GetBuddhistEventMonthsUseCase.kt`, `GetFestivalCalendarUseCase.kt` (including `MAJOR_KEYS` —
      8 keys, which does not match the 10 of BR-CAL-10, so it is deleted, **not** ported — and
      `hostsFor`), `domain/model/FestivalCalendar.kt`, `BuddhistEvent`/`EventMonth` in `HomeContent.kt` —
      only after Phase 3 replaces the source (AC3; BR-CAL-05)
- [ ] `GetUpcomingFastDaysUseCase` reads `CalendarEventRepository` instead of the local catalogue (⚠️
      product — default keeps the current display; see `design.md`)
- [ ] Remove every hard-coded via-date list (dates of Buddhas / Bodhisattvas) in the app: via dates now
      come only from this calendar (BR-DTY-05). Cross-reference: `docs/Specs/Deity` owns the Deity side
      and is **not edited here**; track the removal there. The catalogue gained 6 Buddha / Bodhisattva
      via events (Tỳ Lô Giá Na 15/4, Hư Không Tạng 15/1, Kim Cương Thủ 8/4, Lục Độ Mẫu 8/1, Bạch Độ Mẫu
      15/10, Kim Cương Tát Đỏa 15/7); their dates come from `phat_botat.json` and are **not yet checked
      against GHPGVN** (⚠️ Not confirmed)

## Phase 2 — UI (`il-ui-builder`)

- [x] `FestivalsRoute` + wiring in `MainActivity.kt`; entry points from Home ("Tất cả", "+N"), the Chùa
      tab ("Lịch lễ", festival row) and the Buddha/Bodhisattva detail ("Lịch lễ") (AC6, entry-point part)
- [x] No login required for the festival calendar and the Home block (AC1, access part — keep it when
      switching to the API)
- [x] The list has "Tháng M, YYYY" headings; on open and on chip change it scrolls to the current month
      (AC6, scroll part)
- [x] Month-card carousel on Home: at most 3 rows, "+N sự kiện khác", "Tất cả" → `FestivalsRoute`
      (AC19, layout part)
- [ ] Festival calendar by year: current year, load the next year when the user reaches the end, stop at
      2100, "Thử lại" row (AC6, AC12, AC13; code exists, deviates: preloads 3 years, drops empty months)
- [ ] Festival rows: lunar date with "(nhuận)" on the lunar date (not on the event name), major mark from
      `isMajor`, group label from `data.groups`, tap opens the detail (AC4, AC7, AC8; code exists,
      deviates: no group label, colour by `isFestival`, not tappable)
- [ ] Remove the hosting temples (temple count, temple chip, `FestivalsCallbacks.onTemple`) and the
      sentence about temples (AC9; code exists, deviates: sample temple data)
- [ ] Chips "Tất cả" / "Lễ lớn" / one per group, each with a count; drop "Rằm & Mùng Một"; keep the chip
      when the next year is appended (AC10, AC11; code exists, deviates: "Lễ lớn" filters `isFestival`).
      "Lễ lớn" = `isMajor` (the 10 events of BR-CAL-10)
- [ ] `FestivalsUiState` / `FestivalsViewModel`: `loadState`, `nextYearState`, `CalendarFilter`, a factory
      from `di/CalendarModule.kt` instead of the default `viewModel()`; recompute on `ON_RESUME` when the
      date changes (AC20)
- [ ] Loading state, device copy + "Dữ liệu có thể chưa được cập nhật.", offline / failure / **rate-limited**
      errors + "Thử lại" with the strings of the Error → UI table (AC23–AC26, AC28, AC31; copy per product
      §3.1.1 messages table). On `RateLimited`: show the retry-later message (mentions waiting,
      `retryAfterSeconds` when present), no automatic retry loop, retry only on user tap (AC31; ⚠️
      message text is a product default — hand back to `ba-writer`)
- [ ] `FestivalDetailRoute(eventCode, solarDate)` in `Routes.kt` + `composable<…>` with explicit
      transitions in `MainActivity.kt`; fix the `FestivalsRoute` KDoc
- [ ] `FestivalDetailScreen` + `FestivalDetailUiState` + `FestivalDetailViewModel`: image (Coil +
      placeholder fallback), group + major mark, name, lunar date, solar date + weekday + countdown,
      description by paragraph (one `long_description` line = one paragraph; hidden when empty), numbered
      rituals (one `guidance` line = one action), hide empty sections, not found ("Không tìm thấy ngày lễ
      này."), load error; no sutras / temples / reminder / previous-next (AC14–AC18; ⚠️ default image)
- [ ] Home block: six months from the current month (Vietnam time), tiered through
      `GetHomeEventBlockUseCase`, empty-month card ("Tháng này không có ngày lễ Phật giáo."), error in
      place of the cards / on individual cards, device-copy sentence, row → `FestivalDetailRoute`
      (`HomeCallbacks.onEventDetail`), name + lunar date only, **no countdown** (AC19, AC24, AC25, AC27;
      code exists, deviates: 12 solar-year months, no tiers, rows open the festival calendar); rebuild on
      `ON_RESUME` when a new month / day starts (AC20)
- [ ] Chùa tab: `ChuaViewModel` takes "Ngày lễ & ngày chay sắp tới" through the new use case (⚠️ product)
- [ ] Fake `CalendarEventRepository` (sample JSON following the contract 2.2.0 fields, debug / test only)
      for tests and debugging until Phase 3 lands; record it in `design.md` "Deliberately stubbed"

## Phase 3 — Data (`il-data-builder`)

Unblocked: `calendarEvents` is final in contract 2.2.0 (public, `is_major`, `sort_order`, `recurrence`,
429). Items below are still to implement.

- [x] The backend contract is final: `GET /v1/calendar/events` (`calendarEvents`) in innerlamp-be
      `docs/specs/api/openapi.yaml` `info.version` 2.2.0 — public, `is_major`, `sort_order`, `recurrence`,
      429 `RATE_LIMITED`, `Cache-Control` public with `year` — commit 3af0844 on branch
      `feat/calendar-v3-public-major-via`, merged to main as 80de1da (innerlamp-be PR #55); cite, do not copy the schema.
      Re-cite the commit after the backend merges (hand-off)
- [ ] `data/remote/api/CalendarApiService.kt` (not `@Authenticated`; send **no** `Authorization`; **never
      send `X-API-Key`** — website only, a wrong key gives `401 INVALID_API_KEY`) +
      `data/remote/dto/CalendarEventsDto.kt` (AC1, AC2)
- [ ] `data/mapper/CalendarEventMapper.kt` — groups, flags, unknown `recurrence` → `Recurrence.Unknown`,
      unknown group code (AC3, AC4)
- [ ] `core/database/calendar/` — `CalendarCacheDatabase` (`innerlamp_calendar.db`, destructive
      migration), `CalendarYearCacheEntity`, `CalendarYearCacheDao` (`upsert`, `get`, `deleteBefore`) +
      schema export (AC22; ⚠️ storage approach — default already chosen). Android keeps its own Room cache
      and uses **no** OkHttp HTTP cache (the response is `public, max-age=86400` with `year`, but the app
      always sends an explicit year and does not rely on it)
- [ ] `data/repository/CalendarEventRepositoryImpl.kt` — memory → Room → `NetworkClient.safeCall`; write
      Room after success; map `AppError` per the Error → UI table, **splitting 429 out of the generic HTTP
      error** into `CalendarLoadError.RateLimited(retryAfterSeconds)` (read `details.retry_after_seconds`,
      fall back to the `Retry-After` header, else `null`); a 401 is an unexpected failure and maps to
      `Failed` (Step-by-step Flow "Load one year"; AC22–AC25, AC28, AC31)
- [ ] `di/CalendarModule.kt`; one repository instance held in `InnerLampApplication`; replace the fake
      repository and the local sources in Home, the festival calendar and the Chùa tab; then do the
      "Delete" item of Phase 1

## Phase 4 — Tests

- [ ] Script `scripts/sync-product-vectors.sh` (shared with Memorial Phase 5): copy innerlamp-be
      `docs/product/calendar/rules/{countdown,ordering,home-block}.json` (from `../innerlamp-be` at a
      stated commit) into `app/src/test/resources/product/calendar/rules/` with a `SOURCE` file (commit +
      each file's `product_spec_version`); the copies are **never edited by hand**. Do not copy
      `event-placement.json` (backend only, product §4.1)
- [ ] `CalendarProductVectorsTest` runs **every case** (AC30): `countdown.json` (5) through
      `CalendarCountdown`, `ordering.json` (4) through `CalendarOrdering`, `home-block.json` (5) through
      `HomeEventBlock.select` (`every_lunar_month` ↔ `Recurrence.LunarMonthly`); fail when a
      `product_spec_version` differs from `SOURCE` (at 0eb7d1f: countdown `1.0`, ordering `1.1`,
      home-block `1.1`; ⚠️ re-check the versions when `SOURCE` moves to the v1.2 commit)
- [ ] `EventContentFormat`, `CalendarEventMapper` unit tests (BR-CAL-14: newline-only split, empty long
      description hidden; contract 2.2.0 fields; unknown `recurrence` → `Unknown`; unknown group code)
- [ ] BR-CAL-11 test (AC32): an event flagged as public holiday is marked only on its own day; no
      substitute-day row or mark is produced
- [ ] `CalendarEventRepositoryImpl` tests (MockWebServer or fake `NetworkClient` + in-memory Room):
      success writes Room and deletes older years; each Error → UI row; with / without device copy; a 401
      does not touch the session and maps to `Failed`; **429 `RATE_LIMITED` maps to
      `RateLimited(retry_after_seconds)`, header fallback, `null` when neither is present, with no
      automatic retry** (AC31); the request carries no `Authorization` and no `X-API-Key`
- [ ] Use case + ViewModel tests (`kotlinx-coroutines-test`, Turbine, MockK): device copy then refresh,
      next-year load / 2100 / retry, chips + counts, detail not found, Home with two years where one
      fails, date change, a loaded year is not refetched in the session without a retry tap (AC31),
      rate-limited state shows the retry-later message and waits for the user
- [ ] Rewrite `FestivalsViewModelTest`, `HomeViewModelTest`; delete `GetBuddhistEventMonthsUseCaseTest`
      together with the use case (keep `LunarCalendarTest` — used by Sổ giỗ and Lịch âm)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (the user runs it; the project has flavors, so it
      is not `testDebugUnitTest`)
- [ ] On-device QA: a guest opens the festival calendar (no login prompt), airplane mode with / without
      device copy, Home offline, 2 s on 4G / 0.5 s from device copy (AC29, NFR-CAL-03), compare with the
      `festVals` and `festDetail` prototype screens

## Phase 5 — Documentation

- [ ] Report the Android column status of product §4.2 to `ba-writer` (this skill does not edit the
      backend repo). v1.2 already sets BR-CAL-11 and BR-CAL-14 to `planned` for Android
- [x] Delete `Docs/home_assumptions.md` item #11 (superseded by the product spec; old-doc cleanup,
      2026-10-15)
- [ ] OKF concept through `il-okf-writer` once the feature is verified end to end

## Deferred (not blocking Calendar)

- Rename the package `ui/screens/festivals/` to the `calendar` slug (clashes with `ui/screens/calendar/`
  of Lịch âm).
- Previous / next event on the detail (FR-CAL-009 — website; Android may add it later, §3.6).
- OkHttp HTTP cache for `Cache-Control: public` (the app uses its own Room cache; not needed while the
  app always sends an explicit year).
- Real event images + a shared default image (when product supplies them, §2.7).

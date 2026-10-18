# Design — Calendar

Version: 1.1
Last updated: 2026-10-17
Product spec: `innerlamp-be` `docs/product/calendar/product.md` v1.2
Contract: `innerlamp-be` `docs/specs/api/openapi.yaml` info.version 2.2.0 (`calendarEvents`, final and public; commit 3af0844, merged to main as 80de1da via innerlamp-be PR #55, from branch `feat/calendar-v3-public-major-via`)
Status: in-progress — the festival calendar and the Home block still run on the local catalogue; the new domain, the detail screen and the data layer (API + Room) and the vectors do not exist yet; the contract is now final, so Phase 3 is unblocked (still to implement)

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Three places show events — the "Lịch lễ Phật giáo" screen (`FestivalsScreen`), the new "Chi tiết ngày lễ"
screen (`FestivalDetailScreen`) and the "Sự kiện Phật giáo" block on the Home page (`EventsSection`) —
all read one `CalendarEventRepository` (domain). Target path: `ViewModel → use case →
CalendarEventRepository → CalendarEventRepositoryImpl → NetworkClient.safeCall` (`GET /v1/calendar/events`,
public) plus Room for the on-device copy. The style is **online-first, show the on-device copy first**:
if a copy exists it is shown at once while the network call runs; on success it is replaced and written
to Room; on failure the copy is kept with the "may be out of date" message; with no copy, an error plus
a retry is shown (FR-CAL-007, FR-CAL-008, NFR-CAL-03). The app **computes no festival date**: only three
pure functions in `domain/calendar/` cover what the product assigns to the client — the countdown
(BR-CAL-13), the ordering comparison (BR-CAL-12) and the choice of Home block rows (BR-CAL-15) — each
with a product vector. Android additionally implements BR-CAL-11 (the public holiday is shown only on
its own day, no substitute days off) and the BR-CAL-14 text splitting (both `planned` for Android in
product v1.2).

Current state: `GetBuddhistEventMonthsUseCase` generates events itself from 19 fixed festivals plus
Rằm/Mùng Một using `LunarCalendar`; `GetFestivalCalendarUseCase` flags major festivals through
`MAJOR_KEYS` and a sample of host temples; `FestivalsViewModel`, `HomeViewModel`, `ChuaViewModel` call
them synchronously in the constructor, with no coroutine and no repository; ViewModels are created with
the default `viewModel()`. The architecture stays `ui → domain ← data → core/{network,storage}`, manual
DI in `di/`, thread switching through `DispatcherProvider`. Closest patterns:
`AdministrativeUnitRepositoryImpl` / `di/AdministrativeUnitModule.kt` (public catalogue through
`safeCall`, one shared instance) and `RoomSutraQuoteRepository` / `di/DatabaseModule.kt` (Room).
`JsonProvider` already has `ignoreUnknownKeys` on.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. State read on 2026-10-15 (the Status
column was not re-audited on 2026-10-17; only the contract-driven rows were edited).

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Screen | `ui/screens/festivals/FestivalsScreen.kt` | Festival calendar: chips "Tất cả" / "Lễ lớn" / one chip per group with a count (AC10); month title; row: solar date + weekday, name, lunar date + "(nhuận)" (on the lunar date, never in the event name), countdown, major mark, group label (AC4, AC7); tapping a row → detail (AC8); last row loads next year / "Thử lại" (AC12, AC13); loading / on-device copy / error states (AC23–AC26, AC31). The "Lễ lớn" chip and the major mark come from `isMajor` (BR-CAL-10). **Remove** host temples, the temple intro sentence, the "Rằm & Mùng Một" chip (AC9, AC10) | exists — needs change |
| UI / State | `ui/screens/festivals/FestivalsUiState.kt` | `years: List<CalendarYear>`, `groups`, `filter: CalendarFilter` (`All` / `Major` / `Group(code)`), `rows`, `counts` per chip, `todayRowIndex`, `loadState` (loading / online / on-device copy / error: offline / rate limited (with `retryAfterSeconds`) / other), `nextYearState` (idle / loading / error / end — 2100); the countdown goes through `CalendarCountdown` | exists — needs change (currently built from `FestivalMonth`) |
| UI / ViewModel | `ui/screens/festivals/FestivalsViewModel.kt` | `LoadCalendarYearUseCase` in `viewModelScope`: current year (VN) then next year when the end is reached; `today` from an injected `Clock`, recomputed on `ON_RESUME` (AC20); factory from `di/CalendarModule.kt` | exists — needs change |
| UI / Screen | `ui/screens/festivals/FestivalDetailScreen.kt` | Image (Coil `AsyncImage`, placeholder fallback — AC16), group label + major mark, name, lunar date, solar date card + weekday + countdown, description by paragraph (one paragraph per line of `long_description`; an empty description hides the section, it is never replaced by the short one), numbered practices (one action per line of `guidance`), empty sections hidden (AC14, AC15); not found (AC18); no sutra/temple/reminder/before-after (AC17) | not created |
| UI / State + ViewModel | `ui/screens/festivals/FestivalDetailUiState.kt`, `FestivalDetailViewModel.kt` | Takes `eventCode`, `solarDate` from `SavedStateHandle.toRoute()`; `GetEventOccurrenceUseCase`; states loading / found / not found / load error | not created |
| UI / Home | `ui/screens/home/HomeSections.kt` (`EventsSection`, `EventMonthCard`, `EventRow`), `HomeUiState.kt`, `HomeViewModel.kt`, `HomeScreen.kt` | Six month cards from `GetHomeEventBlockUseCase` (AC19); empty month card; error in place of a card or on each card (AC25, AC27); on-device copy message (AC24); rows show name and lunar date only, **no countdown** (FR-CAL-006); row → `FestivalDetailRoute`, "Tất cả" / "+N" → `FestivalsRoute`; `HomeCallbacks` gains `onEventDetail(code, date)` | exists — needs change (12 months, no tiers, row opens the festival calendar) |
| UI / Chùa tab | `ui/screens/chua/ChuaViewModel.kt`, `domain/usecase/GetUpcomingFastDaysUseCase.kt` | "Ngày lễ & ngày chay sắp tới" block: switch the source to `CalendarEventRepository` (see note below) | exists — needs change |
| UI / Navigation | `ui/navigation/Routes.kt`, `MainActivity.kt` | `FestivalsRoute` (exists); add `@Serializable data class FestivalDetailRoute(val eventCode: String, val solarDate: String)` (ISO `YYYY-MM-DD`), registered with `composable<…>` and explicit transitions (Navigation design: route is a `data class`); fix the KDoc of `FestivalsRoute` (it still says 3 years + temples) | exists — needs change |
| Domain / Model | `domain/model/CalendarEvent.kt` | `CalendarYear(year, groups, events, occurrences)`, `CalendarGroup(code, name)`, `CalendarEvent(code, name, shortDescription, longDescription, guidance, imageUrl?, groupCodes, isMajor, sortOrder, recurrence)`, `Recurrence` (`Solar`, `Lunar`, `LunarMonthly`, `LunarLastDay`, `SolarTerm`, **`Unknown`**), `CalendarOccurrence(eventCode, solarDate: LocalDate, lunar: LunarDay(day, month, year, isLeapMonth))`, `CalendarYearSource` (`Network` / `DeviceCopy`) | not created |
| Domain / Rules | `domain/calendar/CalendarCountdown.kt` | `of(today, solarDate)` → `days` + label (`Today` / `Tomorrow` / `DaysLeft(n)` / `Passed`) per BR-CAL-13; the UI turns the label into text | not created (logic currently lives in `FestivalsUiState.countdown`) |
| Domain / Rules | `domain/calendar/CalendarOrdering.kt` | `Comparator` for BR-CAL-12 (solar date, `sortOrder`, `code`) | not created |
| Domain / Rules | `domain/calendar/HomeEventBlock.kt` | `select(monthOccurrences)` → `shown` (≤ 3) + `more` per BR-CAL-15; `BUDDHIST_GROUP = "buddhist"` (stable code, AC19); tier 3 = `Recurrence.LunarMonthly` (Mùng 1 / Rằm are recognised **only** by `recurrence == lunar_monthly`, never by comparing `code` and never by computing locally); unknown `recurrence` (`Recurrence.Unknown`) counts as a named event (tier 2) | not created |
| Domain / Rules | `domain/calendar/PublicHoliday.kt` | BR-CAL-11: the public-holiday flag is shown only on the occurrence's own solar date; the app never derives or shows substitute days off (no "nghỉ bù") (AC32) | not created |
| Domain / Content | `domain/calendar/EventContentFormat.kt` | `paragraphs(longDescription)`, `practices(guidance)` per BR-CAL-14: split on newlines only (each line of `long_description` = one paragraph, each line of `guidance` = one action rendered as a numbered list), strip leading bullets, drop empty lines; **never split on comma or semicolon**; an empty result hides the section | not created |
| Domain / Repository | `domain/repository/CalendarEventRepository.kt` | `deviceCopy(year): CalendarYear?`, `load(year): Result<CalendarYearLoad>` (data + `source` + "may be out of date" flag), `cachedInMemory(year)`; domain errors `CalendarLoadError.Offline` / `RateLimited(retryAfterSeconds: Int?)` / `Failed` | not created |
| Domain / UseCase | `domain/usecase/LoadCalendarYearUseCase.kt`, `GetHomeEventBlockUseCase.kt`, `GetEventOccurrenceUseCase.kt` | `Flow`/`Result`; current year by `Asia/Ho_Chi_Minh` (AC2); Home: the "current month" is the Vietnam-time month (`Asia/Ho_Chi_Minh`, BR-CAL-15) + 5 months, loading one or two years in parallel (AC19, AC27); detail: find `(code, date)` in the year (AC14, AC18); dispatcher via `DispatcherProvider` | not created |
| Domain / Remove | `domain/usecase/GetBuddhistEventMonthsUseCase.kt`, `GetFestivalCalendarUseCase.kt`, `domain/model/FestivalCalendar.kt`, `BuddhistEvent`/`EventMonth` in `domain/model/HomeContent.kt` | Local catalogue + local computation, `MAJOR_KEYS` (8 keys, which does not match the 10 festivals of BR-CAL-10 — **deleted, not ported**; `isMajor` from the API replaces it), sample temples — delete when Phase 3 runs (BR-CAL-05) | exists — delete |
| Domain / Keep | `domain/calendar/LunarCalendar.kt` | No longer used by this feature; kept for the Memorial book (BR-MEM-10) and the Lunar calendar screen | exists — unchanged |
| Data / Remote | `data/remote/api/CalendarApiService.kt`, `data/remote/dto/CalendarEventsDto.kt` | `@GET("v1/calendar/events") suspend fun getEvents(@Query("year") year: Int): ApiResponse<CalendarEventsPageDto>`; **not** `@Authenticated` (the route is public; an `Authorization` header would be ignored) and **no** `X-API-Key` (website-only; a wrong key gives 401 `INVALID_API_KEY`); `@Serializable` DTOs per `openapi.yaml` (`calendarEvents`, final): each event has required `is_major` (bool), `sort_order` (int ≥ 1), `recurrence` (string, decoded open-ended) | not created |
| Data / Mapper | `data/mapper/CalendarEventMapper.kt` | DTO ↔ domain; unknown `recurrence` → `Recurrence.Unknown` (event kept, treated as a named event); a group code absent from `groups` → ignored (as in the backend design Step 4) | not created |
| Data / Local | `core/database/calendar/CalendarCacheDatabase.kt`, `CalendarYearCacheEntity.kt`, `CalendarYearCacheDao.kt` | Separate DB `innerlamp_calendar.db`: one row per year `(year PK, payload_json, fetched_at_epoch_ms)`; `upsert`, `get(year)`, `deleteBefore(year)`; `fallbackToDestructiveMigration` (it is only a cache) | not created |
| Data / Repository | `data/repository/CalendarEventRepositoryImpl.kt` | Memory (`ConcurrentHashMap<Int, CalendarYear>`) → Room → `NetworkClient.safeCall`; writes Room after network success + deletes old years (AC22); maps `AppError` → `CalendarLoadError` per the Error → UI table | not created |
| DI | `di/CalendarModule.kt` | One repository instance for the whole app (kept in `InnerLampApplication` as the `AdministrativeUnitModule` note says); factories for `FestivalsViewModel`, `FestivalDetailViewModel`, `HomeViewModel`, `ChuaViewModel` | not created |
| Tests | `app/src/test/java/com/example/innerlamp/…`, `app/src/test/resources/product/calendar/rules/`, `scripts/sync-product-vectors.sh` | Vectors (countdown, ordering, home-block, public holiday), mapper, repository, use case, ViewModel | partial (`FestivalsViewModelTest`, `GetBuddhistEventMonthsUseCaseTest`, `HomeViewModelTest` — they test the local catalogue and will be rewritten/deleted) |

> ⚠️ Not confirmed: directory name — default chosen: **keep** `ui/screens/festivals/` for the list and
> the detail (put `FestivalDetail*` next to `Festivals*`), while domain/data use the `Calendar*` names,
> because `ui/screens/calendar/` is already the Lunar calendar screen; renaming the package is Deferred.
> Merging the PR = approval.

**"Ngày lễ & ngày chay sắp tới" block in the Chùa tab** (`ChuaScreen.FastDaysSection`, prototype
`chuaVals`) also feeds on the local catalogue through `GetUpcomingFastDaysUseCase`. The product spec does
not mention this block.

> ⚠️ Not confirmed (product): whether the "Ngày lễ & ngày chay sắp tới" block in the Chùa tab belongs to
> the calendar feature, and what it shows (which group, how many rows, the "Gợi ý" line) — Android
> default: keep the current display and only switch the source to `CalendarEventRepository` (the first 4
> occurrences of the Buddhist group from today, BR-CAL-12 order, Rằm/Mùng Một recognised by
> `Recurrence.LunarMonthly`) so the local catalogue can be deleted; the "Nhắc tôi" button is unchanged
> (out of scope). Hand back to ba-writer in innerlamp-be.

**Cross-reference to the Deity spec (`docs/Specs/Deity`, not edited here).** The catalogue now has 60
events: it adds 6 Buddha/Bodhisattva via events — Tỳ Lô Giá Na (15/4), Hư Không Tạng (15/1), Kim Cương
Thủ (8/4), Lục Độ Mẫu (8/1), Bạch Độ Mẫu (15/10), Kim Cương Tát Đỏa (15/7). The via dates of
Buddhas/Bodhisattvas come **only** from this calendar (BR-DTY-05); any hard-coded via-date list in the
app is to be removed when the Deity feature reads this calendar. This is recorded as a task in
`tasks.md`; the Deity spec owner applies it.

> ⚠️ Not confirmed: the six new via dates come from `phat_botat.json` and have not been checked against
> the GHPGVN calendar (backend status) — the app only displays what the API returns.

## Navigation flow

```
HomeRoute
  ├─ "Sự kiện Phật giáo" block
  │    (tap a row) ──▶ FestivalDetailRoute(eventCode, solarDate)
  │    ("Tất cả" / "+N sự kiện khác") ──▶ FestivalsRoute (scroll to the current month)
  │    ("Thử lại") ──▶ reload the missing year, stay on Home
ChuaRoute ("Lịch lễ", holiday/fast-day row) ──▶ FestivalsRoute
BuddhaRoute ("Lịch lễ") ──▶ FestivalsRoute
FestivalsRoute
  (open) ──▶ on-device copy if any ──▶ LoadCalendarYear(current VN year) ──▶ online | on-device copy + message | error + Thử lại
  (choose chip) ──▶ filter in place, scroll to the current month
  (reach the end of the list, year < 2100) ──▶ LoadCalendarYear(year + 1) ──▶ append | "Thử lại" row
  (tap a row) ──▶ FestivalDetailRoute(eventCode, solarDate)
  (back) ──▶ previous screen
FestivalDetailRoute
  (open) ──▶ GetEventOccurrence ──▶ detail | not found (Quay lại) | load error (Thử lại)
  (back / Quay lại) ──▶ previous screen
```

Removed: `FestivalsCallbacks.onTemple` (temple chip → `TempleRoute`). The detail route carries `solarDate`
as an ISO string so `toRoute()` needs no type map for `LocalDate`.

## Step-by-step Flow — Load one year (festival calendar, Home, detail)

```
Step 1 — Show what exists (this feature)
- Trigger: FestivalsViewModel init / reaching the end of the list / "Thử lại"; HomeViewModel init; FestivalDetailViewModel init
- Pre-condition: year in [current year by Asia/Ho_Chi_Minh, 2100] (AC2, AC13)
- Where: ViewModel → LoadCalendarYearUseCase → CalendarEventRepositoryImpl
    - In memory (already loaded successfully this session) → return it and stop (no network call again,
      so a loaded year is never refetched in the session unless the user taps retry — this keeps the app
      under the IP rate limit, AC31)
    - In Room (CalendarYearCacheDao.get) → emit CalendarYear(source = DeviceCopy) so the UI shows it at
      once, without the out-of-date message yet (AC23); Step 2
    - None → UI in loading state; Step 2

Step 2 — Call the backend (this feature)
- Where: CalendarEventRepositoryImpl → NetworkClient.safeCall { CalendarApiService.getEvents(year) }
- API: GET /v1/calendar/events?year=YYYY (calendarEvents; public: no Bearer, no X-API-Key; always an
  explicit year)
    - Success → Step 3
    - Failure 429 RATE_LIMITED (IP rate limit, dev 60/min; `Retry-After` header and
      `details.retry_after_seconds`), with a Room copy → keep that copy with the "may be out of date"
      flag (AC24); no automatic retry loop
    - Failure 429 RATE_LIMITED, no copy → CalendarLoadError.RateLimited(retryAfterSeconds) → retry-later
      message mentioning the wait + "Thử lại" (AC31); never retried automatically
    - Failure (any other: NoConnection / Timeout / HttpError 5xx / 401 — now unexpected, since the route
      is public — / ApiError / Serialization / Unknown), with a Room copy → keep that copy, "may be out
      of date" flag (AC24, AC28)
    - Failure (NoConnection / Timeout), no copy → CalendarLoadError.Offline → offline message +
      "Thử lại" (AC25)
    - Failure (other), no copy → CalendarLoadError.Failed → generic error message + "Thử lại"
      (AC25, AC28)
    - Next year (festival calendar) fails, no copy → keep the list, "Thử lại" row at the end (AC12)

Step 3 — Write the on-device copy (this feature)
- Where: CalendarEventRepositoryImpl → CalendarYearCacheDao.upsert(year, payload, now) then
  deleteBefore(current year) in one transaction; store in memory
    - Success → return CalendarYear(source = Network)
    - Failure (Room error) → still return the data just loaded; log, do not tell the user

Step 4 — Display (this feature)
- Where: ViewModel
    - Festival calendar: append the year, apply CalendarFilter, count per chip, CalendarCountdown(today,
      date) for each row; public-holiday flag shown only on the occurrence's own day (AC32)
    - Home: GetHomeEventBlockUseCase cuts six months from the current Vietnam-time month,
      HomeEventBlock.select per month (needs one or two years — the next year loads in parallel; when one
      of the two years fails, only that year's months show the failure, AC27)
    - Detail: GetEventOccurrenceUseCase finds (eventCode, solarDate) → found | not found (AC18)
```

Merging years: the server already sorts each year per BR-CAL-12 and the years are appended in sequence,
so the order holds; `CalendarOrdering` is used for the Home tier choice and to run `rules/ordering.json`.

## Local storage

| Data | Where stored | Why |
|---|---|---|
| Copy of each loaded year (the `data` payload of `calendarEvents`, as JSON) | Room, separate DB `innerlamp_calendar.db`, table `calendar_year_cache` | Structured data → Room (`AGENTS.md`); keeping the JSON payload, one row per year, means contract fields can be added without a migration; a separate DB makes `fallbackToDestructiveMigration` safe (it is only a cache) and leaves `innerlamp.db` untouched; the content is public so it need not be excluded from backup (§3.5) |
| Year shown / loaded in the session | Repository memory (one instance for the whole app) | Festival calendar, Home and detail share it; no network call again in the session |
| Event images | Coil cache | No images yet (seed `null`) |

Nothing in `core/storage` prefs or secure storage. No OkHttp HTTP cache.

> ⚠️ Not confirmed: how the on-device copy is stored — default chosen: separate Room DB
> `innerlamp_calendar.db`, one JSON row per year, keep the current year onward (delete old years), no
> limit on the number of years (up to 2100, a few tens of KB each); the OkHttp HTTP cache stays off even
> though the contract now returns `Cache-Control: public, max-age=86400` for a request with `year`
> (and `no-cache` without `year`): the app always sends an explicit year and uses its own Room cache.
> Merging the PR = approval.

## Error → UI mapping

Texts in the "Default text" column are the product §3.1.1 messages table (decision 2026-10-15, used by
every platform) unless marked ⚠️.

| Condition (`AppError` / HTTP) | `CalendarLoadError` | With an on-device copy | Without a copy |
|---|---|---|---|
| `NoConnection`, `Timeout` | `Offline` | keep the copy + "Dữ liệu có thể chưa được cập nhật." (AC24) | "Không có kết nối mạng. Vui lòng kiểm tra và thử lại." + "Thử lại" (AC25) |
| 429 `RATE_LIMITED` (`details.retry_after_seconds`, `Retry-After`) | `RateLimited(retryAfterSeconds: Int?)` | keep the copy + "Dữ liệu có thể chưa được cập nhật." (AC24) | ⚠️ retry-later message that mentions waiting (using `retry_after_seconds` when present) + "Thử lại"; no auto-retry (AC31) |
| 5xx, 401 (unexpected: the route is public), other `HttpError`, `ApiError`, `Serialization`, `Unknown` | `Failed` | keep the copy + "Dữ liệu có thể chưa được cập nhật." (AC24, AC28) | "Không tải được lịch lễ." + "Thử lại" (AC25, AC28) |
| Next year fails (festival calendar) | any | keep the list | "Không tải được năm {year}." + "Thử lại" row (AC12) |
| One of the two Home years fails | any | the other year's months still show | only that year's months show the failure + "Thử lại" (AC27) |
| Home month with no Buddhist event | — | — | "Tháng này không có ngày lễ Phật giáo." (empty card, not an error) |
| `(eventCode, solarDate)` not in the year | — | — | "Không tìm thấy ngày lễ này." + "Quay lại" (AC18) |

> ⚠️ Not confirmed (product): the text of the rate-limit (429) message is not in the product §3.1.1
> messages table. Android default: a short Vietnamese retry-later sentence that says to wait before
> trying again (for example mentioning the seconds from `retry_after_seconds` when present). Hand back to
> ba-writer in innerlamp-be.

## Technical notes

- **Current year vs today**: the year to load the festival calendar for follows `Asia/Ho_Chi_Minh`
  (FR-CAL-002), and so does the "current month" of the Home block (BR-CAL-15); the countdown follows
  the device date (BR-CAL-13). The two differ only around the solar New Year when the device is in
  another time zone.
- **Public route (contract 2.2.0)**: `calendarEvents` is public. The app sends no `Authorization` (it is
  ignored if sent) and must not send `X-API-Key` (website-only; a wrong key returns 401
  `INVALID_API_KEY`). A 401 is therefore an unexpected error, handled like a generic failure (AC28); it
  no longer means "old backend". Phase 3 is unblocked; the fake repository (a sample JSON conforming to
  the contract, Phase 2) is kept for tests and debugging.
- **IP rate limit**: dev 60/min per IP. The app caches per year (memory + Room) and does not refetch a
  loaded year within the session unless the user taps retry. On 429 it shows a retry-later message (AC31)
  and does not retry in a loop.
- **No calendar rule on the client for this feature**: no `LunarCalendar.toLunar` for events, no
  day-30 → 29, no leap months, no local public-holiday or substitute-day logic — the lunar date is read
  straight from `lunar_date`, and "nhuận" is shown on the lunar date, not in the event name.
- **Group labels**: names come from `data.groups` (no hard-coded names); only the code `buddhist` is a
  constant (⚠️ AC19). The Phật giáo group is permanent (BR-CAL-09).
- **`recurrence` open-ended**: unknown values (the backend adds a new kind) decode to
  `Recurrence.Unknown`; the mapper keeps the event and treats it as a named event (tier 2 on Home).
  Mùng 1 / Rằm are identified by `recurrence == lunar_monthly` only.
- **Countdown string**: "Còn n ngày" per BR-CAL-13 (the code currently writes a lowercase "còn").
- **Language**: the app is Vietnamese only; no language parameter is sent (the server uses
  `calendar.default_language`).

## Deliberately stubbed for now

- `GetBuddhistEventMonthsUseCase` / `GetFestivalCalendarUseCase` remain the source of the festival
  calendar, Home and the Chùa tab until Phase 3 — this is drift from BR-CAL-01…07, BR-CAL-05 (product
  §4.2), not the target.
- Host temples on the festival calendar are sample data (`hostsFor`) — removed in Phase 2 (AC9).
- A fake `CalendarEventRepository` (a sample JSON conforming to the contract, including
  `is_major`/`sort_order`/`recurrence`) for Phase 2 and for tests/debugging; the real impl (Phase 3) is
  now unblocked because the contract is final.

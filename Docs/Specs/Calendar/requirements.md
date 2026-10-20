# Requirements — Calendar

Version: 1.3
Last updated: 2026-10-20
Status: in-progress — the "Lịch lễ Phật giáo" screen (`ui/screens/festivals/`) and the "Sự kiện Phật giáo" block on Home are running, but they compute dates from a catalogue that lives in the app (drift from BR-CAL-01…07, BR-CAL-05); no backend call yet, no detail screen, no offline copy, no next-year loading, no filtering by group / major event from data. The backend contract is now final (2.2.0), so the work is no longer blocked.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/festivals/` (old name `festivals`, product §1.5; `ui/screens/calendar/` is the lunar calendar screen and is not part of this spec)
Product spec: innerlamp-be `docs/product/calendar/product.md` @ v1.2
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ info.version 2.2.0 (commit 3af0844 on branch `feat/calendar-v3-public-major-via`, merged to main as 80de1da (innerlamp-be PR #55); `calendarEvents` is final: public, with `is_major` / `sort_order` / `recurrence`). Backend feature spec: innerlamp-be `docs/specs/features/calendar/` @ v3.2 (same commit).
Implements: BR-CAL-01, BR-CAL-02, BR-CAL-03, BR-CAL-04, BR-CAL-05, BR-CAL-06, BR-CAL-07 (Android: drop local computation, show the dates the server returns), BR-CAL-08, BR-CAL-09, BR-CAL-10, BR-CAL-11, BR-CAL-12, BR-CAL-13, BR-CAL-14, BR-CAL-15, FR-CAL-001, FR-CAL-002, FR-CAL-003, FR-CAL-004, FR-CAL-005, FR-CAL-006, FR-CAL-007, FR-CAL-008, NFR-CAL-03

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) of **Buddhist events** for Android, originally written retroactively from the code
(read on 2026-10-15, `main` @ bebaa9c) and re-synced on 2026-10-17 with product spec v1.2 and contract
2.2.0. Product behaviour lives in the product spec and is **not** repeated here; this file only says how
the Android app implements the `BR-CAL-*` / `FR-CAL-*`.

The Android column is `n/a` in product §4.2 (not part of this spec) for: BR-CAL-16 (a turned-off event is
simply not returned by the server), FR-CAL-009 (website, §3.6), FR-CAL-010, NFR-CAL-01, NFR-CAL-02.
Since product v1.2, BR-CAL-11 and BR-CAL-14 are `planned` for Android (they were `n/a` in v1.1) and this
spec implements both (BR-CAL-11 in AC32, BR-CAL-14 in AC15 and AC16).

`Docs/home_assumptions.md` item #11 is superseded by the product spec (BR-CAL-04, BR-CAL-05) — see "Open
questions (resolved)". This spec does not delete that file.

## Constraints from the product spec and the contract

- **The server is the only source of events and dates** (§2.5, BR-CAL-01…07, BR-CAL-05): the app drops
  the local festival catalogue, stops computing festival dates and stops adding Rằm / Mùng Một itself;
  every occurrence, solar date, lunar date, group and major flag comes from `GET /v1/calendar/events`.
  `LunarCalendar` stays because the memorial book (BR-MEM-10) and the lunar calendar screen use it.
- **Public route** (FR-CAL-001, contract 2.2.0): no session needed, no `Authorization` header (the server
  ignores it if sent), and the mobile app **must not** send `X-API-Key` — that header is for the website
  only and a wrong key is rejected with `401 INVALID_API_KEY`. Requests without the key are served and
  are counted by the per-IP rate limit (below).
- **Per-IP rate limit** (contract 2.2.0, `calendar.events_rate_limit`, dev: 60 requests/minute): over the
  limit → `429 RATE_LIMITED` with `details.retry_after_seconds` and a `Retry-After` header. The app
  therefore loads each year once, caches it (memory + Room) and never refetches a year it has already
  loaded in the session unless the user taps "Thử lại" (AC31).
- **One year per load, from the current year to 2100** (FR-CAL-002, FR-CAL-004). The server serves any
  year in 1900..2100; the app only asks for the current year onward.
- **Detail uses the loaded year**, there is no separate detail operation (Appendix A "Separate detail
  capability? No").
- **Device copy** (FR-CAL-007, §3.5 "Device copy"): public content, no personal data, replaced on the next
  successful load. The app keeps its own Room copy and does **not** use an OkHttp HTTP cache.
- **Cache-Control** (contract 2.2.0): with `year` the answer is `public, max-age=86400` (dev; it was
  `private` before 2.2.0); without `year` it is `no-cache`. Android always sends an explicit `year`
  (AC2) and relies on its own Room cache, so it does not depend on this header.
- **Fields the app reads** (contract 2.2.0, all required on each `events[]` item): `is_major` (the 10
  "Lễ lớn" of BR-CAL-10), `sort_order` (integer >= 1, display order within a day; Mùng 1 and Rằm always
  have the largest value), `recurrence` (`solar`, `lunar`, `lunar_monthly`, `lunar_last_day`,
  `solar_term`; decoded open-ended, an unknown value maps to `Unknown`). Mùng 1 and Rằm are recognised
  by `recurrence == lunar_monthly`, never by comparing `code` and never by computing them locally.
- **Backend content still to be confirmed by editors:** the seed catalogue now has 60 events (it was 51
  or 54 in earlier documents). The six new events are Buddha / Bodhisattva via-day events (Tỳ Lô Giá Na
  15/4, Hư Không Tạng 15/1, Kim Cương Thủ 8/4, Lục Độ Mẫu 8/1, Bạch Độ Mẫu 15/10, Kim Cương Tát Đỏa
  15/7); their dates come from `phat_botat.json` and have **not** been checked against GHPGVN
  (⚠️ Not confirmed, content owner). The Android app shows whatever the server returns.
- **Via dates come only from this calendar** (contract 2.2.0, BR-DTY-05): any hard-coded list of via
  dates of Buddhas / Bodhisattvas in the app must be removed (see `tasks.md` Phase 1 and the note under
  "Out of scope" about `docs/Specs/Deity`, which this spec does not edit).

## User Story

As a Buddhist using InnerLamp on Android (guest or signed in), I want to see the Buddhist festivals,
Vietnamese public holidays and folk festivals of each year with their solar date, lunar date and how many
days are left, so that I know on which day Phật đản, Vu Lan, Rằm… fall this year without computing the
lunar calendar myself (product §1.3, §2.2).

## Acceptance Criteria (EARS notation)

### Access and data source

1. WHEN a guest or a signed-in user opens the festival calendar, a detail or Home, THE SYSTEM SHALL load
   data with `GET /v1/calendar/events?year=<year>` **without** an `Authorization` header and **without**
   `X-API-Key`, and SHALL never show a sign-in prompt in this feature. (FR-CAL-001)
2. WHEN the app needs the events of a year, THE SYSTEM SHALL always send an explicit `year` (never
   empty), with the current year computed in `Asia/Ho_Chi_Minh`. (FR-CAL-002, platform)
   > ⚠️ Not confirmed: always send an explicit `year` instead of letting the server choose — default
   > chosen: **yes**, so that every screen uses the same year and the answer is cacheable
   > (`Cache-Control: public, max-age=86400` with `year`; contract 2.2.0); merge of the PR = approval.
3. WHEN the app shows an occurrence, THE SYSTEM SHALL take the solar date, lunar date (including the leap
   month flag), name, groups, major flag and display order exactly as the server returns them
   (`is_major`, `sort_order`, `recurrence` included), and SHALL NOT compute, add or drop any occurrence —
   Rằm and Mùng Một included. (BR-CAL-01, BR-CAL-02, BR-CAL-03, BR-CAL-04, BR-CAL-05, BR-CAL-06,
   BR-CAL-07)
4. WHEN showing the lunar date of an occurrence, THE SYSTEM SHALL show the lunar day/month and the mark
   "(nhuận)" when `lunar_date.is_leap_month` is `true`, together with the solar date, per BR-CAL-08. The
   leap mark belongs to the lunar date and is never appended to the event name. (BR-CAL-08)
5. WHEN merging several years or filtering, THE SYSTEM SHALL keep the BR-CAL-12 order (solar date,
   `sort_order`, `code`) using one shared comparator in `domain/calendar/`. (BR-CAL-12)

### Festival calendar (list)

6. WHEN the user opens the festival calendar (from "Tất cả" of the Home block, "Lịch lễ" on the temple
   tab, or "Lịch lễ" on the Buddha / Bodhisattva detail), THE SYSTEM SHALL open `FestivalsRoute`, load the
   current year and show the occurrences by solar month 1 → 12, scrolled to the current month.
   (FR-CAL-002)
7. WHEN showing one row of the festival calendar, THE SYSTEM SHALL show the solar day and weekday, the
   event name, the lunar date (AC4), the countdown label per BR-CAL-13 (AC21), the major mark when
   `is_major` (BR-CAL-10) and one label for each group of the event, using the names and order in
   `data.groups` (BR-CAL-09). (FR-CAL-002, BR-CAL-09, BR-CAL-10, BR-CAL-13)
8. WHEN the user taps a row, THE SYSTEM SHALL open the detail of exactly that occurrence (AC14).
   (FR-CAL-005)
9. WHEN the festival calendar is shown, THE SYSTEM SHALL **not** show "hosting temples" (temple count,
   temple chips) nor the intro sentence that mentions temples — there is no data source yet (product
   §1.2). (FR-CAL-002)

### Filtering

10. WHEN the festival calendar is shown, THE SYSTEM SHALL offer the filter chips "Tất cả", "Lễ lớn", then
    one chip per group in `data.groups`, in that order, each with the count of matching occurrences in
    the loaded years; there is **no** "Rằm & Mùng Một" chip any more. The Phật giáo group is permanent
    (BR-CAL-09), the other groups are data and may be added by the back office, so group names and
    codes other than `buddhist` are never hard-coded. (FR-CAL-003, BR-CAL-09, BR-CAL-10)
11. WHEN the user selects a chip, THE SYSTEM SHALL show only the matching occurrences (major = `is_major`;
    group = events that carry that group code), hide months that end up with no row, and keep the chip
    when more years are loaded (AC12). (FR-CAL-003)

### Loading the next year

12. WHEN the user scrolls to the end of the last loaded year and that year is below 2100, THE SYSTEM SHALL
    load the next year and append it to the end of the list, then:
    - on success: append the 12 months of the new year, applying the selected chip;
    - on failure with a device copy of that year: append the device copy with the "may be out of date"
      line (AC24);
    - on failure without a device copy: keep the list as it is and show a "Thử lại" row at the end;
      tapping it reloads that year. (FR-CAL-004, FR-CAL-007)
13. WHEN the last loaded year is 2100, THE SYSTEM SHALL not load more and SHALL not show a load-more row;
    THE SYSTEM SHALL never offer a year before the current year. (FR-CAL-004)

### Festival detail

14. WHEN the user opens an occurrence (from the festival calendar or the Home block), THE SYSTEM SHALL
    open `FestivalDetailRoute(eventCode, solarDate)` and take the event from the year of `solarDate`
    (memory, then device copy, then load that year if absent), then show the image, group labels and
    major mark, name, lunar date, a solar date card with weekday and countdown, the description and "Phật
    tử thường làm gì". (FR-CAL-005, BR-CAL-08, BR-CAL-10, BR-CAL-13)
15. WHEN showing the long description and the guidance, THE SYSTEM SHALL split them per BR-CAL-14, on line
    breaks (`\n`) only and **never** on commas or semicolons: each line of `long_description` is one
    paragraph; each line of `guidance` is one action, shown as a numbered list (a leading "-", "•" or
    "*" is dropped). (BR-CAL-14)
16. WHEN a section's content is empty, THE SYSTEM SHALL hide the section; an empty `long_description` is
    hidden and SHALL NOT be replaced by the short description. WHEN `image_url` is `null` or the image
    fails to load, THE SYSTEM SHALL show the shared default image. (BR-CAL-14)
    > ⚠️ Not confirmed: which image is the shared default on Android — default chosen: reuse the existing
    > Home gradient placeholder (`PlaceholderImage`, `home_assumptions.md` #1) until a shared default
    > image exists (product §2.7); merge of the PR = approval.
17. WHEN the detail is shown, THE SYSTEM SHALL have no "Kinh nên tụng" section, no "Chùa tổ chức" section
    and no "Nhắc tôi ngày này" button (product §1.2), and no previous/next event buttons (FR-CAL-009 is
    website only, §3.6); back returns to the previous screen. (FR-CAL-005)
18. WHEN the year of `solarDate` is available (loaded or device copy) but has no occurrence `eventCode` on
    that day, THE SYSTEM SHALL report that the event was not found and let the user go back.
    (FR-CAL-005)

### "Sự kiện Phật giáo" block on Home

19. WHEN Home is shown, THE SYSTEM SHALL build the block per BR-CAL-15 from the current month in Vietnam
    time (`Asia/Ho_Chi_Minh`) for six months — loading the current year and, when the six-month window
    crosses into the next year, the next year too — using the same repository as the festival calendar.
    For each month the pure function `domain/calendar/HomeEventBlock` picks the rows and "+N sự kiện
    khác": Buddhist-group occurrences only (group code `buddhist`), at most three, major events first,
    then other named events, then Rằm and Mùng Một (tier 3 recognised by `recurrence = lunar_monthly`),
    each tier in BR-CAL-12 order. A month with no row still has a card with the empty message. The block
    shows **no countdown** (name and lunar date only; the countdown is on the festival calendar and the
    detail). Choosing a row opens the detail (AC14); choosing "Tất cả" or "+N sự kiện khác" opens the
    festival calendar at the current month. (FR-CAL-006, BR-CAL-10, BR-CAL-12, BR-CAL-15)
    > Resolved 2026-10-17 (backend): the code `buddhist` is stable (product BR-CAL-09 v1.2: the Phật giáo
    > group is permanent). The openapi note about not hard-coding groups concerns display names and groups
    > the back office may add. Constant `BUDDHIST_GROUP = "buddhist"` in `domain/calendar/`.

### Countdown and date change

20. WHEN the app returns to the foreground (ON_RESUME) after the date has changed, THE SYSTEM SHALL
    recompute the countdown in the festival calendar and the detail (device date, BR-CAL-13) and rebuild
    the Home block for the new month (Vietnam time, BR-CAL-15). (platform)
21. WHEN showing a countdown on any screen, THE SYSTEM SHALL use one shared pure function
    `domain/calendar/CalendarCountdown` with "today" being the device date, producing exactly the
    labels of BR-CAL-13. (BR-CAL-13)

### Offline and load errors

22. WHEN a year is loaded successfully, THE SYSTEM SHALL write it to the device copy (Room), replacing the
    previous copy of that year, and delete the copies of years before the current year. (FR-CAL-007,
    platform)
23. WHEN opening the festival calendar or Home while the needed year already has a device copy, THE
    SYSTEM SHALL show that copy immediately (without the "may be out of date" line) while the network
    call runs, then replace it with the fresh result on success. (NFR-CAL-03, FR-CAL-007)
    > ⚠️ Not confirmed: show the device copy first, then refresh (instead of waiting for the network) —
    > default chosen: **yes**, to meet the 0.5 s of NFR-CAL-03; the "may be out of date" line only shows
    > when the network call fails (AC24); merge of the PR = approval.
24. WHEN loading a year fails for any reason (no network, timeout, 5xx, 429, an unexpected 401, parse
    error) and the device has a copy of that year, THE SYSTEM SHALL show that copy with the line "Dữ
    liệu có thể chưa được cập nhật." (Error → UI) — at the top of the festival calendar, on the Home
    block, or at the start of the year appended from the device copy (AC12). (FR-CAL-007)
25. WHEN loading the current year fails and the device has no copy of that year, THE SYSTEM SHALL show a
    load-error message with a "Thử lại" button — its own line for `AppError.NoConnection` / `Timeout`, a
    general line for other errors, and the rate-limit line of AC31 for 429; on the Home block this
    message replaces the month cards, the block does not disappear. (FR-CAL-008)
26. WHEN the user taps "Thử lại" and the load succeeds, THE SYSTEM SHALL show the current year as in AC6
    (festival calendar) or rebuild the block (Home). (FR-CAL-008)
27. WHEN the six-month window of the Home block needs two years and only one has data (network or device
    copy), THE SYSTEM SHALL show the cards of the months that have data and, on the cards of the months
    of the year without data, the load-error message with "Thử lại". (FR-CAL-006, FR-CAL-008,
    BR-CAL-15)
28. WHEN the app receives 401 from `GET /v1/calendar/events`, THE SYSTEM SHALL treat it as an unexpected
    failure (the route is public and the app sends neither `Authorization` nor `X-API-Key`, so a 401
    means something is wrong, for example a proxy or a wrong environment), handle it like any other
    generic load failure (AC24 / AC25), not clear the session and not prompt for sign-in. (FR-CAL-001,
    platform)

### Performance and testing

29. WHEN opening the festival calendar of the current year on a 4G network, THE SYSTEM SHALL show the list
    within 2 seconds; with a device copy, within 0.5 seconds (AC23). (NFR-CAL-03)
30. WHEN unit tests run, THE SYSTEM SHALL pass every case of innerlamp-be
    `docs/product/calendar/rules/countdown.json`, `rules/ordering.json` and `rules/home-block.json`;
    `rules/event-placement.json` is for the backend only (product §4.1). (BR-CAL-12, BR-CAL-13,
    BR-CAL-15)

### Rate limit and public holidays (added in v1.2)

31. WHEN `GET /v1/calendar/events` returns `429 RATE_LIMITED`, THE SYSTEM SHALL treat it as a distinct
    case (not as a generic HTTP error): show a retry-later message that tells the user to wait, using
    `details.retry_after_seconds` (or the `Retry-After` header) for the waiting time when present, and
    SHALL NOT retry automatically in a loop — only the user's "Thử lại" triggers another call. With a
    device copy the copy is shown as in AC24; without one, the message with "Thử lại" is shown as in
    AC25. (FR-CAL-008, platform)
    > ⚠️ Not confirmed (product): the §3.1.1 messages table has no line for a rate limit. Default chosen:
    > "Bạn thao tác quá nhanh. Vui lòng đợi {n} giây rồi thử lại." (EN gloss: "You are going too fast.
    > Please wait {n} seconds and try again."), and "Vui lòng đợi một lát rồi thử lại." when
    > `retry_after_seconds` is absent; hand back to ba-writer in innerlamp-be to fix the copy for all
    > platforms.
32. WHEN showing an occurrence whose `is_public_holiday` is `true`, THE SYSTEM SHALL mark it as a public
    holiday on that occurrence's own day only, and SHALL NOT show any substitute day off (nghỉ bù) —
    the app derives no holiday from other days and adds no occurrence for them. (BR-CAL-11)

### Current state of the code against each AC (read on 2026-10-15, unchanged in v1.2)

| AC | Current state | Note |
|---|---|---|
| 1 | drift | No API call; a guest can view it (FR-CAL-001 `done` only because the data is local) |
| 2, 3 | missing | `GetBuddhistEventMonthsUseCase` has 19 fixed via days + adds Rằm/Mùng Một on every lunar day 15/1 itself — **drift from BR-CAL-01…07, BR-CAL-05**; only Buddhist events (**drift from BR-CAL-09**) |
| 4 | drift | `lunarLabel` = "d/m âm lịch", **no "(nhuận)"** (Rằm/Mùng Một of a leap month still shown) |
| 5 | missing | Ordered by day, at most one event per day (a via festival beats Rằm/Mùng Một on the same day) |
| 6 | drift | `FestivalsRoute` exists, entry from Home/Temple/Buddha ✓, scroll to the current month ✓; but it preloads **3 years** (January this year → end of the third year), not per year, and months without events are dropped |
| 7 | drift | Has day + weekday, name, lunar date, countdown; **no group label**; the day is coloured by `isFestival` (every named festival), not by the major flag |
| 8 | missing | Rows are not tappable (only the temple chip is) |
| 9 | drift | Each row shows "N chùa tổ chức" + temple chips from sample data (`GetFestivalCalendarUseCase.hostsFor`) and an intro sentence about temples |
| 10, 11 | drift | Chips "Tất cả" / "Lễ lớn" / "Rằm & Mùng Một"; "Lễ lớn" actually filters `isFestival` (every named festival); `MAJOR_KEYS` (8 keys, which does not match the 10 of BR-CAL-10) is only used to pick sample temples — it is **deleted, not ported**; no group chips, no counts |
| 12, 13 | missing | No next-year loading; no 2100 limit |
| 14–18 | missing | No detail screen |
| 19 | drift | `EventsSection` has a month-card carousel, at most 3 rows, "+N sự kiện khác", "Tất cả" ✓; but **12 months of the solar year** (not 6 months from the current month), **no tiers**, a row opens the festival calendar (not the detail) |
| 22–28, 31 | missing | No network load, device copy, load error or retry |
| 20 | missing | "Today" is fixed when the ViewModel is built (`home_assumptions.md` #7) |
| 21 | mostly ✓ | `FestivalsUiState.countdown` follows BR-CAL-13; the "còn N ngày" text is lower case (product "Còn n ngày"); not checked against the vectors |
| 29 | not measured | |
| 30 | missing | `FestivalsViewModelTest`, `GetBuddhistEventMonthsUseCaseTest` test the local catalogue, vectors not run |
| 32 | missing | Nothing shows `is_public_holiday` yet; no code derives substitute days either (to be confirmed when the list row is built) |

## Data & API

The source of truth is `openapi.yaml` (operation `calendarEvents`, contract 2.2.0); the schema is not
copied here.

| Operation | Operation (openapi) | Auth | Local storage | Note |
|---|---|---|---|---|
| Load one year | `GET /v1/calendar/events?year=YYYY` (`calendarEvents`) | public — no Bearer, no `X-API-Key` | Room: the copy of that year (full replace) | Final since 2.2.0 (commit 3af0844, merged as 80de1da (innerlamp-be PR #55)). `year` always explicit (AC2). Per-IP limit → `429 RATE_LIMITED` (AC31) |
| Major flag, display order, rule kind | `events[].is_major`, `sort_order`, `recurrence` | — | inside the copy of the year | Required fields since 2.2.0. `recurrence` decoded open-ended (unknown → `Recurrence.Unknown`). Rằm / Mùng Một = `lunar_monthly` |
| Content lines | `events[].long_description`, `events[].guidance` | — | inside the copy of the year | One paragraph / action per `\n`-separated line (AC15) |
| Detail | — (uses the loaded year) | — | reads the copy of the year | No separate operation (product Appendix A) |
| Event image | `events[].image_url` (may be `null`) | — | Coil cache | Seed still has `null` → default image (AC16) |

## Error → UI mapping

The Vietnamese strings are the shared copy of product spec v1.2 §3.1.1 (messages table, approved
2026-10-15); only the lines marked ⚠️ are Android defaults.

> ⚠️ Not confirmed (product): the §3.1.1 table has no line for the rate limit (AC31) and no label for the
> back action of the not-found state ("Quay lại"); the Android defaults are in the "User sees" column;
> hand back to ba-writer in innerlamp-be.

| Operation | Failure case | User sees | Current code |
|---|---|---|---|
| `GET /v1/calendar/events` (current year) | Any failure, device copy exists | Device copy + "Dữ liệu có thể chưa được cập nhật." (AC24) | missing |
| `GET /v1/calendar/events` (current year) | `AppError.NoConnection` / `Timeout`, no copy | "Không có kết nối mạng. Vui lòng kiểm tra và thử lại." + "Thử lại" (AC25) | missing |
| `GET /v1/calendar/events` (current year) | `429 RATE_LIMITED`, no copy | ⚠️ "Bạn thao tác quá nhanh. Vui lòng đợi {n} giây rồi thử lại." ("Vui lòng đợi một lát rồi thử lại." when no `retry_after_seconds`) + "Thử lại"; no automatic retry (AC31) | missing |
| `GET /v1/calendar/events` (current year) | 5xx / other `HttpError` / `Unauthorized` (401, unexpected) / `ApiError` / `Serialization` / `Unknown`, no copy | "Không tải được lịch lễ." + "Thử lại" (AC25, AC28) | missing |
| `GET /v1/calendar/events` (next year) | Any failure, no copy | List unchanged, last row "Không tải được năm {year}." + "Thử lại" (AC12) | missing |
| `GET /v1/calendar/events` (Home block) | Same as the current-year rows | The matching line replaces the cards (or sits on the month cards missing data, AC27) + "Thử lại" | missing (a failure cannot happen today because the data is local) |
| (local) detail | No occurrence `eventCode` on `solarDate` in that year | "Không tìm thấy ngày lễ này." + ⚠️ "Quay lại" (AC18) | missing |
| (local) detail | Year of `solarDate` cannot be loaded and no copy | Same as the current-year load-error rows, "Thử lại" reloads that year | missing |
| (local) Home block | Month with no Buddhist event | Month card with "Tháng này không có ngày lễ Phật giáo." (AC19) | missing |
| (local) image | `image_url` `null` or load error | Default image (AC16) | missing |

`400 INVALID_YEAR` cannot happen (the app only sends the current year → 2100); if it does, handle it like
any other failure.

## Daily Buddha image on the lunar Calendar screen (added in v1.3)

Android-only addendum for `ui/screens/calendar/`. ⚠️ Not confirmed (product): `product.md` v1.2 has no
BR / FR ID for this image, so none is cited here; hand back to `ba-writer` in innerlamp-be. Contract:
`openapi.yaml` v2.2.0, operation `calendarImage` (`GET /v1/calendar/image?date=YYYY-MM-DD`, public, no
auth). The schema is not copied here.

- **AC-IMG-1** WHEN the Calendar screen opens, THE app SHALL request the image of today's date
  (device date) and show it in a card above the "Hôm nay" card. WHEN the user selects another day, THE
  app SHALL request the image of that day and cancel the earlier request.
- **AC-IMG-2** THE app SHALL always send an explicit `date` (`YYYY-MM-DD`); the response is cacheable
  (`Cache-Control: public, max-age=86400`) only then. A date already loaded in the session SHALL NOT be
  fetched again (the same date always gives the same image).
- **AC-IMG-3** WHILE the image is loading, THE card SHALL show a progress indicator.
- **AC-IMG-4** WHEN the image is shown, THE app SHALL show the author and the license name on screen
  (CC BY / CC BY-SA obligation); the license name SHALL be a link to `license_url` when present.
- **AC-IMG-5** IF the request fails with no connection / timeout, THEN THE card SHALL show "Không có
  kết nối mạng. Vui lòng kiểm tra và thử lại." and "Thử lại"; IF it fails otherwise (including
  `400 INVALID_DATE`, 5xx, parse error), THEN "Không tải được ảnh." and "Thử lại". No automatic retry.
- **AC-IMG-6** A date outside 1900..2100 SHALL fail locally without a call (the server returns 400).
- Result of a request for a day that is no longer selected SHALL be ignored.

⚠️ Not confirmed (backend): the JSON type of `id` (the DTO accepts string or number); behaviour when
`license` / `author` are empty for a public-domain image (the app hides the attribution line when both
are blank); `thumbnail_url` (not in 2.2.0, the full `image_url` is loaded); the Vietnamese strings
above are Android defaults (product §3.1.1 has none for the image).

## Out of scope

- The lunar calendar screen (`ui/screens/calendar/` — `CalScreen`, `LunarDetailScreen`), the Rằm · Mùng
  Một dot on Home's week strip, the daily Buddha image — same slug but product v1.x (§1.2). These still
  use the local `LunarCalendar`. Ask `ba-writer` whether BR-CAL-05 ("clients stop computing them")
  applies to them when v1.x is written.
- Temple events (`EventsRoute`, "Khóa tu & lễ sắp tới") — sangha feature.
- Reminding a festival ("Nhắc tôi ngày này", the "Nhắc tôi" button on the temple tab) — product §1.2, will
  use ADR-0001.
- Sutras to chant, hosting temples on the detail or the list (§1.2).
- Previous/next event on the detail (FR-CAL-009, website, §3.6).
- Substitute days off (nghỉ bù): the app never shows them (BR-CAL-11, AC32) — there is nothing to build
  beyond not deriving them; turned-off events (BR-CAL-16) — the server does not return them.
- Years before the current year (FR-CAL-004).
- Content in languages other than Vietnamese (§1.2).
- The hard-coded via-date list of Buddhas / Bodhisattvas (BR-DTY-05): the Deity spec
  (`docs/Specs/Deity`) is owned by another change and is **not** edited here. Cross-reference: once this
  calendar is the source of via dates (60-event catalogue, contract 2.2.0), the Deity feature must read
  via dates from `calendarEvents` and remove its own list; `tasks.md` Phase 1 records the dependency.

## Pending (not yet implemented)

- Drop the local catalogue and calculation; load through the API with a device copy (AC1–AC5, AC22–AC28,
  AC31) — `tasks.md` Phase 1, Phase 3. No longer blocked: the contract is final (2.2.0); the only
  external dependency is that the backend commit 3af0844 is not pushed yet, so it cannot be cited by
  others until merged.
- Festival calendar per year + load next years up to 2100, group labels, major mark, no hosting temples
  (AC6–AC13) — Phase 2.
- Detail screen (AC14–AC18) — Phase 2.
- Home block per BR-CAL-15, rows open the detail (AC19, AC27) — Phase 1, Phase 2.
- Recompute on date change (AC20) — Phase 2.
- Public-holiday rule (AC32) — Phase 2.
- The "Ngày lễ & ngày chay sắp tới" block on the temple tab changes source (⚠️ product, see design.md) —
  Phase 2.
- Vectors and performance measurement (AC29, AC30), tests for AC31 and AC32 — Phase 4.

## Open questions (resolved)

Per product spec v1.1 (2026-10-15) and v1.2 (2026-10-17):

- ~~`home_assumptions.md` #11: lunar day 30 of a short month counts as 29; a leap month does not repeat
  via festivals; Rằm/Mùng Một of a leap month are still fast days~~ → the server computes per BR-CAL-04
  (day 30 does not exist in a short month except for "last day of the month" events; festivals do not
  repeat in a leap month) and BR-CAL-05 (Rằm / Mùng Một exist in a leap month too, and only come from
  the catalogue). Android no longer has any rule of this item (AC3). Resolved 2026-10-15.
- ~~Fixed list of major festivals `MAJOR_KEYS` (8 keys)~~ → the `is_major` flag from data, 10 initial
  events (BR-CAL-10) → AC7, AC10, AC11; `MAJOR_KEYS` is deleted, not ported. Resolved 2026-10-15.
- ~~Show Buddhist events only~~ → the festival calendar shows every group with filter chips; the Home
  block only the Phật giáo group (BR-CAL-09, BR-CAL-15) → AC10, AC19. Resolved 2026-10-15.
- ~~Does the festival calendar need sign-in?~~ → public (FR-CAL-001) → AC1; route public in contract
  2.2.0 → AC28. Resolved 2026-10-15 / 2026-10-17.
- ~~Which years can be viewed~~ → current year onward, one year at a time, up to 2100 (FR-CAL-004) →
  AC12, AC13. Resolved 2026-10-15.
- ~~Does the detail need its own operation?~~ → no, it uses the loaded year (product Appendix A) → AC14.
  Resolved 2026-10-15.
- ~~Hosting temples, sutras, reminders~~ → outside v1.0 (§1.2) → AC9, AC17. Resolved 2026-10-15.
- ~~Copy for out-of-date / offline / failure / retry / empty month / not found~~ → product §3.1.1
  messages table (v1.2) → Error → UI mapping. Resolved 2026-10-17.
- ~~"Current month" of the Home block~~ → Vietnam time (BR-CAL-15) → AC19. Resolved 2026-10-17.
- ~~Does the Home block show a countdown?~~ → no (§3.1.1; FR-CAL-006 no longer cites BR-CAL-13) → AC19.
  Resolved 2026-10-17.
- ~~Empty long description~~ → hidden, never replaced by the short one (BR-CAL-14) → AC16. Resolved
  2026-10-17.
- ~~Home block when one of two years fails~~ → only that year's months show the failure with retry
  (BR-CAL-15) → AC27. Resolved 2026-10-17 (the ⚠️ of v1.0 AC27 is now answered by the product spec).

Android defaults chosen in this spec (merge of the PR = approval; the `⚠️` stays inline to show where they
apply): always send an explicit `year` (AC2); default image = existing placeholder (AC16); device copy first, then refresh (AC23); rate-limit message text (AC31); "Quay lại"
label (Error → UI); extra technical defaults in `design.md` (separate Room DB, folder name kept as
`festivals`, temple-tab block).

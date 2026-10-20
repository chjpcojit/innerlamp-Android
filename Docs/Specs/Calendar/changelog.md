# Changelog — Calendar Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.3 (2026-10-20)

- Added the daily Buddha image on the lunar Calendar screen from `GET /v1/calendar/image` (contract
  2.2.0, `calendarImage`): AC-IMG-1…6 in `requirements.md`, a design section and tasks. Android-only: no
  product ID yet (⚠️ Not confirmed, hand back to `ba-writer`). Unconfirmed: JSON type of `id`, empty
  author/license behaviour, `thumbnail_url`. Other sections unchanged.

## v1.2 (2026-10-17)

- Backend answers (2026-10-17): group code `buddhist` is stable (AC19 resolved). The IP rate limit
  (dev 60/min) is a dev config value, not final for production; only requests with a valid `X-API-Key`
  are exempt, mobile is not, so many devices behind one NAT may get 429 (AC31) - no exemption decided.
  Vector `product_spec_version`: countdown 1.0, ordering 1.1, home-block 1.1, event-placement 1.0
  (unchanged by v1.2). Real seed has Mùng 1 = 59, Rằm = 60; the rule (they sort after every named event) is
  what Android relies on, not the number. Pending on ba-writer: product.md still says 51 events and its
  changelog omits the 6 new via events; 429 message and "Quay lại" label.
- Re-sync to product spec v1.2 and contract `openapi.yaml` `info.version` 2.2.0 (innerlamp-be commit
  3af0844 on branch `feat/calendar-v3-public-major-via`, merged to main as 80de1da (innerlamp-be PR #55)). Docs only; the code
  was not re-read.
- `calendarEvents` is final: public, no longer "before v3.0" / "pending backend". The "blocked by
  backend" wording is removed; Phase 3 of `tasks.md` is unblocked (items still `[ ]` to implement). The
  fake repository task stays, for tests and debugging.
- Auth: the route is public; `Authorization` is ignored if sent. Mobile must NOT send `X-API-Key`
  (website only; a wrong key gives `401 INVALID_API_KEY`). A 401 is now an unexpected error handled like a
  generic failure; AC28 reworded (no longer "old backend").
- Rate limit (per IP, dev 60/min): `429 RATE_LIMITED` with `details.retry_after_seconds` and a
  `Retry-After` header is split out of the generic HTTP error. New AC31 and an Error → UI row: retry-later
  message that mentions waiting, no automatic retry loop, a loaded year is not refetched in the session
  unless the user taps retry. Domain: `CalendarLoadError.RateLimited(retryAfterSeconds: Int?)`. The
  message text is a ⚠️ product default.
- Cache: `Cache-Control: public, max-age=86400` with `year` (was private), `no-cache` without. Android
  still always sends an explicit `year`, keeps its own Room cache and uses no OkHttp HTTP cache.
- New required fields on `events[]`: `is_major` (the 10 "Lễ lớn" of BR-CAL-10; drives the chip and the
  Home block), `sort_order` (int >= 1, order within a day; Mùng 1 and Rằm always largest), `recurrence`
  (`solar` | `lunar` | `lunar_monthly` | `lunar_last_day` | `solar_term`). Mùng 1 / Rằm are recognised by
  `recurrence == lunar_monthly`, never by comparing `code` or computing locally. Decode is open-ended: an
  unknown `recurrence` maps to `Recurrence.Unknown`. `MAJOR_KEYS` (8 keys, mismatching the 10 of
  BR-CAL-10) is deleted, not ported.
- `guidance`: each line = one action (numbered list); `long_description`: each line = one paragraph.
  Split on newlines only, never on comma or semicolon (BR-CAL-14).
- Catalogue is now 60 events (was 51/54): +6 Buddha / Bodhisattva via events (Tỳ Lô Giá Na 15/4, Hư Không
  Tạng 15/1, Kim Cương Thủ 8/4, Lục Độ Mẫu 8/1, Bạch Độ Mẫu 15/10, Kim Cương Tát Đỏa 15/7); dates from
  `phat_botat.json`, not yet checked against GHPGVN (⚠️ Not confirmed). Via dates of Buddhas /
  Bodhisattvas come only from this calendar; any hard-coded via-date list in the app is to be removed
  (BR-DTY-05); cross-reference to `docs/Specs/Deity` (not edited here).
- Product v1.2 deltas applied: Android BR-CAL-11 and BR-CAL-14 are `planned` (was `n/a`), the "n/a"
  statements are removed, and new AC32 = BR-CAL-11 (a public holiday only on its own day, never show
  substitute days off, no nghỉ bù); BR-CAL-15 "current month" is Vietnam time (`Asia/Ho_Chi_Minh`) and
  when one of two years fails only its months show the failure; the Home block shows no countdown
  (FR-CAL-006 no longer cites BR-CAL-13); an empty long description is hidden, never replaced by the
  short one; "nhuận" goes on the lunar date, not on the event name; the Phật giáo group (BR-CAL-09) is
  permanent; Error → UI default strings aligned with the product §3.1.1 messages table (remaining gaps
  marked ⚠️).
- `tasks.md`: header, Phase 1 (`Recurrence.Unknown`, `RateLimited`, BR-CAL-11, via-date removal), Phase 2
  (rate-limited UI, Home no countdown), Phase 3 unblocked + 429 split + no `X-API-Key`, Phase 4 tests for
  429, 401, BR-CAL-11, unknown `recurrence`, newline-only split. `requirements.md` and `design.md` were
  updated by their own owners in the same re-sync.
- Language: per the repo language policy, the spec files edited in this change were rewritten in
  English; UI strings stay in Vietnamese with an English gloss. The older entries below were translated.
- Hand-offs:
  - Backend: the branch is not pushed, so commit 3af0844 cannot be cited by others yet; re-cite the
    merged commit in `requirements.md` and `tasks.md` after merge. Contract line says a request carrying
    a correct `X-API-Key` is NOT counted in the IP rate limit; mobile sends no key, so it is counted;
    confirm with the backend whether mobile needs a limit exemption or the 60/min dev value is enough
    given the per-year cache.
  - `ba-writer`: (1) the 429 retry-later message text is a ⚠️ default; (2) the product changelog v1.2 is
    dated 2026-10-15 and does not list the 60-event catalogue or the 6 new via events; (3) the new via
    dates are unverified against GHPGVN.
  - Deity spec owner: remove the hard-coded via-date list (BR-DTY-05).

## v1.1 (2026-10-15)

- Old-doc cleanup: removed row #11 from `Docs/home_assumptions.md` (missing day 30 of a month, leap
  months not repeating vía days — now BR-CAL-04 and BR-CAL-05 of the product spec); row #7 notes that the
  Buddhist Events block follows this spec. `tasks.md` Phase 5 ticks the matching item.
  `requirements.md` / `design.md` unchanged.

## v1.0 (initial) (2026-10-15)

- First spec, written retroactively from the running code (`main` @ bebaa9c, read on 2026-10-15) and
  checked against the innerlamp-be product spec `docs/product/calendar/product.md` v1.1. Already there:
  the "Lịch lễ Phật giáo" screen (`ui/screens/festivals/`, `FestivalsRoute`, entered from Home / Chùa
  tab / Buddha detail) and the "Sự kiện Phật giáo" block on Home, both on the local catalogue
  (`GetBuddhistEventMonthsUseCase`, `GetFestivalCalendarUseCase` with `MAJOR_KEYS` and sample hosting
  temples). Not there yet: API, device copy, detail, next-year loading, group chips, a major flag from
  data, tiering of the Home block, vectors.
- `requirements.md`: header with product v1.1 + contract `openapi.yaml` @ 0eb7d1f (the pre-v3.0
  `calendarEvents`) + backend `design.md` v3.0 for fields not yet in the contract; 30 ACs (access,
  festival calendar, filters, next year, detail, Home block, countdown, offline / errors, performance,
  vectors); current-state table; Data & API; Error → UI with default strings; Out of scope; Pending; Open
  questions (resolved).
- `design.md`: new domain (`CalendarEvent`, `CalendarCountdown`, `CalendarOrdering`, `HomeEventBlock`,
  `EventContentFormat`, repository + three use cases), the `FestivalDetailScreen` +
  `FestivalDetailRoute` screen, data (public `CalendarApiService`, DTO, mapper, Room
  `innerlamp_calendar.db`, repository impl, `di/CalendarModule.kt`), Step-by-step Flow "Load one year",
  removal of the local catalogue after Phase 3.
- `tasks.md`: Phase 1 Domain, 2 UI, 3 Data (waiting for backend v3.0), 4 Tests (`countdown`, `ordering`,
  `home-block` vectors via copies with `SOURCE`), 5 Documentation; `[x]` for entry points, access
  without login, scroll to the current month, Home carousel layout, the existing contract.
- Product spec / contract: product v1.1 (Android IDs: BR-CAL-01…10, 12, 13, 14, 15; FR-CAL-001…008;
  NFR-CAL-03). Contract @ 0eb7d1f; the fields `is_major`, `sort_order`, `recurrence` and the public
  route follow backend design v3.0 (not implemented yet).
- Android defaults chosen (merging the PR = approval; marked `⚠️` inline): always send an explicit
  `year` (AC2); default image = the existing placeholder (AC16); group code `buddhist` (AC19); show
  device copy first then refresh (AC23); a dedicated Room DB with one JSON row per year, keeping the
  current year onward, no HTTP cache; keep the `ui/screens/festivals/` folder, detail is `FestivalDetail*`.
- Hand-offs:
  - `ba-writer`: (1) §4.2 BR-CAL-14 Android column said `n/a` but FR-CAL-005 (Android `planned`) needs it
    — should be `planned`; (2) copy for device copy, load failure (offline / other), retry, not found,
    empty Home month; (3) Home block when only one of the two years loads (AC27); (4) the "Ngày lễ & ngày
    chay sắp tới" block on the Chùa tab (prototype `chuaVals`) is not in the product spec but uses the
    local catalogue; (5) FR-CAL-006 cites BR-CAL-13 but §3.1.1 does not say the Home block has a
    countdown; (6) whether BR-CAL-05 "clients stop computing" applies to the Rằm · Mùng Một dots on the
    Home week strip / Lịch âm screen (v1.x); (7) §4.2 note for Android: Android's "Lễ lớn" currently
    filters every named festival (`isFestival`), `MAJOR_KEYS` is only used to pick sample temples;
    Android already has the list screen (only the detail is missing); BR-CAL-08 on Android lacks
    "(nhuận)" → `drift`, not `done`; the Android festival calendar still shows sample hosting temples.
  - Backend (contract): update `openapi.yaml` for v3.0 (drop `bearerAuth`, add `is_major`, `sort_order`,
    `recurrence`, 429) — Phase 18; state that the group code `buddhist` is stable (AC19).
- Reason: product spec calendar v1.1 (2026-10-15) requires Android to drop the local catalogue and read
  from the backend; step 4 of the documentation workflow (`spec-writer`).

# Requirements — Temple

Version: 1.2
Last updated: 2026-10-15
Status: planned (drift) — the home "Chùa Việt Nam" block, the "Tất cả chùa" (all temples) screen (`templelist`) and the "Trang chùa" (temple page) screen (`temple`) have UI but run on sample data (8 temples, including a "Tây Nguyên" region), search/filter on the device and do not page; the temple page is the old layout (follow, offering, stats, schedule) and does not call the backend. Only the short list (`TempleOptionRepository`) is wired to the backend.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/temple/` (temple page) and `ui/screens/templelist/` (directory); the featured block lives in `ui/screens/home/` (code folder names unchanged, product §1.5)
Product spec: innerlamp-be `docs/product/temple/product.md` @ v1.2
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ 0eb7d1f (info.version 1.0.0) — `GET /v1/temples` does **not** yet have `q`/`region` and pages of 12, and `GET /v1/temples/{id}` does not yet have the four new field groups (product Appendix B; backend spec `temple` v2.0 Phase 11–13 not done)
Implements: BR-TPL-01, BR-TPL-02, BR-TPL-03, BR-TPL-04, BR-TPL-05, BR-TPL-06, BR-TPL-07, BR-TPL-08, BR-TPL-09, FR-TPL-001, FR-TPL-002, FR-TPL-003, FR-TPL-004, FR-TPL-005 (via `../Onboarding/`), FR-TPL-006, FR-TPL-007, FR-TPL-008, FR-TPL-009, NFR-TPL-01, NFR-TPL-02 — FR-TPL-010 is Backend (Android `n/a` in product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Android platform spec (layer 2) for the **temple directory**. Written in mixed mode: the current state
was read from the code on 2026-10-15 (branch `claude/project-thread-lr6x54` based on `main` @ bebaa9c),
the rest is prospective. Product behavior (order, page size, the three regions, search matching,
missing values, distance, opening hours) lives in the product spec and is **not** restated here; this
file only says how the Android app implements the `BR-TPL-*`/`FR-TPL-*` rules.

## Constraints from the product spec

- **Public, read-only** (§2.3, §2.7): no sign-in needed; no Bearer, no `X-API-Key` (same as the
  existing `TempleApiService`).
- **The server decides order, search and filtering** (BR-TPL-01, BR-TPL-05, §4.1): the app shows the
  order it receives, never re-sorts, never filters on the device. The `temple-order.json` and
  `temple-search-match.json` vectors run on the backend only.
- **The app computes** distance (BR-TPL-07) and open status (BR-TPL-09) itself — it runs the
  `temple-distance.json` and `temple-open-status.json` vectors.
- **Never load the whole directory at once** (NFR-TPL-02): every list follows the server's pages.
- **Sample data is dropped** (§2.1 "Android shows sample data"): every temple comes from the backend;
  the contract's `temple_<region>_<n>` ids replace the sample ids `tp-…`, `tu-quang`.
- **Dependencies:** the contract for search/filter/pages of 12 and the new fields (Appendix B items 1,
  2, 4) — see "Data & API"; the temple picker at sign-up belongs to `../Onboarding/` (FR-TPL-005,
  BR-ONB-10).

## User Story

As a user (signed in or not), I want to see the featured temples on the home screen, search and filter
the whole Vietnamese temple directory by name and region, open a temple's page to read its profile,
know whether it is open now and get directions to it (product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Featured temples on the home screen

1. WHEN the home screen opens, THE SYSTEM SHALL load `GET /v1/temples/top` (public) and show the temple
   cards in the "Chùa Việt Nam" (Vietnamese temples) block in the order received (no re-sorting, no
   truncation), with "Xem tất cả" (see all) opening `TempleListRoute`. (FR-TPL-001, BR-TPL-01,
   BR-TPL-02)
2. WHEN `top` returns an empty list, THE SYSTEM SHALL show the block's empty state and no error.
   (FR-TPL-001, BR-TPL-02)
3. WHEN `top` fails (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`), THE SYSTEM SHALL show
   the same empty state as AC2 — no error text, no "Thử lại" (retry) action and no sample temples — and
   SHALL NOT block or change the other home blocks. (FR-TPL-001)
4. WHEN a temple card is shown (home or directory), THE SYSTEM SHALL show the image (`thumbnail_url`;
   none → placeholder image), name, type, province, heritage rank and sect per §3.1.1 "Temple card".
   (FR-TPL-001, FR-TPL-002, BR-TPL-06)
   > ⚠️ Not confirmed (product): the Vietnamese display labels for the contract's `type`, `sect` and
   > `heritage_rank` codes (e.g. `DI_SAN_THE_GIOI`, `BẮC_TÔNG`) and how to show an unknown code (the
   > contract treats `type`/`sect` as open enums) — blocks the Phase 2 task "Display labels"; hand back
   > to ba-writer in innerlamp-be.

### "Tất cả chùa" directory

5. WHEN the user opens `TempleListRoute`, THE SYSTEM SHALL call `GET /v1/temples` page 1 (no `q`, no
   `region`), show the temples in the order received and the number of temples found taken from the
   response's `total`. (FR-TPL-002, BR-TPL-01, BR-TPL-03)
6. WHEN the user scrolls near the end of the list and `page < total_pages`, THE SYSTEM SHALL load the
   next page and append it to the list, one request at a time; THE SYSTEM SHALL stop when
   `page >= total_pages` or a page comes back empty, and read the page size from the response's
   `page_size`, never assuming a fixed number. (FR-TPL-002, BR-TPL-03, NFR-TPL-02, §3.6)
7. WHEN loading the first page fails, THE SYSTEM SHALL show an error state with "Thử lại" (retry), with
   different text when the device is offline (`AppError.NoConnection` / `AppError.Timeout`) and for
   other errors. (FR-TPL-002)
   > ⚠️ Not confirmed (product): the Vietnamese wording for the load errors (offline / other) on the
   > directory and temple page, the "temple not found" text of FR-TPL-006 and the hint of "Không tìm
   > thấy chùa" (no temple found, FR-TPL-003) — the product spec describes the intent, not the text —
   > blocks the Phase 2 task "Error/empty states"; hand back to ba-writer in innerlamp-be.
8. WHEN loading a later page fails, THE SYSTEM SHALL keep the temples already loaded and show
   "Thử lại" at the end of the list to reload that same page. (platform)
9. WHEN the user types in the search field, THE SYSTEM SHALL wait until the user stops typing
   (debounce), then trim leading/trailing whitespace, cut to 100 characters, call `GET /v1/temples`
   page 1 with `q` (and the selected `region`), replace the list with the result and show the number
   of matches; THE SYSTEM SHALL discard results of an older request that arrive late and SHALL NOT
   filter anything on the device. (FR-TPL-003, BR-TPL-05, NFR-TPL-01)
10. WHEN a search (with or without a region filter) matches no temple, THE SYSTEM SHALL show
    "Không tìm thấy chùa" (no temple found) with a hint to change the term or region (⚠️ hint text —
    AC7). (FR-TPL-003)
11. WHEN the user clears the search field ("Xóa" (clear) button or deleting all text), THE SYSTEM SHALL
    call page 1 again **without** the `q` parameter (keeping the selected region); a whitespace-only
    string counts as empty. (FR-TPL-003, BR-TPL-05)
12. WHEN the user selects a region chip, THE SYSTEM SHALL show exactly four fixed chips "Tất cả · Miền
    Bắc · Miền Trung · Miền Nam" (All · North · Central · South; not derived from the data), only one
    selected, and call page 1 with `region` `north` / `central` / `south` (omitting the parameter for
    "Tất cả") together with the current term. (FR-TPL-004, BR-TPL-04, BR-TPL-05)
13. WHEN the backend returns `400 INVALID_FILTER` (which AC9, AC11, AC12 should already prevent), THE
    SYSTEM SHALL show the generic error state with "Thử lại". (platform)
14. WHEN the app is killed by the system and reopened on the directory (process death), THE SYSTEM
    SHALL keep the current term and region and reload page 1 with them. (platform)
15. WHEN the user taps a temple card on the home screen or in the directory, THE SYSTEM SHALL open
    `TempleRoute(id)` with the contract's `id`; back returns to the same scroll position and loaded
    pages. (FR-TPL-006, platform)

### Short list (temple picker)

16. WHEN another feature needs to pick a temple, THE SYSTEM SHALL use `TempleOptionRepository`
    (`GET /v1/temples/options`, name search — §3.6) as `../Onboarding/requirements.md` criterion 17
    describes; this spec does not redefine the picker, it only keeps the shared repository.
    (FR-TPL-005, BR-TPL-01, BR-TPL-05)

### Temple page

17. WHEN `TempleRoute(id)` opens (from home, the directory, the Chùa tab / my temple, the lunar calendar,
    the festival calendar), THE SYSTEM SHALL show a loading state and call `GET /v1/temples/{id}`
    without sign-in, then:
    - on success: show the page per AC18–AC26;
    - on failure (`404 TEMPLE_NOT_FOUND`): show a "temple not found" state with a back button
      (⚠️ text — AC7);
    - on failure (other): show an error with "Thử lại", with different text when offline as in AC7.
      (FR-TPL-006, BR-TPL-06)
18. WHEN the temple page is shown, THE SYSTEM SHALL show the sections in the order of FR-TPL-007 (cover
    image + heritage rank, sect, name, "Còn gọi: …" (also called), place; open status + hours +
    distance; grid of founding year · heritage rank · sect · region; introduction; abbot · founder ·
    sangha body; architecture & heritage; amenities; visiting notes; events & courses; location &
    contacts; footer), each missing value handled per BR-TPL-06 ("Đang cập nhật" (being updated) /
    "Không có" (not available) / placeholder image / "Còn gọi" hidden when `aliases` is empty). An empty
    string `""` from the contract counts as missing. (FR-TPL-007, BR-TPL-06)
19. WHEN the introduction is longer than 5 lines, THE SYSTEM SHALL collapse it to 5 lines with "Đọc
    thêm" (read more); tapping expands it and switches to "Thu gọn" (collapse). (FR-TPL-007)
20. WHEN there are visiting notes, THE SYSTEM SHALL number them "01, 02, …" in the order received.
    (FR-TPL-007)
21. WHEN the amenities section is shown, THE SYSTEM SHALL show car parking, motorbike parking,
    vegetarian meals and retreats from the four `amenities` flags — a `false` flag shows "Không có" —
    and SHALL always show the "Kinh sách" (sutra books) item with "Đang cập nhật" (never hidden, never
    "Không có") until the contract carries it. (FR-TPL-007, BR-TPL-06)
22. WHEN the architecture & heritage section is shown, THE SYSTEM SHALL show `architecture_heritage`;
    the architecture style, materials, highlights and photo set are not in the contract yet, so their
    rows stay visible with "Đang cập nhật" — never hidden, never "Không có". (FR-TPL-007, BR-TPL-06)
23. WHEN location & contacts are shown, THE SYSTEM SHALL show `location.full_address`, region,
    coordinates, Hotline, Fanpage, Website, Email; a missing contact shows "Đang cập nhật" and cannot be
    tapped. (FR-TPL-007, FR-TPL-008, BR-TPL-06)
24. WHEN `content_updated_month` has a value, THE SYSTEM SHALL show the footer "Cập nhật MM/YYYY"
    (updated MM/YYYY); WHEN it is `null`, THE SYSTEM SHALL hide that line; the footer has **no** "Báo
    thông tin sai" (report wrong information). (FR-TPL-007, BR-TPL-08)
25. WHEN the temple page is shown, THE SYSTEM SHALL have only the "Chỉ đường" (directions) action in the
    action row — no "Theo dõi" (follow; hidden, not disabled), no "Cúng dường" (offering), no report
    link. (BR-TPL-08)
26. WHEN the "Sự kiện & khóa học" (events & courses) section is shown, THE SYSTEM SHALL always show the
    empty state "Chùa chưa đăng sự kiện nào." (the temple has not posted any event) with no follow hint
    and no "Xem tất cả"; the app does not call `GET /v1/temples/{id}/events` in this spec (the content
    belongs to `sangha`). (FR-TPL-009, BR-TPL-08)

### Opening hours

27. WHEN the temple page is shown and the temple has opening hours, THE SYSTEM SHALL compute the status
    per BR-TPL-09 with "now" taken from an injected `Clock` converted to Vietnam time
    (`Asia/Ho_Chi_Minh`), the lunar day of that Vietnam date via `LunarCalendar` (UTC+7, never the
    device time zone), and the Rằm & Mùng Một hours from `holiday_operating_hours`; show the status line,
    the two hour rows and the "Hôm nay" (today) label per BR-TPL-09; WHEN the temple has normal hours
    but no Rằm & Mùng Một hours, THE SYSTEM SHALL show the normal hours on the "Rằm & Mùng Một" row (not
    "Đang cập nhật") and use them on lunar days 1 and 15; WHEN the temple has no opening hours, THE
    SYSTEM SHALL hide the status and show the hours as "Đang cập nhật". (BR-TPL-09, BR-TPL-06)
28. WHILE the temple page is shown, THE SYSTEM SHALL recompute the open status when the screen returns
    to the foreground (`ON_RESUME`) and at every new minute. (platform)
29. WHEN `operating_hours` or `holiday_operating_hours` cannot be parsed as `HH:MM - HH:MM`, THE SYSTEM
    SHALL treat that slot as absent for the computation (hiding the status if it is the normal hours)
    and show the raw string on the hours row. (platform)
    > ⚠️ Not confirmed (contract): the `HH:MM - HH:MM` format is only a commitment of backend spec v2.0
    > AC46 (not implemented); `openapi.yaml` @ 0eb7d1f only has the example `"07:00 - 18:00"` for a
    > free-text string — blocks the Phase 3 task "Hours mapper"; hand-off to the backend.

### Distance

30. WHEN the temple page is shown and the app has location permission (precise or approximate), THE
    SYSTEM SHALL get the device's current location, compute the distance to the temple's coordinates
    and show the "Cách bạn …" (… from you) line per BR-TPL-07; WHEN there is no permission, the location
    cannot be obtained, or the wait times out, THE SYSTEM SHALL hide that line and not block the rest of
    the page. (BR-TPL-07)
31. WHEN the user opens a temple page for the first time and the app has never asked for location
    permission, THE SYSTEM SHALL request `ACCESS_COARSE_LOCATION` + `ACCESS_FINE_LOCATION` once through
    the system dialog; if denied, it does not ask again and hides the distance line. (BR-TPL-07,
    platform)
    > ⚠️ Not confirmed: when to ask for location permission on Android (suggested default: the first
    > time a temple page opens, once; alternative: a "Hiện khoảng cách" (show distance) button — UI
    > outside the prototype, so it needs product) — blocks the Phase 2 task "Location permission".
32. WHEN the app uses the user's location, THE SYSTEM SHALL use it only in memory to compute the
    distance — never store it, send it to the backend, or write it to logs or analytics. (BR-TPL-07,
    product §3.5, §3.7)

### Directions & contacts

33. WHEN the user taps "Chỉ đường" (action row or location section), THE SYSTEM SHALL open the default
    maps app with the temple's coordinates as destination (`geo:` intent); WHEN no app handles that
    intent, THE SYSTEM SHALL open Google Maps directions in the browser. (FR-TPL-008, §3.6)
34. WHEN the user taps a contact that has a value, THE SYSTEM SHALL open the dialer with the Hotline
    number (`ACTION_DIAL`, no `CALL_PHONE` permission), open the Fanpage/Website in the browser, or open
    the mail app with the Email address (`mailto:`); WHEN the contact has no value, THE SYSTEM SHALL do
    nothing; WHEN no app handles the intent, THE SYSTEM SHALL not crash. (FR-TPL-008, BR-TPL-06)

### Vectors

35. WHEN unit tests run, THE SYSTEM SHALL pass every case of `rules/temple-open-status.json`
    (BR-TPL-09) and `rules/temple-distance.json` (BR-TPL-07); `temple-order.json` and
    `temple-search-match.json` are backend-only (product §4.1). (BR-TPL-07, BR-TPL-09)
36. WHEN measured on a 4G network, THE SYSTEM SHALL show page 1 of a search or region change within 2
    seconds of sending the request. (NFR-TPL-01)

### Current code vs each AC (read on 2026-10-15)

| AC | State | Notes |
|---|---|---|
| 1, 2 | drift | `HomeSampleData.load()` (fake 900 ms delay) takes 8 sample temples from `GetTemplesUseCase`; has a skeleton and the empty card "Chưa có chùa nào" (no temples yet) + "Xem tất cả" |
| 3 | drift (data) | Error and empty already share one empty card (`Docs/home_assumptions.md` #13), which matches AC3, but it is on sample data and does not call the API; `HomeViewModel.onReload()` exists but AC3 needs no retry |
| 4 | drift | The home card shows region, name, place and `note` (a sample caption not in the contract); the directory card shows name and "place · region"; no image (gradient placeholder, #1) |
| 5, 6 | missing | `TempleListViewModel` takes all 8 sample temples at once, no paging — **drift BR-TPL-03 / NFR-TPL-02**; the subtitle "N chùa trong danh bạ" (N temples in the directory) is the sample total, not the number found |
| 7, 8, 13 | missing | No error state |
| 9–11 | drift | `TempleListUiState.filtered` filters **on the device**, matching name **and place** (diacritics folded via `foldVietnamese`) — **drift BR-TPL-05** (server, name + aliases, no address); empty text "Không tìm thấy chùa nào khớp “q” ở …" |
| 12 | drift | Region chips derived from the data, including "Tây Nguyên" — **drift BR-TPL-04** |
| 14 | missing | State not kept in `SavedStateHandle` |
| 15 | ✓ navigation part | `onTemple(id)` → `TempleRoute(id)`; the id is a sample id |
| 16 | ✓ repository | `TempleOptionRepositoryImpl` calls `GET /v1/temples/options` via `safeCall`, drops an empty `q`, cuts to 100 characters; the picker does not exist yet (Onboarding) |
| 17 | missing | `GetTempleDetailUseCase` returns one sample temple "Chùa Từ Quang" for every id; no loading, no error, no 404 |
| 18–24 | drift | Old layout: hero, 3-column stats (Buddhists / Schedules / Founding year), introduction, "Thời khóa sắp tới" (upcoming schedule, with sign-up), address — most FR-TPL-007 sections missing |
| 25 | drift | Has "Theo dõi" (local toggle) and "Cúng dường" — **drift BR-TPL-08** |
| 26 | drift | "Thời khóa sắp tới" shows sample data and sign-up labels (`sangha` content) instead of the empty state |
| 27–29 | missing | No open-status computation |
| 30–32 | missing | No location permission/location (`AndroidManifest.xml` only has `USE_BIOMETRIC`) |
| 33, 34 | missing | `onDirections = {}` (no-op) in `MainActivity.kt`; no contacts |
| 35 | missing | No vector tests |
| 36 | not measured | |

## Data & API

`openapi.yaml` is the source of truth; no schema is copied here. Every operation is public (no
`@Authenticated`).

| Action | Operation (openapi) | Auth | Local storage | Notes |
|---|---|---|---|---|
| Featured temples | `GET /v1/temples/top` (`templesTop`) | public | none | In the contract |
| Directory by page | `GET /v1/temples?page=` (`templesList`) | public | none | In the contract, but pages of 20 |
| Search + region filter | `GET /v1/temples?page=&q=&region=` | public | none | ⚠️ Not confirmed (contract): `q` (name + aliases), `region`, `400 INVALID_FILTER` (`details.field` `q`/`region`) and pages of 12 are not in `openapi.yaml` @ 0eb7d1f (product Appendix B items 1, 2; backend v2.0 AC 5, 39–44) — blocks the Phase 3 task "Search/filter"; hand-off to the backend |
| Temple page | `GET /v1/temples/{id}` (`templesGet`) | public | none | In the contract (existing part) |
| New temple-page fields | `GET /v1/temples/{id}` | public | none | ⚠️ Not confirmed (contract): `holiday_operating_hours`, `sangha_body`, `contacts{hotline, fanpage, website, email}`, `content_updated_month` and the `HH:MM - HH:MM` hours format are not in `openapi.yaml` @ 0eb7d1f (Appendix B item 4; backend v2.0 AC 45–46) — blocks the Phase 3 task "Detail DTO"; until then those parts show per BR-TPL-06 |
| Short list | `GET /v1/temples/options` (`templesOptions`) | public | none | Wired (`TempleOptionRepositoryImpl`); used by `../Onboarding/` |
| Temple events | `GET /v1/temples/{id}/events` | Bearer | — | **Not used** in this spec (FR-TPL-009: content from `sangha`) |
| Images | `thumbnail_url` / `image_url` | public | Coil image cache | Use the server URL as is |
| User location | — (device) | — | not stored | BR-TPL-07, product §3.5 |

## Error → UI mapping

Wording still waits on product (⚠️ AC7); the table only says what the user sees.

| Operation | Failure case | User sees | Current code |
|---|---|---|---|
| `GET /v1/temples/top` | Any error (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`) | The block's empty state, same as an empty list — no error text, no retry, no sample temples (AC3) | drift (shared empty card, but no API call) |
| `GET /v1/temples` page 1 | `NoConnection` / `Timeout` | Error state with the "offline" text + "Thử lại" (AC7) | missing |
| `GET /v1/temples` page 1 | 5xx / `Unknown` / `Serialization` / `400 INVALID_FILTER` / `400 INVALID_PAGE` | Error state with the generic text + "Thử lại" (AC7, AC13) | missing |
| `GET /v1/temples` page ≥ 2 | Any error | List kept, "Thử lại" at the end (AC8) | missing |
| `GET /v1/temples` | 200, `total = 0` | "Không tìm thấy chùa" + hint (AC10); with no term and no region → the directory's empty state | drift (filtered on device) |
| `GET /v1/temples/{id}` | `404 TEMPLE_NOT_FOUND` | "Temple not found" + back (AC17) | missing |
| `GET /v1/temples/{id}` | `NoConnection` / `Timeout` / other | Error + "Thử lại", text per AC7 (AC17) | missing |
| (device) | No permission / location unavailable | "Cách bạn …" line hidden (AC30) | missing |
| (device) | No maps app | Google Maps opens in the browser (AC33) | missing |
| (device) | No app handles the contact | Nothing happens, no crash (AC34) | missing |

## Out of scope

- Following a temple, offering, reporting wrong information, suggesting a temple
  (`temple-suggestion`), a temple map, "near me" search, sorting by distance, ratings (product §1.2).
- My temple / sangha, schedules, temple events, sign-up (`sangha`) — including the Chùa tab
  (`ui/screens/chua/`), the `EventsRoute` screen, and the "Sự kiện & khóa học" content once `sangha`
  provides it.
- The temple picker at sign-up (`../Onboarding/`); this spec only keeps the shared
  `TempleOptionRepository`.
- Offline reading (product §3.6 allows it, Android does **not** do it in v1 — see `design.md` "Local
  storage").
- The "Ảnh chùa đẹp" (beautiful temple photos) screen (`ui/screens/photos/`, `GetTemplePhotosUseCase`)
  — not part of the `temple` product spec.
- Buddhist festival calendar (`ui/screens/festivals/`), lunar calendar (`ui/screens/calendar/`),
  profile: only entry points to `TempleRoute`; switching their sample ids to real ids belongs to those
  features' specs.
- Order, featured temples, search, filtering — computed by the server (BR-TPL-01, BR-TPL-02,
  BR-TPL-05).

## Pending (not yet implemented)

- Domain: contract-shaped models, repository, use cases, the opening-hours and distance rules —
  `tasks.md` Phase 1.
- Featured block, paged directory with server search/filter, temple page per FR-TPL-007, removing
  follow / offering / schedule, directions, contacts, location permission (AC1–AC34) — Phase 2.
- Data layer for `top`, the list and the detail (AC1, AC5–AC13, AC17) — Phase 3; search/filter and the
  new fields are blocked by the contract.
- Vectors (AC35), NFR measurement (AC36) — Phase 4.
- Sample-data cleanup — Phase 5 (the temple lines of `Docs/home_assumptions.md` were removed in v1.1).

## Open questions (resolved)

Per product spec v1.2 (2026-10-15) — answers from anh Đức to the questions this spec raised:

- ~~What the home featured block shows when `top` fails (text, retry, offline vs other)~~ → the same
  empty state as an empty directory, no error text, no retry, no sample temples (FR-TPL-001, new
  scenario) → AC3, Error → UI row for `top`; `design.md` Home rows; `tasks.md` Phase 2 "Featured block"
  and Phase 4 `HomeViewModel` test. Resolved 2026-10-15.
- ~~The "Kinh sách" amenity without data: hidden ("when known") or "Đang cập nhật"~~ → always shown with
  "Đang cập nhật", never hidden, never "Không có" (BR-TPL-06, FR-TPL-007); the same for the other later
  fields (architecture style, materials, highlights, photo set) → AC21, AC22; `design.md` Format and
  Temple screen rows; `tasks.md` Phase 2 "Temple screen". Resolved 2026-10-15.
- ~~The "Rằm & Mùng Một" row when the temple has normal hours but no holiday hours~~ → it shows the
  normal hours, not "Đang cập nhật" (BR-TPL-09; already covered by the vector "Rằm without holiday hours
  uses normal hours") → AC27; `design.md` Step 2 of the temple-page flow; `tasks.md` Phase 2 "Hours
  card". Resolved 2026-10-15.

Per product spec v1.1 (2026-10-15) — these replace the Android code's current behavior:

- ~~The "Tây Nguyên" region in the sample data~~ → three regions only, Highlands temples belong to
  Central (BR-TPL-04) → AC12. Resolved 2026-10-15.
- ~~On-device search by name + place~~ → server search by name + aliases, no address (BR-TPL-05) →
  AC9–AC11. Resolved 2026-10-15.
- ~~Local "Theo dõi" toggle, "Cúng dường" button~~ → both hidden (BR-TPL-08) → AC25. Resolved
  2026-10-15.
- ~~"Báo thông tin sai" in the footer (prototype)~~ → not built (BR-TPL-08) → AC24. Resolved
  2026-10-15.
- ~~Distance from the fixed point Quận 1 (prototype)~~ → only when the user allows location
  (BR-TPL-07) → AC30–AC32. Resolved 2026-10-15.
- ~~Opening hours in the device time zone~~ → always Vietnam time, Rằm & Mùng Một hours by the lunar
  calendar (BR-TPL-09) → AC27. Resolved 2026-10-15.
- ~~Images, carousel temples, home data source (`Docs/home_assumptions.md` #1, #3, #13 — temple part)~~
  → data from the backend (FR-TPL-001), images by contract URL, placeholder when absent (BR-TPL-06);
  the block's error state: AC3 (settled in v1.2). The temple part of those lines is removed in Phase 5.
  Resolved 2026-10-15.

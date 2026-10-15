# Requirements — Deity

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — the "Chư Phật & Bồ Tát" (Buddhas & Bodhisattvas) screen (`buddhas`), the "Chi tiết Phật / Bồ Tát" (Buddha / Bodhisattva detail) screen (`buddha`) and the Home and Library blocks already have UI but run on 9 sample profiles hardcoded in the domain; filtering on the device, no pagination, no Chư Thiên (devas) type, no Hình tượng (iconography), via computed on the device (scanning the lunar calendar for 400 days), the mantra is not passed to Niệm Phật (Buddha-name recitation), "Chia sẻ" (Share) is a no-op, the footer still has "Báo thông tin sai" (Report wrong information). No repository/API yet.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/buddha/` (the code folder name is kept, product §1.5); the featured blocks live in `ui/screens/home/` and `ui/screens/library/`
Product spec: innerlamp-be `docs/product/deity/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ a8a016b (info.version 1.0.0) — tag Deities (`deitiesTop`, `deitiesList`, `deitiesGet`) and `deitiesSutras`; the v3.0 part (page size 12, `next_via`, `via_events`, the `date` parameter, `400 INVALID_DATE`) follows innerlamp-be `docs/specs/features/deity/requirements.md` @ v3.0 (2026-10-15) — **contract not yet in openapi.yaml** (backend `tasks.md` Phase 9)
Implements: BR-DTY-01, BR-DTY-02, BR-DTY-03, BR-DTY-04, BR-DTY-05, BR-DTY-06, BR-DTY-07, BR-DTY-08, FR-DTY-001, FR-DTY-002, FR-DTY-003, FR-DTY-004, FR-DTY-005, FR-DTY-006, FR-DTY-007, FR-DTY-008, FR-DTY-009, FR-DTY-010, NFR-DTY-01, NFR-DTY-02; related BR-CAL-08, BR-CAL-12, BR-CAL-13 (calendar @ v1.2) — FR-DTY-011 is Backend (Android `n/a` in product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) of the **catalogue of Chư Phật & Bồ Tát** (Buddhas and Bodhisattvas) for
Android. Written in a mixed style: the current state is read from the code on 2026-10-15 (branch
`docs/deity-specs`, based on `main` @ ec7d77e), the rest is prospective. Product rules (types and
labels, order, page size, missing fields, the source of the ngày vía (a deity's commemoration day; "via" below),
the next via occurrence, collapsed đại nguyện (great vows)) live in the product spec and are **not**
repeated here; this file only says how the Android app implements the `BR-DTY-*`/`FR-DTY-*`.

## Constraints from the product spec

- **Public, read-only** (§2.3, §2.7): no login needed; no Bearer (like `TempleApiService`).
- **The server decides order, filtering and paging** (BR-DTY-02, BR-DTY-03, NFR-DTY-01): the app shows
  exactly the order it receives, does not re-sort, does not filter on the device, does not load the whole
  catalogue at once. The `deity-order.json` vector runs only on the backend (product §4.1).
- **The via day and the next via occurrence are returned by the server** (BR-DTY-05, BR-DTY-06; backend
  deity v3.0 chose "the server computes"): the app does **not** convert lunar ↔ solar, does not scan the
  calendar, does not keep its own list of via days; the app only sends the device date as "today" and
  computes the **countdown** itself (BR-CAL-13). Because the app does not pick the next via occurrence
  itself, `deity-next-via.json` is not an Android vector (product BR-DTY-06 "every platform that picks
  it"); the countdown part runs the `calendar/rules/countdown.json` vector through `CalendarCountdown`
  of `../Calendar/`.
- **No sample data** (FR-DTY-010, §2.1 "Android shows nine sample entries"): every deity comes from the
  backend; the contract id (e.g. `bodhisattva_avalokiteshvara`) replaces the sample ids (`quanam`,
  `thichca`, …).
- **Dependencies:** `../Calendar/` (`CalendarCountdown`, `LunarDay`, the Lịch lễ (festival calendar)
  screen `FestivalsRoute`); the Niệm Phật screen (`ui/screens/mala/`, no Android spec yet); the sutra
  screen (`SutraRoute`, neither a `sutra` product spec nor an Android spec yet).

## User Story

As a user (logged in or not), I want to see the featured Buddhas, Bodhisattvas and Chư Thiên on Home
and Library, browse the whole catalogue by type, and open a deity's page, so that I can read the
biography, great vows, iconography and mantras, know the upcoming via day, recite with the mala (tràng
hạt), share, and open related sutras (product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Featured deities on Home and Library

1. WHEN Home opens, THE SYSTEM SHALL call `GET /v1/deities/top?date=<device date>` (public) and show the
   deity cards in the "Thư viện Phật & Bồ Tát" (Buddha & Bodhisattva library) block in exactly the order
   received, showing **all** items received in a horizontally scrolling carousel (product §3.6: the
   number of cards is up to the layout; Android scrolls so it does not truncate), with "Xem tất cả" (See
   all) opening `BuddhaListRoute`. (FR-DTY-001, BR-DTY-02)
2. WHEN Library opens, THE SYSTEM SHALL show the "Chư Phật & Bồ Tát" block from the same `top` source
   (same order, horizontal scroll strip, "Xem tất cả" opens `BuddhaListRoute`), and the "Phật & Bồ Tát"
   (Buddhas & Bodhisattvas) tile in the hub grid opens `BuddhaListRoute`. (FR-DTY-001, BR-DTY-02)
3. WHEN the user taps a card on Home, Library or the catalogue, THE SYSTEM SHALL open
   `BuddhaRoute(buddhaId)` with the contract `id`; back returns to the same scroll position and the pages
   already loaded. (FR-DTY-001, FR-DTY-004, platform)
4. WHEN `top` returns empty or fails, THE SYSTEM SHALL show the block's own empty state (no cards, no
   sample cards) with "Thử lại" (Retry) calling `top` again, without blocking the screen's other blocks.
   (FR-DTY-010)
   - Home: keep the existing empty card ("Thư viện đang trống" (The library is empty) + "Thử lại",
     `HomeEmptyCard`).
   > ⚠️ Not confirmed (product): the copy of the "Chư Phật & Bồ Tát" block's empty state on **Library**
   > (the product only says "their own empty state"; the prototype has no empty variant for Library) —
   > blocks the Phase 2 task "Library block"; hand back to ba-writer in innerlamp-be. Proposed default:
   > use the same copy as the Home empty card.

### A deity card (Home, Library, catalogue)

5. WHEN showing a card, THE SYSTEM SHALL show the `thumbnail_url` image (Coil; `""`/load error →
   placeholder image), the type label per BR-DTY-01 ("Phật" (Buddha) / "Bồ Tát" (Bodhisattva) / "Chư
   Thiên" (Devas) for `BUDDHA` / `BODHISATTVA` / `DEVA`), `name_vi`, `core_virtue` (missing → drop the
   line, BR-DTY-04; catalogue cards only — Home/Library cards per the prototype do not have this line)
   and "Vía dd/MM âm lịch" (Via dd/MM lunar) taken from the lunar date of `next_via`; `next_via = null` →
   no via line. (FR-DTY-002, BR-DTY-01, BR-DTY-04, BR-DTY-06)
   > ⚠️ Not confirmed (product): how the card and the via date tile appear when `next_via` falls in a
   > **leap month** (BR-CAL-08 adds "nhuận" (leap) after the month; the "Vía dd/MM âm lịch" format of
   > BR-DTY-06 has no room for it) — blocks the Phase 2 task "Via date format"; hand back to ba-writer in
   > innerlamp-be. Proposed default: "Vía dd/MM nhuận âm lịch" (Via dd/MM leap lunar).
6. WHEN `type` is not one of the three known values (the contract treats it as an open enum), THE SYSTEM
   SHALL still show that deity as a normal item and show no type label. (BR-DTY-01, platform)
   > ⚠️ Not confirmed (product): the label for an unknown type (hide the label or a generic label) — not
   > blocking (there are only three types today); hand back to ba-writer in innerlamp-be.

### The "Chư Phật & Bồ Tát" catalogue

7. WHEN the user opens `BuddhaListRoute`, THE SYSTEM SHALL call `GET /v1/deities?page=1&date=<device
   date>` (no `type`), show the deities in exactly the order received and the subtitle "{N} vị · tiểu
   sử, đại nguyện, thần chú" ({N} deities · biography, great vows, mantras) with N = the response's
   `total`. (FR-DTY-002, BR-DTY-02, BR-DTY-03)
8. WHEN the user scrolls near the end of the list and `page < total_pages`, THE SYSTEM SHALL load the
   next page and append it to the end, only one request at a time; THE SYSTEM SHALL stop when
   `page >= total_pages` or the returned page is empty, and read the page size from the response's
   `page_size`, not assume 12. (FR-DTY-002, BR-DTY-03, NFR-DTY-01, §3.6)
9. WHEN the user selects the "Chư Phật" (Buddhas) or "Bồ Tát" chip, THE SYSTEM SHALL show exactly three
   fixed chips "Tất cả · Chư Phật · Bồ Tát" (All · Buddhas · Bodhisattvas) (no "Chư Thiên" chip), only
   one chip selected, call page 1 again with `type=BUDDHA` / `type=BODHISATTVA` (dropping the parameter
   entirely for "Tất cả"), replace the list and update N; THE SYSTEM SHALL discard the result of an older
   request that arrives late and not filter anything on the device. (FR-DTY-003, BR-DTY-01, BR-DTY-03,
   NFR-DTY-02)
10. WHEN loading page 1 fails (any error) **or** page 1 of "Tất cả" returns `total = 0`, THE SYSTEM SHALL
    show the empty state of prototype `buddhasEmpty` — "Chưa có thông tin chư Phật" (No information on
    the Buddhas yet), a hint line suggesting to check the connection, "Thử lại" (calls page 1 again with
    the selected chip) and "Xem lịch lễ vía" (See the via festival calendar) (opens `FestivalsRoute`) —
    the header subtitle changes to "Chưa có dữ liệu" (No data yet), and the filter chips are hidden.
    (FR-DTY-010)
11. WHEN a type chip returns `total = 0` (no error), THE SYSTEM SHALL show the same empty state as AC10
    but keep the chip row so the user can switch back. (FR-DTY-010, platform)
12. WHEN loading a next page fails, THE SYSTEM SHALL keep the deities already loaded and show "Thử lại"
    at the end of the list to reload exactly that page. (platform)
13. WHEN the app is killed by the system and reopened on the catalogue (process death), THE SYSTEM SHALL
    keep the selected chip and then reload page 1 for it. (platform)

### A deity's page

14. WHEN `BuddhaRoute(buddhaId)` opens, THE SYSTEM SHALL show a loading state and call
    `GET /v1/deities/{id}?date=<device date>` without login, then:
    - on success: show the page per AC15–AC24;
    - on failure (`404 DEITY_NOT_FOUND`): show a "not found" state with a back button;
    - on failure (other, including `400 INVALID_DATE`): show an error with "Thử lại". (FR-DTY-004,
      BR-DTY-04)
    > ⚠️ Not confirmed (product): the copy of the "not found" state and of the deity-page load error
    > (the product only describes the intent; the prototype has no error variant for `buddha`) — blocks
    > the Phase 2 task "Deity page states"; hand back to ba-writer in innerlamp-be.
15. WHEN a deity's page is shown, THE SYSTEM SHALL show the sections in exactly the FR-DTY-005 order: the
    `image_url` cover (placeholder when `""`/error) with the type label and `pure_land`; `name_vi`,
    `title_honorific`, `name_sanskrit`, `name_chinese`; the via card (AC17); `core_virtue` and the
    `canonical_source` line; "Tiểu sử" (Biography); "Đại nguyện" (Great vows); "Hình tượng"
    (Iconography); "Thần chú & danh hiệu" (Mantras & epithets) + actions (AC20–AC22); "Kinh liên quan"
    (Related sutras) (AC23); the footer (AC24). Each missing field/section is hidden per BR-DTY-04; the
    contract's empty string `""` and empty array are "missing". The header title on scroll is
    `name_vi`. (FR-DTY-005, BR-DTY-04)
16. WHEN the biography is longer than the collapsed height (6 lines, prototype), THE SYSTEM SHALL
    collapse it with "Đọc thêm" (Read more), which on tap expands it fully and changes to "Thu gọn"
    (Collapse); WHEN the biography fits in 6 lines, THE SYSTEM SHALL not show the button. (FR-DTY-005)
17. WHEN a deity's page is shown and `next_via` is not `null`, THE SYSTEM SHALL show the via card: the
    event name, the lunar date tile (day + "THÁNG GIÊNG" (first lunar month) / "THÁNG n" (month n) /
    "THÁNG CHẠP" (twelfth lunar month) per FR-DTY-006), the solar date with weekday, and the countdown
    per BR-CAL-13 computed from the device date to `solar_date` through `CalendarCountdown` ("Hôm nay"
    (Today) / "Ngày mai" (Tomorrow) / "Còn n ngày" (n days left)), with "Lịch lễ" (Festival calendar)
    opening `FestivalsRoute`; WHEN `next_via = null`, THE SYSTEM SHALL hide the via card. (FR-DTY-006,
    BR-DTY-05, BR-DTY-06, BR-DTY-04, BR-CAL-13)
18. WHEN the via card is shown, THE SYSTEM SHALL list all of the deity's via days from `via_events` in
    exactly the order received, each item with its name and (if `next_occurrence` is present) the lunar
    date + solar date; the app does not use `lunar_memorial_days` (deprecated, backend deity v3.0 AC30).
    (FR-DTY-006, BR-DTY-05, BR-CAL-08, BR-CAL-12)
    > ⚠️ Not confirmed (product): whether a via day that has **no** occurrence in the window of this year
    > + next year (`next_occurrence = null`, e.g. a leap-month event) is shown in the list, and what is
    > shown — blocks the Phase 2 task "Via day list"; hand back to ba-writer in innerlamp-be. Proposed
    > default: show the name, no date.
19. WHEN the number of great vows is greater than 5, THE SYSTEM SHALL show the first 5 great vows,
    numbered from 1 in the order received, and "Xem đủ N đại nguyện" (See all N great vows) expands all
    and then changes to "Thu gọn"; WHEN ≤ 5, THE SYSTEM SHALL show all, no button; WHEN `great_vows` is
    empty, THE SYSTEM SHALL hide the section. (BR-DTY-07, BR-DTY-04)
20. WHEN showing "Hình tượng", THE SYSTEM SHALL show the posture, the throne / mount animal, the sacred
    artifacts (one item per line) and the symbolic meaning from `iconography`, hiding each missing line
    and hiding the whole section when all four are missing. (FR-DTY-005, BR-DTY-04)
21. WHEN showing "Thần chú & danh hiệu", THE SYSTEM SHALL show at most three cards in the order "Danh
    hiệu" (Epithet) (`title_honorific`), "Thần chú" (Mantra) (the short mantra in Sino-Vietnamese, then
    Sanskrit), "Đà-la-ni" (Dharani) (name + excerpt), hiding each card that has no content, and hiding
    the whole section when no card has content; the cards no longer have the sample data's `note`
    caption line (the contract has none). (FR-DTY-005, BR-DTY-04)
22. WHEN the "Thần chú & danh hiệu" section is shown, THE SYSTEM SHALL show **one** action row "Trì niệm
    với tràng hạt" (Recite with the mala) + "Chia sẻ" right after the cards (placed in the "Thần chú"
    card in the prototype's button style), replacing the two buttons repeated on every card today; "Trì
    niệm với tràng hạt" is shown only when there is a Sino-Vietnamese short mantra. (FR-DTY-005,
    FR-DTY-008, FR-DTY-009, BR-DTY-04)
    > ⚠️ Not confirmed (product): whether "Chia sẻ" is shown (and what it shares) when the deity has
    > **no** short mantra — FR-DTY-009 only says share the name + short mantra — blocks the Phase 2 task
    > "Share"; hand back to ba-writer in innerlamp-be. Proposed default: still show it, share the name
    > only.

### Practice actions

23. WHEN the user taps "Trì niệm với tràng hạt", THE SYSTEM SHALL open the Niệm Phật screen with the
    recitation phrase set to the deity's Sino-Vietnamese short mantra and a bead count of 0 (not keeping
    the old bead count). (FR-DTY-008)
24. WHEN the user taps "Chia sẻ", THE SYSTEM SHALL open the system share sheet (`Intent.ACTION_SEND`,
    `text/plain`, via `Intent.createChooser`) with a text made of `name_vi`, the Sino-Vietnamese short
    mantra and (if present) the Sanskrit, one part per line; it does not open a link itself; WHEN no app
    can receive it, THE SYSTEM SHALL not crash. (FR-DTY-009, BR-DTY-04)
    > ⚠️ Not confirmed (product): whether the shared text includes a link to the deity's web page
    > (product Appendix A item 3) — blocks the final share content (Phase 2 task "Share"); hand back to
    > ba-writer in innerlamp-be. Until answered: no link.

### Related sutras and footer

25. WHEN a deity's page finishes loading, THE SYSTEM SHALL call `GET /v1/deities/{id}/sutras` (in
    parallel with the detail, loading all pages per `total_pages`) and show "Kinh liên quan" in the order
    received; tapping an item opens `SutraRoute(sutraId)` with the contract `id`; WHEN there are no linked
    sutras (`total = 0`), THE SYSTEM SHALL hide the section; the app no longer matches sutras by name and
    no longer has the fallback sutra "Pháp Cú" (Dhammapada). (FR-DTY-007, BR-DTY-04)
    > ⚠️ Not confirmed (product): order — FR-DTY-007 says "in the sutra library's order", while
    > `deitiesSutras` sorts by role (`SUBJECT`, `SPEAKER`, `MENTIONED`) then editorial order — does not
    > block Android (the app shows exactly the order the server returns); hand back to ba-writer in
    > innerlamp-be.
    > ⚠️ Not confirmed (product): what each sutra row shows besides the Vietnamese title (the prototype
    > has a secondary line like "26 phẩm · 423 kệ" (26 chapters · 423 verses), the contract has no such
    > field) — blocks the Phase 2 task "Sutra row"; hand back to ba-writer in innerlamp-be. Proposed
    > default: title + `collection`.
26. WHEN `GET /v1/deities/{id}/sutras` fails while the detail succeeds, THE SYSTEM SHALL hide the "Kinh
    liên quan" section and not block the rest of the page. (FR-DTY-007, platform)
    > ⚠️ Not confirmed (product): whether a "Kinh liên quan"-only error needs to be reported to the
    > user/"Thử lại" — not blocking; hand back to ba-writer in innerlamp-be.
27. WHEN a deity's page is shown, THE SYSTEM SHALL show the footer with only "Nội dung do Ban biên tập
    tổng hợp từ kinh điển" (Content compiled by the editorial board from the scriptures) — no "Báo thông
    tin sai", no favourite, no follow. (BR-DTY-08)

### The device date

28. WHEN the app calls one of the three deity operations, THE SYSTEM SHALL send `date` = the device's
    current date (`LocalDate.now(clock)`, device time zone) as `YYYY-MM-DD`, and use that **same** date as
    "today" for the countdown. (BR-DTY-06, BR-CAL-13)
29. WHEN a deity's page returns to the foreground (`ON_RESUME`) and the device date differs from the date
    used for the load, THE SYSTEM SHALL reload the detail with the new date (so that `next_via` and the
    countdown do not turn into "Đã qua" (Passed)). (BR-DTY-06, platform)

### Performance and vectors

30. WHEN measured on a 4G network, THE SYSTEM SHALL show page 1 of a chip change within 2 seconds of
    sending the request. (NFR-DTY-02)
31. WHEN unit tests run, THE SYSTEM SHALL pass every case of innerlamp-be
    `docs/product/calendar/rules/countdown.json` (BR-CAL-13) through `CalendarCountdown` (shared with
    `../Calendar/`, task there) and separately test that a deity's page uses `CalendarCountdown` with the
    date that was sent; `deity-order.json` and `deity-next-via.json` do not run on Android (the server
    picks the order and the via occurrence). (BR-DTY-06, BR-CAL-13)

### Current code state per AC (read on 2026-10-15)

| AC | Status | Notes |
|---|---|---|
| 1 | drift | `HomeSampleData.load()` (fake 900 ms delay) returns 9 sample `BuddhaFigure`; has a skeleton + carousel; sample ids |
| 2 | drift | `LibraryViewModel` synchronously takes 9 `BuddhaProfile` from `GetBuddhaProfilesUseCase`; hub tile `LibraryHub.Buddhas` → `BuddhaListRoute` ✓ |
| 3 | ✓ navigation part | `onBuddha(id)` → `BuddhaRoute(id)` on Home, Library, catalogue; the id is a sample id |
| 4 | drift | Home: the empty card exists, but error and empty are shared with 2 other blocks (`Docs/home_assumptions.md` #13); Library: no empty state |
| 5 | drift | No image (gradient placeholder, `home_assumptions.md` #1); label "Phật tương lai" (Future Buddha) (Di Lặc (Maitreya)) — **drift BR-DTY-01**; the via is a lunar date hardcoded in the sample — **drift BR-DTY-05**; the secondary line is the sample `role` |
| 6 | missing | `BuddhaCategory` only has `Buddha`, `Bodhisattva`; no Chư Thiên — **drift BR-DTY-01** |
| 7, 8 | missing | No pagination; the subtitle counts all 9 samples — **drift BR-DTY-03 / NFR-DTY-01** |
| 9 | drift | The three chips are right, but `BuddhaListUiState.filtered` filters **on the device**; the subtitle does not follow the chip |
| 10–12 | missing | No empty/error state (`TODO(il-data-builder)` in `BuddhaListViewModel`) |
| 13 | missing | The chip does not go through `SavedStateHandle` |
| 14 | missing | `GetBuddhaDetailUseCase` returns the first deity when the id does not match; no loading, no error, no 404 |
| 15 | drift | Missing `title_honorific`, the Chinese-character name, `canonical_source`; the header uses `shortName` (not in the contract); has a sample `vowIntro` |
| 16 | drift | "Đọc thêm" is always shown even when the biography is short |
| 17 | drift | One hardcoded via day + a 400-day lunar scan on the device (`GetBuddhaDetailUseCase.nextVia`) — **drift BR-DTY-05/06**; the countdown exists (logic in `BuddhaDetailUiState.nextViaLabel`); "Lịch lễ" → `FestivalsRoute` ✓ |
| 18 | drift | The other via days are only the sample `extraVia` string |
| 19 | ✓ collapse part | 5 + "Xem đủ …" + "Thu gọn" exist; the label uses the sample `vowUnit` ("lời nguyện" (vows), "hạnh nguyện" (practice vows)), count label "· trích" (· excerpt) — fix per AC19 |
| 20 | missing | No Hình tượng section — **drift FR-DTY-005** |
| 21, 22 | drift | Sample cards "DANH HIỆU" (EPITHET)/"TÂM CHÚ" (HEART MANTRA) with `note`; two buttons on **each** card |
| 23 | drift | `onChant` opens `MalaRoute` without passing the recitation phrase (`MalaUiState.mantra` defaults to "Nam mô A Di Đà Phật" (Homage to Amitabha Buddha)) — **drift FR-DTY-008** |
| 24 | missing | `onShare = {}` (no-op) in `MainActivity.kt` — **drift FR-DTY-009** |
| 25, 26 | drift | Related sutras are sample `BuddhaSutraRef`, with a fallback sutra `phapcu` — **drift FR-DTY-007** |
| 27 | drift | Footer "… · Báo thông tin sai" — **drift BR-DTY-08** |
| 28, 29 | missing | `Clock` is injected in the ViewModel/use case but no API is called |
| 30, 31 | missing | `GetBuddhaDetailUseCaseTest` tests the lunar scan — to be removed |

## Data & API

The source of truth is `openapi.yaml` (and backend deity `requirements.md` v3.0 for the part not yet in
the contract); schemas are not copied here. Every operation is public (no `@Authenticated`).

| Action | Operation (openapi) | Auth | Stored locally | Notes |
|---|---|---|---|---|
| Featured deities | `GET /v1/deities/top?date=` (`deitiesTop`) | public | none | In the contract (at most 10); `date` and `next_via` on items: backend deity v3.0 AC 2, 24–26, 29 — ⚠️ contract not yet in openapi.yaml |
| Paged catalogue + type filter | `GET /v1/deities?page=&type=&date=` (`deitiesList`) | public | none | `page`, `type` are in the contract (page size 20 today); page size 12, `date`, `next_via`: backend deity v3.0 AC 5, 22, 24–26 — ⚠️ contract not yet in openapi.yaml |
| A deity's page | `GET /v1/deities/{id}?date=` (`deitiesGet`) | public | none | The profile part is in the contract; `next_via`, `via_events`, `date`: backend deity v3.0 AC 8, 24–29 — ⚠️ contract not yet in openapi.yaml. **Do not** use `lunar_memorial_days` |
| Related sutras | `GET /v1/deities/{id}/sutras?page=` (`deitiesSutras`) | public | none | In the contract; `role` is not sent |
| Images | `thumbnail_url` / `image_url` | public | Coil image cache | Use the URL exactly as the server returns it; `""` → placeholder |
| Recitation phrase to Niệm Phật | — (in-app navigation) | — | none | Route arg, AC23 |
| Share | — (`ACTION_SEND`) | — | none | AC24 |

## Error → UI mapping

| Operation | Failure case | User sees | Code status |
|---|---|---|---|
| `GET /v1/deities/top` | Any error (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`, `400 INVALID_DATE`) or an empty 200 | The block's empty state + "Thử lại" (AC4; Library ⚠️ copy) | drift (Home: shared empty card; Library: none) |
| `GET /v1/deities` page 1 | Any error, or `total = 0` on "Tất cả" | `buddhasEmpty`: "Chưa có thông tin chư Phật" + a check-the-connection hint + "Thử lại" + "Xem lịch lễ vía" (AC10) | missing |
| `GET /v1/deities` page 1 | 200, `total = 0` on a type chip | The AC10 empty state, chips kept (AC11) | missing |
| `GET /v1/deities` page ≥ 2 | Any error | Keep the list, "Thử lại" at the end (AC12) | missing |
| `GET /v1/deities/{id}` | `404 DEITY_NOT_FOUND` | "Không tìm thấy" (Not found) + back (AC14, ⚠️ copy) | missing (falls back to the first deity) |
| `GET /v1/deities/{id}` | `NoConnection` / `Timeout` / 5xx / `400 INVALID_DATE` / other | Error + "Thử lại" (AC14, ⚠️ copy) | missing |
| `GET /v1/deities/{id}/sutras` | Any error | Hide "Kinh liên quan", the rest still shows (AC26) | missing |
| (device) | No app receives `ACTION_SEND` | Nothing happens, no crash (AC24) | missing |

## Out of scope

- Search in the catalogue, "Báo thông tin sai", favourites, follow, via day reminders, the three-card
  share screen, editing the catalogue (product §1.2).
- Loading catalogue content (FR-DTY-011 — Backend).
- The festival calendar (`../Calendar/`): this spec only opens `FestivalsRoute` and reuses
  `CalendarCountdown`.
- The sutra screen (`SutraRoute`): this spec only opens it with the backend `id`. That screen currently
  runs on a sample sutra catalogue (`GetSutraDetailUseCase`, ids like `phapcu`/`k-kinh-phap-cu`), so a
  backend id (e.g. `pho-mon`) does not open the right sutra until the sutra feature has a spec and a data
  layer — outside this spec.
- The Niệm Phật screen beyond receiving the recitation phrase (saving the bead count, sync —
  `MalaViewModel` TODO).
- An offline reading copy (product §3.6 allows it, Android does **not** do it in v1.0 — see `design.md`
  "Local storage").
- The "Chư Thiên" chip (product Appendix A item 5 — undecided, not blocking).

## Pending (not yet implemented)

- Domain: contract-based model, repository, use cases; remove the sample data and the lunar scan —
  `tasks.md` Phase 1.
- Home/Library blocks, a catalogue with pagination + server-side filtering + empty state, a deity's page
  per FR-DTY-005 (Hình tượng, via card from the server, one action row, "Báo thông tin sai" removed),
  passing the recitation phrase to Niệm Phật, real sharing (AC1–AC29) — Phase 2.
- Data layer for the four operations (AC1, AC7–AC14, AC25) — Phase 3; page size 12, `date`, `next_via`,
  `via_events` wait for backend deity v3.0 to land in `openapi.yaml`.
- Countdown vector, NFR measurement (AC30, AC31) — Phase 4.
- Clean up `Docs/home_assumptions.md` (the Buddha parts of #1, #13) — Phase 5.

## Open questions (resolved)

Per product spec v1.0 (2026-10-15) and backend deity spec v3.0 — these points replace the current
behaviour of the Android code:

- ~~Label "Phật tương lai" (Di Lặc), no Chư Thiên type~~ → three types, labels "Phật" / "Bồ Tát" / "Chư
  Thiên", no Chư Thiên chip (BR-DTY-01) → AC5, AC6, AC9. Resolved 2026-10-15.
- ~~Filtering on the device, loading the whole catalogue~~ → the server filters by `type`, page size 12,
  the next page loads at the end of the list (BR-DTY-03, FR-DTY-002, FR-DTY-003, §3.6) → AC7–AC9.
  Resolved 2026-10-15.
- ~~Hardcoded via day + a 400-day lunar scan on the device~~ → a via day is a festival calendar event,
  the next via occurrence is computed by the server with the device `date`; the app only counts down
  (BR-DTY-05, BR-DTY-06; backend deity v3.0) → AC5, AC17, AC18, AC28. Resolved 2026-10-15.
- ~~Related sutras matched by name, Pháp Cú fallback~~ → `GET /v1/deities/{id}/sutras` (FR-DTY-007) →
  AC25. Resolved 2026-10-15.
- ~~"Báo thông tin sai" in the footer~~ → removed (BR-DTY-08) → AC27. Resolved 2026-10-15.
- ~~"Trì niệm" (Recite) does not pass the recitation phrase, "Chia sẻ" is a no-op~~ → pass the short
  mantra, bead count 0; share through the system share sheet (FR-DTY-008, FR-DTY-009) → AC23, AC24.
  Resolved 2026-10-15.
- ~~No Hình tượng yet~~ → keep the Hình tượng section (FR-DTY-005) → AC20. Resolved 2026-10-15.
- ~~Sample data on error / empty~~ → the `buddhasEmpty` empty state, no fake content (FR-DTY-010) →
  AC4, AC10. Resolved 2026-10-15.
- ~~The lunar calendar & the day-30 / leap-month rules for via days (`Docs/home_assumptions.md` #11,
  removed earlier by `../Calendar/`)~~ → belongs to the festival calendar, computed by the server
  (BR-CAL-04, BR-CAL-07). Resolved 2026-10-15.

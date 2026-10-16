# Requirements — Sutra

Version: 1.0
Last updated: 2026-10-16
Status: planned (drift) — the "Kinh sách" (Sutra library) list (`sutralist`), the sutra page (`sutra`, "Chi tiết bài kinh"), the reading screen (`reader`, "Tụng kinh") and the Library "Kinh thường tụng" (Sutras commonly chanted) block run on sample data hardcoded in the domain (11 catalogue items, one fixed sutra page, one fixed verse); filters and search run on the device over those samples, there is no paging, no table of contents, no version switch, no licence or unreviewed notice, no linked deities, and a simulated player (progress, play/pause, speed, teacher) is still on screen. The font size is an in-memory value that the reader does not use. The quote of the day exists (bundled file + Room + `SutraQuoteUseCase.quote(date)`) but Home and the Calendar screen pick a random verse and the lunar-day screen shows a sample verse. No repository/API yet.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/sutralist/`, `ui/screens/sutra/`, `ui/screens/reader/` (the code folder names are kept, product §1.5); the Library block lives in `ui/screens/library/`, the quote of the day in `ui/screens/home/`, `ui/screens/calendar/` and `ui/screens/lunar/`, the shared font-size setting in `ui/screens/settings/`
Product spec: innerlamp-be `docs/product/sutra/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ a8a016b (info.version 1.0.0) — tag Sutras (`sutrasList`, `sutrasGet`, `sutrasLines`) and `deitiesSutras` (called by `../Deity/`, not here)
Implements: BR-STR-01, BR-STR-02, BR-STR-03, BR-STR-04, BR-STR-05, BR-STR-06, BR-STR-07, BR-STR-08, BR-STR-09, BR-STR-10, BR-STR-11, BR-STR-12, BR-STR-13, FR-STR-001, FR-STR-002, FR-STR-003, FR-STR-004, FR-STR-005, FR-STR-006, FR-STR-007, FR-STR-008, FR-STR-009, FR-STR-010, FR-STR-011, NFR-STR-01, NFR-STR-02 — FR-STR-012 and NFR-STR-03 are Backend (Android `n/a` in product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) of the **sutra library ("Kinh sách")** for Android. Written in a mixed style:
the current state is read from the code on 2026-10-16 (`main` @ 642ef1b), everything else is
prospective. Product rules (kinds, traditions and their labels, library order and page size, the two
chips, search matching, Vietnamese-first titles and versions, text shown as stored, licence and
unreviewed notice, duration, links to Buddhas and Bodhisattvas, quote of the day, reading
preferences, no audio, public access) live in the product spec and are **not** repeated here; this
file only says how the Android app implements the `BR-STR-*`/`FR-STR-*`.

## Constraints from the product spec

- **Public, read-only** (BR-STR-13, §2.3): no login needed; no Bearer (like `TempleApiService`).
  A guest and a signed-in user see the same thing.
- **The server decides order, filtering, searching and paging** (BR-STR-02, BR-STR-03, BR-STR-04,
  NFR-STR-01, NFR-STR-02): the app shows exactly the order it receives, does not re-sort, does not
  filter or search on the device and never loads the whole library or a whole text at once. Its part
  of these rules is to send the right request (chip → parameter, trimmed term cut to 100 characters).
  Consequence for the vectors: `sutra-browse.json` and `sutra-search-match.json` decide what the
  backend returns, so on Android they check the request that is built (tasks Phase 4); see the open
  question "Which clients run the browse and search vectors".
- **The app computes three things itself**: the version that opens first (BR-STR-05, checked against
  `rules/sutra-default-version.json`), the quote number of a date (BR-STR-10, checked against
  `rules/sutra-daily-quote.json`) and the page that holds a given line (arithmetic given by the
  contract for `sutrasGet`/`sutrasLines`, used for the table-of-contents jump).
- **Vietnamese first, whatever the app language** (BR-STR-05): the interface of this feature uses the
  Vietnamese title (`titles.vi`) and opens the Vietnamese version; it never reads the app or device
  locale to choose a title or version.
- **No audio, no simulated player** (BR-STR-12, §1.5): the play button, progress card, elapsed time,
  speed chip, teacher line, "Tải về máy" (Download to device), "Tải để nghe offline" (Download to
  listen offline) and "Chia sẻ câu kinh" (Share verse) chips of the current screens are not adopted and
  are removed (offline download and verse sharing are out of scope, §1.2).
- **Online-only for the library** (§2.4 "may keep the last loaded content"): Android does **not** keep
  an offline copy of lists, pages or text in v1.0 (same choice as `../Deity/` and `../Temple/`; see
  `design.md` "Local storage"). The quote of the day needs no network (bundled file).
- **Dependencies:** `../Deity/` (a deity page opens `SutraRoute(id)` with the contract id from
  `GET /v1/deities/{id}/sutras`; this spec opens `BuddhaRoute(deityId)` from the sutra page);
  `../Calendar/` (the Calendar screen's "today" card and the lunar-day screen show the quote);
  `../Networking/` (`NetworkClient.safeCall`) and `../Storage/` (`PreferencesStorage`); the Settings
  screen (no Android spec yet — this spec owns only the shared font-size preference and the "Cỡ chữ
  kinh văn" (Scripture font size) row's wiring); the later `library` product spec (the "Thư viện" hub
  tab and its "Kinh thường tụng" block).

## User Story

As a visitor (signed in or not), I want to browse, filter and search the sutra library, open a text
to see what it is and where it comes from, read it with a table of contents, and see a verse of the
day, so that I can find and read the text I need (product §1.1, §2.2).

## Acceptance Criteria (EARS notation)

### The library list

1. WHEN the user opens `SutraListRoute`, THE SYSTEM SHALL show a loading state, call
   `GET /v1/sutras?page=1` (public, no filter, no `q`) and show the texts in exactly the order
   received, one card per text, with the line "{total} bản kinh" ({total} texts) from the response's
   `total`; THE SYSTEM SHALL not re-sort and not filter anything on the device. (FR-STR-001,
   BR-STR-02, BR-STR-13)
2. WHEN showing a card, THE SYSTEM SHALL show `titles.vi`, the kind and tradition labels, the
   collection and citation code (`reference`, hidden when `""`), "khoảng N phút" (about N minutes) from
   `duration_minutes` (hidden when `null`), the summary (hidden when `""`, at most three lines) and the
   daily-chanting mark when `daily_chant` is true; THE SYSTEM SHALL show no audio badge and no
   downloaded badge. (FR-STR-001, BR-STR-01, BR-STR-05, BR-STR-08, BR-STR-12)
   > ⚠️ Not confirmed (product): the exact copy of the kind, tradition, purpose and duration-chip labels
   > (product Appendix A item 11). `SutraFormat` keeps the proposed labels of the product spec
   > (kinds "Kinh" / "Kệ" / "Chú"; traditions "Nam Tông" / "Bắc Tông" / "Kim Cang Thừa"; the ten
   > purposes of BR-STR-03; "Tối đa 5 / 15 / 30 / 60 phút"), replacing today's "Nam tông" / "Bắc tông"
   > and "Dưới 15 phút" style labels; they stand until changed.
3. WHEN a text carries a `kind`, `tradition` or `purposes` value the app does not know (the app may be
   older than the server), THE SYSTEM SHALL still show the text as a normal card and show no label for
   the unknown value. (BR-STR-01, platform)
4. WHEN the user scrolls near the end of the list and `page < total_pages`, THE SYSTEM SHALL load the
   next page and append it, one request at a time, with the same chips and term; THE SYSTEM SHALL
   stop when `page >= total_pages` or the returned `items` are empty, without an error, and read the
   page size from the response's `page_size`, not assume 20. (FR-STR-001, BR-STR-02, NFR-STR-01)
   > ⚠️ Not confirmed (product): whether the page size stays 20 or becomes 12 (product Appendix A item
   > 12) — does not block Android, which reads `page_size` from the response.
5. WHEN the user selects a topic chip or a duration chip, THE SYSTEM SHALL show two rows of chips —
   topic: "Tất cả", "Hằng ngày", the ten purposes, the three traditions; duration: "Tất cả", "Tối đa 5
   phút", "Tối đa 15 phút", "Tối đa 30 phút", "Tối đa 60 phút" — with exactly one chip selected in each
   row, then call page 1 again with the parameter of the topic chip (`daily=true`, or `purpose=`, or
   `tradition=`; none for "Tất cả") **and** the parameter of the duration chip (`max_minutes=`; none for
   "Tất cả") together with the current term, replace the list, update the count, and discard the result of
   an older request that arrives late. (FR-STR-002, BR-STR-03, BR-STR-08, NFR-STR-02)
6. WHEN the user types in the search field, THE SYSTEM SHALL wait 400 ms after the last keystroke
   (or until the keyboard's search action), trim the term and cut it to 100 characters, and call page 1
   with `q=<term>` and the active chips (an empty term after trimming sends no `q`), replace the list
   and update the count; WHEN the user clears the field (the "×"), THE SYSTEM SHALL show the list again
   without a term. THE SYSTEM SHALL send the term as typed — it does no diacritic folding and no
   wildcard escaping, because the server matches without case and diacritics and treats wildcards
   literally. (FR-STR-003, BR-STR-04, BR-STR-02, NFR-STR-02)
   > ⚠️ Not confirmed (product): whether a search term narrows within the active chips (backend: both
   > apply) or clears them on the apps (product Appendix A item 8) — blocks nothing on Android; this
   > spec sends both, as the product spec states.
7. WHEN the user opens a text and comes back, THE SYSTEM SHALL keep the scroll position, the pages
   already loaded, the chips and the term; WHEN the system kills the app and the user reopens the list
   (process death), THE SYSTEM SHALL keep the chips and the term and reload page 1 for them.
   (platform)
8. WHEN `total = 0`, THE SYSTEM SHALL show an empty state that says which case it is: the library has
   no text (no chip, no term), the chips match no text, or the term matches no text; WHILE page 1
   loads THE SYSTEM SHALL show a loading state, never an empty one. (FR-STR-011, FR-STR-002,
   FR-STR-003)
   > ⚠️ Not confirmed (product): the copy of the three empty states (product Appendix A item 10).
   > `SutraFormat` keeps the existing copy "Chưa có bản kinh phù hợp" (No matching text yet) / "Thử bỏ
   > bớt bộ lọc hoặc gõ tên khác." (Try removing a filter or typing another name.) for the chips and
   > search cases, and proposes "Thư viện kinh đang trống" (The sutra library is empty) for the first.
   > ⚠️ Not confirmed (product): whether the search-empty view offers "Gửi yêu cầu bản kinh" (Request a
   > text) and what it does (product Appendix A item 9). The button exists today and does nothing
   > (`onRequestSutra` is not wired); until answered it is **hidden**.
9. WHEN loading page 1 fails (any error), THE SYSTEM SHALL show an error state with "Thử lại" (Retry)
   that repeats the same request (same chips and term), keep the chips and the search field usable,
   and show no sample text; WHEN loading a next page fails, THE SYSTEM SHALL keep the texts already
   loaded and show "Thử lại" at the end of the list to reload exactly that page. (FR-STR-011,
   platform)

### The sutra page

10. WHEN `SutraRoute(sutraId)` opens, THE SYSTEM SHALL show a loading state and call
    `GET /v1/sutras/{id}` without login, then:
    - on success: show the page per AC11–AC16;
    - on failure (`404 SUTRA_NOT_FOUND`): show a "not found" state with a back button;
    - on failure (other): show an error state with "Thử lại". (FR-STR-004, FR-STR-011, BR-STR-13)
    > ⚠️ Not confirmed (product): the copy of the not-found and error states (product Appendix A item
    > 10) — `SutraFormat` keeps proposed defaults, marked temporary.
11. WHEN a sutra page is shown, THE SYSTEM SHALL show `titles.vi` as the title, `original_title`
    beneath it, the kind and tradition labels, the collection and citation code, the summary, the
    estimated duration ("khoảng N phút"), and — as facts — the author, the Chinese translator with the
    year (`han_translator`, `han_translation_year`) and the remark (`reference_note`); THE SYSTEM
    SHALL hide every empty value (`""`, `null`, an empty list) and its label. THE SYSTEM SHALL use the
    Vietnamese title whatever the app language. (FR-STR-004, BR-STR-01, BR-STR-05, BR-STR-07, BR-STR-08)
12. WHEN a sutra page is shown, THE SYSTEM SHALL list every version (`translations`, in the order
    received) with its translator, its source (`source_name`, tappable link to `source_url` when that
    is not `""`) and its licence label: "CC0", "CC BY-NC 4.0", "Phạm vi công cộng" (Public domain) or
    "Được sao chép tự do" (Free to copy) for the codes `CC0-1.0`, `CC-BY-NC-4.0`, `PUBLIC_DOMAIN`,
    `COPY_ALLOWED`; for `UNKNOWN` (and any code the app does not know) THE SYSTEM SHALL show **no**
    licence line, and show the source's own statement (`license_note`) when it is not `""`. A licence
    never hides a text. (FR-STR-004, BR-STR-07)
    > ⚠️ Not confirmed (product): whether the version `note` and the licence link (`license_url`, set
    > for CC0 versions) are shown (BR-STR-07 lists translator, source, licence and the source
    > statement only) — blocks nothing; the app shows neither until answered.
13. WHEN `review_status` is `UNREVIEWED`, THE SYSTEM SHALL show one short notice, "Nội dung chưa được
    người am hiểu duyệt" (The content has not yet been checked by someone knowledgeable), on the sutra
    page; WHEN it is any other value, THE SYSTEM SHALL not show the notice. (FR-STR-004, BR-STR-07)
14. WHEN `deities` is not empty, THE SYSTEM SHALL list the linked Buddhas and Bodhisattvas in exactly the
    order received (the server orders them subject, speaker, mentioned), each with its image
    (`thumbnail_url`; `""` or a load error → a placeholder), `name_vi` and its role; choosing one
    SHALL open `BuddhaRoute(deity_id)`; WHEN `deities` is empty, THE SYSTEM SHALL not show the section.
    (FR-STR-008, BR-STR-09)
    > ⚠️ Not confirmed (product): the role labels shown next to a linked deity ("thuyết kinh",
    > "chủ đề", "được nhắc đến" are the proposal) — BR-STR-09 orders by role but gives no label copy;
    > blocks only the label text.
15. WHEN a sutra page is shown, THE SYSTEM SHALL show a "Đọc kinh" (Read) action that opens
    `ReaderRoute(sutraId)`, and SHALL show no play button, no reading-progress card, no
    "Tụng theo" (Chant along) action and no "Tải về máy" action. (FR-STR-004, FR-STR-005, BR-STR-12)
16. WHEN the user taps a source link and no app can open it, THE SYSTEM SHALL not crash and show
    nothing; the link opens outside the app (`Intent.ACTION_VIEW`), because it leads to a third-party
    site (product §3.7). (platform)

### Reading

17. WHEN `ReaderRoute(sutraId)` opens, THE SYSTEM SHALL show a loading state, call
    `GET /v1/sutras/{id}`, choose the version that opens first by the rule of BR-STR-05 from
    `translations` (the position in the list is the source order), and call
    `GET /v1/sutras/{id}/lines?translation=<key>&page=1`, then show the lines from the first one; THE
    SYSTEM SHALL use the Vietnamese version whatever the app language. (FR-STR-005, BR-STR-05)
    > ⚠️ Not confirmed (product): which version opens for the ten Vajrayāna texts that have no
    > Vietnamese version (product Appendix A item 5). `SutraDefaultVersion` returns the first version
    > in source order for them until answered; the vector file does not cover this case.
18. WHILE the user reads, THE SYSTEM SHALL load further pages of lines as the visible position
    approaches them, one request per page, using the `page_size` of the first response for every page
    of that version (it SHALL not change it between calls); THE SYSTEM SHALL stop when
    `page >= total_pages` or `items` are empty, and SHALL never request the whole text. (FR-STR-005,
    NFR-STR-01)
19. WHEN lines are shown, THE SYSTEM SHALL show each line's `text` exactly as received — no spelling
    correction, no re-wrapping that changes words, no translation — in the order of `n`, and a section
    heading (the `section` name) at the first line of each section; line numbers have no meaning
    across versions and are not shown. (FR-STR-005, BR-STR-06)
20. WHEN the selected version has sections (`sections` not empty), THE SYSTEM SHALL offer a table of
    contents ("Mục lục") listing the sections in order; WHEN the user chooses a section, THE SYSTEM
    SHALL show the text from that section's first line (`start_n`), loading the page that holds it
    (`floor((start_n - 1) / page_size) + 1`) without loading the pages before it; WHEN the version has
    no sections, THE SYSTEM SHALL offer no table of contents. (FR-STR-005, BR-STR-06)
    > ⚠️ Not confirmed (product): whether the apps show one section at a time like the website or
    > scroll the whole text continuously (product Appendix A item 7). This spec scrolls continuously,
    > with the table of contents jumping inside the same list.
21. WHEN the text has more than one version, THE SYSTEM SHALL offer a version switch in the reading
    screen; WHEN the user chooses another version, THE SYSTEM SHALL show that version from its first
    line with its own table of contents; WHEN the text has one version, THE SYSTEM SHALL offer no
    switch. (FR-STR-006, BR-STR-05, BR-STR-06)
    > ⚠️ Not confirmed (product): whether Android offers the version switch in this release and how a
    > version is named (product Appendix A item 6). This spec offers it, and `SutraFormat` names a
    > version "{variant} · {language}" with the variants "Bản dịch" (Translation), "Phiên âm Hán-Việt"
    > (Sino-Vietnamese chant), "Dịch nghĩa" (Meaning), "Thơ" (Poem) and the language names
    > "Tiếng Việt", "Tiếng Anh", … as the product spec's examples; a second version of the same variant
    > and language gets its position ("2") appended.
22. WHEN the user opens "Aa" in the reading screen, THE SYSTEM SHALL offer the four sizes "Nhỏ" (Small),
    "Vừa" (Medium), "Lớn" (Large), "Rất lớn" (Very large) in a sheet over the text, apply the choice at
    once, remember it on the device and apply it to every later text; THE SYSTEM SHALL use the same
    stored value for the "Cỡ chữ kinh văn" row in Settings, in both directions (a change in either place
    shows in the other); THE SYSTEM SHALL draw the reading background from the app's light or dark
    appearance and offer no separate reading theme. (FR-STR-007, BR-STR-11)
    > ⚠️ Not confirmed (product): where the size is chosen in the reading view — a sheet over the text
    > (this spec) or "Aa" leading to the Settings screen as the prototype's `goSettings` does — and
    > which size is the default. FR-STR-007/BR-STR-11 say "in the reading view" and give no default;
    > this spec uses the sheet and keeps Android's current default, "Vừa" (index 1), which is the
    > current 21 sp text size.
23. WHEN the reading screen is shown, THE SYSTEM SHALL show no player bar, no play/pause control, no
    speed chip, no elapsed time or progress bar and no teacher line, and SHALL not keep the screen
    awake. (BR-STR-12)
    > ⚠️ Not confirmed (product): what the reading view offers while there is no audio — nothing, or the
    > auto-scroll with a highlighted line (product Appendix A item 4). This spec offers nothing.
24. WHEN the detail call fails, THE SYSTEM SHALL show the error state of AC10 for the reading screen
    (`404 SUTRA_NOT_FOUND` → not found + back; other → error + "Thử lại"); WHEN `lines` returns
    `404 TRANSLATION_NOT_FOUND` for the first page, THE SYSTEM SHALL show the not-found state; WHEN a
    later page fails, THE SYSTEM SHALL keep the lines already shown and show "Thử lại" where the missing
    lines would be, reloading exactly that page; WHEN a version has `total = 0` lines, THE SYSTEM
    SHALL show an empty state, never sample text. (FR-STR-011, platform)
25. WHEN the system kills the app while the reading screen is open and the user reopens it, THE SYSTEM
    SHALL reopen the same text and version at the line that was at the top of the screen. (platform)
26. WHEN the user opens the longest text (6,967 lines), THE SYSTEM SHALL show the first lines within 2
    seconds on a 4G connection, without loading the rest. (NFR-STR-01)

### The quote of the day

27. WHEN Home is shown, THE SYSTEM SHALL show the quote of the Gregorian date shown on the week strip
    (the device date until the user picks another day) — the verse text and its attribution (Kinh Pháp
    Cú, translator, chapter and verse number) — chosen by `SutraQuoteUseCase.quote(date)` instead of
    `randomQuote()` and the sample `verseFor` lunar rotation; the same verse however often Home is
    opened that day. (FR-STR-010, BR-STR-10)
    > ⚠️ Not confirmed (product): whether Home shows the quote of the **selected** day of the week strip
    > (Android today changes the verse when another day is picked) or always today's (FR-STR-010 says
    > "that date" and "today is the date on the user's device"). This spec follows the selected day.
28. WHEN the Calendar screen's "today" card or the lunar-day screen (`LunarDetailRoute(epochDay)`) is
    shown, THE SYSTEM SHALL show the quote of the solar date of the day being shown, by
    `SutraQuoteUseCase.quote(date)`. (FR-STR-010, BR-STR-10)
29. WHEN `SutraQuoteUseCase.quote(date)` is called, THE SYSTEM SHALL apply the rule of BR-STR-10 with
    the day of the year of the Gregorian date and the number of quotes in the bundled file (300),
    counting quotes in `sequence` order; THE SYSTEM SHALL not use `randomQuote()` for any quote-of-the-day
    view. (BR-STR-10, FR-STR-010)
30. WHEN the quote file cannot be read (the table is empty after a seed attempt), THE SYSTEM SHALL keep
    the screen working and show the built-in fallback verse the screen has today. (FR-STR-010)
    > ⚠️ Not confirmed (product): what the quote block shows when the quote file cannot be read — a
    > built-in fallback line or nothing (product Appendix A item 14); the bundled file is also the
    > answer to Appendix A item 3 for Android (the file is tracked in git and seeded at launch) — not
    > blocking.
31. WHEN a view that shows the quote returns to the foreground and the device date differs from the date
    it showed, THE SYSTEM SHALL show the quote of the new date. (platform)

### Cross-cutting

32. WHEN the app calls one of the three sutra operations, THE SYSTEM SHALL send no `Authorization`
    header and no `X-API-Key` (the operations are public) and SHALL behave the same signed in or not.
    (BR-STR-13)
33. WHEN any of the three sutra screens or the Library "Kinh thường tụng" block shows content, THE
    SYSTEM SHALL show only what the backend returned: no sample text, no sample id (`k-kinh-phap-cu`,
    `phapcu`, …); WHEN the Library block cannot load, THE SYSTEM SHALL show the block's own error
    state with "Thử lại" without blocking the rest of the Library screen. (FR-STR-011)
    > ⚠️ Not confirmed (product): which texts the Library "Kinh thường tụng" block shows and how many
    > (the hub is the later `library` product spec, §1.2). This spec takes the first rows of
    > `GET /v1/sutras?daily=true&page=1` that fit the block until that spec exists.
34. WHEN the user taps "Kinh sách" (Library hub tile or the block's link), THE SYSTEM SHALL open
    `SutraListRoute`; WHEN the user taps a related-sutra row on a deity page (`../Deity/` AC25), THE
    SYSTEM SHALL open `SutraRoute(id)` with the contract id and show that text's sutra page. (FR-STR-009,
    platform)
35. WHEN unit tests run, THE SYSTEM SHALL pass every case of innerlamp-be
    `docs/product/sutra/rules/sutra-default-version.json` (BR-STR-05) and `sutra-daily-quote.json`
    (BR-STR-10), and every case of `sutra-browse.json` and `sutra-search-match.json` as far as the app's
    part goes (the request built for the chips and the term, AC5/AC6), and shall fail when a file's
    `product_spec_version` differs from the pinned copy's `SOURCE`. (BR-STR-02, BR-STR-03, BR-STR-04,
    BR-STR-05, BR-STR-10)
36. WHEN measured on a 4G connection, THE SYSTEM SHALL show page 1 of a chip change or a search within 2
    seconds of sending the request. (NFR-STR-02)

### Current code state per AC (read on 2026-10-16)

| AC | Status | Notes |
|---|---|---|
| 1, 4 | drift | `GetSutraCatalogUseCase` returns 11 sample `SutraCatalogItem`; subtitle "11 bộ · Bắc tông & Nam tông"; no paging — **drift BR-STR-02, FR-STR-001** |
| 2 | drift | Card shows sample original title, translator, tags, "Có audio dẫn" badge, description — **drift BR-STR-01, BR-STR-12**; sects are only Bắc / Nam (no Kim Cang Thừa) |
| 3 | missing | Sample enums only |
| 5 | drift | `SutraTagFilter` (Hằng ngày, Cầu an, Cầu siêu, Trì chú, Bắc tông, Nam tông) and `SutraLengthFilter` ("Dưới 15 phút", "15 – 30 phút", "Trên 30 phút") filter on the device — **drift BR-STR-03**; the two rows already combine |
| 6 | drift | `SutraListUiState.visible` folds diacritics on the device and searches description and tags — **drift BR-STR-04**; no debounce, no 100-character cut |
| 7 | missing | Chips and term live in `MutableStateFlow`, not in `SavedStateHandle` |
| 8 | drift | `EmptyResult` exists ("Chưa có bản kinh phù hợp"); no loading state, no distinction between the three cases; "Gửi yêu cầu bản kinh" does nothing |
| 9 | missing | No error state (`TODO(il-data-builder)`) |
| 10 | missing | `GetSutraDetailUseCase` ignores the id and returns Kinh Pháp Cú — **drift FR-STR-004** |
| 11–14 | missing | Sample hero ("KINH VĂN · SONG NGỮ"), badges, meta line "26 phẩm · 423 kệ · Thầy Minh Đạo tụng · 2 giờ 10 phút", chapters I–V; no facts, versions, licence, notice or deities |
| 15 | drift | Bottom bar has "Tụng theo" + play circle (`onPlayer` no-op), header "Tải về máy" (`onDownload` no-op), a 34 % progress card — **drift BR-STR-12** |
| 16 | missing | No source link |
| 17–21 | missing | `GetSutraReaderUseCase` ignores the id and returns two fixed verses with an English line |
| 22 | missing | `SettingsViewModel.fontSizeIndex` is in memory only (default 1, four steps), the reader never reads it; the reader's "Aa" opens `SettingsRoute` |
| 23 | drift | Progress bar, `PlayerBar` / `PlayerCard`, speed chip, "Thầy Minh Đạo", `togglePlay()` — **drift BR-STR-12** |
| 24–26 | missing | No network, no error state |
| 27 | drift | `HomeViewModel` calls `randomQuote()` at start and on each day pick, and `HomeUiState.verseFor(lunar)` (4 sample verses) as the initial/fallback value — **drift BR-STR-10** |
| 28 | drift | `CalViewModel` uses `randomQuote()` for the "today" card; `LunarDetailViewModel` uses `HomeUiState.verseFor(lunar)` (sample verses rotated by lunar day + month), **not** `quote(date)` |
| 29 | ✓ rule, not wired | `SutraQuoteUseCase.quote(date)` = `dayOfYear % total` as 0-based position in `sequence` order, i.e. quote number `dayOfYear mod total + 1` (BR-STR-10); no caller uses it; `SutraQuoteUseCaseTest` checks it only with a 10-quote fake |
| 30 | ✓ | Home / Calendar keep the sample verse when the use case returns `null` |
| 31 | missing | Home fixes "today" at ViewModel creation (`Docs/home_assumptions.md` row 7) |
| 32 | ✓ trivially | No call exists yet; the new `SutraApiService` must carry no `@Authenticated` |
| 33 | drift | `GetLibraryCatalogUseCase` returns four sample `Sutra` rows with sample ids; Chua's livestream row opens `SutraRoute("k-kinh-phap-cu")` |
| 34 | ✓ navigation part | Library hub tile and link → `SutraListRoute`; list → `SutraRoute`; page → `ReaderRoute`; deity page → `SutraRoute(sutra.sutraId)` (ids are sample ids until the data layer exists) |
| 35, 36 | missing | No vector test, no measurement |

## Data & API

The source of truth is `openapi.yaml`; schemas are not copied here. Every operation is public (no
`@Authenticated`).

| Action | Operation (openapi) | Auth | Stored locally | Notes |
|---|---|---|---|---|
| A page of the library, with chips and term | `GET /v1/sutras?page=&tradition=&purpose=&daily=&max_minutes=&q=` (`sutrasList`) | public | none | The list items already carry titles, enums, summary, deities and the versions (key, language, variant, translator, licence code) — enough for the card and for BR-STR-05; the `kind`, `collection` and `lang` filters are not used |
| One text | `GET /v1/sutras/{id}` (`sutrasGet`) | public | none | Adds `review_status`, original title, author, Chinese translator and year, remark, source name and link, licence note, version note and `sections` |
| Lines of one version | `GET /v1/sutras/{id}/lines?translation=&page=&page_size=` (`sutrasLines`) | public | none | `page_size` is never sent (default 200); the response's `page_size` is the size of every page of that version |
| Sutras of a deity | `GET /v1/deities/{id}/sutras` (`deitiesSutras`) | public | none | Called by `../Deity/`; here only `SutraRoute(id)` receives its result |
| Quote of the day | — (bundled `assets/sutra_quotes_300.json` → Room) | — | Room table `sutra_quotes` (seeded at launch) | No contract (product Appendix B item 4) |
| Font size | — (device preference) | — | `PreferencesStorage` key `scripture_font_size_index` | Shared with Settings (AC22) |
| Source link | — (`Intent.ACTION_VIEW`) | — | none | AC16 |

## Error → UI mapping

| Operation | Failure case | User sees | Code status |
|---|---|---|---|
| `GET /v1/sutras` page 1 | Any error (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`, `400 INVALID_FILTER` / `INVALID_PAGE`) | Error state + "Thử lại" (AC9; ⚠️ copy), chips and search stay usable | missing |
| `GET /v1/sutras` page 1 | 200, `total = 0` | One of the three empty states (AC8; ⚠️ copy) | drift (one shared empty card) |
| `GET /v1/sutras` page ≥ 2 | Any error | Keep the list, "Thử lại" at the end (AC9) | missing |
| `GET /v1/sutras/{id}` | `404 SUTRA_NOT_FOUND` | Not-found state + back (AC10, AC24; ⚠️ copy) | missing (falls back to Kinh Pháp Cú) |
| `GET /v1/sutras/{id}` | `NoConnection` / `Timeout` / 5xx / other | Error state + "Thử lại" (AC10, AC24; ⚠️ copy) | missing |
| `GET /v1/sutras/{id}/lines` page 1 | `404 TRANSLATION_NOT_FOUND` / `404 SUTRA_NOT_FOUND` | Not-found state + back (AC24) | missing |
| `GET /v1/sutras/{id}/lines` page 1, `total = 0` | 200 with no lines | Empty state (AC24; ⚠️ copy) | missing |
| `GET /v1/sutras/{id}/lines` page ≥ 2 | Any error | Keep the lines, "Thử lại" where the page is missing (AC24) | missing |
| Library block (`GET /v1/sutras?daily=true`) | Any error or empty | The block's own error / empty state + "Thử lại" (AC33; ⚠️ copy) | missing |
| (device) | No app can open a source link | Nothing happens, no crash (AC16) | missing |
| Quote | Table empty after a seed attempt | Built-in fallback verse (AC30; ⚠️ product) | ✓ |
| Font size | Preference write fails | The new size applies for this session, no message | missing |

## Out of scope

- The library hub tab ("Thư viện") and the downloads view (later `library` and `downloads` product
  specs); this spec touches the hub only to remove the sample sutra rows (AC33).
- Audio, listening, chant-along, offline download, bookmarks, favourites, reading progress and
  chanting history (product §1.2): their screens (`player`, `downloads`, `share`) are not specified
  here, and the sutra / reading screens drop the entry points to them.
- Full-text search inside a text; sharing a verse card; editing texts (product §1.2).
- Languages other than Vietnamese in the interface of this feature (BR-STR-05).
- An offline reading copy (product §2.4 allows it, Android does **not** do it in v1.0 — see
  `design.md` "Local storage").
- The Settings screen itself (no Android spec yet): only the shared font-size value and its row.
- Replacing the sample id in Chua's livestream row (`onSutra = { navController.navigate(
  SutraRoute("k-kinh-phap-cu")) }`, a `TODO(il-data-builder)` in `MainActivity.kt`): it belongs to the
  Chua / events screens; until they pass a real id the row opens the not-found state of AC10.
- The deity page's own related-sutra section and its calls: `../Deity/` AC25–AC26.
- Loading library content (FR-STR-012, Backend).

## Pending (not yet implemented)

- Domain: contract-based models, repository, use cases, the BR-STR-05 and BR-STR-10 rules, the
  line-paging helper, the font-size preference; remove the sample catalogue / page / reader data —
  `tasks.md` Phase 1.
- List with chips, search, paging and states; sutra page; reading screen with table of contents,
  version switch, font sheet and no simulated player; Library block; quote by date on Home, Calendar
  and lunar-day screens; Settings font-size wiring (AC1–AC34) — Phase 2.
- Data layer for the three operations and the preference (AC1, AC10, AC17, AC22) — Phase 3.
- Vector tests, state tests, NFR measurements (AC35, AC36) — Phase 4.
- Hand-offs and the OKF concept (`Docs/home_assumptions.md` row 3) — Phase 5.

## Open questions (resolved)

Per product spec v1.0 (2026-10-15) — these points replace the current behaviour of the Android code:

- ~~Sample catalogue, sample sutra page and sample reader~~ → every text comes from the backend
  (FR-STR-011, BR-STR-13) → AC1, AC10, AC17, AC33. Resolved 2026-10-15.
- ~~Filters on the device (Bắc / Nam, "Dưới 15 phút" style durations), search over descriptions and
  tags~~ → one topic chip + one duration chip (5 / 15 / 30 / 60) that combine, server search by name
  (BR-STR-03, BR-STR-04) → AC5, AC6. Resolved 2026-10-15.
- ~~Simulated player, progress card, download and share chips~~ → none adopted (BR-STR-12, §1.5) →
  AC15, AC23. Resolved 2026-10-15.
- ~~Random verse on Home and Calendar, sample verse on the lunar-day screen~~ → one quote per date
  (BR-STR-10) → AC27–AC29. Resolved 2026-10-15.
- ~~Font size not remembered, not linked to Settings~~ → four sizes, remembered, shared (BR-STR-11) →
  AC22. Resolved 2026-10-15.

## Open questions (still open)

Questions that are open in the product spec (Appendix A) and affect Android are marked inline above;
the temporary default in each marker is what `design.md` implements. Questions found while writing
this spec, to hand back to `ba-writer` in innerlamp-be:

> ⚠️ Not confirmed (product): which clients run `sutra-browse.json` and `sutra-search-match.json`.
> Product §4.1 says "on every client that filters, searches or picks a version on the device"; Android
> filters and searches on the server, so it runs only the request-building part (AC35). Confirm that
> this is what is meant — blocks the wording of the Phase 4 vector tasks.

> ⚠️ Not confirmed (product): the Android column of product §4.2 for BR-STR-10 / FR-STR-010. The matrix
> note says "the lunar-day view already uses the rule on iOS and Android"; on Android it does not — the
> lunar-day screen shows a sample verse rotated by lunar day + month, and `quote(date)` has no caller.
> The Android cell stays `drift`; the note needs correcting for Android.

> ⚠️ Not confirmed (product): the product-level questions marked inline in AC2/AC8 (label and state
> copy, "Gửi yêu cầu bản kinh"), AC12 (version note, licence link), AC14 (role labels), AC22 (where the
> size is chosen, default size), AC27 (Home quote for the selected day) and AC33 (Library block source)
> — these are new to Appendix A and need an owner decision.

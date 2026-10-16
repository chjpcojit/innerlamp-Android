# Tasks — Sutra

Version: 1.0
Last updated: 2026-10-16
Status: planned (drift) — only the routes, the navigation between screens and the quote storage already match the spec; everything else is `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when verified to exist **and to match what product spec v1.0 requires** in the code on
2026-10-16 (`main` @ 642ef1b); an item that has code but drifts from the product stays `[ ]` and is
noted "(has code, drift: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Already exists, keep as is

- [x] `SutraListRoute`, `SutraRoute(sutraId)`, `ReaderRoute(sutraId)` in `ui/navigation/Routes.kt`, and
      their wiring in `MainActivity.kt`: Library hub tile and block link → list, list → sutra page,
      sutra page → reading screen, deity page → `SutraRoute(sutra.sutraId)` (AC34, navigation part; the
      ids become contract ids in Phase 3)
- [x] Quote storage: `assets/sutra_quotes_300.json` (300 verses, `stt` 1..300 without gaps — checked
      by `SutraQuoteJsonTest.bundled_asset_has_300_unique_quotes`), `core/database/SutraQuoteEntity.kt`,
      `SutraQuoteDao.kt`, `data/quote/RoomSutraQuoteRepository.kt` + `SutraQuoteJson.kt`, seeded in
      `InnerLampApplication.onCreate` and again on an empty read (AC29, AC30)
- [x] `domain/usecase/SutraQuoteUseCase.quote(date)`: `dayOfYear % total` as the 0-based position in
      `sequence` order, i.e. quote number `dayOfYear mod 300 + 1` — the BR-STR-10 rule (AC29; the vector
      test is Phase 4, no caller uses it yet — Phase 2)

## Phase 1 — Domain

- [ ] `domain/model/SutraContent.kt` (rewrite) + `domain/model/SutraFilter.kt`: `SutraKind`,
      `SutraTradition`, `SutraPurpose`, `SutraLicense`, `SutraDeityRole` (each with `Unknown`),
      `SutraSummary`, `SutraVersion`, `SutraDetail`, `SutraVersionDetail`, `SutraSection`,
      `SutraDeityLink`, `SutraPage`, `SutraLine`, `SutraLinesPage`, `SutraTopic`, `SutraDuration`,
      `SutraQuery`, `ScriptureFontSize` (design.md) (has code, drift: sample `SutraDetail` /
      `SutraChapter` / `SutraBadge`)
- [ ] `domain/repository/SutraRepository.kt` + `SutraNotFoundException`,
      `SutraTranslationNotFoundException` (AC10, AC24)
- [ ] `domain/repository/ReadingPreferencesRepository.kt` (AC22)
- [ ] `domain/usecase/GetSutraPageUseCase.kt` (trim, cut to 100 characters, empty → no term — AC6),
      `GetSutraDetailUseCase.kt` (rewrite), `GetSutraLinesUseCase.kt` — `Result`, `DispatcherProvider`
- [ ] `domain/usecase/ObserveScriptureFontSizeUseCase.kt`, `SetScriptureFontSizeUseCase.kt` (AC22)
- [ ] `domain/sutra/SutraDefaultVersion.kt`: BR-STR-05 (Vietnamese versions, translation → chant →
      meaning → poem, ties by position; none Vietnamese → first version in source order, ⚠️ product,
      AC17)
- [ ] `domain/sutra/SutraQuoteNumber.kt` (BR-STR-10) and make `SutraQuoteUseCase.quote` call it
      (AC29)
- [ ] `domain/sutra/SutraLinePaging.kt`: `pageOf`, `pagesToLoad` (AC18, AC20)
- [ ] `domain/sutra/SutraRequest.kt`: `SutraQuery` → request parameters (AC5, AC6)
- [ ] Delete `randomQuote()` / `randomQuotes()` from `SutraQuoteUseCase` and their tests once Phase 2
      no longer calls them (AC29)
- [ ] Delete `domain/model/SutraCatalog.kt`, `domain/model/ReaderContent.kt`, the sample `Sutra` /
      `SutraTone` rows of `domain/model/LibraryContent.kt`, `domain/usecase/GetSutraCatalogUseCase.kt`,
      `GetSutraReaderUseCase.kt`, the sutra rows of `GetLibraryCatalogUseCase.kt` once Phase 2 no
      longer uses them (has code, drift: sample data, on-device filtering and search — drift
      BR-STR-03, BR-STR-04)

## Phase 2 — UI (`il-ui-builder`)

- [ ] `ui/screens/sutralist/SutraFormat.kt` (shared by the three screens): kind, tradition, ten purpose
      labels, duration chips and "khoảng N phút", licence labels (UNKNOWN → no line), role labels, version
      names, the temporary state copy marked ⚠️ product (AC2, AC5, AC8, AC12, AC14, AC21)
- [ ] List: rewrite `SutraListUiState` + `SutraListViewModel` — paging, chips through the server,
      debounced search, `collectLatest`, `SavedStateHandle` (AC1, AC4–AC7) (has code, drift: on-device
      filtering, sample catalogue, no paging)
- [ ] `SutraListScreen`: line "{total} bản kinh", two chip rows with the full topic set (Tất cả, Hằng ngày,
      ten purposes, three traditions) and "Tối đa 5 / 15 / 30 / 60 phút", card per AC2 (no audio
      badge), loading / three empty states / error with "Thử lại" / retry at the end of the list, hide
      "Gửi yêu cầu bản kinh" (AC1–AC3, AC8, AC9) (has code, drift: Bắc / Nam only, "Dưới 15 phút" style
      chips, audio badge, dead request button)
- [ ] Sutra page: rewrite `SutraUiState` + `SutraViewModel` (no default id) and `SutraScreen` — hero,
      facts, versions with source link and licence label, unreviewed notice, linked deities, "Đọc
      kinh" bar; loading / not found / error; remove the progress card, "Tụng theo", play circle and
      "Tải về máy" (AC10–AC16) (has code, drift: one fixed sample page, simulated progress)
- [ ] Reader: rewrite `ReaderUiState` + `ReaderViewModel` — Flow C (open, scroll, jump, switch),
      `SavedStateHandle` (AC17–AC21, AC24, AC25) (has code, drift: two fixed verses, simulated player)
- [ ] `ReaderScreen` (+ `ReaderToc.kt`, `ReaderFontSheet.kt`, `ReaderVersionSwitch.kt`): `LazyColumn`
      of `totalLines` items with placeholders, section headings, text as received, "Mục lục" only with
      sections, version switch only with > 1 version, retry row for a failed page, remove the player
      bar, speed chip, progress, teacher line and the share / download chips (AC18–AC21, AC23, AC24)
- [ ] "Aa" sheet with the four sizes; text size per `ScriptureFontSize` (proposed 18 / 21 / 24 / 28 sp,
      "Vừa" = today's 21 sp — match the prototype); background from the app theme (AC22)
- [ ] Navigation: `ReaderRoute(sutraId, translation: String? = null)`; `onDeity(id)` → `BuddhaRoute` on
      the sutra page; drop `onDownload` / `onPlayer` / `onShare` of both screens; `onTextSettings` opens
      the font sheet instead of `SettingsRoute` (AC15, AC22, AC23)
- [ ] Library block: `LibraryViewModel` / `LibraryUiState` / `LibraryScreen` "Kinh thường tụng" from
      `GetSutraPageUseCase(daily = true, page = 1)` with its own loading / error / empty state + "Thử
      lại"; remove the sample rows (AC33) (has code, drift: four sample rows with sample ids)
- [ ] Settings: `SettingsViewModel` / `SettingsUiState` read and write the font size through the two
      use cases; `languageSummary` keeps following the value (AC22)
- [ ] Quote by date: `HomeViewModel` (initial date, each picked day, resume on a new date), `CalViewModel`
      ("today" card), `LunarDetailViewModel` (`epochDay`) call `SutraQuoteUseCase.quote(date)`; keep
      `HomeSampleData.verses` only as the fallback (AC27, AC28, AC30, AC31) (has code, drift:
      `randomQuote()` and `HomeUiState.verseFor(lunar)`)
- [ ] `@Preview`s for the stateless parts with `UiState` built directly (no sample repository in `main`)

## Phase 3 — Data (`il-data-builder`, all three operations are in the contract)

- [ ] Confirm the three operations and the parameters in innerlamp-be `docs/specs/api/openapi.yaml`
      @ a8a016b (done while writing this spec); re-check when the `Contract:` line changes — cite it,
      never copy the schema
- [ ] `data/remote/api/SutraApiService.kt`: `getList`, `getDetail`, `getLines` — public, no
      `@Authenticated` (AC1, AC10, AC17, AC32)
- [ ] `data/remote/dto/SutraDto.kt`: wire enums as `String`; `audio_url` declared, never used; `duration_minutes`
      and `han_translation_year` nullable
- [ ] `data/mapper/SutraMapper.kt`: `""` → `null`, unknown values → `Unknown`, `titles.vi`, version order,
      `UNREVIEWED` → not reviewed (AC2, AC3, AC11–AC13)
- [ ] `data/repository/SutraRepositoryImpl.kt` through `NetworkClient.safeCall`; `SUTRA_NOT_FOUND` /
      `TRANSLATION_NOT_FOUND` → the two exceptions, the rest `NetworkException` per the Error → UI
      table
- [ ] `core/datastore/UserPreferences.kt` + `data/repository/ReadingPreferencesRepositoryImpl.kt`: key
      `scripture_font_size_index`, default index 1 (AC22)
- [ ] `di/SutraModule.kt`: API service, repositories, use cases, ViewModel factories, Library block
      source; wire `HomeScreen` / `CalScreen` / `LunarDetail` factories to the quote use case

## Phase 4 — Tests

- [ ] Vectors: extend `scripts/sync-product-vectors.sh` (planned by `../Calendar/` Phase 4 and
      `../Memorial/` Phase 5; create it if still absent) to copy innerlamp-be
      `docs/product/sutra/rules/{sutra-default-version,sutra-daily-quote,sutra-browse,sutra-search-match}.json`
      into `app/src/test/resources/product/sutra/rules/` with a `SOURCE` line (commit + product spec
      version); `SutraProductVectorsTest` fails when a file's `product_spec_version` differs from
      `SOURCE` (AC35)
- [ ] `sutra-default-version.json`: run **every case** (6) through `SutraDefaultVersion.pick`, with the list
      position as the ordinal (AC35, BR-STR-05)
- [ ] `sutra-daily-quote.json`: run **every case** (8) through `SutraQuoteNumber.of`, then through
      `SutraQuoteUseCase.quote` over the real bundled file and check that the returned quote's
      `sequence` equals `quote_number` (AC35, BR-STR-10)
- [ ] `sutra-browse.json` and `sutra-search-match.json`: for **every case** (13 + 13) build the `SutraQuery`
      from `topic`, `max_minutes` and `term` and assert the request parameters
      (`daily` / `purpose` / `tradition`, `max_minutes`, `q` trimmed and ≤ 100 characters or absent);
      the `expected` lists are the backend's job (product §4.1, ⚠️ product); plus a ViewModel test
      where a fake repository returns the texts in scrambled order and the UI shows them as received
      (AC5, AC6, AC35)
- [ ] `SutraDefaultVersion`, `SutraQuoteNumber`, `SutraLinePaging` (page 1 / 200 / 201 boundaries, the
      6,967-line text, `pagesToLoad`), `SutraQuery` rules
- [ ] Mapper: all enums + unknown values, `""` → `null`, `null` duration, version order, licence codes
- [ ] Repository (`FakeApiService` + `NetworkClient`): success, `NoConnection`, 5xx,
      `404 SUTRA_NOT_FOUND`, `404 TRANSLATION_NOT_FOUND`, `400 INVALID_FILTER`, absent parameters omitted
- [ ] `SutraListViewModel` (rewrite `SutraListViewModelTest`): first page, next page one request at a
      time, stop at the last / empty page, chip change cancels the older request, debounce, term trim and
      cut, three empty states, initial error + retry, next-page error keeps items, `SavedStateHandle`
      (AC1–AC9)
- [ ] `SutraViewModel`: loaded, not found, error + retry, reviewed / unreviewed, no deities, unknown
      licence (AC10–AC15)
- [ ] `ReaderViewModel`: open (detail → default version → page 1), scroll loads visible + next page once,
      jump to a section loads only its page, switch version resets pages, failed page keeps lines and
      retries only that page, `total = 0`, process-death restore (AC17–AC21, AC24, AC25)
- [ ] Font size: shared value between the reader and Settings, default, out-of-range value (AC22)
- [ ] Quote views: `HomeViewModel` (initial, picked day, resume on a new date, fallback), `CalViewModel`,
      `LunarDetailViewModel` (AC27, AC28, AC30, AC31)
- [ ] `SutraFormat`: labels, "khoảng N phút", licence labels (UNKNOWN → none), version names
- [ ] Measure AC26 (NFR-STR-01, longest text) and AC36 (NFR-STR-02) manually on a real device, 4G
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (run by the user — see `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Report the Android column of innerlamp-be `docs/product/sutra/product.md` §4.2 to `ba-writer` (this
      spec does not edit the backend repo), including the BR-STR-10 / FR-STR-010 note on the lunar-day
      screen and the vector wording of §4.1 (requirements.md, "Open questions (still open)")
- [ ] `Docs/home_assumptions.md` row 3 ("Câu kinh lấy từ `HomeSampleData`…"): edit or delete once Phase 2
      lands (the quote then comes from `SutraQuoteUseCase.quote(date)`; the sample verses stay only as
      the fallback) — left untouched by this spec
- [ ] OKF concept through `il-okf-writer` once the feature has been checked end-to-end

## Deferred (not blocking Sutra)

- Offline reading copy of texts (product §2.4 "may"; Android does not do it in v1.0).
- Audio, auto-scroll with a highlighted line, reading progress, bookmarks (product §1.2, Appendix A
  item 4).
- A section-at-a-time reading layout (waits on product Appendix A item 7).
- Replacing the sample id in Chua's livestream row (Chua / events screens).

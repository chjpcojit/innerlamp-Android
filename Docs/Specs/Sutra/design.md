# Design — Sutra

Version: 1.0
Last updated: 2026-10-16
Status: planned (drift) — UI for the list, the sutra page, the reading screen and the Library block exists on sample data; the contract-based domain, repository, data layer, paging, chips, search, table of contents, version switch, shared font size and the quote by date do not exist yet. The quote storage (bundled file, Room, `SutraQuoteUseCase.quote(date)`) exists.

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Three screens on one public, read-only library: the list (`SutraListScreen`), the sutra page
(`SutraScreen`) and the reading screen (`ReaderScreen`), plus the quote of the day shown by Home, the
Calendar screen and the lunar-day screen. Data goes
`ViewModel → use case → SutraRepository (domain) → SutraRepositoryImpl (data) → NetworkClient.safeCall →
SutraApiService`; no Bearer. **Online-only**: every open is a call, no Room table for texts, no offline
copy (product §2.4 says "may"; chosen not to in v1.0, like `../Deity/`). Order, filtering, searching and
paging are done by the server; the app only builds the request, shows the order it receives, and
computes three things itself: the version that opens first (`SutraDefaultVersion`, BR-STR-05), the quote
number of a date (`SutraQuoteNumber`, BR-STR-10) and the page that holds a line
(`SutraLinePaging.pageOf`). The font size is a device preference shared by the reading screen and
Settings through `PreferencesStorage`.

Current state (read on 2026-10-16): `GetSutraCatalogUseCase` (11 sample items), `GetSutraDetailUseCase`
(one fixed page) and `GetSutraReaderUseCase` (two fixed verses) return samples and ignore the id;
`GetLibraryCatalogUseCase` holds four sample sutra rows; `SutraListUiState` filters and searches on the
device; `ReaderUiState` carries a simulated player; `SettingsViewModel.fontSizeIndex` is in memory.
The quote side is real: `assets/sutra_quotes_300.json` (300 verses, `stt` 1..300), `SutraQuoteEntity` /
`SutraQuoteDao` / `RoomSutraQuoteRepository` seeded in `InnerLampApplication`, and
`SutraQuoteUseCase.quote(date)`. The closest model for the data layer: `TempleApiService` +
`TempleOptionRepositoryImpl` + `di/TempleModule.kt`; for a paged list with chips and states:
`../Deity/design.md` (catalogue) and `../Temple/design.md`.

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, thread
switching through `DispatcherProvider`, "today" through an injected `java.time.Clock`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-16.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / List screen | `ui/screens/sutralist/SutraListScreen.kt` (+ `SutraListFilters.kt` if long) | Header "Kinh sách" with the line "{total} bản kinh" (AC1); search field with "×" (AC6); two chip rows, single selection each (AC5); `LazyColumn` of cards that loads the next page when ~4 rows remain (AC4); loading / three empty states / error states (AC8, AC9); "Thử lại" at the end of the list (AC9); no "Có audio dẫn" badge, no downloaded badge (AC2); "Gửi yêu cầu bản kinh" hidden (AC8) | exists — needs major change |
| UI / List state | `ui/screens/sutralist/SutraListUiState.kt` | `topic: SutraTopic`, `duration: SutraDuration`, `query: String`, `items`, `total`, `page`, `totalPages`, `load` (initial loading / loaded / initial error / empty(kind) / loading more / more error / done); **remove** `catalog`, `visible`, `isFiltering`, `subtitle`, `countLabel`, `searchText()` (on-device filtering) | exists — rewrite |
| UI / List VM | `ui/screens/sutralist/SutraListViewModel.kt` | `SavedStateHandle` keeps `topic`, `duration`, `query` (AC7); `combine(topic, duration, debounced query).collectLatest` cancels the older request (AC5, AC6); `onEndReached()` loads the next page, one request at a time (AC4); `onRetry()` | exists — rewrite |
| UI / List format | `ui/screens/sutralist/SutraFormat.kt` (shared with `sutra/` and `reader/`) | Labels: kind, tradition, ten purposes, duration chips, "khoảng N phút" (AC2, AC5), licence labels (AC12), role labels (AC14), version names (AC21), state copy (⚠️ product, temporary defaults) | not created (labels now in `domain/model/SutraCatalog.kt`) |
| UI / Sutra page screen | `ui/screens/sutra/SutraScreen.kt` (+ `SutraSections.kt`) | Hero (title, original title, kind / tradition), facts, summary, versions with licence and source, unreviewed notice, linked deities, "Đọc kinh" bar; loading / not found / error (AC10–AC16); remove the progress card, "Tụng theo", play circle, "Tải về máy", `ChapterList` of sample chapters | exists — rewrite |
| UI / Sutra page state | `ui/screens/sutra/SutraUiState.kt` | `load` (loading / loaded / not found / error), `detail: SutraDetail?`; **remove** `progressLabel`, `progress` | exists — rewrite |
| UI / Sutra page VM | `ui/screens/sutra/SutraViewModel.kt` | `sutraId` from `SavedStateHandle` (no default id); `GetSutraDetailUseCase(id)` (Step-by-step Flow B); `onRetry()` | exists — rewrite |
| UI / Reader screen | `ui/screens/reader/ReaderScreen.kt` (+ `ReaderToc.kt`, `ReaderFontSheet.kt`, `ReaderVersionSwitch.kt`) | Header with title and the version name, "Mục lục" entry (only with sections), version switch (only with > 1 version), "Aa" opening the four-size sheet (AC22); `LazyColumn` with `totalLines` items, each line from the loaded page or a placeholder; section headings (AC19); page error row with "Thử lại" (AC24); remove `ReaderProgress`, `PlayerBar`, `PlayerCard`, `PlayButton`, `SpeedChip`, `NextVersePreview`, the "Chia sẻ câu kinh" / "Tải để nghe offline" chips (AC23) | exists — rewrite |
| UI / Reader state | `ui/screens/reader/ReaderUiState.kt` | `load` (loading / ready / not found / error), `title`, `versions: List<SutraVersion>`, `selectedKey`, `sections`, `totalLines`, `pageSize`, `pages: Map<Int, LinesPageState>` (loading / loaded(lines) / error), `fontSize: ScriptureFontSize`, `tocOpen`, `fontSheetOpen`, `firstVisibleLine`; **remove** `progress`, `currentTime`, `isPlaying`, `reader: SutraReader` | exists — rewrite |
| UI / Reader VM | `ui/screens/reader/ReaderViewModel.kt` | `sutraId`, `translation` (optional) and `firstVisibleLine` from `SavedStateHandle` (AC25); Step-by-step Flow C (open, scroll, jump, switch); observes the font size; **remove** `togglePlay()` and `DEFAULT_SUTRA_ID` | exists — rewrite |
| UI / Navigation | `ui/navigation/Routes.kt` (`SutraListRoute`, `SutraRoute(sutraId)`, `ReaderRoute(sutraId, translation: String? = null)`), wiring in `MainActivity.kt` | Keep the three routes; `ReaderRoute` gains the optional version key (not used by the first release of "Đọc kinh", kept for restoring a version after process death); remove the no-op `onDownload` / `onPlayer` / `onShare` of the sutra and reading screens; `onTextSettings` is no longer `navigate(SettingsRoute)` but opens the font sheet inside the reader; add `onDeity(id)` → `BuddhaRoute(id)` on the sutra page | exists — needs change |
| UI / Library block | `ui/screens/library/LibraryScreen.kt`, `LibraryUiState.kt`, `LibraryViewModel.kt` | "Kinh thường tụng" rows from `GetSutraPageUseCase(daily = true, page = 1)`, own loading / error / empty state + "Thử lại" (AC33); rows use `titles.vi`, "khoảng N phút", open `SutraRoute(id)`; `SutraTone` colours cycle by position | exists — needs change |
| UI / Quote on Home | `ui/screens/home/HomeViewModel.kt`, `HomeUiState.kt`, `HomeSampleData.kt` | `loadRandomVerse()` → `loadVerse(date)` with `SutraQuoteUseCase.quote(date)`, for the initial date and each day picked (AC27); `HomeUiState.verseFor(lunar)` and `HomeSampleData.verses` stay only as the fallback verse (AC30); recompute on resume when the date changed (AC31) | exists — needs change |
| UI / Quote on Calendar | `ui/screens/calendar/CalViewModel.kt` | `randomQuote()` → `quote(today)` for the "today" card (AC28, AC31) | exists — needs change |
| UI / Quote on lunar-day | `ui/screens/lunar/LunarDetailViewModel.kt`, `LunarDetailUiState.kt` | Get `SutraQuoteUseCase`; `verse` from `quote(date)` of the route's `epochDay` instead of `HomeUiState.verseFor(it.lunar)` (AC28) | exists — needs change |
| UI / Settings row | `ui/screens/settings/SettingsViewModel.kt`, `SettingsUiState.kt` | `fontSizeIndex` observed from `ObserveScriptureFontSizeUseCase`; `onFontSizeChange` calls `SetScriptureFontSizeUseCase` (AC22). Settings has no Android spec; only this row changes | exists — needs change |
| Domain / Model | `domain/model/SutraContent.kt` (rewritten), `domain/model/SutraFilter.kt` (new) | `SutraKind { Sutra, Verse, Dharani, Unknown }`, `SutraTradition { Theravada, Mahayana, Vajrayana, Unknown }`, `SutraPurpose` (ten values + `Unknown`), `SutraLicense { Cc0, CcByNc, PublicDomain, CopyAllowed, Unknown }`, `SutraDeityRole { Subject, Speaker, Mentioned, Unknown }`; `SutraSummary` (`id`, `titleVi`, `titles`, `kind`, `tradition`, `collection?`, `reference?`, `summary?`, `dailyChant`, `purposes`, `durationMinutes?`, `deities`, `versions`); `SutraVersion` (`key`, `lang`, `variant`, `translator?`, `license`, `lineCount`); `SutraDetail` (summary + `reviewed: Boolean`, `originalTitle?`, `author?`, `hanTranslator?`, `hanTranslationYear?`, `referenceNote?`, `versions: List<SutraVersionDetail>`); `SutraVersionDetail` (version + `sourceName?`, `sourceUrl?`, `licenseNote?`, `note?`, `sections`); `SutraSection` (`name`, `startLine`, `lineCount`); `SutraDeityLink` (`deityId`, `role`, `nameVi`, `thumbnailUrl?`); `SutraPage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`); `SutraLine` (`n`, `text`, `section?`); `SutraLinesPage` (`translation`, `items`, `page`, `pageSize`, `total`, `totalPages`); `SutraTopic { All, Daily, Purpose(p), Tradition(t) }`; `SutraDuration { Any, Max5, Max15, Max30, Max60 }`; `SutraQuery(topic, duration, term)`; `ScriptureFontSize { Small, Medium, Large, ExtraLarge }`. The contract's empty string → `null`; unknown wire value → `Unknown` (the item is never dropped) | not created (`SutraContent.kt` holds the old sample `SutraDetail` / `SutraChapter` / `SutraBadge`) |
| Domain / Delete | `domain/model/SutraCatalog.kt` (`SutraCatalogItem`, `SutraSect`, `SutraTagFilter`, `SutraLengthFilter`, `stripVietnamese`, `formatSutraDuration`), `domain/model/ReaderContent.kt` (`ChantVerse`, `SutraReader`), the sample `Sutra` / `SutraTone` rows in `domain/model/LibraryContent.kt`, `domain/usecase/GetSutraCatalogUseCase.kt`, `GetSutraReaderUseCase.kt`, the sample body of `GetSutraDetailUseCase.kt`, the sutra rows of `GetLibraryCatalogUseCase.kt` | Sample data and on-device filtering — **drift BR-STR-03, BR-STR-04, BR-STR-12**; delete once Phase 2 no longer uses them (`LibraryHub` and the talk card of `LibraryContent.kt` stay) | exists — delete |
| Domain / Repository | `domain/repository/SutraRepository.kt` | `getPage(query: SutraQuery, page: Int): Result<SutraPage>`, `getDetail(id: String): Result<SutraDetail>`, `getLines(id: String, translationKey: String, page: Int): Result<SutraLinesPage>`; `404 SUTRA_NOT_FOUND` → `SutraNotFoundException`, `404 TRANSLATION_NOT_FOUND` → `SutraTranslationNotFoundException`; the rest `NetworkException(AppError)` | not created |
| Domain / Repository | `domain/repository/ReadingPreferencesRepository.kt` | `val fontSize: Flow<ScriptureFontSize>`, `suspend fun setFontSize(size)` | not created |
| Domain / Repository | `domain/repository/SutraQuoteRepository.kt` | Seed, count, `quoteAt(index)`, `getAll()` over the bundled quotes | exists ✓ |
| Domain / UseCase | `domain/usecase/GetSutraPageUseCase.kt`, `GetSutraDetailUseCase.kt` (rewrite), `GetSutraLinesUseCase.kt` (replaces `GetSutraReaderUseCase`) | `operator fun invoke` → `Result`; dispatcher through `DispatcherProvider`; `GetSutraPageUseCase` trims the term and cuts it to 100 characters (empty → no term) before calling the repository (AC6) | not created |
| Domain / UseCase | `domain/usecase/ObserveScriptureFontSizeUseCase.kt`, `SetScriptureFontSizeUseCase.kt` | Thin wrappers over `ReadingPreferencesRepository` for the reader and Settings (AC22) | not created |
| Domain / UseCase | `domain/usecase/SutraQuoteUseCase.kt` | `quote(date)`: `dayOfYear % total` as the 0-based position in `sequence` order = quote number `dayOfYear mod 300 + 1` (BR-STR-10). `randomQuote()` / `randomQuotes()` have no caller after AC27–AC28; delete them in Phase 1 with their tests | exists ✓ (rule) — needs change (delete random) |
| Domain / Rules | `domain/sutra/SutraDefaultVersion.kt` | `pick(versions): String?` — among `lang == "vi"` versions: variant order translation, chant, meaning, poem, ties by position; none Vietnamese → the first version in source order (⚠️ product, AC17); empty list → `null` | not created |
| Domain / Rules | `domain/sutra/SutraQuoteNumber.kt` | `of(date: LocalDate, quoteCount: Int): Int` = `date.dayOfYear % quoteCount + 1`; `SutraQuoteUseCase.quote` calls it instead of inlining the formula | not created |
| Domain / Rules | `domain/sutra/SutraLinePaging.kt` | `pageOf(line: Int, pageSize: Int): Int` = `(line - 1) / pageSize + 1`; `pagesToLoad(firstVisible, lastVisible, pageSize, totalLines, loadedOrLoading)` → the pages to request (visible + the next one) | not created |
| Domain / Rules | `domain/sutra/SutraRequest.kt` | `SutraQuery.toParams()`: topic → `daily=true` / `purpose=<CODE>` / `tradition=<CODE>`, duration → `max_minutes`, term → trimmed, ≤ 100 characters, `null` when empty (AC5, AC6) | not created |
| Data / Remote | `data/remote/api/SutraApiService.kt` | `getList(page, tradition?, purpose?, daily?, maxMinutes?, q?)` (`@GET("v1/sutras")`), `getDetail(id)` (`@GET("v1/sutras/{id}")`), `getLines(id, translation, page)` (`@GET("v1/sutras/{id}/lines")`, no `page_size`) — return `ApiResponse<…>`, no `@Authenticated`, no `X-API-Key`; absent parameters are `null` so Retrofit omits them | not created |
| Data / Remote | `data/remote/dto/SutraDto.kt` | `@Serializable` DTOs per `openapi.yaml` (`SutraSummary`, `SutraDetail`, `SutraTranslation*`, `SutraSection`, `SutraDeity`, `SutraLine`, the list and lines pages); wire enums are read as `String` so an unknown value does not fail the parse (`JsonProvider` already has `ignoreUnknownKeys`); `audio_url` is declared but never used | not created |
| Data / Mapper | `data/mapper/SutraMapper.kt` | DTO → domain: `""` → `null`; unknown wire values → `Unknown`; `titles.vi` → `titleVi`; list position → version order (the ordinal of BR-STR-05); `UNREVIEWED` → `reviewed = false`; `SutraTopic` / `SutraDuration` → request parameters | not created |
| Data / Repository | `data/repository/SutraRepositoryImpl.kt` | Calls the API through `NetworkClient.safeCall`; maps `AppError.ApiError(code = SUTRA_NOT_FOUND / TRANSLATION_NOT_FOUND)` to the two domain exceptions, the rest to `NetworkException(AppError)`; no cache | not created |
| Data / Local | `core/datastore/UserPreferences.kt`, `data/repository/ReadingPreferencesRepositoryImpl.kt` | Key `scripture_font_size_index` (Int 0..3) over `PreferencesStorage` (`../Storage/`); out-of-range or missing → default `Medium` (index 1); `observeInt` feeds `fontSize` | `UserPreferences` exists — needs change; impl not created |
| Data / Local | `core/database/SutraQuoteEntity.kt`, `SutraQuoteDao.kt`, `data/quote/RoomSutraQuoteRepository.kt`, `SutraQuoteJson.kt`, `assets/sutra_quotes_300.json` | Bundled quotes in Room, seeded at launch and again on an empty read | exists ✓ |
| DI | `di/SutraModule.kt` | `provideSutraApiService()`, `provideSutraRepository()`, `provideReadingPreferencesRepository(context)`, the use cases, factories for `SutraListViewModel`, `SutraViewModel`, `ReaderViewModel`, and the Library block source; `DatabaseModule.provideSutraQuoteUseCase` stays | not created |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Vectors; rules; mapper; repository; ViewModels; format; `SutraListViewModelTest` (exists — tests the sample catalogue, to be rewritten) | partly exists (to be rewritten) |

## Navigation flow

```
LibraryRoute (hub tile "Kinh sách" | block link)        ──▶ SutraListRoute
LibraryRoute ("Kinh thường tụng" row ← GET /v1/sutras?daily=true) ──▶ SutraRoute(sutraId)
SutraListRoute (GET /v1/sutras, pages 1…n; chips; term)
  ├─ (tap card) ──▶ SutraRoute(sutraId)
  └─ (back) ──▶ previous screen
SutraRoute(sutraId) (GET /v1/sutras/{id})
  ├─ (Đọc kinh) ──▶ ReaderRoute(sutraId)
  ├─ (tap linked deity) ──▶ BuddhaRoute(deityId)
  ├─ (tap source link) ──▶ system browser (ACTION_VIEW)
  └─ (back) ──▶ previous screen
ReaderRoute(sutraId[, translation]) (GET /v1/sutras/{id} → GET /v1/sutras/{id}/lines, pages)
  ├─ ("Aa") ──▶ font sheet over the text (no navigation)
  ├─ ("Mục lục") ──▶ table-of-contents sheet ──▶ jump inside the list
  ├─ (version switch) ──▶ same screen, new version
  └─ (back) ──▶ SutraRoute
BuddhaRoute (../Deity/) ──(Kinh liên quan row)──▶ SutraRoute(sutraId)
```

"Kinh sách" = Sutra library; "Kinh thường tụng" = Sutras commonly chanted; "Đọc kinh" = Read;
"Mục lục" = Table of contents; "Aa" = text size.

Default transition (crossfade) as today. `SutraListViewModel` is attached to the `SutraListRoute` entry so
back from `SutraRoute` keeps the scroll position and the pages already loaded (AC7). `SutraRoute` and
`ReaderRoute` keep the registration `composable<…>` with `toRoute()` arguments; there is no default
sutra id any more.

## Step-by-step Flow A — The list: first page, chip or term change, next page

```
Step 1 — First page (this feature)
- Trigger: open SutraListRoute; or change a chip; or the debounced term changes; or "Thử lại" on the error state
- Pre-condition: topic, duration and query read from SavedStateHandle (defaults: All, Any, ""); term = SutraQuery.toParams() (trimmed, ≤ 100 characters, null when empty)
- Where: SutraListViewModel (combine(...).collectLatest — the previous request is cancelled) → GetSutraPageUseCase → SutraRepositoryImpl (safeCall)
- API: GET /v1/sutras?page=1[&daily=true | &purpose=<CODE> | &tradition=<CODE>][&max_minutes=<N>][&q=<term>]
    - Success, total > 0 → replace items, total, page = 1, totalPages; line "{total} bản kinh"
    - Success, total = 0 → empty state: library empty (no chip, no term) / no match for the chips / no match for the term (AC8)
    - Failure (any AppError, including 400 INVALID_FILTER / INVALID_PAGE) → error state + "Thử lại" (AC9); chips and field stay usable

Step 2 — Next page (this feature)
- Trigger: the last visible row is ≤ 4 from the end of the list; or "Thử lại" at the end of the list
- Pre-condition: not loading; page < totalPages; the query unchanged since Step 1 (changed → back to Step 1, cancel Step 2)
- Where: SutraListViewModel → GetSutraPageUseCase → SutraRepositoryImpl
- API: GET /v1/sutras?page=N+1 (same parameters)
    - Success → append items, page = N+1; empty items → treat as the end (AC4)
    - Failure (any error) → keep items, "Thử lại" at the end (AC9)
```

## Step-by-step Flow B — The sutra page

```
Step 1 — Load the text (this feature)
- Trigger: open SutraRoute(sutraId); or "Thử lại"
- Pre-condition: sutraId from SavedStateHandle (no default)
- Where: SutraViewModel → GetSutraDetailUseCase → SutraRepositoryImpl (safeCall)
- API: GET /v1/sutras/{id}
    - Success → load = loaded; the page of AC11–AC15; the reading action is enabled
    - Failure (404 SUTRA_NOT_FOUND) → not-found state + back (AC10; ⚠️ copy)
    - Failure (other) → error state + "Thử lại" (AC10; ⚠️ copy)

Step 2 — Open a source link or a linked deity (this feature, no backend call)
- Source link: SutraScreen → Intent(ACTION_VIEW, sourceUrl); ActivityNotFoundException caught, nothing shown (AC16)
- Linked deity: callback onDeity(deityId) → navigate(BuddhaRoute(deityId)) (AC14)
```

## Step-by-step Flow C — The reading screen

```
Step 1 — Open (this feature)
- Trigger: open ReaderRoute(sutraId[, translation]) (from "Đọc kinh", or restored after process death)
- Pre-condition: sutraId (and translation / firstVisibleLine, when restored) from SavedStateHandle
- Where: ReaderViewModel → GetSutraDetailUseCase, then GetSutraLinesUseCase → SutraRepositoryImpl (safeCall)
- API: GET /v1/sutras/{id}
    - Success → versions, sections, totalLines from line_count; selectedKey = translation ?? SutraDefaultVersion.pick(versions) (AC17); continue to the lines call
    - Failure (404 SUTRA_NOT_FOUND) → not-found state + back (AC24)
    - Failure (other) → error state + "Thử lại" (AC24)
- API: GET /v1/sutras/{id}/lines?translation=<selectedKey>&page=<pageOf(firstVisibleLine ?: 1)>
    - Success → pageSize = response.page_size (kept for every later page of this version, AC18); pages[page] = loaded; show the list with totalLines items (AC19)
    - Failure (404 TRANSLATION_NOT_FOUND) → not-found state + back (AC24)
    - Failure (other) → error state + "Thử lại" (AC24)
    - Success, total = 0 → empty state (AC24)

Step 2 — Read on (this feature)
- Trigger: the visible range of the list changes
- Pre-condition: load = ready; selectedKey set
- Where: ReaderViewModel.onVisibleRangeChanged(first, last) → SutraLinePaging.pagesToLoad → GetSutraLinesUseCase
- API: GET /v1/sutras/{id}/lines?translation=<selectedKey>&page=<P> for each page that is not loaded and not loading (visible pages + the next one), one request per page, no page_size sent
    - Success → pages[P] = loaded; placeholders on those lines become text
    - Failure (any error) → pages[P] = error; "Thử lại" where the page is missing; the lines already shown stay (AC24)
    - A page past the end (page > total_pages) is never requested; an empty `items` ends the text (AC18)

Step 3 — Jump to a section (this feature)
- Trigger: the user chooses a section in the table of contents
- Pre-condition: the selected version has sections
- Where: ReaderViewModel.onSectionChosen(section) → scroll the list to item (section.startLine - 1); Step 2 then loads pageOf(section.startLine) (and the next one)
- API: GET /v1/sutras/{id}/lines?translation=<selectedKey>&page=pageOf(startLine) — the pages before it are not requested (AC20)

Step 4 — Switch version (this feature)
- Trigger: the user chooses another version in the switch
- Pre-condition: the text has more than one version
- Where: ReaderViewModel.onVersionChosen(key) → clear pages, sections and totalLines come from the chosen SutraVersionDetail; scroll to the top; as Step 1's lines call with page 1 (AC21)
- API: GET /v1/sutras/{id}/lines?translation=<key>&page=1 (no second detail call: the versions and sections came with Step 1)

Step 5 — Change the font size (this feature, no backend call)
- Trigger: the user picks a size in the "Aa" sheet (or in Settings)
- Where: ReaderViewModel.onFontSize(size) → SetScriptureFontSizeUseCase → ReadingPreferencesRepositoryImpl → PreferencesStorage.putInt; the reader and Settings both collect ObserveScriptureFontSizeUseCase, so the text and the row update at once (AC22)
```

## Step-by-step Flow D — The quote of the day

```
Step 1 — Quote for a date (this feature, shared with Home, Calendar and lunar-day screens)
- Trigger: Home shown (initial date) or another day picked on the week strip; Calendar screen shown; LunarDetailRoute(epochDay) shown; ON_RESUME when the device date changed (AC31)
- Pre-condition: date = LocalDate.now(clock) or the selected / route date
- Where: HomeViewModel / CalViewModel / LunarDetailViewModel → SutraQuoteUseCase.quote(date) → SutraQuoteRepository (Room)
    - number = SutraQuoteNumber.of(date, count); the count is the number of rows (300 in the bundled file); position = number - 1 in `sequence` order
    - Table empty → seedIfNeeded() once, then read again
    - Success → verse = quote.toVerse() (verse text, "Kinh Pháp Cú (Dhammapada) · {chapter} · kệ {n}" attribution, translator)
    - Still empty or failure → keep the built-in fallback verse (AC30; ⚠️ product)
- API: none (bundled file; seed at launch in InnerLampApplication)
```

## Local storage

- **Texts, lists, lines: nothing is stored.** No Room table, no disk cache (online-only, see Overview).
  Images (linked deities' thumbnails): Coil's default cache.
- **Quotes:** Room table of the 300 bundled verses (exists), seeded at launch.
- **Font size:** one Int (0..3) under `scripture_font_size_index` in `PreferencesStorage` (not secret,
  device-only, product §3.5); survives restarts, cleared with the app data.
- **UI state that must survive process death:** `SavedStateHandle` — list: topic, duration, term;
  reading screen: version key and first visible line (AC7, AC25). Not stored in prefs.

## Deliberately stubbed for now

- **Product questions with a temporary default** (see the ⚠️ markers in `requirements.md`; each default
  is isolated so changing it is one edit): label and state copy (`SutraFormat`), "Gửi yêu cầu bản kinh"
  hidden, default version for Vajrayāna texts without a Vietnamese version (`SutraDefaultVersion`),
  continuous reading (the list is one `LazyColumn`; a section-at-a-time layout would restrict the
  item range to a section), the version switch and its names, the font sheet and the default size, role
  labels, Home quote of the selected day, the quote fallback, the Library block source.
- **The Library "Kinh thường tụng" block** takes `daily=true` page 1 until the `library` product spec
  decides (AC33).
- **Chua's livestream row** still passes a sample id to `SutraRoute` (outside this spec; opens the
  not-found state until fixed).
- **UI and data layer in one batch:** all three operations are in the contract, so Phase 3 is not
  blocked; no new sample data is added to `main`. `FakeSutraRepository` lives only in `app/src/test/…`;
  `@Preview` builds the `UiState` directly for the stateless parts (`SutraListContent`,
  `SutraContent`, `ReaderContent`).

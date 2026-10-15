# Tasks — Article

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — only the routes, the navigation between the three screens and the list's near-end trigger and footer lines already match the spec; everything else is `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when verified to exist **and to match what product spec v1.0 requires** in the code on
2026-10-15; an item that has code but drifts from the product stays `[ ]` and is noted "(has code,
drift: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Already exists, keep as is

- [x] `ArticleRoute(articleId)`, `ArticleListRoute` in `ui/navigation/Routes.kt`; wiring of Home ("Xem
      tất cả" (See all), cards), list → article, "Đọc tiếp" (Read next) → `ArticleRoute(id)` in
      `MainActivity.kt` (AC1, AC2, AC10, AC16 navigation part; sample ids become contract ids in Phase 3)
- [x] List: near-end trigger at 3 cards (`snapshotFlow` in `ArticleListScreen.kt`), the single-request
      guard in `loadMore()`, the copy "Đang tải thêm bài viết…" (Loading more articles…) and "Bạn đã xem
      hết bài viết" (You have seen every article) (AC6, AC7 UI part; the data source changes in Phase 2)

## Phase 1 — Domain

- [ ] `domain/model/Article.kt`: `ArticleSummary`, `ArticleDetail`, `ArticlePage` (design.md)
- [ ] `domain/repository/ArticleRepository.kt` + `ArticleNotFoundException` (AC12)
- [ ] `domain/usecase/GetFeaturedArticlesUseCase.kt`, `GetArticlePageUseCase.kt`,
      `GetArticleDetailUseCase.kt`, `GetRelatedArticlesUseCase.kt` — `Result`, `DispatcherProvider`, no
      re-sorting (AC21)
- [ ] Delete `domain/model/ArticleContent.kt`, `Article` (`domain/model/HomeContent.kt`),
      `domain/usecase/GetArticlesUseCase.kt`, `GetAllArticlesUseCase.kt` once Phase 2 no longer uses them
      (has code, drift: 5 samples cycled to 60 — drift FR-ART-008 / NFR-ART-01)

## Phase 2 — UI (`il-ui-builder`)

- [ ] `ui/screens/article/ArticleFormat.kt`: "{n} phút đọc" (n min read), "{d} tháng {m}", author line
      with missing parts dropped, "Nguồn: {name}" (Source: …), summary notice, no-body line, share text
      (AC4, AC13–AC15, AC18)
- [ ] Shared `ArticleCard` (`HomeSections.kt`) on `ArticleSummary`: Coil image per BR-ART-05 for cards +
      placeholder on `null`/error, category, title 2 lines, author · reading time, missing values hidden
      (AC4) (has code, drift: gradient placeholder, sample strings)
- [ ] Home empty block: Home article block on its own state from `GetFeaturedArticlesUseCase`, all received
      cards in order, empty block on error/empty, retry reloads only this block; remove `articles` from
      `HomeSampleData`/`HomeSectionsContent` (AC1, AC3; ⚠️ A3) (has code, drift: sample data, empty card
      shared by 3 blocks)
- [ ] List: rewrite `ArticleListUiState` + `ArticleListViewModel` — server paging, `page_size` from the
      response, subtitle "{loaded} / {total} bài", end from `totalPages`, page 1 on creation (AC5–AC7,
      AC11); remove `PAGE_SIZE = 20`, `loadDelayMs` (has code, drift: local list, pages of 20 — drift
      BR-ART-03)
- [ ] List empty block: page 1 error or `total = 0` (AC8; ⚠️ A3)
- [ ] List next-page retry: keep loaded cards, re-request row at the end (AC9; ⚠️ copy)
- [ ] Article page states: rewrite `ArticleUiState` + `ArticleViewModel` — detail ‖ related (Step 1, 2),
      loading / not found / empty block (AC12; ⚠️ copy, ⚠️ A3); no fallback to the first article; factory
      through `di/ArticleModule` (has code, drift: synchronous sample data, falls back to the first article)
- [ ] `ArticleScreen` content: Coil cover per BR-ART-05 for the page (replace `ArticleHero`), header
      category + reading time, author row with initials + date, body, quote, missing values hidden,
      no-body line (AC13, AC15) (has code, drift)
- [ ] Source attribution + summary notice; source opens in the browser only for `http`/`https`, catch
      `ActivityNotFoundException` (AC14)
- [ ] Article language: `localeList` from `language` on the article text styles; verify TalkBack reads it
      in that language during manual QA (AC17)
- [ ] Remove Save: `SaveShareRow` → a single "Chia sẻ" (Share) button; delete `isSaved`, `saveLabel`,
      `toggleSaved` (AC17) (has code, drift: in-memory Save — drift BR-ART-09)
- [ ] "Đọc tiếp" from `related` with `ArticleCard` (replace `NextArticleRow`), order received, hidden when
      empty or failed (AC16) (has code, drift: next 2 in a cycle — drift BR-ART-04)
- [ ] `ui/screens/article/ArticleShare.kt` (`ACTION_SEND` + chooser, catches
      `ActivityNotFoundException`); wire `onShare` in `MainActivity.kt` (AC18; link from Phase 4) (has
      code, drift: no-op TODO)
- [ ] Update the `ArticleListRoute` KDoc ("20 bài mỗi lượt" → 10 per page) in `Routes.kt`

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Confirm each operation in innerlamp-be `docs/specs/api/openapi.yaml` (cite, do not copy the
      schema): `articlesTop`, `articlesList`, `articlesGet` exist @ a8a016b; `articlesRelated` does
      **not** yet — planned in backend article design.md v3.0 (⚠️ contract not yet in openapi.yaml,
      backend Phase 7 — "Deliberately stubbed")
- [ ] `data/remote/api/ArticleApiService.kt`: `getTop`, `getList`, `getDetail` (no `@Authenticated`)
- [ ] `data/remote/dto/ArticleDto.kt` (nullable fields kept nullable; `tags`, `license` not declared)
- [ ] `data/mapper/ArticleMapper.kt`: blank → `null`, `published_date` → `LocalDate`, blank paragraphs
      dropped
- [ ] `data/repository/ArticleRepositoryImpl.kt` through `NetworkClient.safeCall`;
      `ARTICLE_NOT_FOUND` → `ArticleNotFoundException`; the other `AppError`s per the Error → UI table
- [ ] Related articles: `ArticleApiService.getRelated(id)` + `ArticleRepositoryImpl.getRelated` per the
      planned contract (AC16, Step 2) — ⚠️ hidden section until the backend ships it
- [ ] `di/ArticleModule.kt`: repository, use cases, ViewModel factories, the article block source for
      `HomeViewModel`
- [ ] When `articlesRelated` lands in `openapi.yaml`: update the `Contract:` line of `requirements.md`,
      remove the ⚠️ (contract) marker in `design.md`, recheck AC16

## Phase 4 — Shared links (ADR-0004; blocked on the open ⚠️)

- [ ] Web base URL per flavor: `BuildConfig.WEB_BASE_URL` + `manifestPlaceholders["webHost"]` in
      `app/build.gradle.kts` (AC18, AC19) — ⚠️ domain per environment not confirmed; do not invent a value
- [ ] `ui/navigation/WebLinks.kt`: `articleUrl(id)`, `routeFor(uri)` for `/articles/<id>` and
      `/en/articles/<id>` (AC18–AC20)
- [ ] `AndroidManifest.xml`: `autoVerify` intent filter on `MainActivity` (https, `${webHost}`,
      `pathAdvancedPattern` for the two article paths only); `launchMode="singleTop"` (AC19, AC20)
- [ ] Route the link: `MainActivity` handles `intent.data` in `onCreate` and `onNewIntent` →
      `navigate(ArticleRoute(id))`; unparseable → browser selector intent, no loop (AC19, AC20; ⚠️ back
      stack on cold start)
- [ ] Final `applicationId` per flavor + release signing certificate SHA-256 handed to the website for
      `assetlinks.json` — ⚠️ not confirmed (ADR-0004)

## Phase 5 — Tests

- [ ] `ArticleMapper` (blank → `null`, date parsing, paragraphs) and `ArticleRepositoryImpl` (success and
      each Error → UI row, 404 → `ArticleNotFoundException`)
- [ ] `ArticleListViewModel`: page 1, next page appended in order, one request at a time, end line,
      page-1 empty/error → empty block, next-page error keeps items (AC5–AC9); rewrite
      `ArticleListUiStateTest` (has code, drift: tests local 20-per-load paging)
- [ ] `ArticleViewModel`: detail ‖ related, related failure hides "Đọc tiếp" only, 404 → not found, other
      error → empty block, no Save in the state (AC12, AC16, AC17)
- [ ] Home article block: own state, error/empty → empty block, no sample cards (AC1, AC3); update
      `HomeViewModelTest` (has code: asserts `HomeSampleData.articles`)
- [ ] `ArticleFormat` and `WebLinks` (build + parse, `/en/`, extra segments, encoded ids) (AC13–AC15,
      AC18–AC20)
- [ ] Order is kept as received, never re-sorted (AC21); `article-order.json` and `article-related.json`
      do **not** run on Android (the server computes, product §4.1)
- [ ] Manual QA on a real device once Phase 4 is unblocked: link opens the app (cold and warm), not
      installed → website, unknown id → not found (FR-ART-007)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (the user runs it — see `AGENTS.md`)

## Phase 6 — Documentation

- [ ] Report the Android column status for innerlamp-be `docs/product/article/product.md` §4.2 to
      `ba-writer` (this skill does not edit the backend repo)
- [x] Remove the article parts of `Docs/home_assumptions.md` rows 1, 3, 13 (they now point here; old-docs
      cleanup step, 2026-10-15)
- [ ] Update `../Navigation/` (deep links listed as Deferred) to point at this spec's App Link handling
      when Phase 4 is built
- [ ] OKF concept via `il-okf-writer` once the feature is verified end-to-end

## Deferred (not blocking Article)

- Shared links for other features (temple, deity, calendar…): each adds its path to `WebLinks` and the
  manifest allow-list when its screen ships (ADR-0004).

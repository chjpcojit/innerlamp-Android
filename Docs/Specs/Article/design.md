# Design — Article

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — UI for the three entry points (Home block, all articles, article page) exists on 5 sample articles; the contract-based domain, repository, data layer, server paging, images, attribution, Share and verified App Links do not exist yet; `GET /v1/articles/{id}/related` is a planned backend contract (not in `openapi.yaml`, not built); the web domain and the final `applicationId` are open (ADR-0004)

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Three entry points into one public, read-only reading list: the "Bài viết Phật pháp hay" (Good Dharma
articles) block on Home (`HomeViewModel`), the "Tất cả bài viết" (All articles) screen
(`ArticleListScreen`) and the article page (`ArticleScreen`), plus a fourth way in: a shared website link
opened as a verified App Link (ADR-0004). Data goes
`ViewModel → use case → ArticleRepository (domain) → ArticleRepositoryImpl (data) → NetworkClient.safeCall →
ArticleApiService`; no Bearer. **Online-only**: every open is a call, no Room, no prefs, no offline copy,
no sample or cached content on failure (product §2.4, FR-ART-008). Order, featured, paging and related
articles are all decided by the server; the app only formats and shows.

Current state (read on 2026-10-15): `GetArticlesUseCase` holds 5 sample `ArticleDetail` (prototype
`Component.ARTICLES`), `GetAllArticlesUseCase` cycles them to 60 `Article`; `ArticleListUiState` pages
the local list 20 at a time; `ArticleUiState.from` picks "Đọc tiếp" (Read next) by cycling and holds an
in-memory `isSaved`; Home uses sample `Article` in `HomeSampleData`; the manifest has only the launcher
intent filter. The closest model for the data layer: `TempleApiService` + `TempleOptionRepositoryImpl` +
`di/TempleModule.kt`; for the paged list and the Home block: `../Deity/design.md` (same
`items/page/page_size/total/total_pages` shape, Home block on its own state). Coil 3 is already declared
in `app/build.gradle.kts`.

Code folders keep their names (`ui/screens/article/`, `ui/screens/articlelist/`, routes `ArticleRoute`,
`ArticleListRoute`; product §1.5).

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, thread switching
through `DispatcherProvider`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-15.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`ArticlesSection`, `ArticleCard`), `HomeSkeletons.kt` (`ArticlesSkeleton`) | Shows all received featured cards in order (AC1); `ArticleCard` takes `ArticleSummary`: image per BR-ART-05 for cards via Coil `AsyncImage` (placeholder on `null`/error), category, title (2 lines), "{author} · {n} phút đọc", missing values left out (AC4); empty block (AC3, ⚠️ A3) | exists — needs change |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt`, `HomeScreen.kt` | The article block gets its own state (loading / loaded / empty-error) read from `GetFeaturedArticlesUseCase`; its retry (if A3 keeps one) reloads only this block; remove `articles` from `HomeSampleData` and from `HomeSectionsContent` (same direction `../Temple/` and `../Deity/` take for their blocks) | exists — needs change |
| UI / List screen | `ui/screens/articlelist/ArticleListScreen.kt` | Keep the near-end trigger (3 cards), header "{loaded} / {total} bài", "Đang tải thêm bài viết…" and "Bạn đã xem hết bài viết" (AC5–AC7); add the empty block (AC8) and the next-page re-request row (AC9, ⚠️ copy); cards are the shared `ArticleCard` | exists — needs change |
| UI / List state | `ui/screens/articlelist/ArticleListUiState.kt` | `items`, `total`, `page`, `totalPages`, `loadState` (initial loading / loaded / initial empty-error / loading more / more error / done); `subtitle` = "{items.size} / {total} bài"; `isEnd` from `page >= totalPages`; **remove** `all`, `visibleCount`, `PAGE_SIZE = 20` | exists — rewrite |
| UI / List VM | `ui/screens/articlelist/ArticleListViewModel.kt` | Loads page 1 on creation (AC5, AC11); `loadMore()` one request at a time, `page + 1` (AC6); next-page failure keeps items (AC9); **remove** `loadDelayMs`/`delay` simulation; factory through `di/ArticleModule` | exists — rewrite |
| UI / Article screen | `ui/screens/article/ArticleScreen.kt` (+ split out `ArticleSections.kt` if long) | Sections in FR-ART-004 order (AC13); cover via Coil per BR-ART-05 for the page (replaces `ArticleHero` gradient); author row with initials + "{d} tháng {m}"; body with the `language` locale on the article text styles (`TextStyle.localeList`, AC17); quote; "Nguồn: …" + summary notice, source tappable only for `http`/`https` via `LocalUriHandler`/`ACTION_VIEW`, catching `ActivityNotFoundException` (AC14); a single "Chia sẻ" (Share) button (**remove** `SaveShareRow` save half, AC17); "Đọc tiếp" with `ArticleCard` (replaces `NextArticleRow`), hidden when empty (AC16); loading / not-found / empty-block states (AC12) | exists — needs major change |
| UI / Article state | `ui/screens/article/ArticleUiState.kt` | `loadState` (loading / loaded / not found / empty-error), `detail: ArticleDetail?`, `related: List<ArticleSummary>` (empty = hidden); **remove** `isSaved`, `saveLabel`, `nextArticles`, `from()` cycling | exists — rewrite |
| UI / Article VM | `ui/screens/article/ArticleViewModel.kt` | `articleId` from `SavedStateHandle` (no fallback to a default article); calls `GetArticleDetailUseCase` and `GetRelatedArticlesUseCase` in parallel (Step-by-step Flow); **remove** `toggleSaved`; factory through `di/ArticleModule` | exists — rewrite |
| UI / Format | `ui/screens/article/ArticleFormat.kt` | Display strings: "{n} phút đọc", "{d} tháng {m}" from `LocalDate`, "{author} · {n} phút đọc" with missing parts dropped, "Nguồn: {name}", the summary notice, the no-body line (BR-ART-07), the share text (title + link) | not created |
| UI / Share | `ui/screens/article/ArticleShare.kt` | `Intent(ACTION_SEND)` `text/plain`, `EXTRA_TEXT` = "{title}\n{link}", `Intent.createChooser`; catches `ActivityNotFoundException` (AC18) | not created |
| UI / Web links | `ui/navigation/WebLinks.kt` | Pure helper (base URL injected): `articleUrl(id)` → `{webBaseUrl}/articles/{id}` (unprefixed form, ADR-0004); `routeFor(uri)` → `ArticleRoute(id)` for `/articles/{id}` and `/en/articles/{id}` (URL-decoded, one segment), else `null` (AC19, AC20). Later features add their paths here | not created |
| UI / Navigation | `ui/navigation/Routes.kt` (`ArticleRoute(articleId)`, `ArticleListRoute`), `MainActivity.kt` | Keep both routes (update the `ArticleListRoute` KDoc "20 bài mỗi lượt" → 10 per page); wire `onShare` to `ArticleShare` with `WebLinks.articleUrl(id)` (AC18); handle `intent.data` in `onCreate` and `onNewIntent` → `WebLinks.routeFor` → `navigate(ArticleRoute(id))`, unparseable → browser (Step-by-step Flow "Open a shared link"); activity `launchMode="singleTop"` so a warm link arrives in `onNewIntent` | exists — needs change |
| Platform / Manifest | `app/src/main/AndroidManifest.xml` | Second intent filter on `MainActivity`: `android:autoVerify="true"`, `VIEW` + `DEFAULT` + `BROWSABLE`, `scheme="https"`, `host="${webHost}"`, `pathAdvancedPattern` `/articles/[^/]+/?` and `/en/articles/[^/]+/?` (minSdk 31 supports it) — the per-app allow-list of ADR-0004 (AC19, AC20) | exists — needs change |
| Platform / Build | `app/build.gradle.kts` | Per flavor: `manifestPlaceholders["webHost"]` and `BuildConfig.WEB_BASE_URL` (next to `BASE_URL` in `androidComponents`) — ⚠️ values not confirmed (ADR-0004); final `applicationId` — ⚠️ not confirmed | exists — needs change |
| Domain / Model | `domain/model/Article.kt` | `ArticleSummary` (`id`, `title`, `category?`, `author?`, `authorInitials?`, `publishedDate: LocalDate?`, `readMinutes: Int?`, `summary?`, `thumbnailUrl?`, `imageUrl?`); `ArticleDetail` (`summary: ArticleSummary`, `paragraphs: List<String>`, `quote?`, `quoteSource?`, `language?`, `sourceName?`, `sourceUrl?`); `ArticlePage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`). Empty string → `null` (BR-ART-07). Replaces `Article` in `HomeContent.kt` and `ArticleDetail` in `ArticleContent.kt` | not created |
| Domain / Delete | `domain/model/ArticleContent.kt`, `Article` in `domain/model/HomeContent.kt`, `domain/usecase/GetArticlesUseCase.kt` (5 samples), `GetAllArticlesUseCase.kt` (cycled 60) | Sample data — **drift FR-ART-008 / NFR-ART-01**; delete once Phase 2/3 run | exists — delete |
| Domain / Repository | `domain/repository/ArticleRepository.kt` | `getFeatured(): Result<List<ArticleSummary>>`, `getPage(page: Int): Result<ArticlePage>`, `getDetail(id: String): Result<ArticleDetail>`, `getRelated(id: String): Result<List<ArticleSummary>>`; 404 on detail → `ArticleNotFoundException` | not created |
| Domain / UseCase | `domain/usecase/GetFeaturedArticlesUseCase.kt`, `GetArticlePageUseCase.kt`, `GetArticleDetailUseCase.kt`, `GetRelatedArticlesUseCase.kt` | `operator fun invoke` → `Result`; dispatcher through `DispatcherProvider`; no re-sorting, no filtering | not created |
| Data / Remote | `data/remote/api/ArticleApiService.kt` | `getTop()`, `getList(page)`, `getDetail(id)`, `getRelated(id)` — return `ApiResponse<…>`, no `@Authenticated` | not created |
| Data / Remote | `data/remote/dto/ArticleDto.kt` | `@Serializable` DTOs per `openapi.yaml` (`ArticleSummary`, `ArticleDetail`, list/top envelopes' `data`); nullable fields stay nullable; `tags`, `license` not declared (not shown); the related response reuses the top `items` DTO | not created |
| Data / Mapper | `data/mapper/ArticleMapper.kt` | DTO → domain: `""`/blank → `null`; `published_date` `YYYY-MM-DD` → `LocalDate` (unparseable → `null`); `body_paragraphs` blank entries dropped | not created |
| Data / Repository | `data/repository/ArticleRepositoryImpl.kt` | Calls the API through `NetworkClient.safeCall`; `AppError.ApiError(code = ARTICLE_NOT_FOUND)` on detail → `ArticleNotFoundException`; the rest `NetworkException(AppError)`; no cache | not created |
| DI | `di/ArticleModule.kt` | `provideArticleRepository()`, the four use cases, factories for `ArticleListViewModel`, `ArticleViewModel` and the article block source of `HomeViewModel` | not created |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Mapper; repository (success + each Error → UI row); `ArticleListViewModel` (paging, single request, end, page-1 empty/error, next-page error keeps items); `ArticleViewModel` (detail ‖ related, 404, error, related failure hides section); `ArticleFormat`; `WebLinks` (build + parse, `/en/`, bad paths); Home article block; `ArticleListUiStateTest` (exists — tests local 20-per-load paging, rewrite) | partly exists (to be rewritten) |

## Navigation flow

```
HomeRoute ("Bài viết Phật pháp hay" block ← GET /v1/articles/top)
  ├─ (tap card) ──▶ ArticleRoute(articleId)
  └─ (Xem tất cả) ──▶ ArticleListRoute
ArticleListRoute (GET /v1/articles, pages 1…n)
  ├─ (tap card) ──▶ ArticleRoute(articleId)
  └─ (back) ──▶ previous screen
ArticleRoute(articleId) (GET /v1/articles/{id} ‖ GET /v1/articles/{id}/related)
  ├─ (tap a "Đọc tiếp" card) ──▶ ArticleRoute(relatedId)   (pushed, opens at its top)
  ├─ (Nguồn: …) ──▶ browser (source_url, http/https only)
  ├─ (Chia sẻ) ──▶ system share sheet (ACTION_SEND)
  └─ (back, or "back" in the not-found state) ──▶ previous screen
Verified App Link https://<web-domain>/[en/]articles/<id>  (⚠️ domain)
  └─ MainActivity (onCreate | onNewIntent) ──▶ ArticleRoute(id)   (⚠️ back stack on cold start)
```

"Xem tất cả" = See all; "Đọc tiếp" = Read next; "Nguồn" = Source; "Chia sẻ" = Share.

Default transition (crossfade) as today. `ArticleListViewModel` is attached to the `ArticleListRoute`
entry so back from `ArticleRoute` keeps the scroll position and the pages already loaded (AC10).

## Step-by-step Flow — All articles: first page, next page

```
Step 1 — First page (this feature)
- Trigger: open ArticleListRoute; process-death restore; or the empty block's retry (⚠️ A3)
- Pre-condition: —
- Where: ArticleListViewModel → GetArticlePageUseCase → ArticleRepositoryImpl (safeCall)
- API: GET /v1/articles?page=1 (articlesList)
    - Success, total > 0 → items, total, page = 1, totalPages; subtitle "{items.size} / {total} bài"
    - Success, total = 0 → empty block (AC8)
    - Failure (any AppError, including 400 INVALID_PAGE) → empty block (AC8; ⚠️ A3)

Step 2 — Next page (this feature)
- Trigger: the last visible card is within 3 of the end; or the end-of-list re-request row (AC9)
- Pre-condition: not already loading; page < totalPages
- Where: ArticleListViewModel → GetArticlePageUseCase → ArticleRepositoryImpl
- API: GET /v1/articles?page=N+1
    - Loading → "Đang tải thêm bài viết…" at the end
    - Success → append items, page = N+1; page >= totalPages or empty items → done, "Bạn đã xem hết bài viết" (AC7)
    - Failure (any error) → keep items, show the re-request row (AC9; ⚠️ copy)
```

## Step-by-step Flow — Article page: detail and "Đọc tiếp"

```
Step 1 — Load the detail (this feature)
- Trigger: open ArticleRoute(articleId) (from Home, list, "Đọc tiếp", or a link); or the empty block's retry (⚠️ A3)
- Pre-condition: articleId from SavedStateHandle
- Where: ArticleViewModel → GetArticleDetailUseCase → ArticleRepositoryImpl (safeCall)
- API: GET /v1/articles/{id} (articlesGet)
    - Success → loadState = loaded; render per AC13–AC17
    - Failure (404 ARTICLE_NOT_FOUND) → not-found state + way back (AC12; ⚠️ copy); Step 2's result is discarded
    - Failure (other AppError) → empty block (AC12; ⚠️ A3); Step 2's result is discarded

Step 2 — Related articles (this feature, runs in parallel with Step 1)
- Trigger: at the same time as Step 1 (async in viewModelScope)
- Where: ArticleViewModel → GetRelatedArticlesUseCase → ArticleRepositoryImpl
- API: GET /v1/articles/{id}/related (articlesRelated)
  ⚠️ Not confirmed: planned contract — backend article design.md v3.0 (`{items: [ArticleSummary]}` 0–2,
  404 ARTICLE_NOT_FOUND), not yet in openapi.yaml and not built; until it ships the call fails (the route does not exist on
  the current server) and "Đọc tiếp" stays hidden — no on-device fallback (BR-ART-04).
    - Success → related = items in the order received (empty → section hidden)
    - Failure (any error, including 404) → related = empty, section hidden (AC16)

Step 3 — Share (this feature, no backend call)
- Trigger: tap "Chia sẻ" on a loaded page
- Pre-condition: loadState = loaded
- Where: ArticleScreen callback → MainActivity → ArticleShare.open(context, ArticleFormat.shareText(title, WebLinks.articleUrl(id)))
    - Success → system share sheet with title + link (AC18)
    - Failure (ActivityNotFoundException) → nothing happens, no crash
  ⚠️ Not confirmed: WEB_BASE_URL per flavor (ADR-0004) — until set, the share text cannot carry a valid link.
```

## Step-by-step Flow — Open a shared link (ADR-0004)

```
Step 1 — The OS verifies and routes the link (platform, outside the app's code)
- Trigger: the user opens https://<web-domain>/articles/<id> (or /en/articles/<id>) on a device with the app
- Pre-condition: the website serves /.well-known/assetlinks.json for that domain listing this flavor's
  applicationId + signing certificate (website hand-off); the manifest filter is autoVerify and the path is in the allow-list
  ⚠️ Not confirmed: web domain per environment, final applicationId and signing certificate (ADR-0004).
    - Verified → the intent goes to MainActivity (Step 2)
    - Not verified / app not installed / in-app browser → the website page opens (FR-ART-007 fallback)

Step 2 — Parse the intent (this feature)
- Trigger: MainActivity.onCreate (cold start, intent.data != null) or onNewIntent (warm, singleTop)
- Where: MainActivity → WebLinks.routeFor(uri)
    - Parsed → ArticleRoute(id), go to Step 3
    - Not parseable (claimed path but no single id segment) → open the URL in the browser through a browser
      selector intent (Intent.makeMainSelectorActivity(ACTION_MAIN, CATEGORY_APP_BROWSER)) so it does not loop
      back into the app (AC20)

Step 3 — Navigate (this feature)
- Pre-condition: NavHost ready
- Where: MainActivity → navController.navigate(ArticleRoute(id))
  ⚠️ Not confirmed: on a cold start, whether Splash/Onboarding are skipped and what Back does (proposed: skip
  them; Back finishes the activity and returns to the source app); on a warm start the route is pushed on the current stack.
    - Then → Article page Step 1 (404 → not-found state, never another article, AC19)
```

## Local storage

- None: no Room, no prefs, no secure storage, no offline copy (online-only, see Overview). Images: Coil's
  default cache only.
- `articleId`: route arg `ArticleRoute.articleId` (back stack, survives process death).

## Deliberately stubbed for now

- **Related articles** (⚠️ planned contract — backend article design v3.0, backend `tasks.md` Phase 7):
  `ArticleApiService.getRelated` and the DTO follow the planned shape; while the server does not serve
  the route the call fails and "Đọc tiếp" is hidden. No on-device selection. When `articlesRelated`
  lands in `openapi.yaml`: update the `Contract:` line of `requirements.md`, remove the marker.
- **Web domain and app identity** (ADR-0004 open questions): `WEB_BASE_URL` / `webHost` per flavor and the
  final `applicationId` are not set; Share and App Links (Phase 4) wait for them. No placeholder domain
  is invented. The current `applicationId` `com.example.innerlamp` is a template value.
- **Copy waiting on product**: the empty block's retry action and text (A3), the not-found state, the
  next-page re-request row — `ArticleFormat` keeps the proposed defaults from `requirements.md`, clearly
  marked as temporary.
- **UI and data layer**: top, list and detail are already in the contract, so Phase 3 is not blocked for
  them — do it in the same batch as Phase 2, no new sample data added to `main`. `FakeArticleRepository`
  lives only in `app/src/test/…`; `@Preview` builds the `UiState` directly for the stateless parts
  (`ArticleContent`, `ArticleListContent`).

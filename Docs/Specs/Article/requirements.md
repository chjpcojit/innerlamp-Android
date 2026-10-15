# Requirements — Article

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — the Home block "Bài viết Phật pháp hay" (Good Dharma articles), the "Tất cả bài viết" (All articles) list and the article page already have UI, but run on 5 sample articles hardcoded in the domain (the list cycles them to 60, 20 per load); "Đọc tiếp" (Read next) is the next 2 in a cycle, the page has a Save button, "Chia sẻ" (Share) is a no-op, no images, no source attribution or summary notice, no shared-link handling. No repository/API yet.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/article/` (article page) and `ui/screens/articlelist/` (all articles); the featured block lives in `ui/screens/home/`
Product spec: innerlamp-be `docs/product/article/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ a8a016b (info.version 1.0.0) — tag Articles (`articlesTop`, `articlesList`, `articlesGet`); the related-articles read `GET /v1/articles/{id}/related` (`articlesRelated`) follows innerlamp-be `docs/specs/features/article/design.md` @ v3.0 (2026-10-15) — **planned contract, not yet in openapi.yaml and not built** (backend `tasks.md` Phase 7)
Implements: BR-ART-01, BR-ART-02, BR-ART-03, BR-ART-04, BR-ART-05, BR-ART-06, BR-ART-07, BR-ART-08, BR-ART-09, FR-ART-001, FR-ART-002, FR-ART-003, FR-ART-004, FR-ART-005, FR-ART-006, FR-ART-007, FR-ART-008, NFR-ART-01, NFR-ART-02 — FR-ART-009 is Backend (Android `n/a` in product §4.2); shared links per ADR-0004

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) of the **Dharma articles** for Android. Written in a mixed style: the current
state is read from the code on 2026-10-15 (branch `claude/project-thread-qanr2m`, based on `main` @
b024b28), the rest is prospective. Product rules (editorial order, featured count, page size, related
articles, image order, attribution, missing values, language, no Save) live in the product spec and are
**not** repeated here; this file only says how the Android app implements the `BR-ART-*`/`FR-ART-*`.

## Constraints from the product spec

- **Public, read-only** (§2.3, §2.7): no login needed; no Bearer (like `TempleApiService`).
- **The server decides order, featured, paging and related articles** (BR-ART-01, BR-ART-02, BR-ART-03,
  BR-ART-04): the app shows exactly what it receives, never re-sorts, never picks related articles on
  the device, never loads all articles at once (NFR-ART-01). The `article-order.json` and
  `article-related.json` vectors therefore run only on the backend (product §4.1).
- **Online only, no fake content** (§2.4, FR-ART-008): no Room, no offline copy, no sample or cached
  articles when loading fails — an empty block instead.
- **Vietnamese interface only** (§3.6, BR-ART-08): interface text is Vietnamese; the article content is
  shown in its own language, never translated.
- **No Save** (BR-ART-09): the Save button and its state are removed.
- **Shared links** (FR-ART-006, FR-ART-007): website URLs opened as verified Android App Links, per
  ADR-0004 (cited, not restated).

## User Story

As a visitor (signed in or not), I want to see the featured Dharma articles on Home, browse all articles
page by page, read an article with its source and two related articles, and share it as a link that
opens the app or the website, so that I can read and pass on Buddhist teachings (product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Featured articles on Home

1. WHEN Home opens, THE SYSTEM SHALL call `GET /v1/articles/top` (public) and show **all** received
   articles (up to 5) as article cards in the "Bài viết Phật pháp hay" block, in exactly the order
   received, with "Xem tất cả" (See all) opening `ArticleListRoute`. (FR-ART-001, BR-ART-01, BR-ART-02)
2. WHEN the user taps a featured card, THE SYSTEM SHALL open `ArticleRoute(articleId)` with the contract
   `id`. (FR-ART-001, FR-ART-003)
3. WHEN `top` fails (any error) or returns no article, THE SYSTEM SHALL show the block's empty block in
   place of the cards — no sample, placeholder or cached cards — without blocking Home's other blocks.
   (FR-ART-008, BR-ART-02)
   > ⚠️ Not confirmed (product): whether the empty block offers "Thử lại" (Retry) and which text it shows
   > (product Appendix A item 3) — blocks the Phase 2 tasks "Home empty block", "List empty block",
   > "Article page states"; hand back to ba-writer in innerlamp-be. Proposed default until answered:
   > keep the existing Home empty card copy ("Chưa có bài viết" (No articles yet) + body + "Thử lại"
   > reloading only this block).

### An article card (Home, all articles, "Đọc tiếp")

4. WHEN showing an article card, THE SYSTEM SHALL show the image per BR-ART-05 for cards
   (`thumbnail_url`, else `image_url`, else the placeholder; a load error → placeholder, no message), the
   category, the title on at most 2 lines and "{author} · {n} phút đọc" ({author} · {n} min read) from
   `read_minutes`, leaving out each missing value per BR-ART-07. (FR-ART-001, FR-ART-002, FR-ART-005,
   BR-ART-05, BR-ART-07, NFR-ART-02)

### All articles

5. WHEN the user opens `ArticleListRoute`, THE SYSTEM SHALL call `GET /v1/articles?page=1`, show the
   articles in exactly the order received, and show the header subtitle "{loaded} / {total} bài"
   ({loaded} / {total} articles) with `total` from the response. (FR-ART-002, BR-ART-01, BR-ART-03)
6. WHEN the user scrolls to within 3 cards of the end of the list and `page < total_pages`, THE SYSTEM
   SHALL show "Đang tải thêm bài viết…" (Loading more articles…), load the next page and append it,
   with only one page request in flight at a time; THE SYSTEM SHALL read the page size from the
   response's `page_size` (10 today) and never request the whole list. (FR-ART-002, BR-ART-03,
   NFR-ART-01, §3.6)
7. WHEN the last page has been loaded (`page >= total_pages`, or a returned page is empty), THE SYSTEM
   SHALL stop loading and end the list with "Bạn đã xem hết bài viết" (You have seen every article).
   (FR-ART-002, BR-ART-03)
8. WHEN loading page 1 fails (any error) or returns `total = 0`, THE SYSTEM SHALL show the empty block
   in place of the list, with no article card. (FR-ART-008, BR-ART-03; ⚠️ retry/copy — AC3)
9. WHEN loading a next page fails, THE SYSTEM SHALL keep every card already loaded and let the user
   request that same page again from the end of the list. (FR-ART-002)
   > ⚠️ Not confirmed (product): the text of the control that re-requests a failed next page (the
   > product scenario only says "can be requested again") — blocks the Phase 2 task "List next-page
   > retry"; hand back to ba-writer in innerlamp-be together with Appendix A item 3. Proposed default: a
   > "Thử lại" (Retry) row at the end of the list.
10. WHEN the user taps a card, THE SYSTEM SHALL open `ArticleRoute(articleId)`; back returns to the list
    with the same scroll position and the pages already loaded. (FR-ART-003, platform)
11. WHEN the app is killed by the system and reopened on the list (process death), THE SYSTEM SHALL
    reload page 1. (platform)

### The article page

12. WHEN `ArticleRoute(articleId)` opens (from Home, the list, "Đọc tiếp" or a shared link), THE SYSTEM
    SHALL show a loading state and call `GET /v1/articles/{id}` without login, then:
    - on success: show the page per AC13–AC17;
    - on failure (`404 ARTICLE_NOT_FOUND`): show a "not found" state with a way back;
    - on failure (any other error): show the empty block of FR-ART-008, with no sample or cached
      article. (FR-ART-003, FR-ART-008)
    > ⚠️ Not confirmed (product): the copy of the "not found" state (FR-ART-003 only describes the
    > intent; the prototype has no such variant) — blocks the Phase 2 task "Article page states"; hand
    > back to ba-writer in innerlamp-be. Proposed default: "Không tìm thấy bài viết" (Article not found)
    > + "Quay lại" (Go back).
13. WHEN an article page is shown, THE SYSTEM SHALL show the sections in exactly the FR-ART-004 order:
    the header with the category and "{n} phút đọc" (n min read); the cover image per BR-ART-05 for the
    page (`image_url`, else `thumbnail_url`, else the placeholder; a load error → placeholder, the text
    still shows); the title; the author row with `author_initials` and the date as "{d} tháng {m}"
    ({d} month {m}) from `published_date`; the body paragraphs as plain text; the quote with its source;
    the source attribution and summary notice (AC14); the "Chia sẻ" action (AC18); "Đọc tiếp" (AC16).
    (FR-ART-004, BR-ART-05)
14. WHEN an article page is shown, THE SYSTEM SHALL show "Nguồn: {source_name}" (Source: …) and the
    summary notice of BR-ART-06; WHEN `source_url` is an `http`/`https` URL, THE SYSTEM SHALL make the
    source tappable and open it in the browser; otherwise THE SYSTEM SHALL show the name without a link.
    (BR-ART-06)
15. WHEN a value is missing (`null` or empty string in the contract), THE SYSTEM SHALL leave it out per
    BR-ART-07 (no date, no author row, no quote block, no category label); WHEN `body_paragraphs` is
    empty, THE SYSTEM SHALL show the summary and "Bài viết chưa có nội dung chi tiết." (This article has
    no detailed content yet.). (BR-ART-07, FR-ART-004)
16. WHEN an article page is shown, THE SYSTEM SHALL call `GET /v1/articles/{id}/related` in parallel with
    the detail and show the received articles (0–2) under "Đọc tiếp" as article cards (AC4) in exactly
    the order received; tapping one opens `ArticleRoute(relatedId)` at its top; WHEN there is none, or
    the call fails, THE SYSTEM SHALL hide the "Đọc tiếp" section and leave the rest of the page
    unaffected. The app never picks related articles itself. (FR-ART-005, BR-ART-04)
    > ⚠️ Not confirmed: `GET /v1/articles/{id}/related` (`articlesRelated`) is a planned contract
    > (backend article design v3.0), not yet in `openapi.yaml` and not built — blocks the Phase 3 task
    > "Related articles"; until it ships, "Đọc tiếp" stays hidden (no on-device fallback).
17. WHEN an article page is shown, THE SYSTEM SHALL declare the article's `language` on the article
    text (title, body, quote, summary) so that hyphenation and the screen reader use it, and SHALL show
    interface text in Vietnamese; it SHALL NOT show a "Lưu" / "Đã lưu" (Save / Saved) action.
    (BR-ART-08, BR-ART-09, FR-ART-004)

### Share and shared links

18. WHEN the user taps "Chia sẻ" on an article page, THE SYSTEM SHALL open the system share sheet
    (`Intent.ACTION_SEND`, `text/plain`, through `Intent.createChooser`) with the article title and its
    InnerLamp website link `https://<web-domain>/articles/<id>` (ADR-0004); WHEN no app can receive it,
    THE SYSTEM SHALL not crash. (FR-ART-006)
    > ⚠️ Not confirmed: the website domain per environment (dev / staging / prod) used in the link
    > (ADR-0004 open question, product Appendix A item 2) — blocks the exact link (Phase 4 task "Web base
    > URL per flavor"); no value is assumed here.
19. WHEN a verified link `https://<web-domain>/articles/<id>` or `https://<web-domain>/en/articles/<id>`
    is opened on a device with the app installed, THE SYSTEM SHALL open the app on
    `ArticleRoute(<id>)` (cold start or already running), without signing in; WHEN the article no longer
    exists, THE SYSTEM SHALL show the not-found state of AC12 and never another article. (FR-ART-007,
    FR-ART-003)
    > ⚠️ Not confirmed: the web domain per environment and the final `applicationId` (still the template
    > `com.example.innerlamp`, with `.dev` / `.staging` suffixes) plus the release signing certificate
    > (ADR-0004 open questions) — blocks the verified intent filter and the website's
    > `assetlinks.json` (Phase 4); no value is assumed here.
    > ⚠️ Not confirmed: what Back does from an article opened by a link on a cold start (return to the
    > app that opened the link, or go to Home) and whether Splash/Onboarding are skipped — blocks the
    > Phase 4 task "Route the link"; Android navigation decision for the user. Proposed default: skip
    > Splash/Onboarding, Back finishes the activity and returns to the source app.
20. WHEN an opened link's path is not in the app's allow-list (anything other than `/articles/<id>` and
    `/en/articles/<id>`), THE SYSTEM SHALL not claim it; WHEN a claimed link cannot be parsed into an id,
    THE SYSTEM SHALL hand it to the browser without looping back into the app. (FR-ART-007, platform)

### Vectors

21. WHEN unit tests run, THE SYSTEM SHALL have tests proving the app keeps the received order and never
    re-sorts or picks related articles; `article-order.json` (BR-ART-01) and `article-related.json`
    (BR-ART-04) do **not** run on Android (the server computes both, product §4.1). (BR-ART-01,
    BR-ART-04)

### Current code state per AC (read on 2026-10-15)

| AC | Status | Notes |
|---|---|---|
| 1 | drift | `HomeSampleData.load()` (fake 900 ms delay) returns 5 sample `Article`; skeleton exists; "Xem tất cả" → `ArticleListRoute` ✓ |
| 2 | ✓ navigation part | `onArticle(id)` → `ArticleRoute(id)` in `MainActivity.kt`; the id is a sample id (`art-1`…) |
| 3 | drift | Home empty card exists ("Chưa có bài viết" + "Thử lại"), but error and empty are shared with the other blocks (`Docs/home_assumptions.md` #13) |
| 4 | drift | `ArticleCard` (shared Home/list) shows a gradient `PlaceholderImage`, no real image (`home_assumptions.md` #1); `readTime` is a sample string |
| 5 | drift | Subtitle "{visible} / {all} bài" exists but counts 60 cycled samples |
| 6 | drift | Near-end trigger (3 cards), "Đang tải thêm bài viết…" and the single-request guard exist; pages of **20** (`PAGE_SIZE`) on a local list — **drift BR-ART-03 / NFR-ART-01** |
| 7 | ✓ copy | "Bạn đã xem hết bài viết" exists; end computed from the local list |
| 8, 9 | missing | No empty block, no next-page error (local list cannot fail) |
| 10 | ✓ | `ArticleListViewModel` lives on the `ArticleListRoute` entry |
| 11 | missing | Rebuilt from sample data |
| 12 | missing | `ArticleUiState.from` falls back to the first article when the id does not match; no loading, no not-found, no error |
| 13 | drift | Order matches except: hero is a gradient placeholder; date/reading time are sample strings; Save/Share row instead of Share only |
| 14 | missing | No source attribution, no summary notice — **drift BR-ART-06** |
| 15 | missing | Every field is non-null in `ArticleDetail`; nothing is hidden |
| 16 | drift | "Đọc tiếp" = the next 2 in a cycle (`(index + k) % 5`), rows without image — **drift BR-ART-04** |
| 17 | drift | No language declared; Save button "Lưu" ↔ "Đã lưu" (`toggleSaved`) — **drift BR-ART-09** |
| 18 | missing | `onShare = {}` in `MainActivity.kt` (TODO) |
| 19, 20 | missing | `AndroidManifest.xml` has only the launcher intent filter; no intent handling in `MainActivity` |
| 21 | missing | `ArticleListUiStateTest` tests the local 20-per-load paging — to be rewritten |

## Data & API

The source of truth is `openapi.yaml` (and backend article `design.md` v3.0 for the related read, not
yet in the contract); schemas are not copied here. Every operation is public (no `@Authenticated`).

| Action | Operation (openapi) | Auth | Stored locally | Notes |
|---|---|---|---|---|
| Featured articles (Home) | `GET /v1/articles/top` (`articlesTop`) | public | none | In the contract (at most 5) |
| All articles, page by page | `GET /v1/articles?page=` (`articlesList`) | public | none | In the contract; 10 per page; a page past the end is `200` with no items |
| Article page | `GET /v1/articles/{id}` (`articlesGet`) | public | none | In the contract; `404 ARTICLE_NOT_FOUND` |
| "Đọc tiếp" | `GET /v1/articles/{id}/related` (`articlesRelated`) | public | none | ⚠️ planned contract (backend article design v3.0): `{items: [ArticleSummary]}` 0–2, `404 ARTICLE_NOT_FOUND`; not in `openapi.yaml`, not built |
| Images | `thumbnail_url` / `image_url` | public | Coil image cache | Use the URL exactly as returned; `null` → next fallback (BR-ART-05) |
| Open the source | `source_url` | — | none | Browser (`ACTION_VIEW`), `http`/`https` only (AC14) |
| Share | — (`ACTION_SEND`) | — | none | Title + website link (AC18, ADR-0004) |
| Open a shared link | — (verified App Link) | — | none | AC19, AC20 |

## Error → UI mapping

| Operation | Failure case | User sees | Code status |
|---|---|---|---|
| `GET /v1/articles/top` | Any `AppError` (`NoConnection`, `Timeout`, 5xx, `Serialization`, `Unknown`) or 200 with no items | The Home block's empty block, no cards (AC3; ⚠️ retry/copy A3) | drift (shared empty card) |
| `GET /v1/articles` page 1 | Any error, or `total = 0` | Empty block in place of the list (AC8; ⚠️ retry/copy A3) | missing |
| `GET /v1/articles` page ≥ 2 | Any error | Loaded cards kept; re-request control at the end (AC9; ⚠️ copy) | missing |
| `GET /v1/articles/{id}` | `404 ARTICLE_NOT_FOUND` | "Not found" state + way back (AC12; ⚠️ copy) | missing (falls back to the first article) |
| `GET /v1/articles/{id}` | Any other error | Empty block, no article content (AC12; ⚠️ retry/copy A3) | missing |
| `GET /v1/articles/{id}/related` | Any error (including 404) or no items | "Đọc tiếp" hidden, rest of the page unchanged (AC16) | missing |
| (image) | Load error | Placeholder, no message (AC4, AC13) | missing (no image loading) |
| (device) | No app receives `ACTION_SEND` / no browser for the source | Nothing happens, no crash (AC14, AC18) | missing |

## Out of scope

- Save / bookmark, saved list (BR-ART-09); search, category/tag filters, reading progress, font size,
  text to speech, offline reading, the "Chia sẻ câu kinh" (share verse) screen (product §1.2).
- Showing tags: FR-ART-004 does not list them on the article page, so Android does not show them.
- An English interface (§3.6: apps are Vietnamese only).
- Loading article content (FR-ART-009 — Backend).
- Deferred deep links, short links, link analytics (ADR-0004).
- Shared links for any other feature (each feature adds its own path to the allow-list when its screen
  ships, ADR-0004).

## Pending (not yet implemented)

- Domain: contract-based models, repository, use cases; remove the sample articles — `tasks.md` Phase 1.
- Home block on its own state, list with server paging and empty block, article page per FR-ART-004
  (images, attribution, notice, missing values, language, no Save), "Đọc tiếp" from the server, Share —
  Phase 2.
- Data layer for the four operations; `articlesRelated` waits for the backend — Phase 3.
- Verified App Links and the web link per flavor (domain and `applicationId` open) — Phase 4.
- Tests — Phase 5.
- Clean up the article parts of `Docs/home_assumptions.md` rows 1, 3, 13 (a later PR, not this spec
  change) — Phase 6.

## Open questions (resolved)

Per product spec v1.0 (2026-10-15) and the Android decisions of 2026-10-15 — these points replace the
current behaviour of the Android code:

- ~~5 sample articles cycled to 60, pages of 20~~ → server data, Home shows the 5 featured, the list
  loads 10 per page with infinite scroll and the end line (BR-ART-02, BR-ART-03, FR-ART-002, §3.6) →
  AC1, AC5–AC7. Resolved 2026-10-15.
- ~~"Đọc tiếp" = next 2 in a cycle~~ → the related API's 0–2 items (BR-ART-04, FR-ART-005) → AC16.
  Resolved 2026-10-15.
- ~~Save button~~ → removed (BR-ART-09) → AC17. Resolved 2026-10-15.
- ~~Share is a no-op~~ → title + website link through the share sheet; links open the app by verified
  App Links (FR-ART-006, FR-ART-007, ADR-0004) → AC18–AC20. Resolved 2026-10-15.
- ~~Sample data when loading fails~~ → empty block, no fake or cached content (FR-ART-008) → AC3, AC8,
  AC12. Resolved 2026-10-15.
- ~~Interface language~~ → Vietnamese only; content in its own language (BR-ART-08, §3.6) → AC17.
  Resolved 2026-10-15.

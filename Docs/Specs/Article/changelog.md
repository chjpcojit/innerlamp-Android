# Changelog — Article Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.1 (2026-10-15)

Step 4 of the docs pipeline: retire the old article notes now that product spec v1.0 and the four
platform specs are approved. No requirement or design change.

- `Docs/home_assumptions.md`: the article parts of rows #1, #3, #13 are removed and replaced with a
  pointer to this spec; the Buddha / temple parts stay.
- `tasks.md` (1.0 → 1.1): the Phase 6 cleanup item is ticked. `requirements.md` (1.0 → 1.1): the matching
  Pending item is struck through, and the stale `#1` reference in the status table is dropped.
  `design.md` (1.0 → 1.1): no change needed, verified — header only.

## v1.0 (initial) (2026-10-15)

- First Android spec of the Dharma articles, written in a mixed style: the current state read from the
  code on 2026-10-15 (`ui/screens/article/`, `ui/screens/articlelist/`, `ui/screens/home/`,
  `domain/model/ArticleContent.kt`, `domain/model/HomeContent.kt`, `domain/usecase/GetArticlesUseCase.kt`,
  `GetAllArticlesUseCase.kt`, `ui/navigation/Routes.kt`, `MainActivity.kt`, `AndroidManifest.xml`,
  `app/build.gradle.kts`, `Docs/home_assumptions.md` rows 1, 3, 13 — read, not edited), the rest
  prospective, per product spec innerlamp-be `docs/product/article/product.md` v1.0 (implements
  BR-ART-01…09, FR-ART-001…008, NFR-ART-01, NFR-ART-02; FR-ART-009 is Backend) and ADR-0004.
- `requirements.md`: 21 EARS criteria; a current-state table (drift: 5 samples cycled to 60, pages of
  20, "Đọc tiếp" (Read next) by cycling, Save button, Share no-op, no images, no attribution or notice,
  no link handling); Data & API, Error → UI.
- `design.md`: online-only (no Room, no cache, no sample content on failure); `ArticleRepository` + four
  use cases; `ArticleSummary`/`ArticleDetail`/`ArticlePage` replace the sample models; Home article block
  on its own state; `ArticleShare` + `WebLinks` + verified App Link intent filter per flavor; three
  Step-by-step Flows (list paging; article page detail ‖ related + share; opening a shared link).
- `tasks.md`: Phase 0 (already exists: routes, navigation, list trigger and footer copy) → Phase 1–6;
  Phase 4 (shared links) blocked on the ADR-0004 open questions; `article-order.json` and
  `article-related.json` do not run on Android.
- Product spec / contract: product v1.0; contract innerlamp-be `openapi.yaml` @ a8a016b (info.version
  1.0.0) for top / list / detail; `GET /v1/articles/{id}/related` (`articlesRelated`) per backend
  article `design.md` v3.0 — planned, not in openapi.yaml, not built.
- Hand-offs: backend — ship `articlesRelated` and add it to `openapi.yaml`; website — serve
  `assetlinks.json` per environment once the domain and Android identity exist (ADR-0004); `ba-writer` —
  empty-block retry and text (Appendix A item 3), not-found copy, next-page re-request copy; open with
  the owner — web domain per environment, final `applicationId` and signing certificate (ADR-0004),
  back stack for a link opened on a cold start.

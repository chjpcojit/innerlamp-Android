# Changelog — Sutra Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (initial)

- Android layer-2 spec of the sutra library ("Kinh sách"), written retroactively from the running code
  (read on 2026-10-16, `main` @ 642ef1b) and checked against innerlamp-be product spec
  `docs/product/sutra/product.md` v1.0 (feature code `STR`) and `openapi.yaml` @ a8a016b (tag Sutras).
  Android had no sutra spec before.
- Scope: the list (`sutralist`), the sutra page (`sutra`), the reading screen (`reader`), the Library
  "Kinh thường tụng" block, the quote of the day on Home / Calendar / lunar-day screens, and the shared
  font-size preference. Implements BR-STR-01…13, FR-STR-001…011, NFR-STR-01…02; FR-STR-012 and
  NFR-STR-03 are Backend.
- Current state: the screens run on sample data (11 catalogue items, one fixed sutra page, two fixed
  verses, a simulated player); only the routes, the navigation and the quote storage match. All
  product §4.2 `planned` / `drift` Android cells are `[ ]` tasks with a Phase. Design choices: online
  only, server-side order / filters / search / paging, the app computes the opening version
  (BR-STR-05), the quote number (BR-STR-10) and the line page; continuous reading over an indexed
  `LazyColumn`; font size shared through `PreferencesStorage`.
- Vectors: `sutra-default-version` and `sutra-daily-quote` run through the rules; `sutra-browse` and
  `sutra-search-match` check the request that is built (Android filters and searches on the server).
- Hand-offs: product questions (new and carried) to `ba-writer` in innerlamp-be — listed in
  `requirements.md` "Open questions (still open)" and in the AC markers; the product §4.2 note on the
  Android lunar-day screen is inaccurate. Old Android docs to clean up later: `Docs/home_assumptions.md`
  row 3. Backend: none required; the `openapi.yaml` examples for `license` show free text while the
  schema is a code enum.
- Reason: new spec after the sutra product spec v1.0 merged (2026-10-15).

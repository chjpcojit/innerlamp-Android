# Changelog — Temple Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.2 (2026-10-15)

Adopts product spec temple v1.2: anh Đức's answers to the three product questions this spec raised.
All four files are rewritten in English per the Language policy in `CLAUDE.md` (a Vietnamese doc that is
edited is translated in the same change); apart from the points below, the content is unchanged.

- `requirements.md` (1.1 → 1.2): header cites product @ v1.2. AC3 — a failed `top` now shows the same
  empty state as an empty list, with no error text, no retry and no sample temples (FR-TPL-001, new
  scenario); the ⚠️ (product) marker is removed and the `top` row of Error → UI and the AC3 state row are
  updated. AC21 — "Kinh sách" is always shown with "Đang cập nhật", never hidden or "Không có"
  (BR-TPL-06, FR-TPL-007); ⚠️ (product) removed. AC22 — architecture style, materials, highlights and
  photo set stay visible with "Đang cập nhật". AC27 — the "Rằm & Mùng Một" row shows the normal hours
  when the temple has no holiday hours (BR-TPL-09); ⚠️ (product) removed. The three markers move to
  "Open questions (resolved)" dated 2026-10-15.
- `design.md` (1.1 → 1.2): Home block and Home state rows drop the error/retry state (failure → empty);
  Temple screen and Format rows render the later fields as "Đang cập nhật" and the Rằm row with the
  normal hours; `TempleOpenStatusRule` row notes the fallback; temple-page Step 2 replaces its ⚠️
  (product) with the resolved behavior; "Deliberately stubbed" gains a "Later fields" item; Navigation
  flow notes failure → empty state. No step added or removed.
- `tasks.md` (1.1 → 1.2): Phase 1 `TempleOpenStatusRule`, Phase 2 "Featured block", "TempleScreen" and
  "Hours card", Phase 4 open-status vector run and `HomeViewModel` test reworded to the resolved
  behavior; the ⚠️ references to these three questions are removed. Every affected task stays `[ ]`;
  no task ticked.
- Product spec / contract: product temple v1.2 (FR-TPL-001, BR-TPL-06, BR-TPL-09, FR-TPL-007; no new
  IDs, no vector change); contract unchanged (`openapi.yaml` @ 0eb7d1f).
- Still open: ⚠️ (product) display labels for `type`/`sect`/`heritage_rank` (AC4); ⚠️ (product)
  wording for errors / not found / no-match hint (AC7); ⚠️ location-permission timing (AC31);
  ⚠️ (contract) search/filter/pages of 12, the new detail fields and the `HH:MM - HH:MM` format.
- Hand-offs: none new; the backend contract hand-offs of v1.0 remain.
- Reason: product spec temple v1.2 (2026-10-15) answers the featured-block failure, "Kinh sách" and
  Rằm-row questions.

## v1.1 (2026-10-15)

Step 4 of the docs process: remove the old temple docs after product spec v1.1 and the four platform
specs were approved. No change to requirements or design.

- `Docs/home_assumptions.md`: removed the temple part of lines #1, #3, #13, replaced by a pointer to
  this spec; the Buddha / article parts stay.
- `tasks.md` (1.0 → 1.1): the Phase 5 `home_assumptions.md` cleanup item ticked `[x]`.
  `requirements.md` (1.0 → 1.1): the matching Pending item. `design.md` (1.0 → 1.1): no change needed,
  verified — header only.

## v1.0 (initial) (2026-10-15)

- First Android spec for the temple directory, written in mixed mode: the current state read from the
  code on 2026-10-15 (`ui/screens/home/`, `templelist/`, `temple/`, `domain/usecase/GetTemples*`,
  `TempleOption*`), the rest prospective, per product spec innerlamp-be
  `docs/product/temple/product.md` v1.1 (implements BR-TPL-01…09, FR-TPL-001…009, NFR-TPL-01,
  NFR-TPL-02; FR-TPL-005 via `../Onboarding/`; FR-TPL-010 is Backend).
- `requirements.md`: 36 EARS criteria; current-state table (drift: sample data, "Tây Nguyên", on-device
  search/filter, no paging, follow / offering, old temple-page layout); Data & API, Error → UI.
- `design.md`: online-only (no Room); a new `TempleRepository` next to `TempleOptionRepository`;
  `domain/temple/` for BR-TPL-07, BR-TPL-09; port `LocationProvider` + `core/location/` using
  `LocationManager`; two Step-by-step Flows (directory: first page / next page; temple page: load,
  hours, location permission, distance).
- `tasks.md`: Phase 0 (already there: short list, routes) → Phases 1–5; runs the
  `temple-open-status.json`, `temple-distance.json` vectors.
- Product spec / contract: product v1.1; contract innerlamp-be `openapi.yaml` @ 0eb7d1f (info.version
  1.0.0) — missing `q`/`region`/pages of 12 and the new detail fields.
- Hand-offs: backend — `q`, `region`, `INVALID_FILTER`, pages of 12, `holiday_operating_hours`,
  `sangha_body`, `contacts`, `content_updated_month`, the `HH:MM - HH:MM` hours format into
  `openapi.yaml` (Appendix B items 1, 2, 4; backend v2.0 AC 5, 39–46); `ba-writer` — display labels for
  type/sect/heritage-rank codes, error/not-found/hint wording, featured-block failure, the "Kinh sách"
  item, the Rằm row without holiday hours.
- Reason: product spec temple v1.1 (2026-10-15) asked for the Android spec `Docs/Specs/Temple/`.

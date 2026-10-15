# Changelog — Deity Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.1 (2026-10-15)

- `requirements.md`: translated to English; no requirement change.
- `design.md`: translated to English; no design change.
- `tasks.md`: translated to English; no task change.
- Product spec / contract: unchanged (product v1.0; contract innerlamp-be `openapi.yaml` @ a8a016b).
- Hand-offs: none.
- Reason: Translated to English per the repo Language policy; no requirement, design or task change.

## v1.0 (initial) (2026-10-15)

- First Android spec of the Chư Phật & Bồ Tát (Buddhas & Bodhisattvas) catalogue, written in a mixed
  style: the current state read from the code on 2026-10-15 (`ui/screens/buddha/`, `ui/screens/home/`,
  `ui/screens/library/`, `ui/screens/mala/`, `domain/model/BuddhaProfile.kt`, `domain/usecase/GetBuddha*`,
  `ui/navigation/Routes.kt`, `MainActivity.kt`, `Docs/home_assumptions.md` #1, #13 — #11 was removed
  earlier), the rest prospective, per product spec innerlamp-be `docs/product/deity/product.md` v1.0
  (implements BR-DTY-01…08, FR-DTY-001…010, NFR-DTY-01, NFR-DTY-02; FR-DTY-011 is Backend).
- `requirements.md`: 31 EARS criteria; a current-state table (drift: 9 sample profiles, "Phật tương lai"
  (Future Buddha), no Chư Thiên (devas), on-device filtering, no pagination, via computed by an on-device
  lunar scan, no Hình tượng (iconography), the mala does not receive the recitation phrase, share is a
  no-op, "Báo thông tin sai" (Report wrong information)); Data & API, Error → UI.
- `design.md`: online-only (no Room); `DeityRepository` + four use cases; `Deity*` models replace
  `BuddhaProfile`/`BuddhaFigure`; uses `CalendarCountdown`/`LunarDay` of `../Calendar/`; `MalaRoute`
  receives `mantra`; two Step-by-step Flows (catalogue: first page / next page; a deity's page: detail ‖
  related sutras, actions, date change on resume).
- `tasks.md`: Phase 0 (already exists: routes, navigation) → Phase 1–5; countdown vector through
  `../Calendar/`; `deity-order.json`, `deity-next-via.json` do not run on Android.
- Product spec / contract: product v1.0; contract innerlamp-be `openapi.yaml` @ a8a016b (info.version
  1.0.0) for the existing part; page size 12, `date`, `INVALID_DATE`, `next_via`, `via_events` per backend
  deity `requirements.md` v3.0 (contract not yet in openapi.yaml, backend Phase 9).
- Hand-offs: backend — bring deity v3.0 into `openapi.yaml`; `ba-writer` — Library block empty copy,
  not-found / error copy for a deity's page, ngày vía (a deity's commemoration day) in a leap month, a via
  day with no occurrence, label for an unknown type, sharing when there is no short mantra, a link in the
  share text (Appendix A item 3), related-sutra order (FR-DTY-007 vs `deitiesSutras`), the sutra secondary
  line, related-sutra-specific errors.
- Reason: product spec deity v1.0 (2026-10-15) requires the Android spec `Docs/Specs/Deity/`.

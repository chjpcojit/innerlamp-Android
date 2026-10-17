# Tasks — LightLamp

Version: 1.0
Last updated: 2026-10-17
Status: not started — every item `[ ]`; verified against the code on 2026-10-17 (no lamp code exists)

> See `changelog.md` in this folder for the version history of this spec.

Every item traces to a row of `design.md`'s Layers & components table or a Step-by-step Flow step.
`[x]` only when verified in the code. Paths are relative to `app/src/main/java/com/example/innerlamp/`.
Backend work (the 3.0.0 contract) is not tracked here; see the report hand-offs.

## Phase 1 — Domain

- [ ] `domain/model/Lamp.kt`: `LampType`, `LampIntention`, `LampDuration`, `LampDraft`, `LitLamp`, `GardenLamp`, `GardenPage` (AC6, AC17–19)
- [ ] `domain/lamp/LampInputRules.kt`: trim, NFC, code-point count, recipient ≤ 100, prayer ≤ 300, blank → none (AC8, BR-LMP-06/07)
- [ ] `domain/lamp/LampRemainingLabel.kt`: `Days(n) | UnderOneDay | Out` from the end moment and now (AC26, BR-LMP-12)
- [ ] `domain/repository/LampRepository.kt` (interface) + `LampLimitReachedException`, `LampInvalidInputException`
- [ ] `domain/usecase/lamp/LightLampUseCase.kt` (Flow Step 2–3) and `GetLampGardenUseCase.kt` (garden Flow Step 1–3)

## Phase 2 — UI (`il-ui-builder`)

- [ ] Blocked by design: the Thắp đèn screens and the lamp artwork are not in the prototype (design.md "Deliberately stubbed"); until they exist build a plain Material3 layout inside `InnerLampTheme` and mark it
- [ ] `ui/screens/lamp/LampScreen.kt` + `LampUiState.kt` + `LampViewModel.kt`: form with defaults, visibility notice, hide-name switch, igniting, confirmation with two actions, "Thắp thêm đèn" (AC5–6, AC9–14), in-flight guard (AC10), `SavedStateHandle` (AC15)
- [ ] `ui/screens/lamp/LampArtwork.kt`: three lamp types, unlit and lit
- [ ] `ui/screens/lamp/LampCopy.kt`: all copy of product §3.1.1, "Tất cả chúng sanh", "Của bạn", chip names, labels, end-date formatter in the device time zone
- [ ] `ui/screens/lamp/LampGardenScreen.kt` + `LampGardenUiState.kt` + `LampGardenViewModel.kt`: header, chips with counts, cards keyed by id, "Xem thêm đèn", empty and error states, way back to lighting, filter with `collectLatest`, label recompute on resume (AC16–26)
- [ ] Guest introduction on both screens: use `AuthGate` / `SignInPrompt` from `../Login/` when it exists, else a minimal local introduction (AC3)
- [ ] `ui/navigation/Routes.kt`: `LampRoute`, `LampGardenRoute`
- [ ] `MainActivity.kt`: register both routes; change the four `IlBottomTab.Lamp -> navigate(SetupRoute)` handlers to `navigate(LampRoute)` (AC1); leave `HomeCallbacks.onSetup`, `ChuaCallbacks.onMeditate`, `TalkCallbacks.onMeditate` as they are (AC2)
- [ ] Decide with design whether `LampRoute` shows the bottom nav (design.md ⚠️); record the answer in design.md
- [ ] `data/repository/FakeLampRepository.kt` + `di/LampModule.kt` wiring the fake until Phase 3 lands (record it in design.md "Deliberately stubbed")

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Confirm each operation in innerlamp-be `docs/specs/api/openapi.yaml`: `POST /v1/lamps` and `GET /v1/lamps` exist at 2.1.1; the optional / nullable `recipient` and `is_mine` are ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0) — wait for 3.0.0, do not assume the shape; then update the `Contract:` line of requirements.md
- [ ] `data/remote/api/LampApiService.kt` (both `@Authenticated`) + `data/remote/dto/LampDto.kt`
- [ ] `data/mapper/LampMapper.kt`: instants, missing recipient → none, unknown lamp type dropped
- [ ] `data/repository/LampRepositoryImpl.kt` via `NetworkClient.safeCall`; map the limit and validation errors to the domain exceptions and the rest to the Error → UI rows (AC13–14, AC22, AC24)
- [ ] `core/network/interceptor/LoggingInterceptor.kt`: skip `/v1/lamps` like `/v1/auth/` (AC30)
- [ ] `di/LampModule.kt`: swap the fake for `LampRepositoryImpl`; delete `FakeLampRepository`

## Phase 4 — Tests

- [ ] `LampInputRulesTest`, `LampRemainingLabelTest`: scenarios beyond the vectors (Vietnamese decomposed text, 24 h boundaries)
- [ ] Extend `scripts/sync-product-vectors.sh` (planned by `../Calendar/` Phase 4 and `../Memorial/` Phase 5; create it if still absent) to copy innerlamp-be `docs/product/lamp/rules/{lamp-remaining-label,lamp-input-validation}.json` into `app/src/test/resources/product/lamp/rules/` with a `SOURCE` line (commit + product spec version); `LampProductVectorsTest` fails when a file's `product_spec_version` differs from `SOURCE` (AC31)
- [ ] `LampProductVectorsTest` — `lamp-remaining-label.json`: run **every case** (11) through `LampRemainingLabel`, `kind` and `days` compared (AC31, BR-LMP-12)
- [ ] `LampProductVectorsTest` — `lamp-input-validation.json`: run **every case** (26) through `LampInputRules`, comparing validity, rejected field, kept recipient and prayer, and blank recipient → "all beings" (AC31, BR-LMP-01/02/06/07). `lamp-lifetime.json` and `lamp-lighter-name.json` are not run on Android (the backend decides; the app displays what it receives)
- [ ] `LampViewModelTest`: defaults, choices, validation message without a request, in-flight guard, `201` and `200` both lit, limit / rejected / network failures keep the form, "Thắp thêm đèn" keeps type, intention, duration (AC5–14)
- [ ] `LampGardenViewModelTest`: first page, filter cancels the older request and keeps total and counts, load more appends and drops a duplicate id, no cursor reuse across filters, empty, error and more-error states, Out cards not drawn (AC16–26)
- [ ] `LampRepositoryImplTest` for success and each Error → UI row; `LampMapperTest`; guest test (no session → no call, AC3)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (flavored build, see `CLAUDE.md`; the user runs it — see `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Report the Android column status for innerlamp-be `docs/product/lamp/product.md` §4.2 to `ba-writer` (this repo never edits the backend repo); FR-LMP-001 changes from "Android spec says the center button opens meditation" once Phase 2 ships
- [ ] Clean up `../Navigation/` (requirements, design, tasks): remove the lamp-button → meditation lines listed in design.md "Legacy to clean up"; point the lamp button to `../LightLamp/`
- [ ] Fix `ui/screens/signup/LegalSheet.kt` line 283 (prayer privacy text) with the product owner's wording — a code change, outside this docs-only spec
- [ ] Correct the design handoff notes listed in design.md "Legacy to clean up"
- [ ] OKF concept via `il-okf-writer` once the feature is verified end-to-end

## Deferred (not blocking LightLamp)

- English locale: copy of the messages and the anonymous label (product Appendix A, unconfirmed).
- Sharing a lit lamp (product Appendix A, unconfirmed).
- "Thỉnh đèn tại chùa" and payment: the future `offering` feature.

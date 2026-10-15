# Tasks — Deity

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — only the routes and the navigation between screens already match the spec; everything else is `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when verified to exist **and to match what product spec v1.0 requires** in the code on
2026-10-15; an item that has code but drifts from the product stays `[ ]` and is noted "(has code,
drift: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Already exists, keep as is

- [x] `BuddhaListRoute`, `BuddhaRoute(buddhaId)` in `ui/navigation/Routes.kt`; wiring of Home ("Xem
      tất cả" (See all), cards), Library ("Xem tất cả", cards, hub tile `LibraryHub.Buddhas`), catalogue
      → a deity's page in `MainActivity.kt` (AC1–AC3, navigation part; sample ids become contract ids in
      Phase 3)
- [x] "Lịch lễ" (Festival calendar) on a deity's page → `FestivalsRoute` (`MainActivity.kt`, AC17
      navigation part)

## Phase 1 — Domain

- [ ] `domain/model/Deity.kt`: `DeityType` (including `Deva`, `Unknown`), `DeitySummary`, `DeityVia`,
      `DeityViaEvent`, `DeityIconography`, `DeityMantras`, `DeityDetail`, `DeityPage`, `RelatedSutra`
      (design.md); `LunarDay` shared with `../Calendar/` (create it if it does not exist yet)
- [ ] `domain/repository/DeityRepository.kt` + `DeityNotFoundException` (AC14)
- [ ] `domain/usecase/GetFeaturedDeitiesUseCase.kt`, `GetDeityPageUseCase.kt`, `GetDeityDetailUseCase.kt`,
      `GetRelatedSutrasUseCase.kt` — `Result`, `DispatcherProvider`, `today` from `Clock` (AC28)
- [ ] Confirm `domain/calendar/CalendarCountdown.kt` already exists from `../Calendar/` Phase 1 before
      building the via card (AC17) — do not create a separate copy
- [ ] Delete `domain/model/BuddhaProfile.kt`, `BuddhaFigure` (`domain/model/HomeContent.kt`),
      `domain/usecase/GetBuddhaProfilesUseCase.kt`, `GetBuddhaDetailUseCase.kt` once Phase 2 no longer
      uses them (has code, drift: 9 sample profiles, 400-day lunar scan — drift BR-DTY-05/06)

## Phase 2 — UI (`il-ui-builder`)

- [ ] `ui/screens/buddha/DeityFormat.kt`: BR-DTY-01 type label ("Chư Thiên" (Devas); `Unknown` → no
      label — AC5, AC6), "Vía dd/MM âm lịch" (Via dd/MM lunar) (⚠️ leap month), "THÁNG GIÊNG/n/CHẠP"
      (first / n-th / twelfth lunar month), solar date + weekday, countdown label from
      `CalendarCountdown`, "Xem đủ N đại nguyện" (See all N great vows) / "Thu gọn" (Collapse), share
      text (AC24)
- [ ] Home block: `HomeViewModel` reads `GetFeaturedDeitiesUseCase`, the block's own state, "Thử lại"
      (Retry) reloads only the block; card per AC5 (Coil image + placeholder, `nameVi`, via from
      `nextVia`); remove `buddhas` from `HomeSampleData` (AC1, AC4) (has code, drift: sample data, empty
      card shared by 3 blocks, "Phật tương lai" (Future Buddha))
- [ ] Library block: `LibraryViewModel`/`LibraryUiState` read `GetFeaturedDeitiesUseCase` async;
      loading / loaded / empty-error + "Thử lại" in `BuddhaStrip` (AC2, AC4; ⚠️ empty copy) (has code,
      drift: synchronous from sample data, no empty state)
- [ ] Catalogue: rewrite `BuddhaListUiState` + `BuddhaListViewModel` — pagination (AC7, AC8, AC12),
      chips through the server + `collectLatest` (AC9), `SavedStateHandle` (AC13) (has code, drift:
      on-device filtering, subtitle counts all samples, no pagination)
- [ ] `BuddhaListScreen`: 2-column `LazyVerticalGrid` that loads the next page, subtitle N from `total`,
      card with `coreVirtue` + via, `buddhasEmpty` state ("Chưa có thông tin chư Phật" (No information on
      the Buddhas yet), hint, "Thử lại", "Xem lịch lễ vía" (See the via festival calendar) →
      `FestivalsRoute`; subtitle "Chưa có dữ liệu" (No data yet); hide/keep chips per AC10/AC11), "Thử
      lại" at the end of the list (AC5, AC7–AC12)
- [ ] A deity's page: rewrite `BuddhaDetailUiState`/`BuddhaDetailViewModel` — load detail ‖ related
      sutras (Step 1, 2), loading / not found / error states (AC14; ⚠️ copy), `onResume()` date change
      (Step 4, AC29); factory through `di/DeityModule` (has code, drift: synchronous sample data, falls
      back to the first deity)
- [ ] `BuddhaDetailScreen` — cover + names (add `titleHonorific`, the Chinese-character name; the header
      uses `nameVi`), `coreVirtue` + `canonicalSource` line (AC15); the biography shows "Đọc thêm" (Read
      more) only when it overflows (AC16); remove the sample `vowIntro` (has code, drift)
- [ ] Via card from `nextVia` + `CalendarCountdown` countdown + `viaEvents` list (AC17, AC18; ⚠️ a ngày
      vía (a deity's commemoration day) with no occurrence) (has code, drift: one sample via day +
      `extraVia`, self-computed countdown)
- [ ] Great vows: fixed "Xem đủ N đại nguyện" label, numbered from 1, hidden when empty (AC19) (has
      code, drift: label from the sample `vowUnit`, count label "· trích" (· excerpt))
- [ ] New **Hình tượng** (Iconography) section: posture, throne / mount animal, sacred artifacts,
      meaning; hide missing lines/section (AC20)
- [ ] "Thần chú & danh hiệu" (Mantras & epithets): three cards Danh hiệu (Epithet) / Thần chú (Mantra) /
      Đà-la-ni (Dharani), remove `note`, hide missing cards; **one** button row in the Thần chú card,
      "Trì niệm" (Recite) only when there is a short mantra (AC21, AC22; ⚠️ Share when there is no short
      mantra) (has code, drift: two buttons on each sample card)
- [ ] "Kinh liên quan" (Related sutras) from `sutras` in the order received, row = title + `collection`
      (⚠️ secondary line), hidden when empty or on error (AC25, AC26) (has code, drift: sample
      `BuddhaSutraRef`, `phapcu` fallback)
- [ ] Footer with only "Nội dung do Ban biên tập tổng hợp từ kinh điển" (Content compiled by the
      editorial board from the scriptures) (AC27) (has code, drift: "· Báo thông tin sai" (· Report wrong
      information) — drift BR-DTY-08)
- [ ] Niệm Phật (Buddha-name recitation) receives the recitation phrase: `MalaRoute` →
      `data class MalaRoute(val mantra: String? = null)` + `composable<MalaRoute>`; `MalaViewModel` reads
      `SavedStateHandle`, `beads = 0`; Home passes `null`; `onChant` in `MainActivity.kt` passes the
      Sino-Vietnamese short mantra (AC23) (has code, drift: not passed — TODO in `MainActivity.kt`)
- [ ] `ui/screens/buddha/DeityShare.kt` (`ACTION_SEND` + chooser, catches `ActivityNotFoundException`);
      wire `onShare` in `MainActivity.kt` (AC24; ⚠️ link) (has code, drift: no-op)
- [ ] `onSutra(id)` → `SutraRoute(id)` with the contract id (AC25)

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Confirm each operation in innerlamp-be `docs/specs/api/openapi.yaml` (cite, do not copy the
      schema): `deitiesTop`, `deitiesList` (`page`, `type`), `deitiesGet`, `deitiesSutras` exist
      @ a8a016b; `date`, `INVALID_DATE`, page size 12, `next_via`, `via_events` do **not** yet — per
      backend deity requirements.md v3.0 (⚠️ contract not yet in openapi.yaml, backend Phase 9 —
      "Deliberately stubbed")
- [ ] `data/remote/api/DeityApiService.kt`: `getTop`, `getList`, `getDetail`, `getSutras` (no
      `@Authenticated`)
- [ ] `data/remote/dto/DeityDto.kt` (the v3.0 part nullable/default empty; `lunar_memorial_days` not
      declared) + a minimal DTO for the related-sutra item
- [ ] `data/mapper/DeityMapper.kt`: unknown `type` → `Unknown`, `""` → `null`, `solar_date` →
      `LocalDate`, `DeityType` → the `type` parameter
- [ ] `data/repository/DeityRepositoryImpl.kt` through `NetworkClient.safeCall`; `date = today`;
      `DEITY_NOT_FOUND` → `DeityNotFoundException`; `getRelatedSutras` merges all pages; the other
      `AppError`s per the Error → UI table
- [ ] `di/DeityModule.kt`: repository, use cases, ViewModel factories, the Buddha block source for
      Home/Library
- [ ] When backend deity v3.0 lands in `openapi.yaml`: update the `Contract:` line of `requirements.md`,
      remove the ⚠️ (contract) marker in `design.md`, recheck AC5, AC7, AC8, AC17, AC18, AC28

## Phase 4 — Tests

- [ ] Countdown vector: `CalendarProductVectorsTest` of `../Calendar/` (runs every case of
      `docs/product/calendar/rules/countdown.json`, BR-CAL-13, through the shared
      `scripts/sync-product-vectors.sh`) must be green before enabling the via card; this spec does not
      copy any further vector (AC31). `deity-order.json`, `deity-next-via.json` do not run on Android
      (the server picks, product §4.1)
- [ ] `DeityFormat`: labels for the three types + `Unknown`, "Vía dd/MM âm lịch", tháng Giêng/Chạp
      (first / twelfth lunar month), countdown today / tomorrow / n days left, great vow label, share
      text with / without Sanskrit
- [ ] Mapper: `""` → `null`, unknown `type`, `next_via` `null`, unparseable date, empty `via_events`
- [ ] Repository: success, 404 `DEITY_NOT_FOUND`, `NoConnection`, 5xx, `400 INVALID_DATE`; related
      sutras over several pages, error midway; `date` is sent as exactly the `Clock` date
- [ ] `BuddhaListViewModel`: first page, next page with one request, stop at the last page, chip change
      cancels the old request and returns to page 1, `total = 0` ("Tất cả" (All) / chip), first-page
      error, later-page error keeps the list, `SavedStateHandle` (AC7–AC13)
- [ ] `BuddhaDetailViewModel`: loaded / 404 / error; a related-sutra error does not block the page; the
      countdown uses exactly the `today` that was sent; resume on the same day does not reload, on a
      different day reloads (AC14, AC17, AC25, AC26, AC29)
- [ ] `HomeViewModel` + `LibraryViewModel`: Buddha block loaded / empty / error + "Thử lại" (AC1, AC2,
      AC4)
- [ ] `MalaViewModel`: with `mantra` → that recitation phrase, 0 beads; without → `DefaultMantra` (AC23)
- [ ] Remove `GetBuddhaDetailUseCaseTest` (tests the lunar scan — behaviour removed per BR-DTY-05)
- [ ] Measure AC30 (NFR-DTY-02) manually on a real device, 4G
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (run by the user — see `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Report the Android column of innerlamp-be `docs/product/deity/product.md` §4.2 to `ba-writer`
      (this spec does not edit the backend repo)
- [ ] Remove the Buddha parts of `Docs/home_assumptions.md` #1 (Buddha statue images — replace with a
      pointer to this spec) and #13 (the Buddha block); line #11 was removed earlier by `../Calendar/`
- [ ] OKF concept through `il-okf-writer` once the feature has been checked end-to-end

## Deferred (not blocking Deity)

- An offline reading copy (product §3.6 "may") — only if the product owner wants it.
- The "Chư Thiên" chip if the product decides on it (Appendix A item 5).
- Opening the right sutra by backend id — the sutra feature's spec/data layer.
- Saving the Niệm Phật bead count across sessions — the practice feature.

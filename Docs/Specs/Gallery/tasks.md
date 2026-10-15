# Tasks — Gallery

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — only the route, the Home → gallery navigation, the chip labels and the absence of removed actions already match the spec; everything else is `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
`[x]` only when verified to exist **and to match what product spec v1.0 requires** in the code on
2026-10-15 (`main` @ b024b28); an item that has code but drifts from the product stays `[ ]` and is
noted "(has code, drift: …)". Re-checked for product spec v1.1 (2026-10-15): its changes (FR-GAL-006,
FR-GAL-007, FR-GAL-008, BR-GAL-05, §2.7) touch none of the `[x]` items; the changed items below are `[ ]`.
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Already exists, keep as is

- [x] `PhotosRoute` in `ui/navigation/Routes.kt`, registered with `ilComposable(PhotosRoute)` in
      `MainActivity.kt`; back → `popBackStack()` (AC5, navigation part)
- [x] Home `PhotosSection`: every tile and "Xem tất cả" (See all) call `HomeCallbacks.onPhotos` →
      `PhotosRoute` (`MainActivity.kt`) (AC5)
- [x] Chip row "Tất cả · Chùa · Tượng Phật · Cảnh thiền · Lễ hội" (All · Temples · Buddha statues ·
      Meditation scenes · Festivals) in BR-GAL-01 order, one selected, "Tất cả" first
      (`PhotosScreen.kt` `PhotosHeader`, `PhotoCategory`) (AC2; the enum is replaced in Phase 1, the
      labels stay)
- [x] No favourite, wallpaper or share action in the lightbox (AC27) — keep it that way in the new
      viewer

## Phase 1 — Domain

- [ ] `domain/model/GalleryPhoto.kt`: `GalleryTopic` (with `Unknown`), `GalleryCredit`, `GalleryPhoto`,
      `GalleryPage`, `GalleryFeatured` (`total: Int?`) (design.md)
- [ ] `domain/gallery/GalleryHomeMore.kt` — BR-GAL-07 per `rules/gallery-home-more.json` (AC4, AC31)
- [ ] `domain/repository/GalleryRepository.kt` (`getFeatured`, `getPage`)
- [ ] `domain/repository/DevicePhotoLibrary.kt` + `PhotoNotSavedException` (AC23)
- [ ] `domain/usecase/GetFeaturedPhotosUseCase.kt`, `GetGalleryPageUseCase.kt`,
      `SavePhotoToDeviceUseCase.kt` — `Result`, `DispatcherProvider`
- [ ] Delete `domain/model/PhotoContent.kt` and `domain/usecase/GetTemplePhotosUseCase.kt` once Phase 2
      no longer uses them (has code, drift: 12 sample photos, `heightDp` masonry — drift BR-GAL-02/05)

## Phase 2 — UI (`il-ui-builder`)

- [ ] `ui/screens/photos/GalleryFormat.kt`: topic labels + uppercase heading (`Unknown` → none, AC29),
      subtitle "{total} ảnh" / "{total} ảnh · {topic}", counter "{n} / {total}", credit
      "Ảnh: {author} · {license}", messages: load error "Không tải được danh sách ảnh. Vui lòng thử lại
      sau." + "Thử lại", "Chưa có ảnh nào trong chủ đề này.", Home empty "Chưa có ảnh nào.",
      "Đã lưu vào thư viện ảnh", "Không lưu được ảnh. Vui lòng thử lại." (AC6–AC8, AC11, AC12, AC15,
      AC17, AC19, AC21, AC23)
- [ ] Title "Ảnh đẹp Phật giáo" (Buddhist photos) on the gallery header and the Home block; KDoc of
      `PhotosRoute` (AC1) (has code, drift: "Ảnh chùa đẹp" — drift BR-GAL-01)
- [ ] Home block: `HomeViewModel` reads `GetFeaturedPhotosUseCase` into its own `GalleryBlockState`
      (Loading / Shown / Error / Empty); `PhotosSection` shows up to 3 `AsyncImage` thumbnails, "+N ảnh"
      from `GalleryHomeMore` only when shown, skeleton while loading; remove `photoMore` /
      `HomeSampleData.PHOTO_MORE` (AC3, AC4) (has code, drift: placeholders, constant "+9")
- [ ] Home block error state: title kept, the load-error message + "Thử lại" in place of the tiles, no
      overlay; `HomeViewModel.retryPhotos()` reloads only `top`, the other blocks untouched (Home flow
      Step 1, AC6) — new in v1.1, replaces "hidden on error"
- [ ] Home block empty state: "Chưa có ảnh nào." in place of the tiles, no overlay (Home flow Step 1,
      AC7) — new in v1.1, replaces "hidden when empty"
- [ ] Rewrite `PhotosUiState` + `PhotosViewModel`: pagination shared by grid and viewer (grid flow
      Steps 1–3), chips through the server + `collectLatest` + revert on failure (AC11, AC14),
      `SavedStateHandle` for the topic (AC16), viewer index, saving flag, one-shot messages; factory
      through `di/GalleryModule` (has code, drift: on-device filtering, synchronous sample data)
- [ ] Grid states in `PhotosScreen`: `LazyVerticalGrid` 2 columns with next-page trigger, 4:3
      `AsyncImage` tiles (`ContentScale.Crop`, placeholder on error), initial loading, initial error +
      "Thử lại" (load-error copy), empty, end-of-grid loading / error + "Thử lại", snackbar host (AC8–AC10,
      AC12, AC13, AC15) (has code, drift: masonry with varying heights — drift BR-GAL-05)
- [ ] `ui/screens/photos/PhotoViewer.kt` replacing `PhotoLightbox`: `HorizontalPager` over `total`,
      loading placeholder for pages not loaded yet, 4:3 large `AsyncImage`, counter, topic heading,
      title, place, previous / next hidden at the ends, `BackHandler`, background tap no longer closes
      (AC17, AC18; 4:3 crop, never letterboxed) (has code, drift: one placeholder photo, no counter, no paging)
- [ ] Viewer paging past the loaded photos: prefetch at `index >= photos.size - 3`; failure → the viewer
      stays on the current photo (counter unchanged), snackbar with the load-error message, next stays
      offered and moving next retries the load (viewer flow Step 2, AC18, AC19)
- [ ] Close → `LazyGridState.scrollToItem(lastIndex)` (viewer flow Step 5, AC20)
- [ ] Credit row in the viewer (hidden when `credit == null`) + `ui/screens/photos/GalleryIntents.kt`
      (`ACTION_VIEW`, `ActivityNotFoundException` caught; no / blank / non-http(s) source link → credit
      still shown, not tappable) (AC21, AC22)
- [ ] "Lưu ảnh" (Save photo) button: disabled while saving, snackbar "Đã lưu vào thư viện ảnh" only on
      success, "Không lưu được ảnh. Vui lòng thử lại." on failure (viewer flow Step 3, AC23, AC25)
- [ ] Inject a `FakeGalleryRepository` / `FakeDevicePhotoLibrary` only in tests; `@Preview`s build the
      `UiState` directly — no sample data left in `main`

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Confirm each operation in innerlamp-be `docs/specs/api/openapi.yaml` (cite, do not copy the
      schema): `galleryTop`, `galleryList` (`page`, `type`) exist @ a8a016b; list-item `author`,
      `license`, `source_url` and top `total` do **not** yet — backend gallery requirements.md v2.0
      AC 3, AC 11 (⚠️ contract pending, backend Phase 6–7 — "Deliberately stubbed")
- [ ] `data/remote/api/GalleryApiService.kt`: `getTop()`, `getList(page, type)` (no `@Authenticated`,
      no detail call)
- [ ] `data/remote/dto/GalleryDto.kt` (v2.0 fields nullable, default `null`)
- [ ] `data/mapper/GalleryMapper.kt`: wire type ↔ `GalleryTopic` (unknown → `Unknown`), credit null
      when author / license blank, blank `source_url` → null, title / location unchanged (AC28, AC29)
- [ ] `data/repository/GalleryRepositoryImpl.kt` through `NetworkClient.safeCall`; every `AppError` →
      `Result.failure(NetworkException)` per the Error → UI table
- [ ] `core/media/MediaStorePhotoLibrary.kt`: Coil disk-cache bytes → `MediaStore` insert with
      `IS_PENDING`, `Pictures/InnerLamp` (AC26), `"{id}.jpg"`; delete the pending row on
      failure; no new manifest permission (viewer flow Step 3, AC23, AC24, AC26)
- [ ] `di/GalleryModule.kt`: repository, photo library, use cases, `PhotosViewModel` factory, the photo
      block source for `HomeViewModel`
- [ ] When backend gallery v2.0 lands in `openapi.yaml`: update the `Contract:` line of
      `requirements.md`, make the v2.0 DTO fields required, remove the ⚠️ (contract) markers, recheck
      AC4 and AC21 on a real build — **release gate for the viewer** (BR-GAL-04, product §2.7): no
      release build ships the viewer before this, and no per-photo `GET /v1/gallery/{id}` is added

## Phase 4 — Tests

- [ ] Vector: extend `scripts/sync-product-vectors.sh` (planned by `../Calendar/` Phase 4) to copy
      innerlamp-be `docs/product/gallery/rules/gallery-home-more.json` into
      `app/src/test/resources/product/gallery/rules/` with its `SOURCE` line; `GalleryProductVectorsTest`
      runs **every case** (5) through `GalleryHomeMore` and fails when `product_spec_version` differs
      from `SOURCE` (AC31). `gallery-order.json` does not run on Android (product §4.1)
- [ ] `GalleryFormat`: four labels + `Unknown`, uppercase heading, subtitle with / without topic,
      counter, credit text, the confirmed message copy (load error, both empty messages, saved, not
      saved)
- [ ] Mapper: four wire types + unknown, missing / blank credit fields, blank `source_url`, title and
      location unchanged
- [ ] Repository: success, `NoConnection`, 5xx, `400 INVALID_GALLERY_TYPE`, `type` omitted for
      "Tất cả", top without `total`
- [ ] `PhotosViewModel` (rewrite `PhotosViewModelTest`): first page, next page one request at a time,
      stop at the last / empty page, chip change cancels the older request, chip failure reverts and
      keeps photos, `total = 0`, initial error + retry, next-page error keeps photos, `SavedStateHandle`,
      viewer prefetch and failure (stays on the current photo, next retries), close index, save success
      / failure ("Không lưu được ảnh. Vui lòng thử lại.") / no double save, credit without a source link
      not tappable (AC8–AC26)
- [ ] `HomeViewModel`: photo block Shown with overlay / without overlay (total null, N = 0), Error with
      the load-error message and `retryPhotos()` reloading only `top`, Empty with "Chưa có ảnh nào.",
      independent of the other blocks (AC3–AC7)
- [ ] `MediaStorePhotoLibrary`: instrumented test (`app/src/androidTest/`) — success leaves one
      non-pending item, a failed copy leaves none
- [ ] Measure AC30 (NFR-GAL-03) manually on a real device, 4G
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (run by the user — see `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Report the Android column of innerlamp-be `docs/product/gallery/product.md` §4.2 to `ba-writer`
      (this spec does not edit the backend repo); the §3.6 FR-GAL-007 Android variance (AC24) is in
      product v1.1, nothing left to report for it
- [x] `Docs/home_assumptions.md` row 1: checked 2026-10-15 — it covers the Buddha-statue placeholder and
      points temple and article photos to their own specs; it says nothing about the gallery tiles, so
      no edit is needed (the Home gallery block is specified here, AC on the Home block)
- [ ] OKF concept through `il-okf-writer` once the feature has been checked end-to-end

## Deferred (not blocking Gallery)

- Masonry layout (product §1.2 "later"; needs photo dimensions in the contract).
- Restoring the open viewer after process death.

# Changelog — Gallery Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.1 (2026-10-15)

- `requirements.md`: re-cites product spec v1.1. New constraint "One load-error message" (FR-GAL-008,
  P1) and the viewer release dependency (§2.7, A2).
  - AC6: on a `top` failure the Home block is no longer hidden — it shows "Không tải được danh sách
    ảnh. Vui lòng thử lại sau." (Could not load the photo list. Please try again later.) with "Thử lại"
    (Retry) in place of the tiles; "Thử lại" reloads only this block (FR-GAL-008, P1).
  - AC7: an empty gallery shows the block with "Chưa có ảnh nào." (No photos yet.) in place of the
    tiles; ⚠️ (product) marker closed (FR-GAL-008, BR-GAL-02, P4).
  - AC12: the load-error copy is confirmed; ⚠️ (product) marker closed (Appendix A item 5).
  - AC17: viewer frame crops to 4:3, never letterboxed; ⚠️ (product) marker closed (BR-GAL-05, P3).
  - AC19: next-photos failure in the viewer = toast (snackbar) with the load-error message, stay on the
    current photo, moving next retries; ⚠️ (product) marker closed (FR-GAL-008, BR-GAL-06, P5).
  - AC21: release-gate wording aligned with product §2.7 (A2): the viewer is not released before
    backend gallery v2.0 carries the credit in every list, no per-photo credit call; the ⚠️ contract
    pending marker stays (v2.0 not built, not in openapi.yaml).
  - AC22: a photo without a source link still shows its credit, not tappable; ⚠️ (product) marker
    closed (BR-GAL-04, FR-GAL-006, P6).
  - AC23: save-failed toast "Không lưu được ảnh. Vui lòng thử lại." (Couldn't save the photo. Please
    try again.) replaces the proposed "Chưa lưu được ảnh. Vui lòng thử lại."; ⚠️ closed (FR-GAL-007, P2).
  - AC24: no permission on Android is now product §3.6 (FR-GAL-007 Android row, A3); ⚠️ closed.
  - AC26: album folder `Pictures/InnerLamp` confirmed (product A4 leaves it to this spec); ⚠️ closed.
  - AC29: unchanged, still ⚠️ Not confirmed (product), default "no topic heading" stated in the marker.
  - Error → UI table: `top` error / empty rows, page 1, chip change, next page (grid and viewer) and
    save rows carry the confirmed copy; new row "photo without a source link". "Open questions
    (resolved)" gains the v1.1 answers; the v1.0 "hidden on error" answer is marked superseded.
- `design.md`: Home `GalleryBlockState` is Loading / Shown / Error / Empty (no Hidden) with
  `retryPhotos()`; `PhotosSection` always composed; Home Step 1 gains the "Thử lại" trigger and the
  Error / Empty outcomes; viewer Step 2 failure stays on the current photo with the load-error
  snackbar; Step 1 / Step 4 / Viewer row: credit without an http(s) source shown but not tappable,
  frame never letterboxed; `GalleryFormat` holds the confirmed copy; `MediaStorePhotoLibrary` album ⚠️
  removed; navigation sketch and "Deliberately stubbed" updated (release gate per §2.7; only AC29 still
  waits on product). No step added or removed in any Step-by-step Flow.
- `tasks.md`: new `[ ]` tasks "Home block error state" (AC6) and "Home block empty state" (AC7); the
  Home block, `GalleryFormat`, grid states, viewer, viewer paging, credit, save and `MediaStorePhotoLibrary`
  tasks drop their ⚠️ and carry the confirmed behaviour; the release-gate task cites §2.7; test tasks
  for `GalleryFormat`, `PhotosViewModel` and `HomeViewModel` cover the new states and copy; the Phase 5
  report task no longer includes the §3.6 Android variance (now in product); Deferred "letterboxed
  viewer" removed. No task ticked: the `[x]` items (Phase 0, Phase 5 home_assumptions) were re-checked
  as unaffected by v1.1; nothing new is verified in code.
- Product spec / contract: product spec innerlamp-be `docs/product/gallery/product.md` v1.0 → v1.1
  (FR-GAL-006, FR-GAL-007, FR-GAL-008, BR-GAL-04, BR-GAL-05, §2.7, §3.6; P1–P6, A2–A4). Contract
  unchanged (`openapi.yaml` @ a8a016b; backend gallery v2.0 still pending).
- Hand-offs: backend — unchanged (ship gallery v2.0 and bring it into `openapi.yaml`; it now also gates
  the apps' viewer release, product Appendix B item 1). `ba-writer` — only the unknown-topic heading
  (AC29) remains open.
- Reason: product spec gallery v1.1 (2026-10-15, merged in innerlamp-be #40) answers the questions this
  spec raised against v1.0.

## v1.0.1 (2026-10-15)

- `tasks.md`: Phase 5 `Docs/home_assumptions.md` row 1 item closed — the row does not cover the
  gallery tiles, so no edit is needed. No requirement or design change. The Android gallery has no
  other superseded doc to delete.

## v1.0 (initial) (2026-10-15)

- First Android spec of the "Ảnh đẹp Phật giáo" (Buddhist photos) gallery, written in a mixed style:
  the current state read from the code on 2026-10-15 (`main` @ b024b28: `ui/screens/photos/`,
  `ui/screens/home/` `PhotosSection` + `HomeSampleData.PHOTO_MORE`, `domain/model/PhotoContent.kt`,
  `domain/usecase/GetTemplePhotosUseCase.kt`, `ui/navigation/Routes.kt`, `MainActivity.kt`,
  `Docs/home_assumptions.md` row 1), the rest prospective, per product spec innerlamp-be
  `docs/product/gallery/product.md` v1.0 (implements BR-GAL-01…09, FR-GAL-001…008, NFR-GAL-01…03;
  FR-GAL-009 is Backend).
- `requirements.md`: 31 EARS criteria; a current-state table (drift: title "Ảnh chùa đẹp", 12 sample
  photos, on-device filtering, no pagination, varying tile heights, a one-photo lightbox without
  counter, paging, credit or save, constant Home "+9"); Data & API, Error → UI.
- `design.md`: online-only (no Room); `GalleryRepository` + three use cases; `Gallery*` models replace
  `TemplePhoto`/`PhotoCategory`; the viewer is an overlay of `PhotosScreen` sharing `PhotosViewModel`;
  `GalleryHomeMore` (BR-GAL-07) in `domain/gallery/`; `DevicePhotoLibrary` port implemented with
  `MediaStore` in `core/media/` (no permission); three Step-by-step Flows (grid, viewer, Home block).
- `tasks.md`: Phase 0 (route, Home navigation, chip labels, no removed actions) → Phase 1–5;
  `gallery-home-more.json` vector through the shared sync script; `gallery-order.json` does not run on
  Android.
- Product spec / contract: product v1.0; contract innerlamp-be `openapi.yaml` @ a8a016b (info.version
  1.0.0) for `galleryTop` and `galleryList`; list-item credit and `top.total` per backend gallery
  `requirements.md` v2.0 AC 3, AC 11 (not built, not in openapi.yaml — release gate for the viewer).
- Hand-offs: backend — ship gallery v2.0 (credit in the lists, `total` on `top`) and bring it into
  `openapi.yaml`; https picture host for staging/production (product Appendix A item 1). `ba-writer` —
  gallery load-error copy (Appendix A item 5), "not saved" copy (item 4), letterbox vs crop (item 3),
  Home block when the gallery is empty, viewer behaviour when the next photos fail to load, credit
  without a source link, unknown topic heading, §3.6 Android row for FR-GAL-007 (no permission asked).
- Reason: product spec gallery v1.0 (2026-10-15) requires the Android spec `Docs/Specs/Gallery/`.

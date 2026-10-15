# Changelog — Gallery Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

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

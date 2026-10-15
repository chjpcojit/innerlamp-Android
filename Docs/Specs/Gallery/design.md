# Design — Gallery

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — UI for the Home block, the grid and a one-photo lightbox exists on 12 sample photos; the contract-based domain, repository, data layer, pagination, the 4:3 tiles, the paging viewer, the credit and "Lưu ảnh" (Save photo) do not exist yet; credit and `top.total` wait for backend gallery v2.0

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Two entry points into one public, read-only gallery: the "Ảnh đẹp Phật giáo" (Buddhist photos) block on
Home (`HomeViewModel`) and the gallery screen (`PhotosScreen`, route `PhotosRoute`) with its full-screen
viewer. Data goes `ViewModel → use case → GalleryRepository (domain) → GalleryRepositoryImpl (data) →
NetworkClient.safeCall → GalleryApiService`; no Bearer. **Online-only**: no Room, no offline copy
(product §2.4). Order, filtering and paging are done by the server; the app computes only the Home
"+N ảnh" through a pure function `domain/gallery/GalleryHomeMore` (BR-GAL-07, vector-tested).

The viewer is **not** a separate route: it is an overlay of `PhotosScreen` driven by the same
`PhotosViewModel`, so the grid and the viewer share one loaded list, one `page`/`totalPages` and one
"load next page" path (BR-GAL-06: the viewer pages through the whole filtered gallery; closing it
returns to the grid at the last photo shown).

"Lưu ảnh" goes through a domain port `DevicePhotoLibrary` implemented in `core/media/` with
`MediaStore` (no runtime permission on minSdk 31). The bytes saved are the large picture exactly as the
server serves it, taken from Coil's disk cache (fetched through Coil when not cached) — a static image
file, not an API call, so it does not go through `safeCall`, the same as every `AsyncImage`.

Current state (read on 2026-10-15): `GetTemplePhotosUseCase` holds 12 sample `TemplePhoto` with a
`heightDp` each (prototype masonry); `PhotosUiState.filteredPhotos` filters on the device;
`PhotoLightbox` shows one placeholder; Home's `PhotosSection` draws three `PlaceholderImage` tiles with
the constant `HomeSampleData.PHOTO_MORE` = 9. Coil 3 is declared in `app/build.gradle.kts` but not used
yet (plan shared with `../Temple/` and `../Deity/`). Closest model for the data layer:
`TempleApiService` + `TempleOptionRepositoryImpl` + `di/TempleModule.kt`; for a paged list with chips:
`../Deity/design.md` (same `items/page/page_size/total/total_pages` shape).

Code names keep the `ui/screens/photos/` folder and `PhotosRoute` (product §1.5); the new domain models
are named `Gallery*`, replacing `TemplePhoto`/`PhotoCategory`.

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, thread
switching through `DispatcherProvider`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-15.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`PhotosSection`), `HomeSkeletons.kt` | Title "Ảnh đẹp Phật giáo" (AC1); up to 3 tiles from `featured.items` with Coil `AsyncImage` (`thumbnailUrl`, `ContentScale.Crop`, placeholder on error) in the prototype mosaic (1 large + 2 small; fewer slots when fewer photos); "+N ảnh" overlay on the last tile only when `GalleryHomeMore` says so (AC3, AC4); every tile and "Xem tất cả" → `onPhotos` (AC5); skeleton while loading; the whole section is not composed when hidden (AC6, AC7) | exists — needs change |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt` | The photo block has its own state `GalleryBlockState` (Loading / Shown(tiles, moreCount) / Hidden) read from `GetFeaturedPhotosUseCase`, independent of the other blocks; remove `photoMore` and `PHOTO_MORE` | exists — needs change |
| UI / Screen | `ui/screens/photos/PhotosScreen.kt` | Header (`IlNavBar` "Ảnh đẹp Phật giáo", subtitle from `GalleryFormat.subtitle`), chip row (AC2), `LazyVerticalGrid` 2 columns replacing the masonry (`verticalScroll` + two `Column`s) that asks for the next page when ~4 cells remain (AC10); tile = `AsyncImage` in `Modifier.aspectRatio(4f / 3f)` with `ContentScale.Crop` + title + place (AC8, AC9); states: initial loading, initial error + "Thử lại" (Retry) (AC12), empty (AC15), end-of-grid loading / error + "Thử lại" (AC13); snackbar for chip-change errors and save results (AC14, AC23); `LazyGridState.scrollToItem(index)` when the viewer closes (AC20) | exists — needs major change |
| UI / Viewer | `ui/screens/photos/PhotoViewer.kt` (split out of `PhotosScreen.kt`) | Full-screen dark overlay (keeps the lightbox palette): close button, counter "{position} / {total}", `HorizontalPager` with `pageCount = total` (pages not loaded yet show a loading placeholder), large `AsyncImage` (`imageUrl`) in a 4:3 frame with `ContentScale.Crop`, uppercase topic heading, title, place, credit row (tappable → `GalleryIntents.openSource`), "Lưu ảnh" button (disabled while saving); previous / next buttons hidden on position 1 / `total`; `BackHandler` closes it; tapping the background no longer closes it (AC17–AC22, AC25, AC27) | not created (today `PhotoLightbox` inside `PhotosScreen.kt`) |
| UI / State | `ui/screens/photos/PhotosUiState.kt` | `topic: GalleryTopic?` (null = "Tất cả"), `photos: List<GalleryPhoto>`, `total`, `page`, `totalPages`, `loadState` (InitialLoading / InitialError / Loaded / Empty), `moreState` (Idle / Loading / Error / Done), `pendingTopic` (chip shown selected while its page 1 loads), `viewerIndex: Int?`, `saving: Boolean`, one-shot `message` (snackbar); **remove** `filteredPhotos`, `categories`, `countLabel` | exists — rewrite |
| UI / ViewModel | `ui/screens/photos/PhotosViewModel.kt` | `SavedStateHandle` keeps `topic` (AC16); topic changes through `collectLatest` (cancels the old request, AC11); one next-page request at a time, shared by grid and viewer (AC10, AC18); revert the chip on topic failure (AC14); `openViewer(index)` / `closeViewer()` / `onViewerPage(index)` (prefetch when `index >= photos.size - 3`); `save(photo)` → `SavePhotoToDeviceUseCase`; created through the `di/GalleryModule` factory | exists — rewrite |
| UI / Format | `ui/screens/photos/GalleryFormat.kt` | Display strings: topic label "Chùa / Tượng Phật / Cảnh thiền / Lễ hội" (Temples / Buddha statues / Meditation scenes / Festivals), uppercase heading (`Unknown` → none, AC29), subtitle "{total} ảnh" / "{total} ảnh · {topic}", counter "{n} / {total}", credit "Ảnh: {author} · {license}" (AC21), messages (load error ⚠️, empty, saved, not saved ⚠️) | not created (labels today in `PhotoCategory.label`, `PhotosUiState.countLabel`) |
| UI / Intents | `ui/screens/photos/GalleryIntents.kt` | `openSource(context, url)`: `Intent(ACTION_VIEW, url.toUri())`, catches `ActivityNotFoundException`; not called for a blank / non-http(s) URL (AC22) | not created |
| UI / Navigation | `ui/navigation/Routes.kt` (`PhotosRoute`), wiring in `MainActivity.kt` | Keep `PhotosRoute` (object, no argument; Home always opens "Tất cả", AC5); `ilComposable(PhotosRoute)` passes the ViewModel factory from `di/GalleryModule`; update the KDoc title to "Ảnh đẹp Phật giáo" | exists — needs change (small) |
| Domain / Model | `domain/model/GalleryPhoto.kt` (replaces `PhotoContent.kt`) | `GalleryTopic { Temple, BuddhaStatue, Meditation, Festival, Unknown }`; `GalleryCredit(author, license, sourceUrl?)`; `GalleryPhoto(id, topic, title, place, thumbnailUrl, imageUrl, credit: GalleryCredit?)`; `GalleryPage(items, page, pageSize, total, totalPages)` with `hasMore`; `GalleryFeatured(items, total: Int?)` (`total` null until backend v2.0) | not created |
| Domain / Delete | `domain/model/PhotoContent.kt` (`PhotoCategory`, `TemplePhoto` with `heightDp`), `domain/usecase/GetTemplePhotosUseCase.kt` (12 sample photos) | Sample data, masonry heights — **drift BR-GAL-02/05**; delete once Phase 2/3 run | exists — delete |
| Domain / Rule | `domain/gallery/GalleryHomeMore.kt` | `fun moreCount(totalPhotos: Int, tilesShown: Int): Int` and `fun overlayShown(...)`, pure Kotlin, BR-GAL-07, checked by `gallery-home-more.json` (AC31) | not created |
| Domain / Repository | `domain/repository/GalleryRepository.kt` | `getFeatured(): Result<GalleryFeatured>`, `getPage(page: Int, topic: GalleryTopic?): Result<GalleryPage>` (`Unknown` is never sent) | not created |
| Domain / Port | `domain/repository/DevicePhotoLibrary.kt` | `suspend fun save(photo: GalleryPhoto): Result<Unit>` — saves the large picture; failure → `PhotoNotSavedException` | not created |
| Domain / UseCase | `domain/usecase/GetFeaturedPhotosUseCase.kt`, `GetGalleryPageUseCase.kt`, `SavePhotoToDeviceUseCase.kt` | `operator fun invoke` → `Result`; dispatcher through `DispatcherProvider` | not created |
| Data / Remote | `data/remote/api/GalleryApiService.kt` | `getTop()`, `getList(page: Int, type: String?)` → `ApiResponse<…>`, no `@Authenticated`; no detail call | not created |
| Data / Remote | `data/remote/dto/GalleryDto.kt` | `@Serializable` DTOs per `openapi.yaml` `GallerySummary` / `GalleryTopEnvelope` / `GalleryListEnvelope`; the v2.0 fields (`author`, `license`, `source_url`, top `total`) declared nullable with default `null` so today's server still parses (`JsonProvider` has `ignoreUnknownKeys`) | not created |
| Data / Mapper | `data/mapper/GalleryMapper.kt` | `CHUA`/`TUONG_PHAT`/`THIEN`/`LE_HOI` ↔ `GalleryTopic` (unknown → `Unknown`, the item is kept); credit = null when `author` or `license` is null/blank; blank `source_url` → null; title / location passed through unchanged (BR-GAL-09) | not created |
| Data / Repository | `data/repository/GalleryRepositoryImpl.kt` | Calls the API through `NetworkClient.safeCall`; maps `NetworkResult` → `Result` (`NetworkException(AppError)` on failure); no cache | not created |
| Core / Media | `core/media/MediaStorePhotoLibrary.kt` | Implements `DevicePhotoLibrary`: gets the original bytes of `imageUrl` from Coil (`SingletonImageLoader` disk cache snapshot; `execute(ImageRequest)` first when missing), inserts into `MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY)` with `DISPLAY_NAME = "{id}.jpg"`, `MIME_TYPE = image/jpeg`, `RELATIVE_PATH = Pictures/InnerLamp` (⚠️), `IS_PENDING = 1`; copies the bytes; sets `IS_PENDING = 0`; on any failure deletes the pending row; runs on `DispatcherProvider.io` | not created |
| DI | `di/GalleryModule.kt` | `provideGalleryRepository()`, `provideDevicePhotoLibrary(context)`, the three use cases, the `PhotosViewModel` factory and the photo-block source of `HomeViewModel` | not created |
| Manifest | `app/src/main/AndroidManifest.xml` | No new permission (no `WRITE_EXTERNAL_STORAGE`, no `READ_MEDIA_IMAGES`) — AC24 | nothing to change |
| Tests | `app/src/test/java/com/example/innerlamp/…`, `app/src/test/resources/product/gallery/rules/` | Vector, mapper, repository, ViewModels, format; `ui/screens/photos/PhotosViewModelTest.kt` (exists — tests on-device filtering, to be rewritten) | partly exists (to be rewritten) |

## Navigation flow

```
HomeRoute ("Ảnh đẹp Phật giáo" block ← GET /v1/gallery/top; hidden on error / empty)
  └─ (tap a tile | "+N ảnh" | Xem tất cả) ──▶ PhotosRoute ("Tất cả")
PhotosRoute (GET /v1/gallery, pages 1…n; type)
  ├─ (tap tile #i) ──▶ viewer overlay at i (same screen, same ViewModel)
  │     ├─ (swipe / next / previous) ──▶ photo i±1 (loads the next page near the end)
  │     ├─ (tap credit) ──▶ browser (ACTION_VIEW source_url)
  │     ├─ (Lưu ảnh) ──▶ MediaStore, snackbar result
  │     └─ (close | system back) ──▶ grid scrolled to the last photo shown
  └─ (back) ──▶ HomeRoute
```

"Xem tất cả" = See all; "Lưu ảnh" = Save photo.

Default transition (crossfade) as today. `PhotosViewModel` is attached to the `PhotosRoute` back stack
entry, so the loaded pages survive configuration changes; after process death only the chip is restored
(AC16).

## Step-by-step Flow — Grid: first page, chip change, next page

```
Step 1 — First page (this feature)
- Trigger: open PhotosRoute; "Thử lại" in the initial error state
- Pre-condition: topic read from SavedStateHandle (null = "Tất cả")
- Where: PhotosViewModel → GetGalleryPageUseCase → GalleryRepositoryImpl (safeCall)
- API: GET /v1/gallery?page=1[&type=CHUA|TUONG_PHAT|THIEN|LE_HOI] (galleryList)
    - Success, total > 0 → photos = items, total, page = 1, totalPages; loadState = Loaded
    - Success, total = 0 → loadState = Empty ("Chưa có ảnh nào trong chủ đề này.", chips kept) (AC15)
    - Failure (any AppError, including 400 INVALID_PAGE / INVALID_GALLERY_TYPE) → loadState = InitialError + "Thử lại" (AC12)

Step 2 — Chip change (this feature)
- Trigger: tap a chip different from the selected one
- Pre-condition: previous topic P and its photos are on screen
- Where: PhotosViewModel (topic flow + collectLatest — an older request is cancelled, its late result discarded) → GetGalleryPageUseCase → GalleryRepositoryImpl
- API: GET /v1/gallery?page=1[&type=<new>]
    - On tap → pendingTopic = new (chip shown selected), grid of P kept, loading indicator
    - Success → topic = new (saved to SavedStateHandle), replace photos/total/page/totalPages, scroll grid to top; total = 0 → Empty (AC11, AC15)
    - Failure (any) → pendingTopic = null (chip back to P), photos of P kept, snackbar with the load-error message (AC14)

Step 3 — Next page (this feature; shared with the viewer, see next flow)
- Trigger: the grid's last visible cell is ≤ 4 from the end; or the viewer reaches index ≥ photos.size − 3; or "Thử lại" at the end of the grid
- Pre-condition: moreState != Loading; page < totalPages; topic unchanged since the page was requested (a chip change cancels it)
- Where: PhotosViewModel → GetGalleryPageUseCase → GalleryRepositoryImpl
- API: GET /v1/gallery?page=N+1 (same type)
    - Success → append items, page = N+1; empty items → moreState = Done (AC10)
    - Failure (any) → keep photos; moreState = Error → grid: error + "Thử lại" at the end (AC13); viewer: snackbar (AC19)
```

## Step-by-step Flow — Viewer: open, page, save, close

```
Step 1 — Open (this feature, no backend call)
- Trigger: tap tile #i in the grid
- Where: PhotosScreen → PhotosViewModel.openViewer(i) → viewerIndex = i
- Effect: pager at page i; counter "i+1 / total"; credit from photos[i].credit (null → no credit line; only possible before backend v2.0, AC21 ⚠️)

Step 2 — Page (this feature)
- Trigger: swipe, or next / previous
- Pre-condition: target index in 0 until total (previous hidden at 0, next hidden at total − 1)
- Where: PhotosViewModel.onViewerPage(j) → viewerIndex = j; if j >= photos.size − 3 → grid flow Step 3
- Effect: a page j >= photos.size shows a loading placeholder until Step 3 succeeds; on Step 3 failure the pager returns to the last loaded photo and a snackbar is shown (AC19 ⚠️ product)

Step 3 — Save (this feature, device side effect)
- Trigger: tap "Lưu ảnh"
- Pre-condition: saving == false (the button is disabled while saving, AC25)
- Where: PhotosViewModel.save(photo) → SavePhotoToDeviceUseCase → DevicePhotoLibrary (MediaStorePhotoLibrary, io dispatcher)
- Calls:
    a. Coil: original bytes of photo.imageUrl (disk cache; fetch when missing)
    b. ContentResolver.insert(MediaStore images, IS_PENDING = 1, Pictures/InnerLamp, "{id}.jpg")
    c. openOutputStream(uri) → copy bytes
    d. update(uri, IS_PENDING = 0)
    - Success (d done) → snackbar "Đã lưu vào thư viện ảnh" (AC23)
    - Failure (a: download / cache; b: insert null; c/d: IOException, no space) → delete the pending row if b ran; snackbar "not saved" (⚠️ copy); never the success message (AC23)

Step 4 — Open the source (this feature, no backend call)
- Trigger: tap the credit
- Pre-condition: credit.sourceUrl is an http(s) URL (else the credit is not tappable, AC22 ⚠️)
- Where: PhotoViewer → GalleryIntents.openSource(context, url) (ActivityNotFoundException caught)

Step 5 — Close (this feature)
- Trigger: close button or system back (BackHandler)
- Where: PhotosViewModel.closeViewer() → viewerIndex = null; PhotosScreen scrolls the LazyVerticalGrid to the last index shown (AC20)
```

## Step-by-step Flow — Home block

```
Step 1 — Featured photos (this feature)
- Trigger: Home opens (HomeViewModel init); not retried automatically
- Where: HomeViewModel → GetFeaturedPhotosUseCase → GalleryRepositoryImpl (safeCall)
- API: GET /v1/gallery/top (galleryTop)
  ⚠️ Not confirmed: contract pending — `total` per backend gallery requirements.md v2.0 AC 3 (not built,
  not in openapi.yaml); until it ships `GalleryFeatured.total` is null and the overlay is not shown.
    - Success, items non-empty → tiles = items.take(3); moreCount = GalleryHomeMore.moreCount(total, tiles.size) when total != null; Shown (AC3, AC4)
    - Success, items empty → Hidden (AC7, ⚠️ product)
    - Failure (any AppError) → Hidden (AC6)
```

## Local storage

- No Room, no prefs, no offline copy (online-only, product §2.4). Pictures: Coil's default memory + disk
  cache; the save flow reads the large picture from that disk cache.
- Selected chip: `SavedStateHandle` (AC16), not prefs.
- Saved photos go to the user's shared `Pictures/InnerLamp` collection through `MediaStore`; they belong
  to the user, the app keeps no record of them (product §3.5) and reads none back.

## Deliberately stubbed for now

- **Backend gallery v2.0** (⚠️ contract pending — backend `requirements.md` v2.0 AC 3, AC 11; backend
  `tasks.md` Phase 6–7, openapi update in Phase 7): the DTOs declare `author`, `license`, `source_url`
  and top `total` nullable. While waiting: no credit in the viewer, no "+N ảnh" overlay. **The viewer
  must not ship in a release build before v2.0 is live** (BR-GAL-04 is a licence obligation). There is
  no fallback to `GET /v1/gallery/{id}` per photo and no sample N. When v2.0 lands in `openapi.yaml`:
  update the `Contract:` line of `requirements.md`, make the fields required in the DTOs, remove the
  markers.
- **Picture URLs**: the contract example serves pictures over `http://` from the server host; release
  builds block cleartext traffic (no `networkSecurityConfig` in the app). The staging/production picture
  host must be `https` — follows the pending photo-storage ADR (product Appendix A item 1); a backend /
  infra hand-off, nothing to stub in the app.
- **Copy still waiting on product**: the gallery load-error message, the "not saved" message, the
  Home block empty case, the viewer's next-page failure, a credit without source link, an unknown topic
  heading — `GalleryFormat` keeps the proposed defaults from `requirements.md`, clearly marked as
  temporary.
- **UI and data layer**: `top` and the paged list are already in the contract, so Phase 3 is not blocked
  for them — do it in the same batch as Phase 2, no new sample data added to `main`.
  `FakeGalleryRepository` and `FakeDevicePhotoLibrary` live only in `app/src/test/…`; `@Preview` builds
  the `UiState` directly for the stateless parts (`PhotosContent`, `PhotoViewerContent`).

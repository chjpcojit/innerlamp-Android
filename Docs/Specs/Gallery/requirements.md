# Requirements — Gallery

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — the "Ảnh chùa đẹp" (Beautiful temple photos) screen (`photos`), its lightbox and the Home photo block have UI on 12 sample photos hardcoded in the domain (placeholders, no real images); filtering on the device, no pagination, varying tile heights, a one-photo lightbox without counter, paging, credit or "Lưu ảnh" (Save photo); Home "+N" is the constant 9. No repository/API yet. List credit and `total` on `top` wait for backend gallery v2.0, which also gates the viewer's release (product §2.7).
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/photos/` (the code folder name is kept, product §1.5); the Home block lives in `ui/screens/home/`
Product spec: innerlamp-be `docs/product/gallery/product.md` @ v1.1
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ a8a016b (info.version 1.0.0) — tag Gallery (`galleryTop`, `galleryList`); the v2.0 part (`author`, `license`, `source_url` on every list item, `total` on `top`) follows innerlamp-be `docs/specs/features/gallery/requirements.md` @ v2.0 (2026-10-15, AC 3 and AC 11) — **not built and not yet in openapi.yaml** (backend `tasks.md` Phase 6–7)
Implements: BR-GAL-01, BR-GAL-02, BR-GAL-03, BR-GAL-04, BR-GAL-05, BR-GAL-06, BR-GAL-07, BR-GAL-08, BR-GAL-09, FR-GAL-001, FR-GAL-002, FR-GAL-003, FR-GAL-004, FR-GAL-005, FR-GAL-006, FR-GAL-007, FR-GAL-008, NFR-GAL-01, NFR-GAL-02, NFR-GAL-03 — FR-GAL-009 is Backend (Android `n/a` in product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) of the **"Ảnh đẹp Phật giáo"** (Buddhist photos) gallery for Android. Written
in a mixed style: the current state is read from the code on 2026-10-15 (`main` @ b024b28), everything
else is prospective. Product rules (topics and labels, gallery order, page sizes, the credit, the tile
shape, the viewer range, the Home "+N", the removed actions, content language) live in the product spec
and are **not** repeated here; this file only says how the Android app implements the
`BR-GAL-*`/`FR-GAL-*`.

## Constraints from the product spec

- **Public, read-only** (§2.3, Q13): no login needed; no Bearer (like `TempleApiService`).
- **The server decides order, filtering and paging** (BR-GAL-02, BR-GAL-03, NFR-GAL-01): the app shows
  exactly the order it receives, does not re-sort, does not filter on the device and never loads the
  whole gallery at once. `gallery-order.json` runs only on the backend (product §4.1).
- **The app computes one rule itself**: the Home "+N ảnh" (BR-GAL-07), checked against
  `rules/gallery-home-more.json`.
- **The credit is legal, not cosmetic** (§2.5, BR-GAL-04): no large photo is shown in a release build
  without "Ảnh: {author} · {licence}" (Photo: author · licence). Release dependency (§2.7, A2): the viewer
  is not released to users before backend gallery v2.0 carries the credit in every list, and the app
  makes no per-photo credit call in the meantime.
- **One load-error message** (FR-GAL-008, P1): "Không tải được danh sách ảnh. Vui lòng thử lại sau."
  (Could not load the photo list. Please try again later.), with "Thử lại" (Retry) wherever a retry is
  possible — called "the load-error message" below. A load failure is always shown, never hidden.
- **No sample data** (§2.1 "Android shows six sample photos"; the code has 12): every photo comes from
  the backend.
- **Dependencies:** backend gallery v2.0 (credit in the lists, `total` on `top`) — a release blocker for
  this feature on Android, see "Data & API"; the device's photo collection through `MediaStore`
  (FR-GAL-007); a browser for the source page (FR-GAL-006).

## User Story

As a visitor (signed in or not), I want to see a few Buddhist photos on Home, browse the whole gallery
by topic, and open a photo large with its credit, so that I can enjoy them quietly, see who took each
photo under which licence, and save the ones I like to my phone (product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Feature name and topics

1. WHEN the Home block or the gallery screen is shown, THE SYSTEM SHALL title both "Ảnh đẹp Phật giáo"
   (Buddhist photos), replacing "Ảnh chùa đẹp" (Beautiful temple photos). (BR-GAL-01)
2. WHEN the gallery screen is shown, THE SYSTEM SHALL show exactly five chips "Tất cả · Chùa · Tượng Phật
   · Cảnh thiền · Lễ hội" (All · Temples · Buddha statues · Meditation scenes · Festivals) in that order,
   exactly one selected, "Tất cả" selected on first open. (BR-GAL-01, FR-GAL-003)

### Home block

3. WHEN Home opens, THE SYSTEM SHALL call `GET /v1/gallery/top` (public) and show the "Ảnh đẹp Phật
   giáo" block with the first 3 photos received, in the order received, as tiles using the small picture
   (`thumbnail_url`), plus "Xem tất cả" (See all); while loading, the block shows a skeleton like the
   other Home blocks. (FR-GAL-001, BR-GAL-02, NFR-GAL-02)
4. WHEN the block is shown, THE SYSTEM SHALL compute N for "+N ảnh" (+N photos) per BR-GAL-07 from the
   whole-gallery `total` returned by `top` and the number of tiles shown, cover the last tile with
   "+N ảnh" only when N > 0, and show fewer tiles (no overlay) when `top` returns fewer than 3 photos.
   (BR-GAL-07, FR-GAL-001)
   > ⚠️ Not confirmed: contract pending — `total` on `top` is backend gallery v2.0 AC 3, not built yet;
   > until it ships the app has no N and shows **no** overlay (never a guessed or sample N) — blocks the
   > final behaviour of this criterion, not the UI work.
5. WHEN the user taps a tile, the "+N ảnh" tile or "Xem tất cả", THE SYSTEM SHALL open the gallery
   (`PhotosRoute`) with the "Tất cả" chip; a Home tile does not open the viewer. (FR-GAL-001)
6. WHEN `top` fails (any error), THE SYSTEM SHALL still show the "Ảnh đẹp Phật giáo" block, with the
   load-error message "Không tải được danh sách ảnh. Vui lòng thử lại sau." and "Thử lại" (Retry) in
   place of the tiles — no photo tile, no sample photo, no "+N ảnh" — and "Thử lại" calls `top` again
   for this block only; the other Home blocks are not blocked or reloaded. (FR-GAL-008)
7. WHEN `top` succeeds with no photo, THE SYSTEM SHALL still show the "Ảnh đẹp Phật giáo" block, with
   "Chưa có ảnh nào." (No photos yet.) in place of the tiles and no "+N ảnh". (BR-GAL-02, FR-GAL-008)

### Gallery grid

8. WHEN the user opens `PhotosRoute`, THE SYSTEM SHALL call `GET /v1/gallery?page=1` (no `type`), show
   the photos in exactly the order received as a 2-column grid of tiles, each with the small picture,
   the title and the place, and show the subtitle "{total} ảnh" ({total} photos) with `total` from the
   response. (FR-GAL-002, BR-GAL-02, BR-GAL-03, NFR-GAL-02)
9. WHEN a tile is shown, THE SYSTEM SHALL give every tile the same 4:3 landscape frame with the picture
   filling it and cropped at the edges (Coil `ContentScale.Crop`), replacing today's varying heights;
   a picture that fails to load shows a placeholder in the same frame. (BR-GAL-05)
10. WHEN the user scrolls near the end of the grid and `page < total_pages`, THE SYSTEM SHALL load the
    next page and append it after the photos already shown, one request at a time; THE SYSTEM SHALL stop
    when `page >= total_pages` or a returned page is empty, and read the page size from the response's
    `page_size` (20 today), not assume it. (FR-GAL-002, BR-GAL-03, NFR-GAL-01)
11. WHEN the user selects a topic chip, THE SYSTEM SHALL request page 1 again with the matching `type`
    (dropping the parameter entirely for "Tất cả"), and on success replace the grid from its first photo
    and update the subtitle to "{total} ảnh · {topic}" ({total} photos · {topic}) with the topic's own
    `total`; THE SYSTEM SHALL discard the result of an older request that arrives late and never filter
    on the device. (FR-GAL-003, BR-GAL-01, BR-GAL-02, BR-GAL-03)
12. WHEN page 1 of the gallery cannot be loaded on first open (or after "Thử lại" (Retry)), THE SYSTEM
    SHALL show an error message with "Thử lại", no photo tile and no count, and "Thử lại" requests page
    1 again for the selected chip; the message is the load-error message "Không tải được danh sách ảnh.
    Vui lòng thử lại sau." (FR-GAL-008)
13. WHEN loading a next page fails, THE SYSTEM SHALL keep every photo already shown and show the error
    message with "Thử lại" at the end of the grid, which reloads exactly that page. (FR-GAL-008)
14. WHEN loading page 1 of a newly selected topic fails, THE SYSTEM SHALL keep the photos already shown,
    move the chip selection back to the topic they belong to, and show the error message as a short
    message (snackbar); the grid is not cleared while the new topic loads. (FR-GAL-008)
15. WHEN the gallery or the selected topic holds no photo (`total = 0`, no error), THE SYSTEM SHALL show
    "Chưa có ảnh nào trong chủ đề này." (No photos in this topic yet.) and keep the chip row so the user
    can switch topic. (FR-GAL-008)
16. WHEN the app is killed by the system and reopened on the gallery (process death), THE SYSTEM SHALL
    keep the selected chip and reload page 1 for it; the viewer is not restored. (platform)

### Photo viewer

17. WHEN the user taps a tile in the gallery grid, THE SYSTEM SHALL open the full-screen viewer on that
    photo, showing a close button, the counter "{position} / {total}" with `total` = the server total
    under the current filter, the large picture (`image_url`) in a 4:3 frame filled and cropped like the
    tile (never letterboxed), the topic as a small uppercase heading (e.g. "CHÙA" (TEMPLES)), the title, the place and the
    credit (AC21). (FR-GAL-004, BR-GAL-05, BR-GAL-06, NFR-GAL-02)
18. WHEN the user swipes or taps next / previous in the viewer, THE SYSTEM SHALL show that photo with its
    own topic, title, place, credit and position, paging through the whole gallery under the current
    filter in the order received; WHEN the viewer comes within 3 photos of the last loaded photo and
    `page < total_pages`, THE SYSTEM SHALL load the next page (the same request and the same list as the
    grid, AC10); previous is not offered on position 1 and next is not offered on position `total`.
    (FR-GAL-005, BR-GAL-06, NFR-GAL-01)
19. WHEN the viewer cannot load the next photos, THE SYSTEM SHALL stay on the current photo (its
    position unchanged, e.g. "20 / 86"), show the load-error message "Không tải được danh sách ảnh. Vui
    lòng thử lại sau." as a short message (toast; a snackbar on this screen), and keep next offered so
    moving to the next photo again retries the load. (FR-GAL-005, FR-GAL-008, BR-GAL-06)
20. WHEN the user closes the viewer (close button or system back), THE SYSTEM SHALL return to the grid
    scrolled so the tile of the photo last shown is visible, with every photo the viewer loaded now also
    in the grid. (FR-GAL-004, platform)

### Credit

21. WHEN a photo is shown in the viewer, THE SYSTEM SHALL show the credit "Ảnh: {author} · {licence}"
    (Photo: author · licence) with `author` and `license` exactly as received, for every licence
    including CC0 and public domain, and the credit changes with the photo. THE SYSTEM SHALL not release
    the viewer to users (no release build ships it) before backend gallery v2.0 carries the credit in
    every list, and SHALL NOT fetch a photo's credit separately in the meantime (no
    `GET /v1/gallery/{id}` per photo). (FR-GAL-006, BR-GAL-04, product §2.7)
    > ⚠️ Not confirmed: contract pending — `author`, `license`, `source_url` on list items are backend
    > gallery v2.0 AC 11, not built yet and not in openapi.yaml — blocks the release of the viewer
    > (product §2.7), not the UI work.
22. WHEN the user taps the credit, THE SYSTEM SHALL open the photo's `source_url` in the browser
    (`Intent.ACTION_VIEW`); WHEN no app can open it, THE SYSTEM SHALL not crash; WHEN the photo has no
    source link (`source_url` missing or blank), THE SYSTEM SHALL still show the credit
    "Ảnh: {author} · {licence}", which then cannot be tapped. (FR-GAL-006, BR-GAL-04)

### Save a photo

23. WHEN the user taps "Lưu ảnh" (Save photo) in the viewer, THE SYSTEM SHALL save the large picture of
    the photo shown (the `image_url` bytes, unchanged) into the device photo collection through
    `MediaStore` without requesting any storage permission (minSdk 31), then:
    - on success: show "Đã lưu vào thư viện ảnh" (Saved to the photo library), only after the item is
      fully written;
    - on failure (download failed, no space, write failed): show the short message (toast; a snackbar
      on this screen) "Không lưu được ảnh. Vui lòng thử lại." (Couldn't save the photo. Please try
      again.), never the success message, and leave no half-written item behind. (FR-GAL-007)
24. WHEN the photo is saved on Android, THE SYSTEM SHALL request no runtime permission, so the
    "permission refused → offer the device settings" branch of FR-GAL-007 does not occur on Android.
    (FR-GAL-007, product §3.6 FR-GAL-007 Android row)
25. WHEN a save is in progress, THE SYSTEM SHALL disable "Lưu ảnh" until it finishes, so one tap saves
    one copy; a later tap saves another copy. (platform)
26. WHEN the photo is saved, THE SYSTEM SHALL put it in the album folder `Pictures/InnerLamp` with the
    photo's `id` as the file name. (platform — product A4 leaves the folder to this spec; confirmed by
    anh Đức 2026-10-15)

### Removed actions and content language

27. WHEN the viewer is shown, THE SYSTEM SHALL offer no favourite (heart), "Đặt hình nền" (Set as
    wallpaper) or "Chia sẻ" (Share) action; "Lưu ảnh" is the only photo action. (BR-GAL-08)
28. WHEN a title or place is shown (Home tile, grid tile, viewer), THE SYSTEM SHALL show it exactly as
    received, in Vietnamese, with no translation or rewriting; only interface copy (labels, buttons,
    messages, the uppercase topic heading) belongs to the app. (BR-GAL-09)
29. WHEN `type` is not one of the four known values (the contract treats it as an open enum), THE SYSTEM
    SHALL still show the photo as a normal tile and viewer page, with no topic heading. (BR-GAL-01,
    platform)
    > ⚠️ Not confirmed (product): the heading for an unknown topic — does not block (there are only four
    > topics); hand back to ba-writer in innerlamp-be. Default used above: no topic heading.

### Performance and vectors

30. WHEN measured on a 4G network, THE SYSTEM SHALL show the first photos of a topic change within 2
    seconds of sending the request. (NFR-GAL-03)
31. WHEN unit tests run, THE SYSTEM SHALL pass every case of innerlamp-be
    `docs/product/gallery/rules/gallery-home-more.json` through `GalleryHomeMore`; `gallery-order.json`
    does not run on Android (the server orders, product §4.1). (BR-GAL-07)

### Current code state per AC (read on 2026-10-15)

| AC | Status | Notes |
|---|---|---|
| 1 | drift | "Ảnh chùa đẹp" in `PhotosScreen.kt` (`IlNavBar`) and `HomeSections.kt` (`PhotosSection`) — **drift BR-GAL-01** |
| 2 | ✓ | "Tất cả" + `PhotoCategory` (`Chùa`, `Tượng Phật`, `Cảnh thiền`, `Lễ hội`) in that order, one selected |
| 3 | drift | Three `PlaceholderImage` tiles (1 large + 2 small, prototype mosaic), no data source, no skeleton |
| 4 | drift | `photoMore = HomeSampleData.PHOTO_MORE` = 9 (constant) — **drift BR-GAL-07** |
| 5 | ✓ navigation part | Every tile and "Xem tất cả" call `onPhotos` → `PhotosRoute` (`MainActivity.kt`) |
| 6, 7 | missing | The block is always shown |
| 8 | drift | `GetTemplePhotosUseCase` returns 12 sample `TemplePhoto` synchronously; subtitle counts samples |
| 9 | drift | Masonry with `heightDp` per photo, gradient placeholder — **drift BR-GAL-05** |
| 10 | missing | No pagination; `verticalScroll` + two `Column`s, not a lazy grid |
| 11 | drift | `PhotosUiState.filteredPhotos` filters **on the device** |
| 12–16 | missing | No loading, error, empty state or `SavedStateHandle` |
| 17 | drift | `PhotoLightbox`: one photo, 360dp-high placeholder, category label, title, place, close button; no counter, no picture, no credit |
| 18, 19 | missing | No paging in the lightbox (tap anywhere closes it) — **drift BR-GAL-06** |
| 20 | partly | Closing returns to the grid (state kept), no scroll to the last photo |
| 21, 22 | missing | No credit — **BR-GAL-04 planned** |
| 23–26 | missing | No "Lưu ảnh" |
| 27 | ✓ | The lightbox has no heart, wallpaper or share action |
| 28 | ✓ (sample) | Titles and places shown as stored; real data in Phase 3 |
| 29 | missing | `PhotoCategory` has no unknown value |
| 30, 31 | missing | `PhotosViewModelTest` tests on-device filtering — to be rewritten |

## Data & API

The source of truth is `openapi.yaml` (and backend gallery `requirements.md` v2.0 for the part not yet in
the contract); schemas are not copied here. Every operation is public (no `@Authenticated`).

| Action | Operation (openapi) | Auth | Stored locally | Notes |
|---|---|---|---|---|
| Home block | `GET /v1/gallery/top` (`galleryTop`) | public | none | In the contract (10 items); `total` is backend v2.0 AC 3 — ⚠️ not built, not in openapi.yaml |
| Grid page + topic filter | `GET /v1/gallery?page=&type=` (`galleryList`) | public | none | `page`, `type` (`CHUA`, `TUONG_PHAT`, `THIEN`, `LE_HOI`), `total`, `total_pages`, `page_size` are in the contract |
| Credit in the viewer | fields of the list items above | public | none | `author`, `license`, `source_url` on list items are backend v2.0 AC 11 — ⚠️ not built, not in openapi.yaml (today only on `galleryGet`) |
| Photo detail | `GET /v1/gallery/{id}` (`galleryGet`) | — | — | **Not used** by Android (no second call to credit a photo, product §3.1.3) |
| Pictures | `thumbnail_url` (tiles), `image_url` (viewer, save) | public | Coil image cache | Use the URL exactly as received |
| Save | — (`MediaStore`) | — | the user's photo collection | AC23–AC26; InnerLamp keeps no record (product §3.5) |
| Source page | — (`ACTION_VIEW`) | — | none | AC22 |

## Error → UI mapping

| Operation | Failure case | User sees | Code status |
|---|---|---|---|
| `GET /v1/gallery/top` | Any error (`NoConnection`, `Timeout`, 5xx, `Serialization`, `Unknown`) | Block shown with "Không tải được danh sách ảnh. Vui lòng thử lại sau." + "Thử lại" in place of the tiles (AC6) | missing (block always shown on samples) |
| `GET /v1/gallery/top` | 200 with no item | Block shown with "Chưa có ảnh nào." in place of the tiles (AC7) | missing |
| `GET /v1/gallery` page 1 (first open / "Thử lại") | Any error, including `400 INVALID_PAGE` / `INVALID_GALLERY_TYPE` | "Không tải được danh sách ảnh. Vui lòng thử lại sau." + "Thử lại", no tile (AC12) | missing |
| `GET /v1/gallery` page 1 (chip change) | Any error | Previous photos kept, chip reverted, load-error message as a snackbar (AC14) | missing |
| `GET /v1/gallery` page ≥ 2 (grid) | Any error | Photos kept, load-error message + "Thử lại" at the end of the grid (AC13) | missing |
| `GET /v1/gallery` page ≥ 2 (viewer) | Any error | Stay on the current photo, load-error message as a toast (snackbar), moving next retries (AC19) | missing |
| `GET /v1/gallery` | 200, `total = 0` | "Chưa có ảnh nào trong chủ đề này." (AC15) | missing |
| (Coil) | Picture fails to load | Placeholder in the same 4:3 frame (AC9, AC17) | drift (placeholder only) |
| (device) Save | Download / write / space failure | "Không lưu được ảnh. Vui lòng thử lại." as a toast (snackbar), no success message (AC23) | missing |
| (device) Source page | No app handles `ACTION_VIEW` | Nothing happens, no crash (AC22) | missing |
| (data) Credit | Photo without a source link | Credit shown, not tappable (AC22) | missing |

## Out of scope

- Favourite, set as wallpaper, share (BR-GAL-08; product §1.2).
- Masonry layout, search, zoom inside the viewer, a web page per photo (product §1.2).
- Loading gallery content (FR-GAL-009 — Backend).
- Temple photos on the temple page (`../Temple/`) and deity images (`../Deity/`).
- An offline copy of the gallery (product §2.4: online only).
- Opening the viewer from a Home tile (FR-GAL-001: a tile opens the gallery).

## Pending (not yet implemented)

- Domain: contract-based model, repository, use cases, `GalleryHomeMore`, the device photo library port;
  remove the sample data — `tasks.md` Phase 1.
- Home block, grid with pagination + server-side filtering + states, viewer with paging, credit and
  save, the new title (AC1–AC29) — Phase 2.
- Data layer for `top` and the list, the `MediaStore` saver (AC3–AC26) — Phase 3; credit and `total`
  wait for backend gallery v2.0.
- `gallery-home-more.json` vector, NFR measurement (AC30, AC31) — Phase 4.
- `Docs/home_assumptions.md` row 1 hand-off — Phase 5.

## Open questions (resolved)

Per product spec v1.0 (2026-10-15) — these points replace the current behaviour of the Android code:

- ~~Title "Ảnh chùa đẹp"~~ → "Ảnh đẹp Phật giáo" on Home and the gallery (BR-GAL-01) → AC1. Resolved
  2026-10-15.
- ~~12 sample photos, filtering on the device, no pagination~~ → backend data, 20 per load, next load at
  the end of the grid, server-side `type` filter (BR-GAL-02, BR-GAL-03, NFR-GAL-01) → AC8, AC10, AC11.
  Resolved 2026-10-15.
- ~~Masonry with varying heights~~ → uniform 4:3 tiles and a 4:3 viewer frame (BR-GAL-05) → AC9, AC17.
  Resolved 2026-10-15.
- ~~One-photo lightbox~~ → a viewer over the whole filtered gallery with "N / total" from the server
  (BR-GAL-06) → AC17–AC20. Resolved 2026-10-15.
- ~~No credit~~ → "Ảnh: {author} · {licence}" + source page on every large photo (BR-GAL-04) → AC21,
  AC22. Resolved 2026-10-15.
- ~~Home "+9" constant~~ → N = whole-gallery total − tiles shown (BR-GAL-07) → AC4. Resolved 2026-10-15.
- ~~Home block on error~~ → hidden on the apps (FR-GAL-008, §3.6) → AC6. Resolved 2026-10-15;
  **superseded by product v1.1** (P1, below).
- ~~Save, favourite, wallpaper, share~~ → only "Lưu ảnh" (FR-GAL-007, BR-GAL-08) → AC23–AC27. Resolved
  2026-10-15.

Per product spec v1.1 (2026-10-15, anh Đức's answers to the questions raised by this spec v1.0):

- ~~Gallery load-error copy (Appendix A item 5)~~ → "Không tải được danh sách ảnh. Vui lòng thử lại
  sau." with "Thử lại", on every platform (FR-GAL-008, P1) → AC12, AC13, AC14, AC19. Resolved 2026-10-15.
- ~~Home block hidden on error~~ → the block stays, with the load-error message and "Thử lại" in place of
  the tiles (FR-GAL-008, P1) → AC6. Resolved 2026-10-15.
- ~~Home block when the gallery is empty~~ → the block shows "Chưa có ảnh nào." (FR-GAL-008, BR-GAL-02,
  P4) → AC7. Resolved 2026-10-15.
- ~~Viewer when the next photos fail to load~~ → toast with the load-error message, stay on the current
  photo, moving next retries (FR-GAL-008, P5) → AC19. Resolved 2026-10-15.
- ~~"Save failed" copy (Appendix A item 4)~~ → "Không lưu được ảnh. Vui lòng thử lại." as a toast,
  replacing the proposed "Chưa lưu được ảnh. Vui lòng thử lại." (FR-GAL-007, P2) → AC23. Resolved
  2026-10-15.
- ~~§3.6 Android row for FR-GAL-007~~ → added: Android asks for no permission, the "open Settings"
  branch is iOS only (A3) → AC24. Resolved 2026-10-15.
- ~~Credit without a source link~~ → credit shown, not tappable (BR-GAL-04, FR-GAL-006, P6) → AC22.
  Resolved 2026-10-15.
- ~~Letterboxed viewer (Appendix A item 3)~~ → crop to 4:3, never letterboxed (BR-GAL-05, P3) → AC17.
  Resolved 2026-10-15.
- ~~Temporary per-photo credit call~~ → none; the viewer is not released before backend gallery v2.0
  (product §2.7, A2) → AC21. Resolved 2026-10-15.
- ~~Album folder on the device~~ → `Pictures/InnerLamp` (product A4 leaves it to this spec; confirmed by
  anh Đức) → AC26. Resolved 2026-10-15.

Still open: the heading for an unknown topic type (AC29, ⚠️ product; default: no heading).

# Tasks — Temple

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — chỉ `TempleOptionRepository` (danh sách rút gọn) và các route đã có đúng spec; mọi việc khác `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
`[x]` chỉ khi đã kiểm tra tồn tại **và đúng như product spec v1.1 yêu cầu** trong code ngày
2026-10-15; mục có code nhưng lệch product để `[ ]` và ghi "(có code, lệch: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Đã có, giữ nguyên

- [x] `domain/repository/TempleOptionRepository.kt` + `data/repository/TempleOptionRepositoryImpl.kt`
      (`GET /v1/temples/options` qua `safeCall`, bỏ `q` rỗng, cắt 100 ký tự) + `di/TempleModule.kt`
      `provideTempleOptionRepository` — dùng bởi `../Onboarding/` (FR-TPL-005, AC16)
- [x] `TempleListRoute`, `TempleRoute(templeId)` trong `ui/navigation/Routes.kt` và wiring Home → danh bạ
      → trang chùa trong `MainActivity.kt` (AC15; callback `onOffer`/`onSchedule` phải bỏ — Phase 2)

## Phase 1 — Domain

- [ ] `domain/model/Temple.kt`: `Temple`, `TempleLocation`, `TempleRegion` (ba miền, BR-TPL-04),
      `TemplePage`; bỏ `Temple` cũ khỏi `domain/model/HomeContent.kt` (cập nhật `GetTemplesUseCase`,
      `GetFestivalCalendarUseCase`, `ChuaSampleData` để vẫn biên dịch — chúng giữ dữ liệu mẫu riêng)
- [ ] `domain/model/TempleContent.kt` → `TempleDetail` theo `design.md` (có code, lệch: thống kê, thời
      khóa, `TempleTone`/`TempleGlyph` — bỏ); `OpeningHours`, `TempleAmenities`, `TempleContacts`
- [ ] `domain/temple/TempleOpenStatusRule.kt` — BR-TPL-09, giờ Việt Nam, `LunarCalendar` (AC27)
- [ ] `domain/temple/TempleDistance.kt` — haversine (BR-TPL-07, AC30)
- [ ] `domain/location/LocationProvider.kt` (port, AC30)
- [ ] `domain/repository/TempleRepository.kt` + `TempleNotFoundException` (AC17)
- [ ] `domain/usecase/GetFeaturedTemplesUseCase.kt`, `GetTemplePageUseCase.kt` (trim, cắt 100, rỗng →
      `null` — AC9, AC11); viết lại `GetTempleDetailUseCase.kt` nhận repository, trả `Result` (có code,
      lệch: trả một chùa mẫu cho mọi id)

## Phase 2 — UI (`il-ui-builder`)

- [ ] Khối nổi bật: `HomeViewModel` đọc `GetFeaturedTemplesUseCase`, trạng thái riêng của khối; thẻ theo
      AC4 (ảnh Coil + giữ chỗ); trống (AC2); lỗi + "Thử lại" (AC3, ⚠️ câu); bỏ chùa khỏi
      `HomeSampleData` (có code, lệch: dữ liệu mẫu, thẻ có `note`)
- [ ] Nhãn hiển thị `type`/`sect`/`heritage_rank`/miền trong `ui/screens/temple/TempleFormat.kt` (AC4;
      ⚠️ product — bảng tạm ghi rõ là tạm)
- [ ] Danh bạ: viết lại `TempleListUiState` + `TempleListViewModel` — phân trang (AC5, AC6, AC8),
      debounce + huỷ request cũ (AC9), xoá tìm (AC11), bốn chip cố định (AC12), `SavedStateHandle` (AC14)
      (có code, lệch: lọc trên máy theo tên + nơi chốn, chip suy từ dữ liệu có "Tây Nguyên", không phân
      trang)
- [ ] `TempleListScreen`: dòng số chùa tìm thấy, nạp trang kế ở cuối, "Thử lại" cuối danh sách, trạng
      thái lỗi trang đầu (mất mạng / chung), "Không tìm thấy chùa" + gợi ý (AC7, AC8, AC10, AC13; ⚠️ câu)
- [ ] Trang chùa: viết lại `TempleUiState`/`TempleViewModel` (bỏ `isFollowing`, `toggleFollow`); trạng
      thái tải / lỗi / không tìm thấy (AC17)
- [ ] `TempleScreen`: các phần theo FR-TPL-007 (AC18), giới thiệu thu gọn 5 dòng (AC19), lưu ý đánh số
      (AC20), tiện ích (AC21; ⚠️ "Kinh sách"), kiến trúc (AC22), vị trí & liên hệ (AC23), chân trang
      (AC24); bỏ "Theo dõi", "Cúng dường", thống kê, "Thời khóa sắp tới" (AC25; có code, lệch: BR-TPL-08)
- [ ] "Sự kiện & khóa học": trạng thái trống cố định, không gợi ý theo dõi, không "Xem tất cả" (AC26)
- [ ] Thẻ giờ: nhãn trạng thái, hai dòng giờ, "Hôm nay"; tính lại khi `ON_RESUME` và mỗi phút
      (AC27, AC28, AC29; ⚠️ product dòng Rằm khi không có giờ Rằm)
- [ ] Quyền vị trí: `ACCESS_COARSE_LOCATION` + `ACCESS_FINE_LOCATION` trong `AndroidManifest.xml`; xin
      một lần (cờ prefs `location_permission_asked`), từ chối thì không xin lại (AC31; ⚠️ thời điểm xin);
      dòng "Cách bạn …" (AC30)
- [ ] `ui/screens/temple/TempleIntents.kt` + `<queries>` trong manifest: chỉ đường `geo:` + fallback
      Google Maps web (AC33); quay số, trình duyệt, mail; không crash khi không có app (AC34); nối
      `onDirections` trong `MainActivity.kt` (có code, lệch: no-op)
- [ ] `MainActivity.kt`: bỏ `onOffer`, `onSchedule` của `TempleCallbacks`; tạo ViewModel qua factory
      `di/TempleModule`

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Xác nhận từng operation trong innerlamp-be `docs/specs/api/openapi.yaml` (cite, không chép
      schema): `top`, danh sách, chi tiết có @ 0eb7d1f; `q`/`region`/trang 12 và trường mới chi tiết
      **chưa** (⚠️ contract — hand-off backend, "Deliberately stubbed")
- [ ] `data/remote/api/TempleApiService.kt`: `getTop`, `getList(page, q, region)`, `getDetail(id)`
- [ ] `data/remote/dto/TempleDto.kt` (trường mới nullable/default để parse cả bản cũ)
- [ ] `data/mapper/TempleMapper.kt`: miền, `""` → `null`, giờ `HH:MM - HH:MM` (AC29; ⚠️ contract AC46
      backend), `YYYY-MM`
- [ ] `data/repository/TempleRepositoryImpl.kt` qua `NetworkClient.safeCall`; `TEMPLE_NOT_FOUND` →
      `TempleNotFoundException`; các `AppError` khác theo bảng Error → UI
- [ ] `core/location/AndroidLocationProvider.kt` (`LocationManager.getCurrentLocation`, hạn 5 s, fallback
      last known; không lưu/log — AC30, AC32)
- [ ] `di/TempleModule.kt`: repository, use case, location provider, factory ViewModel
- [ ] Khi backend có `q`/`region`/trang 12 và trường mới trong `openapi.yaml`: cập nhật dòng `Contract:`
      của `requirements.md`, bỏ marker ⚠️ (contract), kiểm lại AC9–AC12, AC27, AC29

## Phase 4 — Tests

- [ ] Script `scripts/sync-product-vectors.sh` (dùng chung với `../Memorial/`): chép innerlamp-be
      `docs/product/temple/rules/{temple-open-status,temple-distance}.json` vào
      `app/src/test/resources/product/temple/rules/` kèm file `SOURCE` (commit + `product_spec_version`);
      bản chép không bao giờ sửa tay
- [ ] Chạy mọi case của `rules/temple-open-status.json` (BR-TPL-09) qua `TempleOpenStatusRule` +
      `TempleFormat` — gồm case múi giờ và ngày âm (AC35)
- [ ] Chạy mọi case của `rules/temple-distance.json` (BR-TPL-07) qua `TempleFormat.distanceLabel`; test
      riêng cho `TempleDistance.haversineKm` (AC35)
- [ ] Mapper: `""` → `null`, giờ không đọc được, miền lạ, tháng `null`
- [ ] Repository: success, 404 `TEMPLE_NOT_FOUND`, `NoConnection`, 5xx, `400 INVALID_FILTER`
- [ ] `TempleListViewModel`: debounce, huỷ request cũ, xoá tìm bỏ `q`, đổi miền về trang 1, nạp trang kế
      một request, dừng ở trang cuối, lỗi trang sau giữ danh sách, `SavedStateHandle` (AC5–AC14)
- [ ] `TempleViewModel`: tải / 404 / lỗi; trạng thái mở cửa với `Clock` cố định; khoảng cách với
      `FakeLocationProvider` (có / `null`) (AC17, AC27–AC30)
- [ ] `HomeViewModel`: khối chùa có / trống / lỗi (AC1–AC3)
- [ ] Bỏ `TempleListUiStateTest` (test lọc trên máy — hành vi bị bỏ theo BR-TPL-05)
- [ ] Đo AC36 (NFR-TPL-01) thủ công trên máy thật, 4G, khi backend có `q`/`region`
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (người dùng chạy — xem `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Báo cột Android của innerlamp-be `docs/product/temple/product.md` §4.2 cho `ba-writer` (spec này
      không sửa repo backend)
- [ ] Xoá phần chùa trong `Docs/home_assumptions.md` dòng #1 (ảnh chùa / carousel chùa), #3 (chùa từ
      `HomeSampleData`), #13 (khối chùa) khi Phase 2–3 xong — các dòng đó còn nói về Phật / bài viết nên
      chỉ bỏ phần chùa
- [ ] OKF concept qua `il-okf-writer` khi feature đã kiểm end-to-end

## Deferred (not blocking Temple)

- Bản đọc offline (product §3.6 "may") — chỉ khi product owner muốn.
- Nội dung "Sự kiện & khóa học" khi `sangha` có spec Android.
- Đổi id mẫu (`tu-quang`, `tp-…`) ở Hồ sơ, Lịch âm, Lịch lễ, tab Chùa sang id thật — spec của các feature
  đó.
- Kiểu kiến trúc, vật liệu, điểm nổi bật, "Kinh sách", bộ ảnh khi contract có (BR-TPL-06 "later").

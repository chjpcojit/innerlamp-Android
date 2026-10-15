# Design — Temple

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — UI ba điểm (khối nổi bật, danh bạ, trang chùa) có trên dữ liệu mẫu; domain theo contract, repository, data layer cho `top`/danh sách/chi tiết, giờ mở cửa, khoảng cách, chỉ đường chưa có; tìm/lọc và trường mới chờ contract

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Ba điểm vào một danh bạ chùa công khai, chỉ đọc: khối "Chùa Việt Nam" ở Trang chủ (`HomeViewModel`),
màn "Tất cả chùa" (`TempleListScreen`) và "Trang chùa" (`TempleScreen`). Dữ liệu đi
`ViewModel → use case → TempleRepository (domain) → TempleRepositoryImpl (data) → NetworkClient.safeCall
→ TempleApiService`; không Bearer, không `X-API-Key`. Kiểu **online-only**: mỗi lần mở là một lần gọi,
không Room, không bản offline (product §3.6 chỉ "may" — chọn không làm ở v1.0). Thứ tự, tìm và lọc do
server làm; app chỉ tự tính hai thứ, thuần Kotlin trong `domain/temple/`: trạng thái mở cửa theo giờ Việt
Nam + âm lịch (BR-TPL-09, dùng `domain/calendar/LunarCalendar` đã có) và khoảng cách haversine
(BR-TPL-07). Vị trí đi qua một port `domain/location/LocationProvider`, impl ở `core/location/` bằng
`android.location.LocationManager` (không thêm Play Services).

Hiện trạng (đọc ngày 2026-10-15): cả ba điểm đọc `GetTemplesUseCase` / `GetTempleDetailUseCase` trả dữ
liệu mẫu đồng bộ; `TempleListUiState` lọc trên máy; `TempleScreen` là bố cục cũ của prototype (Theo dõi,
Cúng dường, thống kê, thời khóa). Đã có và dùng lại: `TempleApiService` (`getOptions`),
`TempleOptionRepositoryImpl`, `di/TempleModule.kt` — mẫu gần nhất cho repository mới. Coil 3
(`libs.coil.compose`, `libs.coil.network.okhttp`) đã khai báo trong `app/build.gradle.kts` nhưng chưa
dùng ở đâu. `GetTemplesUseCase` còn được `GetFestivalCalendarUseCase` và `ChuaSampleData` dùng làm dữ
liệu mẫu — feature này thôi dùng nó nhưng **không xoá** tới khi hai feature đó đổi nguồn.

Kiến trúc giữ nguyên `ui → domain ← data → core/{network,storage}`, DI thủ công trong `di/`, đổi thread
qua `DispatcherProvider`, "bây giờ" qua `java.time.Clock` được inject (test cố định được giờ).

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-15.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`TemplesSection`, `TempleCard`), `HomeSkeletons.kt` | Thẻ: ảnh `thumbnail_url` (Coil `AsyncImage`, giữ chỗ khi `null`/lỗi), tên, loại, tỉnh, cấp di tích, hệ phái (AC4); trống (AC2); lỗi + "Thử lại" (AC3) | có — cần sửa |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt` | Khối chùa có trạng thái riêng (đang tải / có / trống / lỗi) đọc từ `GetFeaturedTemplesUseCase`; Phật và bài viết giữ nguồn hiện tại; bỏ `temples` khỏi `HomeSampleData` | có — cần sửa |
| UI / Directory screen | `ui/screens/templelist/TempleListScreen.kt` | Ô tìm (nút "Xóa"), bốn chip cố định (AC12), dòng số chùa tìm thấy, `LazyColumn` nạp trang kế khi còn ~3 mục cuối (AC6), lỗi trang đầu / lỗi trang sau / không khớp (AC7, AC8, AC10) | có — cần sửa |
| UI / Directory state | `ui/screens/templelist/TempleListUiState.kt` | `query`, `region: TempleRegion?`, `items`, `total`, `page`, `totalPages`, `loadState` (đầu / thêm / lỗi đầu / lỗi thêm / xong); **bỏ** `filtered`, `regions` suy từ dữ liệu, `foldVietnamese` | có — viết lại |
| UI / Directory VM | `ui/screens/templelist/TempleListViewModel.kt` | `SavedStateHandle` giữ `query`/`region` (AC14); `queryFlow.debounce(300 ms)` + `distinctUntilChanged` + `collectLatest` (huỷ request cũ, AC9); nạp trang kế một lúc một request (AC6) | có — viết lại |
| UI / Temple screen | `ui/screens/temple/TempleScreen.kt` (+ tách `TempleSections.kt` nếu dài) | Các phần theo FR-TPL-007 (AC18–AC26); hàng nút chỉ "Chỉ đường" (AC25); trạng thái tải / lỗi / không tìm thấy (AC17); xin quyền vị trí bằng `rememberLauncherForActivityResult(RequestMultiplePermissions)` (AC31); `LifecycleResumeEffect` + ticker phút để tính lại giờ (AC28) | có — viết lại |
| UI / Temple state | `ui/screens/temple/TempleUiState.kt` | `loadState` (đang tải / có / không tìm thấy / lỗi mất mạng / lỗi khác), `detail`, `openStatus`, `distanceKm: Double?`, `introExpanded`; **bỏ** `isFollowing`, `followLabel` | có — viết lại |
| UI / Temple VM | `ui/screens/temple/TempleViewModel.kt` | `templeId` từ `SavedStateHandle`; gọi `GetTempleDetailUseCase`; `refreshOpenStatus()` qua `TempleOpenStatusRule` + `Clock`; `onLocationPermissionResult()` → `LocationProvider` → `TempleDistance`; **bỏ** `toggleFollow` | có — viết lại |
| UI / Format | `ui/screens/temple/TempleFormat.kt` | Chuỗi hiển thị: nhãn trạng thái (`Đang mở cửa · đóng lúc HH:MM` …, BR-TPL-09), "Cách bạn X km" (dấu phẩy thập phân, BR-TPL-07), "Cập nhật MM/YYYY", số thứ tự "01", "Đang cập nhật"/"Không có"; nhãn `type`/`sect`/`heritage_rank`/miền (⚠️ product, AC4) | chưa tạo |
| UI / Intents | `ui/screens/temple/TempleIntents.kt` | `geo:lat,lng?q=lat,lng(name)`; fallback `https://www.google.com/maps/dir/?api=1&destination=lat,lng`; `ACTION_DIAL tel:`; `ACTION_VIEW https:`; `ACTION_SENDTO mailto:`; bắt `ActivityNotFoundException` (AC33, AC34) | chưa tạo |
| UI / Navigation | `ui/navigation/Routes.kt` (`TempleListRoute`, `TempleRoute(templeId)`), wiring trong `MainActivity.kt` | Giữ route; bỏ callback `onOffer`, `onSchedule`; nối `onDirections` → `TempleIntents`; ViewModel tạo qua factory của `di/TempleModule` | có — cần sửa |
| UI / Navigation (feature khác) | `MainActivity.kt` — Hồ sơ, Lịch âm (`TempleRoute("tu-quang")`), Lịch lễ, tab Chùa | Truyền id mẫu; với backend thật sẽ ra "không tìm thấy" (AC17) — sửa ở spec của feature đó | ngoài spec này |
| Manifest | `AndroidManifest.xml` | `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` (AC31); `<queries>` cho `geo`, `tel`, `mailto`, `https` để `resolveActivity` thấy app (Android 11+) | có — cần sửa |
| Domain / Model | `domain/model/Temple.kt` | `Temple` (summary: `id`, `name`, `type`, `region: TempleRegion`, `thumbnailUrl`, `imageUrl`, `sect`, `heritageRank`, `establishedYear`, `location`), `TempleLocation` (`lat`, `lng`, `province`, `ward?`, `fullAddress`), `TempleRegion { North, Central, South }` (BR-TPL-04), `TemplePage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`). Thay `Temple` cũ ở `HomeContent.kt` (`place`, `region: String`, `note`) | có — thay |
| Domain / Model | `domain/model/TempleContent.kt` → `TempleDetail` | Summary + `aliases`, `founder`, `currentAbbot`, `sanghaBody`, `hours: OpeningHours?`, `holidayHours: OpeningHours?`, `hoursText` (nguyên văn, AC29), `amenities`, `introduction`, `architectureHeritage`, `visitingNotes`, `contacts` (`hotline`, `fanpage`, `website`, `email`, đều `String?`), `contentUpdatedMonth: YearMonth?`; chuỗi rỗng của contract → `null`. **Bỏ** `TempleStat`, `TempleSchedule`, `TempleTone`, `TempleGlyph` (nội dung `sangha`) | có — viết lại |
| Domain / Rule | `domain/temple/TempleOpenStatusRule.kt` | `evaluate(now: Instant, hours, holidayHours): TempleOpenStatus` — đổi `now` sang `Asia/Ho_Chi_Minh`, ngày âm qua `LunarCalendar.toLunar`, áp BR-TPL-09; trả `Hidden` / `Open(closesAt)` / `Closed(reopensAt)` + `todayRow` | chưa tạo |
| Domain / Rule | `domain/temple/TempleDistance.kt` | `haversineKm(from, to)` (bán kính 6371 km theo mô tả vector) | chưa tạo |
| Domain / Port | `domain/location/LocationProvider.kt` | `suspend fun current(): GeoPoint?` — `null` khi không có quyền / tắt định vị / quá hạn | chưa tạo |
| Domain / Repository | `domain/repository/TempleRepository.kt` | `getFeatured(): Result<List<Temple>>`, `getPage(page: Int, query: String?, region: TempleRegion?): Result<TemplePage>`, `getDetail(id: String): Result<TempleDetail>`; lỗi 404 → `TempleNotFoundException` | chưa tạo |
| Domain / Repository | `domain/repository/TempleOptionRepository.kt` | Danh sách rút gọn (FR-TPL-005) | có |
| Domain / UseCase | `domain/usecase/GetFeaturedTemplesUseCase.kt`, `GetTemplePageUseCase.kt`, `GetTempleDetailUseCase.kt` | `operator fun invoke` → `Result`; dispatcher qua `DispatcherProvider`; `GetTemplePageUseCase` trim + cắt 100 + rỗng → `null` (AC9, AC11). `GetTempleDetailUseCase` hiện trả mẫu → viết lại | có (detail, mẫu) — viết lại; còn lại chưa tạo |
| Domain / UseCase | `domain/usecase/GetTemplesUseCase.kt` | Dữ liệu mẫu — feature này thôi dùng; giữ cho `GetFestivalCalendarUseCase`, `ChuaSampleData` | có — giữ |
| Data / Remote | `data/remote/api/TempleApiService.kt` | Thêm `getTop()`, `getList(page, q: String?, region: String?)`, `getDetail(id)`; `getOptions` giữ nguyên | có — cần sửa |
| Data / Remote | `data/remote/dto/TempleDto.kt` | `@Serializable` `TempleSummaryDto`, `TempleLocationDto`, `TemplePageDto`, `TempleDetailDto`, `TempleAmenitiesDto`, `TempleContactsDto` theo `openapi.yaml` (trường mới ⚠️ contract, khai báo nullable/default để parse được cả bản cũ — `JsonProvider` đã `ignoreUnknownKeys`) | chưa tạo |
| Data / Mapper | `data/mapper/TempleMapper.kt` | DTO → domain: `region` → `TempleRegion` (giá trị lạ → bỏ mục + log không chứa dữ liệu người dùng); `""` → `null`; `"HH:MM - HH:MM"` → `OpeningHours` (không đọc được → `null`, giữ `hoursText`, AC29); `YYYY-MM` → `YearMonth` | chưa tạo |
| Data / Repository | `data/repository/TempleRepositoryImpl.kt` | Gọi API qua `NetworkClient.safeCall`; `AppError.ApiError(code = TEMPLE_NOT_FOUND)` → `TempleNotFoundException`; còn lại `NetworkException(AppError)`; không cache | chưa tạo |
| core/location | `core/location/AndroidLocationProvider.kt` | `ContextCompat.checkSelfPermission` (fine **hoặc** coarse); `LocationManager.getCurrentLocation(provider, CancellationSignal, executor, consumer)` (API 30+, minSdk 31) với hạn 5 s, fallback `getLastKnownLocation`; không lưu, không log (AC32) | chưa tạo |
| DI | `di/TempleModule.kt` | Thêm `provideTempleRepository()`, use case, `provideLocationProvider(context)`, factory cho `TempleListViewModel`, `TempleViewModel` và nguồn chùa của `HomeViewModel` | có — cần sửa |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Vector BR-TPL-07, BR-TPL-09; mapper; repository; ViewModel (debounce, huỷ request cũ, phân trang, lỗi); `TempleOptionRepositoryImplTest` (có); `TempleListUiStateTest` (có — test lọc trên máy, sẽ bỏ) | có một phần |

## Navigation flow

```
HomeRoute (khối "Chùa Việt Nam" ← GET /v1/temples/top)
  ├─ (bấm thẻ) ──▶ TempleRoute(templeId)
  └─ (Xem tất cả) ──▶ TempleListRoute (GET /v1/temples, trang 1…n; q, region)
                         ├─ (bấm thẻ) ──▶ TempleRoute(templeId)
                         └─ (back) ──▶ HomeRoute
TempleRoute(templeId)  (GET /v1/temples/{id})
  ├─ (Chỉ đường) ──▶ app bản đồ (geo:) | trình duyệt Google Maps
  ├─ (Hotline / Fanpage / Website / Email) ──▶ quay số | trình duyệt | mail
  └─ (back, hoặc "quay lại" ở trạng thái không tìm thấy) ──▶ màn trước
ChuaRoute / CalRoute / FestivalsRoute / ProfileRoute ──▶ TempleRoute(id)   (feature khác)
```

Transition mặc định (crossfade) như hiện tại. `TempleListViewModel` gắn vào entry của `TempleListRoute`
nên back từ `TempleRoute` giữ nguyên vị trí cuộn và các trang đã tải (AC15).

## Step-by-step Flow — Danh bạ: tìm, lọc, nạp trang

```
Step 1 — Trang đầu (this feature)
- Trigger: mở TempleListRoute; hoặc query (sau debounce 300 ms) / region đổi; hoặc "Thử lại" ở lỗi trang đầu
- Pre-condition: query đã trim, cắt 100 ký tự; rỗng → null (AC9, AC11)
- Where: TempleListViewModel (collectLatest — request trước bị huỷ) → GetTemplePageUseCase → TempleRepositoryImpl (safeCall)
- API: GET /v1/temples?page=1[&q=][&region=north|central|south]
  ⚠️ Not confirmed (contract): q, region, trang 12 chưa có trong openapi.yaml @ 0eb7d1f — tới khi có,
  gửi page đơn thuần; ô tìm và chip vẫn hiện nhưng gọi cùng request (chưa lọc) — xem "Deliberately stubbed".
    - Success → thay items, total, page = 1, totalPages; total = 0 → "Không tìm thấy chùa" (có q hoặc region) / trống (không có)
    - Failure (NoConnection / Timeout) → lỗi trang đầu, câu mất mạng + "Thử lại" (AC7)
    - Failure (khác, kể cả 400 INVALID_FILTER / INVALID_PAGE) → lỗi trang đầu, câu chung + "Thử lại" (AC7, AC13)

Step 2 — Trang kế tiếp (this feature)
- Trigger: mục hiển thị cuối cùng cách cuối danh sách ≤ 3; hoặc "Thử lại" ở cuối danh sách
- Pre-condition: loadState không phải đang tải; page < totalPages; query/region không đổi từ Step 1 (đổi → về Step 1, huỷ Step 2)
- Where: TempleListViewModel → GetTemplePageUseCase → TempleRepositoryImpl
- API: GET /v1/temples?page=N+1 (cùng q, region)
    - Success → nối items, page = N+1; items rỗng → coi như hết (AC6)
    - Failure (mọi lỗi) → giữ items, "Thử lại" ở cuối (AC8)
```

## Step-by-step Flow — Trang chùa: tải, giờ mở cửa, khoảng cách

```
Step 1 — Tải chùa (this feature)
- Trigger: mở TempleRoute(templeId); hoặc "Thử lại"
- Pre-condition: templeId từ SavedStateHandle
- Where: TempleViewModel → GetTempleDetailUseCase → TempleRepositoryImpl (safeCall)
- API: GET /v1/temples/{id}
    - Success → loadState = có; chạy Step 2; nếu đã có quyền vị trí chạy Step 4
    - Failure (404 TEMPLE_NOT_FOUND) → "không tìm thấy chùa" + quay lại (AC17)
    - Failure (NoConnection / Timeout) → lỗi câu mất mạng + "Thử lại"
    - Failure (khác) → lỗi câu chung + "Thử lại"

Step 2 — Trạng thái mở cửa (this feature, không gọi mạng)
- Trigger: Step 1 thành công; ON_RESUME; sang phút mới (AC28)
- Where: TempleViewModel → TempleOpenStatusRule.evaluate(clock.instant(), hours, holidayHours)
    - hours = null → ẩn trạng thái, dòng giờ "Đang cập nhật" hoặc nguyên văn hoursText (AC27, AC29)
    - còn lại → nhãn qua TempleFormat, đánh dấu "Hôm nay" theo todayRow (⚠️ product: dòng Rằm khi không có holidayHours, AC27)

Step 3 — Xin quyền vị trí (this feature)
- Trigger: Step 1 thành công, app chưa từng xin quyền (cờ prefs `location_permission_asked`), chưa có quyền
  ⚠️ Not confirmed: thời điểm xin quyền (AC31)
- Where: TempleScreen → RequestMultiplePermissions(COARSE, FINE) → TempleViewModel.onLocationPermissionResult; ghi cờ đã xin vào core/storage prefs
    - Cho phép (fine hoặc coarse) → Step 4
    - Từ chối → ẩn dòng khoảng cách, không xin lại

Step 4 — Khoảng cách (this feature, không gọi backend)
- Pre-condition: có quyền vị trí
- Where: TempleViewModel → LocationProvider.current() (AndroidLocationProvider, hạn 5 s) → TempleDistance.haversineKm
    - GeoPoint → distanceKm → TempleFormat.distanceLabel (AC30); vị trí chỉ giữ trong biến cục bộ, không vào UiState/log (AC32)
    - null / quá hạn / lỗi → ẩn dòng
```

## Local storage

- Không Room, không bản offline (online-only, xem Overview). Ảnh: cache mặc định của Coil (bộ nhớ + đĩa
  trong thư mục cache của app).
- `core/storage` prefs: một cờ `location_permission_asked` (Boolean) cho AC31 — không phải bí mật.
- Vị trí người dùng: **không** lưu ở đâu (AC32, product §3.5).
- `query`/`region` của danh bạ: `SavedStateHandle` (AC14), không phải prefs.

## Deliberately stubbed for now

- **Tìm/lọc và trang 12** (⚠️ contract, Phase 3): `TempleApiService.getList` khai báo `q`/`region` nullable
  từ đầu; tới khi backend v2.0 AC 39–44 có trong `openapi.yaml`, `TempleRepositoryImpl` vẫn gửi chúng
  (`TempleHandler.List` hiện chỉ đọc `page`, bỏ qua tham số khác — đã đọc innerlamp-be @ 0eb7d1f) — tức
  danh bạ **chưa lọc** dù UI có ô tìm và chip. Không có fallback
  lọc trên máy (BR-TPL-05, NFR-TPL-02). Cỡ trang luôn đọc từ `page_size` (hiện 20).
- **Trường mới ở trang chùa** (⚠️ contract): DTO khai báo nullable; khi thiếu, giờ Rằm, giáo hội, liên hệ,
  tháng cập nhật hiện theo BR-TPL-06 và chân trang ẩn.
- **Nhãn hiển thị mã** `type`/`sect`/`heritage_rank` (⚠️ product, AC4): `TempleFormat` giữ một bảng tạm
  ghi rõ là tạm, chờ product; mã lạ hiện nguyên mã.
- **Sự kiện & khóa học**: luôn trạng thái trống (FR-TPL-009) tới khi `sangha` có spec Android.
- **UI và data layer**: `top`, danh sách (trang) và chi tiết đã có trong contract nên Phase 3 cho ba
  operation đó không bị chặn — làm cùng đợt với Phase 2, không thêm dữ liệu mẫu mới vào `main`.
  `FakeTempleRepository` chỉ nằm trong `app/src/test/…`; `@Preview` dựng `UiState` trực tiếp cho phần
  stateless (`TempleContent`, …).

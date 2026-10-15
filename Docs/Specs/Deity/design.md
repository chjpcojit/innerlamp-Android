# Design — Deity

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — UI bốn điểm (khối Trang chủ, khối Thư viện, danh mục, trang một vị) có trên 9 hồ sơ mẫu; domain theo contract, repository, data layer, phân trang, Hình tượng, chia sẻ, truyền câu niệm chưa có; trang 12 / `date` / `next_via` / `via_events` chờ backend deity v3.0 vào `openapi.yaml`

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Bốn điểm vào một danh mục công khai, chỉ đọc: khối "Thư viện Phật & Bồ Tát" ở Trang chủ
(`HomeViewModel`), khối "Chư Phật & Bồ Tát" ở Thư viện (`LibraryViewModel`), màn danh mục
(`BuddhaListScreen`) và trang một vị (`BuddhaDetailScreen`). Dữ liệu đi
`ViewModel → use case → DeityRepository (domain) → DeityRepositoryImpl (data) → NetworkClient.safeCall →
DeityApiService`; không Bearer. Kiểu **online-only**: mỗi lần mở là một lần gọi, không Room, không bản
offline (product §3.6 chỉ "may" — chọn không làm ở v1.0, giống `../Temple/`). Thứ tự, lọc, trang, ngày vía
và lần vía kế tiếp đều do server làm; app chỉ: gửi `date` = ngày máy, đổi `next_via.solar_date` thành
đếm ngược qua `domain/calendar/CalendarCountdown` (của `../Calendar/`, BR-CAL-13), và định dạng ngày âm
server trả.

Hiện trạng (đọc ngày 2026-10-15): `GetBuddhaProfilesUseCase` giữ 9 `BuddhaProfile` mẫu (prototype
`BUDDHAS`), `GetBuddhaDetailUseCase` quét `LunarCalendar` 400 ngày để tìm lần vía kế tiếp; Trang chủ dùng
`BuddhaFigure` mẫu trong `HomeSampleData`; `LibraryViewModel` đọc đồng bộ. Mẫu gần nhất cho data layer:
`TempleApiService` + `TempleOptionRepositoryImpl` + `di/TempleModule.kt`; cho danh mục có trang:
`../Temple/design.md` (danh bạ chùa, cùng hình dạng `items/page/page_size/total/total_pages`). Coil 3 đã
khai báo trong `app/build.gradle.kts` (dùng chung kế hoạch với `../Temple/`).

Tên code giữ thư mục `ui/screens/buddha/` và route `BuddhaListRoute`/`BuddhaRoute` (product §1.5); model
domain mới đặt tên `Deity*` theo product và contract, thay `BuddhaProfile`/`BuddhaFigure`.

Kiến trúc giữ nguyên `ui → domain ← data → core/{network,storage}`, DI thủ công trong `di/`, đổi thread
qua `DispatcherProvider`, "hôm nay" qua `java.time.Clock` được inject.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-15.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Home block | `ui/screens/home/HomeSections.kt` (`BuddhasSection`, `BuddhaCard`), `HomeSkeletons.kt` | Thẻ: ảnh `thumbnailUrl` (Coil `AsyncImage`, giữ chỗ khi `null`/lỗi), nhãn loại, `nameVi`, "Vía dd/MM âm lịch" từ `nextVia` (AC1, AC5); thẻ rỗng + "Thử lại" (AC4) | có — cần sửa |
| UI / Home state | `ui/screens/home/HomeUiState.kt`, `HomeViewModel.kt`, `HomeSampleData.kt` | Khối Phật có trạng thái riêng (đang tải / có / trống-lỗi) đọc từ `GetFeaturedDeitiesUseCase`, "Thử lại" chỉ tải lại khối này; bỏ `buddhas` khỏi `HomeSampleData` (cùng hướng `../Temple/` làm với khối chùa) | có — cần sửa |
| UI / Library block | `ui/screens/library/LibraryScreen.kt` (`BuddhaStrip`), `LibraryUiState.kt`, `LibraryViewModel.kt` | Dải thẻ từ `GetFeaturedDeitiesUseCase` (async), trạng thái đang tải / có / trống-lỗi + "Thử lại" (AC2, AC4 ⚠️ câu) | có — cần sửa |
| UI / Catalogue screen | `ui/screens/buddha/BuddhaListScreen.kt` | Header (phụ đề N từ `total`), ba chip cố định (AC9), lưới 2 cột bằng `LazyVerticalGrid` (thay `chunked(2)` trong `verticalScroll`) nạp trang kế khi còn ~4 ô cuối (AC8); trạng thái `buddhasEmpty` (AC10, AC11); "Thử lại" cuối danh sách (AC12); thẻ có `coreVirtue` (AC5) | có — cần sửa |
| UI / Catalogue state | `ui/screens/buddha/BuddhaListUiState.kt` | `type: DeityType?`, `items`, `total`, `page`, `totalPages`, `loadState` (đầu / thêm / trống-lỗi đầu / lỗi thêm / xong); **bỏ** `filtered` (lọc trên máy) và phụ đề đếm cả mẫu | có — viết lại |
| UI / Catalogue VM | `ui/screens/buddha/BuddhaListViewModel.kt` | `SavedStateHandle` giữ `type` (AC13); `typeFlow.collectLatest` (huỷ request cũ, AC9); nạp trang kế một lúc một request (AC8) | có — viết lại |
| UI / Detail screen | `ui/screens/buddha/BuddhaDetailScreen.kt` (+ tách `BuddhaDetailSections.kt` nếu dài) | Các phần theo FR-DTY-005 (AC15); tiểu sử thu gọn khi tràn 6 dòng — dùng `onTextLayout.hasVisualOverflow` để quyết định hiện "Đọc thêm" (AC16); thẻ vía + danh sách ngày vía (AC17, AC18); đại nguyện (AC19); **Hình tượng** (AC20, mới); ba thẻ thần chú + **một** hàng nút (AC21, AC22); kinh liên quan (AC25, AC26); chân trang không "Báo thông tin sai" (AC27); trạng thái tải / lỗi / không tìm thấy (AC14); `LifecycleResumeEffect` báo ViewModel kiểm ngày (AC29) | có — cần sửa nhiều |
| UI / Detail state | `ui/screens/buddha/BuddhaDetailUiState.kt` | `loadState` (đang tải / có / không tìm thấy / lỗi), `detail: DeityDetail?`, `sutras: List<RelatedSutra>` (rỗng = ẩn), `today: LocalDate` (ngày đã gửi), `countdown` qua `CalendarCountdown.of(today, nextVia.solarDate)`, `bioExpanded`, `vowsExpanded`; **bỏ** `vowNoun`/`vowUnit`, `extraVia`, `nextViaLabel` tự tính | có — viết lại |
| UI / Detail VM | `ui/screens/buddha/BuddhaDetailViewModel.kt` | `buddhaId` từ `SavedStateHandle`; gọi song song `GetDeityDetailUseCase(id, today)` và `GetRelatedSutrasUseCase(id)` (Step-by-step Flow); `onResume()` tải lại khi `LocalDate.now(clock) != today` (AC29); ViewModel tạo qua factory của `di/DeityModule` | có — viết lại |
| UI / Format | `ui/screens/buddha/DeityFormat.kt` | Chuỗi hiển thị: nhãn loại BR-DTY-01 (`DeityType.Unknown` → không nhãn, AC6), "Vía dd/MM âm lịch" (⚠️ tháng nhuận, AC5), "THÁNG GIÊNG/n/CHẠP", ngày dương kèm thứ (dùng lại `weekdayFull()` của `ui/screens/home/`), nhãn đếm ngược "Hôm nay / Ngày mai / Còn n ngày" từ `CalendarCountdown`, "Xem đủ N đại nguyện" / "Thu gọn", văn bản chia sẻ (AC24) | chưa tạo (logic đang rải trong `BuddhaDetailUiState`, `BuddhaProfile.viaLabel`) |
| UI / Share | `ui/screens/buddha/DeityShare.kt` | `Intent(ACTION_SEND)` `text/plain` + `Intent.createChooser`; bắt `ActivityNotFoundException` (AC24) | chưa tạo |
| UI / Navigation | `ui/navigation/Routes.kt` (`BuddhaListRoute`, `BuddhaRoute(buddhaId)`, `MalaRoute`), wiring trong `MainActivity.kt` | Giữ hai route deity; `MalaRoute` đổi thành `data class MalaRoute(val mantra: String? = null)` (đăng ký `composable<MalaRoute>`; lối vào cũ từ Trang chủ truyền `null`); `onChant` truyền chú ngắn (AC23); `onShare` gọi `DeityShare` (AC24); `onSutra(id)` → `SutraRoute(id)` (AC25) | có — cần sửa |
| UI / Mala (feature khác, chỉ điểm nhận) | `ui/screens/mala/MalaViewModel.kt`, `MalaUiState.kt` | Đọc `mantra` từ `SavedStateHandle`: có → `MalaUiState.mantra = mantra`, `beads = 0`; `null` → giữ `DefaultMantra` như hiện tại (AC23) | có — cần sửa (nhỏ) |
| Domain / Model | `domain/model/Deity.kt` | `DeityType { Buddha, Bodhisattva, Deva, Unknown }`; `DeitySummary` (`id`, `type`, `nameVi`, `nameSanskrit?`, `nameChinese?`, `titleHonorific?`, `thumbnailUrl?`, `imageUrl?`, `coreVirtue?`, `pureLand?`, `nextVia: DeityVia?`); `DeityVia` (`eventCode`, `name`, `solarDate: LocalDate`, `lunar: LunarDay`); `DeityViaEvent` (`eventCode`, `name`, `next: DeityVia?`); `DeityIconography` (`posture?`, `mountOrThrone?`, `sacredArtifacts`, `symbolicMeaning?`, `isEmpty`); `DeityMantras` (`shortSinoVietnamese?`, `shortSanskrit?`, `dharaniName?`, `dharaniSnippet?`); `DeityDetail` (`summary`, `canonicalSource?`, `biography?`, `greatVows`, `iconography`, `mantras`, `viaEvents`); `DeityPage` (`items`, `page`, `pageSize`, `total`, `totalPages`, `hasMore`); `RelatedSutra` (`id`, `title`, `collection?`). Chuỗi rỗng của contract → `null` (BR-DTY-04). `LunarDay(day, month, year, isLeapMonth)` dùng chung với `../Calendar/` (`domain/model/CalendarEvent.kt`; feature nào làm trước thì tạo) | chưa tạo |
| Domain / Xoá | `domain/model/BuddhaProfile.kt` (`BuddhaCategory`, `BuddhaMantra`, `BuddhaSutraRef`, `BuddhaProfile`, `BuddhaDetail`), `BuddhaFigure` trong `domain/model/HomeContent.kt`, `domain/usecase/GetBuddhaProfilesUseCase.kt` (9 hồ sơ mẫu), `GetBuddhaDetailUseCase.kt` (quét âm lịch 400 ngày) | Dữ liệu mẫu và tính vía trên máy — **drift BR-DTY-05**; xoá khi Phase 2/3 chạy. `domain/calendar/LunarCalendar.kt` giữ (feature khác còn dùng) | có — xoá |
| Domain / Repository | `domain/repository/DeityRepository.kt` | `getFeatured(today: LocalDate): Result<List<DeitySummary>>`, `getPage(page: Int, type: DeityType?, today: LocalDate): Result<DeityPage>`, `getDetail(id: String, today: LocalDate): Result<DeityDetail>`, `getRelatedSutras(id: String): Result<List<RelatedSutra>>` (gộp mọi trang); 404 → `DeityNotFoundException` | chưa tạo |
| Domain / UseCase | `domain/usecase/GetFeaturedDeitiesUseCase.kt`, `GetDeityPageUseCase.kt`, `GetDeityDetailUseCase.kt`, `GetRelatedSutrasUseCase.kt` | `operator fun invoke` → `Result`; dispatcher qua `DispatcherProvider`; `today` lấy từ `Clock` được inject (AC28) và trả kèm kết quả để UI đếm ngược đúng ngày đã gửi | chưa tạo |
| Domain / Rules | `domain/calendar/CalendarCountdown.kt` | BR-CAL-13 — **của `../Calendar/`** (Phase 1 ở đó); spec này chỉ dùng | chưa tạo (ngoài spec này) |
| Data / Remote | `data/remote/api/DeityApiService.kt` | `getTop(date)`, `getList(page, type: String?, date)`, `getDetail(id, date)`, `getSutras(id, page)` — trả `ApiResponse<…>`, không `@Authenticated` | chưa tạo |
| Data / Remote | `data/remote/dto/DeityDto.kt` | `@Serializable` DTO theo `openapi.yaml` + backend deity `requirements.md` v3.0 (bảng "Hình dạng") — phần v3.0 khai báo nullable/default để parse được cả bản contract cũ (`JsonProvider` đã `ignoreUnknownKeys`); **không** khai báo `lunar_memorial_days`. DTO kinh liên quan: chỉ các trường mapper cần của `SutraByDeityItem` | chưa tạo |
| Data / Mapper | `data/mapper/DeityMapper.kt` | DTO → domain: `type` → `DeityType` (lạ → `Unknown`, không bỏ mục — contract "enum mở"); `""` → `null`; `solar_date` `YYYY-MM-DD` → `LocalDate` (không đọc được → coi `next_via`/`next_occurrence` là `null`); `DeityType` → tham số `type` (`Unknown` không bao giờ gửi) | chưa tạo |
| Data / Repository | `data/repository/DeityRepositoryImpl.kt` | Gọi API qua `NetworkClient.safeCall`; `date` = `today.toString()`; `AppError.ApiError(code = DEITY_NOT_FOUND)` → `DeityNotFoundException`; còn lại `NetworkException(AppError)`; `getRelatedSutras` gọi lần lượt trang 1…`total_pages`; không cache | chưa tạo |
| DI | `di/DeityModule.kt` | `provideDeityRepository()`, bốn use case, factory cho `BuddhaListViewModel`, `BuddhaDetailViewModel` và nguồn khối Phật của `HomeViewModel`/`LibraryViewModel` | chưa tạo |
| Tests | `app/src/test/java/com/example/innerlamp/…` | Mapper; repository; ViewModel (phân trang, đổi chip huỷ request cũ, trống/lỗi, chi tiết song song, đổi ngày khi resume); `DeityFormat`; Mala nhận câu niệm; `GetBuddhaDetailUseCaseTest` (có — test quét âm lịch, sẽ bỏ) | có một phần (sẽ bỏ) |

## Navigation flow

```
HomeRoute (khối "Thư viện Phật & Bồ Tát" ← GET /v1/deities/top)
  ├─ (bấm thẻ) ──▶ BuddhaRoute(buddhaId)
  └─ (Xem tất cả) ──▶ BuddhaListRoute
LibraryRoute (khối "Chư Phật & Bồ Tát" ← GET /v1/deities/top; ô hub "Phật & Bồ Tát")
  ├─ (bấm thẻ) ──▶ BuddhaRoute(buddhaId)
  └─ (Xem tất cả | ô hub) ──▶ BuddhaListRoute
BuddhaListRoute (GET /v1/deities, trang 1…n; type)
  ├─ (bấm thẻ) ──▶ BuddhaRoute(buddhaId)
  ├─ (trạng thái trống: Xem lịch lễ vía) ──▶ FestivalsRoute
  └─ (back) ──▶ màn trước
BuddhaRoute(buddhaId) (GET /v1/deities/{id} ‖ GET /v1/deities/{id}/sutras)
  ├─ (Lịch lễ) ──▶ FestivalsRoute
  ├─ (Trì niệm với tràng hạt) ──▶ MalaRoute(mantra = chú ngắn Hán Việt)
  ├─ (Chia sẻ) ──▶ bảng chia sẻ hệ thống (ACTION_SEND)
  ├─ (bấm kinh) ──▶ SutraRoute(sutraId)
  └─ (back, hoặc "quay lại" ở trạng thái không tìm thấy) ──▶ màn trước
```

Transition mặc định (crossfade) như hiện tại. `BuddhaListViewModel` gắn vào entry của `BuddhaListRoute`
nên back từ `BuddhaRoute` giữ vị trí cuộn và các trang đã tải (AC3).

## Step-by-step Flow — Danh mục: trang đầu, đổi chip, trang kế

```
Step 1 — Trang đầu (this feature)
- Trigger: mở BuddhaListRoute; hoặc đổi chip; hoặc "Thử lại" ở trạng thái trống
- Pre-condition: type đọc từ SavedStateHandle (null = "Tất cả"); today = LocalDate.now(clock)
- Where: BuddhaListViewModel (collectLatest — request trước bị huỷ) → GetDeityPageUseCase → DeityRepositoryImpl (safeCall)
- API: GET /v1/deities?page=1[&type=BUDDHA|BODHISATTVA]&date=<today>
  ⚠️ Not confirmed: contract pending — `date`, trang 12 và `next_via` theo backend deity requirements.md
  v3.0 (contract not yet in openapi.yaml); tới khi server có, thẻ không có dòng vía (next_via = null) và
  trang là 20 — app vẫn đọc page_size từ response.
    - Success, total > 0 → thay items, total, page = 1, totalPages; phụ đề "{total} vị · …"
    - Success, total = 0 → trạng thái trống AC10 ("Tất cả": ẩn chip) / AC11 (chip loại: giữ chip)
    - Failure (mọi AppError, kể cả 400 INVALID_PAGE / INVALID_DEITY_TYPE / INVALID_DATE) → trạng thái trống AC10 + "Thử lại"

Step 2 — Trang kế tiếp (this feature)
- Trigger: ô cuối hiển thị cách cuối danh sách ≤ 4; hoặc "Thử lại" ở cuối danh sách
- Pre-condition: không đang tải; page < totalPages; type không đổi từ Step 1 (đổi → về Step 1, huỷ Step 2); dùng cùng today của Step 1
- Where: BuddhaListViewModel → GetDeityPageUseCase → DeityRepositoryImpl
- API: GET /v1/deities?page=N+1 (cùng type, date)
    - Success → nối items, page = N+1; items rỗng → coi như hết (AC8)
    - Failure (mọi lỗi) → giữ items, "Thử lại" ở cuối (AC12)
```

## Step-by-step Flow — Trang một vị: chi tiết, kinh liên quan, đổi ngày

```
Step 1 — Tải chi tiết (this feature)
- Trigger: mở BuddhaRoute(buddhaId); "Thử lại"; hoặc Step 4
- Pre-condition: buddhaId từ SavedStateHandle; today = LocalDate.now(clock), lưu vào UiState
- Where: BuddhaDetailViewModel → GetDeityDetailUseCase → DeityRepositoryImpl (safeCall)
- API: GET /v1/deities/{id}?date=<today>
  ⚠️ Not confirmed: contract pending — `next_via`, `via_events`, `date` theo backend deity requirements.md
  v3.0 AC 8, 24–29 (contract not yet in openapi.yaml); tới khi có, thẻ vía ẩn (BR-DTY-04) — app không
  rơi về `lunar_memorial_days` và không tự tính.
    - Success → loadState = có; countdown = CalendarCountdown.of(today, nextVia.solarDate) nếu có nextVia
    - Failure (404 DEITY_NOT_FOUND) → "không tìm thấy" + quay lại (AC14; ⚠️ câu)
    - Failure (khác) → lỗi + "Thử lại" (AC14; ⚠️ câu); Step 2 dù thành công cũng không hiện

Step 2 — Kinh liên quan (this feature, chạy song song với Step 1)
- Trigger: cùng lúc Step 1 (async trong viewModelScope); không chạy lại ở Step 4
- Where: BuddhaDetailViewModel → GetRelatedSutrasUseCase → DeityRepositoryImpl
- API: GET /v1/deities/{id}/sutras?page=1, rồi page=2…total_pages tuần tự (không role)
    - Success → sutras = mọi items theo thứ tự nhận được (rỗng → ẩn phần, AC25)
    - Failure (bất kỳ trang nào, mọi lỗi, kể cả 404) → sutras = rỗng, ẩn phần (AC26; ⚠️ product)

Step 3 — Hành động (this feature, không gọi backend)
- Trì niệm: callback onChant(mantra) → MainActivity navigate(MalaRoute(mantra)) → MalaViewModel đọc SavedStateHandle, beads = 0 (AC23)
- Chia sẻ: DeityFormat.shareText(detail) → DeityShare.open(context, text) (AC24)

Step 4 — Đổi ngày khi quay lại (this feature)
- Trigger: ON_RESUME (LifecycleResumeEffect → BuddhaDetailViewModel.onResume())
- Pre-condition: loadState = có; LocalDate.now(clock) != UiState.today
- Where: như Step 1 với today mới (giữ nội dung cũ trên màn trong lúc tải; lỗi → giữ nội dung cũ, không đổi sang trạng thái lỗi)
```

## Local storage

- Không Room, không prefs, không bản offline (online-only, xem Overview). Ảnh: cache mặc định của Coil.
- Chip đang chọn của danh mục: `SavedStateHandle` (AC13), không phải prefs.
- Câu niệm sang Niệm Phật: route arg `MalaRoute.mantra` (lưu trong back stack, sống qua process death).

## Deliberately stubbed for now

- **Phần v3.0 của contract** (⚠️ contract pending — backend deity requirements.md v3.0, backend
  `tasks.md` Phase 8–10; openapi update là Phase 9): DTO khai báo `next_via`, `via_events` nullable /
  mặc định rỗng và repository luôn gửi `date` (server hiện tại bỏ qua tham số lạ — chưa đối chiếu handler,
  xác nhận khi backend ship). Trong lúc chờ: không dòng vía trên thẻ, không thẻ vía trên trang, trang 20.
  **Không** có fallback tính vía trên máy hay đọc `lunar_memorial_days` (BR-DTY-05). Khi contract vào
  `openapi.yaml`: cập nhật dòng `Contract:` của `requirements.md`, bỏ marker.
- **Câu chữ còn chờ product**: trạng thái trống khối Thư viện, không tìm thấy / lỗi trang một vị, tháng
  nhuận, ngày vía không có lần diễn ra, chia sẻ khi không có chú ngắn, link trong văn bản chia sẻ, dòng phụ
  của kinh — `DeityFormat` giữ giá trị mặc định đề xuất ở `requirements.md`, ghi rõ là tạm.
- **Mở bài kinh**: `SutraRoute(id)` với id backend sẽ không khớp danh mục kinh mẫu của màn kinh tới khi
  feature kinh có data layer (ngoài spec này).
- **UI và data layer**: phần hồ sơ, `type`, `page` và kinh liên quan đã có trong contract nên Phase 3
  không bị chặn cho chúng — làm cùng đợt với Phase 2, không thêm dữ liệu mẫu mới vào `main`.
  `FakeDeityRepository` chỉ nằm trong `app/src/test/…`; `@Preview` dựng `UiState` trực tiếp cho phần
  stateless (`BuddhaListContent`, `BuddhaDetailContent`).

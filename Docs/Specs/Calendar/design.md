# Design — Calendar

Version: 1.0
Last updated: 2026-10-15
Status: in-progress — lịch lễ và khối Home chạy trên danh mục cục bộ; domain mới, màn chi tiết, data layer (API + Room) và vector chưa có; chuyển sang API chờ backend calendar v3.0

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Ba chỗ hiện sự kiện — màn "Lịch lễ Phật giáo" (`FestivalsScreen`), màn "Chi tiết ngày lễ" mới
(`FestivalDetailScreen`) và khối "Sự kiện Phật giáo" ở Trang chủ (`EventsSection`) — cùng đọc một
`CalendarEventRepository` (domain). Đích đến: `ViewModel → use case → CalendarEventRepository →
CalendarEventRepositoryImpl → NetworkClient.safeCall` (`GET /v1/calendar/events`, công khai) và Room cho
bản trên máy. Kiểu **online-first, hiện bản trên máy trước**: có bản thì hiện ngay, đồng thời gọi mạng;
thành công thì thay và ghi Room; thất bại thì giữ bản kèm câu "có thể chưa cập nhật", không có bản thì
báo lỗi + thử lại (FR-CAL-007, FR-CAL-008, NFR-CAL-03). App **không tính ngày lễ nào**: chỉ có ba hàm
thuần trong `domain/calendar/` cho những gì product giao cho client — đếm ngược (BR-CAL-13), so sánh thứ
tự (BR-CAL-12) và chọn dòng của khối Home (BR-CAL-15) — mỗi hàm có vector của product.

Hiện trạng: `GetBuddhistEventMonthsUseCase` tự sinh sự kiện từ 19 lễ vía cố định + Rằm/Mùng Một bằng
`LunarCalendar`; `GetFestivalCalendarUseCase` gắn cờ lễ lớn theo `MAJOR_KEYS` và chùa tổ chức mẫu;
`FestivalsViewModel`, `HomeViewModel`, `ChuaViewModel` gọi chúng đồng bộ trong constructor, không
coroutine, không repository, ViewModel tạo bằng `viewModel()` mặc định. Kiến trúc giữ nguyên `ui →
domain ← data → core/{network,storage}`, DI thủ công trong `di/`, đổi thread qua `DispatcherProvider`.
Mẫu gần nhất: `AdministrativeUnitRepositoryImpl` / `di/AdministrativeUnitModule.kt` (danh mục công khai
qua `safeCall`, một instance dùng chung) và `RoomSutraQuoteRepository` / `di/DatabaseModule.kt` (Room).
`JsonProvider` đã bật `ignoreUnknownKeys`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-15.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Screen | `ui/screens/festivals/FestivalsScreen.kt` | Lịch lễ: chip "Tất cả" / "Lễ lớn" / một chip mỗi nhóm kèm số (AC10); tiêu đề tháng; dòng: ngày + thứ, tên, ngày âm + "(nhuận)", đếm ngược, dấu lễ lớn, nhãn nhóm (AC4, AC7); bấm dòng → chi tiết (AC8); hàng cuối tải năm sau / "Thử lại" (AC12, AC13); trạng thái đang tải / bản trên máy / lỗi (AC23–AC26). **Bỏ** chùa tổ chức, câu giới thiệu về chùa, chip "Rằm & Mùng Một" (AC9, AC10) | có — cần sửa |
| UI / State | `ui/screens/festivals/FestivalsUiState.kt` | `years: List<CalendarYear>`, `groups`, `filter: CalendarFilter` (`All` / `Major` / `Group(code)`), `rows`, `counts` theo chip, `todayRowIndex`, `loadState` (đang tải / online / bản trên máy / lỗi mất mạng / lỗi khác), `nextYearState` (rảnh / đang tải / lỗi / hết — 2100); đếm ngược qua `CalendarCountdown` | có — cần sửa (đang dựng từ `FestivalMonth`) |
| UI / ViewModel | `ui/screens/festivals/FestivalsViewModel.kt` | `LoadCalendarYearUseCase` trong `viewModelScope`: năm hiện tại (VN) rồi năm sau khi chạm cuối; `today` từ `Clock` tiêm vào, tính lại ở `ON_RESUME` (AC20); factory từ `di/CalendarModule.kt` | có — cần sửa |
| UI / Screen | `ui/screens/festivals/FestivalDetailScreen.kt` | Ảnh (Coil `AsyncImage`, fallback placeholder — AC16), nhãn nhóm + dấu lễ lớn, tên, ngày âm, thẻ ngày dương + thứ + đếm ngược, mô tả theo đoạn, lễ nghi đánh số, ẩn mục rỗng (AC14, AC15); không tìm thấy (AC18); không có kinh/chùa/nhắc/trước-sau (AC17) | chưa tạo |
| UI / State + ViewModel | `ui/screens/festivals/FestivalDetailUiState.kt`, `FestivalDetailViewModel.kt` | Nhận `eventCode`, `solarDate` từ `SavedStateHandle.toRoute()`; `GetEventOccurrenceUseCase`; state đang tải / có / không tìm thấy / lỗi tải | chưa tạo |
| UI / Home | `ui/screens/home/HomeSections.kt` (`EventsSection`, `EventMonthCard`, `EventRow`), `HomeUiState.kt`, `HomeViewModel.kt`, `HomeScreen.kt` | Sáu thẻ tháng từ `GetHomeEventBlockUseCase` (AC19); thẻ tháng trống; lỗi thay chỗ thẻ hoặc trên từng thẻ (AC25, AC27); câu bản trên máy (AC24); dòng → `FestivalDetailRoute`, "Tất cả" / "+N" → `FestivalsRoute`; `HomeCallbacks` thêm `onEventDetail(code, date)` | có — cần sửa (12 tháng, không tầng, dòng mở lịch lễ) |
| UI / Tab Chùa | `ui/screens/chua/ChuaViewModel.kt`, `domain/usecase/GetUpcomingFastDaysUseCase.kt` | Khối "Ngày lễ & ngày chay sắp tới": đổi nguồn sang `CalendarEventRepository` (xem ghi chú bên dưới) | có — cần sửa |
| UI / Navigation | `ui/navigation/Routes.kt`, `MainActivity.kt` | `FestivalsRoute` (có); thêm `@Serializable data class FestivalDetailRoute(val eventCode: String, val solarDate: String)` (ISO `YYYY-MM-DD`), đăng ký bằng `composable<…>` với transition tường minh (Navigation design: route `data class`); sửa KDoc của `FestivalsRoute` (đang nói 3 năm + chùa) | có — cần sửa |
| Domain / Model | `domain/model/CalendarEvent.kt` | `CalendarYear(year, groups, events, occurrences)`, `CalendarGroup(code, name)`, `CalendarEvent(code, name, shortDescription, longDescription, guidance, imageUrl?, groupCodes, isMajor, sortOrder, recurrence)`, `Recurrence` (`Solar`, `Lunar`, `LunarMonthly`, `LunarLastDay`, `SolarTerm`), `CalendarOccurrence(eventCode, solarDate: LocalDate, lunar: LunarDay(day, month, year, isLeapMonth))`, `CalendarYearSource` (`Network` / `DeviceCopy`) | chưa tạo |
| Domain / Rules | `domain/calendar/CalendarCountdown.kt` | `of(today, solarDate)` → `days` + nhãn (`Today` / `Tomorrow` / `DaysLeft(n)` / `Passed`) theo BR-CAL-13; UI đổi nhãn ra chữ | chưa tạo (logic đang nằm ở `FestivalsUiState.countdown`) |
| Domain / Rules | `domain/calendar/CalendarOrdering.kt` | `Comparator` BR-CAL-12 (ngày dương, `sortOrder`, `code`) | chưa tạo |
| Domain / Rules | `domain/calendar/HomeEventBlock.kt` | `select(monthOccurrences)` → `shown` (≤ 3) + `more` theo BR-CAL-15; `BUDDHIST_GROUP = "buddhist"` (⚠️ AC19); tầng 3 = `Recurrence.LunarMonthly` | chưa tạo |
| Domain / Content | `domain/calendar/EventContentFormat.kt` | `paragraphs(longDescription)`, `practices(guidance)` theo BR-CAL-14 (tách theo xuống dòng, bỏ dấu đầu dòng, bỏ dòng rỗng) | chưa tạo |
| Domain / Repository | `domain/repository/CalendarEventRepository.kt` | `deviceCopy(year): CalendarYear?`, `load(year): Result<CalendarYearLoad>` (dữ liệu + `source` + cờ "có thể chưa cập nhật"), `cachedInMemory(year)`; lỗi domain `CalendarLoadError.Offline` / `Failed` | chưa tạo |
| Domain / UseCase | `domain/usecase/LoadCalendarYearUseCase.kt`, `GetHomeEventBlockUseCase.kt`, `GetEventOccurrenceUseCase.kt` | `Flow`/`Result`; năm hiện tại theo `Asia/Ho_Chi_Minh` (AC2); Home: tháng hiện tại theo ngày máy + 5 tháng, tải một hoặc hai năm song song (AC19, AC27); chi tiết: tìm `(code, date)` trong năm (AC14, AC18); dispatcher qua `DispatcherProvider` | chưa tạo |
| Domain / Xoá | `domain/usecase/GetBuddhistEventMonthsUseCase.kt`, `GetFestivalCalendarUseCase.kt`, `domain/model/FestivalCalendar.kt`, `BuddhistEvent`/`EventMonth` trong `domain/model/HomeContent.kt` | Danh mục + tính cục bộ, `MAJOR_KEYS`, chùa mẫu — xoá khi Phase 3 chạy (BR-CAL-05) | có — xoá |
| Domain / Giữ | `domain/calendar/LunarCalendar.kt` | Không dùng cho feature này nữa; giữ cho Sổ giỗ (BR-MEM-10) và màn Lịch âm | có — không đổi |
| Data / Remote | `data/remote/api/CalendarApiService.kt`, `data/remote/dto/CalendarEventsDto.kt` | `@GET("v1/calendar/events") suspend fun getEvents(@Query("year") year: Int): ApiResponse<CalendarEventsPageDto>`; **không** `@Authenticated`; DTO `@Serializable` theo `openapi.yaml` (`calendarEvents`) + `is_major`, `sort_order`, `recurrence` theo backend design v3.0 (⚠️ contract) | chưa tạo |
| Data / Mapper | `data/mapper/CalendarEventMapper.kt` | DTO ↔ domain; `recurrence` lạ → `null` tầng (xử lý như sự kiện có tên); mã nhóm không có trong `groups` → bỏ qua (như backend design Step 4) | chưa tạo |
| Data / Local | `core/database/calendar/CalendarCacheDatabase.kt`, `CalendarYearCacheEntity.kt`, `CalendarYearCacheDao.kt` | DB riêng `innerlamp_calendar.db`: một dòng mỗi năm `(year PK, payload_json, fetched_at_epoch_ms)`; `upsert`, `get(year)`, `deleteBefore(year)`; `fallbackToDestructiveMigration` (chỉ là cache) | chưa tạo |
| Data / Repository | `data/repository/CalendarEventRepositoryImpl.kt` | Bộ nhớ (`ConcurrentHashMap<Int, CalendarYear>`) → Room → `NetworkClient.safeCall`; ghi Room sau khi mạng thành công + xoá năm cũ (AC22); map `AppError` → `CalendarLoadError` theo bảng Error → UI | chưa tạo |
| DI | `di/CalendarModule.kt` | Một instance repository cho cả app (giữ ở `InnerLampApplication` như ghi chú của `AdministrativeUnitModule`); factory cho `FestivalsViewModel`, `FestivalDetailViewModel`, `HomeViewModel`, `ChuaViewModel` | chưa tạo |
| Tests | `app/src/test/java/com/example/innerlamp/…`, `app/src/test/resources/product/calendar/rules/`, `scripts/sync-product-vectors.sh` | Vector (countdown, ordering, home-block), mapper, repository, use case, ViewModel | có một phần (`FestivalsViewModelTest`, `GetBuddhistEventMonthsUseCaseTest`, `HomeViewModelTest` — test danh mục cục bộ, sẽ viết lại/xoá) |

> ⚠️ Not confirmed: tên thư mục — mặc định đã chọn: **giữ** `ui/screens/festivals/` cho danh sách và chi
> tiết (đặt `FestivalDetail*` cạnh `Festivals*`), còn domain/data dùng tên `Calendar*`, vì
> `ui/screens/calendar/` đã là màn Lịch âm; đổi tên package là Deferred. Merge PR = duyệt.

**Khối "Ngày lễ & ngày chay sắp tới" ở tab Chùa** (`ChuaScreen.FastDaysSection`, prototype `chuaVals`)
cũng ăn danh mục cục bộ qua `GetUpcomingFastDaysUseCase`. Product spec không nói tới khối này.

> ⚠️ Not confirmed (product): khối "Ngày lễ & ngày chay sắp tới" ở tab Chùa có thuộc feature calendar
> không, và hiện gì (nhóm nào, mấy dòng, dòng "Gợi ý") — mặc định Android: giữ nguyên hiển thị hiện tại,
> chỉ đổi nguồn sang `CalendarEventRepository` (4 lần diễn ra nhóm Phật giáo đầu tiên từ hôm nay, thứ tự
> BR-CAL-12, Rằm/Mùng Một nhận ra bằng `Recurrence.LunarMonthly`) để xoá được danh mục cục bộ; nút "Nhắc
> tôi" không đổi (ngoài phạm vi). Hand back to ba-writer in innerlamp-be.

## Navigation flow

```
HomeRoute
  ├─ khối "Sự kiện Phật giáo"
  │    (bấm một dòng) ──▶ FestivalDetailRoute(eventCode, solarDate)
  │    ("Tất cả" / "+N sự kiện khác") ──▶ FestivalsRoute (cuộn tới tháng hiện tại)
  │    ("Thử lại") ──▶ tải lại năm thiếu, ở lại Home
ChuaRoute ("Lịch lễ", dòng ngày lễ/ngày chay) ──▶ FestivalsRoute
BuddhaRoute ("Lịch lễ") ──▶ FestivalsRoute
FestivalsRoute
  (mở) ──▶ bản trên máy nếu có ──▶ LoadCalendarYear(năm hiện tại VN) ──▶ online | bản trên máy + câu báo | lỗi + Thử lại
  (chọn chip) ──▶ lọc tại chỗ, cuộn về tháng hiện tại
  (chạm cuối danh sách, năm < 2100) ──▶ LoadCalendarYear(năm + 1) ──▶ nối | hàng "Thử lại"
  (bấm một dòng) ──▶ FestivalDetailRoute(eventCode, solarDate)
  (back) ──▶ màn trước
FestivalDetailRoute
  (mở) ──▶ GetEventOccurrence ──▶ chi tiết | không tìm thấy (Quay lại) | lỗi tải (Thử lại)
  (back / Quay lại) ──▶ màn trước
```

Bỏ: `FestivalsCallbacks.onTemple` (chip chùa → `TempleRoute`). Route chi tiết mang `solarDate` dạng
chuỗi ISO để `toRoute()` không cần type map cho `LocalDate`.

## Step-by-step Flow — Tải một năm (lịch lễ, Home, chi tiết)

```
Step 1 — Hiện bản đang có (this feature)
- Trigger: FestivalsViewModel init / chạm cuối danh sách / "Thử lại"; HomeViewModel init; FestivalDetailViewModel init
- Pre-condition: year trong [năm hiện tại theo Asia/Ho_Chi_Minh, 2100] (AC2, AC13)
- Where: ViewModel → LoadCalendarYearUseCase → CalendarEventRepositoryImpl
    - Có trong bộ nhớ (đã tải thành công trong phiên này) → trả luôn, dừng (không gọi mạng lại)
    - Có trong Room (CalendarYearCacheDao.get) → phát CalendarYear(source = DeviceCopy) để UI hiện
      ngay, chưa hiện câu báo (AC23); Step 2
    - Không có → UI ở trạng thái đang tải; Step 2

Step 2 — Gọi backend (this feature)
- Where: CalendarEventRepositoryImpl → NetworkClient.safeCall { CalendarApiService.getEvents(year) }
- API: GET /v1/calendar/events?year=YYYY (calendarEvents; không Bearer, không X-API-Key)
    - Success → Step 3
    - Failure (bất kỳ — NoConnection / Timeout / HttpError 429, 5xx / Unauthorized của backend
      trước v3.0 / ApiError / Serialization / Unknown), có bản Room → giữ bản đó, cờ "có thể chưa
      cập nhật" (AC24, AC28)
    - Failure (NoConnection / Timeout), không có bản → CalendarLoadError.Offline → câu mất mạng +
      "Thử lại" (AC25)
    - Failure (khác), không có bản → CalendarLoadError.Failed → câu lỗi chung + "Thử lại" (AC25, AC28)
    - Năm sau (lịch lễ) thất bại, không có bản → giữ danh sách, hàng "Thử lại" ở cuối (AC12)

Step 3 — Ghi bản trên máy (this feature)
- Where: CalendarEventRepositoryImpl → CalendarYearCacheDao.upsert(year, payload, now) rồi
  deleteBefore(năm hiện tại) trong một transaction; lưu vào bộ nhớ
    - Success → trả CalendarYear(source = Network)
    - Failure (lỗi Room) → vẫn trả dữ liệu vừa tải; log, không báo người dùng

Step 4 — Hiển thị (this feature)
- Where: ViewModel
    - Lịch lễ: nối năm, áp CalendarFilter, đếm theo chip, CalendarCountdown(today, date) cho từng dòng
    - Home: GetHomeEventBlockUseCase cắt sáu tháng từ tháng hiện tại, HomeEventBlock.select mỗi tháng
      (cần một hoặc hai năm — năm sau tải song song; thiếu năm nào thì thẻ tháng của năm đó báo lỗi, AC27)
    - Chi tiết: GetEventOccurrenceUseCase tìm (eventCode, solarDate) → có | không tìm thấy (AC18)
```

Gộp nhiều năm: mỗi năm server đã sắp theo BR-CAL-12 và các năm nối liên tiếp nên thứ tự giữ nguyên;
`CalendarOrdering` dùng cho chọn tầng ở Home và để chạy `rules/ordering.json`.

## Local storage

| Dữ liệu | Nơi lưu | Vì sao |
|---|---|---|
| Bản của từng năm đã tải (payload `data` của `calendarEvents`, dạng JSON) | Room, DB riêng `innerlamp_calendar.db`, bảng `calendar_year_cache` | Dữ liệu có cấu trúc → Room (`AGENTS.md`); giữ payload JSON một dòng mỗi năm để thêm field contract (v3.0) không cần migration; DB riêng để `fallbackToDestructiveMigration` an toàn (chỉ là cache) và không đụng `innerlamp.db`; nội dung công khai nên không cần loại khỏi backup (§3.5) |
| Năm đang hiện / đã tải trong phiên | Bộ nhớ của repository (một instance cho cả app) | Lịch lễ, Home, chi tiết dùng chung, không gọi lại mạng trong phiên |
| Ảnh sự kiện | Cache của Coil | Chưa có ảnh (seed `null`) |

Không có gì ở `core/storage` prefs hay secure storage. Không có HTTP cache của OkHttp.

> ⚠️ Not confirmed: cách lưu bản trên máy — mặc định đã chọn: Room DB riêng `innerlamp_calendar.db`, một
> dòng JSON mỗi năm, giữ năm hiện tại trở đi (xoá năm cũ), không giới hạn số năm (tối đa tới 2100, mỗi
> năm vài chục KB); không bật HTTP cache của OkHttp dù backend v3.0 trả `Cache-Control: public`. Merge
> PR = duyệt.

## Ghi chú kỹ thuật

- **Năm hiện tại vs hôm nay**: năm để tải lịch lễ theo `Asia/Ho_Chi_Minh` (FR-CAL-002); đếm ngược và
  tháng hiện tại của khối Home theo ngày của máy (BR-CAL-13, BR-CAL-15). Hai cái chỉ lệch nhau quanh giao
  thừa dương lịch khi máy ở múi giờ khác.
- **Trước backend v3.0**: route còn `bearerAuth` → app (không gắn token) nhận 401 → AC28. Vì vậy Phase 3
  không bật cho người dùng tới khi backend Phase 18 lên `dev`; trong lúc đó UI chạy trên fake repository
  dựng từ một bản JSON mẫu theo contract (Phase 2), không dùng lại danh mục cục bộ.
- **Không còn quy tắc lịch nào ở client cho feature này**: không `LunarCalendar.toLunar` cho sự kiện,
  không ngày 30 → 29, không tháng nhuận — ngày âm đọc thẳng từ `lunar_date`.
- **Nhãn nhóm**: tên lấy từ `data.groups` (không hard-code tên); chỉ mã `buddhist` là hằng (⚠️ AC19).
- **`recurrence` lạ** (backend thêm kind mới): mapper giữ sự kiện, coi như sự kiện có tên (tầng 2 ở Home).
- **Chuỗi đếm ngược**: "Còn n ngày" theo BR-CAL-13 (code hiện viết thường "còn").
- **Tiếng Việt**: app chỉ có tiếng Việt; không gửi tham số ngôn ngữ (server dùng `calendar.default_language`).

## Deliberately stubbed for now

- `GetBuddhistEventMonthsUseCase` / `GetFestivalCalendarUseCase` vẫn là nguồn của lịch lễ, Home và tab
  Chùa cho tới Phase 3 — đây là drift BR-CAL-01…07, BR-CAL-05 (product §4.2), không phải đích.
- Chùa tổ chức trên lịch lễ là dữ liệu mẫu (`hostsFor`) — bỏ ở Phase 2 (AC9).
- Fake `CalendarEventRepository` (bản JSON mẫu theo contract, gồm `is_major`/`sort_order`/`recurrence`)
  cho Phase 2 trong lúc chờ backend v3.0; thay bằng impl thật ở Phase 3.

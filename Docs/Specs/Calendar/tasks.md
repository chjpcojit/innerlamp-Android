# Tasks — Calendar

Version: 1.1
Last updated: 2026-10-15
Status: in-progress — lối vào, carousel Home và danh sách theo tháng có trên danh mục cục bộ; domain mới, chi tiết, API + bản trên máy, vector còn `[ ]`; Phase 3 chờ backend calendar v3.0

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
`[x]` chỉ khi đã kiểm tra tồn tại **và đúng như product spec v1.1 yêu cầu** trong code ngày 2026-10-15
(`main` @ bebaa9c); mục có code nhưng lệch product để `[ ]` và ghi "(có code, lệch: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 1 — Domain

- [ ] `domain/model/CalendarEvent.kt` — `CalendarYear`, `CalendarGroup`, `CalendarEvent` (có `isMajor`,
      `sortOrder`, `recurrence`), `Recurrence`, `CalendarOccurrence`, `LunarDay` (có `isLeapMonth`),
      `CalendarYearSource`
- [ ] `domain/calendar/CalendarCountdown.kt` — BR-CAL-13 (AC21; có code, lệch: logic nằm trong
      `FestivalsUiState.countdown`, chữ "còn" viết thường)
- [ ] `domain/calendar/CalendarOrdering.kt` — comparator BR-CAL-12 (AC5)
- [ ] `domain/calendar/HomeEventBlock.kt` — chọn dòng + "+N" theo BR-CAL-15, `BUDDHIST_GROUP`
      (AC19; ⚠️ mã nhóm — mặc định `buddhist`)
- [ ] `domain/calendar/EventContentFormat.kt` — đoạn mô tả, mục lễ nghi theo BR-CAL-14 (AC15)
- [ ] `domain/repository/CalendarEventRepository.kt` (interface: `deviceCopy`, `load`, `cachedInMemory`;
      `CalendarLoadError.Offline` / `Failed`)
- [ ] `domain/usecase/LoadCalendarYearUseCase.kt` — năm hiện tại theo `Asia/Ho_Chi_Minh`, giới hạn 2100
      (AC2, AC12, AC13; Step 1–2)
- [ ] `domain/usecase/GetHomeEventBlockUseCase.kt` — sáu tháng từ tháng hiện tại, một hoặc hai năm song
      song, thiếu năm → lỗi theo thẻ (AC19, AC27; ⚠️ product)
- [ ] `domain/usecase/GetEventOccurrenceUseCase.kt` — tìm `(eventCode, solarDate)` trong năm (AC14, AC18)
- [ ] Xoá `GetBuddhistEventMonthsUseCase.kt`, `GetFestivalCalendarUseCase.kt` (gồm `MAJOR_KEYS`,
      `hostsFor`), `domain/model/FestivalCalendar.kt`, `BuddhistEvent`/`EventMonth` trong
      `HomeContent.kt` — chỉ sau khi Phase 3 thay nguồn (AC3; BR-CAL-05)
- [ ] `GetUpcomingFastDaysUseCase` đọc `CalendarEventRepository` thay danh mục cục bộ (⚠️ product —
      mặc định giữ hiển thị hiện tại; xem `design.md`)

## Phase 2 — UI (`il-ui-builder`)

- [x] `FestivalsRoute` + wiring trong `MainActivity.kt`; lối vào từ Trang chủ ("Tất cả", "+N"),
      tab Chùa ("Lịch lễ", dòng ngày lễ) và chi tiết Phật/Bồ Tát ("Lịch lễ") (AC6 phần lối vào)
- [x] Không đòi đăng nhập ở lịch lễ và khối Home (AC1 phần truy cập — giữ khi chuyển sang API)
- [x] Danh sách có tiêu đề "Tháng M, YYYY", mở và đổi chip thì cuộn tới tháng hiện tại (AC6 phần cuộn)
- [x] Carousel thẻ tháng ở Home: tối đa 3 dòng, "+N sự kiện khác", "Tất cả" → `FestivalsRoute`
      (AC19 phần bố cục)
- [ ] Lịch lễ theo năm: năm hiện tại, tải năm sau khi chạm cuối, dừng ở 2100, hàng "Thử lại" (AC6, AC12,
      AC13; có code, lệch: tải sẵn 3 năm, bỏ tháng trống)
- [ ] Dòng lịch lễ: ngày âm có "(nhuận)", dấu lễ lớn từ `isMajor`, nhãn nhóm theo `data.groups`, bấm mở
      chi tiết (AC4, AC7, AC8; có code, lệch: không nhãn nhóm, màu theo `isFestival`, không bấm được)
- [ ] Bỏ chùa tổ chức (số chùa, chip chùa, `FestivalsCallbacks.onTemple`) và câu giới thiệu về chùa (AC9;
      có code, lệch: dữ liệu chùa mẫu)
- [ ] Chip "Tất cả" / "Lễ lớn" / mỗi nhóm kèm số; bỏ "Rằm & Mùng Một"; giữ chip khi nối năm (AC10, AC11;
      có code, lệch: "Lễ lớn" lọc `isFestival`)
- [ ] `FestivalsUiState` / `FestivalsViewModel`: `loadState`, `nextYearState`, `CalendarFilter`, factory từ
      `di/CalendarModule.kt` thay `viewModel()` mặc định; tính lại ở `ON_RESUME` khi đổi ngày (AC20)
- [ ] Trạng thái đang tải, bản trên máy + câu "có thể chưa cập nhật", lỗi mất mạng / lỗi khác + "Thử lại"
      với câu ở bảng Error → UI (AC23–AC26; ⚠️ câu chữ product)
- [ ] `FestivalDetailRoute(eventCode, solarDate)` trong `Routes.kt` + `composable<…>` với transition
      tường minh trong `MainActivity.kt`; sửa KDoc `FestivalsRoute`
- [ ] `FestivalDetailScreen` + `FestivalDetailUiState` + `FestivalDetailViewModel`: ảnh (Coil + fallback
      placeholder), nhóm + lễ lớn, tên, ngày âm, ngày dương + thứ + đếm ngược, mô tả theo đoạn, lễ nghi
      đánh số, ẩn mục rỗng, không tìm thấy, lỗi tải; không kinh/chùa/nhắc/trước-sau (AC14–AC18; ⚠️ ảnh
      mặc định)
- [ ] Khối Home: sáu tháng từ tháng hiện tại, chia tầng qua `GetHomeEventBlockUseCase`, thẻ tháng trống,
      lỗi thay chỗ thẻ / trên từng thẻ, câu bản trên máy, dòng → `FestivalDetailRoute` (`HomeCallbacks
      .onEventDetail`) (AC19, AC24, AC25, AC27; có code, lệch: 12 tháng của năm dương, không tầng, dòng
      mở lịch lễ); dựng lại ở `ON_RESUME` khi sang tháng/ngày mới (AC20)
- [ ] Tab Chùa: `ChuaViewModel` lấy "Ngày lễ & ngày chay sắp tới" qua use case mới (⚠️ product)
- [ ] Fake `CalendarEventRepository` (JSON mẫu theo contract + field v3.0, chỉ ở debug/test) cho tới Phase
      3; ghi ở `design.md` "Deliberately stubbed"

## Phase 3 — Data (`il-data-builder`, chờ backend)

- [ ] **Chặn:** backend calendar v3.0 (innerlamp-be `docs/specs/features/calendar/tasks.md` Phase 14–18)
      đã lên `dev`: `GET /v1/calendar/events` công khai, có `is_major`, `sort_order`, `recurrence`, seed có
      Rằm / Mùng 1 ở `sort_order` cuối và lễ nghi một việc một dòng; `openapi.yaml` đã cập nhật — đổi dòng
      `Contract:` của `requirements.md` sang commit đó (hand-off, không làm ở repo này)
- [x] Contract hiện có `GET /v1/calendar/events` (`calendarEvents`) trong innerlamp-be
      `docs/specs/api/openapi.yaml` @ 0eb7d1f — cite, không chép schema
- [ ] `data/remote/api/CalendarApiService.kt` (không `@Authenticated`) + `data/remote/dto/CalendarEventsDto.kt`
      (AC1, AC2)
- [ ] `data/mapper/CalendarEventMapper.kt` — nhóm, cờ, `recurrence` lạ, mã nhóm lạ (AC3, AC4)
- [ ] `core/database/calendar/` — `CalendarCacheDatabase` (`innerlamp_calendar.db`, destructive migration),
      `CalendarYearCacheEntity`, `CalendarYearCacheDao` (`upsert`, `get`, `deleteBefore`) + schema export
      (AC22; ⚠️ cách lưu — mặc định đã chọn)
- [ ] `data/repository/CalendarEventRepositoryImpl.kt` — bộ nhớ → Room → `NetworkClient.safeCall`; ghi Room
      sau thành công; map `AppError` theo bảng Error → UI (Step-by-step Flow "Tải một năm"; AC22–AC25, AC28)
- [ ] `di/CalendarModule.kt`; một instance repository giữ ở `InnerLampApplication`; thay fake repository
      và nguồn cục bộ ở Home, lịch lễ, tab Chùa; rồi làm mục "Xoá" của Phase 1

## Phase 4 — Tests

- [ ] Script `scripts/sync-product-vectors.sh` (dùng chung với Memorial Phase 5): chép innerlamp-be
      `docs/product/calendar/rules/{countdown,ordering,home-block}.json` (từ `../innerlamp-be` tại một
      commit ghi rõ) vào `app/src/test/resources/product/calendar/rules/` kèm file `SOURCE` (commit +
      `product_spec_version` từng file); bản chép **không bao giờ sửa tay**. Không chép
      `event-placement.json` (chỉ backend, product §4.1)
- [ ] `CalendarProductVectorsTest` chạy **mọi case** (AC30): `countdown.json` (5) qua
      `CalendarCountdown`, `ordering.json` (4) qua `CalendarOrdering`, `home-block.json` (5) qua
      `HomeEventBlock.select` (`every_lunar_month` ↔ `Recurrence.LunarMonthly`); fail nếu
      `product_spec_version` khác `SOURCE` (tại 0eb7d1f: countdown `1.0`, ordering `1.1`, home-block `1.1`)
- [ ] `EventContentFormat`, `CalendarEventMapper` unit tests (BR-CAL-14, field v3.0, mã lạ)
- [ ] `CalendarEventRepositoryImpl` tests (MockWebServer hoặc fake `NetworkClient` + Room in-memory):
      thành công ghi Room và xoá năm cũ; mỗi dòng Error → UI; có/không có bản trên máy; 401 không đụng phiên
- [ ] Use case + ViewModel tests (`kotlinx-coroutines-test`, Turbine, MockK): bản trên máy rồi làm mới,
      tải năm sau / 2100 / thử lại, chip + số, chi tiết không tìm thấy, Home hai năm thiếu một, đổi ngày
- [ ] Viết lại `FestivalsViewModelTest`, `HomeViewModelTest`; xoá `GetBuddhistEventMonthsUseCaseTest` cùng
      use case (giữ `LunarCalendarTest` — Sổ giỗ, Lịch âm dùng)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (người dùng tự chạy; có flavor nên không phải
      `testDebugUnitTest`)
- [ ] QA trên máy: khách mở lịch lễ (không mời đăng nhập), chế độ máy bay có/không có bản trên máy, Home
      offline, 2 s trên 4G / 0,5 s từ bản trên máy (AC29, NFR-CAL-03), so với prototype `festVals`,
      `festDetail`

## Phase 5 — Documentation

- [ ] Báo trạng thái cột Android của product §4.2 cho `ba-writer` (skill này không sửa repo backend)
- [x] Xoá `Docs/home_assumptions.md` mục #11 (đã bị product spec thay thế; bước dọn tài liệu cũ, 2026-10-15)
- [ ] OKF concept qua `il-okf-writer` khi feature đã kiểm chứng end-to-end

## Deferred (not blocking Calendar)

- Đổi tên package `ui/screens/festivals/` → tên theo slug `calendar` (vướng `ui/screens/calendar/` của
  Lịch âm).
- Sự kiện trước/sau trên chi tiết (FR-CAL-009 — website; Android có thể thêm sau, §3.6).
- HTTP cache của OkHttp cho `Cache-Control: public`.
- Ảnh sự kiện thật + ảnh mặc định chung (khi product cung cấp, §2.7).

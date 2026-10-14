# Tasks — Memorial

Version: 2.1
Last updated: 2026-10-14
Status: in-progress — UI và tính kỳ giỗ có trên dữ liệu in-memory; tích hợp backend, bản offline, push FCM, múi giờ tài khoản, tháng nhuận, khoảng năm, xác nhận xoá và vector còn `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
`[x]` chỉ khi đã kiểm tra tồn tại **và đúng như product spec v2.1 yêu cầu** trong code ngày
2026-10-14; mục có code nhưng lệch product để `[ ]` và ghi "(có code, lệch: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 1 — Domain

- [x] `domain/calendar/LunarCalendar.kt` — lịch âm Việt Nam UTC+7, `toLunar` có cờ nhuận (BR-MEM-10;
      chưa đối chiếu vector — Phase 5)
- [x] `domain/calendar/MemorialSchedule.next` — kỳ giỗ tới ở tháng thường, ngày 30 → 29, 29/2 → 28/2,
      số ngày, lần giỗ theo năm của lịch đã nhập (BR-MEM-05…09; chưa đối chiếu vector — Phase 5)
- [ ] `domain/model/MemorialEntry.kt`: thêm `leapMonth`; `id: Long?` của server; `note: String?`;
      **bỏ** `syncReminders` (FR-MEM-009 Removed)
- [ ] `MemorialSchedule`: quy đổi âm → dương **có** cờ nhuận cho ngày mất có năm, trả "không tồn
      tại" khi năm không có tháng nhuận đó (BR-MEM-04, BR-MEM-10)
- [ ] `domain/calendar/MemorialDateRules.kt` — kiểm ngày mất theo BR-MEM-03/04 (AC14)
- [ ] Giới hạn tên và ghi chú theo BR-MEM-02 (AC12, AC13) — trong use case lưu hoặc rules của domain
- [ ] Nearest choice: `domain/model/MemorialReminderChoices.kt` — lựa chọn BR-MEM-11 + `nearest` cho
      giá trị cũ (AC21; cách đều chờ ⚠️ product)
- [ ] `domain/repository/MemorialRepository.kt` (interface: `load`, `create`, `update`, `delete`,
      `clearLocal`)
- [ ] `domain/usecase/LoadMemorialBookUseCase.kt`, `SaveMemorialEntryUseCase.kt`,
      `DeleteMemorialEntryUseCase.kt` (`Result`, `DispatcherProvider`, không lập nhắc — AC30); xoá
      `GetMemorialEntriesUseCase.kt` (seed, product §2.7)
- [ ] `domain/repository/PushTokenRepository.kt`, `AccountTimeZoneRepository.kt`;
      `RegisterPushTokenUseCase`, `UnregisterPushTokenUseCase`, `ReportAccountTimeZoneUseCase`
      (AC24, AC25, AC29)
- [ ] `domain/push/PushPermissionChecker.kt` (port, AC23)

## Phase 2 — UI (`il-ui-builder`)

- [x] `GioRoute` / `GioEditRoute` trong `ui/navigation/Routes.kt` + wiring trong `MainActivity.kt`
      (Home → sổ → form, ViewModel dùng chung qua entry của `GioRoute`)
- [x] Sổ: thẻ nổi bật, danh sách sắp theo số ngày còn lại, nhãn "Hôm nay"/"Ngày mai"/"còn N ngày",
      nhấn mạnh ≤ 7 ngày, dấu "Có nhắc", header "N người thân" (AC4, AC5 trừ dấu nhuận; FR-MEM-002)
- [x] Form: xem trước FR-MEM-006 (AC10), lỗi tên rỗng "Vui lòng nhập họ và tên" xoá khi sửa (AC12 phần
      tên rỗng), back không lưu (AC17), lựa chọn nhắc 0/1/3/7 ngày × 06:00/07:00/12:00/19:00 ẩn khi tắt
      (AC20 phần UI)
- [x] Flash "Đã lưu vào sổ giỗ." / "Đã xóa <tên> khỏi sổ giỗ." (AC15, AC19 phần câu chữ)
- [x] Chữ viết tắt khi không có ảnh (AC38)
- [x] Không có lịch nhắc local (AC30) — giữ nguyên, không thêm
- [ ] Trạng thái trống bỏ hàng "Lấy từ gia phả" + `onGiaPha` (AC6; có code, lệch: FR-MEM-002)
- [ ] Bỏ công tắc "Thêm vào Lời nhắc (Reminders)", `onSyncToggle`, nhãn "· có trong Lời nhắc"; giá
      trị khởi đầu không còn `syncReminders` (AC9, AC31; có code, lệch: FR-MEM-009 Removed)
- [ ] Năm mất 1900 → năm hiện tại + "Không rõ" (AC11; có code, lệch: `YearSpan = 97`)
- [ ] Ngày dương theo độ dài tháng (29/2 cho phép khi không có năm); lựa chọn "Tháng nhuận" chỉ khi
      âm lịch; dấu "nhuận" ở danh sách, thẻ nổi bật, form (AC5, AC11)
- [ ] Lỗi field theo §3.1.1: tên quá dài, ghi chú quá dài, ngày không tồn tại / tháng nhuận không có /
      trước 1900 / sau hôm nay (AC12–AC14)
- [ ] Hộp xác nhận xoá theo §3.1.1 "Confirm delete" (AC19; có code, lệch: xoá ngay — BR-MEM-12)
- [ ] `GioUiState`: `loadState` (khách / đang tải / online / offline / lỗi), `saving`,
      `confirmDelete`, `pushAllowed`; khoá nút khi đang lưu/xoá (AC18)
- [ ] Trạng thái khách + nút đăng nhập (AC2; ⚠️ cách mời đăng nhập; cần Login thật)
- [ ] Trạng thái offline (cả khi server lỗi) và lỗi tải có thử lại, câu §3.1.1 (AC32, AC33)
- [ ] Thông báo lỗi theo bảng Error → UI với câu §3.1.1; nhánh 404 khi sửa/xoá "Mục này không còn
      trong sổ giỗ." (AC16, AC19, AC34)
- [ ] `GioViewModel` gọi use case trong `viewModelScope`, có factory từ `di/MemorialModule.kt` thay
      `viewModel()` mặc định; tính lại khi `ON_RESUME` sang ngày mới (AC7)
- [ ] Hiện lựa chọn gần nhất cho giá trị nhắc cũ, gửi lựa chọn đang hiện khi lưu (AC21)
- [ ] Xin quyền `POST_NOTIFICATIONS` (API 33+, một lần) khi bật nhắc / lưu mục có nhắc (AC22)
- [ ] Gợi ý §3.1.1 "Notifications not allowed on the device" + "Mở Cài đặt" trong phần nhắc, đọc lại
      ở `ON_RESUME`; "Có nhắc" vẫn hiện (AC5, AC23)
- [ ] Mở mục từ nhắc: `GioRoute(openId)`, deep link trong `MainActivity` (`singleTop`, `onNewIntent`)
      (AC27, AC28; mục đã xoá chờ ⚠️ product; khoá id chờ ⚠️ contract)

## Phase 3 — Data (`il-data-builder`)

- [x] Contract đã có: `GET`/`POST /v1/memorials`, `PUT`/`DELETE /v1/memorials/{id}` trong innerlamp-be
      `docs/specs/api/openapi.yaml` @ bf23f8b — cite, không chép schema
- [ ] DTO/Mapper: `data/remote/api/MemorialApiService.kt` (`@Authenticated`) +
      `data/remote/dto/MemorialDto.kt` + `data/mapper/MemorialMapper.kt` — quan hệ theo tên, hai dạng
      ngày mất, cờ nhuận khôi phục từ `death_date`, nhắc (số ngày, giờ) không múi giờ, không
      `add_to_os_reminder`, ghi chú rỗng → `null` (⚠️ AC15 contract)
- [ ] `core/database/memorial/` — `MemorialCacheDatabase` (`innerlamp_memorial.db`), entity có
      `accountId`, DAO (`replaceAll`, `upsert`, `delete`, `getAll(accountId)`, `clear`) + schema export
- [ ] `data/repository/MemorialRepositoryImpl.kt` qua `NetworkClient.safeCall`; Room sau khi server
      thành công; tải lỗi mạng **hoặc** lỗi server → đọc Room của tài khoản; map `AppError` theo bảng
      Error → UI (Step-by-step Flow "Mở sổ", "Lưu", "Xoá")
- [ ] `di/MemorialModule.kt`; bỏ seed in-memory khỏi `GioViewModel`
- [ ] `LoggingInterceptor`: không log body của `/v1/memorials` (AC36)
- [ ] `res/xml/data_extraction_rules.xml` + `backup_rules.xml`: loại `innerlamp_memorial.db` (AC37)
- [ ] Chặn bởi feature Login: phiên thật, account id, làm mới phiên (AC1–AC3, AC35)

## Phase 4 — Push FCM và múi giờ tài khoản (`il-data-builder`, `core/push`)

- [ ] Build: plugin `com.google.gms.google-services`, Firebase BoM + `firebase-messaging` trong
      `gradle/libs.versions.toml`; `google-services.json` cho `dev`/`staging`/`prod` (chặn: Firebase
      project do product owner đăng ký, ADR-0001)
- [ ] `core/push/NotificationChannels.kt` — kênh nhắc giỗ, tạo ở `InnerLampApplication`
- [ ] Messaging service: `core/push/InnerLampMessagingService.kt` — `onNewToken` → đăng ký nếu có
      phiên; `onMessageReceived` → hiện đúng nội dung server, `PendingIntent` mở mục; không log payload
      (AC24, AC26, AC27, AC36; chờ ⚠️ contract payload)
- [ ] `core/push/AndroidPushPermissionChecker.kt` (AC23)
- [ ] Push token: `PushTokenRepositoryImpl` + API service (chờ ⚠️ contract — Appendix B mục 8);
      `FirebaseMessaging.deleteToken()` khi đăng xuất (AC24, AC25)
- [ ] Múi giờ tài khoản: `AccountTimeZoneRepositoryImpl` + API service (chờ ⚠️ contract — Appendix B
      mục 3); prefs `account_time_zone_reported` (AC29)
- [ ] `InnerLampApplication`: `ProcessLifecycleOwner` `ON_START` → báo múi giờ khi đổi, đăng ký lại
      token nếu lần trước lỗi (AC24, AC29)
- [ ] `AndroidManifest.xml`: `POST_NOTIFICATIONS`, service `MESSAGING_EVENT`, `MainActivity`
      `singleTop`; không thêm `RECEIVE_BOOT_COMPLETED`/exact alarm (AC30)
- [ ] `di/PushModule.kt`
- [ ] Hook vào feature Login (chặn bởi Login): đăng nhập → đăng ký token + báo múi giờ; đăng xuất →
      xoá token + `clearLocal()` + xoá prefs múi giờ (AC24, AC25, AC29)

## Phase 5 — Tests

- [x] `MemorialScheduleTest` (quy đổi Tết, ngày 30, 29/2, kỳ giỗ trong/qua năm), `GioFormatTest`,
      `GioViewModelTest` (sắp xếp, lỗi tên, thêm/sửa/xoá in-memory) — sẽ phải viết lại cho use case
      và repository
- [ ] Script `scripts/sync-product-vectors.sh`: chép innerlamp-be
      `docs/product/memorial/rules/{next-anniversary,anniversary-ordinal,death-date-conversion}.json`
      (từ `../innerlamp-be` tại một commit ghi rõ) vào `app/src/test/resources/product/memorial/rules/`
      kèm file `SOURCE` (commit + `product_spec_version` của từng file); bản chép **không bao giờ sửa
      tay**. Không chép `reminder-moment.json` (chỉ backend, product §4.3)
- [ ] `MemorialProductVectorsTest` chạy **mọi case** của ba file (BR-MEM-03…10, NFR-MEM-03, AC39):
      `next-anniversary.json` (17) qua `MemorialSchedule.next` (cần `leapMonth` — Phase 1),
      `anniversary-ordinal.json` (6) qua nhãn lần giỗ, `death-date-conversion.json` (9) qua
      `LunarCalendar.toLunar` + quy đổi âm → dương có nhuận và kiểm "không tồn tại"; test fail nếu
      `product_spec_version` của một file khác giá trị ghi trong `SOURCE` (hiện `1.0` cho cả ba file
      tại bf23f8b)
- [ ] `MemorialDateRules`, `MemorialReminderChoices.nearest`, `MemorialMapper` unit tests
- [ ] `MemorialRepositoryImpl` tests: thành công và từng dòng Error → UI (MockWebServer hoặc fake
      `NetworkClient`), offline đọc Room khi mất mạng và khi 5xx, không đọc bản của account khác
- [ ] `GioViewModel` + use case tests với `kotlinx-coroutines-test`, Turbine, MockK (khách, offline,
      đang lưu, xác nhận xoá, 404 khi sửa/xoá, `openId`, `pushAllowed`)
- [ ] Push/time zone use case tests (đăng ký khi có phiên, bỏ qua lỗi, báo lại khi múi giờ đổi, xoá
      khi đăng xuất)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (người dùng tự chạy — `AGENTS.md`; có flavor nên
      không phải `testDebugUnitTest`)
- [ ] QA trên máy (FR-MEM-008 yêu cầu QA thủ công push): push tới khi app nền/đang mở/đã tắt, màn hình
      khoá chỉ có quan hệ, bấm mở đúng mục, từ chối quyền → gợi ý + vẫn "Có nhắc", đăng xuất → không còn
      push; 100 mục hiện trong 2 giây trên 4G (AC8, NFR-MEM-01)

## Phase 6 — Documentation

- [ ] Báo trạng thái cột Android của product §4.2 cho `ba-writer` (skill này không sửa repo backend)
- [x] Xoá `Docs/gio_assumptions.md` (đã bị product spec thay thế); hai "Ghi chú build" đã chuyển sang
      `AGENTS.md`/`CLAUDE.md` (2026-10-14)
- [ ] OKF concept qua `il-okf-writer` khi feature đã kiểm chứng end-to-end

## Deferred (not blocking Memorial)

- Ảnh chân dung: Photo Picker, đổi sang JPEG, ≤ 5 MB (FR-MEM-011 `later`; contract chưa có route ảnh).
- Giữ bản nháp qua process death (`SavedStateHandle`).
- Đổi tên package `ui/screens/gio/` → `memorial/` (product §1.5).
- Bottom sheet chọn năm thay `DropdownMenu` (127+ năm).
- Chuyển phần push/token/múi giờ sang spec Android của Thông báo/Tài khoản khi có.

# Tasks — Memorial

Version: 1.0
Last updated: 2026-10-14
Status: in-progress — UI và tính kỳ giỗ có trên dữ liệu in-memory; tích hợp backend, bản offline, nhắc local, tháng nhuận, khoảng năm, xác nhận xoá và vector còn `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
`[x]` chỉ khi đã kiểm tra tồn tại **và đúng như product spec v1.0 yêu cầu** trong code ngày
2026-10-14; mục có code nhưng lệch product để `[ ]` và ghi "(có code, lệch: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 1 — Domain

- [x] `domain/calendar/LunarCalendar.kt` — lịch âm Việt Nam UTC+7, `toLunar` có cờ nhuận (BR-MEM-10;
      chưa đối chiếu vector — Phase 5)
- [x] `domain/calendar/MemorialSchedule.next` — kỳ giỗ tới ở tháng thường, ngày 30 → 29, 29/2 → 28/2,
      số ngày, lần giỗ theo năm của lịch đã nhập (BR-MEM-05…09; chưa đối chiếu vector — Phase 5)
- [ ] `domain/model/MemorialEntry.kt`: thêm `leapMonth`; `id: Long?` của server; `note: String?`;
      thay `syncReminders` bằng `addToOsReminder` (chỉ giữ để gửi lại khi `PUT`, ⚠️ AC16)
- [ ] `MemorialSchedule`: quy đổi âm → dương **có** cờ nhuận cho ngày mất có năm, trả "không tồn
      tại" khi năm không có tháng nhuận đó (BR-MEM-04, BR-MEM-10)
- [ ] `domain/calendar/MemorialDateRules.kt` — kiểm ngày mất theo BR-MEM-03/04 (AC14)
- [ ] Giới hạn tên và ghi chú theo BR-MEM-02 (AC12, AC13) — trong use case lưu hoặc rules của domain
- [ ] `domain/calendar/MemorialReminderPlanner.kt` — thời điểm nhắc kế tiếp theo BR-MEM-11, giờ địa
      phương của máy (AC22, AC23); nhánh "thời điểm đã qua" chờ ⚠️ AC22 (product)
- [ ] `domain/repository/MemorialRepository.kt` (interface: `load`, `create`, `update`, `delete`,
      `clearLocal`)
- [ ] `domain/reminder/MemorialReminderScheduler.kt` (port: `replanAll`, `cancel`, `cancelAll`)
- [ ] `domain/usecase/LoadMemorialBookUseCase.kt`, `SaveMemorialEntryUseCase.kt`,
      `DeleteMemorialEntryUseCase.kt` (`Result`, `DispatcherProvider`); xoá
      `GetMemorialEntriesUseCase.kt` (seed, product §2.7)

## Phase 2 — UI (`il-ui-builder`)

- [x] `GioRoute` / `GioEditRoute` trong `ui/navigation/Routes.kt` + wiring trong `MainActivity.kt`
      (Home → sổ → form, ViewModel dùng chung qua entry của `GioRoute`)
- [x] Sổ: thẻ nổi bật, danh sách sắp theo số ngày còn lại, nhãn "Hôm nay"/"Ngày mai"/"còn N ngày",
      nhấn mạnh ≤ 7 ngày, dấu "Có nhắc", header "N người thân", trạng thái trống (AC4, AC6; FR-MEM-002)
- [x] Form: giá trị khởi đầu của FR-MEM-003 (trừ cờ iOS — mục dưới), xem trước FR-MEM-006 (AC10),
      lỗi tên rỗng "Vui lòng nhập họ và tên" xoá khi sửa (AC12 phần tên rỗng), back không lưu (AC17),
      lựa chọn nhắc 0/1/3/7 ngày × 06:00/07:00/12:00/19:00 ẩn khi tắt (AC20 phần UI)
- [x] Flash "Đã lưu vào sổ giỗ." / "Đã xóa <tên> khỏi sổ giỗ." (AC15, AC19 phần câu chữ)
- [x] Chữ viết tắt khi không có ảnh (AC32)
- [ ] Bỏ công tắc "Thêm vào Lời nhắc (Reminders)" và nhãn "· có trong Lời nhắc" (AC26; có code,
      lệch: §3.6)
- [ ] Năm mất 1900 → năm hiện tại + "Không rõ" (AC11; có code, lệch: `YearSpan = 97`)
- [ ] Ngày dương theo độ dài tháng (29/2 cho phép khi không có năm); lựa chọn tháng nhuận chỉ khi âm
      lịch; dấu "nhuận" ở danh sách, thẻ nổi bật, form (AC5, AC11)
- [ ] Lỗi field: tên quá dài, ghi chú quá dài, ngày không hợp lệ / không tồn tại (AC12–AC14)
- [ ] Hộp xác nhận xoá (AC19; có code, lệch: xoá ngay — BR-MEM-12)
- [ ] `GioUiState`: `loadState` (khách / đang tải / online / offline / lỗi), `saving`,
      `confirmDelete`; khoá nút khi đang lưu/xoá (AC18)
- [ ] Trạng thái khách + nút đăng nhập (AC2; ⚠️ cách mời đăng nhập; cần Login thật)
- [ ] Trạng thái offline và lỗi tải có thử lại (AC27, AC28)
- [ ] Thông báo lỗi theo bảng Error → UI (câu chữ chờ ⚠️ product)
- [ ] `GioViewModel` gọi use case trong `viewModelScope`, có factory từ `di/MemorialModule.kt` thay
      `viewModel()` mặc định; tính lại khi `ON_RESUME` sang ngày mới (AC7)
- [ ] Xin quyền `POST_NOTIFICATIONS` khi bật nhắc / lưu mục có nhắc (AC21; ⚠️ khi bị từ chối)
- [ ] Hàng "Lấy từ gia phả" ở trạng thái trống — chờ ⚠️ AC6 (product)

## Phase 3 — Data (`il-data-builder`)

- [x] Contract đã có: `GET`/`POST /v1/memorials`, `PUT`/`DELETE /v1/memorials/{id}` trong innerlamp-be
      `docs/specs/api/openapi.yaml` @ 5f4ca56 — cite, không chép schema
- [ ] `data/remote/api/MemorialApiService.kt` (`@Authenticated`) + `data/remote/dto/MemorialDto.kt`
- [ ] `data/mapper/MemorialMapper.kt` — quan hệ theo tên, hai dạng ngày mất, cờ nhuận khôi phục từ
      `death_date`, nhắc + múi giờ máy, ghi chú rỗng → `null`, giữ `add_to_os_reminder` (⚠️ AC16)
- [ ] `core/database/memorial/` — `MemorialCacheDatabase` (`innerlamp_memorial.db`), entity, DAO
      (`replaceAll`, `upsert`, `delete`, `getAll`, `get`, `clear`) + schema export
- [ ] `data/repository/MemorialRepositoryImpl.kt` qua `NetworkClient.safeCall`; Room sau khi server
      thành công; tải lỗi mạng → đọc Room; map `AppError` theo bảng Error → UI (Step-by-step Flow
      "Mở sổ", "Lưu", "Xoá")
- [ ] `di/MemorialModule.kt`; bỏ seed in-memory khỏi `GioViewModel`
- [ ] `LoggingInterceptor`: không log body của `/v1/memorials` (AC30)
- [ ] `res/xml/data_extraction_rules.xml` + `backup_rules.xml`: loại `innerlamp_memorial.db` (AC31)
- [ ] Chặn bởi feature Login: phiên thật + làm mới phiên + đăng xuất gọi `clearLocal()` (AC1–AC3,
      AC25)

## Phase 4 — Nhắc local (`il-data-builder`, `core/notification`)

- [ ] `NotificationChannels.kt` — kênh "memorial_reminders"
- [ ] `AlarmMemorialReminderScheduler.kt` implement port (`replanAll`, `cancel`, `cancelAll`;
      requestCode = id) — chờ ⚠️ exact alarm (design "Local storage")
- [ ] `MemorialReminderReceiver.kt` — hiện thông báo, mở `GioRoute` khi bấm, lập kỳ kế tiếp (AC23;
      nội dung và màn hình khoá chờ ⚠️ product)
- [ ] `MemorialRescheduleReceiver.kt` — `BOOT_COMPLETED`, `TIMEZONE_CHANGED`, `TIME_SET`,
      `MY_PACKAGE_REPLACED` → lập lại từ Room (AC24)
- [ ] `AndroidManifest.xml`: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` (+ quyền exact alarm nếu
      chọn), hai receiver
- [ ] Gọi `replanAll` sau tải/lưu/xoá (AC22) và `cancelAll` khi đăng xuất (AC25, ⚠️ product)

## Phase 5 — Tests

- [x] `MemorialScheduleTest` (quy đổi Tết, ngày 30, 29/2, kỳ giỗ trong/qua năm), `GioFormatTest`,
      `GioViewModelTest` (sắp xếp, lỗi tên, thêm/sửa/xoá in-memory) — sẽ phải viết lại cho use case
      và repository
- [ ] Script `scripts/sync-product-vectors.sh`: chép innerlamp-be
      `docs/product/memorial/rules/{next-anniversary,anniversary-ordinal,death-date-conversion}.json`
      (từ `../innerlamp-be` tại một commit ghi rõ) vào `app/src/test/resources/product/memorial/rules/`
      kèm file `SOURCE` (commit + `product_spec_version`); bản chép **không bao giờ sửa tay**
- [ ] `MemorialProductVectorsTest` chạy **mọi case** của ba file (BR-MEM-03…10, NFR-MEM-03, AC33):
      `next-anniversary.json` (17) qua `MemorialSchedule.next` (cần `leapMonth` — Phase 1),
      `anniversary-ordinal.json` (6) qua nhãn lần giỗ (`GioFormat.ordinalLabel` hoặc hàm domain thay
      thế), `death-date-conversion.json` (9) qua `LunarCalendar.toLunar` + quy đổi âm → dương có nhuận
      và kiểm "không tồn tại"; test fail nếu `product_spec_version` khác `1.0` của header
- [ ] `MemorialDateRules`, `MemorialReminderPlanner`, `MemorialMapper` unit tests
- [ ] `MemorialRepositoryImpl` tests: thành công và từng dòng Error → UI (MockWebServer hoặc fake
      `NetworkClient`), offline đọc Room
- [ ] `GioViewModel` + use case tests với `kotlinx-coroutines-test`, Turbine, MockK (khách, offline,
      đang lưu, xác nhận xoá, 404 khi sửa)
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (người dùng tự chạy — `AGENTS.md`; có flavor nên
      không phải `testDebugUnitTest`)
- [ ] QA trên máy: nhắc tới giờ, sau khởi động lại, đổi múi giờ (FR-MEM-008 — product yêu cầu QA
      thủ công); 100 mục hiện trong 2 giây trên 4G (AC8, NFR-MEM-01)

## Phase 6 — Documentation

- [ ] Báo trạng thái cột Android của product §4.2 cho `ba-writer` (skill này không sửa repo backend)
- [ ] Xoá `Docs/gio_assumptions.md` (đã bị product spec v1.0 thay thế — `changelog.md` v1.0); chuyển
      hai "Ghi chú build" của nó (task `testDevDebugUnitTest`, lỗi lint sẵn có) sang `AGENTS.md`/
      `CLAUDE.md` trước khi xoá
- [ ] OKF concept qua `il-okf-writer` khi feature đã kiểm chứng end-to-end

## Deferred (not blocking Memorial)

- Ảnh chân dung: Photo Picker, đổi sang JPEG, ≤ 5 MB (FR-MEM-011 `later`; contract chưa có route ảnh).
- Giữ bản nháp qua process death (`SavedStateHandle`).
- Đổi tên package `ui/screens/gio/` → `memorial/` (product §1.5).
- Bottom sheet chọn năm thay `DropdownMenu` (127+ năm).

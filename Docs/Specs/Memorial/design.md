# Design — Memorial

Version: 1.0
Last updated: 2026-10-14
Status: in-progress — UI (`ui/screens/gio/`) và tính kỳ giỗ đã có trên dữ liệu in-memory; repository, data layer (API + Room), nhắc local và phiên đăng nhập chưa có

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Hai màn "Sổ giỗ chạp" (`GioScreen`) và "Thêm/sửa ngày giỗ" (`GioEditScreen`) dùng chung một
`GioViewModel` gắn vào back stack entry của `GioRoute`. Đích đến: dữ liệu đi
`GioViewModel → use case → MemorialRepository (domain) → MemorialRepositoryImpl (data) →
NetworkClient.safeCall` cho backend và Room cho bản offline; kỳ giỗ, số ngày còn lại, lần giỗ và
thời điểm nhắc tính thuần Kotlin trong `domain/calendar/` (product §2.7, §3.6). Kiểu
**online-first, cache đọc offline**: mỗi lần mở sổ gọi `GET /v1/memorials`, thành công thì thay bản
Room; mất mạng thì đọc Room, không cho sửa (FR-MEM-010). Nhắc là alarm local do app lập lại sau mỗi
lần tải/lưu/xoá và sau khi máy khởi động lại (FR-MEM-008).

Hiện trạng: `GioViewModel` giữ `List<MemorialEntry>` seed từ `GetMemorialEntriesUseCase`, không có
repository, không coroutine, không phiên. Kiến trúc giữ nguyên `ui → domain ← data →
core/{network,storage}`, DI thủ công trong `di/`, đổi thread qua `DispatcherProvider`. Gần nhất để
làm mẫu: `TempleOptionRepositoryImpl` / `di/TempleModule.kt` (Retrofit + `safeCall`) và
`RoomSutraQuoteRepository` / `di/DatabaseModule.kt` (Room).

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-14.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Screen | `ui/screens/gio/GioScreen.kt` | Sổ: header "N người thân", thẻ nổi bật, danh sách, trạng thái trống; thêm: trạng thái khách, đang tải, offline, lỗi tải (AC2, AC27, AC28); dấu tháng nhuận (AC5) | có — cần sửa |
| UI / Screen | `ui/screens/gio/GioEditScreen.kt` | Form: tên, quan hệ, lịch, ngày/tháng/năm (1900 → nay), tháng nhuận (chỉ âm), ghi chú, nhắc, xem trước; thêm: hộp xác nhận xoá, trạng thái đang lưu, bỏ công tắc "Lời nhắc" (AC11, AC18, AC19, AC26) | có — cần sửa |
| UI / Format | `ui/screens/gio/GioFormat.kt` | Chuỗi hiển thị (nhãn ngày, lần giỗ, số ngày, xem trước); thêm dấu "nhuận"; bỏ "· có trong Lời nhắc" | có — cần sửa |
| UI / State | `ui/screens/gio/GioUiState.kt` | `items`, `flash`, `draft`; thêm: `loadState` (khách / đang tải / online / offline / lỗi), `saving`, `confirmDelete`, lỗi field (tên dài, ghi chú dài, ngày) | có — cần sửa |
| UI / ViewModel | `ui/screens/gio/GioViewModel.kt` | Gọi use case trong `viewModelScope`, map `Result` → state + Error → UI; `today` tiêm vào; tính lại khi `ON_RESUME` sang ngày mới (AC7) | có — cần sửa (đang in-memory) |
| UI / Navigation | `ui/navigation/Routes.kt` (`GioRoute`, `GioEditRoute`), wiring trong `MainActivity.kt` | Home → `GioRoute` → `GioEditRoute`; `GioEditRoute` lấy ViewModel từ entry của `GioRoute`; thêm điều hướng tới Login cho khách (⚠️ AC2) | có — cần sửa (ViewModel đang `viewModel()` không factory) |
| UI / Permission | `ui/screens/gio/GioEditScreen.kt` | Xin `POST_NOTIFICATIONS` khi bật nhắc / lưu mục có nhắc (AC21) | chưa tạo |
| Domain / Model | `domain/model/MemorialEntry.kt` | Thêm `leapMonth`; `id` của server (`Long?`, `null` cho bản nháp mới); `note: String?`; thay `syncReminders` bằng cờ `addToOsReminder` chỉ để giữ nguyên khi `PUT` (không hiện) | có — cần sửa |
| Domain / Calendar | `domain/calendar/LunarCalendar.kt` | Lịch âm Việt Nam UTC+7 (BR-MEM-10); `toLunar` trả cả cờ nhuận | có |
| Domain / Calendar | `domain/calendar/MemorialSchedule.kt` | `next(entry, today)` — kỳ giỗ tới, số ngày, lần giỗ (BR-MEM-05…09); thêm quy đổi âm → dương **có** tháng nhuận cho ngày mất có năm (BR-MEM-04, BR-MEM-10) | có — cần sửa |
| Domain / Calendar | `domain/calendar/MemorialDateRules.kt` | Kiểm ngày mất theo BR-MEM-03/04 (tồn tại, 1900 → nay, không sau hôm nay theo `Asia/Ho_Chi_Minh`, tháng nhuận có trong năm) | chưa tạo |
| Domain / Calendar | `domain/calendar/MemorialReminderPlanner.kt` | Thời điểm nhắc kế tiếp của một mục: kỳ giỗ tới − số ngày nhắc, tại giờ nhắc, giờ địa phương của máy (BR-MEM-11); ⚠️ AC22 khi thời điểm đã qua | chưa tạo |
| Domain / Repository | `domain/repository/MemorialRepository.kt` | `load(): Result<MemorialBook>` (kèm cờ offline), `create`, `update`, `delete`, `clearLocal()` | chưa tạo |
| Domain / Port | `domain/reminder/MemorialReminderScheduler.kt` | Port: `replanAll(entries)`, `cancel(id)`, `cancelAll()` — impl ở `core/notification` | chưa tạo |
| Domain / UseCase | `domain/usecase/GetMemorialEntriesUseCase.kt` | Hiện trả seed 3 người mẫu → thay bằng `LoadMemorialBookUseCase` | có — thay |
| Domain / UseCase | `domain/usecase/LoadMemorialBookUseCase.kt`, `SaveMemorialEntryUseCase.kt`, `DeleteMemorialEntryUseCase.kt` | `operator fun invoke` → `Result`; gọi repository rồi `MemorialReminderScheduler`; dispatcher qua `DispatcherProvider` | chưa tạo |
| Data / Remote | `data/remote/api/MemorialApiService.kt`, `data/remote/dto/MemorialDto.kt` | Retrofit `@Authenticated` cho 4 operation dùng (bảng "Dữ liệu & API" của requirements); DTO `@Serializable` theo `openapi.yaml` @ 5f4ca56 | chưa tạo |
| Data / Mapper | `data/mapper/MemorialMapper.kt` | DTO ↔ domain: `relationship` ↔ `MemorialRelation` (theo tên, không theo thứ tự); ngày mất hai dạng; `death_calendar`; nhắc + `timezone = ZoneId.systemDefault()`; `note` rỗng → `null`; giữ `add_to_os_reminder` | chưa tạo |
| Data / Local | `core/database/memorial/MemorialCacheDatabase.kt`, `MemorialCacheEntity.kt`, `MemorialCacheDao.kt` | Bản offline (Room), DB riêng `innerlamp_memorial.db` để loại khỏi backup (AC31); `replaceAll` trong một transaction | chưa tạo |
| Data / Repository | `data/repository/MemorialRepositoryImpl.kt` | Gọi API qua `NetworkClient.safeCall`; ghi Room sau khi server thành công; `NoConnection`/`Timeout` khi tải → đọc Room; map `AppError` → lỗi domain cho bảng Error → UI | chưa tạo |
| core/notification | `core/notification/AlarmMemorialReminderScheduler.kt`, `MemorialReminderReceiver.kt`, `MemorialRescheduleReceiver.kt`, `NotificationChannels.kt` | `AlarmManager` (⚠️ exact alarm bên dưới), receiver hiện thông báo rồi lập kỳ kế tiếp, receiver `BOOT_COMPLETED` / `TIMEZONE_CHANGED` / `TIME_SET` / `MY_PACKAGE_REPLACED` lập lại từ Room (AC22–AC24) | chưa tạo |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | Không log body của `/v1/memorials` (AC30, NFR-MEM-02) | có — cần sửa |
| Manifest / res | `AndroidManifest.xml`, `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` | Quyền `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` (+ exact alarm nếu chọn); khai báo receiver; loại `innerlamp_memorial.db` khỏi cloud backup/chuyển máy | có — cần sửa |
| DI | `di/MemorialModule.kt` | Wiring thủ công: API service, DB, DAO, repository, scheduler, use cases, factory cho `GioViewModel` | chưa tạo |
| Session (feature khác) | `ui/screens/login/`, `core/network/auth/` | Có phiên hay không, token Bearer, làm mới phiên, đăng xuất gọi `MemorialRepository.clearLocal()` + `cancelAll()` (AC25) | stub — ngoài spec này |
| Tests | `app/src/test/java/com/example/innerlamp/…` | `MemorialScheduleTest`, `GioFormatTest`, `GioViewModelTest` (có); vector, rules, planner, mapper, repository (chưa) | có một phần |

## Navigation flow

```
HomeRoute
  └─ GioRoute  (GioViewModel gắn vào entry này)
       (mở, chưa đăng nhập) ──▶ trạng thái khách ──(Đăng nhập)──▶ LoginRoute ──▶ quay lại GioRoute   ⚠️ AC2
       (mở, đã đăng nhập) ──▶ LoadMemorialBook ──▶ sổ online | sổ offline | lỗi tải
       (bấm thêm / bấm một mục) ──▶ GioEditRoute
       (back) ──▶ HomeRoute
GioEditRoute  (ViewModel lấy từ entry của GioRoute)
       (Lưu, thành công) ──▶ pop về GioRoute + flash "Đã lưu vào sổ giỗ."
       (Xóa → xác nhận, thành công) ──▶ pop về GioRoute + flash "Đã xóa <tên> khỏi sổ giỗ."
       (Xóa → huỷ) ──▶ ở lại
       (back / hệ thống back) ──▶ discardDraft ──▶ pop về GioRoute
```

Mọi lối ra khỏi `GioEditRoute` đi qua `draft = null` rồi pop đúng một lần (giữ như code hiện tại).
Process death khi đang ở form: form tự đóng, mất bản nháp (giữ quyết định cũ — Deferred).

## Step-by-step Flow — Mở sổ (tải + offline + lập nhắc)

```
Step 1 — Kiểm tra phiên (Login / core/network, out of scope)
- Trigger: GioRoute được mở, hoặc người dùng bấm thử lại
- Pre-condition: —
- Where: GioViewModel → LoadMemorialBookUseCase → (nguồn phiên của feature Login)
    - Không có phiên → state khách (AC2); dừng, không đọc Room
    - Có phiên → Step 2

Step 2 — Tải sổ (this feature)
- Trigger: Step 1 có phiên
- Pre-condition: có access token trong AuthTokenProvider
- Where: LoadMemorialBookUseCase → MemorialRepositoryImpl → NetworkClient.safeCall
- API: GET /v1/memorials (@Authenticated)
    - Success → Step 3
    - Failure (AppError.Unauthorized, sau khi làm mới phiên thất bại) → state khách (AC3)
    - Failure (NoConnection / Timeout) → đọc Room: có dữ liệu → sổ offline (AC27) rồi Step 4;
      rỗng (chưa từng tải) → lỗi tải + thử lại (AC28)
    - Failure (5xx / Unknown / Serialization) → như dòng trên (⚠️ product, bảng Error → UI)

Step 3 — Thay bản offline (this feature)
- Trigger: Step 2 thành công
- Where: MemorialRepositoryImpl → MemorialCacheDao.replaceAll (một transaction)
    - Success → trả MemorialBook(entries, offline = false)
    - Failure (lỗi Room) → vẫn trả dữ liệu vừa tải (bản offline giữ bản cũ); không báo người dùng

Step 4 — Lập lại lịch nhắc (this feature)
- Trigger: có danh sách (online ở Step 3, hoặc offline ở Step 2)
- Where: LoadMemorialBookUseCase → MemorialReminderScheduler.replanAll(entries)
    - Mỗi mục nhắc bật → một alarm tại MemorialReminderPlanner.nextMoment(entry, now)
    - Mục nhắc tắt / không còn trong danh sách → huỷ alarm (requestCode = id)
    - Không có quyền thông báo → vẫn lập alarm; ⚠️ AC21 (product) cho những gì người dùng thấy

Step 5 — Hiển thị (this feature)
- Where: GioViewModel: MemorialSchedule.next(entry, today) cho từng mục → sắp theo daysUntil
    - Kết quả → GioUiState(items, loadState = Online | Offline)
```

## Step-by-step Flow — Lưu một mục (thêm / sửa)

```
Step 1 — Kiểm tra trên máy (this feature)
- Trigger: bấm "Lưu vào sổ giỗ" / "Lưu thay đổi"
- Pre-condition: draft != null, không đang lưu
- Where: GioViewModel → MemorialDateRules + kiểm tên/ghi chú
    - Tên rỗng sau trim → lỗi dưới ô tên (FR-MEM-012); tên/ghi chú quá dài → lỗi field (AC12, AC13)
    - Ngày không hợp lệ / tháng nhuận không có → lỗi ngày (AC14)
    - Hợp lệ → saving = true (AC18), Step 2

Step 2 — Gửi lên server (this feature)
- Where: SaveMemorialEntryUseCase → MemorialRepositoryImpl → MemorialMapper → NetworkClient.safeCall
- Mapper: có năm → death_date dương (âm → dương qua MemorialSchedule, có cờ nhuận, BR-MEM-10);
  không năm → death_day/death_month/death_leap_month; reminder (days_before, time, timezone = vùng
  của máy) hoặc null; note rỗng → null; add_to_os_reminder: giữ giá trị đã đọc (⚠️ AC16), mới → false
- API: POST /v1/memorials (id == null) | PUT /v1/memorials/{id} (cite openapi, không chép schema)
    - Success → Step 3
    - Failure (409 MEMORIAL_LIMIT_REACHED) → ở lại form, báo sổ đầy
    - Failure (404 MEMORIAL_NOT_FOUND, chỉ PUT) → xoá mục khỏi Room, cancel(id), báo mục không còn,
      pop về sổ (AC16)
    - Failure (NoConnection / Timeout) → ở lại form, báo cần kết nối (AC29)
    - Failure (Unauthorized) → state khách (AC3)
    - Failure (khác) → ở lại form, báo không lưu được
    - Mọi failure → saving = false, giữ dữ liệu đã nhập

Step 3 — Ghi bản offline (this feature)
- Where: MemorialRepositoryImpl → MemorialCacheDao.upsert(mục server trả về)
    - Failure (Room) → bỏ qua; lần tải sau sẽ thay bản offline

Step 4 — Lập lại nhắc của mục (this feature)
- Where: SaveMemorialEntryUseCase → MemorialReminderScheduler.replanAll(sổ hiện tại)
    - Nhắc bật → alarm mới thay alarm cũ; nhắc tắt → huỷ

Step 5 — Quay về sổ (this feature)
- Where: GioViewModel: cập nhật items, flash "Đã lưu vào sổ giỗ.", draft = null → GioEditScreen pop
```

## Step-by-step Flow — Xoá một mục

```
Step 1 — Xác nhận (this feature)
- Trigger: bấm "Xóa khỏi sổ giỗ" (chỉ có khi sửa mục đã lưu)
- Where: GioViewModel → confirmDelete = true → AlertDialog
    - Huỷ → confirmDelete = false, không đổi gì
    - Xác nhận → saving = true, Step 2

Step 2 — Xoá trên server (this feature)
- Where: DeleteMemorialEntryUseCase → MemorialRepositoryImpl → NetworkClient.safeCall
- API: DELETE /v1/memorials/{id}
    - Success → Step 3
    - Failure (404 MEMORIAL_NOT_FOUND) → ⚠️ AC19 (product); đề xuất xử lý như thành công
    - Failure (NoConnection / Timeout) → giữ nguyên, báo cần kết nối (AC29)
    - Failure (Unauthorized) → state khách; khác → báo không xoá được, giữ nguyên

Step 3 — Bản offline + nhắc (this feature)
- Where: MemorialCacheDao.delete(id); MemorialReminderScheduler.cancel(id)

Step 4 — Quay về sổ (this feature)
- Where: GioViewModel: bỏ mục khỏi items, flash "Đã xóa <tên> khỏi sổ giỗ.", draft = null
```

## Step-by-step Flow — Nhắc tới giờ, và lập lại sau khởi động

```
Step 1 — Alarm tới giờ (core/notification)
- Trigger: AlarmManager gọi MemorialReminderReceiver (extra: id)
- Where: MemorialReminderReceiver → MemorialCacheDao.get(id)
    - Không còn mục / nhắc đã tắt → không hiện gì
    - Còn → Step 2

Step 2 — Hiện thông báo (core/notification)
- Where: NotificationManagerCompat.notify(channel "memorial_reminders", id)
    - Nội dung + hiển thị trên màn hình khoá: ⚠️ AC23 (product); đề xuất VISIBILITY_PRIVATE với bản
      public không có tên
    - Không có quyền → hệ thống không hiện; không làm gì thêm
    - Bấm thông báo → mở app tại GioRoute

Step 3 — Lập kỳ kế tiếp (core/notification)
- Where: MemorialReminderPlanner.nextMoment(entry, now) sau kỳ vừa nhắc → đặt alarm mới

Step 4 — Lập lại sau sự kiện hệ thống (core/notification)
- Trigger: BOOT_COMPLETED, TIMEZONE_CHANGED, TIME_SET, MY_PACKAGE_REPLACED
- Where: MemorialRescheduleReceiver → MemorialCacheDao.getAll() → replanAll(entries); không gọi mạng
```

## Local storage

| Dữ liệu | Nơi lưu | Vì sao |
|---|---|---|
| Bản offline của sổ (mọi field của mục như server trả về) | Room, DB riêng `innerlamp_memorial.db` | Dữ liệu có cấu trúc → Room (`AGENTS.md`); DB riêng để loại khỏi cloud backup / chuyển máy mà không ảnh hưởng `innerlamp.db` (AC31, NFR-MEM-02); là bản sao, thay toàn bộ ở mỗi lần tải (product §3.5) |
| Alarm đã đặt | `AlarmManager` (hệ thống) | Không lưu riêng; lập lại từ Room (Step 4 flow nhắc) |
| Token | `core/storage` secure (feature Login) | Ngoài spec này |

Không lưu gì vào prefs. Đăng xuất: xoá bảng offline + huỷ mọi alarm (AC25, ⚠️ product).

> ⚠️ Not confirmed: dùng exact alarm (`setExactAndAllowWhileIdle`, cần người dùng cấp
> `SCHEDULE_EXACT_ALARM` trên API 31+) hay alarm không chính xác (`setAndAllowWhileIdle` / `setWindow`,
> có thể trễ vài phút khi Doze) — đề xuất mặc định: exact khi `canScheduleExactAlarms()`, không thì
> `setWindow` 15 phút; không dùng `USE_EXACT_ALARM` (chính sách Play) — chặn task Phase 4 "Scheduler".

## Ghi chú kỹ thuật

- **Lịch là nguồn sai vĩnh viễn.** Ngày mất có năm được gửi dưới dạng dương đã quy đổi; quy đổi sai
  thì server lưu sai mãi (product §3.7). Vì vậy vector `death-date-conversion.json` là cổng bắt buộc
  trước khi bật `POST`/`PUT` thật (tasks Phase 5).
- **Ngày âm có năm khi đọc về**: `death_date` dương + `death_calendar = lunar` → `LunarCalendar.toLunar`
  ra ngày/tháng/năm âm và cờ nhuận (product Appendix B mục 1).
- **"Hôm nay"**: kỳ giỗ và số ngày dùng ngày của máy (BR-MEM-08); kiểm "không sau hôm nay" của ngày
  mất dùng `Asia/Ho_Chi_Minh` (BR-MEM-03).
- **Giờ nhắc** theo giờ địa phương của máy hiện tại (BR-MEM-11), không theo `timezone` đã lưu ở mục
  (mục có thể đặt từ máy ở múi khác); app vẫn gửi múi giờ của máy khi lưu (Appendix B mục 3).
- **Không gửi lựa chọn ngoài product**: backend nhận rộng hơn (0..30 ngày, mọi `HH:mm`), app chỉ gửi
  0/1/3/7 và 06:00/07:00/12:00/19:00; giá trị khác đọc về (từ client cũ) được hiện nguyên và chỉ
  hiện tới khi người dùng sửa (form chỉ cho chọn lại trong danh sách của product).
- **Chọn ngày/tháng/năm** bằng `DropdownMenu` (Material3) thay `<select>` của prototype; năm có 127+
  lựa chọn (1900 → nay) nên cân nhắc bottom sheet có cuộn — quyết định UI, không đổi hành vi.
- **Tên folder**: code giữ `ui/screens/gio/`; product §1.5 khuyên spec mới dùng `Memorial` — đổi tên
  package là việc tuỳ chọn (Deferred), không chặn.

## Deliberately stubbed for now

- `GetMemorialEntriesUseCase` trả 3 người mẫu từ prototype; `GioViewModel` sửa/xoá trong bộ nhớ —
  thay ở Phase 1 + Phase 3 (product §2.7: dữ liệu này bỏ, không migrate).
- Công tắc "Thêm vào Lời nhắc (Reminders)" chỉ lật cờ `syncReminders` — bỏ ở Phase 2 (AC26).
- Hàng "Lấy từ gia phả" ở trạng thái trống là no-op (`onGiaPha = {}` trong `MainActivity.kt`) —
  ⚠️ AC6 (product).
- Phiên đăng nhập: `LoginViewModel` là stub — feature Login, chặn Phase 3 chạy thật.

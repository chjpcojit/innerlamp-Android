# Design — Memorial

Version: 2.1
Last updated: 2026-10-14
Status: in-progress — UI (`ui/screens/gio/`) và tính kỳ giỗ đã có trên dữ liệu in-memory; repository, data layer (API + Room), push FCM, báo múi giờ và phiên đăng nhập chưa có

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Hai màn "Sổ giỗ chạp" (`GioScreen`) và "Thêm/sửa ngày giỗ" (`GioEditScreen`) dùng chung một
`GioViewModel` gắn vào back stack entry của `GioRoute`. Đích đến: dữ liệu đi
`GioViewModel → use case → MemorialRepository (domain) → MemorialRepositoryImpl (data) →
NetworkClient.safeCall` cho backend và Room cho bản offline; kỳ giỗ, số ngày còn lại và lần giỗ tính
thuần Kotlin trong `domain/calendar/` để **hiển thị** (product §2.7, §3.6). Kiểu **online-first, cache
đọc offline**: mỗi lần mở sổ gọi `GET /v1/memorials`, thành công thì thay bản Room của tài khoản; mất
mạng hoặc server lỗi thì đọc Room, không cho sửa (FR-MEM-010).

**Nhắc không còn ở trên máy** (ADR-0002, product v2.0): server tính thời điểm nhắc và gửi vào danh sách
Thông báo + push FCM (ADR-0001). Phía Android chỉ còn: lưu cấu hình nhắc của mục qua API; xin quyền
`POST_NOTIFICATIONS`; đăng ký/xoá token FCM theo phiên; hiện push và mở mục khi bấm; báo múi giờ của
máy làm múi giờ tài khoản. Phần push/token/múi giờ là hạ tầng dùng chung (sau này cho thông báo chùa…)
nên đặt ở `core/push/` và `domain/account/`; khi có spec Android cho Thông báo/Tài khoản thì chuyển
phần đó sang spec ấy.

Hiện trạng: `GioViewModel` giữ `List<MemorialEntry>` seed từ `GetMemorialEntriesUseCase`, không có
repository, không coroutine, không phiên, không Firebase. Kiến trúc giữ nguyên `ui → domain ← data →
core/{network,storage}`, DI thủ công trong `di/`, đổi thread qua `DispatcherProvider`. Gần nhất để
làm mẫu: `TempleOptionRepositoryImpl` / `di/TempleModule.kt` (Retrofit + `safeCall`) và
`RoomSutraQuoteRepository` / `di/DatabaseModule.kt` (Room). `JsonProvider` đã bật
`ignoreUnknownKeys` nên field contract sẽ bỏ (`reminder.timezone`, `add_to_os_reminder`) không làm
vỡ parse.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-14.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Screen | `ui/screens/gio/GioScreen.kt` | Sổ: header "N người thân", thẻ nổi bật, danh sách, trạng thái trống **không** có hàng "Lấy từ gia phả" (AC6); thêm: trạng thái khách, đang tải, offline, lỗi tải (AC2, AC32, AC33); dấu tháng nhuận (AC5) | có — cần sửa |
| UI / Screen | `ui/screens/gio/GioEditScreen.kt` | Form: tên, quan hệ, lịch, ngày/tháng/năm (1900 → nay), tháng nhuận (chỉ âm), ghi chú, nhắc, xem trước; thêm: hộp xác nhận xoá, trạng thái đang lưu, gợi ý "Mở Cài đặt" khi máy không cho phép push (AC23); bỏ công tắc "Lời nhắc" (AC31) | có — cần sửa |
| UI / Format | `ui/screens/gio/GioFormat.kt` | Chuỗi hiển thị; thêm dấu "nhuận"; bỏ "· có trong Lời nhắc" (AC31) | có — cần sửa |
| UI / State | `ui/screens/gio/GioUiState.kt` | `items`, `flash`, `draft`; thêm: `loadState` (khách / đang tải / online / offline / lỗi), `saving`, `confirmDelete`, lỗi field, `pushAllowed` | có — cần sửa |
| UI / ViewModel | `ui/screens/gio/GioViewModel.kt` | Gọi use case trong `viewModelScope`, map `Result` → state + Error → UI; `today` tiêm vào; tính lại khi `ON_RESUME` sang ngày mới (AC7); `openId` từ route → mở form của mục sau khi tải (AC27); đọc `PushPermissionChecker` khi `ON_RESUME` (AC23) | có — cần sửa (đang in-memory) |
| UI / Navigation | `ui/navigation/Routes.kt` (`GioRoute`, `GioEditRoute`), wiring trong `MainActivity.kt` | Home → `GioRoute` → `GioEditRoute`; `GioRoute` thành `data class GioRoute(val openId: Long? = null)` để mở từ nhắc; khách → Login (⚠️ AC2) | có — cần sửa (ViewModel đang `viewModel()` không factory) |
| UI / Deep link | `MainActivity.kt` (`onCreate` / `onNewIntent`) | Intent từ push nhắc giỗ → `navigate(GioRoute(openId))`; activity `singleTop` (AC27). Feature Thông báo gọi cùng route (AC28) | chưa có |
| UI / Permission | `ui/screens/gio/GioEditScreen.kt` | `rememberLauncherForActivityResult(RequestPermission)` cho `POST_NOTIFICATIONS` trên API 33+ khi bật nhắc / lưu mục có nhắc, chỉ lần đầu (AC22); "Mở Cài đặt" → `Settings.ACTION_APP_NOTIFICATION_SETTINGS` (AC23) | chưa tạo |
| Domain / Model | `domain/model/MemorialEntry.kt` | Thêm `leapMonth`; `id: Long?` của server (`null` cho bản nháp mới); `note: String?`; **bỏ** `syncReminders`; cấu hình nhắc = `remind`, `leadDays`, `remindTime` (không múi giờ) | có — cần sửa |
| Domain / Calendar | `domain/calendar/LunarCalendar.kt` | Lịch âm Việt Nam UTC+7 (BR-MEM-10); `toLunar` trả cả cờ nhuận | có |
| Domain / Calendar | `domain/calendar/MemorialSchedule.kt` | `next(entry, today)` — kỳ giỗ tới, số ngày, lần giỗ (BR-MEM-05…09); thêm quy đổi âm → dương **có** tháng nhuận cho ngày mất có năm (BR-MEM-04, BR-MEM-10) | có — cần sửa |
| Domain / Calendar | `domain/calendar/MemorialDateRules.kt` | Kiểm ngày mất theo BR-MEM-03/04 (tồn tại, 1900 → nay, không sau hôm nay theo `Asia/Ho_Chi_Minh`, tháng nhuận có trong năm) | chưa tạo |
| Domain / Rules | `domain/model/MemorialReminderChoices.kt` | Lựa chọn của BR-MEM-11 và `nearest(leadDays, time)` cho giá trị cũ (AC21; ⚠️ product khi cách đều) | chưa tạo |
| Domain / Repository | `domain/repository/MemorialRepository.kt` | `load(): Result<MemorialBook>` (kèm cờ offline), `create`, `update`, `delete`, `clearLocal()` | chưa tạo |
| Domain / UseCase | `domain/usecase/GetMemorialEntriesUseCase.kt` | Hiện trả seed 3 người mẫu → thay bằng `LoadMemorialBookUseCase` | có — thay |
| Domain / UseCase | `domain/usecase/LoadMemorialBookUseCase.kt`, `SaveMemorialEntryUseCase.kt`, `DeleteMemorialEntryUseCase.kt` | `operator fun invoke` → `Result`; gọi repository; dispatcher qua `DispatcherProvider`; **không** gọi scheduler nào (AC30) | chưa tạo |
| Domain / Account | `domain/repository/PushTokenRepository.kt`, `domain/repository/AccountTimeZoneRepository.kt`, `domain/usecase/RegisterPushTokenUseCase.kt`, `UnregisterPushTokenUseCase.kt`, `ReportAccountTimeZoneUseCase.kt` | Đăng ký/xoá token (AC24, AC25); báo múi giờ khi khác lần trước (AC29) | chưa tạo |
| Domain / Port | `domain/push/PushPermissionChecker.kt` | `isPushAllowed(): Boolean` — impl ở `core/push` bằng `NotificationManagerCompat.areNotificationsEnabled()` + trạng thái kênh nhắc giỗ | chưa tạo |
| Data / Remote | `data/remote/api/MemorialApiService.kt`, `data/remote/dto/MemorialDto.kt` | Retrofit `@Authenticated` cho 4 operation (bảng "Dữ liệu & API"); DTO `@Serializable` theo `openapi.yaml` @ bf23f8b, không khai báo `reminder.timezone`/`add_to_os_reminder` khi gửi | chưa tạo |
| Data / Remote | `data/remote/api/PushTokenApiService.kt`, `AccountApiService.kt` | ⚠️ Not confirmed (contract) — chưa có operation; không tạo tới khi contract có | chặn |
| Data / Mapper | `data/mapper/MemorialMapper.kt` | DTO ↔ domain: `relationship` ↔ `MemorialRelation` (theo tên); ngày mất hai dạng; `death_calendar`; nhắc (số ngày, giờ) hoặc `null`; `note` rỗng → `null` | chưa tạo |
| Data / Local | `core/database/memorial/MemorialCacheDatabase.kt`, `MemorialCacheEntity.kt`, `MemorialCacheDao.kt` | Bản offline (Room), DB riêng `innerlamp_memorial.db` để loại khỏi backup (AC37); mỗi dòng có `accountId`; `replaceAll(accountId, …)` trong một transaction; `getAll(accountId)`; `clear()` | chưa tạo |
| Data / Repository | `data/repository/MemorialRepositoryImpl.kt` | Gọi API qua `NetworkClient.safeCall`; ghi Room sau khi server thành công; tải lỗi mạng **hoặc** lỗi server → đọc Room của tài khoản hiện tại; map `AppError` → lỗi domain cho bảng Error → UI | chưa tạo |
| Data / Repository | `data/repository/PushTokenRepositoryImpl.kt`, `AccountTimeZoneRepositoryImpl.kt` | Gọi API (⚠️ contract); múi giờ đã báo lưu ở `core/storage` prefs (`account_time_zone_reported`) — không phải bí mật | chưa tạo |
| core/push | `core/push/InnerLampMessagingService.kt` (`FirebaseMessagingService`), `core/push/NotificationChannels.kt`, `core/push/AndroidPushPermissionChecker.kt` | `onNewToken` → `RegisterPushTokenUseCase` nếu đang đăng nhập (AC24); `onMessageReceived` → hiện thông báo trên kênh nhắc giỗ với đúng nội dung server, `PendingIntent` mở `MainActivity` kèm id mục (AC26, AC27; ⚠️ payload); không log payload (AC36) | chưa tạo |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | Không log body của `/v1/memorials` (AC36, NFR-MEM-02) | có — cần sửa |
| App lifecycle | `InnerLampApplication.kt` | `ProcessLifecycleOwner` `ON_START` → `ReportAccountTimeZoneUseCase` (AC29) và đăng ký lại token nếu lần trước thất bại (AC24) | có — cần sửa |
| Manifest / res | `AndroidManifest.xml`, `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` | `POST_NOTIFICATIONS`; khai báo `InnerLampMessagingService` (`com.google.firebase.MESSAGING_EVENT`); `MainActivity` `launchMode="singleTop"`; loại `innerlamp_memorial.db` khỏi cloud backup/chuyển máy. **Không** có `RECEIVE_BOOT_COMPLETED`, exact alarm hay receiver nhắc (AC30) | có — cần sửa |
| Build | `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/{dev,staging,prod}/google-services.json` | Plugin `com.google.gms.google-services`, Firebase BoM + `firebase-messaging`; file cấu hình theo flavor do product owner cung cấp (ADR-0001) | chưa có |
| DI | `di/MemorialModule.kt`, `di/PushModule.kt` | Wiring thủ công: API service, DB, DAO, repository, use cases, factory cho `GioViewModel`; push/time zone repos và use cases | chưa tạo |
| Session (feature khác) | `ui/screens/login/`, `core/network/auth/` | Có phiên hay không, account id, token Bearer, làm mới phiên; **hook**: đăng nhập thành công → `RegisterPushTokenUseCase` + `ReportAccountTimeZoneUseCase`; đăng xuất → `UnregisterPushTokenUseCase` + `MemorialRepository.clearLocal()` (AC24, AC25, AC29) | stub — ngoài spec này |
| Thông báo (feature khác) | — | Danh sách Thông báo; chọn một nhắc giỗ → `navigate(GioRoute(openId))` (AC28) | chưa có — ngoài spec này |
| Tests | `app/src/test/java/com/example/innerlamp/…` | `MemorialScheduleTest`, `GioFormatTest`, `GioViewModelTest` (có); vector, rules, nearest choice, mapper, repository, push/time zone use cases (chưa) | có một phần |

## Navigation flow

```
HomeRoute
  └─ GioRoute(openId = null)  (GioViewModel gắn vào entry này)
       (mở, chưa đăng nhập) ──▶ trạng thái khách ──(Đăng nhập)──▶ LoginRoute ──▶ quay lại GioRoute   ⚠️ AC2
       (mở, đã đăng nhập) ──▶ LoadMemorialBook ──▶ sổ online | sổ offline | lỗi tải
       (bấm thêm / bấm một mục) ──▶ GioEditRoute
       (back) ──▶ HomeRoute
Push nhắc giỗ (bấm) ──▶ MainActivity (onCreate / onNewIntent) ──▶ GioRoute(openId = id)
Danh sách Thông báo (feature khác) ──▶ GioRoute(openId = id)
       sau khi tải (online hoặc offline): có mục id ──▶ GioEditRoute
                                         không có ──▶ ở sổ   ⚠️ AC27 (product)
GioEditRoute  (ViewModel lấy từ entry của GioRoute)
       (Lưu, thành công) ──▶ pop về GioRoute + flash "Saved"
       (Lưu/Xoá, 404) ──▶ pop về GioRoute + flash "Entry no longer exists"
       (Xóa → xác nhận, thành công) ──▶ pop về GioRoute + flash "Deleted"
       (Xóa → huỷ) ──▶ ở lại
       (Mở Cài đặt) ──▶ cài đặt thông báo của app (hệ thống) ──▶ quay lại, ON_RESUME đọc lại quyền
       (back / hệ thống back) ──▶ discardDraft ──▶ pop về GioRoute
```

Mọi lối ra khỏi `GioEditRoute` đi qua `draft = null` rồi pop đúng một lần (giữ như code hiện tại).
Process death khi đang ở form: form tự đóng, mất bản nháp (giữ quyết định cũ — Deferred).

## Step-by-step Flow — Mở sổ (tải + offline)

```
Step 1 — Kiểm tra phiên (Login / core/network, out of scope)
- Trigger: GioRoute được mở (kể cả từ nhắc), hoặc người dùng bấm thử lại
- Where: GioViewModel → LoadMemorialBookUseCase → (nguồn phiên của feature Login)
    - Không có phiên → state khách (AC2); dừng, không đọc Room
    - Có phiên → Step 2 (biết accountId)

Step 2 — Tải sổ (this feature)
- Pre-condition: có access token trong AuthTokenProvider
- Where: LoadMemorialBookUseCase → MemorialRepositoryImpl → NetworkClient.safeCall
- API: GET /v1/memorials (@Authenticated)
    - Success → Step 3
    - Failure (AppError.Unauthorized, sau khi làm mới phiên thất bại) → state khách (AC3)
    - Failure (NoConnection / Timeout / 5xx / Unknown / Serialization) → MemorialCacheDao.getAll(accountId):
      có dữ liệu → sổ offline + "Showing the offline copy" (AC32) rồi Step 4;
      rỗng → "Cannot load, no offline copy" + thử lại (AC33)

Step 3 — Thay bản offline (this feature)
- Where: MemorialRepositoryImpl → MemorialCacheDao.replaceAll(accountId, entries) (một transaction;
  xoá mọi dòng của account khác — AC35)
    - Success → MemorialBook(entries, offline = false)
    - Failure (lỗi Room) → vẫn trả dữ liệu vừa tải; không báo người dùng

Step 4 — Hiển thị (this feature)
- Where: GioViewModel: MemorialSchedule.next(entry, today) cho từng mục → sắp theo daysUntil;
  giá trị nhắc ngoài lựa chọn → MemorialReminderChoices.nearest để hiện (AC21)
    - Kết quả → GioUiState(items, loadState = Online | Offline)
    - openId != null → mở draft của mục đó → GioEditRoute; không có → ⚠️ AC27 (product)
```

Không còn bước "lập lịch nhắc" (AC30).

## Step-by-step Flow — Lưu một mục (thêm / sửa)

```
Step 1 — Kiểm tra trên máy (this feature)
- Trigger: bấm "Lưu vào sổ giỗ" / "Lưu thay đổi"
- Pre-condition: draft != null, không đang lưu
- Where: GioViewModel → MemorialDateRules + kiểm tên/ghi chú
    - Lỗi tên / ghi chú / ngày → câu §3.1.1 tương ứng (AC12–AC14)
    - Hợp lệ → saving = true (AC18); nếu nhắc bật, API 33+ và chưa từng xin quyền → xin
      POST_NOTIFICATIONS (AC22), không chờ kết quả; Step 2

Step 2 — Gửi lên server (this feature)
- Where: SaveMemorialEntryUseCase → MemorialRepositoryImpl → MemorialMapper → NetworkClient.safeCall
- Mapper: có năm → death_date dương (âm → dương có cờ nhuận, BR-MEM-10); không năm →
  death_day/death_month/death_leap_month; reminder (days_before, time) hoặc null — gửi lựa chọn đang
  hiện (AC21); không gửi reminder.timezone, add_to_os_reminder (⚠️ AC15 contract); note rỗng → null
- API: POST /v1/memorials (id == null) | PUT /v1/memorials/{id}
    - Success → Step 3
    - Failure (409 MEMORIAL_LIMIT_REACHED) → ở lại form, "Book is full"
    - Failure (404 MEMORIAL_NOT_FOUND, chỉ PUT) → MemorialCacheDao.delete(id), bỏ khỏi items,
      flash "Entry no longer exists", pop về sổ (AC16)
    - Failure (NoConnection / Timeout) → ở lại form, "Connection needed to change" (AC34)
    - Failure (Unauthorized) → state khách (AC3)
    - Failure (khác) → ở lại form, "Could not save"
    - Mọi failure → saving = false, giữ dữ liệu đã nhập

Step 3 — Ghi bản offline (this feature)
- Where: MemorialRepositoryImpl → MemorialCacheDao.upsert(accountId, mục server trả về)
    - Failure (Room) → bỏ qua; lần tải sau sẽ thay bản offline

Step 4 — Quay về sổ (this feature)
- Where: GioViewModel: cập nhật items, flash "Saved", draft = null → GioEditScreen pop
```

Server tự lập lại nhắc của mục (FR-MEM-004, BR-MEM-11); app không làm gì thêm.

## Step-by-step Flow — Xoá một mục

```
Step 1 — Xác nhận (this feature)
- Trigger: bấm "Xóa khỏi sổ giỗ" (chỉ có khi sửa mục đã lưu)
- Where: GioViewModel → confirmDelete = true → AlertDialog theo §3.1.1 "Confirm delete"
    - Huỷ → confirmDelete = false, không đổi gì
    - Xác nhận → saving = true, Step 2

Step 2 — Xoá trên server (this feature)
- Where: DeleteMemorialEntryUseCase → MemorialRepositoryImpl → NetworkClient.safeCall
- API: DELETE /v1/memorials/{id}
    - Success → Step 3, flash "Deleted"
    - Failure (404 MEMORIAL_NOT_FOUND) → Step 3, flash "Entry no longer exists" (AC19)
    - Failure (NoConnection / Timeout) → giữ nguyên, "Connection needed to change" (AC34)
    - Failure (Unauthorized) → state khách; khác → "Could not save", giữ nguyên

Step 3 — Bản offline + quay về sổ (this feature)
- Where: MemorialCacheDao.delete(id); GioViewModel: bỏ mục khỏi items, draft = null → pop
```

## Step-by-step Flow — Đăng nhập / đăng xuất: token FCM và múi giờ

```
Step 1 — Đăng nhập thành công (Login, out of scope) → hook
- Trigger: feature Login báo phiên mới
- Where: LoginViewModel (hoặc session manager) → RegisterPushTokenUseCase, ReportAccountTimeZoneUseCase

Step 2 — Đăng ký token FCM (this spec, hạ tầng dùng chung)
- Where: RegisterPushTokenUseCase → FirebaseMessaging.getToken() → PushTokenRepositoryImpl → safeCall
- API: ⚠️ Not confirmed (contract) — đăng ký push token (Appendix B mục 8)
    - Success → xong
    - Failure (mọi lỗi) → không hiện gì; thử lại ở ON_START kế tiếp (InnerLampApplication)
- Cũng chạy khi InnerLampMessagingService.onNewToken và đang có phiên

Step 3 — Báo múi giờ tài khoản (this spec, hạ tầng dùng chung)
- Trigger: Step 1, hoặc ON_START mà ZoneId.systemDefault().id ≠ prefs account_time_zone_reported
- Where: ReportAccountTimeZoneUseCase → AccountTimeZoneRepositoryImpl → safeCall
- API: ⚠️ Not confirmed (contract) — báo múi giờ tài khoản (Appendix B mục 3)
    - Success → lưu zone id vào prefs account_time_zone_reported
    - Failure → không hiện gì; thử lại ở ON_START kế tiếp

Step 4 — Đăng xuất (Login, out of scope) → hook
- Trigger: người dùng đăng xuất
- Where: UnregisterPushTokenUseCase → PushTokenRepositoryImpl (API ⚠️ contract, lỗi bỏ qua) →
  FirebaseMessaging.deleteToken() (token cũ mất hiệu lực, backend tự dọn khi FCM trả UNREGISTERED);
  MemorialRepository.clearLocal() (AC25); xoá prefs account_time_zone_reported
```

## Step-by-step Flow — Nhận và mở push nhắc giỗ

```
Step 1 — Push tới (core/push)
- Trigger: FCM giao message nhắc giỗ (server gửi theo BR-MEM-11/13)
- Where: hệ thống (notification message khi app nền) hoặc InnerLampMessagingService.onMessageReceived
  (app đang mở / data message) → NotificationManagerCompat.notify trên kênh nhắc giỗ
    - Nội dung đúng như server gửi (quan hệ + số ngày, §3.1.1); không thêm tên/ghi chú (AC26)
    - Máy không cho phép push → hệ thống không hiện; nhắc vẫn ở danh sách Thông báo (server)
    - ⚠️ Not confirmed (contract): loại message, khoá id mục, channel id (AC26)

Step 2 — Bấm push (this feature)
- Where: PendingIntent → MainActivity (singleTop) → navigate(GioRoute(openId = id))
    - Tiếp theo như "Mở sổ" Step 1–4 (khách → AC2; offline → mở từ bản offline)
```

## Local storage

| Dữ liệu | Nơi lưu | Vì sao |
|---|---|---|
| Bản offline của sổ (mọi field của mục như server trả về + `accountId`) | Room, DB riêng `innerlamp_memorial.db` | Dữ liệu có cấu trúc → Room (`AGENTS.md`); DB riêng để loại khỏi cloud backup / chuyển máy (AC37, NFR-MEM-02); thay toàn bộ ở mỗi lần tải; gắn tài khoản để không bao giờ hiện bản của tài khoản khác (AC35); xoá khi đăng xuất (AC25) |
| Múi giờ đã báo (`account_time_zone_reported`) | `core/storage` prefs | Cài đặt nhẹ, không bí mật; chỉ để biết khi nào cần báo lại (AC29) |
| Đã từng xin `POST_NOTIFICATIONS` | `core/storage` prefs | Chỉ xin một lần (AC22) |
| Token FCM | Firebase SDK tự giữ | Không lưu riêng |
| Token phiên | `core/storage` secure (feature Login) | Ngoài spec này |

Không có alarm, job hay receiver nhắc nào (AC30).

## Ghi chú kỹ thuật

- **Lịch là nguồn sai vĩnh viễn.** Ngày mất có năm được gửi dưới dạng dương đã quy đổi; quy đổi sai
  thì server lưu sai mãi và nhắc sai (product §3.7, ADR-0002). Vector `death-date-conversion.json` là
  cổng bắt buộc trước khi bật `POST`/`PUT` thật (tasks Phase 5).
- **Ngày âm có năm khi đọc về**: `death_date` dương + `death_calendar = lunar` → `LunarCalendar.toLunar`
  ra ngày/tháng/năm âm và cờ nhuận (product Appendix B mục 1).
- **"Hôm nay"**: kỳ giỗ và số ngày dùng ngày của máy (BR-MEM-08); kiểm "không sau hôm nay" của ngày
  mất dùng `Asia/Ho_Chi_Minh` (BR-MEM-03).
- **Không gửi lựa chọn ngoài product**: backend hiện nhận rộng hơn (0..30 ngày, mọi `HH:mm`); app chỉ
  gửi 0/1/3/7 và 06:00/07:00/12:00/19:00; giá trị cũ hiện lựa chọn gần nhất và chỉ thay khi người dùng
  lưu (AC21).
- **Kênh thông báo**: một kênh nhắc giỗ (`IMPORTANCE_DEFAULT`) tạo ở `Application.onCreate`; nội dung
  không có tên nên `VISIBILITY_PUBLIC` được. Nếu payload là notification message, channel id phải do
  server gửi đúng (⚠️ AC26 contract).
- **API < 33**: không có quyền runtime; `areNotificationsEnabled()` vẫn dùng cho gợi ý AC23.
- **Chọn ngày/tháng/năm** bằng `DropdownMenu` (Material3) thay `<select>` của prototype; năm có 127+
  lựa chọn nên cân nhắc bottom sheet có cuộn — quyết định UI, không đổi hành vi.
- **Tên folder**: code giữ `ui/screens/gio/`; product §1.5 khuyên `Memorial` — đổi tên package là
  việc tuỳ chọn (Deferred).

## Deliberately stubbed for now

- `GetMemorialEntriesUseCase` trả 3 người mẫu; `GioViewModel` sửa/xoá trong bộ nhớ — thay ở Phase 1 +
  Phase 3 (product §2.7: dữ liệu này bỏ, không migrate).
- Công tắc "Thêm vào Lời nhắc (Reminders)" chỉ lật cờ `syncReminders` — bỏ ở Phase 2 (AC31).
- Hàng "Lấy từ gia phả" ở trạng thái trống là no-op (`onGiaPha` mặc định `{}` trong `GioScreen.kt`) — ẩn ở
  Phase 2 (AC6).
- Phiên đăng nhập: `LoginViewModel` là stub — feature Login, chặn Phase 3 và Phase 4 chạy thật.
- Push token, múi giờ tài khoản: chưa có operation trong contract — use case + repository interface
  có thể viết trước, impl data chờ contract (Phase 4).

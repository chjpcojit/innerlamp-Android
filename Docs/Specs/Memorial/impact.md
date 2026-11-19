# Impact & Risk — Sổ giỗ: tích hợp BE cho 3 client

Version: 1.0 · Last updated: 2026-11-16 · Owner: ⚔ Đạo Sư ⚔ (Tech Lead)
Phạm vi: P1c cho 3 repo client. **Chỉ spec — không sửa code, không worktree, không commit.**
Nguồn chung: `innerlamp-memorial/docs/specs/api/openapi.yaml` working tree 2026-11-16 (OpenAPI `3.1.0`, chưa commit; HEAD `e5d4c9c` = 3.0.0); product `docs/product/memorial/product.md` v2.1; contract client `client-contract-memorial-v1.md`.

> Template: `projects/templates/feature/impact.md` (v1.6). Mỗi repo một khối đầy đủ §0–§9.
> **G1 chưa ký**: `reviewed_by` để trống ở cả ba — cần ✦ Thiên Cơ ✦ (BA) và ⚔ Hộ Pháp ⚔ (Security) ký trước P2. Đây là spec, chưa có code để review.

## Tổng hợp

| Repo | HEAD | Criticality | Blast radius | Risk | Luồng | Vì sao |
|---|---|---|---|---|---|---|
| `website/innerlamp` (SvelteKit) | `5e73bc0` | C0 | B1 | 4/16 | M | PII (tên, ngày mất, ghi chú, ảnh); chỉ chạm module memorial + 1 helper API dùng chung |
| `iOS/InnerLamp` (SwiftUI) | `84062d2` | C0 | B2 | 6/16 | L | PII; đổi Core Data model + networking core + util lịch dùng chung |
| `Android/InnerLamp` (Kotlin) | `4ebe5d8` | C0 | B2 | 6/16 | L | PII; thêm Room DB, sửa model dùng chung, Manifest/DI, nhắc local |

---

# 1. Impact — Website SvelteKit (`website/innerlamp`)

```yaml
feature: memorial — BE integration
feature_id: F-MEM-CLIENT-WEB
repo: website/innerlamp
spec: docs/specs/features/memorial/requirements.md
criticality: C0
blast_radius: B1
flow: M
status: Spec
shared: [src/lib/server/api/client.ts, src/lib/utils/lunar.ts]
consumers: 4
test_count: 0   # chưa có test-count trong test-cases.md
review_focus: [authz, contract, pii-logging, regression]
owner: Tech Lead
reviewed_by: ""  # G1: Thiên Cơ (BA), Hộ Pháp (Security)
reviewed_at: ""
updated: 2026-11-16
```

## 0. Bốn câu cho mỗi bề mặt

| Bề mặt | Ai dùng? | Dùng cách nào? | Nếu đổi thì gì fail? | Test nào bắt? |
|---|---|---|---|---|
| `apiCall` | toàn bộ `src/lib/server/api/*` (auth, lamps, temples, users, gallery…) | import, gọi trong `load`/action server | compile; runtime nếu header/Content-Type sai (mọi call JSON) | chưa có `client.spec.ts` → **phải thêm** |
| `utils/lunar.ts` | `almanac.ts`, `HomeHero`, `DayHero`, `DayDetails`, `LunarCalendar`, `MemorialForm` | import hàm thuần | sai ngày âm toàn app (lịch, lễ, sổ giỗ) | `calendar-vectors.spec.ts` (hiện có), thêm vector memorial |
| `types/memorial.ts` + `state/memorial.svelte.ts` | `MemorialBook`, `MemorialForm`, `memorial-format.ts`, `profile.ts`, `ProfileLinks.svelte` | import type/store | compile; mất số đếm ở profile nếu xoá store | chưa có → thêm unit test mapper/format |
| Route `/memorial`, `/memorial/new`, `/memorial/[id]` | người dùng đã đăng nhập; link từ `profile.ts:62` | HTTP/SSR, form action | 500/load fail; link chết | chưa có E2E (Playwright chưa cài) |

## 1. Tóm tắt một dòng

Thay `localStorage` bằng 5 route CRUD qua server (`apiCall` + Bearer từ cookie), giữ nguyên UI; rủi ro chính là helper API dùng chung và quy đổi âm→dương trước khi gửi.

## 2. Shared surface

| Bề mặt | Loại | File / route | Ai dùng | Test hiện có |
|---|---|---|---|---|
| `apiCall`/`ApiError` | package (server) | `src/lib/server/api/client.ts:33-77` | mọi module `src/lib/server/api/*` | chưa có |
| `lunarToSolar`/`solarToLunar` | package | `src/lib/utils/lunar.ts:112,218` | calendar/almanac/home, memorial | `calendar-vectors.spec.ts` |
| `MemorialEntry` view model | type | `src/lib/types/memorial.ts:6-30` | Book/Form/format/profile | chưa có |
| `/memorial*` | route | `src/routes/(site)/memorial/**` | người dùng, `profile.ts:62` | chưa có |

## 3. Consumers

| Consumer | Cách dùng | Caller | Test của consumer | Rủi ro |
|---|---|---|---|---|
| `src/lib/server/api/*` | import `apiCall` | 10 module | chỉ `calendar.spec.ts` | cao (helper dùng chung) |
| `profile.ts`, `ProfileLinks.svelte` | đọc `memorial.entries.length` | 2 | không | trung bình (số đếm sai) |
| `MemorialBook`, `MemorialForm` | type + format | 2 | không | cao (UI chính) |
| `utils/memorial.ts`, `memorial-format.ts` | `nextAnniversary`, nhãn | 3 | không | cao (sai ngày giỗ) |

Đã tìm: `grep -rln "apiCall" src/lib/server/api` (10 file), `grep -rln "utils/lunar" src` (6 file), `grep -rln "types/memorial\|state/memorial" src` (7 file), ngày 2026-11-16.

## 4. Data & contract

- Schema/migration: **không** (không DB).
- API contract: **thêm mới** module `src/lib/server/api/memorials.ts`, không đổi contract cũ.
- Tương thích ngược: không có client cũ gọi API memorial (trước đây local).
- Dữ liệu thật: `localStorage` key `il-memorial` (`state/memorial.svelte.ts:4,19,44`) **không migrate** — product §2.7 nói rõ. Xoá key một lần khi gỡ store; nói trước với người dùng là mục đã nhập trên trình duyệt sẽ mất.
- Rollback: revert PR; dữ liệu đã ghi lên BE vẫn còn; không có migration để down.

## 5. Phân loại

| | Giá trị | Lý do |
|---|---|---|
| Criticality | `C0` | PII: họ tên người đã mất, ngày mất, ghi chú gia đình, ảnh chân dung |
| Blast radius | `B1` | trong module memorial + 1 helper API; không đổi schema/DB |
| Risk | `4/16` | C0×B1 |
| Luồng | `M` | C0 luôn tối thiểu M; không có B2/B3 |

## 6. Review focus

- [x] `authz` — token chỉ đọc từ cookie httpOnly ở server; **không** gọi BE từ browser; không nhận `user_id` từ client.
- [x] `contract` — đúng 7 route, payload tối thiểu, decode bỏ field lạ.
- [x] `pii-logging` — không log tên/ghi chú/`photo_url`; `ApiError` chỉ log code.
- [x] `regression` — `utils/lunar.ts` chỉ **thêm** hàm, không đổi hàm cũ; chạy `calendar-vectors.spec.ts`.
- [ ] `money-path` N/A · `migration` N/A · `shared-pkg` (chỉ helper API, xử lý ở trên) · `rollback` (revert PR, ở §4).

## 7. Verdict

- **Luồng:** M.
- **Ai review kỹ:** Tech Lead — authz server-side, mapping ngày, error→UI; Security — cookie/token, log PII; BA — copy lỗi và empty state.
- **Anh đọc trực tiếp:** diff `client.ts` (helper dùng chung) + `memorials.ts`.
- **Test bổ sung:** unit mapper/validate/nearest + vector; `client.spec.ts` cho multipart; E2E sau khi có Playwright (không chặn P4).
- **Tách PR nền:** có — PR1 helper API + `memorials.ts` + test; PR2 route/action/UI; PR3 ảnh (sau deploy).
- **Điều kiện dừng:** gọi API trực tiếp từ browser; bật UI ảnh khi route chưa deploy; đổi hàm `lunar.ts` cũ; log PII.

## 8. Evidence

| Kết luận | Lệnh / nguồn | Ngày |
|---|---|---|
| Chưa có API memorial | `ls src/lib/server/api` (không có `memorials.ts`) | 2026-11-16 |
| Lưu localStorage | `src/lib/state/memorial.svelte.ts:19,44` | 2026-11-16 |
| Helper dùng chung | `src/lib/server/api/client.ts:34` | 2026-11-16 |
| Base URL dev | `.env:2` | 2026-11-16 |
| Không có spec.config | `find . -name spec.config.yaml` → rỗng | 2026-11-16 |

- GitNexus: không dùng (repo nhỏ, grep đủ).
- Người lập: Đạo Sư · Phản biện: chờ Thiên Cơ/Hộ Pháp.

## 9. Drift log

| Ngày | File/bề mặt phát sinh | Vì sao | Đã nâng luồng? | Ai duyệt |
|---|---|---|---|---|

---

# 2. Impact — iOS SwiftUI (`iOS/InnerLamp`)

```yaml
feature: memorial — BE integration
feature_id: F-MEM-CLIENT-IOS
repo: iOS/InnerLamp
spec: Docs/Specs/Memorial/requirements.md
criticality: C0
blast_radius: B2
flow: L
status: Spec
shared: [InnerLamp.xcdatamodeld, Core/Networking/NetworkService.swift, Core/Utils/ILLunarCalendar.swift]
consumers: 4
test_count: 6   # 6 file test memorial hiện có, chưa chạy
review_focus: [migration, contract, authz, pii-logging, regression]
owner: Tech Lead
reviewed_by: ""
reviewed_at: ""
updated: 2026-11-16
```

## 0. Bốn câu cho mỗi bề mặt

| Bề mặt | Ai dùng? | Dùng cách nào? | Nếu đổi thì gì fail? | Test nào bắt? |
|---|---|---|---|---|
| Core Data model (`InnerLamp 5` → version mới) | `ILMemorialLocalDataSource`, `ILSessionDataCleanerImpl`, toàn app dùng chung stack | `NSPersistentContainer` | migration lỗi → crash khi mở app; mất bản offline; ảnh hưởng mọi entity | `ILMemorialRepositoryTests`, test nâng cấp từ build cũ (thủ công trên máy) |
| `NetworkService` | mọi feature goi API | protocol + impl | thêm method vào protocol → mọi mock conformer vỡ compile | `ILMemorialRepositoryTests`, test networking |
| `ILLunarCalendar` | Home, Sangha, Deity, Almanac, BuddhistEvent, LunarCalendar, Memorial | import static | sai ngày âm toàn app | `ILLunarToSolarTests`, thêm vector |
| `ILMemorialError` | ViewModel/EditViewModel/Repository | enum + switch | thêm case → switch không exhaustive → compile | `ILMemorial*Tests` |
| `DIContainer` | toàn app | khởi tạo repository/VM | thiếu wiring → crash lúc mở sổ | build + test DI |

## 1. Tóm tắt một dòng

Nối `ILMemorialRepositoryImpl` với 5 route CRUD (đã có endpoint), bổ sung ảnh + nhắc local; rủi ro chính là **version Core Data** và **multipart trong networking core**.

## 2. Shared surface

| Bề mặt | Loại | File / route | Ai dùng | Test hiện có |
|---|---|---|---|---|
| Core Data model | schema (local) | `InnerLamp/InnerLamp.xcdatamodeld/` (đang có 2,3,4,5) | memorial cache + session cleaner | `ILMemorialRepositoryTests` |
| `NetworkService` | package (core) | `InnerLamp/Core/Networking/NetworkService.swift:10-13` | mọi feature | networking tests (không bao phủ upload) |
| `ILLunarCalendar` | package (core) | `InnerLamp/Core/Utils/ILLunarCalendar.swift` | 7+ use case | `ILLunarToSolarTests` |
| `ILMemorialError` | enum | `InnerLamp/Domain/Repositories/ILMemorialRepository.swift:25-35` | 3+ view model | 6 file test memorial |
| `APIConfig.serverBaseURL` | config | `InnerLamp/Core/Networking/APIConfig.swift:38-45` | mọi call | không |

## 3. Consumers

| Consumer | Cách dùng | Caller | Test của consumer | Rủi ro |
|---|---|---|---|---|
| Memorial repository/use case | protocol + DTO | 3 | 6 file | cao |
| Home/Sangha/Deity/Almanac/BuddhistEvent | `ILLunarCalendar` | 7 | test rời rạc | cao nếu đổi hàm cũ |
| Session cleaner | xoá `ILMemorialMO` | 1 | không | trung bình |
| Networking | `NetworkService` | toàn app | có | cao nếu sửa protocol |

Đã tìm: `grep -rln "ILLunarCalendar" InnerLamp InnerLampTests` (20 file), `grep -n "func " Core/Networking/NetworkService.swift` (không có upload), `ls InnerLamp/InnerLamp.xcdatamodeld` (5 version), ngày 2026-11-16.

## 4. Data & contract

- Schema/migration: **có** — version Core Data mới: thêm `isLeapMonth` (unknown-year), `ownerAccountID` (chống lẫn cache giữa tài khoản), bỏ dùng `syncToReminders`. Migration nhẹ (attribute optional/default) — không mapping model phức tạp; phải test nâng cấp từ build hiện tại.
- API contract: thêm `MemorialGetEndpoint` (đã có trong design), `MemorialPhotoUploadEndpoint`, `MemorialPhotoDeleteEndpoint`; DTO bỏ gửi `add_to_os_reminder`, `timezone`.
- Tương thích ngược: bản offline cũ đọc được sau migration; không có server cũ.
- Dữ liệu thật: cache Core Data của người dùng hiện tại; **không** backfill; rollback = giữ version cũ, không xoá store (nếu phải revert, app cũ vẫn đọc attribute thừa — Core Data bỏ qua attribute lạ).

## 5. Phân loại

| | Giá trị | Lý do |
|---|---|---|
| Criticality | `C0` | PII + ảnh chân dung; cache trên thiết bị |
| Blast radius | `B2` | Core Data schema + Networking core + util lịch dùng chung |
| Risk | `6/16` | C0×B2 |
| Luồng | `L` | C0 & B2 |

## 6. Review focus

- [x] `migration` — version mới, lightweight, test từ store cũ; không xoá store.
- [x] `contract` — payload tối thiểu; decode bỏ field lạ; 404 = mục mất.
- [x] `authz` — token qua `NetworkService`, không tự gắn header ở feature; cache lọc `ownerAccountID`.
- [x] `pii-logging` — không log tên/note/URL ảnh (AGENTS.md cấm PII).
- [x] `regression` — `ILLunarCalendar` chỉ thêm hàm; chạy test lịch hiện có.
- [ ] `money-path` N/A · `shared-pkg` (network protocol — dùng protocol riêng) · `rollback` (giữ model version cũ).

## 7. Verdict

- **Luồng:** L.
- **Ai review kỹ:** Tech Lead — Core Data migration, mapper ngày/nhuận, repository error; Security — cache theo tài khoản, log PII, ảnh; BA — copy lỗi/offline.
- **Anh đọc trực tiếp:** model version mới + `NetworkService` multipart + mapper.
- **Test bổ sung:** vector product (3 file), migration test, repository 413/415/404, upload multipart.
- **Tách PR nền:** có — PR1 model + DTO/mapper + test; PR2 networking multipart; PR3 UI/reminder local; PR4 ảnh (sau deploy).
- **Điều kiện dừng:** migration không nhẹ/xoá store; sửa `NetworkService` protocol làm vỡ conformer; bật ảnh trước deploy; cache không lọc tài khoản.

## 8. Evidence

| Kết luận | Lệnh / nguồn | Ngày |
|---|---|---|
| 5 route đã có endpoint | `ls InnerLamp/Data/DataSources/Remote/Memorial*Endpoint.swift` (4 file) | 2026-11-16 |
| Chưa có upload ảnh | không có `MemorialPhoto*Endpoint`; `NetworkService` không có upload | 2026-11-16 |
| Base URL dev đang local Mac | `APIConfig.swift:38-45` | 2026-11-16 |
| Core Data có `syncToReminders` | `ILMemorialMO.swift:34` | 2026-11-16 |
| 6 test file memorial | `ls InnerLampTests/ILMemorial*Tests.swift` | 2026-11-16 |

- GitNexus: không dùng.
- Người lập: Đạo Sư · Phản biện: chờ Thiên Cơ/Hộ Pháp.

## 9. Drift log

| Ngày | File/bề mặt phát sinh | Vì sao | Đã nâng luồng? | Ai duyệt |
|---|---|---|---|---|
| | | | | |

---

# 3. Impact — Android Kotlin (`Android/InnerLamp`)

```yaml
feature: memorial — BE integration
feature_id: F-MEM-CLIENT-ANDROID
repo: Android/InnerLamp
spec: Docs/Specs/Memorial/requirements.md
criticality: C0
blast_radius: B2
flow: L
status: Spec
shared: [domain/model/MemorialEntry.kt, domain/calendar/MemorialSchedule.kt, core/network, AndroidManifest.xml]
consumers: 4
test_count: 10  # 5 gốc + vector/mapper/repository/reminder/deep-link/security-config (2026-11-17)
review_focus: [contract, authz, pii-logging, backup-exclusion, notification-privacy, regression, migration]
owner: Tech Lead
reviewed_by: ""
reviewed_at: ""
updated: 2026-11-16
```

## 0. Bốn câu cho mỗi bề mặt

| Bề mặt | Ai dùng? | Dùng cách nào? | Nếu đổi thì gì fail? | Test nào bắt? |
|---|---|---|---|---|
| `MemorialEntry` | `GioViewModel`, `GioScreen`, `GioEditScreen`, `GioFormat`, test | import data class | compile toàn bộ màn sổ/form; `id: String` → `Long` phải sửa route | `GioViewModelTest`, `GioFormatTest` |
| `MemorialSchedule`/`LunarCalendar` | Home, Calendar, Gio, Buddha detail | import object | sai ngày âm/lễ toàn app | `MemorialScheduleTest`, `LunarCalendarTest` |
| `core/network` + `NetworkModule` | mọi feature | Retrofit service + `safeCall` | thiếu provider → crash DI; đổi `safeCall` → mọi feature | test network hiện có (ít) |
| `AndroidManifest` + backup rules | toàn app | manifest merge | thiếu `POST_NOTIFICATIONS` → không xin quyền; backup DB → rò PII | lint + review |
| `GioRoute`/`MainActivity` | Home → sổ → form | navigation | deep link từ nhắc không mở đúng mục | `GioViewModelTest` (một phần) |

## 1. Tóm tắt một dòng

Thay seed in-memory bằng Retrofit + Room cache + nhắc local (WorkManager); rủi ro chính là đổi model dùng chung (`MemorialEntry`), room DB mới và quyền thông báo.

## 2. Shared surface

| Bề mặt | Loại | File / route | Ai dùng | Test hiện có |
|---|---|---|---|---|
| `MemorialEntry` | model | `app/src/main/java/.../domain/model/MemorialEntry.kt:34-60` | 5 file UI + test | `GioViewModelTest` |
| `MemorialSchedule` | package | `domain/calendar/MemorialSchedule.kt` | Home, Calendar, Gio | `MemorialScheduleTest` |
| `LunarCalendar` | package | `domain/calendar/LunarCalendar.kt` | 8 use case | `LunarCalendarTest` |
| `NetworkModule` | DI | `di/NetworkModule.kt` | mọi feature | không |
| Manifest/backup | config | `app/src/main/AndroidManifest.xml`, `res/xml/*rules.xml` | toàn app | lint |

## 3. Consumers

| Consumer | Cách dùng | Caller | Test của consumer | Rủi ro |
|---|---|---|---|---|
| `GioViewModel`/2 screen/format | model + state | 4 | 2 file | cao |
| Home/Calendar | `LunarCalendar`, `MemorialSchedule` | 6 | 2 file | cao |
| `NetworkClient.safeCall` | Retrofit | 5 feature | ít | cao nếu đổi |
| DI graph | `NetworkModule` | toàn app | không | trung bình |

Đã tìm: `grep -rln "LunarCalendar" app/src/main app/src/test` (15 file), `grep -rln "MemorialEntry"` (8 file), `ls app/src/test/.../gio` (2 file), ngày 2026-11-16.

## 4. Data & contract

- Schema/migration: **có** — Room DB mới `innerlamp_memorial.db` (tách khỏi DB chính, loại khỏi backup); không migration từ bản in-memory (không có dữ liệu bền).
- API contract: thêm `MemorialApiService` 7 operation; DTO không khai báo `timezone`/`add_to_os_reminder`.
- Tương thích ngược: không có dữ liệu cũ; seed 3 mục mẫu bị xoá — người dùng thấy sổ trống lần đầu, đúng product §2.7.
- Dữ liệu thật: cache Room theo `accountId`; xoá khi đăng xuất; rollback = xoá DB (không có gì để giữ).

## 5. Phân loại

| | Giá trị | Lý do |
|---|---|---|
| Criticality | `C0` | PII + ảnh; cache thiết bị |
| Blast radius | `B2` | model dùng chung, Room DB mới, DI, Manifest, util lịch |
| Risk | `6/16` | C0×B2 |
| Luồng | `L` | C0 & B2 |

## 6. Review focus

> Sửa 2026-11-17 theo G1 của Hộ Pháp: hai dòng `[x]` cũ (`pii-logging` "không log body", `migration`
> "backup rules loại trừ") **là khai sai** so với code lúc review và đã bị bỏ. A1–A3 dưới đây là điều kiện
> để Hộ Pháp ký G1 lại; trạng thái `[ ]` = đã vá, **chờ xác minh độc lập** (không tự tick).

- [ ] `pii-logging` (A1 — G1 block) — trước bản vá `LoggingInterceptor` để `Level.BODY` ở debug và **log cả
      body** `/v1/memorials` (họ tên, ghi chú, `photo_url`). Nay `/v1/memorials` và `/v1/memorials/{id}/photo`
      nằm trong danh sách no-log cùng `/v1/auth/`; test `LoggingInterceptorTest`. **Chưa** xác minh logcat trên
      máy thật — QA/Đức chạy.
- [ ] `backup-exclusion` (A2 — G1 block) — trước bản vá `allowBackup=true` và rules chỉ loại
      `innerlamp_secure.xml`, nên Room memorial + WorkManager DB vào Google Drive backup. Nay
      `allowBackup=false` (ưu tiên C0) **và** loại `innerlamp_memorial.db` + `androidx.work.workdb` ở cả
      `cloud-backup` lẫn `device-transfer`; test `MemorialSecurityConfigTest`.
- [ ] `notification-privacy` (A3/A5/A6 — G1 block) — WorkManager `inputData` chỉ còn `entry_id`,
      `days_before`, `time`, `anniversary_key` (không quan hệ, không tên); Worker đọc Room và lùi về
      "người thân". Notification `VISIBILITY_PRIVATE`; PendingIntent `FLAG_IMMUTABLE`; `openId` chỉ nhận id
      dương và được tra trong sổ của tài khoản đang đăng nhập. Test `MemorialReminderPayloadTest`,
      `MemorialDeepLinkTest`; riêng `VISIBILITY_PRIVATE` chỉ kiểm bằng review/máy thật.
- [x] `contract` — 7 operation; payload tối thiểu; `ignoreUnknownKeys`; 404 = mục mất.
- [x] `authz` — `@Authenticated` + `AuthTokenProvider`; Room lọc `accountId`; xoá cache khi đăng xuất.
- [x] `regression` — `LunarCalendar`/`MemorialSchedule` chỉ thêm hàm exact; chạy test cũ.
- [ ] `migration` — DB mới, không đụng DB chính. Việc chống rò PII **không** còn dựa vào rules mà vào
      `allowBackup=false` (A2) — cùng điều kiện với `backup-exclusion`.
- [ ] `money-path` N/A · `shared-pkg` (model đổi — xử lý compile) · `rollback` (xoá DB).

## 7. Verdict

- **Luồng:** L.
- **Ai review kỹ:** Tech Lead — model/mapper, Room transaction, WorkManager; Security — cache/backup, quyền thông báo, log; BA — copy lỗi/offline.
- **Anh đọc trực tiếp:** `MemorialEntry` diff + `MemorialRepositoryImpl` + Manifest.
- **Test bổ sung:** vector product, mapper, repository (400/401/404/409/413/415), WorkManager scheduling, ViewModel.
- **Tách PR nền:** có — PR1 model/domain + test; PR2 network/DTO/repo; PR3 Room cache; PR4 UI/reminder local; PR5 ảnh.
- **Điều kiện dừng:** đổi `MemorialEntry` làm vỡ màn khác mà không cập nhật test; Room không lọc `accountId`; bật ảnh trước deploy; dùng exact alarm; **G1:** bất kỳ request `/v1/memorials*` nào đi qua logger body, `allowBackup=true` hoặc rules không loại DB memorial/WorkManager, `inputData` chứa quan hệ/tên, notification không `PRIVATE` hoặc PendingIntent mutable/id không hợp lệ.

## 8. Evidence

| Kết luận | Lệnh / nguồn | Ngày |
|---|---|---|
| Dữ liệu in-memory/seed | `domain/usecase/GetMemorialEntriesUseCase.kt:14-33` | 2026-11-16 |
| Base URL giả | `app/build.gradle.kts:81` | 2026-11-16 |
| Chưa có API service memorial | `ls data/remote/api` (không có `MemorialApiService`) | 2026-11-16 |
| `MemorialEntry.id` là String | `domain/model/MemorialEntry.kt:34` | 2026-11-16 |
| Gate flavor dev | `AGENTS.md` mục Testing | 2026-11-16 |

- GitNexus: không dùng.
- Người lập: Đạo Sư · Phản biện: chờ Thiên Cơ/Hộ Pháp.

## 9. Drift log

| Ngày | File/bề mặt phát sinh | Vì sao | Đã nâng luồng? | Ai duyệt |
|---|---|---|---|---|
| | | | | |
| 2026-11-17 | `core/network/interceptor/LoggingInterceptor.kt`, `AndroidManifest.xml`, `res/xml/{backup_rules,data_extraction_rules}.xml`, `core/reminder/*`, `ui/navigation/MemorialDeepLink.kt` | G1 của Hộ Pháp: §6 khai sai `pii-logging`/`backup`, Android bị BLOCK; vá A1–A6 | Có — giữ `C0/B2`, luồng `L` | Chờ Hộ Pháp ký lại G1 |

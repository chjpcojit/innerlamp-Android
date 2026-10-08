# Requirements — Storage

Version: 1.1
Last updated: 2026-09-22
Status: not started — spec prospective; `core/datastore/UserPreferences.kt` sẽ được chuyển sang `SharedPreferences`, toàn bộ `core/storage/` chưa tồn tại
Feature folder: `app/src/main/java/com/example/innerlamp/core/storage/`

> See `changelog.md` in this folder for the version history of this spec.

Spec này viết **prospective** (chưa có code cho lớp `core/storage/`). Đây không phải màn hình mà là
**lớp lưu trữ dùng chung**, cung cấp một mặt tiền (`Storage`) gồm hai tầng:

- **Tầng không nhạy cảm** — key-value nhẹ (onboarding, pattern mặc định, cờ cài đặt), lưu bằng
  **`SharedPreferences`** (không dùng DataStore).
- **Tầng nhạy cảm** — secret/credential, mã hoá bằng khoá trong **AndroidKeyStore**, chia 2 cấp:
  **`DEVICE_UNLOCKED`** (mở điện thoại là đọc được, không prompt) và **`USER_AUTH`** (mỗi lần đọc đều
  phải xác thực lại bằng sinh trắc học **hoặc** PIN/pattern của điện thoại).

`Storage` là nơi duy nhất các feature gọi vào; chi tiết cài đặt bên dưới (file prefs nào, alias khoá
nào) là việc nội bộ của `core/storage/`, không lộ ra API gọi.

## User Story

Là lập trình viên InnerLamp, tôi muốn một lớp lưu trữ dùng chung với hai tầng (thường và bảo mật,
bảo mật chia 2 cấp xác thực) để mọi feature lưu/đọc dữ liệu nhất quán, không tự dựng KeyStore, không
tự sinh IV, không tự xử lý prompt sinh trắc học ở từng nơi.

## Acceptance Criteria (EARS notation)

### Tầng không nhạy cảm (PreferencesStorage trên SharedPreferences)

- WHEN một feature ghi một giá trị key-value không nhạy cảm (String/Boolean/Int/Long/Float/Set),
  THE SYSTEM SHALL lưu vào `SharedPreferences` qua `PreferencesStorage` và SHALL đọc lại được giá trị
  đó sau khi process bị kill và mở lại.
- WHEN nhiều nơi cùng đọc một key, THE SYSTEM SHALL trả `Flow` phát giá trị hiện tại và cập nhật mỗi
  khi key đổi (dùng `OnSharedPreferenceChangeListener`).
- WHEN một key được xoá, THE SYSTEM SHALL khiến `Flow` tương ứng phát giá trị mặc định (không crash,
  không giữ giá trị cũ).
- WHEN `PreferencesStorage` ghi, THE SYSTEM SHALL chạy `commit()` trên `DispatcherProvider.io` được
  inject (không dùng `apply()` rồi trả về ngay), và SHALL **không** gọi trực tiếp `Dispatchers.IO`.
- WHEN `PreferencesStorage` được test trên JVM, THE SYSTEM SHALL cho phép thay cài đặt
  `SharedPreferences` bằng một fake, vì interface tách khỏi cài đặt; cài đặt `SharedPreferences` thật
  SHALL được kiểm ở instrumented test.

### Phân cấp secret (DEVICE_UNLOCKED vs USER_AUTH)

- WHEN một secret được ghi ở **`DEVICE_UNLOCKED`**, THE SYSTEM SHALL mã hoá bằng khoá AES-256-GCM
  trong AndroidKeyStore **không** yêu cầu xác thực người dùng, và SHALL đọc lại được khi app đang chạy
  trên máy đã mở khoá mà **không** hiện prompt nào.
- WHEN một secret được ghi ở **`USER_AUTH`**, THE SYSTEM SHALL mã hoá bằng khoá AES-256-GCM trong
  AndroidKeyStore **có** yêu cầu xác thực người dùng, với yếu tố được chấp nhận là
  `AUTH_BIOMETRIC_STRONG` **hoặc** `AUTH_DEVICE_CREDENTIAL` (PIN/pattern của điện thoại).
- WHEN người gọi không nêu level, THE SYSTEM SHALL **không** tự đoán: API SHALL bắt buộc truyền level,
  để không có secret nào vô tình rơi vào `DEVICE_UNLOCKED`.

### Đọc USER_AUTH (xác thực lại mỗi lần)

- WHEN đọc một secret `USER_AUTH`, THE SYSTEM SHALL hiện `BiometricPrompt` **trước** khi giải mã, và
  SHALL **không** giải mã nếu người dùng chưa xác thực thành công.
- WHEN người dùng xác thực thành công ở `USER_AUTH`, THE SYSTEM SHALL giải mã và trả giá trị, rồi SHALL
  yêu cầu xác thực lại ở lần đọc kế tiếp.
- WHEN sinh trắc học không khả dụng (chưa đăng ký / lỗi phần cứng / bị lockout) nhưng thiết bị có
  khoá màn hình, THE SYSTEM SHALL cho phép xác thực bằng PIN/pattern (fallback).
- WHEN người dùng huỷ hoặc thất bại khi xác thực, THE SYSTEM SHALL trả
  `StorageError.UserCancelled` (huỷ) / `StorageError.AuthenticationFailed` (thất bại) và SHALL
  **không** giải mã, không rò rỉ plaintext.
- WHEN `SecureStorage` được gọi đọc `USER_AUTH` mà không có Activity/UI để hiện prompt (ví dụ từ
  service chạy nền), THE SYSTEM SHALL trả `StorageError.AuthenticationUnavailable` và SHALL
  **không** giải mã.

### Thiết lập khoá bảo vệ: lúc lưu và sau khi lưu

- WHEN một secret được ghi ở `USER_AUTH` mà thiết bị **chưa** có khoá màn hình (không PIN/pattern),
  THE SYSTEM SHALL từ chối ghi với `StorageError.DeviceLockRequired`, SHALL **không** lưu giá trị ở
  bất kỳ dạng nào, và tầng UI SHALL hướng người dùng sang màn hình thiết lập khoá màn hình.
- WHEN secret `USER_AUTH` **đã lưu** rồi mà người dùng gỡ PIN hoặc gỡ sinh trắc học, THE SYSTEM SHALL
  phát hiện (khi app khởi động hoặc ở lần đọc/ghi `USER_AUTH` kế tiếp) và SHALL **báo lỗi**
  `StorageError.KeyInvalidated`, SHALL **xoá** toàn bộ blob `USER_AUTH` và **xoá alias khoá
  `USER_AUTH`** trong AndroidKeyStore — SHALL **không** cố giải mã, không giữ lại dữ liệu không còn
  bảo vệ được.
- WHEN việc phát hiện dựa trên `KeyguardManager.isDeviceSecure()` (trả `false`) hoặc trên
  `KeyPermanentlyInvalidatedException` từ KeyStore, THE SYSTEM SHALL xử lý **giống hệt nhau** ở cả hai
  đường (cùng một hàm dọn dẹp fail-closed).

### Giới hạn nền tảng (ghi rõ để không hiểu sai yêu cầu)

- WHEN thiết bị chỉ có sinh trắc học **Class 2** (ví dụ face unlock yếu) và không có Class 3,
  THE SYSTEM SHALL coi sinh trắc học đó là **không** hợp lệ cho `USER_AUTH` (AndroidKeyStore không có
  `AUTH_BIOMETRIC_WEAK`), SHALL dùng PIN/pattern làm yếu tố thay thế, và SHALL **không** hạ cấp
  `USER_AUTH` thành "chỉ prompt" mà vẫn giải mã.

### Bền vững dữ liệu & fail-closed

- WHEN ghi một secret (bất kỳ level nào), THE SYSTEM SHALL mã hoá với **IV mới** cho mỗi lần ghi
  (AES-GCM), SHALL **không** tái sử dụng IV.
- WHEN ghi đè một key đã tồn tại, THE SYSTEM SHALL thay thế bản ghi cũ (không giữ lại bản cũ).
- WHEN xoá một secret, THE SYSTEM SHALL xoá cả blob (không chỉ ẩn khỏi API).
- WHEN đọc một secret không tồn tại, THE SYSTEM SHALL trả `StorageError.NotFound` — SHALL **không**
  trả chuỗi rỗng như thể thành công.
- WHEN khoá `DEVICE_UNLOCKED`/`USER_AUTH` được tạo, THE SYSTEM SHALL dùng **StrongBox** khi thiết bị
  hỗ trợ và SHALL fallback về TEE khi không hỗ trợ, không crash.
- WHEN app được backup cloud hoặc chuyển máy, THE SYSTEM SHALL **loại trừ** file prefs bảo mật khỏi
  `data_extraction_rules.xml` và `backup_rules.xml`, vì khoá KeyStore không di chuyển được và blob
  khôi phục sẽ không giải mã được.

### Kiến trúc & an toàn tầng

- WHEN một tầng trên dùng Storage, THE SYSTEM SHALL chỉ thấy API của `core/storage/`; tầng `domain`
  SHALL **không** import Android KeyStore, `SharedPreferences` hay bất kỳ lớp storage nào.
- WHEN lớp storage ném lỗi, THE SYSTEM SHALL dùng `StorageError` trung tính (không lộ
  `java.security.*` / KeyStore), và SHALL trả qua `Result` để tầng `data` map sang domain.
- WHEN `SecureStorage` chạy crypto I/O, THE SYSTEM SHALL chạy trên `DispatcherProvider.io` được
  inject, SHALL **không** gọi trực tiếp `Dispatchers.IO`.
- WHEN nội dung secret được ghi log hoặc hiển thị lỗi, THE SYSTEM SHALL **không** ghi giá trị
  plaintext/ciphertext vào log.

## Out of scope (ranh giới feature này, không phải việc của feature khác)

- **Dữ liệu có cấu trúc (session history…)**: vẫn qua Room ở `data/local/`, không đi qua `Storage`.
- **Token contract / endpoint / refresh-token**: thuộc spec `Networking`. Storage chỉ cung cấp chỗ
  lưu; việc token nào ở level nào do feature auth quyết định.
- **App-lock mềm bằng `BIOMETRIC_WEAK`** (mở app bằng face Class 2 không bind khoá): đã cân nhắc và
  **hoãn** — thuộc feature `AppLock` tương lai, không nằm trong Storage.
- **Đồng bộ secret đa thiết bị / cloud**: không hỗ trợ; secret chỉ trên máy.
- **Lưu secret bằng `SharedPreferences` không mã hoá**: mọi secret phải đi qua `SecureStorage`
  (SharedPreferences chỉ dùng cho dữ liệu không nhạy cảm và cho ciphertext của tầng bảo mật).
- **Xoá dữ liệu mồ côi hậu backup/restore tự động**: Storage chỉ loại trừ khỏi backup; việc dọn dữ
  liệu mồ côi sau restore là task vận hành riêng.
- **StrongBox bắt buộc**: chỉ là best-effort; máy không hỗ trợ vẫn chạy TEE.

## Pending (not yet implemented)

- Toàn bộ `core/storage/` (facade, `PreferencesStorage`, `SecureStorage`, blob store, keystore,
  biometric gate) — theo dõi ở `tasks.md` Phase 1–4.
- Chuyển `core/datastore/UserPreferences.kt` sang `SharedPreferences` qua `PreferencesStorage`, và
  gỡ dependency `androidx.datastore` khỏi build — `tasks.md` Phase 1.
- Thêm dependency `androidx.biometric:biometric`; đổi `MainActivity` sang `FragmentActivity` để
  `BiometricPrompt` chạy được — `tasks.md` Phase 3.
- Cập nhật `data_extraction_rules.xml` + `backup_rules.xml` loại trừ file prefs bảo mật;
  thêm `<uses-permission android:name="android.permission.USE_BIOMETRIC"/>` — `tasks.md` Phase 5.
- Unit test JVM + instrumented test (chỉ chạy trên thiết bị) — `tasks.md` Phase 6.

## Open questions (resolved during implementation)

- ~~Tầng không nhạy cảm dùng SharedPreferences hay DataStore?~~ → **SharedPreferences** (quyết định
  cuối, đảo so với v1.0). Đây là dự án Android, key-value đơn giản dùng `SharedPreferences`; **bỏ
  DataStore** khỏi tầng lưu trữ, kể cả chuyển `UserPreferences.kt` sang `SharedPreferences` và gỡ
  dependency `androidx.datastore`. Resolved 2026-09-22 (v1.1).
- ~~Mô hình `USER_AUTH`: có tách thêm chế độ tương thích cho face Class 2 không?~~ → **không**. Chốt
  **một mô hình hardware-bound duy nhất** (bio Class 3 hoặc PIN/pattern) để giảm độ phức tạp; face
  Class 2 không mở được `USER_AUTH` là **giới hạn nền tảng** (AndroidKeyStore không có
  `AUTH_BIOMETRIC_WEAK`; crypto-based auth không hỗ trợ Class 2), xem `design.md` mục "Giới hạn
  nền tảng & quyết định". Chế độ app-lock mềm đưa vào Deferred. Resolved 2026-09-22.
- ~~Khi thiết bị không có khoá màn hình thì xử lý thế nào (lúc lưu)?~~ → **từ chối lưu**
  `USER_AUTH` (`StorageError.DeviceLockRequired`) và hướng dẫn người dùng thiết lập, không fallback
  lưu ở dạng không bảo vệ. Resolved 2026-09-22.
- ~~Khi gỡ PIN/bio **sau khi đã lưu** thì xử lý thế nào?~~ → **fail-closed**: phát hiện ở lần
  khởi động/đọc/ghi kế tiếp, trả `StorageError.KeyInvalidated`, xoá toàn bộ blob `USER_AUTH` và alias
  khoá `USER_AUTH` trong AndroidKeyStore. Resolved 2026-09-22.
- ~~Có thành phần chạy nền nào cần đọc `USER_AUTH` không?~~ → **không**. `USER_AUTH` chỉ đọc khi có
  UI để prompt; gọi từ nền trả `StorageError.AuthenticationUnavailable`. Resolved 2026-09-22.
- ~~Bản mã (ciphertext) của `USER_AUTH` lưu ở đâu?~~ → **`SharedPreferences` file riêng, chỉ chứa
  ciphertext** (không dùng `EncryptedSharedPreferences` vì thư viện đã deprecated), khoá thật nằm
  trong AndroidKeyStore. Resolved 2026-09-22.

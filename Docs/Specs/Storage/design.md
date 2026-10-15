# Design — Storage

Version: 1.2
Last updated: 2026-10-15
Status: not started — spec prospective; `UserPreferences.kt` sẽ chuyển sang SharedPreferences

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Lớp lưu trữ dùng chung, đặt ở `core/storage/`, thuộc tầng infrastructure của Clean Architecture hiện
tại: `ui → domain ← data → core/{storage,network}`. Nó cung cấp **cơ chế** lưu trữ, không chứa
nghiệp vụ và không biết feature nào đang lưu gì.

Thiết kế theo nguyên tắc "một mặt tiền, hai tầng":

```
Storage (facade)
├─ PreferencesStorage   → key-value không nhạy cảm   → nơi lưu: SharedPreferences (file thường)
└─ SecureStorage        → secret, mã hoá AES-256-GCM → khoá: AndroidKeyStore
                          ├─ DEVICE_UNLOCKED: đọc khi app chạy, không prompt
                          └─ USER_AUTH:       prompt mỗi lần đọc (bio Class 3 HOẶC PIN/pattern)
```

Ràng buộc kiến trúc giữ nguyên như repo:

- `domain` là Kotlin thuần; **không** import Android, KeyStore, SharedPreferences hay `core/storage`.
- Tầng trên (data/ui) chỉ gọi `Storage`; tầng `data` map `Result` sang domain.
- Không gọi `Dispatchers.IO` trực tiếp; nhận `DispatcherProvider` qua inject.

## Layers & components

| Layer | File | Responsibility |
|---|---|---|
| core/storage | `core/storage/Storage.kt` | **Facade**: `interface Storage { val prefs: PreferencesStorage; val secure: SecureStorage }`. Mặt tiền duy nhất tầng trên dùng. |
| core/storage/prefs | `core/storage/prefs/PreferencesStorage.kt` | Interface key-value không nhạy cảm: `put/get` cho String/Boolean/Int/Long/Float/Set, `observe(key): Flow`, `remove(key)`, `clear()`. Bất đồng bộ (`suspend`/`Flow`). |
| core/storage/prefs | `core/storage/prefs/SharedPreferencesStorage.kt` | Cài đặt trên `SharedPreferences` (file `innerlamp_prefs`): `get` đọc trực tiếp, `put` dùng `commit()` trên `DispatcherProvider.io`, `observe` dùng `callbackFlow` + `OnSharedPreferenceChangeListener` (giữ **tham chiếu mạnh** tới listener trong lúc flow còn sống). |
| core/datastore | `core/datastore/UserPreferences.kt` | **File đã tồn tại** — chuyển từ DataStore sang dùng `PreferencesStorage` (`SharedPreferences`); giữ nguyên API công khai (`onboardingCompleted`, `defaultPatternId`, `setOnboardingCompleted`, `setDefaultPatternId`) để không phá chỗ gọi hiện có. |
| core/storage/secure | `core/storage/secure/SecureLevel.kt` | `enum class SecureLevel { DEVICE_UNLOCKED, USER_AUTH }`. |
| core/storage/secure | `core/storage/secure/SecureStorage.kt` | Interface: `suspend fun put(level, key, value)`, `suspend fun get(level, key): Result<String>`, `suspend fun delete(key)`, `suspend fun clear(level)`, `suspend fun isAvailable(level): Boolean`. Bắt buộc truyền `level` (không có default). |
| core/storage/secure | `core/storage/secure/StorageError.kt` | `sealed class StorageError`: `DeviceLockRequired`, `AuthenticationUnavailable`, `UserCancelled`, `AuthenticationFailed`, `KeyInvalidated`, `NotFound`, `CryptoFailure(cause?)`, `StorageFailure(cause?)`. Trung tính, không lộ `java.security.*`. |
| core/storage/secure | `core/storage/secure/blob/SecureBlobStore.kt` | Interface lưu ciphertext: `read(key): Blob?`, `write(key, Blob)`, `delete(key)`, `clearAll()`. |
| core/storage/secure | `core/storage/secure/blob/SharedPrefsSecureBlobStore.kt` | Cài đặt trên **file `SharedPreferences` riêng** (`innerlamp_secure`), `MODE_PRIVATE`, chỉ chứa `key → base64(iv ‖ ciphertext)`. Không dùng `EncryptedSharedPreferences` (đã deprecated). |
| core/storage/secure | `core/storage/secure/crypto/SecretKeyProvider.kt` | Tạo/lấy/xoá khoá AES-256-GCM trong `AndroidKeyStore` cho từng level (2 alias riêng); StrongBox best-effort → fallback TEE; phát hiện key bị vô hiệu. |
| core/storage/secure | `core/storage/secure/crypto/AesGcmCodec.kt` | Mã hoá/giải mã `AES/GCM/NoPadding`, IV 12 byte **mới mỗi lần ghi**, tag 128-bit; ghép `iv ‖ ciphertext` rồi base64. |
| core/storage/secure | `core/storage/secure/auth/BiometricAuthenticator.kt` | **Port**: `suspend fun authenticate(reason: String): AuthResult` (`Success` / `Cancelled` / `Unavailable` / `Failed`). Giữ `core/storage` không phụ thuộc UI. |
| core/storage/secure | `core/storage/secure/auth/AndroidBiometricAuthenticator.kt` | Cài đặt dùng `androidx.biometric.BiometricPrompt`, cần `FragmentActivity`. Xử lý khác biệt API 29 vs 30+. |
| core/storage/secure | `core/storage/secure/DefaultSecureStorage.kt` | Ghép các bước: kiểm tra khoá bảo vệ (`KeyguardManager.isDeviceSecure()`) → lấy khoá → (prompt nếu `USER_AUTH`) → crypto → blob store. Chứa `reconcileUserAuth()` dọn dẹp fail-closed khi khoá bị vô hiệu; map exception KeyStore → `StorageError`. |
| di | `di/StorageModule.kt` | Wiring thủ công (không thêm thư viện DI): `SharedPreferences`, `PreferencesStorage`, `SecureBlobStore`, `SecretKeyProvider`, `SecureStorage`, `Storage`. |
| build / config | `gradle/libs.versions.toml` + `app/build.gradle.kts` | Thêm `androidx.biometric:biometric`; **gỡ** `androidx.datastore` (không còn dùng). **Sửa file**. |
| app / host | `MainActivity.kt` | Đổi `ComponentActivity` → `FragmentActivity` (hoặc `AppCompatActivity`) để `BiometricPrompt` hoạt động. **Sửa file**. |
| security / backup | `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` | Loại trừ file `sharedpref` `innerlamp_secure.xml` khỏi cloud backup & device transfer. **Sửa file**. |
| security / manifest | `AndroidManifest.xml` | Thêm `<uses-permission android:name="android.permission.USE_BIOMETRIC"/>`. **Sửa file**. |

> **Ghi chú tương thích:** AGENTS.md hiện ghi "key-value nhẹ đi qua `core/datastore/UserPreferences.kt`".
> Sau spec này, quy ước đổi thành **SharedPreferences qua `core/storage/`** — cần cập nhật AGENTS.md
> (theo dõi ở `tasks.md` Phase 7).

## Navigation flow

Không áp dụng — đây là lớp hạ tầng, không có màn hình, không có nav graph.

## Step-by-step Flow — Ghi & đọc secret

Rule 5 của skill bắt buộc Step-by-step Flow khi luồng có ≥2 bước tuần tự *kiểu gọi mạng*. Ở đây
không có gọi mạng, nhưng luồng `USER_AUTH` có **nhiều bước tuần tự ẩn** (kiểm tra khoá bảo vệ → lấy
khoá → prompt → giải mã → xử lý vô hiệu), nên spec ghi rõ từng bước để tránh bị hiểu thành "chỉ hiện
prompt rồi giải mã".

```
Step 1 — Ghi secret USER_AUTH (this feature)
- Trigger: tầng trên gọi secure.put(USER_AUTH, key, value)
- Pre-condition: KeyguardManager.isDeviceSecure() == true
- Check: nếu thiết bị KHÔNG có khoá màn hình → StorageError.DeviceLockRequired (không ghi gì)
- Key: ensure khoá alias USER_AUTH tồn tại (StrongBox nếu hỗ trợ → fallback TEE)
- Crypto: AES-256-GCM encrypt với IV mới → blob = base64(iv ‖ ct)
- Store: SharedPrefsSecureBlobStore.write(key, blob)
    - Success → Result.success(Unit)
    - Failure (KeyStoreException/GeneralSecurityException) → StorageError.CryptoFailure, không ghi blob nửa vời

Step 2 — Đọc secret USER_AUTH (this feature)
- Trigger: tầng trên gọi secure.get(USER_AUTH, key)
- Pre-condition: blob tồn tại; có Activity để hiện prompt
- Check: blob không tồn tại → StorageError.NotFound
- Check: thiết bị không còn khoá màn hình (PIN/bio đã gỡ) → reconcileUserAuth() → StorageError.KeyInvalidated
- Check: không có UI (gọi từ service/nền) → StorageError.AuthenticationUnavailable
- Auth: BiometricAuthenticator.authenticate("Mở dữ liệu bảo mật")
    - Success (sinh trắc học Class 3 hoặc PIN/pattern) → Step 3
    - Cancelled → StorageError.UserCancelled (không giải mã)
    - Unavailable/Failed → StorageError.AuthenticationFailed (không giải mã)
- Crypto: AES-256-GCM decrypt
    - Success → Result.success(value)
    - Failure (KeyPermanentlyInvalidatedException) → reconcileUserAuth() → StorageError.KeyInvalidated
    - Failure (khác) → StorageError.CryptoFailure

Step 3 — Ghi/đọc DEVICE_UNLOCKED (this feature)
- Trigger: secure.put/get(DEVICE_UNLOCKED, key, …)
- Pre-condition: app đang chạy (thiết bị đã mở khoá)
- Key: ensure khoá alias DEVICE_UNLOCKED (không yêu cầu xác thực)
- Crypto/Store: như Step 1 nhưng KHÔNG prompt
    - Success → Result.success(...)
    - Failure → StorageError.CryptoFailure / NotFound
```

## Dọn dẹp fail-closed khi khoá bảo vệ bị gỡ (`reconcileUserAuth`)

Người dùng có thể gỡ PIN hoặc gỡ sinh trắc học **sau khi đã lưu** dữ liệu `USER_AUTH`. Cùng một hàm
xử lý cho cả hai đường phát hiện:

```
reconcileUserAuth()
├─ điều kiện kích hoạt:
│   ├─ KeyguardManager.isDeviceSecure() == false            (PIN/pattern bị gỡ)
│   └─ KeyStore ném KeyPermanentlyInvalidatedException      (sinh trắc học đổi/gỡ)
├─ hành động:
│   ├─ SharedPrefsSecureBlobStore.clearAll()                (xoá mọi blob USER_AUTH)
│   └─ SecretKeyProvider.deleteAlias(USER_AUTH)             (xoá alias khoá trong AndroidKeyStore)
└─ kết quả: StorageError.KeyInvalidated → UI báo lỗi + yêu cầu thiết lập/đăng nhập lại
```

- Gọi ở **lần khởi động app** và ở **mỗi lần đọc/ghi `USER_AUTH`** (rẻ: một lần đọc cờ hệ thống + một
  lần `KeyStore.getEntry`), nên dữ liệu không còn bảo vệ được sẽ bị xoá sớm.
- Blob `DEVICE_UNLOCKED` **không** bị ảnh hưởng bởi thao tác này.

## Giới hạn nền tảng & quyết định (đọc kỹ trước khi implement)

Đây là các ràng buộc **không thể lách** của Android, đã đối chiếu tài liệu chính thức
(developer.android.com/identity/sign-in/biometric-auth):

1. **AndroidKeyStore chỉ hỗ trợ `AUTH_BIOMETRIC_STRONG` (Class 3) và `AUTH_DEVICE_CREDENTIAL`.**
   Không có `AUTH_BIOMETRIC_WEAK`. ⇒ Sinh trắc học **Class 2 (face yếu)** không thể mở khoá KeyStore.
2. **Crypto-based authentication (`CryptoObject`) chỉ hỗ trợ Class 3**, không hỗ trợ Class 2
   (`IllegalArgumentException: Crypto-based authentication is not supported for Class 2 …`).
3. **Không thể vừa "chỉ sinh trắc học", vừa "có fallback PIN"** trong cùng một khoá per-use: đường có
   fallback bắt buộc dùng khoá **time-based** (`setUserAuthenticationParameters` duration > 0) và
   **không** truyền `CryptoObject`.
4. API 29 khác API 30+: `AUTH_*` constants và `setUserAuthenticationParameters` có từ API 30; còn
   `setUserAuthenticationValidityDurationSeconds` cho API ≤ 29. `BiometricPrompt`:
   `DEVICE_CREDENTIAL` trong `setAllowedAuthenticators` không hỗ trợ ≤ API 29 (phải dùng
   `setDeviceCredentialAllowed(true)` đã deprecated); **không** được gọi `setNegativeButtonText()`
   cùng lúc với `DEVICE_CREDENTIAL`.

**Quyết định (đơn giản hoá theo yêu cầu người dùng):** `USER_AUTH` dùng **một** mô hình hardware-bound
duy nhất —

```kotlin
// USER_AUTH (API 30+)
KeyGenParameterSpec.Builder(ALIAS_USER_AUTH,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    .setKeySize(256)
    .setUserAuthenticationRequired(true)
    .setUserAuthenticationParameters(1 /* giây: cửa sổ tối thiểu ⇒ gần như per-read */,
        KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
    .setInvalidatedByBiometricEnrollment(true)
    .setIsStrongBoxBacked(true)   // best-effort, fallback TEE nếu ném StrongBoxUnavailableException
    .build()

// API 29: dùng .setUserAuthenticationValidityDurationSeconds(1)
//   (mặc định cho phép sinh trắc học hoặc PIN/pattern)
```

- **Prompt mỗi lần đọc**: `DefaultSecureStorage` **luôn** gọi `BiometricAuthenticator.authenticate`
  trước khi decrypt, không dựa vào việc khoá còn "trong hạn" hay không; cửa sổ 1 giây chỉ để chặn
  tái sử dụng ngoài ý muốn.
- **Fallback PIN/pattern** nằm ở `BiometricPrompt` (`BIOMETRIC_STRONG or DEVICE_CREDENTIAL`), vì vậy
  **không** dùng `CryptoObject` — một đường code duy nhất cho cả bio lẫn PIN.
- **Face Class 2**: máy chỉ có face Class 2 ⇒ người dùng phải dùng **PIN/vân tay** cho `USER_AUTH`.
  Ghi rõ trong tài liệu feature; **không** hạ cấp thành gate mềm (app-lock mềm ở Out of scope).

## Bảo mật & khôi phục (edge cases)

| Tình huống | Hành vi |
|---|---|
| Máy không có khoá màn hình, gọi `put(USER_AUTH)` | `DeviceLockRequired`; UI hướng dẫn thiết lập. Không lưu gì. |
| Gỡ PIN sau khi đã lưu | `reconcileUserAuth()` khi khởi động/lần đọc kế tiếp → xoá blob + alias khoá, `KeyInvalidated`. |
| Gỡ/thêm sinh trắc học sau khi đã lưu | KeyStore vô hiệu khoá → `reconcileUserAuth()` → xoá blob + alias khoá, `KeyInvalidated`. |
| Chỉ có face Class 2, có PIN | `USER_AUTH` mở bằng PIN (hoặc vân tay nếu có); face không dùng được cho `USER_AUTH`. |
| Người dùng huỷ prompt | `UserCancelled`, không giải mã, không ghi. |
| Sinh trắc học bị lockout / lỗi phần cứng | Prompt cho phép chuyển sang PIN/pattern (`DEVICE_CREDENTIAL`). |
| Không có UI (service nền) | `AuthenticationUnavailable`; **không** đọc `USER_AUTH` từ nền. |
| Backup/restore sang máy mới | Loại trừ file prefs bảo mật khỏi backup & device transfer (khoá KeyStore không di chuyển được). |
| App gỡ/cài lại | File prefs bị xoá theo app ⇒ secret mất; không cố phục hồi. |
| Đọc/ghi đồng thời cùng key | Ghi theo kiểu "last write wins"; crypto không giữ state dùng chung (mỗi lần tạo `Cipher` riêng). |

## Deliberately stubbed for now (if applicable)

- `UserPreferences.kt` hiện dùng **DataStore**; chưa chuyển sang `SharedPreferences` qua
  `PreferencesStorage` — theo dõi ở `tasks.md` Phase 1. Chưa có dữ liệu thật nào phụ thuộc
  `UserPreferences` (MainActivity vẫn là template) nên **không cần migrate** dữ liệu.
- `AndroidBiometricAuthenticator` **chưa** có code; `MainActivity` hiện là Compose `ComponentActivity`
  và cần đổi sang `FragmentActivity` trước khi `BiometricPrompt` hoạt động — `tasks.md` Phase 3.
- Dependency `androidx.biometric:biometric` **chưa** có trong `gradle/libs.versions.toml` —
  `tasks.md` Phase 3. Dependency `androidx.datastore` còn đó nhưng sẽ bị gỡ — `tasks.md` Phase 1.
- Backup rules hiện là file mẫu chưa cấu hình — `tasks.md` Phase 5.

> Không còn `⚠️ Not confirmed` trong spec này: mọi câu hỏi đã được chốt ở
> `requirements.md` mục "Open questions (resolved)".

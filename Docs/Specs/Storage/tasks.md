# Tasks — Storage

Version: 1.2
Last updated: 2026-10-15
Status: not started — spec prospective, mọi task còn `[ ]` (trừ ghi chú về file đã có)

> See `changelog.md` in this folder for the version history of this spec.

Mọi task dưới đây trace trực tiếp về bảng **Layers & components** và/hoặc **Step-by-step Flow** trong
`design.md`. Chưa có code trong `core/storage/` → tất cả giữ `[ ]`.

## Phase 1 — Tầng không nhạy cảm (PreferencesStorage trên SharedPreferences)

- [ ] Tạo `core/storage/prefs/PreferencesStorage.kt`: interface key-value không nhạy cảm
      (`put/get` cho String/Boolean/Int/Long/Float/Set, `observe(key): Flow`, `remove`, `clear`),
      bất đồng bộ (`suspend`/`Flow`).
- [ ] Tạo `core/storage/prefs/SharedPreferencesStorage.kt`: cài đặt trên `SharedPreferences`
      (file `innerlamp_prefs`); `put` dùng `commit()` trên `DispatcherProvider.io`;
      `observe` dùng `callbackFlow` + `OnSharedPreferenceChangeListener` và **giữ tham chiếu mạnh**
      tới listener trong lúc flow còn sống (SharedPreferences chỉ giữ tham chiếu yếu).
- [ ] Tạo `core/storage/Storage.kt`: facade `Storage { prefs; secure }`.
- [ ] Chuyển `core/datastore/UserPreferences.kt` sang dùng `PreferencesStorage`
      (`SharedPreferences`); giữ nguyên API công khai (`onboardingCompleted`, `defaultPatternId`,
      `setOnboardingCompleted`, `setDefaultPatternId`). Không cần migrate dữ liệu (chưa có dữ liệu
      thật phụ thuộc nó).
- [ ] Gỡ dependency `androidx.datastore` khỏi `gradle/libs.versions.toml` + `app/build.gradle.kts`
      sau khi `UserPreferences` đã chuyển xong và build xanh.

## Phase 2 — Hạ tầng bảo mật (KeyStore + AES-GCM + blob store)

- [ ] Tạo `core/storage/secure/SecureLevel.kt` (`DEVICE_UNLOCKED`, `USER_AUTH`).
- [ ] Tạo `core/storage/secure/StorageError.kt` (`DeviceLockRequired`, `AuthenticationUnavailable`,
      `UserCancelled`, `AuthenticationFailed`, `KeyInvalidated`, `NotFound`, `CryptoFailure`,
      `StorageFailure`).
- [ ] Tạo `core/storage/secure/blob/SecureBlobStore.kt` (interface) và
      `SharedPrefsSecureBlobStore.kt` (file `innerlamp_secure`, `MODE_PRIVATE`, chỉ chứa
      `base64(iv ‖ ciphertext)`; **không** dùng `EncryptedSharedPreferences` vì đã deprecated).
- [ ] Tạo `core/storage/secure/crypto/AesGcmCodec.kt`: `AES/GCM/NoPadding`, IV 12 byte **mới mỗi lần
      ghi**, tag 128-bit; ghép `iv ‖ ct` + base64; giải mã tách IV đúng cách.
- [ ] Tạo `core/storage/secure/crypto/SecretKeyProvider.kt`:
      - `DEVICE_UNLOCKED` alias: AES-256-GCM, **không** `setUserAuthenticationRequired`.
      - `USER_AUTH` alias: AES-256-GCM, `setUserAuthenticationRequired(true)`,
        `setUserAuthenticationParameters(1, AUTH_BIOMETRIC_STRONG or AUTH_DEVICE_CREDENTIAL)` (API 30+)
        / `setUserAuthenticationValidityDurationSeconds(1)` (API 29),
        `setInvalidatedByBiometricEnrollment(true)`.
      - `setIsStrongBoxBacked(true)` best-effort → fallback TEE khi ném `StrongBoxUnavailableException`.
      - Có `deleteAlias(level)` để `reconcileUserAuth()` xoá khoá `USER_AUTH`.
      - Phát hiện key vô hiệu (`KeyPermanentlyInvalidatedException` → `StorageError.KeyInvalidated`).

## Phase 3 — USER_AUTH & biometric gate

- [ ] Thêm `androidx.biometric:biometric` vào `gradle/libs.versions.toml` (`[versions]` + `[libraries]`)
      rồi thêm `implementation(...)` trong `app/build.gradle.kts`.
- [ ] Đổi `MainActivity` sang `FragmentActivity` (hoặc `AppCompatActivity`) để `BiometricPrompt`
      chạy được; giữ nguyên nội dung Compose.
- [ ] Thêm `<uses-permission android:name="android.permission.USE_BIOMETRIC"/>` vào
      `AndroidManifest.xml`.
- [ ] Tạo `core/storage/secure/auth/BiometricAuthenticator.kt` (port `AuthResult`: `Success`,
      `Cancelled`, `Unavailable`, `Failed`).
- [ ] Tạo `AndroidBiometricAuthenticator.kt`: `BiometricPrompt` với
      `setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)` (API 30+) /
      `setDeviceCredentialAllowed(true)` (API 29); **không** dùng `CryptoObject`; **không** gọi
      `setNegativeButtonText()` cùng `DEVICE_CREDENTIAL`; không có Activity → `Unavailable`.
- [ ] Tạo `core/storage/secure/DefaultSecureStorage.kt`: ghép Step 1–3 của `design.md`
      (kiểm `isDeviceSecure` → lấy khoá → prompt khi `USER_AUTH` → crypto → blob store), luôn prompt
      trước khi decrypt `USER_AUTH`, map exception → `StorageError`, chạy `DispatcherProvider.io`.

## Phase 4 — Dọn dẹp fail-closed (gỡ PIN/bio sau khi đã lưu)

- [ ] Viết `reconcileUserAuth()` trong `DefaultSecureStorage`: kích hoạt khi
      `KeyguardManager.isDeviceSecure() == false` hoặc bắt `KeyPermanentlyInvalidatedException`;
      xoá toàn bộ blob `USER_AUTH` + xoá alias khoá `USER_AUTH` trong AndroidKeyStore; trả
      `StorageError.KeyInvalidated`.
- [ ] Gọi `reconcileUserAuth()` ở lúc **khởi động app** và ở **mỗi lần đọc/ghi `USER_AUTH`**;
      xác nhận blob `DEVICE_UNLOCKED` **không** bị ảnh hưởng.
- [ ] Chốt chữ ký `SecureStorage`: `put(level, …)`/`get(level, …)` **bắt buộc** truyền `level`
      (không default) để secret không vô tình rơi vào `DEVICE_UNLOCKED`.
- [ ] Đảm bảo không log plaintext/ciphertext ở bất kỳ nhánh lỗi nào.
- [ ] Kiểm tra bằng mắt + build xanh: `domain` **không** import `core/storage`/Android KeyStore;
      `core/storage` không import Compose/UI (trừ `AndroidBiometricAuthenticator` — tách biệt rõ).

## Phase 5 — DI, backup & manifest

- [ ] Tạo `di/StorageModule.kt` (thủ công): `SharedPreferences`, `PreferencesStorage`,
      `SecureBlobStore`, `SecretKeyProvider`, `SecureStorage`, `Storage`; cấp
      `BiometricAuthenticator` theo Activity.
- [ ] Sửa `res/xml/data_extraction_rules.xml`: `<exclude domain="sharedpref" path="innerlamp_secure.xml"/>`
      cho cả `cloud-backup` và `device-transfer`.
- [ ] Sửa `res/xml/backup_rules.xml`: `<exclude domain="sharedpref" path="innerlamp_secure.xml"/>`.
- [ ] Xác nhận `allowBackup` vẫn `true` cho dữ liệu thường (`innerlamp_prefs`) nhưng file bảo mật bị
      loại trừ.

## Phase 6 — Test

- [ ] Unit test JVM `PreferencesStorage` với fake (interface tách khỏi `SharedPreferences`);
      `StandardTestDispatcher`.
- [ ] Unit test JVM `DefaultSecureStorage` với `SecureBlobStore` + `SecretKeyProvider` +
      `BiometricAuthenticator` giả: cover `NotFound`, `DeviceLockRequired`,
      `AuthenticationUnavailable`, `UserCancelled`, `AuthenticationFailed`, `KeyInvalidated`.
- [ ] Unit test `AesGcmCodec`: IV khác nhau mỗi lần ghi; giải mã round-trip; dữ liệu hỏng → lỗi.
- [ ] Instrumented test cài đặt `SharedPreferencesStorage` (đọc/ghi/observe/xoá) trên thiết bị/emulator.
- [ ] Instrumented test (thiết bị có khoá màn hình): ghi/đọc `DEVICE_UNLOCKED` không prompt;
      ghi/đọc `USER_AUTH` có prompt; máy không khoá màn hình → `DeviceLockRequired`.
- [ ] Test thủ công: gỡ PIN (hoặc thêm vân tay mới) sau khi đã lưu `USER_AUTH` → blob bị xoá, alias
      khoá bị xoá, trả `KeyInvalidated`.
- [ ] Test thủ công: đọc `USER_AUTH` từ tiến trình không có UI → `AuthenticationUnavailable`.

## Phase 7 — Documentation

- [ ] Tạo `core/storage/README.md`: sơ đồ 2 tầng/2 level, ví dụ `put/get`, bảng `StorageError`,
      hành vi `reconcileUserAuth()`, và **ghi rõ giới hạn face Class 2** cùng cách mở `USER_AUTH`
      (bio Class 3 hoặc PIN/pattern).
- [ ] Cập nhật `AGENTS.md` (+ `CLAUDE.md` nếu còn stale): `core/storage/` là nơi lưu trữ chuẩn;
      key-value nhẹ dùng `SharedPreferences` (không còn DataStore); secret không được lưu rời rạc ở
      feature.
- [ ] (Sau khi có code thật) ghi chú OKF cho lớp storage dưới `Docs/Knowledge/` — chỉ làm khi hành vi
      đã end-to-end.

## Deferred (feature khác sở hữu, không block Storage)

- **App-lock mềm bằng `BIOMETRIC_WEAK`** (mở app/khối dữ liệu bằng face Class 2, không bind khoá):
  feature `AppLock` tương lai — xem `requirements.md` "Out of scope".
- **`AuthTokenStorage`** (token phiên dùng lớp này ở level nào): đã chốt ở `Docs/Specs/Login/` v1.0 —
  `SecureLevel.DEVICE_UNLOCKED`, qua `AuthSessionStore`; Storage chỉ cung cấp API.
- **Đồng bộ secret đa thiết bị / cloud**: không thuộc phạm vi (secret chỉ trên máy).
- **Dọn dữ liệu mồ côi sau restore**: task vận hành riêng.

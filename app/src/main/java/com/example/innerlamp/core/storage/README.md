# core/storage — Lớp lưu trữ dùng chung

Mặt tiền lưu trữ duy nhất mà các feature gọi vào, đặt ở `core/storage/`. Đây là lớp
hạ tầng: cung cấp **cơ chế** lưu trữ, không chứa nghiệp vụ và không biết feature nào
đang lưu gì.

```
Storage (facade)
├─ PreferencesStorage   → key-value không nhạy cảm   → nơi lưu: SharedPreferences (file thường)
└─ SecureStorage        → secret, mã hoá AES-256-GCM → khoá: AndroidKeyStore
                          ├─ DEVICE_UNLOCKED: đọc khi app chạy, không prompt
                          └─ USER_AUTH:       prompt mỗi lần đọc (bio Class 3 HOẶC PIN/pattern)
```

Tầng trên (data/ui) chỉ gọi `Storage`; tầng `domain` là Kotlin thuần và **không** import
`core/storage`, Android KeyStore hay `SharedPreferences`.

## Cách dùng

```kotlin
val storage: Storage = StorageModule.provideStorage(prefs, secure)

// Tầng không nhạy cảm
storage.prefs.putString("default_pattern_id", "box-breathing")
val flow: Flow<String?> = storage.prefs.observeString("default_pattern_id")

// Tầng bảo mật
storage.secure.put(SecureLevel.USER_AUTH, "refresh_token", "…")
val token: Result<String> = storage.secure.get(SecureLevel.USER_AUTH, "refresh_token")
```

`put`/`get` bắt buộc truyền `SecureLevel` (không có giá trị mặc định) để không secret
nào vô tình rơi vào `DEVICE_UNLOCKED`.

## Bảng `StorageError`

| Lỗi | Khi nào |
|---|---|
| `DeviceLockRequired` | Ghi `USER_AUTH` khi thiết bị chưa có khoá màn hình. |
| `AuthenticationUnavailable` | Đọc `USER_AUTH` khi không có Activity/UI để hiện prompt. |
| `UserCancelled` | Người dùng huỷ prompt xác thực. |
| `AuthenticationFailed` | Xác thực thất bại (không đăng ký / lỗi phần cứng / lockout). |
| `KeyInvalidated` | Khoá `USER_AUTH` bị vô hiệu (gỡ PIN/đổi sinh trắc học sau khi lưu). |
| `NotFound` | Secret không tồn tại. |
| `CryptoFailure(cause?)` | Lỗi crypto (dữ liệu hỏng…). |
| `StorageFailure(cause?)` | Lỗi lưu trữ chung. |

## Dọn dẹp fail-closed (`reconcileUserAuth`)

Khi người dùng **gỡ PIN/pattern hoặc gỡ/thêm sinh trắc học sau khi đã lưu** secret
`USER_AUTH`, khoá KeyStore bị vô hiệu hoặc `KeyguardManager.isDeviceSecure()` trả `false`.
`DefaultSecureStorage.reconcileUserAuth()` sẽ:

1. Xoá toàn bộ blob `USER_AUTH` (blob `DEVICE_UNLOCKED` **không** bị ảnh hưởng).
2. Xoá alias khoá `USER_AUTH` trong AndroidKeyStore.
3. Trả `StorageError.KeyInvalidated` để UI báo lỗi và yêu cầu thiết lập/đăng nhập lại.

Được kích hoạt ở **mỗi lần đọc/ghi `USER_AUTH`** (và có thể gọi thêm ở lúc khởi động app
khi có host DI/Application — hiện chưa có feature nào dùng `SecureStorage`).

## Giới hạn nền tảng (face Class 2)

- AndroidKeyStore chỉ hỗ trợ `AUTH_BIOMETRIC_STRONG` (Class 3) và `AUTH_DEVICE_CREDENTIAL`;
  **không có** `AUTH_BIOMETRIC_WEAK`.
- Face unlock **Class 2 (yếu)** không thể mở khoá `USER_AUTH`. Máy chỉ có face Class 2 thì
  người dùng phải dùng **PIN/pattern** (hoặc vân tay nếu có) để mở `USER_AUTH`.
- `USER_AUTH` không hạ cấp thành "chỉ prompt mà vẫn giải mã"; không có app-lock mềm ở đây
  (thuộc feature `AppLock` tương lai).

## Cấu trúc file

```
core/storage/
├─ Storage.kt                         facade { prefs; secure }
├─ prefs/PreferencesStorage.kt        interface key-value (String/Boolean/Int/Long/Float/Set)
├─ prefs/SharedPreferencesStorage.kt  cài đặt trên `innerlamp_prefs` (commit trên IO)
└─ secure/
   ├─ SecureLevel.kt                  DEVICE_UNLOCKED / USER_AUTH
   ├─ SecureStorage.kt                interface put/get/delete/clear/isAvailable
   ├─ StorageError.kt                 sealed class lỗi (kế thừa Exception để trả qua Result)
   ├─ DefaultSecureStorage.kt         ghép khoá bảo vệ → khoá → prompt → crypto → blob
   ├─ blob/SecureBlobStore.kt         interface kho ciphertext
   ├─ blob/SharedPrefsSecureBlobStore.kt  cài đặt trên `innerlamp_secure` (base64(iv‖ct))
   ├─ crypto/AesGcmCodec.kt           AES/GCM/NoPadding, IV 12 byte mới mỗi lần ghi
   ├─ crypto/SecretKeyProvider.kt     port tạo/lấy/xoá khoá
   ├─ crypto/AndroidSecretKeyProvider.kt  cài đặt AndroidKeyStore (StrongBox → fallback TEE)
   └─ auth/
      ├─ BiometricAuthenticator.kt    port authenticate → AuthResult
      ├─ AndroidBiometricAuthenticator.kt  BiometricPrompt (không CryptoObject)
      ├─ ActivityProvider.kt          cung cấp Activity hiện tại
      └─ DeviceLockChecker.kt         kiểm tra khoá màn hình (tách để test JVM)
```

## Lưu ý

- Mọi I/O chạy trên `DispatcherProvider.io` (inject), **không** gọi trực tiếp `Dispatchers.IO`.
- Không log plaintext/ciphertext ở bất kỳ nhánh lỗi nào.
- File prefs bảo mật (`innerlamp_secure.xml`) bị loại trừ khỏi cloud backup & device transfer.

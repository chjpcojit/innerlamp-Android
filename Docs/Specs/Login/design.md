# Design — Login

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — UI Login có (theo số điện thoại, giả lập); `SecureStorage`, `AuthInterceptor` (gắn Bearer) và `InMemoryAuthTokenProvider` có; lưu phiên, refresh, đăng xuất, quên mật khẩu, khách chưa có

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Kiến trúc giữ nguyên `ui → domain ← data → core/{network,storage}`, DI thủ công trong `di/`, mạng qua
`NetworkClient.safeCall`, thread qua `DispatcherProvider`.

Ba mảnh dùng chung toàn app, không chỉ cho màn Login:

- **`AuthSessionStore`** (interface ở `domain/repository/`, impl ở `data/`): `save(session)`,
  `clear()`, `hasSession()`. `save` ghi cặp token vào `SecureStorage` (`DEVICE_UNLOCKED`) và nạp cache
  in-memory; `clear` xoá token, dữ liệu của tài khoản (key tài khoản trong `PreferencesStorage`, bảng
  Room của tài khoản — mỗi feature đăng ký cách xoá của mình qua `AccountDataCleaner`) và giữ thiết lập
  của máy. Đăng nhập, đăng ký (`../Onboarding/`), đăng xuất, phiên hết và đặt lại mật khẩu đều đi qua
  đây — một chỗ duy nhất ghi/xoá token.
- **`SessionTokenCache`** (`core/network/auth/`) implement `AuthTokenProvider` thay cho
  `InMemoryAuthTokenProvider`: giữ cặp token trong bộ nhớ để `AuthInterceptor` đọc đồng bộ (đúng ràng
  buộc "không I/O trong interceptor" của Networking); nạp từ `SecureStorage` lúc mở app (Splash).
- **Refresh bằng OkHttp `Authenticator`** (`core/network/auth/TokenRefreshAuthenticator.kt`) thay cho
  hook `TODO(refresh-token)` trong `AuthInterceptor`: chỉ chạy cho request có `@Authenticated` và có
  refresh token; single-flight bằng một lock — request đến sau thấy access token đã đổi thì dùng luôn,
  không refresh lần hai; gọi refresh bằng một Retrofit service riêng không có `AuthInterceptor`; lưu
  cặp mới vào `SecureStorage` (chặn trên thread OkHttp, `runBlocking(dispatchers.io)`) **trước** khi trả
  request gọi lại. Bị từ chối → phát `SessionEvent.Ended` trên `SessionEvents` (SharedFlow ở
  `core/network/auth/`).
- **`MainActivity`** lắng nghe `SessionEvents`: `AuthSessionStore.clear()` → navigate
  `LoginRoute(banner = SessionEnded)` pop toàn bộ graph.

**UI mới:** banner "Phiên đăng nhập đã hết" / "Đã đổi mật khẩu" trên Login chưa có trong prototype;
chốt giao diện với anh Đức trước khi làm (anh Đức, 2026-10-14: "sẽ cần thêm UI").

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-14.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Login | `ui/screens/login/LoginScreen.kt` | Ô email (bàn phím email, không viết hoa), mật khẩu + Hiện/Ẩn, "Quên mật khẩu?", banner, "Tạo tài khoản mới", "Vào xem trước"; bỏ ô số điện thoại, bộ chọn quốc gia, nút SMS | có — cần sửa |
| UI / Login | `ui/screens/login/LoginUiState.kt`, `LoginViewModel.kt` | `email`, `password`, lỗi field, `submitting`, `banner`, `lockedUntil`; `submit()` gọi `SignInUseCase`; xoá mật khẩu khi lỗi; bỏ `canRequestOtp` | có — cần sửa (đang giả lập) |
| UI / Navigation | `ui/navigation/Routes.kt`, `MainActivity.kt` | `LoginRoute` thành `data class LoginRoute(banner: LoginBanner? = null, email: String? = null)`; thêm `ForgotPasswordRoute(email)`, `ResetPasswordRoute(email)`; lắng nghe `SessionEvents` | có — cần sửa |
| UI / Forgot | mới `ui/screens/forgotpassword/ForgotPasswordScreen.kt` + ViewModel + UiState | Email, "Gửi mã", "Trở lại đăng nhập" | chưa có |
| UI / Reset | mới `ui/screens/forgotpassword/ResetPasswordScreen.kt` + ViewModel + UiState | "Nếu email này có tài khoản…", email, 6 ô số, mật khẩu mới + Hiện/Ẩn, `CodeTimerRow` (`../Onboarding/`), khoá, "Gửi lại mã" | chưa có |
| UI / Sign-out | `ui/screens/profile/ProfileScreen.kt`, `ProfileViewModel.kt` | Hàng "Đăng xuất" cuối màn; hộp xác nhận; `SignOutUseCase`; callback `onSignedOut` → Login gốc | có màn — chưa có đăng xuất |
| UI / Guest | mới `ui/components/auth/SignInPrompt.kt` + `AuthGate` (đọc `AuthSessionStore.hasSession()`) | Lời mời đăng nhập dùng chung; feature tài khoản bọc màn của mình bằng `AuthGate` | chưa có — mỗi feature nối trong spec của nó (Memorial đã liệt kê) |
| Domain / Model | mới `domain/model/AuthSession.kt` (`accessToken`, `refreshToken`, `user`), `AuthUser.kt`, `SignInError.kt`, `PasswordResetError.kt` | Dữ liệu thuần, không import Android | chưa có |
| Domain / Repository | mới `domain/repository/AuthRepository.kt` | `signIn(email, password): AuthSession`, `signOut()`, `requestResetCode(email): CodeTiming`, `confirmReset(email, code, newPassword)` | chưa có |
| Domain / Repository | mới `domain/repository/AuthSessionStore.kt`, `AccountDataCleaner.kt` | `save`, `clear`, `hasSession`; danh sách cleaner do các feature đăng ký trong DI | chưa có |
| Domain / Use case | mới `domain/usecase/auth/SignInUseCase.kt`, `SignOutUseCase.kt`, `RequestPasswordResetUseCase.kt`, `ConfirmPasswordResetUseCase.kt` | Validate (email/mật khẩu không trống; mật khẩu mới theo `RegistrationRules`) → repository → `AuthSessionStore` | chưa có |
| Data / API | mới `data/remote/api/AuthApiService.kt` (login, logout `@Authenticated`, password-reset/*), `RefreshApiService.kt` (refresh, client riêng) | Theo contract ở header | chưa có |
| Data / DTO | mới `data/remote/dto/AuthDto.kt` (+ mapper), `DeviceInfoDto` dựng từ `Build.MODEL`, `Build.VERSION.RELEASE`, `BuildConfig.VERSION_NAME`, `platform = "android"` | — | chưa có |
| Data / Repository | mới `data/repository/AuthRepositoryImpl.kt` | `safeCall`; map mã lỗi → `SignInError` / `PasswordResetError`; logout bỏ qua kết quả | chưa có |
| Data / Session | mới `data/repository/AuthSessionStoreImpl.kt` | `SecureStorage.put/get/delete` (`DEVICE_UNLOCKED`, key `auth.access_token`, `auth.refresh_token`) + `SessionTokenCache` + user ở `PreferencesStorage` (key `account.*`) + gọi các `AccountDataCleaner` | chưa có |
| core/storage | `core/storage/secure/SecureStorage.kt`, `DefaultSecureStorage.kt` | AES-256-GCM, khoá AndroidKeyStore | có |
| core/storage | `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` | Loại `innerlamp_secure` khỏi backup / device transfer | chưa cấu hình — `Docs/Specs/Storage/tasks.md` Phase 5 |
| core/network | mới `core/network/auth/SessionTokenCache.kt` (thay `InMemoryAuthTokenProvider` trong DI) | Cache token đồng bộ cho `AuthInterceptor` | chưa có |
| core/network | mới `core/network/auth/TokenRefreshAuthenticator.kt`, `SessionEvents.kt` | Refresh single-flight, lưu trước khi gọi lại, phát `Ended` | chưa có |
| core/network | `core/network/interceptor/AuthInterceptor.kt` | Gắn Bearer (giữ); bỏ `TODO(refresh-token)` | có — cần sửa nhỏ |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | Che body của `/v1/auth/*` và header `Authorization` | cần kiểm |
| DI | `di/NetworkModule.kt`, mới `di/AuthModule.kt` | Dựng `SessionTokenCache`, `TokenRefreshAuthenticator` (`.authenticator(…)` trên OkHttp), client refresh riêng, repository, store, use case, ViewModel factory | có — cần sửa |

## Navigation flow

```
LoginRoute(banner?, email?)   (gốc, Back thoát app)
  ├─ "Đăng nhập" ✓ → HomeRoute (pop Login)
  ├─ "Quên mật khẩu?" → ForgotPasswordRoute(email)
  │     ├─ "Gửi mã" ✓ → ResetPasswordRoute(email)
  │     │     └─ xác nhận ✓ → LoginRoute(banner = PasswordChanged, email) (pop toàn bộ)
  │     └─ "Trở lại đăng nhập" → pop
  ├─ "Tạo tài khoản mới" → SignupRoute (swap như hiện tại)
  └─ "Vào xem trước, chưa đăng nhập" → HomeRoute (khách)

Home (đã đăng nhập)
  ├─ Hồ sơ "Đăng xuất" → xác nhận → SignOutUseCase → LoginRoute (pop toàn bộ)
  └─ refresh bị từ chối → SessionEvents.Ended → clear() → LoginRoute(banner = SessionEnded) (pop toàn bộ)

Home (khách)
  └─ feature tài khoản → SignInPrompt → LoginRoute / SignupRoute
```

## Step-by-step Flow — Đăng nhập

```
Step 1 — Gửi
- Trigger: "Đăng nhập", email và mật khẩu không trống, không có lần gửi đang chạy.
- API: POST /v1/auth/login {email, password, device}
    - 200 → Step 2.
    - 401 INVALID_CREDENTIALS → tiêu chí 4; xoá mật khẩu.
    - 429 LOGIN_LOCKED → giờ mở khoá; 429 RATE_LIMITED → thời gian chờ.
    - 400 INVALID_EMAIL / INVALID_DEVICE, 5xx, mất mạng → lỗi tương ứng / chung.

Step 2 — Lưu phiên
- AuthSessionStore.save(session): SecureStorage.put(access), put(refresh) → SessionTokenCache.set → user vào PreferencesStorage.
- Lưu thất bại → "Không lưu được phiên đăng nhập"; xoá phần đã ghi; ở lại Login.

Step 3 — Vào app
- navigate(HomeRoute) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Gia hạn và kết thúc phiên

```
Step 1 — Lời gọi có @Authenticated
- AuthInterceptor gắn Bearer từ SessionTokenCache.
- 401 → OkHttp gọi TokenRefreshAuthenticator.authenticate.
    - Không có refresh token trong cache (khách) → trả null (không gọi lại), không phát sự kiện (tiêu chí 12).
    - Access token trong cache đã khác token của request (lời gọi khác vừa refresh) → gọi lại với token mới.
    - Còn lại → Step 2 (giữ lock).

Step 2 — Refresh (single-flight)
- API: POST /v1/auth/refresh {refresh_token} qua RefreshApiService (không AuthInterceptor).
    - 200 → SecureStorage.put cặp mới (runBlocking trên dispatchers.io) → SessionTokenCache.set → gọi lại request một lần.
    - 401 REFRESH_TOKEN_INVALID → Step 3.
    - Mất mạng / timeout / 5xx → trả null; request gốc thất bại với lỗi truyền tải; phiên giữ nguyên.
- Request gọi lại vẫn 401 → Step 3 (không refresh lần hai).

Step 3 — Phiên kết thúc
- SessionTokenCache.clear() → SessionEvents.emit(Ended).
- MainActivity (main thread): AuthSessionStore.clear() → navigate(LoginRoute(banner = SessionEnded)) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Đăng xuất

```
Step 1 — Xác nhận ở Hồ sơ.
Step 2 — API: POST /v1/auth/logout (@Authenticated) — mọi kết quả, kể cả mất mạng, bỏ qua.
Step 3 — AuthSessionStore.clear() (giữ onboarding_completed và lựa chọn giới thiệu).
Step 4 — navigate(LoginRoute) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Quên mật khẩu

```
Step 1 — Xin mã
- Trigger: "Gửi mã" với email hợp lệ.
- API: POST /v1/auth/password-reset/request-otp {email}
    - 200 → ResetPasswordRoute(email) với timing (luôn cùng một câu báo).
    - 400 INVALID_EMAIL → lỗi dưới ô.
    - 429 OTP_RESEND_TOO_SOON / OTP_SEND_LIMITED / OTP_LOCKED / RATE_LIMITED → câu báo kèm thời gian chờ hoặc giờ mở khoá.

Step 2 — Gửi lại (lặp được) — lại Step 1 sau thời gian chờ; xoá mã, chạy lại hai bộ đếm.

Step 3 — Đặt mật khẩu mới
- Pre-condition: đủ 6 số; mật khẩu mới qua RegistrationRules (BR-ONB-06).
- API: POST /v1/auth/password-reset/confirm {email, otp_code, new_password}
    - 200 → nếu có phiên: AuthSessionStore.clear() (không gọi logout — backend đã kết thúc mọi phiên)
      → navigate(LoginRoute(banner = PasswordChanged, email)) { popUpTo graph (inclusive) }.
    - 400 OTP_INVALID / OTP_EXPIRED / INVALID_PASSWORD, 429 OTP_LOCKED / RATE_LIMITED → ở lại, xoá mật
      khẩu mới, báo lỗi; OTP_LOCKED tắt ô nhập tới locked_until.
```

## Local storage

| Dữ liệu | Nơi lưu | Xoá khi đăng xuất / phiên hết? |
|---|---|---|
| Access token, refresh token | `SecureStorage` `DEVICE_UNLOCKED` + `SessionTokenCache` (bộ nhớ) | Có |
| Thông tin người dùng (id, họ tên, pháp danh) | `PreferencesStorage`, key `account.*` | Có |
| Dữ liệu của feature thuộc tài khoản (vd bản offline của sổ giỗ) | Room / prefs của feature, xoá qua `AccountDataCleaner` | Có |
| `onboarding_completed`, lựa chọn giới thiệu | `UserPreferences` | Không |

## Deliberately stubbed for now

- Đăng nhập bằng email và `password-reset/*` mới có trong spec backend; cho tới khi backend ship,
  Android v1.0 build và unit test với fake `AuthRepository`; refresh và logout đã có ở backend.
- Banner phiên hết / đã đổi mật khẩu: chờ chốt giao diện với anh Đức.

# Tasks — Login

Version: 1.1
Last updated: 2026-10-15
Status: planned — Phase 1–7 chưa bắt đầu; mục `[x]` đã đọc trong code ngày 2026-10-14 và khớp spec

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
Paths are relative to `app/src/main/java/com/example/innerlamp/`. Phase 1 làm trước vì
`../Onboarding/` (mở app, xác nhận mã) và mọi feature tài khoản (Memorial) phụ thuộc vào nó.

## Phase 1 — Lưu phiên (`AuthTokenStorage`)

- [x] `SecureStorage` AES-256-GCM, khoá AndroidKeyStore, `SecureLevel.DEVICE_UNLOCKED` (`core/storage/secure/`) (tiêu chí 8)
- [ ] Loại `innerlamp_secure` khỏi backup / device transfer (`Docs/Specs/Storage/tasks.md` Phase 5) (tiêu chí 8)
- [ ] `AuthSession`, `AuthUser`; `AuthSessionStore` + `AccountDataCleaner` (domain); `AuthSessionStoreImpl` (data) (tiêu chí 8, 11, 13)
- [ ] `SessionTokenCache` implement `AuthTokenProvider`; DI thay `InMemoryAuthTokenProvider`; nạp từ `SecureStorage` lúc Splash (tiêu chí 9)
- [x] `AuthInterceptor` gắn Bearer cho `@Authenticated`, gửi trần khi không có token (tiêu chí 10)
- [ ] Unit test: `clear()` xoá token, key `account.*`, gọi mọi cleaner, giữ `onboarding_completed` và lựa chọn giới thiệu

## Phase 2 — Đăng nhập bằng email

- [ ] `AuthApiService` (login), `AuthDto`, `DeviceInfoDto` (`platform = "android"`), `AuthRepositoryImpl` qua `safeCall` (tiêu chí 3)
- [ ] `SignInUseCase`: email/mật khẩu không trống → repository → `AuthSessionStore.save` (Step "Đăng nhập" 1–2) (tiêu chí 2, 3)
- [ ] `LoginUiState` / `LoginViewModel` / `LoginScreen`: ô email, bỏ số điện thoại + quốc gia + nút SMS + điều kiện ≥ 8, "Quên mật khẩu?", xoá mật khẩu khi lỗi, bỏ qua lần bấm khi đang gửi, `LOGIN_LOCKED` với giờ mở khoá (tiêu chí 1–6)
- [ ] `LoginRoute(banner, email)`; Login gốc thì Back thoát app; thành công → `HomeRoute` pop (tiêu chí 3, 7)
- [ ] `di/AuthModule.kt` + ViewModel factory cho Login
- [ ] Unit test: body request, từng lỗi, mật khẩu bị xoá, banner hiện theo tham số route

## Phase 3 — Gia hạn và kết thúc phiên

- [ ] `RefreshApiService` trên client không có `AuthInterceptor` (Step "Gia hạn" 2)
- [ ] `TokenRefreshAuthenticator`: single-flight, bỏ qua khi không có refresh token (khách), lưu cặp mới trước khi gọi lại, không refresh lần hai; nối `.authenticator(…)` trong `NetworkModule`; bỏ `TODO(refresh-token)` ở `AuthInterceptor` (tiêu chí 10, 12)
- [ ] `SessionEvents` + `MainActivity` lắng nghe: `AuthSessionStore.clear()` → `LoginRoute(banner = SessionEnded)` pop toàn bộ (tiêu chí 11)
- [ ] UI banner "Phiên đăng nhập đã hết" và "Đã đổi mật khẩu" trên Login — prototype chưa có, chốt giao diện với anh Đức trước khi làm (tiêu chí 11, 17)
- [ ] Unit test (MockWebServer): 401 → refresh → gọi lại; hai 401 đồng thời → một refresh; từ chối → `Ended` + dọn; mất mạng khi refresh → giữ phiên; khách 401 → không sự kiện

## Phase 4 — Đăng xuất

- [ ] `SignOutUseCase`: logout (bỏ qua kết quả, chạy cả khi mất mạng) → `AuthSessionStore.clear()` (Step "Đăng xuất" 1–4) (tiêu chí 13)
- [ ] `ProfileScreen`: hàng "Đăng xuất" + hộp xác nhận; callback về `MainActivity` mở Login gốc (tiêu chí 13)

## Phase 5 — Quên mật khẩu

- [ ] `AuthApiService` (password-reset/*), DTO, map lỗi `PasswordResetError` (Step "Quên mật khẩu" 1, 3)
- [ ] `RequestPasswordResetUseCase`, `ConfirmPasswordResetUseCase` (kiểm bằng `RegistrationRules`, dọn phiên nếu có) (tiêu chí 15, 17)
- [ ] `ForgotPasswordScreen` + ViewModel: email điền sẵn, "Gửi mã", "Trở lại đăng nhập" (tiêu chí 14)
- [ ] `ResetPasswordScreen` + ViewModel: câu báo chung, mã, mật khẩu mới, `CodeTimerRow`, khoá, gửi lại (tiêu chí 15, 16, 18)
- [ ] Route `ForgotPasswordRoute`, `ResetPasswordRoute`; nối `onForgotPassword` trong `MainActivity`; thành công → `LoginRoute(PasswordChanged, email)` (tiêu chí 17)
- [ ] Unit test: mọi email cùng một màn, gửi lại chạy lại bộ đếm, thành công dọn phiên đang lưu, từng lỗi

## Phase 6 — Khách

- [ ] "Vào xem trước" mở `HomeRoute` không lưu phiên (đã vào Home — giờ chỉ cần không giả làm đã đăng nhập) (tiêu chí 20)
- [ ] `SignInPrompt` + `AuthGate` ở `ui/components/auth/`, dẫn tới Login / Signup (tiêu chí 21); mỗi feature tài khoản nối trong spec của nó

## Phase 7 — Câu chữ, log, QA

- [ ] Câu chữ theo product §3.1.1; bỏ câu số điện thoại / SMS ở Login (tiêu chí 22)
- [ ] `LoggingInterceptor` không in body `/v1/auth/*` và header `Authorization` (tiêu chí 23)
- [ ] Chạy tay với backend dev: đăng nhập, sai thông tin, khoá, đặt lại trên máy khác làm phiên máy này hết kèm câu báo, đăng xuất khi mất mạng, khách gặp lời mời
- [ ] Cập nhật cột Android ở product §4.2 khi xong (báo `ba-writer`)
- [x] Bỏ dòng #5 "Vào xem trước" ở `Docs/home_assumptions.md` (bước dọn tài liệu cũ, 2026-10-15)

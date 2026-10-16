# Tasks — Login

Version: 1.2
Last updated: 2026-10-16
Status: planned — Phase 1–7 not started; `[x]` items were read in code on 2026-10-14 and match the spec

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
Paths are relative to `app/src/main/java/com/example/innerlamp/`. Phase 1 comes first because
`../Onboarding/` (app start, code confirmation) and every account feature (Memorial) depend on it.

## Phase 1 — Session storage (`AuthTokenStorage`)

- [x] `SecureStorage` AES-256-GCM, AndroidKeyStore key, `SecureLevel.DEVICE_UNLOCKED` (`core/storage/secure/`) (criterion 8)
- [ ] Exclude `innerlamp_secure` from backup / device transfer (`Docs/Specs/Storage/tasks.md` Phase 5) (criterion 8)
- [ ] `AuthSession`, `AuthUser`; `AuthSessionStore` + `AccountDataCleaner` (domain); `AuthSessionStoreImpl` (data) (criteria 8, 11, 13)
- [ ] `SessionTokenCache` implementing `AuthTokenProvider`; DI replaces `InMemoryAuthTokenProvider`; load from `SecureStorage` at Splash (criterion 9)
- [x] `AuthInterceptor` attaches Bearer for `@Authenticated`, sends bare when there is no token (criterion 10)
- [ ] Unit test: `clear()` removes the tokens and `account.*` keys, runs every cleaner, keeps `onboarding_completed` and the intro choices

## Phase 2 — Email sign-in

- [ ] `AuthApiService` (login), `AuthDto`, `DeviceInfoDto` (`platform = "android"`, `device_model`, `os_version`, `app_version`; no `device_name`), `AuthRepositoryImpl` via `safeCall`; no `X-API-Key` / client-IP header (criterion 3)
- [ ] `SignInUseCase`: non-empty email/password → repository → `AuthSessionStore.save` (Sign-in Steps 1–2) (criteria 2, 3)
- [ ] `LoginUiState` / `LoginViewModel` / `LoginScreen`: email field, remove phone + country + SMS button + the ≥ 8 condition, "Quên mật khẩu?", clear the password on error, ignore taps while submitting, `LOGIN_LOCKED` with unlock time (criteria 1–6)
- [ ] `LoginRoute(banner, email)`; Back exits the app when Login is the root; success → `HomeRoute` with pop (criteria 3, 7)
- [ ] `di/AuthModule.kt` + ViewModel factory for Login
- [ ] Unit test: request body (no `device_name`, no phone), each error (`INVALID_CREDENTIALS` incl. unverified email, `LOGIN_LOCKED`, `INVALID_DEVICE`), the password is cleared, the banner shows per the route argument

## Phase 3 — Session renewal and end

- [ ] `RefreshApiService` on a client without `AuthInterceptor`; DTO without `user` (Renewal Step 2)
- [ ] `TokenRefreshAuthenticator`: single-flight, skip when there is no refresh token (guest), store the new pair before retrying, no second refresh; wire `.authenticator(…)` in `NetworkModule`; remove `TODO(refresh-token)` from `AuthInterceptor` (criteria 10, 12)
- [ ] `503 SERVICE_UNAVAILABLE` on an authenticated call → generic error, no refresh, session kept (criterion 12a)
- [ ] `SessionEvents` + `MainActivity` listener: `AuthSessionStore.clear()` → `LoginRoute(banner = SessionEnded)` popping everything (criterion 11)
- [ ] UI for the "Phiên đăng nhập đã hết" and "Đã đổi mật khẩu" banners on Login — not in the prototype, settle the design with anh Đức first (criteria 11, 17)
- [ ] Unit test (MockWebServer): 401 `UNAUTHORIZED` → refresh → retry; two concurrent 401s → one refresh; rejected → `Ended` + cleanup (covers a session revoked by sign-out/password change elsewhere); network loss during refresh → session kept; refresh keeps the stored user; guest 401 → no event; 503 → no refresh

## Phase 4 — Sign-out

- [ ] `SignOutUseCase`: logout (result ignored, also offline and on 401) → `AuthSessionStore.clear()` (Sign-out Steps 1–4) (criterion 13)
- [ ] `ProfileScreen`: "Đăng xuất" row + confirmation dialog; callback to `MainActivity` opens Login as the root (criterion 13)

## Phase 5 — Forgot password

- [ ] `AuthApiService` (password-reset/*), DTOs (confirm returns no `data`), map errors to `PasswordResetError` (Forgot-password Steps 1, 3)
- [ ] `RequestPasswordResetUseCase`, `ConfirmPasswordResetUseCase` (check with `RegistrationRules`, clean up a stored session) (criteria 15, 17)
- [ ] `ForgotPasswordScreen` + ViewModel: prefilled email, "Gửi mã" with a loading state (the backend answers after ~1.5 s), "Trở lại đăng nhập" (criteria 14, 15)
- [ ] `ResetPasswordScreen` + ViewModel: shared message, code, new password, `CodeTimerRow`, lock, resend (criteria 15, 16, 18)
- [ ] Routes `ForgotPasswordRoute`, `ResetPasswordRoute`; wire `onForgotPassword` in `MainActivity`; success → `LoginRoute(PasswordChanged, email)` (criterion 17)
- [ ] Unit test: every email leads to the same screen, resend restarts the timers, success cleans a stored session and sends no logout, each error (`OTP_EXPIRED` for an unknown email, `OTP_LOCKED`, `RATE_LIMITED`)

## Phase 6 — Guest

- [ ] "Vào xem trước" opens `HomeRoute` without storing a session (already enters Home — now it only has to stop posing as signed in) (criterion 20)
- [ ] `SignInPrompt` + `AuthGate` in `ui/components/auth/`, leading to Login / Signup (criterion 21); each account feature wires it in its own spec

## Phase 7 — Wording, logging, QA

- [ ] Wording per product §3.1.1; remove the phone-number / SMS wording on Login (criterion 22)
- [ ] `LoggingInterceptor` prints no `/v1/auth/*` body and no `Authorization` header (criterion 23)
- [ ] Manual run against the dev backend (only after the contract 2.1.1 changes ship): sign-in, wrong credentials, lock, a reset on another device ends this device's session with the message, sign-out offline, guest meets the invitation
- [ ] Update the Android column of product §4.2 when done (tell `ba-writer`)
- [x] Drop row #5 "Vào xem trước" from `Docs/home_assumptions.md` (old-doc cleanup, 2026-10-15)

# Design — Login

Version: 1.2
Last updated: 2026-10-16
Status: planned (drift) — Login UI exists (phone-number based, simulated); `SecureStorage`, `AuthInterceptor` (attaches Bearer) and `InMemoryAuthTokenProvider` exist; session storage, refresh, sign-out, forgot password and guest mode do not

> See `changelog.md` in this folder for the version history of this spec.

## Overview

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, network calls
through `NetworkClient.safeCall`, threading through `DispatcherProvider`.

Three app-wide pieces, not only for the Login screen:

- **`AuthSessionStore`** (interface in `domain/repository/`, impl in `data/`): `save(session)`,
  `clear()`, `hasSession()`. `save` writes the token pair to `SecureStorage` (`DEVICE_UNLOCKED`) and
  loads the in-memory cache; `clear` deletes the tokens, the account's data (account keys in
  `PreferencesStorage`, the account's Room tables — each feature registers its own deletion through
  `AccountDataCleaner`) and keeps device settings. Sign-in, registration (`../Onboarding/`), sign-out,
  session end and password reset all go through it — the single place that writes/deletes tokens.
- **`SessionTokenCache`** (`core/network/auth/`) implements `AuthTokenProvider` in place of
  `InMemoryAuthTokenProvider`: keeps the token pair in memory so `AuthInterceptor` can read it
  synchronously (Networking's "no I/O in an interceptor" constraint); loaded from `SecureStorage` at
  app start (Splash).
- **Refresh through an OkHttp `Authenticator`** (`core/network/auth/TokenRefreshAuthenticator.kt`) in
  place of the `TODO(refresh-token)` hook in `AuthInterceptor`: runs only for `@Authenticated`
  requests that have a refresh token; single-flight with one lock — a request arriving later that sees
  the access token already changed uses it without refreshing a second time; calls refresh through a
  separate Retrofit service without `AuthInterceptor`; stores the new pair in `SecureStorage` (blocking
  on the OkHttp thread, `runBlocking(dispatchers.io)`) **before** returning the retry request. The
  refresh response carries no `user`, so only the tokens are replaced; the stored user info is kept.
  When rejected it emits `SessionEvent.Ended` on `SessionEvents` (a SharedFlow in `core/network/auth/`).
- **`MainActivity`** listens to `SessionEvents`: `AuthSessionStore.clear()` → navigate
  `LoginRoute(banner = SessionEnded)` popping the whole graph.

**Server-side session check (contract 2.1.1).** The backend validates the session on every
authenticated request: after sign-out, a password change, or a new sign-in on the same device, the old
access token gets `401 UNAUTHORIZED` at once, and its refresh token is then rejected with
`REFRESH_TOKEN_INVALID`, which ends the session (Step 3 of the session flow). If the backend cannot
check the session it returns `503 SERVICE_UNAVAILABLE`; that is not a `401`, so the `Authenticator`
does not run, the session is kept and the generic error shows. Mobile never sends `X-API-Key` or
`X-InnerLamp-Client-IP`.

**New UI:** the "Phiên đăng nhập đã hết" / "Đã đổi mật khẩu" banner on Login is not in the prototype;
settle the design with anh Đức before building (anh Đức, 2026-10-14: "sẽ cần thêm UI").

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-14.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Login | `ui/screens/login/LoginScreen.kt` | Email field (email keyboard, no capitalisation), password + show/hide, "Quên mật khẩu?", banner, "Tạo tài khoản mới", "Vào xem trước"; remove the phone field, country picker and SMS button | exists — needs change |
| UI / Login | `ui/screens/login/LoginUiState.kt`, `LoginViewModel.kt` | `email`, `password`, field errors, `submitting`, `banner`, `lockedUntil`; `submit()` calls `SignInUseCase`; clears the password on error; remove `canRequestOtp` | exists — needs change (simulated) |
| UI / Navigation | `ui/navigation/Routes.kt`, `MainActivity.kt` | `LoginRoute` becomes `data class LoginRoute(banner: LoginBanner? = null, email: String? = null)`; add `ForgotPasswordRoute(email)`, `ResetPasswordRoute(email)`; listen to `SessionEvents` | exists — needs change |
| UI / Forgot | new `ui/screens/forgotpassword/ForgotPasswordScreen.kt` + ViewModel + UiState | Email, "Gửi mã", "Trở lại đăng nhập" | missing |
| UI / Reset | new `ui/screens/forgotpassword/ResetPasswordScreen.kt` + ViewModel + UiState | "Nếu email này có tài khoản…", email, 6 digit boxes, new password + show/hide, `CodeTimerRow` (`../Onboarding/`), lock, "Gửi lại mã" | missing |
| UI / Sign-out | `ui/screens/profile/ProfileScreen.kt`, `ProfileViewModel.kt` | "Đăng xuất" row at the bottom; confirmation dialog; `SignOutUseCase`; `onSignedOut` callback → Login root | screen exists — no sign-out |
| UI / Guest | new `ui/components/auth/SignInPrompt.kt` + `AuthGate` (reads `AuthSessionStore.hasSession()`) | Shared sign-in invitation; account features wrap their screen in `AuthGate` | missing — each feature wires it in its own spec (Memorial already lists it) |
| Domain / Model | new `domain/model/AuthSession.kt` (`accessToken`, `refreshToken`, `user`), `AuthUser.kt`, `SignInError.kt`, `PasswordResetError.kt` | Plain data, no Android imports | missing |
| Domain / Repository | new `domain/repository/AuthRepository.kt` | `signIn(email, password): AuthSession`, `signOut()`, `requestResetCode(email): CodeTiming`, `confirmReset(email, code, newPassword)` | missing |
| Domain / Repository | new `domain/repository/AuthSessionStore.kt`, `AccountDataCleaner.kt` | `save`, `clear`, `hasSession`; the cleaner list is registered by features in DI | missing |
| Domain / Use case | new `domain/usecase/auth/SignInUseCase.kt`, `SignOutUseCase.kt`, `RequestPasswordResetUseCase.kt`, `ConfirmPasswordResetUseCase.kt` | Validate (email/password non-empty; new password per `RegistrationRules`) → repository → `AuthSessionStore` | missing |
| Data / API | new `data/remote/api/AuthApiService.kt` (login, logout `@Authenticated`, password-reset/*), `RefreshApiService.kt` (refresh, separate client) | Per the contract in the header | missing |
| Data / DTO | new `data/remote/dto/AuthDto.kt` (+ mapper); `DeviceInfoDto` built from `Build.MODEL`, `Build.VERSION.RELEASE`, `BuildConfig.VERSION_NAME`, `platform = "android"` (`device_name` omitted); `RefreshDto` has no `user`; `PasswordResetConfirm` returns no `data` | — | missing |
| Data / Repository | new `data/repository/AuthRepositoryImpl.kt` | `safeCall`; maps error codes → `SignInError` / `PasswordResetError`; sign-out ignores the result | missing |
| Data / Session | new `data/repository/AuthSessionStoreImpl.kt` | `SecureStorage.put/get/delete` (`DEVICE_UNLOCKED`, keys `auth.access_token`, `auth.refresh_token`) + `SessionTokenCache` + user in `PreferencesStorage` (`account.*` keys) + runs the `AccountDataCleaner`s | missing |
| core/storage | `core/storage/secure/SecureStorage.kt`, `DefaultSecureStorage.kt` | AES-256-GCM, AndroidKeyStore key | exists |
| core/storage | `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` | Exclude `innerlamp_secure` from backup / device transfer | not configured — `Docs/Specs/Storage/tasks.md` Phase 5 |
| core/network | new `core/network/auth/SessionTokenCache.kt` (replaces `InMemoryAuthTokenProvider` in DI) | Synchronous token cache for `AuthInterceptor` | missing |
| core/network | new `core/network/auth/TokenRefreshAuthenticator.kt`, `SessionEvents.kt` | Single-flight refresh, store before retry, emit `Ended` | missing |
| core/network | `core/network/interceptor/AuthInterceptor.kt` | Attach Bearer (keep); remove `TODO(refresh-token)` | exists — small change |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | Mask `/v1/auth/*` bodies and the `Authorization` header | to check |
| DI | `di/NetworkModule.kt`, new `di/AuthModule.kt` | Build `SessionTokenCache`, `TokenRefreshAuthenticator` (`.authenticator(…)` on OkHttp), the separate refresh client, repository, store, use cases, ViewModel factory | exists — needs change |

## Navigation flow

```
LoginRoute(banner?, email?)   (root, Back exits the app)
  ├─ "Đăng nhập" ✓ → HomeRoute (pop Login)
  ├─ "Quên mật khẩu?" → ForgotPasswordRoute(email)
  │     ├─ "Gửi mã" ✓ → ResetPasswordRoute(email)
  │     │     └─ confirm ✓ → LoginRoute(banner = PasswordChanged, email) (pop everything)
  │     └─ "Trở lại đăng nhập" → pop
  ├─ "Tạo tài khoản mới" → SignupRoute (swap as today)
  └─ "Vào xem trước, chưa đăng nhập" → HomeRoute (guest)

Home (signed in)
  ├─ Profile "Đăng xuất" → confirm → SignOutUseCase → LoginRoute (pop everything)
  └─ refresh rejected → SessionEvents.Ended → clear() → LoginRoute(banner = SessionEnded) (pop everything)

Home (guest)
  └─ account feature → SignInPrompt → LoginRoute / SignupRoute
```

## Step-by-step Flow — Sign-in

```
Step 1 — Submit
- Trigger: "Đăng nhập", email and password non-empty, no submit in flight.
- API: POST /v1/auth/login {email, password, device}
    - 200 → Step 2.
    - 401 INVALID_CREDENTIALS → criterion 4; clear the password (also covers an unverified email).
    - 429 LOGIN_LOCKED → unlock time from details.locked_until; 429 RATE_LIMITED → wait from retry_after_seconds.
    - 400 INVALID_EMAIL / INVALID_DEVICE / VALIDATION_ERROR, 5xx, no network → matching / generic error.

Step 2 — Store the session
- AuthSessionStore.save(session): SecureStorage.put(access), put(refresh) → SessionTokenCache.set → user to PreferencesStorage.
- Store failure → "Không lưu được phiên đăng nhập"; remove what was written; stay on Login.

Step 3 — Enter the app
- navigate(HomeRoute) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Session renewal and end

```
Step 1 — An @Authenticated call
- AuthInterceptor attaches Bearer from SessionTokenCache.
- 401 UNAUTHORIZED → OkHttp calls TokenRefreshAuthenticator.authenticate. (Also the response after sign-out, a password
  change, or a new sign-in on the same device: the backend checks the session on every request.)
    - No refresh token in the cache (guest) → return null (no retry), emit no event (criterion 12).
    - The cached access token differs from the request's (another call just refreshed) → retry with the new token.
    - Otherwise → Step 2 (hold the lock).
- 503 SERVICE_UNAVAILABLE → not a 401: no refresh, session kept, generic error (criterion 12a).

Step 2 — Refresh (single-flight)
- API: POST /v1/auth/refresh {refresh_token} through RefreshApiService (no AuthInterceptor).
    - 200 {access_token, refresh_token, expires_in} → SecureStorage.put the new pair (runBlocking on dispatchers.io) → SessionTokenCache.set → retry the request once. The stored user info is untouched.
    - 401 REFRESH_TOKEN_INVALID → Step 3.
    - No network / timeout / 5xx / 429 RATE_LIMITED → return null; the original request fails with the transport error; the session is kept.
- The retried request still 401 → Step 3 (no second refresh).

Step 3 — Session ended
- SessionTokenCache.clear() → SessionEvents.emit(Ended).
- MainActivity (main thread): AuthSessionStore.clear() → navigate(LoginRoute(banner = SessionEnded)) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Sign-out

```
Step 1 — Confirm on Profile.
Step 2 — API: POST /v1/auth/logout (@Authenticated) — every outcome, including offline and 401, is ignored.
Step 3 — AuthSessionStore.clear() (keeps onboarding_completed and the intro choices).
Step 4 — navigate(LoginRoute) { popUpTo graph (inclusive) }.
```

## Step-by-step Flow — Forgot password

```
Step 1 — Request a code
- Trigger: "Gửi mã" with a valid email.
- API: POST /v1/auth/password-reset/request-otp {email}
    - 200 → ResetPasswordRoute(email) with the timing (always the same message, account or not; the backend waits ~1.5 s before answering, so show a loading state).
    - 400 INVALID_EMAIL → error under the field.
    - 429 OTP_RESEND_TOO_SOON / OTP_SEND_LIMITED / OTP_LOCKED / RATE_LIMITED → message with the wait or the unlock time.

Step 2 — Resend (repeatable) — Step 1 again after the wait (no separate resend endpoint; the old code stops working); clear the code, restart both timers.

Step 3 — Set the new password
- Pre-condition: 6 digits entered; new password through RegistrationRules (BR-ONB-06).
- API: POST /v1/auth/password-reset/confirm {email, otp_code, new_password}
    - 200 {success: true} (no data, no token) → if a session exists: AuthSessionStore.clear() (no logout call — the backend already
      revoked every session on every device and lifted LOGIN_LOCKED)
      → navigate(LoginRoute(banner = PasswordChanged, email)) { popUpTo graph (inclusive) }.
    - 400 OTP_INVALID / OTP_EXPIRED (also returned for an unknown email) / INVALID_PASSWORD, 429 OTP_LOCKED / RATE_LIMITED → stay, clear the
      new password, show the error; OTP_LOCKED disables the input until locked_until.
```

## Local storage

| Data | Where | Cleared on sign-out / session end? |
|---|---|---|
| Access token, refresh token | `SecureStorage` `DEVICE_UNLOCKED` + `SessionTokenCache` (memory) | Yes |
| User info (id, full name, dharma name) | `PreferencesStorage`, `account.*` keys | Yes |
| Data of account-scoped features (e.g. the memorial book's offline copy) | The feature's Room / prefs, cleared through `AccountDataCleaner` | Yes |
| `onboarding_completed`, intro choices | `UserPreferences` | No |

## Deliberately stubbed for now

- The contract is now in `openapi.yaml` 2.1.1, but the backend changes behind it are uncommitted and
  untested against a real server (2026-10-16). Until they ship, Android v1.x builds and unit-tests
  against a fake `AuthRepository`; do not run the manual check against a dev server before then.
- The session-ended / password-changed banner: waiting for the design to be settled with anh Đức.

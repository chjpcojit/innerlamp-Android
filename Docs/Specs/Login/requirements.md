# Requirements — Login

Version: 1.2
Last updated: 2026-10-16
Status: planned (drift) — the Login screen has a phone-number UI and simulates success; no session storage, refresh, sign-out or forgot-password yet; "Vào xem trước" enters Home as if signed in. (Drift table last read against code on 2026-10-14; not re-read for v1.2.)
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/login/` (+ new `forgotpassword/`), `core/network/auth/`, `MainActivity.kt` (navigation when the session ends), `ui/screens/profile/` (sign-out)
Product spec: innerlamp-be `docs/product/login/product.md` @ v1.4
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ info.version 2.1.1 (Auth tag: `login`, `refresh`, `logout`, `password-reset/*`); backend feature spec innerlamp-be `docs/specs/features/login/requirements.md` @ v2.5. Mobile clients do **not** send `X-API-Key` or `X-InnerLamp-Client-IP`.
Architecture: ADR-0003 — Accepted
Implements: BR-LGN-01, BR-LGN-02, BR-LGN-03, BR-LGN-04, BR-LGN-05, BR-LGN-06, BR-LGN-07, FR-LGN-001, FR-LGN-002, FR-LGN-003, FR-LGN-004, FR-LGN-005, FR-LGN-006, FR-LGN-008, FR-LGN-009, FR-LGN-010, NFR-LGN-01, NFR-LGN-02, NFR-LGN-03
Not on Android: FR-LGN-007 (website only)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) for sign-in, session, sign-out, forgot password and guest mode on Android.
The rules — the shared wrong-credentials message, the lock after 5 failures, the 7-day sliding session,
the password-reset policy, what a guest may open — live in the product spec and are **not** repeated here.

This spec is also the **`AuthTokenStorage`** spec that `Docs/Specs/Networking/` and `Docs/Specs/Storage/`
deferred: storing tokens in `SecureStorage`, providing the token to `AuthInterceptor`, and the refresh
flow on `401`.

## User Story

As a member, I want to sign in with my email and password, stay signed in, be told when my session has
ended, and reset a forgotten password with a code sent to my email — or preview the app as a guest — so
that I can use InnerLamp on my Android phone on my own.

## Acceptance Criteria (EARS notation)

### Sign-in

1. WHEN `LoginRoute` opens, THE SYSTEM SHALL show an email field, a password field with "Hiện/Ẩn", "Đăng nhập", "Quên mật khẩu?", "Tạo tài khoản mới" and "Vào xem trước, chưa đăng nhập"; there is no phone-number field, country picker or "Gửi mã một lần qua SMS". (FR-LGN-001, FR-LGN-010)
2. WHEN the user taps sign-in with an empty email or an empty password, THE SYSTEM SHALL show an error under that field and SHALL NOT call the backend; no password-length rule applies here (drop the current ≥ 8 condition — the password only has to be non-empty). (BR-LGN-01)
3. WHEN the user submits, THE SYSTEM SHALL call `POST /v1/auth/login` with the trimmed email, the password and `device {platform: "android", device_model, os_version, app_version}` (only `device_model` is required by the backend; `platform` is optional — if absent the server stores `mobile` — but Android always sends `"android"`; `device_name` is optional, only stored with the device, and omitted), then:
   - success: store the session (criterion 8) and open `HomeRoute`, removing the sign-in flow from the back stack;
   - failure: stay, keep the email, clear the password, show the error from the Error → UI table. (FR-LGN-001, FR-LGN-002)
4. WHEN the backend returns `INVALID_CREDENTIALS`, THE SYSTEM SHALL show "Email hoặc mật khẩu không đúng". The backend returns the same code for a wrong password, an unknown email, and an email whose registration is not yet verified, so the app SHALL NOT distinguish them. (FR-LGN-002, BR-LGN-01, NFR-LGN-01)
5. WHEN the backend returns `LOGIN_LOCKED` (after 5 failures on the same email), THE SYSTEM SHALL show the lock message with the unlock time from `details.locked_until` in the device's local time. (FR-LGN-003, BR-LGN-02)
6. WHILE a sign-in is in flight, THE SYSTEM SHALL ignore further taps. (platform)
7. WHEN Login is the root (opening the app without a session, after sign-out, session ended), THE SYSTEM SHALL let Back exit the app; when opened from Signup, keep today's Signup ↔ Login swap. (FR-ONB-002, platform)

### Session

8. WHEN sign-in or registration succeeds, THE SYSTEM SHALL store the access token and refresh token only in `SecureStorage` at `SecureLevel.DEVICE_UNLOCKED` (AES-256-GCM, key in AndroidKeyStore, file `innerlamp_secure` excluded from cloud backup and device transfer), never in `PreferencesStorage` or Room; the returned user info (id, full name, dharma name) goes to `PreferencesStorage` under an account-scoped key and is cleared on sign-out. (NFR-LGN-03, BR-LGN-03)
9. WHEN the app opens with a stored session, THE SYSTEM SHALL go to `HomeRoute` without asking for the password again (`../Onboarding/` criterion 1). (FR-LGN-004)
10. WHEN an `@Authenticated` call receives `401`, THE SYSTEM SHALL refresh once (single-flight across concurrent calls; the new token pair is stored before the call is retried) and retry. The backend checks the session on **every** request, so after sign-out, a password change or a new sign-in on the same device, the old access token gets `401 UNAUTHORIZED` on the very next request without waiting for expiry; the refresh that follows then fails with `REFRESH_TOKEN_INVALID` (criterion 11). A refresh that fails from no network / timeout / 5xx / `429 RATE_LIMITED` (the refresh budget is per IP) keeps the session for a later attempt. The refresh response has no `user`, so the stored user info is left unchanged. (FR-LGN-004, BR-LGN-03)
11. WHEN the refresh is rejected (`REFRESH_TOKEN_INVALID`) or a session once existed but the refresh token is gone, THE SYSTEM SHALL clear the tokens and all account data on the device (same as sign-out), open `LoginRoute` as the root and show "Phiên đăng nhập đã hết, vui lòng đăng nhập lại."; it SHALL NOT silently push the user out. (FR-LGN-005, BR-LGN-04)
12. WHEN a guest (who never stored a session) gets `401`, THE SYSTEM SHALL NOT treat it as a session ending (no cleanup, no message); apply the sign-in invitation of criterion 21. (BR-LGN-06, platform)
12a. WHEN an authenticated call returns `503 SERVICE_UNAVAILABLE` (the backend could not check the session), THE SYSTEM SHALL show the generic error, keep the session and SHALL NOT start a refresh or end the session. (FR-LGN-004, platform)

### Sign-out

13. WHEN the user chooses "Đăng xuất" on the Profile screen, THE SYSTEM SHALL ask for confirmation; cancelling keeps the session; confirming calls `POST /v1/auth/logout` (result ignored, including `401 UNAUTHORIZED`), clears the tokens and the account's data on the device, keeps device settings (`onboarding_completed`, intro choices) and opens `LoginRoute` as the root — even when offline. (FR-LGN-006, BR-LGN-07)

### Forgot password

14. WHEN the user chooses "Quên mật khẩu?", THE SYSTEM SHALL open a request screen with an email field (prefilled if typed on Login), "Gửi mã" and "Trở lại đăng nhập". (FR-LGN-008)
15. WHEN the user submits a valid email, THE SYSTEM SHALL call `POST /v1/auth/password-reset/request-otp` and, for any email, open the new-password screen showing "Nếu email này có tài khoản, mã đã được gửi", the email, a countdown of the code's validity and the resend wait — both taken from the response (`expires_in_seconds`, `resend_after_seconds`). The backend answers `200` with an identical body whether or not the account exists (and takes about 1.5 s to do so), so the screen SHALL show a loading state and SHALL NOT time out before that. (FR-LGN-008, BR-LGN-05, NFR-LGN-01)
16. WHEN the user taps "Gửi lại mã" after the wait, THE SYSTEM SHALL repeat the same request (there is no separate resend endpoint; calling again sends a new code and invalidates the old one), clear the entered code and restart both timers. (FR-LGN-008)
17. WHEN the user has entered all 6 digits and a new password, THE SYSTEM SHALL check the password with the BR-ONB-06 rule (the validator in `../Onboarding/`, 8–64 characters) and call `POST /v1/auth/password-reset/confirm`. Success is `200 {success: true}` with **no token and no `data`**: if the device holds a session it is cleaned up as in criterion 13 (no logout call — the backend already revoked every session on every device), and `LoginRoute` opens as the root with "Đã đổi mật khẩu, vui lòng đăng nhập lại." and the email prefilled; the user then signs in with the new password. A successful reset also lifts any `LOGIN_LOCKED` on that email, so the new password works at once. (FR-LGN-009, BR-LGN-05)
18. WHEN confirmation fails, THE SYSTEM SHALL stay on the new-password screen, keep the code, clear the new password and show the error; `OTP_LOCKED` disables the input until `details.locked_until`. An unknown email also returns `OTP_EXPIRED`, indistinguishable from an expired code. (FR-LGN-009)
19. WHEN the password is reset on another device, THE SYSTEM SHALL handle the next `401` on this device per criteria 10–11. (FR-LGN-005, FR-LGN-009)

### Guest

20. WHEN the user chooses "Vào xem trước, chưa đăng nhập", THE SYSTEM SHALL open `HomeRoute` in guest mode (no session stored); public content opens normally. (FR-LGN-010, BR-LGN-06)
21. WHEN a guest opens something that belongs to an account (memorial book, family tree, practice notes, profile, notifications), THE SYSTEM SHALL show the shared sign-in invitation (explanation, "Đăng nhập", "Tạo tài khoản") in place of that screen and SHALL NOT call the backend for it. (FR-LGN-010, BR-LGN-06)

### General

22. WHEN showing an error, THE SYSTEM SHALL use only app-owned wording (per product §3.1.1) and never the backend's `error.message`; `RATE_LIMITED` (a per-IP limit on every `/v1/auth/*` route) shows "try again later" with the wait taken from `details.retry_after_seconds` (also sent as the `Retry-After` header). (NFR-LGN-02)
23. WHEN the app logs requests or errors (including `LoggingInterceptor` in debug builds), THE SYSTEM SHALL never log passwords, codes, tokens or the `Authorization` header. (NFR-LGN-03)

## Data & API

| Action | Operation | Notes |
|---|---|---|
| Sign in | `POST /v1/auth/login` | Public; email + password + device |
| Refresh | `POST /v1/auth/refresh` | Public; called by a client with no Bearer; returns `{access_token, refresh_token, expires_in}` (no `user`) |
| Sign out | `POST /v1/auth/logout` | `@Authenticated`; `200 {success: true}`, also when the session was already revoked |
| Request reset code | `POST /v1/auth/password-reset/request-otp` | Public |
| Set new password | `POST /v1/auth/password-reset/confirm` | Public; `200 {success: true}`, no token |

Request/response shapes: see the contract in the header. This file does not copy schemas.

## Error → UI mapping

| Case | The user sees |
|---|---|
| `INVALID_CREDENTIALS` | "Email hoặc mật khẩu không đúng" |
| `LOGIN_LOCKED` | Lock message with the unlock time |
| `INVALID_EMAIL` | Error under the email field |
| `INVALID_PASSWORD` (reset) | Error under the new-password field |
| `OTP_INVALID` / `OTP_EXPIRED` / `OTP_LOCKED` / `OTP_RESEND_TOO_SOON` / `OTP_SEND_LIMITED` | Same as the registration code screen (`../Onboarding/requirements.md` Error → UI) |
| `RATE_LIMITED` | Try again later, with the wait |
| `REFRESH_TOKEN_INVALID`, `401` after refresh | Session ended (criterion 11) |
| `503 SERVICE_UNAVAILABLE` (authenticated call) | Generic error; session kept (criterion 12a) |
| `INVALID_DEVICE`, `VALIDATION_ERROR`, 5xx | Generic error |
| No network / timeout | Shared no-network / retry message (`AppError` in Networking) |

## Current drift (code read on 2026-10-14)

| Where | Today | Per this spec |
|---|---|---|
| `LoginUiState` / `LoginScreen` | Phone number + country, SMS button, password ≥ 8 | Criteria 1–2 |
| `LoginViewModel.onLoginClick` | Simulates success | Criteria 3–6 |
| `NetworkModule.provideAuthTokenProvider` | `InMemoryAuthTokenProvider`, no real token source | Criteria 8–9 |
| `AuthInterceptor` | Attaches Bearer; `TODO(refresh-token)` | Criteria 10–12a |
| Sign-out | None | Criterion 13 |
| `MainActivity` `onForgotPassword` | TODO | Criteria 14–18 |
| "Vào xem trước" | Enters Home as if signed in (was `Docs/home_assumptions.md` #5, dropped in v1.1) | Criteria 20–21 |

## Out of scope

- Registration and the registration code → `../Onboarding/`.
- Code sign-in, Google sign-in, sign-out of all devices, changing the password while signed in → later (product §1.2).
- How each feature handles guests beyond showing the shared invitation (e.g. `Docs/Specs/Memorial/`).
- Unlocking the app with biometrics (`SecureLevel.USER_AUTH`) → not part of this product spec.

## Pending (not yet implemented)

Everything — `tasks.md` Phase 1–7. The backend changes behind contract 2.1.1 are uncommitted and have not been run against a real server (per the backend team, 2026-10-16); do the manual check in `tasks.md` Phase 7 only once they ship.

## Open questions (resolved)

- ~~Name and folder grouping?~~ → One `Login/` spec named after the product slug, including forgot password and the `AuthTokenStorage` part that Networking/Storage deferred. Claude's default, reviewed in this PR. Resolved 2026-10-14.
- ~~Where does "Đăng xuất" go?~~ → At the bottom of the Profile screen (`ProfileScreen`), the equivalent of the "Tôi" tab on iOS. Claude's default, reviewed in this PR. Resolved 2026-10-14.
- ~~Keep the password after a failed sign-in?~~ → Clear the password, keep the email (product §3.7). Resolved 2026-10-14.
- ~~"Gửi mã một lần qua SMS"?~~ → Removed (no code sign-in). Resolved 2026-10-14.
- ~~Is a guest's `401` a session ending?~~ → No; a guest never had a session, so the sign-in invitation applies (criteria 12, 21). Resolved 2026-10-14.
- ~~Auto sign-in after a reset?~~ → No (BR-LGN-05); open Login with the email prefilled. Resolved 2026-10-14.
- ~~Does the session-ended message need its own UI?~~ → Yes; the prototype has none, so settle the design with anh Đức before building (anh Đức, 2026-10-14: "sẽ cần thêm UI"). Resolved 2026-10-14.

## Open questions (to confirm)

- ~~Send `device.device_name`?~~ → No; the backend only stores it with the device and says Android need not send it (backend, 2026-10-16). Resolved 2026-10-16.

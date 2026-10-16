# Requirements — Onboarding

Version: 1.1
Last updated: 2026-10-16
Status: planned (drift) — splash, 3 intro steps and the Signup form have UI but do not call the backend (`SignupViewModel` simulates success); no code-entry screen, no temple proposal, intro flag not stored; launch always goes to the intro. (Drift table last read against code on 2026-10-14; not re-read for v1.1.)
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/{splash,onboarding,signup}/` (+ new `otp/`)
Product spec: innerlamp-be `docs/product/onboarding/product.md` @ v1.7
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ info.version 2.1.1 (Auth tag: `register/request-otp`, `register/resend-otp`, `register/verify-otp`); backend feature spec innerlamp-be `docs/specs/features/onboarding/requirements.md` @ v8.3. Mobile clients do **not** send `X-API-Key` or `X-InnerLamp-Client-IP`.
Architecture: ADR-0003 (accounts are identified by email and verified by a code sent to the email; SMS removed) — Accepted
Implements: BR-ONB-01, BR-ONB-02, BR-ONB-03, BR-ONB-04, BR-ONB-05, BR-ONB-06, BR-ONB-07, BR-ONB-08, BR-ONB-09, BR-ONB-10, BR-ONB-11, FR-ONB-001, FR-ONB-002, FR-ONB-003, FR-ONB-004, FR-ONB-005, FR-ONB-006, FR-ONB-007, FR-ONB-008, FR-ONB-009, FR-ONB-010, NFR-ONB-01, NFR-ONB-02, NFR-ONB-03
Not on Android: the website row of FR-ONB-003 in product §3.6 (a signed-in visitor opening the registration page)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) for the first app launch and registration on Android. Product behaviour — when
the intro shows, navigation at launch, each field's rules, the list of 12 countries, the code policy,
an existing email looking like a new one, the terms — lives in the product spec and is **not** repeated
here; this file only says how the Android app implements the `BR-ONB-*` / `FR-ONB-*`.

The spec is written **prospectively**: Android had no earlier spec for splash/intro/registration. The
existing UI code was read on 2026-10-14 to record what is kept and what drifts from the product (the
"Current drift" table at the end). Sign-in, session, sign-out, forgot password, guest → `../Login/`.

## User Story

As someone opening InnerLamp on an Android phone for the first time, I want to see a short intro and
then create an account with my email, a password, a phone number and my name, confirmed by a code sent to
my email, so that I am signed in and my practice follows me across devices.

## Acceptance Criteria (EARS notation)

### Splash, intro and navigation at launch

1. WHEN the app starts, THE SYSTEM SHALL show the splash and move on by itself; then open `HomeRoute` if a session is stored, otherwise `OnboardingRoute` when the `onboarding_completed` flag is not set, otherwise **`LoginRoute`** as the root. Splash reads the flag and the session through a use case; a plain `delay` no longer decides the destination. (FR-ONB-002, BR-ONB-02)
2. WHEN the app starts for the first time after install (no `onboarding_completed` flag), THE SYSTEM SHALL delete any token left in secure storage before navigating. (BR-ONB-01, platform — in case a backup/restore carried the secure prefs file over)
3. WHEN the intro shows, THE SYSTEM SHALL show 3 steps per product §3.1.1 with "Bỏ qua" and a progress bar; step 1 has "Bắt đầu" and "Tôi đã có tài khoản", steps 2–3 have "Quay lại". (FR-ONB-001)
4. WHEN the user chooses "Tạo tài khoản" on step 3, "Bỏ qua" on any step, or "Tôi đã có tài khoản" on step 1, THE SYSTEM SHALL set the `onboarding_completed` flag **before** leaving the intro; "Tôi đã có tài khoản" opens `LoginRoute`, the other two open `SignupRoute`; `OnboardingRoute` is removed from the back stack. (FR-ONB-001, BR-ONB-01)
5. WHEN the user picks an intention (step 2) or a bell time (step 3), THE SYSTEM SHALL store it in `UserPreferences` on the device and SHALL NOT send it to the backend. (BR-ONB-03)
6. WHEN the user signs out or the session expires, THE SYSTEM SHALL keep the `onboarding_completed` flag and the intro choices. (BR-ONB-01, `login` BR-LGN-07)

### Registration form (Signup)

7. WHEN `SignupRoute` opens, THE SYSTEM SHALL show, in the order of product §3.1.1: full name (empty), dharma name (**empty**, with "Gợi ý"), place of ordination, phone (country + number), email with a hint that it is used to sign in and receive the code, password (show/hide button + strength hint) and the terms line; the button reads "Đăng ký", with a link to Sign-in. (FR-ONB-003, BR-ONB-07)
8. WHEN the user taps "Gợi ý", THE SYSTEM SHALL fill the next dharma name of the 10 suggestions; the user may keep, edit or clear it. (BR-ONB-07)
9. WHEN the user submits the form, THE SYSTEM SHALL check every field with the BR-ONB-04…07 rules — a phone number whose length **equals** the country's after removing spaces, dots, dashes and one leading 0; a trimmed email matching the pattern; a password of 8–64 **characters** after NFC; a full name of 1–300 and a dharma name of 0–255 characters after trimming — and on errors show them under the field, keep every value and SHALL NOT call the backend. (FR-ONB-003, BR-ONB-04, BR-ONB-05, BR-ONB-06, BR-ONB-07)
10. WHEN the user types a password, THE SYSTEM SHALL show the 3-level strength hint for reference only; it SHALL NOT block submitting. (BR-ONB-06, §3.6)
11. WHILE the terms are not accepted, THE SYSTEM SHALL keep "Đăng ký" disabled. (BR-ONB-11)
12. WHEN the user opens the terms of use or privacy policy from the terms line, THE SYSTEM SHALL keep today's behaviour (static in-app content) and, on closing, return to Signup with every value and the consent box unchanged. Moving to one shared text (BR-ONB-11) is for later. (BR-ONB-11, platform)
13. WHEN the user submits a valid form, THE SYSTEM SHALL call `POST /v1/auth/register/request-otp` with the trimmed email, the password, `phone_country` (the ISO code of the chosen country) and `phone_number` (the digits as typed — no leading `+`, no country code, not rebuilt into E.164; the server normalises to E.164), the full name, the dharma name only when non-empty and `ordination_temple_id` only when a temple from the list is chosen, then:
    - success: open `OtpRoute` with the email and the server's `expires_in_seconds` / `resend_after_seconds`;
    - failure: stay on Signup, keep every value, show the error from the Error → UI table. (FR-ONB-003, BR-ONB-08, BR-ONB-09)
14. WHEN the email already has an account, THE SYSTEM SHALL handle it exactly like a new email (the backend answers `200` with an identical body and sends the owner a notice instead of a code; `verify-otp` for such an email always returns `OTP_INVALID`); the "email already used" message no longer exists and there is no `EMAIL_TAKEN`. (BR-ONB-08, NFR-ONB-01)
15. WHEN the backend returns `PHONE_TAKEN` (`409`), THE SYSTEM SHALL show "Số điện thoại này đã được đăng ký" under the phone field with a link to Sign-in, and stay. (FR-ONB-005)
15a. WHEN the backend returns `REGISTRATION_IN_PROGRESS` (`409`: the email has a pending registration with different content and a code that is still valid), THE SYSTEM SHALL stay on Signup, keep every value and show "Vui lòng chờ N giây rồi thử lại" with N = `details.retry_after_seconds` (never a value of the app's own); the old code stays usable, no email is sent, and the answer is the same whether or not the email has an account. Resubmitting the same details replaces the code and opens the code screen as in criterion 13. Note: after `EMAIL_SEND_FAILED` the pending registration and its code stay stored and valid on the backend, so editing a field and resubmitting at once can also return `REGISTRATION_IN_PROGRESS` even though no email arrived; resubmitting the same details works (the backend has reported this to anh Đức; nothing changed yet). (FR-ONB-003, BR-ONB-08, BR-ONB-09)
16. WHILE a submit is in flight, THE SYSTEM SHALL ignore further taps. (platform)

### Place of ordination and temple proposal

17. WHEN the user opens the place-of-ordination field, THE SYSTEM SHALL list `GET /v1/temples/options` page by page (through the existing `TempleOptionRepository`) with "Chưa xuất gia — Cư sĩ tu tại gia" first and server-side name search (debounced; late stale results dropped). The current free-text field is replaced by this picker. (FR-ONB-004, BR-ONB-10)
18. WHEN the user proposes a temple that is not listed, THE SYSTEM SHALL require a temple name, a province and a ward chosen from the administrative-unit list (the existing `AdministrativeUnitRepository`), with an optional street line; the proposal is kept for the registration session and is never sent as `ordination_temple_id`. (FR-ONB-006, BR-ONB-10)
19. WHEN the account has just been created and a proposal is held, THE SYSTEM SHALL send one `POST /v1/temple-suggestions` (public, no token); a failure SHALL NOT affect registration and is not shown as an error. (FR-ONB-006)

### Email code (Otp)

20. WHEN the code screen opens, THE SYSTEM SHALL show "Đã gửi tới <email>", a 6-digit input, a countdown of the code's remaining validity (from `expires_in_seconds`, measured with elapsed time) and "Gửi lại mã" disabled with the seconds left to wait (from `resend_after_seconds`). (FR-ONB-007, FR-ONB-008, BR-ONB-09)
21. WHEN the user has entered 6 digits and confirms, THE SYSTEM SHALL call `POST /v1/auth/register/verify-otp` with the email and the code; on success (`201`, an `AuthResult` with tokens and `user`) store the session as sign-in does (`../Login/` criterion 8), send the temple proposal (criterion 19) and open `HomeRoute`, removing the whole registration flow from the back stack. (FR-ONB-007)
22. WHEN the countdown reaches 0, THE SYSTEM SHALL say the code has expired and point only to "Gửi lại mã"; the app assumes no duration of its own. (FR-ONB-008, BR-ONB-09)
23. WHEN the user taps "Gửi lại mã" after the wait, THE SYSTEM SHALL call `POST /v1/auth/register/resend-otp` with the email only (no `purpose`), clear the input and restart both timers from the response; `resend_remaining` in the response may be ignored. (FR-ONB-008)
24. WHEN the backend returns `OTP_LOCKED` or `OTP_SEND_LIMITED`, THE SYSTEM SHALL show the unlock time from `details.locked_until` in the device's local time; `OTP_LOCKED` disables the input until then, `OTP_SEND_LIMITED` still lets the user enter the last code. (FR-ONB-007, FR-ONB-008, BR-ONB-09)
25. WHEN the user chooses "Đổi email", THE SYSTEM SHALL return to Signup with every value unchanged, including the password. (FR-ONB-009)
26. WHEN the backend returns `PENDING_REGISTRATION_EXPIRED` (no pending registration for the email, or older than 24 hours), THE SYSTEM SHALL show "Thời gian đăng ký đã hết. Vui lòng đăng ký lại từ đầu." and return to Signup with every value unchanged. (FR-ONB-010)

### General

27. WHEN showing an error, THE SYSTEM SHALL use only app-owned wording (per product §3.1.1) and never the backend's `error.message`; `RATE_LIMITED` (a per-IP limit on every `/v1/auth/*` route) shows "try again later" with the wait taken from `details.retry_after_seconds` (also the `Retry-After` header). (NFR-ONB-02)
28. WHEN the app logs requests or errors (including `LoggingInterceptor` in debug builds), THE SYSTEM SHALL never log passwords, codes or tokens. (NFR-ONB-03)
29. WHEN unit tests run, THE SYSTEM SHALL pass every case of `rules/phone-number.json`, `rules/email.json`, `rules/password.json` and `rules/names.json` with the app's validator. (BR-ONB-04…07)

## Data & API

| Action | Operation | Notes |
|---|---|---|
| Submit the form | `POST /v1/auth/register/request-otp` | Public; email + `phone_country`; the code is sent by **email**, valid 600 s (dev), resend after 60 s, at most 4 codes per cycle |
| Resend the code | `POST /v1/auth/register/resend-otp` | Public; body `{email}` only |
| Confirm the code | `POST /v1/auth/register/verify-otp` | Public; `201`, returns a session like sign-in |
| Temple list | `GET /v1/temples/options` | `TempleApiService` exists |
| Province / ward | the administrative-unit routes | `AdministrativeUnitApiService` exists |
| Temple proposal | `POST /v1/temple-suggestions` | Public, already live on the backend (temple-suggestion v3.4 US-TSG-01) |

Request/response shapes: see the contract in the header. This file does not copy schemas.

## Error → UI mapping

| Case | The user sees |
|---|---|
| `INVALID_EMAIL` / `INVALID_PHONE` / `INVALID_PASSWORD` / `INVALID_FULL_NAME` / `INVALID_DHARMA_NAME` | Error under the matching field |
| `INVALID_TEMPLE` (at submit or at `verify-otp`) | Error on the place-of-ordination field, ask to choose again (at `verify-otp`: return to Signup) |
| `VALIDATION_ERROR` | Generic invalid-data message |
| `PHONE_TAKEN` (at submit, or at `verify-otp` when taken meanwhile) | Criterion 15 (at `verify-otp`: back to Signup) |
| `REGISTRATION_IN_PROGRESS` | Criterion 15a |
| `OTP_RESEND_TOO_SOON` | The resend timer restarts from `retry_after_seconds` |
| `OTP_SEND_LIMITED` | Unlock time; the last code still works (criterion 24) |
| `OTP_INVALID` | "Mã xác thực không đúng" |
| `OTP_EXPIRED` | Expired message, stressing "Gửi lại mã" |
| `OTP_LOCKED` | Unlock time, input disabled until then (criterion 24) |
| `PENDING_REGISTRATION_EXPIRED` | Criterion 26 |
| `RATE_LIMITED` | Try again later, with the wait |
| `EMAIL_SEND_FAILED` (HTTP 500), 5xx | Generic error (no code is counted for a failed send); see the note in criterion 15a |
| No network / timeout | Shared no-network / retry message (`AppError` in Networking) |

## Current drift (code read on 2026-10-14)

| Where | Today | Per this spec |
|---|---|---|
| `SplashViewModel` + `MainActivity` | Splash always goes to `OnboardingRoute` | Criteria 1–2 |
| `OnboardingViewModel` | Choices only in memory, flag never set | Criteria 4–5 |
| `SignupUiModel` | No full name; "Nơi xuất gia" is a free-text field | Criteria 7, 17 |
| `SignupViewModel` | Email checked only for "@" and "."; password ≥ 8; phone ≥ country length; simulates success | Criteria 9, 13 |
| `SignupScreen` | Old wording about SMS / phone number | Criteria 7, 27 |
| Code screen | Missing | Criteria 20–26 |

## Out of scope

- Sign-in, session, sign-out, forgot password, guest → `../Login/`.
- Changing email or phone, deleting an account, Google sign-in → later (product §1.2).
- Scheduling notifications for the bell on the intro step → not part of this product spec.

## Pending (not yet implemented)

Everything — `tasks.md` Phase 1–7. The backend changes behind contract 2.1.1 are uncommitted and have not been run against a real server (per the backend team, 2026-10-16).

> Later (anh Đức, 2026-10-14): the "one shared text" part of BR-ONB-11; Signup keeps today's static terms until then.

## Open questions (resolved)

- ~~Name and folder grouping?~~ → One `Onboarding/` spec named after the product slug (splash + intro + registration + code + temple proposal), like iOS v2.0. Claude's default, reviewed in this PR. Resolved 2026-10-14.
- ~~Where do the terms come from?~~ → Later; keep today's static content (anh Đức: "bỏ qua phần điều khoản, chúng ta sẽ làm sau"). Resolved 2026-10-14.
- ~~Does the backend accept temple proposals yet?~~ → Yes: `POST /v1/temple-suggestions` is live, public (no token, limited per device address), backend temple-suggestion v3.4 US-TSG-01. Android sends it (criterion 19). Resolved 2026-10-14.
- ~~Navigation at launch?~~ → First time: intro, set the flag, go to Signup. Later launches without a session: Login (anh Đức, 2026-10-14; criteria 1, 4). Resolved 2026-10-14.
- ~~Keep the password on "Đổi email" / expired registration?~~ → Keep every field (FR-ONB-009, FR-ONB-010). Resolved 2026-10-14.
- ~~A real countdown or a fixed "valid N minutes" text?~~ → Count down the remaining time (FR-ONB-008). Resolved 2026-10-14.

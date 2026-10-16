# Design — Onboarding

Version: 1.1
Last updated: 2026-10-16
Status: planned (drift) — splash / intro / Signup UI exists; use cases, the registration repository and data layer, the code screen, the temple picker and the temple proposal do not

> See `changelog.md` in this folder for the version history of this spec.

## Overview

The architecture stays `ui → domain ← data → core/{network,storage}`, manual DI in `di/`, every network
call through `NetworkClient.safeCall`, threading through `DispatcherProvider`, time counted with elapsed
time. Closest pattern: `TempleOptionRepositoryImpl` + `di/TempleModule.kt`.

Main changes from the current code:

- **Splash decides the destination.** `SplashViewModel` calls `ResolveLaunchDestinationUseCase` (stored
  session? intro flag?) instead of a plain `delay`; `MainActivity` navigates on the result. The session is
  read through `AuthSessionStore` from `../Login/`.
- **The intro flag and choices live in `UserPreferences`** (`onboardingCompleted` already exists); add
  the intentions and the bell time. This is device data, not cleared on sign-out.
- **Email registration.** One `RegistrationRepository` for the three public calls; `SignupViewModel` and
  `OtpViewModel` go through use cases. A plain-Kotlin validator in `domain/auth/` runs the product's
  test vectors.
- **The state of one registration** (form values, held temple proposal, email) lives in a
  `SignupViewModel` scoped to the back stack entry of `SignupRoute`; `OtpRoute` shares that ViewModel
  (like `GioRoute`/`GioEditRoute`), so "Đổi email" and "registration expired" just pop back to Signup.
- **Session storage after the code is confirmed** uses `AuthSessionStore.save` from `../Login/` — the
  single place that writes tokens.
- **No client secrets.** Mobile sends neither `X-API-Key` nor `X-InnerLamp-Client-IP` on `register/*`.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Status read on 2026-10-14.

| Layer | File | Responsibility | Status |
|---|---|---|---|
| UI / Splash | `ui/screens/splash/SplashViewModel.kt`, `SplashUiState.kt` | Keep a minimum splash time, call `ResolveLaunchDestinationUseCase`, emit `destination` (`Home` / `Onboarding` / `Login`) | exists — needs change (only a `delay` today) |
| UI / Navigation | `MainActivity.kt` (`AppNavHost`), `ui/navigation/Routes.kt` | Splash → destination (pop Splash); Onboarding → Signup / Login (pop Onboarding); add `OtpRoute`, `TemplePickerRoute`, `TempleProposalRoute` nested under `SignupRoute`; code confirmed → `HomeRoute` popping the whole flow | exists — needs change |
| UI / Onboarding | `ui/screens/onboarding/OnboardingViewModel.kt` | Step, intentions, bell as today; `complete(then)` calls `CompleteOnboardingUseCase` and only then navigates | exists — needs change |
| UI / Onboarding | `ui/screens/onboarding/OnboardingScreen.kt` | "Tôi đã có tài khoản" / "Bỏ qua" / "Tạo tài khoản" all go through `complete` | exists — needs change |
| UI / Signup | `ui/screens/signup/SignupScreen.kt` | Field order per product §3.1.1; add full name; the place-of-ordination field opens a picker; "Đăng ký" button; errors under fields; `PHONE_TAKEN` + Sign-in link; `REGISTRATION_IN_PROGRESS` wait message | exists — needs change |
| UI / Signup | `ui/screens/signup/SignupViewModel.kt`, `SignupUiState.kt` | Form + field errors + `submitting` + held temple proposal + email/timing for the code screen; `submit()` calls `RequestRegistrationCodeUseCase`; maps errors → UI | exists — needs change (simulated) |
| UI / Temple picker | new `ui/screens/signup/TemplePickerScreen.kt` (+ state in `SignupViewModel` or `TemplePickerViewModel`) | `TempleOptionRepository` list page by page, "Chưa xuất gia — Cư sĩ tu tại gia" first, debounced search, drop stale results; "Đề xuất chùa" entry | missing |
| UI / Temple proposal | new `ui/screens/signup/TempleProposalScreen.kt` | Temple name, province, ward (`AdministrativeUnitRepository`), optional street line; saved into `SignupViewModel` | missing |
| UI / Otp | new `ui/screens/otp/OtpScreen.kt`, `OtpUiState.kt` | "Đã gửi tới <email>", 6 digit boxes, validity countdown, "Gửi lại mã" + wait seconds, lock state, "Đổi email" | missing |
| UI / Otp | new `ui/screens/otp/OtpViewModel.kt` or the Otp part of `SignupViewModel` | `verify()`, `resend()`, two timers on `ElapsedClock`, `locked_until` → device time | missing |
| UI / Shared | new `ui/components/auth/CodeTimerRow.kt` | Countdown + "Gửi lại mã" row shared with forgot password (`../Login/`) | missing |
| Domain / Model | `domain/model/SignupUiModel.kt` → split: `domain/model/RegistrationForm.kt` (plain data), field error `RegistrationFieldError` | Add `fullName`, `templeId`, `templeProposal`; drop the free-text `placeOfOrigin`; `emailValid`/`phoneOk` computed by the validator | exists — needs change |
| Domain / Model | new `domain/model/CodeTiming.kt`, `TempleProposal.kt`, `RegistrationError.kt` | Code lifetime + resend wait; temple proposal; business errors mapped from `AppError` | missing |
| Domain / Rules | new `domain/auth/RegistrationRules.kt` | BR-ONB-04…07 validator (exact length per country, email, password 8–64 characters after NFC, full name/dharma name); reuses `PhoneCountries` (12 countries) | missing — `PhoneCountry.isComplete` is `>=` today |
| Domain / Rules | `domain/model/PasswordStrength.kt` | 3-level hint, does not block submitting | exists |
| Domain / Repository | new `domain/repository/RegistrationRepository.kt` | `requestCode(form)`, `resendCode(email)`, `verify(email, code): AuthSession`, `proposeTemple(proposal)` | missing |
| Domain / Use case | new `domain/usecase/onboarding/ResolveLaunchDestinationUseCase.kt`, `CompleteOnboardingUseCase.kt` | Launch destination; set the flag + store the choices; on first install `AuthSessionStore.clear()` | missing |
| Domain / Use case | new `domain/usecase/onboarding/RequestRegistrationCodeUseCase.kt`, `ResendRegistrationCodeUseCase.kt`, `VerifyRegistrationUseCase.kt` | Validate → repository; verify success → `AuthSessionStore.save` → send the temple proposal (result ignored) | missing |
| Data / API | new `data/remote/api/RegistrationApiService.kt` | 3 `register/*` routes + `temple-suggestions`; none `@Authenticated` | missing |
| Data / DTO | new `data/remote/dto/RegistrationDto.kt` (+ mapper) | Per the contract in the header; `phone_number` as typed digits; omit `dharma_name` / `ordination_temple_id` when empty; `resend-otp` body is `{email}` only | missing |
| Data / Repository | new `data/repository/RegistrationRepositoryImpl.kt` | `safeCall`; maps backend error codes → `RegistrationError` | missing |
| Data / Repository | `data/repository/TempleOptionRepositoryImpl.kt`, `AdministrativeUnitRepositoryImpl.kt` | Temple list, province/ward | exists |
| Data / Prefs | `core/datastore/UserPreferences.kt` | `onboardingCompleted` (exists); add `practiceIntents`, `bellTimes` | exists — needs change |
| DI | new `di/RegistrationModule.kt`; `di/OnboardingModule.kt` (or merged) | Build the API service, repository, use cases; ViewModel factory for Splash / Onboarding / Signup / Otp | missing — ViewModels use `viewModel()` without a factory today |

## Navigation flow

```
SplashRoute (pop)
  ├─ session stored        → HomeRoute
  ├─ intro not seen        → OnboardingRoute
  └─ otherwise             → LoginRoute (root, ../Login/)

OnboardingRoute (pop on leaving, flag set)
  ├─ "Tạo tài khoản" / "Bỏ qua" → SignupRoute
  └─ "Tôi đã có tài khoản"      → LoginRoute

SignupRoute  (SignupViewModel keeps the form for the whole branch)
  ├─ place-of-ordination field → TemplePickerRoute → "Đề xuất chùa" → TempleProposalRoute → pop to Signup
  ├─ "Đăng nhập"    → LoginRoute (swap as today)
  └─ "Đăng ký" ✓    → OtpRoute
        ├─ "Đổi email" / registration expired → pop to SignupRoute
        └─ confirmed ✓ → HomeRoute, pop the whole flow (Back exits the app)
```

## Step-by-step Flow — App launch

```
Step 1 — Read the state
- Trigger: SplashViewModel.init.
- ResolveLaunchDestinationUseCase (dispatchers.io):
    - onboarding_completed flag missing → AuthSessionStore.clear() (token left from a previous install) → Onboarding.
    - AuthSessionStore.hasSession() → Home.
    - otherwise → Login.
- No network call; a secure-storage read failure counts as no session (Login).

Step 2 — Navigate
- After the minimum splash time: MainActivity navigate(destination) { popUpTo<SplashRoute>(inclusive) }.
```

## Step-by-step Flow — Registration

```
Step 1 — Submit the form
- Trigger: "Đăng ký", terms accepted, RegistrationRules all pass, no submit in flight.
- API: POST /v1/auth/register/request-otp (public)
    - 200 → CodeTiming {expiresIn, resendAfter}; go to Step 2. An email that already has an account gets the identical answer (criterion 14).
    - 400 INVALID_* → error under the field; stay.
    - 409 PHONE_TAKEN → criterion 15; stay.
    - 409 REGISTRATION_IN_PROGRESS → criterion 15a: message with details.retry_after_seconds; stay, keep every value.
    - 429 OTP_RESEND_TOO_SOON / OTP_SEND_LIMITED / RATE_LIMITED → message with the wait/unlock time; stay.
    - EMAIL_SEND_FAILED / 5xx / no network → generic error / no-network message; stay.

Step 2 — Open the code screen
- navigate(OtpRoute); both timers run from the response, measured with ElapsedClock.

Step 3 — Resend (repeatable)
- Trigger: "Gửi lại mã" after the wait.
- API: POST /v1/auth/register/resend-otp {email}
    - 200 → clear the input, restart both timers (resend_remaining may be ignored).
    - 429 OTP_RESEND_TOO_SOON → restart the resend timer from retry_after_seconds.
    - 429 OTP_SEND_LIMITED → unlock time; the input stays usable.
    - 400 PENDING_REGISTRATION_EXPIRED → Step 5.

Step 4 — Confirm
- Trigger: 6 digits entered, "Xác nhận", input not locked.
- API: POST /v1/auth/register/verify-otp {email, otp_code}
    - 201 → AuthSessionStore.save(session) (secure storage + token cache); go to Step 6.
      Store failure → "Không lưu được phiên đăng nhập" (the account exists; the user can sign in).
    - 400 OTP_INVALID → wrong-code message, keep the input (also the answer for an email that already has an account).
    - 400 OTP_EXPIRED → expired message, stress resend.
    - 429 OTP_LOCKED → unlock time, disable the input until locked_until.
    - 400 PENDING_REGISTRATION_EXPIRED → Step 5.
    - 400 INVALID_TEMPLE (the temple chosen at Step 1 no longer exists) → back to Signup with the error on the place-of-ordination field.
    - 409 PHONE_TAKEN (taken while waiting) → back to Signup with the error on the phone field.

Step 5 — Registration expired
- Show the FR-ONB-010 message; popBackStack to SignupRoute, every value kept.

Step 6 — Send the temple proposal (only if held)
- API: POST /v1/temple-suggestions (public, no token)
    - Any result → ignored as far as the user is concerned.
- navigate(HomeRoute) { popUpTo<SignupRoute>(inclusive) }.
```

## Local storage

| Data | Where | Cleared on sign-out? |
|---|---|---|
| `onboarding_completed`, intentions, bell time | `UserPreferences` (`PreferencesStorage`) | No |
| Tokens after the code is confirmed | `AuthSessionStore` → `SecureStorage` `DEVICE_UNLOCKED` (`../Login/`) | Yes |
| Form, held temple proposal | `SignupViewModel` (memory; `SavedStateHandle` for non-sensitive fields; the password only in memory) | — |

## Deliberately stubbed for now

- The email `register/*` routes are now in `openapi.yaml` 2.1.1, but the backend changes behind them are
  uncommitted and untested against a real server (2026-10-16). Until they ship, Android v1.x builds and
  unit-tests against a fake `RegistrationRepository`; E2E waits for the backend (innerlamp-be
  `docs/specs/features/onboarding/tasks.md`).
- Terms: keep today's static content (the shared-text part of BR-ONB-11 comes later).

# Tasks — Onboarding

Version: 1.1
Last updated: 2026-10-16
Status: planned — Phase 1–7 not started; `[x]` items were read in code on 2026-10-14 and match the spec

> See `changelog.md` in this folder for the version history of this spec.

Each item traces to a row of the **Layers & components** table or a Step in `design.md`.
Paths are relative to `app/src/main/java/com/example/innerlamp/`. Dependency: `AuthSessionStore` from
`../Login/tasks.md` Phase 1 must exist before Phase 3 (app launch) and Phase 5 (code confirmation).

## Phase 1 — Domain

- [ ] `domain/auth/RegistrationRules.kt`: phone number equal to the country's length (fix `PhoneCountry.isComplete`, currently `>=`), email, password 8–64 characters after NFC, full name 1–300, dharma name 0–255 (criterion 9)
- [x] 12 countries and number lengths (`domain/model/PhoneCountry.kt` `PhoneCountries`) (criterion 7)
- [x] 3-level password strength hint, does not block submitting (`domain/model/PasswordStrength.kt`) (criterion 10)
- [ ] `RegistrationForm`, `RegistrationFieldError`, `CodeTiming`, `TempleProposal`, `RegistrationError` (incl. `REGISTRATION_IN_PROGRESS` with its wait); drop the free-text `placeOfOrigin` (criteria 7, 13, 15a, 18)
- [ ] `domain/repository/RegistrationRepository.kt`
- [ ] Use cases: `ResolveLaunchDestinationUseCase`, `CompleteOnboardingUseCase`, `RequestRegistrationCodeUseCase`, `ResendRegistrationCodeUseCase`, `VerifyRegistrationUseCase` (Launch Step 1, Registration Steps 1, 3, 4, 6)
- [ ] Unit test running `rules/phone-number.json`, `rules/email.json`, `rules/password.json`, `rules/names.json` from innerlamp-be `docs/product/onboarding/` (criterion 29)

## Phase 2 — Intro and flag

- [ ] `UserPreferences`: add `practiceIntents`, `bellTimes` (criterion 5)
- [x] `onboarding_completed` flag in `UserPreferences` (criterion 4)
- [ ] `OnboardingViewModel.complete(then)` calls `CompleteOnboardingUseCase` before navigating; all three exits go through it (criterion 4)
- [x] 3 steps, "Bỏ qua", progress bar, "Bắt đầu" / "Tôi đã có tài khoản" / "Quay lại" (`OnboardingScreen`) (criterion 3) — re-check wording against product §3.1.1 when doing Phase 2
- [ ] Unit test: all three exits set the flag; sign-out keeps the flag and the choices (criterion 6)

## Phase 3 — App launch

- [ ] `SplashViewModel` uses `ResolveLaunchDestinationUseCase`; `SplashUiState.destination` (criterion 1)
- [ ] First install: `AuthSessionStore.clear()` before navigating (criterion 2)
- [ ] `MainActivity`: Splash → destination (pop Splash); Onboarding → Signup / Login (pop Onboarding) (criteria 1, 4)
- [ ] Unit test `ResolveLaunchDestinationUseCase`: session stored / intro not seen / otherwise

## Phase 4 — Registration form, place of ordination, temple proposal

- [ ] `SignupScreen`: field order per product §3.1.1, add full name, dharma name empty at first, "Đăng ký" button, errors under fields, `PHONE_TAKEN` + Sign-in link, `REGISTRATION_IN_PROGRESS` wait message (criteria 7, 15, 15a)
- [x] "Gợi ý" dharma name cycling through 10 names (`SignupViewModel.onSuggestDharmaName`) (criterion 8)
- [ ] `SignupViewModel.submit()`: validate → `RequestRegistrationCodeUseCase`; ignore taps while submitting; keep every value on error; remove the simulated `delay(600)` (criteria 9, 11, 13, 16)
- [ ] The terms line keeps today's static content; closing it keeps the form (criterion 12)
- [ ] `TemplePickerScreen`: `TempleOptionRepository` page by page, "Chưa xuất gia — Cư sĩ tu tại gia" first, debounced search, drop stale results (criterion 17)
- [x] `TempleOptionRepository` + `TempleOptionRepositoryImpl` + `di/TempleModule.kt` (criterion 17)
- [ ] `TempleProposalScreen`: name, province, ward, street line; kept in `SignupViewModel` (criterion 18)
- [x] `AdministrativeUnitRepository` + impl + `di/AdministrativeUnitModule.kt` (criterion 18)
- [ ] Routes `TemplePickerRoute`, `TempleProposalRoute` nested under `SignupRoute`, sharing `SignupViewModel`
- [ ] Unit test: an invalid form does not call the repository; the body has no `dharma_name` / `ordination_temple_id` when empty and `phone_number` is the typed digits (no `+`, no country code); an email with an account is handled like a new one; `REGISTRATION_IN_PROGRESS` keeps the form (criteria 13, 14, 15a)

## Phase 5 — Registration data and code screen

- [ ] `RegistrationApiService`, `RegistrationDto` + mapper (resend body `{email}` only), `RegistrationRepositoryImpl` via `safeCall`, errors mapped per Error → UI; no `X-API-Key` / client-IP header (Registration Steps 1, 3, 4, 6)
- [ ] `di/RegistrationModule.kt` + ViewModel factory for Splash / Onboarding / Signup / Otp (replacing `viewModel()` without a factory)
- [ ] `OtpRoute` + `OtpScreen` + state: email, 6 digit boxes, validity countdown and resend wait on `ElapsedClock`, lock from `locked_until` (criteria 20, 22, 24)
- [ ] `ui/components/auth/CodeTimerRow.kt` shared with forgot password (criteria 20, 23)
- [ ] Confirm: `VerifyRegistrationUseCase` → `AuthSessionStore.save` → temple proposal (result ignored) → `HomeRoute` popping the whole flow (criteria 19, 21)
- [ ] Resend, "Đổi email", registration expired, `INVALID_TEMPLE` / `PHONE_TAKEN` at confirm → pop to Signup keeping values (criteria 23, 25, 26)
- [ ] Unit test: each error of Steps 3–4; session store failure; a temple-proposal failure does not block entering Home

## Phase 6 — Wording and logging

- [ ] Wording per product §3.1.1; remove every "SMS" / "OTP by phone number" sentence; never use the backend's `error.message` (criterion 27)
- [ ] `LoggingInterceptor`: do not log `/v1/auth/*` bodies, also in debug builds (criterion 28)

## Phase 7 — QA and docs

- [ ] Manual run against the dev backend (only after the contract 2.1.1 changes ship): first launch → intro → Signup; restart → Login; register with all fields; an email that already has an account; `PHONE_TAKEN`; `REGISTRATION_IN_PROGRESS`; wrong / expired / locked code; resend; change email; temple proposal
- [ ] Update the Android column of product §4.2 when done (tell `ba-writer`)

# Design — Onboarding

Version: 1.0
Last updated: 2026-10-14
Status: planned (drift) — UI splash / giới thiệu / Signup có; use case, repository, data layer đăng ký, màn nhập mã, bộ chọn chùa và đề xuất chùa chưa có

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Kiến trúc giữ nguyên `ui → domain ← data → core/{network,storage}`, DI thủ công trong `di/`, mọi lời
gọi mạng qua `NetworkClient.safeCall`, đổi thread qua `DispatcherProvider`, đếm thời gian bằng elapsed
time. Mẫu gần nhất: `TempleOptionRepositoryImpl` + `di/TempleModule.kt`.

Thay đổi chính so với code hiện tại:

- **Splash quyết định đích.** `SplashViewModel` gọi `ResolveLaunchDestinationUseCase` (phiên đã lưu?
  cờ giới thiệu?) thay cho `delay` thuần; `MainActivity` điều hướng theo kết quả. Phiên đọc qua
  `AuthSessionStore` của `../Login/`.
- **Cờ và lựa chọn giới thiệu nằm ở `UserPreferences`** (đã có `onboardingCompleted`); thêm ý hướng
  và khung giờ chuông. Đây là dữ liệu của máy, không xoá khi đăng xuất.
- **Đăng ký bằng email.** Một `RegistrationRepository` cho ba lời gọi public; `SignupViewModel` và
  `OtpViewModel` gọi qua use case. Validator thuần Kotlin trong `domain/auth/` để chạy vector của
  product.
- **Bộ nhớ của một lần đăng ký** (giá trị form, đề xuất chùa đang giữ, email) nằm ở
  `SignupViewModel` gắn vào back stack entry của `SignupRoute`; `OtpRoute` dùng chung ViewModel đó
  (giống `GioRoute`/`GioEditRoute`), nên "Đổi email" và "đăng ký hết hạn" chỉ cần pop về Signup.
- **Lưu phiên sau khi xác nhận mã** dùng `AuthSessionStore.save` của `../Login/` — một chỗ duy nhất
  ghi token.

## Layers & components

Paths are relative to `app/src/main/java/com/example/innerlamp/`. Trạng thái đọc ngày 2026-10-14.

| Layer | File | Responsibility | Trạng thái |
|---|---|---|---|
| UI / Splash | `ui/screens/splash/SplashViewModel.kt`, `SplashUiState.kt` | Giữ thời lượng splash tối thiểu, gọi `ResolveLaunchDestinationUseCase`, phát `destination` (`Home` / `Onboarding` / `Login`) | có — cần sửa (đang chỉ `delay`) |
| UI / Navigation | `MainActivity.kt` (`AppNavHost`), `ui/navigation/Routes.kt` | Splash → đích theo `destination` (pop Splash); Onboarding → Signup / Login (pop Onboarding); thêm `OtpRoute`, `TemplePickerRoute`, `TempleProposalRoute` lồng dưới `SignupRoute`; xác nhận mã → `HomeRoute` pop cả luồng | có — cần sửa |
| UI / Onboarding | `ui/screens/onboarding/OnboardingViewModel.kt` | Bước, ý hướng, chuông như hiện tại; `complete(then)` gọi `CompleteOnboardingUseCase` rồi mới điều hướng | có — cần sửa |
| UI / Onboarding | `ui/screens/onboarding/OnboardingScreen.kt` | "Tôi đã có tài khoản" / "Bỏ qua" / "Tạo tài khoản" đều đi qua `complete` | có — cần sửa |
| UI / Signup | `ui/screens/signup/SignupScreen.kt` | Thứ tự field theo product §3.1.1; thêm họ tên; ô nơi xuất gia mở bộ chọn; nút "Đăng ký"; lỗi dưới field; `PHONE_TAKEN` + link Đăng nhập | có — cần sửa |
| UI / Signup | `ui/screens/signup/SignupViewModel.kt`, `SignupUiState.kt` | Form + lỗi field + `submitting` + đề xuất chùa đang giữ + email/timing cho màn mã; `submit()` gọi `RequestRegistrationCodeUseCase`; map lỗi → UI | có — cần sửa (đang giả lập) |
| UI / Temple picker | mới `ui/screens/signup/TemplePickerScreen.kt` (+ state trong `SignupViewModel` hoặc `TemplePickerViewModel`) | Danh sách `TempleOptionRepository` theo trang, "Chưa xuất gia — Cư sĩ tu tại gia" đầu, tìm kiếm debounce, bỏ kết quả cũ; lối "Đề xuất chùa" | chưa có |
| UI / Temple proposal | mới `ui/screens/signup/TempleProposalScreen.kt` | Tên chùa, tỉnh, phường/xã (`AdministrativeUnitRepository`), địa chỉ đường tuỳ chọn; lưu vào `SignupViewModel` | chưa có |
| UI / Otp | mới `ui/screens/otp/OtpScreen.kt`, `OtpUiState.kt` | "Đã gửi tới <email>", 6 ô số, đếm ngược hiệu lực, "Gửi lại mã" + giây chờ, trạng thái khoá, "Đổi email" | chưa có |
| UI / Otp | mới `ui/screens/otp/OtpViewModel.kt` hoặc phần Otp của `SignupViewModel` | `verify()`, `resend()`, hai bộ đếm theo `ElapsedClock`, `locked_until` → giờ máy | chưa có |
| UI / Shared | mới `ui/components/auth/CodeTimerRow.kt` | Hàng đếm ngược + "Gửi lại mã" dùng chung với quên mật khẩu (`../Login/`) | chưa có |
| Domain / Model | `domain/model/SignupUiModel.kt` → tách: `domain/model/RegistrationForm.kt` (dữ liệu thuần), lỗi field `RegistrationFieldError` | Thêm `fullName`, `templeId`, `templeProposal`; bỏ `placeOfOrigin` tự do, `emailValid`/`phoneOk` tính bằng validator | có — cần sửa |
| Domain / Model | mới `domain/model/CodeTiming.kt`, `TempleProposal.kt`, `RegistrationError.kt` | Thời hạn mã + chờ gửi lại; đề xuất chùa; lỗi nghiệp vụ map từ `AppError` | chưa có |
| Domain / Rules | mới `domain/auth/RegistrationRules.kt` | Validator BR-ONB-04…07 (độ dài chính xác theo quốc gia, email, mật khẩu 8–64 ký tự sau NFC, họ tên/pháp danh); dùng lại `PhoneCountries` (12 quốc gia) | chưa có — `PhoneCountry.isComplete` hiện là `>=` |
| Domain / Rules | `domain/model/PasswordStrength.kt` | Gợi ý 3 mức, không chặn gửi | có |
| Domain / Repository | mới `domain/repository/RegistrationRepository.kt` | `requestCode(form)`, `resendCode(email)`, `verify(email, code): AuthSession`, `proposeTemple(proposal)` | chưa có |
| Domain / Use case | mới `domain/usecase/onboarding/ResolveLaunchDestinationUseCase.kt`, `CompleteOnboardingUseCase.kt` | Đích lúc mở app; bật cờ + lưu lựa chọn; lần đầu cài thì `AuthSessionStore.clear()` | chưa có |
| Domain / Use case | mới `domain/usecase/onboarding/RequestRegistrationCodeUseCase.kt`, `ResendRegistrationCodeUseCase.kt`, `VerifyRegistrationUseCase.kt` | Validate → repository; verify thành công → `AuthSessionStore.save` → gửi đề xuất chùa (bỏ qua kết quả) | chưa có |
| Data / API | mới `data/remote/api/RegistrationApiService.kt` | 3 route `register/*` + `temple-suggestions`; không có `@Authenticated` | chưa có |
| Data / DTO | mới `data/remote/dto/RegistrationDto.kt` (+ mapper) | Theo contract ở header; không gửi `dharma_name` / `ordination_temple_id` khi trống | chưa có |
| Data / Repository | mới `data/repository/RegistrationRepositoryImpl.kt` | `safeCall`; map mã lỗi backend → `RegistrationError` | chưa có |
| Data / Repository | `data/repository/TempleOptionRepositoryImpl.kt`, `AdministrativeUnitRepositoryImpl.kt` | Danh sách chùa, tỉnh/phường | có |
| Data / Prefs | `core/datastore/UserPreferences.kt` | `onboardingCompleted` (có); thêm `practiceIntents`, `bellTimes` | có — cần sửa |
| DI | mới `di/RegistrationModule.kt`; `di/OnboardingModule.kt` (hoặc gộp) | Dựng API service, repository, use case; ViewModel factory cho Splash / Onboarding / Signup / Otp | chưa có — ViewModel đang `viewModel()` không factory |

## Navigation flow

```
SplashRoute (pop)
  ├─ có phiên            → HomeRoute
  ├─ chưa xem giới thiệu → OnboardingRoute
  └─ còn lại             → LoginRoute (gốc, ../Login/)

OnboardingRoute (pop khi rời, cờ đã bật)
  ├─ "Tạo tài khoản" / "Bỏ qua" → SignupRoute
  └─ "Tôi đã có tài khoản"      → LoginRoute

SignupRoute  (SignupViewModel giữ form cho cả nhánh)
  ├─ ô nơi xuất gia → TemplePickerRoute → "Đề xuất chùa" → TempleProposalRoute → pop về Signup
  ├─ "Đăng nhập"    → LoginRoute (swap như hiện tại)
  └─ "Đăng ký" ✓    → OtpRoute
        ├─ "Đổi email" / đăng ký hết hạn → pop về SignupRoute
        └─ xác nhận ✓ → HomeRoute, pop toàn bộ luồng (Back thoát app)
```

## Step-by-step Flow — Mở app

```
Step 1 — Đọc trạng thái
- Trigger: SplashViewModel.init.
- ResolveLaunchDestinationUseCase (dispatchers.io):
    - cờ onboarding_completed chưa có → AuthSessionStore.clear() (token sót từ lần cài trước) → Onboarding.
    - AuthSessionStore.hasSession() → Home.
    - còn lại → Login.
- Không gọi mạng; lỗi đọc secure storage → coi như không có phiên (Login).

Step 2 — Điều hướng
- Sau thời lượng splash tối thiểu: MainActivity navigate(destination) { popUpTo<SplashRoute>(inclusive) }.
```

## Step-by-step Flow — Đăng ký

```
Step 1 — Gửi form
- Trigger: "Đăng ký", đã đồng ý điều khoản, RegistrationRules qua hết, không có lần gửi đang chạy.
- API: POST /v1/auth/register/request-otp (public)
    - 200 → CodeTiming {expiresIn, resendAfter}; sang Step 2. Email đã có tài khoản trả y hệt (tiêu chí 14).
    - 400 INVALID_* → lỗi dưới field; ở lại.
    - 409 PHONE_TAKEN → tiêu chí 15; ở lại.
    - 429 OTP_RESEND_TOO_SOON / OTP_SEND_LIMITED / RATE_LIMITED → câu báo kèm thời gian chờ/giờ mở khoá; ở lại.
    - 500 EMAIL_SEND_FAILED / 5xx / mất mạng → lỗi chung / không có mạng; ở lại.

Step 2 — Mở màn nhập mã
- navigate(OtpRoute); hai bộ đếm chạy từ response, đo bằng ElapsedClock.

Step 3 — Gửi lại (lặp được)
- Trigger: "Gửi lại mã" sau thời gian chờ.
- API: POST /v1/auth/register/resend-otp {email}
    - 200 → xoá ô nhập, chạy lại hai bộ đếm.
    - 429 OTP_RESEND_TOO_SOON → chạy lại bộ đếm gửi lại từ retry_after_seconds.
    - 429 OTP_SEND_LIMITED → giờ mở khoá; ô nhập vẫn dùng được.
    - 400 PENDING_REGISTRATION_EXPIRED → Step 5.

Step 4 — Xác nhận
- Trigger: đủ 6 số, "Xác nhận", ô nhập không bị khoá.
- API: POST /v1/auth/register/verify-otp {email, otp_code}
    - 201 → AuthSessionStore.save(session) (secure storage + cache token); sang Step 6.
      Lưu thất bại → "Không lưu được phiên đăng nhập" (tài khoản đã có; người dùng đăng nhập lại được).
    - 400 OTP_INVALID → báo sai mã, giữ ô nhập.
    - 400 OTP_EXPIRED → báo hết hạn, nhấn mạnh gửi lại.
    - 429 OTP_LOCKED → giờ mở khoá, tắt ô nhập tới locked_until.
    - 400 PENDING_REGISTRATION_EXPIRED → Step 5.
    - 409 PHONE_TAKEN (bị lấy trong lúc chờ) → về Signup với lỗi ở ô số điện thoại.

Step 5 — Đăng ký hết hạn
- Câu báo của FR-ONB-010; popBackStack về SignupRoute, mọi giá trị giữ nguyên.

Step 6 — Gửi đề xuất chùa (chỉ khi có)
- API: POST /v1/temple-suggestions (public, không token)
    - Mọi kết quả → bỏ qua với người dùng.
- navigate(HomeRoute) { popUpTo<SignupRoute>(inclusive) }.
```

## Local storage

| Dữ liệu | Nơi lưu | Xoá khi đăng xuất? |
|---|---|---|
| `onboarding_completed`, ý hướng, khung giờ chuông | `UserPreferences` (`PreferencesStorage`) | Không |
| Token sau khi xác nhận mã | `AuthSessionStore` → `SecureStorage` `DEVICE_UNLOCKED` (`../Login/`) | Có |
| Form, đề xuất chùa đang giữ | `SignupViewModel` (bộ nhớ, `SavedStateHandle` cho field không nhạy cảm; mật khẩu chỉ trong bộ nhớ) | — |

## Deliberately stubbed for now

- Các route `register/*` theo email mới có trong spec backend; cho tới khi backend ship, Android v1.0
  có thể build và unit test với fake `RegistrationRepository`, E2E chờ backend (innerlamp-be
  `docs/specs/features/onboarding/tasks.md`).
- Điều khoản: giữ nội dung tĩnh hiện tại (BR-ONB-11 phần văn bản dùng chung để sau).

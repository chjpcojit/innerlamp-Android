# Tasks — Onboarding

Version: 1.0
Last updated: 2026-10-14
Status: planned — Phase 1–7 chưa bắt đầu; mục `[x]` đã đọc trong code ngày 2026-10-14 và khớp spec

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
Paths are relative to `app/src/main/java/com/example/innerlamp/`. Phụ thuộc: `AuthSessionStore` của
`../Login/tasks.md` Phase 1 phải có trước Phase 3 (mở app) và Phase 5 (xác nhận mã).

## Phase 1 — Domain

- [ ] `domain/auth/RegistrationRules.kt`: số điện thoại đúng bằng độ dài quốc gia (sửa `PhoneCountry.isComplete` đang `>=`), email, mật khẩu 8–64 ký tự sau NFC, họ tên 1–300, pháp danh 0–255 (tiêu chí 9)
- [x] 12 quốc gia và độ dài số (`domain/model/PhoneCountry.kt` `PhoneCountries`) (tiêu chí 7)
- [x] Gợi ý độ mạnh mật khẩu 3 mức, không chặn gửi (`domain/model/PasswordStrength.kt`) (tiêu chí 10)
- [ ] `RegistrationForm`, `RegistrationFieldError`, `CodeTiming`, `TempleProposal`, `RegistrationError`; bỏ `placeOfOrigin` tự do (tiêu chí 7, 13, 18)
- [ ] `domain/repository/RegistrationRepository.kt`
- [ ] Use case: `ResolveLaunchDestinationUseCase`, `CompleteOnboardingUseCase`, `RequestRegistrationCodeUseCase`, `ResendRegistrationCodeUseCase`, `VerifyRegistrationUseCase` (Step "Mở app" 1, Step "Đăng ký" 1, 3, 4, 6)
- [ ] Unit test chạy `rules/phone-number.json`, `rules/email.json`, `rules/password.json`, `rules/names.json` của innerlamp-be `docs/product/onboarding/` (tiêu chí 29)

## Phase 2 — Giới thiệu và cờ

- [ ] `UserPreferences`: thêm `practiceIntents`, `bellTimes` (tiêu chí 5)
- [x] Cờ `onboarding_completed` trong `UserPreferences` (tiêu chí 4)
- [ ] `OnboardingViewModel.complete(then)` gọi `CompleteOnboardingUseCase` trước khi điều hướng; ba lối thoát đều qua đây (tiêu chí 4)
- [x] 3 bước, "Bỏ qua", thanh tiến độ, "Bắt đầu" / "Tôi đã có tài khoản" / "Quay lại" (`OnboardingScreen`) (tiêu chí 3) — đối chiếu lại câu chữ với product §3.1.1 khi làm Phase 2
- [ ] Unit test: ba lối thoát đều bật cờ; đăng xuất không xoá cờ và lựa chọn (tiêu chí 6)

## Phase 3 — Mở app

- [ ] `SplashViewModel` dùng `ResolveLaunchDestinationUseCase`; `SplashUiState.destination` (tiêu chí 1)
- [ ] Lần đầu cài: `AuthSessionStore.clear()` trước khi điều hướng (tiêu chí 2)
- [ ] `MainActivity`: Splash → đích (pop Splash); Onboarding → Signup / Login (pop Onboarding) (tiêu chí 1, 4)
- [ ] Unit test `ResolveLaunchDestinationUseCase`: có phiên / chưa xem giới thiệu / còn lại

## Phase 4 — Form đăng ký, nơi xuất gia, đề xuất chùa

- [ ] `SignupScreen`: thứ tự field product §3.1.1, thêm họ tên, pháp danh trống ban đầu, nút "Đăng ký", lỗi dưới field, `PHONE_TAKEN` + link Đăng nhập (tiêu chí 7, 15)
- [x] "Gợi ý" pháp danh xoay vòng 10 tên (`SignupViewModel.onSuggestDharmaName`) (tiêu chí 8)
- [ ] `SignupViewModel.submit()`: validate → `RequestRegistrationCodeUseCase`; bỏ qua lần bấm khi đang gửi; giữ mọi giá trị khi lỗi; bỏ giả lập `delay(600)` (tiêu chí 9, 11, 13, 16)
- [ ] Dòng điều khoản giữ nội dung tĩnh hiện tại; đóng thì giữ form (tiêu chí 12)
- [ ] `TemplePickerScreen`: `TempleOptionRepository` theo trang, mục "Chưa xuất gia — Cư sĩ tu tại gia" đầu, tìm debounce, bỏ kết quả cũ (tiêu chí 17)
- [x] `TempleOptionRepository` + `TempleOptionRepositoryImpl` + `di/TempleModule.kt` (tiêu chí 17)
- [ ] `TempleProposalScreen`: tên, tỉnh, phường/xã, địa chỉ đường; giữ trong `SignupViewModel` (tiêu chí 18)
- [x] `AdministrativeUnitRepository` + impl + `di/AdministrativeUnitModule.kt` (tiêu chí 18)
- [ ] Route `TemplePickerRoute`, `TempleProposalRoute` lồng dưới `SignupRoute`, dùng chung `SignupViewModel`
- [ ] Unit test: form lỗi không gọi repository; body không có `dharma_name` / `ordination_temple_id` khi trống; email có tài khoản xử lý như email mới (tiêu chí 13, 14)

## Phase 5 — Data đăng ký và màn nhập mã

- [ ] `RegistrationApiService`, `RegistrationDto` + mapper, `RegistrationRepositoryImpl` qua `safeCall`, map lỗi theo Error → UI (Step "Đăng ký" 1, 3, 4, 6)
- [ ] `di/RegistrationModule.kt` + ViewModel factory cho Splash / Onboarding / Signup / Otp (thay `viewModel()` không factory)
- [ ] `OtpRoute` + `OtpScreen` + state: email, 6 ô số, đếm ngược hiệu lực và chờ gửi lại theo `ElapsedClock`, khoá theo `locked_until` (tiêu chí 20, 22, 24)
- [ ] `ui/components/auth/CodeTimerRow.kt` dùng chung với quên mật khẩu (tiêu chí 20, 23)
- [ ] Xác nhận: `VerifyRegistrationUseCase` → `AuthSessionStore.save` → đề xuất chùa (bỏ qua kết quả) → `HomeRoute` pop cả luồng (tiêu chí 19, 21)
- [ ] Gửi lại, "Đổi email", đăng ký hết hạn → pop về Signup giữ giá trị (tiêu chí 23, 25, 26)
- [ ] Unit test: từng lỗi của Step 3–4; lưu phiên thất bại; đề xuất chùa thất bại không chặn vào Home

## Phase 6 — Câu chữ và log

- [ ] Câu chữ theo product §3.1.1; bỏ mọi câu "SMS" / "OTP qua số điện thoại"; không dùng `error.message` của backend (tiêu chí 27)
- [ ] `LoggingInterceptor`: không log body của `/v1/auth/*` kể cả bản debug (tiêu chí 28)

## Phase 7 — QA và tài liệu

- [ ] Chạy tay với backend dev: lần đầu → giới thiệu → Signup; tắt app mở lại → Login; đăng ký đủ field; email đã có tài khoản; `PHONE_TAKEN`; mã sai / hết hạn / khoá; gửi lại; đổi email; đề xuất chùa
- [ ] Cập nhật cột Android ở product §4.2 khi xong (báo `ba-writer`)

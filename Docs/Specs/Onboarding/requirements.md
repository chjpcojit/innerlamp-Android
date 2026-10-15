# Requirements — Onboarding

Version: 1.0
Last updated: 2026-10-14
Status: planned (drift) — splash, 3 bước giới thiệu, form Đăng ký đã có UI nhưng chưa gọi backend (`SignupViewModel` giả lập thành công); chưa có màn nhập mã, chưa có đề xuất chùa, chưa lưu cờ giới thiệu; launch luôn vào giới thiệu
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/{splash,onboarding,signup}/` (+ `otp/` mới)
Product spec: innerlamp-be `docs/product/onboarding/product.md` @ v1.2
Contract: innerlamp-be `docs/specs/features/onboarding/requirements.md` @ v8.0 §API contract (47122d1). `openapi.yaml` chưa có shape email — backend cập nhật khi handler ship.
Architecture: ADR-0003 (tài khoản định danh bằng email, xác thực bằng mã gửi qua email; bỏ SMS) — Accepted
Implements: BR-ONB-01, BR-ONB-02, BR-ONB-03, BR-ONB-04, BR-ONB-05, BR-ONB-06, BR-ONB-07, BR-ONB-08, BR-ONB-09, BR-ONB-10, BR-ONB-11, FR-ONB-001, FR-ONB-002, FR-ONB-003, FR-ONB-004, FR-ONB-005, FR-ONB-006, FR-ONB-007, FR-ONB-008, FR-ONB-009, FR-ONB-010, NFR-ONB-01, NFR-ONB-02, NFR-ONB-03
Not on Android: dòng website của FR-ONB-003 ở product §3.6 (khách đã đăng nhập mở trang đăng ký)

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của lần mở app đầu tiên và đăng ký trên Android. Hành vi sản phẩm — khi nào
hiện giới thiệu, điều hướng lúc mở app, luật từng field, danh sách 12 quốc gia, chính sách mã, việc
email đã có tài khoản trông như email mới, điều khoản — nằm ở product spec và **không** nhắc lại ở
đây; file này chỉ nói app Android hiện thực các `BR-ONB-*` / `FR-ONB-*` thế nào.

Spec viết **prospective**: Android chưa có spec nào cho splash/giới thiệu/đăng ký trước đây. Code UI
hiện có được đọc ngày 2026-10-14 để ghi những gì giữ lại và những gì lệch product (bảng "Lệch hiện
tại" cuối file). Đăng nhập, phiên, đăng xuất, quên mật khẩu, khách → `../Login/`.

## User Story

Là người mở InnerLamp trên điện thoại Android lần đầu, tôi muốn xem phần giới thiệu ngắn rồi tạo tài
khoản bằng email, mật khẩu, số điện thoại và tên, xác nhận bằng mã gửi tới email, để được đăng nhập
và hành trình tu tập của tôi được giữ trên mọi thiết bị.

## Acceptance Criteria (EARS notation)

### Splash, giới thiệu và điều hướng lúc mở app

1. WHEN app khởi động, THE SYSTEM SHALL hiện splash rồi tự chuyển; sau đó mở `HomeRoute` nếu có phiên đã lưu, nếu không thì mở `OnboardingRoute` khi cờ `onboarding_completed` chưa bật, nếu không thì mở **`LoginRoute`** làm gốc. Splash đọc cờ và phiên qua use case, không còn `delay` thuần quyết định đích. (FR-ONB-002, BR-ONB-02)
2. WHEN app khởi động lần đầu sau khi cài (cờ `onboarding_completed` chưa có), THE SYSTEM SHALL xoá mọi token còn sót trong secure storage trước khi điều hướng. (BR-ONB-01, platform — phòng trường hợp backup/restore mang file prefs bảo mật sang)
3. WHEN phần giới thiệu hiện, THE SYSTEM SHALL hiện 3 bước theo product §3.1.1 với "Bỏ qua" và thanh tiến độ; bước 1 có "Bắt đầu" và "Tôi đã có tài khoản", bước 2–3 có "Quay lại". (FR-ONB-001)
4. WHEN người dùng chọn "Tạo tài khoản" ở bước 3, "Bỏ qua" ở bất kỳ bước nào, hoặc "Tôi đã có tài khoản" ở bước 1, THE SYSTEM SHALL bật cờ `onboarding_completed` **trước** khi rời giới thiệu; "Tôi đã có tài khoản" mở `LoginRoute`, hai lựa chọn còn lại mở `SignupRoute`; `OnboardingRoute` bị bỏ khỏi back stack. (FR-ONB-001, BR-ONB-01)
5. WHEN người dùng chọn ý hướng (bước 2) hoặc khung giờ chuông (bước 3), THE SYSTEM SHALL lưu chúng vào `UserPreferences` trên máy và SHALL NOT gửi lên backend. (BR-ONB-03)
6. WHEN người dùng đăng xuất hoặc phiên hết hạn, THE SYSTEM SHALL giữ cờ `onboarding_completed` và các lựa chọn giới thiệu. (BR-ONB-01, `login` BR-LGN-07)

### Form đăng ký (Signup)

7. WHEN `SignupRoute` mở, THE SYSTEM SHALL hiện theo thứ tự product §3.1.1: họ tên (trống), pháp danh (**trống**, có "Gợi ý"), nơi xuất gia, số điện thoại (quốc gia + số), email kèm gợi ý dùng để đăng nhập và nhận mã, mật khẩu (nút hiện/ẩn + gợi ý độ mạnh) và dòng điều khoản; nút là "Đăng ký", có link sang Đăng nhập. (FR-ONB-003, BR-ONB-07)
8. WHEN người dùng bấm "Gợi ý", THE SYSTEM SHALL điền pháp danh kế tiếp trong 10 tên gợi ý; người dùng giữ, sửa hoặc xoá tuỳ ý. (BR-ONB-07)
9. WHEN người dùng gửi form, THE SYSTEM SHALL kiểm mọi field bằng luật BR-ONB-04…07 — số điện thoại đúng **bằng** độ dài của quốc gia sau khi bỏ khoảng trắng, dấu chấm, gạch và một số 0 đầu; email đã trim và khớp mẫu; mật khẩu 8–64 **ký tự** sau NFC; họ tên 1–300 và pháp danh 0–255 ký tự sau trim — và khi có lỗi thì hiện dưới field, giữ mọi giá trị, SHALL NOT gọi backend. (FR-ONB-003, BR-ONB-04, BR-ONB-05, BR-ONB-06, BR-ONB-07)
10. WHEN người dùng gõ mật khẩu, THE SYSTEM SHALL hiện gợi ý độ mạnh 3 mức chỉ để tham khảo; nó SHALL NOT chặn việc gửi form. (BR-ONB-06, §3.6)
11. WHILE chưa đồng ý điều khoản, THE SYSTEM SHALL để "Đăng ký" ở trạng thái tắt. (BR-ONB-11)
12. WHEN người dùng mở điều khoản sử dụng hoặc chính sách riêng tư từ dòng điều khoản, THE SYSTEM SHALL giữ cách hiện tại (nội dung tĩnh trong app) và khi đóng thì quay lại Signup với mọi giá trị và ô đồng ý không đổi. Chuyển sang một văn bản dùng chung (BR-ONB-11) để sau. (BR-ONB-11, platform)
13. WHEN người dùng gửi form hợp lệ, THE SYSTEM SHALL gọi `POST /v1/auth/register/request-otp` với email (đã trim), mật khẩu, `phone_country` (mã ISO của quốc gia đã chọn) và `phone_number` (chữ số như đã gõ, không dựng lại E.164), họ tên, pháp danh chỉ khi không trống và `ordination_temple_id` chỉ khi chọn một chùa trong danh sách, rồi:
    - thành công: mở `OtpRoute` với email và `expires_in_seconds` / `resend_after_seconds` của server;
    - thất bại: ở lại Signup, giữ mọi giá trị, báo lỗi theo bảng Error → UI. (FR-ONB-003, BR-ONB-08, BR-ONB-09)
14. WHEN email đã có tài khoản, THE SYSTEM SHALL xử lý y hệt email mới (backend trả lời giống nhau); không còn câu "email đã được dùng". (BR-ONB-08, NFR-ONB-01)
15. WHEN backend trả `PHONE_TAKEN`, THE SYSTEM SHALL hiện "Số điện thoại này đã được đăng ký" dưới ô số điện thoại kèm link sang Đăng nhập, và ở lại. (FR-ONB-005)
16. WHEN một lần gửi đang chạy, THE SYSTEM SHALL bỏ qua lần bấm tiếp theo. (platform)

### Nơi xuất gia và đề xuất chùa

17. WHEN người dùng mở ô nơi xuất gia, THE SYSTEM SHALL liệt kê `GET /v1/temples/options` theo trang (qua `TempleOptionRepository` sẵn có) với "Chưa xuất gia — Cư sĩ tu tại gia" đứng đầu và tìm theo tên phía server (debounce; kết quả cũ về muộn bị bỏ). Ô nhập tự do hiện tại bị thay bằng bộ chọn này. (FR-ONB-004, BR-ONB-10)
18. WHEN người dùng đề xuất một chùa chưa có, THE SYSTEM SHALL yêu cầu tên chùa, tỉnh và phường/xã chọn từ danh sách đơn vị hành chính (`AdministrativeUnitRepository` sẵn có), dòng địa chỉ đường tuỳ chọn; đề xuất được giữ cùng phiên đăng ký, không bao giờ gửi như `ordination_temple_id`. (FR-ONB-006, BR-ONB-10)
19. WHEN tài khoản vừa được tạo và có đề xuất đang giữ, THE SYSTEM SHALL gửi một lần `POST /v1/temple-suggestions` (public, không token); thất bại SHALL NOT ảnh hưởng tới đăng ký và không hiện thành lỗi. (FR-ONB-006)

### Mã xác thực qua email (Otp)

20. WHEN màn nhập mã mở, THE SYSTEM SHALL hiện "Đã gửi tới <email>", ô nhập 6 chữ số, đếm ngược thời gian mã còn hiệu lực (từ `expires_in_seconds`, đo bằng elapsed time) và "Gửi lại mã" bị tắt kèm số giây còn chờ (từ `resend_after_seconds`). (FR-ONB-007, FR-ONB-008, BR-ONB-09)
21. WHEN người dùng nhập đủ 6 chữ số và xác nhận, THE SYSTEM SHALL gọi `POST /v1/auth/register/verify-otp` với email và mã; thành công thì lưu phiên như đăng nhập (`../Login/` tiêu chí 8), gửi đề xuất chùa (tiêu chí 19) và mở `HomeRoute`, xoá cả luồng đăng ký khỏi back stack. (FR-ONB-007)
22. WHEN đếm ngược về 0, THE SYSTEM SHALL báo mã đã hết hạn và chỉ tới "Gửi lại mã"; app không tự giả định thời lượng nào. (FR-ONB-008, BR-ONB-09)
23. WHEN người dùng bấm "Gửi lại mã" sau thời gian chờ, THE SYSTEM SHALL gọi `POST /v1/auth/register/resend-otp` với email, xoá ô nhập và khởi động lại cả hai bộ đếm từ response. (FR-ONB-008)
24. WHEN backend trả `OTP_LOCKED` hoặc `OTP_SEND_LIMITED`, THE SYSTEM SHALL hiện giờ mở khoá từ `locked_until` theo giờ địa phương của máy; `OTP_LOCKED` tắt ô nhập tới lúc đó, `OTP_SEND_LIMITED` vẫn cho nhập mã cuối cùng. (FR-ONB-007, FR-ONB-008, BR-ONB-09)
25. WHEN người dùng chọn "Đổi email", THE SYSTEM SHALL quay lại Signup với mọi giá trị như cũ, kể cả mật khẩu. (FR-ONB-009)
26. WHEN backend trả `PENDING_REGISTRATION_EXPIRED`, THE SYSTEM SHALL hiện "Thời gian đăng ký đã hết. Vui lòng đăng ký lại từ đầu." và quay lại Signup với mọi giá trị như cũ. (FR-ONB-010)

### Chung

27. WHEN hiện lỗi, THE SYSTEM SHALL chỉ dùng câu chữ của app (theo product §3.1.1), không bao giờ dùng `error.message` của backend; `RATE_LIMITED` hiện "thử lại sau" kèm thời gian chờ từ `retry_after_seconds`. (NFR-ONB-02)
28. WHEN app log request hoặc lỗi (kể cả `LoggingInterceptor` ở bản debug), THE SYSTEM SHALL không bao giờ log mật khẩu, mã hay token. (NFR-ONB-03)
29. WHEN unit test chạy, THE SYSTEM SHALL qua mọi case của `rules/phone-number.json`, `rules/email.json`, `rules/password.json` và `rules/names.json` bằng validator của app. (BR-ONB-04…07)

## Dữ liệu & API

| Hành động | Operation | Ghi chú |
|---|---|---|
| Gửi form | `POST /v1/auth/register/request-otp` | Public; email + `phone_country` |
| Gửi lại mã | `POST /v1/auth/register/resend-otp` | Public |
| Xác nhận mã | `POST /v1/auth/register/verify-otp` | Public; trả phiên như đăng nhập |
| Danh sách chùa | `GET /v1/temples/options` | Đã có `TempleApiService` |
| Tỉnh / phường | các route đơn vị hành chính | Đã có `AdministrativeUnitApiService` |
| Đề xuất chùa | `POST /v1/temple-suggestions` | Public, đã chạy ở backend (temple-suggestion v3.4 US-TSG-01) |

Shape request/response: contract ở header. File này không chép schema.

## Error → UI mapping

| Trường hợp | Người dùng thấy |
|---|---|
| `INVALID_EMAIL` / `INVALID_PHONE` / `INVALID_PASSWORD` / `INVALID_FULL_NAME` / `INVALID_DHARMA_NAME` | Lỗi dưới field tương ứng |
| `INVALID_TEMPLE` | Lỗi ở ô nơi xuất gia, yêu cầu chọn lại |
| `VALIDATION_ERROR` | Câu báo dữ liệu không hợp lệ chung |
| `PHONE_TAKEN` | Tiêu chí 15 |
| `OTP_RESEND_TOO_SOON` | Bộ đếm gửi lại chạy lại từ `retry_after_seconds` |
| `OTP_SEND_LIMITED` | Giờ mở khoá; mã cuối vẫn dùng được (tiêu chí 24) |
| `OTP_INVALID` | "Mã xác thực không đúng" |
| `OTP_EXPIRED` | Báo hết hạn, nhấn mạnh "Gửi lại mã" |
| `OTP_LOCKED` | Giờ mở khoá, tắt ô nhập tới lúc đó (tiêu chí 24) |
| `PENDING_REGISTRATION_EXPIRED` | Tiêu chí 26 |
| `RATE_LIMITED` | Thử lại sau, kèm thời gian chờ |
| `EMAIL_SEND_FAILED`, 5xx | Lỗi chung |
| Mất mạng / timeout | Câu báo không có mạng / thử lại dùng chung (`AppError` của Networking) |

## Lệch hiện tại (code đọc ngày 2026-10-14)

| Chỗ | Hiện tại | Theo spec này |
|---|---|---|
| `SplashViewModel` + `MainActivity` | Splash luôn sang `OnboardingRoute` | Tiêu chí 1–2 |
| `OnboardingViewModel` | Lựa chọn chỉ trong bộ nhớ, không bật cờ | Tiêu chí 4–5 |
| `SignupUiModel` | Không có họ tên; "Nơi xuất gia" là ô chữ tự do | Tiêu chí 7, 17 |
| `SignupViewModel` | Email chỉ kiểm "@" và "."; mật khẩu ≥ 8; số điện thoại ≥ độ dài quốc gia; giả lập thành công | Tiêu chí 9, 13 |
| `SignupScreen` | Câu chữ cũ theo SMS / số điện thoại | Tiêu chí 7, 27 |
| Màn nhập mã | Chưa có | Tiêu chí 20–26 |

## Out of scope

- Đăng nhập, phiên, đăng xuất, quên mật khẩu, khách → `../Login/`.
- Đổi email hoặc số điện thoại, xoá tài khoản, đăng nhập Google → để sau (product §1.2).
- Lên lịch thông báo cho chuông ở bước giới thiệu → không thuộc product spec này.

## Pending (not yet implemented)

Toàn bộ — `tasks.md` Phase 1–7.

> Để sau (anh Đức, 2026-10-14): phần "một văn bản dùng chung" của BR-ONB-11; Signup giữ nội dung điều khoản tĩnh hiện tại cho tới khi làm.

## Open questions (resolved)

- ~~Tên và cách gom thư mục?~~ → Một spec `Onboarding/` theo slug của product (splash + giới thiệu + đăng ký + mã + đề xuất chùa), giống iOS v2.0. Mặc định của Claude, review cùng PR này. Resolved 2026-10-14.
- ~~Điều khoản lấy từ đâu?~~ → Để sau, giữ nội dung tĩnh hiện tại (anh Đức: "bỏ qua phần điều khoản, chúng ta sẽ làm sau"). Resolved 2026-10-14.
- ~~Backend nhận đề xuất chùa chưa?~~ → Rồi: `POST /v1/temple-suggestions` đã chạy, public (không token, giới hạn theo địa chỉ máy), backend temple-suggestion v3.4 US-TSG-01. Android gửi (tiêu chí 19). Resolved 2026-10-14.
- ~~Điều hướng lúc mở app?~~ → Lần đầu: giới thiệu, bật cờ, sang Signup. Các lần sau (tắt app mở lại) chưa có phiên: Login (anh Đức, 2026-10-14; tiêu chí 1, 4). Resolved 2026-10-14.
- ~~Giữ mật khẩu khi "Đổi email" / đăng ký hết hạn?~~ → Giữ mọi field (FR-ONB-009, FR-ONB-010). Resolved 2026-10-14.
- ~~Đếm ngược thật hay chữ "còn hiệu lực N phút" cố định?~~ → Đếm ngược thời gian còn lại (FR-ONB-008). Resolved 2026-10-14.

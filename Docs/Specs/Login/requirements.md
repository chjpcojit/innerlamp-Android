# Requirements — Login

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — màn Đăng nhập có UI theo số điện thoại và giả lập thành công; chưa có lưu phiên, refresh, đăng xuất, quên mật khẩu; "Vào xem trước" vào Home như đã đăng nhập
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/login/` (+ `forgotpassword/` mới), `core/network/auth/`, `MainActivity.kt` (điều hướng khi phiên kết thúc), `ui/screens/profile/` (đăng xuất)
Product spec: innerlamp-be `docs/product/login/product.md` @ v1.2
Contract: innerlamp-be `docs/specs/features/login/requirements.md` @ v2.2 §API contract (47122d1); refresh và logout không đổi so với `openapi.yaml`. `openapi.yaml` chưa có đăng nhập bằng email và `password-reset/*` — backend cập nhật khi handler ship.
Architecture: ADR-0003 — Accepted
Implements: BR-LGN-01, BR-LGN-02, BR-LGN-03, BR-LGN-04, BR-LGN-05, BR-LGN-06, BR-LGN-07, FR-LGN-001, FR-LGN-002, FR-LGN-003, FR-LGN-004, FR-LGN-005, FR-LGN-006, FR-LGN-008, FR-LGN-009, FR-LGN-010, NFR-LGN-01, NFR-LGN-02, NFR-LGN-03
Not on Android: FR-LGN-007 (chỉ website)

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của đăng nhập, phiên, đăng xuất, quên mật khẩu và chế độ khách trên Android.
Các luật — câu báo sai thông tin dùng chung, khoá sau 5 lần sai, phiên 7 ngày trượt, chính sách đặt lại
mật khẩu, khách được mở gì — nằm ở product spec và **không** nhắc lại ở đây.

Spec này cũng là spec **`AuthTokenStorage`** mà `Docs/Specs/Networking/` và `Docs/Specs/Storage/`
hẹn "làm sau": lưu token bằng `SecureStorage`, cung cấp token cho `AuthInterceptor`, và luồng refresh
khi `401`.

## User Story

Là thành viên, tôi muốn đăng nhập bằng email và mật khẩu, được giữ đăng nhập, được báo khi phiên đã
hết, và đặt lại mật khẩu đã quên bằng mã gửi tới email — hoặc xem trước như khách — để tự dùng
InnerLamp trên điện thoại Android.

## Acceptance Criteria (EARS notation)

### Đăng nhập

1. WHEN `LoginRoute` mở, THE SYSTEM SHALL hiện ô email, ô mật khẩu có "Hiện/Ẩn", "Đăng nhập", "Quên mật khẩu?", "Tạo tài khoản mới" và "Vào xem trước, chưa đăng nhập"; không còn ô số điện thoại, bộ chọn quốc gia và "Gửi mã một lần qua SMS". (FR-LGN-001, FR-LGN-010)
2. WHEN người dùng bấm đăng nhập với email trống hoặc mật khẩu trống, THE SYSTEM SHALL báo lỗi dưới field đó và SHALL NOT gọi backend; ở đây không áp luật độ dài mật khẩu (bỏ điều kiện ≥ 8 hiện tại). (BR-LGN-01)
3. WHEN người dùng gửi, THE SYSTEM SHALL gọi `POST /v1/auth/login` với email đã trim, mật khẩu và `device {platform: "android", device_model, os_version, app_version}`, rồi:
   - thành công: lưu phiên (tiêu chí 8) và mở `HomeRoute`, xoá luồng đăng nhập khỏi back stack;
   - thất bại: ở lại, giữ email, xoá mật khẩu, báo lỗi theo bảng Error → UI. (FR-LGN-001, FR-LGN-002)
4. WHEN backend trả `INVALID_CREDENTIALS`, THE SYSTEM SHALL hiện "Email hoặc mật khẩu không đúng". (FR-LGN-002, BR-LGN-01, NFR-LGN-01)
5. WHEN backend trả `LOGIN_LOCKED`, THE SYSTEM SHALL hiện câu báo khoá kèm giờ mở khoá từ `locked_until` theo giờ địa phương của máy. (FR-LGN-003, BR-LGN-02)
6. WHEN một lần đăng nhập đang chạy, THE SYSTEM SHALL bỏ qua lần bấm tiếp theo. (platform)
7. WHEN Login là gốc (mở app chưa có phiên, sau đăng xuất, phiên hết), THE SYSTEM SHALL để Back thoát app; khi mở từ Signup thì giữ cách hoán đổi Signup ↔ Login hiện tại. (FR-ONB-002, platform)

### Phiên

8. WHEN đăng nhập hoặc đăng ký thành công, THE SYSTEM SHALL lưu access token và refresh token chỉ trong `SecureStorage` ở `SecureLevel.DEVICE_UNLOCKED` (AES-256-GCM, khoá trong AndroidKeyStore, file `innerlamp_secure` bị loại khỏi cloud backup và device transfer), không bao giờ ở `PreferencesStorage` hay Room; thông tin người dùng trả về (id, họ tên, pháp danh) lưu ở `PreferencesStorage` dưới key thuộc tài khoản, xoá khi đăng xuất. (NFR-LGN-03, BR-LGN-03)
9. WHEN app mở với phiên đã lưu, THE SYSTEM SHALL vào `HomeRoute` không hỏi lại mật khẩu (`../Onboarding/` tiêu chí 1). (FR-LGN-004)
10. WHEN một lời gọi có `@Authenticated` nhận `401`, THE SYSTEM SHALL refresh một lần (single-flight giữa các lời gọi đồng thời, cặp token mới được lưu trước khi gọi lại) rồi gọi lại; refresh bị mất mạng / timeout / 5xx thì giữ phiên để thử lần sau. (FR-LGN-004, BR-LGN-03)
11. WHEN refresh bị từ chối (`REFRESH_TOKEN_INVALID`) hoặc phiên từng có nhưng không còn refresh token, THE SYSTEM SHALL xoá token và mọi dữ liệu của tài khoản trên máy (giống đăng xuất), mở `LoginRoute` làm gốc và hiện "Phiên đăng nhập đã hết, vui lòng đăng nhập lại."; SHALL NOT âm thầm đẩy người dùng ra. (FR-LGN-005, BR-LGN-04)
12. WHEN khách (chưa từng lưu phiên) gặp `401`, THE SYSTEM SHALL NOT coi đó là phiên kết thúc (không dọn dữ liệu, không hiện câu báo); áp dụng lời mời đăng nhập của tiêu chí 21. (BR-LGN-06, platform)

### Đăng xuất

13. WHEN người dùng chọn "Đăng xuất" ở màn Hồ sơ, THE SYSTEM SHALL hỏi xác nhận; huỷ thì giữ phiên; xác nhận thì gọi `POST /v1/auth/logout` (bỏ qua kết quả), xoá token và dữ liệu của tài khoản trên máy, giữ thiết lập của máy (`onboarding_completed`, lựa chọn giới thiệu) và mở `LoginRoute` làm gốc, kể cả khi mất mạng. (FR-LGN-006, BR-LGN-07)

### Quên mật khẩu

14. WHEN người dùng chọn "Quên mật khẩu?", THE SYSTEM SHALL mở màn yêu cầu với ô email (điền sẵn nếu đã gõ ở Login), "Gửi mã" và "Trở lại đăng nhập". (FR-LGN-008)
15. WHEN người dùng gửi email hợp lệ, THE SYSTEM SHALL gọi `POST /v1/auth/password-reset/request-otp` và, với bất kỳ email nào, mở màn mật khẩu mới với "Nếu email này có tài khoản, mã đã được gửi", email, đếm ngược hiệu lực của mã và thời gian chờ gửi lại, cả hai lấy từ response. (FR-LGN-008, BR-LGN-05, NFR-LGN-01)
16. WHEN người dùng bấm "Gửi lại mã" sau thời gian chờ, THE SYSTEM SHALL gọi lại cùng yêu cầu, xoá mã đã nhập và chạy lại hai bộ đếm. (FR-LGN-008)
17. WHEN người dùng nhập đủ 6 số và mật khẩu mới, THE SYSTEM SHALL kiểm mật khẩu bằng luật BR-ONB-06 (validator của `../Onboarding/`) rồi gọi `POST /v1/auth/password-reset/confirm`; thành công thì nếu máy đang có phiên sẽ dọn như tiêu chí 13 (không gọi logout), và mở `LoginRoute` làm gốc với "Đã đổi mật khẩu, vui lòng đăng nhập lại." và email điền sẵn. (FR-LGN-009, BR-LGN-05)
18. WHEN xác nhận thất bại, THE SYSTEM SHALL ở lại màn mật khẩu mới, giữ mã, xoá mật khẩu mới và báo lỗi; `OTP_LOCKED` tắt ô nhập tới `locked_until`. (FR-LGN-009)
19. WHEN mật khẩu được đặt lại trên máy khác, THE SYSTEM SHALL xử lý `401` kế tiếp trên máy này theo tiêu chí 10–11. (FR-LGN-005, FR-LGN-009)

### Khách

20. WHEN người dùng chọn "Vào xem trước, chưa đăng nhập", THE SYSTEM SHALL mở `HomeRoute` ở chế độ khách (không lưu phiên); nội dung public mở bình thường. (FR-LGN-010, BR-LGN-06)
21. WHEN khách mở thứ thuộc về tài khoản (sổ giỗ, gia phả, ghi chép tu tập, hồ sơ, thông báo), THE SYSTEM SHALL hiện lời mời đăng nhập dùng chung (giải thích, "Đăng nhập", "Tạo tài khoản") thay cho màn đó và SHALL NOT gọi backend cho nó. (FR-LGN-010, BR-LGN-06)

### Chung

22. WHEN hiện lỗi, THE SYSTEM SHALL chỉ dùng câu chữ của app (theo product §3.1.1), không bao giờ dùng `error.message` của backend; `RATE_LIMITED` hiện "thử lại sau" kèm thời gian chờ từ `retry_after_seconds`. (NFR-LGN-02)
23. WHEN app log request hoặc lỗi (kể cả `LoggingInterceptor` ở bản debug), THE SYSTEM SHALL không bao giờ log mật khẩu, mã, token hay header `Authorization`. (NFR-LGN-03)

## Dữ liệu & API

| Hành động | Operation | Ghi chú |
|---|---|---|
| Đăng nhập | `POST /v1/auth/login` | Public; email + mật khẩu + device |
| Refresh | `POST /v1/auth/refresh` | Public; gọi bằng client không gắn Bearer |
| Đăng xuất | `POST /v1/auth/logout` | `@Authenticated` |
| Xin mã đặt lại | `POST /v1/auth/password-reset/request-otp` | Public, mới |
| Đặt mật khẩu mới | `POST /v1/auth/password-reset/confirm` | Public, mới |

Shape request/response: contract ở header. File này không chép schema.

## Error → UI mapping

| Trường hợp | Người dùng thấy |
|---|---|
| `INVALID_CREDENTIALS` | "Email hoặc mật khẩu không đúng" |
| `LOGIN_LOCKED` | Câu báo khoá kèm giờ mở khoá |
| `INVALID_EMAIL` | Lỗi dưới ô email |
| `INVALID_PASSWORD` (đặt lại) | Lỗi dưới ô mật khẩu mới |
| `OTP_INVALID` / `OTP_EXPIRED` / `OTP_LOCKED` / `OTP_RESEND_TOO_SOON` / `OTP_SEND_LIMITED` | Như màn mã đăng ký (`../Onboarding/requirements.md` Error → UI) |
| `RATE_LIMITED` | Thử lại sau, kèm thời gian chờ |
| `REFRESH_TOKEN_INVALID`, `401` sau refresh | Phiên kết thúc (tiêu chí 11) |
| `INVALID_DEVICE`, 5xx | Lỗi chung |
| Mất mạng / timeout | Câu báo không có mạng / thử lại dùng chung (`AppError` của Networking) |

## Lệch hiện tại (code đọc ngày 2026-10-14)

| Chỗ | Hiện tại | Theo spec này |
|---|---|---|
| `LoginUiState` / `LoginScreen` | Số điện thoại + quốc gia, nút SMS, mật khẩu ≥ 8 | Tiêu chí 1–2 |
| `LoginViewModel.onLoginClick` | Giả lập thành công | Tiêu chí 3–6 |
| `NetworkModule.provideAuthTokenProvider` | `InMemoryAuthTokenProvider`, không có nguồn token thật | Tiêu chí 8–9 |
| `AuthInterceptor` | Gắn Bearer; `TODO(refresh-token)` | Tiêu chí 10–12 |
| Đăng xuất | Không có | Tiêu chí 13 |
| `MainActivity` `onForgotPassword` | TODO | Tiêu chí 14–18 |
| "Vào xem trước" | Vào Home như đã đăng nhập (trước ghi ở `Docs/home_assumptions.md` #5, đã bỏ ở v1.1) | Tiêu chí 20–21 |

## Out of scope

- Đăng ký và mã xác thực đăng ký → `../Onboarding/`.
- Đăng nhập bằng mã, Google, đăng xuất mọi thiết bị, đổi mật khẩu khi đang đăng nhập → để sau (product §1.2).
- Cách từng feature xử lý khách ngoài việc hiện lời mời dùng chung (vd `Docs/Specs/Memorial/`).
- Mở khoá app bằng sinh trắc học (`SecureLevel.USER_AUTH`) → không thuộc product spec này.

## Pending (not yet implemented)

Toàn bộ — `tasks.md` Phase 1–7.

## Open questions (resolved)

- ~~Tên và cách gom thư mục?~~ → Một spec `Login/` theo slug của product, gồm luôn quên mật khẩu và phần `AuthTokenStorage` mà Networking/Storage hẹn. Mặc định của Claude, review cùng PR này. Resolved 2026-10-14.
- ~~Nút "Đăng xuất" đặt ở đâu?~~ → Cuối màn Hồ sơ (`ProfileScreen`), tương đương tab "Tôi" trên iOS. Mặc định của Claude, review cùng PR này. Resolved 2026-10-14.
- ~~Giữ mật khẩu sau khi đăng nhập thất bại?~~ → Xoá mật khẩu, giữ email (product §3.7). Resolved 2026-10-14.
- ~~"Gửi mã một lần qua SMS"?~~ → Bỏ (không có đăng nhập bằng mã). Resolved 2026-10-14.
- ~~`401` của khách có phải phiên kết thúc?~~ → Không; khách chưa từng có phiên nên áp dụng lời mời đăng nhập (tiêu chí 12, 21). Resolved 2026-10-14.
- ~~Đặt lại xong có tự đăng nhập?~~ → Không (BR-LGN-05); mở Login với email điền sẵn. Resolved 2026-10-14.
- ~~Câu báo phiên hết cần UI riêng?~~ → Có; prototype chưa có, chốt giao diện với anh Đức trước khi làm (anh Đức, 2026-10-14: "sẽ cần thêm UI"). Resolved 2026-10-14.

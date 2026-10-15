# Changelog — Networking Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v2.4 (2026-10-15)

- Trỏ spec `AuthTokenStorage` (trước ghi "chưa tạo, làm sau") về `Docs/Specs/Login/` v1.0: lưu token
  (`AuthSessionStore`, `SessionTokenCache` thay `InMemoryAuthTokenProvider`) và refresh khi 401 bằng
  OkHttp `Authenticator` single-flight. Không đổi hành vi hay task của Networking; chỉ đổi chỗ trỏ
  trong `requirements.md`, `design.md`, `tasks.md`.

## v2.3 (2026-09-22)

- `requirements.md`: đổi tiêu chí base URL sang guard "rỗng thì `NotConfigured`" và nêu rõ giá trị
  dùng là **URL fake** đã chốt; thêm câu hỏi giá trị URL vào "Open questions (resolved)"; **không còn
  `⚠️ Not confirmed`** nào; cập nhật Out of scope (backend thật/URL thật).
- `design.md`: bảng flavor ghi rõ URL là fake nhưng là cấu hình thật (fail bằng `NoConnection`, không
  phải `NotConfigured`); cập nhật mục "Deliberately stubbed" cho `baseUrl`; bỏ `⚠️` cuối file.
- `tasks.md`: Phase 6 tách rõ test URL rỗng (`NotConfigured`) và thay URL fake bằng URL thật (task
  thường, không còn bị block).
- Reason: người dùng chốt dùng URL fake cho `dev`/`staging`/`prod` và để phần token storage làm sau.
  Giải quyết câu hỏi mở cuối cùng nên bump minor; cascade đầy đủ.

## v2.2 (2026-09-22)

- `requirements.md`: thay tiêu chí 401 đơn lẻ bằng 5 tiêu chí EARS về auth (opt-in `@Authenticated`,
  gắn `Bearer`, token `null` không crash, 401 → `Unauthorized`, `AuthTokenProvider` đọc cache in-memory,
  port nằm ở `core/network`); chuyển câu hỏi auth sang "Open questions (resolved)"; thêm token storage
  vào Out of scope (thuộc spec riêng `AuthTokenStorage`). `⚠️` còn lại chỉ là giá trị base URL thật.
- `design.md`: thêm 3 component `Authenticated.kt`, `AuthTokenProvider.kt`, `InMemoryAuthTokenProvider.kt`;
  viết lại row `AuthInterceptor` (đọc `Invocation` + annotation); thêm mục "Auth flow — per-endpoint
  opt-in" với sơ đồ; cập nhật "Deliberately stubbed" (refresh-token còn trống, provider dùng impl tạm).
- `tasks.md`: Phase 2 thay task no-op hook bằng task annotation + port + impl tạm + interceptor thật;
  Phase 6 thêm test `AuthInterceptor`; Deferred nêu rõ `AuthTokenStorage` (AndroidKeyStore) là spec riêng.
- Reason: người dùng chốt cơ chế token — port `AuthTokenProvider` + DI, endpoint opt-in bằng
  `@Authenticated`, network không tự đọc storage, lưu token bằng AndroidKeyStore trực tiếp nhưng để
  spec riêng làm sau. Resolved câu hỏi mở từ v1.0/v2.1 nên bump minor; cascade đầy đủ.

## v2.1 (2026-09-22)

- `requirements.md`: thay 2 tiêu chí HTTP 2xx/4xx-5xx bằng 6 tiêu chí EARS mô tả envelope
  (`success` kiểm trước, `success=false` trên HTTP 2xx vẫn là lỗi, `details` giữ nguyên, các field
  optional không crash); chuyển câu hỏi envelope/error body sang "Open questions (resolved)"; `⚠️`
  chỉ còn lại là giá trị URL thật + auth.
- `design.md`: thêm component `ApiResponse.kt` vào bảng Layers & components; mở rộng `AppError` bằng
  case `ApiError(code, message, type, details?)`; thêm mục "Envelope response (đã xác nhận v2.1)" với
  contract Go, ánh xạ Kotlin và bảng quy tắc bóc envelope; cập nhật pipeline Data flow và chữ ký
  `safeCall` (nhận `ApiResponse<T>`).
- `tasks.md`: Phase 1 thay task "quyết định ApiResponse" bằng task tạo `ApiResponse<T>` +
  `ApiErrorDetail`; Phase 3 thay task trích `serverMessage` bằng task bóc envelope + xử lý `data` vắng
  + test `success=false` trên HTTP 200.
- Reason: backend cung cấp contract envelope thật (`ApiResponse`/`ErrorDetail` dạng Go), giải quyết
  câu hỏi mở từ v1.0. Không đổi cấu trúc build nên bump minor; cascade requirements → design → tasks
  thực hiện đầy đủ.

## v2.0 (2026-09-22)

- `requirements.md`: thêm 3 tiêu chí EARS về môi trường build (nhúng `BuildConfig.BASE_URL` theo
  flavor, cài song song dev/staging, mặc định staging cho debug / prod cho release); chuyển câu hỏi
  cơ chế base URL sang "Open questions (resolved)"; thu hẹp `⚠️` còn lại thành "giá trị URL thật chưa
  có".
- `design.md`: thêm mục mới "Build variants & môi trường (product flavors)" (dimension `environment`,
  3 flavor dev/staging/prod, `applicationIdSuffix`, phác thảo `androidComponents.onVariants`); thêm
  row `app/build.gradle.kts` vào bảng Layers & components; cập nhật row `NetworkConfig` (đọc
  `BuildConfig.BASE_URL`) và mục "Deliberately stubbed".
- `tasks.md`: thêm **Phase 5 — Product flavors & môi trường** (đây là lý do bump major); dồn việc test
  sang Phase 6 và thêm Phase 7 — Documentation; bỏ task "chốt cơ chế cấu hình" đã giải quyết.
- Reason: người dùng chốt cách đổi môi trường là product flavors `dev`/`staging`/`prod` thay vì
  build-type + Gradle property. Đây là thay đổi cấu trúc build (thêm 3 flavor/variant) nên bump major;
  cascade requirements → design → tasks được thực hiện đầy đủ.

## v1.0 (2026-09-22)

- `requirements.md`: khởi tạo — user story (lớp network dùng chung cho mọi feature), 13 tiêu chí
  EARS, out-of-scope (endpoint/DTO/token refresh/cache), 3 câu hỏi `⚠️ Not confirmed` về backend.
- `design.md`: khởi tạo — kiến trúc `ui → domain ← data → core/network`, bảng Layers & components
  (11 file), pipeline "Data flow" một request, mục "Deliberately stubbed" cho `AuthInterceptor` và
  `NetworkConfig.baseUrl`.
- `tasks.md`: khởi tạo — 5 Phase (config/model lỗi → interceptor → NetworkClient → DI → test), toàn
  bộ `[ ]`; mục Deferred cho auth-refresh và endpoint của feature.
- Reason: viết spec prospective cho lớp network dùng chung, không bao gồm endpoint cụ thể, tuân thủ
  Clean Architecture hiện tại của repo. Người dùng bỏ qua bảng hỏi làm rõ nên các quyết định thiết
  kế dùng mặc định đã gợi ý (DI thủ công, `AppError` + `Result`, envelope generic, auth hook); các
  điểm phụ thuộc backend thật được đánh dấu `⚠️ Not confirmed` thay vì giả định.

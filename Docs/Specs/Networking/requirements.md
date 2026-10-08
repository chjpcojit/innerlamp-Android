# Requirements — Networking

Version: 2.3
Last updated: 2026-09-22
Status: not started — spec prospective, lớp network dùng chung chưa có code (thư mục `core/network/` chưa tồn tại)
Feature folder: `app/src/main/java/com/example/innerlamp/core/network/`

> See `changelog.md` in this folder for the version history of this spec.

This spec is written prospectively (chưa có code). Đây **không** phải một tính năng có màn hình —
là một **lớp hạ tầng dùng chung ở tầng network**, phục vụ mọi feature sau này gọi backend qua RESTful
API. Spec cố ý **không** định nghĩa endpoint cụ thể nào.

## User Story

Là lập trình viên của InnerLamp, tôi muốn có một lớp network dùng chung (OkHttp + Retrofit +
kotlinx.serialization) để mọi repository ở tầng `data` gọi backend RESTful một cách nhất quán —
không phải lặp lại code cấu hình HTTP, JSON, header, timeout và map lỗi ở từng feature.

## Acceptance Criteria (EARS notation)

- WHEN bất kỳ repository/tầng `data` nào thực hiện một lời gọi API qua lớp network dùng chung,
  THE SYSTEM SHALL gắn các header chung (`Accept: application/json`, `Content-Type: application/json`,
  app version, locale) vào **mọi** request.
- WHEN một feature tạo một Retrofit service cho endpoint của nó, THE SYSTEM SHALL cung cấp service đó
  qua factory của lớp network, sao cho core/network không chứa bất kỳ khai báo endpoint cụ thể nào.
- WHEN base URL rỗng (chưa được set), THE SYSTEM SHALL **không** thực hiện network call thật, SHALL trả
  về `NetworkResult.Failure(AppError.NotConfigured)` và không crash.
- WHEN app được build cho một môi trường (`dev` / `staging` / `prod`), THE SYSTEM SHALL nhúng base URL
  tương ứng của môi trường đó vào `BuildConfig.BASE_URL` tại thời điểm build, SHALL **không** hard-code
  URL trong source Kotlin. Trước khi backend sẵn sàng, giá trị dùng là **URL fake** đã chốt (xem
  `design.md` mục Build variants); chúng là giá trị thật của cấu hình, chỉ là domain fake.
- WHEN app được build cho môi trường `dev` hoặc `staging`, THE SYSTEM SHALL cài song song được với bản
  `prod` trên cùng thiết bị (khác `applicationId`), để tester không phải gỡ bản cũ.
- WHEN app build cho `release`, THE SYSTEM SHALL mặc định dùng môi trường `prod`; WHEN build cho
  `debug`, THE SYSTEM SHALL mặc định dùng `staging` (trừ khi người dựng build chọn rõ môi trường khác).
- WHEN một request hoàn tất, THE SYSTEM SHALL bóc envelope `ApiResponse` và kiểm tra `success`
  **trước**, rồi mới quyết định dùng `data` hay `error`.
- WHEN envelope có `success == true`, THE SYSTEM SHALL trả về `NetworkResult.Success` chứa `data` đã
  deserialize; WHEN `data` vắng mặt (endpoint không trả dữ liệu), THE SYSTEM SHALL vẫn coi là thành
  công với giá trị rỗng và không crash.
- WHEN envelope có `success == false` (bất kể HTTP status, **kể cả 2xx**), THE SYSTEM SHALL trả về
  `NetworkResult.Failure(AppError.ApiError(code, message, type, details))` lấy từ `error`.
- WHEN `success == false` và envelope có `details`, THE SYSTEM SHALL giữ nguyên `details` (map
  field → lỗi) trong `AppError.ApiError` để tầng trên hiển thị lỗi theo từng field.
- WHEN một request thất bại với HTTP 4xx/5xx **và** body parse được theo envelope, THE SYSTEM SHALL
  dùng `error` để tạo lỗi có `code`/`type`; WHEN body **không** parse được, THE SYSTEM SHALL fallback
  `AppError.HttpError(code, serverMessage?)`. Trong mọi trường hợp, THE SYSTEM SHALL **không** để lộ
  kiểu `retrofit2.*`/`okhttp3.*`/`java.io.IOException` ra ngoài lớp network.
- WHEN envelope thiếu `data`, `error` hoặc `details` (mỗi trường optional), THE SYSTEM SHALL
  deserialize được mà không crash, không phụ thuộc thứ tự hay sự hiện diện của chúng.
- WHEN thiết bị không có kết nối mạng, THE SYSTEM SHALL trả về `AppError.NoConnection`.
- WHEN một request vượt quá timeout đã cấu hình, THE SYSTEM SHALL trả về `AppError.Timeout`.
- WHEN response body không deserialize được (sai kiểu/JSON hỏng), THE SYSTEM SHALL trả về
  `AppError.Serialization`.
- WHEN một endpoint được đánh dấu `@Authenticated`, THE SYSTEM SHALL gắn header
  `Authorization: Bearer <token>` lấy từ `AuthTokenProvider`; WHEN endpoint không đánh dấu, THE SYSTEM
  SHALL **không** gắn header auth (mặc định là public, opt-in rõ ràng).
- WHEN endpoint `@Authenticated` được gọi nhưng `AuthTokenProvider.currentToken()` trả `null`, THE
  SYSTEM SHALL vẫn gửi request không kèm `Authorization` (để server quyết định) và SHALL **không**
  crash; kết quả 401 được xử lý như tiêu chí bên dưới.
- WHEN server trả về HTTP 401, THE SYSTEM SHALL trả về `AppError.Unauthorized` và SHALL chừa sẵn chỗ
  cắm refresh-token trong `AuthInterceptor` mà không phải sửa chữ ký `NetworkClient`.
- WHEN `AuthTokenProvider` được lấy token, THE SYSTEM SHALL đọc từ bộ nhớ đệm (in-memory) và SHALL
  **không** thực hiện I/O keystore/disk trong `Interceptor` (tránh chặn luồng mạng).
- WHEN lớp network cần token, THE SYSTEM SHALL lấy qua port `AuthTokenProvider` do `core/network` sở
  hữu; việc lưu/đọc secret thật (AndroidKeyStore) là trách nhiệm của spec `AuthTokenStorage`, không
  nằm trong `core/network`.
- WHEN app chạy ở build `debug`, THE SYSTEM SHALL log request/response qua logging interceptor; WHEN
  chạy ở build `release`, THE SYSTEM SHALL **không** log body của request/response.
- WHEN lớp network thực thi một request, THE SYSTEM SHALL chạy trên `DispatcherProvider.io` được
  inject, SHALL **không** gọi trực tiếp `Dispatchers.IO`.
- WHEN một tầng `data` chuyển kết quả từ `NetworkResult` sang `Result` của domain, THE SYSTEM SHALL
  bọc `AppError` trong một exception, sao cho tầng `domain` không bao giờ phải import `core/network`.
- WHEN lớp network không dùng tới một dependency nào của Android UI, THE SYSTEM SHALL giữ toàn bộ
  `core/network` là Kotlin thuần + Android framework tối thiểu, để test được trên JVM.

## Out of scope (this feature boundary, not backend work)

- **Mọi endpoint cụ thể** (`/auth/*`, `/sessions/*`, `/patterns/*`…): thuộc spec của từng feature và
  nằm ở `data/remote/api/`, không nằm trong `core/network`.
- **DTO & mapper của từng feature**: tầng `data` của feature đó sở hữu.
- **Logic nghiệp vụ / validation**: thuộc `domain/usecase`.
- **Triển khai refresh-token đầy đủ**: spec này chỉ chừa hook trong `AuthInterceptor`; luồng refresh
  thuộc feature auth sau này.
- **Lưu/đọc secret bằng AndroidKeyStore**: thuộc spec riêng **`AuthTokenStorage`** (chưa tạo — sẽ làm
  sau, theo yêu cầu). Networking chỉ tiêu thụ port `AuthTokenProvider`.
- **Ràng buộc endpoint nào cần auth**: thuộc feature sở hữu `XxxApiService`, nơi gắn `@Authenticated`.
- **Cache/offline-first cục bộ**: Room vẫn là nguồn dữ liệu có cấu trúc; lớp network không tự cache.
- **Backend thật / URL thật**: spec dùng URL fake đã chốt cho `dev`/`staging`/`prod`; việc thay bằng
  URL backend là thao tác cấu hình khi backend có, không thuộc phạm vi lớp network.

## Pending (not yet implemented)

- Toàn bộ lớp network — theo dõi ở `tasks.md` Phase 1–7.
- Cấu hình product flavors + `BuildConfig.BASE_URL` — xem `tasks.md` Phase 5.
- `ApiResponse<T>` + `ApiErrorDetail` + bóc envelope trong `safeCall` — xem `tasks.md` Phase 1 & 3.
- `@Authenticated` + `AuthTokenProvider` + `AuthInterceptor` thật — xem `tasks.md` Phase 2.
- Thay URL fake bằng URL backend thật khi backend sẵn sàng — xem `tasks.md` Phase 6.

<!-- Không còn câu hỏi mở nào ở phiên bản này; mọi ⚠️ trước đây đã được chuyển xuống mục dưới. -->

## Open questions (resolved during implementation)

- ~~Base URL được cấu hình thế nào để đổi môi trường dễ dàng?~~ → dùng **product flavors** `dev` /
  `staging` / `prod` (một flavor dimension `environment`), mỗi flavor nhúng `BuildConfig.BASE_URL`
  riêng qua `androidComponents.onVariants` và có `applicationIdSuffix` để cài song song. Resolved
  2026-09-22.
- ~~Giá trị base URL thật của từng môi trường là gì (backend chưa tồn tại)?~~ → dùng **URL fake** đã
  chốt cho `dev`/`staging`/`prod` (`https://dev-api.innerlamp.vn/`, `https://staging-api.innerlamp.vn/`,
  `https://api.innerlamp.vn/`); domain fake nhưng là giá trị cấu hình thật, sẽ thay bằng URL backend
  khi có (task thường ở Phase 6, không còn là câu hỏi mở). Resolved 2026-09-22.
- ~~Backend có bọc response trong envelope chung không, và error body có shape cố định không?~~ →
  có envelope chung `ApiResponse { success, data?, error?, details? }`; lỗi nằm ở
  `ErrorDetail { code, message, type }`. `safeCall` kiểm `success` trước: `true` → lấy `data`,
  `false` → `AppError.ApiError(code, message, type, details)`. `data`/`error`/`details` đều optional.
  Backend cung cấp contract Go (struct `ApiResponse`/`ErrorDetail`). Resolved 2026-09-22.
- ~~Endpoint nào cần auth, token cấp/lưu ở đâu, và token chảy vào request thế nào?~~ → endpoint cần
  auth tự khai báo bằng `@Authenticated` (opt-in, mặc định public). Token chảy vào qua port
  `AuthTokenProvider` do `core/network` sở hữu, `AuthInterceptor` đọc annotation qua `Invocation`
  rồi gắn `Authorization: Bearer <token>`. Network **không** tự đọc storage; caller **không** truyền
  token từng lời gọi. Lưu token bằng **AndroidKeyStore trực tiếp**, thuộc spec riêng
  `AuthTokenStorage` (chưa implement, làm sau) — Networking dùng provider in-memory/DI tạm. Resolved
  2026-09-22.

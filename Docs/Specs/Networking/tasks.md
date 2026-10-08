# Tasks — Networking

Version: 2.3
Last updated: 2026-09-22
Status: not started — spec prospective, mọi task còn `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Toàn bộ task dưới đây trace trực tiếp về bảng **Layers & components** / pipeline **Data flow**
trong `design.md`. Chưa có code nào được implement trong `core/network/` → tất cả giữ `[ ]`.

## Phase 1 — Nền tảng config & model lỗi

- [ ] Tạo `core/network/NetworkConfig.kt` (`baseUrl = BuildConfig.BASE_URL`, `connectTimeoutMillis`,
      `readTimeoutMillis`, `writeTimeoutMillis`, `isDebugLogging`).
- [ ] Tạo `core/network/AppError.kt` (`NotConfigured`, `NoConnection`, `Timeout`, `Unauthorized`,
      `ApiError(code, message, type, details?)`, `HttpError(code, serverMessage?)`, `Serialization`,
      `Unknown`).
- [ ] Tạo `core/network/ApiResponse.kt`: `@Serializable ApiResponse<T>(success, data? = null,
      error? = null, details? = null)` + `@Serializable ApiErrorDetail(code, message, type)`;
      `details` để dạng `Map<String, JsonElement>`.
- [ ] Tạo `core/network/NetworkResult.kt` (`Success` / `Failure(AppError)`).
- [ ] Tạo `core/network/NetworkException.kt` (bọc `AppError` cho tầng `data`).
- [ ] Tạo `core/network/JsonProvider.kt` (`ignoreUnknownKeys`, `explicitNulls = false`,
      `coerceInputValues = true`) và dùng chung với converter kotlinx.serialization.

## Phase 2 — Interceptors & OkHttp

- [ ] Tạo `core/network/interceptor/CommonHeadersInterceptor.kt` (Accept, Content-Type, app version,
      locale).
- [ ] Tạo `core/network/auth/Authenticated.kt` — annotation `@Target(FUNCTION) @Retention(RUNTIME)`.
- [ ] Tạo `core/network/auth/AuthTokenProvider.kt` — port `fun currentToken(): String?` (đồng bộ).
- [ ] Tạo `core/network/auth/InMemoryAuthTokenProvider.kt` — impl tạm (AtomicReference + `update`/
      `clear`) để wiring/test trước khi có storage thật.
- [ ] Tạo `core/network/interceptor/AuthInterceptor.kt`: đọc `request.tag(Invocation::class.java)`,
      nếu method có `@Authenticated` thì gắn `Authorization: Bearer <token>` (token `null` → gửi
      trần, không crash); chừa chỗ cắm refresh-token khi gặp 401.
- [ ] Tạo `core/network/interceptor/LoggingInterceptor.kt` — bật body ở `debug`, tắt ở `release`.
- [ ] Dựng `OkHttpClient.Builder` nối các interceptor trên (nằm ở `di/NetworkModule.kt`).

## Phase 3 — NetworkClient & map lỗi

- [ ] Tạo `core/network/NetworkErrorMapper.kt`:
      `UnknownHostException` → `NoConnection`, `SocketTimeoutException` → `Timeout`,
      HTTP 401 → `Unauthorized`, HTTP 4xx/5xx parse được envelope → `ApiError(code, message, type,
      details)`, HTTP 4xx/5xx không parse được → `HttpError`, `SerializationException` →
      `Serialization`, còn lại → `Unknown`.
- [ ] Tạo `core/network/NetworkClient.kt` (interface `createService` +
      `safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T>`).
- [ ] Tạo `core/network/RetrofitNetworkClient.kt`:
      `createService` dựng service từ `Retrofit`; `safeCall` guard `baseUrl` rỗng →
      `AppError.NotConfigured`, chạy `DispatcherProvider.io`, **bóc envelope** (kiểm `success` trước;
      `true` → `Success(data!!)`, `false` → `ApiError(...)`, kể cả HTTP 2xx), map lỗi qua
      `NetworkErrorMapper`, trả `NetworkResult` và **không** để lộ kiểu retrofit/okhttp.
- [ ] Xử lý `data` vắng ở nhánh `success == true` (endpoint không trả dữ liệu) mà không crash —
      chốt kiểu `T` rỗng phù hợp cho các endpoint đó.
- [ ] Test: `success = false` trên HTTP 200 vẫn trả `ApiError` (không nuốt lỗi thành Success).

## Phase 4 — DI wiring

- [ ] Tạo `di/NetworkModule.kt` (thủ công, không thêm thư viện DI): provider cho `Json`,
      `OkHttpClient`, `Retrofit`, `NetworkClient`, `NetworkConfig`.
- [ ] Xác nhận lớp network không import Android UI và `domain` không import `core/network`
      (kiểm tra bằng mắt + build xanh).

## Phase 5 — Product flavors & môi trường

- [ ] Trong `app/build.gradle.kts`: thêm `flavorDimensions += "environment"` và 3 flavor
      `create("dev")`, `create("staging")`, `create("prod")` với `applicationIdSuffix` `.dev` /
      `.staging` (prod không suffix).
- [ ] Nhúng `BuildConfig.BASE_URL` cho từng variant qua `androidComponents.onVariants` +
      `BuildConfigField` (String phải kèm dấu ngoặc kép trong `value`), chọn URL theo
      `variant.flavorName`; flavor lạ → chuỗi rỗng để rơi vào `AppError.NotConfigured`.
- [ ] Xác nhận cài song song được `dev` / `staging` / `prod` trên cùng thiết bị.
- [ ] Ghi lại lệnh build cho từng môi trường (`installDevDebug`, `installStagingDebug`,
      `installProdRelease`) vào README feature (Phase 7).

## Phase 6 — Test & backend thật

- [ ] Unit test JVM cho `NetworkErrorMapper` (map đủ các case lỗi) với MockK.
- [ ] Unit test JVM cho `RetrofitNetworkClient.safeCall` dùng `MockWebServer`-kiểu fake hoặc test
      double, chạy `StandardTestDispatcher` qua `DispatcherProvider` giả.
- [ ] Test case base URL **rỗng** → `AppError.NotConfigured` (không gọi ra ngoài). Lưu ý: URL fake
      (non-empty) thì đi ra mạng và fail bằng `NoConnection`, không phải `NotConfigured`.
- [ ] Test `AuthInterceptor`: method `@Authenticated` có token → gắn `Bearer`; token `null` → không
      gắn header và không crash; method không đánh dấu → không gắn header.
- [ ] Thay URL fake bằng URL backend thật cho cả 3 flavor khi backend sẵn sàng.
- [ ] Manual test một lời gọi thật trên môi trường `staging` (và `prod` khi có).

## Phase 7 — Documentation

- [ ] Tạo `core/network/README.md`: vai trò của lớp, ví dụ `NetworkClient.safeCall`, quy ước
      `AppError`, cách gắn `@Authenticated`, và bảng môi trường (`dev`/`staging`/`prod`) kèm lệnh
      build tương ứng.
- [ ] (Sau khi có code thật) ghi chú OKF cho lớp network dưới `Docs/Knowledge/` — chỉ làm khi hành vi
      đã end-to-end, không mô tả stub.

## Deferred (feature khác sở hữu, không block Networking)

- **Impl `AuthTokenProvider` thật** đọc/ghi **AndroidKeyStore trực tiếp** — thuộc spec riêng
  `AuthTokenStorage` (chưa tạo, làm sau). Networking dùng `InMemoryAuthTokenProvider` tới lúc đó.
- Triển khai refresh-token đầy đủ trong `AuthInterceptor` — thuộc feature auth.
- `XxxApiService` + DTO + repository của từng feature — thuộc spec của feature đó, đặt ở
  `data/remote/api/`.
- Cache/offline-first cục bộ — thuộc feature sử dụng Room.

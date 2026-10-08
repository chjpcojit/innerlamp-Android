# Design — Networking

Version: 2.3
Last updated: 2026-09-22
Status: not started — spec prospective, lớp network dùng chung chưa có code

> See `changelog.md` in this folder for the version history of this spec.

## Overview

Đây là một **lớp hạ tầng dùng chung** (không phải feature có màn hình), đặt trong `core/network/`,
thuộc tầng infrastructure của Clean Architecture hiện tại: `ui → domain ← data → core/network`.
Lớp này chỉ cung cấp **cơ chế** gọi RESTful API (OkHttp + Retrofit + kotlinx.serialization, header
chung, timeout, map lỗi, bọc kết quả) — **không** chứa bất kỳ endpoint, DTO hay nghiệp vụ nào.

Ràng buộc kiến trúc của repo được giữ nguyên:

- `domain` là Kotlin thuần và sở hữu `repository` interface; **không** import `core/network`,
  Room, Retrofit hay Android.
- `data` implement repository interface của `domain`, gọi `core/network`, tự map DTO ↔ model.
- `core/network` là Kotlin thuần + Android framework tối thiểu (chỉ OkHttp/Retrofit/serialization),
  test được trên JVM, và nhận `DispatcherProvider` qua inject thay vì gọi `Dispatchers.IO`.

## Layers & components

| Layer | File | Responsibility |
|---|---|---|
| core/network | `core/network/NetworkConfig.kt` | Nguồn cấu hình bất biến: `baseUrl`, timeout (connect/read/write), `isDebugLogging`. `baseUrl` đọc từ `BuildConfig.BASE_URL` (do product flavor sinh ra — xem "Build variants & môi trường"); vẫn có guard: URL rỗng → `AppError.NotConfigured`. |
| core/network | `core/network/AppError.kt` | `sealed class AppError` trung tính với thư viện mạng: `NotConfigured`, `NoConnection`, `Timeout`, `Unauthorized`, `ApiError(code, message, type, details?)`, `HttpError(code, serverMessage?)`, `Serialization`, `Unknown(cause?)`. |
| core/network | `core/network/ApiResponse.kt` | `@Serializable data class ApiResponse<T>(success, data?, error?, details?)` + `@Serializable data class ApiErrorDetail(code, message, type)`. Envelope chung của backend (đã xác nhận v2.1) — xem "Envelope response". |
| core/network | `core/network/NetworkResult.kt` | `sealed interface NetworkResult<out T>` với `Success(data)` / `Failure(error: AppError)`. Là kiểu trả về duy nhất của lớp network. |
| core/network | `core/network/NetworkException.kt` | `class NetworkException(val error: AppError) : Exception()`. Cầu nối để tầng `data` bọc `AppError` vào `Result.failure` mà `domain` vẫn chỉ thấy `Throwable`. |
| core/network | `core/network/JsonProvider.kt` | Tạo `Json` dùng chung cho cả app: `ignoreUnknownKeys = true`, `explicitNulls = false`, `coerceInputValues = true`. Không cấu hình rải rác ở từng feature. |
| core/network | `core/network/auth/Authenticated.kt` | `@Target(FUNCTION) @Retention(RUNTIME) annotation class Authenticated` — đánh dấu endpoint Retrofit cần Bearer. Mặc định không đánh dấu = public (opt-in). |
| core/network | `core/network/auth/AuthTokenProvider.kt` | **Port**: `fun currentToken(): String?` (đồng bộ, đọc cache in-memory). `core/network` sở hữu interface này; impl thật nằm ngoài (spec `AuthTokenStorage`). |
| core/network | `core/network/auth/InMemoryAuthTokenProvider.kt` | Impl tạm: token giữ trong `AtomicReference`, có `update(token?)`/`clear()`. Cho phép wiring + test trước khi `AuthTokenStorage` tồn tại. |
| core/network | `core/network/interceptor/CommonHeadersInterceptor.kt` | Gắn header chung (`Accept`, `Content-Type`, app version, locale) vào mọi request. |
| core/network | `core/network/interceptor/AuthInterceptor.kt` | Đọc `request.tag(Invocation::class.java)` → nếu method có `@Authenticated` thì gắn `Authorization: Bearer <token>` lấy từ `AuthTokenProvider`. Không có token thì gửi request trần (server trả 401). |
| core/network | `core/network/interceptor/LoggingInterceptor.kt` | OkHttp logging: chỉ bật body ở `debug`, mức `NONE`/header-only ở `release`. |
| core/network | `core/network/NetworkErrorMapper.kt` | Map `UnknownHostException`/`SocketTimeoutException`/`HttpException`/`SerializationException`/còn lại → `AppError`; khi HTTP lỗi mà body parse được envelope thì lấy `error.code/message/type` (xem "Envelope response"). |
| core/network | `core/network/NetworkClient.kt` | Interface dùng chung: `fun <T : Any> createService(clazz: Class<T>): T` và `suspend fun <T> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T>`. Là mặt tiền duy nhất tầng `data` dùng; `safeCall` bóc envelope. |
| core/network | `core/network/RetrofitNetworkClient.kt` | Impl `NetworkClient`: dựng `Retrofit` quanh `OkHttpClient`, `createService` trả service, `safeCall` chạy trên `DispatcherProvider.io` + `NotConfigured` guard + bóc `ApiResponse` + gọi `NetworkErrorMapper`. |
| di | `di/NetworkModule.kt` | Wiring thủ công (không thêm thư viện DI): tạo `Json`, `OkHttpClient`, `Retrofit`, `NetworkClient`; các provider dùng chung cho toàn app. |
| build / config | `app/build.gradle.kts` | Khai báo product flavors `dev`/`staging`/`prod`, mỗi flavor có `applicationIdSuffix`; qua `androidComponents.onVariants` nhúng `BuildConfig.BASE_URL` tương ứng. **Sửa file này** (không tạo mới). |
| data (chỉ là thư mục) | `data/remote/api/` | **Nơi khai báo endpoint interface của từng feature** (`XxxApiService`), nằm ngoài `core/network` để giữ lớp dùng chung không biết endpoint. |

## Navigation flow

Không áp dụng — đây là lớp hạ tầng, không có màn hình, không có coordinator/nav graph.

## Build variants & môi trường (product flavors)

Chốt (v2.0): đổi môi trường bằng **product flavors**, không sửa source Kotlin. Một flavor dimension
tên `environment` với 3 flavor: `dev`, `staging`, `prod`. Kết hợp với 2 build type sẵn có (`debug`,
`release`) tạo 6 variant; chỉ `debug` variant được dùng hằng ngày.

| Flavor | `applicationIdSuffix` | `BASE_URL` (URL **fake** — chốt dùng ngay) | Ghi chú |
|---|---|---|---|
| `dev` | `.dev` | `https://dev-api.innerlamp.vn/` | dùng khi dev local/ngrok |
| `staging` | `.staging` | `https://staging-api.innerlamp.vn/` | mặc định của `debug` |
| `prod` | (không) | `https://api.innerlamp.vn/` | mặc định của `release` |

- Đây là **domain fake** (backend chưa tồn tại), nhưng là **giá trị cấu hình thật** — không phải
  placeholder rỗng. Khi gọi ra, request sẽ thất bại ở tầng DNS/kết nối → `AppError.NoConnection`, chứ
  **không** phải `AppError.NotConfigured`. `NotConfigured` chỉ còn là guard khi base URL **rỗng**.
- Thay bằng URL backend thật khi backend sẵn sàng = sửa 1 dòng trong `app/build.gradle.kts`.

- `applicationIdSuffix` khác nhau ⇒ cài song song `dev`/`staging`/`prod` trên cùng máy.
- Giá trị URL nhúng vào `BuildConfig.BASE_URL` qua **Variant API của AGP 9** (không hard-code trong
  Kotlin), vì `BuildConfigField` kiểu `String` phải kèm dấu ngoặc kép trong `value`. Phác thảo:

  ```kotlin
  // app/build.gradle.kts
  import com.android.build.api.variant.BuildConfigField

  android {
      flavorDimensions += "environment"
      productFlavors {
          create("dev")     { dimension = "environment"; applicationIdSuffix = ".dev" }
          create("staging") { dimension = "environment"; applicationIdSuffix = ".staging" }
          create("prod")    { dimension = "environment" }
      }
      buildFeatures { buildConfig = true }
  }

  androidComponents {
      onVariants { variant ->
          val url = when (variant.flavorName) {
              "dev"     -> "https://dev-api.innerlamp.vn/"
              "staging" -> "https://staging-api.innerlamp.vn/"
              "prod"    -> "https://api.innerlamp.vn/"
              else      -> "" // chưa cấu hình → AppError.NotConfigured
          }
          variant.buildConfigFields?.put(
              "BASE_URL",
              BuildConfigField("String", "\"$url\"", "Backend REST base URL"),
          )
      }
  }
  ```

- Cách chạy: `./gradlew installDevDebug` / `installStagingDebug` / `installProdRelease`.
- `NetworkConfig.baseUrl = BuildConfig.BASE_URL` — vẫn giữ guard rỗng → `AppError.NotConfigured`.
- **Ngoài scope:** cơ chế override runtime (menu đổi môi trường trong app) không nằm trong spec này.

## Data flow (request lifecycle)

Lớp này chỉ có **một** API surface (`NetworkClient.safeCall`), mỗi request đi qua đúng một pipeline.
Vì không có luồng người dùng gồm 2+ lời gọi API tuần tự, mục **Step-by-step Flow** (rule 5) không bắt
buộc ở đây; thay bằng pipeline một request dưới đây.

```
Data repository (feature)
  └─ NetworkClient.safeCall { featureApiService.getXxx(...) }   // kiểu trả về ApiResponse<T>
       └─ [guard] NetworkConfig.baseUrl rỗng? → NetworkResult.Failure(AppError.NotConfigured)
       └─ DispatcherProvider.io
            └─ OkHttp
                 ├─ CommonHeadersInterceptor  → gắn header chung
                 ├─ AuthInterceptor           → @Authenticated? gắn Bearer từ AuthTokenProvider
                 └─ LoggingInterceptor        → debug: log; release: không log body
            └─ Retrofit + JsonProvider        → deserialize ApiResponse<T>
       └─ [unwrap] success == true  → NetworkResult.Success(data!!)
                   success == false → NetworkResult.Failure(AppError.ApiError(error.code, error.message, error.type, details))
       └─ NetworkErrorMapper (khi throw)      → AppError → NetworkResult.Failure
  └─ data repository map: NetworkResult → Result<T>
       (Failure → Result.failure(NetworkException(error)))
  └─ domain usecase: Result<T>  (không thấy core/network)
```

- Tầng `data` **không** tự `try/catch` IOException hay tự dựng `Retrofit`; luôn đi qua `NetworkClient`.
- `domain` **không** bao giờ import `core/network`; `NetworkException` chỉ xuất hiện ở tầng `data`.
- Endpoint interface (`XxxApiService`) do feature khai báo ở `data/remote/api/`, được tạo qua
  `NetworkClient.createService(...)` — core/network không biết nó là endpoint nào.

## Envelope response (đã xác nhận v2.1)

Mọi response của backend bọc trong một envelope chung; `safeCall` **luôn kiểm `success` trước** rồi
mới quyết định lấy `data` hay `error`. Contract (Go) do backend cung cấp:

```go
type ErrorDetail struct {
    Code    string `json:"code"`
    Message string `json:"message"`
    Type    string `json:"type"`
}

type ApiResponse struct {
    Success bool           `json:"success"`
    Data    any            `json:"data,omitempty"`
    Error   *ErrorDetail   `json:"error,omitempty"`
    Details map[string]any `json:"details,omitempty"`
}
```

Ánh xạ sang Kotlin:

```kotlin
@Serializable
data class ApiErrorDetail(val code: String, val message: String, val type: String)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorDetail? = null,
    val details: Map<String, JsonElement>? = null,
)
```

Quy tắc bóc envelope trong `safeCall`:

| Trường hợp | Kết quả |
|---|---|
| `success == true`, có `data` | `NetworkResult.Success(data)` |
| `success == false`, có `error` | `NetworkResult.Failure(AppError.ApiError(code, message, type, details))` — **kể cả khi HTTP status là 2xx** |
| `success == true` nhưng `data` vắng (endpoint không trả dữ liệu) | `NetworkResult.Success` với giá trị rỗng; caller khai báo `T` là object rỗng/`Unit`-tương đương, không được crash vì `data = null` |
| HTTP 4xx/5xx, body parse được envelope | dùng `error` để tạo lỗi có `code`/`type`; 401 ưu tiên `Unauthorized` |
| HTTP 4xx/5xx, body **không** parse được | fallback `AppError.HttpError(code, serverMessage?)` |
| Body không deserialize được | `AppError.Serialization` |

- `details` (map field → lỗi) chỉ dùng khi `success == false`; giữ dạng `Map<String, JsonElement>` để
  không ràng buộc shape từng endpoint.
- `data`, `error`, `details` đều optional: `ApiResponse` khai báo default `null` nên mọi tổ hợp đều
  deserialize được, không phụ thuộc thứ tự hay sự hiện diện.
- Tầng `domain` không thấy `ApiResponse`; nó chỉ nhận `Result<T>` đã map.

## Auth flow — per-endpoint opt-in (đã xác nhận v2.2)

**Quyết định:** network **không** tự đọc security storage, và caller **không** truyền token từng lời
gọi. Thay vào đó `core/network` định nghĩa port `AuthTokenProvider`; impl thật (AndroidKeyStore) nằm
ở spec riêng `AuthTokenStorage`, nối vào qua DI.

```
XxxApiService (feature, data/remote/api/)
  @Authenticated @GET("sessions") suspend fun getSessions(): ApiResponse<List<SessionDto>>

AuthInterceptor
  ├─ request.tag(Invocation::class.java)?.method()?.getAnnotation(Authenticated::class.java)
  ├─ nếu CÓ  → token = AuthTokenProvider.currentToken()
  │              • token != null → header("Authorization", "Bearer $token")
  │              • token == null → gửi trần (server trả 401 → AppError.Unauthorized)
  └─ nếu KHÔNG → chain.proceed(request) nguyên trạng (endpoint public)
```

- **Opt-in, mặc định public**: chỉ method gắn `@Authenticated` mới có header auth.
- **Port** (`core/network/auth/AuthTokenProvider.kt`): `fun currentToken(): String?`.
  - Đồng bộ, **chỉ đọc cache in-memory** — `Interceptor` chạy đồng bộ, tuyệt đối không I/O keystore.
  - Cache được nạp lúc app khởi động và cập nhật sau login/logout (do `AuthTokenStorage` lo).
- **Impl tạm** `InMemoryAuthTokenProvider` (AtomicReference) để wiring/test được trước khi có storage
  thật; DI thay bằng impl Keystore khi `AuthTokenStorage` xong.
- **401** → `AppError.Unauthorized`; chừa chỗ cắm refresh-token trong `AuthInterceptor` (chưa làm).
- **Không thuộc networking**: lưu/đọc secret bằng Android Keystore — spec riêng `AuthTokenStorage`.

## Deliberately stubbed for now (if applicable)

- `AuthTokenProvider`: hiện chỉ có impl tạm `InMemoryAuthTokenProvider`. Chưa đọc/ghi AndroidKeyStore
  vì `AuthTokenStorage` (spec riêng) chưa implement — theo yêu cầu làm sau. DI sẽ đổi impl khi có.
  Theo dõi ở `tasks.md` Phase 2 và mục Deferred.
- `AuthInterceptor`: đã implement đầy đủ phần gắn Bearer + đọc annotation; **refresh-token khi 401 là
  chỗ cắm còn trống** (thuộc feature auth), theo dõi ở `tasks.md` Deferred.
- `NetworkConfig.baseUrl`: đọc `BuildConfig.BASE_URL` do flavor sinh ra. Hiện dùng **URL fake** cho cả
  3 môi trường (xem bảng trên); khi gọi sẽ fail ở tầng kết nối (`AppError.NoConnection`) vì domain
  chưa tồn tại. `AppError.NotConfigured` chỉ xảy ra nếu URL rỗng. Thay URL thật khi backend sẵn sàng
  (xem `tasks.md` Phase 6).

> Không còn `⚠️ Not confirmed` trong spec này. URL fake đã chốt ở mục "Build variants & môi trường";
> việc thay bằng URL thật là task thường ở `tasks.md` Phase 6, không phải câu hỏi mở.

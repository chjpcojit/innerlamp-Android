# core/network — lớp mạng dùng chung

Lớp hạ tầng dùng chung cho mọi lời gọi RESTful (OkHttp + Retrofit + kotlinx.serialization).
Nó **chỉ** cung cấp cơ chế gọi API — header chung, timeout, map lỗi, bọc kết quả — và
**không** chứa endpoint, DTO hay nghiệp vụ nào. Endpoint thuộc từng feature, khai báo ở
`data/remote/api/`.

Kiến trúc: `ui → domain ← data → core/network`. `domain` **không bao giờ** import `core/network`.

## Cách dùng (`NetworkClient.safeCall`)

Tầng `data` chỉ dùng một mặt tiền duy nhất là `NetworkClient`:

```kotlin
// data/remote/api/SessionApiService.kt (do feature khai báo)
interface SessionApiService {
    @Authenticated
    @GET("sessions")
    suspend fun getSessions(): ApiResponse<List<SessionDto>>
}

// data/repository/SessionRepositoryImpl.kt
class SessionRepositoryImpl(
    private val api: SessionApiService,
    private val networkClient: NetworkClient,
) : SessionRepository {
    override suspend fun sync(): Result<Unit> =
        when (val r = networkClient.safeCall { api.getSessions() }) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Failure -> Result.failure(NetworkException(r.error))
        }
}
```

- `safeCall` tự bóc envelope `ApiResponse` (kiểm `success` trước), chạy trên
  `DispatcherProvider.io`, map lỗi → `AppError`, trả `NetworkResult` và **không** để lộ
  kiểu retrofit/okhttp/IOException.
- Service được tạo qua `networkClient.createService(XxxApiService::class.java)` (xem `di/NetworkModule.kt`).
- Base URL rỗng → `NetworkResult.Failure(AppError.NotConfigured)`, không gọi ra ngoài.

## Envelope response

Backend bọc mọi response trong `ApiResponse<T>`:

```json
{ "success": true, "data": { }, "error": null, "details": null }
```

`success == true` → dùng `data`; `success == false` → `AppError.ApiError(code, message, type, details)`
(kể cả khi HTTP 2xx). `data`/`error`/`details` đều optional.

## Quy ước `AppError`

| Case | `AppError` |
|---|---|
| Base URL rỗng | `NotConfigured` |
| Không có mạng (DNS/connect) | `NoConnection` |
| Quá timeout | `Timeout` |
| HTTP 401 | `Unauthorized` |
| `success == false` (kể cả 2xx) | `ApiError(code, message, type, details)` |
| HTTP 4xx/5xx, body không parse được | `HttpError(code, serverMessage?)` |
| Body không deserialize được | `Serialization` |
| Còn lại | `Unknown(cause?)` |

## Auth — opt-in từng endpoint

Endpoint cần auth tự khai báo `@Authenticated` (mặc định không đánh dấu = public):

```kotlin
@Authenticated
@GET("me")
suspend fun me(): ApiResponse<UserDto>
```

`AuthInterceptor` đọc annotation qua `request.tag(Invocation::class.java)` rồi gắn
`Authorization: Bearer <token>` lấy từ port `AuthTokenProvider` (đồng bộ, đọc cache in-memory).
Hiện tại dùng impl tạm `InMemoryAuthTokenProvider`; impl AndroidKeyStore thật thuộc spec
`AuthTokenStorage` (làm sau), nối vào qua `di/NetworkModule.kt`.

## Môi trường build (product flavors)

Một flavor dimension `environment`, 3 flavor. Base URL nhúng vào `BuildConfig.BASE_URL` lúc build.

| Flavor | `applicationIdSuffix` | `BASE_URL` | Lệnh build |
|---|---|---|---|
| `dev` | `.dev` | `https://dev-api.innerlamp.vn/` | `./gradlew installDevDebug` |
| `staging` | `.staging` | `https://staging-api.innerlamp.vn/` | `./gradlew installStagingDebug` |
| `prod` | (không) | `https://api.innerlamp.vn/` | `./gradlew installProdRelease` |

- `debug` mặc định dùng `staging`; `release` mặc định dùng `prod` (chọn flavor rõ khi build).
- URL hiện là **fake** (backend chưa có) → request sẽ fail bằng `AppError.NoConnection`,
  không phải `NotConfigured`. Thay URL thật khi backend sẵn sàng = sửa trong `app/build.gradle.kts`.
- `applicationIdSuffix` khác nhau ⇒ cài song song `dev`/`staging`/`prod` trên cùng thiết bị.

## File chính

| File | Vai trò |
|---|---|
| `NetworkConfig.kt` | baseUrl + timeout + `isDebugLogging` |
| `AppError.kt` | kiểu lỗi trung tính với thư viện mạng |
| `ApiResponse.kt` | envelope chung của backend |
| `NetworkResult.kt` / `NetworkException.kt` | kết quả trả về + cầu nối sang `Result` |
| `JsonProvider.kt` | `Json` dùng chung (cấu hình một chỗ) |
| `auth/` | `@Authenticated` + port `AuthTokenProvider` + impl tạm |
| `interceptor/` | header chung, auth, logging |
| `NetworkErrorMapper.kt` | exception thô → `AppError` |
| `NetworkClient.kt` / `RetrofitNetworkClient.kt` | mặt tiền + impl Retrofit |
| `di/NetworkModule.kt` | wiring thủ công (không dùng thư viện DI) |

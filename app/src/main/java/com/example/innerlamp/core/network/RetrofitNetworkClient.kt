package com.example.innerlamp.core.network

import com.example.innerlamp.core.util.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

/**
 * Impl [NetworkClient] dùng Retrofit + OkHttp.
 *
 * - [createService] dựng service từ [Retrofit].
 * - [safeCall] guard base URL rỗng → [AppError.NotConfigured], chạy trên
 *   `DispatcherProvider.io`, bóc envelope [ApiResponse] (kiểm `success` trước),
 *   rồi map lỗi qua [NetworkErrorMapper].
 */
class RetrofitNetworkClient(
    private val retrofit: Retrofit,
    private val baseUrl: String,
    private val dispatchers: DispatcherProvider,
    private val errorMapper: NetworkErrorMapper,
) : NetworkClient {

    override fun <T : Any> createService(clazz: Class<T>): T = retrofit.create(clazz)

    override suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T> {
        if (baseUrl.isBlank()) {
            return NetworkResult.Failure(AppError.NotConfigured)
        }
        return try {
            withContext(dispatchers.io) {
                val response = block()
                if (response.success) {
                    val data = response.data
                    if (data != null) {
                        NetworkResult.Success(data)
                    } else {
                        // `success == true` nhưng `data` vắng (endpoint không trả dữ
                        // liệu) → vẫn coi là thành công với giá trị rỗng. Caller khai
                        // báo `T` là kiểu rỗng (vd `Unit`) và không đọc `data`; cast
                        // unchecked an toàn vì `T` bị xoá type (erasure).
                        @Suppress("UNCHECKED_CAST")
                        NetworkResult.Success(Unit as T)
                    }
                } else {
                    NetworkResult.Failure(
                        AppError.ApiError(
                            code = response.error?.code.orEmpty(),
                            message = response.error?.message.orEmpty(),
                            type = response.error?.type.orEmpty(),
                            details = response.details,
                        ),
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(errorMapper.map(e))
        }
    }
}

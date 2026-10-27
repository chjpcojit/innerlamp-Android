package com.example.innerlamp.data.fake

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult

/**
 * [NetworkClient] for repository tests: runs the block, or answers [failures] in order (one per call)
 * before running it. /
 * [NetworkClient] cho test repository: chạy thẳng block, hoặc trả lần lượt các lỗi trong [failures] (mỗi
 * lần gọi một lỗi) trước khi chạy.
 */
class FakeContentNetworkClient(vararg failures: AppError) : NetworkClient {
    private val pending = ArrayDeque(failures.toList())
    var calls = 0
        private set

    override fun <T : Any> createService(clazz: Class<T>): T = error("not used")

    override suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T> {
        calls++
        pending.removeFirstOrNull()?.let { return NetworkResult.Failure(it) }
        return NetworkResult.Success(block().data!!)
    }
}

fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

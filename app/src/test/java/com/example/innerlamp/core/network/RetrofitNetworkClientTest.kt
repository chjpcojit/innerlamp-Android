package com.example.innerlamp.core.network

import com.example.innerlamp.core.util.DispatcherProvider
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

/**
 * Test JVM cho [RetrofitNetworkClient.safeCall] dùng test double (block giả) và
 * `StandardTestDispatcher` qua [DispatcherProvider] giả — không cần MockWebServer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitNetworkClientTest {

    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher,
    ) : DispatcherProvider {
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val main: CoroutineDispatcher get() = dispatcher
    }

    private fun client(
        baseUrl: String,
        dispatcher: CoroutineDispatcher,
    ): RetrofitNetworkClient = RetrofitNetworkClient(
        retrofit = Retrofit.Builder().baseUrl("https://example.com/").build(),
        baseUrl = baseUrl,
        dispatchers = TestDispatcherProvider(dispatcher),
        errorMapper = NetworkErrorMapper(JsonProvider.json),
    )

    @Test
    fun emptyBaseUrl_returnsNotConfigured_withoutCallingBlock() = runTest {
        var called = false
        val result = client(baseUrl = "", dispatcher = StandardTestDispatcher(testScheduler))
            .safeCall<Unit> {
                called = true
                ApiResponse(success = true, data = Unit)
            }

        assertEquals(NetworkResult.Failure(AppError.NotConfigured), result)
        assertFalse(called)
    }

    @Test
    fun successTrue_withData_returnsSuccess() = runTest {
        val result = client(baseUrl = "https://example.com/", dispatcher = StandardTestDispatcher(testScheduler))
            .safeCall<String> { ApiResponse(success = true, data = "hello") }

        assertEquals(NetworkResult.Success("hello"), result)
    }

    @Test
    fun successTrue_withoutData_returnsSuccess_noCrash() = runTest {
        // Endpoint không trả dữ liệu (data == null) → vẫn coi là thành công, không crash.
        val result = client(baseUrl = "https://example.com/", dispatcher = StandardTestDispatcher(testScheduler))
            .safeCall<Unit> { ApiResponse(success = true, data = null) }

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun successFalse_onHttp2xx_returnsApiError() = runTest {
        // Envelope `success == false` → lỗi nghiệp vụ, kể cả khi HTTP status là 2xx.
        val result = client(baseUrl = "https://example.com/", dispatcher = StandardTestDispatcher(testScheduler))
            .safeCall<String> {
                ApiResponse(
                    success = false,
                    error = ApiErrorDetail(code = "E1", message = "sai", type = "validation"),
                    data = null,
                )
            }

        assertTrue(result is NetworkResult.Failure)
        val error = (result as NetworkResult.Failure).error
        assertTrue(error is AppError.ApiError)
        error as AppError.ApiError
        assertEquals("E1", error.code)
        assertEquals("sai", error.message)
        assertEquals("validation", error.type)
    }

    @Test
    fun thrownException_mapsThroughErrorMapper() = runTest {
        val result = client(baseUrl = "https://example.com/", dispatcher = StandardTestDispatcher(testScheduler))
            .safeCall<String> { throw UnknownHostException("down") }

        assertEquals(NetworkResult.Failure(AppError.NoConnection), result)
    }
}

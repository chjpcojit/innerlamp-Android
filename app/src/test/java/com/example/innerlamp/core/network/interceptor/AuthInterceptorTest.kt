package com.example.innerlamp.core.network.interceptor

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.auth.Authenticated
import com.example.innerlamp.core.network.auth.InMemoryAuthTokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.lang.reflect.Method
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Invocation
import retrofit2.http.GET

/**
 * Test JVM cho [AuthInterceptor]: endpoint `@Authenticated` có token → gắn Bearer;
 * token null → gửi trần, không crash; endpoint không đánh dấu → không gắn header.
 */
class AuthInterceptorTest {

    private interface TestApi {
        @Authenticated
        @GET("auth")
        fun authenticated(): ApiResponse<Unit>

        @GET("public")
        fun public(): ApiResponse<Unit>
    }

    private val tokenProvider = InMemoryAuthTokenProvider()

    @Test
    fun authenticatedMethod_withToken_addsBearerHeader() {
        tokenProvider.update("abc123")
        val (original, proceeded) = proceed(method("authenticated"))

        assertEquals("Bearer abc123", proceeded.header("Authorization"))
        assertNull(original.header("Authorization")) // request gốc không bị sửa trực tiếp
    }

    @Test
    fun authenticatedMethod_withoutToken_sendsBare_noCrash() {
        tokenProvider.clear()
        val (_, proceeded) = proceed(method("authenticated"))

        assertNull(proceeded.header("Authorization"))
    }

    @Test
    fun nonAuthenticatedMethod_doesNotAddHeader() {
        tokenProvider.update("abc123")
        val (_, proceeded) = proceed(method("public"))

        assertNull(proceeded.header("Authorization"))
    }

    /** Chạy interceptor và trả về (request gốc, request sau khi qua interceptor). */
    private fun proceed(method: Method): Pair<Request, Request> {
        val request = buildRequest(method)
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val proceeded = slot<Request>()
        every { chain.request() } returns request
        every { chain.proceed(capture(proceeded)) } returns mockk(relaxed = true)

        AuthInterceptor(tokenProvider).intercept(chain)
        return request to proceeded.captured
    }

    private fun buildRequest(method: Method): Request {
        val invocation = Invocation.of(TestApi::class.java, mockk<TestApi>(), method, emptyList<Any>())
        return Request.Builder()
            .url("https://example.com/")
            .tag(Invocation::class.java, invocation)
            .build()
    }

    /** Lấy `Method` theo tên, không phụ thuộc suspend/param của Kotlin. */
    private fun method(name: String): Method =
        TestApi::class.java.declaredMethods.first { it.name == name }
}

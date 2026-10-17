package com.example.innerlamp.core.network.interceptor

import com.example.innerlamp.core.network.auth.AuthTokenProvider
import com.example.innerlamp.core.network.auth.Authenticated
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

/**
 * Gắn `Authorization: Bearer <token>` cho endpoint được đánh dấu [Authenticated].
 *
 * Đọc annotation qua `request.tag(Invocation::class.java)` mà Retrofit tự gắn vào
 * mỗi request. Endpoint không đánh dấu = public → chuyển request nguyên trạng.
 */
class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.tag(Invocation::class.java)?.method()
        val requiresAuth = method?.getAnnotation(Authenticated::class.java) != null
        if (!requiresAuth) return chain.proceed(request)

        val token = tokenProvider.currentToken()
        val authedRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            // Không có token → gửi trần, để server tự quyết định (thường trả 401).
            request
        }

        // 401 được `TokenRefreshAuthenticator` xử lý (refresh một lần rồi retry).
        return chain.proceed(authedRequest)
    }
}

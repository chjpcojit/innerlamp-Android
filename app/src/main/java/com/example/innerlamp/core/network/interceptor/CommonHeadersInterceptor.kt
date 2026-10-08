package com.example.innerlamp.core.network.interceptor

import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn header chung vào mọi request: `Accept`, `Content-Type`, app version, locale.
 */
class CommonHeadersInterceptor(
    private val appVersion: String,
    private val locale: String = Locale.getDefault().toLanguageTag(),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("X-App-Version", appVersion)
            .header("Accept-Language", locale)
            .build()
        return chain.proceed(request)
    }
}

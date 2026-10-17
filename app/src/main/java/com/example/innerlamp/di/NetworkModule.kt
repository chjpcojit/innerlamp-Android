package com.example.innerlamp.di

import com.example.innerlamp.BuildConfig
import com.example.innerlamp.core.network.JsonProvider
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkConfig
import com.example.innerlamp.core.network.NetworkErrorMapper
import com.example.innerlamp.core.network.RetrofitNetworkClient
import com.example.innerlamp.core.network.auth.AuthTokenProvider
import com.example.innerlamp.core.network.auth.TokenRefreshAuthenticator
import com.example.innerlamp.core.network.interceptor.AuthInterceptor
import com.example.innerlamp.core.network.interceptor.CommonHeadersInterceptor
import com.example.innerlamp.core.network.interceptor.LoggingInterceptor
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Wiring thủ công cho lớp network (không thêm thư viện DI).
 * Các provider dùng chung cho toàn app.
 */
object NetworkModule {

    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    fun provideJson(): Json = JsonProvider.json

    fun provideAuthTokenProvider(): AuthTokenProvider = AuthModule.tokenCache

    /** Refresh một lần khi 401 rồi retry; xem [TokenRefreshAuthenticator]. */
    fun provideTokenRefreshAuthenticator(): Authenticator = TokenRefreshAuthenticator(
        cache = AuthModule.tokenCache,
        refresher = AuthModule.tokenRefresher,
        events = AuthModule.sessionEvents,
        dispatchers = DefaultDispatcherProvider,
    )

    fun provideNetworkErrorMapper(json: Json = provideJson()): NetworkErrorMapper =
        NetworkErrorMapper(json)

    fun provideOkHttpClient(
        authTokenProvider: AuthTokenProvider = provideAuthTokenProvider(),
        appVersion: String = BuildConfig.VERSION_NAME,
        locale: String = Locale.getDefault().toLanguageTag(),
        refreshAuthenticator: Authenticator? = provideTokenRefreshAuthenticator(),
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConfig.connectTimeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(NetworkConfig.readTimeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(NetworkConfig.writeTimeoutMillis, TimeUnit.MILLISECONDS)
        .addInterceptor(CommonHeadersInterceptor(appVersion, locale))
        .addInterceptor(AuthInterceptor(authTokenProvider))
        .addInterceptor(LoggingInterceptor.create(NetworkConfig.isDebugLogging))
        .apply { refreshAuthenticator?.let { authenticator(it) } }
        .build()

    fun provideRetrofit(
        baseUrl: String = NetworkConfig.baseUrl,
        client: OkHttpClient = provideOkHttpClient(),
        json: Json = provideJson(),
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()

    fun provideNetworkClient(
        retrofit: Retrofit = provideRetrofit(),
        baseUrl: String = NetworkConfig.baseUrl,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
        errorMapper: NetworkErrorMapper = provideNetworkErrorMapper(),
    ): NetworkClient = RetrofitNetworkClient(
        retrofit = retrofit,
        baseUrl = baseUrl,
        dispatchers = dispatchers,
        errorMapper = errorMapper,
    )
}

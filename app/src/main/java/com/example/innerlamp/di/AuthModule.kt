package com.example.innerlamp.di

import android.content.Context
import android.os.Build
import com.example.innerlamp.BuildConfig
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.auth.RefreshOutcome
import com.example.innerlamp.core.network.auth.SessionEvents
import com.example.innerlamp.core.network.auth.SessionTokenCache
import com.example.innerlamp.core.network.auth.TokenRefresher
import com.example.innerlamp.data.remote.api.AuthApiService
import com.example.innerlamp.data.remote.api.RefreshApiService
import com.example.innerlamp.data.remote.api.RegistrationApiService
import com.example.innerlamp.data.remote.dto.DeviceInfoDto
import com.example.innerlamp.data.repository.AuthRepositoryImpl
import com.example.innerlamp.data.repository.AuthSessionStoreImpl
import com.example.innerlamp.data.repository.RegistrationRepositoryImpl
import com.example.innerlamp.data.repository.SessionTokenRefresher
import com.example.innerlamp.domain.repository.AccountDataCleaner
import com.example.innerlamp.domain.repository.AuthRepository
import com.example.innerlamp.domain.repository.AuthSessionStore
import com.example.innerlamp.domain.repository.RegistrationRepository
import com.example.innerlamp.domain.usecase.auth.ConfirmPasswordResetUseCase
import com.example.innerlamp.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.innerlamp.domain.usecase.auth.RequestRegistrationCodeUseCase
import com.example.innerlamp.domain.usecase.auth.ResendRegistrationCodeUseCase
import com.example.innerlamp.domain.usecase.auth.SignInUseCase
import com.example.innerlamp.domain.usecase.auth.SignOutUseCase
import com.example.innerlamp.domain.usecase.auth.VerifyRegistrationUseCase

/**
 * Manual wiring for sign-in, registration and the session. Call [init] once from
 * `Application.onCreate` before any network call; the token cache and events are process-wide
 * singletons shared with [NetworkModule]. /
 * Wiring thủ công cho đăng nhập, đăng ký và phiên. Gọi [init] một lần ở `Application.onCreate`
 * trước mọi network call; token cache và events là singleton dùng chung với [NetworkModule].
 */
object AuthModule {

    val tokenCache = SessionTokenCache()
    val sessionEvents = SessionEvents()

    @Volatile
    private var store: AuthSessionStore? = null

    @Volatile
    private var refresher: TokenRefresher? = null

    /** Delegates to the refresher built in [init]; no refresh is possible before that. / Uỷ quyền cho refresher dựng ở [init]. */
    val tokenRefresher: TokenRefresher = TokenRefresher { token ->
        refresher?.refresh(token) ?: RefreshOutcome.Failed
    }

    fun init(context: Context, cleaners: List<AccountDataCleaner> = emptyList()) {
        if (store != null) return
        val app = context.applicationContext
        val secure = StorageModule.provideSecureStorage(
            blobStore = StorageModule.provideSecureBlobStore(app),
            keyProvider = StorageModule.provideSecretKeyProvider(),
            authenticator = StorageModule.provideBiometricAuthenticator(CurrentActivityProvider),
            deviceLockChecker = StorageModule.provideDeviceLockChecker(app),
        )
        val newStore = AuthSessionStoreImpl(
            secure = secure,
            prefs = StorageModule.providePreferencesStorage(app),
            cache = tokenCache,
            cleaners = cleaners,
        )
        // The refresh call uses its own client: no authenticator, so a rejected refresh cannot recurse.
        // Refresh dùng client riêng không có authenticator để không đệ quy.
        val refreshClient = refreshNetworkClient()
        refresher = SessionTokenRefresher(
            api = refreshClient.createService(RefreshApiService::class.java),
            networkClient = refreshClient,
            store = newStore,
        )
        store = newStore
    }

    fun provideSessionStore(): AuthSessionStore =
        checkNotNull(store) { "AuthModule.init(context) must be called first / phải gọi AuthModule.init(context) trước" }

    fun provideAuthRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): AuthRepository = AuthRepositoryImpl(
        api = networkClient.createService(AuthApiService::class.java),
        networkClient = networkClient,
        device = deviceInfo(),
    )

    fun provideRegistrationRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): RegistrationRepository = RegistrationRepositoryImpl(
        api = networkClient.createService(RegistrationApiService::class.java),
        networkClient = networkClient,
    )

    fun provideSignInUseCase() = SignInUseCase(provideAuthRepository(), provideSessionStore())

    fun provideSignOutUseCase() = SignOutUseCase(provideAuthRepository(), provideSessionStore())

    fun provideRequestPasswordResetUseCase() = RequestPasswordResetUseCase(provideAuthRepository())

    fun provideConfirmPasswordResetUseCase() =
        ConfirmPasswordResetUseCase(provideAuthRepository(), provideSessionStore())

    fun provideRequestRegistrationCodeUseCase() = RequestRegistrationCodeUseCase(provideRegistrationRepository())

    fun provideResendRegistrationCodeUseCase() = ResendRegistrationCodeUseCase(provideRegistrationRepository())

    fun provideVerifyRegistrationUseCase() =
        VerifyRegistrationUseCase(provideRegistrationRepository(), provideSessionStore())

    private fun refreshNetworkClient(): NetworkClient = NetworkModule.provideNetworkClient(
        retrofit = NetworkModule.provideRetrofit(client = NetworkModule.provideOkHttpClient(refreshAuthenticator = null)),
    )

    private fun deviceInfo() = DeviceInfoDto(
        platform = "android",
        deviceModel = Build.MODEL,
        osVersion = Build.VERSION.RELEASE,
        appVersion = BuildConfig.VERSION_NAME,
    )
}

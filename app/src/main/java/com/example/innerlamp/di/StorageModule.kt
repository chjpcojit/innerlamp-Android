package com.example.innerlamp.di

import android.app.KeyguardManager
import android.content.Context
import com.example.innerlamp.core.storage.DefaultStorage
import com.example.innerlamp.core.storage.Storage
import com.example.innerlamp.core.storage.prefs.PreferencesStorage
import com.example.innerlamp.core.storage.prefs.SharedPreferencesStorage
import com.example.innerlamp.core.storage.secure.DefaultSecureStorage
import com.example.innerlamp.core.storage.secure.SecureStorage
import com.example.innerlamp.core.storage.secure.auth.ActivityProvider
import com.example.innerlamp.core.storage.secure.auth.AndroidBiometricAuthenticator
import com.example.innerlamp.core.storage.secure.auth.BiometricAuthenticator
import com.example.innerlamp.core.storage.secure.auth.DeviceLockChecker
import com.example.innerlamp.core.storage.secure.blob.SecureBlobStore
import com.example.innerlamp.core.storage.secure.blob.SharedPrefsSecureBlobStore
import com.example.innerlamp.core.storage.secure.crypto.AndroidSecretKeyProvider
import com.example.innerlamp.core.storage.secure.crypto.SecretKeyProvider
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider

/**
 * Wiring thủ công cho lớp lưu trữ (không thêm thư viện DI).
 */
object StorageModule {

    private const val PREFS_NAME = "innerlamp_prefs"
    private const val SECURE_PREFS_NAME = "innerlamp_secure"

    fun providePreferencesStorage(
        context: Context,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    ): PreferencesStorage = SharedPreferencesStorage(
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        dispatchers = dispatchers,
    )

    fun provideSecureBlobStore(context: Context): SecureBlobStore = SharedPrefsSecureBlobStore(
        preferences = context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE),
    )

    fun provideSecretKeyProvider(): SecretKeyProvider = AndroidSecretKeyProvider()

    fun provideDeviceLockChecker(context: Context): DeviceLockChecker {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return DeviceLockChecker { keyguardManager.isDeviceSecure }
    }

    fun provideBiometricAuthenticator(
        activityProvider: ActivityProvider,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    ): BiometricAuthenticator = AndroidBiometricAuthenticator(activityProvider, dispatchers)

    fun provideSecureStorage(
        blobStore: SecureBlobStore,
        keyProvider: SecretKeyProvider,
        authenticator: BiometricAuthenticator,
        deviceLockChecker: DeviceLockChecker,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    ): SecureStorage = DefaultSecureStorage(
        blobStore = blobStore,
        keyProvider = keyProvider,
        authenticator = authenticator,
        deviceLockChecker = deviceLockChecker,
        dispatchers = dispatchers,
    )

    fun provideStorage(
        prefs: PreferencesStorage,
        secure: SecureStorage,
    ): Storage = DefaultStorage(prefs = prefs, secure = secure)
}

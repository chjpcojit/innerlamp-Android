package com.example.innerlamp.core.storage.secure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.innerlamp.core.storage.secure.auth.AuthResult
import com.example.innerlamp.core.storage.secure.auth.BiometricAuthenticator
import com.example.innerlamp.core.storage.secure.auth.DeviceLockChecker
import com.example.innerlamp.core.storage.secure.blob.SharedPrefsSecureBlobStore
import com.example.innerlamp.core.storage.secure.crypto.AndroidSecretKeyProvider
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test cho [DefaultSecureStorage] trên AndroidKeyStore thật.
 * Chỉ kiểm tra `DEVICE_UNLOCKED` (không cần prompt); `USER_AUTH` cần thiết bị
 * có khoá màn hình + thao tác thủ công (xem tasks.md Phase 6).
 */
@RunWith(AndroidJUnit4::class)
class DefaultSecureStorageInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private class NoOpAuthenticator : BiometricAuthenticator {
        override suspend fun authenticate(reason: String): AuthResult = AuthResult.Success
    }

    private fun storage(): DefaultSecureStorage = DefaultSecureStorage(
        blobStore = SharedPrefsSecureBlobStore(
            context.getSharedPreferences("test_innerlamp_secure", Context.MODE_PRIVATE),
        ),
        keyProvider = AndroidSecretKeyProvider(),
        authenticator = NoOpAuthenticator(),
        deviceLockChecker = DeviceLockChecker { true },
        dispatchers = DefaultDispatcherProvider,
    )

    @Test
    fun deviceUnlocked_putGet_roundTrip_withoutPrompt() = runBlocking {
        val storage = storage()
        assertEquals(Result.success(Unit), storage.put(SecureLevel.DEVICE_UNLOCKED, "k", "secret"))
        assertEquals("secret", storage.get(SecureLevel.DEVICE_UNLOCKED, "k").getOrNull())
    }
}

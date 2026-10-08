package com.example.innerlamp.core.storage.secure

import com.example.innerlamp.core.storage.secure.auth.AuthResult
import com.example.innerlamp.core.storage.secure.auth.BiometricAuthenticator
import com.example.innerlamp.core.storage.secure.auth.DeviceLockChecker
import com.example.innerlamp.core.storage.secure.blob.SecureBlobStore
import com.example.innerlamp.core.storage.secure.crypto.SecretKeyProvider
import com.example.innerlamp.core.util.DispatcherProvider
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JVM cho [DefaultSecureStorage] với các double giả cho `SecureBlobStore`,
 * `SecretKeyProvider`, `BiometricAuthenticator` và `DeviceLockChecker`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSecureStorageTest {

    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher,
    ) : DispatcherProvider {
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val main: CoroutineDispatcher get() = dispatcher
    }

    private class FakeSecureBlobStore : SecureBlobStore {
        private val map = mutableMapOf<String, String>()
        override fun read(key: String): String? = map[key]
        override fun write(key: String, encoded: String) { map[key] = encoded }
        override fun delete(key: String) { map.remove(key) }
        override fun keys(): Set<String> = map.keys.toSet()
        override fun clearAll() { map.clear() }
        fun size(): Int = map.size
        fun storedKeys(): Set<String> = map.keys.toSet()
    }

    private class FakeSecretKeyProvider : SecretKeyProvider {
        private val key: SecretKey =
            KeyGenerator.getInstance("AES").run { init(256); generateKey() }
        val deletedAliases = mutableListOf<SecureLevel>()
        override fun getOrCreate(level: SecureLevel): SecretKey = key
        override fun deleteAlias(level: SecureLevel) { deletedAliases.add(level) }
    }

    private class FakeBiometricAuthenticator(var result: AuthResult) : BiometricAuthenticator {
        var calls = 0
        override suspend fun authenticate(reason: String): AuthResult {
            calls++
            return result
        }
    }

    private class FakeDeviceLockChecker(var secure: Boolean) : DeviceLockChecker {
        override fun isDeviceSecure(): Boolean = secure
    }

    private fun build(
        dispatcher: CoroutineDispatcher,
        blobStore: FakeSecureBlobStore = FakeSecureBlobStore(),
        keyProvider: FakeSecretKeyProvider = FakeSecretKeyProvider(),
        authenticator: FakeBiometricAuthenticator = FakeBiometricAuthenticator(AuthResult.Success),
        deviceLockChecker: FakeDeviceLockChecker = FakeDeviceLockChecker(secure = true),
    ): DefaultSecureStorage = DefaultSecureStorage(
        blobStore = blobStore,
        keyProvider = keyProvider,
        authenticator = authenticator,
        deviceLockChecker = deviceLockChecker,
        dispatchers = TestDispatcherProvider(dispatcher),
    )

    @Test
    fun deviceUnlocked_putGet_roundTrip_withoutPrompt() = runTest {
        val blobStore = FakeSecureBlobStore()
        val authenticator = FakeBiometricAuthenticator(AuthResult.Success)
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            blobStore = blobStore,
            authenticator = authenticator,
        )

        assertEquals(Result.success(Unit), storage.put(SecureLevel.DEVICE_UNLOCKED, "k", "secret"))
        assertEquals(Result.success("secret"), storage.get(SecureLevel.DEVICE_UNLOCKED, "k"))
        // DEVICE_UNLOCKED không hiện prompt.
        assertEquals(0, authenticator.calls)
    }

    @Test
    fun get_missingKey_returnsNotFound() = runTest {
        val storage = build(dispatcher = StandardTestDispatcher(testScheduler))

        assertEquals(
            Result.failure<String>(StorageError.NotFound),
            storage.get(SecureLevel.DEVICE_UNLOCKED, "missing"),
        )
    }

    @Test
    fun putUserAuth_withoutDeviceLock_returnsDeviceLockRequired_andWritesNothing() = runTest {
        val blobStore = FakeSecureBlobStore()
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            blobStore = blobStore,
            deviceLockChecker = FakeDeviceLockChecker(secure = false),
        )

        assertEquals(
            Result.failure<Unit>(StorageError.DeviceLockRequired),
            storage.put(SecureLevel.USER_AUTH, "k", "secret"),
        )
        assertEquals(0, blobStore.size())
    }

    @Test
    fun getUserAuth_authenticatorUnavailable_returnsAuthenticationUnavailable() = runTest {
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            authenticator = FakeBiometricAuthenticator(AuthResult.Unavailable),
        )
        storage.put(SecureLevel.USER_AUTH, "k", "secret")

        assertEquals(
            Result.failure<String>(StorageError.AuthenticationUnavailable),
            storage.get(SecureLevel.USER_AUTH, "k"),
        )
    }

    @Test
    fun getUserAuth_authenticatorCancelled_returnsUserCancelled() = runTest {
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            authenticator = FakeBiometricAuthenticator(AuthResult.Cancelled),
        )
        storage.put(SecureLevel.USER_AUTH, "k", "secret")

        assertEquals(
            Result.failure<String>(StorageError.UserCancelled),
            storage.get(SecureLevel.USER_AUTH, "k"),
        )
    }

    @Test
    fun getUserAuth_authenticatorFailed_returnsAuthenticationFailed() = runTest {
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            authenticator = FakeBiometricAuthenticator(AuthResult.Failed),
        )
        storage.put(SecureLevel.USER_AUTH, "k", "secret")

        assertEquals(
            Result.failure<String>(StorageError.AuthenticationFailed),
            storage.get(SecureLevel.USER_AUTH, "k"),
        )
    }

    @Test
    fun getUserAuth_deviceBecomesInsecure_reconcilesAndReturnsKeyInvalidated() = runTest {
        val blobStore = FakeSecureBlobStore()
        val keyProvider = FakeSecretKeyProvider()
        val deviceLockChecker = FakeDeviceLockChecker(secure = true)
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            blobStore = blobStore,
            keyProvider = keyProvider,
            deviceLockChecker = deviceLockChecker,
        )

        storage.put(SecureLevel.USER_AUTH, "k", "secret")
        assertTrue(blobStore.storedKeys().any { it.startsWith("USER_AUTH:") })

        // Gỡ PIN sau khi đã lưu.
        deviceLockChecker.secure = false

        assertEquals(
            Result.failure<String>(StorageError.KeyInvalidated),
            storage.get(SecureLevel.USER_AUTH, "k"),
        )

        // Blob USER_AUTH bị xoá + alias khoá bị xoá.
        assertTrue(blobStore.storedKeys().none { it.startsWith("USER_AUTH:") })
        assertTrue(keyProvider.deletedAliases.contains(SecureLevel.USER_AUTH))
    }

    @Test
    fun clear_userAuth_doesNotAffectDeviceUnlocked() = runTest {
        val blobStore = FakeSecureBlobStore()
        val storage = build(
            dispatcher = StandardTestDispatcher(testScheduler),
            blobStore = blobStore,
        )
        storage.put(SecureLevel.DEVICE_UNLOCKED, "du", "a")
        storage.put(SecureLevel.USER_AUTH, "ua", "b")

        storage.clear(SecureLevel.USER_AUTH)

        assertTrue(blobStore.storedKeys().none { it.startsWith("USER_AUTH:") })
        assertTrue(blobStore.storedKeys().any { it.startsWith("DEVICE_UNLOCKED:") })
    }
}

package com.example.innerlamp.core.storage.secure

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.example.innerlamp.core.storage.secure.auth.AuthResult
import com.example.innerlamp.core.storage.secure.auth.BiometricAuthenticator
import com.example.innerlamp.core.storage.secure.auth.DeviceLockChecker
import com.example.innerlamp.core.storage.secure.blob.SecureBlobStore
import com.example.innerlamp.core.storage.secure.crypto.AesGcmCodec
import com.example.innerlamp.core.storage.secure.crypto.SecretKeyProvider
import com.example.innerlamp.core.util.DispatcherProvider
import java.security.GeneralSecurityException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Ghép các bước ghi/đọc secret: kiểm tra khoá bảo vệ → lấy khoá → (prompt nếu
 * `USER_AUTH`) → crypto → blob store. Luôn prompt trước khi giải mã `USER_AUTH`;
 * map exception KeyStore → [StorageError]; chạy trên `DispatcherProvider.io`.
 *
 * Blob được namespace theo level (`"$LEVEL:$key"`) nên `clear(level)`/
 * `reconcileUserAuth()` chỉ xoá đúng level, không ảnh hưởng level còn lại.
 */
class DefaultSecureStorage(
    private val blobStore: SecureBlobStore,
    private val keyProvider: SecretKeyProvider,
    private val authenticator: BiometricAuthenticator,
    private val deviceLockChecker: DeviceLockChecker,
    private val dispatchers: DispatcherProvider,
) : SecureStorage {

    override suspend fun put(level: SecureLevel, key: String, value: String): Result<Unit> =
        withContext(dispatchers.io) {
            if (level == SecureLevel.USER_AUTH && !deviceLockChecker.isDeviceSecure()) {
                return@withContext Result.failure(StorageError.DeviceLockRequired)
            }
            try {
                val secretKey = keyProvider.getOrCreate(level)
                val encoded = AesGcmCodec.encrypt(value.toByteArray(Charsets.UTF_8), secretKey)
                blobStore.write(storageKey(level, key), encoded)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: KeyPermanentlyInvalidatedException) {
                reconcileUserAuth()
                Result.failure(StorageError.KeyInvalidated)
            } catch (e: GeneralSecurityException) {
                Result.failure(StorageError.CryptoFailure(e))
            } catch (e: Exception) {
                Result.failure(StorageError.StorageFailure(e))
            }
        }

    override suspend fun get(level: SecureLevel, key: String): Result<String> =
        withContext(dispatchers.io) {
            if (level == SecureLevel.USER_AUTH && !deviceLockChecker.isDeviceSecure()) {
                // Khoá bảo vệ đã bị gỡ sau khi lưu → dọn dẹp fail-closed.
                reconcileUserAuth()
                return@withContext Result.failure(StorageError.KeyInvalidated)
            }

            val encoded = blobStore.read(storageKey(level, key))
                ?: return@withContext Result.failure(StorageError.NotFound)

            if (level == SecureLevel.USER_AUTH) {
                when (authenticator.authenticate("Mở dữ liệu bảo mật")) {
                    AuthResult.Success -> Unit
                    AuthResult.Cancelled ->
                        return@withContext Result.failure(StorageError.UserCancelled)
                    AuthResult.Unavailable ->
                        return@withContext Result.failure(StorageError.AuthenticationUnavailable)
                    AuthResult.Failed ->
                        return@withContext Result.failure(StorageError.AuthenticationFailed)
                }
            }

            try {
                val secretKey = keyProvider.getOrCreate(level)
                val plaintext = AesGcmCodec.decrypt(encoded, secretKey)
                Result.success(plaintext.toString(Charsets.UTF_8))
            } catch (e: CancellationException) {
                throw e
            } catch (e: KeyPermanentlyInvalidatedException) {
                reconcileUserAuth()
                Result.failure(StorageError.KeyInvalidated)
            } catch (e: GeneralSecurityException) {
                Result.failure(StorageError.CryptoFailure(e))
            } catch (e: IllegalArgumentException) {
                // base64 hỏng → dữ liệu hỏng, coi như crypto failure (không lộ chi tiết).
                Result.failure(StorageError.CryptoFailure(e))
            } catch (e: Exception) {
                Result.failure(StorageError.StorageFailure(e))
            }
        }

    override suspend fun delete(key: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            blobStore.delete(storageKey(SecureLevel.DEVICE_UNLOCKED, key))
            blobStore.delete(storageKey(SecureLevel.USER_AUTH, key))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageError.StorageFailure(e))
        }
    }

    override suspend fun clear(level: SecureLevel): Result<Unit> = withContext(dispatchers.io) {
        try {
            val prefix = "${level.name}:"
            blobStore.keys()
                .filter { it.startsWith(prefix) }
                .forEach { blobStore.delete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageError.StorageFailure(e))
        }
    }

    override suspend fun isAvailable(level: SecureLevel): Boolean = withContext(dispatchers.io) {
        when (level) {
            SecureLevel.DEVICE_UNLOCKED -> true
            SecureLevel.USER_AUTH -> deviceLockChecker.isDeviceSecure()
        }
    }

    /**
     * Dọn dẹp fail-closed khi khoá bảo vệ `USER_AUTH` bị gỡ: xoá toàn bộ blob
     * `USER_AUTH` + xoá alias khoá trong AndroidKeyStore. Blob `DEVICE_UNLOCKED`
     * không bị ảnh hưởng.
     */
    private fun reconcileUserAuth() {
        runCatching {
            val prefix = "${SecureLevel.USER_AUTH.name}:"
            blobStore.keys()
                .filter { it.startsWith(prefix) }
                .forEach { blobStore.delete(it) }
            keyProvider.deleteAlias(SecureLevel.USER_AUTH)
        }
    }

    private fun storageKey(level: SecureLevel, key: String): String = "${level.name}:$key"
}

package com.example.innerlamp.core.storage.secure.auth

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.innerlamp.core.util.DispatcherProvider
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Cài đặt [BiometricAuthenticator] bằng `androidx.biometric.BiometricPrompt`.
 *
 * - Không dùng `CryptoObject` (một đường code duy nhất cho cả sinh trắc học lẫn PIN).
 * - Không gọi `setNegativeButtonText()` cùng với `DEVICE_CREDENTIAL`.
 * - Không có Activity → trả [AuthResult.Unavailable].
 */
class AndroidBiometricAuthenticator(
    private val activityProvider: ActivityProvider,
    private val dispatchers: DispatcherProvider,
) : BiometricAuthenticator {

    override suspend fun authenticate(reason: String): AuthResult {
        val activity = activityProvider.current() ?: return AuthResult.Unavailable
        return withContext(dispatchers.main) {
            suspendCancellableCoroutine { continuation ->
                val prompt = BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult,
                        ) {
                            if (continuation.isActive) continuation.resume(AuthResult.Success)
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            if (continuation.isActive) continuation.resume(mapError(errorCode))
                        }
                    },
                )

                continuation.invokeOnCancellation { prompt.cancelAuthentication() }

                prompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle(reason)
                        .applyAllowedAuthenticators()
                        .build(),
                )
            }
        }
    }

    private fun mapError(errorCode: Int): AuthResult = when (errorCode) {
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        -> AuthResult.Cancelled

        else -> AuthResult.Failed
    }

    @Suppress("DEPRECATION")
    private fun BiometricPrompt.PromptInfo.Builder.applyAllowedAuthenticators() = apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
        } else {
            setDeviceCredentialAllowed(true)
        }
    }
}

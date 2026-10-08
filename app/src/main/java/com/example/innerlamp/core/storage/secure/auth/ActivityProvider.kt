package com.example.innerlamp.core.storage.secure.auth

import androidx.fragment.app.FragmentActivity

/**
 * Cung cấp Activity hiện tại để `BiometricPrompt` hiển thị prompt.
 * Không có Activity → null ⇒ đọc `USER_AUTH` trả `AuthenticationUnavailable`.
 */
fun interface ActivityProvider {
    fun current(): FragmentActivity?
}

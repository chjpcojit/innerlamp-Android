package com.example.innerlamp.core.storage.secure.auth

/**
 * Kiểm tra thiết bị có khoá màn hình (PIN/pattern) hay không. Tách khỏi
 * `KeyguardManager` để unit test JVM chạy được.
 */
fun interface DeviceLockChecker {
    fun isDeviceSecure(): Boolean
}

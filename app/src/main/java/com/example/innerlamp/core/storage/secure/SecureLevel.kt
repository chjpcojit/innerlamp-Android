package com.example.innerlamp.core.storage.secure

/**
 * Cấp bảo mật của một secret trong [SecureStorage].
 *
 * - [DEVICE_UNLOCKED]: đọc được ngay khi máy đã mở khoá, không hiện prompt.
 * - [USER_AUTH]: mỗi lần đọc đều phải xác thực lại (sinh trắc học Class 3
 *   hoặc PIN/pattern của điện thoại).
 */
enum class SecureLevel {
    DEVICE_UNLOCKED,
    USER_AUTH,
}

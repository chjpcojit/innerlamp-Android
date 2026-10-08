package com.example.innerlamp.core.storage

import com.example.innerlamp.core.storage.prefs.PreferencesStorage
import com.example.innerlamp.core.storage.secure.SecureStorage

/**
 * Mặt tiền lưu trữ duy nhất mà tầng trên gọi vào:
 *
 * - [prefs]  — key-value không nhạy cảm (SharedPreferences).
 * - [secure] — secret mã hoá AES-256-GCM (AndroidKeyStore), chia 2 cấp.
 */
interface Storage {
    val prefs: PreferencesStorage
    val secure: SecureStorage
}

/** Cài đặt mặc định của [Storage]. */
class DefaultStorage(
    override val prefs: PreferencesStorage,
    override val secure: SecureStorage,
) : Storage

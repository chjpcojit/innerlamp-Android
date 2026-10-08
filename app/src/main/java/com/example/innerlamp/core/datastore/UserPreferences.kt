package com.example.innerlamp.core.datastore

import com.example.innerlamp.core.storage.prefs.PreferencesStorage
import kotlinx.coroutines.flow.Flow

/**
 * Cấu hình nhẹ, không quan hệ (onboarding, pattern mặc định…). Đã chuyển từ
 * DataStore sang dùng [PreferencesStorage] (SharedPreferences). Dữ liệu có cấu
 * trúc (session history) đi qua Room, không đi qua đây.
 */
class UserPreferences(private val storage: PreferencesStorage) {

    val onboardingCompleted: Flow<Boolean> =
        storage.observeBoolean(KEY_ONBOARDING_DONE, default = false)

    val defaultPatternId: Flow<String?> =
        storage.observeString(KEY_DEFAULT_PATTERN, default = null)

    suspend fun setOnboardingCompleted(completed: Boolean) {
        storage.putBoolean(KEY_ONBOARDING_DONE, completed)
    }

    suspend fun setDefaultPatternId(patternId: String) {
        storage.putString(KEY_DEFAULT_PATTERN, patternId)
    }

    private companion object {
        const val KEY_ONBOARDING_DONE = "onboarding_completed"
        const val KEY_DEFAULT_PATTERN = "default_pattern_id"
    }
}

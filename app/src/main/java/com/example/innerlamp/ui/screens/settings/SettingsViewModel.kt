package com.example.innerlamp.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn Cài đặt (`settings`).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Các switch/cỡ chữ chỉ đổi state UI cục bộ; chưa ghi xuống storage vì data
 *   layer chưa wiring. TODO(il-data-builder): nối vào use case lưu cài đặt
 *   (prefs) khi data layer có.
 *
 * @author innerlamp
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            user = SettingsSampleData.user,
            phone = SettingsSampleData.PHONE,
            reminderEnabled = true,
            reminderSummary = SettingsSampleData.REMINDER_SUMMARY,
            fastingReminderEnabled = true,
            bellSound = SettingsSampleData.BELL_SOUND,
            fontSizeIndex = 1,
            bilingualEnabled = false,
            themeLabel = "Sáng",
            downloadsSummary = SettingsSampleData.DOWNLOADS_SUMMARY,
            hideJournalEnabled = true,
            appVersion = SettingsSampleData.APP_VERSION,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Bật/tắt chuông tỉnh thức. */
    fun toggleReminder() = _uiState.update { it.copy(reminderEnabled = !it.reminderEnabled) }

    /** Bật/tắt nhắc ngày trai & rằm. */
    fun toggleFastingReminder() =
        _uiState.update { it.copy(fastingReminderEnabled = !it.fastingReminderEnabled) }

    /** Bật/tắt song ngữ Việt – Anh. */
    fun toggleBilingual() = _uiState.update { it.copy(bilingualEnabled = !it.bilingualEnabled) }

    /** Bật/tắt ẩn nhật ký khỏi đạo tràng. */
    fun toggleHideJournal() =
        _uiState.update { it.copy(hideJournalEnabled = !it.hideJournalEnabled) }

    /** Đặt cỡ chữ kinh văn (0..[SettingsFontSizeSteps] - 1). */
    fun onFontSizeChange(index: Int) {
        _uiState.update { it.copy(fontSizeIndex = index.coerceIn(0, SettingsFontSizeSteps - 1)) }
    }
}

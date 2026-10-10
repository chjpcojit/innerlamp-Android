package com.example.innerlamp.ui.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel cho màn Hồ sơ (`profile` / tab "Tôi").
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Dữ liệu hiện lấy từ [ProfileSampleData] vì chưa có repository người dùng.
 *   TODO(il-data-builder): nối vào use case `GetProfile` khi data layer có.
 *
 * @author innerlamp
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            user = ProfileSampleData.user,
            stats = ProfileSampleData.stats,
            journalLoggedDays = ProfileSampleData.JOURNAL_LOGGED_DAYS,
            journalTotalDays = ProfileSampleData.JOURNAL_TOTAL_DAYS,
            heat = ProfileSampleData.heat,
            badges = ProfileSampleData.badges,
            merit = ProfileSampleData.merit,
            reminderSummary = ProfileSampleData.REMINDER_SUMMARY,
            templeName = ProfileSampleData.TEMPLE_NAME,
        ),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}

package com.example.innerlamp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn Hồ sơ (`profile` / tab "Tôi").
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Dữ liệu hiện lấy từ [ProfileSampleData] vì chưa có repository người dùng.
 *   TODO(il-data-builder): nối vào use case `GetProfile` khi data layer có.
 * - [signOut] gọi [SignOutUseCase] (kết quả bỏ qua, kể cả offline) rồi báo screen quay về Login.
 *
 * @author innerlamp
 */
class ProfileViewModel(
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

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

    /** Đăng xuất: xoá phiên và dữ liệu tài khoản trên máy rồi gọi [onDone]. */
    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onDone()
        }
    }
}

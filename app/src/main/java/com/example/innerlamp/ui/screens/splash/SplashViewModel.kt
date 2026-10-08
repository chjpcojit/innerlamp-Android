package com.example.innerlamp.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel màn splash. Chỉ giữ một bộ đếm thời gian: sau [SplashDurationMs]
 * thì phát tín hiệu [SplashUiState.finished] để màn hình chuyển tiếp.
 *
 * Splash không cần dữ liệu từ tầng data — thời lượng hiển thị là chi tiết UI.
 * Nếu sau này cần chặn splash để kiểm tra phiên đăng nhập (restore token),
 * inject use case tương ứng vào đây thay cho `delay` thuần.
 */
class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SplashDurationMs)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

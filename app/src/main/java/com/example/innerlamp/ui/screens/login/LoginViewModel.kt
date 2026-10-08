package com.example.innerlamp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Đăng nhập (login).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Validate form: số điện thoại đủ số theo quốc gia, mật khẩu ≥ 8 ký tự.
 * - Chưa có use case/repository đăng nhập (data layer chưa wiring) nên [onLoginClick]
 *   đang giả lập thành công, giống `SignupViewModel`.
 *
 * @author innerlamp
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Đặt số điện thoại. */
    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phoneRaw = value) }
    }

    /** Đặt quốc gia (mã ISO) cho ô số điện thoại. */
    fun onCountrySelect(isoCode: String) {
        _uiState.update { it.copy(selectedCountryIso = isoCode) }
    }

    /** Đặt mật khẩu. */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    /** Gửi yêu cầu đăng nhập bằng số điện thoại + mật khẩu. */
    fun onLoginClick() {
        val current = _uiState.value
        if (!current.canSubmit) return
        _uiState.update { it.copy(isLoading = true) }
        // TODO(il-data-builder): thay bằng use case đăng nhập khi AuthRepository được wiring.
        // Hiện giả lập thành công sau một khoảng trễ ngắn.
        viewModelScope.launch {
            delay(600)
            _uiState.update { it.copy(isLoading = false, loggedIn = true) }
        }
    }

    /** Screen đã điều hướng sau khi đăng nhập xong — hạ cờ để không điều hướng lặp lại. */
    fun onLoggedInHandled() {
        _uiState.update { it.copy(loggedIn = false) }
    }
}

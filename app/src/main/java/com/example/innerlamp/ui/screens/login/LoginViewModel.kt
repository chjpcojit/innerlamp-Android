package com.example.innerlamp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.EmailAddress
import com.example.innerlamp.domain.usecase.auth.SignInUseCase
import com.example.innerlamp.ui.screens.auth.localTimeOf
import com.example.innerlamp.ui.screens.auth.toAuthError
import com.example.innerlamp.ui.screens.auth.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the email + password Login screen. / ViewModel cho màn Đăng nhập bằng email + mật khẩu.
 *
 * - Validates on submit (empty / malformed email, empty password); editing a field clears its error.
 * - Calls [SignInUseCase]; the lock after 5 failures is decided by the server (`LOGIN_LOCKED`).
 * - A failed sign-in keeps the email and clears the password.
 *
 * @author innerlamp
 */
class LoginViewModel(
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Đặt email; xoá lỗi email và lỗi sai thông tin đăng nhập. */
    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, passwordError = it.passwordError.takeIf { e -> e == EMPTY_PASSWORD })
        }
    }

    /** Đặt mật khẩu; xoá lỗi dưới ô mật khẩu. */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    /** Hiện thông báo đầu form (khi bị đưa về do hết phiên / vừa đổi mật khẩu). */
    fun showNote(note: LoginNote?) {
        _uiState.update { it.copy(note = note) }
    }

    /** Điền sẵn email (sau khi đổi mật khẩu) nếu người dùng chưa gõ gì. */
    fun prefillEmail(email: String?) {
        if (email.isNullOrBlank()) return
        _uiState.update { if (it.email.isEmpty()) it.copy(email = email) else it }
    }

    /** Gửi yêu cầu đăng nhập; cũng là hành động của link "Thử lại" trên banner mất mạng. */
    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return

        val email = current.email.trim()
        val emailError = when {
            email.isEmpty() -> "Vui lòng nhập email."
            !EmailAddress.isValid(email) -> "Email chưa đúng định dạng, ví dụ: ten@email.com."
            else -> null
        }
        val passwordError = if (current.password.isEmpty()) EMPTY_PASSWORD else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, banner = null) }
            return
        }

        _uiState.update { it.copy(emailError = null, passwordError = null, banner = null, isLoading = true) }
        viewModelScope.launch {
            signIn(email, current.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, note = null, password = "", loggedIn = true) }
                }
                .onFailure { showError(it.toAuthError()) }
        }
    }

    /** Screen đã điều hướng sau khi đăng nhập xong — hạ cờ để không điều hướng lặp lại. */
    fun onLoggedInHandled() {
        _uiState.update { it.copy(loggedIn = false) }
    }

    private fun showError(error: AuthError) {
        _uiState.update {
            val cleared = it.copy(isLoading = false, password = "")
            when (error) {
                AuthError.InvalidCredentials -> cleared.copy(passwordError = error.userMessage())
                is AuthError.LoginLocked ->
                    cleared.copy(banner = LoginBanner.Locked(localTimeOf(error.lockedUntil) ?: "ít phút"))
                AuthError.NoConnection, AuthError.Timeout -> cleared.copy(banner = LoginBanner.Network)
                AuthError.InvalidEmail -> cleared.copy(emailError = error.userMessage())
                else -> cleared.copy(banner = LoginBanner.Message("Không đăng nhập được", error.userMessage()))
            }
        }
    }

    private companion object {
        const val EMPTY_PASSWORD = "Vui lòng nhập mật khẩu."
    }
}

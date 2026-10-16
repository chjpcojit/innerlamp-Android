package com.example.innerlamp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.EmailAddress
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Đăng nhập (login) bằng email + mật khẩu.
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Validate khi bấm gửi (email trống/sai định dạng, mật khẩu trống); sửa ô nào thì xoá lỗi ô đó.
 * - Sai mật khẩu 5 lần liên tiếp → tạm khoá 15 phút (banner "Tạm khóa đăng nhập").
 * - Chưa có use case/repository đăng nhập (data layer chưa wiring) nên [authenticate] đang
 *   giả lập thành công.
 *
 * @param now đồng hồ (ms) — tách ra để test được khoá đăng nhập.
 * @author innerlamp
 */
class LoginViewModel(
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Số lần sai liên tiếp; reset khi đăng nhập được hoặc khi bị khoá. */
    private var failCount = 0

    /** Mốc hết khoá (ms); 0 = không khoá. */
    private var lockedUntil = 0L

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

    /** Gửi yêu cầu đăng nhập; cũng là hành động của link "Thử lại" trên banner mất mạng. */
    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return
        if (lockedUntil > now()) return
        if (current.isLocked) _uiState.update { it.copy(banner = null) } // hết hạn khoá

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
            when (authenticate(email, current.password)) {
                LoginOutcome.Success -> {
                    failCount = 0
                    _uiState.update { it.copy(isLoading = false, note = null, password = "", loggedIn = true) }
                }
                LoginOutcome.InvalidCredentials -> {
                    failCount++
                    if (failCount >= MAX_FAILS) lock() else _uiState.update {
                        it.copy(isLoading = false, password = "", passwordError = "Email hoặc mật khẩu không đúng.")
                    }
                }
                LoginOutcome.Locked -> lock()
                LoginOutcome.Network -> _uiState.update { it.copy(isLoading = false, banner = LoginBanner.Network) }
            }
        }
    }

    /** Screen đã điều hướng sau khi đăng nhập xong — hạ cờ để không điều hướng lặp lại. */
    fun onLoggedInHandled() {
        _uiState.update { it.copy(loggedIn = false) }
    }

    private fun lock() {
        failCount = 0
        lockedUntil = now() + LOCK_MILLIS
        val label = Instant.ofEpochMilli(lockedUntil).atZone(ZoneId.systemDefault()).format(HOUR_MINUTE)
        _uiState.update { it.copy(isLoading = false, password = "", banner = LoginBanner.Locked(label)) }
    }

    // TODO(il-data-builder): thay bằng use case đăng nhập (email + mật khẩu) khi AuthRepository
    // được wiring; map lỗi domain sang [LoginOutcome]. Hiện giả lập thành công sau 800ms.
    private suspend fun authenticate(email: String, password: String): LoginOutcome {
        delay(800)
        return LoginOutcome.Success
    }

    /** Kết quả gọi đăng nhập, đã quy về các nhánh UI của prototype. */
    private enum class LoginOutcome { Success, InvalidCredentials, Locked, Network }

    private companion object {
        const val EMPTY_PASSWORD = "Vui lòng nhập mật khẩu."
        const val MAX_FAILS = 5
        const val LOCK_MILLIS = 15 * 60 * 1000L
        val HOUR_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

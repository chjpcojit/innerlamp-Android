package com.example.innerlamp.ui.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.usecase.auth.ConfirmPasswordResetUseCase
import com.example.innerlamp.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.innerlamp.ui.screens.auth.CodeCountdown
import com.example.innerlamp.ui.screens.auth.toAuthError
import com.example.innerlamp.ui.screens.auth.userMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String,
    val code: String = "",
    val newPassword: String = "",
    val codeError: String? = null,
    val passwordError: String? = null,
    val expirySeconds: Int = 0,
    val resendSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val locked: Boolean = false,
    /** Password changed; the screen goes to Login once. / Đã đổi mật khẩu; screen về Login một lần. */
    val done: Boolean = false,
)

/** Forgot password step 2: code + new password, resend. / Quên mật khẩu bước 2: mã + mật khẩu mới, gửi lại. */
class ResetPasswordViewModel(
    email: String,
    timing: CodeTiming,
    private val requestReset: RequestPasswordResetUseCase,
    private val confirmReset: ConfirmPasswordResetUseCase,
    private val countdown: CodeCountdown = CodeCountdown(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = email))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    init {
        countdown.start(timing)
        tick()
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    fun onCodeChange(value: String) =
        _uiState.update { it.copy(code = value.filter(Char::isDigit).take(CODE_LENGTH), codeError = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value, passwordError = null) }

    fun onConfirm() {
        val s = _uiState.value
        if (s.isSubmitting || s.locked) return
        if (s.code.length != CODE_LENGTH) {
            _uiState.update { it.copy(codeError = "Vui lòng nhập đủ 6 chữ số.") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, codeError = null, passwordError = null) }
        viewModelScope.launch {
            confirmReset(s.email, s.code, s.newPassword)
                .onSuccess { _uiState.update { it.copy(isSubmitting = false, done = true) } }
                .onFailure { fail(it.toAuthError()) }
        }
    }

    fun onResend() {
        val s = _uiState.value
        if (s.isResending || s.resendSeconds > 0) return
        _uiState.update { it.copy(isResending = true, codeError = null) }
        viewModelScope.launch {
            requestReset(s.email)
                .onSuccess { timing ->
                    countdown.start(timing)
                    _uiState.update { it.copy(isResending = false, code = "", locked = false) }
                    tick()
                }
                .onFailure { e ->
                    val error = e.toAuthError()
                    if (error is AuthError.OtpResendTooSoon) countdown.restartResend(error.retryAfterSeconds ?: 60)
                    _uiState.update { it.copy(isResending = false, codeError = error.userMessage()) }
                    tick()
                }
        }
    }

    fun onDoneHandled() = _uiState.update { it.copy(done = false) }

    /** Keep the code, clear the new password, show the error. / Giữ mã, xoá mật khẩu mới, hiện lỗi. */
    private fun fail(error: AuthError) {
        _uiState.update {
            val base = it.copy(isSubmitting = false, newPassword = "")
            when (error) {
                AuthError.InvalidPassword -> base.copy(passwordError = error.userMessage())
                is AuthError.OtpLocked -> base.copy(locked = true, codeError = error.userMessage())
                else -> base.copy(codeError = error.userMessage())
            }
        }
    }

    private fun tick() {
        _uiState.update { it.copy(expirySeconds = countdown.expirySeconds(), resendSeconds = countdown.resendSeconds()) }
    }

    private companion object {
        const val CODE_LENGTH = 6
    }
}

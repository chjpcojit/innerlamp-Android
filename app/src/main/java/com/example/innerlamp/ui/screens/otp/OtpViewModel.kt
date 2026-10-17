package com.example.innerlamp.ui.screens.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.usecase.auth.ResendRegistrationCodeUseCase
import com.example.innerlamp.domain.usecase.auth.VerifyRegistrationUseCase
import com.example.innerlamp.ui.screens.auth.CodeCountdown
import com.example.innerlamp.ui.screens.auth.toAuthError
import com.example.innerlamp.ui.screens.auth.userMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State of the registration code screen. / State của màn nhập mã đăng ký. */
data class OtpUiState(
    val email: String,
    val code: String = "",
    val error: String? = null,
    val expirySeconds: Int = 0,
    val resendSeconds: Int = 0,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    /** `OTP_LOCKED`: input disabled. / Khoá nhập mã. */
    val locked: Boolean = false,
    /** Confirmed; the screen navigates once and clears it. / Đã xác nhận; screen điều hướng một lần. */
    val verified: Boolean = false,
    /** The flow must go back to Signup with this error. / Cần quay về Signup kèm lỗi này. */
    val exit: AuthError? = null,
)

/**
 * Code entry after registration: verify, resend, two server-driven countdowns. /
 * Nhập mã sau khi đăng ký: xác nhận, gửi lại, hai bộ đếm theo server.
 */
class OtpViewModel(
    email: String,
    timing: CodeTiming,
    private val verify: VerifyRegistrationUseCase,
    private val resend: ResendRegistrationCodeUseCase,
    private val countdown: CodeCountdown = CodeCountdown(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState(email = email))
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

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

    fun onCodeChange(value: String) {
        val digits = value.filter(Char::isDigit).take(CODE_LENGTH)
        _uiState.update { it.copy(code = digits, error = null) }
    }

    fun onConfirm() {
        val s = _uiState.value
        if (s.isVerifying || s.locked) return
        if (s.code.length != CODE_LENGTH) {
            _uiState.update { it.copy(error = "Vui lòng nhập đủ 6 chữ số.") }
            return
        }
        _uiState.update { it.copy(isVerifying = true, error = null) }
        viewModelScope.launch {
            verify(s.email, s.code)
                .onSuccess { _uiState.update { it.copy(isVerifying = false, verified = true) } }
                .onFailure { fail(it.toAuthError()) }
        }
    }

    fun onResend() {
        val s = _uiState.value
        if (s.isResending || s.resendSeconds > 0) return
        _uiState.update { it.copy(isResending = true, error = null) }
        viewModelScope.launch {
            resend(s.email)
                .onSuccess { timing ->
                    countdown.start(timing)
                    _uiState.update { it.copy(isResending = false, code = "", locked = false) }
                    tick()
                }
                .onFailure { e ->
                    val error = e.toAuthError()
                    if (error is AuthError.OtpResendTooSoon) countdown.restartResend(error.retryAfterSeconds ?: 60)
                    fail(error)
                    tick()
                }
        }
    }

    fun onVerifiedHandled() = _uiState.update { it.copy(verified = false) }

    fun onExitHandled() = _uiState.update { it.copy(exit = null) }

    private fun fail(error: AuthError) {
        _uiState.update {
            val base = it.copy(isVerifying = false, isResending = false)
            when (error) {
                AuthError.PendingRegistrationExpired, AuthError.InvalidTemple, AuthError.PhoneTaken -> base.copy(exit = error)
                is AuthError.OtpLocked -> base.copy(locked = true, error = error.userMessage())
                else -> base.copy(error = error.userMessage())
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

package com.example.innerlamp.ui.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.innerlamp.ui.screens.auth.toAuthError
import com.example.innerlamp.ui.screens.auth.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val error: String? = null,
    /** The backend answers after ~1.5 s, so a loading state is shown. / Backend trả lời sau ~1,5 giây. */
    val isLoading: Boolean = false,
    /** Code sent (for any email): the screen opens the reset screen once and clears it. */
    val sentTiming: CodeTiming? = null,
)

/** Forgot password step 1: request the emailed code. / Quên mật khẩu bước 1: xin mã qua email. */
class ForgotPasswordViewModel(
    initialEmail: String,
    private val requestReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState(email = initialEmail))
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun onSubmit() {
        val s = _uiState.value
        if (s.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            requestReset(s.email)
                .onSuccess { timing -> _uiState.update { it.copy(isLoading = false, sentTiming = timing) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toAuthError().userMessage()) } }
        }
    }

    fun onSentHandled() = _uiState.update { it.copy(sentTiming = null) }
}

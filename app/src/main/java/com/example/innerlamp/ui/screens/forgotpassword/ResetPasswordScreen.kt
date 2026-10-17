package com.example.innerlamp.ui.screens.forgotpassword

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.ui.components.IlAuthCta
import com.example.innerlamp.ui.components.IlAuthScaffold
import com.example.innerlamp.ui.components.IlCodeTimerRow
import com.example.innerlamp.ui.components.IlFieldError
import com.example.innerlamp.ui.components.IlInsetField
import com.example.innerlamp.ui.components.IlRevealToggle
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

/** Forgot password step 2: 6-digit code + new password. / Bước 2: mã 6 số + mật khẩu mới. */
@Composable
fun ResetPasswordScreen(
    email: String,
    timing: CodeTiming,
    onPasswordChanged: (email: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ResetPasswordViewModel(
                    email = email,
                    timing = timing,
                    requestReset = AuthModule.provideRequestPasswordResetUseCase(),
                    confirmReset = AuthModule.provideConfirmPasswordResetUseCase(),
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalIlColors.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.done) {
        if (state.done) {
            onPasswordChanged(state.email)
            viewModel.onDoneHandled()
        }
    }

    IlAuthScaffold(title = "Mật khẩu mới", onBack = onBack, modifier = modifier) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Đặt mật khẩu mới",
            style = TextStyle(fontFamily = Lora, fontSize = 28.sp, lineHeight = 34.sp, color = colors.ink),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Nếu email này có tài khoản, mã đã được gửi tới ${state.email}",
            style = TextStyle(fontFamily = BeVietnamPro, fontSize = 14.sp, color = colors.ink.copy(alpha = 0.52f)),
        )
        Spacer(Modifier.height(20.dp))
        IlInsetField(
            value = state.code,
            onValueChange = viewModel::onCodeChange,
            placeholder = "Mã 6 chữ số",
            isError = state.codeError != null,
            maxLength = 6,
            keyboardType = KeyboardType.NumberPassword,
        )
        state.codeError?.let { IlFieldError(it) }
        IlCodeTimerRow(
            expirySeconds = state.expirySeconds,
            resendSeconds = state.resendSeconds,
            onResend = viewModel::onResend,
            busy = state.isResending,
        )
        Spacer(Modifier.height(12.dp))
        IlInsetField(
            value = state.newPassword,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Mật khẩu mới (8–64 ký tự)",
            isError = state.passwordError != null,
            masked = !passwordVisible,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            trailingAction = {
                IlRevealToggle(revealed = passwordVisible, onToggle = { passwordVisible = !passwordVisible })
            },
        )
        state.passwordError?.let { IlFieldError(it) }
        Spacer(Modifier.height(14.dp))
        IlAuthCta(
            label = "Đổi mật khẩu",
            onClick = viewModel::onConfirm,
            busy = state.isSubmitting,
            dimmed = state.locked,
        )
    }
}

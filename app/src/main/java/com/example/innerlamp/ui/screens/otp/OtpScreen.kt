package com.example.innerlamp.ui.screens.otp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.ui.components.IlAuthCta
import com.example.innerlamp.ui.components.IlAuthScaffold
import com.example.innerlamp.ui.components.IlCodeTimerRow
import com.example.innerlamp.ui.components.IlFieldError
import com.example.innerlamp.ui.components.IlInsetField
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

/**
 * Registration code screen. / Màn nhập mã xác thực đăng ký.
 *
 * @param onVerified code confirmed and the session stored (go to Home). / Đã xác nhận, phiên đã lưu.
 * @param onBackToSignup "Đổi email" (`null`) or the flow was rejected (error to show on Signup). / "Đổi email" hoặc bị từ chối.
 */
@Composable
fun OtpScreen(
    email: String,
    timing: CodeTiming,
    onVerified: () -> Unit,
    onBackToSignup: (AuthError?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                OtpViewModel(
                    email = email,
                    timing = timing,
                    verify = AuthModule.provideVerifyRegistrationUseCase(),
                    resend = AuthModule.provideResendRegistrationCodeUseCase(),
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalIlColors.current

    LaunchedEffect(state.verified) {
        if (state.verified) {
            onVerified()
            viewModel.onVerifiedHandled()
        }
    }
    LaunchedEffect(state.exit) {
        state.exit?.let {
            onBackToSignup(it)
            viewModel.onExitHandled()
        }
    }

    IlAuthScaffold(title = "Xác thực email", onBack = { onBackToSignup(null) }, modifier = modifier) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Nhập mã xác thực",
            style = TextStyle(fontFamily = Lora, fontSize = 28.sp, lineHeight = 34.sp, color = colors.ink),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Đã gửi tới ${state.email}",
            style = TextStyle(fontFamily = BeVietnamPro, fontSize = 14.sp, color = colors.ink.copy(alpha = 0.52f)),
        )
        Spacer(Modifier.height(20.dp))
        IlInsetField(
            value = state.code,
            onValueChange = viewModel::onCodeChange,
            placeholder = "6 chữ số",
            isError = state.error != null,
            maxLength = 6,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
        )
        state.error?.let { IlFieldError(it) }
        IlCodeTimerRow(
            expirySeconds = state.expirySeconds,
            resendSeconds = state.resendSeconds,
            onResend = viewModel::onResend,
            busy = state.isResending,
        )
        Spacer(Modifier.height(14.dp))
        IlAuthCta(
            label = "Xác nhận",
            onClick = viewModel::onConfirm,
            busy = state.isVerifying,
            dimmed = state.locked,
        )
        Text(
            text = "Đổi email",
            modifier = Modifier
                .clickable { onBackToSignup(null) }
                .padding(vertical = 12.dp),
            style = TextStyle(fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = colors.link),
        )
    }
}

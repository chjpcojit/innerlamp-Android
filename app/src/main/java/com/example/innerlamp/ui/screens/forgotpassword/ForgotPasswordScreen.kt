package com.example.innerlamp.ui.screens.forgotpassword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.ui.components.IlAuthCta
import com.example.innerlamp.ui.components.IlAuthScaffold
import com.example.innerlamp.ui.components.IlFieldError
import com.example.innerlamp.ui.components.IlInsetField
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

/** Forgot password: email → "Gửi mã". / Quên mật khẩu: nhập email rồi "Gửi mã". */
@Composable
fun ForgotPasswordScreen(
    initialEmail: String,
    onCodeSent: (email: String, timing: CodeTiming) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ForgotPasswordViewModel(initialEmail, AuthModule.provideRequestPasswordResetUseCase())
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalIlColors.current

    LaunchedEffect(state.sentTiming) {
        state.sentTiming?.let {
            onCodeSent(state.email.trim(), it)
            viewModel.onSentHandled()
        }
    }

    IlAuthScaffold(title = "Quên mật khẩu", onBack = onBack, modifier = modifier) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Đặt lại mật khẩu",
            style = TextStyle(fontFamily = Lora, fontSize = 28.sp, lineHeight = 34.sp, color = colors.ink),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Nhập email của bạn, chúng tôi sẽ gửi mã để đặt mật khẩu mới.",
            style = TextStyle(fontFamily = BeVietnamPro, fontSize = 14.sp, color = colors.ink.copy(alpha = 0.52f)),
        )
        Spacer(Modifier.height(20.dp))
        IlInsetField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "phapdanh@email.com",
            isError = state.error != null,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        )
        state.error?.let { IlFieldError(it) }
        Spacer(Modifier.height(14.dp))
        IlAuthCta(label = "Gửi mã", onClick = viewModel::onSubmit, busy = state.isLoading)
        Text(
            text = "Trở lại đăng nhập",
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 14.dp),
            style = TextStyle(fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = colors.link),
        )
    }
}

package com.example.innerlamp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.ui.components.IlAuthCta
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlFieldError
import com.example.innerlamp.ui.components.IlInsetField
import com.example.innerlamp.ui.components.IlLogo
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlNotice
import com.example.innerlamp.ui.components.IlNoticeTone
import com.example.innerlamp.ui.components.IlOuterLabel
import com.example.innerlamp.ui.components.IlRevealToggle
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Màn hình Đăng nhập (login) — dựng theo màn "Đăng nhập" của
 * `Docs/innerlamp-project/project/InnerLamp Prototype.dc.html`.
 *
 * Bố cục: IL-NavBar "Đăng nhập" → logo 56 → "Kính chào bạn" → (thông báo hết phiên/đổi mật khẩu)
 * → EMAIL + mật khẩu (Hiện/Ẩn) → "Quên mật khẩu?" → (banner lỗi) → CTA "Đăng nhập";
 * đáy màn: "Tạo tài khoản mới" → "Vào xem trước, chưa đăng nhập".
 * Màn không giữ `NavController`: mọi điều hướng đi qua callback.
 *
 * @param onLoggedIn đăng nhập xong.
 * @param onForgotPassword bấm "Quên mật khẩu?".
 * @param onSignupClick bấm "Tạo tài khoản mới".
 * @param onPreviewClick bấm "Vào xem trước, chưa đăng nhập".
 *
 * @author innerlamp
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoggedIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    note: LoginNote? = null,
    prefillEmail: String? = null,
    viewModel: LoginViewModel = viewModel(
        factory = viewModelFactory { initializer { LoginViewModel(AuthModule.provideSignInUseCase()) } },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Thông báo đầu form + email điền sẵn đến từ route (hết phiên / vừa đổi mật khẩu).
    LaunchedEffect(note, prefillEmail) {
        viewModel.showNote(note)
        viewModel.prefillEmail(prefillEmail)
    }

    // Đăng nhập xong → điều hướng một lần rồi hạ cờ.
    LaunchedEffect(uiState.loggedIn) {
        if (uiState.loggedIn) {
            onLoggedIn()
            viewModel.onLoggedInHandled()
        }
    }

    LoginContent(
        state = uiState,
        modifier = modifier,
        onBack = onBack,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onLoginClick,
        onForgotPassword = onForgotPassword,
        onSignupClick = onSignupClick,
        onPreviewClick = onPreviewClick,
    )
}

/** Phần hiển thị thuần (không ViewModel) — dùng cho Preview các trạng thái. */
@Composable
private fun LoginContent(
    state: LoginUiState,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignupClick: () -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    // Bàn phím mở → ẩn khối đáy để ô nhập không bị đè.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scroll = rememberScrollState()

    IlBlurHeaderScaffold(
        modifier = modifier.fillMaxSize(),
        scrollOffset = { scroll.value },
        header = {
            Box(Modifier.padding(horizontal = 26.dp)) {
                IlNavBar(title = "Đăng nhập", showBack = true, onBack = onBack)
            }
        },
    ) { headerHeight ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.card2, colors.sunk)))
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(horizontal = 26.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll),
            ) {
                Spacer(Modifier.height(headerHeight + 22.dp))
                IlLogo(size = 56.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Kính chào bạn",
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 32.sp,
                        lineHeight = 37.sp,
                        color = colors.ink,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Đăng nhập để lưu hành trình tu tập của bạn",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colors.ink.copy(alpha = 0.52f),
                    ),
                )
                state.note?.let { note ->
                    Spacer(Modifier.height(18.dp))
                    IlNotice(
                        body = note.message,
                        tone = if (note.positive) IlNoticeTone.Green else IlNoticeTone.Gold,
                    )
                }

                Spacer(Modifier.height(20.dp))
                IlOuterLabel("Email")
                Spacer(Modifier.height(8.dp))
                IlInsetField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "phapdanh@email.com",
                    isError = state.emailError != null,
                    keyboardType = KeyboardType.Email,
                )
                state.emailError?.let { IlFieldError(it) }

                Spacer(Modifier.height(12.dp))
                IlInsetField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Mật khẩu",
                    isError = state.passwordError != null,
                    masked = !passwordVisible,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    trailingAction = {
                        IlRevealToggle(revealed = passwordVisible, onToggle = { passwordVisible = !passwordVisible })
                    },
                )
                state.passwordError?.let { IlFieldError(it) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        modifier = Modifier
                            .clickable(onClick = onForgotPassword)
                            .padding(vertical = 8.dp),
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.link,
                        ),
                    )
                }
                state.banner?.let { banner ->
                    Spacer(Modifier.height(6.dp))
                    IlNotice(
                        title = banner.title,
                        body = banner.body,
                        tone = IlNoticeTone.Error,
                        onRetry = if (banner.retryable) onSubmit else null,
                    )
                }
                Spacer(Modifier.height(14.dp))
                IlAuthCta(
                    label = "Đăng nhập",
                    onClick = onSubmit,
                    busy = state.isLoading,
                    dimmed = state.isLocked,
                )
                Spacer(Modifier.height(24.dp))
            }

            if (!imeVisible) {
                Column(modifier = Modifier.padding(bottom = 34.dp)) {
                    CreateAccountButton(onClick = onSignupClick)
                    Text(
                        text = "Vào xem trước, chưa đăng nhập",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable(onClick = onPreviewClick)
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.ink.copy(alpha = 0.45f),
                        ),
                    )
                }
            }
        }
    }
}

/** Nút pill 52dp nền card, viền vàng .5, chữ link 13sp — "Tạo tài khoản mới". */
@Composable
private fun CreateAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(colors.card, shape)
            .border(1.dp, IlGold.copy(alpha = 0.5f), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Tạo tài khoản mới",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colors.link,
            ),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Composable
private fun LoginContentPreview(state: LoginUiState) {
    LoginContent(
        state = state,
        onBack = {},
        onEmailChange = {},
        onPasswordChange = {},
        onSubmit = {},
        onForgotPassword = {},
        onSignupClick = {},
        onPreviewClick = {},
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        LoginContentPreview(LoginUiState())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreviewErrors() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        LoginContentPreview(
            LoginUiState(
                email = "phapdanh@",
                emailError = "Email chưa đúng định dạng, ví dụ: ten@email.com.",
                passwordError = "Vui lòng nhập mật khẩu.",
                note = LoginNote.SessionExpired,
                banner = LoginBanner.Network,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        LoginContentPreview(
            LoginUiState(
                email = "phapdanh@email.com",
                note = LoginNote.PasswordChanged,
                banner = LoginBanner.Locked("14:35"),
            ),
        )
    }
}

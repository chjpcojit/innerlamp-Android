package com.example.innerlamp.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.ui.components.IlCountryPicker
import com.example.innerlamp.ui.components.IlField
import com.example.innerlamp.ui.components.IlFieldTextAction
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlPhoneField
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.IlGoldBright2
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Màn hình Đăng nhập (login) — dựng lại từ prototype `Docs/design_handoff_innerlamp/`.
 *
 * Bố cục: IL-NavBar "Đăng nhập" → ngọn đèn + lời chào → SĐT (mã vùng) + mật khẩu ("Quên?")
 * → CTA "Đăng nhập" + "Gửi mã một lần qua SMS"; đáy màn ghim "hoặc" → "Tạo tài khoản mới"
 * → "Vào xem trước". Màn không giữ `NavController`: mọi điều hướng đi qua callback.
 *
 * @param onLoggedIn đăng nhập xong.
 * @param onRequestOtp người dùng chọn nhận mã một lần qua SMS (sang màn OTP).
 * @param onForgotPassword bấm "Quên?".
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
    onRequestOtp: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalIlColors.current
    var countryPickerOpen by remember { mutableStateOf(false) }

    // Đăng nhập xong → điều hướng một lần rồi hạ cờ.
    LaunchedEffect(uiState.loggedIn) {
        if (uiState.loggedIn) {
            onLoggedIn()
            viewModel.onLoggedInHandled()
        }
    }

    // Bàn phím mở → ẩn khối đáy để ô nhập không bị đè.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Box(modifier = modifier.fillMaxSize()) {
        val scroll = rememberScrollState()
        IlBlurHeaderScaffold(
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
                    LampBadge()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Kính chào bạn",
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            color = colors.ink,
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Đăng nhập để lưu hành trình tu tập của bạn",
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = colors.ink.copy(alpha = 0.52f),
                        ),
                    )
                    Spacer(Modifier.height(20.dp))

                    IlPhoneField(
                        value = uiState.phoneRaw,
                        onValueChange = viewModel::onPhoneChange,
                        country = PhoneCountries.byIso(uiState.selectedCountryIso),
                        onCountryClick = { countryPickerOpen = true },
                    )
                    Spacer(Modifier.height(12.dp))
                    IlField(
                        label = "Mật khẩu",
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Mật khẩu",
                        maskPassword = true,
                        trailingAction = {
                            IlFieldTextAction(label = "Quên?", onClick = onForgotPassword)
                        },
                    )
                    Spacer(Modifier.height(20.dp))
                    IlButton(
                        label = "Đăng nhập",
                        // Khi loading vẫn giữ enabled để nút hiện spinner thay vì kiểu disabled.
                        enabled = uiState.canSubmit || uiState.isLoading,
                        loading = uiState.isLoading,
                        loadingLabel = "Đang đăng nhập…",
                        onClick = viewModel::onLoginClick,
                    )
                    Spacer(Modifier.height(10.dp))
                    IlButton(
                        label = "Gửi mã một lần qua SMS",
                        variant = IlButtonVariant.Ghost,
                        enabled = uiState.canRequestOtp,
                        onClick = onRequestOtp,
                    )
                    Spacer(Modifier.height(24.dp))
                }

                if (!imeVisible) {
                    Column(
                        modifier = Modifier.padding(bottom = 34.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OrDivider()
                        Spacer(Modifier.height(14.dp))
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
                                fontSize = 12.5.sp,
                                color = colors.ink.copy(alpha = 0.45f),
                            ),
                        )
                    }
                }
            }
        }

        // Sheet "Chọn quốc gia": trượt từ đáy 280ms như prototype.
        AnimatedVisibility(
            visible = countryPickerOpen,
            enter = slideInVertically(tween(280, easing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f))) { it },
            exit = slideOutVertically(tween(220)) { it },
        ) {
            IlCountryPicker(
                selectedIso = uiState.selectedCountryIso,
                onSelect = {
                    viewModel.onCountrySelect(it.iso)
                    countryPickerOpen = false
                },
                onDismiss = { countryPickerOpen = false },
            )
        }
    }
}

/** Vòng tròn 56dp viền vàng .45 + quầng sáng + ngọn lửa 12×17 (đầu màn đăng nhập). */
@Composable
private fun LampBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .drawBehind {
                // radial-gradient(circle at 50% 74%, rgba(217,150,60,.24), transparent 66%)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(IlGoldBright.copy(alpha = 0.24f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.74f),
                        radius = size.width * 0.66f,
                    ),
                )
            }
            .border(1.dp, IlGold.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(
            Modifier
                .padding(bottom = 13.dp)
                .size(width = 12.dp, height = 17.dp),
        ) {
            // Quầng phát sáng quanh ngọn lửa (box-shadow 0 0 18px của prototype).
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(IlGoldBright.copy(alpha = 0.55f), Color.Transparent),
                    center = center,
                    radius = 18.dp.toPx(),
                ),
                radius = 18.dp.toPx(),
                center = center,
            )
            drawOval(
                brush = Brush.verticalGradient(listOf(IlGoldBright2, IlGoldBright)),
            )
        }
    }
}

/** Đường kẻ — "hoặc" — đường kẻ. */
@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(colors.ink.copy(alpha = 0.12f)))
        Text(
            text = "hoặc",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = colors.ink.copy(alpha = 0.38f),
            ),
        )
        Box(Modifier.weight(1f).height(1.dp).background(colors.ink.copy(alpha = 0.12f)))
    }
}

/** Nút pill nền card, viền vàng .5, chữ link — "Tạo tài khoản mới" (không thuộc biến thể IlButton). */
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
                fontSize = 14.5.sp,
                color = colors.link,
            ),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        LoginScreen(viewModel = LoginViewModel())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        LoginScreen(viewModel = LoginViewModel())
    }
}

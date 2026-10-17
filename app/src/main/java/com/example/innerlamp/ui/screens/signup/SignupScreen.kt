package com.example.innerlamp.ui.screens.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.model.EmailAddress
import com.example.innerlamp.ui.components.IlAuthCta
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlCountryPicker
import com.example.innerlamp.ui.components.IlFieldError
import com.example.innerlamp.ui.components.IlFieldHint
import com.example.innerlamp.ui.components.IlFieldPillAction
import com.example.innerlamp.ui.components.IlInsetField
import com.example.innerlamp.ui.components.IlLogo
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlNotice
import com.example.innerlamp.ui.components.IlNoticeTone
import com.example.innerlamp.ui.components.IlPasswordField
import com.example.innerlamp.ui.components.IlPhoneField
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Màn hình Đăng ký (Signup) — dựng theo màn "Đăng ký" của
 * `Docs/innerlamp-project/project/InnerLamp Prototype.dc.html`.
 *
 * Bố cục: IL-NavBar "Tạo tài khoản" (sticky) → vùng cuộn: logo 100, "Kết duyên cùng InnerLamp",
 * Họ và tên · Pháp danh (Gợi ý) · Nơi xuất gia · Số điện thoại · Email · Mật khẩu, checkbox điều khoản
 * → chân cố định: (banner lỗi) CTA "Đăng ký" + "Tôi đã có tài khoản".
 * Phủ trên cùng: sheet "Chọn quốc gia" và sheet "Điều khoản & quyền riêng tư".
 *
 * @param onLoginClick "Tôi đã có tài khoản" / link "Đăng nhập" khi số điện thoại đã đăng ký.
 * @param onOtpSent đã gửi mã xác thực tới email (sang màn nhập mã).
 * @param onAddTemple "Không thấy chùa của bạn? Thêm thông tin chùa".
 *
 * @author innerlamp
 */
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onOtpSent: (email: String, timing: CodeTiming) -> Unit = { _, _ -> },
    onAddTemple: () -> Unit = {},
    viewModel: SignupViewModel = viewModel(factory = signupViewModelFactory()),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Gửi mã xong → điều hướng một lần rồi hạ cờ.
    LaunchedEffect(uiState.otpSentTo) {
        val email = uiState.otpSentTo
        val timing = uiState.otpTiming
        if (email != null && timing != null) {
            onOtpSent(email, timing)
            viewModel.onOtpSentHandled()
        }
    }

    SignupContent(
        state = uiState,
        modifier = modifier,
        actions = SignupActions(
            onBack = onBack,
            onLoginClick = onLoginClick,
            onFullNameChange = viewModel::onFullNameChange,
            onDharmaNameChange = viewModel::onDharmaNameChange,
            onSuggestDharmaName = viewModel::onSuggestDharmaName,
            onOrdinationToggle = viewModel::onOrdinationToggle,
            onOrdinationQueryChange = viewModel::onOrdinationQueryChange,
            onOrdinationSelect = viewModel::onOrdinationSelect,
            onOrdinationRetry = viewModel::onOrdinationRetry,
            onAddTemple = {
                viewModel.onOrdinationDismiss()
                onAddTemple()
            },
            onCountrySelect = viewModel::onCountrySelect,
            onPhoneChange = viewModel::onPhoneChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onTermsToggle = viewModel::onTermsToggle,
            onTermsAgree = viewModel::onTermsAgree,
            onSubmit = viewModel::onSubmit,
        ),
    )
}

/** Gom callback của form để [SignupContent] không phải nhận hàng chục tham số. */
private class SignupActions(
    val onBack: () -> Unit = {},
    val onLoginClick: () -> Unit = {},
    val onFullNameChange: (String) -> Unit = {},
    val onDharmaNameChange: (String) -> Unit = {},
    val onSuggestDharmaName: () -> Unit = {},
    val onOrdinationToggle: () -> Unit = {},
    val onOrdinationQueryChange: (String) -> Unit = {},
    val onOrdinationSelect: (OrdinationChoice) -> Unit = {},
    val onOrdinationRetry: () -> Unit = {},
    val onAddTemple: () -> Unit = {},
    val onCountrySelect: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onTermsToggle: () -> Unit = {},
    val onTermsAgree: () -> Unit = {},
    val onSubmit: () -> Unit = {},
)

/** Phần hiển thị thuần (không ViewModel) — dùng cho Preview các trạng thái. */
@Composable
private fun SignupContent(
    state: SignupUiState,
    actions: SignupActions,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    var countryPickerOpen by rememberSaveable { mutableStateOf(false) }
    var legalTab by rememberSaveable { mutableStateOf<LegalTab?>(null) }
    val errors = state.errors
    // Bàn phím mở → ẩn chân CTA để ô nhập không bị đè.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scroll = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        IlBlurHeaderScaffold(
            scrollOffset = { scroll.value },
            header = {
                Box(Modifier.padding(horizontal = 26.dp)) {
                    IlNavBar(title = "Tạo tài khoản", showBack = true, onBack = actions.onBack)
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
                // ── Vùng cuộn ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scroll)
                        .padding(bottom = 8.dp),
                ) {
                    Spacer(Modifier.height(headerHeight + 24.dp))
                    IlLogo(size = 100.dp)
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "Kết duyên cùng\nInnerLamp",
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 28.sp,
                            lineHeight = 35.sp,
                            color = colors.ink,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Hành trình tu tập sẽ được lưu lại, dù bạn đổi máy.",
                        modifier = Modifier.widthIn(max = 290.dp),
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 22.4.sp,
                            color = colors.ink.copy(alpha = 0.52f),
                        ),
                    )

                    Column(
                        modifier = Modifier.padding(top = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Họ và tên (bắt buộc).
                        Column {
                            IlInsetField(
                                value = state.fullName,
                                onValueChange = actions.onFullNameChange,
                                label = "Họ và tên",
                                placeholder = "VD: Nguyễn Bình An",
                                isError = errors.fullName != null,
                                maxLength = 60,
                            )
                            errors.fullName?.let { IlFieldError(it) }
                        }

                        // Pháp danh (không bắt buộc) + "Gợi ý".
                        IlInsetField(
                            value = state.dharmaName,
                            onValueChange = actions.onDharmaNameChange,
                            label = "Pháp danh",
                            labelTrailing = "không bắt buộc",
                            placeholder = "VD: ${dharmaSuggestion(state.suggestionIndex)}",
                            maxLength = 40,
                            trailingAction = {
                                IlFieldPillAction(label = "Gợi ý", onClick = actions.onSuggestDharmaName)
                            },
                        )

                        OrdinationField(
                            state = state.ordination,
                            onToggle = actions.onOrdinationToggle,
                            onQueryChange = actions.onOrdinationQueryChange,
                            onSelect = actions.onOrdinationSelect,
                            onRetry = actions.onOrdinationRetry,
                            onAddTemple = actions.onAddTemple,
                        )

                        IlPhoneField(
                            value = state.phoneRaw,
                            onValueChange = actions.onPhoneChange,
                            country = state.country,
                            onCountryClick = { countryPickerOpen = true },
                            error = errors.phone,
                            errorAction = if (errors.phoneTaken) "Đăng nhập" to actions.onLoginClick else null,
                        )

                        // Email (bắt buộc) + ✓ khi hợp lệ.
                        Column {
                            IlInsetField(
                                value = state.email,
                                onValueChange = actions.onEmailChange,
                                label = "Email",
                                placeholder = "phapdanh@email.com",
                                isError = errors.email != null,
                                showCheck = EmailAddress.isValid(state.email),
                                keyboardType = KeyboardType.Email,
                            )
                            if (errors.email != null) {
                                IlFieldError(errors.email)
                            } else {
                                IlFieldHint("Dùng để đăng nhập và nhận mã xác thực")
                            }
                        }

                        IlPasswordField(
                            value = state.password,
                            onValueChange = actions.onPasswordChange,
                            error = errors.password,
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    TermsRow(
                        accepted = state.termsAccepted,
                        isError = errors.terms != null,
                        onToggle = actions.onTermsToggle,
                        onOpenTerms = { legalTab = LegalTab.Terms },
                        onOpenPrivacy = { legalTab = LegalTab.Privacy },
                    )
                    errors.terms?.let { IlFieldError(it, Modifier.padding(start = 15.dp)) }
                }

                // ── Chân cố định ────────────────────────────────────────────
                if (!imeVisible) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.ink.copy(alpha = 0.08f)),
                    )
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 34.dp)) {
                        state.banner?.let { banner ->
                            IlNotice(
                                title = banner.title,
                                body = banner.body,
                                tone = IlNoticeTone.Error,
                                onRetry = if (banner.retryable) actions.onSubmit else null,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                        IlAuthCta(label = "Đăng ký", onClick = actions.onSubmit, busy = state.isSubmitting)
                        Text(
                            text = "Tôi đã có tài khoản",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable(onClick = actions.onLoginClick)
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

        // Sheet "Chọn quốc gia": trượt từ đáy 280ms như prototype.
        AnimatedVisibility(
            visible = countryPickerOpen,
            enter = slideInVertically(tween(280, easing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f))) { it },
            exit = slideOutVertically(tween(220)) { it },
        ) {
            IlCountryPicker(
                selectedIso = state.selectedCountryIso,
                onSelect = {
                    actions.onCountrySelect(it.iso)
                    countryPickerOpen = false
                },
                onDismiss = { countryPickerOpen = false },
            )
        }

        // Sheet "Điều khoản & quyền riêng tư": nền mờ dần + sheet trượt lên 420ms.
        AnimatedVisibility(
            visible = legalTab != null,
            enter = fadeIn(tween(300)) +
                slideInVertically(tween(420, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))) { it },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(260)) { it },
        ) {
            // Giữ tab cuối khi đang chạy hiệu ứng đóng (legalTab đã về null).
            var shownTab by rememberSaveable { mutableStateOf(LegalTab.Terms) }
            legalTab?.let { shownTab = it }
            LegalSheet(
                tab = shownTab,
                onTabChange = { legalTab = it },
                onDismiss = { legalTab = null },
                onAgree = {
                    actions.onTermsAgree()
                    legalTab = null
                },
            )
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun SignupPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SignupContent(state = SignupUiState(), actions = SignupActions())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1300)
@Composable
private fun SignupPreviewErrors() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SignupContent(
            state = SignupUiState(
                phoneRaw = "0912 345 678",
                email = "phapdanh@",
                password = "abc",
                errors = SignupErrors(
                    fullName = "Vui lòng nhập họ và tên.",
                    phone = "Số điện thoại này đã được đăng ký.",
                    phoneTaken = true,
                    email = "Email chưa đúng định dạng, ví dụ: ten@email.com.",
                    password = "Mật khẩu cần ít nhất 8 ký tự.",
                    terms = "Vui lòng đồng ý Điều khoản và Chính sách để tiếp tục.",
                ),
                banner = SignupBanner.Network,
            ),
            actions = SignupActions(),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun SignupPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SignupContent(
            state = SignupUiState(
                fullName = "Nguyễn Bình An",
                dharmaName = "Diệu Tâm",
                ordination = OrdinationPickerState(selected = OrdinationChoice.None),
                phoneRaw = "0949 123 456",
                email = "phapdanh@email.com",
                password = "MatKhau@2026",
                termsAccepted = true,
                isSubmitting = true,
            ),
            actions = SignupActions(),
        )
    }
}

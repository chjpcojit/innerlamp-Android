package com.example.innerlamp.ui.screens.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.components.IlButtonVariant
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.ui.components.IlCountryPicker
import com.example.innerlamp.ui.components.IlField
import com.example.innerlamp.ui.components.IlFieldPillAction
import com.example.innerlamp.ui.components.IlNavBar
import com.example.innerlamp.ui.components.IlPasswordField
import com.example.innerlamp.ui.components.IlPhoneField
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGold
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.ThemeMode

/**
 * Màn hình Đăng ký (Signup) — dựng lại từ prototype `Docs/design_handoff_innerlamp/`.
 *
 * Luồng:
 *   IL-NavBar (sticky, title "Tạo tài khoản") → Form fields → CTA "Gửi mã xác thực"
 *   Chuyển sang màn OTP sau khi nhấn CTA.
 *
 * @author innerlamp
 */
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: SignupViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val model = viewModel.model
    val colors = LocalIlColors.current

    val isLoading = uiState is SignupUiState.Loading
    val canSubmit = uiState is SignupUiState.Success
    var countryPickerOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.card2, colors.sunk)))
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            IlNavBar(
                title = "Tạo tài khoản",
                showBack = true,
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Header nội dung: "Kết duyên cùng InnerLamp" ──────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Kết duyên cùng\nInnerLamp",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = Lora,
                            fontWeight = FontWeight.Normal,
                            fontSize = 29.sp,
                            lineHeight = 35.sp,
                            color = colors.ink,
                        ),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Hành trình tu tập sẽ được lưu lại, dù bạn đổi máy.",
                        modifier = Modifier.widthIn(max = 290.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = colors.ink.copy(alpha = 0.52f),
                        ),
                    )
                }

                // ── Dharma name field (nút "Gợi ý" bên phải) ─────────────────
                IlField(
                    label = "Pháp danh hoặc tên gọi",
                    value = model.dharmaName,
                    onValueChange = viewModel::onDharmaNameChange,
                    placeholder = "Dharma name",
                    counter = "${model.dharmaName.length}/24",
                    trailingAction = {
                        IlFieldPillAction(label = "Gợi ý", onClick = viewModel::onSuggestDharmaName)
                    },
                )

                // ── Nơi xuất gia (không bắt buộc) ────────────────────────────
                IlField(
                    label = "Nơi xuất gia",
                    value = model.placeOfOrigin,
                    onValueChange = viewModel::onPlaceOfOriginChange,
                    placeholder = "Tên chùa, thiền viện…",
                    hint = "Không bắt buộc",
                )

                // ── Số điện thoại (mã vùng + ô nhập, đúng prototype) ─────────
                IlPhoneField(
                    value = model.phoneRaw,
                    onValueChange = viewModel::onPhoneChange,
                    country = PhoneCountries.byIso(model.selectedCountryIso),
                    onCountryClick = { countryPickerOpen = true },
                )

                // ── Email ────────────────────────────────────────────────────
                IlField(
                    label = "Email",
                    value = model.emailRaw,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "phapdanh@email.com",
                    isError = !model.emailValid && model.emailRaw.isNotEmpty(),
                    error = "Vui lòng nhập email hợp lệ",
                    trailing = if (model.emailValid) "✓" else "",
                    isSuccess = model.emailValid,
                )

                // ── Mật khẩu (Hiện/Ẩn + thanh độ mạnh 3 đoạn) ────────────────
                IlPasswordField(
                    value = model.password,
                    onValueChange = viewModel::onPasswordChange,
                )

                // ── Checkbox điều khoản ──────────────────────────────────────
                TermsRow(
                    accepted = model.termsAccepted,
                    onToggle = viewModel::onTermsChange,
                )

                // ── CTA: Gửi mã xác thực ─────────────────────────────────────
                IlButton(
                    label = "Gửi mã xác thực",
                    variant = IlButtonVariant.Gold,
                    enabled = canSubmit,
                    loading = isLoading,
                    onClick = viewModel::onSendOtpClick,
                )

                // ── "Tôi đã có tài khoản" ────────────────────────────────────
                Text(
                    text = "Tôi đã có tài khoản",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLoginClick)
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

        // Sheet "Chọn quốc gia": trượt từ đáy 280ms như prototype.
        AnimatedVisibility(
            visible = countryPickerOpen,
            enter = slideInVertically(tween(280, easing = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f))) { it },
            exit = slideOutVertically(tween(220)) { it },
        ) {
            IlCountryPicker(
                selectedIso = model.selectedCountryIso,
                onSelect = {
                    viewModel.onCountrySelect(it.iso)
                    countryPickerOpen = false
                },
                onDismiss = { countryPickerOpen = false },
            )
        }
    }
}

/** Checkbox đồng ý điều khoản (vẽ tay để khớp thiết kế gold). */
@Composable
private fun TermsRow(
    accepted: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val boxShape = RoundedCornerShape(5.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle(!accepted) },
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(boxShape)
                .background(if (accepted) IlGold else colors.card)
                .border(1.dp, if (accepted) IlGold else colors.ink.copy(alpha = 0.30f), boxShape),
            contentAlignment = Alignment.Center,
        ) {
            if (accepted) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.card,
                    ),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Tôi đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư của InnerLamp",
            style = TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 11.5.sp,
                lineHeight = 17.sp,
                color = colors.ink.copy(alpha = 0.60f),
            ),
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupPreview() {
    InnerLampTheme(themeMode = ThemeMode.Light) {
        SignupScreen(
            viewModel = SignupViewModel(),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupPreviewDark() {
    InnerLampTheme(themeMode = ThemeMode.Dark) {
        SignupScreen(
            viewModel = SignupViewModel(),
        )
    }
}

package com.example.innerlamp.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.domain.model.SignupError
import com.example.innerlamp.domain.model.SignupUiModel
import com.example.innerlamp.domain.model.isComplete
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Đăng ký (signup).
 *
 * - Constructor-injected use cases (manual DI). Ở cấp UI này chúng ta dùng
 *   fake in-memory repository vì data layer chưa được wiring (xem `il-data-builder`).
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Validate form và bật/tắt CTA dựa trên: terms checkbox, email format, phone length.
 * - Map domain errors (SignupError) sang message tiếng Việt.
 *
 * @author innerlamp
 */
class SignupViewModel : ViewModel() {

    /** Stream state render màn hình. */
    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    /** Model form hiện tại. */
    private var _model = SignupUiModel()
    val model: SignupUiModel
        get() = _model

    /** Vị trí gợi ý pháp danh hiện tại; -1 để lần bấm đầu ra tên đầu danh sách. */
    private var suggestionIndex = -1

    // ── Form actions ──────────────────────────────────────────────────────

    /** Đặt giá trị dharma name. */
    fun onDharmaNameChange(value: String) {
        _model = _model.copy(dharmaName = value)
        emitUiState()
    }

    /** Gợi ý pháp danh kế tiếp (xoay vòng danh sách mẫu của prototype). */
    fun onSuggestDharmaName() {
        suggestionIndex = (suggestionIndex + 1) % DharmaNameSuggestions.size
        onDharmaNameChange(DharmaNameSuggestions[suggestionIndex])
    }

    /** Đặt nơi xuất gia. */
    fun onPlaceOfOriginChange(value: String) {
        _model = _model.copy(placeOfOrigin = value)
        emitUiState()
    }

    /** Đặt quốc gia chọn (ISO code). */
    fun onCountrySelect(isoCode: String) {
        _model = _model.copy(selectedCountryIso = isoCode)
        refreshPhoneOk()
        emitUiState()
    }

    /** Đặt giá trị số điện thoại. */
    fun onPhoneChange(value: String) {
        _model = _model.copy(phoneRaw = value)
        refreshPhoneOk()
        emitUiState()
    }

    /** Đặt giá trị email. */
    fun onEmailChange(value: String) {
        _model = _model.copy(emailRaw = value)
        val valid = value.isNotEmpty() && value.contains("@") && value.contains(".")
        _model = _model.copy(emailValid = valid)
        emitUiState()
    }

    /** Đặt giá trị mật khẩu. */
    fun onPasswordChange(value: String) {
        _model = _model.copy(password = value)
        emitUiState()
    }

    /** Đặt đồng ý/không đồng ý điều khoản. */
    fun onTermsChange(accepted: Boolean) {
        _model = _model.copy(termsAccepted = accepted)
        emitUiState()
    }

    /** Gửi mã OTP - gọi use case (fake hiện tại). */
    fun onSendOtpClick() {
        if (_uiState.value is SignupUiState.Loading) return
        _uiState.value = SignupUiState.Loading
        // TODO: Thay bằng real use case khi data layer wiring xong.
        // For now, simulate success after short delay.
        viewModelScope.launch {
            delay(600)
            _uiState.value = SignupUiState.Success(_model.copy(error = null))
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    /** Emit mới state UI từ model hiện tại. */
    private fun emitUiState() {
        val error: SignupError? = when {
            !_model.termsAccepted -> SignupError.TermsRequired
            !_model.emailValid -> SignupError.InvalidEmail
            !_model.phoneOk -> SignupError.ShortPhone
            _model.password.length < 8 -> SignupError.ShortPassword
            else -> null
        }

        val updated = _model.copy(error = error)
        _uiState.value = if (error != null) {
            SignupUiState.Error(error, updated)
        } else {
            SignupUiState.Success(updated)
        }
    }

    /** Đủ số theo độ dài của quốc gia đang chọn (đổi quốc gia cũng phải tính lại). */
    private fun refreshPhoneOk() {
        val country = PhoneCountries.byIso(_model.selectedCountryIso)
        _model = _model.copy(phoneOk = country.isComplete(_model.phoneRaw))
    }
}

/** Pháp danh mẫu cho nút "Gợi ý" (khớp `NAMES` trong prototype). */
private val DharmaNameSuggestions = listOf(
    "Diệu Tâm", "Tịnh An", "Minh Đăng", "Huệ Nhẫn", "Chân Như",
    "Thiện Duyên", "Giác Ngộ", "Liên Hoa", "Từ Quang", "An Lạc",
)

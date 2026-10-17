package com.example.innerlamp.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.EmailAddress
import com.example.innerlamp.domain.model.PasswordStrength
import com.example.innerlamp.domain.model.RegistrationForm
import com.example.innerlamp.domain.model.nationalDigits
import com.example.innerlamp.domain.usecase.SearchTempleOptionsUseCase
import com.example.innerlamp.domain.usecase.auth.RequestRegistrationCodeUseCase
import com.example.innerlamp.ui.screens.auth.toAuthError
import com.example.innerlamp.ui.screens.auth.userMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Đăng ký (signup).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Validate khi bấm "Đăng ký" (như prototype): họ tên bắt buộc, số điện thoại đủ số theo quốc gia,
 *   email đúng định dạng, mật khẩu 8–64 ký tự, đã tick điều khoản. Sửa ô nào thì xoá lỗi ô đó.
 * - Ô "Nơi xuất gia" tìm chùa qua [searchTemples] (debounce 300ms).
 * - Gửi form qua [RequestRegistrationCodeUseCase]; thành công → mã xác thực được gửi qua email.
 *
 * @author innerlamp
 */
class SignupViewModel(
    private val searchTemples: SearchTempleOptionsUseCase,
    private val requestCode: RequestRegistrationCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private var templeJob: Job? = null

    // ── Form ─────────────────────────────────────────────────────────────

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errors = it.errors.copy(fullName = null)) }
    }

    fun onDharmaNameChange(value: String) {
        _uiState.update { it.copy(dharmaName = value) }
    }

    /** "Gợi ý": lấy tên mẫu kế tiếp, điền vào ô và đổi luôn placeholder. */
    fun onSuggestDharmaName() {
        _uiState.update {
            val next = it.suggestionIndex + 1
            it.copy(suggestionIndex = next, dharmaName = dharmaSuggestion(next))
        }
    }

    fun onCountrySelect(isoCode: String) {
        _uiState.update { it.copy(selectedCountryIso = isoCode, errors = it.errors.copy(phone = null, phoneTaken = false)) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phoneRaw = value, errors = it.errors.copy(phone = null, phoneTaken = false)) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errors = it.errors.copy(email = null)) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errors = it.errors.copy(password = null)) }
    }

    fun onTermsToggle() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted, errors = it.errors.copy(terms = null)) }
    }

    /** "Tôi đồng ý" trong sheet Điều khoản & quyền riêng tư. */
    fun onTermsAgree() {
        _uiState.update { it.copy(termsAccepted = true, errors = it.errors.copy(terms = null)) }
    }

    // ── Nơi xuất gia ─────────────────────────────────────────────────────

    /** Mở/đóng dropdown; lần mở đầu tiên tải danh sách. */
    fun onOrdinationToggle() {
        val picker = _uiState.value.ordination
        updatePicker { it.copy(expanded = !it.expanded) }
        if (!picker.expanded && picker.results.isEmpty() && !picker.isLoading) searchTemplesNow(picker.query)
    }

    fun onOrdinationQueryChange(value: String) {
        updatePicker { it.copy(query = value) }
        templeJob?.cancel()
        templeJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadTemples(value)
        }
    }

    fun onOrdinationRetry() = searchTemplesNow(_uiState.value.ordination.query)

    fun onOrdinationSelect(choice: OrdinationChoice) {
        val hadQuery = _uiState.value.ordination.query.isNotBlank()
        updatePicker { it.copy(selected = choice, expanded = false, query = "") }
        // Đã xoá từ khoá → nạp lại danh sách mặc định cho lần mở sau.
        if (hadQuery) searchTemplesNow("")
    }

    /** Đóng dropdown (vd khi đi sang màn "Thêm thông tin chùa"). */
    fun onOrdinationDismiss() {
        updatePicker { it.copy(expanded = false) }
    }

    // ── Gửi ──────────────────────────────────────────────────────────────

    /** Bấm "Đăng ký" (cũng là "Thử lại" trên banner mất mạng). */
    fun onSubmit() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val digits = s.country.nationalDigits(s.phoneRaw).length
        val email = s.email.trim()
        val errors = SignupErrors(
            fullName = if (s.fullName.isBlank()) "Vui lòng nhập họ và tên." else null,
            phone = when {
                digits == s.country.len -> null
                digits == 0 -> "Vui lòng nhập số điện thoại."
                else -> "Số điện thoại cần đủ ${s.country.len} chữ số sau mã ${s.country.dial}."
            },
            email = when {
                EmailAddress.isValid(email) -> null
                email.isEmpty() -> "Vui lòng nhập email."
                else -> "Email chưa đúng định dạng, ví dụ: ten@email.com."
            },
            password = when {
                s.password.length < PasswordStrength.MIN_LENGTH -> "Mật khẩu cần ít nhất ${PasswordStrength.MIN_LENGTH} ký tự."
                s.password.length > PasswordStrength.MAX_LENGTH -> "Mật khẩu tối đa ${PasswordStrength.MAX_LENGTH} ký tự."
                else -> null
            },
            terms = if (s.termsAccepted) null else "Vui lòng đồng ý Điều khoản và Chính sách để tiếp tục.",
        )
        if (errors != SignupErrors()) {
            _uiState.update { it.copy(errors = errors, banner = null) }
            return
        }

        _uiState.update { it.copy(errors = SignupErrors(), banner = null, isSubmitting = true) }
        viewModelScope.launch {
            requestCode(s.toForm(email))
                .onSuccess { timing ->
                    _uiState.update { it.copy(isSubmitting = false, otpSentTo = email, otpTiming = timing) }
                }
                .onFailure { showError(it.toAuthError()) }
        }
    }

    /** Screen đã điều hướng sang OTP — hạ cờ để không điều hướng lặp lại. */
    fun onOtpSentHandled() {
        _uiState.update { it.copy(otpSentTo = null, otpTiming = null) }
    }

    // ── Private ──────────────────────────────────────────────────────────

    private fun updatePicker(transform: (OrdinationPickerState) -> OrdinationPickerState) {
        _uiState.update { it.copy(ordination = transform(it.ordination)) }
    }

    private fun searchTemplesNow(query: String) {
        templeJob?.cancel()
        templeJob = viewModelScope.launch { loadTemples(query) }
    }

    private suspend fun loadTemples(query: String) {
        updatePicker { it.copy(isLoading = true, loadFailed = false) }
        searchTemples(query)
            .onSuccess { page -> updatePicker { it.copy(isLoading = false, results = page.items) } }
            .onFailure { updatePicker { it.copy(isLoading = false, loadFailed = true, results = emptyList()) } }
    }

    private fun SignupUiState.toForm(trimmedEmail: String) = RegistrationForm(
        email = trimmedEmail,
        password = password,
        phoneCountry = country.iso,
        phoneNumber = country.nationalDigits(phoneRaw),
        fullName = fullName.trim(),
        dharmaName = dharmaName.trim().ifEmpty { null },
        ordinationTempleId = (ordination.selected as? OrdinationChoice.Temple)?.option?.id,
    )

    private fun showError(error: AuthError) {
        _uiState.update {
            val done = it.copy(isSubmitting = false)
            when (error) {
                AuthError.PhoneTaken -> done.copy(errors = it.errors.copy(phone = error.userMessage(), phoneTaken = true))
                AuthError.InvalidPhone -> done.copy(errors = it.errors.copy(phone = error.userMessage()))
                AuthError.InvalidEmail -> done.copy(errors = it.errors.copy(email = error.userMessage()))
                AuthError.InvalidPassword -> done.copy(errors = it.errors.copy(password = error.userMessage()))
                AuthError.InvalidFullName -> done.copy(errors = it.errors.copy(fullName = error.userMessage()))
                AuthError.NoConnection, AuthError.Timeout -> done.copy(banner = SignupBanner.Network)
                else -> done.copy(banner = SignupBanner("Chưa đăng ký được", error.userMessage(), retryable = false))
            }
        }
    }

    /**
     * Called when the code screen sends the user back (registration expired, phone taken, temple gone).
     * Gọi khi màn mã xác thực đưa người dùng về (hết hạn đăng ký, trùng số, chùa không còn).
     */
    fun onReturnedFromOtp(error: AuthError?) {
        if (error != null) showError(error)
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

/** Pháp danh mẫu cho placeholder và nút "Gợi ý" (khớp `NAMES` trong prototype). */
private val DharmaNameSuggestions = listOf(
    "Diệu Tâm", "Tịnh An", "Minh Đăng", "Huệ Nhẫn", "Chân Như",
    "Thiện Duyên", "Giác Ngộ", "Liên Hoa", "Từ Quang", "An Lạc",
)

/** Tên mẫu ở vị trí [index] (xoay vòng). */
internal fun dharmaSuggestion(index: Int): String = DharmaNameSuggestions[index % DharmaNameSuggestions.size]

/** Factory dùng chung cho màn Signup và màn mã xác thực (cùng một ViewModel theo back stack entry). */
fun signupViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory =
    viewModelFactory {
        initializer {
            SignupViewModel(
                searchTemples = SearchTempleOptionsUseCase(
                    com.example.innerlamp.di.TempleModule.provideTempleOptionRepository(),
                ),
                requestCode = com.example.innerlamp.di.AuthModule.provideRequestRegistrationCodeUseCase(),
            )
        }
    }

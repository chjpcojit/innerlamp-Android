package com.example.innerlamp.ui.screens.signup

import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.domain.model.PhoneCountry
import com.example.innerlamp.domain.model.TempleOption
import com.example.innerlamp.domain.model.isComplete

/**
 * State render cho màn hình Đăng ký (signup).
 *
 * Gộp giá trị form, lỗi từng ô (chỉ có sau khi bấm "Đăng ký"), banner lỗi gửi và trạng thái
 * ô chọn "Nơi xuất gia" vào một data class để form luôn hiển thị được dù đang gửi.
 *
 * @author innerlamp
 */
data class SignupUiState(
    val fullName: String = "",
    /** Pháp danh (không bắt buộc). */
    val dharmaName: String = "",
    /** Vị trí tên mẫu đang dùng làm placeholder "VD: …" (tăng mỗi lần bấm "Gợi ý"). */
    val suggestionIndex: Int = 0,
    val ordination: OrdinationPickerState = OrdinationPickerState(),
    val selectedCountryIso: String = "VN",
    val phoneRaw: String = "",
    val email: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val errors: SignupErrors = SignupErrors(),
    val banner: SignupBanner? = null,
    val isSubmitting: Boolean = false,
    /** Đã gửi mã xác thực tới email này — screen điều hướng sang OTP rồi hạ cờ. */
    val otpSentTo: String? = null,
) {
    val country: PhoneCountry get() = PhoneCountries.byIso(selectedCountryIso)

    /** Số điện thoại đã đủ số → hiện ✓ xanh. */
    val phoneOk: Boolean get() = country.isComplete(phoneRaw)
}

/** Lỗi từng ô của form đăng ký; `null` = không lỗi. */
data class SignupErrors(
    val fullName: String? = null,
    val phone: String? = null,
    /** Lỗi số điện thoại là "đã được đăng ký" → hiện thêm link "Đăng nhập". */
    val phoneTaken: Boolean = false,
    val email: String? = null,
    val password: String? = null,
    val terms: String? = null,
)

/** Banner lỗi phía trên CTA "Đăng ký". */
enum class SignupBanner(val title: String, val body: String, val retryable: Boolean) {
    Network(
        "Không kết nối được",
        "Kiểm tra mạng rồi thử lại. Thông tin bạn đã nhập vẫn được giữ nguyên.",
        retryable = true,
    ),
    RateLimited("Bạn đã thử quá nhiều lần", "Vui lòng thử lại sau 15 phút.", retryable = false),
}

/** Lựa chọn ở ô "Nơi xuất gia". */
sealed interface OrdinationChoice {
    val label: String

    /** Cư sĩ tu tại gia. */
    data object None : OrdinationChoice {
        override val label = "Chưa xuất gia"
    }

    data class Temple(val option: TempleOption) : OrdinationChoice {
        override val label: String get() = option.name
    }
}

/** Trạng thái ô chọn "Nơi xuất gia" (dropdown có ô tìm kiếm). */
data class OrdinationPickerState(
    val selected: OrdinationChoice? = null,
    val expanded: Boolean = false,
    val query: String = "",
    val results: List<TempleOption> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
)

package com.example.innerlamp.domain.model

/**
 * Các giá trị UI cho màn hình Đăng ký (signup).
 * Chứa trạng thái form + dữ liệu hiển thị — tuân theo clean-architecture:
 * domain layer chỉ sở hữu dữ liệu thuần túy, không import Android/x-compose.
 *
 * @author innerlamp
 */
data class SignupUiModel(
    /** Pháp danh hoặc tên gọi người dùng */
    val dharmaName: String = "",
    /** Nơi xuất gia (quê hương) */
    val placeOfOrigin: String = "",
    /** Quốc gia chọn (mã ISO) */
    val selectedCountryIso: String = "VN",
    /** Số điện thoại */
    val phoneRaw: String = "",
    /** Email */
    val emailRaw: String = "",
    /** Mật khẩu */
    val password: String = "",
    /** Đã đồng ý điều khoản */
    val termsAccepted: Boolean = false,
    /** Email đã có định dạng hợp lệ */
    val emailValid: Boolean = false,
    /** Số điện thoại đã đủ ký tự */
    val phoneOk: Boolean = false,
    /** Lỗi hiển thị cho field */
    val error: SignupError? = null,
)

/** Lỗi nghiệp vụ của form đăng ký. */
sealed class SignupError {
    /** Email không hợp lệ */
    data object InvalidEmail : SignupError()

    /** Số điện thoại quá ngắn */
    data object ShortPhone : SignupError()

    /** Mật khẩu quá ngắn */
    data object ShortPassword : SignupError()

    /** Chưa đồng ý điều khoản */
    data object TermsRequired : SignupError()
}

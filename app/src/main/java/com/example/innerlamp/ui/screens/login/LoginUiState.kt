package com.example.innerlamp.ui.screens.login

import com.example.innerlamp.domain.model.PasswordStrength
import com.example.innerlamp.domain.model.PhoneCountries
import com.example.innerlamp.domain.model.isComplete

/**
 * State render cho màn hình Đăng nhập (login).
 *
 * Gộp giá trị form và trạng thái gửi vào một data class: form luôn hiển thị được
 * dù đang [isLoading].
 *
 * @author innerlamp
 */
data class LoginUiState(
    /** Quốc gia chọn (mã ISO) cho ô số điện thoại. */
    val selectedCountryIso: String = "VN",
    /** Số điện thoại người dùng nhập (chưa chuẩn hoá). */
    val phoneRaw: String = "",
    /** Mật khẩu người dùng nhập. */
    val password: String = "",
    /** Đang gửi yêu cầu đăng nhập. */
    val isLoading: Boolean = false,
    /** Đăng nhập xong — screen điều hướng rồi ViewModel được reset qua `onLoggedInHandled`. */
    val loggedIn: Boolean = false,
) {
    /** Số điện thoại đã đủ số theo độ dài của quốc gia đang chọn. */
    val phoneOk: Boolean
        get() = PhoneCountries.byIso(selectedCountryIso).isComplete(phoneRaw)

    /** Mật khẩu đạt độ dài tối thiểu (≥ 8 ký tự theo prototype). */
    val passwordOk: Boolean
        get() = password.length >= PasswordStrength.MIN_LENGTH

    /** Bật nút "Đăng nhập": cần cả số điện thoại lẫn mật khẩu hợp lệ. */
    val canSubmit: Boolean
        get() = phoneOk && passwordOk && !isLoading

    /** Bật nút "Gửi mã một lần qua SMS": chỉ cần số điện thoại hợp lệ. */
    val canRequestOtp: Boolean
        get() = phoneOk && !isLoading
}

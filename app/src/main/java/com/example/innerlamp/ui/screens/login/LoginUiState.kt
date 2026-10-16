package com.example.innerlamp.ui.screens.login

/**
 * State render cho màn hình Đăng nhập (login) — đăng nhập bằng email + mật khẩu.
 *
 * Gộp giá trị form, lỗi từng ô và trạng thái gửi vào một data class: form luôn hiển thị được
 * dù đang [isLoading]. Lỗi chỉ xuất hiện sau khi bấm "Đăng nhập" (validate khi gửi, như prototype).
 *
 * @author innerlamp
 */
data class LoginUiState(
    /** Email người dùng nhập. */
    val email: String = "",
    /** Mật khẩu người dùng nhập. */
    val password: String = "",
    /** Lỗi ô email (trống / sai định dạng). */
    val emailError: String? = null,
    /** Lỗi dưới ô mật khẩu: trống, hoặc "Email hoặc mật khẩu không đúng." từ server. */
    val passwordError: String? = null,
    /** Thông báo đầu form khi bị đưa về màn đăng nhập (hết phiên / vừa đổi mật khẩu). */
    val note: LoginNote? = null,
    /** Banner lỗi phía trên CTA (mất mạng / tạm khoá). */
    val banner: LoginBanner? = null,
    /** Đang gửi yêu cầu đăng nhập. */
    val isLoading: Boolean = false,
    /** Đăng nhập xong — screen điều hướng rồi ViewModel được reset qua `onLoggedInHandled`. */
    val loggedIn: Boolean = false,
) {
    /** Đang bị tạm khoá do sai nhiều lần → CTA mờ .4 và bỏ qua thao tác. */
    val isLocked: Boolean get() = banner is LoginBanner.Locked
}

/** Thông báo đầu form. */
enum class LoginNote(val message: String, val positive: Boolean) {
    SessionExpired("Phiên đăng nhập đã hết, vui lòng đăng nhập lại.", positive = false),
    PasswordChanged("Đã đổi mật khẩu. Vui lòng đăng nhập lại bằng mật khẩu mới.", positive = true),
}

/** Banner lỗi phía trên CTA "Đăng nhập". */
sealed interface LoginBanner {
    val title: String
    val body: String

    /** Có link "Thử lại" hay không. */
    val retryable: Boolean

    /** Không kết nối được tới server. */
    data object Network : LoginBanner {
        override val title = "Không kết nối được"
        override val body = "Kiểm tra mạng rồi thử lại. Email và mật khẩu bạn nhập vẫn được giữ."
        override val retryable = true
    }

    /** Sai quá 5 lần — tạm khoá đến [untilLabel] (giờ:phút). */
    data class Locked(val untilLabel: String) : LoginBanner {
        override val title = "Tạm khóa đăng nhập"
        override val body = "Bạn đã nhập sai nhiều lần. Vui lòng thử lại sau $untilLabel."
        override val retryable = false
    }
}

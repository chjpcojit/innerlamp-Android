package com.example.innerlamp.ui.screens.auth

import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The [AuthError] carried by a failed `Result`, or [AuthError.Generic]. / [AuthError] trong `Result` lỗi, hoặc [AuthError.Generic]. */
internal fun Throwable.toAuthError(): AuthError = (this as? AuthException)?.error ?: AuthError.Generic

/**
 * App-owned wording for an auth error; the backend's message is never shown.
 * Câu chữ do app sở hữu cho lỗi auth; không bao giờ hiện message của backend.
 */
internal fun AuthError.userMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Email hoặc mật khẩu không đúng."
    is AuthError.LoginLocked -> "Bạn đã nhập sai nhiều lần. Vui lòng thử lại ${untilText(lockedUntil)}."
    is AuthError.RateLimited -> "Bạn đã thử quá nhiều lần. Vui lòng thử lại ${waitText(retryAfterSeconds)}."
    AuthError.InvalidEmail -> "Email chưa đúng định dạng, ví dụ: ten@email.com."
    AuthError.InvalidPassword -> "Mật khẩu cần từ 8 đến 64 ký tự."
    AuthError.InvalidPhone -> "Số điện thoại chưa hợp lệ."
    AuthError.InvalidFullName -> "Họ và tên chưa hợp lệ."
    AuthError.InvalidDharmaName -> "Pháp danh chưa hợp lệ."
    AuthError.InvalidTemple -> "Nơi xuất gia không còn trong danh sách, vui lòng chọn lại."
    AuthError.PhoneTaken -> "Số điện thoại này đã được đăng ký."
    is AuthError.RegistrationInProgress -> "Vui lòng chờ ${retryAfterSeconds ?: 60} giây rồi thử lại."
    is AuthError.OtpResendTooSoon -> "Vui lòng chờ ${retryAfterSeconds ?: 60} giây rồi gửi lại mã."
    is AuthError.OtpSendLimited -> "Đã gửi mã quá nhiều lần. Vui lòng thử lại ${untilText(lockedUntil)}."
    is AuthError.OtpLocked -> "Bạn đã nhập sai mã nhiều lần. Vui lòng thử lại ${untilText(lockedUntil)}."
    AuthError.OtpInvalid -> "Mã xác thực không đúng."
    AuthError.OtpExpired -> "Mã đã hết hạn. Bấm \"Gửi lại mã\" để nhận mã mới."
    AuthError.PendingRegistrationExpired -> "Thời gian đăng ký đã hết. Vui lòng đăng ký lại từ đầu."
    AuthError.SessionStoreFailed -> "Không lưu được phiên đăng nhập. Vui lòng thử đăng nhập lại."
    AuthError.NoConnection, AuthError.Timeout -> "Không kết nối được. Kiểm tra mạng rồi thử lại."
    AuthError.ServiceUnavailable, AuthError.Generic -> "Có lỗi xảy ra. Vui lòng thử lại sau."
}

private val HOUR_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** `locked_until` (ISO-8601) in the device's local time as `HH:mm`, or `null` if absent/unparsable. */
internal fun localTimeOf(lockedUntil: String?): String? = lockedUntil?.let {
    runCatching { Instant.parse(it).atZone(ZoneId.systemDefault()).format(HOUR_MINUTE) }.getOrNull()
}

private fun untilText(lockedUntil: String?): String =
    localTimeOf(lockedUntil)?.let { "sau $it" } ?: "sau ít phút"

private fun waitText(seconds: Int?): String = seconds?.let { "sau $it giây" } ?: "sau ít phút"

package com.example.innerlamp.domain.model

/**
 * Quy tắc định dạng email dùng chung cho Đăng nhập / Đăng ký / Quên mật khẩu
 * (khớp `EMAIL_RE` của prototype). Thuần Kotlin.
 */
object EmailAddress {
    private val PATTERN = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]{2,}$""")

    /** Email (đã bỏ khoảng trắng hai đầu) có đúng định dạng `ten@mien.tld` không. */
    fun isValid(raw: String): Boolean = PATTERN.matches(raw.trim())
}

package com.example.innerlamp.domain.model

/**
 * Registration data sent to `register/request-otp`. / Dữ liệu đăng ký gửi lên `register/request-otp`.
 *
 * @param phoneCountry ISO code of the chosen country. / Mã ISO của quốc gia đã chọn.
 * @param phoneNumber digits as typed, no `+`, no country code. / Số như đã gõ, không `+`, không mã quốc gia.
 * @param dharmaName `null` when empty. / `null` khi để trống.
 * @param ordinationTempleId `null` unless a temple from the list is chosen. / `null` nếu không chọn chùa trong danh sách.
 */
data class RegistrationForm(
    val email: String,
    val password: String,
    val phoneCountry: String,
    val phoneNumber: String,
    val fullName: String,
    val dharmaName: String? = null,
    val ordinationTempleId: String? = null,
)

/**
 * A temple the user proposes because it is not listed. / Chùa người dùng đề xuất vì chưa có trong danh sách.
 */
data class TempleProposal(
    val name: String,
    val provinceCode: String,
    val wardCode: String,
    val address: String,
)

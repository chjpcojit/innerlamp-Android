package com.example.innerlamp.ui.screens.signup

import com.example.innerlamp.domain.model.SignupError
import com.example.innerlamp.domain.model.SignupUiModel

/**
 * State render cho màn hình Đăng ký (signup).
 * Màn hình dùng sealed interface để capture các trạng thái: Idle, Loading, Success, Error.
 * State cấp form bên trong bên qua SignupUiModel.
 *
 * @author innerlamp
 */
sealed interface SignupUiState {

    /** Trạng thái ban đầu / idle — form sẵn sàng nhập liệu. */
    data object Idle : SignupUiState

    /** Đang gửi mã OTP / validations. */
    data object Loading : SignupUiState

    /** Thành công — đã gửi mã OTP, chờ user nhập OTP. */
    data class Success(val model: SignupUiModel) : SignupUiState

    /** Có lỗi — hiển thị thông báo cho người dùng. */
    data class Error(val error: SignupError, val model: SignupUiModel) : SignupUiState
}

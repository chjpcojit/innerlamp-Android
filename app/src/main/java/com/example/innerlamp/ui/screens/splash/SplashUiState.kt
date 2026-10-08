package com.example.innerlamp.ui.screens.splash

/**
 * Thời lượng hiển thị splash trước khi chuyển sang onboarding (ms).
 * Khớp `setTimeout(..., 3200)` trong prototype: sau 3.2 giây splash fade sang `onb`.
 */
const val SplashDurationMs = 3200L

/**
 * Trạng thái render của màn splash.
 *
 * - [finished] bật khi hết thời lượng hiển thị; màn hình lắng nghe và gọi
 *   callback `onFinished` để nhường quyền điều hướng cho NavHost/MainActivity.
 */
data class SplashUiState(
    val finished: Boolean = false,
)

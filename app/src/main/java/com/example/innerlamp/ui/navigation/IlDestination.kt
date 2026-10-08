package com.example.innerlamp.ui.navigation

/**
 * Base class (interface) cho mọi destination trong NavHost.
 *
 * Mỗi màn hình (route) implement interface này để khai báo transition chuyển
 * trang của riêng nó qua [transitions]. Không override → dùng crossfade, khớp với
 * global default của [AppNavHost].
 *
 * Route có tham số (`@Serializable data class`) vẫn implement được interface này;
 * chỉ có điều khi đó cần truyền [transitions] tường minh khi đăng ký graph
 * (không có instance singleton để đọc), hoặc dùng thẳng `composable<T>(...)`.
 */
interface IlDestination {
    val transitions: NavTransitionSpec
        get() = NavTransitionSpec.Crossfade
}

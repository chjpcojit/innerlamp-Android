package com.example.innerlamp.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Thời lượng crossfade mặc định (global). Khớp với prototype `tween(200)`. */
private const val DefaultCrossfadeDurationMs = 220

/**
 * NavHost chính của app. Global default transition là crossfade: mọi màn hình
 * chưa tự khai báo [IlDestination.transitions] đều chuyển bằng crossfade.
 *
 * Màn hình muốn custom thì override `transitions` và đăng ký qua [ilComposable].
 *
 * @param navController điều khiển back stack (tạo ở caller để có thể `navigate`).
 * @param startDestination route khởi đầu (thường là `SplashRoute`).
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: IlDestination,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(DefaultCrossfadeDurationMs)) },
        exitTransition = { fadeOut(tween(DefaultCrossfadeDurationMs)) },
        popEnterTransition = { fadeIn(tween(DefaultCrossfadeDurationMs)) },
        popExitTransition = { fadeOut(tween(DefaultCrossfadeDurationMs)) },
        builder = builder,
    )
}

/**
 * Đăng ký một màn hình vào graph, tự lấy transition từ `screen.transitions`.
 *
 * @param screen route singleton (`@Serializable object`). Vừa cung cấp kiểu
 *   (để `navigate(...)` type-safe) vừa cung cấp transition của màn hình.
 * @param content nội dung composable của màn hình (receiver `AnimatedContentScope`
 *   để dùng shared-element transition nếu cần).
 */
inline fun <reified T : IlDestination> NavGraphBuilder.ilComposable(
    screen: T,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(
        enterTransition = screen.transitions.enter,
        exitTransition = screen.transitions.exit,
        popEnterTransition = screen.transitions.popEnter,
        popExitTransition = screen.transitions.popExit,
        sizeTransform = screen.transitions.sizeTransform,
        content = content,
    )
}

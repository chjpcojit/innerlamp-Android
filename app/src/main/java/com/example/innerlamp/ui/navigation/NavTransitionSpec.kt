package com.example.innerlamp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

/**
 * Bộ 4 transition (enter/exit/popEnter/popExit) + [sizeTransform] cho một màn hình.
 *
 * Đây là "hợp đồng" animation chuyển trang. Mỗi lambda nhận
 * [AnimatedContentTransitionScope] nên có thể dựa vào `initialState`/`targetState`
 * (hai [NavBackStackEntry]) để quyết định animation — nghĩa là custom tuỳ ý chứ
 * không bị giới hạn vào preset. Trả `null` = không animate hướng đó.
 *
 * Dùng preset có sẵn trong [Companion] hoặc tự dựng `NavTransitionSpec(...)`:
 *
 * ```
 * val MyTransition = NavTransitionSpec(
 *     enter = { fadeIn(tween(300)) + slideIntoContainer(SlideDirection.Left, tween(300)) },
 *     exit = { fadeOut(tween(200)) },
 *     popEnter = { fadeIn(tween(300)) },
 *     popExit = { fadeOut(tween(200)) },
 * )
 * ```
 */
class NavTransitionSpec(
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?,
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?,
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?,
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?,
    val sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null,
) {
    companion object {
        /**
         * Crossfade mặc định. Mọi màn hình chưa tự khai báo transition (kể cả
         * global default của [AppNavHost]) đều dùng bộ này.
         */
        val Crossfade = NavTransitionSpec(
            enter = { fadeIn(tween(220)) },
            exit = { fadeOut(tween(220)) },
            popEnter = { fadeIn(tween(220)) },
            popExit = { fadeOut(tween(220)) },
        )

        /** Material "fade through": màn mới fade-in + nở nhẹ, màn cũ fade-out + thu nhẹ. */
        val FadeThrough = NavTransitionSpec(
            enter = {
                fadeIn(tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300))
            },
            exit = {
                fadeOut(tween(300)) + scaleOut(targetScale = 0.96f, animationSpec = tween(300))
            },
            popEnter = {
                fadeIn(tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300))
            },
            popExit = {
                fadeOut(tween(300)) + scaleOut(targetScale = 0.96f, animationSpec = tween(300))
            },
        )

        /** Trượt ngang: push vào từ cạnh phải, pop lùi ra cạnh phải. */
        val SlideHorizontal = NavTransitionSpec(
            enter = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300),
                )
            },
            exit = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300),
                    targetOffset = { fullOffset -> fullOffset / 3 },
                )
            },
            popEnter = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300),
                    initialOffset = { fullOffset -> fullOffset / 3 },
                )
            },
            popExit = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300),
                )
            },
        )

        /** Trượt dọc: push vào từ cạnh dưới, pop lùi xuống dưới. */
        val SlideVertical = NavTransitionSpec(
            enter = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(300),
                )
            },
            exit = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(300),
                    targetOffset = { fullOffset -> fullOffset / 3 },
                )
            },
            popEnter = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300),
                    initialOffset = { fullOffset -> fullOffset / 3 },
                )
            },
            popExit = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300),
                )
            },
        )

        /** Không animation — chuyển màn tức thì. */
        val None = NavTransitionSpec(
            enter = { EnterTransition.None },
            exit = { ExitTransition.None },
            popEnter = { EnterTransition.None },
            popExit = { ExitTransition.None },
        )
    }
}

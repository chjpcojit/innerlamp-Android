package com.example.innerlamp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/** Ba lựa chọn hiển thị ở màn "Ngôn ngữ & hiển thị" (`lang`). */
enum class ThemeMode { Light, Dark, System }

/**
 * Theme của InnerLamp: 3 chế độ Sáng/Tối/Theo máy, `dynamicColor = false`
 * (không để Material You đè bảng màu thương hiệu).
 *
 * Bộ token `--il-*` nằm trong [LocalIlColors]; [MaterialTheme.colorScheme] chỉ
 * ánh xạ tối thiểu để các component Material (ripple, text field...) không lệch.
 */
@Composable
fun InnerLampTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkIlColors else LightIlColors
    val medColors = if (dark) DarkIlMedColors else LightIlMedColors

    val scheme = if (dark) {
        darkColorScheme(
            primary = IlGold,
            onPrimary = Color(0xFF221E19),
            secondary = IlGreen,
            onSecondary = Color(0xFF221E19),
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.card,
            onSurface = colors.ink,
            surfaceVariant = colors.card2,
            onSurfaceVariant = colors.mid,
            error = IlDanger,
        )
    } else {
        lightColorScheme(
            primary = IlGold,
            onPrimary = Color(0xFF221E19),
            secondary = IlGreen,
            onSecondary = Color(0xFF221E19),
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.card,
            onSurface = colors.ink,
            surfaceVariant = colors.card2,
            onSurfaceVariant = colors.mid,
            error = IlDanger,
        )
    }

    CompositionLocalProvider(
        LocalIlColors provides colors,
        LocalIlMedColors provides medColors,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            shapes = IlShapes,
            content = content,
        )
    }
}

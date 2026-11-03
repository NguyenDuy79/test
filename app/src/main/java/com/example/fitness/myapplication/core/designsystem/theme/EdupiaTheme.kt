package com.example.fitness.myapplication.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Khớp 1-1 với Flutter:
 * ```dart
 * static const ColorScheme _colorScheme = ColorScheme(
 *     brightness: Brightness.light,
 *     primary: AppColors.primary500,
 *     onPrimary: AppColors.white,
 *     secondary: AppColors.brandingPink,
 *     onSecondary: AppColors.white,
 *     error: AppColors.semanticRedError,
 *     onError: AppColors.white,
 *     background: AppColors.white,
 *     onBackground: AppColors.neutral900,
 *     surface: AppColors.white,
 *     onSurface: AppColors.neutral900,
 * );
 * ```
 */
val AppLightColorScheme: ColorScheme = lightColorScheme(
    primary = AppColors.primary500,
    onPrimary = AppColors.white,
    primaryContainer = AppColors.primary500,
    onPrimaryContainer = AppColors.white,
    secondary = AppColors.brandingPink,
    onSecondary = AppColors.white,
    secondaryContainer = AppColors.brandingPink,
    onSecondaryContainer = AppColors.white,
    tertiary = AppColors.brandingPink,
    onTertiary = AppColors.white,
    background = AppColors.white,
    onBackground = AppColors.neutral900,
    surface = AppColors.white,
    onSurface = AppColors.neutral900,
    surfaceTint = AppColors.primary500,
    error = AppColors.semanticRedError,
    onError = AppColors.white,
)

val AppDarkColorScheme: ColorScheme = darkColorScheme(
    primary = AppColors.primary500,
    onPrimary = AppColors.white,
    primaryContainer = AppColors.primary700,
    onPrimaryContainer = AppColors.primary100,
    secondary = AppColors.brandingPink,
    onSecondary = AppColors.white,
    tertiary = AppColors.brandingPink,
    background = AppColors.neutral900,
    onBackground = AppColors.white,
    surface = AppColors.neutral800,
    onSurface = AppColors.white,
    error = AppColors.semanticRedError,
    onError = AppColors.white,
)

@Composable
fun EdupiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AppColors.white.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides AppColors,
        LocalEdupiaColors provides EdupiaColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EdupiaTypography,
            shapes = EdupiaShapes.MaterialShapes,
            content = content,
        )
    }
}

/** Alias cho [EdupiaTheme] */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    EdupiaTheme(darkTheme = darkTheme, content = content)
}

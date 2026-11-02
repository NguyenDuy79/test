package com.example.fitness.myapplication.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EdupiaColors.Primary,
    onPrimary = EdupiaColors.White,
    primaryContainer = EdupiaColors.Primary100,
    onPrimaryContainer = EdupiaColors.Primary800,
    secondary = EdupiaColors.BrandingPink,
    onSecondary = EdupiaColors.White,
    tertiary = EdupiaColors.BrandingLightBlue,
    background = EdupiaColors.BackgroundWhite,
    onBackground = EdupiaColors.Neutral900,
    surface = EdupiaColors.SurfaceCard,
    onSurface = EdupiaColors.Neutral900,
    error = EdupiaColors.SemanticError,
    onError = EdupiaColors.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = EdupiaColors.Primary,
    onPrimary = EdupiaColors.White,
    primaryContainer = EdupiaColors.Primary700,
    onPrimaryContainer = EdupiaColors.Primary100,
    secondary = EdupiaColors.BrandingPink,
    onSecondary = EdupiaColors.White,
    tertiary = EdupiaColors.BrandingLightBlue,
    background = EdupiaColors.Neutral900,
    onBackground = EdupiaColors.White,
    surface = EdupiaColors.Neutral800,
    onSurface = EdupiaColors.White,
    error = EdupiaColors.SemanticError,
    onError = EdupiaColors.White,
)

@Composable
fun EdupiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = EdupiaColors.White.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalEdupiaColors provides EdupiaColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EdupiaTypography,
            shapes = EdupiaShapes.MaterialShapes,
            content = content
        )
    }
}

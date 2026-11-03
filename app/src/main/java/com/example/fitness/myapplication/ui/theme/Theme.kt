package com.example.fitness.myapplication.ui.theme

import androidx.compose.runtime.Composable
import com.example.fitness.myapplication.core.designsystem.theme.AppDarkColorScheme
import com.example.fitness.myapplication.core.designsystem.theme.AppLightColorScheme
import com.example.fitness.myapplication.core.designsystem.theme.AppTheme as CoreAppTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme

val LightColorScheme = AppLightColorScheme
val DarkColorScheme = AppDarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    EdupiaTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CoreAppTheme(darkTheme = darkTheme, content = content)
}

package com.example.fitness.myapplication.ui.theme

import androidx.compose.runtime.Composable
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    EdupiaTheme(darkTheme = darkTheme, content = content)
}

package com.example.fitness.myapplication.feature.splash

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class SplashState(
    val isLoading: Boolean = true,
) : UiState

sealed interface SplashIntent : UiIntent {
    object CheckAuthStatus : SplashIntent
}

sealed interface SplashEffect : UiEffect {
    object NavigateToHome : SplashEffect
    object NavigateToOnboarding : SplashEffect
}

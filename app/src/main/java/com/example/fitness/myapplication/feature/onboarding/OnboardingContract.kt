package com.example.fitness.myapplication.feature.onboarding

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class OnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val isLoading: Boolean = false,
) : UiState

sealed interface OnboardingIntent : UiIntent {
    data class PageChanged(val page: Int) : OnboardingIntent
    object NextPage : OnboardingIntent
    object Skip : OnboardingIntent
    object CompleteOnboarding : OnboardingIntent
}

sealed interface OnboardingEffect : UiEffect {
    object NavigateToLogin : OnboardingEffect
    object NavigateToSignUp : OnboardingEffect
}

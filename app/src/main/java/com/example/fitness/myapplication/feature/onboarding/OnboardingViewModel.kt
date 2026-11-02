package com.example.fitness.myapplication.feature.onboarding

import com.example.fitness.myapplication.core.base.BaseViewModel

class OnboardingViewModel : BaseViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(
    initialState = OnboardingState()
) {

    override suspend fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageChanged -> {
                setState { copy(currentPage = intent.page) }
            }
            is OnboardingIntent.NextPage -> {
                // TODO: Logic chuyển trang kế tiếp
            }
            is OnboardingIntent.Skip -> {
                sendEffect(OnboardingEffect.NavigateToLogin)
            }
            is OnboardingIntent.CompleteOnboarding -> {
                sendEffect(OnboardingEffect.NavigateToLogin)
            }
        }
    }
}

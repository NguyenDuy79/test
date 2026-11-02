package com.example.fitness.myapplication.feature.onboarding

import com.example.fitness.myapplication.core.base.BaseViewModel

class OnboardingViewModel : BaseViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(
    initialState = OnboardingState(),
) {

    override suspend fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageChanged -> {
                setState { copy(currentPage = intent.page) }
            }
            is OnboardingIntent.NextPage -> {
                val nextPage = currentState.currentPage + 1
                if (nextPage < currentState.pages.size) {
                    setState { copy(currentPage = nextPage) }
                    sendEffect(OnboardingEffect.ScrollToPage(nextPage))
                } else {
                    sendEffect(OnboardingEffect.NavigateToLogin)
                }
            }
            is OnboardingIntent.Skip -> {
                sendEffect(OnboardingEffect.NavigateToLogin)
            }
            is OnboardingIntent.NavigateToLogin -> {
                sendEffect(OnboardingEffect.NavigateToLogin)
            }
            is OnboardingIntent.NavigateToSignUp -> {
                sendEffect(OnboardingEffect.NavigateToSignUp)
            }
        }
    }
}

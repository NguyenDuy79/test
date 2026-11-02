package com.example.fitness.myapplication.feature.onboarding

import androidx.annotation.DrawableRes
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

data class OnboardingPageItem(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
)

data class OnboardingState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPageItem> = listOf(
        OnboardingPageItem(
            title = "Học Tiếng Anh Với AI",
            description = "Luyện giao tiếp phản xạ 1:1 cùng giáo viên AI Edupia mọi lúc mọi nơi.",
            imageRes = R.drawable.image_login,
        ),
        OnboardingPageItem(
            title = "Lộ Trình Chuẩn Quốc Tế",
            description = "Chương trình học bám sát khung tham chiếu Châu Âu CEFR và bộ GD&ĐT.",
            imageRes = R.drawable.logo_splash,
        ),
        OnboardingPageItem(
            title = "Học Mà Chơi - Chơi Mà Học",
            description = "Hệ thống bài giảng tương tác và trò chơi sinh động giúp bé say mê học tập.",
            imageRes = R.drawable.bg_onboarding,
        ),
    ),
) : UiState {
    val isLastPage: Boolean get() = currentPage == pages.size - 1
    val isFirstPage: Boolean get() = currentPage == 0
}

sealed interface OnboardingIntent : UiIntent {
    data class PageChanged(val page: Int) : OnboardingIntent
    object NextPage : OnboardingIntent
    object Skip : OnboardingIntent
    object NavigateToLogin : OnboardingIntent
    object NavigateToSignUp : OnboardingIntent
}

sealed interface OnboardingEffect : UiEffect {
    data class ScrollToPage(val page: Int) : OnboardingEffect
    object NavigateToLogin : OnboardingEffect
    object NavigateToSignUp : OnboardingEffect
}

package com.example.fitness.myapplication.feature.home

import com.example.fitness.myapplication.core.base.UiEffect
import com.example.fitness.myapplication.core.base.UiIntent
import com.example.fitness.myapplication.core.base.UiState

enum class HomeTab(val title: String) {
    HOME("Trang chủ"),
    CLASS("Lớp học"),
    PRACTICE("Luyện tập"),
    AI_TALK("AI Talk"),
    PROFILE("Tài khoản")
}

data class HomeState(
    val selectedTab: HomeTab = HomeTab.HOME,
    val profileName: String = "",
    val phone: String = "",
    val profileCount: Int = 0,
    val activeLicenseCount: Int = 0,
    val isLoadingLicenses: Boolean = false,
) : UiState

sealed interface HomeIntent : UiIntent {
    data class SelectTab(val tab: HomeTab) : HomeIntent
    object Refresh : HomeIntent
    object SignOut : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class ShowToast(val message: String) : HomeEffect
}
